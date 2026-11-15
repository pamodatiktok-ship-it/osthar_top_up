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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.AuthSession
import com.example.data.model.Profile
import com.example.data.model.SiteSettingsData
import com.example.data.model.UiState
import com.example.ui.AuthMode
import com.example.ui.AuthUiState
import com.example.ui.components.LiquidEntrance
import com.example.ui.components.LiquidFloatingHeroCard
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidIconCircleButton
import com.example.ui.components.OstharBrandLogo
import com.example.ui.components.OstharPrimaryButton
import com.example.ui.components.OstharSecondaryButton
import com.example.ui.components.OstharTextField
import com.example.ui.components.liquidPress
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
fun AuthScreen(
    state: AuthUiState,
    onModeChange: (AuthMode) -> Unit,
    onFieldChange: (
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        whatsapp: String,
        otpCode: String,
        consentAccepted: Boolean
    ) -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onResendEmail: () -> Unit,
    onOpenLegal: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (onBack != null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LiquidIconCircleButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            onClick = onBack
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                OstharBrandLogo(logoSize = 52)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = when (state.mode) {
                        AuthMode.LOGIN -> "Welcome back."
                        AuthMode.REGISTER -> "Create your account."
                        AuthMode.OTP -> "Check your inbox."
                        AuthMode.FORGOT_PASSWORD -> "Forgot your password?"
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = when (state.mode) {
                        AuthMode.LOGIN -> "Sign in to manage your wallet and Free Fire top-ups."
                        AuthMode.REGISTER -> "Register and start gaming on the Singapore server."
                        AuthMode.OTP -> "Enter the verification code sent to ${state.email}."
                        AuthMode.FORGOT_PASSWORD -> "We will send a password reset link to your email."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                LiquidEntrance {
                    LiquidFloatingHeroCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OstharTextField(
                            value = state.email,
                            onValueChange = {
                                onFieldChange(it, state.password, state.confirmPassword, state.fullName, state.whatsapp, state.otpCode, state.consentAccepted)
                            },
                            label = "Email Address",
                            placeholder = "you@example.com",
                            keyboardType = KeyboardType.Email,
                            enabled = !state.busy,
                            modifier = Modifier.testTag("auth_email_input")
                        )

                        if (state.mode == AuthMode.REGISTER) {
                            OstharTextField(
                                value = state.fullName,
                                onValueChange = {
                                    onFieldChange(state.email, state.password, state.confirmPassword, it, state.whatsapp, state.otpCode, state.consentAccepted)
                                },
                                label = "Full Name (optional)",
                                placeholder = "Your name",
                                enabled = !state.busy
                            )
                            OstharTextField(
                                value = state.whatsapp,
                                onValueChange = {
                                    onFieldChange(state.email, state.password, state.confirmPassword, state.fullName, it, state.otpCode, state.consentAccepted)
                                },
                                label = "WhatsApp Number (optional)",
                                placeholder = "+94771234567",
                                keyboardType = KeyboardType.Phone,
                                enabled = !state.busy
                            )
                        }

                        if (state.mode == AuthMode.LOGIN || state.mode == AuthMode.REGISTER) {
                            Column {
                                Text("Password", style = MaterialTheme.typography.labelLarge, color = TextPrimary, modifier = Modifier.padding(bottom = 6.dp))
                                OutlinedTextField(
                                    value = state.password,
                                    onValueChange = {
                                        onFieldChange(state.email, it, state.confirmPassword, state.fullName, state.whatsapp, state.otpCode, state.consentAccepted)
                                    },
                                    enabled = !state.busy,
                                    singleLine = true,
                                    placeholder = {
                                        Text(
                                            if (state.mode == AuthMode.REGISTER) "At least 8 characters" else "••••••••",
                                            color = TextMuted
                                        )
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_password_input")
                                )
                            }
                        }

                        if (state.mode == AuthMode.REGISTER) {
                            Column {
                                Text("Confirm Password", style = MaterialTheme.typography.labelLarge, color = TextPrimary, modifier = Modifier.padding(bottom = 6.dp))
                                OutlinedTextField(
                                    value = state.confirmPassword,
                                    onValueChange = {
                                        onFieldChange(state.email, state.password, it, state.fullName, state.whatsapp, state.otpCode, state.consentAccepted)
                                    },
                                    enabled = !state.busy,
                                    singleLine = true,
                                    placeholder = { Text("Re-enter password", color = TextMuted) },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = BgDeep.copy(alpha = 0.7f),
                                        unfocusedContainerColor = BgDeep.copy(alpha = 0.55f),
                                        focusedBorderColor = CyanPrimary,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (state.mode == AuthMode.OTP) {
                            OstharTextField(
                                value = state.otpCode,
                                onValueChange = {
                                    onFieldChange(state.email, state.password, state.confirmPassword, state.fullName, state.whatsapp, it, state.consentAccepted)
                                },
                                label = "Verification Code",
                                placeholder = "Enter code from your email",
                                keyboardType = KeyboardType.Number,
                                enabled = !state.busy
                            )
                        }

                        if (state.mode == AuthMode.LOGIN || state.mode == AuthMode.REGISTER) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = state.consentAccepted,
                                    onCheckedChange = {
                                        onFieldChange(state.email, state.password, state.confirmPassword, state.fullName, state.whatsapp, state.otpCode, it)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = CyanPrimary, checkmarkColor = BgDeep)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("I agree to ", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text(
                                        "Terms",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyanPrimary,
                                        modifier = Modifier.clickable { onOpenLegal("terms") }
                                    )
                                    Text(" & ", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text(
                                        "Privacy Policy",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyanPrimary,
                                        modifier = Modifier.clickable { onOpenLegal("privacy") }
                                    )
                                }
                            }
                        }

                        state.error?.let { err ->
                            Surface(
                                color = RedError.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, RedError.copy(alpha = 0.3f))
                            ) {
                                Text(err, style = MaterialTheme.typography.bodySmall, color = RedError, modifier = Modifier.fillMaxWidth().padding(12.dp))
                            }
                        }

                        state.infoMessage?.let { info ->
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
                            ) {
                                Text(info, style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess, modifier = Modifier.fillMaxWidth().padding(12.dp))
                            }
                        }

                        OstharPrimaryButton(
                            text = when (state.mode) {
                                AuthMode.LOGIN -> "Login"
                                AuthMode.REGISTER -> "Create Account"
                                AuthMode.OTP -> "Verify Email"
                                AuthMode.FORGOT_PASSWORD -> "Send Reset Link"
                            },
                            onClick = onSubmit,
                            loading = state.busy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_submit_button")
                        )

                        if (state.mode == AuthMode.LOGIN || state.mode == AuthMode.REGISTER) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                                Text("OR", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                            }

                            OutlinedButton(
                                onClick = onGoogleSignIn,
                                enabled = !state.busy,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = BgDeep.copy(alpha = 0.65f),
                                    contentColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("auth_google_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CyanPrimary.copy(alpha = 0.16f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "G",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CyanPrimary
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (state.mode == AuthMode.LOGIN) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { onModeChange(AuthMode.FORGOT_PASSWORD) }) {
                                    Text("Forgot password?", color = CyanPrimary)
                                }
                                TextButton(onClick = { onModeChange(AuthMode.REGISTER) }) {
                                    Text("Create account", color = CyanPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            if (state.mode == AuthMode.OTP) {
                                TextButton(
                                    onClick = onResendEmail,
                                    enabled = state.resendCooldownSec == 0 && !state.busy,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(
                                        if (state.resendCooldownSec > 0) "Resend in ${state.resendCooldownSec}s"
                                        else "Resend confirmation email",
                                        color = CyanPrimary
                                    )
                                }
                            }
                            TextButton(
                                onClick = { onModeChange(AuthMode.LOGIN) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("← Back to Sign In", color = TextMuted)
                            }
                        }
                    }
                }
                }

                Spacer(Modifier.height(14.dp))
                TextButton(
                    onClick = onOpenAdmin,
                    modifier = Modifier.testTag("auth_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AdminPanelSettings,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Superadmin Panel", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    session: AuthSession,
    profileState: UiState<Profile?>,
    walletBalance: Double,
    onOpenWallet: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenForgotPassword: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    onOpenEmailSupport: () -> Unit,
    onOpenLegal: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    val profile = (profileState as? UiState.Success)?.data

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("YOUR ACCOUNT", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Player Profile.", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            }
        }

        item {
            LiquidEntrance {
                LiquidFloatingHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                color = CyanPrimary.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = CyanPrimary)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile?.fullName?.takeIf { it.isNotBlank() }
                                        ?: session.fullName.ifBlank { "Osthar Player" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(session.email, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                        HorizontalDivider(color = BorderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("WhatsApp", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    profile?.whatsappNumber?.takeIf { it.isNotBlank() }
                                        ?: session.whatsappNumber.ifBlank { "Not supplied" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Joined", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    Formatters.formatDate(profile?.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
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
                cornerRadius = 20.dp
            ) {
                Column {
                    ProfileActionRow(
                        icon = Icons.Rounded.Wallet,
                        title = "My Wallet",
                        subtitle = Formatters.money(walletBalance),
                        onClick = onOpenWallet
                    )
                    HorizontalDivider(color = BorderSubtle)
                    ProfileActionRow(
                        icon = Icons.Rounded.ReceiptLong,
                        title = "My Orders",
                        subtitle = "Track pending & completed top-ups",
                        onClick = onOpenOrders
                    )
                    HorizontalDivider(color = BorderSubtle)
                    ProfileActionRow(
                        icon = Icons.Rounded.Chat,
                        title = "WhatsApp Support",
                        subtitle = "Chat with our team",
                        onClick = onOpenWhatsApp
                    )
                    HorizontalDivider(color = BorderSubtle)
                    ProfileActionRow(
                        icon = Icons.Rounded.Email,
                        title = "Email Support",
                        subtitle = "Send us an email",
                        onClick = onOpenEmailSupport
                    )
                    HorizontalDivider(color = BorderSubtle)
                    ProfileActionRow(
                        icon = Icons.Rounded.Description,
                        title = "Terms & Conditions",
                        subtitle = "Service rules & refund policy",
                        onClick = { onOpenLegal("terms") }
                    )
                    HorizontalDivider(color = BorderSubtle)
                    ProfileActionRow(
                        icon = Icons.Rounded.Policy,
                        title = "Privacy Policy",
                        subtitle = "How your account data is handled",
                        onClick = { onOpenLegal("privacy") }
                    )
                    HorizontalDivider(color = BorderSubtle)
                    ProfileActionRow(
                        icon = Icons.Rounded.LockReset,
                        title = "Reset Password",
                        subtitle = "Send a password reset link",
                        onClick = onOpenForgotPassword
                    )
                    HorizontalDivider(color = BorderSubtle)
                    ProfileActionRow(
                        icon = Icons.Rounded.AdminPanelSettings,
                        title = "Admin Panel",
                        subtitle = "Superadmin orders, top-ups & products",
                        onClick = onOpenAdmin
                    )
                }
            }
        }

        item {
            OstharSecondaryButton(
                text = "Sign Out",
                onClick = onSignOut,
                icon = Icons.AutoMirrored.Rounded.Logout,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_sign_out_button")
            )
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
fun LegalScreen(
    type: String,
    settings: SiteSettingsData?,
    onBack: () -> Unit
) {
    val isTerms = type == "terms"
    val bodyText = if (isTerms) {
        settings?.terms?.takeIf { it.isNotBlank() }
            ?: "Our terms are being prepared. Please contact support before placing an order."
    } else {
        settings?.privacy?.takeIf { it.isNotBlank() }
            ?: "Our privacy policy is being prepared. Please contact support if you have questions."
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
                    Text("THE IMPORTANT DETAILS", style = MaterialTheme.typography.labelMedium, color = CyanPrimary)
                    Text(
                        if (isTerms) "Terms & Conditions" else "Privacy Policy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }

        if (isTerms && !settings?.refundPolicy.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Refund Policy", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(settings?.refundPolicy.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    }
                }
            }
        }
    }
}
