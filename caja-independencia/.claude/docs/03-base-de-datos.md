# 03 — Base de datos

> Verificado contra la BD real el **2026-08-29**. Motor: **MySQL 8.0.31-google** (Google Cloud SQL).

## Conexión

| Dato | Valor |
| --- | --- |
| Host | `35.225.67.182:3306` |
| Esquema | `sindicato` |
| Usuario | `sindicatoindependencia` |
| TLS | **deshabilitado** (`useSSL=false`) |
| Fuente | `src/main/resources/hibernate.cfg.xml` (credenciales versionadas) |

El servidor filtra por **IP autorizada**. Si la conexión se queda colgada sin error, la IP pública
actual no está en la whitelist de Cloud SQL:

```bash
curl https://ipinfo.io/ip     # obtener IP y agregarla en Google Cloud SQL → Conexiones
```

No hay cliente `mysql` instalado en la máquina. Para consultar sin instalarlo, se puede usar el
driver del repo Maven con un archivo Java de un solo fichero (Java 11 lo ejecuta directo):

```bash
java -cp ~/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar \
     Q.java "select ..."
```

> **La BD es de producción.** Solo lectura salvo instrucción explícita. `hbm2ddl.auto=update`
> significa que además arrancar la app puede alterar el esquema.

Certificados TLS preparados pero no usados: `../mysql_ssl/*.pem`, `../mysql_cert/*.p12`
(en la raíz del repo).

## Volumetría real

| Tabla | Filas (aprox.) | Tamaño | Nota |
| --- | --- | --- | --- |
| `movimientos` | **2,225,463** | 598 MB | la más grande: ahorros y rendimientos |
| `amortizacion` | **460,713** | 145 MB | renglones de tablas de amortización |
| `pagos` | **331,862** | 75 MB | pagos cargados por archivo |
| `imagenes` | 92,782 | 11 MB | documentos de solicitudes |
| `bancos` | 39,731 | 4 MB | movimientos bancarios del sistema |
| `solicitud_avales` | 28,107 | 4 MB | |
| `estado_cuenta` | 26,044 | 6.5 MB | movimientos del banco real |
| `registro_transaccion` | 16,747 | 1.5 MB | auditoría de acciones |
| `solicitudes` | 11,565 | 4.6 MB | |
| `usuarios` | **12,382** | 3.7 MB | 3,989 activos / 8,389 de baja |
| `creditos_final` | 10,240 | 1.5 MB | |
| `beneficiarios` | 8,128 | 1.5 MB | |
| `baja_empleados` | 7,741 | 0.4 MB | |
| `bitacora` | 3,999 | 1.5 MB | |
| `archivos_historial` | 2,610 | 0.3 MB | |
| `altas_cambios_hist` | 5,200 | 0.3 MB | |
| `catorcenas` | 430 | — | 2011-07-14 → **2027-12-23** |
| `rendimiento` | 157 | — | mensual desde 2012-03 |
| `password_reset_tokens` | 120 | — | |

Al tocar `movimientos`, `amortizacion` o `pagos`, **siempre** revisa el plan de ejecución: son
tablas de cientos de miles a millones de filas.

## Integridad referencial: solo 7 FKs

La BD declara únicamente estas llaves foráneas (exactamente las que Hibernate mapea como
`many-to-one`):

```
amortizacion.amo_credito        → creditos_final.cre_id
imagenes.ima_solicitud          → solicitudes.sol_id
solicitud_avales.sol_ava_solicitud → solicitudes.sol_id
solicitudes.sol_estatus         → solicitud_estatus.sol_est_id
solicitudes.sol_producto        → productos.pro_id
solicitudes.sol_usu_id          → usuarios.usu_id
usuarios.usu_empresa            → empresas.emp_id
```

**Todo lo demás son referencias lógicas sin restricción en BD.** Las más importantes, que hay que
mantener a mano al escribir SQL:

