# Diseño de la bitácora de operaciones (Fase 0) — versión 2

Versión 2 · 2026-10-31 · para revisión de Ivette. Reemplaza a la [versión 1](diseno-bitacora-v1.md) e incorpora la
[revisión del Product Owner](revision-po-bitacora.md) y las respuestas de Ivette. Insumo: [inventario-escrituras.md](inventario-escrituras.md).

**Propuesta:** registrar en un esquema aparte (`bitacora`) cada operación que modifica datos, con lo necesario para tres fines:
saber quién hizo qué, repetirla en otro sistema y diagnosticar diferencias. La captura sigue siendo central (un filtro web y un
interceptor de Hibernate), pero ahora solo cuenta lo que realmente se confirmó en la base, registra lo que cambia de una ejecución a
otra (hora, aleatorios, archivos, correos) y nunca frena la operación: escribe en segundo plano por su propia conexión. Antes de
construir el sistema nuevo, la bitácora se valida repitiendo una semana real **en el propio sistema actual**.

## 1. Objetivos y criterio de éxito

1. **Trazabilidad:** quién, cuándo, desde dónde, con qué datos y con qué resultado.
2. **Réplica:** repetir cada operación real en otro sistema (primero en el actual, después en el nuevo) en el mismo orden y con los
   mismos datos.
3. **Diagnóstico:** cuando un saldo no cuadre, ver qué operaciones lo tocaron y qué escribieron.

**Criterio de éxito (estado esperado):** la copia del lunes N, más las operaciones registradas entre la marca de esa copia y la del
lunes N+1, aplicadas en orden, debe dar la copia del lunes N+1, comparando por llave de negocio con las reglas de la sección 8. Lo que
no cuadre es una de tres cosas: información que falta en la bitácora, un cambio hecho por fuera del sistema o un error.

## 2. Qué se registra

### 2.1 La operación (`bitacora.operacion`, una fila por acción)
| Dato | Detalle |
|---|---|
| Identificador y secuencia | id propio + número de arranque del servidor + secuencia global en el momento de confirmar |
| Inicio y fin | `DATETIME(3)` en **UTC**. Se muestran en hora de México al consultarlos |
| Origen | `PANTALLA`, `SISTEMA` (arranque, evento de la base) o `INTERVENCION` |
| Usuario que actúa | id, clave, **empresa** y rol, leídos al **inicio y al final** de la petición (el login lo crea al final, el logout lo borra); si cambian, se marca |
| IP y sesión | IP del cliente (no hay proxy: es la IP real) y hash corto del id de sesión |
| Vista, componente y evento | p. ej. `fondeo-pendientes.xhtml`, el botón, `click` / `blur` / `rowEdit` / `fileUpload` |
| Operación y variante | código de la sección 4 y su variante (p. ej. `CANCELA_CREDITO` / `DESDE_DEPOSITO`) |
| Contrato de réplica | entradas, estado de pantalla usado, valores calculados que vio el usuario y salidas (sección 3) |
| Parámetros crudos | del formulario, filtrados (sin ViewState, paginado, filtros ni contraseñas) y truncados |
| Resultado | `OK`, `ERROR` (no se guardó nada) o **`PARCIAL`** (error con escrituras ya confirmadas) y el mensaje que vio el usuario, redactado si toca contraseñas |
| Escrituras confirmadas | número |
| Versión | descripción de git, rama, fecha de compilación y SHA-256 del WAR |
| Encadenamiento | hash de la fila anterior + contenido (sección 9) |

**Cuándo se guarda:** si hubo escrituras confirmadas, si hubo error o si es una operación con nombre (por eso el `LOGIN`, que no
escribe, también queda).

### 2.2 Escrituras confirmadas (`bitacora.escritura`)
- Solo cuentan las sentencias de transacciones que **hicieron commit**. El interceptor las acumula por transacción y las da por buenas en
  `afterTransactionCompletion` cuando `wasCommitted()`; las demás se descartan. Así una operación que falla a la mitad queda `PARCIAL`
  y se sabe exactamente qué sí quedó guardado.
