(function () {
    'use strict';
    var ui = window.MedClinic = window.MedClinic || {};
    ui.money = function (amount) {
        if (amount === null || amount === undefined || amount === '') return '-';
        var raw = typeof amount === 'number' && Number.isFinite(amount) ?
            amount.toLocaleString('en-US', {useGrouping: false, maximumFractionDigits: 20}) : String(amount);
        var parts = raw.match(/^(-?)(\d+)(?:\.(\d+))?$/);
        if (!parts) return '-';
        var decimals = (parts[3] || '').replace(/0+$/, '');
        return parts[1] + parts[2].replace(/\B(?=(\d{3})+(?!\d))/g, ' ') +
            (decimals ? ',' + decimals : '') + ' đ';
    };
    ui.prepareTable = function (table) {
        var headings = Array.from(table.querySelectorAll('thead th'));
        if (!headings.length) return;
        table.classList.add('mc-table-cards');
        var hint = table.closest('.mc-data-card')?.querySelector('.mc-mobile-scroll-hint');
        if (hint) hint.hidden = true;
        table.setAttribute('role', 'table');
        table.querySelectorAll('thead, tbody, tfoot').forEach(function (group) { group.setAttribute('role', 'rowgroup'); });
        headings.forEach(function (heading) { heading.setAttribute('scope', 'col'); heading.setAttribute('role', 'columnheader'); });
        table.querySelectorAll('tr').forEach(function (row) { row.setAttribute('role', 'row'); });
        table.querySelectorAll('tbody tr').forEach(function (row) {
            Array.from(row.children).forEach(function (cell, index) {
                if (cell.tagName !== 'TD') return;
                cell.setAttribute('role', 'cell');
                if (cell.colSpan > 1) { cell.classList.add('mc-table-full'); return; }
                var label = headings[index] ? headings[index].textContent.trim().replace(/\s+/g, ' ') : '';
                cell.dataset.label = label;
                if (/Bệnh nhân|Tên|Chuyên khoa|Tài khoản/i.test(label)) cell.classList.add('mc-table-primary');
                if (/Thao tác|Xóa/i.test(label)) cell.classList.add('mc-table-actions');
                if (/Giới thiệu|Mô tả|Triệu chứng|Ghi chú|Tần suất/i.test(label)) cell.classList.add('mc-table-full');
                if (/Giá|Tiền|Phí|Thành tiền/i.test(label)) cell.classList.add('mc-money');
            });
        });
    };
    function ready() {
        document.querySelectorAll('.nav-link-med[href], .sidebar-link[href]').forEach(function (link) {
            var path = new URL(link.href, window.location.origin).pathname;
            var current = window.location.pathname;
            if (current === path || (path !== '/' && current.startsWith(path + '/'))) {
                link.classList.add('active');
                link.setAttribute('aria-current', 'page');
            }
        });
        if (window.navigator && 'serviceWorker' in window.navigator) {
            window.addEventListener('load', function () {
                window.navigator.serviceWorker.register('/sw.js').catch(function () {});
            }, {once: true});
        }
        document.querySelectorAll('.mc-data-table').forEach(ui.prepareTable);
        document.querySelectorAll('.table-responsive').forEach(function (region) {
            var title = region.closest('.mc-data-card, .mc-workflow-card, .card')?.querySelector('h2, h3');
            region.setAttribute('role', 'region');
            region.setAttribute('aria-label', title ? title.textContent.trim() : 'Bảng dữ liệu');
            function updateScrollAccess() { region.tabIndex = region.scrollWidth > region.clientWidth + 1 ? 0 : -1; }
            updateScrollAccess();
            window.addEventListener('resize', updateScrollAccess, {passive: true});
        });
        var themeButton = document.querySelector('#themeToggleBtn');
        function reflectTheme() {
            if (!themeButton) return;
            var dark = document.documentElement.getAttribute('data-bs-theme') === 'dark';
            themeButton.setAttribute('aria-pressed', String(dark));
            themeButton.setAttribute('aria-label', dark ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối');
            themeButton.querySelector('.theme-icon-dark')?.classList.toggle('d-none', dark);
            themeButton.querySelector('.theme-icon-light')?.classList.toggle('d-none', !dark);
        }
        if (themeButton) themeButton.addEventListener('click', function () {
            var next = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-bs-theme', next);
            try { localStorage.setItem('medclinic-theme', next); } catch (_) {}
            reflectTheme();
            document.dispatchEvent(new CustomEvent('medclinic:theme-change'));
        });
        reflectTheme();
        document.querySelectorAll('button[title], a[title]').forEach(function (control) {
            if (!control.textContent.trim() && !control.getAttribute('aria-label')) control.setAttribute('aria-label', control.title);
        });
        document.querySelectorAll('.bi:not([role])').forEach(function (icon) { icon.setAttribute('aria-hidden', 'true'); });
        // Bubble after form-level validation/confirmation. A microtask inside a form
        // listener can run before the next listener in a real browser.
        document.addEventListener('submit', function (event) {
            var form = event.target;
            if (event.defaultPrevented || !form || form.tagName !== 'FORM') return;
            if (form.dataset.submitting === 'true') { event.preventDefault(); return; }
            form.dataset.submitting = 'true';
            var button = event.submitter || form.querySelector('[type="submit"]');
            // Preserve successful submitter name/value while the browser builds form data.
            setTimeout(function () {
                if (event.defaultPrevented) { delete form.dataset.submitting; return; }
                if (form.dataset.submitting === 'true' && button) {
                    button.disabled = true; button.setAttribute('aria-busy', 'true'); button.dataset.submitLock = 'true';
                    button.dataset.originalLabel = button.innerHTML;
                    button.textContent = 'Đang xử lý…';
                }
            }, 0);
        });
        document.querySelectorAll('.is-invalid').forEach(function (field, index) {
            var error = field.parentElement.querySelector('.invalid-feedback, [data-field-error]');
            field.setAttribute('aria-invalid', 'true');
            if (error) {
                if (!error.id) error.id = 'field-error-' + index;
                field.setAttribute('aria-describedby', [field.getAttribute('aria-describedby'), error.id].filter(Boolean).join(' '));
            }
        });
        var firstError = document.querySelector('input.is-invalid, select.is-invalid, textarea.is-invalid');
        // Server validation returns the whole page, including the closed modal.
        // Wait for Bootstrap's focus trap before moving focus to the invalid field.
        var invalidModal = firstError?.closest('.modal') || document.querySelector('.modal [data-form-error]')?.closest('.modal');
        if (invalidModal && window.bootstrap?.Modal) {
            invalidModal.addEventListener('shown.bs.modal', function () {
                var target = invalidModal.querySelector('.is-invalid, input:not([type="hidden"]), select, textarea');
                if (target) target.focus();
            }, {once: true});
            window.bootstrap.Modal.getOrCreateInstance(invalidModal).show();
        } else if (firstError) firstError.focus();
        else document.querySelector('[data-error-summary]')?.focus();
        // Keep Tab inside a modal even when the next native tab stop is browser chrome.
        document.addEventListener('keydown', function (event) {
            if (event.key !== 'Tab') return;
            var modal = event.target.closest('.modal.show');
            if (!modal) return;
            var controls = Array.from(modal.querySelectorAll('a[href], button, input:not([type="hidden"]), select, textarea, [tabindex]'))
                .filter(function (control) { return !control.disabled && control.tabIndex >= 0 && control.getClientRects().length > 0; });
            if (!controls.length) { event.preventDefault(); modal.focus(); return; }
            var first = controls[0], last = controls[controls.length - 1];
            if (event.shiftKey && (event.target === first || event.target === modal)) { event.preventDefault(); last.focus(); }
            else if (!event.shiftKey && (event.target === last || event.target === modal)) { event.preventDefault(); first.focus(); }
        });
        document.querySelectorAll('img[data-doctor-avatar], img[data-avatar]').forEach(function (image) {
            function showFallback() {
                image.hidden = true;
                var fallback = image.parentElement.querySelector('[data-avatar-fallback]');
                if (fallback) fallback.hidden = false;
            }
            image.addEventListener('error', showFallback);
            if (image.complete && image.naturalWidth === 0) showFallback();
        });
    }
    window.addEventListener('pageshow', function () {
        document.querySelectorAll('form[data-submitting]').forEach(function (form) { delete form.dataset.submitting; });
        document.querySelectorAll('[data-submit-lock]').forEach(function (button) {
            button.disabled = false; button.removeAttribute('aria-busy'); delete button.dataset.submitLock;
            if (button.dataset.originalLabel) { button.innerHTML = button.dataset.originalLabel; delete button.dataset.originalLabel; }
        });
    });
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', ready); else ready();
})();
