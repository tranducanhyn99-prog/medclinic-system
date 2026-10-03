(function () {
    function initialize() {
        var track = document.getElementById('mcBannerTrack'), viewport = document.getElementById('mcBannerViewport');
        if (!track || !viewport) return;
        var slides = Array.from(track.children), dots = Array.from(document.querySelectorAll('.mc-carousel-dot'));
        var pause = document.getElementById('mcBannerPauseBtn'), status = document.getElementById('mcBannerStatus');
        var current = 0, timer, hovering = false, focusing = false, paused = false;
        var reduced = window.matchMedia('(prefers-reduced-motion: reduce)');
        function visible() { return innerWidth <= 680 ? 1 : innerWidth <= 1024 ? 2 : 3; }
        function update(index, announce) {
            var count = Math.min(visible(), slides.length), max = Math.max(0, slides.length - count);
            current = index < 0 ? max : index > max ? 0 : index;
            var gap = parseFloat(getComputedStyle(track).columnGap) || 20;
            track.style.transform = 'translateX(-' + current * (slides[0].getBoundingClientRect().width + gap) + 'px)';
            slides.forEach(function (slide, i) {
                var hidden = i < current || i >= current + count;
                slide.inert = hidden; slide.setAttribute('aria-hidden', String(hidden));
                slide.setAttribute('role', 'group'); slide.setAttribute('aria-label', 'Banner ' + (i + 1) + ' trên ' + slides.length);
            });
            dots.forEach(function (dot, i) {
                dot.hidden = i > max; dot.classList.toggle('active', i === current);
                dot.setAttribute('aria-pressed', String(i === current));
                dot.setAttribute('aria-label', 'Hiển thị nhóm banner ' + (i + 1) + ' trên ' + (max + 1));
            });
            if (announce) status.textContent = 'Đang hiển thị banner ' + (current + 1) + ' đến ' + (current + count) + ' trên ' + slides.length;
        }
        function schedule() {
            clearInterval(timer);
            pause.setAttribute('aria-pressed', String(paused));
            pause.textContent = paused ? 'Tiếp tục chuyển banner' : 'Tạm dừng chuyển banner';
            pause.hidden = reduced.matches;
            if (!paused && !reduced.matches && !document.hidden) timer = setInterval(function () {
                if (!hovering && !focusing) update(current + 1, false);
            }, 4800);
        }
        document.getElementById('mcBannerNextBtn').addEventListener('click', function () { update(current + 1, true); schedule(); });
        document.getElementById('mcBannerPrevBtn').addEventListener('click', function () { update(current - 1, true); schedule(); });
        dots.forEach(function (dot, i) { dot.addEventListener('click', function () { update(i, true); schedule(); }); });
        pause.addEventListener('click', function () { paused = !paused; schedule(); });
        viewport.addEventListener('mouseenter', function () { hovering = true; });
        viewport.addEventListener('mouseleave', function () { hovering = false; });
        viewport.addEventListener('focusin', function () { focusing = true; });
        viewport.addEventListener('focusout', function (event) { focusing = viewport.contains(event.relatedTarget); });
        viewport.addEventListener('keydown', function (event) {
            if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return;
            if (event.target.closest('a, button')) return;
            event.preventDefault(); update(current + (event.key === 'ArrowRight' ? 1 : -1), true); schedule();
        });
        var touchX;
        viewport.addEventListener('touchstart', function (event) { touchX = event.changedTouches[0].clientX; hovering = true; }, {passive:true});
        viewport.addEventListener('touchend', function (event) {
            var diff = touchX - event.changedTouches[0].clientX;
            if (Math.abs(diff) > 40) update(current + (diff > 0 ? 1 : -1), true);
            hovering = false; schedule();
        }, {passive:true});
        viewport.addEventListener('touchcancel', function () { hovering = false; });
        window.addEventListener('resize', function () {
            update(Math.min(current, Math.max(0, slides.length - visible())), false);
            if (slides.some(function (slide) { return slide.inert && slide.contains(document.activeElement); })) viewport.focus();
        });
        document.addEventListener('visibilitychange', schedule); reduced.addEventListener('change', schedule);
        update(0, false); schedule();
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize); else initialize();
})();
