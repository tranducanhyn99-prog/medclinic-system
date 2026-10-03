/*
 * Vanilla JavaScript adaptation of page-mascot by Kamran Ahmed.
 * Original project: https://github.com/nilbuild/page-mascot
 * Licensed under the MIT License. See /vendor/page-mascot/LICENSE.txt.
 */
(function () {
    'use strict';

    var directions = ['up-left', 'up', 'up-right', 'left', 'center', 'right', 'down-left', 'down', 'down-right'];
    var reactions = ['blink', 'heart', 'sparkle', 'surprised', 'wink', 'bashful', 'sleepy', 'dizzy', 'delighted'];
    var clockwise = ['right', 'down-right', 'down', 'down-left', 'left', 'up-left', 'up', 'up-right'];
    var payoffs = ['heart', 'sparkle', 'delighted'];
    var sectorSize = (Math.PI * 2) / clockwise.length;
    var hysteresis = 0.12;
    var deadZone = 70;

    function cellPosition(index) {
        return ((index % 3) * 50) + '% ' + (Math.floor(index / 3) * 50) + '%';
    }

    function wrap(angle) {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }

    function initializeMascot(button) {
        var directionLayer = button.querySelector('[data-mascot-directions-layer]');
        var reactionLayer = button.querySelector('[data-mascot-reactions-layer]');
        var stage = button.querySelector('[data-mascot-stage]');
        if (!directionLayer || !reactionLayer || !stage) return;

        directionLayer.style.backgroundImage = 'url("' + button.dataset.directions + '")';
        reactionLayer.style.backgroundImage = 'url("' + button.dataset.reactions + '")';
        directionLayer.style.backgroundPosition = cellPosition(directions.indexOf('center'));
        reactionLayer.style.backgroundPosition = cellPosition(reactions.indexOf('blink'));

        var activeSector = -1;
        var pointer = null;
        var timers = [];
        var boops = { count: 0, at: 0 };

        function clearTimers() {
            timers.forEach(window.clearTimeout);
            timers = [];
        }

        function showDirection(direction) {
            directionLayer.style.backgroundPosition = cellPosition(directions.indexOf(direction));
        }

        function showReaction(reaction) {
            reactionLayer.style.backgroundPosition = cellPosition(reactions.indexOf(reaction));
            reactionLayer.style.opacity = '1';
            directionLayer.style.opacity = '0';
        }

        function clearReaction() {
            reactionLayer.style.opacity = '0';
            directionLayer.style.opacity = '1';
        }

        function aim() {
            if (!pointer || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
            var box = button.getBoundingClientRect();
            var dx = pointer.x - (box.left + box.width / 2);
            var dy = pointer.y - (box.top + box.height / 2);

            if (Math.hypot(dx, dy) < deadZone) {
                activeSector = -1;
                showDirection('center');
                return;
            }

            var angle = Math.atan2(dy, dx);
            if (activeSector !== -1 && Math.abs(wrap(angle - activeSector * sectorSize)) < sectorSize / 2 + hysteresis) {
                return;
            }

            activeSector = (Math.round(angle / sectorSize) + clockwise.length) % clockwise.length;
            showDirection(clockwise[activeSector]);
        }

        if (window.matchMedia('(hover: hover) and (pointer: fine)').matches && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            window.addEventListener('pointermove', function (event) {
                pointer = { x: event.clientX, y: event.clientY };
                aim();
            }, { passive: true });
            window.addEventListener('scroll', aim, { passive: true });
        }

        var widget = button.closest('.mc-mascot-widget');
        var bubble = widget ? widget.querySelector('#mascotBubble') : null;
        var closeBtn = widget ? widget.querySelector('#closeMascotBubble') : null;

        function setBubble(open) {
            if (!bubble) return;
            bubble.classList.toggle('mc-bubble-dismissed', !open);
            bubble.hidden = !open;
            bubble.inert = !open;
            button.setAttribute('aria-expanded', String(open));
            button.setAttribute('aria-label', open ? 'Đóng hướng dẫn từ điều dưỡng An Nhiên' : 'Mở hướng dẫn từ điều dưỡng An Nhiên');
            if (open && closeBtn) closeBtn.focus();
        }
        setBubble(false);
        if (closeBtn) closeBtn.addEventListener('click', function () { setBubble(false); button.focus(); });
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && bubble && !bubble.hidden) { setBubble(false); button.focus(); }
        });
        document.addEventListener('click', function (event) {
            if (widget && !widget.contains(event.target)) setBubble(false);
        });

        button.addEventListener('click', function () {
            clearTimers();
            var now = Date.now();
            boops.count = now - boops.at < 1600 ? boops.count + 1 : 1;
            boops.at = now;

            if (bubble) setBubble(bubble.classList.contains('mc-bubble-dismissed'));

            if (boops.count >= 4) {
                boops.count = 0;
                showReaction('dizzy');
                timers.push(window.setTimeout(clearReaction, 1100));
            } else {
                showReaction('blink');
                timers.push(window.setTimeout(function () {
                    showReaction(payoffs[(boops.count - 1) % payoffs.length]);
                }, 120));
                timers.push(window.setTimeout(clearReaction, 560));
            }

            if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches && stage.animate) {
                stage.animate([
                    { transform: 'scale(1, 1)', easing: 'ease-in' },
                    { transform: 'scale(1.10, .86)', offset: .18, easing: 'ease-out' },
                    { transform: 'scale(.95, 1.08)', offset: .45, easing: 'ease-in-out' },
                    { transform: 'scale(1.03, .97)', offset: .72, easing: 'ease-in-out' },
                    { transform: 'scale(1, 1)' }
                ], { duration: 420, easing: 'linear' });
            }
        });
    }

    function initializeAll() {
        document.querySelectorAll('[data-page-mascot]').forEach(initializeMascot);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeAll);
    } else {
        initializeAll();
    }
})();
