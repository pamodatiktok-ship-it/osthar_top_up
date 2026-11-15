package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
import com.example.data.model.SiteSettingsData
import com.example.data.model.UiState
import com.example.ui.components.LiquidEntrance
import com.example.ui.components.LiquidFloatingHeroCard
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.OstharPrimaryButton
import com.example.ui.components.OstharSecondaryButton
import com.example.ui.components.ProductCard
import com.example.ui.components.liquidPress
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassSpecularRim
import com.example.ui.theme.GlassSpecularSoft
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.WhatsAppGreen

@Composable
fun HomeScreen(
    productsState: UiState<List<Product>>,
    settings: SiteSettingsData?,
    onOpenStore: (String?) -> Unit,
    onOpenProduct: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onOpenTopUp: () -> Unit,
    onOpenRegister: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    onOpenLegal: (String) -> Unit,
    onOpenAdmin: () -> Unit
) {
    val featured = (productsState as? UiState.Success)?.data
        ?.let { list ->
            val feat = list.filter { it.featured }
            if (feat.isNotEmpty()) feat.take(6) else list.take(6)
        }.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (!settings?.announcement.isNullOrBlank()) {
            item {
                Surface(
                    color = CyanPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = settings?.announcement.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Hero Section
        item {
            val pulseTransition = rememberInfiniteTransition(label = "hero_live_dot")
            val dotScale by pulseTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1100),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale"
            )

            LiquidEntrance {
                LiquidFloatingHeroCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    cornerRadius = 26.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_battle_hero),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.24f)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        BgDeep.copy(alpha = 0.35f),
                                        SurfaceCard.copy(alpha = 0.84f)
                                    )
                                )
                            )
                    )
                    Column(modifier = Modifier.padding(22.dp)) {
                        Surface(
                            color = Color(0x99060A13),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, GlassSpecularRim)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .graphicsLayer {
                                            scaleX = dotScale
                                            scaleY = dotScale
                                        }
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess)
                                )
                                Text(
                                    text = "Trusted by gamers nationwide",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Fast & Secure\n")
                                withStyle(SpanStyle(color = CyanPrimary)) { append("Gaming ") }
                                withStyle(SpanStyle(color = VioletAccent)) { append("Top-Ups") }
                            },
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Recharge your LKR wallet and purchase Free Fire diamonds instantly. Verified player IDs and fast processing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )

                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OstharPrimaryButton(
                                text = "Shop Now",
                                onClick = { onOpenStore(null) },
                                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_shop_now_button")
                            )
                            OstharSecondaryButton(
                                text = "Recharge",
                                onClick = onOpenTopUp,
                                icon = Icons.Rounded.Wallet,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_recharge_button")
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x99121C30), Color(0xBA060A13))
                                    )
                                )
                                .border(1.dp, GlassSpecularSoft, RoundedCornerShape(16.dp))
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("24/7", "SUPPORT")
                            StatItem("100%", "SECURE")
                            StatItem("INSTANT", "DELIVERY")
                        }
                    }
                }
            }
        }

        // Supported Games
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionEyebrow("WHAT WE SUPPORT", "Supported Games")
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameBannerCard(
                        title = "Free Fire",
                        drawableRes = R.drawable.img_free_fire,
                        onClick = { onOpenStore("Free Fire") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_game_free_fire")
                    )
                    GameBannerCard(
                        title = "Free Fire MAX",
                        drawableRes = R.drawable.img_free_fire_max,
                        onClick = { onOpenStore("Free Fire MAX") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_game_free_fire_max")
                    )
                }
            }
        }

        // Featured Packages Carousel
        if (featured.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        SectionEyebrow("POPULAR PACKAGES", "Top Diamonds")
                        Text(
                            text = "See all →",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyanPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenStore(null) }
                                .padding(6.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featured, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onOpen = { onOpenProduct(product.id) },
                                onAddToCart = { onAddToCart(product) },
                                modifier = Modifier.width(185.dp)
                            )
                        }
                    }
                }
            }
        }

        // How It Works
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionEyebrow("SIMPLE PROCESS", "How It Works")
                Spacer(Modifier.height(4.dp))
                StepCard(
                    step = "01",
                    title = "Create Account & Recharge",
                    body = "Register and top up your LKR wallet securely via eZ Cash or Bank Transfer.",
                    icon = Icons.Rounded.PersonAdd,
                    onClick = onOpenRegister
                )
                StepCard(
                    step = "02",
                    title = "Select & Verify Player UID",
                    body = "Browse the store, verify your Free Fire Singapore Player ID, and pay with wallet.",
                    icon = Icons.Rounded.ShoppingBag,
                    onClick = { onOpenStore(null) }
                )
                StepCard(
                    step = "03",
                    title = "Fast Admin Delivery",
                    body = "Track every order in real time from pending to completed.",
                    icon = Icons.Rounded.Wallet,
                    onClick = onOpenOrders
                )
            }
        }

        // Payment Methods
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionEyebrow("EASY TOP-UPS", "Payment Methods")
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentTile(
                        title = "eZ Cash",
                        subtitle = "Mobile top-up & slip upload",
                        icon = Icons.Rounded.PhoneAndroid,
                        onClick = onOpenTopUp,
                        modifier = Modifier.weight(1f)
                    )
                    PaymentTile(
                        title = "Bank Transfer",
                        subtitle = "Direct bank & receipt review",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = onOpenTopUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Why Choose Us
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionEyebrow("OUR ADVANTAGES", "Why Choose Us")
                Spacer(Modifier.height(4.dp))
                AdvantageRow(Icons.Rounded.Security, "Secure Wallet System", "Full ledger & receipt history for every transaction.")
                AdvantageRow(Icons.Rounded.Verified, "Verified Player Details", "Singapore UID verification ensures accurate delivery.")
                AdvantageRow(Icons.Rounded.Inventory2, "Easy Order Tracking", "Cancel pending orders anytime for instant wallet refund.")
                AdvantageRow(Icons.Rounded.Bolt, "Fast Admin Processing", "Quick approval for wallet top-ups and diamond orders.")
            }
        }

        // Support + Legal Footer
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_whatsapp_support_card"),
                    cornerRadius = 20.dp,
                    accentGlow = WhatsAppGreen,
                    highlighted = true,
                    onClick = onOpenWhatsApp
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = WhatsAppGreen,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Chat,
                                    contentDescription = "WhatsApp Support",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Need Help? WhatsApp Support", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("Chat directly with our Sri Lanka support team.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = WhatsAppGreen
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms & Conditions",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier
                            .clickable { onOpenLegal("terms") }
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                    )
                    Text("·", color = TextMuted)
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier
                            .clickable { onOpenLegal("privacy") }
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                    )
                    Text("·", color = TextMuted)
                    Text(
                        text = "Admin",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanPrimary,
                        modifier = Modifier
                            .clickable(onClick = onOpenAdmin)
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                            .testTag("home_admin_link")
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun SectionEyebrow(eyebrow: String, title: String) {
    Column {
        Text(eyebrow, style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
    }
}

@Composable
private fun GameBannerCard(
    title: String,
    drawableRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.aspectRatio(1f),
        cornerRadius = 20.dp,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xBB0B1222), Color(0xEC060A13))
                        )
                    )
                    .border(1.dp, GlassSpecularSoft)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StepCard(
    step: String,
    title: String,
    body: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = CyanPrimary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GlassSpecularRim),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(Modifier.height(3.dp))
                Text(body, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Text(
                text = step,
                style = MaterialTheme.typography.headlineMedium,
                color = CyanPrimary.copy(alpha = 0.35f),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun PaymentTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = CyanPrimary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GlassSpecularRim),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
private fun AdvantageRow(icon: ImageVector, title: String, subtitle: String) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = SurfaceElevated.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlassSpecularSoft),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}
