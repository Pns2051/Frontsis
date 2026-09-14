package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.example.ui.i18n.Strings
import com.example.ui.theme.BalooDa2Family
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.HindSiliguriFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Fully solid rich vibrant green for onboarding action buttons (never transparent)
private val SolidButtonGreen = Color(0xFF16A34A)

/**
 * ONBOARDING FLOW:
 * Step 1: Language selection (Bangla / English)
 * Step 2: Sign-In Screen:
 *   - "Continue with Google" -> If existing/normal account has name, proceeds directly to Chat ("if normal then ok").
 *                               If new account without name, moves to Step 3 to ask name ("if new then ask name").
 *   - "Continue as guest"   -> Moves to Step 3 to ask name ("if guest then ask name").
 *   - Email login is completely removed.
 * Step 3: Name Input Screen (shown when new Google account needs a name, or when guest mode is chosen).
 */
@Composable
fun OnboardingScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onFinishOnboarding: (userName: String) -> Unit,
    onGoogleSignInSuccess: (account: GoogleSignInAccount, customName: String?, onResult: (Boolean, String?) -> Unit) -> Unit = { _, _, _ -> },
    onAnonymousSignIn: (name: String, onResult: (Boolean, String?) -> Unit) -> Unit = { _, it -> it(true, null) },
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    var step by remember { mutableIntStateOf(1) } // 1 = Language, 2 = Login / Guest, 3 = Name (for new user / guest)
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    var enteredName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    var isAuthLoading by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var pendingGoogleAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }
    var isGuestFlow by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    val totalSteps = if (step == 3) 3 else 2

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isAuthLoading = false
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val googleDisplayName = account.displayName?.trim().orEmpty()
                // If user already has a real name in Google ("normal user"):
                if (googleDisplayName.isNotBlank() && !googleDisplayName.equals("User", ignoreCase = true)) {
                    isAuthLoading = true
                    authErrorMessage = null
                    onGoogleSignInSuccess(account, null) { ok, err ->
                        isAuthLoading = false
                        if (!ok && err != null) {
                            authErrorMessage = err
                        }
                    }
                } else {
                    // New user without a display name: ask for their name!
                    pendingGoogleAccount = account
                    isGuestFlow = false
                    authErrorMessage = null
                    step = 3
                }
            }
        } catch (e: ApiException) {
            if (e.statusCode == 12501) {
                // User cancelled the picker - do not show error banner
                authErrorMessage = null
            } else {
                val msg = when (e.statusCode) {
                    12500 -> "Google Play services error (12500): Check SHA-1 in Firebase Console"
                    10 -> "Configuration error (Code 10): App SHA-1 is not registered in Firebase Console"
                    7 -> "Network error: Please check your internet connection"
                    else -> "Google sign-in error (code ${e.statusCode})"
                }
                authErrorMessage = msg
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun triggerShake() {
        scope.launch {
            shakeOffset.snapTo(0f)
            repeat(3) {
                shakeOffset.animateTo(10f, tween(35))
                shakeOffset.animateTo(-10f, tween(35))
            }
            shakeOffset.animateTo(0f, tween(35))
        }
    }

    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, url, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 20.dp)
            .testTag("onboarding_screen")
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(tween(220)) { it } + fadeIn(tween(200)))
                        .togetherWith(slideOutHorizontally(tween(220)) { -it } + fadeOut(tween(180)))
                } else {
                    (slideInHorizontally(tween(220)) { -it } + fadeIn(tween(200)))
                        .togetherWith(slideOutHorizontally(tween(220)) { it } + fadeOut(tween(180)))
                }
            },
            label = "onboarding_step"
        ) { currentStep ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Portion: Sleek, clean, minimalist explanation card
                OnboardingExplanationCard(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    selectedLanguage = selectedLanguage,
                    modifier = Modifier.fillMaxWidth()
                )

                // Middle Portion: Centered in the available vertical space
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (currentStep) {
                            // ── STEP 1: LANGUAGE ──
                            1 -> {
                                Text(
                                    text = Strings.onboardingStep1Title(selectedLanguage),
                                    fontFamily = BalooDa2Family,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = Strings.onboardingStep1Sub(selectedLanguage),
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 14.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(28.dp))

                                // Two cards side by side: বাংলা / Bangla & English / International
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    val isBn = selectedLanguage == "bn"
                                    // বাংলা Card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(116.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isBn) colors.primary.copy(alpha = 0.12f) else colors.surface
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = if (isBn) colors.primary else colors.border,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                selectedLanguage = "bn"
                                                onLanguageChange("bn")
                                            }
                                            .testTag("lang_card_bn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "বাংলা",
                                                fontFamily = BalooDa2Family,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 28.sp,
                                                color = if (isBn) colors.primary else colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Bangla",
                                                fontFamily = HindSiliguriFamily,
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }

                                    val isEn = selectedLanguage == "en"
                                    // English Card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(116.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isEn) colors.primary.copy(alpha = 0.12f) else colors.surface
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = if (isEn) colors.primary else colors.border,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                selectedLanguage = "en"
                                                onLanguageChange("en")
                                            }
                                            .testTag("lang_card_en"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "English",
                                                fontFamily = BalooDa2Family,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 28.sp,
                                                color = if (isEn) colors.primary else colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "International",
                                                fontFamily = HindSiliguriFamily,
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // ── STEP 2: SIGN IN / LOGIN ──
                            2 -> {
                                Text(
                                    text = Strings.onboardingStep3Title(selectedLanguage),
                                    fontFamily = BalooDa2Family,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (selectedLanguage == "bn") "যেভাবে সুবিধা সেভাবে চালিয়ে যাও" else "Continue the way you like",
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 14.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(28.dp))

                                // Google Button: [G] "Continue with Google"
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.surface)
                                        .border(2.dp, colors.border, RoundedCornerShape(14.dp))
                                        .clickable(enabled = !isAuthLoading) {
                                            authErrorMessage = null
                                            isAuthLoading = true
                                            try {
                                                val client = AuthManager.getGoogleSignInClient(context)
                                                client.signOut().addOnCompleteListener {
                                                    try {
                                                        googleSignInLauncher.launch(client.signInIntent)
                                                    } catch (ex: Exception) {
                                                        isAuthLoading = false
                                                        authErrorMessage = ex.localizedMessage
                                                        Toast.makeText(context, ex.localizedMessage ?: "Could not start Google sign in", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                isAuthLoading = false
                                                authErrorMessage = e.localizedMessage
                                                Toast.makeText(context, e.localizedMessage ?: "Could not start Google sign in", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .testTag("google_login_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAuthLoading) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = colors.primary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = if (selectedLanguage == "bn") "লগইন হচ্ছে..." else "Signing in...",
                                                fontFamily = BalooDa2Family,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            // Google "G" circular badge
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "G",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF4285F4)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = Strings.googleSignIn(selectedLanguage),
                                                fontFamily = BalooDa2Family,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }

                                if (authErrorMessage != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.error.copy(alpha = 0.12f))
                                            .border(1.dp, colors.error.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .padding(12.dp)
                                    ) {
                                        val runtimeSha1 = remember(context) { AuthManager.getRuntimeSha1(context) }
                                        Column {
                                            Text(
                                                text = authErrorMessage ?: "",
                                                fontFamily = HindSiliguriFamily,
                                                fontSize = 12.5.sp,
                                                color = colors.error,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "App SHA-1: $runtimeSha1",
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                color = colors.textSecondary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Button to copy runtime SHA-1
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(colors.surfaceElevated)
                                                        .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                            clipboard.setPrimaryClip(ClipData.newPlainText("SHA-1", runtimeSha1))
                                                            Toast.makeText(context, "SHA-1 copied: $runtimeSha1", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "📋 Copy SHA-1",
                                                        fontFamily = HindSiliguriFamily,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colors.textPrimary
                                                    )
                                                }

                                                // Quick bypass button: "Skip to Chat"
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(SolidButtonGreen)
                                                        .clickable {
                                                            onAnonymousSignIn("Friend") { _, _ -> }
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "🚀 Skip to Chat",
                                                        fontFamily = HindSiliguriFamily,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Divider: "or" / "অথবা"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = colors.border,
                                        thickness = 1.dp
                                    )
                                    Text(
                                        text = Strings.orDivider(selectedLanguage),
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 13.sp,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = colors.border,
                                        thickness = 1.dp
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Guest button: "Continue as guest" - Asking name on step 3
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SolidButtonGreen)
                                        .clickable {
                                            pendingGoogleAccount = null
                                            isGuestFlow = true
                                            authErrorMessage = null
                                            step = 3
                                        }
                                        .testTag("guest_login_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = Strings.continueAsGuest(selectedLanguage),
                                        fontFamily = BalooDa2Family,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Guest mode disclaimer
                                Text(
                                    text = Strings.guestModeDisclaimer(selectedLanguage),
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Terms and Privacy clickable legal notice
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (selectedLanguage == "bn") "চালিয়ে যাওয়ার মাধ্যমে আপনি " else "By continuing, you accept our ",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 11.5.sp,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = if (selectedLanguage == "bn") "শর্তাবলী" else "Terms",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 11.5.sp,
                                        color = colors.primary,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable { openBrowser(Strings.TERMS_URL) }
                                    )
                                    Text(
                                        text = if (selectedLanguage == "bn") " ও " else " & ",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 11.5.sp,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = if (selectedLanguage == "bn") "গোপনীয়তা নীতি" else "Privacy Policy",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 11.5.sp,
                                        color = colors.primary,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable { openBrowser(Strings.PRIVACY_URL) }
                                    )
                                }
                            }

                            // ── STEP 3: NAME (FOR NEW GOOGLE USER OR GUEST) ──
                            3 -> {
                                Text(
                                    text = Strings.onboardingStep2Title(selectedLanguage),
                                    fontFamily = BalooDa2Family,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (selectedLanguage == "bn") "বন্ধু তোমাকে এই নামে ডাকবে" else "Bondhu will call you by this name",
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 14.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                // Live Interactive Avatar Preview with dynamic initial
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(SolidButtonGreen)
                                        .border(2.5.dp, colors.border, CircleShape)
                                        .testTag("onboarding_avatar_preview"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val displayInitial = enteredName.trim().firstOrNull()?.uppercaseChar()
                                    if (displayInitial != null) {
                                        Text(
                                            text = "$displayInitial",
                                            fontFamily = BalooDa2Family,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Avatar",
                                            tint = Color.White,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Input: surfaceElevated bg, 2px border, 12dp radius, centered text
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceElevated)
                                        .border(
                                            width = 2.dp,
                                            color = if (nameError) colors.error else colors.border,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (enteredName.isEmpty()) {
                                        Text(
                                            text = Strings.onboardingStep2Placeholder(selectedLanguage),
                                            fontFamily = HindSiliguriFamily,
                                            fontSize = 16.sp,
                                            color = colors.textSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    BasicTextField(
                                        value = enteredName,
                                        onValueChange = {
                                            enteredName = it
                                            if (nameError && it.isNotBlank()) {
                                                nameError = false
                                            }
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = BalooDa2Family,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 18.sp,
                                            color = colors.textPrimary,
                                            textAlign = TextAlign.Center
                                        ),
                                        cursorBrush = SolidColor(colors.primary),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (enteredName.trim().isEmpty()) {
                                                    nameError = true
                                                    triggerShake()
                                                    Toast.makeText(
                                                        context,
                                                        Strings.onboardingStep2Validation(selectedLanguage),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    isAuthLoading = true
                                                    val cleanName = enteredName.trim()
                                                    if (pendingGoogleAccount != null) {
                                                        onGoogleSignInSuccess(pendingGoogleAccount!!, cleanName) { ok, err ->
                                                            isAuthLoading = false
                                                            if (!ok && err != null) {
                                                                authErrorMessage = err
                                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    } else {
                                                        onAnonymousSignIn(cleanName) { ok, err ->
                                                            isAuthLoading = false
                                                            if (!ok && err != null) {
                                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("name_input_field")
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Portion: Primary Action Button & Progress Dots
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        1 -> {
                            Button(
                                onClick = { step = 2 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_next_1"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SolidButtonGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = Strings.next(selectedLanguage),
                                    fontFamily = BalooDa2Family,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }

                        2 -> {
                            // Back button to return to Language selection
                            Text(
                                text = Strings.back(selectedLanguage),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textSecondary,
                                modifier = Modifier
                                    .clickable { step = 1 }
                                    .padding(8.dp)
                            )
                        }

                        3 -> {
                            Button(
                                onClick = {
                                    if (enteredName.trim().isEmpty()) {
                                        nameError = true
                                        triggerShake()
                                        Toast.makeText(
                                            context,
                                            Strings.onboardingStep2Validation(selectedLanguage),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        isAuthLoading = true
                                        val cleanName = enteredName.trim()
                                        if (pendingGoogleAccount != null) {
                                            onGoogleSignInSuccess(pendingGoogleAccount!!, cleanName) { ok, err ->
                                                isAuthLoading = false
                                                if (!ok && err != null) {
                                                    authErrorMessage = err
                                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            onAnonymousSignIn(cleanName) { ok, err ->
                                                isAuthLoading = false
                                                if (!ok && err != null) {
                                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = !isAuthLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_get_started"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SolidButtonGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = Strings.getStarted(selectedLanguage),
                                        fontFamily = BalooDa2Family,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Back to Step 2
                            Text(
                                text = Strings.back(selectedLanguage),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textSecondary,
                                modifier = Modifier
                                    .clickable {
                                        step = 2
                                        authErrorMessage = null
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..totalSteps) {
                            val isActive = i == currentStep
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) SolidButtonGreen else colors.border)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Onboarding Explanation Card:
 * Clean, sleek, minimalist guide card pinned at the top:
 * - Subtle background and delicate border
 * - Concise, elegant single-sentence guidance for each phase
 * - Discreet step badge
 */
@Composable
fun OnboardingExplanationCard(
    currentStep: Int,
    totalSteps: Int = 2,
    selectedLanguage: String,
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val isBn = selectedLanguage == "bn"

    val guidanceText = when (currentStep) {
        1 -> if (isBn) "এগিয়ে যেতে আপনার পছন্দের ভাষা নির্বাচন করুন" else "Choose your preferred language to continue"
        2 -> if (isBn) "চ্যাট ইতিহাস সংরক্ষণ করতে সাইন ইন করুন বা গেস্ট হিসেবে প্রবেশ করুন" else "Sign in to save chat history, or continue as guest"
        else -> if (isBn) "ব্যক্তিগত অভিজ্ঞতার জন্য আপনার নাম লিখুন" else "Enter your name to personalize your experience"
    }

    val stepBadge = when {
        totalSteps == 2 && currentStep == 1 -> if (isBn) "ধাপ ১/২" else "Step 1/2"
        totalSteps == 2 && currentStep == 2 -> if (isBn) "ধাপ ২/২" else "Step 2/2"
        currentStep == 1 -> if (isBn) "ধাপ ১/৩" else "Step 1/3"
        currentStep == 2 -> if (isBn) "ধাপ ২/৩" else "Step 2/3"
        else -> if (isBn) "ধাপ ৩/৩" else "Step 3/3"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (colors.isDark) Color(0xFF191919) else Color(0xFFF6F7F9))
            .border(
                width = 1.dp,
                color = colors.border.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(SolidButtonGreen)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = guidanceText,
                    fontFamily = HindSiliguriFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = colors.textPrimary,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.1f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stepBadge,
                    fontFamily = BalooDa2Family,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.primary
                )
            }
        }
    }
}
