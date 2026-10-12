async page => {
 const origin='http://127.0.0.1:18090',browser=page.context().browser(),contexts=[],report={checks:[],fixture:true,clinicalRecordsSaved:0,invoicesCreated:0,realPayments:0};
 const check=(test,passed,extra={})=>{report.checks.push({test,passed,...extra});if(!passed)throw new Error(test);};
 async function login(role){const ctx=await browser.newContext({viewport:{width:390,height:844},locale:'vi-VN',timezoneId:'Asia/Ho_Chi_Minh'});contexts.push(ctx);const p=await ctx.newPage();p.setDefaultTimeout(8000);
  await p.goto(origin+'/login');await p.getByLabel('Tên đăng nhập hoặc email').fill(role);await p.getByLabel('Mật khẩu',{exact:true}).fill('123456');
  await Promise.all([p.waitForURL(u=>!u.pathname.startsWith('/login')),p.getByRole('button',{name:'Đăng nhập',exact:true}).click()]);return {ctx,p};}
 try{
  const {p:patient}=await login('patient');await patient.goto(origin+'/patient/appointments');
  const card=patient.locator('.mc-appointment-card').filter({hasText:'DEMO Playwright CLI future-date boundary'});
  const href=await card.locator('a[href^="/patient/appointments/"]').getAttribute('href'),id=href.match(/\/(\d+)$/)[1];
  const {ctx,p}=await login('doctor');await p.goto(origin+'/doctor/desk');
  check('demo-future-booking-visible-in-doctor-queue',await p.locator('form[action="/doctor/start-examination/'+id+'"]').count()===1);
  const csrf=await p.locator('input[name=_csrf]').first().inputValue();
  const response=await ctx.request.post(origin+'/doctor/start-examination/'+id,{form:{_csrf:csrf},maxRedirects:0});
  check('demo-future-date-start-examination',response.status()===302 && new URL(response.headers().location,origin).pathname==='/doctor/examine/'+id);
  await p.goto(origin+'/doctor/examine/'+id);
  check('demo-future-examination-form',await p.locator('#examinationForm').count()===1);
  const formCsrf=await p.locator('#examinationForm input[name=_csrf]').inputValue();
  await p.locator('#btnAddMedicine').click();
  const medicineId=await p.locator('select[name=medicineIds] option').filter({hasText:'Paracetamol'}).first().getAttribute('value');
  check('demo-prescription-medicine-available',!!medicineId);
  const longNotes=('Hồ sơ minh họa cho đồ án Lập trình Web. Các thông tin này dùng để kiểm tra cách xuống dòng của bệnh án trên màn hình lớn và điện thoại. Dữ liệu thử được tạo riêng, không phải hồ sơ của bệnh nhân thật.\n').repeat(3);
  const save=await ctx.request.post(origin+'/doctor/examine/'+id,{form:{_csrf:formCsrf,symptoms:'DEMO triệu chứng',diagnosis:'DEMO chẩn đoán',doctorNotes:longNotes,medicineIds:medicineId,quantities:'2',dosages:'1 viên / lần',frequencies:'2 lần / ngày',instructions:'Uống sau ăn'},maxRedirects:0});
  check('demo-save-future-clinical-record',save.status()===302 && new URL(save.headers().location,origin).pathname==='/doctor/desk');
  await patient.goto(origin+href);
  check('demo-future-appointment-completed',(await patient.locator('body').innerText()).includes('Đã hoàn tất'));
  report.clinicalRecordsSaved=1;
  await p.goto(origin+'/doctor/records');
  check('doctor-completed-record-navigation',await p.locator('a[href="/doctor/records/'+id+'"]').count()===1);
  await p.locator('a[href="/doctor/records/'+id+'"]').click();
  check('doctor-completed-record-read-only',await p.getByText('DEMO chẩn đoán',{exact:true}).isVisible()
   && await p.locator('#examinationForm').count()===0);
  check('doctor-record-prescription-snapshot',await p.getByText('Paracetamol 500mg (Hasan)',{exact:true}).isVisible()
   && await p.getByText('Uống sau ăn',{exact:true}).isVisible());
  const pdf=await ctx.request.get(origin+'/export/prescription/'+id);
  check('doctor-completed-record-pdf',pdf.status()===200 && /application\/pdf/.test(pdf.headers()['content-type']));
  const main=p.locator('#main-content');
  for(const theme of ['light','dark']){
   await p.evaluate(value=>localStorage.setItem('medclinic-theme',value),theme);
   for(const width of [1440,390]){
    await p.setViewportSize({width,height:900});await p.reload();
    check('doctor-record-detail-'+theme+'-'+width,await main.getByRole('heading',{name:'Bệnh án đã hoàn tất',exact:true}).isVisible()
     && await p.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth));
    await p.screenshot({path:'output/playwright-check/artifacts/doctor-record-detail-'+theme+'-'+width+'.png',fullPage:true});
    await p.goto(origin+'/doctor/records');
    check('doctor-record-list-'+theme+'-'+width,await p.locator('a[href="/doctor/records/'+id+'"]').isVisible()
     && await p.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth));
    await p.screenshot({path:'output/playwright-check/artifacts/doctor-records-'+theme+'-'+width+'.png',fullPage:true});
    await p.goto(origin+'/doctor/records/'+id);
   }
  }
  await p.goto(origin+'/doctor/desk');
  await p.screenshot({path:'output/playwright-check/artifacts/fixture-clinical-boundary.png',fullPage:true});
  const {ctx:staffCtx,p:staff}=await login('staff');await staff.goto(origin+'/staff/billing');
  const payment=staff.locator('form[action^="/staff/billing/pay/"]').first();
  check('demo-clinical-workflow-creates-invoice',await payment.count()===1);report.invoicesCreated=1;
  const paymentAction=await payment.getAttribute('action'),paymentCsrf=await payment.locator('input[name=_csrf]').inputValue();
  const paid=await staffCtx.request.post(origin+paymentAction,{form:{_csrf:paymentCsrf,method:'CASH'},maxRedirects:0});
  await staff.goto(origin+'/staff/billing?status=PAID');
  check('demo-fixture-cash-payment',paid.status()===302 && (await staff.locator('body').innerText()).includes('Đã thanh toán'));
  const pdfHref=await staff.locator('a[href^="/export/invoice/"]').first().getAttribute('href');
  const invoicePdf=await staffCtx.request.get(origin+pdfHref),bytes=await invoicePdf.body();
  check('demo-paid-invoice-pdf',invoicePdf.status()===200 && /application\/pdf/.test(invoicePdf.headers()['content-type']) && bytes.subarray(0,5).toString()==='%PDF-');
 }catch(e){report.fatalError=e.message;}
 finally{for(const ctx of contexts)await ctx.close();}
 report.passed=!report.fatalError && report.checks.every(c=>c.passed);return report;
}
