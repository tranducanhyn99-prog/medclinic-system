[CmdletBinding()]
param([switch]$Reset)
$ErrorActionPreference = 'Stop'
$fixtureName = 'medclinic-academic-fixture'
$ownedLabel = 'medclinic.academic-fixture'
$existing = & docker ps -a --filter "name=^/$fixtureName$" --format '{{.Names}}'
if ($LASTEXITCODE -ne 0) { throw 'Docker is unavailable.' }
if ($existing) {
    $owner = & docker inspect $fixtureName --format '{{index .Config.Labels "medclinic.academic-fixture"}}'
    if ($owner -ne 'disposable-h2') { throw 'Existing container is not a labelled academic fixture; refusing to replace it.' }
    if (-not $Reset) { throw 'Fixture already exists. Use -Reset to explicitly discard its in-memory test data.' }
    & docker stop $fixtureName | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Could not stop fixture.' }
    & docker rm $fixtureName | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Could not remove fixture.' }
}
$mailImage = & docker inspect medclinic-mailpit --format '{{.Config.Image}}'
if ($LASTEXITCODE -ne 0 -or $mailImage -notlike 'axllent/mailpit:*') { throw 'Start the demo Mailpit sink with docker compose up -d first.' }
$networks = (& docker inspect medclinic-mailpit --format '{{json .NetworkSettings.Networks}}' | ConvertFrom-Json)
$fixtureNetwork = $networks.PSObject.Properties.Name | Select-Object -First 1
if (-not $fixtureNetwork) { throw 'Mailpit has no Docker network.' }
$fixtureArgs = @('run', '-d', '--name', $fixtureName, '--label', "$ownedLabel=disposable-h2",
    '--network', $fixtureNetwork, '-p', '127.0.0.1:18090:8080',
    '-e', 'SPRING_PROFILES_ACTIVE=demo',
    '-e', 'SPRING_DATASOURCE_URL=jdbc:h2:mem:academic;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
    '-e', 'SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver', '-e', 'SPRING_DATASOURCE_USERNAME=sa',
    '-e', 'SPRING_DATASOURCE_PASSWORD=', '-e', 'SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop',
    '-e', 'SPRING_FLYWAY_ENABLED=false', '-e', 'REMEMBER_ME_KEY=academic-disposable-fixture-only-20261003',
    '-e', 'SPRING_MAIL_HOST=medclinic-mailpit', '-e', 'SPRING_MAIL_PORT=1025',
    '-e', 'SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=false', '-e', 'SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=false',
    '-e', 'MAIL_FROM=no-reply@medclinic.test', '-e', 'APP_WEBSITE=http://127.0.0.1:18090',
    '-e', 'BANK_PAYMENT_ENABLED=false', '-e', 'TZ=Asia/Ho_Chi_Minh', 'medclinic-system:local')
& docker @fixtureArgs | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Could not start fixture; check image and port 18090.' }
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    try {
        $health = Invoke-RestMethod 'http://127.0.0.1:18090/actuator/health' -TimeoutSec 2
        if ($health.status -eq 'UP') {
            Write-Output 'Disposable H2 fixture ready at http://127.0.0.1:18090; Mailpit sink at http://127.0.0.1:8025.'
            return
        }
    } catch { }
    Start-Sleep -Seconds 1
}
throw 'Fixture startup timed out; inspect docker logs medclinic-academic-fixture.'
