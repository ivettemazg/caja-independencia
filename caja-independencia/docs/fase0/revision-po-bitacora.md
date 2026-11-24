# Revisión del diseño de la bitácora (Product Owner / analista)

2026-10-31 · revisión de [diseno-bitacora.md](diseno-bitacora.md) (v1) contra el código. Marcas: **[V]** verificado en código o en
las fuentes de Hibernate 4.3.11; **[O]** opinión o supuesto; **[M]** dato de Ivette.

## Veredicto
La forma de capturar es correcta: filtro web + interceptor global de Hibernate. Todas las escrituras de la app pasan por la misma
`SessionFactory`, y `onPrepareStatement` sí recibe el SQL nativo y el HQL masivo [V]. Sirve para **trazabilidad**. Para la
**réplica semanal determinista** y el **diagnóstico** todavía no alcanza:
- no registra lo no determinista (reloj, ids aleatorios, ids autoincrementales);
- no distingue lo confirmado de lo intentado (las operaciones no son atómicas);
- no define contra qué estado se compara;
- no detecta cambios hechos fuera de la app.

Prioridad: #1, #3, #5 y #7, y validar con una **réplica viejo→viejo** antes de que exista el sistema nuevo.

## Hallazgos
| # | Sev. | Tipo | Hallazgo | Recomendación |
|---|---|---|---|---|
| 1 | Crítico | Hueco | [V] Fuentes no deterministas sin registrar: ~100 usos de `new Date()`/`Calendar`/now (algunos con ajuste de −6 h) y `current_timestamp()`; `Util.genUUID()` (`Util.java:47`) en 14 puntos para la relación bancos↔estado de cuenta; UUID en `AdminPanelBean:119`; 25 de 36 mappings con `identity`. | Registrar instante de referencia (UTC), cada aleatorio generado y los ids generados por tabla. Sistema nuevo con reloj e ids inyectables en "modo réplica", o fijar `AUTO_INCREMENT` antes de repetir. |
| 2 | Crítico | Hueco | [M] Otras personas cambian la base con el mismo usuario MySQL de la app (así se creó el evento de borrado). Nada lo detecta. | Rotar la contraseña del usuario de la app y dar usuario propio a cada persona. La réplica viejo→viejo deja como residuo lo que no pasó por la app. Opcional: triggers en 5–6 tablas financieras con `USER()`. |
| 3 | Crítico | Error | [V] OK/ERROR y el conteo no reflejan lo guardado: cada helper confirma por separado; `onPrepareStatement` se llama al preparar, antes de ejecutar (`StatementPreparerImpl:176`); cerrar sin commit no avisa (`getOrCreateConfig:27`). | Acumular por transacción y aceptar solo en `afterTransactionCompletion` si `wasCommitted()`. Resultado `PARCIAL`. En la réplica, lo parcial se aplica como parche de estado. |
| 4 | Alto | Error | [V] ~80 de ~111 escrituras son HQL masivo o SQL nativo (cargas fila por fila, `CargaPagosAportacionesDao:283,320`): no pasan por `onSave`; con `identity` el id llega `null`. | Para nativo/HQL guardar el SQL truncado (ya trae valores). Para entidades, ids en `postFlush`. El antes/después exacto sale del diff entre copias. |
| 5 | Alto | Hueco | [V] Los datos con nombre son generales; muchas acciones dependen de estado de pantalla que no viaja en la petición que escribe (`DoctosSolBean:108`, `AmortizacionRepEmpBean:433-447`, renglones editados en memoria). | **Contrato de réplica** por código: entradas, estado del bean usado, valores calculados y salidas. Validar con réplica viejo→viejo. |
| 6 | Alto | Hueco | [V/O] Operaciones concurrentes y procesos de minutos se intercalan; ordenar por inicio o fin no reproduce el orden real. | Secuencia global por sentencia confirmada. Marcar solapes. Regla en el paralelo: no operar durante procesos masivos. |
| 7 | Alto | Ambigüedad | Estado esperado sin definir; operaciones en curso al tomar la copia. | Regla: copia N + operaciones entre marca N y marca N+1 = copia N+1. Registrar `CORTE_COPIA`. |
| 8 | Alto | Riesgo | [V] `CHECKSUM TABLE` es todo o nada y fallará por diferencias esperadas (ids aleatorios, fechas, tokens, contraseñas con hash). | Huellas por llave de negocio con columnas excluidas y tolerancias (±1 peso); el CHECKSUM como semáforo. |
| 9 | Alto | Riesgo | [V] El Excel se escribe antes de validarlo y un reintento lo pisa (`CargaArchivoPagMovsBean:130, 192-206`). | Copia del Excel nombrada por su SHA-256 en carpeta aparte (~1 MB/mes). De los PDF basta el hash. |
| 10 | Alto | Error | [V] Fugas de contraseñas: el reseteo del panel la muestra en el mensaje (`AdminPanelBean:122`); `UsuarioDto` de sesión trae la contraseña; token en la liga del correo. **Hoy `sindicato.log` guarda los valores de cada sentencia, incluidas contraseñas y CLABE (`log4j.properties:10`, `BasicBinder=TRACE`).** | Redactar mensajes de contraseña; no serializar el `UsuarioDto` ni correos. **Bajar `BasicBinder` a INFO.** |
| 11 | Alto | Riesgo | [V] **`PerfilBean.actualizaPerfil` reemplaza el usuario de la sesión por el del socio editado.** Desde el panel, el administrador queda con la identidad y el rol del socio; con `ControlAccesoFiltro` pierde las pantallas administrativas hasta volver a entrar. | Corregir `PerfilBean` (actualizar la sesión solo si se editó a sí mismo). Limpiar el contexto por hilo en `finally`. |
| 12 | Alto | Riesgo | [V] El escritor comparte el pool c3p0 de 10 sin `checkoutTimeout` (espera indefinida). | Escritor asíncrono de un hilo con cola acotada; si falla, archivo local y reinyección. |
| 13 | Alto | Hueco | [V] Los correos no se registran (`EnviaCorreo:31,110,158`); la copia lleva correos reales. | Registrar asunto, destinatario enmascarado y resultado. En la copia: enmascarar correos y SMTP falso obligatorio. |
| 14 | Alto | Riesgo | [O] `IntervencionManual` con transacción abierta esperando confirmación bloquea filas; sin imagen previa no se puede revertir; un DDL hace commit implícito. | `SELECT` previo con el mismo `WHERE`, ejecutar y confirmar de inmediato con tiempo límite; guardar filas antes/después; rechazar DDL y varias sentencias; registrar `CURRENT_USER()`. |
| 15 | Medio | Riesgo | [O] Con el usuario compartido cualquiera puede alterar la bitácora. | Esquema aparte con solo INSERT para la app; SHA-256 de cada lote semanal guardado fuera de la base. |
| 16 | Medio | Error | [V] Orden real de filtros: Control de Acceso → FileUpload → CharacterEncoding. Leer parámetros antes de fijar UTF-8 corrompe acentos. | `BitacoraFiltro` al final o leer después de `chain.doFilter`. Opcional `ACCESO_NEGADO`. |
| 17 | Medio | Error | [V] Tomar mensajes "después de la acción" no ve subidas, validaciones fallidas ni redirecciones; hay `catch` que solo loguean. | Acumular mensajes en todas las fases; appender de log4j que marque ERROR por hilo. |
| 18 | Medio | Hueco | [M/V] **El evento corre a las 03:00 UTC = 21:00 en CDMX**; el diseño no indica zona. Solo conteos, no ids. Procedimiento fuera del repo. | Todo en UTC. Registrar ids borrados. Versionar `SHOW CREATE`. En la copia, desactivar el evento y aplicar sus borrados en su posición. |
| 19 | Medio | Riesgo | [V] El interceptor es una instancia compartida; una excepción rompe la sentencia del usuario. `HibernateUtil` tiene construcción duplicada. | `try/catch(Throwable)` en cada callback. Sin contexto: origen `SISTEMA`. |
| 20 | Medio | Hueco | [V/M] El hash del commit no identifica el WAR. | `git describe --dirty` + rama + fecha + SHA-256 del WAR. Registrar `ARRANQUE`. |
| 21 | Medio | Riesgo | [O] Tamaño subestimado (logins, `blur`, >1,000 parámetros en bancos); campos largos o JSON inválido pierden la fila. | Truncar campos; excluir ViewState, paginado y filtros; medir en la copia. |
| 22 | Medio | Ambigüedad | No dice dónde vive la copia ni quién la toma. [O] Un clon encendido toda la semana costaría ~600 MXN/mes. | `mysqldump --single-transaction` programado en el servidor Windows con usuario de solo lectura. Costo ≈ 0. |
| 23 | Medio | Riesgo | [O] Una sola regla de conservación; copias con datos de 12,500 socios. | Dos niveles: cabecera más tiempo; carga útil se purga al cerrar cada semana o el paralelo. Copias cifradas. |
| 24 | Medio | Hueco | [O] Sin plan de reversa, interruptor ni umbral de tiempo; faltan pruebas de concurrencia y de base caída. | Interruptor en caliente; criterio de reversa (+10 % de tiempo); réplica viejo→viejo de una semana simulada. |
| 25 | Medio | Hueco | [V] Son 58 códigos, no ~40; variantes sin distinguir; faltan `ARRANQUE`, `CORTE_COPIA`, correos, `ACCESO_NEGADO`, borrado de una carga; ningún código registra salidas. | Campo de variante u origen; agregar los faltantes y las salidas. |
| 26 | Bajo | Hueco | [V] Falta la empresa del usuario (la clave no es única entre empresas); un LOGIN correcto no escribe y no se guardaría. | `bop_usu_empresa`; guardar si hay escrituras, error u operación con nombre. |
| 27 | Bajo | Riesgo | [V] Excluir `bitacora_*` excluiría la tabla existente `bitacora_transacciones`; la clase `Bitacora` choca con el POJO. | Excluir por nombre exacto; otro nombre de clase. |
| 28 | Bajo | Ambigüedad | [V] El inventario (§5) dice que no hay filtro, pero ya se desplegó; `docs/fase0/` no está en git. | Actualizar y versionar. |
| 29 | Bajo | Mejora | [O] Algunas lecturas importan (reporte de descuento de nómina, expedientes con datos personales). | Registrar solo esos reportes. |
| 30 | Bajo | Mejora | [O] El formato sirve al sistema nuevo si se trata como contrato. | Que el sistema nuevo escriba la misma bitácora: comparación operación por operación y auditoría continua. |

