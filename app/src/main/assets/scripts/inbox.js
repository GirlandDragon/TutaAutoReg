(function () {
  'use strict';

  var emails = [];
  var items = document.querySelectorAll(
    '[class*="mailItem"], [class*="conversation"], [role="listitem"], [class*="email-list"] > div, ' +
      '[class*="message-list"] > div, tr[class]'
  );

  items.forEach(function (el) {
    var sender =
      (el.querySelector('[class*="sender"], [class*="from"], [class*="name"]') || {})
        .textContent || '';
    var subject =
      (el.querySelector('[class*="subject"], [class*="title"], [class*="header"]') || {})
        .textContent || '';
    var time =
      (el.querySelector('[class*="time"], [class*="date"], [class*="timestamp"]') || {})
        .textContent || '';
    var snippet =
      (el.querySelector('[class*="snippet"], [class*="preview"], [class*="body"]') || {})
        .textContent || '';

    if (sender || subject) {
      emails.push({
        sender: sender.trim().substring(0, 100),
        subject: subject.trim().substring(0, 200),
        time: time.trim().substring(0, 50),
        snippet: snippet.trim().substring(0, 200),
      });
    }
  });

  return JSON.stringify(emails);
})();
