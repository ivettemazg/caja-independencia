# Diseño de la bitácora de operaciones (Fase 0) — VERSIÓN 1 (reemplazada por diseno-bitacora.md)

Versión 1 · 2026-10-31 · para revisión de Ivette. Se basa en [inventario-escrituras.md](inventario-escrituras.md).

**Propuesta:** registrar automáticamente, en tres tablas nuevas, cada acción que modifica la base: quién, cuándo, desde dónde,
con qué datos y qué cambió. Se captura en dos puntos centrales (un filtro web y un interceptor de Hibernate), así que cubre todas
las pantallas sin tocarlas una por una. Además, unas 40 acciones clave registran sus datos con nombre para poder repetirlas en el
sistema nuevo. Las intervenciones manuales y el borrado programado también quedan registrados.

## 1. Para qué sirve

1. **Trazabilidad:** saber quién hizo cada operación, cuándo, desde qué pantalla y con qué datos.
2. **Réplica semanal:** durante el periodo en paralelo, repetir en el sistema nuevo cada operación real de la semana, en el mismo
   orden y con los mismos datos, y comparar los resultados.
3. **Diagnóstico:** cuando un saldo no cuadre, ver qué operaciones lo tocaron.

Lo que existe hoy no alcanza: `registro_transaccion` guarda usuario, fecha sin hora, tipo e id, sin datos ni resultado, y en 2026
solo se usaron 5 de sus 35 tipos. Se conserva como está; la bitácora nueva no la reemplaza ni la modifica.

## 2. Qué se registra

### Nivel 1: la operación (una fila por acción que escribe)
| Dato | Ejemplo | De dónde sale |
|---|---|---|
| Inicio y fin, con milisegundos | 2026-11-20 10:14:03.512 | reloj del servidor |
| Usuario que actúa: id, clave, rol | 1234 · 90012 · Administrador | atributo de sesión `usuario` |
| IP y sesión (hash del id) | 189.x.x.x · a91f… | petición |
| Vista | `/navegacion/admonsolicitudes/fondeo-pendientes.xhtml` | petición |
| Componente y evento | `frmFondeo:btnFondear` · `click` / `blur` / `rowEdit` / `fileUpload` | parámetros de JSF |
| Operación con nombre (si es una de las clave) | `FONDEAR_CREDITO` | código, sección 4 |
| Datos con nombre | `{"solId": 9876, "fechaPrimerPago": "2026-11-26"}` | código, sección 4 |
| Parámetros crudos del formulario, sin contraseñas | `{"frmFondeo:cal_input": "26/11/26", …}` | petición |
| Archivo cargado: ruta, tamaño y SHA-256 | `c:/Documentos/2026-11-12 ahorro fijo tech.xlsx` · 48 213 · 9c1e… | disco, al terminar |
| Resultado | `OK` / `ERROR` y el mensaje que vio el usuario o la excepción | JSF / excepción |
| Número de escrituras y versión de la app | 3 · `44e99c6` | interceptor / build |

### Nivel 2: qué cambió en la base (por operación)
Por cada tabla y tipo (alta, cambio, baja): cuántas sentencias hubo y qué registros tocó.
- Operaciones chicas (hasta 50 sentencias): el detalle completo, con los valores de cada registro.
- Operaciones masivas (cargas, algoritmo de pagos, rendimiento): solo el resumen por tabla, con una muestra de ids. Para comparar se
  usan las huellas de las tablas, no las sentencias.

### Nivel 3: intervenciones manuales y procesos automáticos
- **Intervenciones manuales** (cambio de tasa, rehacer amortizaciones, último pago pendiente, etc.): fecha, quién la ejecutó, quién
  la pidió, motivo, socio y crédito afectados, el SQL exacto, filas afectadas y resultado.
- **Borrado programado** (`ev_eliminar_solicitudes_antiguas`, diario 03:00): una fila con cuántas solicitudes, avales e imágenes borró.

### Qué NO se registra
- **Contraseñas**, nunca: ni en los parámetros ni en los valores de `usu_password`. Se excluyen por nombre de campo y, en login,
  recuperación, restablecimiento y reseteo de administrador, no se guardan parámetros crudos (solo datos con nombre sin contraseña).
- Consultas de solo lectura y paginado o filtros de tablas: no cambian nada y no hacen falta para la réplica.
- Sí se registran los inicios de sesión (correctos y fallidos), por seguridad.

## 3. Cómo se captura

