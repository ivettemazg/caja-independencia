# 07 — Recetario de cambios

Procedimientos concretos para los cambios más frecuentes. Cada receta lista **todos** los archivos
a tocar, en orden.

---

## Localizar el código de una funcionalidad

```bash
# 1. Encontrar la pantalla
find src/main/webapp/navegacion -name "*.xhtml" | grep -i <palabra>

# 2. Identificar el bean que usa
grep -oE '#\{[a-zA-Z]+' src/main/webapp/navegacion/<modulo>/<pantalla>.xhtml | sort -u

# 3. Localizar la clase del bean
grep -rn 'name = "<nombreBean>"' src/main/java/mx/com/evoti/presentacion

# 4. Seguir la cadena: el bean instancia su BO en el constructor; el BO sus DAOs.

# 5. Si toca BD, buscar la columna en todo el SQL nativo
grep -rn "nombre_columna" src/main/java
```

El mapa completo pantalla→bean→BO está en
[04-modulos-y-navegacion.md](04-modulos-y-navegacion.md).

---

## Agregar una pantalla nueva

1. **Vista** — `src/main/webapp/navegacion/<modulo>/mi-pantalla.xhtml`:
   ```xml
   <ui:composition xmlns="http://www.w3.org/1999/xhtml"
                   xmlns:h="http://java.sun.com/jsf/html"
                   xmlns:f="http://java.sun.com/jsf/core"
                   xmlns:ui="http://java.sun.com/jsf/facelets"
                   xmlns:p="http://primefaces.org/ui"
                   template="/WEB-INF/template.xhtml">
       <ui:define name="content">
           <div class="layout-portlets-box">
               <h:form id="frmPrincipal">
                   <!-- Convención del proyecto: preRenderComponent, NO preRenderView -->
                   <f:event type="preRenderComponent" rendered="true" listener="#{miBean.init}" />
                   ...
               </h:form>
           </div>
       </ui:define>
   </ui:composition>
   ```

