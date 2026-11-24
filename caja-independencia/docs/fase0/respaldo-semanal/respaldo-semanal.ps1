<#
Respaldo semanal de la base de Caja Independencia. Se ejecuta en el servidor Windows.

Que hace, en orden:
  1. Saca una copia consistente de la base `sindicato` con mysqldump --single-transaction, con el
     usuario de solo lectura `respaldo_semanal` (credenciales en config\respaldo.cnf).
  2. Agrega los Excel cargados en los ultimos dias (C:\Documentos) y un manifiesto con las horas UTC
     de inicio y fin y la huella SHA-256 de cada archivo.
  3. Empaqueta todo (tar.gz) y lo cifra con age para la llave publica de Ivette: el servidor puede
     cifrar, pero NO descifrar, la copia.
  4. Borra siempre la copia sin cifrar, aunque algo falle, y conserva solo las ultimas N copias.

Nunca escribe en la base: el usuario solo tiene permisos de lectura.
Este archivo esta escrito sin acentos a proposito: Windows PowerShell 5.1 lee los .ps1 sin BOM como
ANSI y un acento mal leido puede romper el script.

Uso manual:  powershell -NoProfile -ExecutionPolicy Bypass -File C:\RespaldoCaja\respaldo-semanal.ps1
Guia completa: docs/fase0/respaldo-semanal/GUIA.md
#>
param(
    [string]$Raiz = 'C:\RespaldoCaja',
    [string]$Mysqldump = 'C:\RespaldoCaja\mysql\bin\mysqldump.exe',
    [string]$Age = 'C:\RespaldoCaja\age\age.exe',
    [string]$Tar = (Join-Path $env:SystemRoot 'System32\tar.exe'),
    [string]$Documentos = 'C:\Documentos',
    [string]$Base = 'sindicato',
    [int]$DiasExcel = 8,
    [int]$CopiasAConservar = 8,
    [long]$MinimoBytesCopia = 100MB,
    [long]$MinimoLibre = 3GB
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2

$config  = Join-Path $Raiz 'config'
$trabajo = Join-Path $Raiz 'trabajo'
$copias  = Join-Path $Raiz 'copias'
$logs    = Join-Path $Raiz 'logs'
foreach ($d in @($trabajo, $copias, $logs)) {
    if (-not (Test-Path $d)) { New-Item -ItemType Directory -Path $d | Out-Null }
}

$sello    = (Get-Date).ToString('yyyy-MM-dd_HHmm')
$log      = Join-Path $logs "respaldo-$sello.log"
$estado   = Join-Path $Raiz 'ESTADO.txt'
$candado  = Join-Path $trabajo 'EN_CURSO'
$paquete  = Join-Path $trabajo 'paquete'
$tgz      = Join-Path $trabajo "caja-$sello.tgz"
$final    = Join-Path $copias "caja-$sello.tgz.age"
$parcial  = "$final.parcial"
$cnf      = Join-Path $config 'respaldo.cnf'
$receptor = Join-Path $config 'destinatario.txt'

function Escribe([string]$texto) {
    $linea = '{0}  {1}' -f (Get-Date).ToString('yyyy-MM-dd HH:mm:ss'), $texto
    Add-Content -Path $log -Value $linea -Encoding UTF8
    Write-Host $linea
}

# Ejecuta un programa externo; si termina con codigo distinto de 0, detiene el respaldo.
function Ejecuta([string]$programa, [string[]]$argumentos) {
    $anterior = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $salida = & $programa @argumentos 2>&1 | ForEach-Object { "$_" }
        $codigo = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $anterior
    }
    foreach ($l in @($salida)) { if ($l) { Escribe "    $l" } }
    if ($codigo -ne 0) { throw ('{0} termino con codigo {1}' -f [IO.Path]::GetFileName($programa), $codigo) }
}

function Huella([string]$ruta) {
    (Get-FileHash -Path $ruta -Algorithm SHA256).Hash.ToLower()
}

# Lee un pedazo del archivo (inicio o final) sin cargarlo completo en memoria.
function Fragmento([string]$ruta, [int]$bytes, [bool]$delFinal) {
    $fs = [IO.File]::OpenRead($ruta)
    try {
        $n = [int][Math]::Min([long]$bytes, $fs.Length)
        if ($delFinal) { [void]$fs.Seek(-$n, [IO.SeekOrigin]::End) }
        $buf = New-Object byte[] $n
        $leidos = 0
        while ($leidos -lt $n) {
            $r = $fs.Read($buf, $leidos, $n - $leidos)
            if ($r -le 0) { break }
            $leidos += $r
        }
        return [Text.Encoding]::UTF8.GetString($buf, 0, $leidos)
    } finally {
        $fs.Dispose()
    }
}

