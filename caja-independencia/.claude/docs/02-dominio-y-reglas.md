# 02 — Dominio y reglas de negocio

## Qué es el sistema

Una **caja de ahorro sindical**. Los trabajadores afiliados (empleados de varias empresas del
grupo aeronáutico) aportan dinero vía descuento de nómina y, a cambio, pueden solicitar créditos.
El sistema administra:

- El **ahorro** de cada socio (fijo, no fijo y voluntario) y el **rendimiento** que genera.
- El ciclo de vida de **solicitudes de crédito** → autorización → fondeo → crédito activo.
- La **cobranza catorcenal** vía archivos de nómina que las empresas entregan.
- **Bajas y finiquitos** cuando un socio deja la empresa o la caja.
- La **conciliación bancaria** entre lo que el sistema registra y el estado de cuenta real.

## La catorcena: unidad de tiempo del negocio

Todo el sistema opera en **catorcenas** (periodos de 14 días), no en quincenas ni meses. La tabla
`catorcenas` es el calendario maestro: cada fila es una fecha de pago válida.

- Un año tiene **26 catorcenas** (`TablaAmortizacionBo.TIPO_PAGOS = 26`).
- Las fechas de amortización, los archivos de pago y las validaciones de fecha se comparan contra
  esta tabla (`Util.validaSiEsCatorcena`, `BaseBean.validaSiEsCatorcena`).
- Si una fecha capturada no existe en `catorcenas`, el flujo se rechaza.

## Empresas (`empresas`)

Los ids son constantes de negocio duras (`Constantes`):

| id | Constante | Abreviación |
| --- | --- | --- |
| 1 | `AMX` | AEROMEXICO |
| 2 | `TECH` | TECH OPS |
| 3 | `SIS` | SISTEM |
| 4 | `CAR` | CARGO |
| 5 | `SND` | (Sindicato) |

La empresa determina reglas: número de avales requeridos, agrupación de archivos de pago, y
columna `pag_empresa` / `mov_empresa` / `ban_empresa`.

## Roles (`roles`)

| id | Constante | Nombre |
| --- | --- | --- |
| 1 | `ROL_USR_ADMON_I` | Administrador |
| 2 | `ROL_USR_AUX_I` | Auxiliar |
| 3 | `ROL_USR_USR_I` | Usuario |

**Cómo se autoriza:** no hay filtros ni interceptores. El menú (`WEB-INF/layoutmenu.xhtml`) oculta
las secciones administrativas con `rendered="#{navigationController.usuario.rol != 'Usuario'}"`.
Es decir, la autorización es **solo visual, comparando el nombre del rol como cadena**. Un
`Usuario` que escriba la URL de una pantalla de administración llega a ella. Cualquier trabajo de
seguridad debe empezar aquí.

## Productos de crédito

| id | Constante | Producto | Plazo | Tope |
| --- | --- | --- | --- | --- |
| 4 | `FA` | Fondo de Ahorro | hasta catorcena elegida (jun/dic) | $125,000 |
| 5 | `AG` | Aguinaldo | hasta última catorcena de diciembre | $125,000 |
| 6 | `NO` | Nómina | hasta **78** catorcenas (3 años) | $130,000 |
| 7 | `AU` | Automóvil | hasta **130** catorcenas (5 años) | $300,000 |
| 11 | `SAU` | Seguro de Auto | como FA/AG | — |

Los topes y catorcenas máximas están en `SolicitudCreditoBean.muestraSimulador()`.

> **Inconsistencia real en el código:** en `aplicaValidacionesPreGuardado()` la validación de auto
> rechaza montos `> 300000` pero el mensaje al usuario dice *"no puede superar los $150,000"*.
> Además hay validaciones de sueldo neto y de "libre por catorcena" comentadas.

### FA y AG: créditos "a vencimiento"

FA (Fondo de Ahorro) y AG (Aguinaldo) **no se amortizan catorcena a catorcena**. Se liquidan de
golpe cuando la empresa paga el fondo de ahorro (junio) o el aguinaldo (diciembre):

