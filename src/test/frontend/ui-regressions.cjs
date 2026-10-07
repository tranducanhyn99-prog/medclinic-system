const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const {test} = require('node:test');
const source = name => fs.readFileSync('src/main/resources/static/' + name, 'utf8');
class Element {
    constructor() {
        this.dataset = {}; this.children = []; this.attrs = {}; this.listeners = {}; this.value = ''; this.disabled = false;
        const classes = new Set();
        this.classList = {add: v => classes.add(v), remove: v => classes.delete(v), contains: v => classes.has(v),
            toggle: (v, force) => { if (force === undefined) force = !classes.has(v); force ? classes.add(v) : classes.delete(v); }};
    }
    setAttribute(key, value) { this.attrs[key] = String(value); }
    getAttribute(key) { return this.attrs[key] ?? null; }
    removeAttribute(key) { delete this.attrs[key]; }
    addEventListener(key, fn) { (this.listeners[key] ??= []).push(fn); }
    fire(key, event = {}) { for (const fn of this.listeners[key] ?? []) fn(event); }
    click() { if (!this.disabled) this.fire('click'); }
    appendChild(child) { this.children.push(child); }
    replaceChildren() { this.children = []; }
    querySelectorAll() { return this.children.flatMap(col => col.children); }
}
function submissionFixture() {
    const form = new Element(), button = new Element(), businessDisabled = new Element(), window = new Element();
    businessDisabled.disabled = true; form.querySelector = () => button;
    form.tagName = 'FORM';
    const document = Object.assign(new Element(), {readyState: 'complete', querySelector: () => null,
        querySelectorAll: selector => selector === 'form' ? [form] : selector === 'form[data-submitting]' ? [form] :
            selector === '[data-submit-lock]' ? [button, businessDisabled].filter(b => b.dataset.submitLock) : []});
    vm.runInNewContext(source('js/medclinic-ui.js'), {document, window, setTimeout});
    const submit = () => { const event = {target: form, submitter: button, defaultPrevented: false, preventDefault() {this.defaultPrevented = true;}}; form.fire('submit', event); document.fire('submit', event); return event; };
    return {form, button, businessDisabled, window, submit};
}

test('shared layout marks exact and child navigation paths and registers the service worker', () => {
    for (const current of ['/patient/appointments', '/patient/appointments/42']) {
        const window = Object.assign(new Element(), {location: {origin: 'https://clinic.example', pathname: current}});
        const links = ['/', '/patient/appointments', '/patient/appointment', '/doctor/queue'].map(path =>
            Object.assign(new Element(), {href: 'https://clinic.example' + path}));
        const registrations = [];
        window.navigator = {serviceWorker: {register: path => { registrations.push(path); return Promise.resolve(); }}};
        const document = Object.assign(new Element(), {readyState: 'complete', querySelector: () => null,
            querySelectorAll: selector => selector === '.nav-link-med[href], .sidebar-link[href]' ? links : []});
        vm.runInNewContext(source('js/medclinic-ui.js'), {document, window, URL, setTimeout});
        assert.deepEqual(links.map(link => link.classList.contains('active')), [false, true, false, false]);
        assert.equal(links[1].getAttribute('aria-current'), 'page');
        assert.equal(registrations.length, 0);
        window.fire('load');
        assert.deepEqual(registrations, ['/sw.js']);
    }
});

test('VND formatting preserves decimal strings and large values across UI surfaces', () => {
    const f = submissionFixture();
    const money = f.window.MedClinic.money;
    for (const [value, expected] of [[null, '-'], ['', '-'], ['invalid', '-'], [0, '0 đ'],
        ['353000.00', '353 000 đ'], ['1500.50', '1 500,5 đ'],
        ['9007199254740993.00', '9 007 199 254 740 993 đ'], [-1500, '-1 500 đ']]) {
        assert.equal(money(value), expected);
    }
});

