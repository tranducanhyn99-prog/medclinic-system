# Local UI assets

Pinned npm packages from https://registry.npmjs.org/: Bootstrap 5.3.3 (MIT), Bootstrap Icons 1.11.3 (MIT), SweetAlert2 11.10.6 (MIT), Geist 1.4.2 (SIL OFL), html5-qrcode 2.3.8 (Apache-2.0), SockJS client 1.6.1 (MIT), STOMP.js 7.2.1 (Apache-2.0). Keep the included licenses with redistributed files. `manifest.json` records package members and SHA-256 of shipped files. Source-map trailers were removed because maps are not shipped. No package runtime installation is needed to run the app.

Geist variable fonts replace the Google Fonts requests; Bootstrap, icons and SweetAlert keep the versions already used by the templates. Charts and the UI's other assets already live under `static/vendor`, `static/js` or `static/images`. Optional bank QR images remain external.

Optional Google buttons use locally served Google Sans and the official G logo; source/license details in `google/README.md`. Other UI continues to use Geist.
