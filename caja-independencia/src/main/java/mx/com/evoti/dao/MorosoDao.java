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
import mx.com.evoti.util.Util;
import org.hibernate.Query;

/**
 *
 * @author Venette
 */
public class MorosoDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = -2325493451677611752L;

    /**
     * Regresa la cantidad de 
     * @param catorcena
     * @param usuId
     * @return
     * @throws IntegracionException 
     */
    public Integer getCatorcenasAdeudadas(Date catorcena, int usuId) throws IntegracionException {
        
        super.beginTransaction();
        String sql = String.format("select amo_id "
                + "from creditos_final c inner join usuarios u on c.cre_usu_id=u.usu_id  "
                + "inner join amortizacion a on c.cre_id=a.amo_credito "
                + "where cre_estatus in (1,2)  "
                + "and a.amo_fecha_pago<='%1$s' "
                + "and a.amo_estatus_int=1 "
                + "and a.amo_monto_pago > 0 "
                + "and a.amo_usu_id = %2$s ", Util.generaFechaFormateada(catorcena), usuId);
        
         Query query = session.createSQLQuery(sql);

        List catorcenasAdeudadas =  query.list();

        super.endTransaction();
        return catorcenasAdeudadas.size();
    }

    
    
    
    
}
