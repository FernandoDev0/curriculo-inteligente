$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..\frontend')
npm test -- --watch=false
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
