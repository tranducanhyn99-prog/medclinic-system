async page => {
 const origin='http://127.0.0.1:18090', browser=page.context().browser(), contexts=[];
 const report={origin,fixture:true,checks:[],pageErrors:[],realPayments:0};
 const assert=(condition,message)=>{if(!condition)throw new Error(message);};
 const record=(test,passed,extra={})=>report.checks.push({test,passed,...extra});
 async function login(role){
  const ctx=await browser.newContext({viewport:{width:390,height:844},locale:'vi-VN',timezoneId:'Asia/Ho_Chi_Minh'}); contexts.push(ctx);
  const p=await ctx.newPage();p.setDefaultTimeout(8000);p.on('pageerror',e=>report.pageErrors.push(e.message));
  await p.goto(origin+'/login');await p.getByLabel('Tên đăng nhập hoặc email').fill(role);await p.getByLabel('Mật khẩu',{exact:true}).fill('123456');
  await Promise.all([p.waitForURL(u=>!u.pathname.startsWith('/login')),p.getByRole('button',{name:'Đăng nhập',exact:true}).click()]);return {ctx,p};
 }
 async function mail(code){
  for(let i=0;i<12;i++){
   const r=await page.context().request.get('http://127.0.0.1:8025/api/v1/search?query='+encodeURIComponent(code));
   const data=await r.json();if(data.messages?.length){const message=await page.context().request.get('http://127.0.0.1:8025/api/v1/message/'+data.messages[0].ID+'/raw');return message.text();}
   await new Promise(resolve=>setTimeout(resolve,500));
  }throw new Error('Confirmation did not reach Mailpit');
 }
 try {
  const {ctx,p}=await login('patient');
  await p.goto(origin+'/');await p.keyboard.press('Tab');await p.keyboard.press('Enter');
  record('skip-link-focus',await p.evaluate(()=>document.activeElement.id==='main-content'));
  await p.goto(origin+'/doctors');await p.locator('#keyword').fill('Nguyễn Văn Hùng');
  await Promise.all([p.waitForURL(u=>u.pathname==='/doctors' && u.search.includes('keyword=')),p.locator('form[aria-label="Bộ lọc bác sĩ"] button[type=submit]').click()]);
  record('doctor-search-filter',await p.locator('main,#main-content').innerText().then(t=>t.includes('Nguyễn Văn Hùng')&&!t.includes('Trần Thị Mai')));
  await p.goto(origin+'/patient/booking?doctorId=1');
  await p.evaluate(()=>{localStorage.setItem('medclinic-theme','dark');});await p.reload();
  record('theme-persistence',await p.locator('html').getAttribute('data-bs-theme')==='dark');
  const tomorrow=new Date(Date.now()+86400000).toLocaleDateString('en-CA',{timeZone:'Asia/Ho_Chi_Minh'});
  await p.route('**/api/public/doctors/*/slots?*',route=>route.abort('failed'));
  await p.locator('#appointmentDate').fill(tomorrow);
  await p.locator('#slotStateError').waitFor({state:'visible'});
  record('slot-fetch-error-visible',await p.locator('#submitBookingBtn').isDisabled());
  await p.unroute('**/api/public/doctors/*/slots?*');await p.locator('#retrySlots').click();
  await p.locator('#slotGrid .slot-btn:not([disabled])').first().waitFor();
  record('slot-retry-recovers',await p.locator('#slotStateError').isHidden());
  const contrast=await p.locator('#slotGrid .slot-btn:not([disabled])').first().evaluate(e=>{
   const rgb=s=>s.match(/[\d.]+/g).slice(0,3).map(Number);
   const luminance=c=>c.map(v=>{v/=255;return v<=0.04045?v/12.92:Math.pow((v+0.055)/1.055,2.4)}).reduce((a,v,i)=>a+v*[0.2126,0.7152,0.0722][i],0);
   const style=getComputedStyle(e),a=luminance(rgb(style.color)),b=luminance(rgb(style.backgroundColor));
   return {ratio:(Math.max(a,b)+0.05)/(Math.min(a,b)+0.05),height:e.getBoundingClientRect().height,color:style.color,background:style.backgroundColor};
  });record('dark-slot-text-contrast',contrast.ratio>=4.5,contrast);record('slot-touch-height',contrast.height>=44,{height:contrast.height});
  await p.screenshot({path:'output/playwright-check/artifacts/fixture-booking-mobile-dark.png',fullPage:true});
  await p.locator('#slotGrid .slot-btn:not([disabled])').first().click();await p.locator('#reason').fill('DEMO Playwright CLI cancellation');
  record('slot-selection-and-summary',await p.locator('#selectedSlotId').inputValue()!=='' && await p.locator('#slotGrid [aria-pressed=true]').count()===1);
  await Promise.all([p.waitForURL(/\/patient\/appointments\/\d+/),p.locator('#submitBookingBtn').click()]);
  const appointmentURL=p.url(),code=(await p.locator('body').innerText()).match(/MED-\d{6}-[A-Z0-9]+/)?.[0];assert(code,'Missing booking code');
  record('booking-detail-QR',await p.locator('img[src^="data:image/png"]').count()>0);
  const email=await mail(code);record('booking-Mailpit-inline-QR',/Content-ID: <booking-qr>/i.test(email)&&/Content-Type: image\/png/i.test(email));
  await p.emulateMedia({media:'print'});record('print-ticket-visible',await p.locator('#printTicketSection').isVisible());await p.emulateMedia({media:'screen'});
  await p.goto(origin+'/patient/appointments');await p.getByRole('button',{name:'Hủy lịch',exact:true}).first().click();
  await p.getByRole('button',{name:'Giữ lại lịch',exact:true}).click();await p.locator('.swal2-container').waitFor({state:'hidden'});
  await p.goto(appointmentURL);record('cancel-dialog-dismiss-preserves-booking',(await p.locator('body').innerText()).includes('Đã xác nhận'));
  await p.goto(origin+'/patient/appointments');
  await p.getByRole('button',{name:'Hủy lịch',exact:true}).click();
  await Promise.all([p.waitForNavigation(),p.getByRole('button',{name:'Đồng ý hủy',exact:true}).click()]);
  await p.goto(appointmentURL);record('cancel-booking-persisted',(await p.locator('body').innerText()).includes('Đã hủy'));
  // Defense demo: a future booking can be received immediately in dev/demo.
  await p.goto(origin+'/patient/booking?doctorId=1');await p.locator('#appointmentDate').fill(tomorrow);
  await p.locator('#slotGrid .slot-btn:not([disabled])').first().waitFor();await p.locator('#slotGrid .slot-btn:not([disabled])').first().click();
  await p.locator('#reason').fill('DEMO Playwright CLI future-date boundary');
  await Promise.all([p.waitForURL(/\/patient\/appointments\/\d+/),p.locator('#submitBookingBtn').click()]);
  const futureURL=p.url(),id=futureURL.match(/\/(\d+)$/)[1];
  const {ctx:staffCtx,p:staff}=await login('staff');
  const csrf=await staff.locator('input[name=_csrf]').first().inputValue();
  const response=await staffCtx.request.post(origin+'/staff/reception/check-in/'+id,{form:{_csrf:csrf},headers:{Referer:origin+'/staff/reception'},maxRedirects:0});
  await staff.goto(origin+'/staff/reception');
  const received=(await staff.locator('body').innerText()).includes('Đã check-in thành công');
  await p.reload();const accepted=(await p.locator('body').innerText()).includes('Đã tiếp nhận (Chờ khám)');
  record('demo-future-date-check-in',response.status()===302 && received && accepted,{appointmentDate:tomorrow,httpStatus:response.status(),futureCheckInAccepted:accepted});
  await p.screenshot({path:'output/playwright-check/artifacts/future-check-in-boundary.png',fullPage:true});
  // Browser offline behavior and cache privacy on an authenticated route.
  await p.goto(origin+'/patient/appointments');
  await p.evaluate(()=>navigator.serviceWorker.ready);
  await p.waitForFunction(()=>!!navigator.serviceWorker.controller);
  await ctx.setOffline(true);await p.goto(origin+'/patient/medical-history');
  record('authenticated-navigation-offline-fallback',(await p.locator('body').innerText()).includes('Chưa có kết nối mạng'),{title:await p.title()});
  await p.screenshot({path:'output/playwright-check/artifacts/fixture-offline.png',fullPage:true});
  await ctx.setOffline(false);
  const cached=await p.evaluate(async()=>{const urls=[];for(const name of await caches.keys()){const cache=await caches.open(name);for(const req of await cache.keys())urls.push(new URL(req.url).pathname);}return urls;});
  record('no-authenticated-HTML-or-API-in-cache',!cached.some(path=>/^\/(patient|staff|doctor|admin|auth|api)\//.test(path)),{cachedPaths:cached});
  await p.goto(origin+'/patient/appointments');record('offline-online-recovery',!(await p.locator('body').innerText()).includes('Mất kết nối'));
  report.fixtureData={appointmentsCreated:2,appointmentsCancelled:1,invoicesCreated:0};
 }catch(e){report.fatalError=e.message;}
 finally{for(const ctx of contexts)await ctx.close();}
 report.passed=!report.fatalError && report.checks.every(c=>c.passed) && !report.pageErrors.length;
 return report;
}
