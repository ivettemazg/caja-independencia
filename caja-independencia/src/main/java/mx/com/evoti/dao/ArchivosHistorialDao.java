/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.Date;
import mx.com.evoti.dao.exception.IntegracionException;
import org.hibernate.Query;

/**
 *
 * @author Venette
 */
public class ArchivosHistorialDao extends ManagerDB implements Serializable {
    
    private static final long serialVersionUID = 4894621475653242688L;
    
    
    /**
     * Obtiene la fecha mas reciente de subida de un archivo
     * @param empresa
     * @return
     * @throws IntegracionException 
     */
     public Date getCatorUltArchivo(int empresa) throws IntegracionException {
        Date catorcena;
        super.beginTransaction();

        String sql = String.format(
                "select arh_fecha_catorcena "
                        + "from archivos_historial "
                        + "where arh_empresa = %1$s "
                        + "and arh_tipo_archivo = 1 "
                        + "order by arh_fecha_catorcena desc "
                        + "limit 1 ", empresa);

        Query query = session.createSQLQuery(sql);

        catorcena = (Date) query.uniqueResult();

        super.endTransaction();

        return catorcena;
    }
    
}
