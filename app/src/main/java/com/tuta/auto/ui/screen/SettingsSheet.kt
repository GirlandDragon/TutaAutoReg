package com.tuta.auto.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuta.auto.TutaApp
import com.tuta.auto.util.PreferenceManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    app: TutaApp,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val themeMode by app.preferenceManager.themeMode.collectAsState(initial = PreferenceManager.ThemeMode.SYSTEM)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            ThemeOption(
                label = "Follow System",
                selected = themeMode == PreferenceManager.ThemeMode.SYSTEM,
                onClick = {
                    scope.launch {
                        app.preferenceManager.setThemeMode(PreferenceManager.ThemeMode.SYSTEM)
                    }
                }
            )
            ThemeOption(
                label = "Light",
                selected = themeMode == PreferenceManager.ThemeMode.LIGHT,
                onClick = {
                    scope.launch {
                        app.preferenceManager.setThemeMode(PreferenceManager.ThemeMode.LIGHT)
                    }
                }
            )
            ThemeOption(
                label = "Dark",
                selected = themeMode == PreferenceManager.ThemeMode.DARK,
                onClick = {
                    scope.launch {
                        app.preferenceManager.setThemeMode(PreferenceManager.ThemeMode.DARK)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Logout Current Account",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
