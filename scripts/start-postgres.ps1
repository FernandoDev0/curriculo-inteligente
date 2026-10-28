$ErrorActionPreference = 'Stop'

$installRoot = Join-Path $env:USERPROFILE '.curriculo-inteligente'
$pgsqlHome = Join-Path $installRoot 'pgsql'
$bin = Join-Path $pgsqlHome 'bin'
$data = Join-Path $installRoot 'pgdata'
$log = Join-Path $installRoot 'postgres.log'
$zip = Join-Path $installRoot 'postgresql-17.6-2-windows-x64-binaries.zip'
$downloadUrl = 'https://get.enterprisedb.com/postgresql/postgresql-17.6-2-windows-x64-binaries.zip'
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'

function Read-DotEnv([string]$path) {
    $map = @{}
    if (-not (Test-Path -LiteralPath $path)) {
        return $map
    }
    Get-Content -LiteralPath $path | ForEach-Object {
        if ($_ -match '^\s*#' -or $_ -match '^\s*$') {
            return
        }
        $pair = $_.Split('=', 2)
        if ($pair.Length -eq 2) {
            $map[$pair[0].Trim()] = $pair[1].Trim()
        }
    }
    return $map
}

function Sql-Literal([string]$value) {
    return "'" + $value.Replace("'", "''") + "'"
}

$config = Read-DotEnv $envFile
$dbName = $config['POSTGRES_DB']
if (-not $dbName) { $dbName = 'curriculo' }
$dbUser = $config['POSTGRES_USER']
if (-not $dbUser) { $dbUser = 'curriculo' }
$dbPassword = $config['POSTGRES_PASSWORD']
if (-not $dbPassword) { $dbPassword = $config['SPRING_DATASOURCE_PASSWORD'] }
$portText = $config['POSTGRES_PORT']
if (-not $portText) { $portText = '5432' }
$port = 5432
[void][int]::TryParse($portText, [ref]$port)

if (-not $dbPassword) {
    throw "Defina POSTGRES_PASSWORD em .env antes de subir o banco. Copie .env.example para .env."
}

New-Item -ItemType Directory -Force -Path $installRoot | Out-Null

if (-not (Test-Path -LiteralPath (Join-Path $bin 'initdb.exe'))) {
    Write-Output "Baixando PostgreSQL 17 para uso local."
    curl.exe -L --fail --retry 2 -o $zip $downloadUrl
    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel baixar o PostgreSQL. Codigo $LASTEXITCODE"
    }
    tar.exe -xf $zip -C $installRoot
    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel extrair o PostgreSQL."
    }
}

$env:Path = $bin + ';' + $env:Path
$previous = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
& (Join-Path $bin 'pg_isready.exe') -h 127.0.0.1 -p $port | Out-Null
$readyCode = $LASTEXITCODE
$ErrorActionPreference = $previous

if ($readyCode -eq 0) {
    Write-Output "PostgreSQL ja esta no ar em 127.0.0.1:$port. O banco nao foi reiniciado."
    return
}

if ($readyCode -ne 0) {
    if (-not (Test-Path -LiteralPath (Join-Path $data 'PG_VERSION'))) {
        Write-Output "Criando o banco local em $data"
        $pwFile = Join-Path $installRoot 'pg-superuser.pw'
        [System.IO.File]::WriteAllText($pwFile, $dbPassword)
        & (Join-Path $bin 'initdb.exe') -D $data -U postgres -E UTF8 --locale=C -A scram-sha-256 --pwfile=$pwFile
        $initCode = $LASTEXITCODE
        Remove-Item -LiteralPath $pwFile -Force -ErrorAction SilentlyContinue
        if ($initCode -ne 0) {
            throw "initdb falhou com o codigo $initCode"
        }
        Add-Content -LiteralPath (Join-Path $data 'postgresql.conf') -Value @"
listen_addresses = '127.0.0.1'
port = $port
"@
    }
    Write-Output "Iniciando PostgreSQL em 127.0.0.1:$port"
    & (Join-Path $bin 'pg_ctl.exe') -D $data -l $log -w -t 40 start
    if ($LASTEXITCODE -ne 0) {
        throw "PostgreSQL nao iniciou. Veja o log em $log. Se a porta $port estiver ocupada, encerre o outro programa ou mude POSTGRES_PORT no .env."
    }
}

$env:PGPASSWORD = $dbPassword
$psql = Join-Path $bin 'psql.exe'
$quotedUser = Sql-Literal $dbUser
$quotedPassword = Sql-Literal $dbPassword
$roleSql = "DO `$`$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = $quotedUser) THEN CREATE ROLE $dbUser LOGIN PASSWORD $quotedPassword; ELSE ALTER ROLE $dbUser WITH LOGIN PASSWORD $quotedPassword; END IF; END `$`$;"
& $psql -h 127.0.0.1 -p $port -U postgres -d postgres -v ON_ERROR_STOP=1 -c $roleSql
if ($LASTEXITCODE -ne 0) {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    throw "Nao foi possivel criar o usuario do banco."
}
$quotedDb = Sql-Literal $dbName
$exists = & $psql -h 127.0.0.1 -p $port -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = $quotedDb"
if ($LASTEXITCODE -ne 0) {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    throw "Nao foi possivel consultar os bancos."
}
if (($exists | Out-String).Trim() -ne '1') {
    & $psql -h 127.0.0.1 -p $port -U postgres -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $dbName OWNER $dbUser"
    if ($LASTEXITCODE -ne 0) {
        Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
        throw "Nao foi possivel criar o banco $dbName."
    }
}
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
Write-Output "PostgreSQL local pronto em 127.0.0.1:$port, banco $dbName."