- La tabla de amortización se calcula sobre las catorcenas intermedias solo para **acumular
  intereses**; capital, amortización y monto de pago quedan en `0.0` en cada renglón.
- Al guardar el crédito se persiste **un único renglón de amortización** con el total
  (`TablaAmortizacionBo.getUltAmortFAAGSAU()`), con fecha de pago = última catorcena.
- Por eso el algoritmo de asignación de pagos filtra `cre_producto in (6,7)`: **FA, AG y SAU nunca
  entran al algoritmo automático de pagos.**

La elección de catorcena destino se controla con `sol_facatorcena`:
- AG → siempre `12` (diciembre), forzado en `SolicitudCreditoBean.creaSolicitud()` y en
  `FondeosBo.creaAmortizacion()`.
- FA → `Constantes.FA_CAT_JUN = 6` (junio) o 12, según cuándo se solicite.

Ventana temporal (`SolicitudCreditoBean.configuraSolFa()`):
- Después de la **primera catorcena de mayo** ya no se puede elegir julio (`rdrEnableJulio=false`).
- Después de la **primera catorcena de noviembre** ya no se puede elegir diciembre.

## Tasas de interés

Definidas como constantes privadas en `mx.com.evoti.bo.TablaAmortizacionBo`:

| Constante | Valor | Aplica a |
| --- | --- | --- |
| `TASA` / `TASA_PORCENTAJE` | **18 % anual** | Nómina (6), FA (4), AG (5), SAU (11) |
| `TASA_AUTO` / `TASA_AUTO_PORCENTAJE` | **12 % anual** | Automóvil (7) |
| `TASA_ESPECIAL` / `TASA_ESPECIAL_PORCENTAJE` | **10 % anual** | promoción puntual (ver abajo) |
| `TIPO_PAGOS` | 26 | pagos por año (catorcenas) |

### Fórmulas

**Tasa por catorcena** = tasa anual / 26

**Pago catorcenal** (anualidad estándar), `generaMontoPago()`:
```
i = tasaAnual / 26
pago = monto * ( i * (1+i)^n ) / ( (1+i)^n - 1 )
```

**Interés de cada renglón**, `generaInteres()`:
```
interes = capitalPendiente * tasaAnualEntera / (26 * 100)
```

**Amortización a capital** = `pago - interes` (el IVA siempre es `0.0`).

El capital del renglón `i` es `capital(i-1) - amortizacion(i-1)`.

**Para FA/AG/SAU** (`generaTablaAmortizacionAgFA`): no hay anualidad. Cada catorcena intermedia
genera `interes = monto * 18 / 2600` sobre el **monto completo** (no decrece), y el total a pagar
es `monto + suma(intereses)`.

### La "tasa especial" del 2018-11-11

`TASA_ESPECIAL = 10%` se activa con un `if` que compara si la fecha de creación de la solicitud
es exactamente **11 de noviembre de 2018**:

```java
cal1.setTime(Util.generaFechaDeString("2018-11-11"));
cal2.setTime(solicitud.getSolFechaCreacion());
boolean sameDay = cal1.DAY_OF_YEAR == cal2.DAY_OF_YEAR && cal1.YEAR == cal2.YEAR;
int tasaEspecial = sameDay ? 1 : 0;
```

Está duplicado en `AmortizacionBean.generaAmortizacion()` (comparando contra *hoy*) y en
`FondeosBo.fondeaSolicitud()` (comparando contra la fecha de la solicitud). Es código muerto para
todo crédito nuevo, pero **sigue afectando el recálculo de créditos históricos de esa fecha**.
No lo elimines sin revisar si quedan créditos vivos originados ese día.

### Redondeo

