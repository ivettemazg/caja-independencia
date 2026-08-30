# 01 — Arquitectura, stack y build

## Identidad del artefacto

| Dato | Valor |
| --- | --- |
| groupId / artifactId | `com.sindicato` / `sindicato-web` |
| Versión | `1.0.0` |
| Packaging | `war`, `finalName` = `Sindicato` → `target/Sindicato.war` |
| Contexto de despliegue | `/Sindicato` |
| Paquete raíz Java | `mx.com.evoti` |
| Servidor | Tomcat 8.5 (se despliega con `tomcat7-maven-plugin`) |

Repositorio git: la raíz real es `/Users/ivettemanzano/Projects/Cajaindependencia`, no la carpeta
del proyecto. El `.gitignore` (que ignora `target/`) vive en esa raíz.

## Stack

| Capa | Tecnología | Versión | Nota |
| --- | --- | --- | --- |
| Lenguaje | Java | 11 (source/target) | JDK local: OpenJDK 11.0.16.1 |
| Build | Maven | — | `maven-compiler-plugin` 3.8.1, `maven-war-plugin` 3.3.2 |
| Vista | JSF (Mojarra) | 2.3.9 | Facelets XHTML, `javax.faces.*` (no Jakarta) |
| Componentes UI | PrimeFaces | **6.0** | + tema `spark-theme` 2.1, `primefaces.THEME=spark-blue` |
| CDI | Weld servlet core | 3.1.8 | Declarado, pero **no se usa**: los beans son `@ManagedBean` de JSF |
| ORM | Hibernate Core | **4.3.11.Final** | Mappings XML `.hbm.xml`, sin anotaciones JPA |
| Pool | c3p0 (vía Hibernate) | 0.9.2.1 | min 5 / max 10 conexiones |
| BD | MySQL | driver `mysql-connector-j` 8.4.0 | esquema `sindicato`, dialecto `MySQLDialect` |
| Reportes | JasperReports | 6.3.1 | + iText 2.1.7.js5, Groovy 2.4.15 |
| Excel | Apache POI | 5.2.3 | solo `.xlsx` (XSSF) |
| Correo | Jakarta Mail | 1.6.7 | SMTP propio en `mail.cajaindependencia.com:26` |
| Logging | SLF4J 1.7.30 + Log4J 1.x | — | config en `src/main/resources/log4j.properties` |
| Upload | commons-fileupload 1.2.2 | — | + `PrimeFaces FileUpload Filter` |

Todas las dependencias son de la era `javax.*`. **No hay ninguna dependencia Jakarta EE 9+**; una
migración a Jakarta implicaría cambiar JSF, PrimeFaces, Hibernate y el servidor de aplicaciones.

## Estructura de directorios

```text
caja-independencia/
├── pom.xml
├── Readme.md                    # flujo Maven/Tomcat, debug, whitelist Cloud SQL
├── CLAUDE.md                    # índice de contexto (este set de docs)
├── .claude/docs/                # documentación técnica detallada
├── docs/
│   ├── AI_CONTEXT.md            # resumen previo (superado por .claude/docs)
│   ├── manuales/                # manuales de usuario/admin del reset de password
│   └── sql/password_reset_tokens.sql
├── src/main/java/mx/com/evoti/
│   ├── bo/                      # 47 clases — lógica de negocio
│   ├── dao/                     # 42 clases — acceso a datos
│   ├── dto/                     # 48 clases — transferencia vista/reportes/SQL
│   ├── hibernate/config/        # bootstrap Hibernate (4 clases)
│   ├── hibernate/pojos/         # POJOs persistentes + copia OBSOLETA de .hbm.xml
│   ├── presentacion/            # 55 ManagedBeans JSF
│   ├── service/finiquito/       # capa "service" (solo finiquito, patrón más nuevo)
│   ├── servicio/                # legacy/auxiliar
│   └── util/                    # Constantes.java, Util.java
├── src/main/java/org/primefaces/spark/   # 150 clases — tema vendorizado, NO tocar
├── src/main/resources/
│   ├── hibernate.cfg.xml        # conexión + lista de 38 mappings
│   ├── log4j.properties
│   ├── jasperreports.properties
│   └── mx/com/evoti/hibernate/pojos/*.hbm.xml   # ← MAPPINGS EFECTIVOS (38)
├── src/main/webapp/
│   ├── login.xhtml, dashboard.xhtml, recuperar-password.xhtml, restablecer-password.xhtml
│   ├── WEB-INF/{web.xml, template.xhtml, layoutmenu.xhtml, topbar.xhtml, faces-config.xml}
│   ├── navegacion/**            # ~70 pantallas funcionales
│   ├── reportes/*.jrxml|.jasper # plantillas Jasper + logo
│   ├── resources/               # CSS/JS/imágenes (libraries: spark-layout, sindicato)
│   └── ui/**                    # showcase de PrimeFaces (demo, no es funcionalidad)
└── src/test/java/               # VACÍO — no hay pruebas
```

Conteo real de `.java` de negocio: 393 en total, de los cuales 150 son del tema vendorizado
`org.primefaces.spark`.

## Arranque de la aplicación

`mx.com.evoti.hibernate.config.ContextListener` (`@WebListener` + declarado en `web.xml`):

