(function () {
  'use strict';

  window.__tutaAuto = window.__tutaAuto || {};

  function waitForEl(selector, maxMs) {
    var deadline = Date.now() + (maxMs || 60000);
    return new Promise(function (resolve, reject) {
      function check() {
        var el = document.querySelector(selector);
        if (el) return resolve(el);
        if (Date.now() > deadline)
          return reject(new Error('Timeout waiting for ' + selector));
        setTimeout(check, 300);
      }
      check();
    });
  }

  function waitForMs(ms) {
    return new Promise(function (r) { setTimeout(r, ms); });
  }

  function triggerInput(el, val) {
    el.value = val;
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
  }

  function report(obj) {
    if (window.TutaAutoBridge) {
      window.TutaAutoBridge.onStepComplete(JSON.stringify(obj));
    }
  }

  function watchCaptcha() {
    // Observe DOM for clock captcha overlay
    var observer = new MutationObserver(function () {
      var captchaImg = document.querySelector(
        'img[alt*="clock"], img[src*="captcha"], [class*="captcha"] img'
      );
      if (captchaImg) {
        observer.disconnect();
        report({ type: 'captcha', imageUrl: captchaImg.src || '' });
      }
    });
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: false,
    });
  }

  window.__tutaAuto.startSignup = async function (prefix, password) {
    try {
      watchCaptcha();

      // Wait for the signup form to render
      await waitForMs(3000);
      var emailInput =
        document.querySelector('input[type="email"], input[name*="mail"], input[autocomplete*="email"]') ||
        document.querySelector('input:not([type="hidden"])');
      var pwdInput =
        document.querySelector('input[type="password"]');
      var confirmInput =
        document.querySelectorAll('input[type="password"]')[1] || pwdInput;
      var submitBtn =
        document.querySelector('button[type="submit"], button:not([aria-label])') ||
        document.querySelector('button:last-of-type');

      if (emailInput && pwdInput) {
        triggerInput(emailInput, prefix);
        await waitForMs(500);
        triggerInput(pwdInput, password);
        await waitForMs(500);
        if (confirmInput && confirmInput !== pwdInput) {
          triggerInput(confirmInput, password);
          await waitForMs(500);
        }
      }

      // Wait for PoW to complete (Tuta's JS handles it)
      await waitForMs(5000);

      // Check if captcha overlay appeared
      await waitForMs(2000);

      if (submitBtn) {
        submitBtn.click();
      }

      // Wait and check result
      await waitForMs(8000);

      // Try to detect success
      var successEl = document.querySelector(
        '[class*="success"], [class*="thank"], [data-testid*="success"]'
      );
      if (successEl) {
        report({
          type: 'success',
          email: prefix + '@tuta.com',
          recoveryCode: '',
        });
      } else {
        report({
          type: 'error',
          message: 'Signup may have failed - check WebView',
        });
      }
    } catch (e) {
      report({ type: 'error', message: e.message });
    }
  };

  window.__tutaAuto.solveCaptcha = function (answer) {
    var input = document.querySelector(
      'input[placeholder*="time"], input[placeholder*="clock"], input[placeholder*="HH:MM"], ' +
        'input[placeholder*="hh:mm"], [class*="captcha"] input'
    );
    if (input) {
      triggerInput(input, answer);
      var btn =
        document.querySelector(
          '[class*="captcha"] button, button:contains("Submit")'
        ) ||
        document.querySelector('button:not([aria-label*="close"])');
      if (btn) btn.click();
    }
  };

  window.__tutaAuto.login = function (email, password) {
    var emailInput = document.querySelector(
      'input[name="mailAddress"], input[autocomplete="username"], input[type="email"]'
    );
    var pwdInput = document.querySelector(
      'input[type="password"]'
    );
    var btn =
      document.querySelector(
        'button[type="submit"], button:contains("Log in")'
      );

    if (emailInput && pwdInput) {
      triggerInput(emailInput, email);
      triggerInput(pwdInput, password);
      if (btn) setTimeout(function () { btn.click(); }, 1000);
    }
  };

  window.__tutaAuto.fetchEmails = function () {
    var emails = [];
    var items = document.querySelectorAll(
      '[class*="mail"], [class*="item"], [role="listitem"], tr'
    );
    items.forEach(function (el) {
      var sender = (el.querySelector('[class*="sender"], [class*="from"]') || {}).textContent || '';
      var subject = (el.querySelector('[class*="subject"], [class*="title"]') || {}).textContent || '';
      var time = (el.querySelector('[class*="time"], [class*="date"]') || {}).textContent || '';
      if (sender || subject) {
        emails.push({
          sender: sender.trim(),
          subject: subject.trim(),
          time: time.trim(),
          snippet: '',
        });
      }
    });
    if (window.TutaInboxBridge) {
      window.TutaInboxBridge.onEmailsFetched(JSON.stringify(emails));
    }
  };
})();
