package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GlassCyanRim
import com.example.ui.theme.GlassDockBg
import com.example.ui.theme.GlassSpecularRim
import com.example.ui.theme.GlassSpecularSoft
import com.example.ui.theme.GlassSurfaceBottom
import com.example.ui.theme.GlassSurfaceTop
import com.example.ui.theme.GlassVioletRim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletAccent
import kotlinx.coroutines.delay

/**
 * GPU-only iPhone-style spring press + subtle 3D perspective tilt.
 * Operates strictly in the graphicsLayer draw phase so scrolling remains 60/120fps lag-free.
 */
fun Modifier.liquidPress(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.965f,
    enable3DTilt: Boolean = true
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = 520f
        ),
        label = "liquid_scale"
    )
    val tiltX by animateFloatAsState(
        targetValue = if (isPressed && enable3DTilt) 2.2f else 0f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = 480f
        ),
        label = "liquid_tilt"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationX = tiltX
        cameraDistance = 14f * density
    }
}

/**
 * Lightweight one-shot spring entrance for screen headers and hero blocks.
 * Runs purely on graphicsLayer without re-measuring layouts.
 */
@Composable
fun LiquidEntrance(
    delayMillis: Int = 0,
    offsetYDp: Float = 16f,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        visible = true
    }

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "liquid_entrance"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * offsetYDp * density
            val s = 0.96f + (0.04f * progress)
            scaleX = s
            scaleY = s
        }
    ) {
        content()
    }
}

/**
 * 3D Liquid Glass card with translucent gradient depth, diagonal specular border,
 * and a crisp top glass rim highlight.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    accentGlow: Color = CyanPrimary,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    val interactiveModifier = if (onClick != null) {
        Modifier
            .liquidPress(interactionSource = interactionSource)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = accentGlow.copy(alpha = 0.25f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
    } else {
        Modifier.clip(shape)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (highlighted) 14.dp else 8.dp,
                shape = shape,
                ambientColor = accentGlow.copy(alpha = 0.25f),
                spotColor = accentGlow.copy(alpha = 0.35f)
            )
            .then(interactiveModifier)
            .drawWithCache {
                val radiusPx = cornerRadius.toPx()
                val bgBrush = Brush.linearGradient(
                    colors = if (highlighted) {
                        listOf(
                            Color(0xE6142A3E),
                            Color(0xF0091323)
                        )
                    } else {
                        listOf(
                            GlassSurfaceTop,
                            GlassSurfaceBottom
                        )
                    },
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
                val sheenBrush = Brush.radialGradient(
                    colors = listOf(
                        accentGlow.copy(alpha = if (highlighted) 0.16f else 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.2f, 0f),
                    radius = size.maxDimension * 0.85f
                )
                val topRimBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        GlassSpecularRim,
                        accentGlow.copy(alpha = 0.45f),
                        GlassSpecularSoft,
                        Color.Transparent
                    )
                )
                val borderBrush = Brush.linearGradient(
                    colors = listOf(
                        if (highlighted) accentGlow.copy(alpha = 0.55f) else GlassSpecularRim,
                        if (highlighted) GlassVioletRim else GlassSpecularSoft,
                        Color(0xFF162033)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )

                onDrawBehind {
                    drawRoundRect(
                        brush = bgBrush,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                    drawRoundRect(
                        brush = sheenBrush,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                    // Specular top bevel line for liquid 3D rim thickness
                    drawRoundRect(
                        brush = topRimBrush,
                        topLeft = Offset(radiusPx * 0.4f, 0.5.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(
                            width = (size.width - radiusPx * 0.8f).coerceAtLeast(0f),
                            height = 1.4.dp.toPx()
                        ),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        brush = borderBrush,
                        cornerRadius = CornerRadius(radiusPx, radiusPx),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                }
            },
        content = content
    )
}

/**
 * 3D Floating Hero Glass Card with smooth, GPU-cached liquid light sweep.
 */
