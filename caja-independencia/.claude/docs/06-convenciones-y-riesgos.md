# 06 — Convenciones, trampas y deuda técnica

## Convenciones del código

### Nomenclatura

- **Idioma:** todo en español (clases, métodos, variables, comentarios, mensajes). Mantenlo.
- **Prefijos de columna por tabla:** cada tabla usa un prefijo de 3 letras —
  `usu_`, `sol_`, `cre_`, `amo_`, `pag_`, `mov_`, `ban_`, `ec_`, `ima_`, `ben_`, `bae_`,
  `arh_`, `car_` (catorcenas **y** cargos), `bit_`, `prt_`, `con_`, `cnh_`, `tran_`, `rbe_`, `bec_`.
- **POJOs:** `Usuarios`, `Solicitudes`, `CreditosFinal` — nombre de tabla en CamelCase, a menudo
  en plural.
- **Propiedades Java:** camelCase del nombre de columna: `usu_clave_empleado` → `usuClaveEmpleado`.
- **DTOs:** el nombre de la propiedad debe coincidir con el **alias SQL**, no con la columna:
  `select usu_clave_empleado as cveEmpleado` → `UsuarioDto.cveEmpleado`.
- **Sufijos de clase:** `*Bean` (JSF), `*Bo` (negocio), `*Dao` (datos), `*Dto`, `*Service`.

### Patrón de un ManagedBean

```java
@ManagedBean(name = "miBean")
@ViewScoped
public class MiBean extends BaseBean implements Serializable {
    private static final long serialVersionUID = ...;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MiBean.class);

    private final MiBo bo;

    public MiBean() { bo = new MiBo(); }          // dependencias con `new`, sin CDI

    // La vista lo invoca con:
    //   <f:event type="preRenderComponent" rendered="true" listener="#{miBean.init}" />
    // Es la convención del proyecto: NO se usa preRenderView ni @PostConstruct
    // (solo LoginBean tiene @PostConstruct en todo el proyecto).
    public void init() {
        if (super.validateUser()) {               // ← SIEMPRE primero
            try {
                ...bo.hazAlgo(usuario.getId());
            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
                super.muestraMensajeError("...", "", null);
            }
        }
    }
}
```

Helpers heredados de `BaseBean`:

| Método | Uso |
| --- | --- |
| `validateUser()` | Verifica el `UsuarioDto` en sesión; muestra `dlgSessionLost` si no hay |
| `getUsuarioSesion()` | `(UsuarioDto) session.getAttribute("usuario")` |
| `muestraMensajeExito/Error/Gen(...)` | `FacesMessage` con severidad |
| `muestraMensajeDialog(...)` | Mensaje dentro de un diálogo PrimeFaces |
| `hideShowDlg("PF('dlg').hide()")` | Ejecuta JS vía `RequestContext` |
| `updtComponent("form:tabla")` | Update AJAX programático |
| `validaSiEsCatorcena(fecha)` | Valida contra la tabla `catorcenas` |
| `logout()` / `invalidateSession()` | Cierre de sesión |

### Patrón de un DAO

**Lectura:**
```java
public List<MiDto> consulta(int id) throws IntegracionException {
    try {
        super.beginTransaction();
        SQLQuery q = session.createSQLQuery(
            "select col_a as propA, col_b as propB from tabla where col_c = :id");
        q.setParameter("id", id);
        return q.setResultTransformer(Transformers.aliasToBean(MiDto.class)).list();
    } catch (HibernateException he) {
        throw new IntegracionException(he);
    } finally {
        super.endTransaction();     // ← OBLIGATORIO: cierra la sesión Hibernate
    }
}
```

**Escritura:** usa los helpers de `ManagerDB`, que ya manejan commit/rollback en su propia sesión:
`savePojo`, `savePojos`, `updatePojo`, `mergePojo`, `executeUpdate` (HQL),
`executeUpdateSql` (SQL nativo).

### Manejo de excepciones

```
DAO   → lanza IntegracionException (envuelve HibernateException)
BO    → captura y relanza como BusinessException
Bean  → captura, LOGGER.error(...), y muestra FacesMessage
```

No dejes que una excepción llegue sin traducir a la vista.

---

## Trampas conocidas

### Mappings duplicados

