package mx.com.evoti.dao.creditos;

import mx.com.evoti.dao.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.util.Util;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class ReporteMorososDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 8958190915055950161L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteMorososDao.class);

    /**
     * Regresa el reporte de creditos
     *
     * @param fechaCorte
     * @return
     * @throws IntegracionException
     */
    public List<CreditoDto> getReporteMorosos(Date fechaCorte) throws IntegracionException {
        String sql = String.format("select cre_id as creId,cre_fecha_deposito as creFechaDeposito,cre_prestamo as crePrestamo,cre_catorcenas as creCatorcenas, "
                + "cre_fecha_primer_pago as creFechaPrimerPago,u.usu_clave_empleado as creClaveEmpleado, "
                + "cre_pago_quincenal as crePagoQuincenal,cre_clave as creClave,cre_est_nombre as creEstatusStr, "
                + "concat_ws(' ',u.usu_paterno,u.usu_materno,u.usu_nombre) as creNombre, cre_usu_id as creUsuId, "
                + "p.pro_siglas as creTipo,e.emp_abreviacion as creEmpresa,count(*) as creCatorcenasPendts, "
                + "sum(a.amo_monto_pago) as saldoTotalPendt, sum(a.amo_amortizacion) as capitalPendiente "
                + "from creditos_final c left join usuarios u on c.cre_usu_id=u.usu_id  "
                + "left join productos p on c.cre_producto=p.pro_id "
                + "left join empresas e on u.usu_empresa=e.emp_id "
                + "left join amortizacion a on c.cre_id=a.amo_credito "
                + "left join credito_estatus ce on c.cre_estatus = ce.cre_est_id "
                + "where ce.cre_est_id = 1 "
                + "and c.cre_producto in (4,5,6,7,11) "
                + "and a.amo_fecha_pago<='%1$s' "
                + "and a.amo_estatus_int in (1,3) "
                + "and a.amo_numero_pago is not null "
                + "group by cre_id,cre_fecha_deposito,cre_prestamo,cre_catorcenas,cre_fecha_primer_pago, "
                + "u.usu_clave_empleado,cre_producto,cre_pago_quincenal,cre_clave,cre_estatus, "
                + "concat_ws(' ',u.usu_paterno,u.usu_materno,u.usu_nombre),u.usu_correo,u.usu_empresa,u.usu_telefono, "
                + "u.usu_celular,p.pro_siglas,e.emp_abreviacion order by cre_fecha_primer_pago",Util.generaFechaFormateada(fechaCorte));

        super.beginTransaction();
        SQLQuery sqlQuery = session.createSQLQuery(sql);

        List<CreditoDto> creds = sqlQuery.setResultTransformer(Transformers.aliasToBean(CreditoDto.class)).list();

        super.endTransaction();
        return creds;
    }
}