- `RoundingMode.DOWN` a 2 decimales para monto de pago, interés y amortización.
- `RoundingMode.UP` a 2 decimales para el capital.
- Las comparaciones monetarias usan **tolerancias de ±1 peso** (`diferencia <= 1 && >= -1`) para
  absorber estos redondeos. Es intencional; respétalo al tocar comparaciones de montos.

## Reglas de elegibilidad para solicitar crédito

Implementadas en `SolicitudBo.validaAntiguedadYCreditos()` y aplicadas en
`SolicitudCreditoBean.validacionesInicialesRdr()`. El resultado son 4 banderas
(`dsblBtnNo/Au/Fa/Ag`) que habilitan o no cada botón de producto.

### A. Puertas previas (bloquean todo)

Se evalúan en este orden; la primera que aplica gana:

1. `usu_primeravez == 1` → "complete su información en Mi Perfil".
2. Tiene solicitudes **incompletas** (estatus 1) → debe terminarlas primero.
3. `usu_omitir_validaciones == 1` **y** `usu_habilitado == 1` → **se saltan todas las
   validaciones**, los 4 productos quedan habilitados ("autorizado de manera extraordinaria").
4. Es **moroso** → bloqueado.
5. `usu_habilitado == 0` → bloqueado ("ha sido bloqueado, contacte a la administración").
6. En caso contrario, se corren las validaciones de antigüedad y créditos.

Además, banderas globales de mantenimiento (tabla `configuracion`) pueden apagar FA y AG para
todos: `con_fa_habilitado` / `con_ag_habilitado`.

### B. Antigüedad (`validaAntiguedad`)

Se mide en **días** contra `usu_fecha_ingreso` (caja) y `usu_fecha_ingreso_empresa`:

| Condición | Efecto |
| --- | --- |
| Antigüedad en caja ≤ **90 días** | Bloquea **los 4** productos |
| Antigüedad en empresa ≤ **365 días** | Bloquea Nómina y Auto |
| Antigüedad en empresa ≤ **1825 días** (5 años) **o** en caja ≤ 365 días | Bloquea Auto |

### C. Créditos y solicitudes activas (`validaSolicitudesCreditosActivos`)

| Situación | Puede solicitar |
| --- | --- |
| Tiene una solicitud pendiente de autorización | Nada |
| Tiene **≥ 2** créditos activos | Nada |
| Tiene 1 crédito de **Nómina (6)** | FA y AG siempre; Nómina **solo si ya pagó ≥ 50 % del capital**; Auto nunca |
| Tiene 1 crédito de **Auto (7)** | Solo FA y AG |
| Tiene 1 crédito de FA o AG | Los 4 productos |
| Sin créditos | Los 4 productos |

**Cálculo del 50 %:** se suma `amo_amortizacion` de los renglones con
`amo_estatus_int in (2,3,4,5,6,7,8)` (todos los estados que implican pago aplicado) y se compara
`suma * 2 >= cre_prestamo`.

### D. Morosidad (`SolicitudBo.validaMorosidad`)

1. Obtiene la catorcena del **último archivo de pagos** cargado para la empresa del usuario
   (`ArchivosHistorialDao.getCatorUltArchivo`).
2. Consulta `MorosoDao.getCatorcenasAdeudadas(catorcena, idUsuario)`.
3. Es moroso si el resultado es `> 0`.

Si no hay archivos cargados para esa empresa, **no es moroso** (regresa `false`).

## Avales requeridos

`SolicitudBo.determinaAvalNoAgFaXEmpresa(empresa, monto)`:

| Empresa | Avales |
| --- | --- |
| 1 (AEROMEXICO) | 1 si monto < $20,000; 2 si ≥ $20,000 |
| 5 (SINDICATO) | 2 |
| Cualquier otra | 3 |

**Excepción:** si el producto es **Auto (`au`)**, se fuerza a **3 avales** sin importar la empresa.

Al crear la solicitud se generan, por cada aval, un registro en `solicitud_avales` (estatus 1
PENDIENTE) y un registro `imagenes` de tipo 5 (AVAL) para el documento.