- Por cada sentencia: secuencia global, tabla, tipo (alta, cambio, baja) y:
  - **SQL nativo y HQL** (unas 80 de las ~111 escrituras): el texto del SQL, truncado. Ya trae los valores porque el sistema los arma
    con `String.format`.
  - **Entidades** (`savePojo`, `updatePojo`, `persist`): la tabla, el id (leído después del flush) y los valores, sin contraseñas.
- El antes y después exacto de cada fila no se guarda: sale del diff entre la copia de un lunes y la del siguiente.

### 2.3 Efectos no deterministas (`bitacora.efecto`)
Lo que cambia de una ejecución a otra y hace que dos sistemas no den lo mismo aunque reciban lo mismo:
- **Hora de referencia** de la operación (UTC). El código usa la hora del servidor en ~100 lugares, algunos con un ajuste manual de
  −6 h; el sistema nuevo deberá poder fijar su reloj a esta hora durante la réplica.
- **Cada aleatorio generado:** una línea en `Util.genUUID` (14 usos: relación bancos ↔ estado de cuenta) y en el UUID de
  `AdminPanelBean`.
- **Ids generados** por tabla, cuando se pueden leer (entidades). Los ids no se comparan entre sistemas: se comparan por llave de negocio.
- **Correos enviados:** asunto, destinatario enmascarado (`j***@dominio`) y resultado. Nunca el cuerpo ni ligas con token.
- **Archivos:** ver 2.4.

### 2.4 Archivos cargados
- **Excel de pagos y aportaciones: se guarda una copia** (decisión de Ivette), nombrada por su SHA-256 calculado sobre los bytes recibidos,
  en `c:/Documentos/bitacora/archivos/`. Son unos 20 archivos y ~1 MB al mes. Así un reintento que sobrescriba el original no impide repetir
  la carga.
- **PDF de solicitudes:** solo ruta, tamaño y SHA-256 (no hacen falta para repetir cálculos).

### 2.5 Intervenciones manuales (`bitacora.intervencion`)
Herramienta `IntervencionManual`, rediseñada para no afectar la operación:
1. Pide quién lo solicita, motivo, socio y crédito.
2. Muestra con un `SELECT` del mismo `WHERE` qué filas se van a tocar, sin abrir transacción.
3. Con la confirmación, ejecuta y hace commit de inmediato, con tiempo límite.
4. Guarda las filas antes y después (JSON), el SQL, filas afectadas, `CURRENT_USER()` y resultado.
5. Rechaza DDL (hace commit implícito) y varias sentencias juntas.

### 2.6 Eventos del sistema
- `ARRANQUE` (al iniciar Tomcat): versión y SHA-256 del WAR.
- `ACCESO_NEGADO` (desde `ControlAccesoFiltro`): ruta, motivo, IP.
- `CORTE_COPIA` (lo escribe el respaldo del lunes justo antes de copiar): es la marca que separa una semana de la siguiente.
- `ELIMINA_SOLICITUDES_ANTIGUAS` (evento de la base, diario a las **03:00 UTC = 21:00 en México**): el procedimiento se modifica para
  registrar los **ids** que borra, no solo cuántos. Su definición se guarda en el repositorio.

### 2.7 Qué NO se registra
- **Contraseñas**, en ningún lugar: ni parámetros, ni valores de `usu_password`, ni el `UsuarioDto` de la sesión (lo trae en claro), ni la
  contraseña temporal que el reseteo del panel muestra en su mensaje, ni el cuerpo o la liga de los correos.
- Consultas de solo lectura, paginado y filtros de tablas.
- Pendiente de decisión (sección 11): la generación de reportes con datos personales.

## 3. Contrato de réplica

Para cada código de operación se define qué hace falta para repetirla. Muchas acciones dependen de lo que la pantalla tenía en memoria
(un renglón seleccionado antes, montos editados en la tabla, un valor calculado en un `blur`) y eso no viaja en la petición que escribe;
el contrato lo hace explícito y el código de esa acción lo registra al ejecutarse.

| Parte | Qué es |
|---|---|
| Entradas | lo que capturó el usuario en esa petición |
| Estado de pantalla | lo que el bean ya tenía: ids seleccionados, renglones completos editados en memoria |
| Calculados | valores que el sistema calculó y el usuario vio antes de confirmar |
| Salidas | ids creados, montos resultantes, estatus finales |

