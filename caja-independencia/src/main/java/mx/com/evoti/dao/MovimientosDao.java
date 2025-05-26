/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.AhorroVoluntarioDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.RendimientoDto;
import mx.com.evoti.hibernate.pojos.Movimientos;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Venette
 */
public class MovimientosDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 3938836229899238731L;

    /**
     * Guarda una lista de movimientos
     *
     * @param pojo
     * @throws IntegracionException
     */
    public void saveMovimientos(List pojo) throws IntegracionException {
        super.savePojos(pojo);
    }

    /**
     * Obtiene los ahorros fijo y no fijo con sus respectivos rendimientos,
     * obteniendo los totales de cadaa uno
     *
     * @param usuId
     * @return
     * @throws IntegracionException
     */
    public List<MovimientosDto> getAFANFyR(Integer usuId) throws IntegracionException {
        super.beginTransaction();

        String sql =
            "SELECT mov_producto, mov_ar, SUM(mov_deposito), " +
            "       ANY_VALUE(mov_usu_id), " +
            "       ANY_VALUE(mov_clave_empleado), " +
            "       ANY_VALUE(mov_empresa) " +
            "FROM movimientos " +
            "WHERE mov_usu_id = :usuId " +
            "  AND mov_producto IN (1, 2) " +
            "GROUP BY mov_producto, mov_ar";

        SQLQuery query = session.createSQLQuery(sql);
        query.setParameter("usuId", usuId);

        @SuppressWarnings("unchecked")
        List<Object[]> rawResults = (List<Object[]>) query.list();

        List<MovimientosDto> resultados = new ArrayList<>();

        for (Object[] row : rawResults) {
            MovimientosDto dto = new MovimientosDto();
            dto.setMovProducto(row[0] != null ? ((Number) row[0]).intValue() : null);
            dto.setMovAr(row[1] != null ? ((Number) row[1]).intValue() : null);
            dto.setTotalMovimiento(row[2] != null ? ((Number) row[2]).doubleValue() : null);
            dto.setMovUsuId(row[3] != null ? ((Number) row[3]).intValue() : null);
            dto.setMovClaveEmpleado(row[4] != null ? ((Number) row[4]).intValue() : null);
            dto.setMovEmpresa(row[5] != null ? ((Number) row[5]).intValue() : null);
            resultados.add(dto);
        }

        super.endTransaction();
        return resultados;
    }

    /**
     * OBtiene los ahorros voluntarios de un usuario con su respectivo
     * rendimiento
     *
     * @param usuId
     * @return
     * @throws IntegracionException
     */
    public List<MovimientosDto> getAVyR(Integer usuId) throws IntegracionException {

        super.beginTransaction();
        String sql = String.format("select movimientos.mov_usu_id as movUsuId,movimientos.mov_id as movId,"
                + "movimientos2.mov_producto as movProducto,movimientos2.mov_ar as movAr,"
                + "sum(movimientos2.mov_deposito) as totalMovimiento,movimientos2.mov_clave_empleado as movClaveEmpleado,"
                + "movimientos2.mov_empresa as movEmpresa "
                + "from movimientos,(select mov_id,mov_producto,mov_ar,mov_deposito,mov_id_padre,mov_fecha,"
                + "mov_clave_empleado,mov_empresa from movimientos where mov_producto=3) as movimientos2 "
                + "where movimientos.mov_usu_id=%1$s and "
                + "movimientos.mov_producto=3 and movimientos.mov_tipo='APORTACION' "
                + "and (movimientos.mov_id=movimientos2.mov_id_padre or movimientos.mov_id=movimientos2.mov_id) "
                + "group by movimientos.mov_id,movimientos2.mov_producto,movimientos2.mov_ar,"
                + "movimientos2.mov_clave_empleado,movimientos2.mov_empresa "
                + "order by movimientos.mov_id,movimientos2.mov_producto,movimientos2.mov_ar,"
                + "movimientos2.mov_clave_empleado,movimientos2.mov_empresa", usuId);
        SQLQuery query = session.createSQLQuery(sql);

        List<MovimientosDto> movs = query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).list();

        super.endTransaction();
        return movs;

    }

    /**
     * Guarda un registro en la tabla movimientos
     *
     * @param movimiento
     * @throws IntegracionException
     */
    public void saveMovimiento(Movimientos movimiento) throws IntegracionException {
        super.savePojo(movimiento);
    }

    /**
     * OBtiene el detalle de los ahorros
     *
     * @param usuId
     * @param movProducto
     * @param movAr
     * @return
     * @throws IntegracionException
     */
    public List<MovimientosDto> getDetalleAhorros(Integer usuId, Integer movProducto) throws IntegracionException {

        super.beginTransaction();
        String sql = String.format("select mov_usu_id as movUsuId,"
                + "mov_producto as movProducto,mov_ar as movAr,"
                + "mov_deposito as movDeposito, mov_fecha as movFecha, "
                + "mov_tipo as movTipo "
                + "from movimientos "
                + "where mov_usu_id =%1$s "
                + "and mov_producto =%2$s "
                + "order by mov_fecha asc ", usuId, movProducto);
        SQLQuery query = session.createSQLQuery(sql);

        List<MovimientosDto> movs = query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).list();

        super.endTransaction();
        return movs;

    }

    public AhorroVoluntarioDto getDetalleAhorroV(Integer usuId, Integer movProducto, Integer movIdPadre) throws IntegracionException {
      
        
        String sql = "";

        sql = String.format("select mov_id as id,"
                + "mov_deposito as monto, mov_fecha as fecha, "
                + "mov_tipo as nombre "
                + "from movimientos "
                + "where mov_id =%1$s "
                + "order by mov_fecha asc ", movIdPadre);

               
        
        super.beginTransaction();

        SQLQuery qry = session.createSQLQuery(sql);

        AhorroVoluntarioDto arVol = (AhorroVoluntarioDto) qry.setResultTransformer(Transformers.aliasToBean(AhorroVoluntarioDto.class)).uniqueResult();

        super.endTransaction();
        
       
        
        return arVol;

    }

    public List<RendimientoDto> getDetalleRendVol(Integer usuId, Integer movProducto, Integer movId) throws IntegracionException {
        String sql = "";

        sql = String.format("select mov_id as id,"
                + "mov_deposito as monto, mov_fecha as fecha, "
                + "mov_tipo as nombre "
                + "from movimientos "
                + "where mov_usu_id =%1$s "
                + "and mov_id_padre =%2$s "
                + "and mov_producto =%3$s "
                + "order by mov_fecha asc ", usuId, movId, movProducto);

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(sql);

        List<RendimientoDto> rends = query.setResultTransformer(Transformers.aliasToBean(RendimientoDto.class)).list();

        super.endTransaction();
        return rends;

    }

    /**
     * Obtiene los ahorros y rendimientos obteniendo los totales de cadaa uno
     *
     * @param usuId
     * @return
     * @throws IntegracionException
     */
    public List<MovimientosDto> getAhorrosTotalesFNF(Integer usuId) throws IntegracionException {

        super.beginTransaction();
        String sql = String.format("select mov_usu_id as movUsuId,mov_producto as movProducto,"
                + "sum(mov_deposito) as totalMovimiento "
                + "from movimientos where mov_usu_id=%1$s and mov_producto in (1,2) "
                + "group by mov_producto, mov_usu_id", usuId);

        SQLQuery query = session.createSQLQuery(sql);

        List<MovimientosDto> movs = query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).list();

        super.endTransaction();
        return movs;

    }

    /**
     *
     * @param usuId
     * @return
     * @throws IntegracionException
     */
    public List<MovimientosDto> getAhorrosTotalesVol(Integer usuId) throws IntegracionException {

        super.beginTransaction();
        String sql = String.format("select mov_padre.movUsuId as movUsuId,mov_padre.movProducto as movProducto,  "
                + "mov_padre.totalMovimiento+IFNULL(mov_hijos.totalMovimiento,0) as totalMovimiento, mov_padre.idMov as movId "
                + "from "
                + "(select sum(mov_deposito) as totalMovimiento, movimientos.mov_id_padre as idPadre   "
                + "from movimientos where mov_usu_id=%1$s and mov_producto in (3)   "
                + "and mov_id_padre is not null  "
                + "group by mov_id_padre  "
                + ") mov_hijos right join   "
                + "(select mov_usu_id as movUsuId,mov_producto as movProducto,  "
                + "mov_clave_empleado as movClaveEmpleado,mov_empresa as movEmpresa,  "
                + "sum(mov_deposito) as totalMovimiento, mov_id as idMov  "
                + "from movimientos where mov_usu_id=%1$s and mov_producto in (3)   "
                + "and mov_id_padre is null  "
                + "group by mov_usu_id ,mov_producto,  "
                + "mov_id) mov_padre on mov_padre.idMov = mov_hijos.idPadre", usuId);

        SQLQuery query = session.createSQLQuery(sql);

        List<MovimientosDto> movs = query.setResultTransformer(Transformers.aliasToBean(MovimientosDto.class)).list();

        super.endTransaction();
        return movs;

    }

}
