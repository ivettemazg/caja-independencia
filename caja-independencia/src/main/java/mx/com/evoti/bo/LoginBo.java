/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.util.List;
import mx.com.evoti.dao.LoginDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.UsuarioDto;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
public class LoginBo implements Serializable {

    private static final long serialVersionUID = 1543689251912959474L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoginBo.class);
    
    private final LoginDao dao;

    public LoginBo() {
        dao = new LoginDao();
    }

    /**
     * Regresa la lista de usuarios que coinciden con el numero de empleado dado
     * @param nickname
     * @return 
     */
    public List<UsuarioDto> login(String nickname) {

        List<UsuarioDto> login;
        try {
            login = dao.login(nickname);

                        
            return login;

        } catch (IntegracionException ex) {
           LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

}
