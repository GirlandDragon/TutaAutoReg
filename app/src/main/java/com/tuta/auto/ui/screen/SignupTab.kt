package com.tuta.auto.ui.screen

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tuta.auto.TutaApp
import com.tuta.auto.data.model.Account
import com.tuta.auto.data.model.Message
import com.tuta.auto.ui.component.AccountCard
import com.tuta.auto.util.NameGenerator
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SignupTab(app: TutaApp) {
    val accounts by app.accountRepository.getAllAccounts().collectAsState(initial = emptyList())
    var emailPrefix by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var count by remember { mutableIntStateOf(1) }
    var isRunning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showCaptcha by remember { mutableStateOf(false) }
    var captchaCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = emailPrefix,
            onValueChange = { emailPrefix = it },
            label = { Text("Email Prefix (leave empty for random)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (leave empty for random)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (isRunning) return@Button
                isRunning = true
                val prefix = emailPrefix.ifBlank { NameGenerator.generateEmailPrefix() }
                val pwd = password.ifBlank { NameGenerator.generatePassword() }
                statusText = "Registering $prefix@tuta.com..."

                scope.launch {
                    try {
                        val account = Account(
                            email = "$prefix@tuta.com",
                            password = pwd
                        )
                        app.accountRepository.insertAccount(account)
                        statusText = "Account saved: $prefix@tuta.com"
                    } catch (e: Exception) {
                        statusText = "Error: ${e.message}"
                    } finally {
                        isRunning = false
                    }
                }
            },
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRunning) "Registering..." else "Add Account (Manual)")
        }

        if (statusText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Saved Accounts (${accounts.size})",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(accounts) { account ->
                AccountCard(
                    account = account,
                    onClick = { /* TODO: view details */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