## Catálogos de estatus

### Estatus de solicitud (`solicitud_estatus`)

Derivado de `Constantes` + `DetalleSolicitudBo.getSolsByUsuId()`:

| id | Significado | Observación mostrada al socio |
| --- | --- | --- |
| 1 | **Creada / incompleta** | "Aún no ha terminado de llenar su solicitud…" |
| 2 | **Validando** | "Su solicitud se encuentra pendiente de autorización" |
| 3 | **Aceptada** (`SOL_EST_ACEPTADA`) | "Su solicitud fue aceptada…" |
| 4 | **Fondeada** (`SOL_EST_FONDEADA`) | "Por favor descargue los siguientes documentos" |
| 5 | **Documentos enviados** (`SOL_EST_DOCTOS_ENV`) | "Sus documentos ya fueron enviados" |
| 6 | **Cerrada** (depósito realizado) | "Fecha de depósito: …" |
| 7 | **Rechazada** (`SOL_EST_RECHAZADA`) | "Motivo de rechazo: " + notas de bitácora |
| 8 | **Documentos aprobados** (`SOL_EST_DOCTOS_APROBADOS`) | — |

Las solicitudes con estatus **6 y 7 son terminales** (`where s.sol_estatus not in (6,7)`).

Existe además `sol_estatus_db`, un campo paralelo que se usa como bandera interna de avance
(1 al crear, 2 cuando el detalle está completo → dispara el paso a estatus 2, 3 en seguro de auto).

### Estatus de crédito (`credito_estatus`)

| id | Constante | Significado |
| --- | --- | --- |
| 1 | `CRE_EST_ACTIVO` | Activo |
| 2 | `CRE_EST_PAGADO` | Pagado / liquidado |
| 3 | `CRE_EST_CANCELADO` | Cancelado |
| 4 | `CRE_EST_TRANSFERIDO` | Transferido a los avales |
| 5 | `CRE_EST_INCOBRABLE` | Incobrable |
| 6 | `CRE_EST_AJUSTADO` | Ajustado |
| 7 | `CRE_EST_AJUST_FINIQ` | Ajuste por finiquito |

### Estatus de amortización (`amortizacion_estatus`, campo `amo_estatus_int`)

| id | Significado |
| --- | --- |
| 1 | Pendiente |
| 2 | Pagado |
| 3 | Pago menor |
| 4 | Pago mayor |
| 5 | Pago acumulado |
| 6 | Extemporáneo |
| 7 | Capital (pago a capital) |
| 8 | Abono a crédito |
| 9 | Finiquito |
| 10 | Deuda final |
| 11 | Transferencia |
| 12 | Incobrable |
| 13 | Recorrida |

`amo_estatus` (texto: `'PENDIENTE'`, `'PAGADO'`, `'PAGO MAYOR'`, `'PAGO MENOR'`…) se mantiene en
paralelo al entero. **Al escribir uno hay que escribir el otro**; el SQL del algoritmo de pagos
siempre actualiza ambos.

### Estatus de pago (`pagos_estatus`, campo `pag_estatus`)

| id | Constante | Significado |
| --- | --- | --- |
| 1 | `PAGEST_PEND_1` | Pendiente de asignar |
| 2 | `PAGEST_EXACT_2` | Pago exacto |
| 3 | `PAGEST_MENOR_3` | Pago menor a la amortización |
| 4 | `PAGEST_MAYOR_4` | Pago mayor a la amortización |
| 5 | `PAGEST_MD1_5` | Cubre exactamente n créditos |
| 6 | `PAGEST_MD1YA_6` | Cubre n créditos y sobra acumulado |
| 7 | `PAGEST_SAMO_7` | **Sin amortización** que asignar |
| 8 | `PAGEST_ACUM_8` | Acumulado |
| 9 | `PAGEST_CAPITAL_9` | Pago a capital |
| 10 | `PAGEST_EXTEMP_10` | Extemporáneo |
| 11 | `PAGEST_DEVOL_11` | Devolución |
| 12 | `PAGEST_FINIQ_12` | Finiquito |