Ejemplos (la lista completa se escribe en `contratos-replica.md` durante la etapa B):

| Código | Entradas | Estado de pantalla | Calculados | Salidas |
|---|---|---|---|---|
| `CARGA_ARCHIVO` | tipo, nombre del archivo | — | empresa y catorcena leídas del Excel, renglones | `arh_id`, filas creadas, `ban_id`, SHA-256 y ruta de la copia |
| `FONDEAR_CREDITO` | fecha del primer pago | solicitudes seleccionadas (ids) | monto, catorcenas y pago de cada una | `cre_id` por solicitud, renglones de amortización |
| `PAGO_CAPITAL` | monto, catorcenas restantes, fecha | crédito y renglón seleccionados | nuevo pago calculado en el `blur` | `pag_id`, renglones nuevos, pago y catorcenas finales |
| `AJUSTE_BANCOS` | rango de fechas | ids seleccionados de ambos lados | sumas y tipo de ajuste | id de relación (UUID), estatus de ajuste |

## 4. Catálogo de operaciones con nombre

Unos 65 códigos y ~60 métodos a instrumentar. La **variante** distingue la misma acción desde pantallas distintas.

| Módulo | Códigos (variantes) |
|---|---|
| Acceso | `LOGIN`, `LOGIN_FALLIDO`, `ACCESO_NEGADO`, `RECUPERAR_PASSWORD`, `RESTABLECER_PASSWORD`, `RESETEO_PASSWORD_ADMIN` |
| Socio | `CREAR_SOLICITUD`, `DATOS_BANCARIOS`, `CAPTURA_AVAL`, `SUBIR_DOCUMENTO`, `SUBIR_DOC_FIRMADO`, `PERFIL` (SOCIO / ADMIN), `BENEFICIARIO_GUARDAR`, `BENEFICIARIO_BORRAR` |
| Validación | `VALIDA_SOLICITUD_DATOS` (blur), `APRUEBA_SOLICITUD`, `RECHAZA_SOLICITUD`, `APRUEBA_AVAL`, `RECHAZA_AVAL`, `ESTATUS_DOCUMENTO` |
| Fondeo | `FONDEAR_CREDITO`, `RECHAZA_EN_FONDEO`, `MARCA_DEPOSITO`, `CANCELA_CREDITO` (DEPOSITO / FONDEADOS / DOC_ENVIADA), `DOCS_FIRMADOS`, `ESTATUS_DOC_FIRMADO`, `CREAR_SOLICITUD_SEGURO_AUTO` |
| Pagos y aportaciones | `CARGA_ARCHIVO` (PAGOS / APORTACIONES), `APLICA_PAGOS`, `APLICA_CAMBIOS_EMPRESA`, `CAMBIO_EMPRESA_MANUAL`, `ALTA_SOCIO`, `REACTIVA_SOCIOS`, `ELIMINA_CARGA` (hoy se hace a mano) |
| Reporte personal | `HABILITAR_SOCIO`, `OMITIR_VALIDACIONES`, `APORTACION_VOLUNTARIA`, `DEVOLUCION_AHORRO`, `DEVOLUCION_ACUMULADO`, `NOTA_SOCIO`, `RECORRE_CATORCENAS`, `PAGO_CAPITAL`, `PAGO_CAPITAL_AHORRO`, `PAGO_EXTEMPORANEO`, `PAGO_CON_ACUMULADO`, `PAGO_CON_AHORRO`, `TRANSFIERE_A_AVALES` (REPORTE / FINIQUITO), `INCOBRABLE` (REPORTE_PERSONAL / REPORTE_CREDITOS / FINIQUITO) |
| Finiquitos | `BAJA_SOCIO`, `FINIQUITO_DEVOLUCION`, `FINIQUITO_AJUSTE_CREDITO` (FINIQUITO / ABONO), `DEVOLUCION_TOTAL_AHORRO` |
| Bancos | `EDOCTA_EDITA`, `EDOCTA_BORRA`, `EDOCTA_AGREGA`, `AJUSTE_BANCOS`, `QUITA_AJUSTE`, `DESCUENTO_COBRADO` |
| Rendimiento y administración | `RENDIMIENTO`, `BANDERAS_FA_AG` |
| Sistema | `ARRANQUE`, `CORTE_COPIA`, `ELIMINA_SOLICITUDES_ANTIGUAS`, `INTERVENCION_MANUAL` |

