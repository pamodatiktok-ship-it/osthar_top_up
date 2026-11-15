package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.ui.CheckoutFormState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderBright
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassSpecularRim
import com.example.ui.theme.GlassSpecularSoft
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletAccent
import com.example.util.Formatters

@Composable
fun OstharGridBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDeep)
            .drawWithCache {
                val gridStep = 52.dp.toPx()
                val lineColor = CyanPrimary.copy(alpha = 0.032f)
                val cyanOrb = Brush.radialGradient(
                    colors = listOf(
                        CyanPrimary.copy(alpha = 0.12f),
                        CyanPrimary.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.25f, size.height * 0.06f),
                    radius = size.width * 0.9f
                )
                val violetOrb = Brush.radialGradient(
                    colors = listOf(
                        VioletAccent.copy(alpha = 0.11f),
                        VioletAccent.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.58f),
                    radius = size.width * 0.85f
                )
                val bottomGlow = Brush.radialGradient(
                    colors = listOf(
                        CyanPrimary.copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.4f, size.height * 0.96f),
                    radius = size.width * 0.75f
                )

                onDrawBehind {
                    drawRect(brush = cyanOrb)
                    drawRect(brush = violetOrb)
                    drawRect(brush = bottomGlow)

                    var x = 0f
                    while (x < size.width) {
                        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += gridStep
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += gridStep
                    }
                }
            }
    ) {
        content()
    }
}

@Composable
fun OstharBrandLogo(
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    logoSize: Int = 34
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_osthar_logo),
            contentDescription = "Osthar Top Up logo",
            modifier = Modifier
                .size(logoSize.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        if (showText) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = CyanPrimary, fontWeight = FontWeight.Bold)) {
                        append("OSTHAR ")
                    }
                    withStyle(SpanStyle(color = VioletAccent, fontWeight = FontWeight.Bold)) {
                        append("TOP UP")
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                letterSpacing = (-0.3).sp
            )
        }
    }
}

