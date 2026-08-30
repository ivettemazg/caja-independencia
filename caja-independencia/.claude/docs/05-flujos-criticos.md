# 05 — Flujos críticos paso a paso

Los seis procesos que mueven dinero. Antes de tocar cualquiera, lee el flujo completo: casi todos
escriben en varias tablas sin una transacción que los abarque.

---

## 1. Ciclo de vida de una solicitud de crédito

```
[socio]  solicitud-credito.xhtml
   │  valida elegibilidad → simula amortización → crea solicitud
   ▼
sol_estatus = 1 CREADA  ────────────────────────────────┐
   │  el socio completa avales, documentos y datos bancarios
   ▼                                                     │ (si no completa,
sol_estatus = 2 VALIDANDO                                │  queda "incompleta"
   │  [admin] validar-solicitudes.xhtml                  │  y bloquea nuevas
   ├── rechaza ──► sol_estatus = 7 RECHAZADA (terminal)  │  solicitudes)
   └── acepta  ──► sol_estatus = 3 ACEPTADA  ◄───────────┘
   │  [admin] fondeo-pendientes.xhtml
   ▼
sol_estatus = 4 FONDEADA  ← ¡AQUÍ NACE EL CRÉDITO Y SU AMORTIZACIÓN!
   │  el socio descarga anexos, los firma y los sube
   ▼
sol_estatus = 5 FIRMA DOCUMENTOS
   │  [admin] aprueba documentos
   ▼
sol_estatus = 8 DOC APROBADA
   │  [admin] fondeo-deposito.xhtml → marca depósito
   ▼
sol_estatus = 6 CERRADA (terminal)     +  cre_fecha_deposito
```

Estatus **9 EN REVISION** existe en BD (19 solicitudes) para solicitudes detenidas por problemas de
avales o documentos, pero **no tiene constante en `Constantes.java`**.

### Paso 1 — Creación (`SolicitudBo.creaSolicitud`)

