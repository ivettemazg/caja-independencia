# Contexto tecnico para IA - Caja Independencia

Este documento es el punto de entrada recomendado para cualquier IA o desarrollador que trabaje en este proyecto. Resume la arquitectura, los modulos funcionales, las convenciones de codigo y la estructura de base de datos sin tener que leer todo el codigo fuente.

## Resumen ejecutivo

`caja-independencia` es una aplicacion web Java EE clasica para administrar una caja de ahorro/sindicato. El artefacto Maven genera un WAR llamado `Sindicato.war`, desplegado normalmente en Tomcat bajo el contexto `/Sindicato`.

La aplicacion usa JSF 2.3 con PrimeFaces 6.0 y tema Spark para la capa web. La persistencia se implementa con Hibernate 4.3 usando archivos XML `*.hbm.xml`, no anotaciones JPA. La base de datos principal es MySQL y el esquema configurado se llama `sindicato`.

El dominio cubre usuarios/empleados, beneficiarios, solicitudes de credito, avales, creditos fondeados, tablas de amortizacion, pagos, movimientos de ahorro, carga de archivos de pagos/aportaciones, conciliacion bancaria, bajas/finiquitos, rendimiento, bitacora y reportes Jasper.

## Stack principal

| Capa | Tecnologia |
| --- | --- |
| Lenguaje | Java 11 |
| Build | Maven, packaging `war` |
| Servidor objetivo | Tomcat 8.x, aunque se usa `tomcat7-maven-plugin` para deploy |
| UI | JSF 2.3, Facelets XHTML, PrimeFaces 6.0, Spark Theme |
| Beans web | `javax.faces.bean.ManagedBean` con scopes JSF (`SessionScoped`, `ViewScoped`, `RequestScoped`) |
| Persistencia | Hibernate 4.3, mappings XML `*.hbm.xml`, `SessionFactory` singleton |
| Base de datos | MySQL, esquema `sindicato` |
| Pool | c3p0 por configuracion Hibernate |
| Reportes | JasperReports 6.3.1, plantillas `.jrxml` y `.jasper` |
| Excel | Apache POI |
| Mail | Jakarta Mail 1.6.7 |
| Logs | SLF4J + Log4J 1.x |

## Entrypoints y configuracion

- `pom.xml`: define dependencias, Java 11, `finalName` `Sindicato` y deploy a `/Sindicato`.
- `src/main/webapp/WEB-INF/web.xml`: configura `FacesServlet` para `*.xhtml`, PrimeFaces FileUpload, encoding filter, tema `spark-blue`, timeout de sesion de 60 minutos y el listener de Hibernate.
- `src/main/resources/hibernate.cfg.xml`: configura MySQL, c3p0, `show_sql=true`, `hbm2ddl.auto=update` y todos los mappings XML.
- `src/main/resources/log4j.properties`: configuracion de Log4J.
- `src/main/java/mx/com/evoti/hibernate/config/ContextListener.java`: inicializa el driver MySQL, Log4J y `HibernateUtil.buildSessionFactory()` al levantar la aplicacion; al destruir el contexto cierra `AbandonedConnectionCleanupThread` y `SessionFactory`.
- `src/main/java/mx/com/evoti/hibernate/config/HibernateUtil.java`: mantiene el `SessionFactory` estatico de Hibernate.

Importante: `hibernate.cfg.xml` contiene actualmente datos sensibles de conexion. No duplicar esos valores en documentacion, issues o commits. Si se modifica infraestructura, priorizar mover host, usuario y password a variables de entorno, JNDI o configuracion externa.

## Estructura del codigo

```text
src/main/java/mx/com/evoti
  bo/                 Logica de negocio y orquestacion
  dao/                Acceso a datos Hibernate/HQL/SQL nativo
  dto/                Objetos de transferencia para pantallas, reportes y consultas SQL
  hibernate/config/   Bootstrap de Hibernate
  hibernate/pojos/    POJOs persistentes + mappings XML .hbm.xml
  presentacion/       ManagedBeans JSF usados por las paginas XHTML
  service/            Servicios puntuales, actualmente finiquito
  servicio/           Codigo legacy/auxiliar
  util/               Constantes y utilidades generales

src/main/webapp
  login.xhtml, dashboard.xhtml, panel.xhtml
  WEB-INF/template.xhtml, layoutmenu.xhtml, topbar.xhtml
  navegacion/**       Paginas funcionales de usuario, administracion, creditos, bancos, finiquito, etc.
  reportes/**         Plantillas Jasper y logo
```

