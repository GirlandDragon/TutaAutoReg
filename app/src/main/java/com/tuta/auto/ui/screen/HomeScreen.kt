package com.tuta.auto.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tuta.auto.TutaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    app: TutaApp,
    onOpenSettings: () -> Unit
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Sign Up", "Login")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TutaAutoReg") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(padding)
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (tabIndex) {
                0 -> SignupTab(app = app)
                1 -> LoginTab(app = app)
            }
        }
    }
}