| Columna | Apunta a | Comentario |
| --- | --- | --- |
| `creditos_final.cre_usu_id` | `usuarios.usu_id` | join principal de créditos |
| `creditos_final.cre_solicitud` | `solicitudes.sol_id` | |
| `creditos_final.cre_producto` | `productos.pro_id` | |
| `creditos_final.cre_estatus` | `credito_estatus.cre_est_id` | **tabla no mapeada en Hibernate** |
| `creditos_final.cre_padre` | `creditos_final.cre_id` | crédito origen en transferencias |
| `amortizacion.amo_usu_id` | `usuarios.usu_id` | usado por el algoritmo de pagos |
| `amortizacion.amo_pago_id` | `pagos.pag_id` | asignación pago↔amortización |
| `amortizacion.amo_estatus_int` | `amortizacion_estatus.amo_est_id` | |
| `pagos.pag_usu_id` | `usuarios.usu_id` | se llena **después** de insertar |
| `pagos.pag_credito` | `creditos_final.cre_id` | lo asigna el algoritmo |
| `pagos.pag_arh_id` | `archivos_historial.arh_id` | |
| `pagos.pag_estatus` | `pagos_estatus.pag_est_id` | |
| `movimientos.mov_usu_id` | `usuarios.usu_id` | se llena después de insertar |
| `movimientos.mov_arh_id` | `archivos_historial.arh_id` | |
| `movimientos.mov_id_padre` | `movimientos.mov_id` | rendimiento sobre ahorro voluntario |
| `beneficiarios.ben_usu_id` | `usuarios.usu_id` | |
| `baja_empleados.bae_id_empleado` | `usuarios.usu_id` | |
| `bitacora.bit_usuario` | `usuarios.usu_id` | `bit_referencia` suele ser `sol_id` |
| `bancos.ban_concepto` | `bancos_conceptos.cban_id` | |
| `bancos.ban_id_concepto_sistema` | PK de la tabla que indique `cban_tabla` | **polimórfico** |
| `bancos.ban_id_relacion` / `estado_cuenta.ec_id_relacion` | UUID de conciliación | agrupa banco↔edocta |
| `password_reset_tokens.prt_usu_id` | `usuarios.usu_id` | |
| `registro_transaccion.tran_tipo_tran` | `tipo_transaccion.tipo_tran_id` | |

> `bancos.ban_id_concepto_sistema` es una **referencia polimórfica**: la tabla destino y su PK
> están declaradas como texto en `bancos_conceptos.cban_tabla` / `cban_columnapk`. No se puede
> hacer un join genérico; hay que ramificar por `ban_concepto`.

## Índices relevantes

El esquema está bien indexado en las rutas calientes del algoritmo de pagos y el rendimiento:

```
amortizacion  idx_amo1  (amo_monto_pago, amo_credito, amo_fecha_pago, amo_estatus,
                         amo_usu_id, amo_estatus_int)      ← asignación de pagos
              idx_amo2  (amo_pago_id, amo_usu_id, amo_estatus, amo_fecha_pago, amo_monto_pago)
              idx_amo3  (amo_credito)   idx_amo4 (amo_pago_id)
              idx_amo_moroso (amo_id, amo_fecha_pago, amo_credito, amo_monto_pago)
pagos         idx_pag_arh (pag_arh_id, pag_empresa, pag_usu_id)
              idx_pag_emp (pag_fecha, pag_empresa, pag_usu_id)
              idx_pag_usu (pag_clave_empleado, pag_arh_id)
movimientos   idx_mov_usu_prod, idx_mov_empresa, idx_mov_arh_prod_usu, idx_mov_usuario,
              idx_ahorros_vol1/2, IndexMovSelRenVol, IndexMovWheRenVol
bancos        ajuste_banco (ban_id, ban_concepto, ban_monto, ban_empresa,
                            ban_fechatransaccion, ban_id_concepto_sistema, ban_ajustado)
estado_cuenta ajuste_bancos, ajuste_bancos_where (ec_fechatransaccion)
```

`creditos_final`, `baja_empleados`, `archivos_historial` y `catorcenas` **solo tienen PRIMARY**.
Los joins masivos por `cre_usu_id` o `cre_estatus` hacen full scan de 10k filas (tolerable hoy).

## Desincronizaciones entre BD, mappings y `Constantes`

Estas diferencias son reales y hay que tenerlas presentes:

### 1. Tablas en BD **sin mapping Hibernate**

| Tabla | Filas | Estado |
| --- | --- | --- |
| `credito_estatus` | 7 | **En uso activo** vía SQL nativo (`CreditosDao`, `ReporteDao`, `ReporteMorososDao`, `DescuentoNominaDao`). Solo le falta el `.hbm.xml`. |
| `ajuste` | 210 | Sin referencias en el código → residuo |
| `avales` | 0 | Sin referencias → residuo (los avales viven en `solicitud_avales`) |
| `bajasahorro` | 309 | Sin referencias → residuo |
| `creditos02` | 88 | Sin referencias → respaldo histórico |

### 2. Columnas en BD **fuera de los mappings**