Conteo aproximado al documentar: `presentacion` 55 clases Java, `bo` 47, `dao` 42 y `dto` 48. `src/test/java` existe, pero no contiene pruebas.

## Patron de capas

El flujo tipico es:

1. Una pagina XHTML invoca acciones o lee propiedades de un ManagedBean en `mx.com.evoti.presentacion`.
2. El bean valida sesion, prepara DTOs y llama a una clase `Bo`.
3. El `Bo` concentra reglas de negocio y llama a uno o varios `Dao`.
4. El `Dao` extiende o usa `ManagerDB`, abre una sesion Hibernate, ejecuta HQL o SQL nativo y transforma resultados a POJOs/DTOs.
5. Los POJOs persistentes viven en `mx.com.evoti.hibernate.pojos` y se mapean con `*.hbm.xml`.

No hay inyeccion CDI/Spring para la mayoria de dependencias; muchas clases construyen sus BO/DAO con `new`. Para cambios nuevos conviene seguir el patron local salvo que se haga una refactorizacion deliberada.

## Sesion y autenticacion

- `LoginBean` autentica con `LoginBo.login(nickname)`.
- La contrasena se compara directamente contra `UsuarioDto.password`; no se observa hashing en el flujo actual.
- En login exitoso se guarda `UsuarioDto` en la sesion HTTP con la llave `"usuario"` y redirige a `/dashboard.xhtml`.
- `login.xhtml` enlaza a `recuperar-password.xhtml`. La recuperacion pide clave de empleado, empresa y correo; solo procede para usuarios activos que ya actualizaron perfil (`usu_primeravez = 0`).
- `BaseBean` concentra helpers de JSF: obtener sesion, logout, mensajes PrimeFaces/JSF, validacion de catorcena y `validateUser()`.
- `NavigationBean` concentra redirecciones a pantallas.

Para nuevas pantallas, revisar primero si el bean debe extender `BaseBean` y validar usuario con el mismo mecanismo de sesion.

## Modulos funcionales

| Modulo | Pantallas/paquetes principales | Responsabilidad |
| --- | --- | --- |
| Usuario comun | `navegacion/usuariocomun`, `presentacion/usuariocomun`, `bo/usuarioComun` | Solicitar credito, ver ahorros, creditos, solicitudes, beneficiarios, datos bancarios y perfil. |
| Administracion de solicitudes | `navegacion/admonsolicitudes`, `presentacion/admon/solicitudes`, `bo/administrador/solicitud` | Validar solicitudes, revisar documentos/avales, autorizar/rechazar, fondeo y seguimiento. |
| Creditos | `navegacion/creditos`, `CreditosBo`, `TablaAmortizacionBo`, reportes de creditos | Reportes, descuentos de nomina, morosos y manejo de creditos activos/fondeados. |
| Pagos y aportaciones | `navegacion/cargapagosaportaciones`, `presentacion/admon/pagosaportaciones`, `bo/administrador/algoritmopagos` | Carga de archivos, asignacion de pagos, cambios de empresa, reporte de resultados y aportaciones. |
| Bancos | `navegacion/bancos`, `presentacion/banco`, `bo/bancos`, `dao/bancos` | Ajuste/conciliacion de movimientos bancarios contra estado de cuenta o registros del sistema. |
| Finiquito/bajas | `navegacion/finiquito`, `presentacion/finiquito`, `service/finiquito`, `bo/administrador/finiquito` | Baja de empleados, calculo de ahorros/deudas, finiquito, historiales y documentos. |
| Rendimiento | `navegacion/rendimiento`, `bo/reporteRendto`, `dao/rendimiento` | Proceso/reporte de rendimiento de inversiones/intereses. |
| Bitacora | `navegacion/bitacora`, `bo/bitacora`, `dao/bitacora` | Registro y consulta de eventos de negocio. |
| Reportes | `bo/jasper`, `dao/jasper`, `webapp/reportes` | Generacion de PDFs/documentos Jasper para solicitudes, pagare, amortizacion, aviso y finiquito. |
| Recuperacion de password | `recuperar-password.xhtml`, `restablecer-password.xhtml`, `RecuperaPasswordBean`, `PasswordRecoveryBo`, `PasswordRecoveryDao` | Flujo publico para solicitar liga por correo y restablecer password con token de un solo uso. |

## Reglas y constantes de dominio

`mx.com.evoti.util.Constantes` es el principal catalogo de IDs y literales usados en reglas de negocio. Antes de agregar un nuevo estatus o producto revisar este archivo y los catalogos en BD.

