async page => {
    const origin = 'http://127.0.0.1:8080';
    // The CLI wrapper replaces this value for each isolated role context.
    const role = '__ROLE__';
    const routes = {
        public: ['/', '/doctors', '/doctors/1', '/login', '/register', '/forgot-password'],
        patient: ['/patient/booking?doctorId=1', '/patient/appointments', '/patient/medical-history', '/profile'],
        staff: ['/staff/reception', '/staff/walk-in', '/staff/billing', '/profile'],
        doctor: ['/doctor/desk', '/doctor/schedule', '/doctor/records', '/profile'],
        admin: ['/admin/dashboard', '/admin/users', '/admin/doctors', '/admin/specialties', '/admin/medicines', '/profile']
    };
    const browser = page.context().browser();
    const context = await browser.newContext({viewport:{width:1440,height:900},locale:'vi-VN',timezoneId:'Asia/Ho_Chi_Minh'});
    await context.addInitScript(()=>localStorage.removeItem('medclinic-theme'));
    const checkPage = await context.newPage();
    checkPage.setDefaultTimeout(7000);
    const result = {role, origin, browser:browser.version(), checks:[], pageErrors:[], consoleErrors:[], httpErrors:[], requestFailures:[], externalRequests:[]};
    const clean = url => {try {const u = new URL(url); return u.origin + u.pathname;} catch {return 'invalid-url';}};
    let collecting = true;
    checkPage.on('pageerror', e => result.pageErrors.push({route:clean(checkPage.url()),message:e.message}));
    checkPage.on('console', m => {if(collecting && m.type()==='error') result.consoleErrors.push({route:clean(checkPage.url()),message:m.text()});});
    checkPage.on('response', r => {if(collecting && r.status()>=400) result.httpErrors.push({url:clean(r.url()),status:r.status()});});
    checkPage.on('requestfailed', r => {if(collecting && r.failure()?.errorText!=='net::ERR_ABORTED') result.requestFailures.push({url:clean(r.url()),error:r.failure()?.errorText});});
    checkPage.on('request', r => {if(!r.url().startsWith(origin) && !r.url().startsWith('data:')) result.externalRequests.push(clean(r.url()));});
    try {
        if(role!=='public') {
            await checkPage.goto(origin+'/login');
            await checkPage.getByLabel('Tên đăng nhập hoặc email').fill(role);
            await checkPage.getByLabel('Mật khẩu',{exact:true}).fill('123456');
            await Promise.all([checkPage.waitForURL(url=>!url.pathname.startsWith('/login') && !url.pathname.startsWith('/auth/otp')),checkPage.getByRole('button',{name:'Đăng nhập',exact:true}).click()]);
            result.loginPassed=true;
        }
        for(const route of routes[role]) {
            for(const mode of [
                {name:'desktop-light',width:1440,height:900,theme:'light'},
                {name:'desktop-dark',width:1440,height:900,theme:'dark'},
                {name:'tablet-light',width:768,height:1024,theme:'light'},
                {name:'mobile-light',width:390,height:844,theme:'light'},
                {name:'mobile-dark',width:390,height:844,theme:'dark'}
            ]) {
                await checkPage.setViewportSize({width:mode.width,height:mode.height});
                await checkPage.emulateMedia({colorScheme:mode.theme});
                const response = await checkPage.goto(origin+route,{waitUntil:'load'});
                await checkPage.evaluate(()=>document.fonts.ready);
                const metrics = await checkPage.evaluate(()=>{
                    const visible = e=>e.getClientRects().length>0 && getComputedStyle(e).visibility!=='hidden';
                    const main=document.querySelector('main,#main-content');
                    const controls=[...document.querySelectorAll('input:not([type=hidden]),select,textarea')].filter(visible);
                    const missingLabels=controls.filter(e=>!e.labels?.length && !e.getAttribute('aria-label') && !e.getAttribute('aria-labelledby') && !e.getAttribute('title')).map(e=>e.id||e.name||e.type);
                    return {title:document.title,headings:[...document.querySelectorAll('h1')].filter(visible).length,main:!!main,
                        overflow:document.documentElement.scrollWidth>innerWidth+1,bodyFont:getComputedStyle(document.body).fontFamily,
                        brokenVisibleImages:[...document.images].filter(e=>visible(e)&&e.complete&&!e.naturalWidth).map(e=>e.getAttribute('src')),
                        missingLabels,theme:document.documentElement.getAttribute('data-bs-theme'),
                        favicon:document.querySelector('link[rel="icon"]')?.getAttribute('href')};
                });
                const problems=[];
                if(response.status()!==200) problems.push('HTTP '+response.status());
                if(new URL(checkPage.url()).pathname==='/login' && !route.startsWith('/login')) problems.push('unexpected-login');
                if(metrics.overflow) problems.push('horizontal-page-overflow');
                if(!metrics.main || metrics.headings!==1) problems.push('main-or-H1-count');
                if(metrics.brokenVisibleImages.length) problems.push('broken-images');
                if(metrics.missingLabels.length) problems.push('unlabelled-inputs');
                if(metrics.theme!==mode.theme) problems.push('theme-did-not-apply');
                if(metrics.favicon!=='/images/logo.svg') problems.push('missing-project-favicon');
                if(mode.name==='mobile-dark' || mode.name==='desktop-light' || problems.length) {
                    const slug=route.split('?')[0].replace(/\//g,'_')||'home';
                    await checkPage.screenshot({path:'output/playwright-check/artifacts/'+role+slug+'-'+mode.name+'.png',fullPage:true});
                }
                result.checks.push({route,mode:mode.name,status:response.status(),...metrics,problems,passed:!problems.length});
            }
        }
        collecting=false;
        if(role==='public') {
            const favicon = await context.request.get(origin+'/favicon.ico');
            const faviconBytes = await favicon.body();
            result.checks.push({test:'anonymous-favicon',status:favicon.status(),contentType:favicon.headers()['content-type'],
                passed:favicon.status()===200 && favicon.headers()['content-type']==='image/svg+xml' && faviconBytes.toString().includes('<svg ')});
            for(const protectedRoute of ['/admin/dashboard','/staff/billing','/doctor/desk','/patient/medical-history']) {
                await checkPage.goto(origin+protectedRoute);
                result.checks.push({test:'anonymous-route-protection',route:protectedRoute,passed:new URL(checkPage.url()).pathname==='/login'});
            }
            await checkPage.goto(origin+'/');
            await checkPage.keyboard.press('Tab');
            result.checks.push({test:'keyboard-skip-link-first',passed:await checkPage.evaluate(()=>document.activeElement?.textContent.includes('Bỏ qua'))});
            await checkPage.keyboard.press('Enter');
            result.checks.push({test:'keyboard-skip-link-target',passed:await checkPage.evaluate(()=>document.activeElement?.id==='main-content')});
            await checkPage.goto(origin+'/register');
            await checkPage.locator('.mc-auth-form button[type=submit]').click();
            const invalid = await checkPage.locator('.mc-auth-form').evaluate(form=>!form.checkValidity());
            result.checks.push({test:'empty-registration-validation',passed:new URL(checkPage.url()).pathname==='/register'
                && (invalid || await checkPage.locator('.is-invalid').count()>0)});
        } else {
            const denied = role==='admin'?null:'/admin/dashboard';
            if(denied) {
                const response=await checkPage.goto(origin+denied);
                result.checks.push({test:'cross-role-admin-denied',passed:response.status()===403,status:response.status()});
            }
            if(role==='doctor') {
                await checkPage.goto(origin+'/doctor/desk');
                try {await checkPage.locator('#doctorQueueRealtimeStatus').filter({hasText:'Đang nhận cập nhật'}).waitFor({timeout:10000}); result.checks.push({test:'doctor-websocket-connected',passed:true});}
                catch {result.checks.push({test:'doctor-websocket-connected',passed:false,text:await checkPage.locator('#doctorQueueRealtimeStatus').innerText()});}
            }
        }
    } catch(e) { result.fatalError=e.message; }
    finally {await context.close();}
    result.externalRequests=[...new Set(result.externalRequests)];
    result.passed=!result.fatalError && result.checks.every(c=>c.passed) && !result.pageErrors.length && !result.consoleErrors.length && !result.httpErrors.length && !result.requestFailures.length;
    return result;
}