| Tabla.columna | ¿Se usa en el código? |
| --- | --- |
| `solicitudes.sol_numero` | **Sí**, en `DoctosJasperSolicitudDao` (SQL nativo, alias `solicitudNumero` / `noSolicitud`). Es el folio impreso en los documentos Jasper. **No se puede leer desde el POJO `Solicitudes`.** |
| `amortizacion_estatus.amo_est_color` | Sí, como texto de clase CSS (`PEND`, `PAG`, `DFIN`, `TRA`, `INCO`, `REC`) |
| `pagos_estatus.pag_est_color` | Sí, igual (`EXACTO`, `MENOR`, `MAYOR`, `MASUNO`, …) |
| `tipo_transaccion.tipo_tran_tabla` | Sí |
| `amortizacion.amo_estatus_anterior`, `amortizacion.amo_estatus2` | **No** — columnas muertas |
| `pagos.pag_estatus_anterior` | **No** — columna muerta |
| `beneficiarios.ben_flag` | **No** |
| `movimientos.flag_abono` | **No** |

### 3. Valores de catálogo **ausentes en `Constantes.java`**

| Catálogo | Valor en BD | Falta en Constantes |
| --- | --- | --- |
| `solicitud_estatus` | **9 = EN REVISION** ("no aceptada por problemas de Avales o Documentos") | sí — y **hay 19 solicitudes en ese estado** |
| `amortizacion_estatus` | **14 = CANCELADO** | sí — 276 renglones |
| `pagos_estatus` | **13 = CAPITAL - AHORRO** | sí |
| `bancos_conceptos` | **16, 17 (INTERESES INVERSION BX), 18 (INVERSIONES), 19 (INTERESES INVERSION TCR)** | sí |
| `empresas` | **0 = OTR ("Otras")** | sí |
| `credito_estatus` | 7 se llama **AJUSTADO_DESC_EMP** ("ajustó con descuento de la empresa") | `Constantes` lo llama `CRE_EST_AJUST_FINIQ` — **nombres divergentes** |

Antes de agregar un estatus nuevo, revisa **la tabla de catálogo en BD**, no solo `Constantes`.

## Catálogos (contenido real)

### `roles`
`1` Administrador · `2` Auxiliar · `3` Usuario

### `empresas`
| id | Descripción | Abrev. |
| --- | --- | --- |
| 0 | Otras | OTR |
| 1 | AEROVIAS DE MEXICO SA DE CV | AMX |
| 2 | AM DL MRO JV, SAPI DE C.V. (TECH OPS) | TECH |
| 3 | SISTEMAS INTEGRALES DE SOPORTE TERRESTRE EN MEXICO | SIS |
| 4 | Aerovías Empresa de Cargo, S.A. de C.V. | CAR |
| 5 | SINDICATO | SND |

Distribución de socios: SIS 4,139 · TECH 3,824 · AMX 3,678 · CAR 729 · SND 10 · OTR 2.

### `productos` (11 filas — mezcla ahorro y crédito)
| id | Descripción | Siglas | Tipo |
| --- | --- | --- | --- |
| 1 | AHORRO FIJO | AF | ahorro |
| 2 | AHORRO NO FIJO | AN | ahorro |
| 3 | AHORRO VOLUNTARIO | AV | ahorro |
| 4 | CREDITO A FONDO AHORRO | FA | crédito |
| 5 | CREDITO AGUINALDO | AG | crédito |
| 6 | CREDITO NOMINA | NO | crédito |
| 7 | CREDITO AUTOMOVIL | AU | crédito |
| 8 | A F RENDIMIENTO | AFR | rendimiento |
| 9 | A NF RENDIMIENTO | ANFR | rendimiento |
| 10 | A V RENDIMIENTO | AVR | rendimiento |
| 11 | CREDITO SEGURO AUTO | SAU | crédito |

> `movimientos.mov_producto` usa 1/2/3 (ahorros). `creditos_final.cre_producto` usa 4/5/6/7/11.
> **Anomalía de datos:** existen 3 registros en `creditos_final` con `cre_producto = 8` (AFR) y
> 1 con `cre_producto = NULL`. Considera este ruido al escribir agregados sobre créditos.

`pro_siglas` se usa para construir la clave de crédito (`FondeosBo.generaCreClave`):
`{empAbreviacion}{AA}{proSiglas}{solId}` — ej. `AMX26NO11542`.

### Catálogos de estatus
Los contenidos completos de `solicitud_estatus`, `credito_estatus`, `amortizacion_estatus`,
`pagos_estatus`, `bancos_conceptos`, `parentesco_ben` y `tipo_transaccion` están en
[02-dominio-y-reglas.md](02-dominio-y-reglas.md), con las diferencias respecto a BD anotadas arriba.

