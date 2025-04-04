package mx.com.evoti.dao.creditos;

import mx.com.evoti.dao.*;
import java.io.Serializable;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CreditoDto;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class ReporteDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 8958190915055950161L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteDao.class);

    /**
     * Regresa el reporte de creditos
     *
     * @return
     * @throws IntegracionException
     */
    public List<CreditoDto> getReporte() throws IntegracionException {
        String sql = String.format("select cre_prestamo as crePrestamo,cre_fecha_primer_pago as creFechaPrimerPago,"
                + "p.pro_siglas as creTipo,cre_clave as creClave,r.cre_est_nombre as creEstatusStr,"
                + "u.usu_clave_empleado as creClaveEmpleado, cre_estatus as creEstatus, "
                + "e.emp_abreviacion as creEmpresa,cre_solicitud as creSol, "
                + "concat_ws(' ',usu_paterno,usu_materno,usu_nombre) as creNombre, cre_id as creId "
                + "from creditos_final c left join usuarios u on c.cre_usu_id=u.usu_id "
                + "left join productos p on c.cre_producto=p.pro_id  "
                + "left join empresas e on u.usu_empresa=e.emp_id "
                + "left join credito_estatus r on c.cre_estatus=r.cre_est_id "
                + "order by cre_id");

        super.beginTransaction();
        SQLQuery sqlQuery = session.createSQLQuery(sql);

          List<CreditoDto> creds = sqlQuery.setResultTransformer(Transformers.aliasToBean(CreditoDto.class)).list();

        super.endTransaction();
        return creds;
    }
}
