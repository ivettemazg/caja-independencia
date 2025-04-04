/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.ReporteAportacionesDto;
import mx.com.evoti.util.Util;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author NeOleon
 */
public class ReporteAportacionesDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 7574553879515724967L;

    public List<ReporteAportacionesDto> extraeDatosArchivo(Date fechaCatorcena, Integer empresa) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(
                String.format("select arh_nombre_archivo as nombreArchivo,arh_fecha_subida as fechaSubida,"
                        + "arh_estatus as estatus "
                        + "from archivos_historial where arh_fecha_catorcena='%1$s' and arh_empresa=%2$s "
                        + "and arh_tipo_archivo=2"
                        + "", Util.generaFechaFormateada(fechaCatorcena), empresa));

        List<ReporteAportacionesDto> creeds = query.setResultTransformer(Transformers.aliasToBean(ReporteAportacionesDto.class)).list();
        super.endTransaction();
        return creeds;

    }

    public List<ReporteAportacionesDto> extraeResumenArchivo(Date fechaCatorcena, Integer empresa) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(
                String.format("select p.pro_descripcion as producto,sum(m.mov_deposito) as deposito,"
                        + "count(*) as registros "
                        + "from archivos_historial a left join movimientos m on a.arh_id=m.mov_arh_id "
                        + "left join productos p on m.mov_producto=p.pro_id "
                        + "where arh_fecha_catorcena='%1$s' and arh_empresa=%2$s and arh_tipo_archivo=2 "
                        + "group by p.pro_descripcion order by pro_descripcion "
                        + "", Util.generaFechaFormateada(fechaCatorcena), empresa));

        List<ReporteAportacionesDto> creeds = query.setResultTransformer(Transformers.aliasToBean(ReporteAportacionesDto.class)).list();
        super.endTransaction();
        return creeds;

    }

    public List<ReporteAportacionesDto> extraeDetalleArchivo(Date fechaCatorcena, Integer empresa) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(
                String.format("select p.pro_descripcion as producto,mov_clave_empleado as claveEmpleado,"
                        + "mov_nombre_empleado as nombre,mov_deposito as deposito "
                        + "from archivos_historial a left join movimientos m on a.arh_id=m.mov_arh_id "
                        + "left join productos p on m.mov_producto=p.pro_id where arh_fecha_catorcena='%1$s' "
                        + "and arh_empresa=%2$s and arh_tipo_archivo=2 "
                        + "", Util.generaFechaFormateada(fechaCatorcena), empresa));

        List<ReporteAportacionesDto> creeds = query.setResultTransformer(Transformers.aliasToBean(ReporteAportacionesDto.class)).list();
        super.endTransaction();
        return creeds;

    }

    public List<ReporteAportacionesDto> extraeUsuariosNoRegistrados(Date fechaCatorcena, Integer empresa) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(
                String.format("select p.pro_descripcion as producto,mov_clave_empleado as claveEmpleado,"
                        + "mov_nombre_empleado as nombre, mov_deposito as deposito "
                        + "from archivos_historial a left join movimientos m on a.arh_id=m.mov_arh_id "
                        + "left join productos p on m.mov_producto=p.pro_id "
                        + "where arh_fecha_catorcena='%1$s' and arh_empresa=%2$s and arh_tipo_archivo=2 "
                        + "and mov_usu_id is null "
                        + "", Util.generaFechaFormateada(fechaCatorcena), empresa));

        List<ReporteAportacionesDto> creeds = query.setResultTransformer(Transformers.aliasToBean(ReporteAportacionesDto.class)).list();
        super.endTransaction();
        return creeds;

    }

    public List<ReporteAportacionesDto> extraeUsuariosConBaja(Date fechaCatorcena, Integer empresa) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(
                String.format("select u.usu_id as usuId, p.pro_descripcion as producto,mov_clave_empleado as claveEmpleado,"
                        + "mov_nombre_empleado as nombre,mov_deposito as deposito,u.usu_fecha_baja as fechaBaja "
                        + "from archivos_historial a left join movimientos m on a.arh_id=m.mov_arh_id "
                        + "left join productos p on m.mov_producto=p.pro_id "
                        + "left join usuarios u on m.mov_usu_id = u.usu_id "
                        + "where arh_fecha_catorcena='%1$s' and arh_empresa=%2$s and arh_tipo_archivo=2 "
                        + "and usu_fecha_baja is not null "
                        + "", Util.generaFechaFormateada(fechaCatorcena), empresa));

        List<ReporteAportacionesDto> creeds = query.setResultTransformer(Transformers.aliasToBean(ReporteAportacionesDto.class)).list();
        super.endTransaction();
        return creeds;

    }
    
    public void updtEstatusAltaUsr(Integer usuId) throws IntegracionException {
        String hql = String.format("update Usuarios "
                + "set usuFechaBaja = null, usuEstatus = 1 "
                + "where usuId = %1$s", usuId);

        super.executeUpdate(hql);

    }

}
