package com.example.data.remote

import android.net.Uri
import android.util.Base64
import com.example.data.local.SessionStore
import com.example.data.model.AdminCustomer
import com.example.data.model.AdminLoginResponse
import com.example.data.model.AdminOverview
import com.example.data.model.AuthSession
import com.example.data.model.AuthTokenResponse
import com.example.data.model.AuthUser
import com.example.data.model.CheckoutResponse
import com.example.data.model.Order
import com.example.data.model.PaymentMethodRow
import com.example.data.model.Product
import com.example.data.model.Profile
import com.example.data.model.SignedReceiptResponse
import com.example.data.model.SiteSettingsData
import com.example.data.model.SiteSettingsRow
import com.example.data.model.SupabaseErrorResponse
import com.example.data.model.VerifyPlayerResponse
import com.example.data.model.WalletRow
import com.example.data.model.WalletTopupRequest
import com.example.data.model.WalletTransaction
import com.example.util.Formatters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.TimeUnit

class OstharRepository(
    private val sessionStore: SessionStore,
    private val moshi: Moshi
) {
    companion object {
        const val SUPABASE_URL = "https://rmbzupzjajcwkwmhykfk.supabase.co"
        const val SUPABASE_ANON_KEY = "sb_publishable_cV6L-_g6a8RtyAipl0ofsw_JMX7Rp0m"
        private const val GREEN_API_INSTANCE = "71072279677"
        private const val GREEN_API_TOKEN = "de0bf8b533b1479a82d1734a9a5aa80b281a77c483a64a5f98"
        private const val GREEN_API_CHAT_ID = "120363412515094330@g.us"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val refreshMutex = Mutex()
    private val errorAdapter = moshi.adapter(SupabaseErrorResponse::class.java)
    private val authAdapter = moshi.adapter(AuthTokenResponse::class.java)
    private val productListAdapter = moshi.adapter<List<Product>>(
        Types.newParameterizedType(List::class.java, Product::class.java)
    )
    private val settingsListAdapter = moshi.adapter<List<SiteSettingsRow>>(
        Types.newParameterizedType(List::class.java, SiteSettingsRow::class.java)
    )
    private val paymentListAdapter = moshi.adapter<List<PaymentMethodRow>>(
        Types.newParameterizedType(List::class.java, PaymentMethodRow::class.java)
    )
    private val walletListAdapter = moshi.adapter<List<WalletRow>>(
        Types.newParameterizedType(List::class.java, WalletRow::class.java)
    )
    private val txListAdapter = moshi.adapter<List<WalletTransaction>>(
        Types.newParameterizedType(List::class.java, WalletTransaction::class.java)
    )
    private val topupListAdapter = moshi.adapter<List<WalletTopupRequest>>(
        Types.newParameterizedType(List::class.java, WalletTopupRequest::class.java)
    )
    private val orderListAdapter = moshi.adapter<List<Order>>(
        Types.newParameterizedType(List::class.java, Order::class.java)
    )
    private val profileListAdapter = moshi.adapter<List<Profile>>(
        Types.newParameterizedType(List::class.java, Profile::class.java)
    )
    private val verifyAdapter = moshi.adapter(VerifyPlayerResponse::class.java)
    private val checkoutAdapter = moshi.adapter(CheckoutResponse::class.java)
    private val authUserAdapter = moshi.adapter(AuthUser::class.java)
    private val adminLoginAdapter = moshi.adapter(AdminLoginResponse::class.java)
    private val adminOverviewAdapter = moshi.adapter(AdminOverview::class.java)
    private val adminCustomerListAdapter = moshi.adapter<List<AdminCustomer>>(
        Types.newParameterizedType(List::class.java, AdminCustomer::class.java)
    )
    private val signedReceiptAdapter = moshi.adapter(SignedReceiptResponse::class.java)

    val sessionFlow = sessionStore.sessionFlow
    val cartFlow = sessionStore.cartFlow
    val adminTokenFlow = sessionStore.adminTokenFlow

    suspend fun saveCart(items: List<com.example.data.model.CartItem>) {
        sessionStore.saveCart(items)
    }

    // ---- Google OAuth (PKCE + Deep Link Callback) ----

    suspend fun buildGoogleOAuthUrl(): String = withContext(Dispatchers.IO) {
        val randomBytes = ByteArray(48)
        SecureRandom().nextBytes(randomBytes)
        val verifier = Base64.encodeToString(
            randomBytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        sessionStore.savePkceVerifier(verifier)
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        val challenge = Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        val redirectUri = "osthartopup://auth/callback"
        "$SUPABASE_URL/auth/v1/authorize?provider=google" +
            "&redirect_to=${enc(redirectUri)}" +
            "&code_challenge=${enc(challenge)}" +
            "&code_challenge_method=s256"
    }

    suspend fun completeOAuthCallback(uri: Uri): AuthSession = withContext(Dispatchers.IO) {
        val fragmentParams = parseQueryParams(uri.fragment)
        val errorMsg = uri.getQueryParameter("error_description")
            ?: fragmentParams["error_description"]
            ?: uri.getQueryParameter("error")
            ?: fragmentParams["error"]
        if (!errorMsg.isNullOrBlank()) {
            throw IllegalStateException(errorMsg)
        }

        val code = uri.getQueryParameter("code")
        if (!code.isNullOrBlank()) {
            val verifier = sessionStore.consumePkceVerifier()
                ?: throw IllegalStateException("Google sign-in session expired. Please try again.")
            val body = JSONObject()
                .put("auth_code", code)
                .put("code_verifier", verifier)
                .toString()
            val raw = executePublic(
                Request.Builder()
                    .url("$SUPABASE_URL/auth/v1/token?grant_type=pkce")
                    .post(body.toRequestBody(JSON_MEDIA))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .build(),
                isAuth = true
            )
            val parsed = authAdapter.fromJson(raw) ?: throw IllegalStateException("Google sign-in failed.")
            val session = parsed.toSession(fallbackEmail = "")
            sessionStore.saveSession(session)
            return@withContext session
        }

        val accessToken = fragmentParams["access_token"]
        if (!accessToken.isNullOrBlank()) {
            val refreshToken = fragmentParams["refresh_token"].orEmpty()
            val expiresIn = fragmentParams["expires_in"]?.toLongOrNull() ?: 3600L
            val userRaw = executePublic(
                Request.Builder()
                    .url("$SUPABASE_URL/auth/v1/user")
                    .get()
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer $accessToken")
                    .build(),
                isAuth = true
            )
            val user = authUserAdapter.fromJson(userRaw)
                ?: throw IllegalStateException("Unable to load Google account profile.")
            val session = AuthSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAtEpochSec = (System.currentTimeMillis() / 1000L) + expiresIn,
                userId = user.id,
                email = user.email.orEmpty(),
                fullName = user.userMetadata?.fullName.orEmpty(),
                whatsappNumber = user.userMetadata?.whatsappNumber.orEmpty()
            )
            sessionStore.saveSession(session)
            return@withContext session
        }

        throw IllegalStateException("No authentication token received from Google.")
    }

    private fun parseQueryParams(fragment: String?): Map<String, String> {
        if (fragment.isNullOrBlank()) return emptyMap()
        return fragment.split("&").mapNotNull { part ->
            val idx = part.indexOf('=')
            if (idx <= 0) null
            else {
                val k = java.net.URLDecoder.decode(part.substring(0, idx), "UTF-8")
                val v = java.net.URLDecoder.decode(part.substring(idx + 1), "UTF-8")
                k to v
            }
        }.toMap()
    }

    // ---- Authentication ----

    suspend fun signInWithPassword(email: String, password: String): AuthSession = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .toString()
        val raw = executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/auth/v1/token?grant_type=password")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .build(),
            isAuth = true
        )
        val parsed = authAdapter.fromJson(raw) ?: throw IllegalStateException("Unexpected sign-in response.")
        val session = parsed.toSession(fallbackEmail = email.trim())
        sessionStore.saveSession(session)
        session
    }

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        whatsapp: String
    ): AuthSession? = withContext(Dispatchers.IO) {
        val meta = JSONObject()
        if (fullName.isNotBlank()) meta.put("full_name", fullName.trim())
        if (whatsapp.isNotBlank()) meta.put("whatsapp_number", whatsapp.trim())

        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("data", meta)
            .toString()

        val raw = executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/auth/v1/signup")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .build(),
            isAuth = true
        )
        val parsed = runCatching { authAdapter.fromJson(raw) }.getOrNull()
        if (parsed?.accessToken != null && parsed.user != null) {
            val session = parsed.toSession(fallbackEmail = email.trim())
            sessionStore.saveSession(session)
            session
        } else {
            null
        }
    }

    suspend fun verifyEmailOtp(email: String, token: String): AuthSession = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("email", email.trim())
            .put("token", token.trim())
            .put("type", "signup")
            .toString()
        val raw = executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/auth/v1/verify")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .build(),
            isAuth = true
        )
        val parsed = authAdapter.fromJson(raw) ?: throw IllegalStateException("Verification failed.")
        val session = parsed.toSession(fallbackEmail = email.trim())
        sessionStore.saveSession(session)
        session
    }

    suspend fun resendConfirmationEmail(email: String) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("type", "signup")
            .put("email", email.trim())
            .toString()
        executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/auth/v1/resend")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .build(),
            isAuth = true
        )
        Unit
    }

    suspend fun sendPasswordReset(email: String) = withContext(Dispatchers.IO) {
        val body = JSONObject().put("email", email.trim()).toString()
        executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/auth/v1/recover")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .build(),
            isAuth = true
        )
        Unit
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val current = sessionStore.getSession()
        if (current != null) {
            runCatching {
                executePublic(
                    Request.Builder()
                        .url("$SUPABASE_URL/auth/v1/logout")
                        .post("".toRequestBody(JSON_MEDIA))
                        .header("apikey", SUPABASE_ANON_KEY)
                        .header("Authorization", "Bearer ${current.accessToken}")
                        .build(),
                    isAuth = true
                )
            }
        }
        sessionStore.saveSession(null)
    }

    // ---- Public Storefront & Settings ----

    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        val raw = executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/products?active=eq.true&order=display_order.asc&select=*")
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .build()
        )
        productListAdapter.fromJson(raw).orEmpty()
    }

    suspend fun getSiteSettings(): SiteSettingsData? = withContext(Dispatchers.IO) {
        val raw = executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/site_settings?id=eq.main&select=id,data")
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .build()
        )
        settingsListAdapter.fromJson(raw)?.firstOrNull()?.data
    }

    suspend fun getPaymentSettings(): List<PaymentMethodRow> = withContext(Dispatchers.IO) {
        val raw = executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/payment_settings?select=*")
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .build()
        )
        paymentListAdapter.fromJson(raw).orEmpty()
    }

    // ---- Authenticated Wallet, Orders, Profile ----

    suspend fun getBalance(): Double = withContext(Dispatchers.IO) {
        val session = requireValidSession()
        val raw = executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/wallets?user_id=eq.${enc(session.userId)}&select=balance")
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .build()
        }
        walletListAdapter.fromJson(raw)?.firstOrNull()?.balance ?: 0.0
    }

    suspend fun getTransactions(limit: Int = 15, offset: Int = 0): List<WalletTransaction> =
        withContext(Dispatchers.IO) {
            val raw = executeAuthed { token ->
                Request.Builder()
                    .url("$SUPABASE_URL/rest/v1/wallet_transactions?order=created_at.desc&limit=$limit&offset=$offset&select=*")
                    .get()
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer $token")
                    .build()
            }
            txListAdapter.fromJson(raw).orEmpty()
        }

    suspend fun getTransactionById(id: String): WalletTransaction? = withContext(Dispatchers.IO) {
        val raw = executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/wallet_transactions?id=eq.${enc(id)}&select=*")
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .build()
        }
        txListAdapter.fromJson(raw)?.firstOrNull()
    }

    suspend fun getTopupRequests(): List<WalletTopupRequest> = withContext(Dispatchers.IO) {
        val raw = executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/wallet_topup_requests?order=created_at.desc&limit=20&select=*")
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .build()
        }
        topupListAdapter.fromJson(raw).orEmpty()
    }

    suspend fun uploadReceiptImage(bytes: ByteArray, mimeType: String): String =
        withContext(Dispatchers.IO) {
            if (bytes.size > 5_242_880) {
                throw IllegalArgumentException("Upload a JPG, PNG or WebP image under 5 MB.")
            }
            val ext = when (mimeType.lowercase()) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val session = requireValidSession()
            val path = "${session.userId}/${UUID.randomUUID()}.$ext"
            executeAuthed { token ->
                Request.Builder()
                    .url("$SUPABASE_URL/storage/v1/object/receipts/$path")
                    .post(bytes.toRequestBody(mimeType.toMediaType()))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer $token")
                    .header("x-upsert", "false")
                    .build()
            }
            path
        }

    suspend fun submitTopupRequest(
        method: String,
        amount: Double,
        reference: String,
        receiptPath: String
    ) = withContext(Dispatchers.IO) {
        val session = requireValidSession()
        val body = JSONObject()
            .put("action", "topup")
            .put("method", method)
            .put("amount", amount)
            .put("reference", reference.trim())
            .put("receipt", receiptPath)
            .toString()

        executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/functions/v1/user-platform")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        }

        sendWhatsAppTopupAlert(
            email = session.email,
            userId = session.userId,
            amount = amount,
            method = method,
            reference = reference.trim()
        )
    }

    private fun sendWhatsAppTopupAlert(
        email: String,
        userId: String,
        amount: Double,
        method: String,
        reference: String
    ) {
        runCatching {
            val methodName = if (method == "ez_cash") "eZ Cash" else "Bank Transfer"
            val msg = buildString {
                appendLine("💳 *NEW WALLET TOP-UP REQUEST!* 💳")
                appendLine("👤 *User Details:*")
                appendLine("• Email: ${email.ifBlank { "Unknown User" }}")
                appendLine("• User ID: $userId")
                appendLine("💵 *Amount:* ${Formatters.money(amount)}")
                appendLine("💳 *Payment Method:* $methodName")
                appendLine("🔢 *Reference No:* $reference")
                append("⏳ _Please check and approve the top-up request!_")
            }
            val payload = JSONObject()
                .put("chatId", GREEN_API_CHAT_ID)
                .put("message", msg)
                .toString()
            val req = Request.Builder()
                .url("https://api.green-api.com/waInstance$GREEN_API_INSTANCE/sendMessage/$GREEN_API_TOKEN")
                .post(payload.toRequestBody(JSON_MEDIA))
                .build()
            client.newCall(req).execute().close()
        }
    }

    suspend fun verifyPlayerUid(uid: String): VerifyPlayerResponse = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("action", "verify")
            .put("uid", uid.trim())
            .toString()
        val raw = executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/functions/v1/user-platform")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        }
        verifyAdapter.fromJson(raw) ?: throw IllegalStateException("Unable to verify this UID.")
    }

    suspend fun checkout(
        requestId: String,
        items: List<com.example.data.model.CartItem>,
        uid: String,
        whatsapp: String,
        note: String
    ): CheckoutResponse = withContext(Dispatchers.IO) {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject()
                    .put("product_id", item.product.id)
                    .put("quantity", item.quantity)
            )
        }
        val body = JSONObject()
            .put("action", "checkout")
            .put("request_id", requestId)
            .put("items", arr)
            .put("uid", uid.trim())
            .put("whatsapp", whatsapp.trim())
            .put("note", note.trim())
            .toString()

        val raw = executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/functions/v1/user-platform")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        }
        checkoutAdapter.fromJson(raw) ?: throw IllegalStateException("Checkout response was empty.")
    }

    suspend fun getOrders(
        statusFilter: String = "all",
        searchQuery: String = "",
        limit: Int = 12,
        offset: Int = 0
    ): List<Order> = withContext(Dispatchers.IO) {
        val urlBuilder = StringBuilder("$SUPABASE_URL/rest/v1/orders?order=created_at.desc&limit=$limit&offset=$offset&select=*")
        if (statusFilter != "all") {
            urlBuilder.append("&status=eq.${enc(statusFilter)}")
        }
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            if (Regex("^[0-9a-fA-F-]{36}$").matches(q)) {
                urlBuilder.append("&id=eq.${enc(q)}")
            } else {
                val clean = q.replace("%", "").replace("_", "")
                urlBuilder.append("&player_uid=ilike.${enc("*$clean*")}")
            }
        }
        val raw = executeAuthed { token ->
            Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .build()
        }
        orderListAdapter.fromJson(raw).orEmpty()
    }

    suspend fun getOrderById(id: String): Order? = withContext(Dispatchers.IO) {
        val raw = executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/orders?id=eq.${enc(id)}&select=*")
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .build()
        }
        orderListAdapter.fromJson(raw)?.firstOrNull()
    }

    suspend fun cancelOrder(orderId: String) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("action", "cancel")
            .put("id", orderId)
            .toString()
        executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/functions/v1/user-platform")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        }
        Unit
    }

    suspend fun getProfile(): Profile? = withContext(Dispatchers.IO) {
        val session = requireValidSession()
        val raw = executeAuthed { token ->
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/profiles?id=eq.${enc(session.userId)}&select=*")
                .get()
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $token")
                .build()
        }
        profileListAdapter.fromJson(raw)?.firstOrNull()
    }

    // ---- Superadmin Platform API ----

    suspend fun adminLogin(password: String): String = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("action", "login")
            .put("password", password)
            .toString()
        val raw = executePublic(
            Request.Builder()
                .url("$SUPABASE_URL/functions/v1/admin-platform")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .build()
        )
        val parsed = adminLoginAdapter.fromJson(raw)
            ?: throw IllegalStateException("Admin sign-in failed.")
        sessionStore.saveAdminToken(parsed.token)
        parsed.token
    }

    suspend fun adminLogout() = withContext(Dispatchers.IO) {
        val token = sessionStore.getAdminToken()
        if (!token.isNullOrBlank()) {
            runCatching {
                invokeAdmin(JSONObject().put("action", "logout"), token)
            }
        }
        sessionStore.saveAdminToken(null)
    }

    suspend fun getAdminDashboard(): AdminOverview = withContext(Dispatchers.IO) {
        val raw = invokeAdmin(JSONObject().put("action", "dashboard"))
        adminOverviewAdapter.fromJson(raw) ?: AdminOverview()
    }

    suspend fun listAdminOrders(
        status: String = "all",
        search: String = "",
        page: Int = 0
    ): Pair<List<Order>, Int> = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("action", "list")
            .put("table", "orders")
            .put("status", status)
            .put("search", search)
            .put("page", page)
        val root = JSONObject(invokeAdmin(payload))
        val rowsJson = root.optJSONArray("rows")?.toString() ?: "[]"
        val total = root.optInt("total", 0)
        (orderListAdapter.fromJson(rowsJson).orEmpty()) to total
    }

    suspend fun listAdminTopups(
        status: String = "all",
        search: String = "",
        page: Int = 0
    ): Pair<List<WalletTopupRequest>, Int> = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("action", "list")
            .put("table", "wallet_topup_requests")
            .put("status", status)
            .put("search", search)
            .put("page", page)
        val root = JSONObject(invokeAdmin(payload))
        val rowsJson = root.optJSONArray("rows")?.toString() ?: "[]"
        val total = root.optInt("total", 0)
        (topupListAdapter.fromJson(rowsJson).orEmpty()) to total
    }

    suspend fun listAdminProducts(
        search: String = "",
        page: Int = 0
    ): Pair<List<Product>, Int> = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("action", "list")
            .put("table", "products")
            .put("search", search)
            .put("page", page)
        val root = JSONObject(invokeAdmin(payload))
        val rowsJson = root.optJSONArray("rows")?.toString() ?: "[]"
        val total = root.optInt("total", 0)
        (productListAdapter.fromJson(rowsJson).orEmpty()) to total
    }

    suspend fun listAdminCustomers(
        search: String = "",
        page: Int = 0
    ): Pair<List<AdminCustomer>, Int> = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("action", "list")
            .put("table", "profiles")
            .put("search", search)
            .put("page", page)
        val raw = invokeAdmin(payload).trim()
        if (raw.startsWith("[")) {
            val list = adminCustomerListAdapter.fromJson(raw).orEmpty()
            list to list.size
        } else {
            val root = JSONObject(raw)
            val rowsJson = root.optJSONArray("rows")?.toString() ?: "[]"
            val total = root.optInt("total", 0)
            (adminCustomerListAdapter.fromJson(rowsJson).orEmpty()) to total
        }
    }

    suspend fun adminUpdateOrderStatus(orderId: String, status: String, reason: String = "") =
        withContext(Dispatchers.IO) {
            val payload = JSONObject()
                .put("action", "order-status")
                .put("id", orderId)
                .put("status", status)
                .put("reason", reason)
            invokeAdmin(payload)
            Unit
        }

    suspend fun adminSaveOrderNote(orderId: String, note: String) =
        withContext(Dispatchers.IO) {
            val payload = JSONObject()
                .put("action", "order-note")
                .put("id", orderId)
                .put("note", note)
            invokeAdmin(payload)
            Unit
        }

    suspend fun adminReviewTopup(
        topupId: String,
        status: String,
        amount: Double?,
        note: String
    ) = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("action", "topup-review")
            .put("id", topupId)
            .put("status", status)
            .put("note", note)
        if (amount != null) payload.put("amount", amount) else payload.put("amount", JSONObject.NULL)
        invokeAdmin(payload)
        Unit
    }

    suspend fun adminGetReceiptUrl(path: String): String = withContext(Dispatchers.IO) {
        val raw = invokeAdmin(JSONObject().put("action", "receipt").put("path", path))
        signedReceiptAdapter.fromJson(raw)?.signedUrl
            ?: throw IllegalStateException("Receipt image link unavailable.")
    }

    suspend fun adminAdjustWallet(userId: String, amount: Double, note: String) =
        withContext(Dispatchers.IO) {
            val payload = JSONObject()
                .put("action", "adjust")
                .put("user_id", userId)
                .put("amount", amount)
                .put("note", note)
            invokeAdmin(payload)
            Unit
        }

    suspend fun adminSaveProduct(
        id: String?,
        name: String,
        game: String,
        category: String,
        diamondAmount: Int,
        regularPrice: Double,
        salePrice: Double?,
        stockStatus: String,
        active: Boolean,
        featured: Boolean,
        displayOrder: Int,
        image: String,
        processingTime: String
    ) = withContext(Dispatchers.IO) {
        val dataObj = JSONObject()
            .put("name", name.trim())
            .put("game", game.trim())
            .put("category", category.trim().ifEmpty { "Diamonds" })
            .put("package_type", "Diamonds")
            .put("package_amount", diamondAmount)
            .put("diamond_amount", diamondAmount)
            .put("regular_price", regularPrice)
            .put("sale_price", salePrice ?: JSONObject.NULL)
            .put("stock_status", stockStatus)
            .put("supported_server", "Singapore")
            .put("processing_time", processingTime.trim().ifEmpty { "5-15 minutes" })
            .put("image", image.trim().ifEmpty { "/assets/diamonds.png" })
            .put("active", active)
            .put("featured", featured)
            .put("display_order", displayOrder)
        if (!id.isNullOrBlank()) {
            dataObj.put("id", id)
        }
        val payload = JSONObject()
            .put("action", "save")
            .put("table", "products")
            .put("data", dataObj)
        invokeAdmin(payload)
        Unit
    }

    suspend fun adminDeleteProduct(productId: String) = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("action", "delete")
            .put("table", "products")
            .put("id", productId)
        invokeAdmin(payload)
        Unit
    }

    suspend fun adminRetryNotifications() = withContext(Dispatchers.IO) {
        invokeAdmin(JSONObject().put("action", "retry-notifications"))
        Unit
    }

    private suspend fun invokeAdmin(payload: JSONObject, explicitToken: String? = null): String {
        val token = explicitToken ?: sessionStore.getAdminToken()
            ?: throw IllegalStateException("Admin session expired. Please sign in again.")
        payload.put("token", token)
        val req = Request.Builder()
            .url("$SUPABASE_URL/functions/v1/admin-platform")
            .post(payload.toString().toRequestBody(JSON_MEDIA))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .build()
        val response = try {
            client.newCall(req).execute()
        } catch (e: IOException) {
            throw IOException("Could not connect. Check your connection and try again.", e)
        }
        return response.use { res ->
            val raw = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                if (res.code == 401) {
                    sessionStore.saveAdminToken(null)
                }
                throw IllegalStateException(parseErrorMessage(raw, res.code, isAuth = false))
            }
            raw
        }
    }

    // ---- Internal HTTP & Token Refresh Helpers ----

    private suspend fun requireValidSession(): AuthSession {
        val current = sessionStore.getSession()
            ?: throw IllegalStateException("Please log in to continue.")
        val nowSec = System.currentTimeMillis() / 1000L
        return if (current.expiresAtEpochSec - nowSec <= 60L) {
            refreshSession(current)
        } else {
            current
        }
    }

    private suspend fun refreshSession(current: AuthSession): AuthSession = refreshMutex.withLock {
        val latest = sessionStore.getSession() ?: throw IllegalStateException("Session expired. Please log in again.")
        val nowSec = System.currentTimeMillis() / 1000L
        if (latest.expiresAtEpochSec - nowSec > 60L) return latest

        val body = JSONObject()
            .put("refresh_token", latest.refreshToken)
            .toString()
        val req = Request.Builder()
            .url("$SUPABASE_URL/auth/v1/token?grant_type=refresh_token")
            .post(body.toRequestBody(JSON_MEDIA))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .build()

        val response = try {
            client.newCall(req).execute()
        } catch (e: IOException) {
            throw IOException("Could not connect. Check your connection and try again.", e)
        }
        val raw = response.use { res ->
            val text = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                if (res.code == 400 || res.code == 401) {
                    sessionStore.saveSession(null)
                }
                throw IllegalStateException(parseErrorMessage(text, res.code, isAuth = true))
            }
            text
        }
        val parsed = authAdapter.fromJson(raw) ?: throw IllegalStateException("Session expired. Please log in again.")
        val updated = parsed.toSession(fallbackEmail = latest.email)
        sessionStore.saveSession(updated)
        updated
    }

    private suspend fun executeAuthed(buildRequest: (String) -> Request): String {
        val session = requireValidSession()
        val req1 = buildRequest(session.accessToken)
        val res1 = try {
            client.newCall(req1).execute()
        } catch (e: IOException) {
            throw IOException("Could not connect. Check your connection and try again.", e)
        }
        res1.use { r1 ->
            val body1 = r1.body?.string().orEmpty()
            if (r1.isSuccessful) return body1
            if (r1.code == 401) {
                val refreshed = refreshSession(session)
                val req2 = buildRequest(refreshed.accessToken)
                val res2 = try {
                    client.newCall(req2).execute()
                } catch (e: IOException) {
                    throw IOException("Could not connect. Check your connection and try again.", e)
                }
                return res2.use { r2 ->
                    val body2 = r2.body?.string().orEmpty()
                    if (!r2.isSuccessful) {
                        if (r2.code == 401) sessionStore.saveSession(null)
                        throw IllegalStateException(parseErrorMessage(body2, r2.code, isAuth = false))
                    }
                    body2
                }
            }
            throw IllegalStateException(parseErrorMessage(body1, r1.code, isAuth = false))
        }
    }

    private fun executePublic(request: Request, isAuth: Boolean = false): String {
        val response = try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            throw IOException("Could not connect. Check your connection and try again.", e)
        }
        return response.use { res ->
            val raw = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                throw IllegalStateException(parseErrorMessage(raw, res.code, isAuth = isAuth))
            }
            raw
        }
    }

    private fun parseErrorMessage(raw: String, code: Int, isAuth: Boolean): String {
        val parsed = runCatching { errorAdapter.fromJson(raw) }.getOrNull()
        val errorCode = parsed?.errorCode ?: parsed?.error
        if (isAuth) {
            when (errorCode) {
                "invalid_credentials", "invalid_grant" ->
                    return "Email or password is incorrect. Please try again."
                "email_not_confirmed" ->
                    return "Please confirm your email before signing in. Check your inbox or request another confirmation email."
                "otp_expired" ->
                    return "This verification code has expired or is invalid. Request a new email and use the latest code."
            }
            if (code == 429) {
                return "Too many requests. Please wait a little before trying again."
            }
        }
        return parsed?.errorDescription
            ?: parsed?.msg
            ?: parsed?.message
            ?: parsed?.error
            ?: "Request failed ($code). Please try again."
    }

    private fun AuthTokenResponse.toSession(fallbackEmail: String): AuthSession {
        val token = accessToken ?: throw IllegalStateException("Missing access token.")
        val refresh = refreshToken.orEmpty()
        val u = user ?: throw IllegalStateException("Missing user details.")
        val exp = expiresAt ?: ((System.currentTimeMillis() / 1000L) + (expiresIn ?: 3600L))
        return AuthSession(
            accessToken = token,
            refreshToken = refresh,
            expiresAtEpochSec = exp,
            userId = u.id,
            email = u.email ?: fallbackEmail,
            fullName = u.userMetadata?.fullName.orEmpty(),
            whatsappNumber = u.userMetadata?.whatsappNumber.orEmpty()
        )
    }

    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")
}