test('mobile table enhancement retains original actions, labels and accessible table structure', () => {
    const f = submissionFixture();
    const table = new Element(), head = new Element(), body = new Element(), row = new Element();
    const headings = ['Bệnh nhân', 'Trạng thái', 'Thao tác'].map(textContent => Object.assign(new Element(), {textContent}));
    const cells = headings.map(() => Object.assign(new Element(), {tagName: 'TD', colSpan: 1}));
    const action = new Element(); cells[2].appendChild(action); row.children = cells;
    table.closest = () => null;
    table.querySelectorAll = selector => ({'thead th': headings, 'thead, tbody, tfoot': [head, body], 'tr': [row], 'tbody tr': [row]})[selector];
    f.window.MedClinic.prepareTable(table);
    assert.equal(table.getAttribute('role'), 'table');
    assert.equal(headings[0].getAttribute('scope'), 'col');
    assert.equal(cells[0].dataset.label, 'Bệnh nhân');
    assert.equal(cells[2].classList.contains('mc-table-actions'), true);
    assert.equal(cells[2].children[0], action);
    const empty = Object.assign(new Element(), {tagName: 'TD', colSpan: 3}); row.children = [empty];
    f.window.MedClinic.prepareTable(table);
    assert.equal(empty.classList.contains('mc-table-full'), true);
});

