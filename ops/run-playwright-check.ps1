[CmdletBinding()]
param([switch]$WithFixture, [switch]$ResetFixture)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
Push-Location $repoRoot
$sessionName = 'medclinic-project-check'
$fixtureStarted = $false
$browserStarted = $false
$results = @()
try {
    New-Item -ItemType Directory -Path output/playwright-check/artifacts -Force | Out-Null
    $cli = Join-Path $repoRoot 'output/playwright-tools/node_modules/.bin/playwright-cli.cmd'
    if (-not (Test-Path -LiteralPath $cli)) {
        & npm.cmd install --prefix output/playwright-tools --no-audit --no-fund --save-exact '@playwright/cli@0.1.22'
        if ($LASTEXITCODE -ne 0) { throw 'Could not install official Playwright CLI.' }
    }
    $cliVersion = & $cli --version
    if ($cliVersion -ne '0.1.22') { throw 'Expected Playwright CLI 0.1.22; install the pinned version in output/playwright-tools.' }
    $health = Invoke-RestMethod http://127.0.0.1:8080/actuator/health -TimeoutSec 5
    if ($health.status -ne 'UP') { throw 'Start the project Docker Compose demo at port 8080 first.' }
    $config = @{
        browser = @{ browserName='chromium'; isolated=$true; launchOptions=@{channel='msedge';headless=$true};
            contextOptions=@{viewport=@{width=1440;height=900};locale='vi-VN';timezoneId='Asia/Ho_Chi_Minh'} }
        outputDir = (Join-Path $repoRoot 'output/playwright-check/artifacts'); outputMode='file'
        timeouts=@{action=7000;navigation=20000}
    }
    $config | ConvertTo-Json -Depth 8 | Set-Content output/playwright-check/config.json -Encoding utf8
    & $cli "-s=$sessionName" open http://127.0.0.1:8080 --config=output/playwright-check/config.json
    if ($LASTEXITCODE -ne 0) { throw 'Could not open isolated Edge headless session.' }
    $browserStarted = $true
    $matrix = Get-Content ops/playwright-page-matrix.js -Raw
    foreach ($role in @('public','patient','staff','doctor','admin')) {
        $scriptFile = "output/playwright-check/$role.js"
        [IO.File]::WriteAllText((Join-Path $repoRoot $scriptFile), $matrix.Replace('__ROLE__',$role), [Text.UTF8Encoding]::new($false))
        & $cli "-s=$sessionName" --raw run-code "--filename=$scriptFile" *> "output/playwright-check/$role-result.json"
        if ($LASTEXITCODE -ne 0) { throw "CLI failed for $role; inspect the ignored raw result." }
        $result = Get-Content "output/playwright-check/$role-result.json" -Raw | ConvertFrom-Json
        $results += $result
        Write-Output "$role`: $($result.checks.Count) checks; passed=$($result.passed)"
    }
    if ($WithFixture) {
        & ./ops/start-academic-fixture.ps1 -Reset:$ResetFixture
        $fixtureStarted = $true
        foreach ($group in @(
            @{name='interactions';file='ops/playwright-fixture-interactions.js'},
            @{name='clinical';file='ops/playwright-clinical-boundary.js'},
            @{name='walk-in';file='ops/playwright-walk-in-boundary.js'}
        )) {
            & $cli "-s=$sessionName" --raw run-code "--filename=$($group.file)" *> "output/playwright-check/$($group.name)-result.json"
            if ($LASTEXITCODE -ne 0) { throw "CLI failed for $($group.name); inspect the ignored raw result." }
            $result = Get-Content "output/playwright-check/$($group.name)-result.json" -Raw | ConvertFrom-Json
            $results += $result
            Write-Output "$($group.name): $($result.checks.Count) checks; passed=$($result.passed)"
        }
    }
    $checks = @($results | ForEach-Object { $_.checks })
    $summary = [ordered]@{ cliVersion=$cliVersion; withFixture=[bool]$WithFixture; checks=$checks.Count;
        passed=@($checks | Where-Object passed -eq $true).Count; failed=@($checks | Where-Object passed -ne $true).Count;
        failedGroups=@($results | Where-Object passed -ne $true).Count; realPayments=0 }
    $summary | ConvertTo-Json -Depth 8 | Set-Content output/playwright-check/summary.json -Encoding utf8
    $summary | ConvertTo-Json
    if ($summary.failedGroups -gt 0) { throw 'Project checks found failures; inspect JSON and screenshots. No application source was changed.' }
} finally {
    if ($browserStarted) { & $cli "-s=$sessionName" close | Out-Null }
    if ($fixtureStarted) {
        $fixtureLabel = & docker inspect medclinic-academic-fixture --format '{{index .Config.Labels "medclinic.academic-fixture"}}'
        if ($LASTEXITCODE -eq 0 -and $fixtureLabel -eq 'disposable-h2') {
            & docker stop medclinic-academic-fixture | Out-Null
            & docker rm medclinic-academic-fixture | Out-Null
        } else { Write-Warning 'Fixture identity changed; cleanup skipped.' }
    }
    Pop-Location
}
