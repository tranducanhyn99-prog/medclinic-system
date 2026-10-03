(function () {
    'use strict';
    var saved = null;
    try { saved = localStorage.getItem('medclinic-theme'); } catch (_) {}
    var theme = saved === 'dark' || saved === 'light' ? saved :
        (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    document.documentElement.setAttribute('data-bs-theme', theme);
})();