### `tipo_transaccion` (35 filas) — auditoría
Es el catálogo de acciones auditadas en `registro_transaccion` (16,747 registros). Cada fila indica
la pantalla/acción y la tabla afectada. Ejemplos: `1 VALIDA SOLICITUD`, `4 FONDEAR CREDITO`,
`18 PAGO A CAPITAL`, `25 DAR DE BAJA USUARIO`, `33 EJECUTAR RENDIMIENTO`, `35 ASIGNAR PAGOS`.
Se escribe con `TransaccionBo.guardaTransaccion(idTipoTran, idSistema, idUsu)`.

### `tabulador` (24 filas)
Salarios de referencia por puesto y empresa (mensual / diario / catorcenal). Ej.
`AEROVIAS: Mecanico de Aviacion` → 21,825 mensual / 10,185 catorcenal. Tabla de consulta, sin
escritura desde la app.

### `configuracion` (1 fila)
```
con_id=1  con_fa_habilitado=1  con_ag_habilitado=1  con_fecha_mod=2026-01-15
```
Banderas globales para apagar los créditos FA y AG en mantenimiento. Se administra desde
`navegacion/administracion/panel.xhtml` (`AdminPanelBean.guardarBanderas`). El generador de la PK
es `assigned`: **la fila 1 debe existir siempre**; `ConfiguracionBo.getConfig()` la asume.

## Estado operativo actual (foto del 2026-08-29)

Útil para dimensionar cambios y detectar regresiones.

**Créditos** (`creditos_final`, 10,240):
| Producto | Activos | Pagados | Cancelados | Transferidos | Incobrables |
| --- | --- | --- | --- | --- | --- |
| NO (nómina) | 1,092 ($70.7M) | 6,371 | 227 | 330 | 63 |
| AU (auto) | 287 ($44.6M) | 449 | 53 | 32 | 8 |
| FA | 86 ($4.3M) | 1,393 | 59 | 7 | 3 |
| AG | 11 ($244k) | 264 | 16 | 1 | 5 |
| SAU | 0 | 93 | 1 | — | — |

**Solicitudes** (11,565): CERRADA 9,099 · RECHAZADA 2,890 · CREADA 57 · FONDEADA 52 ·
DOC APROBADA 36 · **EN REVISION 19** · FIRMA DOCUMENTOS 5 · ACEPTADA 4 · VALIDANDO 3.

**Amortización**: PAGADO 396,314 · PENDIENTE 69,639 · ABONO CREDITO 6,497 · PAGO MAYOR 6,439 ·
DEUDA_FINIQ 5,784 · EXTEMPORÁNEO 3,347 · A CAPITAL 2,318 · RECORRIDA 2,314 · ACUM 1,881 ·
TRANSFERIDO 1,780 · INCOBRABLE 363 · CANCELADO 276 · PAGO MENOR 42 · FINIQUITO 1.

**Pagos**: EXACTO 375,553 (96 %) · **SIN AMORTIZACION 7,474** · MAYOR 6,448 · MAS DE 1 CREDITO
4,358 · EXTEMPORÁNEO 2,795 · CAPITAL 2,702 · DEVOLUCIÓN 2,190 · ACUMULADO 1,810 · MENOR 690 ·
MAS DE 1 CRÉDITO Y ACUM 524 · **PENDIENTE 504** · FINIQUITO 1.

**Movimientos** (2.2M): APORTACION/fijo 1,400,204 · RENDIMIENTO/fijo 697,366 ·
APORTACION/no fijo 173,516 · RENDIMIENTO/no fijo 85,783 · RENDIMIENTO/voluntario 25,100 · resto
devoluciones y abonos. Hay 1,931 aportaciones con `mov_producto = NULL`.

**Bajas** (`baja_empleados`, 7,741): estatus 2 *ahorros por devolver* **6,206** · 3 completada 861 ·
1 pendiente 654 · 0 iniciada 154. El backlog de devolución de ahorros es la cola más grande
del sistema.

**Conciliación bancaria** (`bancos`, 39,731): ajustado 30,186 · **no ajustado 13,455** ·
parcial 149.

**Archivos** (`archivos_historial`, 2,610): pagos procesados 1,504 · aportaciones procesadas
1,356 · **pagos pendientes (estatus 1): 12**, el más reciente con catorcena `2025-05-26`.
Última catorcena procesada en general: **2026-08-20**.

