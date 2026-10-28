$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'java-home.ps1')
Set-Location (Join-Path $PSScriptRoot '..\backend')
& .\mvnw.cmd test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
