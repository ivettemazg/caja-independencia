# Inventario de escrituras a la base (sistema actual)

Levantado el 2026-10-31 recorriendo el código (`src/main/java` y `src/main/webapp`). Es la base para la
bitácora de la Fase 0, para la revisión de errores y para el sistema nuevo.

Rutas: `J/` = `src/main/java/mx/com/evoti/`, `W/` = `src/main/webapp/` (las vistas sin prefijo están en
`W/navegacion/`).

## 1. Por dónde pasan las escrituras

`J/dao/ManagerDB.java`. Cada helper abre su propia sesión y su propia transacción, así que **ninguna operación
de negocio es atómica**.

| Helper | Línea | Llamadas |
|---|---|---|
| `executeUpdate(String hql)` | 115 | 34 |
| `executeUpdateSql(String sql)` | 141 | 43 |
| `updatePojo(Object)` | 167 | 13 |
| `savePojo(Object)` | 202 | 10 |
| `savePojos(List<Object>)` (una transacción para la lista) | 238 | 5 |
| `mergePojo(Object)` | 265 | 3 |

- `beginTransaction()` (:90) abre sesión y transacción; `endTransaction()` (:103) solo cierra, sin commit.
- También se llaman desde BOs: `FiniquitosBo` (:97, 116, 183, 189, 206, 224, 284, 437), `ProcesoPagoCapitalBo` (:101, 187),
  `PagoCapitalFiniquitoBoImpl` (:76, 109), `BancosBo` (:85).

**17 escrituras no pasan por los helpers** (sesión directa):
`ConfiguracionDao.getOrCreateConfig` :27 (sin commit) · `PasswordRecoveryDao.actualizaPasswordYUsaToken` :112-113 ·
`FiniquitoDao.actualizarBajaEmpleadoConFiniquito` :70 · `CargaPagosAportacionesDao.insertaArchivo` :82,
`insertaListaPagos` :298 y `insertaListaMovimientos` :336 (un INSERT por fila, un commit) · `CreditosDao.guardaCredito` :514 ·
`FondeoDao.guardaCredito` :79 · `SolicitudCreditoDao.guardaSolicitud` :151 · `BeneficiariosDao.saveOrUpdate` :63 ·
`BitacoraDao.saveBitacora` :39 · `CambiosEmpresaDao.insertCambioEmpresa` :132 · `AltasUsuarioDao.insertAltaCambioHist` :113,
`guardarUsuario` :130, `updateUsuarioClaveEmpresa` :148 (sin llamadores) · `SolicitudUpdtDaoImpl` :32 (sin commit, sin llamadores).

**El único paso común a todas las escrituras** es `HibernateUtil.getSessionFactory().openSession()`. No hay JDBC directo.
No hay Interceptor ni listeners de Hibernate.

## 2. Acciones que escriben, por módulo

`TX n` = escribe `registro_transaccion` tipo n · `BIT n` = escribe `bitacora` tipo n · `—` = sin rastro.

### 2.1 Acceso y contraseñas
| Vista | Acción | Escribe | Entrada | Rastro |
|---|---|---|---|---|
| `W/login.xhtml:48` | loginBean.validaLogin | nada (sesión) | clave, contraseña | — |
| `W/recuperar-password.xhtml:87` | recuperaPasswordBean.solicitarRecuperacion | password_reset_tokens (expira anteriores + nuevo) | clave, empresa, correo; IP y UA | — |
| `W/restablecer-password.xhtml:78` | recuperaPasswordBean.restablecerPassword | usuarios.usu_password (texto plano) + token usado | token, contraseña nueva ×2 | — |
| `administracion/panel.xhtml:66` | adminPanelBean.resetearPassword | usuarios (fila completa); si no se captura, genera "Tmp-xxxxxx" y la muestra | clave, empresa, contraseña | — |