**`contextInitialized`:**
1. `Class.forName("com.mysql.cj.jdbc.Driver")` — carga forzada del driver para evitar
   "No suitable driver".
2. `PropertyConfigurator.configure(...log4j.properties)` desde el classloader del hilo.
3. `HibernateUtil.buildSessionFactory()` — crea el `SessionFactory` estático único.

**`contextDestroyed`:**
1. `AbandonedConnectionCleanupThread.checkedShutdown()` — evita fugas de hilos del driver MySQL al
   redesplegar.
2. `HibernateUtil.closeSessionFactory()`.

> El cierre del `SessionFactory` está **centralizado aquí**. `ManagerDB.closeSessionFactory()` está
> marcado `@Deprecated` y no debe invocarse en el flujo web.

## Configuración web (`WEB-INF/web.xml`)

- Welcome file: `login.xhtml`.
- `javax.faces.STATE_SAVING_METHOD = server`, `PROJECT_STAGE = Production`.
- `primefaces.THEME = spark-blue`; taglib propia `/WEB-INF/primefaces-spark.taglib.xml`.
- Filtros sobre `Faces Servlet`: `PrimeFaces FileUpload Filter` y
  `org.primefaces.spark.filter.CharacterEncodingFilter`.
- `session-timeout`: **60 minutos**.
- Mime-mappings para fuentes web (ttf/woff/woff2/eot/svg).

**Defecto conocido:** el servlet `Faces Servlet` está declarado **dos veces** (una con
`load-on-startup`, otra sin él). Funciona, pero es una duplicación que conviene limpiar.

## `faces-config.xml` (en `src/main/resources` del tema)

- Registra el componente `SparkMenu` y su renderer.
- `DialogActionListener` / `DialogNavigationHandler` / `DialogViewHandler` de PrimeFaces
  (habilita Dialog Framework, usado por ejemplo en la bitácora).
- `resource-handler`: `mx.com.evoti.hibernate.config.SafeResourceHandler`.
- Un único `navigation-case`: `dlgBitacora` → `/navegacion/bitacora/dlg-bitacora.xhtml`.
  Todo el resto de la navegación es programática (`ExternalContext.redirect`) o por `outcome`
  directo a la vista.

## Configuración Hibernate (`src/main/resources/hibernate.cfg.xml`)

```
url       jdbc:mysql://35.225.67.182:3306/sindicato
          ?zeroDateTimeBehavior=convertToNull&autoReconnect=true
          &allowPublicKeyRetrieval=true&useSSL=false
usuario   sindicatoindependencia
dialecto  org.hibernate.dialect.MySQLDialect
aislamiento  2 (READ_COMMITTED)
show_sql     true          ← ruidoso en producción
hbm2ddl.auto update        ← PELIGRO: modifica el esquema al arrancar
```

Pool c3p0: `min_size=5`, `max_size=10`, `timeout=1000`, `max_statements=60`,
`idle_test_period=600`, `acquire_increment=5`, `testConnectionOnCheckout=true`,
`preferredTestQuery=SELECT 1`, `unreturnedConnectionTimeout=600`,
`debugUnreturnedConnectionStackTraces=true`.

Los últimos dos parámetros y el trabajo de los commits `6beaa4b` / `edf3ed4` responden a un
historial de **fugas de conexión y sesiones colgadas** que tumbaban el sistema. Al escribir DAOs
nuevos, cerrar siempre la sesión en `finally`.

Hay bloques comentados de SSL. En la raíz del repo existen `mysql_ssl/` (pem) y `mysql_cert/`
(p12) preparados pero no activos: la conexión va **sin TLS** (`useSSL=false`).

## Patrón de capas

```
XHTML (#{bean.metodo})
   └─> presentacion/*Bean          @ManagedBean, @ViewScoped/@SessionScoped, extiende BaseBean
         └─> bo/*Bo                reglas de negocio, orquestación, lanza BusinessException
               └─> dao/*Dao        extiende ManagerDB; HQL o SQL nativo; lanza IntegracionException
                     └─> hibernate/pojos/*   POJOs + .hbm.xml
```

- Excepciones: `IntegracionException` (DAO) se envuelve en `BusinessException` (BO) y el bean la
  loguea y muestra un `FacesMessage`.
- DTOs (`dto/`) son el vehículo entre capas para consultas nativas y pantallas; los POJOs
  (`hibernate/pojos/`) solo para persistencia.
- El paquete `service/finiquito/` es un intento posterior de capa de servicio que orquesta varios
  BOs (`FiniquitoService`). Es el patrón más limpio del proyecto y buen modelo a seguir para
  funcionalidad nueva compleja.

## Despliegue

Ver `Readme.md` para el detalle. Resumen:

| Etapa | Comando |
| --- | --- |
| Cambios en `pom.xml` | `mvn clean install -U` |
| Compilar y generar WAR | `mvn clean package` |
| Desplegar / redesplegar | `mvn tomcat7:deploy` / `mvn tomcat7:redeploy` |
| Eliminar del servidor | `mvn tomcat7:undeploy` |

Credenciales de Tomcat manager (`ivette`/`ivette`) están hardcodeadas en el `pom.xml`.

Debug remoto:
```bash
export JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
./catalina.sh run
```
