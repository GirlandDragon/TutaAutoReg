package com.tuta.auto.ui.screen

import android.annotation.SuppressLint
import android.view.ViewGroup
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tuta.auto.TutaApp
import com.tuta.auto.data.model.Account
import com.tuta.auto.ui.component.AccountCard
import com.tuta.auto.util.NameGenerator
import com.tuta.auto.webview.SignupAutomator
import com.tuta.auto.webview.SignupEvent
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SignupTab(app: TutaApp) {
    val accounts by app.accountRepository.getAllAccounts().collectAsState(initial = emptyList())
    var emailPrefix by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showCaptcha by remember { mutableStateOf(false) }
    var automator by remember { mutableStateOf<SignupAutomator?>(null) }
    var lastPrefix by remember { mutableStateOf("") }
    var lastPwd by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(automator) {
        automator?.events?.observeForever { event ->
            when (event) {
                is SignupEvent.Success -> {
                    isRunning = false
                    statusText = "Registered: ${event.email}"
                    scope.launch {
                        app.accountRepository.insertAccount(
                            Account(email = event.email, password = lastPwd)
                        )
                    }
                }
                is SignupEvent.CaptchaRequired -> {
                    showCaptcha = true
                }
                is SignupEvent.Error -> {
                    isRunning = false
                    statusText = "Error: ${event.message}"
                }
            }
        }
    }

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
                val prefix = emailPrefix.ifBlank { NameGenerator.generateEmailPrefix() }
                val pwd = password.ifBlank { NameGenerator.generatePassword() }
                lastPrefix = prefix
                lastPwd = pwd
                isRunning = true
                statusText = "Registering $prefix@tuta.com..."

                automator?.startSignup(prefix, pwd)
            },
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRunning) "Registering..." else "Start Registration")
        }

        if (statusText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(1, 1)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                automator?.onPageLoaded(url ?: "")
                            }
                        }
                        loadUrl("https://app.tuta.com/signup")
                        webView = this
                        automator = SignupAutomator(this)
                        }
                },
                modifier = Modifier.fillMaxSize()
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
                    onClick = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showCaptcha) {
        CaptchaDialog(
            onDismiss = { showCaptcha = false },
            onConfirm = { answer ->
                showCaptcha = false
                automator?.submitCaptchaAnswer(answer)
            }
        )
    }
}
