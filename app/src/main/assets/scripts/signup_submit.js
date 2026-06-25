(function () {
  'use strict';
  var btn =
    document.querySelector('button[type="submit"]') ||
    document.querySelector('button:not([aria-label])') ||
    document.querySelector('button:last-of-type');
  if (btn) btn.click();
})();