**Rendimiento** (157): 171 ejecutados (estatus 1) desde 2012-03 hasta 2026-06-30; 2 en estatus 0
(precargados sin ejecutar: 2020-11-30 y 2025-12-31). Factor mensual típico 0.0077–0.0125.

**Catorcenas**: cargadas hasta **2027-12-23**, con 35 fechas futuras disponibles. Cuando se agoten
hay que precargar más o los flujos de amortización dejarán de encontrar catorcenas.

**Tokens de reset**: 96 usados (estatus 2) · 17 expirados (3) · 7 activos (1). El flujo se usa.

### Seguridad: contraseñas verificadas en texto plano

```
total 12,382 · nulos 2 · longitud mínima 2 · longitud máxima 18
hashes bcrypt (60 chars): 0 · hashes sha-256 (64 chars): 0
```

No hay ni un solo hash. Confirma lo que indica el código (`LoginBean` compara con `.equals`).

## Esquema de tablas (mappings efectivos)

Fuente: `src/main/resources/mx/com/evoti/hibernate/pojos/*.hbm.xml` (38 mappings).
Notación: `PK` llave primaria, `FK` relación declarada en Hibernate, `→` referencia lógica.

### Núcleo: socios y solicitudes

**`usuarios`** — PK `usu_id` · POJO `Usuarios`
```
usu_id PK          usu_numero_empleado(30)   usu_clave_empleado INT   ← identificador de login
usu_nombre(50)     usu_paterno(50)           usu_materno(50)
usu_edo_civil(20)  usu_correo(50)            usu_estado(2)            usu_rfc(15)
usu_empresa FK→empresas.emp_id
usu_puesto(50)     usu_telefono(20)          usu_extension(5)
usu_departamento(50) usu_area_trabajo(50)    usu_estacion(50)
usu_fecha_ingreso DATE          ← ingreso a la CAJA (antigüedad de caja)
usu_fecha_ingreso_empresa DATE  ← ingreso a la EMPRESA (antigüedad laboral)
usu_fecha_nacimiento DATE       usu_sexo(1)  usu_identificacion(30)
usu_celular(20) usu_municipio(50) usu_cp(5) usu_colonia(50) usu_calle(50)
usu_numext(10)  usu_numint(50)
usu_salario_neto DOUBLE
usu_password(200)          ← TEXTO PLANO
usu_primeravez INT         ← 1 = aún no completa su perfil; bloquea solicitar crédito
usu_habilitado INT         ← 0 = bloqueado para solicitar créditos
usu_omitir_validaciones INT← 1 = salta TODAS las validaciones de elegibilidad
usu_temporal(50)   usu_fecha_baja DATE
usu_ahorro_fijo / usu_ahorro_nofijo / usu_interes DOUBLE   ← saldos denormalizados
usu_flagunico INT  usu_estatus INT (1 activo / 0 baja)     usu_rol → roles.rol_id
1-N solicitudes (sol_usu_id)
```

> `usu_clave_empleado` es la **clave de negocio** (lo que el socio teclea al entrar), no `usu_id`.
> Puede repetirse entre empresas, por eso `LoginDao.login()` devuelve una **lista**.

**`solicitudes`** — PK `sol_id` (BIGINT) · POJO `Solicitudes`
```
sol_id PK
sol_producto  FK→productos.pro_id         sol_estatus FK→solicitud_estatus.sol_est_id
sol_usu_id    FK→usuarios.usu_id          sol_clave_empleado INT
sol_sueldo_neto / sol_deducciones / sol_monto_solicitado / sol_pago_credito
sol_pago_total / sol_aguinaldo / sol_fa / sol_intereses  DOUBLE
sol_catorcenas INT           sol_facatorcena INT  ← 6 (junio) o 12 (diciembre) en FA/AG
sol_banco(50) sol_numero_cuenta(50) sol_clabe_interbancaria(50) sol_referencia(50)
sol_no_poliza(50) sol_aseguradora(50) sol_nombre_tarjetahabiente(50)   ← seguro de auto
sol_observacion(200)         sol_motivo_rechazo(150)
sol_fecha_creacion / _autorizacion / _fondeo / _enviodocumentos /
sol_fecha_deposito / _cancelacion / _ult_catorcena   DATE
sol_estatus_db INT           ← bandera interna de avance (1 creada, 2 completa, 3 seguro auto)
sol_formato_doc_firmada INT
sol_numero                   ← ¡EXISTE EN BD pero NO en el mapping! folio de documentos Jasper
1-N solicitud_avales (sol_ava_solicitud)   1-N imagenes (ima_solicitud)
```

