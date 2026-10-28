$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..\frontend')
npm start
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
