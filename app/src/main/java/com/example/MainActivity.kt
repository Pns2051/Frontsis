package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BannedScreen
import com.example.ui.components.ChatScreen
import com.example.ui.components.LanguageSelectScreen
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

    BondhuTheme(darkTheme = uiState.isDarkMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (uiState.stage) {
                AppStage.SPLASH -> {
                    SplashScreen()
                }

                AppStage.LANGUAGE_SELECT -> {
                    LanguageSelectScreen(
                        onLanguageSelected = { lang ->
                            viewModel.selectInitialLanguage(lang)
                        }
                    )
                }

                AppStage.CHAT -> {
                    if (uiState.isBanned) {
                        BannedScreen(language = uiState.language)
                    } else {
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
