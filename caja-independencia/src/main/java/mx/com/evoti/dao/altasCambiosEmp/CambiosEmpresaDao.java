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
import mx.com.evoti.dto.PagoDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.AltasCambiosHist;
import mx.com.evoti.hibernate.pojos.Catorcenas;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Util;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class CambiosEmpresaDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = -4889812958164185071L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CambiosEmpresaDao.class);

    /**
     * Obtiene los registros a los que no se les pudo pegar el usuid, y que por
     * lo tanto pueden ser cambios de empresa
     *
     * @param catorcena
     * @return
     * @throws IntegracionException
     */
    public List<MovimientosDto> getPendtsCambiosAltas(String catorcena) throws IntegracionException {

        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    String.format(
                            "select mov.movNombreEmpleado as movNombreEmpleado, "
                                    + "mov.movClaveEmpleado as movClaveEmpleado, "
                            + "mov.movFecha as movFecha, emp.empAbreviacion as empAbreviacion,"
                            + "mov.movEmpresa as movEmpresa, mov.movArhId as movArhId "
                            + "from Movimientos mov, Empresas emp "
                            + "where emp.empId = mov.movEmpresa  "
                            + "and mov.movFecha = '%1$s' "
                            + "and mov.movUsuId is null "
                            + "group by  mov.movNombreEmpleado, mov.movClaveEmpleado, mov.movFecha,emp.empAbreviacion, mov.movEmpresa, mov.movArhId", catorcena));

            List<MovimientosDto> movimientos = query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).list();

            return movimientos;
        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene los registros a los que no se les pudo pegar el usu id, y que por
     * lo tanto pueden ser cambios de empresa
     *
     * @param catorcena
     * @return
     * @throws IntegracionException
     */
    public List<PagoDto> getCambiosEmpresaFrmPag(String catorcena) throws IntegracionException {

        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    String.format(
                            "select pag.pagUsuNombre as pagUsuNombre, pag.pagClaveEmpleado as pagClaveEmpleado,"
                            + "emp.empAbreviacion as empAbreviacion, pag.pagFecha as pagFecha,pag.pagArhId as pagArhId "
                            + "from Pagos pag, Empresas emp "
                            + "where emp.empId= pag.pagEmpresa "
                            + "and pag.pagFecha = '%1$s' "
                            + "and pag.pagUsuId is null "
                            + "group by  pag.pagUsuNombre, pag.pagClaveEmpleado,emp.empAbreviacion,pag.pagFecha,pag.pagArhId ",
                            catorcena));

            List<PagoDto> pagos = query.setResultTransformer(Transformers.aliasToBean(PagoDto.class)).list();

            return pagos;
        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public List obtieneUsuarioXNombre(String pat) throws IntegracionException {
        super.beginTransaction();

        String sql = String.format(""
                + "from Usuarios usu inner join usu.empresas emp "
                + "where usu.usuPaterno like %1$s ",
                "'" + pat + "%' ");

        Query query = session.createQuery(sql);

        List usuarios = (List) query.list();
        super.endTransaction();
        return usuarios;

    }

    /**
     * Inserta registro en altas cambios hist
     *
     * @param pojo
     * @throws IntegracionException
     */
    public void insertCambioEmpresa(AltasCambiosHist pojo) throws IntegracionException {
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

    public void updateUsuarioClaveEmpresa(Usuarios pojo) throws IntegracionException {
        super.beginTransaction();

        try {

            String sql = String.format("update usuarios set usu_clave_empleado = %1$s, "
                    + "usu_empresa = %2$s where usu_id = %3$s ",
                    pojo.getUsuClaveEmpleado(), pojo.getEmpresas().getEmpId(), pojo.getUsuId());

            super.executeUpdateSql(sql);

        } catch (Exception ex) {

            throw new IntegracionException(ex.getMessage() + " No se pudo guardar la solicitud", ex);

        }

    }

    /**
     * Actualiza el id de usuario en la tabla pagos
     *
     * @param catorcena
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public void updateUsuIdPago(Date catorcena) throws IntegracionException {
        String sql = String.format("update pagos left join usuarios on pagos.pag_clave_empleado = usuarios.usu_clave_empleado \n"
                + "and pagos.pag_empresa = usu_empresa\n"
                + "set pag_usu_id = usu_id \n"
                + "where pagos.pag_usu_id is null \n"
                + "and pag_fecha ='%1$s'",
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
        String sql = String.format("update movimientos inner join usuarios on mov_clave_empleado = usu_clave_empleado \n"
                + "and mov_empresa = usu_empresa\n"
                + "set mov_usu_id = usu_id \n"
                + "where mov_usu_id is null and mov_fecha = '%1$s'",
                Util.generaFechaFormateada(catorcena));

        super.executeUpdateSql(sql);
    }

    public Date obtieneCatorcenaExacta(Date fecha) throws IntegracionException {
        Date catorcena;

        super.beginTransaction();

        String sql = String.format("select carFecha "
                + "from Catorcenas where carFecha = '%1$s' ", Util.generaFechaFormateada(fecha));

        Query query = session.createQuery(sql);

        catorcena = (Date) query.uniqueResult();

        super.endTransaction();

        return catorcena;

    }

    /**
     *
     * @param idUsuario
     * @param movId
     * @return
     * @throws IntegracionException
     */
    public MovimientosDto getUltimosMovs(Integer idUsuario, Integer movId) throws IntegracionException {

        try {
            super.beginTransaction();

            Query query = session.createSQLQuery(
                    String.format(
                            "select mov.mov_nombre_empleado as movNombreEmpleado, mov.mov_clave_empleado as movClaveEmpleado, "
                            + "mov.mov_fecha as movFecha,mov.mov_empresa as movEmpresa, mov.mov_arh_id as movArhId  "
                            + "from movimientos mov where mov.mov_usu_id = %1$s "
                            + "and mov.mov_arh_id is not null "
                            + "and mov.mov_id <> %2$s "
                            + "order by mov_fecha desc", idUsuario, movId));

            MovimientosDto movimientos = (MovimientosDto) query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).setMaxResults(1).uniqueResult();

            return movimientos;
        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    public static void main(String args[]) {
        try {
            HibernateUtil.buildSessionFactory2();
            CambiosEmpresaDao dao = new CambiosEmpresaDao();
//            List<MovimientosDto> movimientos = dao.getPendtsCambiosAltas("2016-10-13");
//            List<PagoDto> pagos = dao.getCambiosEmpresaFrmPag("2016-10-13");
//            List<Object[]> result = dao.obtieneUsuarioXNombre("MANZANO");

            MovimientosDto movs = dao.getUltimosMovs(1, 1302126);

            System.out.println(movs.getMovFecha());

            HibernateUtil.closeSessionFactory();
        } catch (IntegracionException ex) {
            Logger.getLogger(CambiosEmpresaDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
