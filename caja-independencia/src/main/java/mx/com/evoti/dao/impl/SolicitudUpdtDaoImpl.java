/*
 * 
 */
package mx.com.evoti.dao.impl;

import java.util.Date;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.config.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Venette
 */
public interface SolicitudUpdtDaoImpl {

    default public void updtEstatusSolicitud(Long idSolicitud, Integer estatus, Date fechaGenerica) throws IntegracionException {
            Session session ;
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            
             String hql = String.format("update Solicitudes sol "
                + "set sol.solicitudEstatus.solEstId = %2$s "
                + "where sol.solId = %1$s ", idSolicitud, estatus);
            
        try {

            Query query;
            query = session.createQuery(hql);
            query.executeUpdate();

             if(session != null && session.isConnected()){
                 session.close();
            }

        } catch (HibernateException ex) {
            session.getTransaction().rollback();
            throw new IntegracionException(hql, ex);
        } finally {
             if(session != null && session.isConnected()){
                 session.close();
            }
        }
     
    }

}