**`solicitud_avales`** — PK `id_sol_ava`
```
sol_ava_solicitud FK→solicitudes.sol_id
sol_ava_clave_empleado INT   sol_ava_id_empleado → usuarios.usu_id
sol_ava_credito INT          ← se llena al fondear (FondeosBo.updtSolAvalesCreId)
sol_ava_estatus INT          ← 1 PEND / 2 VALIDANDO / 3 APROBADO / 4 RECHAZADO
```

**`imagenes`** — PK `ima_id`
```
ima_solicitud FK→solicitudes.sol_id
ima_imagen(500)         ← nombre de archivo: {usuId}_{TIPO}_{ddMMyy}.pdf
ima_tipoimagen INT      ← 1 IFE, 2 DOMICILIO, 3 NOMINA, 4 EDOCTA, 5 AVAL, 6 FA, 7 AG, 8 FIRMADA
ima_estatus INT         ← 1 PEND / 2 VALIDANDO / 3 APROBADA / 4 RECHAZADO
ima_observaciones(500)
```

**`beneficiarios`** — PK `ben_id`
```
ben_nombre/paterno/materno(50)  ben_parentesco → parentesco_ben.par_id
ben_pct DOUBLE   ← porcentaje asignado (debe sumar 100 entre los beneficiarios del socio)
ben_direccion(100) ben_telefono(20) ben_celular(20)
ben_usu_id → usuarios.usu_id
```

### Créditos y cobranza

**`creditos_final`** — PK `cre_id` · POJO `CreditosFinal` — **la tabla de créditos vigente**
```
cre_id PK
cre_usu_id → usuarios.usu_id       cre_clave_empleado INT     cre_nombre(50)
cre_solicitud → solicitudes.sol_id cre_producto → productos.pro_id
cre_empresa(50)                    ← abreviación en TEXTO, no el id
cre_clave(50)                      ← {ABREV}{AA}{SIGLAS}{solId}, ej. AMX26NO11542
cre_tipo(20)                       cre_prestamo DOUBLE (monto original)
cre_catorcenas INT                 cre_pago_quincenal DOUBLE (pago catorcenal)
cre_saldo DOUBLE
cre_estatus → credito_estatus.cre_est_id (1..7)
cre_padre → creditos_final.cre_id  ← crédito origen cuando se transfiere a un aval
cre_fecha_primer_pago / cre_fecha_deposito / cre_fecha_incobrable /
cre_fecha_nuevo_monto  DATE
1-N amortizacion (amo_credito, cascade=all)
```

> `creditos` (0 filas) y `creditos02` (88) son tablas **históricas muertas**. Todo el sistema usa
> `creditos_final`.

**`amortizacion`** — PK `amo_id` · POJO `Amortizacion` — 460k filas
```
amo_credito FK→creditos_final.cre_id (cascade all desde el crédito)
amo_numero_pago INT       amo_fecha_pago DATE   ← debe ser una fecha de `catorcenas`
amo_capital / amo_amortizacion / amo_interes / amo_iva / amo_monto_pago / amo_saldo  DOUBLE
amo_estatus(20)           ← TEXTO ('PENDIENTE','PAGADO','PAGO MAYOR','PAGO MENOR',…)
amo_estatus_int INT       ← ENTERO paralelo → amortizacion_estatus.amo_est_id
amo_pago_id → pagos.pag_id   ← NULL mientras no se asigne un pago
amo_usu_id → usuarios.usu_id  amo_clave_empleado INT
amo_solicitud BIGINT      amo_producto INT
amo_estatus_anterior, amo_estatus2   ← en BD, sin mapear, sin uso
```

> `amo_estatus` (texto) y `amo_estatus_int` **deben mantenerse sincronizados**. Todo el SQL del
> algoritmo de pagos actualiza los dos en el mismo `UPDATE`. Si escribes solo uno, rompes
> reportes que filtran por el otro.
> `amo_iva` siempre vale `0.0` — el cálculo no aplica IVA.

**`pagos`** — PK `pag_id` · 331k filas
```
pag_clave_empleado INT    pag_usu_id → usuarios.usu_id   ← se resuelve DESPUÉS de insertar
pag_usu_nombre(200)       pag_empresa → empresas.emp_id
pag_fecha DATE            ← catorcena del pago
pag_deposito DOUBLE       ← lo que la empresa descontó
pag_acumulado DOUBLE      ← saldo a favor tras aplicar la amortización
pag_credito → creditos_final.cre_id   ← lo asigna el algoritmo
pag_estatus → pagos_estatus.pag_est_id
pag_estatus_amortizacion INT   pag_arh_id → archivos_historial.arh_id
antes7 INT                pag_estatus_anterior ← en BD, sin mapear, sin uso
```