Valores relevantes:

- Roles: `1 Administrador`, `2 Auxiliar`, `3 Usuario`.
- Productos de credito: `FA=4`, `AG=5`, `NO=6`, `AU=7`, `SAU=11`.
- Estatus de solicitud: aceptada `3`, fondeada `4`, documentos enviados `5`, rechazada `7`, documentos aprobados `8`.
- Estatus de credito: activo `1`, pagado `2`, cancelado `3`, transferido `4`, incobrable `5`, ajustado `6`, ajuste finiquito `7`.
- Estatus de amortizacion: pendiente `1`, pagado `2`, pago menor `3`, pago mayor `4`, pago acumulado `5`, extemporaneo `6`, capital `7`, abono credito `8`, finiquito `9`, deuda final `10`, transferencia `11`, incobrable `12`, recorrida `13`.
- Estatus de pago: pendiente `1`, exacto `2`, menor `3`, mayor `4`, acumulado `8`, capital `9`, extemporaneo `10`, devolucion `11`, finiquito `12`.
- Estatus baja empleado: iniciada `0`, pendiente `1`, ahorros por devolver `2`, completada `3`.
- Ajuste banco/estado de cuenta: no ajustado `0`, ajustado `1`, parcial `2`.

## Persistencia y transacciones

`ManagerDB` es la base comun de acceso a datos. Sus metodos abren sesiones con `HibernateUtil.getSessionFactory().openSession()`.

Puntos importantes:

- `beginTransaction()` inicia transaccion, pero algunos metodos de lectura solo cierran sesion en `endTransaction()` sin `commit`; este es comportamiento existente.
- `savePojo`, `updatePojo`, `mergePojo`, `executeUpdate` y `executeUpdateSql` usan transaccion local con commit/rollback.
- Muchos DAOs usan `createSQLQuery` con SQL nativo y `Transformers.aliasToBean`, por lo que los alias SQL deben coincidir con propiedades de DTO.
- Existen consultas construidas con `String.format`; al tocar esas zonas, revisar riesgo de inyeccion SQL y preferir parametros Hibernate cuando sea viable.
- `HibernateUtil.closeSessionFactory()` esta centralizado en `ContextListener`; no cerrarlo desde DAOs durante el flujo web normal.

## Base de datos

Fuente de verdad local: `src/main/resources/hibernate.cfg.xml` y `src/main/java/mx/com/evoti/hibernate/pojos/*.hbm.xml`.

La configuracion Hibernate apunta al esquema MySQL `sindicato`. `hbm2ddl.auto=update` esta activo, por lo que Hibernate puede intentar ajustar el esquema al arrancar. Tratar cambios de mapping con cuidado y respaldar BD antes de cambios estructurales.

### Relaciones principales

- `empresas` 1-N `usuarios` por `usuarios.usu_empresa`.
- `usuarios` 1-N `solicitudes` por `solicitudes.sol_usu_id`.
- `productos` 1-N `solicitudes` por `solicitudes.sol_producto`.
- `solicitud_estatus` 1-N `solicitudes` por `solicitudes.sol_estatus`.
- `solicitudes` 1-N `solicitud_avales` por `solicitud_avales.sol_ava_solicitud`.
- `solicitudes` 1-N `imagenes` por `imagenes.ima_solicitud`.
- `creditos_final` 1-N `amortizacion` por `amortizacion.amo_credito`.

Hay columnas que funcionan como referencias logicas pero no estan declaradas como `many-to-one` en los mappings, por ejemplo `beneficiarios.ben_usu_id`, `movimientos.mov_usu_id`, `pagos.pag_usu_id`, `pagos.pag_credito`, `creditos_final.cre_usu_id`, `creditos_final.cre_solicitud`, `baja_empleados.bae_id_empleado`, `bitacora.bit_usuario` y campos de empresa/archivo. Antes de cambiar nombres o tipos revisar consultas SQL nativas.

### Tablas mapeadas por Hibernate