Existen **dos copias** de los `.hbm.xml`:

| Ruta | Cantidad | ¿Se empaqueta? |
| --- | --- | --- |
| `src/main/java/mx/com/evoti/hibernate/pojos/` | 36 | ❌ **NO** — Maven no copia `.xml` desde `src/main/java` |
| `src/main/resources/mx/com/evoti/hibernate/pojos/` | **38** | ✅ Sí — va a `target/classes` |

Los 36 comunes son byte a byte idénticos; los 2 extra (`Configuracion.hbm.xml`,
`PasswordResetTokens.hbm.xml`, los más recientes) **solo existen en `resources/`**.

> **Edita siempre `src/main/resources/`.** Si además quieres mantener la copia sincronizada,
> hazlo, pero la que manda es la de resources. Lo ideal sería borrar la copia de `java/`.

### `hbm2ddl.auto=update`

Al arrancar Tomcat, Hibernate compara los mappings con el esquema y **aplica los cambios que
puede** (agregar columnas y tablas; no borra ni renombra). Consecuencias:

- Agregar un `<property>` a un mapping ⇒ `ALTER TABLE ADD COLUMN` en producción al reiniciar.
- Una propiedad mal escrita crea una columna basura que queda ahí para siempre.
- Cambiar un tipo puede fallar silenciosamente y dejar el mapping desalineado.

Al modificar un mapping: decide el DDL explícito, aplícalo tú, y solo entonces despliega.

### `amo_estatus` (texto) vs `amo_estatus_int` (entero)

`amortizacion` guarda el estatus **dos veces**. Todo `UPDATE` del algoritmo de pagos escribe ambos:

```sql
SET amo_estatus = 'PAGADO', amo_estatus_int = 2
```

Si escribes solo uno, rompes reportes que filtran por el otro. Lo mismo aplica —en menor medida— a
`pag_estatus` / `pag_estatus_amortizacion`.

### Inyección SQL generalizada

La mayoría de los DAOs construyen SQL con `String.format`:

```java
// LoginDao.login() — punto de entrada SIN autenticar
"... where usu_clave_empleado = '%1$s'", nickname
```

Está en decenas de clases. Al tocar un DAO, migra esa consulta a `setParameter`; es una mejora
segura y local. Prioriza `LoginDao` y `PasswordRecoveryDao` (superficie no autenticada).

### `Transformers.aliasToBean` es frágil

El alias SQL debe coincidir **exactamente** (mayúsculas incluidas) con el nombre de la propiedad
del DTO, y el DTO necesita el setter. Un alias mal escrito produce un error en tiempo de ejecución,
no de compilación. Si renombras una propiedad de DTO:

```bash
grep -rn "as nombrePropiedad" src/main/java
```

### Fechas: el ajuste manual de zona horaria

```java
solicitud.setSolFechaCreacion(Util.restaHrToDate(new Date(), 6));
```

Se restan **6 horas** a mano para pasar de UTC a hora del centro de México. Aparece en varios
puntos. No es consistente en todo el código: algunos flujos usan `new Date()` directo. Al comparar
fechas entre módulos, ten presente que pueden diferir en 6 horas.

Además, las columnas de fecha son `DATE` (sin hora) en casi todas las tablas, así que el desfase
puede cambiar el **día** registrado en operaciones cercanas a medianoche.

### Comparaciones monetarias con tolerancia

Nunca se comparan montos por igualdad exacta. Las tolerancias vigentes:

| Contexto | Tolerancia |
| --- | --- |
| Pago exacto (algoritmo) | ±1 peso |
| Amortización 5/6 (multi-crédito) | ±1 peso |
| Abono de ahorros a crédito | $3 |
| Transferencia a avales | $2 |
| Snapshot de baja (deuda/ahorro) | $5 |

Respétalas. Vienen del redondeo `DOWN`/`UP` a 2 decimales del cálculo de amortización.

### Scopes de bean no uniformes

`cargaPagosApoBean` y `generadorReportesBean` son `@SessionScoped`; `asignaPagosBean` y
`altasCambiosBean` son `@RequestScoped`. **Es intencional** (procesos largos que sostienen estado
entre peticiones, o listas grandes que no conviene retener). Cambiarlos rompe esos flujos.

