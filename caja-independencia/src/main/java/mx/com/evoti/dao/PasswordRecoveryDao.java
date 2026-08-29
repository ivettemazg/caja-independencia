package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.Date;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.PasswordResetTokens;
import mx.com.evoti.hibernate.pojos.Usuarios;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordRecoveryDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordRecoveryDao.class);

    public Usuarios buscaUsuarioParaRecuperacion(Integer claveEmpleado, Integer empresaId)
            throws IntegracionException {
        try {
            super.beginTransaction();
            Query query = session.createQuery("from Usuarios usu "
                    + "where usu.usuClaveEmpleado = :claveEmpleado "
                    + "and usu.empresas.empId = :empresaId "
                    + "and usu.usuEstatus = 1 "
                    + "and usu.usuFechaBaja is null");
            query.setParameter("claveEmpleado", claveEmpleado);
            query.setParameter("empresaId", empresaId);
            return (Usuarios) query.uniqueResult();
        } catch (HibernateException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    public Usuarios buscaUsuarioActivoPorClave(Integer claveEmpleado) throws IntegracionException {
        try {
            super.beginTransaction();
            Query query = session.createQuery("from Usuarios usu "
                    + "where usu.usuClaveEmpleado = :claveEmpleado "
                    + "and usu.usuEstatus = 1 "
                    + "and usu.usuFechaBaja is null");
            query.setParameter("claveEmpleado", claveEmpleado);
            query.setMaxResults(1);
            return (Usuarios) query.uniqueResult();
        } catch (HibernateException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    public void cancelaTokensActivos(Integer usuarioId) throws IntegracionException {
        String hql = "update PasswordResetTokens "
                + "set prtEstatus = 3 "
                + "where prtUsuId = " + usuarioId
                + " and prtEstatus = 1 "
                + "and prtFechaUso is null";
        super.executeUpdate(hql);
    }

    public void guardaToken(PasswordResetTokens token) throws IntegracionException {
        super.savePojo(token);
    }

    public PasswordResetTokens buscaTokenActivo(String tokenHash) throws IntegracionException {
        try {
            super.beginTransaction();
            Query query = session.createQuery("from PasswordResetTokens "
                    + "where prtTokenHash = :tokenHash "
                    + "and prtEstatus = 1 "
                    + "and prtFechaUso is null");
            query.setParameter("tokenHash", tokenHash);
            return (PasswordResetTokens) query.uniqueResult();
        } catch (HibernateException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    public void marcaTokenExpirado(Integer tokenId) throws IntegracionException {
        String hql = "update PasswordResetTokens "
                + "set prtEstatus = 3 "
                + "where prtId = " + tokenId;
        super.executeUpdate(hql);
    }

    public void actualizaPasswordYUsaToken(Integer usuarioId, Integer tokenId, String nuevoPassword)
            throws IntegracionException {
        Transaction tx = null;
        Session localSession = null;
        try {
            localSession = HibernateUtil.getSessionFactory().openSession();
            tx = localSession.beginTransaction();

            Usuarios usuario = (Usuarios) localSession.get(Usuarios.class, usuarioId);
            PasswordResetTokens token = (PasswordResetTokens) localSession.get(PasswordResetTokens.class, tokenId);

            if (usuario == null || token == null || !Integer.valueOf(1).equals(token.getPrtEstatus())) {
                throw new IntegracionException("La solicitud de recuperacion ya no es valida.");
            }

            usuario.setUsuPassword(nuevoPassword);
            token.setPrtFechaUso(new Date());
            token.setPrtEstatus(2);

            localSession.update(usuario);
            localSession.update(token);
            localSession.flush();
            tx.commit();
        } catch (HibernateException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollEx) {
                    LOGGER.error("Error al hacer rollback de recuperacion de password", rollEx);
                }
            }
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            if (localSession != null && localSession.isOpen()) {
                localSession.close();
            }
        }
    }
}
