package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.PaymentMethodRow
import com.example.data.model.SiteSettingsData
import com.example.data.model.UiState
import com.example.data.model.WalletTopupRequest
import com.example.data.model.WalletTransaction
import com.example.ui.TopUpFormState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.LiquidEntrance
import com.example.ui.components.LiquidFloatingHeroCard
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidIconCircleButton
import com.example.ui.components.OstharBrandLogo
import com.example.ui.components.OstharPrimaryButton
import com.example.ui.components.OstharSecondaryButton
import com.example.ui.components.OstharTextField
import com.example.ui.components.StatusBadge
import com.example.ui.components.liquidPress
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassSpecularRim
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.Formatters

@Composable
fun WalletOverviewScreen(
    isLoggedIn: Boolean,
    balance: Double,
    balanceLoading: Boolean,
    transactionsState: UiState<List<WalletTransaction>>,
    topupRequests: List<WalletTopupRequest>,
    onLoginClick: () -> Unit,
    onOpenTopUp: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenReceipt: (String) -> Unit,
    onOpenStore: () -> Unit,
    onRefresh: () -> Unit
) {
    if (!isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            EmptyStateCard(
                title = "Your LKR Gaming Wallet",
                description = "Sign in to view your balance, recharge via eZ Cash or Bank Transfer, and inspect receipts.",
                icon = Icons.Rounded.Lock,
                actionText = "Login to Wallet",
                onAction = onLoginClick
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("YOUR WALLET", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Your game-time balance.", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            }
        }

        // Hero Wallet Balance Card
        item {
            LiquidEntrance {
                LiquidFloatingHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Wallet, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                Text("Available balance", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = if (balanceLoading) "LKR …" else Formatters.money(balance),
                            style = MaterialTheme.typography.displayMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OstharPrimaryButton(
                                text = "Top Up Wallet",
                                onClick = onOpenTopUp,
                                icon = Icons.Rounded.Add,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("wallet_topup_button")
                            )
                            OstharSecondaryButton(
                                text = "History",
                                onClick = onOpenHistory,
                                icon = Icons.Rounded.History,
                                modifier = Modifier
                                    .weight(0.8f)
                                    .testTag("wallet_history_button")
                            )
                        }
                    }
                }
            }
        }

        // Play with confidence info card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                onClick = onOpenStore
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PLAY WITH CONFIDENCE", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text("Your funds stay yours.", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Use your wallet for verified orders. Cancel any pending order and funds return automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Pending / Recent Top-up Requests
        if (topupRequests.isNotEmpty()) {
            item {
                Text("Recent Top-Up Requests", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            items(topupRequests.take(5), key = { it.id }) { req ->
                TopupRequestRow(req)
            }
        }

        // Recent Activity
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    text = "View all →",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenHistory)
                        .padding(6.dp)
                )
            }
        }

        when (transactionsState) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CyanPrimary)
                    }
                }
            }
            is UiState.Error -> {
                item {
                    ErrorStateCard("Could not load transactions", transactionsState.message, onRetry = onRefresh)
                }
            }
            is UiState.Success -> {
                val recent = transactionsState.data.take(5)
                if (recent.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "A clean slate.",
                            description = "Your wallet transactions will appear here after your first top-up."
                        )
                    }
                } else {
                    items(recent, key = { it.id }) { tx ->
                        TransactionRowCard(tx = tx, onClick = { onOpenReceipt(tx.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun WalletTopUpScreen(
    paymentMethods: List<PaymentMethodRow>,
    settings: SiteSettingsData?,
    topupRequests: List<WalletTopupRequest>,
    form: TopUpFormState,
    onBack: () -> Unit,
    onSelectMethod: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onReferenceChange: (String) -> Unit,
    onReceiptSelected: (android.net.Uri?, String) -> Unit,
    onSubmit: (Context) -> Unit
) {
    val context = LocalContext.current
    val selectedPayment = paymentMethods.find { it.id == form.selectedMethod }?.data
    val minAmount = maxOf(selectedPayment?.minAmount ?: 0.0, settings?.minTopupAmount ?: 100.0)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onReceiptSelected(uri, "Receipt attached")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidIconCircleButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
                Column {
                    Text("WALLET TOP-UP", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                    Text("Fuel your next win.", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                }
            }
        }

        // Method Selector
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("ez_cash" to "eZ Cash", "bank_transfer" to "Bank Transfer").forEach { (id, label) ->
                    val selected = form.selectedMethod == id
                    val interaction = remember { MutableInteractionSource() }
                    val bgColor by animateColorAsState(
                        targetValue = if (selected) CyanPrimary.copy(alpha = 0.16f) else SurfaceCard,
                        animationSpec = tween(220),
                        label = "method_bg_$id"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (selected) CyanPrimary else BorderSubtle,
                        animationSpec = tween(220),
                        label = "method_border_$id"
                    )
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .liquidPress(interaction, pressedScale = 0.96f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = interaction,
                                indication = null,
                                onClick = { onSelectMethod(id) }
                            )
                            .testTag("topup_method_$id"),
                        shape = RoundedCornerShape(16.dp),
                        color = bgColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (id == "ez_cash") Icons.Rounded.PhoneAndroid else Icons.Rounded.AccountBalance,
                                contentDescription = null,
                                tint = if (selected) CyanPrimary else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) CyanPrimary else TextMuted)
                        }
                    }
                }
            }
        }

        // Payment Details Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(selectedPayment?.name ?: "Payment details", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    val entries = listOfNotNull(
                        selectedPayment?.number?.takeIf { it.isNotBlank() }?.let { "Number" to it },
                        selectedPayment?.holderName?.takeIf { it.isNotBlank() }?.let { "Holder name" to it },
                        selectedPayment?.bank?.takeIf { it.isNotBlank() }?.let { "Bank" to it },
                        selectedPayment?.holder?.takeIf { it.isNotBlank() }?.let { "Account holder" to it },
                        selectedPayment?.accountNumber?.takeIf { it.isNotBlank() }?.let { "Account number" to it },
                        selectedPayment?.branch?.takeIf { it.isNotBlank() }?.let { "Branch" to it },
                        selectedPayment?.branchCode?.takeIf { it.isNotBlank() }?.let { "Branch code" to it }
                    )
                    entries.forEach { (k, v) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(k, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(v, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            if (k in listOf("Number", "Account number")) {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText(k, v))
                                }) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy $k", tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    selectedPayment?.instructions?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    if (selectedPayment?.enabled != true) {
                        Text(
                            "This method is not accepting payments yet. Do not send funds until enabled.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberWarning
                        )
                    }
                }
            }
        }

        // Top-up Form
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OstharTextField(
                        value = form.amount,
                        onValueChange = onAmountChange,
                        label = "Amount (LKR) *",
                        placeholder = "1500.00",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.testTag("topup_amount_input")
                    )
                    Text("Minimum ${Formatters.money(minAmount)}", style = MaterialTheme.typography.labelSmall, color = TextMuted)

                    OstharTextField(
                        value = form.reference,
                        onValueChange = onReferenceChange,
                        label = "Transaction / Reference Number *",
                        placeholder = "Enter your payment reference",
                        modifier = Modifier.testTag("topup_reference_input")
                    )

                    Column {
                        Text("Receipt Image (optional)", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("topup_receipt_picker"),
                            shape = RoundedCornerShape(14.dp),
                            color = BgDeep,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (form.receiptUri != null) {
                                    AsyncImage(
                                        model = form.receiptUri,
                                        contentDescription = "Selected receipt image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text("Tap to change receipt", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                                } else {
                                    Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(26.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text("Choose a payment receipt", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    Text("JPG, PNG or WebP · Up to 5 MB", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }
                        }
                    }

                    form.error?.let { err ->
                        Text(err, style = MaterialTheme.typography.bodySmall, color = RedError)
                    }

                    OstharPrimaryButton(
                        text = "Submit Top-Up Request",
                        onClick = { onSubmit(context) },
                        enabled = selectedPayment?.enabled == true,
                        loading = form.submitting,
                        icon = Icons.Rounded.VerifiedUser,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topup_submit_button")
                    )

                    Text(
                        "Creates a pending request. Funds are credited after admin verification.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        if (topupRequests.isNotEmpty()) {
            item {
                Text("Recent Requests", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            items(topupRequests, key = { it.id }) { req ->
                TopupRequestRow(req)
            }
        }
    }
}

@Composable
fun WalletHistoryScreen(
    transactionsState: UiState<List<WalletTransaction>>,
    onBack: () -> Unit,
    onOpenReceipt: (String) -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidIconCircleButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
                Column {
                    Text("WALLET LEDGER", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                    Text("Every rupee. Accounted for.", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                }
            }
        }

        when (transactionsState) {
            is UiState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            }
            is UiState.Error -> item {
                ErrorStateCard("Could not load ledger", transactionsState.message, onRetry = onRetry)
            }
            is UiState.Success -> {
                if (transactionsState.data.isEmpty()) {
                    item {
                        EmptyStateCard("No wallet transactions yet", "Your top-ups, orders, and refunds will appear here.")
                    }
                } else {
                    items(transactionsState.data, key = { it.id }) { tx ->
                        TransactionRowCard(tx = tx, onClick = { onOpenReceipt(tx.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptScreen(
    receiptState: UiState<WalletTransaction>,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidIconCircleButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
                Text("Transaction Receipt", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            }
        }

        when (receiptState) {
            is UiState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            }
            is UiState.Error -> item {
                ErrorStateCard("Receipt unavailable", receiptState.message, onRetry = onRetry)
            }
            is UiState.Success -> {
                val tx = receiptState.data
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceCard,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OstharBrandLogo()
                            Text("Wallet Transaction Receipt", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                            Text(Formatters.formatDate(tx.createdAt), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                text = Formatters.money(tx.amount),
                                style = MaterialTheme.typography.displayMedium,
                                color = if (tx.amount >= 0) EmeraldSuccess else CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(color = BorderSubtle)
                            ReceiptLine("Transaction ID", tx.id)
                            ReceiptLine("Type", tx.type.replace("_", " "))
                            ReceiptLine("Description", tx.description)
                            ReceiptLine("Balance Before", Formatters.money(tx.balanceBefore))
                            ReceiptLine("Balance After", Formatters.money(tx.balanceAfter))
                            ReceiptLine("Related Order", tx.orderId ?: "—")
                            ReceiptLine("Related Top-Up", tx.topupId ?: "—")
                            ReceiptLine("Created By", tx.createdByAdmin ?: "Customer")
                            HorizontalDivider(color = BorderSubtle)
                            Text(
                                "This receipt records a wallet movement. It is not evidence of completed game delivery.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            OstharPrimaryButton(
                                text = "Share Receipt",
                                onClick = {
                                    val shareText = buildString {
                                        appendLine("OSTHAR TOP UP — Wallet Receipt")
                                        appendLine("Transaction ID: ${tx.id}")
                                        appendLine("Date: ${Formatters.formatDate(tx.createdAt)}")
                                        appendLine("Amount: ${Formatters.money(tx.amount)}")
                                        appendLine("Type: ${tx.type.replace("_", " ")}")
                                        appendLine("Description: ${tx.description}")
                                        appendLine("Balance After: ${Formatters.money(tx.balanceAfter)}")
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Receipt"))
                                },
                                icon = Icons.Rounded.Share,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}

@Composable
private fun TransactionRowCard(tx: WalletTransaction, onClick: () -> Unit) {
    val isCredit = tx.amount > 0
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        accentGlow = if (isCredit) EmeraldSuccess else CyanPrimary,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = (if (isCredit) EmeraldSuccess else CyanPrimary).copy(alpha = 0.14f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlassSpecularRim),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCredit) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                        tint = if (isCredit) EmeraldSuccess else CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.description.ifBlank { tx.type.replace("_", " ") },
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(Formatters.formatDate(tx.createdAt), style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.money(tx.amount),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCredit) EmeraldSuccess else CyanPrimary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Text("Receipt", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun TopupRequestRow(req: WalletTopupRequest) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(Formatters.money(req.submittedAmount), style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(
                    "${if (req.paymentMethod == "ez_cash") "eZ Cash" else "Bank Transfer"} · ${req.transactionReference}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(Formatters.formatDate(req.createdAt), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                req.rejectionReason?.takeIf { it.isNotBlank() }?.let { reason ->
                    Spacer(Modifier.height(4.dp))
                    Text(reason, style = MaterialTheme.typography.bodySmall, color = RedError)
                }
            }
            Spacer(Modifier.width(8.dp))
            StatusBadge(req.status)
        }
    }
}