test('doctor queue combines status and patient search without persisting patient text', () => {
    const names = ['queuePatientSearch', 'queueViewFilter', 'queueFilteredEmpty', 'queueResultCount', 'queueFilters'];
    const nodes = Object.fromEntries(names.map(id => [id, new Element()]));
    const rows = ['CHECKED_IN', 'IN_PROGRESS', 'COMPLETED'].map((state, i) =>
        Object.assign(new Element(), {dataset: {queueState: state}, textContent: ['An', 'Bình', 'Chi'][i]}));
    const document = Object.assign(new Element(), {getElementById: id => nodes[id], querySelectorAll: () => rows});
    const saved = [];
    vm.runInNewContext(source('js/doctor-queue.js'), {document, sessionStorage: {getItem: () => null, setItem: (...args) => saved.push(args)}});
    document.fire('DOMContentLoaded');
    assert.deepEqual(rows.map(row => row.hidden), [false, false, true]);
    nodes.queuePatientSearch.value = 'bÌNH'; nodes.queuePatientSearch.fire('input');
    assert.deepEqual(rows.map(row => row.hidden), [true, false, true]);
    assert.equal(saved.length, 0);
    nodes.queueViewFilter.value = 'completed'; nodes.queueViewFilter.fire('change');
    assert.equal(nodes.queueFilteredEmpty.hidden, false);
    nodes.queuePatientSearch.value = ''; nodes.queuePatientSearch.fire('input');
    assert.deepEqual(rows.map(row => row.hidden), [true, true, false]);
    assert.deepEqual(saved, [['medclinic-queue-view', 'completed']]);
});
test('form-level confirmation cancellation reaches document guard without locking', async () => {
    const f = submissionFixture();
    f.form.addEventListener('submit', event => event.preventDefault());
    f.submit();
    await new Promise(resolve => setTimeout(resolve, 10));
    assert.equal(f.button.disabled, false); assert.equal(f.form.dataset.submitting, undefined);
});
test('cancelled asynchronous confirmation leaves form usable even when listener runs after guard', async () => {
    const f = submissionFixture();
    const event = f.submit(); event.preventDefault();
    await new Promise(resolve => setTimeout(resolve, 10));
    assert.equal(f.button.disabled, false); assert.equal(f.form.dataset.submitting, undefined);
    f.submit(); await new Promise(resolve => setTimeout(resolve, 10));
    assert.equal(f.button.disabled, true); assert.equal(f.submit().defaultPrevented, true);
});
test('back navigation clears only temporary submit locks', async () => {
    const f = submissionFixture(); f.submit(); await new Promise(resolve => setTimeout(resolve, 10)); f.window.fire('pageshow');
    assert.equal(f.button.disabled, false); assert.equal(f.businessDisabled.disabled, true);
    assert.equal(f.form.dataset.submitting, undefined);
});
function slotsFixture() {
    const requests = [], window = {};
    vm.runInNewContext(source('js/slot-picker.js'), {window, document: {createElement: () => new Element()}, AbortController,
        setTimeout, clearTimeout, fetch: (url, options) => new Promise(resolve => requests.push({url, options, resolve}))});
    const options = Object.fromEntries(['grid', 'selected', 'submit', 'prompt', 'loading', 'empty', 'error'].map(key => [key, new Element()]));
    const picker = window.MedClinicSlots(options);
    const resolve = (index, data, ok = true) => requests[index].resolve({ok, status: ok ? 200 : 503, json: async () => data});
    return {requests, options, picker, resolve};
}
const slot = id => ({id, startTime: '08:00:00', endTime: '08:30:00', status: 'AVAILABLE'});
test('late response cannot replace current doctor slots', async () => {
    const f = slotsFixture(); const old = f.picker.load(1, '2026-10-02'); const current = f.picker.load(2, '2026-10-03');
    assert.equal(f.requests[0].options.signal.aborted, true);
    f.resolve(1, [slot(20)]); await current; f.resolve(0, [slot(10)]); await old;
    assert.equal(f.options.grid.children.length, 1);
    f.options.grid.children[0].children[0].click();
    assert.equal(f.options.selected.value, 20); assert.equal(f.options.submit.disabled, false);
});
test('clearing specialty invalidates pending response', async () => {
    const f = slotsFixture(); const pending = f.picker.load(1, '2026-10-02'); f.picker.clear();
    f.resolve(0, [slot(10)]); await pending;
    assert.equal(f.options.grid.children.length, 0); assert.equal(f.options.selected.value, '');
    assert.equal(f.options.prompt.classList.contains('d-none'), false);
});
test('HTTP failure, empty result, invalid payload and successful retry have distinct states', async () => {
    const f = slotsFixture(); let pending = f.picker.load(1, '2026-10-02'); f.resolve(0, [], false); await pending;
    assert.equal(f.options.error.classList.contains('d-none'), false); assert.equal(f.options.empty.classList.contains('d-none'), true);
    pending = f.picker.load(1, '2026-10-02'); f.resolve(1, []); await pending;
    assert.equal(f.options.empty.classList.contains('d-none'), false);
    pending = f.picker.load(1, '2026-10-02'); f.resolve(2, {}); await pending;
    assert.equal(f.options.error.classList.contains('d-none'), false);
    pending = f.picker.load(1, '2026-10-02'); f.resolve(3, [slot(10)]); await pending;
    assert.equal(f.options.error.classList.contains('d-none'), true); assert.equal(f.options.grid.children.length, 1);
});
function workerFixture(fail = false) {
    const handlers = {}, writes = [], deleted = [], additions = [];
    const response = {ok: true, type: 'basic', clone() {return this;}};
    const caches = {open: async () => ({addAll: async paths => additions.push(...paths), put: async request => writes.push(request.url)}),
        match: async path => path === '/offline.html' ? 'offline-shell' : undefined,
        keys: async () => ['medclinic-static-v1', 'medclinic-static-v2', 'another-app'], delete: async key => deleted.push(key)};
    vm.runInNewContext(source('sw.js'), {URL, Response, caches, fetch: async () => {if (fail) throw Error('offline'); return response;},
        self: {location: {origin: 'https://medclinic.test'}, clients: {claim: async () => {}}, skipWaiting() {}, addEventListener: (key, fn) => handlers[key] = fn}});
    async function send(path, mode = 'cors', method = 'GET') {
        const waits = []; let answer;
        const request = {url: path.startsWith('https:') ? path : 'https://medclinic.test' + path, method, mode};
        handlers.fetch({request, waitUntil: p => waits.push(p), respondWith: p => answer = p});
        const result = await answer; await Promise.all(waits); return result;
    }
    return {handlers, writes, deleted, additions, send};
}
test('worker never caches navigation, private avatars, API or external responses', async () => {
    const f = workerFixture();
    await f.send('/patient/appointments', 'navigate'); await f.send('/api/public/doctors/1/slots');
    await f.send('/user-content/avatars/private.png'); await f.send('https://cdn.test/js/library.js'); await f.send('/images/logo.svg', 'cors', 'POST');
    assert.deepEqual(f.writes, []); await f.send('/css/medclinic-design-system.css');
    assert.deepEqual(f.writes, ['https://medclinic.test/css/medclinic-design-system.css']);
});
test('worker serves offline identity and purges only its own obsolete cache', async () => {
    const f = workerFixture(true);
    assert.equal(await f.send('/patient/appointments', 'navigate'), 'offline-shell');
    const waits = []; f.handlers.install({waitUntil: p => waits.push(p)}); f.handlers.activate({waitUntil: p => waits.push(p)});
    await Promise.all(waits);
    assert.deepEqual(f.additions, ['/offline.html', '/images/logo.svg']); assert.deepEqual(f.deleted, ['medclinic-static-v1']);
});
test('chart data and legend/grid colors update together when theme changes', () => {
    const charts = [], document = new Element();
    const motion = Object.assign(new Element(), {matches: false});
    document.documentElement = {}; document.getElementById = id => ({id});
    let theme = 'light';
    class Chart {
        constructor(canvas, config) { this.config = config; this.data = config.data; this.options = config.options; charts.push(this);
            for (const scale of Object.values(this.options.scales)) scale.border = {}; }
        update(mode) { this.updated = mode; }
    }
    const html = fs.readFileSync('src/main/resources/templates/admin/dashboard.html','utf8');
    const script = html.slice(html.indexOf("document.addEventListener('DOMContentLoaded'"), html.indexOf('</script>', html.indexOf("document.addEventListener('DOMContentLoaded'")));
    vm.runInNewContext(script, {document, Chart, window: {Chart, matchMedia: () => motion},
        statusRawData: {'Đã xác nhận': 3}, specialtyRawData: {'Tim mạch':3},
        getComputedStyle: () => ({getPropertyValue: token => theme + token})});
    document.fire('DOMContentLoaded'); assert.equal(charts.length,2);
    assert.equal(charts[0].data.datasets[0].data[0], 3);
    theme = 'dark'; document.fire('medclinic:theme-change');
    assert.equal(charts[1].data.datasets[0].backgroundColor, 'dark--mc-primary');
    assert.equal(charts[0].options.plugins.legend.labels.color, 'dark--mc-text');
    assert.equal(charts[1].options.scales.y.grid.color, 'dark--mc-border-strong');
    assert.equal(charts[1].updated, 'none');
    motion.fire('change', {matches: true});
    assert.equal(charts[0].options.animation, false);
    assert.equal(charts[1].options.animation, false);
});