Todas registran sus **salidas**. Las acciones no instrumentadas quedan igualmente registradas por la captura central, con parámetros crudos.

## 5. Cómo se captura

```
Navegador ─► [1] ControlAccesoFiltro ─► FileUpload ─► CharacterEncoding ─► [2] BitacoraFiltro ─► FacesServlet ─► Bean ─► BO ─► DAO ─► Hibernate ─► MySQL
                (ACCESO_NEGADO)                                             │ contexto por petición                           │
                                                                            │ (usuario inicio/fin, IP, vista, parámetros)     │
                                                                            │◄── [3] contrato en ~60 métodos            [4] BitacoraInterceptor
                                                                            │◄── [5] PhaseListener: mensajes en          acumula por transacción,
                                                                            │        todas las fases + errores del log    acepta solo si hubo commit
                                                                            ▼
                                       al terminar: arma la operación y la deja en una cola acotada (no espera)
                                                                            ▼
                                [6] EscritorBitacora: un hilo, su propia conexión (usuario bitacora_escritor, solo INSERT)
                                    si la base no responde o la cola se llena → archivo local JSONL → se reinyecta después
```

1. `ControlAccesoFiltro` (ya desplegado) deja en la cola los `ACCESO_NEGADO`.
2. `BitacoraFiltro` va **al final** de la cadena, después de fijar UTF-8, para no dañar acentos y ñ. Abre el contexto por hilo y lo
   **limpia siempre en `finally`** (Tomcat reutiliza hilos).
3. Una o dos líneas por método clave registran el contrato (clase `RegistroBitacora`; el nombre `Bitacora` ya lo usa un POJO).
4. `BitacoraInterceptor`, registrado en `HibernateUtil` (en las dos construcciones que existen). Cada método atrapa cualquier error y
   devuelve el SQL original: **nunca puede romper la sentencia del usuario**. Ignora por nombre exacto las tablas del esquema `bitacora`.
5. `BitacoraPhaseListener` toma los mensajes de todas las fases (las subidas de archivo se procesan antes de "invocar la aplicación") y
   un appender de log4j marca `ERROR` cuando el hilo registra un error que el bean atrapó sin mostrar mensaje.
6. `EscritorBitacora`: un solo hilo con cola acotada y **su propia conexión** (no usa el pool de 10 de la aplicación). Credenciales fuera
   del WAR (archivo en el servidor o variables de entorno); si faltan, solo escribe al archivo local.

**Interruptor:** si existe `c:/Documentos/bitacora/APAGADA`, la bitácora deja de registrar (se revisa cada minuto, sin reiniciar).

## 6. Tablas (esquema `bitacora`)

Esquema aparte en la misma instancia: no cuesta más, no lo toca la actualización automática del esquema `sindicato` y permite permisos
propios. Usuarios: `bitacora_escritor` (solo INSERT, lo usa el sistema) y `claude_lectura` (agregar SELECT para exportar).

