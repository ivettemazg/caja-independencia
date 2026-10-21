# 04 — Módulos, pantallas y navegación

Mapa para localizar rápido el código de cualquier funcionalidad. El punto de entrada es siempre la
pantalla XHTML; de ahí se sigue el nombre del ManagedBean en las expresiones `#{...}`.

## Cómo se navega

Tres mecanismos conviven:

1. **Redirección programática** (dominante): `NavigationBean` (`#{navigationController}`,
   `@SessionScoped`) tiene ~25 métodos `goToXxx()` que hacen
   `ExternalContext.redirect(contextPath + "/ruta.xhtml")`. Es lo que usa el menú.
2. **`outcome` directo** a la vista, para unas pocas pantallas (crédito seguro de auto, reporte
   personal, reporte usuarios, descuentos por cobrar, reporte bancos, resultado de pagos).
3. **Dialog Framework de PrimeFaces**: un único `navigation-case` en `faces-config.xml`
   (`dlgBitacora` → `navegacion/bitacora/dlg-bitacora.xhtml`).

No hay `navigation-rule` clásicas. Para agregar una pantalla, lo normal es añadir un `goToXxx()` a
`NavigationBean` y un `<p:menuitem>` en `WEB-INF/layoutmenu.xhtml`.

## Plantilla y layout

| Archivo | Rol |
| --- | --- |
| `WEB-INF/template.xhtml` | Plantilla base. Define `<ui:insert name="head">` y `name="content"`. Incluye topbar y menú, el diálogo `dlgSessionLost` ("Se perdió la sesión") y carga los CSS (`spark-layout`, `sindicato/colores-estatus.css`). |
| `WEB-INF/layoutmenu.xhtml` | Menú lateral (`<ps:menu>`, componente vendorizado `SparkMenu`). |
| `WEB-INF/topbar.xhtml` | Barra superior. |
| `WEB-INF/error.xhtml`, `404.xhtml`, `access-denied.xhtml` | Páginas de error. |

Toda pantalla funcional empieza con:

```xml
<ui:composition ... template="/WEB-INF/template.xhtml">
    <ui:define name="content"> ... </ui:define>
</ui:composition>
```

`src/main/webapp/ui/**` (≈200 archivos) es el **showcase de demostración de PrimeFaces** que vino
con el tema. No es funcionalidad del negocio; ignóralo salvo que busques un ejemplo de uso de un
componente.

## Estructura del menú (`layoutmenu.xhtml`)

Todo lo que no sea "Personal" se oculta con
`rendered="#{navigationController.usuario.rol != 'Usuario'}"`.

```
Personal                                    → dashboard.xhtml
Solicitudes
  ├ Pendientes                              → admonsolicitudes/validar-solicitudes.xhtml
  └ Historial solicitudes                   → solicitudes/historial.xhtml
Créditos
  ├ Fondeos                                 → admonsolicitudes/fondeo-principal.xhtml
  ├ Seguimiento documentación fondeos       → admonsolicitudes/f-docs-seguimiento.xhtml
  ├ Crédito seguro de auto                  → admonsolicitudes/credito-sauto.xhtml
  ├ Reporte de créditos                     → creditos/reporte.xhtml
  └ Descuentos de nómina
      ├ Nomina / Automovil                  → creditos/descuentoNomina.xhtml
      └ Fondo Ahorro / Aguinaldo            → creditos/descuentoNominaFAAG.xhtml
Empleados
  ├ Altas y cambios
  │   ├ Aplicar cambios                     → cargapagosaportaciones/cambios-empresa.xhtml
  │   └ Pendientes                          → cargapagosaportaciones/cam-al-pendts.xhtml
  ├ Finiquitos
  │   ├ Dar de baja Usuario                 → finiquito/baja-empleado.xhtml
  │   ├ Bajas pendientes                    → finiquito/bajas-pendientes.xhtml
  │   └ Historial bajas                     → finiquito/historial-bajas.xhtml
  ├ Reporte morosos                         → creditos/reporte-morosos.xhtml
  ├ Reporte personal                        → common/reporte-empleado.xhtml
  └ Reporte Usuarios                        → common/reporteUsuarios.xhtml
Tesorería
  ├ Ahorros por devolver de bajas           → finiquito/ahorrosxdevolver.xhtml
  ├ Descuentos por cobrar de créditos       → tesoreria/desctos-x-cobrar.xhtml
  ├ Reporte bancos                          → bancos/reporte-bancos.xhtml
  └ Ajuste bancos                           → bancos/ajuste-banco.xhtml
Rendimiento
  ├ Proceso                                 → rendimiento/proceso.xhtml
  └ Reporte mensual                         → (sin acción: menuitem vacío)
Pagos/Aportaciones
  ├ Carga de archivos                       → cargapagosaportaciones/carga-archivo.xhtml
  ├ Aplicar pagos                           → cargapagosaportaciones/aplica-pagos.xhtml
  └ Reporte de resultados
      ├ Pagos                               → cargapagosaportaciones/rep-resultado-pagos.xhtml
      └ Aportaciones                        → cargapagosaportaciones/reporteAportaciones.xhtml
Administrador
  ├ Panel de administrador                  → administracion/panel.xhtml
  └ Bitacora de movimientos                 → administracion/reporte-bitacora.xhtml
```

