(function () {
    'use strict';
    // Status checks only. Confirmation remains a separate, deliberate form action.
    window.MedClinicBankStatus = function (options) {
        var state = options.initialStatus, timer, controller, stopped = false, inFlight = false, sessionEnded = false;
        var terminal = ['CONFIRMED', 'EXPIRED', 'REJECTED'];
        function render(status) {
            state = status;
            var done = terminal.includes(status);
            options.status.className = 'alert mt-3 mb-3 ' + (status === 'CONFIRMED' ? 'alert-success' : 'alert-warning');
            var message = {
                PENDING: 'Đang chờ ngân hàng xác nhận. Chưa ghi nhận thanh toán.',
                CONFIRMED: 'Đã xác nhận thanh toán. Bạn có thể về danh sách hóa đơn.',
                EXPIRED: 'Mã chuyển khoản đã hết hạn. Không dùng QR cũ. Nếu đã chuyển tiền, đối soát giao dịch tại quầy trước khi tạo mã mới; tránh chuyển tiền thêm.',
                REJECTED: 'Giao dịch chưa được chấp nhận. Kiểm tra với người phụ trách đối soát trước khi chuyển tiền lại.'
            }[status];
            if (options.status.textContent !== message) options.status.textContent = message;
            if (options.manual) options.manual.hidden = status === 'CONFIRMED' || status === 'REJECTED';
            if (options.qr) options.qr.hidden = done;
            options.retry.hidden = done;
            return done;
        }
        function schedule() {
            if (!stopped && !terminal.includes(state)) timer = window.setTimeout(check, 3000);
        }
        async function check() {
            if (stopped || inFlight || terminal.includes(state)) return;
            window.clearTimeout(timer);
            inFlight = true;
            options.retry.disabled = true;
            options.retry.setAttribute('aria-busy', 'true');
            controller = new AbortController();
            var timeout = window.setTimeout(function () { controller.abort(); }, 10000);
            try {
                var response = await window.fetch(options.url, {headers: {'Accept': 'application/json'}, signal: controller.signal});
                if (stopped) return;
                if (response.status === 401 || response.status === 403 || response.redirected) {
                    stopped = true;
                    sessionEnded = true;
                    options.status.textContent = 'Phiên làm việc đã hết hạn hoặc không còn quyền truy cập. Đăng nhập lại để kiểm tra; chưa có xác nhận thanh toán.';
                    options.retry.hidden = true;
                    if (options.manual) options.manual.hidden = true;
                    if (options.qr) options.qr.hidden = true;
                    if (options.login) options.login.hidden = false;
                    return;
                }
                if (!response.ok) throw new Error('Status unavailable');
                var data = await response.json();
                if (stopped) return;
                if (!['PENDING', ...terminal].includes(data.status) ||
                    (data.status === 'CONFIRMED' && data.confirmed !== true)) throw new Error('Invalid status');
                render(data.status);
            } catch (_) {
                if (!stopped) {
                    options.status.className = 'alert alert-warning mt-3 mb-3';
                    var message = 'Chưa kiểm tra được trạng thái giao dịch. Chưa có xác nhận thanh toán. Kiểm tra kết nối rồi thử lại; tránh chuyển tiền thêm.';
                    if (options.status.textContent !== message) options.status.textContent = message;
                    options.retry.hidden = false;
                }
            } finally {
                window.clearTimeout(timeout);
                inFlight = false;
                options.retry.disabled = false;
                options.retry.removeAttribute('aria-busy');
                schedule();
            }
        }
        options.retry.addEventListener('click', check);
        render(state);
        schedule();
        return {
            check: check,
            stop: function () { stopped = true; window.clearTimeout(timer); if (controller) controller.abort(); },
            resume: function () { if (!sessionEnded && !terminal.includes(state)) { stopped = false; check(); } }
        };
    };
    function ready() {
        var root = document.querySelector('[data-bank-payment]');
        if (!root) return;
        var monitor = window.MedClinicBankStatus({
            initialStatus: root.dataset.initialStatus, url: root.dataset.statusUrl,
            status: document.getElementById('paymentStatus'), retry: document.getElementById('paymentRetry'),
            manual: document.getElementById('paymentManual'), qr: document.getElementById('paymentQr'),
            login: document.getElementById('paymentLogin')
        });
        document.addEventListener('visibilitychange', function () { document.hidden ? monitor.stop() : monitor.resume(); });
        window.addEventListener('pagehide', monitor.stop);
        window.addEventListener('pageshow', function (event) { if (event.persisted) monitor.resume(); });
        var image = document.getElementById('paymentQrImage');
        if (image) {
            function fallback() { image.hidden = true; document.getElementById('paymentQrError').hidden = false; }
            image.addEventListener('error', fallback);
            if (image.complete && !image.naturalWidth) fallback();
        }
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', ready); else ready();
})();