| Tabla | PK | Columnas mapeadas | Relaciones declaradas |
| --- | --- | --- | --- |
| `altas_cambios_hist` | `cnh_id` | `cnh_usu_id`, `cnh_arh_id`, `cnh_tipo`, `cnh_clave_anterior`, `cnh_clave_actual`, `cnh_empresa_anterior`, `cnh_empresa_actual`, `cnh_catorcena_transaccion`, `cnh_mov_id`, `cnh_fecha` |  |
| `amortizacion` | `amo_id` | `amo_numero_pago`, `amo_capital`, `amo_amortizacion`, `amo_interes`, `amo_iva`, `amo_monto_pago`, `amo_solicitud`, `amo_saldo`, `amo_fecha_pago`, `amo_estatus`, `amo_clave_empleado`, `amo_pago_id`, `amo_usu_id`, `amo_producto`, `amo_estatus_int` | `amo_credito -> creditos_final` |
| `amortizacion_estatus` | `amo_est_id` | `amo_est_nombre`, `amo_est_descripcion` |  |
| `archivos_historial` | `arh_id` | `arh_nombre_archivo`, `arh_fecha_subida`, `arh_empresa`, `arh_estatus`, `arh_registros`, `arh_tipo_archivo`, `arh_fecha_catorcena` |  |
| `baja_empleados` | `bae_id` | `bae_id_empleado`, `bae_estatus`, `bae_fecha_administracion`, `bae_monto_finiquito`, `bae_ahorros`, `bae_deuda_creditos`, `bae_clabe`, `bae_cuenta`, `bae_banco`, `bae_fecha_creacion`, `bae_fecha_pdf`, `bae_fecha_correo`, `bae_ruta_archivo`, `bae_fecha_baja`, `bae_fecha_deposito` |  |
| `banco_edocta` | `bec_id_banco`, `bec_id_edocta` | `bec_fecha_transaccion` |  |
| `bancos` | `ban_id` | `ban_concepto`, `ban_id_concepto_sistema`, `ban_ajustado`, `ban_monto`, `ban_empresa`, `ban_fechatransaccion`, `ban_fecha_relacion`, `ban_id_relacion` |  |
| `bancos_conceptos` | `cban_id` | `cban_nombre`, `cban_tipo`, `cban_tabla`, `cban_columnapk` |  |
| `beneficiarios` | `ben_id` | `ben_nombre`, `ben_paterno`, `ben_materno`, `ben_parentesco`, `ben_pct`, `ben_direccion`, `ben_telefono`, `ben_celular`, `ben_usu_id` |  |
| `bitacora` | `bit_id` | `bit_tipo`, `bit_nota`, `bit_titulo`, `bit_fecha`, `bit_usuario`, `bit_referencia`, `bit_subreferencia` |  |
| `bitacora_transacciones` | `bitra_id` | `bitra_nombre`, `bitra_descripcion` |  |
| `cargos` | `id_cargo` | `car_credito`, `car_numero_pago`, `car_catorcenas`, `car_saldo`, `car_amortizacion`, `car_interes`, `car_pago`, `car_fecha`, `car_saldo_actual` |  |
| `catorcenas` | `car_id` | `car_fecha`, `car_dia`, `car_mes`, `car_anio`, `car_inversiones` |  |
| `creditos` | `cre_id` | `cre_clave_credito`, `cre_prestamos`, `cre_pagos`, `cre_fecha_inicio`, `cre_fecha_termino`, `cre_prospecto`, `cre_clave_empleado`, `cre_fondeo`, `cre_fondeador` |  |
| `creditos_final` | `cre_id` | `cre_fecha_deposito`, `cre_fecha_incobrable`, `cre_empresa`, `cre_nombre`, `cre_tipo`, `cre_prestamo`, `cre_catorcenas`, `cre_fecha_primer_pago`, `cre_clave_empleado`, `cre_producto`, `cre_solicitud`, `cre_pago_quincenal`, `cre_saldo`, `cre_clave`, `cre_estatus`, `cre_usu_id`, `cre_padre`, `cre_fecha_nuevo_monto` | `creditos_final` 1-N `amortizacion` |
| `devoluciones` | `dev_id` | `dev_acum_id`, `dev_monto` |  |
| `empresas` | `emp_id` | `emp_descripcion`, `emp_abreviacion`, `emp_rfc`, `emp_telefono`, `emp_direccion` | `empresas` 1-N `usuarios` |
| `estado_cuenta` | `ec_id` | `ec_concepto`, `ec_monto`, `ec_empresa`, `ec_fechatransaccion`, `ec_ajustado`, `ec_padre`, `ec_descripcion`, `ec_fecha_relacion`, `ec_id_relacion` |  |
| `imagenes` | `ima_id` | `ima_imagen`, `ima_tipoimagen`, `ima_estatus`, `ima_observaciones` | `ima_solicitud -> solicitudes` |
| `movimientos` | `mov_id` | `mov_fecha`, `mov_usu_id`, `mov_deposito`, `mov_producto`, `mov_clave_empleado`, `mov_empresa`, `mov_tipo`, `mov_arh_id`, `mov_nombre_empleado`, `mov_id_padre`, `mov_estatus`, `mov_ar`, `mov_bandera`, `mov_cambioanfaf` |  |
| `pagos` | `pag_id` | `pag_clave_empleado`, `pag_fecha`, `pag_deposito`, `pag_acumulado`, `pag_empresa`, `pag_credito`, `pag_usu_id`, `pag_estatus`, `pag_usu_nombre`, `pag_arh_id`, `pag_estatus_amortizacion`, `antes7` |  |
| `pagos_estatus` | `pag_est_id` | `pag_est_nombre`, `pag_est_descripcion` |  |
| `password_reset_tokens` | `prt_id` | `prt_usu_id`, `prt_token_hash`, `prt_fecha_creacion`, `prt_fecha_expira`, `prt_fecha_uso`, `prt_estatus`, `prt_ip_solicitud`, `prt_user_agent` | Referencia logica a `usuarios.usu_id`; guarda hash de token, no token plano. |
| `parentesco_ben` | `par_id` | `par_nombre` |  |
| `productos` | `pro_id` | `pro_descripcion`, `pro_siglas` | `productos` 1-N `solicitudes` |
| `registro_transaccion` | `tran_id` | `tran_id_usuario`, `tran_fecha`, `tran_tipo_tran`, `tran_id_sistema` |  |
| `rel_banco_edocta` | `rbe_id`, `rbe_id_banco_ec`, `rbe_tipo` | `rbe_fecha_rel` |  |
| `rendimiento` | `ren_id` | `ren_fecha`, `ren_interes`, `ren_acumulado`, `ren_factor`, `ren_estatus`, `ren_intereses_inversion`, `ren_comisiones_bancarias`, `ren_reserva`, `ren_ganancia_neta` |  |
| `rendimiento3` | `ren_id` | `ren_fecha`, `ren_interes`, `ren_acumulado`, `ren_factor`, `ren_estatus` |  |
| `roles` | `rol_id` | `rol_nombre` |  |
| `solicitud_avales` | `id_sol_ava` | `sol_ava_clave_empleado`, `sol_ava_credito`, `sol_ava_id_empleado`, `sol_ava_estatus` | `sol_ava_solicitud -> solicitudes` |
| `solicitud_estatus` | `sol_est_id` | `sol_est_nmbr_est`, `sol_est_descripcion` | `solicitud_estatus` 1-N `solicitudes` |
| `solicitudes` | `sol_id` | `sol_clave_empleado`, `sol_sueldo_neto`, `sol_deducciones`, `sol_monto_solicitado`, `sol_catorcenas`, `sol_pago_credito`, `sol_banco`, `sol_numero_cuenta`, `sol_clabe_interbancaria`, `sol_referencia`, `sol_no_poliza`, `sol_aseguradora`, `sol_nombre_tarjetahabiente`, `sol_pago_total`, `sol_aguinaldo`, `sol_fa`, `sol_intereses`, `sol_observacion`, `sol_fecha_ult_catorcena`, `sol_fecha_autorizacion`, `sol_fecha_creacion`, `sol_fecha_cancelacion`, `sol_fecha_fondeo`, `sol_fecha_enviodocumentos`, `sol_facatorcena`, `sol_fecha_deposito`, `sol_motivo_rechazo`, `sol_estatus_db`, `sol_formato_doc_firmada` | `sol_producto -> productos`, `sol_estatus -> solicitud_estatus`, `sol_usu_id -> usuarios`, `solicitudes` 1-N `solicitud_avales`, `solicitudes` 1-N `imagenes` |
| `tabulador` | `tab_id` | `tab_descripcion`, `tab_mensual`, `tab_diario`, `tab_catorcenal` |  |
| `tipo_transaccion` | `tipo_tran_id` | `tipo_tran`, `tipo_tran_descripcion` |  |
| `usuarios` | `usu_id` | `usu_numero_empleado`, `usu_clave_empleado`, `usu_nombre`, `usu_paterno`, `usu_materno`, `usu_edo_civil`, `usu_correo`, `usu_estado`, `usu_rfc`, `usu_puesto`, `usu_telefono`, `usu_extension`, `usu_departamento`, `usu_area_trabajo`, `usu_estacion`, `usu_fecha_ingreso`, `usu_fecha_nacimiento`, `usu_sexo`, `usu_identificacion`, `usu_celular`, `usu_municipio`, `usu_cp`, `usu_colonia`, `usu_calle`, `usu_numext`, `usu_salario_neto`, `usu_password`, `usu_primeravez`, `usu_habilitado`, `usu_omitir_validaciones`, `usu_temporal`, `usu_fecha_ingreso_empresa`, `usu_numint`, `usu_fecha_baja`, `usu_ahorro_fijo`, `usu_ahorro_nofijo`, `usu_interes`, `usu_flagunico`, `usu_estatus`, `usu_rol` | `usu_empresa -> empresas`, `usuarios` 1-N `solicitudes` |
| `usuarios_resp` | `usu_id` | `usu_clave_empleado`, `usu_nombre`, `usu_paterno`, `usu_edo_civil`, `usu_correo`, `usu_estado`, `usu_rfc`, `usu_empresa`, `usu_puesto`, `usu_telefono`, `usu_departamento`, `usu_area_trabajo`, `usu_estacion`, `usu_fecha_ingreso`, `usu_fecha_nacimiento`, `usu_sexo`, `usu_identificacion`, `usu_celular`, `usu_municipio`, `usu_cp`, `usu_colonia`, `usu_calle`, `usu_numext`, `usu_salario_neto`, `usu_materno`, `usu_password`, `usu_primeravez`, `usu_temporal`, `usu_fecha_ingreso_empresa` |  |

