[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,
    [string]$SqlServerContainer = 'medclinic-sqlserver'
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$resolvedBackup = (Resolve-Path -LiteralPath $BackupFile).Path
if ([System.IO.Path]::GetExtension($resolvedBackup) -ne '.bak') {
    throw 'BackupFile must be a .bak file.'
}

$stamp = Get-Date -Format 'yyyyMMddHHmmss'
$containerName = "medclinic_$($stamp.Substring(0,8))_$($stamp.Substring(8,6)).bak"
$containerPath = "/var/opt/mssql/backup/$containerName"
$drillDatabase = "medclinic_restore_drill_$stamp"

Push-Location $projectRoot
try {
    & docker cp $resolvedBackup "${SqlServerContainer}:${containerPath}"
    if ($LASTEXITCODE -ne 0) { throw 'Could not copy backup into SQL Server container.' }

    & docker exec $SqlServerContainer /bin/bash /docker/restore-drill-db.sh $containerPath $drillDatabase
    if ($LASTEXITCODE -ne 0) { throw 'Restore drill failed.' }

    Write-Host "Restore drill passed. Temporary database $drillDatabase was checked and removed."
} finally {
    Pop-Location
}
