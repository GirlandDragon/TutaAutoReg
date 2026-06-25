package com.tuta.auto.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

class SignupAutomator(
    private val webView: WebView,
    private val onCaptchaRequired: suspend (suspend (String) -> Unit) -> Unit
) {

    private var stepChannel = Channel<StepResult>(Channel.CONFLATED)
    private var captchaResolver: (suspend (String) -> Unit)? = null

    sealed class StepResult {
        data class Success(val email: String, val recoveryCode: String = "") : StepResult()
        data class CaptchaRequired(val imageUrl: String = "") : StepResult()
        data class Error(val message: String) : StepResult()
    }

    suspend fun register(emailPrefix: String, password: String): StepResult {
        return suspendCancellableCoroutine { continuation ->
            webView.addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onStepComplete(json: String) {
                        val result = JSONObject(json)
                        when (result.getString("type")) {
                            "success" -> continuation.resume(
                                StepResult.Success(
                                    email = result.getString("email"),
                                    recoveryCode = result.optString("recoveryCode")
                                )
                            )
                            "captcha" -> {
                                val resolver: suspend (String) -> Unit = { answer ->
                                    webView.post {
                                        webView.evaluateJavascript(
                                            "window.__tutaAuto.solveCaptcha('$answer');",
                                            null
                                        )
                                    }
                                }
                                onCaptchaRequired(resolver)
                            }
                            "error" -> continuation.resume(
                                StepResult.Error(result.getString("message"))
                            )
                        }
                    }
                },
                "TutaAutoBridge"
            )

            webView.post {
                webView.evaluateJavascript(
                    """
                    (function() {
                        window.__tutaAuto = window.__tutaAuto || {};
                        window.__tutaAuto.startSignup('$emailPrefix', '$password');
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }
    }
}