**`archivos_historial`** — PK `arh_id` — control de cargas
```
arh_nombre_archivo(500)  ← se valida que no se repita
arh_fecha_subida DATE    arh_fecha_catorcena DATE
arh_empresa → empresas.emp_id
arh_tipo_archivo INT     ← 1 pagos, 2 aportaciones
arh_estatus INT          ← 1 pendiente de procesar, 2 procesado
arh_registros INT
```

**`catorcenas`** — PK `car_id` — calendario maestro
```
car_fecha DATE (única real)  car_dia / car_mes / car_anio INT
car_inversiones INT
```

### Ahorro y rendimiento

**`movimientos`** — PK `mov_id` — 2.2M filas, la tabla más grande
```
mov_usu_id → usuarios.usu_id     mov_clave_empleado INT   mov_nombre_empleado(200)
mov_empresa → empresas.emp_id    mov_fecha DATE
mov_deposito DOUBLE              ← NEGATIVO en devoluciones y abonos
mov_producto INT                 ← 1 fijo, 2 no fijo, 3 voluntario
mov_ar INT                       ← 1 aportación, 2 rendimiento
mov_tipo(20)                     ← APORTACION|RENDIMIENTO|DEVOLUCION|ABONO CREDITO|ABONO CAPITAL
mov_arh_id → archivos_historial.arh_id
mov_id_padre → movimientos.mov_id  ← rendimiento ligado a su aportación voluntaria
mov_estatus INT   mov_bandera INT  mov_cambioanfaf INT
flag_abono        ← en BD, sin mapear, sin uso
```
El saldo de ahorro de un socio es `SUM(mov_deposito)` filtrando por producto.

**`rendimiento`** — PK `ren_id` — cierre mensual
```
ren_fecha DATE (fin de mes)      ren_estatus INT (0 precargado / 1 ejecutado)
ren_interes DOUBLE               ← total de pagos del mes (ingresos por intereses de crédito)
ren_acumulado DOUBLE             ← base total de ahorro del mes
ren_factor DOUBLE                ← interesNeto / acumuladoTotal  (se aplica a cada socio)
ren_intereses_inversion DOUBLE   ← capturado a mano
ren_comisiones_bancarias DOUBLE  ← capturado a mano
ren_reserva DOUBLE               ← 10 % del interés total
ren_ganancia_neta DOUBLE         ← 90 % del interés total
```
`rendimiento3` (0 filas) es una versión antigua sin los campos de inversión/comisión.

### Bajas y finiquitos

**`baja_empleados`** — PK `bae_id`
```
bae_id_empleado → usuarios.usu_id   bae_estatus INT (0..3)
bae_deuda_creditos / bae_ahorros / bae_monto_finiquito DOUBLE  ← snapshot al dar de baja
bae_banco(50) bae_cuenta(50) bae_clabe(50)   ← destino del depósito
bae_fecha_baja / _creacion / _administracion / _pdf / _correo / _deposito DATE
bae_ruta_archivo(500)   ← PDF de finiquito generado
```

**`devoluciones`** (0 filas) — `dev_acum_id`, `dev_monto`. Tabla en desuso: las devoluciones
reales se registran como `movimientos` negativos.

### Conciliación bancaria

**`bancos`** — PK `ban_id` — lo que el SISTEMA cree que pasó en el banco
```
ban_concepto → bancos_conceptos.cban_id
ban_id_concepto_sistema INT   ← PK POLIMÓRFICA hacia la tabla que indica cban_tabla
ban_monto DOUBLE              ← negativo en salidas (depósitos de crédito, devoluciones)
ban_empresa INT               ban_fechatransaccion DATE
ban_ajustado INT              ← 0 no / 1 sí / 2 parcial
ban_id_relacion BIGINT        ← UUID que agrupa el ajuste   ban_fecha_relacion DATE
```

**`estado_cuenta`** — PK `ec_id` — lo que el BANCO reporta
```
ec_concepto INT   ec_monto DOUBLE   ec_empresa INT   ec_fechatransaccion DATE
ec_descripcion(100)  ec_ajustado INT (0/1/2)
ec_padre → estado_cuenta.ec_id
ec_id_relacion BIGINT   ec_fecha_relacion DATE
```