> "Reporte mensual" de Rendimiento es un `<p:menuitem>` **sin `action` ni `outcome`**: no hace nada.

## Registro completo de ManagedBeans

| Bean EL | Scope | Clase |
| --- | --- | --- |
| `loginBean` | Session | `presentacion.LoginBean` |
| `navigationController` | Session | `presentacion.NavigationBean` |
| `dashboardBean` | Session | `presentacion.DashboardBean` |
| (base) | Session | `presentacion.BaseBean` |
| `recuperaPasswordBean` | View | `presentacion.RecuperaPasswordBean` |
| `movsTblBean` | View | `presentacion.MovimientosTblBean` |
| `desctosXCobrarBean` | View | `presentacion.DesctosXCobrarCredsBean` |
| `adminPanelBean` | View | `presentacion.administracion.AdminPanelBean` |
| `solCreditoBean` | View | `presentacion.usuariocomun.SolicitudCreditoBean` |
| `detSolBean` | View | `presentacion.usuariocomun.DetalleSolicitudBean` |
| `misSolsBean` | View | `presentacion.usuariocomun.MisSolicitudesBean` |
| `misCredBean` | View | `presentacion.usuariocomun.MisCreditosBean` |
| `misMovBean` | View | `presentacion.usuariocomun.MisAhorrosBean` |
| `perfilBean` | View | `presentacion.usuariocomun.PerfilBean` |
| `beneficiariosBean` | View | `presentacion.usuariocomun.BeneficiariosBean` |
| `dbancariosBean` | View | `presentacion.usuariocomun.DatosBancariosBean` |
| `doctosSolBean` | View | `presentacion.usuariocomun.DoctosSolBean` |
| `avaSolBean` | View | `presentacion.usuariocomun.SolicitudAvalesBean` |
| `valSolBean` | View | `presentacion.admon.solicitudes.ValidaSolicitudBean` |
| `valSolPpalBean` | View | `presentacion.admon.solicitudes.ValSolPrincipalBean` |
| `valSolDetBean` | View | `presentacion.admon.solicitudes.ValSolDetalleBean` |
| `fondeoBean` | View | `presentacion.admon.solicitudes.FondeosBean` |
| `credsActivBean` | View | `presentacion.admon.SolCreditosActivosBean` |
| `valsolCredHistorial` | View | `presentacion.admon.ValSolCreditosHistorialBean` |
| `repEmpleadoBean` | View | `presentacion.admon.ReporteEmpleadoBean` |
| `amoRepEmpBean` | View | `presentacion.admon.AmortizacionRepEmpBean` |
| `detAdCreBean` | View | `presentacion.admon.DetalleAdeudoCreditosBean` |
| `transAvalesBean` | View | `presentacion.admon.TransfiereAvalesBean` |
| `resultadoPagosBean` | View | `presentacion.admon.ReporteResultadoPagosBean` |
| `bitacoraAdmonBean` | View | `presentacion.admon.ReporteBitacoraBean` |
| `cargaPagosApoBean` | **Session** | `presentacion.admon.pagosaportaciones.CargaArchivoPagMovsBean` |
| `asignaPagosBean` | **Request** | `presentacion.admon.pagosaportaciones.AsignaPagosBean` |
| `altasCambiosBean` | **Request** | `presentacion.admon.pagosaportaciones.CambiosEmpresaBean` |
| `altasBean` | View | `presentacion.admon.pagosaportaciones.AltasUsuarioBean` |
| `reporteAportacionesBean` | View | `presentacion.admon.pagosaportaciones.ReporteAportacionesBean` |
| `bancoAjusteBean` | View | `presentacion.banco.BancoAjusteBean` |
| `repBancosBean` | View | `presentacion.banco.ReporteBancosBean` |
| `bitaBean` | View | `presentacion.bitacora.BitacoraBean` |
| `amortizacionBean` | View | `presentacion.common.AmortizacionBean` |
| `avalBean` | View | `presentacion.common.AvalesTblBean` |
| `busqEmplBean` | View | `presentacion.common.BusquedaEmpleadoBean` |
| `generadorReportesBean` | **Session** | `presentacion.common.GeneradorReportesBean` |
| `reporteUsuariosBean` | View | `presentacion.common.ReporteUsuariosBean` |
| `credSegAuBean` | View | `presentacion.creditos.CreditoSeguroAutoBean` |
| `descuentoNominaBean` | View | `presentacion.creditos.DescuentoNominaBean` |
| `reporteBean` | View | `presentacion.creditos.ReporteBean` |
| `reporteMorososBean` | View | `presentacion.creditos.ReporteMorososBean` |
| `baeBean` | View | `presentacion.finiquito.BajaEmpleadoBean` |
| `bajasPendtsBean` | View | `presentacion.finiquito.BajasPendientesBean` |
| `finBean` | View | `presentacion.finiquito.FiniquitoBean` |
| `finiquitoBean` | View | `presentacion.finiquito.FiniquitoAnteroirBean` (versión antigua) |
| `ahorrosXDevBean` | View | `presentacion.finiquito.AhorrosXDevolverBean` |
| `histBajasBean` | View | `presentacion.finiquito.HistorialBajasBean` |
| `procesoBean` | View | `presentacion.rendimiento.ProcesoBean` |
| `historialBean` | View | `presentacion.solicitudes.HistorialBean` |