### 2.2 Socio (`usuariocomun/`)
| Vista | Acción | Escribe | Entrada | Rastro |
|---|---|---|---|---|
| `solicitud-credito.xhtml:199` | solCreditoBean.creaSolicitud | solicitudes (1) + imagenes + solicitud_avales | tipo, monto, deducciones, catorcenas, catorcena FA, montos FA/aguinaldo | — |
| `datos-bancarios.xhtml:33` | dbancariosBean.updtDatosBancarios | solicitudes (banco, cuenta, CLABE, titular); si está completa: estatus 2 + avales 3 + correo | banco, cuenta, CLABE, titular | — |
| `avales-sol.xhtml:21` (rowEdit) | avaSolBean.onEditAval | solicitud_avales (2); posible estatus 2 | clave del aval | — |
| `documentos-sol.xhtml:42` (fileUpload) | doctosSolBean.handleFileUpload | disco + imagenes (2); posible solicitudes 2 o 5 | PDF, tipo de documento | — |
| `mis-solicitudes.xhtml:175` (fileUpload) | misSolsBean.handleFileUpload | disco + solicitudes 5 + INSERT imagenes (tipo 8) por archivo | hasta 6 PDF | — |
| `perfil.xhtml:207` | perfilBean.actualizaPerfil | usuarios (fila completa); usu_primeravez=0 | ~27 campos personales | — |
| `beneficiarios.xhtml:23, 256` | onEditBen / guardaBeneficiario | beneficiarios | nombre, apellidos, parentesco, %, teléfonos, dirección | — |
| `beneficiarios.xhtml:170, 189` | borraBeneficiario | DELETE beneficiarios | beneficiario | — |

`perfil.xhtml` también se incluye en `administracion/panel.xhtml:59` (origen 4): el administrador edita el perfil completo de cualquier socio.

### 2.3 Validación de solicitudes (`admonsolicitudes/`)
| Vista | Acción | Escribe | Entrada | Rastro |
|---|---|---|---|---|
| `valsol-principal.xhtml:30,38,51,59` (**blur**) | valSolPpalBean.actualizarDatos | solicitudes (monto, pago, catorcenas) cada vez que el campo pierde el foco | tabulador, deducciones, monto, catorcenas | — |
| `valsol-detalle.xhtml:108` | aprobarSolicitud | solicitudes 3 + fecha + correo | — | — |
| `valsol-detalle.xhtml:136` | rechazarSolicitud | solicitudes 7 + fecha + bitacora + correo | motivo | BIT 3 |
| `common/avales-tabla.xhtml:51` | avalBean.aprobarAval | solicitud_avales | avales | — |
| `common/avales-tabla.xhtml:81` | avalBean.rechazaAval | solicitud_avales + solicitudes 9 + bitacora + correo | motivo | BIT 3 |
| `valsol-documentos.xhtml:39 / :77` | updtEstatusDoc(3 / 4) | imagenes; si rechaza: solicitudes 9 + bitacora + correo | documento, motivo | BIT 3 (rechazo) |

### 2.4 Fondeo, seguimiento y seguro de auto
| Vista | Acción | Escribe | Entrada | Rastro |
|---|---|---|---|---|
| `fondeo-pendientes.xhtml:128` | fondeoBean.fondeaSolicitud | INSERT creditos_final + amortizacion; solicitudes 4 (u 8); avales; correo | solicitudes, fecha del primer pago | — |
| `fondeo-pendientes.xhtml:153` | bitaBean.rechazarSolicitud | solicitudes 7 + bitacora + correo | observación | BIT 3 |
| `fondeo-deposito.xhtml:150` | fondeoBean.marcaDeposito | solicitudes 6; crédito con fecha de depósito; INSERT bancos (12) y estado_cuenta | solicitudes, fecha de depósito | — |
| `fondeo-deposito.xhtml:213`, `f-doc-fondeados.xhtml:146`, `f-doc-enviada.xhtml:180` | cancelarCredito* | crédito 3; pagos ligados a acumulado; DELETE amortización pendiente; solicitudes 6; bitacora | motivo, selección | BIT 4 / 3 |
| `f-doc-fondeados.xhtml:122` | updtEstatusSolicitudDocsFC | solicitudes 8 + formato + fecha | formato | — |
| `f-doc-enviada.xhtml:113 / :159` | updtEstatusDocSol(3 / 4) | imagenes + solicitudes 8 o 4 (+ correo) | motivo | — |
| `credito-sauto.xhtml:162` | credSegAuBean.creaSolicitud | INSERT solicitudes (producto 11) | clave, empresa, monto, catorcena, banco, CLABE, póliza, referencia, aseguradora | — |

