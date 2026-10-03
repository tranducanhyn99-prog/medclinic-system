// Read-only acceptance samples exported by isolated MockMvc tests.
// No proxy, application database, credentials, uploads or write endpoints.
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const workspace = path.resolve(__dirname, '..');
const destination = path.join(workspace, 'output', 'ux-manual', 'site');
const raw = path.join(workspace, 'output', 'ux-manual', 'raw');
const staticRoot = path.join(workspace, 'src', 'main', 'resources', 'static');

function build() {
    fs.mkdirSync(destination, {recursive: true});
    for (const folder of ['css', 'js', 'images', 'vendor', 'fonts']) {
        const source = path.join(staticRoot, folder);
        if (fs.existsSync(source)) fs.cpSync(source, path.join(destination, folder), {recursive: true});
    }
    const files = fs.readdirSync(raw).filter(name => /^(bank-(pending|confirmed|expired|rejected)|examination-errors)\.html$/.test(name));
    for (const name of files) {
        let html = fs.readFileSync(path.join(raw, name), 'utf8');
        html = html.replace(/<input\b[^>]*name="_csrf"[^>]*>/g, '')
            .replace(/<form\b[^>]*>/g, match => match.replace(/action="[^"]*"/g, 'action="#"'))
            .replace(/href="\/(?!css\/)[^"]*"/g, 'href="#"')
            .replace(/data-status-url="[^"]*"/g, 'data-status-url="/__review/status"');
        if (name === 'bank-pending.html') html = html.replace(/(id="paymentQrImage"\s+src=")[^"]*"/, '$1/__review/missing-qr.svg"');
        const safety = `<script>
document.addEventListener('submit', function(event) { event.preventDefault(); }, true);
document.addEventListener('click', function(event) {
    var link = event.target.closest('a[href="#"]'); if(link) event.preventDefault();
});
</script>`;
        html = html.replace('</head>', safety + '</head>').replace(/(<body[^>]*>)/, '$1' +
            '<aside class="alert alert-warning text-center rounded-0 mb-0" role="note">Mẫu nghiệm thu · Dữ liệu giả · Không lưu hoặc thu tiền · <a href="/">Về hướng dẫn</a></aside>');
        fs.writeFileSync(path.join(destination, name), html);
    }
    const links = files.map(name => `<li><a href="/${name}">${name === 'examination-errors.html' ? 'Form khám: lỗi validation, thêm/xóa thuốc và rà soát' : 'Chuyển khoản: ' + name.replace('bank-', '').replace('.html', '')}</a></li>`).join('');
    fs.writeFileSync(path.join(destination, 'index.html'), `<!doctype html><html lang="vi"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Nghiệm thu MedClinic</title><link rel="stylesheet" href="/css/medclinic-design-system.css"><style>body{padding:24px;max-width:760px;margin:auto;background:#f8fafc;color:#0f172a;font-family:system-ui,sans-serif;line-height:1.65}a{color:#0e7490;display:inline-block;padding:10px 0;min-height:44px}h1{font-size:1.75rem;line-height:1.25}li{margin:8px 0}</style><main><h1>Nghiệm thu UX/UI MedClinic</h1><p>Các trang dưới đây được render bằng template của dự án, với dữ liệu giả trong test H2. Mọi thao tác gửi form đều bị chặn tại trang; server chỉ hỗ trợ đọc. QR mẫu không dùng để chuyển tiền.</p><ol><li>Trên Chrome/Edge, đặt mức thu phóng <strong>200%</strong> trong menu trình duyệt. Kiểm tra chữ, nút, bảng và menu không bị cắt hoặc chồng nhau.</li><li>Bật Narrator bằng <strong>Windows + Ctrl + Enter</strong> (hoặc NVDA). Dùng Tab, Shift+Tab, Enter và Escape; nghe nhãn, thông báo lỗi và trạng thái.</li><li>Ở form khám, nghe lỗi triệu chứng/chẩn đoán. Thêm hai dòng thuốc, xóa một dòng và mở phần rà soát. Focus cần ở thuốc vừa thêm, dòng còn lại sau xóa; thông báo cần đọc đúng.</li><li>Bật/tắt theme. Trong Windows, tắt <strong>Accessibility → Visual effects → Animation effects</strong>; tải lại trang và kiểm tra chuyển động trang trí dừng.</li><li>Trên điện thoại cùng Wi-Fi, mở địa chỉ LAN được cung cấp. Thử chạm menu, đổi theme, thêm/xóa thuốc, mở/đóng phần rà soát; xem chữ dài và bàn phím ảo có che thao tác không.</li></ol><h2>Trang mẫu</h2><ul>${links}</ul><p>Gửi kết quả theo mẫu: zoom: đạt/lỗi; đọc màn hình: đạt/lỗi; giảm chuyển động: đạt/lỗi; touch: đạt/lỗi + tên thiết bị/trình duyệt. Nếu lỗi, mô tả trang và thao tác.</p></main></html>`);
    console.log(`Built ${files.length} rendered samples in ${destination}`);
}

function serve() {
    const port = 18089;
    const mime = {'.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
        '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg', '.woff2': 'font/woff2', '.woff': 'font/woff'};
    const server = http.createServer((request, response) => {
        response.setHeader('Cache-Control', 'no-store');
        response.setHeader('X-Content-Type-Options', 'nosniff');
        response.setHeader('Referrer-Policy', 'no-referrer');
        if (!['GET', 'HEAD'].includes(request.method)) { response.writeHead(405, {'Allow': 'GET, HEAD'}); response.end('Read-only review'); return; }
        const url = new URL(request.url, 'http://review.invalid');
        if (url.pathname === '/__review/status') { response.writeHead(200, {'Content-Type': 'application/json'}); response.end('{"status":"PENDING","confirmed":false}'); return; }
        const relative = url.pathname === '/' ? 'index.html' : url.pathname.slice(1);
        if (!/^(index\.html|examination-errors\.html|bank-(pending|expired|rejected|confirmed)\.html|(css|js|images|vendor|fonts)\/[A-Za-z0-9_./-]+)$/.test(relative) || relative.split('/').includes('..')) {
            response.writeHead(404); response.end('Unknown review asset'); return;
        }
        const file = path.resolve(destination, relative);
        if (!file.startsWith(destination + path.sep) || !fs.existsSync(file) || !fs.statSync(file).isFile()) {
            response.writeHead(404); response.end('Unknown review asset'); return;
        }
        response.writeHead(200, {'Content-Type': mime[path.extname(file)] || 'application/octet-stream'});
        if (request.method === 'HEAD') response.end(); else fs.createReadStream(file).pipe(response);
    });
    server.listen(port, '0.0.0.0', () => console.log(`Read-only synthetic UX review: http://127.0.0.1:${port}/ (LAN enabled; no backend proxy)`));
}
if (process.argv[2] === 'build') build();
else if (process.argv[2] === 'serve') serve();
else { console.error('Usage: node ops/ux-review-server.cjs build|serve'); process.exitCode = 1; }