## Réplica semanal
- **Repetibles con lo registrado:** notas, beneficiarios, perfil del socio, habilitar, omitir validaciones, datos bancarios, aval, aprobar o
  rechazar solicitud/aval/documento, baja, documentos firmados.
- **Solo con #1 + #5:** solicitudes, fondeo, depósito, cancelación, aportaciones y devoluciones, bancos, pagos, recorrido, transferencias,
  incobrables, finiquitos, altas, cambio manual, reactivación, banderas.
- **Masivas** (carga, algoritmo de pagos, cambios de empresa, rendimiento): además copia del Excel (#9), sin concurrencia (#6) e ids iniciales.
- **No repetibles por diseño** (se comparan por estado o se aplican como parche): contraseñas, `blur` de validación, escrituras al abrir
  páginas, operaciones parciales, intervenciones manuales, borrado programado y cambios externos (solo se detectan).

## Preguntas para Ivette
1. ¿Quién más tiene credenciales de MySQL? ¿Se puede rotar la del usuario de la app y dar uno por persona?
2. ¿Los 3 administradores tienen cuenta propia?
3. ¿Qué cuenta como "igual" en la comparación?
4. ¿El sistema nuevo tendrá reloj e ids inyectables en modo réplica?
5. ¿Dónde vive la copia semanal, quién la dispara y cuánto se puede gastar?
6. ¿Comparamos contra la copia del lunes siguiente en lugar de contra producción del sábado?
7. ¿La caja puede no operar mientras corren cargas, algoritmo de pagos o rendimiento?
8. Conservación en dos niveles: ¿de acuerdo?
9. ¿Copia de cada Excel? ¿Y de los PDF?
10. ¿Corregimos ya `PerfilBean` (#11)?
11. ¿Horas en UTC?
12. ¿Hay un proxy delante de Tomcat? (afecta la IP registrada)
13. ¿Registrar consultas a datos personales y generación de reportes?
