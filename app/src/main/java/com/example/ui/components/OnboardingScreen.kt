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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
 * SCREENS 2-5: ONBOARDING
 * Layout (all 4 steps):
 *   48dp top padding
 *   Character illustration (upper portion, 120dp)
 *   Title: 28sp Baloo Da 2, center
 *   Subtitle: 14sp secondary, center
 *   Content: middle
 *   Primary button: bottom, primary green, 12dp radius, 100% width, 52dp height, 15sp bold text
 *   Below: 4 progress dots
 */
@Composable
fun OnboardingScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onFinishOnboarding: (userName: String) -> Unit,
    onGoogleSignInSuccess: (GoogleSignInAccount) -> Unit = {},
    onEmailSignIn: (email: String, pass: String, (Boolean, String?) -> Unit) -> Unit = { _, _, _ -> },
    onEmailSignUp: (name: String, email: String, pass: String, (Boolean, String?) -> Unit) -> Unit = { _, _, _, _ -> },
    onAnonymousSignIn: ((Boolean, String?) -> Unit) -> Unit = { it(true, null) },
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    var step by remember { mutableIntStateOf(1) }
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    var enteredName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    var acceptedPrivacy by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    var isAuthLoading by remember { mutableStateOf(false) }
    var showEmailAuth by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isAuthLoading = false
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                isAuthLoading = true
                onGoogleSignInSuccess(account)
            }
        } catch (e: ApiException) {
            val msg = when (e.statusCode) {
                12501 -> "Sign-in cancelled"
                12500 -> "Google Play services error (12500): Check SHA-1 in Firebase Console"
                10 -> "Configuration error (Code 10): App SHA-1 is not registered in Firebase Console"
                7 -> "Network error: Please check your internet connection"
                else -> "Google sign-in error (code ${e.statusCode})"
            }
            authErrorMessage = msg
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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

                        // ── STEP 2: NAME ──
                        2 -> {
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

                            // Live Interactive Avatar Preview with emerald ring and dynamic initial
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
                                                step = 3
                                            }
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("name_input_field")
                                )
                            }
                        }

                        // ── STEP 3: SIGN IN ──
                        3 -> {
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
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surface)
                                    .border(2.dp, colors.border, RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isAuthLoading) {
                                        authErrorMessage = null
                                        isAuthLoading = true
                                        try {
                                            val client = AuthManager.getGoogleSignInClient(context)
                                            googleSignInLauncher.launch(client.signInIntent)
                                        } catch (e: Exception) {
                                            isAuthLoading = false
                                            authErrorMessage = e.localizedMessage
                                            Toast.makeText(context, e.localizedMessage ?: "Could not start Google sign in", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .testTag("google_login_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAuthLoading && !showEmailAuth) {
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
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "G",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF4285F4)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
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

                            Spacer(modifier = Modifier.height(12.dp))

                            if (authErrorMessage != null && !showEmailAuth) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.error.copy(alpha = 0.12f))
                                        .border(1.dp, colors.error.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = authErrorMessage ?: "",
                                            fontFamily = HindSiliguriFamily,
                                            fontSize = 12.5.sp,
                                            color = colors.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Button to copy SHA-1
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(colors.surfaceElevated)
                                                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("SHA-1", AuthManager.SHA1_FINGERPRINT))
                                                        Toast.makeText(context, "SHA-1 copied to clipboard! Add to Firebase Console", Toast.LENGTH_SHORT).show()
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
                                                        onAnonymousSignIn { _, _ -> }
                                                        step = 4
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
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Email Button / Expandable form
                            if (!showEmailAuth) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surface)
                                        .border(2.dp, colors.border, RoundedCornerShape(12.dp))
                                        .clickable {
                                            showEmailAuth = true
                                            authErrorMessage = null
                                        }
                                        .testTag("email_login_toggle_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = Strings.continueWithEmail(selectedLanguage),
                                            fontFamily = BalooDa2Family,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            } else {
                                // Expanded Email/Password Card
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceElevated)
                                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isSignUpMode) Strings.signUp(selectedLanguage) else Strings.signIn(selectedLanguage),
                                            fontFamily = BalooDa2Family,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = if (isSignUpMode) Strings.signIn(selectedLanguage) else Strings.signUp(selectedLanguage),
                                            fontFamily = HindSiliguriFamily,
                                            fontSize = 13.sp,
                                            color = colors.primary,
                                            textDecoration = TextDecoration.Underline,
                                            modifier = Modifier.clickable {
                                                isSignUpMode = !isSignUpMode
                                                authErrorMessage = null
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Email field
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.surface)
                                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        if (emailInput.isEmpty()) {
                                            Text(
                                                text = Strings.emailLabel(selectedLanguage),
                                                fontFamily = HindSiliguriFamily,
                                                fontSize = 14.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        BasicTextField(
                                            value = emailInput,
                                            onValueChange = { emailInput = it },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                fontFamily = HindSiliguriFamily,
                                                fontSize = 14.sp,
                                                color = colors.textPrimary
                                            ),
                                            cursorBrush = SolidColor(colors.primary),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Password field
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.surface)
                                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                if (passwordInput.isEmpty()) {
                                                    Text(
                                                        text = Strings.passwordLabel(selectedLanguage),
                                                        fontFamily = HindSiliguriFamily,
                                                        fontSize = 14.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                                BasicTextField(
                                                    value = passwordInput,
                                                    onValueChange = { passwordInput = it },
                                                    singleLine = true,
                                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                    textStyle = TextStyle(
                                                        fontFamily = HindSiliguriFamily,
                                                        fontSize = 14.sp,
                                                        color = colors.textPrimary
                                                    ),
                                                    cursorBrush = SolidColor(colors.primary),
                                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            IconButton(
                                                onClick = { isPasswordVisible = !isPasswordVisible },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (authErrorMessage != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = authErrorMessage ?: "",
                                            fontFamily = HindSiliguriFamily,
                                            fontSize = 12.sp,
                                            color = colors.error
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (emailInput.isBlank() || passwordInput.length < 6) {
                                                authErrorMessage = if (emailInput.isBlank()) "Please enter your email" else "Password must be at least 6 characters"
                                                return@Button
                                            }
                                            isAuthLoading = true
                                            authErrorMessage = null
                                            if (isSignUpMode) {
                                                onEmailSignUp(enteredName, emailInput, passwordInput) { ok, err ->
                                                    isAuthLoading = false
                                                    if (!ok) {
                                                        authErrorMessage = err ?: "Sign up failed"
                                                    }
                                                }
                                            } else {
                                                onEmailSignIn(emailInput, passwordInput) { ok, err ->
                                                    isAuthLoading = false
                                                    if (!ok) {
                                                        authErrorMessage = err ?: "Sign in failed"
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !isAuthLoading,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SolidButtonGreen,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        if (isAuthLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text(
                                                text = if (isSignUpMode) Strings.signUp(selectedLanguage) else Strings.signIn(selectedLanguage),
                                                fontFamily = BalooDa2Family,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

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

                            Spacer(modifier = Modifier.height(14.dp))

                            // Guest button: "Continue as guest" - Fully opaque solid green
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SolidButtonGreen)
                                    .clickable {
                                        // Trigger anonymous Firebase login in background
                                        onAnonymousSignIn { _, _ -> }
                                        step = 4
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

                            Spacer(modifier = Modifier.height(14.dp))

                            // Below: "Guest mode: credits & history stored on this device"
                            Text(
                                text = Strings.guestModeDisclaimer(selectedLanguage),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        // ── STEP 4: PRIVACY & TERMS ──
                        4 -> {
                            Text(
                                text = Strings.onboardingStep4Title(selectedLanguage),
                                fontFamily = BalooDa2Family,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Strings.onboardingStep4Sub(selectedLanguage),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            // Checkbox 1: Privacy Policy
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { acceptedPrivacy = !acceptedPrivacy }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = acceptedPrivacy,
                                    onCheckedChange = { acceptedPrivacy = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = colors.primary,
                                        uncheckedColor = colors.textSecondary,
                                        checkmarkColor = colors.onPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "I accept the ",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Privacy Policy",
                                        fontFamily = HindSiliguriFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = colors.primary,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable {
                                            openBrowser(Strings.PRIVACY_URL)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Checkbox 2: Terms of Service
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { acceptedTerms = !acceptedTerms }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = acceptedTerms,
                                    onCheckedChange = { acceptedTerms = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = colors.primary,
                                        uncheckedColor = colors.textSecondary,
                                        checkmarkColor = colors.onPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "I accept the ",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Terms of Service",
                                        fontFamily = HindSiliguriFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = colors.primary,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable {
                                            openBrowser(Strings.TERMS_URL)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

                // Bottom Portion: Primary Action Button & 4 Progress Dots
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
                                        step = 3
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_next_2"),
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

                        3 -> {
                            // On Step 3 the action buttons are Google, Email, and Guest mode
                            // Subtle "Back" button to return to name step if needed
                            Text(
                                text = Strings.back(selectedLanguage),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textSecondary,
                                modifier = Modifier
                                    .clickable { step = 2 }
                                    .padding(8.dp)
                            )
                        }

                        4 -> {
                            Button(
                                onClick = {
                                    if (acceptedPrivacy && acceptedTerms) {
                                        val finalName = enteredName.trim().ifBlank {
                                            if (selectedLanguage == "bn") "বন্ধু" else "Friend"
                                        }
                                        onFinishOnboarding(finalName)
                                    } else {
                                        triggerShake()
                                        Toast.makeText(
                                            context,
                                            if (selectedLanguage == "bn") "দয়া করে গোপনীয়তা নীতি ও ব্যবহারের শর্তাবলী গ্রহণ করুন" else "Please accept Privacy Policy and Terms of Service to continue",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
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
                                Text(
                                    text = Strings.getStarted(selectedLanguage),
                                    fontFamily = BalooDa2Family,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 Progress Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..4) {
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
    selectedLanguage: String,
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val isBn = selectedLanguage == "bn"

    val guidanceText = when (currentStep) {
        1 -> if (isBn) "এগিয়ে যেতে আপনার পছন্দের ভাষা নির্বাচন করুন" else "Choose your preferred language to continue"
        2 -> if (isBn) "ব্যক্তিগত অভিজ্ঞতার জন্য আপনার নাম লিখুন" else "Enter your name to personalize your experience"
        3 -> if (isBn) "চ্যাট ইতিহাস সংরক্ষণ করতে সাইন ইন করুন বা সরাসরি চালিয়ে যান" else "Sign in to save chat history, or continue as guest"
        else -> if (isBn) "শুরু করতে গোপনীয়তা নীতি ও ব্যবহারের শর্তাবলীতে সম্মতি দিন" else "Agree to Privacy Policy & Terms of Service to start"
    }

    val stepBadge = when (currentStep) {
        1 -> if (isBn) "ধাপ ১/৪" else "Step 1/4"
        2 -> if (isBn) "ধাপ ২/৪" else "Step 2/4"
        3 -> if (isBn) "ধাপ ৩/৪" else "Step 3/4"
        else -> if (isBn) "ধাপ ৪/৪" else "Step 4/4"
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

