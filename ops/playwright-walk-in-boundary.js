async page => {
 const origin='http://127.0.0.1:18090',context=await page.context().browser().newContext({viewport:{width:1440,height:900},timezoneId:'Asia/Ho_Chi_Minh'}),p=await context.newPage();const report={checks:[],fixture:true,realPayments:0};
 const check=(test,passed,extra={})=>report.checks.push({test,passed,...extra});
 try{
  await p.goto(origin+'/login');await p.getByLabel('Tên đăng nhập hoặc email').fill('staff');await p.getByLabel('Mật khẩu',{exact:true}).fill('123456');
  await Promise.all([p.waitForURL('**/staff/reception'),p.getByRole('button',{name:'Đăng nhập',exact:true}).click()]);
  await p.goto(origin+'/staff/walk-in');
  const dateControl=p.locator('#appointmentDate'),today=await dateControl.inputValue();
  check('walk-in-date-bounds-use-server-today',today===await dateControl.getAttribute('min') && today===await dateControl.getAttribute('max'));
  const tomorrow=new Date(Date.now()+86400000).toLocaleDateString('en-CA',{timeZone:'Asia/Ho_Chi_Minh'});
  const yesterday=new Date(Date.now()-86400000).toLocaleDateString('en-CA',{timeZone:'Asia/Ho_Chi_Minh'});
  for(const [name,date] of [['future',tomorrow],['past',yesterday]]){
   await dateControl.fill(date);await dateControl.dispatchEvent('change');
   check('walk-in-'+name+'-date-invalid',await dateControl.evaluate(e=>!e.checkValidity()) && await p.locator('#btnSubmitWalkIn').isDisabled());
  }
  await dateControl.fill(today);
  const csrf=await p.locator('input[name=_csrf]').first().inputValue();
  const slotsResponse=await context.request.get(origin+'/api/public/doctors/1/slots?date='+tomorrow);
  if(slotsResponse.status()!==200)throw new Error('Could not load future fixture slots');
  const slots=await slotsResponse.json(),slot=slots.find(s=>s.status==='AVAILABLE');
  if(!slot)throw new Error('No available future fixture slot');
  const response=await context.request.post(origin+'/staff/walk-in',{form:{_csrf:csrf,fullName:'DEMO PLAYWRIGHT WALKIN',phone:'0900000020',doctorId:'1',date:tomorrow,slotId:String(slot.id),reason:'DEMO future walk-in boundary'},headers:{Referer:origin+'/staff/walk-in'},maxRedirects:0});
  await p.goto(origin+'/staff/walk-in');const text=await p.locator('body').innerText();
  check('reject-forged-future-walk-in',response.status()===302 && new URL(response.headers().location,origin).pathname==='/staff/walk-in' &&
    text.includes('Chỉ tiếp nhận và cấp số thứ tự cho lịch khám hôm nay') && !text.includes('Đã đăng ký khám trực tiếp thành công'),{httpStatus:response.status()});
  const after=await context.request.get(origin+'/api/public/doctors/1/slots?date='+tomorrow);
  check('rejected-walk-in-leaves-slot-available',(await after.json()).some(s=>s.id===slot.id && s.status==='AVAILABLE'));
  await p.screenshot({path:'output/playwright-check/artifacts/walk-in-date-rejected.png',fullPage:true});
 }catch(e){report.fatalError=e.message;}
 finally{await context.close();}
 report.passed=!report.fatalError&&report.checks.every(c=>c.passed);return report;
}
