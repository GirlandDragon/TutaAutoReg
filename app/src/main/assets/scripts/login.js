(function () {
  'use strict';

  var email = ARG_EMAIL;
  var pwd = ARG_PASSWORD;

  function trigger(el, val) {
    el.value = val;
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
  }

  var emailInput = document.querySelector(
    'input[name="mailAddress"], input[autocomplete="username"], input[type="email"]'
  );
  var pwdInput = document.querySelector('input[type="password"]');
  var btn =
    document.querySelector('button[type="submit"]') ||
    document.querySelector('button:contains("Log in")') ||
    document.querySelector('button:contains("Sign in")');

  if (emailInput) trigger(emailInput, email);
  if (pwdInput) trigger(pwdInput, pwd);
  if (btn) {
    setTimeout(function () { btn.click(); }, 800);
  }

  return JSON.stringify({ filled: !!(emailInput && pwdInput) });
})();
