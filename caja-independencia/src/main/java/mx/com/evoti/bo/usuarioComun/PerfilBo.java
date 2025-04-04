/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.usuarioComun;


import org.slf4j.LoggerFactory;

import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.UsuariosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.Usuarios;

/**
 *
 * @author Venette
 */
public class PerfilBo {

    private static final long serialVersionUID = 1L; 
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PerfilBo.class);

    private final UsuariosDao dao;

    public PerfilBo() {
        dao = new UsuariosDao();
    }

    /**
     * Obtiene el usuario para la pantalla Perfil
     * @param idUsuario
     * @return
     * @throws BusinessException 
     */
    public Usuarios getUsuarioById(Integer idUsuario) throws BusinessException {
        try {
           return dao.obtenerUsuario(idUsuario);
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);

        }
    }

    public void updtUsuario(Usuarios usuario) throws BusinessException {
        try {
            dao.updtUsuario(usuario);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
     /**
      * Actualiza el campo usuprimeravez de un usuario
      * @param idUsuario
      * @param usuPrimeraVez
      * @throws BusinessException 
      */
    public void updtUsuPrimeraVez(Integer idUsuario, int usuPrimeraVez) throws BusinessException{
         try {
             dao.updtUsrPrimeraVez(idUsuario,usuPrimeraVez);
         } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
         }
    }
    

    public UsuarioDto getUsuarioActualizado(Integer idUsuario) throws BusinessException{
        try {
            return dao.getUsrActualizado(idUsuario);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
}