### 2.5 Pagos, aportaciones, altas y cambios (`cargapagosaportaciones/`)
| Vista | Acción | Escribe | Entrada | Rastro |
|---|---|---|---|---|
| `carga-archivo.xhtml:337` (fileUpload) | cargaPagosApoBean.hfuExcel | disco + archivos_historial + pagos o movimientos (una fila por renglón) + usu_id + bancos (1 o 2); en aportaciones, ajuste de ahorros no fijos | tipo (1/2), archivo .xlsx | — |
| `aplica-pagos.xhtml:71` | asignaPagosBean.procesaPagos | pagos, amortizacion, creditos_final, archivos_historial (algoritmo de 5 fases); `updtCreditosPagados` actúa sobre **todos** los créditos | catorcena | — |
| `cambios-empresa.xhtml:71` | altasCambiosBean.procesaAltasCambios | usuarios, altas_cambios_hist (2), movimientos y pagos de la catorcena | catorcena | — |
| `cam-al-pendts.xhtml:152` | altasBean.realizarCambioEmpresa | lo mismo para un socio elegido | pendiente, socio | — |
| `cam-al-pendts.xhtml:175` | altasBean.guardarUsuario | INSERT usuarios + altas_cambios_hist (1) + usu_id en movimientos y pagos | nombre, pendiente | — |
| `reporteAportaciones.xhtml:326` | reactivarUsuarios | usuarios (quita baja) | catorcena, empresa, socios | — |

### 2.6 Reporte personal (`common/reporte-empleado.xhtml` y fragmentos)
| Vista | Acción | Escribe | Entrada | Rastro |
|---|---|---|---|---|
| `busqueda-empleado.xhtml:54 / :67` | habilitarDeshabilitar / omitirValidaciones | usuarios | socio | — |
| `ahorros-rp.xhtml:92` | guardaAportacionVoluntaria | movimientos + bancos + estado_cuenta | monto, fecha | TX 16 |
| `reporte-empleado.xhtml:137` | aplicaDevolucion | movimientos (negativo) + bancos + estado_cuenta | movimiento, monto, fecha | TX 15 |
| `reporte-empleado.xhtml:165` (rowEdit) | onEditAcumulado | pagos (11, negativo) + bancos + estado_cuenta | monto, fecha | TX 17 |
| `notas-repe.xhtml:32` | guardaRegBitacora | bitacora | título, nota | BIT 5 |
| `amortizacion-rep-e.xhtml:161` | recorreCatorcenas | DELETE + INSERT de toda la amortización; crédito | renglones, mandar a reporte, catorcena siguiente | — |
| `amortizacion-rep-e.xhtml:226` | aplicarPagoCapital | pagos (9), bancos (5), amortización recalculada, crédito | monto, catorcenas restantes, fecha, renglón | TX 18 |
| `amortizacion-rep-e.xhtml:349` | aplicarPagoCapitalAhorro | lo anterior + movimientos (ABONO CAPITAL) | devoluciones por renglón, fecha | TX 18 |
| `amortizacion-rep-e.xhtml:393` | realizarPago (extemporáneo) | pagos (10), bancos (4), amortización (6) | monto, fecha, renglón | TX 20 |
| `amortizacion-rep-e.xhtml:446` | pagarConAcumulado | pagos (8), amortización (5) | fecha, renglón | — |
| `amortizacion-rep-e.xhtml:553` | ajustaCredito (con ahorro) | movimientos (ABONO CREDITO), amortización (8) | devoluciones por renglón | — |
| `transf-avales.xhtml:119` | transfiereCreditos | créditos nuevos por aval (producto 6) + amortización; crédito padre 4 | por aval: monto, catorcena, catorcenas | — |
| `transf-avales.xhtml:133` | mandarAIncobrable | crédito 5 + fecha; amortización 12 | fecha | — |

### 2.7 Créditos
`creditos/reporte.xhtml:307` reporteBean.mandarAIncobrable → crédito 5 + amortización 12 (fecha) · —

### 2.8 Finiquitos y bajas (`finiquito/`)
| Vista | Acción | Escribe | Entrada | Rastro |
|---|---|---|---|---|
| `baja-empleado.xhtml:109` | baeBean.darDeBaja | baja_empleados; usuarios (fila completa, baja) | clave, empresa, socio, fecha de baja | — |
| `finiquito.xhtml:123` | finBean.aplicaDevolucion | movimientos (+ bancos y estado_cuenta) + baja_empleados | monto, fecha | — |
| `finiquito.xhtml:191` | finBean.ajustaCredito | pagos (12) + bancos (13) o movimientos; amortización; crédito; baja_empleados | monto, fecha, devoluciones por renglón | — |
| `finiquito.xhtml:314` | finBean.transfiereCreditos | como transf-avales + baja_empleados | por aval | — |
| `finiquito.xhtml:328` | finBean.mandarAIncobrable | crédito 5 + amortización 12 | fecha | — |
| `common/ahorros-tab-ajuste.xhtml:45` | finBean.devolverTotalAhorro | movimientos en lote + bancos y estado_cuenta + baja_empleados | montos por renglón | — |

