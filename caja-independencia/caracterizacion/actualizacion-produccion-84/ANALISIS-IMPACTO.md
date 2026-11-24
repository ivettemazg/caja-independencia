# Impacto de MySQL 8.4 en el código y qué observar después

Análisis del 15 de noviembre de 2026, antes de actualizar `evoti-sindicato` de MySQL 8.0.31 a 8.4.
Se revisó todo el código que habla con la base (WAR en producción `d802f331`, rama `main` 6ee2e84) y se
probó contra una base 8.4 local (primero sin datos y después con la copia de control), la copia de control
8.0.31 y, **solo con lecturas**, producción.

## En una frase

No encontré nada en el código que deje de funcionar con 8.4. Lo que puede variar es **el orden** en que la
base entrega algunos renglones (y, con él, cuál se toma cuando el código toma "el primero") y **la
velocidad** de algunos procesos. Los dos se vigilan con la lista del final.

## Qué se revisó y cómo

| # | Área | Cómo se revisó | Resultado |
|---|---|---|---|
| 1 | Conexión | Versiones en `pom.xml` y en el WAR; propiedades de `hibernate.cfg.xml` | Conector `mysql-connector-j 8.4.0` (el mismo que hoy; está hecho para 8.4). Soporta el método de contraseña nuevo del paso 4; la URL ya trae `allowPublicKeyRetrieval=true`. El certificado del servidor no depende de la versión (huella guardada para confirmarlo). `Conexion.java` (JDBC directo) no se usa en ningún lado |
| 2 | Las 196 sentencias SQL/HQL del código (34 archivos; 109 SQL nativo, 48 HQL, 45 escrituras) | Búsqueda de todo lo que 8.4 quitó o cambió: variables `@x :=`, `SQL_CALC_FOUND_ROWS`, `VALUES()` en `ON DUPLICATE KEY`, juegos de caracteres `utf8`, operadores `&&` `\|\|`, `BINARY`, `LOAD DATA`, `SHOW MASTER/SLAVE`, funciones quitadas, `GROUP BY … ASC/DESC`, fechas `0000-00-00`, `FOR UPDATE`, `SELECT … INTO`, DDL desde el código, procedimientos | **Ninguna** usa algo de eso. No hay SQL fuera de Java (ni en mappings, ni en reportes Jasper) |
| 3 | Palabras reservadas | Lista exacta de 8.0.31 contra 8.4 (`information_schema.KEYWORDS`) | 8.4 reserva dos palabras nuevas, `QUALIFY` y `TABLESAMPLE`. No aparecen en tablas, columnas, índices ni en el código |
| 4 | Esquema | Copia de control | 43 tablas InnoDB. Las 7 llaves foráneas apuntan a llaves únicas (8.4 ya no deja crear las que no). Sin vistas, sin `ZEROFILL`, sin `FLOAT(M,D)`, sin índices de texto completo |
| 5 | `hbm2ddl.auto=update` (Hibernate altera el esquema al arrancar Tomcat) | Herramienta nueva `SimulaHbm2ddl.java`: con los mappings del WAR genera los `ALTER/CREATE` que Hibernate intentaría, **sin ejecutarlos** | 0 sentencias en la copia 8.0.31, 0 en la 8.4 local y 0 en producción hoy. Prueba de control: al quitar una columna y una llave en la 8.4 local, la herramienta las detectó |
| 6 | Consultas cuyo resultado depende del orden | 117 consultas de lectura: 31 con `ORDER BY`, 6 de un solo valor, 80 sin orden | Ver la sección siguiente |
| 7 | Configuración del servidor | Copia de las 744 variables de producción (lectura) para compararlas esta noche; vista previa con la 8.4 local | Lo que importa para resultados (`sql_mode`, zona horaria, mayúsculas en nombres, eventos) es igual en ambas; si cambia, el script detiene el proceso. Lo que cambia por omisión en 8.4 afecta velocidad, no resultados |
| 8 | Ensayo del 20–21 de octubre (clon de producción en 8.4 contra la copia 8.0.31) | — | Idénticos: carga y asignación de pagos, documentos PDF, rendimiento, pagos a capital, recorrido de catorcenas, finiquitos, solicitudes y fondeo, ajuste de bancos, altas y cambios de empresa, y 397 consultas de pantallas y reportes **comparando también el orden** |

