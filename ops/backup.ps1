[CmdletBinding()]
param(
    [string]$OutputDirectory = (Join-Path $PSScriptRoot '..\backups')
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$backupName = "medclinic_$stamp.bak"
$containerPath = "/var/opt/mssql/backup/$backupName"
$destination = Join-Path ([System.IO.Path]::GetFullPath($OutputDirectory)) $stamp
New-Item -ItemType Directory -Path $destination -Force | Out-Null

Push-Location $projectRoot
try {
    & docker compose exec -T sqlserver /bin/bash /docker/backup-db.sh $containerPath
    if ($LASTEXITCODE -ne 0) { throw 'SQL Server backup failed.' }

    & docker cp "medclinic-sqlserver:${containerPath}" (Join-Path $destination $backupName)
    if ($LASTEXITCODE -ne 0) { throw 'Could not copy database backup from the container.' }

    $uploads = Join-Path $destination 'uploads'
    & docker cp 'medclinic-app:/app/uploads' $uploads
    if ($LASTEXITCODE -ne 0) { throw 'Could not copy uploaded content from the app container.' }

    & docker compose ps --format json | Set-Content -LiteralPath (Join-Path $destination 'compose-state.json') -Encoding utf8
    Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $destination $backupName) |
        Select-Object Algorithm, Hash, Path |
        ConvertTo-Json | Set-Content -LiteralPath (Join-Path $destination 'database.sha256.json') -Encoding utf8

    Write-Host "Backup verified and saved to $destination"
} finally {
    Pop-Location
}