`BaseBean` está anotado `@ManagedBean @SessionScoped` **y además** todos los beans lo extienden.
Funciona, pero significa que existe una instancia de `BaseBean` en sesión que nadie usa.

### El `while` sin tope del algoritmo de pagos

`AlgoritmoAsignaPagosBo.aplicaPagos()` itera hasta que no queden pagos con amortizaciones
duplicadas. Sin contador máximo. Si modificas los `WHERE` de `updtAmortizacionExacto/Mayor/Menor`
o de `getPagosRepetidosAmortizacion`, verifica que el ciclo siga convergiendo.

### Procesos largos sin transacción global

Ni el rendimiento ni el fondeo ni el finiquito corren en una transacción única: cada
`savePojo`/`executeUpdateSql` abre y cierra la suya. Una interrupción deja estado a medias sin
rollback. Antes de re-ejecutar cualquiera de esos procesos, **verifica en BD qué alcanzó a
escribirse** (consultas en [03-base-de-datos.md](03-base-de-datos.md#consultas-útiles-para-diagnóstico)).

### `main()` de utilería en clases de producción

**25 clases** tienen un `public static void main` para pruebas manuales
(`AlgoritmoAsignaPagosBo`, `TablaAmortizacionBo`, `CargaPagosMovimientosBo`, `BancosBo`,
`CambiosEmpresaBo`, `AltasBo`, `UsuariosDao`, `AmortizacionDao`…). Varios llaman a
`HibernateUtil.buildSessionFactory2()` y **operan contra la BD de producción**. No los ejecutes
por accidente.

### Sesiones Hibernate: historia de fallas

Los commits `6beaa4b` y `edf3ed4` corrigen "sesiones fallidas en mysql que provocaban que el
sistema se caiga". Por eso:

- `ManagerDB.list()` está `synchronized`.
- Los helpers de escritura abren una `localSession` propia en vez de reutilizar `this.session`.
- c3p0 tiene `unreturnedConnectionTimeout=600` y `debugUnreturnedConnectionStackTraces=true`.

Con un pool de **máximo 10 conexiones**, una sesión sin cerrar agota el pool rápido. **Siempre**
`endTransaction()` en `finally`.

Nota: `ManagerDB.beginTransaction()` llama a `session.beginTransaction()`, pero los métodos de
lectura solo hacen `close()` sin `commit()`. Es el comportamiento existente; en lecturas es inocuo.

---

## Deuda técnica y riesgos

### Seguridad — lo más urgente

| # | Riesgo | Evidencia |
| --- | --- | --- |
| 1 | **Contraseñas en texto plano** | Verificado en BD: 12,361 de 12,382 con ≤12 caracteres, máx. 18, **cero hashes**. Escritura en `AdminUsuariosBo.updatePassword` y `PasswordRecoveryDao.actualizaPasswordYUsaToken`; comparación con `.equals` en `LoginBean` |
| 2 | **Inyección SQL sin autenticar** | `LoginDao.login()` interpola `nickname` con `String.format` |
| 3 | **Autorización solo visual** | El menú oculta opciones con `rendered="#{...rol != 'Usuario'}"`; no hay filtro ni verificación en los beans. Escribir la URL da acceso |
| 4 | Credenciales versionadas | BD en `hibernate.cfg.xml`; SMTP en `EnviaCorreo`; Tomcat manager en `pom.xml` |
| 5 | Conexión a BD sin TLS | `useSSL=false` contra una IP pública de Cloud SQL. Los certificados existen (`../mysql_ssl/`) pero están sin usar |
| 6 | Enumeración de usuarios | `PasswordRecoveryBo` distingue "clave no existe" de "empresa no coincide" |
| 7 | `show_sql=true` en producción | Vuelca todo el SQL —con datos— al log |

> Si abordas #1, hay que migrar **los tres puntos a la vez** (login, reset por correo, reset del
> panel admin) y planear la transición de los 12,382 registros existentes (típicamente: hashear al
> siguiente login exitoso y forzar cambio a quienes no entren).

### Obsolescencia

| Componente | Versión | Situación |
| --- | --- | --- |
| PrimeFaces | **6.0** (2016) | Muy atrasada; muchas CVE corregidas después |
| Hibernate | 4.3.11 (2015) | Fin de soporte |
| Log4J | **1.x** | Fin de vida desde 2015 |
| commons-fileupload | 1.2.2 (2010) | Vulnerabilidades conocidas |
| iText | 2.1.7 | Muy antigua |
| JasperReports | 6.3.1 | Atrasada |
| Java EE | `javax.*` | Bloquea Jakarta EE 9+ y Tomcat 10+ |

Hay carpetas `.github/java-upgrade/` y `.github/modernize/java-upgrade/` en la raíz del repo:
alguien ya inició un intento de modernización.

### Calidad

- **Cero pruebas.** `src/test/java` existe vacío. Cualquier refactor se verifica solo manualmente.
- **Sin `.gitignore` propio** en la carpeta del proyecto (el de la raíz sí ignora `target/`).
- Servlet `Faces Servlet` declarado dos veces en `web.xml`.
- Código muerto: la lógica de tasa especial del 2018-11-11; `FiniquitoAnteroirBean` (con typo);
  `TablaAmortizacionBo` con constantes comentadas; `if (dtoPago.getPagId() == 90298)` en
  `AlgoritmoAsignaPagosBo`.
- Duplicación: `FiniquitoService.transferir()` vs `transferirCreditoAAvAles()`;
  `marcarIncobrable()` vs `marcarCreditoComoIncobrable()` (idénticos);
  `bo/administrador/finiquito/` vs `service/finiquito/`.
- `System.out.println` mezclado con SLF4J en beans y BOs.
- Rutas hardcodeadas: `Constantes.PATH_DOCTOS` (`c:/Documentos`) y `PATH_DOCTOS_LOCAL`
  (ruta personal de una máquina de desarrollo).
- Mensaje incorrecto: la validación de crédito de auto rechaza `> 300000` pero dice
  *"no puede superar los $150,000"*.
- Menú "Rendimiento → Reporte mensual" sin acción.

### Datos

- **5 tablas huérfanas:** `ajuste` (210), `avales` (0), `bajasahorro` (309), `creditos02` (88),
  y `credito_estatus` (7, **sí en uso** pero sin mapping).
- **8 columnas fuera de los mappings**, de las cuales 5 están muertas
  (`amo_estatus_anterior`, `amo_estatus2`, `pag_estatus_anterior`, `ben_flag`, `flag_abono`)
  y una es funcional pero inaccesible desde el POJO (`solicitudes.sol_numero`, usada por Jasper).
- **Valores de catálogo sin constante:** `solicitud_estatus 9 EN REVISION` (19 solicitudes),
  `amortizacion_estatus 14 CANCELADO` (276), `pagos_estatus 13`, `bancos_conceptos 16-19`,
  `empresas 0`.
- Anomalías: 3 créditos con `cre_producto = 8` (producto de rendimiento, no de crédito),
  1 con producto `NULL`; 1,931 movimientos con `mov_producto = NULL`.
- **Solo 7 llaves foráneas** en toda la BD; el resto de la integridad depende del código.
- Backlogs operativos: 6,206 bajas con ahorro por devolver · 13,455 registros bancarios sin
  conciliar · 7,474 pagos sin amortización · 12 archivos de pagos sin procesar desde 2025-05-26.
- `catorcenas` llega hasta **2027-12-23**: quedan 35 fechas futuras. Habrá que precargar más.

---

## Antes de dar por terminado un cambio

1. `mvn clean package` compila sin errores.
2. Si tocaste un `.hbm.xml`: ¿fue el de **`src/main/resources/`**? ¿Decidiste el DDL en vez de
   dejárselo a `hbm2ddl`?
3. Si tocaste SQL nativo: ¿los alias siguen coincidiendo con el DTO?
   `grep -rn "as nombreProp" src/main/java`
4. Si tocaste amortización: ¿escribiste `amo_estatus` **y** `amo_estatus_int`?
5. Si agregaste un estatus: ¿lo insertaste en la tabla de catálogo **y** en `Constantes`?
6. Si tocaste un DAO: ¿hay `endTransaction()` en el `finally`?
7. Si tocaste montos: ¿respetaste la tolerancia del contexto?
8. ¿Dejaste `System.out.println` o un `main()` nuevo?
