package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.ui.CheckoutFormState
import com.example.ui.components.CheckoutPanel
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.OstharProductImage
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.Formatters

@Composable
fun CartScreen(
    cart: List<CartItem>,
    isLoggedIn: Boolean,
    walletBalance: Double,
    checkoutForm: CheckoutFormState,
    onBrowseStore: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onUidChange: (String) -> Unit,
    onVerifyNow: () -> Unit,
    onWhatsappChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPay: () -> Unit,
    onLoginClick: () -> Unit,
    onTopUpClick: () -> Unit
) {
    val total = cart.sumOf { it.lineTotal }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("YOUR CART", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Ready for checkout.", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Review your packages and verify your Singapore Player UID.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        if (cart.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Your cart is waiting.",
                    description = "Explore the store to find your next top-up.",
                    icon = Icons.Rounded.ShoppingCart,
                    actionText = "Browse Store",
                    onAction = onBrowseStore
                )
            }
        } else {
            items(cart, key = { it.product.id }) { item ->
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    cornerRadius = 18.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OstharProductImage(
                            imagePath = item.product.image,
                            contentDescription = item.product.name,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenProduct(item.product.id) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.game.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = item.product.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable { onOpenProduct(item.product.id) }
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = Formatters.money(item.product.effectivePrice),
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BgDeep)
                                ) {
                                    IconButton(
                                        onClick = { onUpdateQuantity(item.product.id, item.quantity - 1) },
                                        modifier = Modifier.testTag("cart_minus_${item.product.id}")
                                    ) {
                                        Icon(Icons.Rounded.Remove, contentDescription = "Decrease quantity", tint = TextPrimary)
                                    }
                                    Text(
                                        text = item.quantity.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { onUpdateQuantity(item.product.id, item.quantity + 1) },
                                        modifier = Modifier.testTag("cart_plus_${item.product.id}")
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = "Increase quantity", tint = TextPrimary)
                                    }
                                }
                                IconButton(
                                    onClick = { onUpdateQuantity(item.product.id, 0) },
                                    modifier = Modifier.testTag("cart_remove_${item.product.id}")
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Remove ${item.product.name}",
                                        tint = RedError
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Order summary", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        cart.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${item.product.name} × ${item.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(Formatters.money(item.lineTotal), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                        HorizontalDivider(color = BorderSubtle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(Formatters.money(total), style = MaterialTheme.typography.titleMedium, color = CyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                CheckoutPanel(
                    items = cart,
                    isLoggedIn = isLoggedIn,
                    walletBalance = walletBalance,
                    form = checkoutForm,
                    onUidChange = onUidChange,
                    onVerifyNow = onVerifyNow,
                    onWhatsappChange = onWhatsappChange,
                    onNoteChange = onNoteChange,
                    onPay = onPay,
                    onLoginClick = onLoginClick,
                    onTopUpClick = onTopUpClick
                )
            }
        }
    }
}