Converter: `presentacion.converter.EmpresaConverter`.

> Los 4 beans con scope distinto a `@ViewScoped` (session en carga de archivos y generador de
> reportes; request en asignar pagos y cambios de empresa) **son intencionales**: sostienen estado
> entre peticiones de un proceso largo, o evitan retener listas enormes. No los cambies sin
> entender el flujo.
> `FiniquitoAnteroirBean` (con el typo) es la implementación previa de finiquitos, todavía
> registrada como `finiquitoBean`. La vigente es `FiniquitoBean` (`finBean`).

---

## Módulo: acceso y sesión

| Pantalla | Bean | Backend |
| --- | --- | --- |
| `login.xhtml` | `loginBean` | `LoginBo` → `LoginDao` |
| `recuperar-password.xhtml` | `recuperaPasswordBean` | `PasswordRecoveryBo` → `PasswordRecoveryDao` |
| `restablecer-password.xhtml` | `recuperaPasswordBean` | idem |
| `dashboard.xhtml` | `dashboardBean` | — |
| `registro.xhtml` | — | (sin bean; pantalla no conectada) |

**Sesión:** `LoginBean.iniciaSesion()` guarda el `UsuarioDto` en
`HttpSession` bajo la llave `"usuario"` y redirige a `/dashboard.xhtml`. Todo el resto del sistema
lo recupera con `BaseBean.getUsuarioSesion()` o `super.validateUser()`, que muestra el diálogo
`dlgSessionLost` si no hay usuario.

**Regla de baja en login:** si `usu_fecha_baja` no es nulo, el socio todavía puede entrar durante
**1 mes** después de la fecha de baja; pasado ese plazo se rechaza con `MSJ_ERR_LOGIN_USR_BAJA`.

---

## Módulo: usuario común (`navegacion/usuariocomun/`)

Lo que ve el socio. Es el único módulo accesible con rol `Usuario`.

| Pantalla | Bean | BO principal |
| --- | --- | --- |
| `solicitud-credito.xhtml` | `solCreditoBean` + `amortizacionBean` | `SolicitudBo`, `TablaAmortizacionBo`, `CatorcenasBo`, `ConfiguracionBo` |
| `detalle-solicitud.xhtml` | `detSolBean` | `DetalleSolicitudBo` |
| `mis-solicitudes.xhtml` | `misSolsBean` | `DetalleSolicitudBo` |
| `mis-creditos.xhtml` | `misCredBean` | `CreditosBo`, `TablaAmortizacionBo` |
| `mis-ahorros.xhtml` | `misMovBean` | `MovimientosBo` |
| `mi-perfil.xhtml` / `perfil.xhtml` | `perfilBean` | `PerfilBo` → `UsuariosDao` |
| `beneficiarios.xhtml` | `beneficiariosBean` | `BeneficiariosBo` |
| `datos-bancarios.xhtml` | `dbancariosBean` | `DetalleSolicitudBo.updtDatosBancarios` |
| `documentos-sol.xhtml` | `doctosSolBean` | `DocumentosSolicitudBo` |
| `avales-sol.xhtml` | `avaSolBean` | `AvalesSolicitudBo` |

