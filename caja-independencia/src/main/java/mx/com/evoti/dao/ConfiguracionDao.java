package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.Date;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.Configuracion;
import org.hibernate.HibernateException;
import org.hibernate.Query;

/**
 * DAO para configuración del sistema (banderas de créditos)
 */
public class ConfiguracionDao extends ManagerDB implements Serializable {
    private static final long serialVersionUID = 1L;

    public Configuracion getOrCreateConfig() throws IntegracionException {
        try {
            super.beginTransaction();
            Query q = session.createQuery("from Configuracion where conId = 1");
            Configuracion cfg = (Configuracion) q.uniqueResult();
            if (cfg == null) {
                cfg = new Configuracion();
                cfg.setConId(1);
                cfg.setConFaHabilitado(1);
                cfg.setConAgHabilitado(1);
                cfg.setConFechaMod(new Date());
                session.save(cfg);
                session.flush();
            }
            return cfg;
        } catch (HibernateException e) {
            throw new IntegracionException(e);
        } finally {
            super.endTransaction();
        }
    }

    public void updateFlags(int fa, int ag) throws IntegracionException {
        String hql = String.format("update Configuracion set conFaHabilitado = %1$s, conAgHabilitado = %2$s, conFechaMod = current_timestamp() where conId = 1", fa, ag);
        super.executeUpdate(hql);
    }
}