```
Navegador ──► FileUploadFilter ──► [1] BitacoraFiltro ──► FacesServlet ──► Bean ──► BO ──► DAO ──► Hibernate ──► MySQL
                                        │  abre el contexto                                     │
                                        │  (usuario, IP, vista, parámetros)              [2] BitacoraInterceptor
                                        │                                                 anota cada INSERT/UPDATE/DELETE
                                        │◄──────── [3] Bitacora.operacion(...) en ~40 métodos clave (datos con nombre)
                                        │◄──────── [4] BitacoraPhaseListener: mensajes de error de la pantalla
                                        ▼
                         al terminar la petición, si hubo escrituras o error:
                         [5] BitacoraEscritor guarda la operación y su detalle (sesión propia; nunca rompe la operación)
```

1. **`BitacoraFiltro`** (filtro web en `web.xml`, después del de carga de archivos): abre un contexto por petición con el usuario de
   la sesión, IP, vista, componente, evento y parámetros filtrados. Al final decide si guarda.
2. **`BitacoraInterceptor`** (interceptor global de Hibernate, se registra en `HibernateUtil.buildSessionFactory` con
   `configuration.setInterceptor`). Como **todas** las escrituras pasan por la misma `SessionFactory` (helpers de `ManagerDB` y las 17
   escrituras directas), ve todas: `onPrepareStatement` cuenta cada INSERT/UPDATE/DELETE por tabla, y `onSave`, `onFlushDirty` y
   `onDelete` dan ids y valores de las entidades. Ignora las tablas `bitacora_*`. *A comprobar en la prueba: que `onPrepareStatement`
   también reciba el SQL nativo de `createSQLQuery`.*
3. **`Bitacora.operacion("CODIGO").dato("nombre", valor)`**: una o dos líneas al inicio de cada método clave (sección 4), para que la
   réplica tenga los datos con nombre y no dependa de ids de componentes como `j_idt80`, que cambian al editar las vistas.
4. **`BitacoraPhaseListener`** (en `faces-config.xml`): después de ejecutar la acción, toma la severidad máxima de los mensajes de la
   pantalla, porque muchos beans atrapan la excepción y solo muestran un mensaje.
5. **`BitacoraEscritor`**: guarda con su propia sesión y una sola transacción. Si falla, lo anota en el log de Tomcat y la operación del
   usuario sigue su curso.

Las operaciones largas (rendimiento, algoritmo de pagos) quedan como una sola operación, con su resumen por tabla. Las escrituras que
ocurren al abrir una página o al salir de un campo (por ejemplo el `blur` de validación de solicitudes) quedan como operaciones propias:
también son operaciones reales.

Todo va en un paquete nuevo, `mx.com.evoti.bitacora`. Cambios fuera de él: `web.xml` (filtro), `faces-config.xml` (listener),
`HibernateUtil` (una línea), ~40 métodos de beans (una o dos líneas cada uno) e ids explícitos en los campos de contraseña de 3 vistas.

## 4. Operaciones con nombre (primera lista)

| Código | Acción (ver inventario) | Datos con nombre |
|---|---|---|
| `LOGIN` / `LOGIN_FALLIDO` | loginBean.validaLogin | clave, motivo del fallo |
| `RECUPERAR_PASSWORD` / `RESTABLECER_PASSWORD` / `RESETEO_PASSWORD_ADMIN` | 2.1 | clave, empresa, socio (sin contraseña) |
| `CREAR_SOLICITUD` / `CREAR_SOLICITUD_SEGURO_AUTO` | 2.2 / 2.4 | tipo, monto, deducciones, catorcenas, catorcena FA, montos |
| `DATOS_BANCARIOS`, `CAPTURA_AVAL`, `SUBIR_DOCUMENTO`, `SUBIR_DOC_FIRMADO` | 2.2 | solicitud, campos capturados, documento |
| `PERFIL`, `BENEFICIARIO_GUARDAR`, `BENEFICIARIO_BORRAR` | 2.2 | socio, campos cambiados |
| `VALIDA_SOLICITUD_DATOS`, `APRUEBA_SOLICITUD`, `RECHAZA_SOLICITUD` | 2.3 | solicitud, valores, motivo |
| `APRUEBA_AVAL`, `RECHAZA_AVAL`, `ESTATUS_DOCUMENTO` | 2.3 | solicitud, aval/documento, estatus, motivo |
| `FONDEAR_CREDITO`, `RECHAZA_EN_FONDEO`, `MARCA_DEPOSITO`, `CANCELA_CREDITO`, `DOCS_FIRMADOS`, `ESTATUS_DOC_FIRMADO` | 2.4 | solicitudes, fechas, motivo |
| `CARGA_ARCHIVO` | 2.5 | tipo, nombre, empresa y catorcena del archivo, renglones, ruta y SHA-256 |
| `APLICA_PAGOS` | 2.5 | catorcena, archivos procesados |
| `APLICA_CAMBIOS_EMPRESA`, `CAMBIO_EMPRESA_MANUAL`, `ALTA_SOCIO`, `REACTIVA_SOCIOS` | 2.5 | catorcena, pendiente, socio, cambios aplicados |
| `HABILITAR_SOCIO`, `OMITIR_VALIDACIONES` | 2.6 / 2.11 | socio, valor nuevo |
| `APORTACION_VOLUNTARIA`, `DEVOLUCION_AHORRO`, `DEVOLUCION_ACUMULADO`, `NOTA_SOCIO` | 2.6 | socio, monto, fecha, movimiento |
| `RECORRE_CATORCENAS`, `PAGO_CAPITAL`, `PAGO_CAPITAL_AHORRO`, `PAGO_EXTEMPORANEO`, `PAGO_CON_ACUMULADO`, `PAGO_CON_AHORRO` | 2.6 | crédito, renglones elegidos, montos, fechas, opciones |
| `TRANSFIERE_A_AVALES`, `INCOBRABLE` | 2.6 / 2.7 / 2.8 | crédito, montos por aval, catorcenas, fecha |
| `BAJA_SOCIO`, `FINIQUITO_DEVOLUCION`, `FINIQUITO_AJUSTE_CREDITO`, `DEVOLUCION_TOTAL_AHORRO` | 2.8 | socio, fecha, montos por renglón |
| `EDOCTA_EDITA`, `EDOCTA_BORRA`, `EDOCTA_AGREGA`, `AJUSTE_BANCOS`, `QUITA_AJUSTE`, `DESCUENTO_COBRADO` | 2.9 | registros, montos, fechas, rango |
| `RENDIMIENTO` | 2.10 | mes, intereses, comisiones |
| `BANDERAS_FA_AG` | 2.11 | valores nuevos |

