/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import mx.com.evoti.dto.UsuarioDto;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
@ManagedBean(name = "dashboardBean")
@SessionScoped
public class DashboardBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DashboardBean.class);
    private static final long serialVersionUID = 2902516767485526997L;

    private String nickname;
   
    public DashboardBean(){
    }
    
     @ManagedProperty("#{navigationController}")
    private NavigationBean navigationBean;
   
     public void validaUsuario(){
        super.sesionGlobal = super.getSession();
        usuario = (UsuarioDto) super.getSession().getAttribute("usuario");
         
         if(usuario != null){
         
            nickname = usuario.getNombre();
            navigationBean.setUsuario(usuario);
            
        }else{
           super.hideShowDlg("PF('dlgSessionLost').show()");  
           LOGGER.info("Se perdio la informacion de sesion de usuario en DashboardBean");
        }
    }


    /**
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return the navigationBean
     */
    public NavigationBean getNavigationBean() {
        return navigationBean;
    }

    /**
     * @param navigationBean the navigationBean to set
     */
    public void setNavigationBean(NavigationBean navigationBean) {
        this.navigationBean = navigationBean;
    }

   
}
