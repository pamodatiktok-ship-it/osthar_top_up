package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Product(
    val id: String,
    val name: String,
    val game: String = "Free Fire",
    val category: String = "Diamonds",
    @Json(name = "package_type") val packageType: String = "Diamonds",
    @Json(name = "package_amount") val packageAmount: Int = 0,
    @Json(name = "diamond_amount") val diamondAmount: Int = 0,
    @Json(name = "regular_price") val regularPrice: Double = 0.0,
    @Json(name = "sale_price") val salePrice: Double? = null,
    val image: String = "",
    @Json(name = "short_description") val shortDescription: String = "",
    @Json(name = "full_description") val fullDescription: String = "",
    @Json(name = "supported_server") val supportedServer: String = "Singapore",
    @Json(name = "processing_time") val processingTime: String = "5-15 minutes",
    @Json(name = "stock_status") val stockStatus: String = "in_stock",
    val featured: Boolean = false,
    val active: Boolean = true,
    @Json(name = "display_order") val displayOrder: Int = 0
) {
    val effectivePrice: Double get() = salePrice ?: regularPrice
    val isInStock: Boolean get() = stockStatus == "in_stock"
    val discountPercent: Int?
        get() = salePrice?.takeIf { regularPrice > 0 && it < regularPrice }?.let {
            kotlin.math.round((1.0 - it / regularPrice) * 100.0).toInt()
        }
}

@JsonClass(generateAdapter = true)
data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val lineTotal: Double get() = product.effectivePrice * quantity
}

@JsonClass(generateAdapter = true)
data class UserMetadata(
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "whatsapp_number") val whatsappNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthUser(
    val id: String,
    val email: String? = null,
    @Json(name = "user_metadata") val userMetadata: UserMetadata? = null
)

@JsonClass(generateAdapter = true)
data class AuthTokenResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "expires_at") val expiresAt: Long? = null,
    val user: AuthUser? = null
)

@JsonClass(generateAdapter = true)
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSec: Long,
    val userId: String,
    val email: String,
    val fullName: String = "",
    val whatsappNumber: String = ""
)

@JsonClass(generateAdapter = true)
data class Profile(
    val id: String,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "whatsapp_number") val whatsappNumber: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SiteSettingsRow(
    val id: String? = null,
    val data: SiteSettingsData? = null
)

@JsonClass(generateAdapter = true)
data class SiteSettingsData(
    @Json(name = "website_name") val websiteName: String? = null,
    @Json(name = "support_whatsapp") val supportWhatsapp: String? = null,
    @Json(name = "support_email") val supportEmail: String? = null,
    val announcement: String? = null,
    @Json(name = "maintenance_mode") val maintenanceMode: Boolean = false,
    @Json(name = "hero_title") val heroTitle: String? = null,
    @Json(name = "hero_subtitle") val heroSubtitle: String? = null,
    @Json(name = "footer_text") val footerText: String? = null,
    @Json(name = "currency_label") val currencyLabel: String? = "LKR",
    @Json(name = "min_topup_amount") val minTopupAmount: Double? = 100.0,
    @Json(name = "order_processing_message") val orderProcessingMessage: String? = null,
    val terms: String? = null,
    val privacy: String? = null,
    @Json(name = "refund_policy") val refundPolicy: String? = null
)

@JsonClass(generateAdapter = true)
data class PaymentMethodRow(
    val id: String,
    val data: PaymentMethodData? = null
)

@JsonClass(generateAdapter = true)
data class PaymentMethodData(
    val name: String? = null,
    val number: String? = null,
    @Json(name = "holder_name") val holderName: String? = null,
    val bank: String? = null,
    val holder: String? = null,
    @Json(name = "account_number") val accountNumber: String? = null,
    val branch: String? = null,
    @Json(name = "branch_code") val branchCode: String? = null,
    @Json(name = "min_amount") val minAmount: Double? = null,
    val instructions: String? = null,
    val note: String? = null,
    val enabled: Boolean = false
)

