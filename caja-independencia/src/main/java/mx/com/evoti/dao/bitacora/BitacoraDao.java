/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.bitacora;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import mx.com.evoti.dao.ManagerDB;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.Bitacora;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class BitacoraDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = -1505929711172591518L;
    
     private Logger LOGGER = LoggerFactory.getLogger(BitacoraDao.class);
     
     /**
      * Metodo que guarda un registro en bitacora
      * @param bitacora
      * @return
      * @throws IntegracionException 
      */
     public Bitacora saveBitacora(Bitacora bitacora) throws IntegracionException{
           super.beginTransaction();
          
        try {
             super.session.save(bitacora);
             super.session.getTransaction().commit();
            LOGGER.debug("La nota se guardó correctamente");
        } catch (Exception ex) {
            super.session.getTransaction().rollback();

            throw new IntegracionException(ex.getMessage() + " No se pudo guardar la solicitud", ex);

        }finally{
            super.endTransaction();
        }
        return bitacora;
     }
     
     
     /**
      * Obtiene un registro de bitacora a partir de la referencia
      * @param referencia
     * @param tipoTransaccion
      * @return
      * @throws IntegracionException 
      */
     public List<Bitacora> getBitacoraXReferencia(BigInteger referencia, Integer tipoTransaccion) throws IntegracionException{
       try {
            super.beginTransaction();

            Query query = session.createQuery(String.format("from Bitacora "
                    + "where bitReferencia = '%1$s' and bitTipo = %2$s ", referencia, tipoTransaccion));

            List<Bitacora> bitacora = (List<Bitacora>) query.list();
            return bitacora;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }    
     }
     
     /**
      * Obtiene un registro de bitacora a partir de la subreferencia
      * @param subreferencia
      * @return
      * @throws IntegracionException 
      */
     public List<Bitacora> getBitacoraXSubReferencia(BigInteger subreferencia) throws IntegracionException{
       try {
            super.beginTransaction();

            Query query = session.createQuery(String.format("from Bitacora "
                    + "where bitSubreferencia = '%1$s'", subreferencia));

            List<Bitacora> bitacora = (List<Bitacora>) query.list();
            return bitacora;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }    
     }
    
     
    
     
}
