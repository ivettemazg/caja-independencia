package mx.com.evoti.dao.solicitudes;

import mx.com.evoti.dao.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.HistorialSolicitudesDto;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class HistorialDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 8958190915055950161L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HistorialDao.class);

    /**
     * Regresa el historial de solicitudes
     *
     * @return
     * @throws IntegracionException
     */
    public List getHistorial() throws IntegracionException {
        String sql = String.format("select sol_id as folio,sol_monto_solicitado as montoSolicitado,"
                + "sol_catorcenas as catorcenas,o.sol_est_nmbr_est as estatus, sol_fecha_creacion as fechaCreacion, "
                + "u.usu_clave_empleado as usuClaveEmpleado, concat_ws(' ',usu_paterno,usu_materno,usu_nombre) as nombre,"
                + "e.emp_abreviacion as empresa,p.pro_descripcion as producto "
                + "from solicitudes s left join usuarios u on s.sol_usu_id=u.usu_id "
                + "left join empresas e on u.usu_empresa=e.emp_id "
                + "left join productos p on s.sol_producto=p.pro_id "
                + "left join solicitud_estatus o on s.sol_estatus=o.sol_est_id order by sol_fecha_creacion desc");

        super.beginTransaction();
        SQLQuery sqlQuery = session.createSQLQuery(sql);

        List<HistorialSolicitudesDto> results = sqlQuery.setResultTransformer(Transformers.aliasToBean(HistorialSolicitudesDto.class)).list();
        super.endTransaction();
        return results;
    }

    public HistorialSolicitudesDto getDetalleSolicitud(BigInteger folio) throws IntegracionException {
        super.beginTransaction();

        SQLQuery sqlQuery = session.createSQLQuery(
                String.format("select sol_sueldo_neto as sueldoNeto,sol_deducciones as deducciones,"
                        + "sol_monto_solicitado as montoSolicitado,sol_catorcenas as catorcenas,"
                        + "sol_pago_credito as pagoCredito,sol_banco as banco,sol_numero_cuenta as numeroCuenta,"
                        + "sol_clabe_interbancaria as clabe,e.sol_est_nmbr_est as estatus,"
                        + "sol_nombre_tarjetahabiente as tarjetaHabiente,p.pro_descripcion as producto,"
                        + "sol_fecha_autorizacion as fechaAutorizacion,sol_fecha_creacion as fechaCreacion,"
                        + "sol_fecha_cancelacion as fechaCancelacion, sol_fecha_fondeo as fechaFondeo,"
                        + "sol_fecha_enviodocumentos as fechaEnvioDoc,sol_fecha_deposito as fechaDeposito,"
                        + "sol_motivo_rechazo as motivoRechazo, sol_referencia as referencia,"
                        + "sol_aseguradora as aseguradora, sol_no_poliza as poliza, sol_estatus as estatusSol "
                        + "from solicitudes s left join solicitud_estatus e on s.sol_estatus=e.sol_est_id "
                        + "left join productos p on s.sol_producto=p.pro_id "
                        + "where sol_id=%1$s", folio));

        super.beginTransaction();

        HistorialSolicitudesDto detalleSolicitud = (HistorialSolicitudesDto) sqlQuery.setResultTransformer(Transformers.aliasToBean(HistorialSolicitudesDto.class)).uniqueResult();
        super.endTransaction();
        return detalleSolicitud;

    }
    
        public List<HistorialSolicitudesDto> getAvalesSolicitud(BigInteger folio) throws IntegracionException {
        super.beginTransaction();

        SQLQuery sqlQuery = session.createSQLQuery(
                String.format("select u.usu_clave_empleado as usuClaveEmpleado, "
                        + "concat_ws(' ',usu_paterno,usu_materno,usu_nombre) as nombre,u.usu_correo as correo, "
                        + "u.usu_telefono as telefono,u.usu_celular as celular,usu_fecha_baja as fechaBaja "
                        + "from solicitudes s left join solicitud_avales a on s.sol_id=a.sol_ava_solicitud "
                        + "left join usuarios u on sol_ava_id_empleado=u.usu_id where sol_id=%1$s", folio));

        List<HistorialSolicitudesDto> avalesSolicitud = sqlQuery.setResultTransformer(Transformers.aliasToBean(HistorialSolicitudesDto.class)).list();
        super.endTransaction();
        return avalesSolicitud;

    }

}
