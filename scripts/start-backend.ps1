$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'java-home.ps1')
& (Join-Path $PSScriptRoot 'start-postgres.ps1')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Set-Location (Join-Path $PSScriptRoot '..\backend')
if (Test-Path '..\.env') {
    Get-Content '..\.env' | ForEach-Object {
        if ($_ -match '^\s*#' -or $_ -match '^\s*$') { return }
        $pair = $_.Split('=', 2)
        if ($pair.Length -eq 2) { Set-Item -Path ("Env:" + $pair[0].Trim()) -Value $pair[1].Trim() }
    }
}
& .\mvnw.cmd spring-boot:run
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
