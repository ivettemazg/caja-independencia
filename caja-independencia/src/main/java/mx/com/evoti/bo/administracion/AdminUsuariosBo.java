package mx.com.evoti.bo.administracion;

import java.io.Serializable;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.UsuariosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.Usuarios;

public class AdminUsuariosBo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UsuariosDao usuariosDao;

    public AdminUsuariosBo() {
        this.usuariosDao = new UsuariosDao();
    }

    public Usuarios getById(Integer id) throws BusinessException {
        try {
            return usuariosDao.getUsrById(id);
        } catch (IntegracionException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

    public void updateUsuario(Usuarios usuario) throws BusinessException {
        try {
            usuariosDao.updtUsuario(usuario);
        } catch (IntegracionException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

    public void updatePassword(Integer idUsuario, String nuevoPassword) throws BusinessException {
        try {
            // Reutilizamos updtUsuario para limitar cambios: obtenemos el pojo y actualizamos password.
            Usuarios u = usuariosDao.getUsrById(idUsuario);
            u.setUsuPassword(nuevoPassword);
            usuariosDao.updtUsuario(u);
        } catch (IntegracionException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }
}