Antes de crear, se corre `validaAntiguedadYCreditos()` (reglas completas en
[02-dominio-y-reglas.md](02-dominio-y-reglas.md#reglas-de-elegibilidad-para-solicitar-crédito)).

**Guarda de idempotencia:** lo primero que hace `creaSolicitud` es
`solDao.consultaSolicitudEnProceso(usuId)`; si ya existe una solicitud en proceso, **devuelve su id
en vez de crear otra**. Esto evita duplicados por doble clic. No lo quites.

En una sola operación (`solDao.guardaSolicitud`, con `cascade` desde `Solicitudes`) se persiste:

- La fila de `solicitudes` con `sol_estatus = 1`, `sol_estatus_db = 1`,
  `sol_fecha_creacion = Util.restaHrToDate(new Date(), 6)` ← **se restan 6 horas** para ajustar
  UTC→hora local de México.
- Un `Set<Imagenes>` con los documentos obligatorios: IFE(1), DOMICILIO(2), NOMINA(3), EDOCTA(4),
  más FAHORRO(6) si es FA o AGUINALDO(7) si es AG, más un AVAL(5) por cada aval requerido.
- Un `Set<SolicitudAvales>` con un renglón por aval, en estatus 1 (PENDIENTE).

Por producto:
- `no` / `au` → guarda `sol_catorcenas`.
- `fa` / `ag` → guarda `sol_facatorcena` y `sol_fecha_ult_catorcena`; el número de catorcenas lo
  calcula el simulador (`amortizacionBean.getTotalCatorcenasFaAgSau()`), y para AG se fuerza
  `faCatorcena = 12`.

El id resultante se guarda en sesión como `"idSolicitud"` y se redirige al detalle.

### Paso 2 — Completar y enviar

`DetalleSolicitudBean` / `DetalleSolicitudBo` gestionan datos bancarios, documentos y avales.
Cuando `sol_estatus_db` llega a 2, `updtEstatusDbSolicitud()` mueve la solicitud a estatus 2
(VALIDANDO) y se envía el correo de acuse (`Constantes.CUERPO`).

`DetalleSolicitudBo.validaImgsAvals()` verifica que todas las imágenes y avales estén completos.

### Paso 3 — Validación (`ValidaSolicitudBo`, `valsol-*.xhtml`)

El administrador revisa documentos (`ima_estatus`) y avales (`sol_ava_estatus`) uno a uno.

- **Autorizar** → estatus 3 + correo `SOLICITUD_AUTORIZADA` (incluye monto, catorcenas y pago).
- **Rechazar** → estatus 7 + correo `SOLICITUD_RECHAZADA` + registro en `bitacora`
  (`BIT_SOL_RECHAZADA = 3`) con el motivo, que después se muestra al socio en "Mis solicitudes".

### Paso 4 — Fondeo (`FondeosBo.fondeaSolicitud`) — el paso que crea el crédito

```java
CreditosFinal credito = creaCredito(solicitud, fechaPrimerPago);
// tasa especial: solo si sol_fecha_creacion == 2018-11-11
Set<Amortizacion> amort = creaAmortizacion(solicitud, credito, fechaPrimerPago, tasaEspecial);
credito.setAmortizacions(amort);
CreditosFinal credFin = guardaCredito(credito);        // cascade guarda la amortización

if (solicitud.getProId() == 11)                        // seguro de auto
    detSolBo.updtEstatusSolicitud(solId, SOL_EST_DOCTOS_APROBADOS /*8*/, 5);
else
    updtSolicitudFondeada(solId, new Date());          // estatus 4 + sol_fecha_fondeo

updtSolAvalesCreId(solId, credFin.getCreId());         // liga avales al crédito
enviaMensajeFondeo(solicitud.getUsuEmail());           // MSJ_CUERPO_FONDEADA
```

**El crédito que se crea:**

| Campo | Valor |
| --- | --- |
| `cre_clave` | `{empAbreviacion}{AA}{proSiglas}{solId}` — ej. `AMX26NO11542` |
| `cre_estatus` | `1` ACTIVO |
| `cre_prestamo` | `sol_monto_solicitado` |
| `cre_pago_quincenal` | `sol_pago_credito` (pese al nombre, es **catorcenal**) |
| `cre_empresa` | abreviación en **texto**, no el id |
| `cre_fecha_primer_pago` | capturada por el administrador en la pantalla |

**La amortización que se crea:**

- Productos **6 (NO) y 7 (AU)** → `generaTablaAmortizacion(...)`: N renglones (uno por catorcena),
  con anualidad, interés decreciente y fechas cada 14 días desde `fechaPrimerPago`.
- Productos **4 (FA), 5 (AG), 11 (SAU)** → se calcula la tabla sobre las catorcenas intermedias
  solo para sumar intereses, pero **se guarda un único renglón**
  (`tamortizacionBo.getUltAmortFAAGSAU()`) con el total y fecha = última catorcena.
  Para AG se fuerza `sol_facatorcena = 12` antes de calcular.

Todos los renglones nacen con `amo_estatus_int = 1` (PENDIENTE) y `amo_estatus = 'PENDIENTE'`.

> **Riesgo:** `fondeaSolicitud` no es atómico. Si falla el `updtSolicitudFondeada` posterior al
> `guardaCredito`, queda un crédito activo con la solicitud aún en estatus 3, y un nuevo fondeo
> crearía un segundo crédito. Ante un fondeo sospechoso, verifica
> `SELECT * FROM creditos_final WHERE cre_solicitud = ?`.

### Paso 5 — Documentos firmados y depósito

El socio descarga los anexos (Jasper), los firma con sus avales y sube un PDF único
(`ima_tipoimagen = 8`). El administrador aprueba (estatus 8) y luego marca el depósito:

```java
updtSolicitudDeposito(idSolicitud, fechaDep);
// → estatus 5 + creditos_final.cre_fecha_deposito
// + registro en `bancos` concepto 12 DEPOSITO CREDITOS con monto NEGATIVO
```

### Cancelación (`FondeosBo.cancelarCredito`)

1. `cre_estatus = 3` CANCELADO.
2. `updtPagosPagados()` — los pagos ligados a la amortización que se va a borrar pasan a
   estatus 7 (SIN AMORTIZACION) y su monto va a `pag_acumulado`.
3. `removeAmortizacion()` — **borra físicamente** los renglones de amortización pendientes.
4. Correo `CANCELACIONCREDITO`.

> El borrado es destructivo y sin respaldo. Es el motivo de que existan 276 renglones en estatus
> 14 CANCELADO (ruta alternativa que sí conserva historia).

---

## 2. Carga de archivos de pagos y aportaciones

`carga-archivo.xhtml` → `CargaArchivoPagMovsBean` (**@SessionScoped**) → `CargaPagosMovimientosBo`.

### Validaciones previas

1. `validaArchivoExiste(nombreArchivo)` — el nombre no puede repetirse en `archivos_historial`.
2. `validaSiEsCatorcena(fecha)` — la fecha debe existir en `catorcenas`.
3. `LectorExcel.validarFormatoExcel()` — columnas requeridas y conteo de filas vacías.
4. Solo `.xlsx`; `.xls` se rechaza explícitamente.

### Procesamiento (`procesaExcel`)

**Archivo de pagos (`tipoArchivo = 1`):**
```
LectorExcel.leerArchivoExcel  →  List<PagoDto>
dao.insertaArchivo(...)       →  archivos_historial con arh_estatus = 1 (PENDIENTE)
dao.insertaListaPagos(...)    →  N filas en `pagos` con pag_estatus = 1
dao.updateUsuIdPago(idArchivo)→  resuelve pag_usu_id cruzando clave de empleado + empresa
guardaEnBancos(...)           →  `bancos` concepto 1 PAGOS ARCHIVO, ban_ajustado = 0
```

**Archivo de aportaciones (`tipoArchivo = 2`):**
```
insertaArchivo                →  arh_estatus = 2 (PROCESADO de entrada; no hay algoritmo)
insertaListaMovimientos       →  N filas en `movimientos`
updateUsuIdMov(idArchivo)     →  resuelve mov_usu_id
actualizacionAFyNF(idArchivo) →  ajusta ahorro no fijo cuando no existe ahorro fijo
guardaEnBancos(...)           →  `bancos` concepto 2 APORTACION ARCHIVO
```

`updateUsuIdPago` / `updateUsuIdMov` son el punto donde un socio no registrado deja el pago
huérfano (`pag_usu_id = NULL`). Eso se resuelve en "Altas y cambios" antes de aplicar pagos.

### Eliminar un archivo

`eliminarArchivo()` borra en cascada manual: primero `pagos` (o `movimientos`) del archivo, luego
la fila de `archivos_historial`. **No revierte el registro en `bancos`** — queda un movimiento
bancario huérfano que hay que ajustar a mano.

---

## 3. Algoritmo de asignación de pagos

`aplica-pagos.xhtml` → `AsignaPagosBean` (**@RequestScoped**) →
`CargaPagosMovimientosBo.procesaArchivosCargados(catorcena)` →
`AlgoritmoAsignaPagosBo.initAlgoritmoAsignacion(arhId)`.

Es el corazón de la cobranza. Toda la lógica está en **SQL nativo** dentro de
`AlgoritmoAsignaPagosDao`, no en Java.

### Guardas previas

```java
archivos = buscaArchivosEnFechaConEstatusPendiente(catorcena);
pagos    = buscaPagosSinUsuId(catorcena);

if (archivos.isEmpty())  → "No hay archivos cargados en esta catorcena."
else if (pagos > 0)      → "Hay cambios de empresa pendientes, favor de aplicarlos…"
else if (!archivosPendientes) → (no hay nada por procesar)
```

> Antes existía una validación que exigía tener cargados **todos** los archivos de todas las
> empresas antes de procesar. Fue removida (hay un comentario al respecto). Hoy se puede procesar
> una empresa a la vez.

### Las 6 fases

```java
aplicaPagos(arhId, 2);          // 1. exactos
aplicaPagos(arhId, 3);          // 2. menores
aplicaPagos(arhId, 4);          // 3. mayores  (+ arreglaAcumuladoPagoMayor)
updtPagEstSinAmortizacion(arhId);// 4. sin amortización → estatus 7
aplicaPagosEst5y6(arhId);       // 5. multi-crédito → estatus 5 / 6
actualizaEstatusCredito();      // 6. créditos liquidados → cre_estatus = 2
actualizaEstatusArchivo(arhId); // arh_estatus = 2
```

**Filtro universal:** todos los `UPDATE` incluyen `cre_producto IN (6,7)` y `cre_estatus = 1`.
Es decir, el algoritmo **solo toca créditos de Nómina y Auto activos**. FA, AG y SAU se liquidan
manualmente.

#### Fases 1–3: emparejar pago ↔ amortización

Los tres UPDATE comparten la estructura:

```sql
UPDATE pagos
  INNER JOIN usuarios       ON usu_id = pag_usu_id
  INNER JOIN creditos_final ON cre_usu_id = usu_id
  INNER JOIN amortizacion   ON cre_id = amo_credito
SET amo_pago_id = pag_id, amo_estatus = '<TEXTO>', amo_estatus_int = <N>
WHERE <comparación de monto>
  AND amo_fecha_pago = pagos.pag_fecha     -- misma catorcena
  AND amo_pago_id IS NULL                  -- aún sin asignar
  AND pag_estatus = 1                      -- pago pendiente
  AND amo_monto_pago > 0
  AND amo_usu_id = pag_usu_id
  AND pag_arh_id = <arhId>
  AND cre_producto IN (6,7) AND cre_estatus = 1
```

| Fase | Comparación de monto | Estatus resultante |
| --- | --- | --- |
| Exacto | `amo_monto_pago - pag_deposito` entre **−1 y +1** | amo 2 PAGADO |
| Menor | `amo_monto_pago > pag_deposito` | amo 3 PAGO MENOR |
| Mayor | `amo_monto_pago < pag_deposito` | amo 4 PAGO MAYOR |

Las fases menor y mayor exigen además `amo_estatus_int = 1`, para no pisar lo ya asignado.

Después de cada fase, `updtPagosEstatus()` fija el estatus del pago y calcula el acumulado:

- **Menor (3):** `pag_acumulado = pag_deposito` (todo el pago queda como saldo a favor).
- **Exacto (2) y Mayor (4):** `pag_acumulado = pag_deposito - amo_monto_pago`.

#### El bucle de deduplicación

Un mismo pago puede engancharse con varias amortizaciones (por ejemplo si el socio tiene dos
créditos con la misma fecha y monto). Cada fase corre dentro de un `while`:

```java
while (hayRepetidos) {
    idPagosRepetidos = ejecutaUpdateXEstatus(arhId, estatus);   // update + detectar repetidos
    if (!idPagosRepetidos.isEmpty()) limpiaAmortizaciones(idPagosRepetidos);
    else hayRepetidos = false;
}
```

`getPagosRepetidosAmortizacion()` busca pagos con **más de una** amortización asignada;
`limpiaAmortizaciones()` conserva la primera y regresa las demás a `PENDIENTE` con
`amo_pago_id = NULL`. El ciclo repite hasta que no queden repetidos.

Tras la fase de mayores, `fixAcumPagosMayores()` recalcula
`pag_acumulado = pag_deposito - amo_monto_pago` porque la limpieza pudo dejarlo mal.

> Este `while` **no tiene tope de iteraciones**. Si un caso de datos hiciera que el update
> reintrodujera repetidos indefinidamente, el proceso se colgaría. No se ha observado, pero es una
> consideración al modificar los `WHERE`.

#### Fase 4: pagos sin amortización

```sql
UPDATE pagos SET pag_estatus = 7, pag_acumulado = pag_deposito
WHERE pag_estatus = 1 AND pag_arh_id = <arhId>
```

Todo lo que no encontró amortización queda como saldo a favor. Son **7,474 pagos históricos** en
este estado: socios que pagaron sin tener crédito activo, o cuyo crédito ya estaba liquidado.

#### Fase 5: pagos que cubren varios créditos (estatus 5 y 6)

Para socios con **más de un crédito** activo (o pagado) de producto 6/7 y un pago mayor con
`pag_acumulado >= 100`:

```java
for (pago : getUsuEst5y6(arhId)) {
    acumulado = pago.getPagAcumulado();
    for (amort : getAmortEstatus5y6(usuId, acumulado, pagFecha)) {
        diferencia = acumulado - amort.getAmoMontoPago();
        if (diferencia entre -1 y 1)  updtPagoEst5y6(pagId, 5, diferencia);  // cubre exacto
        else if (diferencia > 1)      updtPagoEst5y6(pagId, 6, diferencia);  // sobra acumulado
        updtAmortizacionPagoIdEst5y6(pagId, amoId);   // amo_estatus_int = 2 PAGADO
        acumulado = diferencia;
    }
}
finally: updtAmortizacionPagadosIn5y6(arhId);  // amortizaciones de pagos 5/6 → PAGADO
```

- Estatus **5** = "MAS DE 1 CREDITO": el pago cubre exactamente n créditos.
- Estatus **6** = "MAS DE 1 CREDITO Y ACUMULADO": cubre n créditos y sobra dinero.

> En `aplicaPagosEst5y6` hay un bloque de depuración olvidado:
> `if (dtoPago.getPagId() == 90298) { System.out.println("paraaqui"); }`. Es inocuo, pero es
> basura que conviene retirar en un cambio de mantenimiento.

#### Fase 6: cerrar créditos liquidados

```sql
UPDATE creditos_final
  LEFT JOIN (SELECT cre_id, COUNT(amo_id) pendientes
             FROM creditos_final LEFT JOIN amortizacion ON cre_id = amo_credito
             WHERE amo_estatus_int = 1 AND cre_estatus IN (1,2)
               AND cre_catorcenas IS NOT NULL AND cre_producto IN (6,7)
             GROUP BY cre_id) pendts ON pendts.cre_id = creditos_final.cre_id
SET cre_estatus = 2
WHERE cre_estatus = 1 AND cre_catorcenas IS NOT NULL
  AND cre_producto IN (6,7) AND pendts.cre_id IS NULL;
```

Un crédito se marca PAGADO cuando **no le queda ningún renglón en estatus 1**.

Finalmente `arh_estatus = 2`.

### Reproceso

`AlgoritmoAsignaPagosBo` tiene un `main()` que recorre **todos** los archivos de pagos
(`getArchivosPagos()`, orden `arhId desc`) y los reprocesa. Se usa como utilería fuera del
contenedor, con `HibernateUtil.buildSessionFactory2()`. **No lo ejecutes contra producción sin
respaldo.**

---

## 4. Proceso de rendimiento mensual

`rendimiento/proceso.xhtml` → `ProcesoBean` → `rendimiento.ProcesoDao` (sin BO).

Reparte entre los socios las ganancias del mes, en proporción a su ahorro.

### Paso A — `init()`
Carga las fechas disponibles (`getFechas()`, filas de `rendimiento`). Cada mes se precarga una fila
con `ren_estatus = 0`; al ejecutar pasa a `1`.

### Paso B — captura manual
El operador selecciona el mes y captura dos cifras que **no están en el sistema**:
- `intereses` → `ren_intereses_inversion` (rendimiento de las inversiones de la caja)
- `comisiones` → `ren_comisiones_bancarias`

### Paso C — `mesesRendimiento()`: cálculo

```java
// 1. Base de ahorro del mes
acumuladoTotal = Σ movimientos ahorro fijo(1) + no fijo(2) + voluntario(3)

// 2. Ingresos por intereses de crédito cobrados en el mes
pagosTotal = Σ pagos con estatus 2,4,5,6,8,9,10
             (exacto, mayor, mas1credito, mas1credito+acum, acumulado, capital, extemporáneo)

// 3. Utilidad del mes
interesTotalMensual = pagosTotal + intereses − comisiones
reserva             = interesTotalMensual * 0.10     // 10 % se retiene
interesNetoMensual  = interesTotalMensual * 0.90     // 90 % se reparte
factorRendimiento   = interesNetoMensual / acumuladoTotal
```

### Paso D — `ejecutaCalculo()`: aplicación

**Guarda:** si `intereses <= 0.0` se bloquea con "No ha registrado los intereses". Si
`comisiones == 0` solo advierte, no bloquea.

```java
double f = factorRendimiento;
for (cada saldo de ahorro fijo/no fijo del socio) {
    Movimientos mov = new Movimientos();
    mov.setMovDeposito(saldo * f);
    mov.setMovTipo("RENDIMIENTO");
    mov.setMovAr(2);
    mov.setMovFecha(selectedDate);
    // ahorro voluntario además: mov.setMovIdPadre(movIdOrigen)
}
pdao.updateAmountsv2(afynf);   // inserta uno por uno
pdao.updateAmountsv2(avol);
pdao.updateAmounts(valores, selectedDate, idFecha);  // actualiza la fila de `rendimiento`
```

Resultado: un movimiento de tipo RENDIMIENTO por socio y producto de ahorro. Explica por qué
`movimientos` tiene 2.2 M filas (697 k solo de rendimiento sobre ahorro fijo).

> **Riesgo operativo:** `updateAmountsv2` hace `savePojo` **uno por uno**, cada uno con su propia
> transacción y sesión Hibernate. Con ~4,000 socios activos son ~8,000 transacciones
> independientes. Si el proceso se interrumpe a la mitad, **queda parcialmente aplicado y no hay
> rollback**. La fila de `rendimiento` se actualiza al final, así que `ren_estatus` seguiría en 0.
> Antes de re-ejecutar un mes, verifica:
> ```sql
> SELECT COUNT(*) FROM movimientos
> WHERE mov_tipo='RENDIMIENTO' AND mov_fecha='YYYY-MM-DD';
> ```

Factores históricos típicos: 0.0077 – 0.0125 mensual.

---

## 5. Baja de empleado y finiquito

`finiquito/baja-empleado.xhtml` → `BajaEmpleadoBean` → `FiniquitoService` + `FiniquitosBo`.

### Paso 1 — Dar de baja

Se busca al socio, se registra `usu_fecha_baja` / `usu_estatus = 0` y se inserta en
`baja_empleados` un **snapshot** del momento
(`FiniquitoService.actualizarSnapshotBajaInicial`):

```java
saldoCreditos = Σ DetalleCreditoDto.saldoTotal  (créditos vivos)
saldoAhorros  = Σ MovimientosDto.totalMovimiento (ahorros)

if      (saldoCreditos >= 5.0) estatus = 1 BAJA_PENDIENTE;
else if (saldoAhorros  >= 5.0) estatus = 2 BAJA_AHORROSXDEVOLVER;
else                            estatus = 3 BAJA_COMPLETADA;
```

El umbral de **$5** absorbe centavos residuales. La deuda tiene prioridad sobre la devolución.

> Recuerda: tras la baja, el socio **conserva acceso al sistema durante un mes**
> (`LoginBean.validaLogin`).

### Paso 2 — Liquidar la deuda (`finiquito.xhtml`)

Cuatro salidas, en orden de preferencia:

**a) Abonar con los ahorros** (`FiniquitoService.aplicarAbono` / `devolverAhorroAbonoCredito`)
```java
montoAbono = Σ devoluciones seleccionadas
if (montoAbono - saldoCredito <= 3) {        // tolerancia de $3
    guardaDevoluciones(movimientos negativos, tipo ABONO CREDITO);
    ajustaCredito(..., AMO_ESTATUS_ABONO_CRE_8);
} else throw "El monto de la devolución no puede ser mayor al adeudo del crédito";
```

**b) Finiquito de la empresa** (`aplicarFiniquito`)
```java
idPago = finiquitoBo.guardaFiniquito(creId, montoFiniq, empId, fecha, usuId); // pagos estatus 12
finiquitoBo.ajustaCredito(..., AMO_ESTATUS_FINIQ_9);
finiquitoBo.updtAmoPagId(idPago, creId);
```

