package com.tuta.auto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tuta.auto.ui.screen.HomeScreen
import com.tuta.auto.ui.screen.SettingsSheet
import com.tuta.auto.ui.theme.TutaAutoRegTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TutaApp

        setContent {
            TutaAutoRegTheme {
                var showSettings by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                HomeScreen(
                    app = app,
                    onOpenSettings = { showSettings = true }
                )

                if (showSettings) {
                    SettingsSheet(
                        app = app,
                        onDismiss = { showSettings = false },
                        onLogout = {
                            scope.launch {
                                app.preferenceManager.setCurrentAccountId(null)
                                showSettings = false
                            }
                        }
                    )
                }
            }
        }
    }
}
