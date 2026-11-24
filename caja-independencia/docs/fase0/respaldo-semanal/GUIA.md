# Guía: respaldo semanal programado en el servidor Windows

Cada **lunes a las 3:00 am (hora de México)**, el servidor Windows saca una copia completa de la base `sindicato`, la cifra y la guarda.
Sirve para dos cosas:

1. **La réplica de la bitácora** ([diseno-bitacora.md](../diseno-bitacora.md), sección 8): la copia del lunes, más las operaciones de la
   semana, debe dar la copia del lunes siguiente.
2. **Una copia fuera de Google.** Los respaldos automáticos de Cloud SQL viven en el mismo proyecto que la base; esta copia queda en
   tu servidor y en tu Mac.

**Cuándo:** la actualización a MySQL 8.4 ya se hizo (domingo 15 de noviembre), así que ya se puede instalar. No hay que esperar al
sistema nuevo: como copia fuera de Google sirve desde el primer lunes, y para la bitácora hace falta en su etapa C, antes de la réplica.
- Los pasos 0 a 5 no tocan la base y los puedes hacer cuando quieras. El paso 0 ya está hecho (23 de noviembre).
- El paso 6 sí modifica producción, porque crea un usuario. Lo hacemos juntos y con tu confirmación.

**Tiempo total:** alrededor de una hora, la primera vez.

## Cómo funciona

```
Lunes 03:00  Tarea programada de Windows (cuenta SYSTEM)
   │
   ├─ mysqldump --single-transaction ──► lee la base con un usuario de SOLO LECTURA, por conexión cifrada.
   │                                     Una sola transacción de lectura: no bloquea ni frena a la caja.
   ├─ copia los Excel cargados en los últimos 8 días (C:\Documentos)
   ├─ escribe un manifiesto: hora de inicio y fin (UTC), versión de MySQL y huella SHA-256 de cada archivo
   ├─ empaqueta (.tgz) y CIFRA con tu llave pública (age) ──► C:\RespaldoCaja\copias\caja-AAAA-MM-DD_0300.tgz.age
   └─ borra la copia sin cifrar y conserva solo las últimas 8 copias
```

**Qué protege:**
- Solo tu llave privada, que vive en tu Mac, puede abrir la copia. El servidor tiene únicamente la llave pública: puede cifrar, pero no descifrar.
- Si alguien entra al servidor, no puede leer las copias.

**Qué contiene:** todos los datos de unos 12,500 socios, incluidas las contraseñas tal como están hoy en la base, en texto plano.
Por eso la copia va cifrada, la carpeta queda restringida y, en tu Mac, el `.sql` descifrado se borra en cuanto se carga.

**Qué no incluye:**
- **El evento de borrado de las 21:00.** Para copiarlo, el usuario de respaldo necesitaría un permiso que también deja crear, cambiar y
  borrar eventos. Además, en la réplica ese borrado no debe correr solo.
- Su definición, y la del procedimiento que llama, quedan en [objetos-programados.sql](objetos-programados.sql) para una recuperación de
  desastre.

**Medido con la copia local de la base (12-nov-2026):**
- La copia sin cifrar pesa **372 MB**; cifrada y comprimida, **68 MB**.
- Restaurada en MySQL 8.4, las 43 tablas quedaron **idénticas** al origen: `CHECKSUM TABLE` tabla por tabla, y el procedimiento incluido.
- En el servidor, sacar la copia tardará unos minutos, según el ancho de banda.

## Archivos de esta carpeta