`mi-perfil.xhtml` no contiene expresiones EL propias: es un contenedor que incluye
`perfil.xhtml`.

El simulador de crédito vive en `solicitud-credito.xhtml` + `common/tablaAmortizacion.xhtml`
(`amortizacionBean`), inyectado con `@ManagedProperty("#{amortizacionBean}")`.

---

## Módulo: validación de solicitudes (`navegacion/admonsolicitudes/`)

| Pantalla | Bean | Rol |
| --- | --- | --- |
| `validar-solicitudes.xhtml` | `valSolBean` | Bandeja de solicitudes en estatus 2 (Validando) |
| `valsol-principal.xhtml` | `valSolPpalBean` | Contenedor con pestañas del expediente |
| `valsol-detalle.xhtml` | `valSolDetBean` | Datos y decisión (autorizar / rechazar) |
| `valsol-documentos.xhtml` | `doctosSolBean` | Aprobar/rechazar cada documento |
| `valsol-ahorros.xhtml` | `valSolDetBean` | Ahorros del solicitante |
| `valsol-creditos.xhtml` | `credsActivBean` | Créditos activos |
| `valsol-creditos-historial.xhtml` | `valsolCredHistorial` | Historial de créditos |
| `amortizacion.xhtml` | `amoRepEmpBean` | Tabla de amortización |

BO: `ValidaSolicitudBo` (bandeja, detalle, actualización) + `DetalleSolicitudBo` (estatus, correos,
motivo de rechazo en bitácora) + `DocumentosSolicitudBo` + `AvalesSolicitudBo`.

## Módulo: fondeo (`navegacion/admonsolicitudes/`)

| Pantalla | Bean | Rol |
| --- | --- | --- |
| `fondeo-principal.xhtml` | (contenedor) | Pestañas del proceso de fondeo |
| `fondeo-pendientes.xhtml` | `fondeoBean`, `bitaBean` | Solicitudes aceptadas listas para fondear |
| `fondeo-deposito.xhtml` | `fondeoBean` | Marcar depósito realizado |
| `f-doc-fondeados.xhtml` | `fondeoBean` | Créditos fondeados esperando documentos firmados |
| `f-doc-enviada.xhtml` | `fondeoBean` | Documentación recibida |
| `f-docs-seguimiento.xhtml` | (contenedor) | Seguimiento global |
| `credito-sauto.xhtml` | `credSegAuBean` | Alta directa de crédito de seguro de auto |

BO: `FondeosBo` → `FondeoDao`, `CreditosDao`. Es donde **nace el crédito** (ver
[05-flujos-criticos.md](05-flujos-criticos.md)).

---

## Módulo: pagos y aportaciones (`navegacion/cargapagosaportaciones/`)

El proceso operativo más importante y delicado.

| Pantalla | Bean | Rol |
| --- | --- | --- |
| `carga-archivo.xhtml` | `cargaPagosApoBean` (**Session**) | Subir `.xlsx` de pagos o aportaciones |
| `aplica-pagos.xhtml` | `asignaPagosBean` (**Request**) | Disparar el algoritmo de asignación |
| `cambios-empresa.xhtml` | `altasCambiosBean` (**Request**) | Aplicar cambios de empresa detectados |
| `cam-al-pendts.xhtml` | `altasBean` | Altas pendientes de socios nuevos |
| `rep-resultado-pagos.xhtml` | `resultadoPagosBean` | Resultado de la asignación |
| `reporteAportaciones.xhtml` | `reporteAportacionesBean` | Resultado de aportaciones |

BOs: `CargaPagosMovimientosBo`, `AlgoritmoAsignaPagosBo`, `AltasBo`, `CambiosEmpresaBo`,
`ReporteAportacionesBo`. Utilería: `LectorExcel`, `AlgoritmoLevensthein` (para emparejar nombres
en cambios de empresa).

### Formato de los archivos `.xlsx`