```sql
CREATE DATABASE bitacora DEFAULT CHARACTER SET utf8mb4;

CREATE TABLE bitacora.operacion (
  bop_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  bop_arranque      BIGINT       NOT NULL,           -- bop_id del ARRANQUE de este servidor
  bop_secuencia     BIGINT       NOT NULL,           -- orden global al confirmar
  bop_inicio_utc    DATETIME(3)  NOT NULL,
  bop_fin_utc       DATETIME(3)  NULL,
  bop_origen        VARCHAR(20)  NOT NULL,           -- PANTALLA | SISTEMA | INTERVENCION
  bop_usu_id        INT NULL,  bop_usu_clave INT NULL,  bop_usu_empresa INT NULL,  bop_usu_rol VARCHAR(20) NULL,
  bop_usu_cambio    TINYINT      NOT NULL DEFAULT 0, -- 1 si el usuario de la sesión cambió durante la petición
  bop_ip            VARCHAR(45)  NULL,
  bop_sesion        CHAR(16)     NULL,
  bop_vista         VARCHAR(200) NULL,
  bop_componente    VARCHAR(200) NULL,
  bop_evento        VARCHAR(40)  NULL,
  bop_operacion     VARCHAR(60)  NULL,
  bop_variante      VARCHAR(40)  NULL,
  bop_contrato      JSON         NULL,               -- entradas, estado de pantalla, calculados, salidas
  bop_parametros    JSON         NULL,
  bop_resultado     VARCHAR(10)  NOT NULL,           -- OK | ERROR | PARCIAL
  bop_mensaje       VARCHAR(1000) NULL,
  bop_escrituras    INT          NOT NULL DEFAULT 0,
  bop_version       VARCHAR(200) NULL,
  bop_hash_anterior CHAR(64)     NULL,
  bop_hash          CHAR(64)     NOT NULL,
  KEY idx_inicio (bop_inicio_utc), KEY idx_secuencia (bop_arranque, bop_secuencia),
  KEY idx_usuario (bop_usu_id, bop_inicio_utc), KEY idx_operacion (bop_operacion, bop_inicio_utc)
) ENGINE=InnoDB;

CREATE TABLE bitacora.escritura (
  bes_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  bes_bop_id BIGINT NOT NULL, bes_secuencia BIGINT NOT NULL,
  bes_tabla VARCHAR(64) NOT NULL, bes_tipo CHAR(1) NOT NULL,     -- I | U | D
  bes_id_registro VARCHAR(40) NULL,                            -- cuando se conoce
  bes_sql VARCHAR(4000) NULL,                                  -- nativo/HQL, truncado
  bes_valores JSON NULL,                                       -- entidades, sin contraseñas
  KEY idx_operacion (bes_bop_id)
) ENGINE=InnoDB;

CREATE TABLE bitacora.efecto (
  bef_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  bef_bop_id BIGINT NOT NULL,
  bef_tipo VARCHAR(20) NOT NULL,                               -- HORA | UUID | ID_GENERADO | CORREO | ARCHIVO
  bef_valor JSON NOT NULL,
  KEY idx_operacion (bef_bop_id)
) ENGINE=InnoDB;

CREATE TABLE bitacora.intervencion (
  bin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  bin_fecha_utc DATETIME(3) NOT NULL,
  bin_ejecuto VARCHAR(60) NOT NULL, bin_usuario_mysql VARCHAR(80) NOT NULL, bin_solicito VARCHAR(120) NOT NULL,
  bin_motivo VARCHAR(1000) NOT NULL, bin_usu_id INT NULL, bin_cre_id INT NULL,
  bin_sql MEDIUMTEXT NOT NULL, bin_filas INT NULL,
  bin_antes JSON NULL, bin_despues JSON NULL,
  bin_resultado VARCHAR(10) NOT NULL, bin_mensaje VARCHAR(1000) NULL,
  bin_hash_anterior CHAR(64) NULL, bin_hash CHAR(64) NOT NULL,
  KEY idx_fecha (bin_fecha_utc)
) ENGINE=InnoDB;
```

**Tamaño:** se mide en la copia antes de subirlo. Estimación prudente: de 5,000 a 20,000 operaciones al mes contando inicios de sesión y
`blur`, y unos cientos de MB al año, por debajo del 5 % del disco actual. Cada campo se trunca antes de insertar.

## 7. Réplica viejo→viejo (validar la bitácora antes del sistema nuevo)

Antes de construir nada nuevo se comprueba que la bitácora alcanza: se toma la copia del lunes N, se repiten las operaciones registradas
de esa semana con **las clases del sistema actual** (como hacen los arneses de `caracterizacion/`) y el resultado se compara con la copia
del lunes N+1.
- Si coincide, la bitácora alcanza para esas operaciones.
- Si no, o falta información en la bitácora (se corrige el contrato), o hubo un cambio por fuera del sistema (queda al descubierto), o hay un error.

