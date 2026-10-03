(function () {
    'use strict';
    window.MedClinicOtpCountdown = function (options) {
        var seconds = Number(options.timer.dataset.seconds);
        var deadline = Date.now() + (Number.isFinite(seconds) ? Math.max(0, seconds) : 0) * 1000;
        var interval;
        function render() {
            var remaining = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
            options.timer.textContent = String(Math.floor(remaining / 60)).padStart(2, '0') + ':' + String(remaining % 60).padStart(2, '0');
            if (!remaining) {
                options.timer.classList.add('text-danger');
                options.expired.hidden = false;
                options.submit.disabled = true;
                window.clearInterval(interval);
            }
            return remaining;
        }
        function start() { window.clearInterval(interval); if (render()) interval = window.setInterval(render, 1000); }
        start();
        window.addEventListener('pagehide', function () { window.clearInterval(interval); });
        window.addEventListener('pageshow', start);
        document.addEventListener('visibilitychange', function () { if (!document.hidden) start(); });
        return {render: render};
    };
    function ready() {
        var timer = document.getElementById('otpCountdown');
        if (timer) window.MedClinicOtpCountdown({timer: timer, expired: document.getElementById('otpExpired'), submit: document.getElementById('otpSubmit')});
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', ready); else ready();
})();
