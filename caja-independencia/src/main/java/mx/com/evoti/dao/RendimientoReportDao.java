/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.util.Util;
import org.hibernate.SQLQuery;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class RendimientoReportDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 516517538449528910L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RendimientoReportDao.class);
    
    
    
    public BigDecimal obtieneAcumuladoXMes(Date fecha) throws IntegracionException{
          super.beginTransaction();
        SQLQuery query = session.createSQLQuery(String.format("select sum(mov_deposito) "
                + "from usuarios u left join baja_empleados b on u.usu_id=b.bae_id_empleado " 
                + "left join movimientos m on u.usu_id=m.mov_usu_id "
                + "where mov_ar = 1 and mov_tipo in ('APORTACION','DEVOLUCION') "
                + "and (usu_fecha_baja is null or usu_fecha_baja>'%1$s') "
                + "and (b.bae_fecha_baja is null or b.bae_fecha_baja>'%1$s') "
                + "and year(m.mov_fecha)=year(date_sub('%1$s',interval 1 month)) " 
                +"and month(m.mov_fecha)=month(date_sub('%1$s',interval 1 month)) ", Util.generaFechaFormateada(fecha)));

        Double monto = (Double) query.uniqueResult();
        

        super.endTransaction();
        return new BigDecimal(monto!=null ? monto : 0.0);
    }
    
    
}