test('server validation opens its modal and focuses the error after the focus trap activates', () => {
    const modal = new Element(), field = new Element(), error = new Element(); let focused = false, shown = false;
    field.focus = () => focused = true; field.closest = () => modal;
    field.parentElement = {querySelector: () => error}; modal.querySelector = () => field;
    const document = Object.assign(new Element(), {readyState: 'complete',
        querySelectorAll: selector => selector === '.is-invalid' ? [field] : [],
        querySelector: selector => selector.startsWith('input.is-invalid') ? field : null});
    const window = Object.assign(new Element(), {bootstrap: {Modal: {getOrCreateInstance: node => {
        assert.equal(node, modal); return {show() { shown = true; }};
    }}}});
    vm.runInNewContext(source('js/medclinic-ui.js'), {document, window, setTimeout});
    assert.equal(shown, true); assert.equal(focused, false);
    assert.equal(field.getAttribute('aria-invalid'), 'true');
    assert.equal(field.getAttribute('aria-describedby'), error.id);
    modal.fire('shown.bs.modal'); assert.equal(focused, true);
});

test('modal keyboard boundary stays inside the dialog and skips disabled or invisible controls', () => {
    const modal = new Element(), first = new Element(), last = new Element(), disabled = new Element(), hidden = new Element();
    let focused;
    for (const control of [first, last, disabled, hidden]) {
        control.tabIndex = 0; control.getClientRects = () => [1];
        control.closest = () => modal; control.focus = () => focused = control;
    }
    disabled.disabled = true; hidden.getClientRects = () => [];
    modal.querySelectorAll = () => [disabled, first, hidden, last]; modal.closest = () => modal;
    const document = Object.assign(new Element(), {readyState: 'complete', querySelectorAll: () => [], querySelector: () => null});
    vm.runInNewContext(source('js/medclinic-ui.js'), {document, window: new Element(), setTimeout});
    function tab(target, shiftKey) {
        const event = {key: 'Tab', target, shiftKey, prevented: false, preventDefault() {this.prevented = true;}};
        document.fire('keydown', event); return event.prevented;
    }
    assert.equal(tab(last, false), true); assert.equal(focused, first);
    assert.equal(tab(first, true), true); assert.equal(focused, last);
    assert.equal(tab(modal, false), true); assert.equal(focused, first);
    assert.equal(tab(first, false), false);
});