@Composable
fun OstharProductImage(
    imagePath: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (Formatters.isRemoteUrl(imagePath)) {
        AsyncImage(
            model = imagePath,
            contentDescription = contentDescription,
            placeholder = painterResource(id = Formatters.localAssetRes(imagePath)),
            error = painterResource(id = Formatters.localAssetRes(imagePath)),
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = Formatters.localAssetRes(imagePath)),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val normalized = status.lowercase()
    val (bg, fg, label) = when (normalized) {
        "completed", "approved" -> Triple(
            EmeraldSuccess.copy(alpha = 0.14f),
            EmeraldSuccess,
            normalized.replaceFirstChar { it.uppercase() }
        )
        "processing" -> Triple(
            CyanPrimary.copy(alpha = 0.14f),
            CyanPrimary,
            "Processing"
        )
        "cancelled", "rejected" -> Triple(
            RedError.copy(alpha = 0.14f),
            RedError,
            normalized.replaceFirstChar { it.uppercase() }
        )
        else -> Triple(
            AmberWarning.copy(alpha = 0.14f),
            AmberWarning,
            "Pending"
        )
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.35f))
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun OstharPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(16.dp)
    val isActive = enabled && !loading

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = isActive,
        interactionSource = interactionSource,
        modifier = modifier
            .heightIn(min = 50.dp)
            .liquidPress(interactionSource = interactionSource, pressedScale = 0.96f)
            .then(
                if (isActive) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                        ambientColor = CyanPrimary.copy(alpha = 0.45f),
                        spotColor = CyanPrimary.copy(alpha = 0.55f)
                    )
                } else {
                    Modifier
                }
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = BgDeep,
            disabledContainerColor = CyanPrimary.copy(alpha = 0.22f),
            disabledContentColor = TextMuted
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .then(
                    if (isActive) {
                        Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF2AE8FF),
                                        CyanPrimary,
                                        Color(0xFF06B6D4)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.65f),
                                        Color.Transparent
                                    )
                                ),
                                shape = shape
                            )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = BgDeep
                    )
                    Spacer(Modifier.width(8.dp))
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun OstharSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(16.dp)

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .heightIn(min = 50.dp)
            .liquidPress(interactionSource = interactionSource, pressedScale = 0.965f),
        shape = shape,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(GlassSpecularRim, BorderBright)
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfaceElevated.copy(alpha = 0.72f),
            contentColor = TextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = CyanPrimary)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun OstharTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            placeholder = {
                Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BgDeep.copy(alpha = 0.7f),
                unfocusedContainerColor = BgDeep.copy(alpha = 0.55f),
                disabledContainerColor = SurfaceElevated.copy(alpha = 0.4f),
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = CyanPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onOpen: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickAddInteraction = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        cornerRadius = 20.dp,
        highlighted = product.featured,
        onClick = onOpen
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .background(SurfaceElevated)
            ) {
                OstharProductImage(
                    imagePath = product.image,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.04f),
                                    Color.Transparent,
                                    BgDeep.copy(alpha = 0.78f)
                                )
                            )
                        )
                )
                Surface(
                    color = Color(0xBF0A101E),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, GlassSpecularRim),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = product.game.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
                product.discountPercent?.let { pct ->
                    Surface(
                        color = CyanPrimary,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .shadow(6.dp, RoundedCornerShape(999.dp), ambientColor = CyanPrimary)
                    ) {
                        Text(
                            text = "SAVE $pct%",
                            style = MaterialTheme.typography.labelSmall,
                            color = BgDeep,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Diamond,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (product.diamondAmount > 0) "${product.diamondAmount} Diamonds" else product.packageType,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (product.isInStock) EmeraldSuccess else RedError)
                        )
                        Text(
                            text = if (product.isInStock) "In stock" else "Sold out",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (product.isInStock) EmeraldSuccess else RedError
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Formatters.money(product.effectivePrice),
                            style = MaterialTheme.typography.titleSmall,
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.salePrice != null && product.salePrice < product.regularPrice) {
                            Text(
                                text = Formatters.money(product.regularPrice),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .liquidPress(quickAddInteraction, pressedScale = 0.88f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                enabled = product.isInStock,
                                interactionSource = quickAddInteraction,
                                indication = androidx.compose.material3.ripple(color = CyanPrimary),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onAddToCart()
                                }
                            )
                            .testTag("quick_add_cart_${product.id}"),
                        color = if (product.isInStock) CyanPrimary.copy(alpha = 0.16f) else SurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (product.isInStock) CyanPrimary.copy(alpha = 0.45f) else BorderSubtle
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddShoppingCart,
                                contentDescription = "Add ${product.name} to cart",
                                tint = if (product.isInStock) CyanPrimary else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutPanel(
    items: List<CartItem>,
    isLoggedIn: Boolean,
    walletBalance: Double,
    form: CheckoutFormState,
    onUidChange: (String) -> Unit,
    onVerifyNow: () -> Unit,
    onWhatsappChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPay: () -> Unit,
    onLoginClick: () -> Unit,
    onTopUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = items.sumOf { it.lineTotal }
    val validUid = Regex("^\\d{6,15}$").matches(form.uid)
    val verified = form.verifiedPlayer?.uid == form.uid && form.uid.isNotEmpty()

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        highlighted = verified
    ) {
        if (!isLoggedIn) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Your next win is one login away.",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Sign in to verify your Free Fire Player UID and pay securely with your LKR wallet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(Modifier.height(18.dp))
                OstharPrimaryButton(
                    text = "Login to Continue",
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_login_button")
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Secure Wallet Checkout",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                // Player UID + Verify Button
                Column {
                    Text(
                        text = "Player UID *",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = form.uid,
                            onValueChange = onUidChange,
                            enabled = !form.submitting,
                            singleLine = true,
                            placeholder = { Text("Enter Free Fire UID", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BgDeep.copy(alpha = 0.7f),
                                unfocusedContainerColor = BgDeep.copy(alpha = 0.55f),
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("checkout_uid_input")
                        )
                        OstharPrimaryButton(
                            text = if (verified) "Recheck" else "Verify",
                            onClick = onVerifyNow,
                            enabled = validUid && !form.submitting,
                            loading = form.verifying,
                            modifier = Modifier.testTag("checkout_verify_button")
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Auto-verifies after typing. Singapore server only.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    AnimatedVisibility(
                        visible = form.verifying,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CyanPrimary)
                            Text("Checking Player UID...", style = MaterialTheme.typography.bodySmall, color = CyanPrimary)
                        }
                    }

                    AnimatedVisibility(
                        visible = verified && form.verifiedPlayer != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        form.verifiedPlayer?.let { vp ->
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.42f)),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${vp.name} · Singapore verified",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !form.verifyError.isNullOrBlank(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = form.verifyError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = RedError,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                OstharTextField(
                    value = form.whatsapp,
                    onValueChange = onWhatsappChange,
                    label = "WhatsApp Number *",
                    placeholder = "+94771234567",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.testTag("checkout_whatsapp_input")
                )

                OstharTextField(
                    value = form.note,
                    onValueChange = onNoteChange,
                    label = "Order Note (optional)",
                    placeholder = "Anything we should know?",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.testTag("checkout_note_input")
                )

                Surface(
                    color = BgDeep,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onTopUpClick)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Wallet, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Text("Wallet balance", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Text(
                            text = Formatters.money(walletBalance),
                            style = MaterialTheme.typography.labelLarge,
                            color = CyanPrimary
                        )
                    }
                }

                if (walletBalance < total) {
                    Surface(
                        color = AmberWarning.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onTopUpClick)
                    ) {
                        Text(
                            text = "Not enough funds. Tap here to top up your wallet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberWarning,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                form.submitError?.let { err ->
                    Text(err, style = MaterialTheme.typography.bodySmall, color = RedError)
                }

                OstharPrimaryButton(
                    text = "Pay ${Formatters.money(total)}",
                    onClick = onPay,
                    enabled = verified && walletBalance >= total && items.isNotEmpty() && !form.verifying,
                    loading = form.submitting,
                    icon = Icons.Rounded.VerifiedUser,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_pay_button")
                )

                Text(
                    text = "Your UID is re-verified on submit. Prices and wallet balance are checked securely on our server.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector = Icons.Rounded.Diamond,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = CyanPrimary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GlassSpecularRim)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(28.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            if (actionText != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                OstharPrimaryButton(text = actionText, onClick = onAction)
            }
        }
    }
}

@Composable
fun ErrorStateCard(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateCard(
        title = title,
        description = message,
        icon = Icons.Rounded.ErrorOutline,
        actionText = "Try Again",
        onAction = onRetry,
        modifier = modifier
    )
}