## Consultas que dependen del orden

Cuando una consulta no dice en qué orden quiere los renglones (`ORDER BY`), MySQL los entrega en el orden en
que los encuentra. Ese orden depende del **plan de ejecución**: qué tabla lee primero y qué índice usa. Mientras
el plan no cambie, el resultado es siempre el mismo. Una actualización de versión puede cambiar el plan, y
también lo pueden cambiar los datos con el tiempo: hoy mismo, producción y la copia de control (las dos 8.0.31)
tienen planes distintos en la búsqueda por apellido de cambios de empresa (P15) porque sus datos son distintos.

**Ninguno de estos casos es nuevo: existen hoy.** Revisé el código de las 80 consultas sin orden y de los
`.get(0)`/`ANY_VALUE`. Estos son los que pueden cambiar un dato guardado, con lo que hay hoy en producción:

| # | Proceso | Qué decide el orden | Datos de hoy que caen en el caso | Qué podría pasar |
|---|---|---|---|---|
| 1 | **Asignación de pagos** al cargar el archivo de pagos (`AlgoritmoAsignaPagosBo`) | Si un pago encaja con dos renglones del mismo socio (misma fecha, montos con diferencia de ±1), cuál crédito se lo queda (`limpiaAmortizaciones` conserva "el primero"). Con el sobrante de un pago mayor, qué renglones se cubren primero (`aplicaPagosEst5y6`) | 49 socios con dos pagos de crédito pendientes el 26 de noviembre (55 a 59 en las catorcenas siguientes) | El total pagado por el socio no cambia; puede cambiar **a cuál de sus créditos** se aplica |
| 2 | **Asignación de cuenta** al cargar pagos y aportaciones, en altas y en cambios de empresa (`updateUsuIdPago/Mov`) | Si dos cuentas tienen la misma clave y empresa, a cuál se asigna el pago o la aportación | 23 pares de cuentas; en 2 pares las dos están activas. Hoy siempre recibe la de número menor (desde octubre: 3 cuentas recibieron, todas activas; ninguna inactiva) | Un pago podría ir a la otra cuenta del mismo socio |
| 3 | **Finiquitos y devoluciones de ahorro** (`MovimientosDao.getAFANFyR`, `ANY_VALUE`) | Con qué empresa y clave se registra el movimiento de devolución o abono | 7,573 socios tienen ahorro registrado con más de una empresa o clave (por cambios de empresa) | El monto no cambia; la **empresa** del movimiento puede ser otra (afecta reportes por empresa y conciliación de bancos). Aun sin la actualización, hoy no necesariamente es la empresa actual: conviene revisarlo como error aparte |
| 4 | Pago a capital y finiquito (`AmortizacionDao.getAmortizacionPCap`) | En qué renglón se aplica si un crédito tiene dos pendientes con la misma fecha | 4 créditos | Bajo |
| 5 | Cambios de empresa (`getUltimosMovs`, `obtieneUsuarioXNombre`) | Homónimos al buscar por nombre; empresa del último movimiento si hay dos el mismo día | 151 nombres completos repetidos; 0 socios con dos empresas el mismo día en los últimos 180 días | Bajo; la pantalla muestra los cambios antes de aplicarlos |
| 6 | Inicio de sesión (`LoginBean`) | Si un socio tiene dos cuentas con la misma clave y contraseña, con cuál entra (gana la última) | 1 clave | Bajo |

