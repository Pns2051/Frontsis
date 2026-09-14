package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ChatScreen
import com.example.ui.components.OnboardingScreen
import com.example.ui.components.SplashScreen
import com.example.ui.theme.BondhuTheme
import com.example.ui.viewmodel.AppStage
import com.example.ui.viewmodel.BondhuViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BondhuApp()
        }
    }
}

@Composable
fun BondhuApp(
    viewModel: BondhuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BondhuTheme(themeMode = uiState.themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = uiState.stage,
                animationSpec = tween(300),
                label = "app_stage_crossfade"
            ) { stage ->
                when (stage) {
                    AppStage.SPLASH -> {
                        SplashScreen(
                            language = uiState.language,
                            onFinished = { viewModel.onSplashComplete() }
                        )
                    }

                    AppStage.ONBOARDING -> {
                        OnboardingScreen(
                            currentLanguage = uiState.language,
                            onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                            onFinishOnboarding = { name -> viewModel.completeOnboarding(name) },
                            onGoogleSignInSuccess = { account ->
                                viewModel.signInWithGoogleAccount(account) { success, err ->
                                    if (!success && err != null) {
                                        viewModel.showToast(err)
                                    }
                                }
                            },
                            onEmailSignIn = { email, pass, onResult ->
                                viewModel.signInWithEmail(email = email, pass = pass, onResult = onResult)
                            },
                            onEmailSignUp = { name, email, pass, onResult ->
                                viewModel.signUpWithEmail(name = name, email = email, pass = pass, onResult = onResult)
                            },
                            onAnonymousSignIn = { onResult ->
                                viewModel.signInAnonymously(onResult = onResult)
                            }
                        )
                    }

                    AppStage.CHAT -> {
                        ChatScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    }
                }
            }
        }
    }
}
