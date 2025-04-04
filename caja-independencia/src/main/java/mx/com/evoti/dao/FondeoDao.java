/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.SolicitudDto;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.util.Util;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class FondeoDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 6416130381952570889L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FondeoDao.class);

    /**
     * Metodo que obtiene todas las solicitudes para la pantalla de fondeos
     *
     * @param idSolicitud
     * @return
     * @throws IntegracionException
     */
    public List<SolicitudDto> getSolsPendtsFondeoScreen(Integer idSolicitud) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(String.format("select sol.sol_Id as solId,"
                + "pro.pro_Descripcion as proDescripcion, pro.pro_siglas as proSiglas, "
                + "sol.sol_Fecha_Creacion as solFechaCreacion, usu.usu_Clave_Empleado as solClaveEmpleado, "
                + "concat(usu.usu_Nombre,' ',usu.usu_Paterno,' ',usu.usu_Materno) as usuNombreCompleto,"
                + "usu.usu_Nombre as usuNombre, usu.usu_Paterno as usuPaterno, usu.usu_materno as usuMaterno, "
                + "sol.sol_usu_id as usuId, sol.sol_fecha_autorizacion as solFechaAutorizacion,"
                + "emp.emp_Abreviacion as empAbreviacion, sol.sol_catorcenas as solCatorcenas,"
                + "sol.sol_pago_credito as solPagoCredito, sol.sol_monto_solicitado as solMontoSolicitado,"
                + "sol.sol_facatorcena as solFacatorcena, sol.sol_fecha_ult_catorcena as solFechaUltCatorcena, "
                + "usu.usu_correo as usuEmail, emp.emp_id as empId, "
                + "sol.sol_fecha_fondeo as solFechaFondeo, sol.sol_banco as solBanco, sol.sol_numero_cuenta as solNumeroCuenta,"
                + "sol.sol_clabe_interbancaria as solClabeInterbancaria, sol.sol_nombre_tarjetahabiente as solNombreTarjetahabiente,"
                + "sol.sol_fecha_enviodocumentos as solFechaEnviodocumentos, sol.sol_producto as proId "
                + "from solicitudes as sol inner join solicitud_estatus as est on sol_estatus = est.sol_est_id "
                + "inner join productos as pro on pro.pro_id = sol.sol_producto "
                + "inner join usuarios as usu on usu.usu_id = sol.sol_usu_id "
                + "inner join empresas as emp on emp.emp_id = usu.usu_empresa "
                + "where est.sol_est_id = %1$s", idSolicitud));

        List<SolicitudDto> solicitudes = query.setResultTransformer(Transformers.aliasToBean(SolicitudDto.class)).list();

        super.endTransaction();

        return solicitudes;
    }

    /**
     * Guarda el credito
     *
     * @param credito
     * @return
     * @throws IntegracionException
     */
    public CreditosFinal guardaCredito(CreditosFinal credito) throws IntegracionException {
        super.beginTransaction();

        try {
            super.session.persist(credito);
            super.session.getTransaction().commit();
            LOGGER.debug("El credito se guardó correctamente");
        } catch (HibernateException ex) {
            super.session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage() + " No se pudo guardar el credito", ex);

        } finally {
            super.endTransaction();
        }
        return credito;
    }

    /**
     * Actualiza el estatus de la solicitud a fondeada 4, envio documentos 5 y
     * deposito (6 cerrada) Siendo la última cuando ha finalizado el ciclo de la
     * solicitud
     *
     * @param idSolicitud
     * @param estatus
     * @param fechaFondOEnvOCan
     * @param fechaDep
     * @throws IntegracionException
     */
    public void updtEstatusSolicitud4567(Long idSolicitud, int estatus, Date fechaFondOEnvOCan) throws IntegracionException {
        String hql2 = "";
        switch (estatus) {
            case 4:
                hql2 = String.format(",sol.solFechaFondeo = '%1$s' ", Util.generaFechaFormateada(fechaFondOEnvOCan));
                break;
            case 5:
                hql2 = String.format(",sol.solFechaEnviodocumentos = '%1$s' ", Util.generaFechaFormateada(fechaFondOEnvOCan));
                break;
            case 7:
                hql2 = String.format(",sol.solFechaCancelacion = '%1$s' ", Util.generaFechaFormateada(new Date()));
                break;

        }

        String hql = String.format("update Solicitudes sol "
                + "set sol.solicitudEstatus.solEstId = %2$s "
                + "%3$s"
                + "where sol.solId = %1$s ", idSolicitud, estatus, hql2);

        super.executeUpdate(hql);

    }

    /**
     * Actualiza la fecha de deposito del credito
     *
     * @param idSolicitud
     * @param fechaDep
     * @throws IntegracionException
     */
    public void updtFechaDepositoCredito(Long idSolicitud, Date fechaDep) throws IntegracionException {

        String hql = String.format("update CreditosFinal cre "
                + "set cre.creFechaDeposito = '%1$s' "
                + "where creSolicitud = %2$s ", Util.generaFechaFormateada(fechaDep), idSolicitud);

        super.executeUpdate(hql);

    }

    /**
     * Obtiene la imagen de la documentacion firmada por el usuario para
     * mostrarse en fondeos
     *
     * @param idSolicitud
     * @param tipoDocto
     * @return
     * @throws IntegracionException
     */
    public Imagenes getDoctoFirmado(Long idSolicitud, int tipoDocto) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    String.format(
                            "from Imagenes where solicitudes.solId = %1$s "
                            + "and imaTipoimagen = %2$s ",
                            idSolicitud, tipoDocto));

            Imagenes img = (Imagenes) query.setMaxResults(1).uniqueResult();

            return img;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Actualiza el id del credito en la tabla de Solicitud_avales
     *
     * @param idSolicitud
     * @param creId
     * @throws IntegracionException
     */
    public void updtAvalSolicitudCreId(Long idSolicitud, int creId) throws IntegracionException {

        String sql = String.format("update solicitud_avales "
                + "set sol_ava_credito = %1$s "
                + "where sol_ava_solicitud = %2$s  ", creId, idSolicitud);

        super.executeUpdateSql(sql);

    }

}
