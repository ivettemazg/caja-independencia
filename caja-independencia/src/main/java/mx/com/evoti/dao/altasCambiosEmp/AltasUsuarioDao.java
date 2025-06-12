/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.altasCambiosEmp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.ManagerDB;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.AltasCambiosHist;
import mx.com.evoti.hibernate.pojos.Usuarios;
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
public class AltasUsuarioDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = -4889812958164185071L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AltasUsuarioDao.class);

    /**
     * Obtiene los registros a los que no se les pudo pegar el usu id, y que por
     * lo tanto pueden ser cambios de empresa
     *
     * @return
     * @throws IntegracionException
     */
    public List<UsuarioDto> getUsuarios() throws IntegracionException {
        List<UsuarioDto> mapList = null;

        try {
            super.beginTransaction();
            String sql = String.format("select concat(u.usu_nombre,' ',u.usu_paterno,' ',u.usu_materno) as nombreCompleto, "
                    + "usu_id as id,usu_nombre as nombre,usu_paterno as paterno,usu_materno as materno,usu_rfc as rfc, "
                    + "e.emp_descripcion as empresa,usu_clave_empleado as cveEmpleado,usu_correo as correo,"
                    + "usu_empresa as idEmpresa, usu_fecha_baja as fechaBaja "
                    + "from usuarios u "
                    + "left join empresas e on u.usu_empresa=e.emp_id ");

            SQLQuery query = session.createSQLQuery(sql);
            mapList = query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).list();

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
        return mapList;
    }

    /**
     * Obtiene todos los movimientos que no tienen movusuid
     *
     * @return
     * @throws IntegracionException
     */
    public List<MovimientosDto> obtenerHistorialCambiosAltas() throws IntegracionException {
        try {
            super.beginTransaction();

            String sql = 
                "SELECT ANY_VALUE(mov.mov_nombre_empleado) AS movNombreEmpleado, " +
                "       mov.mov_clave_empleado            AS movClaveEmpleado, " +
                "       mov.mov_fecha                     AS movFecha, " +
                "       emp.emp_abreviacion               AS empAbreviacion, " +
                "       mov.mov_arh_id                    AS movArhId, " +
                "       mov.mov_empresa                   AS movEmpresa " +
                "FROM   movimientos mov " +
                "JOIN   empresas emp ON emp.emp_id = mov.mov_empresa " +
                "WHERE  mov.mov_usu_id IS NULL " +
                "  AND  mov.mov_nombre_empleado IS NOT NULL " +
                "GROUP BY mov.mov_clave_empleado, mov.mov_fecha, emp.emp_abreviacion, " +
                "         mov.mov_arh_id, mov.mov_empresa";

            SQLQuery query = session.createSQLQuery(sql);
            query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class));

            List<MovimientosDto> movimientos = query.list();
            return movimientos;

        } catch (Exception e) {
            throw new IntegracionException("Error al obtener historial de cambios de altas", e);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Inserta registro en altas cambios hist
     *
     * @param pojo
     * @throws IntegracionException
     */
    public void insertAltaCambioHist(AltasCambiosHist pojo) throws IntegracionException {
        super.beginTransaction();

        try {
            super.session.save(pojo);
            super.session.getTransaction().commit();
            LOGGER.debug("El registro en AltasCambiosHistorial se guardó correctamente");
        } catch (Exception ex) {
            super.session.getTransaction().rollback();

            throw new IntegracionException(ex.getMessage() + " No se pudo guardar la solicitud", ex);

        } finally {
            super.endTransaction();
        }
    }

    public Integer guardarUsuario(Usuarios pojo) throws IntegracionException {
        super.beginTransaction();

        try {
             super.session.save(pojo);
             Integer id = pojo.getUsuId();
            super.session.getTransaction().commit();

            LOGGER.debug("El registro en Usuarios se guardó correctamente");
            return id;
        } catch (Exception ex) {
            super.session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage() + " No se pudo guardar el usuario", ex);
        } finally {
            super.endTransaction();
        }
    }

    public void updateUsuarioClaveEmpresa(Usuarios pojo) throws IntegracionException {
        super.beginTransaction();

        try {
            super.session.update(pojo);
              super.session.getTransaction().commit();
            LOGGER.debug("El registro en AltasCambiosHistorial se guardó correctamente");
        } catch (Exception ex) {
            super.session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage() + " No se pudo guardar la solicitud", ex);

        }finally {
            super.endTransaction();
        }

      
    }

    /**
     * Actualiza el id de usuario en la tabla pagos
     *
     * @param catorcena
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public void updateUsuIdPago(Date catorcena) throws IntegracionException {
        String sql = String.format("update pagos inner join usuarios on pagos.pag_clave_empleado = usuarios.usu_clave_empleado  "
                + "and pag_empresa = usu_empresa "
                + "set pag_usu_id = usu_id "
                + "where pagos.pag_usu_id is null ",
                Util.generaFechaFormateada(catorcena));

        super.executeUpdateSql(sql);

    }

    /**
     * Actualiza el id de usuario en la tabla movimientos
     *
     * @param catorcena
     * @throws IntegracionException
     */
    public void updateUsuIdMov(Date catorcena) throws IntegracionException {
        String sql = String.format("update movimientos left join usuarios on mov_clave_empleado = usu_clave_empleado "
                + "and mov_empresa = usu_empresa "
                + "set mov_usu_id = usu_id "
                + "where mov_usu_id is null ",
                Util.generaFechaFormateada(catorcena));

        super.executeUpdateSql(sql);
    }

    public static void main(String args[]) {
        try {
            HibernateUtil.buildSessionFactory2();
            AltasUsuarioDao dao = new AltasUsuarioDao();

            dao.getUsuarios();
            dao.obtenerHistorialCambiosAltas();

            HibernateUtil.closeSessionFactory();
        } catch (IntegracionException ex) {
            Logger.getLogger(AltasUsuarioDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