Solo se aceptan `.xlsx` (POI XSSF); `.xls` se rechaza. Sin encabezado — se procesan todas las filas.

| Col | Archivo de pagos (`tipo 1`) | Archivo de aportaciones (`tipo 2`) |
| --- | --- | --- |
| 0 | Clave de empleado (numérico) | Clave de empleado |
| 1 | Fecha (debe ser catorcena) | Fecha |
| 2 | Monto | Monto |
| 3 | Empresa (id numérico) | Empresa |
| 4 | Nombre | Nombre |
| 5 | — | **Producto** (1 fijo / 2 no fijo / 3 voluntario) |

`LectorExcel.validarFormatoExcel()` valida columnas requeridas (5 vs 6), cuenta filas vacías y
detecta el tipo por la presencia de la columna 6. `limpiaMontos()` normaliza formatos de moneda.

Los archivos reales de ejemplo están en la raíz del repo (`../*.xlsx`), con nombres como
`2026-08-21 prestamo e intereses aeromexico.xlsx` y
`2026-08-21 ahorro fijo no fijo sindicato.xlsx`.

---

## Módulo: créditos y cobranza (`navegacion/creditos/`, `tesoreria/`)

| Pantalla | Bean | BO / DAO |
| --- | --- | --- |
| `creditos/reporte.xhtml` | `reporteBean` | `creditos.ReporteDao` |
| `creditos/reporte-morosos.xhtml` | `reporteMorososBean` | `MorosoBo`, `creditos.ReporteMorososDao` |
| `creditos/descuentoNomina.xhtml` | `descuentoNominaBean` | `DescuentoNominaBo` → `DescuentoNominaDao` |
| `creditos/descuentoNominaFAAG.xhtml` | `descuentoNominaBean` | idem (variante FA/AG) |
| `tesoreria/desctos-x-cobrar.xhtml` | `desctosXCobrarBean` | `BancosBo` |

Los **descuentos de nómina** son el archivo que la caja entrega a cada empresa indicando cuánto
descontar a cada trabajador en la catorcena. Es la contraparte del archivo de pagos que la empresa
devuelve.

---

## Módulo: reporte personal (`navegacion/common/reporte-empleado.xhtml`)

La pantalla más rica del sistema: expediente 360° de un socio, y desde donde se ejecutan casi todas
las operaciones manuales.

Bean principal `repEmpleadoBean` (`ReporteEmpleadoBean`), apoyado por fragmentos:

| Fragmento incluido | Bean | Contenido |
| --- | --- | --- |
| `busqueda-empleado.xhtml` | `repEmpleadoBean` | Buscador de socio |
| `ahorros-rp.xhtml` | `repEmpleadoBean` | Ahorros y rendimientos |
| `creditos-adeudo-tbl.xhtml` | `detAdCreBean` | Créditos y adeudo |
| `credito-padre.xhtml` | `repEmpleadoBean` | Crédito origen (transferencias) |
| `amortizacion-rep-e.xhtml` | `amoRepEmpBean` | Tabla de amortización |
| `avales-rep-e.xhtml` | `repEmpleadoBean` | Avales |
| `pagos-Personal.xhtml` | `repEmpleadoBean` | Pagos y acumulado |
| `notas-repe.xhtml` | `repEmpleadoBean` | Notas de bitácora |
| `transf-avales.xhtml` | `transAvalesBean` | Transferir crédito a avales |
| `tablaAmortizacion.xhtml` | `amortizacionBean` | Simulador reutilizable |
| `ahorros-tabla.xhtml`, `ahorros-tab-ajuste.xhtml` | `finBean` | Ahorros en finiquito |
| `reporteUsuarios.xhtml` | `reporteUsuariosBean` | Listado de usuarios |

Procesos disparados desde aquí (`bo/reporteEmpleado/`):

| Proceso | Clase | Qué hace |
| --- | --- | --- |
| Pago a capital | `ProcesoPagoCapitalBo` | Abona a capital y **recalcula toda la amortización restante** |
| Pago acumulado | `ProcesoPagoAcumuladoBo` | Aplica el saldo a favor a amortizaciones pendientes |
| Pago extemporáneo | `ProcesoPagoExtemporaneoBo` | Registra un pago fuera del archivo de nómina |
| Recorrer catorcenas | `ProcesoRecorreCatorcenasBo` | Desplaza las fechas de pago pendientes |

Cada uno registra su acción en `registro_transaccion` (tipos 17–23).

