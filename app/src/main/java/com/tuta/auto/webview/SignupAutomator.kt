package com.tuta.auto.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

sealed class SignupEvent {
    data class Success(val email: String) : SignupEvent()
    data object CaptchaRequired : SignupEvent()
    data class Error(val message: String) : SignupEvent()
}

class SignupAutomator(private val webView: WebView) {

    private val _events = MutableLiveData<SignupEvent>()
    val events: LiveData<SignupEvent> = _events

    private val bridge = object {
        @JavascriptInterface
        fun onCaptchaRequired() {
            _events.postValue(SignupEvent.CaptchaRequired)
        }

        @JavascriptInterface
        fun onSignupSuccess(email: String) {
            _events.postValue(SignupEvent.Success(email))
        }

        @JavascriptInterface
        fun onSignupError(message: String) {
            _events.postValue(SignupEvent.Error(message))
        }
    }

    init {
        webView.addJavascriptInterface(bridge, "TutaBridge")
    }

    fun startSignup(emailPrefix: String, password: String) {
        val escapedPrefix = emailPrefix.replace("'", "\\'").replace("\\", "\\\\")
        val escapedPassword = password.replace("'", "\\'").replace("\\", "\\\\")

        webView.evaluateJavascript(INIT_SCRIPT, null)
        webView.postDelayed({
            webView.evaluateJavascript(
                buildSignupJs(escapedPrefix, escapedPassword),
                null
            )
        }, 3000)
    }

    fun submitCaptchaAnswer(answer: String) {
        val escaped = answer.replace("'", "\\'")
        webView.evaluateJavascript(
            "window.__tutaAuto.solveCaptcha('$escaped');",
            null
        )
    }

    fun onPageLoaded(url: String) {
        if (url.contains("/mail") || url.contains("success") || url.contains("thank")) {
            _events.postValue(SignupEvent.Success("registration-complete"))
        }
    }

    private fun buildSignupJs(prefix: String, pwd: String): String {
        return """
(function() {
    function trigger(el, val) {
        el.value = val;
        el.dispatchEvent(new Event('input', {bubbles: true}));
        el.dispatchEvent(new Event('change', {bubbles: true}));
    }
    
    function fillForm() {
        var inputs = document.querySelectorAll('input');
        var emailInput = null;
        var pwdInputs = [];
        
        for (var i = 0; i < inputs.length; i++) {
            var type = (inputs[i].getAttribute('type') || '').toLowerCase();
            if (type === 'email' || type === 'text' || !type || type === '') {
                if (!emailInput) emailInput = inputs[i];
            }
            if (type === 'password') pwdInputs.push(inputs[i]);
        }
        
        if (emailInput) {
            trigger(emailInput, '$prefix');
            console.log('[TutaAuto] Filled email: $prefix');
        }
        if (pwdInputs[0]) {
            trigger(pwdInputs[0], '$pwd');
            if (pwdInputs[1]) trigger(pwdInputs[1], '$pwd');
            console.log('[TutaAuto] Filled password');
        }
    }
    
    function checkCaptcha() {
        var captchaEl = document.querySelector(
            'img[alt*="clock"], img[alt*="time"], ' +
            '[class*="captcha"], [id*="captcha"], ' +
            '.clock-captcha, [data-test*="captcha"]'
        );
        return captchaEl !== null;
    }
    
    function monitorForCaptcha() {
        var observer = new MutationObserver(function() {
            if (checkCaptcha()) {
                observer.disconnect();
                console.log('[TutaAuto] CAPTCHA detected');
                try { TutaBridge.onCaptchaRequired(); } catch(e) {}
            }
        });
        observer.observe(document.body, { childList: true, subtree: true });
    }
    
    function submitForm() {
        var btn = document.querySelector(
            'button[type="submit"], ' +
            'button:not([aria-label]):not([class*="icon"]):not([class*="close"])'
        ) || document.querySelector('button:last-of-type');
        if (btn) {
            console.log('[TutaAuto] Submitting form');
            btn.click();
        }
    }
    
    fillForm();
    monitorForCaptcha();
    
    // Wait for PoW then submit
    setTimeout(function() {
        if (!checkCaptcha()) {
            submitForm();
        }
    }, 8000);
})();
        """.trimIndent()
    }

    companion object {
        private const val INIT_SCRIPT = """
(function() {
    window.__tutaAuto = window.__tutaAuto || {};
    
    window.__tutaAuto.solveCaptcha = function(answer) {
        var input = document.querySelector(
            'input[placeholder*="time"], input[placeholder*="clock"], ' +
            'input[placeholder*="HH:MM"], input[placeholder*="hh:mm"], ' +
            'input[placeholder*="Time"], ' +
            '[class*="captcha"] input, [id*="captcha"] input'
        );
        if (input) {
            input.value = answer;
            input.dispatchEvent(new Event('input', {bubbles: true}));
            input.dispatchEvent(new Event('change', {bubbles: true}));
            
            var btn = document.querySelector(
                'button[type="submit"], ' +
                '[class*="captcha"] button, ' +
                'button:contains("Submit"), button:contains("OK")'
            );
            if (btn) {
                setTimeout(function() { btn.click(); }, 500);
            } else {
                // Try pressing enter
                input.dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter'}));
            }
        }
    };
    
    window.__tutaAuto.fetchEmails = function() {
        var items = document.querySelectorAll(
            '[class*="mailItem"], [class*="conversation"], ' +
            '[role="listitem"], [class*="email-row"]'
        );
        var result = [];
        items.forEach(function(el) {
            var sender = (el.querySelector('[class*="sender"]') || {}).textContent || '';
            var subject = (el.querySelector('[class*="subject"]') || {}).textContent || '';
            if (sender || subject) {
                result.push({
                    sender: sender.trim().substring(0, 100),
                    subject: subject.trim().substring(0, 200),
                    time: (el.querySelector('[class*="time"]') || {}).textContent || ''
                });
            }
        });
        return JSON.stringify(result);
    };
    
    console.log('[TutaAuto] Init script loaded');
})();
        """.trimIndent()
    }
}
