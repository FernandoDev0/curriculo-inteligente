# Instala as dependencias do frontend e do backend.
# Duplo clique: instalar.cmd
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

function Write-Step([string]$message) {
    Write-Output ""
    Write-Output $message
}

function Get-NodeVersionText {
    $command = Get-Command node -ErrorAction SilentlyContinue
    if (-not $command) {
        return $null
    }
    $previous = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $text = (& node -v 2>&1 | Out-String).Trim()
    $ErrorActionPreference = $previous
    if (-not $text) {
        return $null
    }
    return $text.TrimStart('v').Split()[0]
}

function Test-NodeSupported([string]$versionText) {
    $pieces = $versionText.Split('.')
    if ($pieces.Length -lt 2) {
        return $false
    }
    $major = 0
    $minor = 0
    $patch = 0
    [void][int]::TryParse($pieces[0], [ref]$major)
    [void][int]::TryParse($pieces[1], [ref]$minor)
    if ($pieces.Length -ge 3) {
        [void][int]::TryParse($pieces[2], [ref]$patch)
    }
    $current = New-Object System.Version $major, $minor, $patch
    if ($major -eq 22) {
        return $current -ge (New-Object System.Version 22, 22, 3)
    }
    if ($major -eq 24) {
        return $current -ge (New-Object System.Version 24, 15, 0)
    }
    if ($major -eq 26) {
        return $current -ge (New-Object System.Version 26, 0, 0)
    }
    return $false
}

function Find-Java21Home {
    $homes = New-Object System.Collections.Generic.List[string]
    $local = Join-Path $env:USERPROFILE '.jdks'
    if (Test-Path -LiteralPath $local) {
        $bundled = Get-ChildItem -LiteralPath $local -Filter 'java.exe' -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -match '\\bin\\java.exe$' }
        foreach ($item in $bundled) {
            $homes.Add($item.Directory.Parent.FullName)
        }
    }
    if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        $homes.Add($env:JAVA_HOME)
    }
    $onPath = Get-Command java -ErrorAction SilentlyContinue
    if ($onPath -and $onPath.Source) {
        $homes.Add((Split-Path -Parent (Split-Path -Parent $onPath.Source)))
    }

    $seen = @{}
    $wrong = $null
    foreach ($jdkHome in $homes) {
        if ($seen.ContainsKey($jdkHome)) {
            continue
        }
        $seen[$jdkHome] = $true
        $javaExe = Join-Path $jdkHome 'bin\java.exe'
        if (-not (Test-Path -LiteralPath $javaExe)) {
            continue
        }
        $previous = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        $banner = (& $javaExe -version 2>&1 | Out-String)
        $ErrorActionPreference = $previous
        if ($banner -match 'version "(21)[\."]') {
            return $jdkHome
        }
        if (-not $wrong -and $banner -match 'version "([^"]+)"') {
            $wrong = $Matches[1]
        }
    }
    return @{ Wrong = $wrong }
}

Write-Output "Instalacao das dependencias"
Write-Output "Pasta: $root"

$problems = New-Object System.Collections.Generic.List[string]
$nodeVersion = Get-NodeVersionText
if (-not $nodeVersion) {
    $problems.Add(@"
Node.js nao encontrado.
Instale o Node.js 24 LTS, versao 24.15.0 ou mais nova.
Este projeto tambem aceita Node.js 22.22.3 ou mais novo, ou Node.js 26.
Site: https://nodejs.org/
"@)
}
elseif (-not (Test-NodeSupported $nodeVersion)) {
    $problems.Add(@"
Node.js encontrado: v$nodeVersion
Essa versao nao serve para o Angular 22 deste projeto.
Instale o Node.js 24 LTS, versao 24.15.0 ou mais nova.
Alternativas aceitas: Node.js 22.22.3 ou mais novo, ou Node.js 26.
Site: https://nodejs.org/
"@)
}

$npm = Get-Command npm -ErrorAction SilentlyContinue
if ($nodeVersion -and -not $npm) {
    $problems.Add("O npm nao foi encontrado. Reinstale o Node.js 24 LTS, que ja inclui o npm. Site: https://nodejs.org/")
}

$javaHome = Find-Java21Home
if ($javaHome -is [hashtable]) {
    if ($javaHome.Wrong) {
        $problems.Add(@"
Java encontrado, versao $($javaHome.Wrong).
Este projeto precisa do JDK 21.
Instale o Eclipse Temurin 21 LTS.
Site: https://adoptium.net/temurin/releases/?version=21
Depois deixe o java no PATH ou extraia o JDK em $env:USERPROFILE\.jdks
"@)
    }
    else {
        $problems.Add(@"
JDK 21 nao encontrado.
Instale o Eclipse Temurin 21 LTS.
Site: https://adoptium.net/temurin/releases/?version=21
Depois deixe o java no PATH ou extraia o JDK em $env:USERPROFILE\.jdks
"@)
    }
}

if ($problems.Count -gt 0) {
    Write-Output ""
    Write-Output "Instale o que falta e rode este atalho de novo."
    foreach ($problem in $problems) {
        Write-Output ""
        Write-Output $problem.Trim()
    }
    exit 1
}

$env:JAVA_HOME = [string]$javaHome
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
Write-Step "Node.js v$nodeVersion"
Write-Step "JAVA_HOME=$env:JAVA_HOME"

Write-Step "Frontend: npm install"
Set-Location (Join-Path $root 'frontend')
& npm install
if ($LASTEXITCODE -ne 0) {
    throw "npm install falhou com o codigo $LASTEXITCODE"
}

Write-Step "Backend: baixando dependencias e compilando"
Set-Location (Join-Path $root 'backend')
& .\mvnw.cmd -DskipTests package
if ($LASTEXITCODE -ne 0) {
    throw "Maven falhou com o codigo $LASTEXITCODE"
}

Write-Step "Banco de dados"
& (Join-Path $root 'scripts\start-postgres.ps1')
if ($LASTEXITCODE -ne 0) {
    throw "A preparacao do PostgreSQL falhou com o codigo $LASTEXITCODE"
}

Write-Step "Instalacao concluida. Agora use rodar.cmd para reiniciar o backend e o frontend."
exit 0
