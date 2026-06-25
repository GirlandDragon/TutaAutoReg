package com.tuta.auto.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CompletableDeferred

sealed class SignupEvent {
    data class Success(val email: String) : SignupEvent()
    data object CaptchaRequired : SignupEvent()
    data class Error(val message: String) : SignupEvent()
}

class SignupAutomator(private val webView: WebView) {

    private var continuation: CompletableDeferred<String>? = null

    private val _events = MutableLiveData<SignupEvent>()
    val events: LiveData<SignupEvent> = _events

    fun startSignup(emailPrefix: String, password: String) {
        webView.evaluateJavascript(
            ASSETS_SCRIPT,
            null
        )

        webView.postDelayed({
            webView.evaluateJavascript(
                buildSignupJs(emailPrefix, password),
                null
            )
        }, 2000)
    }

    fun submitCaptchaAnswer(answer: String) {
        webView.evaluateJavascript(
            "window.__tutaAuto.solveCaptcha('$answer');",
            null
        )
    }

    fun onPageLoaded(url: String) {
        if (url.contains("/mail") || url.contains("inbox")) {
            _events.postValue(SignupEvent.Success("unknown"))
        }
    }

    private fun buildSignupJs(prefix: String, pwd: String): String {
        return """
        (function() {
            window.__tutaAuto = window.__tutaAuto || {};
            
            function trigger(el, val) {
                el.value = val;
                el.dispatchEvent(new Event('input', {bubbles: true}));
                el.dispatchEvent(new Event('change', {bubbles: true}));
            }
            
            function fillForm() {
                var emailInput = document.querySelector(
                    'input[type="email"], input[name*="mail"], input[autocomplete*="email"]'
                ) || document.querySelector('input:not([type="hidden"]):not([type="password"])');
                var pwdInputs = document.querySelectorAll('input[type="password"]');
                
                if (emailInput) trigger(emailInput, '$prefix');
                if (pwdInputs[0]) trigger(pwdInputs[0], '$pwd');
                if (pwdInputs[1]) trigger(pwdInputs[1], '$pwd');
            }
            
            function checkCaptcha() {
                var captcha = document.querySelector(
                    'img[alt*="clock"], [class*="captcha"], [data-test*="captcha"]'
                );
                return captcha ? true : false;
            }
            
            function submitForm() {
                var btn = document.querySelector(
                    'button[type="submit"]'
                ) || document.querySelector('button:not([aria-label])');
                if (btn) btn.click();
            }
            
            setTimeout(function() {
                fillForm();
                setTimeout(function() {
                    var hasCaptcha = checkCaptcha();
                    if (hasCaptcha) {
                        try {
                            Android.onCaptchaRequired();
                        } catch(e) {
                            // fallback
                        }
                    } else {
                        submitForm();
                    }
                }, 3000);
            }, 2000);
        })();
        """.trimIndent()
    }

    companion object {
        private const val ASSETS_SCRIPT = """
        (function() {
            window.__tutaAuto = window.__tutaAuto || {};
            window.__tutaAuto.solveCaptcha = function(answer) {
                var input = document.querySelector(
                    'input[placeholder*="time"], input[placeholder*="clock"], ' +
                    'input[placeholder*="HH:MM"], [class*="captcha"] input'
                );
                if (input) {
                    input.value = answer;
                    input.dispatchEvent(new Event('input', {bubbles: true}));
                    input.dispatchEvent(new Event('change', {bubbles: true}));
                    
                    var btn = document.querySelector(
                        '[class*="captcha"] button, button[type="submit"]'
                    );
                    if (btn) setTimeout(function() { btn.click(); }, 500);
                }
            };
            window.__tutaAuto.fetchEmails = function() {
                var items = document.querySelectorAll(
                    '[class*="mailItem"], [class*="conversation"], [role="listitem"]'
                );
                var result = [];
                items.forEach(function(el) {
                    var sender = (el.querySelector('[class*="sender"]') || {}).textContent || '';
                    var subject = (el.querySelector('[class*="subject"]') || {}).textContent || '';
                    if (sender || subject) {
                        result.push({
                            sender: sender.trim(),
                            subject: subject.trim(),
                            time: (el.querySelector('[class*="time"]') || {}).textContent || ''
                        });
                    }
                });
                return JSON.stringify(result);
            };
        })();
        """
    }
}