function bankFixture(initialStatus = 'PENDING') {
    const requests = [], timers = new Map(); let nextTimer = 0;
    const nodes = {status: new Element(), retry: new Element(), manual: new Element(), qr: new Element(), login: new Element()};
    const window = {setTimeout: (fn, delay) => {timers.set(++nextTimer, {fn, delay}); return nextTimer;},
        clearTimeout: id => timers.delete(id), fetch: (url, options) => new Promise((resolve, reject) => {
            requests.push({url, options, resolve, reject});
            options.signal.addEventListener('abort', () => reject(new Error('aborted')));
        })};
    const document = {readyState: 'complete', querySelector: () => null};
    vm.runInNewContext(source('js/bank-payment-status.js'), {window, document, AbortController});
    const monitor = window.MedClinicBankStatus({...nodes, initialStatus, url: '/staff/billing/bank/payment/1/status'});
    const answer = (i, status, confirmed = false) => requests[i].resolve({ok: true, status: 200, json: async () => ({status, confirmed})});
    return {nodes, requests, timers, monitor, answer};
}
test('bank checks never overlap, disclose network errors and recover through explicit retry', async () => {
    const f = bankFixture(); const first = f.monitor.check(); await f.monitor.check();
    assert.equal(f.requests.length, 1); assert.equal(f.nodes.retry.disabled, true);
    f.requests[0].reject(new Error('offline')); await first;
    assert.match(f.nodes.status.textContent, /Chưa có xác nhận/); assert.equal(f.nodes.retry.disabled, false);
    const retry = f.monitor.check(); f.answer(1, 'PENDING'); await retry;
    assert.match(f.nodes.status.textContent, /Đang chờ/);
    assert.equal(f.requests[1].options.method, undefined); // GET only; no automatic confirmation.
});
test('bank terminal states stop polling and stale QR while preserving expired-payment reconciliation', async () => {
    for (const status of ['CONFIRMED', 'EXPIRED', 'REJECTED']) {
        const f = bankFixture(); const pending = f.monitor.check(); f.answer(0, status, status === 'CONFIRMED'); await pending;
        assert.equal(f.nodes.manual.hidden, status !== 'EXPIRED'); assert.equal(f.nodes.qr.hidden, true);
        assert.equal(f.nodes.retry.hidden, true); assert.equal(f.timers.size, 0);
        await f.monitor.check(); assert.equal(f.requests.length, 1);
        const initial = bankFixture(status); await initial.monitor.check(); assert.equal(initial.requests.length, 0);
    }
});