### Estatus de baja de empleado (`baja_empleados.bae_estatus`)

| id | Constante | Significado |
| --- | --- | --- |
| 0 | `BAJA_INICIADA` | Iniciada |
| 1 | `BAJA_PENDIENTE` | Pendiente (tiene deuda de créditos ≥ $5) |
| 2 | `BAJA_AHORROSXDEVOLVER` | Ahorros por devolver (≥ $5) |
| 3 | `BAJA_COMPLETADA` | Completada |

La transición la decide `FiniquitoService.actualizarSnapshotBajaInicial()`: primero deuda, luego
ahorros, si no hay nada → completada.

### Estatus de archivo (`archivos_historial.arh_estatus`)

- `1` = pendiente de procesar (archivos de **pagos** al cargarse)
- `2` = procesado (archivos de **aportaciones** nacen así; los de pagos pasan a 2 al terminar el
  algoritmo)

`arh_tipo_archivo`: `1` = pagos, `2` = aportaciones/movimientos.

### Ajuste bancario (`ban_ajustado` / `ec_ajustado`)

| valor | Constante | Significado | Clase CSS |
| --- | --- | --- | --- |
| 0 | `BAN_NO_AJUSTADO` / `EC_NO_AJUSTADO` | No conciliado | — |
| 1 | `BAN_AJUSTADO` / `EC_AJUSTADO` | Conciliado | `ajustado` |
| 2 | `BAN_AJUSTADO_PARCIAL` / `EC_AJUSTADO_PARCIAL` | Conciliado parcialmente | `ajustado-parcial` |

## Productos de ahorro (`movimientos.mov_producto`)

| id | Constante | Significado |
| --- | --- | --- |
| 1 | `MOV_PRODUCTO_F` | Ahorro **fijo** |
| 2 | `MOV_PRODUCTO_NF` | Ahorro **no fijo** |
| 3 | `MOV_PRODUCTO_VOL` | Ahorro **voluntario** |

`mov_ar` distingue el origen del movimiento: `1` = aportación (`MOV_AR_APO`),
`2` = rendimiento (`MOV_AR_RDTO`).

`mov_tipo` (texto): `APORTACION`, `RENDIMIENTO`, `DEVOLUCION`, `ABONO CREDITO`, `ABONO CAPITAL`.

Las **devoluciones se registran como movimientos con `mov_deposito` negativo**
(`FiniquitoService.crearMovimientoDesdeDto` multiplica por -1). El saldo de ahorro es la suma
algebraica de los movimientos.

## Conceptos de banco (`bancos_conceptos`, campo `ban_concepto`)

| id | Constante | Concepto |
| --- | --- | --- |
| 1 | `BAN_PAGOSARCHIVO` | Total de un archivo de pagos |
| 2 | `BAN_APORTACIONARCHIVO` | Total de un archivo de aportaciones |
| 4 | `BAN_PAGO_EXTMP` | Pago extemporáneo |
| 5 | `BAN_PAGO_CAPITAL` | Pago a capital |
| 6 | `BAN_DEV_ACUMULADO` | Devolución de acumulado |
| 7 | `BAN_AP_VOL` | Aportación voluntaria |
| 8 | `BAN_DEV_A_FIJO` | Devolución de ahorro fijo |
| 9 | `BAN_DEV_A_N_FIJO` | Devolución de ahorro no fijo |
| 10 | `BAN_DEV_A_VOL` | Devolución de ahorro voluntario |
| 11 | `BAN_DEV_RDTO_FIJO` | Devolución de rendimiento de ahorro fijo |
| 12 | `BAN_DEP_CREDITO` | Depósito de un crédito (monto **negativo**: sale dinero) |
| 13 | `BAN_DESCTOXCOB_CRED` | Descuento por cobrar de créditos |
| 14 | `BAN_DEV_RDTO_N_FIJO` | Devolución de rendimiento de ahorro no fijo |
| 15 | `BAN_DEV_RDTO_VOL` | Devolución de rendimiento de ahorro voluntario |

