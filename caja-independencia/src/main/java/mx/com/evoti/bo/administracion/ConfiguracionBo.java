package mx.com.evoti.bo.administracion;

import java.io.Serializable;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.ConfiguracionDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.Configuracion;

public class ConfiguracionBo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ConfiguracionDao dao;

    public ConfiguracionBo() {
        dao = new ConfiguracionDao();
    }

    public Configuracion getConfig() throws BusinessException {
        try {
            return dao.getOrCreateConfig();
        } catch (IntegracionException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

    public void updateFlags(boolean faEnabled, boolean agEnabled) throws BusinessException {
        try {
            dao.updateFlags(faEnabled ? 1 : 0, agEnabled ? 1 : 0);
        } catch (IntegracionException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }
}

