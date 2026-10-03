(function () {
    'use strict';
    document.addEventListener('DOMContentLoaded', function () {
        var search = document.getElementById('queuePatientSearch');
        var view = document.getElementById('queueViewFilter');
        if (!search || !view) return;
        var rows = Array.from(document.querySelectorAll('[data-queue-state]'));
        var empty = document.getElementById('queueFilteredEmpty');
        var count = document.getElementById('queueResultCount');
        document.getElementById('queueFilters').hidden = false;
        try { view.value = sessionStorage.getItem('medclinic-queue-view') || 'active'; } catch (_) {}
        if (!view.value) view.value = 'active';
        function filter() {
            var keyword = search.value.trim().toLocaleLowerCase('vi-VN');
            var shown = 0;
            rows.forEach(function (row) {
                var state = row.dataset.queueState;
                var matchesView = view.value === 'all' || (view.value === 'completed' ? state === 'COMPLETED' : state === 'CHECKED_IN' || state === 'IN_PROGRESS');
                row.hidden = !matchesView || (keyword.length > 0 && !row.textContent.toLocaleLowerCase('vi-VN').includes(keyword));
                if (!row.hidden) shown++;
            });
            empty.hidden = shown > 0;
            count.textContent = shown + ' bệnh nhân trong danh sách';
        }
        search.addEventListener('input', filter);
        view.addEventListener('change', function () {
            try { sessionStorage.setItem('medclinic-queue-view', view.value); } catch (_) {}
            filter();
        });
        filter();
    });
})();