## Tipos de documento de solicitud (`imagenes.ima_tipoimagen`)

| id | Constante | Documento |
| --- | --- | --- |
| 1 | `DOC_ID_INT` | Identificación (IFE) |
| 2 | `DOC_DOMICILIO_INT` | Comprobante de domicilio |
| 3 | `DOC_NOMINA_INT` | Recibo de nómina |
| 4 | `DOC_EDOCTA_INT` | Estado de cuenta |
| 5 | `DOC_AVAL_INT` | Documento de aval (uno por aval) |
| 6 | `DOC_FA_INT` | Comprobante de fondo de ahorro |
| 7 | `DOC_AG_INT` | Comprobante de aguinaldo |
| 8 | `DOC_FIRMADA_INT` | Documentación firmada |

Estatus de imagen (`ima_estatus`): 1 PENDIENTE, 2 VALIDANDO, 3 APROBADA, 4 RECHAZADO
(constantes `IMA_STT_*`).

**Nombre del archivo generado:** `{idUsuario}_{TIPO}_{ddMMyy}.pdf`
(`SolicitudBo.generaNombreImg`).

Rutas de almacenamiento (`Constantes`):
- `PATH_DOCTOS = "c:/Documentos"` (Windows, producción)
- `PATH_DOCTOS_LOCAL = "/Users/ivettemanzano/Projects/Documentos"` (desarrollo)

> Ambas rutas están **hardcodeadas**; hay que elegir manualmente cuál usa cada flujo.

## Estatus de aval (`solicitud_avales.sol_ava_estatus`)

| id | Constante | Estatus |
| --- | --- | --- |
| 1 | `SOLAVA_PENDIENTE_I` | PENDIENTE |
| 2 | `SOLAVA_VALIDANDO_I` | VALIDANDO |
| 3 | `SOLAVA_APROBADO_I` | APROBADO |
| 4 | `SOLAVA_RECHAZADO_I` | RECHAZADO |

## Tipos de bitácora (`bitacora.bit_tipo`)

| id | Constante | Evento |
| --- | --- | --- |
| 1 | `BIT_SOL_AVALRECH` | Aval rechazado |
| 2 | `BIT_SOL_DOCTORECH` | Documento rechazado |
| 3 | `BIT_SOL_RECHAZADA` | Solicitud rechazada |
| 4 | `BIT_CRE_CANCELADO` | Crédito cancelado |
| 5 | `BIT_NOTA_USUARIO` | Nota libre sobre el usuario |

`bit_referencia` suele ser el `sol_id`; `bit_subreferencia` el detalle (id de aval o documento).

## Correo

Toda la mensajería sale de `mx.com.evoti.bo.util.EnviaCorreo`, con SMTP y credenciales
**hardcodeadas**:

```
host  mail.cajaindependencia.com   puerto 26   auth true
from  contacto@cajaindependencia.com
```

Buzones institucionales en `Constantes`: `contacto@`, `jeffrey.deltoro@`, `asistente@`
(`EMAIL_1..3`).

Plantillas de mensaje (constantes con `String.format`):
- `CUERPO` / `ASUNTO` — acuse de solicitud registrada
- `SOLICITUD_AUTORIZADA` / `SOLICITUD_RECHAZADA`
- `MSJ_CUERPO_FONDEADA` — lista de documentos a firmar (ANEXO A/B/C, AVISO, SOLICITUD)
- `CREDITOFONDEADO`, `CANCELACIONCREDITO`, `DOCTO_FIRMADO_RECHAZADO`
- `sendPasswordResetMessage(nombre, liga, email)` — recuperación de contraseña

El texto firma como *"Juan Bernardo Carmona Ávila — Presidente Caja de Ahorro"*.
