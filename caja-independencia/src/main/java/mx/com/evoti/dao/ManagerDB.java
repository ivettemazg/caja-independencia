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
import org.hibernate.Transaction;
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
        Transaction tx = null;
        Session localSession = null;
        try {
            localSession = HibernateUtil.getSessionFactory().openSession();
            tx = localSession.beginTransaction();
            Query createQuery = localSession.createQuery(hql);
            createQuery.executeUpdate();
            localSession.flush();
            tx.commit();
        } catch (HibernateException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de la transaccion", rollEx);
                }
            }
            throw new IntegracionException(LogError.QUERY + hql, ex);
        } finally {
            if (localSession != null && localSession.isOpen()) {
                localSession.close();
            }
        }
    }

    protected void executeUpdateSql(String sql) throws IntegracionException {
        Transaction tx = null;
        Session localSession = null;
        try {
            localSession = HibernateUtil.getSessionFactory().openSession();
            tx = localSession.beginTransaction();
            SQLQuery sqQuery = localSession.createSQLQuery(sql);
            sqQuery.executeUpdate();
            tx.commit();

        } catch (HibernateException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de la transaccion", rollEx);
                }
            }
            throw new IntegracionException(LogError.QUERY + sql, ex);
        } finally {
            if (localSession != null && localSession.isOpen()) {
                localSession.close();
            }
        }
    }

    public void updatePojo(Object pojo) throws IntegracionException {
        Transaction tx = null;
        Session localSession = null;
        try {
            localSession = HibernateUtil.getSessionFactory().openSession();
            tx = localSession.beginTransaction();
            localSession.update(pojo);
            localSession.flush();
            tx.commit();
        } catch (HibernateException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de la transaccion", rollEx);
                }
            }
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de la transaccion", rollEx);
                }
            }
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            if (localSession != null && localSession.isOpen()) {
                localSession.close();
            }
        }

    }

    public void savePojo(Object pojo) throws IntegracionException {
        Transaction tx = null;
        Session localSession = null;
        try {
            localSession = HibernateUtil.getSessionFactory().openSession();
            tx = localSession.beginTransaction();
            localSession.save(pojo);
            localSession.flush();
            tx.commit();

        } catch (HibernateException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de la transaccion", rollEx);
                }
            }
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de la transaccion", rollEx);
                }
            }
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            if (localSession != null && localSession.isOpen()) {
                localSession.close();
            }
        }

    }

    public void savePojos(List<Object> pojos) throws IntegracionException {
        Transaction tx = null;
        Session localSession = null;
        try {
            localSession = HibernateUtil.getSessionFactory().openSession();
            tx = localSession.beginTransaction();
            pojos.forEach(localSession::save);
            localSession.flush();
            tx.commit();

        } catch (HibernateException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de la transaccion", rollEx);
                }
            }
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            if (localSession != null && localSession.isOpen()) {
                localSession.close();
            }
        }

    }

    public Object mergePojo(Object pojo) throws IntegracionException {
        Transaction tx = null;
        Session localSession = null;
        try {
            localSession = HibernateUtil.getSessionFactory().openSession();
            tx = localSession.beginTransaction();
            pojo = localSession.merge(pojo);
            localSession.flush();
            tx.commit();

        } catch (HibernateException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de la transaccion", rollEx);
                }
            }
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            if (localSession != null && localSession.isOpen()) {
                localSession.close();
            }
        }
        return pojo;

    }

    /**
     * Uso restringido.
     * <p>
     * La creación y cierre del {@link org.hibernate.SessionFactory} está
     * centralizado en {@link mx.com.evoti.hibernate.config.ContextListener}.
     * No invocar este método desde el flujo normal de la aplicación; únicamente
     * existe para utilerías standalone o pruebas locales.
     */
    @Deprecated
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