**c) Transferir a los avales** (`FiniquitoService.transferir`)
```java
totalAvales = Σ aval.montoCredito;
if (Math.abs(totalAvales - credito.saldoTotal) > 2.0)     // tolerancia de $2
    throw "Debe transferir el monto total del saldo del crédito.";

for (aval : avales) {
    nuevo CreditosFinal { cre_usu_id = aval, cre_producto = 6 NOMINA,
                          cre_tipo = "NOMINA", cre_estatus = 1,
                          cre_padre = credito.creId }
    creditoBo.creaCreditoTransferido(credTransfer, aval.primerCatorcena);  // genera amortización
}
creditoBo.updtCreditoEstatus(creId, CRE_EST_TRANSFERIDO /*4*/, null);
```
`asignarMontosAvalesProporcion()` reparte el adeudo en partes iguales entre los avales.

> Hay un comentario en el propio código señalando que se fija `cre_producto = 6`
> *"revisar porque tiene 6 que corresponde a ajustado"*. El comentario confunde producto con
> estatus: 6 es correctamente NOMINA. Existen dos métodos casi idénticos,
> `transferir()` (con validación de monto) y `transferirCreditoAAvAles()` (sin ella); **usa
> `transferir()`**.

**d) Declarar incobrable** (`marcarIncobrable`)
```java
creditoBo.updtCreditoEstatus(creId, CRE_EST_INCOBRABLE /*5*/, fechaIncobrable);
creditoBo.updtEstatusAmoInt(creId, AMO_ESTATUS_INCOB_12);
```

