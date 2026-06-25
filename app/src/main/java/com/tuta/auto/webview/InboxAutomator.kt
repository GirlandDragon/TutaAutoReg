package com.tuta.auto.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

data class EmailEntry(
    val sender: String,
    val subject: String,
    val time: String,
    val snippet: String
)

class InboxAutomator(private val webView: WebView) {

    suspend fun login(email: String, password: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            webView.addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onLoginResult(json: String) {
                        val result = JSONObject(json)
                        continuation.resume(result.optBoolean("success", false))
                    }
                },
                "TutaInboxBridge"
            )

            webView.post {
                webView.evaluateJavascript(
                    """
                    (function() {
                        window.__tutaAuto = window.__tutaAuto || {};
                        window.__tutaAuto.login('$email', '$password');
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }
    }

    suspend fun fetchEmails(): List<EmailEntry> {
        return suspendCancellableCoroutine { continuation ->
            webView.addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onEmailsFetched(json: String) {
                        val arr = JSONArray(json)
                        val emails = mutableListOf<EmailEntry>()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            emails.add(
                                EmailEntry(
                                    sender = obj.optString("sender", ""),
                                    subject = obj.optString("subject", ""),
                                    time = obj.optString("time", ""),
                                    snippet = obj.optString("snippet", "")
                                )
                            )
                        }
                        continuation.resume(emails)
                    }
                },
                "TutaInboxBridge"
            )

            webView.post {
                webView.evaluateJavascript(
                    """
                    (function() {
                        window.__tutaAuto = window.__tutaAuto || {};
                        window.__tutaAuto.fetchEmails();
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }
    }
}
