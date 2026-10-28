# Sobe backend e frontend e abre o navegador quando os dois respondem.
# Duplo clique: rodar.cmd
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendUrl = 'http://127.0.0.1:8080/api/v1/auth/session'
$frontendUrl = 'http://localhost:4200/'

function Test-HttpReady([string]$url) {
    try {
        $request = [System.Net.HttpWebRequest]::Create($url)
        $request.Method = 'GET'
        $request.Timeout = 3000
        $request.AllowAutoRedirect = $true
        $response = $request.GetResponse()
        $response.Close()
        return $true
    }
    catch [System.Net.WebException] {
        $webResponse = $_.Exception.Response
        if ($webResponse) {
            $webResponse.Close()
            return $true
        }
        return $false
    }
    catch {
        return $false
    }
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
    }
    return $null
}

function Stop-Listeners([int]$port, [string]$name) {
    $processIds = New-Object System.Collections.Generic.List[int]
    $previous = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $rows = & netstat.exe -ano -p tcp
    $ErrorActionPreference = $previous
    foreach ($row in $rows) {
        if ($row -match (":{0}\s+\S+\s+LISTENING\s+(\d+)$" -f $port)) {
            $procId = [int]$Matches[1]
            if ($procId -gt 4) {
                [void]$processIds.Add($procId)
            }
        }
    }
    $unique = @($processIds | Select-Object -Unique)
    if ($unique.Count -eq 0) {
        Write-Output "$name nao estava no ar."
        return
    }
    Write-Output "Encerrando $name para iniciar de novo."
    foreach ($procId in $unique) {
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    }
}

Write-Output "Conferindo o banco de dados"
$nodeVersion = Get-NodeVersionText
if (-not $nodeVersion -or -not (Test-NodeSupported $nodeVersion)) {
    Write-Output ""
    Write-Output "Node.js compativel nao encontrado."
    Write-Output "Instale o Node.js 24 LTS, versao 24.15.0 ou mais nova."
    Write-Output "Alternativas aceitas: Node.js 22.22.3 ou mais novo, ou Node.js 26."
    Write-Output "Site: https://nodejs.org/"
    exit 1
}
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Output "O npm nao foi encontrado. Reinstale o Node.js 24 LTS. Site: https://nodejs.org/"
    exit 1
}
if (-not (Find-Java21Home)) {
    Write-Output ""
    Write-Output "JDK 21 nao encontrado."
    Write-Output "Instale o Eclipse Temurin 21 LTS."
    Write-Output "Site: https://adoptium.net/temurin/releases/?version=21"
    Write-Output "Depois deixe o java no PATH ou extraia o JDK em $env:USERPROFILE\.jdks"
    exit 1
}
if (-not (Test-Path -LiteralPath (Join-Path $root 'frontend\node_modules'))) {
    Write-Output "As dependencias do frontend ainda nao foram instaladas."
    Write-Output "Rode instalar.cmd antes de rodar as aplicacoes."
    exit 1
}

& (Join-Path $root 'scripts\start-postgres.ps1')
if ($LASTEXITCODE -ne 0) {
    throw "A preparacao do PostgreSQL falhou com o codigo $LASTEXITCODE"
}

Stop-Listeners 8080 'Backend'
Stop-Listeners 4200 'Frontend'
Start-Sleep -Seconds 1

$backendScript = Join-Path $root 'scripts\start-backend.ps1'
$frontendScript = Join-Path $root 'scripts\start-frontend.ps1'
Write-Output "Abrindo a janela do backend em $backendUrl"
Start-Process -FilePath 'powershell.exe' -WorkingDirectory $root -ArgumentList @(
    '-NoExit', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $backendScript
)
Write-Output "Abrindo a janela do frontend em $frontendUrl"
Start-Process -FilePath 'powershell.exe' -WorkingDirectory $root -ArgumentList @(
    '-NoExit', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $frontendScript
)

$deadline = (Get-Date).AddMinutes(5)
$backendReady = $false
$frontendReady = $false
while ((Get-Date) -lt $deadline) {
    if (-not $backendReady) {
        $backendReady = Test-HttpReady $backendUrl
    }
    if (-not $frontendReady) {
        $frontendReady = Test-HttpReady $frontendUrl
    }
    if ($backendReady -and $frontendReady) {
        break
    }
    $backendState = 'aguardando'
    if ($backendReady) { $backendState = 'pronto' }
    $frontendState = 'aguardando'
    if ($frontendReady) { $frontendState = 'pronto' }
    Write-Output ("Backend: {0} | Frontend: {1}" -f $backendState, $frontendState)
    Start-Sleep -Seconds 2
}

if (-not ($backendReady -and $frontendReady)) {
    Write-Output ""
    Write-Output "O navegador nao foi aberto porque uma das aplicacoes nao respondeu."
    if (-not $backendReady) {
        Write-Output "Backend ainda indisponivel em $backendUrl"
        Write-Output "Confira a janela do backend."
    }
    if (-not $frontendReady) {
        Write-Output "Frontend ainda indisponivel em $frontendUrl"
        Write-Output "Confira a janela do frontend."
    }
    exit 1
}

Write-Output "As duas aplicacoes responderam. Abrindo o navegador."
Start-Process $frontendUrl
exit 0