---

## Módulo: finiquitos y bajas (`navegacion/finiquito/`)

| Pantalla | Bean | Rol |
| --- | --- | --- |
| `baja-empleado.xhtml` | `baeBean` | Dar de baja: snapshot de ahorros y deuda |
| `bajas-pendientes.xhtml` | `bajasPendtsBean` | Bajas con deuda de crédito pendiente |
| `ahorrosxdevolver.xhtml` | `ahorrosXDevBean` | Bajas con ahorro por devolver (**6,206 en cola**) |
| `finiquito.xhtml` | `finBean` | Cálculo y aplicación del finiquito |
| `historial-bajas.xhtml` | `histBajasBean` | Consulta histórica |

Backend: `FiniquitoService` (capa service, orquesta varios BOs) + `FiniquitosBo`,
`HistorialBajasBo`, `FiniquitoDao`, `ValidadorFiniquito`, y el reporte `Finiquito.jrxml`.

---

## Módulo: bancos y conciliación (`navegacion/bancos/`)

| Pantalla | Bean | Rol |
| --- | --- | --- |
| `ajuste-banco.xhtml` | `bancoAjusteBean` | Conciliar `bancos` ↔ `estado_cuenta` |
| `reporte-bancos.xhtml` | `repBancosBean` | Reporte de movimientos bancarios |

Backend: `BancoAjustesBo`, `BancosBo` → `bancos.BancosDao`.

---

## Módulo: rendimiento (`navegacion/rendimiento/proceso.xhtml`)

Bean `procesoBean` (`ProcesoBean`), DAO directo `rendimiento.ProcesoDao` (**sin BO intermedio**;
`RendimientoReportBo` existe pero solo tiene un `main`). Ver el cálculo en
[05-flujos-criticos.md](05-flujos-criticos.md#proceso-de-rendimiento-mensual).

---

## Módulo: administración (`navegacion/administracion/`)

| Pantalla | Bean | Operaciones |
| --- | --- | --- |
| `panel.xhtml` | `adminPanelBean` | Banderas FA/AG globales; buscar empleado; habilitar/bloquear; **resetear contraseña** |
| `reporte-bitacora.xhtml` | `bitacoraAdmonBean` | Consulta de `registro_transaccion` por tipo |

Backend: `AdminUsuariosBo` (`updateUsuario`, `updatePassword`), `ConfiguracionBo` (`updateFlags`),
`BusquedaEmpleadoBo` (`updtUsrHabilitar`, `updtUsrOmitirVals`).

## Módulo: bitácora (`navegacion/bitacora/dlg-bitacora.xhtml`)

Diálogo reutilizable (`bitaBean`) invocado con el outcome `dlgBitacora`. Escribe en `bitacora` vía
`BitacoraBo.saveBitacora(solId, tipoBit, usuId, subreferencia, motivo)`.

## Módulo: historial de solicitudes (`navegacion/solicitudes/`)

`historial.xhtml` (`historialBean` → `HistorialSolicitudesBo` → `solicitudes.HistorialDao`) y
`hist-documentos.xhtml` (`doctosSolBean`).

---

## Reportes Jasper

Plantillas en `src/main/webapp/reportes/`:

| Archivo | Documento |
| --- | --- |
| `SolicitudCredito.jrxml/.jasper` | Solicitud de crédito |
| `AnexoATblAmort.jrxml/.jasper` | Anexo A — tabla de amortización |
| `AnexoBPagare.jrxml/.jasper` | Anexo B — pagaré |
| `AnexoCPagare.jrxml/.jasper` | Anexo C — pagaré de avales |
| `Aviso.jrxml/.jasper` | Aviso de privacidad / retención |
| `Finiquito.jrxml` | Finiquito (**solo fuente, sin `.jasper` compilado**) |

Generación: `GeneradorReportesBo.crearReporteGenerico()` (compila el `.jrxml` en caliente) y
`crearReporteGenericoSinCompilar()` (usa el `.jasper`). Los datos los arma
`DoctosJasperSolicitudBo` → `DoctosJasperSolicitudDao`, y el bean de pantalla es
`generadorReportesBean` (**Session scoped**, porque sirve el `StreamedContent` en una segunda
petición).

> `Finiquito.jrxml` no tiene `.jasper`: ese reporte se compila en cada ejecución. Si lo modificas,
> no necesitas recompilar; los otros cinco sí requieren regenerar el `.jasper`.
