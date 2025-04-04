/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import mx.com.evoti.dto.ResultadoAmoDto;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.ResultadoPagosDto;
import mx.com.evoti.util.Util;
import org.hibernate.Query;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Venette
 */
public class ReporteResultadosDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 8994868066726041742L;

    /**
     * Obtiene el detalle de la asignación de los pagos del archivo a las
     * amortizaciones
     *
     * @param fecha
     * @return
     * @throws IntegracionException
     */
    public List<ResultadoPagosDto> obtieneSituacionPagosXCatorcena(Date fecha) throws IntegracionException {

        super.beginTransaction();

        Query sql = session.createSQLQuery(
                String.format("select pag_clave_empleado as pagClaveEmpleado, "
                        + "pag_credito as pagCredito, pag_deposito as pagDeposito, pag_empresa as pagEmpresa, "
                        + "pag_estatus as pagEstatus, pag_fecha as pagFecha, pag_id as pagId, pag_usu_id as pagUsuId,"
                        + "pag_acumulado as pagAcumulado, "
                        + "amo_est_nombre as amoEstNombre, "
                        + "emp_abreviacion as empAbreviacion, pag_est_nombre as pagEstNombre, "
                        + "pag_est_color as pagEstColor, "
                        + "pag_usu_nombre as pagUsuNombre, cre_clave as creClave "
                        + "from pagos inner join empresas on emp_id = pag_empresa "
                        + "left join amortizacion on pagos.pag_id = amortizacion.amo_pago_id "
                        + "left join creditos_final on amortizacion.amo_credito = creditos_final.cre_id "
                        + "left join pagos_estatus on pag_est_id = pag_estatus "
                        + "left join amortizacion_estatus on amo_est_id = amo_estatus_int "
                        + "where pag_fecha = '%1$s' "
                        + "and pag_estatus not in (8,9,10,11,12)  ", Util.generaFechaFormateada(fecha)));

         List<ResultadoPagosDto> creditos = sql.setResultTransformer(Transformers.aliasToBean(ResultadoPagosDto.class)).list();

        super.endTransaction();

        return creditos;

    }

    /**
     * Regresa el detalle de la asignacion de la amortizacion de los creditos
     * @param fecha
     * @return 
     * @throws IntegracionException
     */
    public List<ResultadoAmoDto> obtieneResultadoAmortizacion(Date fecha) throws IntegracionException {
        super.beginTransaction();

        Query sql = session.createSQLQuery(
                String.format("select amo_id as amoId, amo_numero_pago as amoNumeroPago,\n"
                        + "amo_capital as amoCapital,amo_amortizacion as amoAmortizacion,\n"
                        + "amo_interes as amoInteres,amo_monto_pago as amoMontoPago,\n"
                        + "amo_fecha_pago as amoFechaPago,\n"
                        + "amo_est_nombre as amoEstNombre,amo_credito as amoCredito, \n"
                        + "cre_usu_id as creUsuId, usu_clave_empleado as usuClaveEmpleado, \n"
                        + "usu_empresa as usuEmpresa, pro_siglas as creTipo, \n"
                        + "pag_deposito as pagDeposito, IFNULL(pag_deposito-amo_monto_pago,0) as diferencia, cre_clave as creClave, \n"
                        + "emp_abreviacion as empAbreviacion \n"
                        + "from amortizacion inner join creditos_final on cre_id = amo_credito\n"
                        + "left join usuarios on usu_id = cre_usu_id\n"
                        + "inner join pagos on amo_pago_id = pag_id \n"
                        + "inner join empresas on usu_empresa = emp_id\n"
                        + "left join amortizacion_estatus on amortizacion.amo_estatus_int = amortizacion_estatus.amo_est_id\n"
                        + "left join productos on pro_id = cre_producto \n"
                        + "where amo_numero_pago is not null \n"
                        + "and amo_fecha_pago = '%1$s' \n"
                        + "and (cre_producto = 6 or cre_producto = 7) \n"
                        + "and (amo_monto_pago is not null or amo_monto_pago > 0) ", Util.generaFechaFormateada(fecha)));

        List<ResultadoAmoDto> amortizacion = sql.setResultTransformer(Transformers.aliasToBean(ResultadoAmoDto.class)).list();

        super.endTransaction();

        return amortizacion;
    }

    
    /**
     * Regresa los creditos no pagados activos en la catorcena seleccionada
     * @param fecha
     * @return
     * @throws IntegracionException 
     */
    public List<ResultadoAmoDto> obtieneCreditosNoPagados(Date fecha) throws IntegracionException {
        super.beginTransaction();
        
        Query sql = session.createSQLQuery(
                String.format("select amo_id as amoId, amo_numero_pago as amoNumeroPago,amo_capital as amoCapital, "
                        + "amo_amortizacion as amoAmortizacion,amo_interes as amoInteres,amo_monto_pago as amoMontoPago, "
                        + "amo_fecha_pago as amoFechaPago, amo_est_nombre as amoEstNombre, "
                        + "amo_credito as amoCredito, cre_usu_id as creUsuId, usu_clave_empleado as usuClaveEmpleado, "
                        + "usu_empresa as usuEmpresa, cre_tipo as creTipo, "
                        + "cre_clave as creClave, emp_abreviacion as empAbreviacion, "
                        + "CONCAT_WS(' ',usu_nombre,usu_paterno,usu_materno) as creNombre "
                        + "from amortizacion inner join creditos_final on cre_id = amo_credito "
                        + "left join usuarios on usu_id = cre_usu_id "
                        + "inner join empresas on usu_empresa = emp_id "
                        + "left join amortizacion_estatus on amortizacion.amo_estatus_int = amortizacion_estatus.amo_est_id "
                        + "left join productos on pro_id = cre_producto  "
                        + "where amo_numero_pago is not null  "
                        + "and amo_fecha_pago = '%1$s' "
                        + "and amo_estatus_int = 1 "
                        + "and (cre_producto = 6 or cre_producto = 7)  "
                        + "and (amo_monto_pago is not null or amo_monto_pago > 0) "
                        + "and cre_estatus = 1 ", Util.generaFechaFormateada(fecha)));

        List<ResultadoAmoDto> amortizacion = sql.setResultTransformer(Transformers.aliasToBean(ResultadoAmoDto.class)).list();

        super.endTransaction();

        return amortizacion;
    }

}
