/* Public static assets only. Never persist authenticated HTML or API responses. */
const CACHE_NAME = 'medclinic-static-v2';
const STATIC_ASSETS = ['/offline.html', '/images/logo.svg'];
self.addEventListener('install', event => {
    event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(STATIC_ASSETS)));
    self.skipWaiting();
});
self.addEventListener('activate', event => {
    event.waitUntil(caches.keys().then(keys => Promise.all(keys
        .filter(key => key.startsWith('medclinic-static-') && key !== CACHE_NAME)
        .map(key => caches.delete(key)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', event => {
    const request = event.request;
    const url = new URL(request.url);
    if (request.method !== 'GET' || url.origin !== self.location.origin) return;
    if (request.mode === 'navigate') {
        event.respondWith(fetch(request).catch(() => caches.match('/offline.html')));
        return;
    }
    if (!/^\/(css|js|images)\//.test(url.pathname)) return;
    const network = fetch(request);
    event.waitUntil(network.then(response => {
        if (response.ok && response.type === 'basic') {
            const copy = response.clone();
            return caches.open(CACHE_NAME).then(cache => cache.put(request, copy));
        }
    }).catch(() => {}));
    event.respondWith(network.catch(async () => (await caches.match(request)) || Response.error()));
});
