(function () {
    'use strict';
    window.MedClinicExaminationGuard = function (options) {
        var edited = false, saving = false, confirming = false, confirmed = false;
        function status(message) { if (options.status.textContent !== message) options.status.textContent = message; }
        function markEdited() { edited = true; status('Đã chỉnh sửa · chưa lưu'); }
        options.form.addEventListener('submit', function (event) {
            if (confirmed) { confirmed = false; saving = true; status('Đang lưu kết quả khám…'); return; }
            event.preventDefault();
            if (confirming) return;
            confirming = true;
            options.renderReview(); options.review.open = true;
            var submitter = event.submitter;
            function finish(accepted) {
                confirming = false;
                if (!accepted) { options.submit.focus(); return; }
                // Recheck after the dialog: never carry confirmation into a later, changed form.
                if (!options.form.checkValidity()) { options.form.reportValidity(); return; }
                confirmed = true;
                try { options.form.requestSubmit(submitter || undefined); }
                catch (_) { confirmed = false; saving = false; status('Chưa gửi được dữ liệu. Kiểm tra biểu mẫu rồi thử lại.'); }
            }
            var text = options.confirmationText();
            if (!window.Swal) { finish(window.confirm(text)); return; }
            window.Swal.fire({title: 'Hoàn tất ca khám này?', text: text, icon: 'question',
                showCancelButton: true, confirmButtonColor: '#0e7490', cancelButtonColor: '#475569',
                confirmButtonText: 'Đồng ý lưu bệnh án', cancelButtonText: 'Kiểm tra lại'
            }).then(function (result) { finish(result.isConfirmed); }, function () { finish(false); });
        });
        window.addEventListener('beforeunload', function (event) {
            if (edited && !saving) { event.preventDefault(); event.returnValue = ''; }
        });
        window.addEventListener('pageshow', function (event) {
            if (!event.persisted) return;
            saving = false; confirming = false; confirmed = false;
            if (edited) status('Đã chỉnh sửa · chưa lưu');
        });
        return {markEdited: markEdited};
    };
})();
