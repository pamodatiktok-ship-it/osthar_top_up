package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SessionStore
import com.example.data.model.AdminCustomer
import com.example.data.model.AdminOverview
import com.example.data.model.AuthSession
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.PaymentMethodRow
import com.example.data.model.Product
import com.example.data.model.Profile
import com.example.data.model.SiteSettingsData
import com.example.data.model.UiState
import com.example.data.model.VerifyPlayerResponse
import com.example.data.model.WalletTopupRequest
import com.example.data.model.WalletTransaction
import com.example.data.remote.OstharRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class StoreSort(val label: String) {
    RECOMMENDED("Recommended"),
    LOW_TO_HIGH("Price: Low to High"),
    HIGH_TO_LOW("Price: High to Low")
}

enum class AuthMode {
    LOGIN, REGISTER, OTP, FORGOT_PASSWORD
}

data class CheckoutFormState(
    val uid: String = "",
    val verifying: Boolean = false,
    val verifiedPlayer: VerifyPlayerResponse? = null,
    val verifyError: String? = null,
    val whatsapp: String = "",
    val note: String = "",
    val submitting: Boolean = false,
    val submitError: String? = null,
    val requestId: String = UUID.randomUUID().toString()
)

data class TopUpFormState(
    val selectedMethod: String = "ez_cash",
    val amount: String = "",
    val reference: String = "",
    val receiptUri: Uri? = null,
    val receiptLabel: String = "",
    val submitting: Boolean = false,
    val error: String? = null
)

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullName: String = "",
    val whatsapp: String = "",
    val otpCode: String = "",
    val consentAccepted: Boolean = true,
    val busy: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null,
    val resendCooldownSec: Int = 0
)

enum class AdminTab(val label: String) {
    OVERVIEW("Overview"),
    ORDERS("Orders"),
    TOPUPS("Top-Ups"),
    PRODUCTS("Products"),
    CUSTOMERS("Customers")
}

data class AdminPanelUiState(
    val activeTab: AdminTab = AdminTab.OVERVIEW,
    val loginPassword: String = "",
    val loginBusy: Boolean = false,
    val loginError: String? = null,
    val loading: Boolean = false,
    val actionBusy: Boolean = false,
    val error: String? = null,
    val statusFilter: String = "all",
    val searchQuery: String = "",
    val page: Int = 0,
    val totalRecords: Int = 0,
    val overview: AdminOverview? = null,
    val orders: List<Order> = emptyList(),
    val topups: List<WalletTopupRequest> = emptyList(),
    val products: List<Product> = emptyList(),
    val customers: List<AdminCustomer> = emptyList(),
    val receiptSignedUrl: String? = null
)

