$ErrorActionPreference = 'Continue'
Write-Output '--- node ---'
node -v
Write-Output '--- npm ---'
npm -v
Write-Output '--- git ---'
git --version
Write-Output '--- java no PATH ---'
cmd /c 'where java 2>nul'
$jdk = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.jdks') -Filter 'java.exe' -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jdk) { Write-Output ('JDK local: ' + $jdk.FullName) } else { Write-Output 'JDK local: ausente' }
Write-Output '--- docker ---'
cmd /c 'where docker 2>nul'
Write-Output '--- psql ---'
cmd /c 'where psql 2>nul'
