-- Planes de ejecución de las consultas cuyo resultado depende del orden en que MySQL entrega los renglones.
-- Mismo SQL que la aplicación (los UPDATE de varias tablas se escriben como SELECT con los mismos JOIN y WHERE,
-- porque claude_lectura no puede pedir el plan de un UPDATE). Valores fijos para comparar antes y después.
-- Si el plan (orden de tablas, índices y tipo de unión) es el mismo, el orden de los renglones es el mismo.

-- P01 Asignación de pagos: pago exacto (AlgoritmoAsignaPagosDao.updtAmortizacionExacto)
explain format=tree select pag_id, amo_id from sindicato.pagos inner join sindicato.usuarios on usu_id = pag_usu_id inner join sindicato.creditos_final on cre_usu_id = usu_id inner join sindicato.amortizacion on cre_id = amo_credito where (amo_monto_pago-pag_deposito <= 1 and amo_monto_pago-pag_deposito >= -1) and amo_fecha_pago = pagos.pag_fecha and amo_pago_id is null and pag_estatus = 1 and amo_monto_pago > 0 and amo_usu_id = pag_usu_id and pag_arh_id = 3060 and cre_producto in (6,7) and cre_estatus = 1;

-- P02 Asignación de pagos: pago mayor (updtPagoMayor)
explain format=tree select pag_id, amo_id from sindicato.pagos inner join sindicato.usuarios on usu_id = pag_usu_id inner join sindicato.creditos_final on cre_usu_id = usu_id inner join sindicato.amortizacion on cre_id = amo_credito where amo_monto_pago < pag_deposito and amo_fecha_pago = pagos.pag_fecha and amo_pago_id is null and amo_monto_pago > 0 and amortizacion.amo_estatus_int = 1 and pag_estatus = 1 and amo_usu_id = pag_usu_id and pag_arh_id = 3060 and cre_producto in (6,7) and cre_estatus = 1;

-- P03 Asignación de pagos: pago menor (updtPagoMenor)
explain format=tree select pag_id, amo_id from sindicato.pagos inner join sindicato.usuarios on usu_id = pag_usu_id inner join sindicato.creditos_final on cre_usu_id = usu_id inner join sindicato.amortizacion on cre_id = amo_credito where amo_monto_pago > pag_deposito and amo_fecha_pago = pagos.pag_fecha and amo_pago_id is null and amo_monto_pago > 0 and amortizacion.amo_estatus_int = 1 and pag_estatus = 1 and amo_usu_id = pag_usu_id and pag_arh_id = 3060 and cre_producto in (6,7) and cre_estatus = 1;

-- P04 Asignación de pagos: estatus y crédito del pago (updtPagosEstatus)
explain format=tree select pag_id, amo_credito, amo_monto_pago from sindicato.pagos inner join sindicato.amortizacion on pag_id = amo_pago_id where pag_arh_id = 3060 and pag_estatus = 1;

-- P05 Asignación de pagos: cuál amortización conserva un pago repetido (getAmortizacionesCPagRep)
explain format=tree select amo_id from sindicato.amortizacion where amo_pago_id = 440612;

-- P06 Asignación de pagos: pagos mayores de socios con varios créditos (getUsuEst5y6)
explain format=tree select amo_usu_id, amo_monto_pago, pag_usu_id, pag_deposito, pag_id, pag_acumulado, pag_fecha from sindicato.amortizacion inner join sindicato.pagos on pag_id = amo_pago_id, (select usu_id, count(cre_id) as totalcreditos from sindicato.usuarios inner join sindicato.creditos_final on cre_usu_id = usu_id where cre_producto in (6,7) and cre_estatus in (1,2) group by usu_id) usucre where amo_usu_id = usucre.usu_id and usucre.totalcreditos > 1 and pag_acumulado >= 100 and pag_estatus = 4 and pag_arh_id = 3060;

-- P07 Asignación de pagos: orden en que se cubren amortizaciones con el sobrante (getAmortEstatus5y6)
explain format=tree select amo_id, amo_numero_pago, amo_capital, amo_monto_pago, amo_credito, amo_fecha_pago, amo_estatus, amo_pago_id, amo_usu_id from sindicato.amortizacion inner join sindicato.creditos_final on cre_id = amo_credito where amo_usu_id = 195 and ((amortizacion.amo_monto_pago <= 1000.00) or (amortizacion.amo_monto_pago-1000.00 <= 1 and amortizacion.amo_monto_pago-1000.00 >= -1)) and amortizacion.amo_fecha_pago = '2026-11-26' and amo_pago_id is null and amo_monto_pago > 0 and cre_producto in (6,7);