Se empieza por las operaciones más frecuentes y de más impacto (cargas, asignación de pagos, cambios de empresa, fondeo, pagos a capital,
finiquitos, bancos, rendimiento) y se agregan las demás. El mismo reproductor, cambiando el ejecutor, servirá después para el sistema nuevo.

## 8. Réplica semanal y comparación

1. **Lunes, 03:00 hora de México:** el respaldo programado en el servidor Windows escribe la marca `CORTE_COPIA` y en seguida saca la copia
   con `mysqldump --single-transaction` (usuario de solo lectura), más un zip de los Excel de la semana. Copia cifrada. Costo ≈ 0.
   *(Guía paso a paso: [respaldo-semanal/GUIA.md](respaldo-semanal/GUIA.md).)*
2. Las operaciones de la semana N son las que quedan entre la marca N y la marca N+1, por secuencia. Las que empezaron antes de una marca
   y terminaron después se tratan aparte.
3. La réplica de la semana N se hace cuando ya existe la copia N+1, y se compara contra ella. No hace falta que la caja deje de operar:
   el orden real queda en la secuencia.
4. **Comparación por llave de negocio**, no por tabla completa. `CHECKSUM TABLE` queda solo como semáforo:
   - cada tabla con su llave (p. ej. amortización = crédito + número de pago) y sus columnas excluidas;
   - montos con la tolerancia que ya usa el sistema (±1 peso);
   - columnas de "ahora" (fechas que pone el servidor): deben caer en la fecha de la operación registrada;
   - relaciones por UUID: se comparan por pareja, no por valor;
   - ids: se resuelven por la llave de negocio del registro referido;
   - se excluyen contraseñas, tokens, `registro_transaccion` y el propio esquema `bitacora`.
5. **No repetibles por diseño**, se aplican como parche de estado: operaciones `PARCIAL`, contraseñas, escrituras al abrir páginas,
   intervenciones manuales (con su imagen posterior) y el borrado de las 21:00 (con sus ids). Los cambios hechos por fuera del sistema
   solo se detectan.

En las copias de trabajo se enmascaran los correos de los socios y se usa siempre un SMTP falso.

## 9. Seguridad, integridad y conservación

- **Integridad:** `bitacora_escritor` solo puede insertar, y cada fila guarda el hash de la anterior (un solo hilo escribe, así que la
  cadena es simple). Alterar o borrar una fila rompe la cadena y se detecta. El SHA-256 de cada exportación semanal se guarda fuera de la base.
- **Usuarios de la base:** Jorge M. tendrá su propio usuario y se cambiará la contraseña del usuario de la aplicación (hoy está en el
  repositorio). Se hace como cambio aparte, coordinado con un WAR nuevo.
- **Datos personales:** la bitácora y las copias contienen datos de ~12,500 socios. Sin pantalla de consulta; acceso solo con usuarios de
  base nominales. Revisar que el aviso de privacidad cubra este registro.
- **Conservación en dos niveles:** la cabecera (quién, cuándo, qué, resultado) se conserva como evidencia; la carga útil (parámetros,
  valores, SQL, contrato) se purga al cerrar el periodo en paralelo. Las copias semanales se borran cuando ya no se necesitan.

## 10. Implementación por etapas

Cada etapa se prueba con el mismo estándar que el control de acceso: el WAR nuevo contra el de producción, con los mismos datos, en la copia
aislada; recorrido de las 333 páginas con las 4 identidades; escenarios con navegador real; búsqueda de contraseñas de prueba en las tablas
y en los logs; y en producción, cada paso con tu confirmación.

| Etapa | Qué incluye | Pruebas específicas |
|---|---|---|
| A. Base | esquema y usuarios, filtro, interceptor con commit, escritor asíncrono con respaldo en archivo, interruptor, `ARRANQUE`, `ACCESO_NEGADO`, `LOGIN`, correos, copia de Excel, redacción de contraseñas | dos usuarios a la vez; base caída; cola llena; acentos; tiempos de carga, asignación de pagos y rendimiento sin aumento mayor al 10 % (si no, se apaga y se regresa al WAR anterior) |
| B. Contratos | los ~60 métodos con su contrato y salidas, empezando por los de la sección 7; `contratos-replica.md` | cada operación registrada con su contrato completo |
| C. Copia semanal | respaldo programado con `CORTE_COPIA`, exportación, comparación por llave de negocio (`llaves-comparacion.md`) | dos semanas de copias consecutivas |
| D. Réplica viejo→viejo | reproductor con las clases actuales; iterar hasta que copia N + operaciones = copia N+1 | semanas reales; residuos clasificados |
| E. Control de cambios | `IntervencionManual`, usuario propio para Jorge, cambio de contraseña del usuario de la app, evento con ids | intervenciones de prueba en la copia |

