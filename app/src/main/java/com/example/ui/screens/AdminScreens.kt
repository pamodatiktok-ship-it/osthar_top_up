package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.AdminCustomer
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.WalletTopupRequest
import com.example.ui.AdminPanelUiState
import com.example.ui.AdminTab
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
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.Formatters

@Composable
fun AdminPanelScreen(
    adminToken: String?,
    state: AdminPanelUiState,
    onBack: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSelectTab: (AdminTab) -> Unit,
    onFilterChange: (status: String, search: String) -> Unit,
    onRefresh: () -> Unit,
    onChangePage: (Int) -> Unit,
    onUpdateOrderStatus: (orderId: String, status: String, reason: String, onDone: () -> Unit) -> Unit,
    onSaveOrderNote: (orderId: String, note: String) -> Unit,
    onReviewTopup: (topupId: String, status: String, amount: Double?, note: String, onDone: () -> Unit) -> Unit,
    onViewReceipt: (path: String) -> Unit,
    onClearReceipt: () -> Unit,
    onAdjustWallet: (userId: String, amount: Double, note: String, onDone: () -> Unit) -> Unit,
    onSaveProduct: (
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
    ) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onRetryNotifications: () -> Unit
) {
    if (adminToken.isNullOrBlank()) {
        AdminLoginView(
            password = state.loginPassword,
            busy = state.loginBusy,
            error = state.loginError,
            onBack = onBack,
            onPasswordChange = onPasswordChange,
            onLogin = onLogin
        )
        return
    }

    LaunchedEffect(adminToken) {
        onRefresh()
    }

    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var selectedTopup by remember { mutableStateOf<WalletTopupRequest?>(null) }
    var selectedCustomer by remember { mutableStateOf<AdminCustomer?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var creatingProduct by remember { mutableStateOf(false) }

    state.receiptSignedUrl?.let { signedUrl ->
        AlertDialog(
            onDismissRequest = onClearReceipt,
            containerColor = SurfaceCard,
            title = { Text("Customer Payment Receipt", color = TextPrimary) },
            text = {
                AsyncImage(
                    model = signedUrl,
                    contentDescription = "Customer uploaded receipt",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            },
            confirmButton = {
                TextButton(onClick = onClearReceipt) {
                    Text("Close", color = CyanPrimary)
                }
            }
        )
    }

    selectedOrder?.let { order ->
        AdminOrderDialog(
            order = order,
            busy = state.actionBusy,
            onDismiss = { selectedOrder = null },
            onSaveNote = { note -> onSaveOrderNote(order.id, note) },
            onMarkStatus = { newStatus, reason ->
                onUpdateOrderStatus(order.id, newStatus, reason) { selectedOrder = null }
            }
        )
    }

    selectedTopup?.let { topup ->
        AdminTopupDialog(
            topup = topup,
            busy = state.actionBusy,
            onDismiss = { selectedTopup = null },
            onViewReceipt = { path -> onViewReceipt(path) },
            onReview = { status, amount, note ->
                onReviewTopup(topup.id, status, amount, note) { selectedTopup = null }
            }
        )
    }

    selectedCustomer?.let { customer ->
        AdminCustomerDialog(
            customer = customer,
            busy = state.actionBusy,
            onDismiss = { selectedCustomer = null },
            onAdjust = { amount, note ->
                onAdjustWallet(customer.id, amount, note) { selectedCustomer = null }
            }
        )
    }

    if (creatingProduct || editingProduct != null) {
        AdminProductDialog(
            initial = editingProduct,
            busy = state.actionBusy,
            onDismiss = {
                creatingProduct = false
                editingProduct = null
            },
            onSave = { id, name, game, category, diamonds, regular, sale, stock, active, featured, order, img, time ->
                onSaveProduct(id, name, game, category, diamonds, regular, sale, stock, active, featured, order, img, time) {
                    creatingProduct = false
                    editingProduct = null
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiquidIconCircleButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack
                    )
                    Column {
                        Text("SUPERADMIN", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                        Text("Admin Control", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh admin data", tint = CyanPrimary)
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_button")) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Admin sign out", tint = RedError)
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AdminTab.entries) { tab ->
                    FilterChip(
                        selected = state.activeTab == tab,
                        onClick = { onSelectTab(tab) },
                        label = { Text(tab.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceCard,
                            labelColor = TextMuted,
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.16f),
                            selectedLabelColor = CyanPrimary
                        )
                    )
                }
            }
        }

        if (state.activeTab != AdminTab.OVERVIEW) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onFilterChange(state.statusFilter, it) },
                        singleLine = true,
                        placeholder = { Text("Search UID, reference, name, ID…", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = TextMuted) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.activeTab == AdminTab.ORDERS) {
                        val filters = listOf("all", "pending", "processing", "completed", "cancelled")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filters) { f ->
                                FilterChip(
                                    selected = state.statusFilter == f,
                                    onClick = { onFilterChange(f, state.searchQuery) },
                                    label = { Text(f.replaceFirstChar { it.uppercase() }) },
                                    colors = adminChipColors()
                                )
                            }
                        }
                    } else if (state.activeTab == AdminTab.TOPUPS) {
                        val filters = listOf("all", "pending", "approved", "rejected")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filters) { f ->
                                FilterChip(
                                    selected = state.statusFilter == f,
                                    onClick = { onFilterChange(f, state.searchQuery) },
                                    label = { Text(f.replaceFirstChar { it.uppercase() }) },
                                    colors = adminChipColors()
                                )
                            }
                        }
                    } else if (state.activeTab == AdminTab.PRODUCTS) {
                        OstharPrimaryButton(
                            text = "Add New Product",
                            onClick = { creatingProduct = true },
                            icon = Icons.Rounded.Add,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (state.loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            }
        } else if (state.error != null) {
            item {
                ErrorStateCard("Admin request failed", state.error, onRetry = onRefresh)
            }
        } else {
            when (state.activeTab) {
                AdminTab.OVERVIEW -> {
                    val ov = state.overview
                    if (ov == null) {
                        item { EmptyStateCard("Loading dashboard", "Tap refresh to load overview.") }
                    } else {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MetricCard("Total Orders", ov.totalOrders.toString(), Modifier.weight(1f))
                                    MetricCard("Pending Orders", ov.pendingOrders.toString(), Modifier.weight(1f), highlight = ov.pendingOrders > 0)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MetricCard("Pending Top-Ups", ov.pendingTopups.toString(), Modifier.weight(1f), highlight = ov.pendingTopups > 0)
                                    MetricCard("Customers", ov.totalUsers.toString(), Modifier.weight(1f))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MetricCard("Revenue Today", Formatters.money(ov.revenueToday), Modifier.weight(1f))
                                    MetricCard("Wallet Liability", Formatters.money(ov.walletLiability), Modifier.weight(1f))
                                }
                                OstharSecondaryButton(
                                    text = "Retry Pending Notifications",
                                    onClick = onRetryNotifications,
                                    enabled = !state.actionBusy,
                                    icon = Icons.Rounded.NotificationsActive,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (ov.recentOrders.isNotEmpty()) {
                            item { Text("Recent Orders", style = MaterialTheme.typography.titleMedium, color = TextPrimary) }
                            items(ov.recentOrders, key = { "ov_o_${it.id}" }) { o ->
                                AdminOrderRow(o, onClick = { selectedOrder = o })
                            }
                        }
                        if (ov.recentTopups.isNotEmpty()) {
                            item { Text("Recent Top-Up Requests", style = MaterialTheme.typography.titleMedium, color = TextPrimary) }
                            items(ov.recentTopups, key = { "ov_t_${it.id}" }) { t ->
                                AdminTopupRow(t, onClick = { selectedTopup = t })
                            }
                        }
                    }
                }

                AdminTab.ORDERS -> {
                    if (state.orders.isEmpty()) {
                        item { EmptyStateCard("No orders found", "Adjust search or status filter.") }
                    } else {
                        items(state.orders, key = { it.id }) { o ->
                            AdminOrderRow(o, onClick = { selectedOrder = o })
                        }
                        item {
                            AdminPager(state.page, state.orders.size, state.totalRecords, onChangePage)
                        }
                    }
                }

                AdminTab.TOPUPS -> {
                    if (state.topups.isEmpty()) {
                        item { EmptyStateCard("No top-up requests", "Adjust search or status filter.") }
                    } else {
                        items(state.topups, key = { it.id }) { t ->
                            AdminTopupRow(t, onClick = { selectedTopup = t })
                        }
                        item {
                            AdminPager(state.page, state.topups.size, state.totalRecords, onChangePage)
                        }
                    }
                }

                AdminTab.PRODUCTS -> {
                    if (state.products.isEmpty()) {
                        item { EmptyStateCard("No products found", "Add a product or clear search.") }
                    } else {
                        items(state.products, key = { it.id }) { p ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceCard,
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth().clickable { editingProduct = p }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        Text(
                                            "${p.game} · ${Formatters.money(p.effectivePrice)} · ${if (p.isInStock) "In stock" else "Out"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                    IconButton(onClick = { onDeleteProduct(p.id) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete product", tint = RedError)
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.CUSTOMERS -> {
                    if (state.customers.isEmpty()) {
                        item { EmptyStateCard("No customers found", "Search by name or email.") }
                    } else {
                        items(state.customers, key = { it.id }) { c ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceCard,
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth().clickable { selectedCustomer = c }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(c.fullName?.ifBlank { null } ?: c.email ?: c.id, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        Text("${c.email.orEmpty()} · ${c.orderCount} orders", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                    Text(Formatters.money(c.balance), style = MaterialTheme.typography.titleSmall, color = CyanPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLoginView(
    password: String,
    busy: Boolean,
    error: String?,
    onBack: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp).padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    LiquidIconCircleButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack
                    )
                }
                Spacer(Modifier.height(16.dp))
                OstharBrandLogo(logoSize = 50)
                Spacer(Modifier.height(14.dp))
                Text("Admin Operations", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Enter your Superadmin password to review top-ups, process orders, and manage products.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(Modifier.height(20.dp))

                LiquidEntrance {
                    LiquidFloatingHeroCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = onPasswordChange,
                                enabled = !busy,
                                singleLine = true,
                                label = { Text("Superadmin Password") },
                                placeholder = { Text("Enter ADMIN_PASSWORD", color = TextMuted) },
                                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { visible = !visible }) {
                                        Icon(
                                            imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Toggle password",
                                            tint = TextMuted
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BgDeep.copy(alpha = 0.7f),
                                    unfocusedContainerColor = BgDeep.copy(alpha = 0.55f),
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                            )

                            error?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = RedError)
                            }

                            OstharPrimaryButton(
                                text = "Sign In as Admin",
                                onClick = onLogin,
                                loading = busy,
                                icon = Icons.Rounded.AdminPanelSettings,
                                modifier = Modifier.fillMaxWidth().testTag("admin_login_submit")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        highlighted = highlight
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = if (highlight) CyanPrimary else TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminOrderRow(o: Order, onClick: () -> Unit) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${o.productNameSnapshot} × ${o.quantity}", style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("UID ${o.playerUid} · ${o.verifiedPlayerName}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("${Formatters.money(o.totalAmount)} · ${Formatters.formatDate(o.createdAt)}", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
            }
            Spacer(Modifier.width(8.dp))
            StatusBadge(o.status)
        }
    }
}

@Composable
private fun AdminTopupRow(t: WalletTopupRequest, onClick: () -> Unit) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(Formatters.money(t.submittedAmount), style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text("${if (t.paymentMethod == "ez_cash") "eZ Cash" else "Bank"} · ${t.transactionReference}", style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(Formatters.formatDate(t.createdAt), style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Spacer(Modifier.width(8.dp))
            StatusBadge(t.status)
        }
    }
}

@Composable
private fun AdminOrderDialog(
    order: Order,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit,
    onMarkStatus: (status: String, reason: String) -> Unit
) {
    var note by remember(order.id) { mutableStateOf("") }
    var cancelReason by remember(order.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = { Text("Order #${order.id.take(8).uppercase()}", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusBadge(order.status)
                Text("${order.productNameSnapshot} × ${order.quantity} (${Formatters.money(order.totalAmount)})", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                Text("UID: ${order.playerUid} (${order.verifiedPlayerName})", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                Text("WhatsApp: ${order.whatsappNumber}", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                order.orderNote?.takeIf { it.isNotBlank() }?.let {
                    Text("Customer note: $it", color = CyanPrimary, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider(color = BorderSubtle)
                OstharTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Internal Admin Note",
                    placeholder = "Optional internal note",
                    singleLine = false
                )
                OstharSecondaryButton(
                    text = "Save Note",
                    onClick = { onSaveNote(note) },
                    enabled = !busy && note.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (order.status !in listOf("completed", "cancelled")) {
                    HorizontalDivider(color = BorderSubtle)
                    if (order.status == "pending") {
                        OstharSecondaryButton(
                            text = "Mark Processing",
                            onClick = { onMarkStatus("processing", "") },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OstharPrimaryButton(
                        text = "Mark Completed",
                        onClick = { onMarkStatus("completed", "") },
                        enabled = !busy,
                        loading = busy,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OstharTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = "Cancellation Reason",
                        placeholder = "Explain why cancelled"
                    )
                    OstharSecondaryButton(
                        text = "Cancel & Refund Wallet",
                        onClick = { onMarkStatus("cancelled", cancelReason) },
                        enabled = !busy && cancelReason.trim().length >= 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextMuted) }
        }
    )
}

@Composable
private fun AdminTopupDialog(
    topup: WalletTopupRequest,
    busy: Boolean,
    onDismiss: () -> Unit,
    onViewReceipt: (String) -> Unit,
    onReview: (status: String, amount: Double?, note: String) -> Unit
) {
    var amountText by remember(topup.id) { mutableStateOf(topup.submittedAmount.toString()) }
    var note by remember(topup.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = { Text("Top-Up Review", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusBadge(topup.status)
                Text("Submitted: ${Formatters.money(topup.submittedAmount)}", color = CyanPrimary, style = MaterialTheme.typography.titleMedium)
                Text("Method: ${topup.paymentMethod}", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                Text("Reference: ${topup.transactionReference}", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                topup.receiptImage?.takeIf { it.isNotBlank() }?.let { path ->
                    OstharSecondaryButton(
                        text = "View Receipt Image",
                        onClick = { onViewReceipt(path) },
                        enabled = !busy,
                        icon = Icons.Rounded.Receipt,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (topup.status == "pending") {
                    HorizontalDivider(color = BorderSubtle)
                    OstharTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = "Approved Amount (LKR)",
                        placeholder = "1500.00",
                        keyboardType = KeyboardType.Decimal
                    )
                    OstharTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = "Admin Note / Rejection Reason",
                        placeholder = "Required if rejecting",
                        singleLine = false
                    )
                    val parsed = amountText.toDoubleOrNull()
                    OstharPrimaryButton(
                        text = "Approve & Credit",
                        onClick = { if (parsed != null && parsed > 0) onReview("approved", parsed, note) },
                        enabled = !busy && parsed != null && parsed > 0,
                        loading = busy,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OstharSecondaryButton(
                        text = "Reject Request",
                        onClick = { onReview("rejected", null, note) },
                        enabled = !busy && note.trim().length >= 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextMuted) }
        }
    )
}

@Composable
private fun AdminCustomerDialog(
    customer: AdminCustomer,
    busy: Boolean,
    onDismiss: () -> Unit,
    onAdjust: (amount: Double, note: String) -> Unit
) {
    var amountText by remember(customer.id) { mutableStateOf("") }
    var note by remember(customer.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = { Text(customer.fullName?.ifBlank { null } ?: "Customer", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(customer.email.orEmpty(), color = TextMuted, style = MaterialTheme.typography.bodySmall)
                Text("Balance: ${Formatters.money(customer.balance)} · ${customer.orderCount} orders", color = CyanPrimary, style = MaterialTheme.typography.titleSmall)
                HorizontalDivider(color = BorderSubtle)
                OstharTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Wallet Adjustment (+/- LKR)",
                    placeholder = "e.g. 500 or -200",
                    keyboardType = KeyboardType.Number
                )
                OstharTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Reason *",
                    placeholder = "Reason for adjustment"
                )
                val amt = amountText.toDoubleOrNull()
                OstharPrimaryButton(
                    text = "Apply Adjustment",
                    onClick = { if (amt != null && amt != 0.0) onAdjust(amt, note) },
                    enabled = !busy && amt != null && amt != 0.0 && note.trim().length >= 2,
                    loading = busy,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextMuted) }
        }
    )
}

@Composable
private fun AdminProductDialog(
    initial: Product?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var game by remember { mutableStateOf(initial?.game ?: "Free Fire") }
    var category by remember { mutableStateOf(initial?.category ?: "Diamonds") }
    var diamonds by remember { mutableStateOf((initial?.diamondAmount ?: 100).toString()) }
    var regular by remember { mutableStateOf((initial?.regularPrice ?: 0.0).toString()) }
    var sale by remember { mutableStateOf(initial?.salePrice?.toString() ?: "") }
    var inStock by remember { mutableStateOf(initial?.isInStock ?: true) }
    var active by remember { mutableStateOf(initial?.active ?: true) }
    var featured by remember { mutableStateOf(initial?.featured ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = { Text(if (initial == null) "Add Product" else "Edit Product", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OstharTextField(value = name, onValueChange = { name = it }, label = "Name *", placeholder = "100 Diamonds")
                OstharTextField(value = game, onValueChange = { game = it }, label = "Game *", placeholder = "Free Fire")
                OstharTextField(value = category, onValueChange = { category = it }, label = "Category", placeholder = "Diamonds")
                OstharTextField(value = diamonds, onValueChange = { diamonds = it }, label = "Diamond Amount", placeholder = "100", keyboardType = KeyboardType.Number)
                OstharTextField(value = regular, onValueChange = { regular = it }, label = "Regular Price (LKR) *", placeholder = "350", keyboardType = KeyboardType.Decimal)
                OstharTextField(value = sale, onValueChange = { sale = it }, label = "Sale Price (optional)", placeholder = "320", keyboardType = KeyboardType.Decimal)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("In Stock", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    Switch(checked = inStock, onCheckedChange = { inStock = it }, colors = SwitchDefaults.colors(checkedThumbColor = BgDeep, checkedTrackColor = CyanPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Active", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    Switch(checked = active, onCheckedChange = { active = it }, colors = SwitchDefaults.colors(checkedThumbColor = BgDeep, checkedTrackColor = CyanPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Featured", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    Switch(checked = featured, onCheckedChange = { featured = it }, colors = SwitchDefaults.colors(checkedThumbColor = BgDeep, checkedTrackColor = CyanPrimary))
                }
            }
        },
        confirmButton = {
            val regPrice = regular.toDoubleOrNull()
            val salePrice = sale.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            val valid = name.isNotBlank() && game.isNotBlank() && regPrice != null && regPrice > 0 &&
                (salePrice == null || (salePrice > 0 && salePrice < regPrice))
            TextButton(
                enabled = !busy && valid,
                onClick = {
                    onSave(
                        initial?.id,
                        name,
                        game,
                        category,
                        diamonds.toIntOrNull() ?: 0,
                        regPrice!!,
                        salePrice,
                        if (inStock) "in_stock" else "out_of_stock",
                        active,
                        featured,
                        initial?.displayOrder ?: 0,
                        initial?.image ?: "/assets/diamonds.png",
                        initial?.processingTime ?: "5-15 minutes"
                    )
                }
            ) {
                Text("Save", color = if (valid) CyanPrimary else TextMuted, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

@Composable
private fun AdminPager(page: Int, count: Int, total: Int, onChangePage: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onChangePage(-1) }, enabled = page > 0) {
            Text("← Previous", color = if (page > 0) CyanPrimary else TextMuted)
        }
        Text("Page ${page + 1} · $total records", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        TextButton(onClick = { onChangePage(1) }, enabled = (page + 1) * 25 < total && count > 0) {
            Text("Next →", color = if ((page + 1) * 25 < total) CyanPrimary else TextMuted)
        }
    }
}

@Composable
private fun adminChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = SurfaceCard,
    labelColor = TextMuted,
    selectedContainerColor = CyanPrimary.copy(alpha = 0.16f),
    selectedLabelColor = CyanPrimary
)