Las acciones que no estén en la lista quedan igualmente registradas por los niveles 1 y 2, con sus parámetros crudos.

## 5. Tablas nuevas

No se mapean en Hibernate, para que la actualización automática del esquema no las toque. Se crean con este script, probado antes en
la copia y ejecutado en producción solo con confirmación de Ivette:

```sql
CREATE TABLE bitacora_operacion (
  bop_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  bop_inicio        DATETIME(3)  NOT NULL,
  bop_fin           DATETIME(3)  NULL,
  bop_origen        VARCHAR(20)  NOT NULL,          -- PANTALLA | EVENTO_BD | SISTEMA
  bop_usu_id        INT          NULL,              -- quien actúa (NULL si no hay sesión)
  bop_usu_clave     INT          NULL,
  bop_usu_rol       VARCHAR(20)  NULL,
  bop_ip            VARCHAR(45)  NULL,
  bop_sesion        CHAR(16)     NULL,              -- hash del id de sesión
  bop_vista         VARCHAR(200) NULL,
  bop_componente    VARCHAR(200) NULL,
  bop_evento        VARCHAR(40)  NULL,
  bop_operacion     VARCHAR(60)  NULL,              -- código de la sección 4
  bop_datos         JSON         NULL,              -- datos con nombre
  bop_parametros    JSON         NULL,              -- parámetros crudos filtrados
  bop_archivo_ruta  VARCHAR(500) NULL,
  bop_archivo_bytes BIGINT       NULL,
  bop_archivo_sha256 CHAR(64)    NULL,
  bop_resultado     VARCHAR(10)  NOT NULL,          -- OK | ERROR
  bop_mensaje       VARCHAR(1000) NULL,
  bop_escrituras    INT          NOT NULL DEFAULT 0,
  bop_version_app   VARCHAR(40)  NULL,
  KEY idx_bop_inicio (bop_inicio),
  KEY idx_bop_usuario (bop_usu_id, bop_inicio),
  KEY idx_bop_operacion (bop_operacion, bop_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bitacora_escritura (
  bes_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  bes_bop_id      BIGINT       NOT NULL,
  bes_tabla       VARCHAR(64)  NOT NULL,
  bes_tipo        CHAR(1)      NOT NULL,            -- I | U | D
  bes_sentencias  INT          NOT NULL,
  bes_ids         VARCHAR(1000) NULL,               -- muestra de ids afectados
  bes_detalle     JSON         NULL,                -- valores, solo en operaciones chicas
  KEY idx_bes_operacion (bes_bop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bitacora_intervencion (
  bin_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  bin_fecha       DATETIME(3)  NOT NULL,
  bin_ejecuto     VARCHAR(60)  NOT NULL,
  bin_solicito    VARCHAR(120) NOT NULL,
  bin_motivo      VARCHAR(1000) NOT NULL,
  bin_usu_id      INT          NULL,
  bin_cre_id      INT          NULL,
  bin_sql         MEDIUMTEXT   NOT NULL,
  bin_filas       INT          NULL,
  bin_resultado   VARCHAR(10)  NOT NULL,             -- OK | ERROR | REVERTIDA
  bin_mensaje     VARCHAR(1000) NULL,
  KEY idx_bin_fecha (bin_fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

El procedimiento `sp_eliminar_solicitudes_antiguas` se modifica para insertar al final una fila en `bitacora_operacion`
(`bop_origen = 'EVENTO_BD'`, `bop_operacion = 'ELIMINA_SOLICITUDES_ANTIGUAS'`) con los conteos de `ROW_COUNT()` de cada DELETE.

**Tamaño estimado:** con el volumen actual (unas 20 cargas, 75 solicitudes y 50 altas o cambios al mes, más pagos y finiquitos),
unas 2,000 a 5,000 operaciones al mes y menos de 50 MB al año.

## 6. Archivos cargados

No se copian: el sistema ya los guarda en `c:/Documentos`. La bitácora guarda la ruta, el tamaño y el SHA-256 calculados al terminar
la carga. Para la réplica se toma el archivo del servidor y se compara su SHA-256 con el de la bitácora.

Riesgo conocido (inventario, sección 4): un archivo se sobrescribe si se vuelve a subir con el mismo nombre después de una carga
fallida, y los documentos de solicitud se sobrescriben al volver a subirlos. Con el SHA-256 lo detectamos. Si en la práctica pasa, se
decide entonces si conviene guardar versiones.

## 7. Intervenciones manuales

Regla: **todo cambio manual a la base se hace con la herramienta `IntervencionManual`**, nunca directo. La herramienta:
1. pide quién lo solicita, el motivo, y el socio o crédito afectado;
2. ejecuta el SQL en una transacción y muestra cuántas filas tocaría;
3. pide confirmación; con la confirmación, hace commit y registra todo en `bitacora_intervencion`; sin ella, deshace.

Más adelante, las tres intervenciones más frecuentes (cambio de tasa, rehacer amortización, último pago pendiente) pueden volverse
funciones del sistema, con su propia operación en la bitácora.

## 8. Cómo se usa en la réplica semanal

1. **Lunes:** copia de producción. **La copia debe tomarse después de las 03:00**, porque a esa hora corre el borrado programado.
   Propongo las 03:30.
2. **Durante la semana:** la bitácora acumula las operaciones reales.
3. **Sábado:** se exportan las operaciones desde la copia del lunes hasta el corte de la semana, en orden, con sus archivos
   (verificando el SHA-256) y las intervenciones manuales.
4. Se repiten en el sistema nuevo sobre la copia y se comparan las huellas de las tablas y los resultados (documentos, reportes).
   Las diferencias se clasifican en esperadas (errores corregidos a propósito) e inesperadas (a investigar).

## 9. Seguridad y conservación

- La bitácora contiene datos personales (RFC, domicilio, CLABE), igual que la base. Se trata como confidencial y no tendrá pantalla de
  consulta en el sistema actual.
- Conservación propuesta: hasta un año después del corte. Después se archiva o se depura.
- Los mismos datos pueden servir como evidencia ante una reclamación de un socio.

## 10. Implementación y pruebas

1. Crear las tablas en la base de control local y desarrollar el paquete `mx.com.evoti.bitacora`.
2. Probar en la copia aislada (sin salida a internet) recorriendo cada grupo de operaciones del inventario con los arneses HTTP y
   de procesos que ya tenemos. Por cada una se revisa: que se registre con usuario, hora, datos y escrituras; que **ninguna contraseña**
   aparezca (se buscan las contraseñas de prueba en las tablas); y que los tiempos de carga, algoritmo de pagos y rendimiento no cambien.
3. Comparar el resto de la base antes y después: la bitácora no debe alterar ningún otro dato.
4. En producción, con tu confirmación paso a paso: crear las tablas, modificar el procedimiento del borrado programado y subir el WAR.
5. Al día siguiente, revisar en solo lectura que las operaciones reales se estén registrando.

**Orden con los demás trabajos:** después de la actualización a MySQL 8.4 del 14 de noviembre, para no mezclar cambios. La corrección
del acceso sin sesión (ver abajo) va antes que todo.

## 11. Decisiones pendientes

1. **Conservación:** ¿hasta un año después del corte te parece bien?
2. **Hora de la copia del lunes:** ¿la movemos a las 03:30?
3. **Intervenciones manuales:** ¿de acuerdo en hacerlas siempre con la herramienta?
4. **Consultas:** ¿confirmas que no se registran las de solo lectura (sí los inicios de sesión)?

## Nota: acceso sin sesión

El inventario encontró, y se verificó en la copia aislada el 2026-10-31, que las pantallas de `/navegacion/` no exigen sesión ni rol:
sin iniciar sesión se abren y sus acciones se ejecutan. Se corrige con un filtro de acceso (sesión obligatoria y rol de administración
para las pantallas administrativas), independiente de la bitácora, y debe ir primero.