### 2.9 Bancos y tesorería
| Vista | Acción | Escribe | Entrada | Rastro |
|---|---|---|---|---|
| `bancos/ajuste-banco.xhtml:204` (rowEdit) | onRowEditEC | estado_cuenta | concepto, descripción, fecha, monto | — |
| `ajuste-banco.xhtml:321` | eliminaConcepto | DELETE estado_cuenta | concepto | — |
| `ajuste-banco.xhtml:344` | ajustaConceptos | bancos y estado_cuenta (relación) | selección de ambos lados, rango de fechas | — |
| `ajuste-banco.xhtml:350` | quitarAjuste | bancos y estado_cuenta | registro | — |
| `ajuste-banco.xhtml:401` | agregaConceptoEdoCta | INSERT estado_cuenta | fecha, descripción, concepto, monto | — |
| `tesoreria/desctos-x-cobrar.xhtml:115` | updtRegBanco | bancos + INSERT estado_cuenta | depósito y fecha editados | — |

### 2.10 Rendimiento
`rendimiento/proceso.xhtml:205` procesoBean.ejecutaCalculo → INSERT movimientos (RENDIMIENTO), **una transacción por fila**; UPDATE rendimiento ·
entrada: mes, intereses, comisiones · —. **No revisa si el mes ya se aplicó.**

### 2.11 Administración (`administracion/panel.xhtml`)
`:29` guardarBanderas (configuracion FA/AG) · `:54` toggleHabilitado (usuarios) · `:66` resetearPassword · `:59` perfil completo · todos —.

## 3. Escrituras sin acción directa
- No hay tareas programadas en Java. `ContextListener` construye Hibernate al arrancar, con `hbm2ddl.auto=update`.
- Al renderizar: `restablecer-password.xhtml` marca tokens vencidos; `panel.xhtml` y `solicitud-credito.xhtml` pueden insertar la fila de `configuracion` (sin commit).
- MySQL: evento `ev_eliminar_solicitudes_antiguas` diario a las 03:00 → `sp_eliminar_solicitudes_antiguas` borra las solicitudes en
  estatus 1 con más de 2 meses, con sus avales e imágenes.
- Utilerías `main()` que escriben si se corren a mano: `AlgoritmoAsignaPagosBo.main`, `CargaPagosMovimientosBo.main`.
- El log de Tomcat (`sindicato.log`) tiene todo el SQL, pero rota a 4 MB × 5, sin fecha en el patrón y sin usuario.

## 4. Archivos en disco
- Todo se guarda plano en `Constantes.PATH_DOCTOS = "c:/Documentos"`.
- **Excel de pagos y aportaciones** (`CargaArchivoPagMovsBean.hfuExcel` :100): nombre original en minúsculas; se escribe **antes** de validar;
  sobrescribe si ya existe. La única protección es el nombre en `archivos_historial`. Un intento fallido deja el archivo en disco y el
  siguiente con el mismo nombre lo pisa. En la base solo queda el nombre (sin ruta, usuario ni hash).
- **Documentos de solicitud**: `{usuId}_{TIPO}_{ddMMyy}.pdf`; volver a subir sobrescribe; dos solicitudes del mismo socio el mismo día
  chocan; si falla la escritura en disco, la base queda como "subido" igual.
- **Documento firmado**: `{solId}_DOC_SOLICITUD.pdf`; con varios archivos, cada uno sobrescribe al anterior e inserta otra fila.
- Al visualizar se copian a la raíz de la webapp y se borran al cerrar el diálogo (si no se cierra, quedan).

## 5. Punto central y control de acceso
- No hay PhaseListener, filtro de autenticación ni `security-constraint`. `faces-config.xml` solo registra los handlers de diálogos
  de PrimeFaces.
- `BaseBean.validateUser()` no bloquea nada (solo muestra un diálogo) y ningún bean administrativo que escribe lo llama.
- Los roles solo ocultan opciones del menú.
- **Verificado el 2026-10-31 en la copia aislada:** sin iniciar sesión se abren las pantallas de administración y se ejecutan sus acciones.
- **Corregido el 2026-10-31** (commit 6e7a8f4, desplegado): `ControlAccesoFiltro` es el primer filtro del Faces Servlet. Sin sesión
  solo permite login, recuperación y restablecimiento de contraseña, páginas de error y recursos de JSF; el socio solo abre
  `dashboard.xhtml` y `navegacion/usuariocomun/`; Administrador y Auxiliar abren todo. Orden actual de filtros:
  Control de Acceso → FileUpload → CharacterEncoding.
- **Corregido el 2026-10-31** (commit 6ee2e84): guardar un perfil desde el panel ya no reemplaza el usuario de la sesión.