**Sin impacto (verificado en el código):** rendimiento (cada socio se calcula por separado, sin repartir
sobrantes), recuperar contraseña, documento firmado de una solicitud, crédito de una solicitud (0 casos
repetidos), morosos (solo cuenta renglones), saldo de finiquito con `ANY_VALUE` (solo productos con un
renglón). En los PDF de la solicitud, el orden de los avales podría cambiar de lugar en las firmas; salen todos.

**Cómo se vigila:** el paso nuevo `planes` guarda el plan de esas 18 consultas antes y después (es `EXPLAIN`:
no ejecuta nada). Si el plan de una consulta es el mismo, su orden es el mismo y el proceso se comporta igual
que hoy. Si cambia, ese proceso se revisa con cuidado en la catorcena del 26 de noviembre (lista del final).

**Corrección de fondo (después, no hoy):** agregar un `ORDER BY` explícito a esas consultas con el orden que
usan hoy, para que el resultado ya no dependa del plan. Es un cambio de código que se prueba en la copia
aislada como los anteriores; va a la lista de errores de la fase 0.

**Vista previa (hoy, en local):** cargué la copia de control en una base 8.4.13 y comparé los 18 planes contra
la misma copia en 8.0.31, con estadísticas recalculadas en las dos. **17 de 18 son iguales.** El distinto es
P06 (pagos mayores de socios con varios créditos): 8.4 usa otro índice para unir cada pago con su amortización.
No altera el resultado: en ese paso del algoritmo cada pago ya tiene una sola amortización (en producción, 0
casos con más de una), y el orden entre pagos lo da la tabla de pagos, que se lee igual. Es una vista previa:
Google instala su propia versión 8.4 y producción tiene sus estadísticas; la comparación que cuenta es la de
esta noche (pasos 2, 8 y 9).

## Lo que se agregó al plan de esta noche (todo es de lectura)

- **Paso 2:** además de las huellas y los objetos, `variables antes`, `hbm2ddl antes` y `planes antes` (ya se
  tomaron hoy en la mañana; se repiten con Tomcat detenido).
- **Paso 8:** `variables despues` y `planes despues`.
- **Paso 9:** `comparar` dice cuántos de los 18 planes quedaron iguales, muestra qué configuración cambió y se
  detiene si cambió algo que altere resultados. Un plan distinto **no** detiene el arranque (los datos que ya
  existen no cambian); ese proceso pasa a revisión especial en la catorcena del 26 de noviembre.
- **Paso 9b, nuevo:** `hbm2ddl despues`. Antes de arrancar Tomcat se confirma que Hibernate no va a tocar
  el esquema de 8.4. Si propone algo, no se arranca.

## Lista de observación

### Esta noche, antes de arrancar Tomcat (pasos 7 a 9b)

- El certificado del servidor conserva la misma huella (guardada hoy en `certificado_antes.txt`). Lo reviso
  en el paso 7.
- `comparar`: "IDÉNTICAS" y cuántos planes quedaron iguales. `hbm2ddl despues`: "Sin cambios".

### Esta noche, al arrancar Tomcat (paso 10)

Buscar en `sindicato.log` y en el log de Tomcat:

| Si aparece | Quiere decir |
|---|---|
| `Public Key Retrieval is not allowed`, `Access denied`, `Communications link failure`, `SSL` | Problema de conexión (ver tabla de abajo) |
| `HHH000299`, `SchemaUpdate`, `Unsuccessful: alter table` | Hibernate intentó cambiar el esquema (el paso 9b debió avisarlo antes) |
| `SQLGrammarException`, `SQLSyntaxErrorException`, `Unknown column`, `doesn't exist` | Una consulta que 8.4 no acepta |

Además de las pruebas del plan (login, consulta de créditos, reporte de aportaciones, recuperar contraseña),
abrir **sin guardar nada** el reporte de un socio con créditos y ahorro y el reporte de descuentos de nómina
de una empresa.

### Mañana, lunes 16

- **Borrado automático de solicitudes:** corre a las 21:00 de hoy, ya en 8.4. `./produccion84.sh objetos despues`
  debe decir `ultima_ejecucion … 2026-11-16 03:00:00`. Lo reviso yo.
