/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.administrador.finiquito;

import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.UsuariosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.finiquito.UsuarioBajaDto;

/**
 *
 * @author Venette
 */
public class HistorialBajasBo {
    
    private UsuariosDao usuDao;

    public HistorialBajasBo() {
        this.usuDao = new UsuariosDao();
    }
    
    /**
     * Obtiene el historial de bajas de usuarios
     * @param estatus
     * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException 
     */
    public List<UsuarioBajaDto> getBajaUsuarios(Integer estatus) throws BusinessException{
        try {
            return usuDao.getUsrsBaja(estatus);
        } catch (IntegracionException ex) {
           throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
     public List<UsuarioBajaDto> getUsuariosBajaPendiente(Integer estatus) throws BusinessException{
        try {
            return usuDao.getUsrsBajaPendiente(estatus);
        } catch (IntegracionException ex) {
           throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
}