$exito = $false
$creoCandado = $false
try {
    Escribe "=== Respaldo semanal de la base '$Base' ==="

    # 0. Un solo respaldo a la vez. Un candado de mas de 6 horas es de una ejecucion interrumpida.
    if (Test-Path $candado) {
        $edad = (Get-Date) - (Get-Item $candado).LastWriteTime
        if ($edad.TotalHours -lt 6) { throw "Ya hay un respaldo en curso (existe $candado)." }
        Escribe 'Candado viejo de una ejecucion interrumpida; se reemplaza.'
    }
    Set-Content -Path $candado -Value $sello -Encoding Ascii
    $creoCandado = $true
    Get-ChildItem -Path $trabajo -Force | Where-Object { $_.FullName -ne $candado } | Remove-Item -Recurse -Force

    # 1. Revisiones previas
    foreach ($archivo in @($Mysqldump, $Age, $Tar, $cnf, $receptor)) {
        if (-not (Test-Path $archivo)) { throw "No existe $archivo" }
    }
    $unidad = (Get-Item $Raiz).PSDrive.Name
    $libre = (Get-PSDrive -Name $unidad).Free
    Escribe ('Espacio libre: {0:N1} GB' -f ($libre / 1GB))
    if ($libre -lt $MinimoLibre) {
        throw ('Espacio libre insuficiente: {0:N1} GB (minimo {1:N1} GB).' -f ($libre / 1GB), ($MinimoLibre / 1GB))
    }
    New-Item -ItemType Directory -Path (Join-Path $paquete 'excel') -Force | Out-Null

    # 2. Copia consistente de la base (una sola transaccion de lectura; no bloquea a la caja)
    $dump = Join-Path $paquete "$Base.sql"
    $errores = Join-Path $trabajo 'mysqldump-errores.txt'
    $inicioUtc = (Get-Date).ToUniversalTime()
    Escribe ('Inicio de la copia (UTC): {0:yyyy-MM-ddTHH:mm:ssZ}' -f $inicioUtc)
    try {
        Ejecuta $Mysqldump @(
            "--defaults-extra-file=$cnf",
            '--single-transaction', '--quick', '--no-tablespaces', '--set-gtid-purged=OFF',
            '--routines', '--skip-events', '--triggers', '--hex-blob',
            '--default-character-set=utf8mb4', '--compression-algorithms=zstd,zlib,uncompressed',
            "--result-file=$dump", "--log-error=$errores", $Base)
    } finally {
        if ((Test-Path $errores) -and (Get-Item $errores).Length -gt 0) {
            Get-Content $errores | ForEach-Object { Escribe "    mysqldump: $_" }
        }
    }
    $finUtc = (Get-Date).ToUniversalTime()
    Escribe ('Fin de la copia (UTC): {0:yyyy-MM-ddTHH:mm:ssZ}' -f $finUtc)

    $tam = (Get-Item $dump).Length
    $cola = Fragmento $dump 262144 $true
    if ($cola -notmatch '-- Dump completed') { throw 'La copia no termino completa (falta la linea "Dump completed").' }
    if ($tam -lt $MinimoBytesCopia) { throw ('La copia es demasiado chica: {0:N0} bytes.' -f $tam) }
    $versionMysql = ''
    if ((Fragmento $dump 4096 $false) -match '-- Server version\s+(\S+)') { $versionMysql = $Matches[1] }
    $rutinas = @([regex]::Matches($cola, '(?:PROCEDURE|FUNCTION) `([^`]+)`') |
                 ForEach-Object { $_.Groups[1].Value } | Select-Object -Unique)
    Escribe ('Copia completa: {0:N1} MB, MySQL {1}, procedimientos: {2}' -f ($tam / 1MB), $versionMysql, ($rutinas -join ', '))

    # 3. Excel cargados en los ultimos dias
    $desde = (Get-Date).AddDays(-$DiasExcel)
    $excel = @()
    if (Test-Path $Documentos) {
        $excel = @(Get-ChildItem -Path $Documentos -File |
                   Where-Object { (@('.xlsx', '.xls') -contains $_.Extension.ToLower()) -and $_.LastWriteTime -ge $desde })
        foreach ($x in $excel) { Copy-Item -Path $x.FullName -Destination (Join-Path $paquete 'excel') }
    } else {
        Escribe "Aviso: no existe $Documentos; la copia va sin Excel."
    }
    Escribe ('Excel de los ultimos {0} dias: {1}' -f $DiasExcel, $excel.Count)

    # Manifiesto: lo que hace falta para la replica y para comprobar que nada se altero.
    $m = New-Object 'System.Collections.Generic.List[string]'
    $m.Add('Respaldo semanal de Caja Independencia')
    $m.Add("servidor_windows: $env:COMPUTERNAME")
    $m.Add("base: $Base")
    $m.Add(('inicio_utc: {0:yyyy-MM-ddTHH:mm:ssZ}' -f $inicioUtc))
    $m.Add(('fin_utc: {0:yyyy-MM-ddTHH:mm:ssZ}' -f $finUtc))
    $m.Add("zona_horaria_servidor: $([TimeZoneInfo]::Local.Id)")
    $m.Add("mysqldump: $((& $Mysqldump --version) -join ' ')")
    $m.Add("version_mysql: $versionMysql")
    $m.Add("procedimientos: $($rutinas -join ', ')")
    $m.Add('eventos: no se incluyen a proposito (ver objetos-programados.sql)')
    $m.Add(('copia: {0}.sql  bytes={1}  sha256={2}' -f $Base, $tam, (Huella $dump)))
    foreach ($x in $excel) {
        $copia = Join-Path (Join-Path $paquete 'excel') $x.Name
        $m.Add(('excel: {0}  bytes={1}  modificado={2:yyyy-MM-dd HH:mm:ss}  sha256={3}' -f $x.Name, $x.Length, $x.LastWriteTime, (Huella $copia)))
    }
    [IO.File]::WriteAllLines((Join-Path $paquete 'manifiesto.txt'), $m, (New-Object Text.UTF8Encoding $false))

    # 4. Empaquetar y cifrar. El nombre final solo aparece cuando el archivo cifrado esta completo.
    Ejecuta $Tar @('-czf', $tgz, '-C', $paquete, '.')
    Ejecuta $Age @('-R', $receptor, '-o', $parcial, $tgz)
    Move-Item -Path $parcial -Destination $final -Force
    $h = Huella $final
    $bytesFinal = (Get-Item $final).Length
    Add-Content -Path (Join-Path $copias 'huellas.txt') -Encoding Ascii `
        -Value ('{0}  {1}  {2}  bytes={3}' -f $sello, $h, (Split-Path $final -Leaf), $bytesFinal)
    Escribe ('Copia cifrada: {0} ({1:N1} MB) sha256={2}' -f $final, ($bytesFinal / 1MB), $h)

    # 5. Conservar solo las ultimas N copias y un anio de bitacoras de este script
    $viejas = @(Get-ChildItem -Path $copias -Filter 'caja-*.tgz.age' | Sort-Object Name -Descending |
                Select-Object -Skip $CopiasAConservar)
    foreach ($v in $viejas) {
        Remove-Item $v.FullName -Force
        Escribe "Se borra la copia vieja $($v.Name)"
    }
    Get-ChildItem -Path $logs -Filter 'respaldo-*.log' |
        Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-365) } | Remove-Item -Force

    $exito = $true
}
catch {
    Escribe "ERROR: $($_.Exception.Message)"
}
finally {
    if ($creoCandado) {
        # La copia sin cifrar nunca se queda en disco (esto tambien libera el candado).
        Get-ChildItem -Path $trabajo -Force -ErrorAction SilentlyContinue |
            Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
        if (Test-Path $parcial) { Remove-Item $parcial -Force -ErrorAction SilentlyContinue }
        if (Test-Path $paquete) { Escribe "AVISO: no se pudo borrar $paquete; borralo a mano." }
    }
    $resultado = 'ERROR'
    if ($exito) { $resultado = 'OK' }
    Set-Content -Path $estado -Encoding Ascii `
        -Value ('{0}  {1}  (detalle en {2})' -f (Get-Date).ToString('yyyy-MM-dd HH:mm:ss'), $resultado, $log)
    Escribe "=== Fin: $resultado ==="
}
if ($exito) { exit 0 } else { exit 1 }
