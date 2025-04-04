/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.SolicitudAvales;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Venette
 */
public class AvalesSolicitudDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 6588006705724572627L;

    public UsuarioDto getPerfilAvales(Integer cveEmpleado, Integer idEmpresa) throws IntegracionException {
        UsuarioDto usuDto;

        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select concat(u.usu_nombre,' ',u.usu_paterno,' ',u.usu_materno) as nombreCompleto, "
                    + "usu_id as id, usu_nombre as nombre,usu_paterno as paterno,usu_materno as materno,"
                    + "usu_empresa as idEmpresa, "
                    + "usu_clave_empleado as cveEmpleado "
                    + "from usuarios u "
                    + "left join empresas e on u.usu_empresa=e.emp_id "
                    + "where usu_clave_empleado = %1$s "
                    + "and usu_fecha_baja is null "
                    + "and e.emp_id = %2$s ", cveEmpleado, idEmpresa));

            List<UsuarioDto> avales = query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).list();

            if (avales.isEmpty()) {
                return null;
            } else {
                usuDto = avales.get(0);
            }

            return usuDto;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    public List<UsuarioDto> consultaClavesAval() throws IntegracionException {

        try {
            super.beginTransaction();
            SQLQuery query = session.createSQLQuery("select usu_id as id, usu_clave_empleado as cveEmpleado,"
                    + "usu_nombre as nombre,usu_paterno as paterno,"
                    + "usu_materno as materno,usu_correo as correo,"
                    + "usu_telefono as telefono,usu_estado as estado,"
                    + "usu_municipio as municipio,usu_cp as cp,usu_colonia as colonia,usu_calle as calle,"
                    + "usu_numext as numext "
                    + "from usuarios ");
            List<UsuarioDto> avales = query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).list();

            return avales;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene los avales para mostrar en pantalla de detalle solicitud
     *
     * @param usuId
     * @return
     * @throws IntegracionException
     */
    public UsuarioDto getInfCmpltAval(Integer usuId) throws IntegracionException {
        UsuarioDto usuDto;

        try {
            super.beginTransaction();
            SQLQuery query = session.createSQLQuery(String.format("select concat(u.usu_nombre,' ',u.usu_paterno,' ',u.usu_materno) "
                    + "as nombreCompleto, "
                    + "u.usu_clave_empleado as cveEmpleado "
                    + "from usuarios u where u.usu_id = %1$s", usuId));
            usuDto = (UsuarioDto) query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).uniqueResult();

            return usuDto;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Metodo que actualiza los valores de los avales
     *
     * @param dto
     * @param estatus
     * @throws IntegracionException
     */
    public void updtAval(UsuarioDto dto, int estatus) throws IntegracionException {
        String hql = String.format(
                "update SolicitudAvales set solAvaIdEmpleado = %1$s, solAvaClaveEmpleado = %2$s, "
                + "solAvaEstatus = %3$s "
                + "where idSolAva = %4$s ", dto.getId(), dto.getCveEmpleado(), estatus, dto.getIdSolicitudAval());

        super.executeUpdate(hql);
    }

    /**
     * Obtiene los avales de una solicitud especifica
     *
     * @param idSolicitud
     * @return
     * @throws IntegracionException
     */
    public List<SolicitudAvales> getAvalesXSolicitud(BigInteger idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    String.format(
                            "from SolicitudAvales ava where ava.solicitudes.solId = %1$s "
                            + "order by ava.idSolAva",
                            idSolicitud));

            List<SolicitudAvales> avales = (List<SolicitudAvales>) query.list();

            return avales;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene los avales de una solicitud especifica
     *
     * @param idSolicitud
     * @return
     * @throws IntegracionException
     */
    public List<UsuarioDto> getAvalesByIdSolicitud(BigInteger idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select u.usu_id as id, s.sol_ava_clave_empleado as cveEmpleado,"
                            + "s.id_sol_ava as idSolicitud, "
                            + "s.sol_ava_estatus as estatusAvalInt, "
                            + "CONCAT_WS(' ',u.usu_nombre,u.usu_paterno,u.usu_materno) as nombreCompleto,"
                            + "CONCAT_WS(' ',u.usu_calle,' ',u.usu_numext,' ',u.usu_colonia,' ', u.usu_cp,' ', u.usu_municipio,' ', u.usu_estado) as domicilioConcat,"
                            + "u.usu_fecha_ingreso as fechaIngresoCaja,u.usu_telefono as telefono,u.usu_celular as celular,e.emp_abreviacion as empresaAbreviacion,"
                            + "u.usu_estatus as estatus "
                            + "from solicitud_avales s inner join usuarios u on s.sol_ava_id_empleado=u.usu_id "
                            + "inner join empresas e on u.usu_empresa=e.emp_id where sol_ava_solicitud= %1$s", idSolicitud));

            List<UsuarioDto> avales = (List<UsuarioDto>) query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).list();

            return avales;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        }finally{
            super.endTransaction();
        }
    }

    /**
     * Obtiene los avales de una credito especifico
     *
     * @param idCredito
     * @return
     * @throws IntegracionException
     */
    public List<UsuarioDto> getAvalesByCredito(Integer idCredito) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select u.usu_id as id, s.sol_ava_clave_empleado as cveEmpleado,"
                            + "s.id_sol_ava as idSolicitud, "
                            + "s.sol_ava_estatus as estatusAvalInt, "
                            + "CONCAT_WS(' ',u.usu_nombre,u.usu_paterno,u.usu_materno) as nombreCompleto,"
                            + "CONCAT_WS(' ',u.usu_calle,' ',u.usu_numext,' ',u.usu_colonia,' ', u.usu_cp,' ', u.usu_municipio,' ', u.usu_estado) as domicilioConcat,"
                            + "u.usu_fecha_ingreso as fechaIngresoCaja,u.usu_telefono as telefono,u.usu_celular as celular,e.emp_abreviacion as empresaAbreviacion,"
                            + "u.usu_estatus as estatus "
                            + "from solicitud_avales s inner join usuarios u on s.sol_ava_id_empleado=u.usu_id "
                            + "inner join empresas e on u.usu_empresa=e.emp_id "
                            + "left join creditos_final on cre_id = sol_ava_credito "
                            + "where sol_ava_credito= %1$s", idCredito));

            List<UsuarioDto> avales = (List<UsuarioDto>) query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).list();

            return avales;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
         }finally{
            super.endTransaction();
        }
    }

    /**
     * Metodo que actualiza los valores de los avales
     *
     * @param dto
     * @param estatus
     * @throws IntegracionException
     */
    public void updtAvalEstatus(UsuarioDto dto, int estatus) throws IntegracionException {
        String hql = String.format(
                "update SolicitudAvales set solAvaEstatus = %1$s "
                + "where idSolAva = %2$s ", estatus, dto.getIdSolicitud());

        super.executeUpdate(hql);
    }

    /**
     * Metodo que actualiza los valores de los avales
     *
     * @param idSol
     * @param estatus
     * @throws IntegracionException
     */
    public void updtAvalEstatusXSolId(BigInteger idSol, int estatus) throws IntegracionException {
        String hql = String.format(
                "update SolicitudAvales set solAvaEstatus = %1$s "
                + "where solicitudes.solId = %2$s ", estatus, idSol);

        super.executeUpdate(hql);
    }

    public static void main(String args[]) {
        try {
            AvalesSolicitudDao dao = new AvalesSolicitudDao();
            System.setProperty("Log4J.configuration", new File(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "Log4J.properties").toURI().toURL().toString());
            HibernateUtil.buildSessionFactory2();

            System.out.println(dao.consultaClavesAval().size());

            System.out.println(dao.getPerfilAvales(1, 1).getCveEmpleado());

            HibernateUtil.closeSessionFactory();

        } catch (IntegracionException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            Logger.getLogger(AvalesSolicitudDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