@JsonClass(generateAdapter = true)
data class WalletRow(
    val balance: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class WalletTransaction(
    val id: String,
    @Json(name = "user_id") val userId: String? = null,
    val type: String = "",
    val amount: Double = 0.0,
    @Json(name = "balance_before") val balanceBefore: Double = 0.0,
    @Json(name = "balance_after") val balanceAfter: Double = 0.0,
    val description: String = "",
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "topup_id") val topupId: String? = null,
    @Json(name = "created_by_admin") val createdByAdmin: String? = null,
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class WalletTopupRequest(
    val id: String,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "payment_method") val paymentMethod: String = "",
    @Json(name = "submitted_amount") val submittedAmount: Double = 0.0,
    @Json(name = "transaction_reference") val transactionReference: String = "",
    @Json(name = "receipt_image") val receiptImage: String? = null,
    val status: String = "pending",
    @Json(name = "rejection_reason") val rejectionReason: String? = null,
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class OrderPackageSnapshot(
    val game: String? = null,
    val diamonds: Int? = null,
    @Json(name = "processing_time") val processingTime: String? = null
)

@JsonClass(generateAdapter = true)
data class Order(
    val id: String,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "customer_email") val customerEmail: String? = null,
    @Json(name = "product_id") val productId: String? = null,
    @Json(name = "product_name_snapshot") val productNameSnapshot: String = "",
    @Json(name = "product_price_snapshot") val productPriceSnapshot: Double = 0.0,
    @Json(name = "product_image_snapshot") val productImageSnapshot: String = "",
    @Json(name = "product_package_snapshot") val productPackageSnapshot: OrderPackageSnapshot? = null,
    val quantity: Int = 1,
    @Json(name = "player_uid") val playerUid: String = "",
    @Json(name = "verified_player_name") val verifiedPlayerName: String = "",
    val server: String = "Singapore",
    @Json(name = "whatsapp_number") val whatsappNumber: String = "",
    @Json(name = "order_note") val orderNote: String? = null,
    val status: String = "pending",
    @Json(name = "wallet_transaction_id") val walletTransactionId: String? = null,
    @Json(name = "cart_group_id") val cartGroupId: String? = null,
    @Json(name = "verification_date") val verificationDate: String? = null,
    @Json(name = "processing_date") val processingDate: String? = null,
    @Json(name = "completed_date") val completedDate: String? = null,
    @Json(name = "cancelled_date") val cancelledDate: String? = null,
    @Json(name = "cancellation_reason") val cancellationReason: String? = null,
    @Json(name = "created_at") val createdAt: String = ""
) {
    val totalAmount: Double get() = productPriceSnapshot * quantity
}

@JsonClass(generateAdapter = true)
data class VerifyPlayerResponse(
    val uid: String = "",
    val name: String = "",
    val server: String = "Singapore"
)

@JsonClass(generateAdapter = true)
data class CheckoutResponse(
    val orders: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SupabaseErrorResponse(
    val error: String? = null,
    @Json(name = "error_description") val errorDescription: String? = null,
    @Json(name = "error_code") val errorCode: String? = null,
    val msg: String? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminLoginResponse(
    val token: String
)

@JsonClass(generateAdapter = true)
data class AdminOverview(
    @Json(name = "total_orders") val totalOrders: Int = 0,
    @Json(name = "pending_orders") val pendingOrders: Int = 0,
    @Json(name = "revenue_today") val revenueToday: Double = 0.0,
    @Json(name = "pending_topups") val pendingTopups: Int = 0,
    @Json(name = "total_users") val totalUsers: Int = 0,
    @Json(name = "wallet_liability") val walletLiability: Double = 0.0,
    @Json(name = "recent_orders") val recentOrders: List<Order> = emptyList(),
    @Json(name = "recent_topups") val recentTopups: List<WalletTopupRequest> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AdminCustomer(
    val id: String,
    @Json(name = "full_name") val fullName: String? = null,
    val email: String? = null,
    @Json(name = "whatsapp_number") val whatsappNumber: String? = null,
    val balance: Double = 0.0,
    @Json(name = "order_count") val orderCount: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SignedReceiptResponse(
    val signedUrl: String? = null
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