**`rel_banco_edocta`** (0 filas) — PK compuesta `(rbe_id, rbe_id_banco_ec, rbe_tipo)`,
`rbe_fecha_rel`. `rbe_tipo`: `1` = registro de `bancos` (`RBE_BANCO`), `2` = de `estado_cuenta`
(`RBE_EC`). Es la tabla puente de la conciliación, pero **está vacía**: en la práctica la relación
se lleva por `ban_id_relacion` / `ec_id_relacion`.

**`banco_edocta`** (0 filas) — PK compuesta `(bec_id_banco, bec_id_edocta)` + `bec_fecha_transaccion`.
También en desuso.

### Auditoría y soporte

**`bitacora`** — PK `bit_id` — notas de negocio visibles al socio
```
bit_tipo INT (1..5)   bit_titulo(100)   bit_nota(500)   bit_fecha DATE
bit_usuario → usuarios.usu_id
bit_referencia BIGINT     ← normalmente sol_id
bit_subreferencia BIGINT  ← id de aval o documento
```

**`registro_transaccion`** — PK `tran_id` — auditoría técnica (16,747 filas)
```
tran_id_usuario → usuarios.usu_id   tran_fecha DATE
tran_tipo_tran → tipo_transaccion.tipo_tran_id (1..35)
tran_id_sistema INT   ← id del objeto afectado (solicitud, crédito, movimiento…)
```

**`altas_cambios_hist`** — PK `cnh_id` — historial de altas y cambios de empresa
```
cnh_usu_id → usuarios.usu_id   cnh_arh_id → archivos_historial.arh_id
cnh_tipo INT (1 nuevo / 2 cambio)
cnh_clave_anterior / cnh_clave_actual INT
cnh_empresa_anterior / cnh_empresa_actual INT
cnh_catorcena_transaccion DATE   cnh_mov_id → movimientos.mov_id   cnh_fecha DATE
```

**`password_reset_tokens`** — PK `prt_id` — DDL en `docs/sql/password_reset_tokens.sql`
```
prt_usu_id → usuarios.usu_id       prt_token_hash VARCHAR(128)  ← SHA-256 hex del token
prt_fecha_creacion / prt_fecha_expira / prt_fecha_uso DATETIME
prt_estatus INT   ← 1 activo · 2 usado · 3 cancelado/expirado
prt_ip_solicitud(45)   prt_user_agent(255)
índices: idx_prt_token_hash, idx_prt_usu_id, idx_prt_expira
```

> El token **plano nunca se guarda**: solo su hash SHA-256. Vigencia 10 minutos
> (`PasswordRecoveryBo.TOKEN_MINUTOS_VIGENCIA`). Al pedir uno nuevo, los activos previos pasan a 3.
> Es el único punto del sistema que aplica criptografía correctamente — pero la contraseña
> resultante se guarda en texto plano.

**`usuarios_resp`** (0 filas) — copia de respaldo de `usuarios`, sin uso.
**`cargos`** (0 filas), **`bitacora_transacciones`** (0 filas) — sin uso.

## Consultas útiles para diagnóstico

```sql
-- Créditos activos de un socio (por clave de empleado)
SELECT c.cre_id, c.cre_clave, p.pro_siglas, c.cre_prestamo, c.cre_saldo, ce.cre_est_nombre
FROM creditos_final c
JOIN usuarios u ON u.usu_id = c.cre_usu_id
LEFT JOIN productos p ON p.pro_id = c.cre_producto
LEFT JOIN credito_estatus ce ON ce.cre_est_id = c.cre_estatus
WHERE u.usu_clave_empleado = ? AND c.cre_estatus = 1;

-- Amortización pendiente de un crédito
SELECT amo_numero_pago, amo_fecha_pago, amo_monto_pago, amo_estatus, amo_estatus_int, amo_pago_id
FROM amortizacion WHERE amo_credito = ? ORDER BY amo_numero_pago;

-- Saldo de ahorro por producto
SELECT mov_producto, SUM(mov_deposito) saldo
FROM movimientos WHERE mov_usu_id = ? GROUP BY mov_producto;

-- Pagos que quedaron sin asignar en un archivo
SELECT pag_id, pag_clave_empleado, pag_fecha, pag_deposito, pag_acumulado
FROM pagos WHERE pag_arh_id = ? AND pag_estatus = 7;

-- Archivos de pagos atascados (nunca procesados)
SELECT arh_id, arh_nombre_archivo, arh_fecha_catorcena, arh_empresa, arh_registros
FROM archivos_historial WHERE arh_tipo_archivo = 1 AND arh_estatus = 1;

-- Catorcenas futuras disponibles
SELECT car_fecha FROM catorcenas WHERE car_fecha >= CURDATE() ORDER BY car_fecha;
```
