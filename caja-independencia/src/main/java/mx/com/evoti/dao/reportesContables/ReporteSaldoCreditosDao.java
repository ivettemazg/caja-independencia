/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.reportesContables;

import mx.com.evoti.dto.reportesContables.ReporteCreditoSaldoDto;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dao.ManagerDB;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.util.Util;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class ReporteSaldoCreditosDao extends ManagerDB implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteSaldoCreditosDao.class);
    private static final long serialVersionUID = 5102890145481955501L;

    /**
     * Obtiene los saldos de los creditos a una fecha dada, tomando en cuenta la
     * fecha de los pagos
     *
     * @param fecha
     * @param credito
     * @return
     * @throws IntegracionException
     */
    public ReporteCreditoSaldoDto getSaldosCreXFechaPago(Date fecha, Integer credito) throws IntegracionException {
             this.beginTransaction();


        SQLQuery query = session.createSQLQuery(String.format("select amo_credito as idCredito, "
                + "sum(amo_amortizacion) as capitalPagado,  usu_empresa as empresa, "
                + "creditos_final.cre_prestamo as montoPrestamo, cre_prestamo-sum(amo_amortizacion) as saldo, "
                + "cre_fecha_primer_pago as fechaPrimerPago, creditos_final.cre_pago_quincenal as pagoCatorcenal "
                + "from usuarios inner join creditos_final on usu_id = cre_usu_id "
                + "inner join amortizacion on amo_credito = cre_id "
                + "inner join pagos on amo_pago_id = pag_id "
                + "where pag_fecha <= '%1$s' "
                + "and pag_estatus in (2,4,5,6,8,9,10) "
                + "and amo_credito = %2$s "
                + "group by amo_credito, cre_prestamo", Util.generaFechaFormateada(fecha), credito));

        ReporteCreditoSaldoDto lstPagos = (ReporteCreditoSaldoDto)query.setResultTransformer(Transformers.aliasToBean(ReporteCreditoSaldoDto.class)).uniqueResult();

       this.endTransaction();
        return lstPagos;

    }

    
    /**
     * Obtiene los saldos de los creditos a una fecha dada, tomando en cuenta la
     * fecha de la amortizacion
     *
     * @param fecha
     * @param credito
     * @return
     * @throws IntegracionException
     */
    public ReporteCreditoSaldoDto getSaldosCreXFechaAmort(Date fecha, Integer credito) throws IntegracionException {
     this.beginTransaction();

        SQLQuery query = session.createSQLQuery(String.format("select amo_credito as idCredito, "
                + "sum(amo_amortizacion) as capitalPagado, usu_empresa as empresa, "
                + "creditos_final.cre_prestamo as montoPrestamo, cre_prestamo-sum(amo_amortizacion) as saldo, "
                + "cre_fecha_primer_pago as fechaPrimerPago, creditos_final.cre_pago_quincenal as pagoCatorcenal "
                + "from usuarios inner join creditos_final on usu_id = cre_usu_id "
                + "inner join amortizacion on amo_credito = cre_id "
                + "where amo_pago_id is null and amo_estatus = 'PAGADO' "
                + "and amo_fecha_pago <= '%1$s' "
                + "and amo_credito = %2$s "
                + "group by amo_credito, cre_prestamo", Util.generaFechaFormateada(fecha), credito));

       ReporteCreditoSaldoDto lstPagos = (ReporteCreditoSaldoDto)query.setResultTransformer(Transformers.aliasToBean(ReporteCreditoSaldoDto.class)).uniqueResult();

       this.endTransaction();
        return lstPagos;

    }

}