### Paso 3 — Devolver los ahorros (`ahorrosxdevolver.xhtml`)

`FiniquitoService.devolverTotalesAhorros()`:
- Genera un `Movimientos` **negativo** por cada saldo, tipo `DEVOLUCION`.
- Los guarda en lote (`guardaDevoluciones`).
- Genera un registro en `bancos` por cada movimiento, **compartiendo un único
  `ban_id_relacion`** (UUID) para agruparlos en la conciliación.
- El concepto de banco depende del producto y de `mov_ar`
  (`generarBancoDesdeMovimiento`): fijo→8/11, no fijo→9/14, voluntario→10/15.

Al terminar, la baja pasa a estatus 3 COMPLETADA. **Hay 6,206 bajas esperando este paso.**

### Paso 4 — Documentos

Se genera el PDF con `Finiquito.jrxml`, se guarda la ruta en `bae_ruta_archivo` y se registran
`bae_fecha_pdf` / `bae_fecha_correo` / `bae_fecha_deposito`.

---

## 6. Conciliación bancaria

`bancos/ajuste-banco.xhtml` → `BancoAjusteBean` → `BancoAjustesBo` → `bancos.BancosDao`.

Dos universos que deben cuadrar:

| Tabla | Origen |
| --- | --- |
| `bancos` | Lo que el **sistema** cree que pasó: totales de archivos, devoluciones, depósitos de crédito, aportaciones voluntarias… |
| `estado_cuenta` | Lo que el **banco** reporta realmente |