@Composable
fun LiquidFloatingHeroCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    primaryGlow: Color = CyanPrimary,
    secondaryGlow: Color = VioletAccent,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_hero_sweep")
    val sweep by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_progress"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = primaryGlow.copy(alpha = 0.3f),
                spotColor = secondaryGlow.copy(alpha = 0.35f)
            )
            .clip(shape)
            .drawWithCache {
                val radiusPx = cornerRadius.toPx()
                val baseBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xEB102237),
                        Color(0xF5091120),
                        Color(0xF50B0E1D)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
                val borderBrush = Brush.linearGradient(
                    colors = listOf(
                        GlassCyanRim,
                        GlassSpecularRim,
                        GlassVioletRim,
                        Color(0xFF1A253B)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
                val topRimBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.42f),
                        primaryGlow.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                )

                onDrawBehind {
                    drawRoundRect(
                        brush = baseBrush,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                    // Ambient liquid shimmer beam across the glass surface
                    val cx = size.width * sweep
                    val beamBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.055f),
                            primaryGlow.copy(alpha = 0.065f),
                            Color.Transparent
                        ),
                        start = Offset(cx - size.width * 0.45f, 0f),
                        end = Offset(cx + size.width * 0.25f, size.height)
                    )
                    drawRoundRect(
                        brush = beamBrush,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                    drawRoundRect(
                        brush = topRimBrush,
                        topLeft = Offset(radiusPx * 0.4f, 0.5.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(
                            width = (size.width - radiusPx * 0.8f).coerceAtLeast(0f),
                            height = 1.6.dp.toPx()
                        ),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        brush = borderBrush,
                        cornerRadius = CornerRadius(radiusPx, radiusPx),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.1.dp.toPx())
                    )
                }
            },
        content = content
    )
}

/**
 * Smooth pulsing liquid skeleton box for loading states.
 */
@Composable
fun LiquidShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF10192A).copy(alpha = alpha),
                        Color(0xFF1A2842).copy(alpha = alpha),
                        Color(0xFF10192A).copy(alpha = alpha)
                    )
                )
            )
            .border(1.dp, GlassSpecularSoft, RoundedCornerShape(cornerRadius))
    )
}

data class LiquidNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

/**
 * iPhone-inspired floating Liquid Glass bottom dock with a spring-animated sliding pill indicator.
 * Respects system navigation bars inset cleanly.
 */
@Composable
fun LiquidFloatingNavBar(
    items: List<LiquidNavItem>,
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val haptic = LocalHapticFeedback.current
    val dockShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = dockShape,
                    ambientColor = CyanPrimary.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.8f)
                )
                .clip(dockShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xEB182339),
                            GlassDockBg
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            GlassSpecularRim,
                            GlassCyanRim.copy(alpha = 0.35f),
                            GlassSpecularSoft
                        )
                    ),
                    shape = dockShape
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val tabWidth = maxWidth / items.size.coerceAtLeast(1)
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = 430f
                ),
                label = "liquid_nav_indicator"
            )

            // Sliding liquid glass capsule indicator
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CyanPrimary.copy(alpha = 0.24f),
                                VioletAccent.copy(alpha = 0.14f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                CyanPrimary.copy(alpha = 0.55f),
                                VioletAccent.copy(alpha = 0.22f)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
            )

            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.14f else 1f,
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = 480f
                        ),
                        label = "nav_icon_scale_$index"
                    )
                    val contentTint by animateColorAsState(
                        targetValue = if (selected) CyanPrimary else TextMuted,
                        animationSpec = tween(durationMillis = 200),
                        label = "nav_tint_$index"
                    )
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelect(tab.route)
                                }
                            )
                            .testTag(tab.testTag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = contentTint,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (selected) TextPrimary else TextMuted,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Small frosted glass 3D icon badge for back buttons & action medallions.
 */
@Composable
fun LiquidIconCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = modifier
            .size(44.dp)
            .liquidPress(interactionSource, pressedScale = 0.92f)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = CyanPrimary.copy(alpha = 0.25f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        shape = CircleShape,
        color = Color(0xCC162034),
        border = BorderStroke(1.dp, GlassSpecularRim)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