## 11. Decisiones pendientes

1. **Conservación:** ¿la cabecera por cuánto tiempo? Propuesta: lo mismo que los registros contables de la caja (confirmarlo con su contador).
2. **`show_sql`:** imprime el SQL en la salida estándar de Tomcat y ahí pueden aparecer valores como la CLABE. ¿Se apaga? Perderías ese
   SQL para diagnosticar.
3. **Reportes con datos personales** (descuento de nómina, expediente del socio): ¿se registra quién los generó?
4. **Cuándo** cambiar la contraseña del usuario de la app y crear el de Jorge.
5. **Reglas de comparación** de la sección 8: ¿de acuerdo con las tolerancias y exclusiones?

## Cambios respecto a la versión 1

| Revisión PO | Cómo se atiende |
|---|---|
| #1 no deterministas | 2.3 (hora, UUID, ids, correos) y reglas de comparación en 8 |
| #2 cambios externos | usuario propio para Jorge, cambio de contraseña (9) y detección en la réplica viejo→viejo (7) |
| #3 confirmado vs intentado | 2.2: solo escrituras con commit; resultado `PARCIAL` |
| #4 promesa de valores | 2.2: SQL para nativo/HQL, valores solo de entidades; antes/después por diff de copias |
| #5 estado de pantalla | contrato de réplica (3) y validación viejo→viejo (7) |
| #6 concurrencia | secuencia global al confirmar (2.1); la caja no deja de operar |
| #7 estado esperado | criterio de éxito (1) y marca `CORTE_COPIA` (2.6, 8) |
| #8 comparación | por llave de negocio con reglas (8) |
| #9 Excel sobrescritos | copia por SHA-256 (2.4) |
| #10 contraseñas | 2.7. *Corrección: los valores ligados no se escribían en `sindicato.log`; lo que expone datos es `show_sql` (11.2)* |
| #11 perfil y sesión | corregido y desplegado (commit 6ee2e84); usuario leído al inicio y al final; contexto limpiado en `finally` |
| #12 pool y bloqueo | escritor asíncrono con conexión propia y respaldo en archivo (5) |
| #13 correos | 2.3 y SMTP falso en copias (8) |
| #14 intervenciones | herramienta rediseñada (2.5) |
| #15 integridad | esquema aparte, solo INSERT, cadena de hashes (9) |
| #16 orden de filtros | `BitacoraFiltro` al final (5) |
| #17 mensajes | todas las fases + appender de errores (5) |
| #18 evento y zona horaria | UTC; el evento corre a las 21:00 de México; registra ids (2.6) |
| #19 interceptor seguro | atrapa todo y devuelve el SQL original; las dos construcciones (5) |
| #20 versión | descripción de git + SHA-256 del WAR + `ARRANQUE` (2.1, 2.6) |
| #21 tamaño | truncado, exclusiones y medición previa (6) |
| #22 dónde vive la copia | respaldo programado en el servidor Windows (8) |
| #23 conservación | dos niveles (9) |
| #24 reversa | interruptor, criterio del 10 % y WAR anterior (5, 10) |
| #25 catálogo | ~65 códigos con variante y salidas (4) |
| #26 empresa del usuario | `bop_usu_empresa`; regla de cuándo guardar (2.1) |
| #27 nombres | esquema aparte y clase `RegistroBitacora` |
| #28 documentos | inventario actualizado; versionar `docs/fase0/` |
| #29 lecturas importantes | decisión pendiente (11.3) |
| #30 sistema nuevo | el sistema nuevo escribirá la misma bitácora: comparación operación por operación y auditoría continua después del corte |