function examinationFixture() {
    const f = submissionFixture(), dialogs = [], status = new Element(), review = new Element();
    let valid = true, requested = 0, reported = 0, focused = 0;
    f.button.focus = () => focused++;
    f.form.checkValidity = () => valid;
    f.form.reportValidity = () => reported++;
    f.form.requestSubmit = () => { requested++; f.submit(); };
    f.window.Swal = {fire: () => new Promise(resolve => dialogs.push(resolve))};
    vm.runInNewContext(source('js/examination-guard.js'), {window: f.window});
    const guard = f.window.MedClinicExaminationGuard({form: f.form, submit: f.button, review, status,
        renderReview() {}, confirmationText: () => 'Ca khám DEMO, chưa thu tiền'});
    return {...f, guard, dialogs, status, review, invalidate() {valid = false;},
        counts: () => ({requested, reported, focused})};
}
const settle = () => new Promise(resolve => setTimeout(resolve, 10));
function leaving(window) {
    const event = {prevented: false, preventDefault() {this.prevented = true;}};
    window.fire('beforeunload', event); return event.prevented;
}
test('examination cancellation returns focus, prevents duplicate dialogs and keeps the unsaved warning', async () => {
    const f = examinationFixture(); f.guard.markEdited();
    assert.equal(leaving(f.window), true);
    f.submit(); f.submit(); assert.equal(f.dialogs.length, 1); assert.equal(f.review.open, true);
    f.dialogs[0]({isConfirmed: false}); await settle();
    assert.deepEqual(f.counts(), {requested: 0, reported: 0, focused: 1});
    assert.equal(f.button.disabled, false); assert.equal(leaving(f.window), true);
});
test('examination revalidates changed input before confirmation can submit and can then be cancelled', async () => {
    const f = examinationFixture(); f.guard.markEdited(); f.submit(); f.invalidate();
    f.dialogs[0]({isConfirmed: true}); await settle();
    assert.deepEqual(f.counts(), {requested: 0, reported: 1, focused: 0});
    assert.equal(f.button.disabled, false); assert.equal(leaving(f.window), true);
    f.submit(); assert.equal(f.dialogs.length, 2); f.dialogs[1]({isConfirmed: false}); await settle();
    assert.equal(f.counts().requested, 0);
});
test('confirmed examination submits once, locks briefly and restores unsaved protection after back navigation', async () => {
    const f = examinationFixture(); f.guard.markEdited(); f.submit();
    f.dialogs[0]({isConfirmed: true}); await settle();
    assert.equal(f.counts().requested, 1); assert.equal(f.button.disabled, true);
    assert.equal(leaving(f.window), false);
    f.window.fire('pageshow', {persisted: true});
    assert.equal(f.button.disabled, false); assert.equal(leaving(f.window), true);
    f.submit(); assert.equal(f.dialogs.length, 2); f.dialogs[1]({isConfirmed: false}); await settle();
});
test('failed examination dispatch clears confirmation and preserves recoverable input', async () => {
    const f = examinationFixture(); f.guard.markEdited();
    f.form.requestSubmit = () => {throw new Error('request blocked');}; f.submit();
    f.dialogs[0]({isConfirmed: true}); await settle();
    assert.match(f.status.textContent, /Chưa gửi được/); assert.equal(f.button.disabled, false);
    assert.equal(leaving(f.window), true);
    f.submit(); assert.equal(f.dialogs.length, 2); f.dialogs[1]({isConfirmed: false}); await settle();
});
function otpFixture(seconds) {
    let now = 0, next = 0; const intervals = new Map();
    const window = Object.assign(new Element(), {setInterval: fn => {intervals.set(++next, fn); return next;},
        clearInterval: id => intervals.delete(id)});
    const document = Object.assign(new Element(), {readyState: 'complete', hidden: false, getElementById: () => null});
    vm.runInNewContext(source('js/otp-countdown.js'), {window, document, Date: {now: () => now}});
    const timer = new Element(), expired = new Element(), submit = new Element();
    timer.dataset.seconds = seconds; expired.hidden = true;
    const countdown = window.MedClinicOtpCountdown({timer, expired, submit});
    return {window, document, timer, expired, submit, intervals, countdown, advance: ms => now += ms};
}
test('OTP uses elapsed time after background suspension and disables an expired submission', () => {
    const f = otpFixture('120'); assert.equal(f.timer.textContent, '02:00');
    f.window.fire('pagehide'); assert.equal(f.intervals.size, 0); f.advance(121000);
    f.window.fire('pageshow'); assert.equal(f.timer.textContent, '00:00');
    assert.equal(f.submit.disabled, true); assert.equal(f.expired.hidden, false); assert.equal(f.intervals.size, 0);
});
test('OTP visibility resume never adds concurrent timers and rejects invalid initial deadlines', () => {
    const f = otpFixture('65'); f.advance(6000); f.document.fire('visibilitychange');
    assert.equal(f.timer.textContent, '00:59'); assert.equal(f.intervals.size, 1);
    f.document.fire('visibilitychange'); assert.equal(f.intervals.size, 1);
    for (const seconds of ['0', '-20', 'invalid']) {
        const expired = otpFixture(seconds); assert.equal(expired.submit.disabled, true);
        assert.equal(expired.expired.hidden, false); assert.equal(expired.intervals.size, 0);
    }
});
test('bank rejects inconsistent confirmation and requires re-login after session expiry', async () => {
    const f = bankFixture(); let pending = f.monitor.check(); f.answer(0, 'CONFIRMED', false); await pending;
    assert.match(f.nodes.status.textContent, /Chưa có xác nhận/); assert.equal(f.nodes.manual.hidden, false);
    pending = f.monitor.check(); f.requests[1].resolve({ok: false, status: 401}); await pending;
    assert.equal(f.nodes.login.hidden, false); assert.match(f.nodes.status.textContent, /Đăng nhập lại/);
    f.monitor.resume(); await f.monitor.check(); assert.equal(f.requests.length, 2); assert.equal(f.timers.size, 0);
});
test('bank timeout and hide/resume abort requests without stranding the monitor', async () => {
    const f = bankFixture(); let pending = f.monitor.check();
    [...f.timers.values()].find(t => t.delay === 10000).fn(); await pending;
    assert.match(f.nodes.status.textContent, /Chưa kiểm tra/);
    pending = f.monitor.check(); f.monitor.stop(); f.monitor.resume(); await pending;
    assert.equal(f.requests[1].options.signal.aborted, true);
    assert.equal([...f.timers.values()].some(t => t.delay === 3000), true);
    pending = f.monitor.check(); f.answer(2, 'PENDING'); await pending;
    assert.match(f.nodes.status.textContent, /Đang chờ/);
});