-- P08 Carga de pagos: a qué cuenta se asigna cada pago (CargaPagosAportacionesDao.updateUsuIdPago)
explain format=tree select pag_id, usu_id from sindicato.pagos left join sindicato.usuarios on (pagos.pag_clave_empleado = usuarios.usu_clave_empleado and pagos.pag_empresa = usuarios.usu_empresa) inner join sindicato.archivos_historial on pagos.pag_arh_id = archivos_historial.arh_id where pag_empresa = archivos_historial.arh_empresa and pagos.pag_usu_id is null and pag_arh_id = 3060;

-- P09 Carga de aportaciones: a qué cuenta se asigna cada movimiento (CargaPagosAportacionesDao.updateUsuIdMov)
explain format=tree select mov_id, usu_id from sindicato.movimientos left join sindicato.usuarios on (mov_clave_empleado = usu_clave_empleado and mov_empresa = usu_empresa) inner join sindicato.archivos_historial on mov_arh_id = arh_id where mov_empresa = arh_empresa and mov_usu_id is null and mov_arh_id = 3059;

-- P10 Cambios de empresa: asignación de pagos (CambiosEmpresaDao.updateUsuIdPago)
explain format=tree select pag_id, usu_id from sindicato.pagos left join sindicato.usuarios on pagos.pag_clave_empleado = usuarios.usu_clave_empleado and pagos.pag_empresa = usu_empresa where pagos.pag_usu_id is null and pag_fecha = '2026-11-12';

-- P11 Cambios de empresa: asignación de movimientos (CambiosEmpresaDao.updateUsuIdMov)
explain format=tree select mov_id, usu_id from sindicato.movimientos inner join sindicato.usuarios on mov_clave_empleado = usu_clave_empleado and mov_empresa = usu_empresa where mov_usu_id is null and mov_fecha = '2026-11-12';

-- P12 Altas: asignación de pagos (AltasUsuarioDao.updateUsuIdPago)
explain format=tree select pag_id, usu_id from sindicato.pagos inner join sindicato.usuarios on pagos.pag_clave_empleado = usuarios.usu_clave_empleado and pag_empresa = usu_empresa where pagos.pag_usu_id is null;

-- P13 Altas: asignación de movimientos (AltasUsuarioDao.updateUsuIdMov)
explain format=tree select mov_id, usu_id from sindicato.movimientos left join sindicato.usuarios on mov_clave_empleado = usu_clave_empleado and mov_empresa = usu_empresa where mov_usu_id is null;

-- P14 Cambios de empresa: último movimiento del socio (CambiosEmpresaDao.getUltimosMovs)
explain format=tree select mov.mov_nombre_empleado, mov.mov_clave_empleado, mov.mov_fecha, mov.mov_empresa, mov.mov_arh_id from sindicato.movimientos mov where mov.mov_usu_id = 5001 and mov.mov_arh_id is not null order by mov_fecha desc limit 1;

-- P15 Cambios de empresa: búsqueda por apellido (CambiosEmpresaDao.obtieneUsuarioXNombre, HQL)
explain format=tree select usu.*, emp.* from sindicato.usuarios usu inner join sindicato.empresas emp on usu.usu_empresa = emp.emp_id where usu.usu_paterno like 'GAR%';

-- P16 Finiquito: empresa y clave de las devoluciones (MovimientosDao.getAFANFyR, ANY_VALUE)
explain format=tree SELECT mov_producto, mov_ar, SUM(mov_deposito), ANY_VALUE(mov_usu_id), ANY_VALUE(mov_clave_empleado), ANY_VALUE(mov_empresa) FROM sindicato.movimientos WHERE mov_usu_id = 5001 AND mov_producto IN (1, 2) GROUP BY mov_producto, mov_ar;

-- P17 Pago a capital / finiquito: renglón donde se aplica (AmortizacionDao.getAmortizacionPCap, HQL)
explain format=tree select amo.* from sindicato.amortizacion amo where amo.amo_estatus_int = 1 and amo.amo_credito = 7456 and amo.amo_fecha_pago >= '2026-11-26' order by amo.amo_fecha_pago asc;

-- P18 Login: cuentas con la misma clave (LoginDao.login)
explain format=tree select usu_id, usu_clave_empleado, usu_empresa, usu_fecha_baja, rol_id from sindicato.usuarios inner join sindicato.roles on rol_id = usu_rol where usu_clave_empleado = '1';
