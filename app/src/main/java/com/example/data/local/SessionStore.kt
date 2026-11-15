package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AuthSession
import com.example.data.model.CartItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "osthar_store_prefs")

class SessionStore(
    private val context: Context,
    private val moshi: Moshi
) {
    private val sessionKey = stringPreferencesKey("auth_session_json")
    private val cartKey = stringPreferencesKey("cart_items_json")
    private val pkceVerifierKey = stringPreferencesKey("oauth_pkce_verifier")
    private val adminTokenKey = stringPreferencesKey("admin_session_token")

    private val sessionAdapter = moshi.adapter(AuthSession::class.java)
    private val cartListType = Types.newParameterizedType(List::class.java, CartItem::class.java)
    private val cartAdapter = moshi.adapter<List<CartItem>>(cartListType)

    val sessionFlow: Flow<AuthSession?> = context.dataStore.data.map { prefs ->
        prefs[sessionKey]?.let { raw ->
            runCatching { sessionAdapter.fromJson(raw) }.getOrNull()
        }
    }

    val cartFlow: Flow<List<CartItem>> = context.dataStore.data.map { prefs ->
        prefs[cartKey]?.let { raw ->
            runCatching { cartAdapter.fromJson(raw) }.getOrNull()
        } ?: emptyList()
    }

    val adminTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[adminTokenKey]?.takeIf { it.isNotBlank() }
    }

    suspend fun getSession(): AuthSession? = sessionFlow.first()

    suspend fun saveSession(session: AuthSession?) {
        context.dataStore.edit { prefs ->
            if (session == null) {
                prefs.remove(sessionKey)
            } else {
                prefs[sessionKey] = sessionAdapter.toJson(session)
            }
        }
    }

    suspend fun saveCart(items: List<CartItem>) {
        context.dataStore.edit { prefs ->
            prefs[cartKey] = cartAdapter.toJson(items)
        }
    }

    suspend fun savePkceVerifier(verifier: String?) {
        context.dataStore.edit { prefs ->
            if (verifier == null) prefs.remove(pkceVerifierKey)
            else prefs[pkceVerifierKey] = verifier
        }
    }

    suspend fun consumePkceVerifier(): String? {
        val value = context.dataStore.data.first()[pkceVerifierKey]
        savePkceVerifier(null)
        return value
    }

    suspend fun getAdminToken(): String? = adminTokenFlow.first()

    suspend fun saveAdminToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token.isNullOrBlank()) prefs.remove(adminTokenKey)
            else prefs[adminTokenKey] = token
        }
    }
}