## Vistas y navegacion

Las pantallas reales viven principalmente en `src/main/webapp/navegacion`. Las rutas mas importantes son:

- `usuariocomun/*`: flujos de usuario final.
- `admonsolicitudes/*`: validacion, autorizacion y fondeo.
- `cargapagosaportaciones/*`: carga y aplicacion de pagos/aportaciones.
- `creditos/*`: reportes y descuentos.
- `bancos/*`: conciliacion bancaria.
- `finiquito/*`: bajas y liquidaciones.
- `common/*`: fragmentos/componentes XHTML reutilizados.

La plantilla base esta en `WEB-INF/template.xhtml`; el menu en `WEB-INF/layoutmenu.xhtml`; la barra superior en `WEB-INF/topbar.xhtml`.

## Reportes y archivos

- Los reportes Jasper estan en `src/main/webapp/reportes`.
- `GeneradorReportesBo` y `DoctosJasperSolicitudBo` son piezas centrales para generar documentos de solicitudes, pagares, anexos, avisos y finiquitos.
- `Constantes.PATH_DOCTOS` y `PATH_DOCTOS_LOCAL` definen rutas de documentos; validar ambiente antes de cambiar flujos de upload/download.
- `commons-fileupload` y `PrimeFaces FileUpload Filter` habilitan cargas desde JSF.

