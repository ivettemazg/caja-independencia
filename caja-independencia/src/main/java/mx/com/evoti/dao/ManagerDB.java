package mx.com.evoti.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.exception.LogError;
import mx.com.evoti.hibernate.config.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Ivette Manzano
 */
public class ManagerDB {

    private static final Logger LOGGER = Logger.getLogger(ManagerDB.class.getSimpleName());
    public static int sessionUser;
    public Session session = null;
    private Query query = null;
    private final ManagerDB managerDB = this;
    private final SQLQuery sqlQuery = null;

    public ManagerDB() {
        super();
    }

    public ManagerDB createQuery(String hql) throws IntegracionException {
        try {
            this.beginTransaction();
            this.query = this.session.createQuery(hql);
        } catch (HibernateException ex) {

            if (this.query != null) {
                throw new IntegracionException(LogError.QUERY
                        + this.query.getQueryString(), ex);
            } else {
                throw new IntegracionException(LogError.QUERY + LogError.QUERY_NULO, ex);
            }
        }
        return this.managerDB;
    }

    public ManagerDB setResultTransformer(Class type) {
        this.query = this.query.setResultTransformer(Transformers.aliasToBean(type));
        return this.managerDB;
    }

    public Object uniqueResult() throws IntegracionException {
        Object pojo = null;
        try {
            pojo = query.uniqueResult();
        } catch (HibernateException ex) {
            throw new IntegracionException(LogError.QUERY
                    + this.query.getQueryString(), ex);
        } finally {
            this.endTransaction();
        }
        return pojo;
    }

    public synchronized List<?> list() throws IntegracionException {
        List<?> listado = null;
        try {
            if (query != null) {
                listado = query.list();
            } else if (sqlQuery != null) {
                listado = sqlQuery.list();
            }
        } catch (HibernateException ex) {
            throw new IntegracionException(LogError.QUERY
                    + (query != null ? query.getQueryString() : sqlQuery.getQueryString()), ex);
        } finally {
            this.endTransaction();
        }
        return listado;
    }

    /**
     * Metodo que inicializa el objeto session
     *
     * @throws IntegracionException
     */
    public void beginTransaction() throws IntegracionException {
        try {
            this.session = null;
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.session.beginTransaction();
//                LOGGER.info("<---Se Inicializa Una Transaccion...--->");
        } catch (HibernateException ex) {
//            endTransaction();
//           closeSessionFactory();
            throw new IntegracionException(LogError.OPEN_SESSION, ex);
        }
    }

    public void endTransaction() throws IntegracionException {
        try {
            if (this.session != null && this.session.isConnected()) {
//                 LOGGER.info("<---Se Ha Finalizado La Transaccion--->");
                this.session.close();

            }
        } catch (HibernateException ex) {
            throw new IntegracionException(LogError.CLOSE, ex);
        }
    }

    protected void executeUpdate(String hql) throws IntegracionException {
        try {
            Query createQuery;
            this.beginTransaction();
            createQuery = this.session.createQuery(hql);
            createQuery.executeUpdate();
            this.session.flush();
            this.session.getTransaction().commit();

        } catch (HibernateException ex) {
            this.session.getTransaction().rollback();
            throw new IntegracionException(LogError.QUERY + hql, ex);
        } finally {
            this.endTransaction();
        }
    }

    protected void executeUpdateSql(String sql) throws IntegracionException {
        try {
            SQLQuery sqQuery;
            this.beginTransaction();
            sqQuery = this.session.createSQLQuery(sql);
            sqQuery.executeUpdate();

//                this.session.flush();
            this.session.getTransaction().commit();

        } catch (HibernateException ex) {
            this.session.getTransaction().rollback();
            throw new IntegracionException(LogError.QUERY + sql, ex);
        } finally {
            this.endTransaction();
        }
    }

    public void updatePojo(Object pojo) throws IntegracionException {
             
        this.beginTransaction();

        try {
            session.update(pojo);
            session.getTransaction().commit();
            session.flush();
        } catch (HibernateException ex) {
            session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage(), ex);
        }catch (Exception ex){ 
            session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            this.endTransaction();
        }

    }

    public void savePojo(Object pojo) throws IntegracionException {
       this.beginTransaction();

        try {
            session.save(pojo);
            session.getTransaction().commit();
            session.flush();

        } catch (HibernateException ex) {
            session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage(), ex);
        }catch (Exception ex){ 
            session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage(), ex);
        }finally {
            this.endTransaction();
        }

    }

    public void savePojos(List<Object> pojos) throws IntegracionException {
      this.beginTransaction();

        try {
            pojos.forEach(pojo -> session.save(pojo));
            session.getTransaction().commit();
            session.flush();

        } catch (HibernateException ex) {
            session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
           this.endTransaction();
        }

    }

    public Object mergePojo(Object pojo) throws IntegracionException {
        this.beginTransaction();

        try {
            pojo = session.merge(pojo);
            session.flush();
            session.getTransaction().commit();

        } catch (HibernateException ex) {
            session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            this.endTransaction();
        }
        return pojo;

    }

    protected void closeSessionFactory() {
        HibernateUtil.closeSessionFactory();
    }

    public void cerrarConexion(Connection connection) {

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                LOGGER.error("Error al intentar cerrar la conexión", ex);
            }
        }
    }
}