| Archivo | Dónde va | Para qué |
|---|---|---|
| `respaldo-semanal.ps1` | servidor: `C:\RespaldoCaja\` | el script que corre cada lunes |
| `crea-usuario-respaldo.sql` | servidor: `C:\RespaldoCaja\` | crea el usuario de solo lectura (paso 6, una vez) |
| `server-ca.pem` | servidor: `C:\RespaldoCaja\config\` | certificado público de Cloud SQL, para verificar que el servidor habla con la base real (vence en 2034) |
| `restaura-copia.sh` | tu Mac (se queda en el repo) | descifra una copia y la carga en un MySQL local |
| `objetos-programados.sql` | (referencia) | el evento y el procedimiento de borrado |

## Paso 0. Revisar el servidor

1. En el servidor, abre **PowerShell como administrador**: menú Inicio → escribe "PowerShell" → clic derecho → "Ejecutar como administrador".
2. Pega esto:

```powershell
(Get-CimInstance Win32_OperatingSystem).Caption
$PSVersionTable.PSVersion.ToString()
tzutil /g
'Libre en C: {0:N1} GB' -f ((Get-PSDrive C).Free / 1GB)
'tar.exe: ' + (Test-Path "$env:SystemRoot\System32\tar.exe")
'C:\Documentos: ' + (Test-Path C:\Documentos)
```

3. **Mándame lo que salga.** Esto es lo que espero:
   - **Windows Server 2019 o más nuevo**, porque trae `tar.exe`. Si sale `tar.exe: False`, ajusto el script para usar 7-Zip.
   - **Zona horaria `Central Standard Time (Mexico)`.** Si es otra, calculo a qué hora del servidor equivalen las 3:00 de México.
   - **Al menos 5 GB libres.** Las copias ocupan unos 600 MB, más 3 GB temporales los lunes mientras se arma la copia.

## Paso 1. Tu llave (en tu Mac)

En la Terminal de tu Mac:

```bash
brew install age
mkdir -p ~/.config/caja-independencia
age-keygen -o ~/.config/caja-independencia/respaldo-age.key
chmod 600 ~/.config/caja-independencia/respaldo-age.key
```

`age-keygen` imprime una línea `Public key: age1…`. **Esa es tu llave pública**; la usas en el paso 5.

> ⚠️ **Guarda una copia de la llave privada fuera de tu Mac**, por ejemplo como nota segura en tu gestor de contraseñas. Está en el
> archivo `respaldo-age.key`, en la línea que empieza con `AGE-SECRET-KEY-`. Si pierdes esa llave, **ninguna copia se puede abrir**.
> Nunca la pongas en el servidor ni en el repositorio.

## Paso 2. Carpetas en el servidor

En PowerShell como administrador:

```powershell
New-Item -ItemType Directory -Force -Path C:\RespaldoCaja\config, C:\RespaldoCaja\mysql, C:\RespaldoCaja\age | Out-Null
icacls C:\RespaldoCaja /inheritance:r /grant:r "*S-1-5-18:(OI)(CI)F" "*S-1-5-32-544:(OI)(CI)F"
```

La segunda línea deja la carpeta visible **solo para SYSTEM y los administradores** del servidor. Usa los identificadores internos para
que funcione igual en Windows en español o en inglés. Debe terminar con `Se procesaron correctamente 1 archivos` (o su equivalente en inglés).

## Paso 3. Herramientas (gratuitas)

**MySQL 8.4, solo para usar `mysqldump`.** No se instala ningún servicio.
1. Entra a <https://dev.mysql.com/downloads/mysql/>.
2. Elige *Select Version*: la **8.4.x LTS** más reciente; *Operating System*: **Microsoft Windows**.
3. Descarga **"Windows (x86, 64-bit), ZIP Archive"**. El sitio te ofrece "No thanks, just start my download".
4. Antes de abrir el ZIP, desbloquéalo: `Unblock-File "$env:USERPROFILE\Downloads\mysql-8.4*.zip"`
5. Abre el ZIP y copia **la carpeta `bin` completa** a `C:\RespaldoCaja\mysql\`. Debe quedar `C:\RespaldoCaja\mysql\bin\mysqldump.exe`.

**age, para cifrar.**
1. Entra a <https://github.com/FiloSottile/age/releases> y descarga `age-v1.3.2-windows-amd64.zip`, o la versión más reciente.
2. Desbloquéalo con `Unblock-File`, igual que el de MySQL.
3. Copia `age.exe` a `C:\RespaldoCaja\age\`.

**Comprueba las dos:**

```powershell
C:\RespaldoCaja\mysql\bin\mysqldump.exe --version     # debe decir "Ver 8.4.x for Win64"
C:\RespaldoCaja\age\age.exe --version                 # debe decir "v1.3.2" o más nuevo
```

Si `mysqldump` marca que falta `VCRUNTIME140.dll` o `MSVCP140.dll`, instala el
[Visual C++ Redistributable x64](https://aka.ms/vs/17/release/vc_redist.x64.exe) de Microsoft y vuelve a probar.

## Paso 4. Copiar los archivos de esta carpeta al servidor

Copia los archivos igual que subes el WAR:
- `respaldo-semanal.ps1` y `crea-usuario-respaldo.sql` → `C:\RespaldoCaja\`
- `server-ca.pem` → `C:\RespaldoCaja\config\`

Los dos primeros están escritos sin acentos a propósito: Windows PowerShell los lee como ANSI y un acento mal leído puede romperlos.

## Paso 5. Configuración: contraseña del usuario y llave pública

En PowerShell como administrador:
1. Reemplaza `<IP de Cloud SQL>` por la IP de la base, la misma que aparece en `hibernate.cfg.xml` después de `jdbc:mysql://`.
2. Reemplaza `age1...` por tu llave pública del paso 1.
3. Pega el bloque:

```powershell
$b = New-Object byte[] 24
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
$pw = [Convert]::ToBase64String($b) -replace '[^A-Za-z0-9]', ''
@(
  '[client]'
  'host=<IP de Cloud SQL>'
  'port=3306'
  'user=respaldo_semanal'
  "password=$pw"
  'ssl-mode=VERIFY_CA'
  'ssl-ca=C:/RespaldoCaja/config/server-ca.pem'
) | Set-Content -Path C:\RespaldoCaja\config\respaldo.cnf -Encoding Ascii
'age1...' | Set-Content -Path C:\RespaldoCaja\config\destinatario.txt -Encoding Ascii
Remove-Variable b, pw
```

- La contraseña se genera al azar en el servidor y **no sale de ahí**. Ni tú ni yo necesitamos conocerla.
- En `ssl-ca` van diagonales normales (`/`) a propósito: en estos archivos de MySQL, la diagonal invertida (`\`) tiene otro significado.

## Paso 6. Crear el usuario de solo lectura (MODIFICA PRODUCCIÓN, lo hacemos juntos)

**Qué hace:**
- Crea el usuario `respaldo_semanal` con permiso **solo de lectura** sobre `sindicato`.
- Solo puede entrar desde la IP del servidor y solo por conexión cifrada.
- No toca ningún dato.

**Lo que necesitas:**
- La **IP del servidor**: es la de la red autorizada "Sindicato server" en Cloud SQL. Yo te la doy ese día.
- La **contraseña del usuario de la aplicación**: `mysql` te la pide y no queda guardada.

**Cómo se hace:**
1. Reemplaza los tres valores entre `< >`.
2. Pega el bloque en PowerShell como administrador:

```powershell
$ipServidor = '<IP del servidor>'
$pw = (Get-Content C:\RespaldoCaja\config\respaldo.cnf | Where-Object { $_ -like 'password=*' }) -replace '^password=', ''
$sql = (Get-Content C:\RespaldoCaja\crea-usuario-respaldo.sql -Raw) -replace '<IP_SERVIDOR>', $ipServidor -replace '<CONTRASENA>', $pw
$sql | C:\RespaldoCaja\mysql\bin\mysql.exe --host=<IP de Cloud SQL> --user=<usuario de la aplicación> -p --ssl-mode=VERIFY_CA --ssl-ca=C:\RespaldoCaja\config\server-ca.pem --table
Remove-Variable pw, sql
```

**Qué debe salir:**
- Primero, 1 procedimiento (`sp_eliminar_solicitudes_antiguas`), 1 evento y 0 disparadores.
- Al final, los permisos del usuario: `USAGE`, `SELECT, SHOW VIEW` sobre `sindicato` y `SHOW_ROUTINE`.
- Si Cloud SQL rechaza solo la línea de `SHOW_ROUTINE`, no pasa nada: el procedimiento está documentado en `objetos-programados.sql`.
- Si no aparece la petición de contraseña, avísame y lo hacemos de otra forma.

## Paso 7. Primera copia a mano

En PowerShell como administrador:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\RespaldoCaja\respaldo-semanal.ps1
```

Debe terminar con algo así:

```
Copia completa: 37x.x MB, MySQL 8.4.x, procedimientos: sp_eliminar_solicitudes_antiguas
Excel de los ultimos 8 dias: N
Copia cifrada: C:\RespaldoCaja\copias\caja-2026-11-16_1030.tgz.age (6x.x MB) sha256=...
=== Fin: OK ===
```

Comprueba que **`C:\RespaldoCaja\trabajo` quedó vacía**: ahí estuvo un momento la copia sin cifrar. Si algo sale `ERROR`, mándame el
archivo de `C:\RespaldoCaja\logs\` (no trae contraseñas).

## Paso 8. Programar la tarea (lunes 3:00 am)

```powershell
$accion   = New-ScheduledTaskAction -Execute 'powershell.exe' -Argument '-NoProfile -ExecutionPolicy Bypass -File C:\RespaldoCaja\respaldo-semanal.ps1'
$cuando   = New-ScheduledTaskTrigger -Weekly -DaysOfWeek Monday -At '01:00'
$quien    = New-ScheduledTaskPrincipal -UserId 'SYSTEM' -LogonType ServiceAccount -RunLevel Highest
$ajustes  = New-ScheduledTaskSettingsSet -StartWhenAvailable -ExecutionTimeLimit (New-TimeSpan -Hours 2) -MultipleInstances IgnoreNew
Register-ScheduledTask -TaskName 'Respaldo semanal Caja Independencia' -Action $accion -Trigger $cuando -Principal $quien -Settings $ajustes
```

- **Corre como SYSTEM**, así que no depende de que alguien tenga la sesión abierta y no guarda ninguna contraseña de Windows.
- **Si el servidor estaba apagado a esa hora**, corre en cuanto encienda (`-StartWhenAvailable`). El manifiesto registra la hora real.
- **Por qué `'01:00'`:** el servidor está en `Pacific Standard Time` (paso 0, 23-nov-2026). La 1:00 del servidor son las 3:00 de México
  en invierno; de marzo a noviembre, por el horario de verano de EE. UU., serán las 2:00 de México. Ninguna de las dos choca con nada.

**Pruébala una vez:**

```powershell
Start-ScheduledTask -TaskName 'Respaldo semanal Caja Independencia'
# espera unos minutos y luego:
Get-ScheduledTaskInfo -TaskName 'Respaldo semanal Caja Independencia' | Select-Object LastRunTime, LastTaskResult
Get-Content C:\RespaldoCaja\ESTADO.txt
```

Debe salir `LastTaskResult` igual a `0` y `ESTADO.txt` debe decir `OK`. Si sale `1`, falló: el detalle está en el log.

## Paso 9. Probar que la copia se puede restaurar (en tu Mac)

Un respaldo que no se ha restaurado no está probado.
1. Trae a tu Mac un archivo `caja-….tgz.age` de `C:\RespaldoCaja\copias\`, de la misma forma que subes el WAR, pero al revés.
2. En la Terminal:

```bash
cd ~/Projects/Cajaindependencia/caja-independencia/docs/fase0/respaldo-semanal
./restaura-copia.sh ~/Downloads/caja-2026-11-16_1030.tgz.age
```

**Qué hace el script:**
- Descifra la copia con tu llave y comprueba las huellas del manifiesto.
- La carga en un MySQL 8.4 local y desechable: el contenedor `caja-copia-semanal`, solo en `127.0.0.1:3309`. Nunca se conecta a la nube.
- Muestra el conteo de filas de cada tabla. Tarda unos 5 minutos.
- Borra el `.sql` descifrado. En `~/Documents/caja-independencia/copias-semanales/` quedan el manifiesto, los Excel y los conteos.

**Después:** yo comparo esos conteos contra producción, solo con lectura. Con eso, el respaldo queda probado de punta a punta.

## Cada semana: revisión de un minuto

- En el servidor, abre `C:\RespaldoCaja\ESTADO.txt`: debe decir `OK` con la fecha del lunes.
- Cuando empecemos la réplica de la bitácora, cada semana traerás la copia nueva a tu Mac (paso 9).

## Si algo falla

| Lo que dice el log | Qué significa | Qué hacer |
|---|---|---|
| `Access denied for user 'respaldo_semanal'` | contraseña, usuario o IP no coinciden | ¿Cambió la IP del servidor? Revisa la red autorizada y avísame |
| `SSL connection error` o `certificate` | el certificado de Cloud SQL cambió | descarga el nuevo `server-ca.pem` (Cloud SQL → Conexiones → Seguridad) |
| `Can't connect to MySQL server` | la base no responde o el servidor no tiene internet | ver si la caja puede trabajar; si sí, reintenta a mano (paso 7) |
| `Espacio libre insuficiente` | menos de 3 GB libres | libera espacio en C: |
| `Ya hay un respaldo en curso` | otra ejecución sigue corriendo | espera; si no hay ninguna, borra `C:\RespaldoCaja\trabajo\EN_CURSO` (se libera solo tras 6 h) |
| `No existe C:\RespaldoCaja\...` | falta un archivo de los pasos 3 a 5 | revisa la ruta |
| `La copia no termino completa` | se cortó la conexión a medio camino | reintenta a mano; si se repite, avísame |

Ningún error deja la copia sin cifrar en el disco: el script la borra siempre al terminar, falle o no.

## Costos

| Concepto | Costo |
|---|---|
| Servidor Windows | el que ya pagas; usa unos 600 MB permanentes (8 copias) y 3 GB temporales los lunes |
| Transferencia de Cloud SQL al servidor | Google cobra la salida a internet a [0.19 USD por GiB](https://cloud.google.com/sql/pricing). La conexión va comprimida y son entre 0.1 y 0.4 GiB por semana: **menos de 10 pesos al mes** |
| MySQL Community y age | gratuitos |

No se habilita ningún servicio nuevo en Google Cloud.

## Horarios que no chocan (hora de México)

| Hora | Qué pasa |
|---|---|
| Diario, 21:00 | evento de la base que borra solicitudes viejas |
| Diario, 0:00 a 1:30 aprox. | respaldo automático de Cloud SQL |
| Domingo, 0:00 | ventana de mantenimiento de Cloud SQL |
| **Lunes, 3:00** (2:00 de marzo a noviembre) | **este respaldo** (1:00 en el reloj del servidor) |

## Cuando exista la bitácora (etapa C del diseño)

1. **Marca `CORTE_COPIA` antes de la copia.** El mecanismo exacto se define en la etapa A, porque la bitácora tiene un solo escritor.
2. **Misma copia para la bitácora.** El mismo `mysqldump` copiará también la base `bitacora`, con `--databases sindicato bitacora`, para que
   ambas queden en la misma foto. El usuario recibirá `SELECT` sobre `bitacora`, y `restaura-copia.sh` se ajustará a ese formato.
3. **Excel desde su propia carpeta.** Se tomarán de `C:\Documentos\bitacora\archivos\`, donde la bitácora guarda una copia de cada
   archivo cargado, en lugar de la ventana de 8 días.