- **Respaldo automático de la madrugada:** `./produccion84.sh estado`.
- **Memoria de la instancia:** hoy está justa (alrededor de 88 % en uso; 96 % contando caché; CPU con picos de
  70 %). Si 8.4 usa más y llega al 100 % o hay reinicios, se nota como cortes. Lo reviso con el acceso de lectura.
- **Primer día de la caja y Jorge:** preguntar si algo se sintió lento o raro, y si Jorge se conecta bien.

### Durante la semana

- Velocidad de pantallas y reportes; memoria y reinicios de la instancia.

### Catorcena del 26 de noviembre: primera carga en 8.4 (la más importante)

Después de cargar pagos y aportaciones, antes de seguir, corro una revisión de solo lectura:
- las 2 parejas de cuentas activas con la misma clave y empresa: que el pago y la aportación lleguen a la misma
  cuenta que en catorcenas anteriores;
- los 49 socios con dos pagos pendientes en esa fecha: que cada pago quede en el crédito que le toca con la
  regla de hoy;
- que no quede ningún pago ligado a dos amortizaciones.

Los cambios de empresa se revisan en pantalla antes de aplicarlos, como siempre (hay 151 nombres repetidos).

### Rendimiento de noviembre (fin de mes)

- Anotar cuánto tarda (hoy, decenas de minutos). Si tarda mucho más, revisar la configuración que cambió con
  8.4 (índice hash adaptativo y búfer de cambios: afectan velocidad, no resultados).

### Primeros finiquitos o devoluciones en 8.4

- Revisar con qué empresa quedó el movimiento de devolución. Si no es la empresa actual del socio, avisar: es
  el caso del `ANY_VALUE` (tabla de arriba, renglón 3).

### Principios de diciembre

- Borrar el respaldo manual de 8.0 (después de la catorcena del 26 y del rendimiento).
- Decidir si se borra la red temporal `Mac Ivette 15-nov` y se cierra la sesión de administración.
- Borrar los contenedores locales de prueba (`caja-analisis-84`, `caja-mysql80-control`, `caja-mysql-local`).
- Pasar a la lista de errores de la fase 0: `ORDER BY` explícito en las consultas de la tabla de arriba y la
  empresa de las devoluciones.

## Qué hacer si algo sale distinto

| Síntoma | Qué hacer |
|---|---|
| La aplicación no conecta al arrancar | `./produccion84.sh usuario revertir CONFIRMO-<IP>` (regresa el método de contraseña anterior; misma contraseña) y reiniciar Tomcat |
| Error de SQL en una pantalla | Copiar el mensaje y la hora; no repetir la operación si guardaba algo; avisarme. Se reproduce la consulta en la 8.4 local |
| Un pago o una aportación quedó en otro crédito u otra cuenta | No corregirlo a mano en ese momento; avisarme con el nombre del socio. Se arregla con un ajuste revisado y se agrega el `ORDER BY` que fija la regla |
| Lentitud | Anotar pantalla y hora; reviso métricas y planes de esa consulta |
| Memoria al 100 % o reinicios | Lo reviso; la salida sería un tamaño mayor de instancia, que cuesta más (te diría cuánto antes de proponerlo) |

## Herramientas nuevas (en esta carpeta)

| Archivo | Qué hace | ¿Escribe? |
|---|---|---|
| `ObjetosApp.java` | Evento, rutina y disparadores, con el usuario de la app | No (conexión de solo lectura) |
| `SimulaHbm2ddl.java` | Lo que Hibernate intentaría alterar al arrancar | No (genera el texto; en producción usa `claude_lectura`) |
| `PlanesSql.java` + `planes.sql` | Plan de ejecución de las 18 consultas sensibles al orden | No (solo `EXPLAIN`) |
| `produccion84.sh` | Pasos nuevos `objetos`, `variables`, `hbm2ddl`, `planes`; `comparar` los incluye | No, salvo los pasos que ya pedían confirmación |
