package com.tuta.auto.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray

data class EmailEntry(
    val sender: String,
    val subject: String,
    val time: String
)

class InboxAutomator(private val webView: WebView) {

    private val _emails = MutableLiveData<List<EmailEntry>>()
    val emails: LiveData<List<EmailEntry>> = _emails

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val inboxJs = object {
        @JavascriptInterface
        fun onEmails(json: String) {
            val arr = JSONArray(json)
            val list = mutableListOf<EmailEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    EmailEntry(
                        sender = obj.optString("sender", ""),
                        subject = obj.optString("subject", ""),
                        time = obj.optString("time", "")
                    )
                )
            }
            _emails.postValue(list)
        }

        @JavascriptInterface
        fun onLogin(success: Boolean) {
            _loginResult.postValue(success)
        }
    }

    fun loginAndFetch(email: String, password: String) {
        webView.addJavascriptInterface(inboxJs, "TutaInboxBridge")

        webView.evaluateJavascript(
            """
            (function() {
                function trigger(el, val) {
                    el.value = val;
                    el.dispatchEvent(new Event('input', {bubbles: true}));
                    el.dispatchEvent(new Event('change', {bubbles: true}));
                }
                
                var emailInput = document.querySelector(
                    'input[name="mailAddress"], input[autocomplete="username"], input[type="email"]'
                );
                var pwdInput = document.querySelector('input[type="password"]');
                var btn = document.querySelector('button[type="submit"]');
                
                if (emailInput && pwdInput) {
                    trigger(emailInput, '$email');
                    trigger(pwdInput, '$password');
                    if (btn) {
                        setTimeout(function() { btn.click(); }, 800);
                    }
                    TutaInboxBridge.onLogin(true);
                } else {
                    TutaInboxBridge.onLogin(false);
                }
            })();
            """.trimIndent(),
            null
        )
    }

    fun fetchEmails() {
        webView.evaluateJavascript(
            """
            (function() {
                var items = document.querySelectorAll(
                    '[class*="mailItem"], [class*="conversation"], [role="listitem"], ' +
                    '[class*="email-list"] > div, [class*="message-list"] > div'
                );
                var result = [];
                items.forEach(function(el) {
                    var sender = (el.querySelector('[class*="sender"], [class*="from"]') || {}).textContent || '';
                    var subject = (el.querySelector('[class*="subject"], [class*="title"]') || {}).textContent || '';
                    var time = (el.querySelector('[class*="time"], [class*="date"]') || {}).textContent || '';
                    if (sender || subject) {
                        result.push({
                            sender: sender.trim().substring(0, 100),
                            subject: subject.trim().substring(0, 200),
                            time: time.trim().substring(0, 50)
                        });
                    }
                });
                TutaInboxBridge.onEmails(JSON.stringify(result));
            })();
            """.trimIndent(),
            null
        )
    }
}
