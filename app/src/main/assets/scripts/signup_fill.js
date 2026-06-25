(function () {
  'use strict';
  var prefix = ARG_PREFIX;
  var pwd = ARG_PASSWORD;

  function trigger(el, val) {
    el.value = val;
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
  }

  var emailInput =
    document.querySelector('input[type="email"], input[name*="mail"], input[autocomplete*="email"]') ||
    document.querySelector('input:not([type="hidden"]):not([type="password"])');
  var pwdInputs = document.querySelectorAll('input[type="password"]');

  if (emailInput) trigger(emailInput, prefix);
  if (pwdInputs[0]) trigger(pwdInputs[0], pwd);
  if (pwdInputs[1]) trigger(pwdInputs[1], pwd);
})();