2. **Bean** — `presentacion/<paquete>/MiBean.java`, siguiendo el patrón de
   [06-convenciones-y-riesgos.md](06-convenciones-y-riesgos.md#patrón-de-un-managedbean):
   `@ManagedBean(name="miBean") @ViewScoped`, `extends BaseBean implements Serializable`,
   `serialVersionUID`, LOGGER SLF4J, y `super.validateUser()` como primera línea de `init()`.

3. **Navegación** — método en `NavigationBean`:
   ```java
   public void goToMiPantalla() {
       try {
           ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
           ec.redirect(ec.getRequestContextPath() + "/navegacion/<modulo>/mi-pantalla.xhtml");
       } catch (IOException ex) { LOGGER.error(ex.getMessage(), ex); }
   }
   ```

4. **Menú** — `WEB-INF/layoutmenu.xhtml`, dentro del `<p:submenu>` correspondiente:
   ```xml
   <p:menuitem value="Mi pantalla" action="#{navigationController.goToMiPantalla()}"
               icon="ui-icon-document"/>
   ```
   Si es solo para administradores, el `<p:submenu>` contenedor ya lleva
   `rendered="#{navigationController.usuario.rol != 'Usuario'}"`.

> Recuerda: ese `rendered` **solo oculta**, no protege. Si la pantalla es sensible, valida el rol
> dentro del bean:
> ```java
> if (!Constantes.ROL_USR_USR_I.equals(getUsuarioSesion().getRolId())) { ... }
> ```

---

## Agregar un campo a una entidad existente

Ejemplo: agregar `usu_whatsapp VARCHAR(20)` a `usuarios`.

1. **DDL explícito primero** (no dejes que `hbm2ddl` improvise):
   ```sql
   ALTER TABLE usuarios ADD COLUMN usu_whatsapp VARCHAR(20) NULL;
   ```
   Documenta el script en `docs/sql/`.

2. **Mapping** — `src/main/resources/mx/com/evoti/hibernate/pojos/Usuarios.hbm.xml`
   (⚠️ **resources**, no `java/`):
   ```xml
   <property name="usuWhatsapp" type="string">
       <column name="usu_whatsapp" length="20" />
   </property>
   ```

3. **POJO** — `hibernate/pojos/Usuarios.java`: campo privado + getter/setter.
   Revisa también los **constructores con parámetros**, que en estos POJOs suelen ser largos.

4. **DTO** — si el campo debe llegar a pantalla, agrégalo a `UsuarioDto` con getter/setter.

5. **SQL nativo** — agrega el alias en cada consulta que alimente ese DTO:
   ```sql
   select ..., usu_whatsapp as whatsapp, ... from usuarios ...
   ```
   Búscalas con `grep -rn "as cveEmpleado" src/main/java` (o cualquier alias vecino conocido).

6. **Vista** — el `<p:inputText value="#{perfilBean.usuario.usuWhatsapp}" />` correspondiente.

7. **Verifica** que la copia obsoleta en `src/main/java/.../pojos/Usuarios.hbm.xml` no cause
   confusión después (idealmente actualízala igual, o bórrala).

---

## Agregar un valor de catálogo (estatus, producto, concepto)

Ejemplo: un nuevo estatus de crédito.

1. **Tabla de catálogo en BD** — es la fuente de verdad:
   ```sql
   INSERT INTO credito_estatus (cre_est_id, cre_est_nombre, cre_est_descripcion)
   VALUES (8, 'MI_ESTATUS', 'Descripción de cuándo aplica');
   ```

2. **`Constantes.java`** — agrega la constante siguiendo el bloque existente:
   ```java
   public static final int CRE_EST_MI_ESTATUS = 8;
   ```

3. **Revisa los `WHERE` que enumeran estatus.** Muchas consultas listan valores a mano
   (`cre_estatus IN (1,2)`, `sol_estatus NOT IN (6,7)`, `pag_estatus IN (2,3,4)`):
   ```bash
   grep -rn "cre_estatus" src/main/java | grep -iE "in \(|= *[0-9]"
   ```
   Decide en cada una si el valor nuevo debe incluirse.

4. Si el catálogo tiene columna de color (`amo_est_color`, `pag_est_color`), define el valor y
   añade la clase en `webapp/resources/sindicato/css/colores-estatus.css`.

> Ojo: ya existen valores en BD **sin** constante en `Constantes`
> (`solicitud_estatus 9`, `amortizacion_estatus 14`, `pagos_estatus 13`, `bancos_conceptos 16-19`,
> `empresas 0`). Si tu cambio los toca, agrégalos de paso.

---

## Cambiar una tasa de interés

Las tasas son constantes privadas en `mx.com.evoti.bo.TablaAmortizacionBo`:

```java
private final Double TASA_PORCENTAJE      = .18;   private final Integer TASA       = 18;
private final Double TASA_AUTO_PORCENTAJE = .12;   private final Integer TASA_AUTO  = 12;
private final Double TASA_ESPECIAL_PORCENTAJE = .10; private final Integer TASA_ESPECIAL = 10;
private final Integer TIPO_PAGOS = 26;
```

Cada tasa está **duplicada** (decimal para el pago, entero para el interés). `generaMontoPago()`
usa la decimal; `generaInteres()` la entera. **Cambia siempre las dos.**

Consideraciones:
- La tasa se aplica al **calcular la amortización**, no se guarda en el crédito. Los créditos ya
  fondeados conservan su tabla; solo cambian los nuevos.
- El simulador (`AmortizacionBean`) y el fondeo (`FondeosBo`) usan la misma clase, así que quedan
  consistentes automáticamente.
- Si la tasa debe variar por fecha o producto, lo correcto es sacarla a la tabla `configuracion`
  o a un catálogo nuevo, en vez de agregar otro `if` como el de la tasa especial de 2018.
- **No borres** la lógica de `TASA_ESPECIAL` sin verificar antes que no queden créditos vivos
  creados el 2018-11-11.

---

## Cambiar una regla de elegibilidad

Todo vive en `SolicitudBo`:

| Regla | Método | Constante del mensaje |
| --- | --- | --- |
| Antigüedad en caja / empresa | `validaAntiguedad()` | `MSJ_VALIDACION3MESES`, `MSJ_VALIDACION1ANO`, `MSJ_VALIDACION5ANO` |
| Créditos y solicitudes activas | `validaSolicitudesCreditosActivos()` | `MSJ_2CREDITOSACTIVOS`, `MSJ_VALIDACION1NOMINAACTIVO`, `MSJ_VALIDACION1NOMINAMENOSMITAD`, `MSJ_CREDITOSAUTOACTIVOS` |
| Morosidad | `validaMorosidad()` | (mensaje inline en el bean) |
| Número de avales | `determinaAvalNoAgFaXEmpresa()` | — |

Los umbrales en días están **hardcodeados** en las llamadas:
`validaMesesAntiguedadCaja(90)`, `validaMesesAntiguedadEmpresa(365)`,
`validaMesesAntiguedadEmpresa(1825)`.

El resultado son 8 banderas (`dsblBtnXxA` de antigüedad × `dsblBtnXxC` de créditos) que se
combinan con AND en `validaAntiguedadYCreditos()`. Si agregas una dimensión de validación, sigue
ese patrón: nuevas banderas `dsblBtnXxN` y un AND más.

Los topes de monto y catorcenas máximas están en otro lugar:
`SolicitudCreditoBean.muestraSimulador()` (`topeMaximoPermitido`, `catorcenasMax`).

---

## Agregar o modificar un reporte Jasper

1. Edita el `.jrxml` en `src/main/webapp/reportes/`.
2. **Recompila el `.jasper`** si el reporte lo usa. Cinco de los seis tienen `.jasper` precompilado;
   solo `Finiquito.jrxml` se compila en caliente.
   - `GeneradorReportesBo.crearReporteGenerico()` → compila el `.jrxml` en cada ejecución
   - `crearReporteGenericoSinCompilar()` → carga el `.jasper`
   Para desarrollo, es más simple usar la variante que compila.
3. Los datos vienen de `DoctosJasperSolicitudBo` → `DoctosJasperSolicitudDao`. Si necesitas un
   campo nuevo, agrégalo al SQL con su alias y al DTO correspondiente
   (`dto/jasper/FondeoDto`, `CreditoDto`, `PagoDto`, `PendienteDto`, `ReporteAmortizacionDto`).
4. Los nombres de campo del `.jrxml` (`<field name="...">`) deben coincidir con las propiedades del
   DTO que se pasa como datasource.
5. El bean que sirve el archivo es `generadorReportesBean` (**@SessionScoped**, necesario porque
   PrimeFaces pide el `StreamedContent` en una segunda petición).

---

## Modificar el algoritmo de asignación de pagos

⚠️ Es el proceso más delicado del sistema. Lee
[05-flujos-criticos.md](05-flujos-criticos.md#3-algoritmo-de-asignación-de-pagos) completo antes.

Toda la lógica está en SQL dentro de `AlgoritmoAsignaPagosDao`. Checklist:

1. ¿Tu cambio respeta el filtro `cre_producto IN (6,7) AND cre_estatus = 1`? FA/AG/SAU **no** deben
   entrar al algoritmo automático.
2. ¿Escribes `amo_estatus` **y** `amo_estatus_int` en el mismo UPDATE?
3. ¿El `while` de deduplicación sigue convergiendo con tus nuevos `WHERE`?
4. ¿Mantuviste la tolerancia de ±1 peso en las comparaciones de monto?
5. ¿`pag_acumulado` sigue calculándose correcto en cada fase (menor = depósito completo;
   exacto/mayor = depósito − amortización)?

**Cómo probar sin tocar producción:** toma un `arh_id` real, respalda las filas afectadas, y
compara antes/después:

```sql
-- Antes
SELECT pag_estatus, COUNT(*) FROM pagos WHERE pag_arh_id = ? GROUP BY pag_estatus;
SELECT a.amo_estatus_int, COUNT(*) FROM amortizacion a
  JOIN pagos p ON p.pag_id = a.amo_pago_id WHERE p.pag_arh_id = ?
  GROUP BY a.amo_estatus_int;
```

Para revertir un archivo mal procesado hay que regresar sus pagos a `pag_estatus = 1`,
limpiar `amo_pago_id`, restaurar `amo_estatus`/`amo_estatus_int` a PENDIENTE y volver
`arh_estatus` a 1. **No existe una función de reverso en la aplicación**: es trabajo manual en SQL.

---

## Consultar la base de datos

No hay cliente `mysql` instalado. Opción sin instalar nada (Java 11 ejecuta archivos `.java`
directamente):

```java
// Q.java
import java.sql.*;
public class Q {
    public static void main(String[] a) throws Exception {
        String sql = String.join(" ", a);
        if (!sql.trim().toLowerCase().matches("^(select|show|desc|describe|explain).*")) {
            System.err.println("SOLO LECTURA"); System.exit(2);
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection c = DriverManager.getConnection(
                 "jdbc:mysql://35.225.67.182:3306/sindicato?useSSL=false&allowPublicKeyRetrieval=true",
                 "sindicatoindependencia", "<password de hibernate.cfg.xml>")) {
            c.setReadOnly(true);
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                ResultSetMetaData m = rs.getMetaData();
                while (rs.next()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i <= m.getColumnCount(); i++)
                        sb.append(i>1?"\t":"").append(rs.getObject(i));
                    System.out.println(sb);
                }
            }
        }
    }
}
```

```bash
java -cp ~/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar \
     Q.java "select count(*) from usuarios where usu_estatus = 1"
```

Si la conexión se queda colgada sin error: la IP pública cambió y no está en la whitelist de
Cloud SQL. Obtén la actual con `curl https://ipinfo.io/ip` y agrégala en Google Cloud SQL →
Conexiones → Redes autorizadas.

**Es la BD de producción: solo lectura salvo instrucción explícita.**

---

## Compilar, desplegar y depurar

```bash
mvn clean package                 # genera target/Sindicato.war
mvn clean install -U              # solo si cambiaste el pom.xml

# Despliegue local a mano (el más confiable)
rm -rf /opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/webapps/Sindicato*
mvn clean package -DskipTests
cp target/Sindicato.war /opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/webapps/

# Arranque / paro
brew services start tomcat@8   |   brew services stop tomcat@8
/opt/homebrew/Cellar/tomcat@8/8.5.100/libexec/bin/{startup,shutdown}.sh

# Logs
tail -f /opt/homebrew/Cellar/tomcat@8/*/libexec/logs/catalina.out

# Debug remoto en el puerto 5005
export JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
./catalina.sh run
```

Si Tomcat se cuelga: `ps aux | grep tomcat` y `kill -9 <pid>`.

**Al reiniciar, `hbm2ddl.auto=update` corre contra la BD.** Si acabas de cambiar un mapping y no
querías tocar el esquema, revísalo antes de arrancar.

---

## Trabajo de modernización

Si el objetivo es actualizar el stack, el orden con menos riesgo:

1. **Seguridad primero, sin cambiar versiones:** parametrizar `LoginDao` y `PasswordRecoveryDao`;
   hashear contraseñas (BCrypt) en los tres puntos de escritura; agregar verificación de rol en los
   beans administrativos; sacar credenciales a variables de entorno o JNDI.
2. **Cerrar la brecha de datos:** mapear `credito_estatus`, agregar `sol_numero` al mapping,
   registrar en `Constantes` los valores de catálogo faltantes, borrar la copia obsoleta de
   `.hbm.xml` en `src/main/java/`.
3. **Desactivar `hbm2ddl.auto`** y pasar a scripts DDL versionados (`docs/sql/`) o Flyway.
4. **Red de seguridad:** pruebas sobre `TablaAmortizacionBo` (cálculo puro, sin BD: es el candidato
   ideal para empezar) y sobre el algoritmo de pagos con una BD de prueba.
5. **Actualizar dependencias de una en una**, empezando por las que no cruzan la frontera
   `javax`→`jakarta`: Log4J 1.x → Log4J 2 / Logback, commons-fileupload, iText, JasperReports.
6. **Hibernate 4.3 → 5.x** (mismo `javax.persistence`), verificando `Transformers.aliasToBean`
   (deprecado en 5.2+) y el manejo de sesiones.
7. **PrimeFaces 6 → 8/10** dentro de JSF 2.3. Ojo: el tema `spark` vendorizado
   (`org.primefaces.spark`, 150 clases) está acoplado a PrimeFaces 6 y probablemente haya que
   reescribir el layout.
8. **Solo al final:** `javax` → `jakarta` (JSF 4, PrimeFaces 12+, Hibernate 6, Tomcat 10+).
   Es el salto más grande y toca todos los imports.

Las carpetas `.github/java-upgrade/` y `.github/modernize/java-upgrade/` en la raíz del repo
indican que ya hubo un intento previo; revísalas antes de empezar de cero.
