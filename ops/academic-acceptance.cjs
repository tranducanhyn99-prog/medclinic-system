/* Local academic acceptance. Writes are restricted to the dedicated disposable fixture on 18090. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {performance} = require('node:perf_hooks');
const args = process.argv.slice(2);
const option = (name, fallback) => args.includes(name) ? args[args.indexOf(name) + 1] : fallback;
const origin = new URL(option('--url', 'http://127.0.0.1:8080')).origin;
const fixture = args.includes('--fixture');
const endpoint = new URL(origin);
assert.ok(['127.0.0.1', 'localhost'].includes(endpoint.hostname) && endpoint.protocol === 'http:', 'Local HTTP only');
if (fixture) assert.equal(endpoint.port, '18090', 'Fixture writes only on dedicated port 18090');
const output = path.resolve(option('--output', 'output/academic-runtime.json'));
assert.ok(output.startsWith(path.resolve('output') + path.sep), 'Report must stay in ignored output directory');
const report = {date: new Date().toLocaleDateString('en-CA',{timeZone:'Asia/Ho_Chi_Minh'}), origin, fixture, checks: [], timings: [], assetCount: 0};
const decode = s => s.replace(/&amp;/g, '&').replace(/&quot;/g, '"').replace(/&#39;/g, "'");
function attributes(tag) {
    return Object.fromEntries([...tag.matchAll(/([\w:-]+)\s*=\s*["']([^"']*)["']/g)].map(m => [m[1], decode(m[2])]));
}
function input(html, name) {
    for (const tag of html.matchAll(/<input\b[^>]*>/g)) {
        const a = attributes(tag[0]);
        if (a.name === name) return a.value || '';
    }
    throw new Error('Missing input: ' + name);
}
function client() {
    const jar = new Map();
    return async (route, body) => {
        const url = new URL(route, origin);
        assert.equal(url.origin, origin, 'Request must stay on acceptance origin');
        const response = await fetch(url, {redirect: 'manual', signal: AbortSignal.timeout(15000),
            method: body ? 'POST' : 'GET', headers: {Cookie: [...jar].map(([k,v]) => k + '=' + v).join('; '),
                ...(body ? {'Content-Type': 'application/x-www-form-urlencoded'} : {})},
            body: body ? new URLSearchParams(body) : undefined});
        for (const cookie of response.headers.getSetCookie()) {
            const pair = cookie.split(';')[0], index = pair.indexOf('=');
            jar.set(pair.slice(0,index),pair.slice(index+1));
        }
        const bytes = Buffer.from(await response.arrayBuffer());
        return {status: response.status, html: bytes.toString('utf8'), bytes, location: response.headers.get('location'),
            type: response.headers.get('content-type') || ''};
    };
}
async function login(user) {
    const request = client(), page = await request('/login');
    assert.equal(page.status, 200);
    const result = await request('/auth/login', {username:user,password:'123456',_csrf:input(page.html,'_csrf')});
    assert.equal(result.status, 302);
    assert.ok(!result.location.includes('/login') && !result.location.includes('/auth/otp'), 'Seed role must sign in');
    report.checks.push('login-' + user);
    return request;
}
async function measure(request, route) {
    for (let i=0;i<2;i++) assert.equal((await request(route)).status,200);
    const samples=[];
    for (let i=0;i<30;i++) {
        const start=performance.now();
        assert.equal((await request(route)).status,200);
        samples.push(performance.now()-start);
    }
    samples.sort((a,b)=>a-b);
    const stats={route,samples:30,warmup:2,p50_ms:+samples[14].toFixed(2),p95_ms:+samples[28].toFixed(2),
        max_ms:+samples[29].toFixed(2),errors:0};
    report.timings.push(stats);
    assert.ok(stats.p95_ms<1500, 'HTML p95 must be below local academic target: ' + route);
}
const assets = new Set();
async function asset(url) {
    const resource = new URL(url, origin);
    assert.equal(resource.origin,origin,'Critical assets must be local');
    if (assets.has(resource.href)) return;
    assets.add(resource.href);
    const response=await fetch(resource,{redirect:'manual',signal:AbortSignal.timeout(15000)});
    assert.equal(response.status,200,'Missing static asset: '+resource.pathname);
    const type=response.headers.get('content-type') || '';
    assert.ok(!type.includes('text/html'), 'Static request must not return a login page: ' + resource.pathname);
    if (resource.pathname.endsWith('.css')) assert.ok(type.includes('text/css'));
    if (resource.pathname.endsWith('.js')) assert.ok(type.includes('javascript'));
    const bytes=Buffer.from(await response.arrayBuffer());
    assert.ok(bytes.length>0);
    if (resource.pathname.endsWith('.css')) {
        for (const match of bytes.toString('utf8').matchAll(/url\(\s*['"]?([^)'"\s]+)['"]?\s*\)/g)) {
            if (!match[1].startsWith('data:') && !match[1].startsWith('#')) await asset(new URL(match[1],resource).href);
        }
    }
}
async function htmlAssets(html) {
    for (const tag of html.matchAll(/<(?:script|link)\b[^>]*>/g)) {
        const a=attributes(tag[0]);
        if (a.src || a.rel==='stylesheet') await asset(a.src||a.href);
    }
}
const mailOrigin='http://127.0.0.1:8025';
async function mail(recipient,subject) {
    for(let attempt=0;attempt<24;attempt++) {
        const list=await (await fetch(mailOrigin+'/api/v1/messages')).json();
        const found=(list.messages||[]).find(m=>m.Subject.includes(subject) && (m.To||[]).some(to=>to.Address===recipient));
        if(found) return await (await fetch(mailOrigin+'/api/v1/message/'+found.ID)).json();
        await new Promise(resolve=>setTimeout(resolve,250));
    }
    throw new Error('Expected fixture email did not reach Mailpit: '+subject);
}
async function postForm(request,page,route,values) {
    const form=await request(page);
    assert.equal(form.status,200);
    return request(route,{...values,_csrf:input(form.html,'_csrf')});
}
async function workflow(staff,doctor) {
    const patient=client(),nonce=Date.now().toString(36),email='academic-'+nonce+'@example.test';
    const registration=await postForm(patient,'/register','/register',{username:'academic'+nonce,password:'AcademicDemo21!',
        confirmPassword:'AcademicDemo21!',fullName:'NGHIEM THU DO AN DEMO',phone:'09'+String(Date.now()).slice(-8),email});
    assert.equal(registration.status,302);
    assert.ok(registration.location.includes('/auth/otp?challenge='),'Registration must issue a challenge');
    const otpMail=await mail(email,'Mã xác thực');
    const otp=otpMail.HTML.match(/letter-spacing:[^>]+>(\d{6})<\/div>/)?.[1];
    assert.ok(otp,'OTP email must contain six-digit code');
    const otpPage=await patient(registration.location);
    const activated=await patient('/auth/otp',{challengeId:input(otpPage.html,'challengeId'),code:otp,_csrf:input(otpPage.html,'_csrf')});
    assert.equal(activated.status,302);
    assert.ok(!activated.location.includes('/auth/otp'),'OTP must activate and create session');
    report.checks.push('registration-and-Mailpit-OTP');
    assert.equal((await patient('/admin/dashboard')).status,403);
    report.checks.push('patient-RBAC-denied-admin');
    const dateParts=new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Ho_Chi_Minh',year:'numeric',month:'2-digit',day:'2-digit'}).formatToParts(new Date());
    const value=type=>dateParts.find(p=>p.type===type).value;
    const today=value('year')+'-'+value('month')+'-'+value('day');
    const currentTime=new Intl.DateTimeFormat('en-GB',{timeZone:'Asia/Ho_Chi_Minh',hour:'2-digit',minute:'2-digit',hour12:false}).format(new Date());
    const slotResponse=await patient('/api/public/doctors/1/slots?date='+today);
    const slot=JSON.parse(slotResponse.html).find(s=>s.status==='AVAILABLE' && s.startTime>currentTime);
    assert.ok(slot,'Run the clinical fixture while doctor 1 has a future slot today; no bypass of booking clock');
    const booking=await postForm(patient,'/patient/booking?doctorId=1','/patient/booking',{doctorId:'1',slotId:String(slot.id),date:today,reason:'DEMO academic acceptance'});
    assert.equal(booking.status,302);
    const appointmentId=booking.location.match(/\/patient\/appointments\/(\d+)/)?.[1];
    assert.ok(appointmentId,'Booking must redirect to saved appointment');
    const confirmation=await mail(email,'Xác nhận đặt lịch');
    assert.ok(confirmation.HTML.includes('cid:booking-qr'));
    const raw=await (await fetch(mailOrigin+'/api/v1/message/'+confirmation.ID+'/raw')).text();
    assert.ok(/Content-ID: <booking-qr>/i.test(raw) && /Content-Type: image\/png/i.test(raw),'SMTP must retain inline QR MIME image');
    report.checks.push('booking-and-Mailpit-inline-QR');
    const checkIn=await postForm(staff,'/staff/reception','/staff/reception/check-in/'+appointmentId,{});
    assert.equal(checkIn.status,302);
    const start=await postForm(doctor,'/doctor/desk','/doctor/start-examination/'+appointmentId,{});
    assert.equal(start.status,302);
    const form=await doctor('/doctor/examine/'+appointmentId);
    assert.equal(form.status,200);
    const catalog=JSON.parse(form.html.match(/const activeMedicines = (\[[^\n]+\]);/)?.[1] || '[]');
    const medicine=String(catalog.find(m=>m.name.includes('Paracetamol'))?.id || '');
    assert.ok(medicine,'Examination must offer a medicine from seeded catalog');
    const saved=await doctor('/doctor/examine/'+appointmentId,{symptoms:'DEMO academic acceptance',diagnosis:'DEMO academic acceptance',
        doctorNotes:'DEMO academic acceptance',medicineIds:medicine,quantities:'2',dosages:'1 vien',frequencies:'DEMO',instructions:'DEMO',_csrf:input(form.html,'_csrf')});
    assert.equal(saved.status,302);
    const closedLocation=new URL((await doctor('/doctor/examine/'+appointmentId)).location,origin);
    assert.equal(closedLocation.pathname+closedLocation.search,'/doctor/desk?closed=true');
    const history=await patient('/patient/medical-history');
    assert.equal(history.status,200); assert.ok(history.html.includes('DEMO academic acceptance'));
    const prescription=await patient('/export/prescription/'+appointmentId);
    assert.equal(prescription.status,200); assert.ok(prescription.type.includes('application/pdf'));
    assert.ok(prescription.bytes.subarray(0,5).toString()==='%PDF-');
    const billing=await staff('/staff/billing');
    const invoiceId=billing.html.match(/\/staff\/billing\/pay\/(\d+)/)?.[1]
        || billing.html.match(/\/export\/invoice\/(\d+)/)?.[1];
    assert.ok(invoiceId);
    const invoice=await staff('/export/invoice/'+invoiceId);
    assert.equal(invoice.status,200);assert.ok(invoice.bytes.subarray(0,5).toString()==='%PDF-');
    fs.writeFileSync(path.join(path.dirname(output),'academic-prescription.pdf'),prescription.bytes);
    fs.writeFileSync(path.join(path.dirname(output),'academic-invoice.pdf'),invoice.bytes);
    report.checks.push('check-in-examination-history-completed-redirect-prescription-invoice-PDF');
    const reset=await postForm(client(),'/forgot-password','/forgot-password',{email});
    assert.equal(reset.status,302);
    const resetMail=await mail(email,'Khôi phục mật khẩu');
    assert.ok(resetMail.HTML.includes('/reset-password?token='));
    report.checks.push('Mailpit-password-reset-delivery-no-password-change');
    const tomorrow=new Date(Date.now()+86400000).toLocaleDateString('en-CA',{timeZone:'Asia/Ho_Chi_Minh'});
    const futureSlots=JSON.parse((await patient('/api/public/doctors/1/slots?date='+tomorrow)).html);
    const next=futureSlots.find(s=>s.status==='AVAILABLE');assert.ok(next);
    const cancellable=await postForm(patient,'/patient/booking?doctorId=1','/patient/booking',{doctorId:'1',slotId:String(next.id),date:tomorrow,reason:'DEMO cancellation'});
    const cancellationId=cancellable.location?.match(/\/patient\/appointments\/(\d+)/)?.[1];assert.ok(cancellationId);
    assert.equal((await postForm(patient,cancellable.location,'/patient/appointments/'+cancellationId+'/cancel',{})).status,302);
    const cancelled=await mail(email,'Hủy lịch khám');assert.ok(cancelled.HTML.includes('đã được hủy'));
    report.checks.push('cancellation-and-Mailpit-delivery');
    report.fixtureData={patientsCreated:1,appointmentsCreated:2,clinicalCasesSaved:1,invoicesUnpaid:1,realPayments:0};
}
(async()=>{
    const publicRequest=client();
    assert.equal((await publicRequest('/actuator/health')).status,200);
    assert.equal((await publicRequest('/doctor/desk')).status,302);
    report.checks.push('health-and-anonymous-route-protection');
    const [patient,staff,doctor,admin]=await Promise.all(['patient','staff','doctor','admin'].map(login));
    for(const [request,route] of [[publicRequest,'/'],[patient,'/patient/booking'],[staff,'/staff/reception'],[doctor,'/doctor/desk'],[staff,'/staff/billing'],[admin,'/admin/dashboard']]) {
        const page=await request(route);assert.equal(page.status,200);await htmlAssets(page.html);await measure(request,route);
    }
    report.assetCount=assets.size;report.checks.push('four-role-pages-local-assets-and-HTML-p95');
    if(fixture) await workflow(staff,doctor);
    report.passed=true;
    fs.mkdirSync(path.dirname(output),{recursive:true});fs.writeFileSync(output,JSON.stringify(report,null,2)+'\n');
    console.log(JSON.stringify(report,null,2));
})().catch(error=>{
    report.passed=false;report.error=error.message;
    fs.mkdirSync(path.dirname(output),{recursive:true});fs.writeFileSync(output,JSON.stringify(report,null,2)+'\n');
    console.error('Acceptance failed: '+error.message);process.exitCode=1;
});