### Mecánica

1. `obtieneListasBanco(fechaInicio, fechaFin)` trae ambos lados del periodo.
2. El operador selecciona N registros de `bancos` y M de `estado_cuenta` que corresponden entre sí.
3. `persistRelBancoEC()` genera un **UUID** (`Util.genUUID()`) y lo escribe en `ban_id_relacion` y
   `ec_id_relacion` de todos los seleccionados, más `ban_fecha_relacion` / `ec_fecha_relacion`.
4. `updtAjustado(idRelacion, ajustadoBan, ajustadoEc)` marca `ban_ajustado` / `ec_ajustado`
   en 1 (total) o 2 (parcial).

Deshacer: `borraRelacion(idRelacion)` + `quitarAjusteBanco` / `quitarAjusteEc`.

También se pueden crear conceptos manuales en `estado_cuenta` (`guardaEdoCta`) y eliminarlos
(`eliminaEC`) — auditado con tipos de transacción 29–32.

> Las tablas `rel_banco_edocta` y `banco_edocta`, diseñadas como puente de esta relación, **están
> vacías**. La relación real se lleva por los campos `*_id_relacion`. No escribas código nuevo
> contra esas dos tablas.
>
> Estado actual: **13,455 registros de `bancos` sin conciliar** (34 %).

