$ErrorActionPreference = 'Stop'
$root = Join-Path $env:USERPROFILE '.jdks'
$java = Get-ChildItem -Path $root -Filter 'java.exe' -Recurse -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match '\\bin\\java.exe$' } |
    Select-Object -First 1
if (-not $java) {
    Write-Error "JDK 21 nao encontrado em $root. Baixe o Temurin 21 antes de compilar."
    exit 1
}
$env:JAVA_HOME = $java.Directory.Parent.FullName
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
Write-Output "JAVA_HOME=$env:JAVA_HOME"
