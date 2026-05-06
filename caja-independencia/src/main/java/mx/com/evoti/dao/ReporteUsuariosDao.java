/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.List;
import mx.com.evoti.dao.exception.LogError;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.ReporteUsuariosDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author NeOleon
 */
public class ReporteUsuariosDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 8958190915055950161L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteUsuariosDao.class);

    public List getReporteUsuarios() throws IntegracionException {
        String sql = String.format("select usu_clave_empleado as claveEmpleado, usu_nombre as nombre, "
                + "usu_paterno as paterno, usu_materno as materno, e.emp_abreviacion as empresaAbreviacion, usu_password as psw, "
                + "usu_fecha_ingreso as fechaIngresoCaja, usu_fecha_ingreso_empresa as fechaIngresoEmpresa, "
                + "usu_fecha_baja as fechaBaja "
                + "from usuarios u left join empresas e on u.usu_empresa=e.emp_id order by usu_id");
        Session localSession = null;
        try {
            localSession = HibernateUtil.getSessionFactory().openSession();
            SQLQuery sqlQuery = localSession.createSQLQuery(sql);
            return sqlQuery.setResultTransformer(Transformers.aliasToBean(ReporteUsuariosDto.class)).list();
        } catch (HibernateException ex) {
            throw new IntegracionException(LogError.QUERY + sql, ex);
        } finally {
            if (localSession != null && localSession.isOpen()) {
                localSession.close();
            }
        }
    }

}