### Conceptos con signo negativo

En `bancos`, el monto es negativo cuando **sale** dinero de la caja:
- Concepto 12 DEPOSITO CREDITOS → `banMonto = deposito * (-1)`
- Concepto 6 DEVOLUCION ACUMULADO → `banMonto = pagDeposito * -1`
- Devoluciones de ahorro (8,9,10,11,14,15) → heredan el `mov_deposito`, que ya es negativo

`bancos_conceptos.cban_tipo` (`SUMA` / `RESTA`) documenta esta convención.

---

## Recuperación de contraseña (flujo secundario, pero el mejor implementado)

`recuperar-password.xhtml` → `RecuperaPasswordBean` → `PasswordRecoveryBo`.

**Solicitud** (`solicitaRecuperacion`):
1. Valida clave de empleado, empresa, correo y URL base.
2. `buscaUsuarioParaRecuperacion(clave, empresa)` — exige `usu_estatus = 1`.
   Si no lo encuentra pero sí existe la clave en otra empresa, responde
   *"La empresa no coincide…"* (**enumera empresas**: fuga menor de información).
3. Exige `usu_primeravez == 0` (perfil completo) y que el correo coincida exactamente
   (case-insensitive).
4. `cancelaTokensActivos(usuId)` → los tokens en estatus 1 pasan a 3.
5. Genera 32 bytes con `SecureRandom`, los codifica en Base64 URL-safe → **token plano**.
6. Guarda **solo el SHA-256 hex** en `prt_token_hash`, con vigencia de **10 minutos**, más
   `prt_ip_solicitud` y `prt_user_agent`.
7. Envía la liga `{resetBaseUrl}?token={urlencoded}` por correo.

**Restablecimiento** (`restablecePassword`):
1. Valida longitud ≥ 6 y que la confirmación coincida.
2. Busca por hash; si `prt_fecha_expira` pasó, marca el token en 3 y rechaza.
3. `actualizaPasswordYUsaToken()` — actualiza `usu_password` y pone el token en estatus 2.

> El manejo del token es correcto (hash, un solo uso, expiración, cancelación de previos). Lo que
> rompe la cadena es que la contraseña resultante se almacena **en texto plano**.
> Manuales de operación: `docs/manuales/manual-usuario-restablecimiento-password.md` y
> `manual-administracion-restablecimiento-password.md`.