class OstharViewModel(application: Application) : AndroidViewModel(application) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val sessionStore = SessionStore(application.applicationContext, moshi)
    private val repository = OstharRepository(sessionStore, moshi)

    val session: StateFlow<AuthSession?> = repository.sessionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val cart: StateFlow<List<CartItem>> = repository.cartFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val adminToken: StateFlow<String?> = repository.adminTokenFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _adminState = MutableStateFlow(AdminPanelUiState())
    val adminState: StateFlow<AdminPanelUiState> = _adminState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastEvents = _toastEvents.asSharedFlow()

    // Storefront state
    private val _productsState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val productsState: StateFlow<UiState<List<Product>>> = _productsState.asStateFlow()

    private val _siteSettings = MutableStateFlow<SiteSettingsData?>(null)
    val siteSettings: StateFlow<SiteSettingsData?> = _siteSettings.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<PaymentMethodRow>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethodRow>> = _paymentMethods.asStateFlow()

    // Store filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGame = MutableStateFlow<String?>(null)
    val selectedGame: StateFlow<String?> = _selectedGame.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _inStockOnly = MutableStateFlow(false)
    val inStockOnly: StateFlow<Boolean> = _inStockOnly.asStateFlow()

    private val _sortMode = MutableStateFlow(StoreSort.RECOMMENDED)
    val sortMode: StateFlow<StoreSort> = _sortMode.asStateFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(
        _productsState,
        _searchQuery,
        _selectedGame,
        _selectedCategory,
        _inStockOnly,
        _sortMode
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val state = values[0] as UiState<List<Product>>
        val query = (values[1] as String).trim().lowercase()
        val game = values[2] as String?
        val category = values[3] as String?
        val stockOnly = values[4] as Boolean
        val sort = values[5] as StoreSort

        val items = (state as? UiState.Success)?.data ?: return@combine emptyList()
        items.filter { p ->
            (game == null || p.game.equals(game, ignoreCase = true)) &&
                (category == null || p.category.equals(category, ignoreCase = true)) &&
                (!stockOnly || p.isInStock) &&
                (query.isEmpty() ||
                    "${p.name} ${p.game} ${p.diamondAmount}".lowercase().contains(query))
        }.let { list ->
            when (sort) {
                StoreSort.RECOMMENDED -> list.sortedBy { it.displayOrder }
                StoreSort.LOW_TO_HIGH -> list.sortedBy { it.effectivePrice }
                StoreSort.HIGH_TO_LOW -> list.sortedByDescending { it.effectivePrice }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Wallet state
    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _balanceLoading = MutableStateFlow(false)
    val balanceLoading: StateFlow<Boolean> = _balanceLoading.asStateFlow()

    private val _transactionsState = MutableStateFlow<UiState<List<WalletTransaction>>>(UiState.Loading)
    val transactionsState: StateFlow<UiState<List<WalletTransaction>>> = _transactionsState.asStateFlow()

    private val _topupRequests = MutableStateFlow<List<WalletTopupRequest>>(emptyList())
    val topupRequests: StateFlow<List<WalletTopupRequest>> = _topupRequests.asStateFlow()

    private val _receiptState = MutableStateFlow<UiState<WalletTransaction>>(UiState.Loading)
    val receiptState: StateFlow<UiState<WalletTransaction>> = _receiptState.asStateFlow()

    // Orders state
    private val _ordersState = MutableStateFlow<UiState<List<Order>>>(UiState.Loading)
    val ordersState: StateFlow<UiState<List<Order>>> = _ordersState.asStateFlow()

    private val _orderStatusFilter = MutableStateFlow("all")
    val orderStatusFilter: StateFlow<String> = _orderStatusFilter.asStateFlow()

    private val _orderSearchQuery = MutableStateFlow("")
    val orderSearchQuery: StateFlow<String> = _orderSearchQuery.asStateFlow()

    private val _orderDetailState = MutableStateFlow<UiState<Order>>(UiState.Loading)
    val orderDetailState: StateFlow<UiState<Order>> = _orderDetailState.asStateFlow()

    private val _cancellingOrder = MutableStateFlow(false)
    val cancellingOrder: StateFlow<Boolean> = _cancellingOrder.asStateFlow()

    // Profile state
    private val _profileState = MutableStateFlow<UiState<Profile?>>(UiState.Loading)
    val profileState: StateFlow<UiState<Profile?>> = _profileState.asStateFlow()

    // Forms
    private val _checkoutState = MutableStateFlow(CheckoutFormState())
    val checkoutState: StateFlow<CheckoutFormState> = _checkoutState.asStateFlow()

    private val _topUpState = MutableStateFlow(TopUpFormState())
    val topUpState: StateFlow<TopUpFormState> = _topUpState.asStateFlow()

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private var verifyJob: Job? = null
    private var cooldownJob: Job? = null

    init {
        refreshCatalogAndSettings()
        viewModelScope.launch {
            session.collectLatest { current ->
                if (current != null) {
                    if (_checkoutState.value.whatsapp.isBlank() && current.whatsappNumber.isNotBlank()) {
                        _checkoutState.value = _checkoutState.value.copy(whatsapp = current.whatsappNumber)
                    }
                    refreshUserData()
                } else {
                    _walletBalance.value = 0.0
                    _transactionsState.value = UiState.Success(emptyList())
                    _topupRequests.value = emptyList()
                    _ordersState.value = UiState.Success(emptyList())
                    _profileState.value = UiState.Success(null)
                }
            }
        }
    }

    fun refreshCatalogAndSettings() {
        viewModelScope.launch {
            _productsState.value = UiState.Loading
            runCatching { repository.getProducts() }
                .onSuccess { _productsState.value = UiState.Success(it) }
                .onFailure { _productsState.value = UiState.Error(it.message ?: "Unable to load store.") }
        }
        viewModelScope.launch {
            runCatching { repository.getSiteSettings() }
                .onSuccess { _siteSettings.value = it }
        }
        viewModelScope.launch {
            runCatching { repository.getPaymentSettings() }
                .onSuccess { _paymentMethods.value = it }
        }
    }

    fun refreshUserData() {
        if (session.value == null) return
        refreshBalance()
        refreshTransactions()
        refreshTopupRequests()
        refreshOrders()
        refreshProfile()
    }

    fun refreshBalance() {
        if (session.value == null) return
        viewModelScope.launch {
            _balanceLoading.value = true
            runCatching { repository.getBalance() }
                .onSuccess { _walletBalance.value = it }
            _balanceLoading.value = false
        }
    }

    fun refreshTransactions(limit: Int = 20, offset: Int = 0) {
        if (session.value == null) return
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading
            runCatching { repository.getTransactions(limit = limit, offset = offset) }
                .onSuccess { _transactionsState.value = UiState.Success(it) }
                .onFailure { _transactionsState.value = UiState.Error(it.message ?: "Could not load transactions.") }
        }
    }

    fun loadReceipt(txId: String) {
        viewModelScope.launch {
            _receiptState.value = UiState.Loading
            runCatching { repository.getTransactionById(txId) }
                .onSuccess { tx ->
                    if (tx != null) _receiptState.value = UiState.Success(tx)
                    else _receiptState.value = UiState.Error("This transaction could not be found in your wallet.")
                }
                .onFailure { _receiptState.value = UiState.Error(it.message ?: "Receipt unavailable.") }
        }
    }

    fun refreshTopupRequests() {
        if (session.value == null) return
        viewModelScope.launch {
            runCatching { repository.getTopupRequests() }
                .onSuccess { _topupRequests.value = it }
        }
    }

    fun refreshOrders() {
        if (session.value == null) return
        viewModelScope.launch {
            _ordersState.value = UiState.Loading
            runCatching {
                repository.getOrders(
                    statusFilter = _orderStatusFilter.value,
                    searchQuery = _orderSearchQuery.value,
                    limit = 30
                )
            }
                .onSuccess { _ordersState.value = UiState.Success(it) }
                .onFailure { _ordersState.value = UiState.Error(it.message ?: "Couldn't load orders.") }
        }
    }

    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            _orderDetailState.value = UiState.Loading
            runCatching { repository.getOrderById(orderId) }
                .onSuccess { order ->
                    if (order != null) _orderDetailState.value = UiState.Success(order)
                    else _orderDetailState.value = UiState.Error("Order not found.")
                }
                .onFailure { _orderDetailState.value = UiState.Error(it.message ?: "Unable to load order.") }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _cancellingOrder.value = true
            runCatching { repository.cancelOrder(orderId) }
                .onSuccess {
                    emitToast("Order cancelled. Wallet refunded.")
                    refreshBalance()
                    refreshOrders()
                    loadOrderDetail(orderId)
                }
                .onFailure {
                    emitToast(it.message ?: "Unable to cancel order.")
                }
            _cancellingOrder.value = false
        }
    }

    fun refreshProfile() {
        if (session.value == null) return
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            runCatching { repository.getProfile() }
                .onSuccess { profile ->
                    _profileState.value = UiState.Success(profile)
                    val wa = profile?.whatsappNumber
                    if (!wa.isNullOrBlank() && _checkoutState.value.whatsapp.isBlank()) {
                        _checkoutState.value = _checkoutState.value.copy(whatsapp = wa)
                    }
                }
                .onFailure { _profileState.value = UiState.Error(it.message ?: "Unable to load profile.") }
        }
    }

    // ---- Store filter controls ----

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun selectGameFilter(game: String?) { _selectedGame.value = game }
    fun selectCategoryFilter(category: String?) { _selectedCategory.value = category }
    fun toggleInStockOnly(enabled: Boolean) { _inStockOnly.value = enabled }
    fun selectSort(sort: StoreSort) { _sortMode.value = sort }
    fun clearStoreFilters() {
        _searchQuery.value = ""
        _selectedGame.value = null
        _selectedCategory.value = null
        _inStockOnly.value = false
        _sortMode.value = StoreSort.RECOMMENDED
    }

    fun setOrderStatusFilter(status: String) {
        _orderStatusFilter.value = status
        refreshOrders()
    }

    fun setOrderSearchQuery(query: String) {
        _orderSearchQuery.value = query
        refreshOrders()
    }

    // ---- Cart operations ----

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val current = cart.value.toMutableList()
            val idx = current.indexOfFirst { it.product.id == product.id }
            if (idx >= 0) {
                val existing = current[idx]
                current[idx] = existing.copy(quantity = (existing.quantity + quantity).coerceAtMost(99))
            } else {
                current.add(CartItem(product = product, quantity = quantity.coerceIn(1, 99)))
            }
            repository.saveCart(current)
            emitToast("${product.name} added to cart")
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            val updated = if (quantity <= 0) {
                cart.value.filterNot { it.product.id == productId }
            } else {
                cart.value.map {
                    if (it.product.id == productId) it.copy(quantity = quantity.coerceAtMost(99)) else it
                }
            }
            repository.saveCart(updated)
        }
    }

    fun clearCart() {
        viewModelScope.launch { repository.saveCart(emptyList()) }
    }

    // ---- Checkout & UID verification ----

    fun onCheckoutUidChanged(raw: String) {
        val cleaned = raw.filter { it.isDigit() }.take(15)
        _checkoutState.value = _checkoutState.value.copy(
            uid = cleaned,
            verifiedPlayer = null,
            verifyError = null,
            submitError = null
        )
        verifyJob?.cancel()
        if (session.value != null && Regex("^\\d{6,15}$").matches(cleaned)) {
            verifyJob = viewModelScope.launch {
                delay(650)
                verifyPlayerNow()
            }
        }
    }

    fun verifyPlayerNow() {
        val uid = _checkoutState.value.uid
        if (session.value == null || !Regex("^\\d{6,15}$").matches(uid)) return
        verifyJob?.cancel()
        verifyJob = viewModelScope.launch {
            _checkoutState.value = _checkoutState.value.copy(verifying = true, verifyError = null)
            runCatching { repository.verifyPlayerUid(uid) }
                .onSuccess { player ->
                    if (_checkoutState.value.uid == uid) {
                        _checkoutState.value = _checkoutState.value.copy(
                            verifying = false,
                            verifiedPlayer = player,
                            verifyError = null
                        )
                    }
                }
                .onFailure { err ->
                    if (_checkoutState.value.uid == uid) {
                        _checkoutState.value = _checkoutState.value.copy(
                            verifying = false,
                            verifiedPlayer = null,
                            verifyError = err.message ?: "Unable to verify this UID."
                        )
                    }
                }
        }
    }

    fun onCheckoutWhatsappChanged(value: String) {
        _checkoutState.value = _checkoutState.value.copy(whatsapp = value, submitError = null)
    }

    fun onCheckoutNoteChanged(value: String) {
        _checkoutState.value = _checkoutState.value.copy(note = value.take(1000))
    }

    fun submitCheckout(
        items: List<CartItem>,
        clearCartOnSuccess: Boolean,
        onOrderCreated: (String) -> Unit
    ) {
        val state = _checkoutState.value
        val verified = state.verifiedPlayer
        if (state.submitting || state.verifying || verified == null || verified.uid != state.uid) return
        if (!Regex("^\\+\\d{8,15}$").matches(state.whatsapp.trim())) {
            _checkoutState.value = state.copy(
                submitError = "Enter WhatsApp with country code, e.g. +94771234567."
            )
            return
        }

        viewModelScope.launch {
            _checkoutState.value = state.copy(submitting = true, submitError = null)
            runCatching {
                repository.checkout(
                    requestId = state.requestId,
                    items = items,
                    uid = state.uid,
                    whatsapp = state.whatsapp,
                    note = state.note
                )
            }.onSuccess { resp ->
                if (clearCartOnSuccess) {
                    repository.saveCart(emptyList())
                }
                _checkoutState.value = _checkoutState.value.copy(
                    submitting = false,
                    note = "",
                    requestId = UUID.randomUUID().toString()
                )
                refreshBalance()
                refreshOrders()
                refreshTransactions()
                emitToast("Order placed. Your next upgrade is on its way!")
                val firstOrderId = resp.orders.firstOrNull()
                if (firstOrderId != null) {
                    onOrderCreated(firstOrderId)
                }
            }.onFailure { err ->
                _checkoutState.value = _checkoutState.value.copy(
                    submitting = false,
                    submitError = err.message ?: "Checkout failed. Please try again."
                )
            }
        }
    }

    // ---- Wallet Top-Up ----

    fun selectTopUpMethod(method: String) {
        _topUpState.value = _topUpState.value.copy(selectedMethod = method, error = null)
    }

    fun updateTopUpAmount(amount: String) {
        _topUpState.value = _topUpState.value.copy(amount = amount, error = null)
    }

    fun updateTopUpReference(ref: String) {
        _topUpState.value = _topUpState.value.copy(reference = ref.take(200), error = null)
    }

    fun setTopUpReceipt(uri: Uri?, label: String) {
        _topUpState.value = _topUpState.value.copy(receiptUri = uri, receiptLabel = label, error = null)
    }

    fun submitTopUp(context: Context) {
        val form = _topUpState.value
        val payment = _paymentMethods.value.find { it.id == form.selectedMethod }?.data
        val minAmount = maxOf(payment?.minAmount ?: 0.0, _siteSettings.value?.minTopupAmount ?: 100.0)
        val parsedAmount = form.amount.trim().toDoubleOrNull()

        if (payment?.enabled != true) {
            _topUpState.value = form.copy(error = "This payment method is not accepting payments yet.")
            return
        }
        if (parsedAmount == null || parsedAmount < minAmount || parsedAmount > 1_000_000.0) {
            _topUpState.value = form.copy(error = "Enter an amount between LKR ${minAmount.toInt()} and LKR 1,000,000.")
            return
        }
        if (form.reference.trim().isEmpty()) {
            _topUpState.value = form.copy(error = "Transaction / reference number is required.")
            return
        }

        viewModelScope.launch {
            _topUpState.value = form.copy(submitting = true, error = null)
            runCatching {
                var uploadedPath = ""
                val uri = form.receiptUri
                if (uri != null) {
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    if (mime !in listOf("image/jpeg", "image/jpg", "image/png", "image/webp")) {
                        throw IllegalArgumentException("Upload a JPG, PNG or WebP image under 5 MB.")
                    }
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IllegalArgumentException("Unable to read selected receipt image.")
                    val normalizedMime = if (mime == "image/jpg") "image/jpeg" else mime
                    uploadedPath = repository.uploadReceiptImage(bytes, normalizedMime)
                }
                repository.submitTopupRequest(
                    method = form.selectedMethod,
                    amount = parsedAmount,
                    reference = form.reference,
                    receiptPath = uploadedPath
                )
            }.onSuccess {
                _topUpState.value = TopUpFormState(selectedMethod = form.selectedMethod)
                refreshTopupRequests()
                emitToast("Top-up submitted for review. Balance updates after approval.")
            }.onFailure { err ->
                _topUpState.value = _topUpState.value.copy(
                    submitting = false,
                    error = err.message ?: "Unable to submit top-up request."
                )
            }
        }
    }

    // ---- Auth Form Controls ----

    fun setAuthMode(mode: AuthMode) {
        _authState.value = _authState.value.copy(
            mode = mode,
            error = null,
            infoMessage = null
        )
    }

    fun updateAuthField(
        email: String = _authState.value.email,
        password: String = _authState.value.password,
        confirmPassword: String = _authState.value.confirmPassword,
        fullName: String = _authState.value.fullName,
        whatsapp: String = _authState.value.whatsapp,
        otpCode: String = _authState.value.otpCode,
        consentAccepted: Boolean = _authState.value.consentAccepted
    ) {
        _authState.value = _authState.value.copy(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            fullName = fullName,
            whatsapp = whatsapp,
            otpCode = otpCode,
            consentAccepted = consentAccepted,
            error = null
        )
    }

    fun submitAuth(onSuccess: () -> Unit = {}) {
        val state = _authState.value
        if (state.busy) return

        when (state.mode) {
            AuthMode.LOGIN -> {
                if (!state.consentAccepted) {
                    _authState.value = state.copy(error = "Please accept the Terms & Privacy Policy.")
                    return
                }
                if (state.email.isBlank() || state.password.isBlank()) {
                    _authState.value = state.copy(error = "Enter your email and password.")
                    return
                }
                viewModelScope.launch {
                    _authState.value = state.copy(busy = true, error = null)
                    runCatching { repository.signInWithPassword(state.email, state.password) }
                        .onSuccess {
                            _authState.value = AuthUiState()
                            emitToast("Welcome back!")
                            onSuccess()
                        }
                        .onFailure { err ->
                            _authState.value = _authState.value.copy(
                                busy = false,
                                error = err.message ?: "Sign in failed."
                            )
                        }
                }
            }

            AuthMode.REGISTER -> {
                if (!state.consentAccepted) {
                    _authState.value = state.copy(error = "Please accept the Terms & Privacy Policy.")
                    return
                }
                if (state.email.isBlank() || state.password.length < 8) {
                    _authState.value = state.copy(error = "Enter a valid email and a password of at least 8 characters.")
                    return
                }
                if (state.password != state.confirmPassword) {
                    _authState.value = state.copy(error = "Passwords do not match.")
                    return
                }
                viewModelScope.launch {
                    _authState.value = state.copy(busy = true, error = null)
                    runCatching {
                        repository.signUp(
                            email = state.email,
                            password = state.password,
                            fullName = state.fullName,
                            whatsapp = state.whatsapp
                        )
                    }.onSuccess { createdSession ->
                        if (createdSession != null) {
                            _authState.value = AuthUiState()
                            emitToast("Account created!")
                            onSuccess()
                        } else {
                            _authState.value = _authState.value.copy(
                                busy = false,
                                mode = AuthMode.OTP,
                                infoMessage = "Account created! Check ${state.email.trim()} for your confirmation link or code."
                            )
                        }
                    }.onFailure { err ->
                        _authState.value = _authState.value.copy(
                            busy = false,
                            error = err.message ?: "Registration failed."
                        )
                    }
                }
            }

            AuthMode.OTP -> {
                if (state.email.isBlank() || state.otpCode.trim().isEmpty()) {
                    _authState.value = state.copy(error = "Enter your email and verification code.")
                    return
                }
                viewModelScope.launch {
                    _authState.value = state.copy(busy = true, error = null)
                    runCatching { repository.verifyEmailOtp(state.email, state.otpCode) }
                        .onSuccess {
                            _authState.value = AuthUiState()
                            emitToast("Email verified!")
                            onSuccess()
                        }
                        .onFailure { err ->
                            _authState.value = _authState.value.copy(
                                busy = false,
                                error = err.message ?: "Verification failed."
                            )
                        }
                }
            }

            AuthMode.FORGOT_PASSWORD -> {
                if (state.email.isBlank()) {
                    _authState.value = state.copy(error = "Enter your email address.")
                    return
                }
                viewModelScope.launch {
                    _authState.value = state.copy(busy = true, error = null)
                    runCatching { repository.sendPasswordReset(state.email) }
                        .onSuccess {
                            _authState.value = _authState.value.copy(
                                busy = false,
                                infoMessage = "If an account exists for this address, a reset email will arrive shortly."
                            )
                        }
                        .onFailure { err ->
                            _authState.value = _authState.value.copy(
                                busy = false,
                                error = err.message ?: "Unable to send reset email."
                            )
                        }
                }
            }
        }
    }

    fun resendConfirmationEmail() {
        val state = _authState.value
        if (state.busy || state.resendCooldownSec > 0 || state.email.isBlank()) return
        viewModelScope.launch {
            _authState.value = state.copy(busy = true, error = null)
            runCatching { repository.resendConfirmationEmail(state.email) }
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        busy = false,
                        infoMessage = "Confirmation email resent. Check your inbox and spam folder."
                    )
                    startResendCooldown()
                }
                .onFailure { err ->
                    _authState.value = _authState.value.copy(
                        busy = false,
                        error = err.message ?: "Unable to resend confirmation email."
                    )
                }
        }
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (sec in 60 downTo 1) {
                _authState.value = _authState.value.copy(resendCooldownSec = sec)
                delay(1000)
            }
            _authState.value = _authState.value.copy(resendCooldownSec = 0)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            emitToast("Signed out")
        }
    }

    // ---- Google OAuth ----

    fun startGoogleSignIn(onUrlReady: (String) -> Unit) {
        val state = _authState.value
        if (!state.consentAccepted) {
            _authState.value = state.copy(error = "Please accept the Terms & Privacy Policy first.")
            return
        }
        viewModelScope.launch {
            _authState.value = state.copy(busy = true, error = null)
            runCatching { repository.buildGoogleOAuthUrl() }
                .onSuccess { url ->
                    _authState.value = _authState.value.copy(busy = false)
                    onUrlReady(url)
                }
                .onFailure { err ->
                    _authState.value = _authState.value.copy(
                        busy = false,
                        error = err.message ?: "Unable to start Google sign-in."
                    )
                }
        }
    }

    fun handleOAuthCallback(uri: Uri, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(busy = true, error = null)
            runCatching { repository.completeOAuthCallback(uri) }
                .onSuccess {
                    _authState.value = AuthUiState()
                    emitToast("Signed in with Google!")
                    onSuccess()
                }
                .onFailure { err ->
                    _authState.value = _authState.value.copy(
                        busy = false,
                        error = err.message ?: "Google sign-in could not be completed."
                    )
                    emitToast(err.message ?: "Google sign-in failed")
                }
        }
    }

    // ---- Superadmin Panel ----

    fun updateAdminPassword(password: String) {
        _adminState.value = _adminState.value.copy(loginPassword = password, loginError = null)
    }

    fun adminLogin() {
        val pwd = _adminState.value.loginPassword
        if (pwd.isBlank() || _adminState.value.loginBusy) return
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(loginBusy = true, loginError = null)
            runCatching { repository.adminLogin(pwd) }
                .onSuccess {
                    _adminState.value = _adminState.value.copy(loginBusy = false, loginPassword = "")
                    emitToast("Admin signed in")
                    refreshAdminData()
                }
                .onFailure { err ->
                    _adminState.value = _adminState.value.copy(
                        loginBusy = false,
                        loginError = err.message ?: "Invalid admin password."
                    )
                }
        }
    }

    fun adminLogout() {
        viewModelScope.launch {
            repository.adminLogout()
            _adminState.value = AdminPanelUiState()
            emitToast("Admin signed out")
        }
    }

    fun selectAdminTab(tab: AdminTab) {
        _adminState.value = _adminState.value.copy(
            activeTab = tab,
            statusFilter = "all",
            searchQuery = "",
            page = 0,
            error = null
        )
        refreshAdminData()
    }

    fun updateAdminFilter(
        statusFilter: String = _adminState.value.statusFilter,
        searchQuery: String = _adminState.value.searchQuery
    ) {
        _adminState.value = _adminState.value.copy(
            statusFilter = statusFilter,
            searchQuery = searchQuery,
            page = 0
        )
        refreshAdminData()
    }

    fun changeAdminPage(delta: Int) {
        val next = (_adminState.value.page + delta).coerceAtLeast(0)
        _adminState.value = _adminState.value.copy(page = next)
        refreshAdminData()
    }

    fun refreshAdminData() {
        if (adminToken.value.isNullOrBlank()) return
        val state = _adminState.value
        viewModelScope.launch {
            _adminState.value = state.copy(loading = true, error = null)
            runCatching {
                when (state.activeTab) {
                    AdminTab.OVERVIEW -> {
                        val overview = repository.getAdminDashboard()
                        _adminState.value = _adminState.value.copy(loading = false, overview = overview)
                    }
                    AdminTab.ORDERS -> {
                        val (rows, total) = repository.listAdminOrders(state.statusFilter, state.searchQuery, state.page)
                        _adminState.value = _adminState.value.copy(loading = false, orders = rows, totalRecords = total)
                    }
                    AdminTab.TOPUPS -> {
                        val (rows, total) = repository.listAdminTopups(state.statusFilter, state.searchQuery, state.page)
                        _adminState.value = _adminState.value.copy(loading = false, topups = rows, totalRecords = total)
                    }
                    AdminTab.PRODUCTS -> {
                        val (rows, total) = repository.listAdminProducts(state.searchQuery, state.page)
                        _adminState.value = _adminState.value.copy(loading = false, products = rows, totalRecords = total)
                    }
                    AdminTab.CUSTOMERS -> {
                        val (rows, total) = repository.listAdminCustomers(state.searchQuery, state.page)
                        _adminState.value = _adminState.value.copy(loading = false, customers = rows, totalRecords = total)
                    }
                }
            }.onFailure { err ->
                _adminState.value = _adminState.value.copy(
                    loading = false,
                    error = err.message ?: "Unable to load admin records."
                )
            }
        }
    }

    fun adminUpdateOrderStatus(orderId: String, status: String, reason: String = "", onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actionBusy = true, error = null)
            runCatching { repository.adminUpdateOrderStatus(orderId, status, reason) }
                .onSuccess {
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast("Order marked $status")
                    refreshAdminData()
                    refreshOrders()
                    onDone()
                }
                .onFailure { err ->
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast(err.message ?: "Order update failed.")
                }
        }
    }

    fun adminSaveOrderNote(orderId: String, note: String) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actionBusy = true)
            runCatching { repository.adminSaveOrderNote(orderId, note) }
                .onSuccess {
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast("Order note saved")
                }
                .onFailure { err ->
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast(err.message ?: "Unable to save note.")
                }
        }
    }

    fun adminReviewTopup(topupId: String, status: String, amount: Double?, note: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actionBusy = true)
            runCatching { repository.adminReviewTopup(topupId, status, amount, note) }
                .onSuccess {
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast("Top-up $status")
                    refreshAdminData()
                    refreshBalance()
                    refreshTopupRequests()
                    onDone()
                }
                .onFailure { err ->
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast(err.message ?: "Top-up review failed.")
                }
        }
    }

    fun adminOpenReceipt(path: String) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actionBusy = true, receiptSignedUrl = null)
            runCatching { repository.adminGetReceiptUrl(path) }
                .onSuccess { url ->
                    _adminState.value = _adminState.value.copy(actionBusy = false, receiptSignedUrl = url)
                }
                .onFailure { err ->
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast(err.message ?: "Unable to open receipt.")
                }
        }
    }

    fun adminClearReceiptPreview() {
        _adminState.value = _adminState.value.copy(receiptSignedUrl = null)
    }

    fun adminAdjustWallet(userId: String, amount: Double, note: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actionBusy = true)
            runCatching { repository.adminAdjustWallet(userId, amount, note) }
                .onSuccess {
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast("Wallet adjusted")
                    refreshAdminData()
                    refreshBalance()
                    onDone()
                }
                .onFailure { err ->
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast(err.message ?: "Adjustment failed.")
                }
        }
    }

    fun adminSaveProduct(
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
        processingTime: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actionBusy = true)
            runCatching {
                repository.adminSaveProduct(
                    id = id,
                    name = name,
                    game = game,
                    category = category,
                    diamondAmount = diamondAmount,
                    regularPrice = regularPrice,
                    salePrice = salePrice,
                    stockStatus = stockStatus,
                    active = active,
                    featured = featured,
                    displayOrder = displayOrder,
                    image = image,
                    processingTime = processingTime
                )
            }.onSuccess {
                _adminState.value = _adminState.value.copy(actionBusy = false)
                emitToast("Product saved")
                refreshAdminData()
                refreshCatalogAndSettings()
                onDone()
            }.onFailure { err ->
                _adminState.value = _adminState.value.copy(actionBusy = false)
                emitToast(err.message ?: "Unable to save product.")
            }
        }
    }

    fun adminDeleteProduct(productId: String) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actionBusy = true)
            runCatching { repository.adminDeleteProduct(productId) }
                .onSuccess {
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast("Product deleted")
                    refreshAdminData()
                    refreshCatalogAndSettings()
                }
                .onFailure { err ->
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast(err.message ?: "Unable to delete product.")
                }
        }
    }

    fun adminRetryNotifications() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actionBusy = true)
            runCatching { repository.adminRetryNotifications() }
                .onSuccess {
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast("Pending notifications retried")
                }
                .onFailure { err ->
                    _adminState.value = _adminState.value.copy(actionBusy = false)
                    emitToast(err.message ?: "Retry failed.")
                }
        }
    }

    private fun emitToast(msg: String) {
        _toastEvents.tryEmit(msg)
    }
}
