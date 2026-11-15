package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Order
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidIconCircleButton
import com.example.ui.components.OstharProductImage
import com.example.ui.components.OstharSecondaryButton
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
fun OrdersScreen(
    isLoggedIn: Boolean,
    ordersState: UiState<List<Order>>,
    statusFilter: String,
    searchQuery: String,
    onSelectStatus: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenOrder: (String) -> Unit,
    onBrowseStore: () -> Unit,
    onLoginClick: () -> Unit,
    onRetry: () -> Unit
) {
    if (!isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            EmptyStateCard(
                title = "Track Every Upgrade",
                description = "Sign in to view your Free Fire diamond orders and live delivery progress.",
                icon = Icons.Rounded.Lock,
                actionText = "Login to View Orders",
                onAction = onLoginClick
            )
        }
        return
    }

    val statuses = listOf("all", "pending", "processing", "completed", "cancelled")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text("YOUR ORDERS", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Every upgrade. In one place.", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(statuses) { status ->
                        FilterChip(
                            selected = statusFilter == status,
                            onClick = { onSelectStatus(status) },
                            label = { Text(status.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceCard,
                                labelColor = TextMuted,
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.16f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    singleLine = true,
                    placeholder = { Text("Search full order ID or Player UID", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = TextMuted)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("orders_search_input")
                )
            }
        }

        when (ordersState) {
            is UiState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            }
            is UiState.Error -> item {
                ErrorStateCard("Couldn't load orders", ordersState.message, onRetry = onRetry)
            }
            is UiState.Success -> {
                if (ordersState.data.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No orders here yet.",
                            description = "Your next upgrade is waiting in the store.",
                            icon = Icons.Rounded.Inventory2,
                            actionText = "Browse Packages",
                            onAction = onBrowseStore
                        )
                    }
                } else {
                    items(ordersState.data, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onOpenOrder(order.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit, modifier: Modifier = Modifier) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        cornerRadius = 20.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.id.take(8).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                StatusBadge(order.status)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OstharProductImage(
                    imagePath = order.productImageSnapshot,
                    contentDescription = order.productNameSnapshot,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${order.productNameSnapshot} × ${order.quantity}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${order.verifiedPlayerName} · ${order.playerUid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = Formatters.money(order.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(Icons.Rounded.ArrowOutward, contentDescription = null, tint = TextMuted)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(Modifier.height(8.dp))
            Text(
                text = Formatters.formatDate(order.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun OrderDetailScreen(
    orderDetailState: UiState<Order>,
    cancelling: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onCancelOrder: (String) -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    val activeOrder = (orderDetailState as? UiState.Success)?.data
    if (showCancelDialog && activeOrder != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = SurfaceCard,
            title = { Text("Cancel pending order?", color = TextPrimary) },
            text = {
                Text(
                    "The full payment (${Formatters.money(activeOrder.totalAmount)}) will be refunded to your wallet immediately.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    onCancelOrder(activeOrder.id)
                }) {
                    Text("Cancel & Refund", color = RedError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Order", color = TextPrimary)
                }
            }
        )
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
                Text("Order Details", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            }
        }

        when (orderDetailState) {
            is UiState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            }
            is UiState.Error -> item {
                ErrorStateCard("Order not found", orderDetailState.message, onRetry = onRetry)
            }
            is UiState.Success -> {
                val o = orderDetailState.data

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ORDER #${o.id.take(8).uppercase()}", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                            Text(Formatters.formatDate(o.createdAt), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        StatusBadge(o.status)
                    }
                }

                // Product Snapshot Card
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OstharProductImage(
                                imagePath = o.productImageSnapshot,
                                contentDescription = o.productNameSnapshot,
                                modifier = Modifier
                                    .size(78.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                o.productPackageSnapshot?.game?.let {
                                    Text(it.uppercase(), style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                                }
                                Text(o.productNameSnapshot, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(
                                    "Qty ${o.quantity}" + (o.productPackageSnapshot?.diamonds?.let { " · $it diamonds" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    Formatters.money(o.totalAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Timeline
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Order Timeline", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            TimelineRow("Order created", o.createdAt)
                            TimelineRow("Processing", o.processingDate)
                            TimelineRow(
                                label = if (o.status == "cancelled") "Cancelled & refunded" else "Completed",
                                time = if (o.status == "cancelled") o.cancelledDate else o.completedDate
                            )

                            o.cancellationReason?.takeIf { it.isNotBlank() }?.let { reason ->
                                Surface(
                                    color = RedError.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, RedError.copy(alpha = 0.25f))
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                        Text(reason, style = MaterialTheme.typography.bodySmall, color = RedError)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Refund: ${Formatters.money(o.totalAmount)}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = EmeraldSuccess
                                        )
                                    }
                                }
                            }

                            if (o.status == "pending") {
                                OstharSecondaryButton(
                                    text = if (cancelling) "Cancelling…" else "Cancel & Refund Order",
                                    onClick = { showCancelDialog = true },
                                    enabled = !cancelling,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("order_cancel_button")
                                )
                            }
                        }
                    }
                }

                // Delivery Details
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Delivery Details", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            DetailLine("Player UID", o.playerUid)
                            DetailLine("Verified Player", o.verifiedPlayerName)
                            DetailLine("Server", o.server)
                            DetailLine("WhatsApp", o.whatsappNumber)
                            DetailLine("Verification Date", Formatters.formatDate(o.verificationDate))
                            DetailLine("Order Note", o.orderNote?.ifBlank { null } ?: "No note supplied")
                        }
                    }
                }

                // Payment Snapshot
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Payment & Snapshot", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            DetailLine("Payment Method", "Wallet")
                            DetailLine("Price per Package", Formatters.money(o.productPriceSnapshot))
                            DetailLine("Wallet Transaction", o.walletTransactionId ?: "—")
                            DetailLine("Cart Group", o.cartGroupId ?: "—")
                            DetailLine("Delivery Estimate", o.productPackageSnapshot?.processingTime ?: "—")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(label: String, time: String?) {
    val active = !time.isNullOrBlank()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (active) CyanPrimary.copy(alpha = 0.14f) else BgDeep)
                .border(1.dp, if (active) CyanPrimary.copy(alpha = 0.45f) else BorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (active) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                contentDescription = null,
                tint = if (active) CyanPrimary else TextMuted,
                modifier = Modifier.size(15.dp)
            )
        }
        Column {
            Text(label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(
                if (active) Formatters.formatDate(time) else "Waiting",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}
