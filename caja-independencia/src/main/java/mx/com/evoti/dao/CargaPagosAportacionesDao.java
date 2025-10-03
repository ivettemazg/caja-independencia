/*
 * To change super template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.util.Util;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.exception.LogError;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.ArchivoDto;
import mx.com.evoti.dto.PagoDto;
import mx.com.evoti.hibernate.pojos.ArchivosHistorial;
import mx.com.evoti.hibernate.pojos.Catorcenas;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
public class CargaPagosAportacionesDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 5148615645164614031L;
    private Logger LOGGER = LoggerFactory.getLogger(CargaPagosAportacionesDao.class);

    /**
     * Busca x nombre un registro en la tabla archivos_historial
     *
     * @param nombreArchivo
     * @return
     * @throws IntegracionException
     */
    public ArchivosHistorial buscaNombreArchivo(String nombreArchivo) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(String.format("from ArchivosHistorial "
                    + "where arhNombreArchivo = '%1$s'", nombreArchivo));

            ArchivosHistorial archivo = (ArchivosHistorial) query.uniqueResult();
            return archivo;
        } catch (HibernateException he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    public Catorcenas obtieneCatorcenaExacta(Date catorcena) throws IntegracionException {

        try {
            super.beginTransaction();

            Query query = session.createQuery(String.format("from Catorcenas where carFecha = '%1$s' ",
                    Util.generaFechaFormateada(catorcena)));

            Catorcenas cator = (Catorcenas) query.uniqueResult();

            return cator;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public int insertaArchivo(ArchivosHistorial archivo) throws IntegracionException {

        super.beginTransaction();

        try {
            super.session.save(archivo);
            super.session.getTransaction().commit();
            LOGGER.debug("El archivo se guardó correctamente");
        } catch (HibernateException ex) {
            super.session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage() + " No se pudo guardar el archivo", ex);
        } finally {
            super.endTransaction();
        }
        return archivo.getArhId();

    }

    public List<ArchivoDto> obtenerArchivosHistorico() throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery("select archivos_historial.arh_id as arhId, "
                    + "archivos_historial.arh_nombre_archivo as arhNombreArchivo, "
                    + "archivos_historial.arh_fecha_subida as arhFechaSubida, archivos_historial.arh_empresa as arhEmpresa, "
                    + "archivos_historial.arh_estatus as arhEstatus, archivos_historial.arh_registros as arhRegistros, "
                    + "archivos_historial.arh_tipo_archivo as arhTipoArchivo, archivos_historial.arh_fecha_catorcena as arhFechaCatorcena, "
                    + "emp_abreviacion as empAbreviacion "
                    + "FROM archivos_historial, empresas "
                    + "where arh_empresa = emp_id "
                    + "order by archivos_historial.arh_id desc "
                    + "limit 8");

            List<ArchivoDto> solsPndts = query.setResultTransformer(Transformers.aliasToBean(ArchivoDto.class)).list();

            return solsPndts;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene los archivos correspondientes a la catorcena que se está mandando
     *
     * @param catorcena
     * @return
     * @throws IntegracionException
     */
    public List<ArchivoDto> obtenerArchivosHistoricoXCatorcena(Date catorcena) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select archivos_historial.arh_id as arhId, "
                    + "archivos_historial.arh_nombre_archivo as arhNombreArchivo, "
                    + "archivos_historial.arh_fecha_subida as arhFechaSubida, archivos_historial.arh_empresa as arhEmpresa, "
                    + "archivos_historial.arh_estatus as arhEstatus, archivos_historial.arh_registros as arhRegistros, "
                    + "archivos_historial.arh_tipo_archivo as arhTipoArchivo, archivos_historial.arh_fecha_catorcena as arhFechaCatorcena, "
                    + "emp_abreviacion as empAbreviacion "
                    + "FROM archivos_historial, empresas "
                    + "where arh_empresa = emp_id "
                    + "and arh_fecha_catorcena = '%1$s' "
                    + "order by arh_tipo_archivo desc ", Util.generaFechaFormateada(catorcena)));
            List<ArchivoDto> solsPndts = query.setResultTransformer(Transformers.aliasToBean(ArchivoDto.class)).list();

            return solsPndts;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    public void eliminarArchivoXId(int idArchivo) throws IntegracionException {

        String sql = String.format("delete from archivos_historial where arh_id = %1$s", idArchivo);

        super.executeUpdateSql(sql);
    }

    public void eliminarPagosXArchivo(int idArchivo) throws IntegracionException {

        String sql = String.format("delete from pagos where pagos.pag_arh_id = %1$s", idArchivo);

        super.executeUpdateSql(sql);
    }

    public void eliminarMovimientosXArchivo(int idArchivo) throws IntegracionException {

        String sql = String.format("delete from movimientos where mov_arh_id  = %1$s", idArchivo);

        super.executeUpdateSql(sql);

    }

    public List<ArchivoDto> buscaArchivosEnFechaConEstatusPendiente(Date catorcena) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select archivos_historial.arh_id as arhId, "
                    + "archivos_historial.arh_nombre_archivo as arhNombreArchivo, "
                    + "archivos_historial.arh_fecha_subida as arhFechaSubida, archivos_historial.arh_empresa as arhEmpresa, "
                    + "archivos_historial.arh_estatus as arhEstatus, archivos_historial.arh_registros as arhRegistros, "
                    + "archivos_historial.arh_tipo_archivo as arhTipoArchivo, archivos_historial.arh_fecha_catorcena as arhFechaCatorcena, "
                    + "emp_abreviacion as empAbreviacion "
                    + "FROM archivos_historial, empresas "
                    + "where arh_empresa = emp_id "
                    + "and arh_fecha_catorcena = '%1$s' "
                    + "and arh_tipo_archivo = 1", Util.generaFechaFormateada(catorcena)));
            List<ArchivoDto> solsPndts = query.setResultTransformer(Transformers.aliasToBean(ArchivoDto.class)).list();

            return solsPndts;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    public Long buscaPagosSinUsuId(Date catorcena) throws IntegracionException {
        try {
            super.beginTransaction();
            Query query = session.createQuery(String.format("select count(*) from Pagos where "
                    + "pagUsuId is null and pagFecha = '%1$s' "
                    + "and pagArhId is not null ", Util.generaFechaFormateada(catorcena)));

            Long noPagos = (Long) query.uniqueResult();

            return noPagos;
        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public void actualizaEstatusArchivo(String catorcena, int tipoArchivo) throws IntegracionException {
        String sql = String.format("update archivos_historial set arh_estatus = 2 "
                + "where arh_fecha_catorcena = '%1$s' "
                + "and arh_tipo_archivo = %2$s",
                catorcena, tipoArchivo);
        super.executeUpdateSql(sql);
    }

    public List<PagoDto> obtenerPagosXArchivo(int idArchivo) throws IntegracionException {
        try {
            super.beginTransaction();
            SQLQuery query = session.createSQLQuery(String.format("select pag_clave_empleado as pagClaveEmpleado,"
                    + " pag_credito as pagCredito, pag_deposito as pagMonto, emp_abreviacion as pagNombreEmpresa, "
                    + "pag_est_nombre as pagEstNombre, pag_fecha as pagFecha, pag_id as pagoId, "
                    + "pag_usu_nombre as pagNombreUsuario "
                    + "from pagos inner join pagos_estatus on pag_est_id = pag_estatus "
                    + "inner join empresas on pag_empresa = emp_id "
                    + "where pag_arh_id = %1$s ", idArchivo));

            List<PagoDto> pagos = query.setResultTransformer(Transformers.aliasToBean(PagoDto.class)).list();

            return pagos;
        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    public List<MovimientosDto> obtenerAportacionesXArchivo(int idArchivo) throws IntegracionException {
        try {
            super.beginTransaction();
            SQLQuery query = session.createSQLQuery(String.format(" select mov_nombre_empleado as movNombreEmpleado, "
                    + "mov_arh_id as movArhId, mov_tipo as movTipo, emp_id as movEmpresa,"
                    + "emp_abreviacion as empAbreviacion, "
                    + "mov_clave_empleado as movClaveEmpleado, "
                    + "mov_producto as movProducto, "
                    + "mov_deposito as movDeposito, mov_fecha as movFecha, mov_id as movId "
                    + "FROM movimientos inner join empresas on emp_id = mov_empresa "
                    + "where mov_arh_id = %1$s ", idArchivo));
            List<MovimientosDto> movimientos = query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).list();

            return movimientos;
        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Inserta la lista de pagos que vienen del archivo de pagos
     *
     * @param lstPagos
     * @param idArchivo
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public void insertaListaPagos(List<PagoDto> lstPagos, int idArchivo) throws IntegracionException {

        try {
            SQLQuery sqQuery;
            this.beginTransaction();

            for (PagoDto pago : lstPagos) {

                String sql = String.format("insert into pagos ("
                        + "pag_clave_empleado,pag_fecha,pag_deposito,"
                        + "pag_empresa,pag_estatus,"
                        + "pag_arh_id,pag_usu_nombre) "
                        + "VALUES (%1$s,'%2$s',%3$s,%4$s,%5$s,%6$s,'%7$s')",
                        pago.getPagClaveEmpleado(),
                        Util.generaFechaFormateada(pago.getPagFecha()),
                        pago.getPagMonto(),
                        pago.getPagEmpresa(),
                        pago.getPagEstatus(), //Debe ser 1 cuando es recién ingresado
                        idArchivo,
                        pago.getPagNombreUsuario()
                );

                sqQuery = this.session.createSQLQuery(sql);
                sqQuery.executeUpdate();

            }

            this.session.getTransaction().commit();

        } catch (HibernateException ex) {
            this.session.getTransaction().rollback();
            throw new IntegracionException(LogError.QUERY, ex);
        } finally {
            this.endTransaction();
        }

    }

    public void insertaListaMovimientos(List<MovimientosDto> lstMovimientos, int idArchivo) throws IntegracionException {

        try {
            SQLQuery sqQuery;
            this.beginTransaction();

            for (MovimientosDto apo : lstMovimientos) {
                String sql = String.format("insert into movimientos ("
                        + "mov_fecha,mov_deposito,mov_producto,"
                        + "mov_clave_empleado,mov_empresa,"
                        + "mov_tipo,mov_arh_id, mov_nombre_empleado) "
                        + "VALUES ('%1$s',%2$s,%3$s,%4$s,%5$s,'%6$s',%7$s,'%8$s')",
                        Util.generaFechaFormateada(apo.getMovFecha()),
                        apo.getMovDeposito(),
                        apo.getMovProducto(),
                        apo.getMovClaveEmpleado(), //Debe ser 1 cuando es recién ingresado
                        apo.getMovEmpresa(),
                        apo.getMovTipo(),
                        idArchivo,
                        apo.getMovNombreEmpleado()
                );

                sqQuery = this.session.createSQLQuery(sql);
                sqQuery.executeUpdate();

            }

            this.session.getTransaction().commit();

        } catch (HibernateException ex) {
            this.session.getTransaction().rollback();
            throw new IntegracionException(LogError.QUERY, ex);
        } finally {
            this.endTransaction();
        }

    }

    /**
     * Actualiza el id de usuario en la tabla pagos
     *
     * @param idArchivo
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public void updateUsuIdPago(int idArchivo) throws IntegracionException {
        String sql = String.format("update pagos left join usuarios "
                + "on (pagos.pag_clave_empleado = usuarios.usu_clave_empleado and pagos.pag_empresa = usuarios.usu_empresa) "
                + "inner join archivos_historial on pagos.pag_arh_id = archivos_historial.arh_id "
                + "set pag_usu_id = usu_id "
                + "where pag_empresa = archivos_historial.arh_empresa "
                + "and pagos.pag_usu_id is null "
                + "and pag_arh_id =%1$s",
                idArchivo);

        super.executeUpdateSql(sql);

    }

    /**
     * Actualiza el id de usuario en la tabla movimientos
     *
     * @param idArchivo
     * @throws IntegracionException
     */
    public void updateUsuIdMov(int idArchivo) throws IntegracionException {
        String sql = String.format("update movimientos left join usuarios "
                + "on (mov_clave_empleado = usu_clave_empleado and mov_empresa = usu_empresa) "
                + "inner join archivos_historial on mov_arh_id = arh_id "
                + "set mov_usu_id = usu_id "
                + "where mov_empresa = arh_empresa "
                + "and mov_usu_id is null "
                + "and mov_arh_id = %1$s",
                idArchivo);

        super.executeUpdateSql(sql);

    }

    public Double obtieneSumaArchivo(Integer idArchivo, Integer tipoArchivo) throws IntegracionException {
        String sql = "";

        //Cuando es un archivo de PAGOS
        if (tipoArchivo == 1) {
            sql = String.format("select IFNULL(sum(pag_deposito),0)"
                    + " from pagos where pagos.pag_arh_id = %1$s ", idArchivo);
        } else if (tipoArchivo == 2) {
            sql = String.format("select IFNULL(sum(mov_deposito),0) "
                    + "from movimientos where mov_arh_id = %1$s ", idArchivo);
        }

        super.beginTransaction();
        SQLQuery query = session.createSQLQuery(sql);
        Double sumaArchivo = (Double) query.uniqueResult();

        super.endTransaction();

        return Util.round(sumaArchivo);

    }

    public List<MovimientosDto> getANoFijo(Integer idArchivo) throws IntegracionException {
        try {
            super.beginTransaction();
            SQLQuery query = session.createSQLQuery(String.format("select mov_id as movId, "
                    + "mov_usu_id as movUsuId from movimientos "
                    + "where mov_arh_id = %1$s and mov_producto = 2 ", idArchivo));
            List<MovimientosDto> movimientos = query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).list();

            return movimientos;
        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    public List<MovimientosDto> getAFijo(Integer movUsuId) throws IntegracionException {
         try {
            super.beginTransaction();
            SQLQuery query = session.createSQLQuery(String.format("select mov_id as movId, "
                    + "mov_usu_id as movUsuId from movimientos "
                    + "where mov_usu_id = %1$s and mov_producto = 1 ", movUsuId));
            List<MovimientosDto> movimientos = query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).list();

            return movimientos;
        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    
    public void updtMovimiento(MovimientosDto mov) throws IntegracionException {
        String sql = String.format("update movimientos "
                + "set mov_cambioanfaf = %1$s, "
                + "mov_producto = %2$s "
                + "where mov_id = %3$s",
                mov.getMovCambioanfaf(), mov.getMovProducto(), mov.getMovId());

        super.executeUpdateSql(sql);
    }

    public void actualizaMovimientosNoFijosSinAhorroFijo(Integer idArchivo) throws IntegracionException {
        String actualizaUsuariosConId = String.format(
                "update movimientos m "
                + "join (select min(base.mov_id) as movId "
                + "      from movimientos base "
                + "      where base.mov_arh_id = %1$s "
                + "        and base.mov_producto = 2 "
                + "        and base.mov_usu_id is not null "
                + "        and not exists (select 1 from movimientos fijo "
                + "                        where fijo.mov_usu_id = base.mov_usu_id "
                + "                          and fijo.mov_producto = 1) "
                + "      group by base.mov_usu_id) candidatos on candidatos.movId = m.mov_id "
                + "set m.mov_producto = 1, "
                + "    m.mov_cambioanfaf = 1 "
                + "where m.mov_arh_id = %1$s "
                + "  and m.mov_producto = 2",
                idArchivo);

        String actualizaSinUsuario = String.format(
                "update movimientos "
                + "set mov_producto = 1, "
                + "    mov_cambioanfaf = 1 "
                + "where mov_arh_id = %1$s "
                + "  and mov_producto = 2 "
                + "  and mov_usu_id is null",
                idArchivo);

        super.executeUpdateSql(actualizaUsuariosConId);
        super.executeUpdateSql(actualizaSinUsuario);
    }

}
