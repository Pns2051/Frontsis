package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.auth.AuthManager
import com.example.ui.i18n.Strings
import com.example.ui.theme.BalooDa2Family
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.HindSiliguriFamily
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

/**
 * ═══════════ SCREEN 8: SETTINGS ═══════════
 * Modern, futuristic, high-contrast settings dialog with:
 * - Dynamic light/dark theme adaptation via BondhuTheme.colors
 * - Simplified API configurations with one-tap presets
 * - Clean typography and spacious touch targets
 */
@Composable
fun SettingsDialog(
    language: String,
    themeMode: String,
    userName: String,
    userEmail: String = "",
    userBio: String = "",
    loginType: String = "guest",
    currentModel: String = "light",
    fontSize: String = "normal",
    animationsEnabled: Boolean = true,
    isCustomApiEnabled: Boolean,
    customApiEndpoint: String,
    customApiKey: String,
    customApiModel: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onSaveName: (String) -> Unit,
    onSaveBio: (String) -> Unit = {},
    onModelChange: (String) -> Unit = {},
    onFontSizeChange: (String) -> Unit = {},
    onAnimationsChange: (Boolean) -> Unit = {},
    onConnectGoogle: ((GoogleSignInAccount) -> Unit)? = null,
    onSaveCustomApi: (enabled: Boolean, endpoint: String, apiKey: String, model: String) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = BondhuTheme.colors

    var nameInput by remember { mutableStateOf(userName) }
    var bioInput by remember { mutableStateOf(userBio) }
    var selectedTheme by remember { mutableStateOf(themeMode) }
    var selectedModel by remember { mutableStateOf(currentModel) }
    var selectedFontSize by remember { mutableStateOf(fontSize) }
    var isAnimationsOn by remember { mutableStateOf(animationsEnabled) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                onConnectGoogle?.invoke(account)
                Toast.makeText(context, "Google account connected!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    // Simplified API Provider state
    val defaultEndpoint = "https://bondhu-ai-backed-beta26.onrender.com"
    var endpointInput by remember { mutableStateOf(customApiEndpoint.ifBlank { defaultEndpoint }) }
    var apiKeyInput by remember { mutableStateOf(customApiKey) }
    var modelInput by remember { mutableStateOf(customApiModel.ifBlank { "gpt-4o-mini" }) }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedProvider by remember {
        mutableStateOf(
            when {
                !isCustomApiEnabled || endpointInput.contains("onrender.com") -> "default"
                endpointInput.contains("api.openai.com") -> "openai"
                endpointInput.contains("api.groq.com") -> "groq"
                endpointInput.contains("generativelanguage.googleapis.com") -> "gemini"
                else -> "custom"
            }
        )
    }

    var showLogoutConfirm by remember { mutableStateOf(false) }

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("settings_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Strings.settings(language),
                        fontFamily = BalooDa2Family,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colors.textPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.border,
                    thickness = 1.dp
                )

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── SECTION: ACCOUNT ──
                    SettingsSection(title = Strings.account(language)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.signInMode(language),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = when (loginType) {
                                    "google" -> "Google"
                                    "email" -> "Email"
                                    "anonymous" -> "Guest (Anonymous)"
                                    else -> "Guest"
                                },
                                fontFamily = BalooDa2Family,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.primary
                            )
                        }

                        if (userEmail.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userEmail,
                                fontFamily = HindSiliguriFamily,
                                fontSize = 12.5.sp,
                                color = colors.textSecondary
                            )
                        }

                        if (loginType != "google") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val client = AuthManager.getGoogleSignInClient(context)
                                        client.signOut().addOnCompleteListener {
                                            try {
                                                googleSignInLauncher.launch(client.signInIntent)
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, ex.localizedMessage ?: "Could not start Google sign in", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.localizedMessage ?: "Could not start Google sign in", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary)
                            ) {
                                Text(
                                    text = Strings.connectGoogle(language),
                                    fontFamily = BalooDa2Family,
                                    fontSize = 13.sp,
                                    color = colors.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Name field + Save
                        Text(
                            text = Strings.nameLabel(language),
                            fontFamily = HindSiliguriFamily,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceRaised)
                                    .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                BasicTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    ),
                                    cursorBrush = SolidColor(colors.primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Button(
                                onClick = {
                                    if (nameInput.isNotBlank()) {
                                        onSaveName(nameInput.trim())
                                        Toast.makeText(context, Strings.saved(language), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary
                                )
                            ) {
                                Text(
                                    text = Strings.save(language),
                                    fontFamily = BalooDa2Family,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Profile description
                        Text(
                            text = Strings.profileDesc(language),
                            fontFamily = HindSiliguriFamily,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceRaised)
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            if (bioInput.isEmpty()) {
                                Text(
                                    text = Strings.profileDescPlaceholder(language),
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 13.sp,
                                    color = colors.textTertiary
                                )
                            }
                            BasicTextField(
                                value = bioInput,
                                onValueChange = {
                                    bioInput = it
                                    onSaveBio(it)
                                },
                                textStyle = TextStyle(
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                ),
                                cursorBrush = SolidColor(colors.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── SECTION: AI MODEL ──
                    SettingsSection(title = Strings.aiModel(language)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isLight = selectedModel == "light"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isLight) colors.surfaceRaised else colors.surface)
                                    .border(
                                        1.5.dp,
                                        if (isLight) colors.primary else colors.border,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedModel = "light"
                                        onModelChange("light")
                                    }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Bondhu Light",
                                        fontFamily = BalooDa2Family,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isLight) colors.primary else colors.textPrimary
                                    )
                                    Text(
                                        text = "Default",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            val isReasoning = selectedModel == "reasoning"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isReasoning) colors.surfaceRaised else colors.surface)
                                    .border(
                                        1.5.dp,
                                        if (isReasoning) colors.primary else colors.border,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedModel = "reasoning"
                                        onModelChange("reasoning")
                                    }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Bondhu 5.3",
                                        fontFamily = BalooDa2Family,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isReasoning) colors.primary else colors.textPrimary
                                    )
                                    Text(
                                        text = "Reasoning",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // ── SECTION: APPEARANCE ──
                    SettingsSection(title = Strings.appearance(language)) {
                        // Theme: Light / Dark
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.theme(language),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val isDark = selectedTheme == "dark"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) colors.primary.copy(alpha = 0.15f) else colors.surfaceRaised)
                                        .border(
                                            1.dp,
                                            if (isDark) colors.primary else colors.border,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedTheme = "dark"
                                            onThemeChange("dark")
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = Strings.themeDark(language),
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 12.sp,
                                        color = if (isDark) colors.primary else colors.textSecondary
                                    )
                                }

                                val isLight = selectedTheme == "light"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isLight) colors.primary.copy(alpha = 0.15f) else colors.surfaceRaised)
                                        .border(
                                            1.dp,
                                            if (isLight) colors.primary else colors.border,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedTheme = "light"
                                            onThemeChange("light")
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = Strings.themeLight(language),
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 12.sp,
                                        color = if (isLight) colors.primary else colors.textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Font size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.font(language),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("small", "normal", "large").forEach { sizeOpt ->
                                    val isSelected = selectedFontSize == sizeOpt
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surfaceRaised)
                                            .border(
                                                1.dp,
                                                if (isSelected) colors.primary else colors.border,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedFontSize = sizeOpt
                                                onFontSizeChange(sizeOpt)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = when (sizeOpt) {
                                                "small" -> Strings.fontSmall(language)
                                                "large" -> Strings.fontLarge(language)
                                                else -> Strings.fontNormal(language)
                                            },
                                            fontFamily = HindSiliguriFamily,
                                            fontSize = 11.sp,
                                            color = if (isSelected) colors.primary else colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Animations: On / Off
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.animations(language),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isAnimationsOn) colors.primary.copy(alpha = 0.15f) else colors.surfaceRaised)
                                        .border(
                                            1.dp,
                                            if (isAnimationsOn) colors.primary else colors.border,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            isAnimationsOn = true
                                            onAnimationsChange(true)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = Strings.animationsOn(language),
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 12.sp,
                                        color = if (isAnimationsOn) colors.primary else colors.textSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isAnimationsOn) colors.primary.copy(alpha = 0.15f) else colors.surfaceRaised)
                                        .border(
                                            1.dp,
                                            if (!isAnimationsOn) colors.primary else colors.border,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            isAnimationsOn = false
                                            onAnimationsChange(false)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = Strings.animationsOff(language),
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 12.sp,
                                        color = if (!isAnimationsOn) colors.primary else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // ── SECTION: SIMPLIFIED API SETTINGS ──
                    SettingsSection(title = Strings.customApi(language)) {
                        Text(
                            text = if (language == "bn") "সহজেই নিজের API কী ব্যবহার করুন অথবা ডিফল্ট সার্ভারেই চ্যাট করুন।" else "Use your own AI provider or chat directly with the default server.",
                            fontFamily = HindSiliguriFamily,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Provider Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val providers = listOf(
                                "default" to "Default",
                                "openai" to "OpenAI",
                                "groq" to "Groq",
                                "custom" to "Custom"
                            )
                            providers.forEach { (key, label) ->
                                val isSelected = selectedProvider == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.primary.copy(alpha = 0.18f) else colors.surfaceRaised)
                                        .border(
                                            1.dp,
                                            if (isSelected) colors.primary else colors.border,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedProvider = key
                                            when (key) {
                                                "default" -> {
                                                    endpointInput = defaultEndpoint
                                                    apiKeyInput = ""
                                                    modelInput = "light"
                                                }
                                                "openai" -> {
                                                    endpointInput = "https://api.openai.com/v1"
                                                    modelInput = "gpt-4o-mini"
                                                }
                                                "groq" -> {
                                                    endpointInput = "https://api.groq.com/openai/v1"
                                                    modelInput = "llama-3.3-70b-versatile"
                                                }
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) colors.primary else colors.textSecondary
                                    )
                                }
                            }
                        }

                        if (selectedProvider != "default") {
                            Spacer(modifier = Modifier.height(12.dp))

                            // API Key Field
                            Text(
                                text = Strings.apiKeyLabel(language),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceRaised)
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = apiKeyInput,
                                        onValueChange = { apiKeyInput = it },
                                        singleLine = true,
                                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                        textStyle = TextStyle(
                                            fontFamily = HindSiliguriFamily,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        ),
                                        cursorBrush = SolidColor(colors.primary),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { showApiKey = !showApiKey },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle key",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (selectedProvider == "custom") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Strings.endpointLabel(language),
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceRaised)
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    BasicTextField(
                                        value = endpointInput,
                                        onValueChange = { endpointInput = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = HindSiliguriFamily,
                                            fontSize = 12.sp,
                                            color = colors.textPrimary
                                        ),
                                        cursorBrush = SolidColor(colors.primary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Save & Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val enabled = selectedProvider != "default" && apiKeyInput.isNotBlank()
                                    onSaveCustomApi(
                                        enabled,
                                        endpointInput.trim(),
                                        apiKeyInput.trim(),
                                        modelInput.trim()
                                    )
                                    Toast.makeText(context, Strings.apiConfigSaved(language), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary
                                )
                            ) {
                                Text(
                                    text = Strings.save(language),
                                    fontFamily = BalooDa2Family,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    selectedProvider = "default"
                                    endpointInput = defaultEndpoint
                                    apiKeyInput = ""
                                    modelInput = "light"
                                    onSaveCustomApi(false, defaultEndpoint, "", "light")
                                    Toast.makeText(context, "Reset to default", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = Strings.removeKeyButton(language),
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // ── SECTION: LEGAL ──
                    SettingsSection(title = Strings.legal(language)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openBrowser(Strings.PRIVACY_URL) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.privacyPolicy(language),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                            Text(text = "↗", color = colors.textSecondary, fontSize = 14.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openBrowser(Strings.TERMS_URL) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.termsOfService(language),
                                fontFamily = HindSiliguriFamily,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                            Text(text = "↗", color = colors.textSecondary, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // [Log out / Reset]
                        Text(
                            text = Strings.logoutReset(language),
                            fontFamily = BalooDa2Family,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.error,
                            modifier = Modifier
                                .clickable { showLogoutConfirm = true }
                                .padding(vertical = 8.dp)
                                .testTag("settings_logout_button")
                        )
                    }
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    text = Strings.logoutReset(language),
                    fontFamily = BalooDa2Family,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out? Local chat history on this device will be cleared.",
                    fontFamily = HindSiliguriFamily,
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onDismiss()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Reset",
                        fontFamily = BalooDa2Family,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutConfirm = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = Strings.cancel(language),
                        fontFamily = HindSiliguriFamily,
                        color = colors.textSecondary
                    )
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = BondhuTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontFamily = BalooDa2Family,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = colors.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
