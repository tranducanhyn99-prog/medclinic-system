(function () {
    'use strict';
    window.MedClinicSlots = function (options) {
        var grid = options.grid, selected = options.selected, sequence = 0, controller;
        function state(name) {
            ['prompt', 'loading', 'empty', 'error'].forEach(function (key) { options[key].classList.toggle('d-none', key !== name); });
            grid.classList.toggle('d-none', name !== 'ready');
            grid.setAttribute('aria-busy', String(name === 'loading'));
        }
        function reset() {
            grid.replaceChildren(); selected.value = '';
            if (options.submit) options.submit.disabled = true;
            if (options.onReset) options.onReset();
        }
        function clear() {
            sequence++; if (controller) controller.abort(); reset(); state('prompt');
        }
        async function load(doctor, date) {
            var request = ++sequence;
            if (controller) controller.abort();
            reset();
            if (!doctor || !date) { state('prompt'); return; }
            controller = new AbortController();
            var currentController = controller;
            var timer = setTimeout(function () { currentController.abort(); }, 15000);
            state('loading');
            try {
                var response = await fetch('/api/public/doctors/' + encodeURIComponent(doctor) + '/slots?date=' + encodeURIComponent(date),
                    {signal: currentController.signal, headers: {'Accept': 'application/json'}});
                if (!response.ok) throw new Error('HTTP ' + response.status);
                var slots = await response.json();
                if (request !== sequence) return;
                if (!Array.isArray(slots) || slots.some(function (slot) {
                    return !slot || !slot.id || typeof slot.startTime !== 'string' || typeof slot.endTime !== 'string';
                })) throw new Error('Invalid slot response');
                if (!slots.length) { state('empty'); return; }
                state('ready');
                slots.forEach(function (slot) {
                    var col = document.createElement('div'), button = document.createElement('button');
                    col.className = 'col'; button.type = 'button'; button.className = 'slot-btn w-100';
                    var time = slot.startTime.substring(0, 5) + ' - ' + slot.endTime.substring(0, 5);
                    button.textContent = time; button.dataset.slotId = slot.id;
                    button.setAttribute('aria-pressed', 'false'); button.disabled = slot.status !== 'AVAILABLE';
                    if (button.disabled) button.title = 'Khung giờ này không còn khả dụng';
                    button.addEventListener('click', function () {
                        grid.querySelectorAll('.slot-btn').forEach(function (other) { other.classList.remove('selected'); other.setAttribute('aria-pressed', 'false'); });
                        button.classList.add('selected'); button.setAttribute('aria-pressed', 'true'); selected.value = slot.id;
                        if (options.submit) options.submit.disabled = false;
                        if (options.onSelect) options.onSelect(time);
                    });
                    col.appendChild(button); grid.appendChild(col);
                    if (!button.disabled && options.restore && options.restore(slot.id)) button.click();
                });
                if (options.onLoaded) options.onLoaded();
            } catch (error) {
                if (request === sequence) state('error');
            } finally { clearTimeout(timer); }
        }
        return {load: load, clear: clear};
    };
})();