## Como agregar funcionalidad sin leer todo el codigo

1. Ubicar primero el modulo por ruta XHTML en `src/main/webapp/navegacion`.
2. Identificar el ManagedBean usado por la pagina con expresiones `#{...}`.
3. Revisar ese bean y su BO inmediato.
4. Revisar solo los DAOs invocados por ese BO y los DTOs/POJOs afectados.
5. Si toca BD, revisar el mapping `.hbm.xml` de las tablas afectadas y buscar SQL nativo con `rg "nombre_tabla|nombre_columna" src/main/java`.
6. Si toca estatus, productos, roles o banderas, revisar `Constantes.java`.
7. Mantener los alias SQL compatibles con propiedades DTO cuando se use `Transformers.aliasToBean`.
8. Compilar con `mvn clean package`; si cambian dependencias usar `mvn clean install -U`.

## Riesgos tecnicos conocidos

- Credenciales y host de BD estan versionados en `hibernate.cfg.xml`.
- `hbm2ddl.auto=update` puede cambiar el esquema al iniciar.
- Contrasenas parecen compararse en texto directo en `LoginBean`.
- No hay pruebas automatizadas en `src/test/java`.
- Hay uso amplio de SQL nativo con concatenacion/interpolacion de strings.
- Log4J 1.x y PrimeFaces 6.0 son dependencias antiguas.
- `web.xml` declara dos veces el servlet `Faces Servlet`; revisar si se limpia configuracion.
- El codigo contiene paquete `org.primefaces.spark` vendorizado/plantilla junto a codigo de negocio; evitar modificarlo salvo necesidad de UI/theme.

## Comandos habituales

```bash
mvn clean package
mvn clean install -U
mvn tomcat7:deploy
mvn tomcat7:redeploy
mvn tomcat7:undeploy
```

Para despliegue local detallado, ver `Readme.md`.
