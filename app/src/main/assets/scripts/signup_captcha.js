(function () {
  'use strict';
  var captchaImg = document.querySelector(
    'img[alt*="clock"], img[src*="captcha"], [class*="captcha"] img'
  );
  if (captchaImg) {
    return JSON.stringify({ hasCaptcha: true, imageUrl: captchaImg.src || '' });
  }
  return JSON.stringify({ hasCaptcha: false });
})();
