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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tuta.auto.data.model.Message
import com.tuta.auto.webview.EmailEntry
import com.tuta.auto.webview.InboxAutomator
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginTab(app: TutaApp) {
    val accounts by app.accountRepository.getAllAccounts().collectAsState(initial = emptyList())
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loggedIn by remember { mutableStateOf(false) }
    var inboxEmails by remember { mutableStateOf<List<EmailEntry>>(emptyList()) }
    var statusText by remember { mutableStateOf("") }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var automator by remember { mutableStateOf<InboxAutomator?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!loggedIn) {
            Text(
                text = "Select Account",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(accounts) { account ->
                    Card(
                        onClick = {
                            selectedAccountId = account.id
                            email = account.email
                            password = account.password
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (account.id == selectedAccountId)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = account.email,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    webView?.loadUrl("https://app.tuta.com/login")
                    statusText = "Loading login page..."
                },
                enabled = email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login & Check Inbox")
            }
        } else {
            Text(
                text = "Inbox: $email",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (inboxEmails.isEmpty()) {
                Text(
                    text = "No messages found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(inboxEmails) { entry ->
                        EmailCard(entry)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    automator?.fetchEmails()
                    statusText = "Refreshing..."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    loggedIn = false
                    inboxEmails = emptyList()
                    statusText = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }

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
                        val currentAutomator = InboxAutomator(this)
                        automator = currentAutomator
                        webView = this

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                when {
                                    url?.contains("/login") == true -> {
                                        currentAutomator.loginAndFetch(email, password)
                                    }
                                    url?.contains("/mail") == true -> {
                                        currentAutomator.fetchEmails()
                                    }
                                }
                            }
                        }

                        currentAutomator.emails.observeForever { emails ->
                            inboxEmails = emails
                            loggedIn = true
                            statusText = "Found ${emails.size} email(s)"
                            val currentEmail = email
                            scope.launch {
                                val account = app.accountRepository.getAccountByEmail(currentEmail)
                                account?.let { acct ->
                                    for (e in emails) {
                                        app.messageRepository.insertMessage(
                                            Message(
                                                accountId = acct.id,
                                                sender = e.sender,
                                                subject = e.subject,
                                                body = ""
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        currentAutomator.loginResult.observeForever { success ->
                            statusText = if (success) "Logged in, fetching..." else "Login failed"
                        }

                        loadUrl("https://app.tuta.com/login")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (statusText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmailCard(entry: EmailEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = entry.subject,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${entry.sender}  •  ${entry.time}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