test('all templates resolve UI, QR scanner and realtime libraries locally without CDN requests', () => {
    const path = require('node:path');
    const staticRoot = path.resolve('src/main/resources/static');
    const visited = new Set();
    function verifyAsset(relative) {
        const absolute = path.resolve(staticRoot, relative);
        assert.ok(absolute.startsWith(staticRoot + path.sep), 'Asset must stay under static root');
        assert.ok(fs.existsSync(absolute), 'Missing asset: ' + relative);
        assert.ok(fs.statSync(absolute).size > 0, 'Empty asset: ' + relative);
        if (visited.has(absolute)) return;
        visited.add(absolute);
        if (relative.endsWith('.css')) {
            const css = fs.readFileSync(absolute, 'utf8');
            for (const match of css.matchAll(/url\(\s*['"]?([^)'"\s]+)['"]?\s*\)/g)) {
                const url = match[1];
                if (url.startsWith('data:') || url.startsWith('#')) continue;
                assert.ok(!/^https?:|^\/\//.test(url), 'External CSS resource: ' + url);
                verifyAsset(path.join(path.dirname(relative), url.split(/[?#]/)[0]));
            }
        }
    }
    const templateRoot = 'src/main/resources/templates';
    for (const template of fs.readdirSync(templateRoot, {recursive:true}).filter(p => p.endsWith('.html'))) {
        const html = fs.readFileSync(path.join(templateRoot, template), 'utf8');
        assert.equal(/(?:src|href)="https?:[^"\n]+(?:cdn\.|fonts\.google)/.test(html), false);
        const references = [...html.matchAll(/th:(?:href|src)="@\{\/([^{}]+\.(?:css|js))\}"/g)];
        if (template.startsWith('layout')) assert.ok(references.length >= 6, 'Layout must load local libraries');
        for (const match of references) verifyAsset(match[1]);
    }
    assert.ok([...visited].some(p => p.endsWith('Geist-Variable.woff2')));
    assert.ok([...visited].some(p => p.endsWith('bootstrap-icons.woff2')));
});
