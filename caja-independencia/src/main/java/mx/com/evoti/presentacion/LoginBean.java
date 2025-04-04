/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import mx.com.evoti.bo.LoginBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.util.Constantes;

/**
 *
 * @author Venus
 */
@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -4358712411072225408L;

    private String nickname;
    private String pass;
    private LoginBo bo;

    public LoginBean() {
        bo = new LoginBo();
    }

    @PostConstruct
    public void init() {
        this.sesionGlobal = this.getSession();

    }

    public void validaLogin() {
        System.out.println("DENTRO DE VALIDA LOGIN");
        List<UsuarioDto> usrs = bo.login(nickname);

        if (!usrs.isEmpty()) {

            for (UsuarioDto userDto : usrs) {

                if (userDto.getPassword().equals(pass)) {

                    if (userDto.getFechaBaja()!=null) {
                        
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        Calendar c = Calendar.getInstance();
                        c.setTime( userDto.getFechaBaja());
                     
                        c.add(Calendar.MONTH, 1);
                        
                        Date fechaBajaMesDespues = c.getTime();
                        Date today = new Date();
                        System.out.println(dateFormat.format(fechaBajaMesDespues));
                        System.out.println(dateFormat.format(today));
                        
                        if(today.before(fechaBajaMesDespues)){
                            iniciaSesion(userDto);
                        }else{
                            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage
                            (FacesMessage.SEVERITY_ERROR, String.format(Constantes.MSJ_ERR_LOGIN_USR_BAJA, dateFormat.format(userDto.getFechaBaja())), ""));
                        }
                      
                    } else {

                       iniciaSesion(userDto);
                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, Constantes.MSJ_LOGIN_PASS_ERR, ""));
                }
            }

        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, Constantes.MSJ_LOGIN_USR_NO_EXST, ""));
        }

    }
    
    public void iniciaSesion(UsuarioDto userDto){
            try {

                            if (userDto.getNombre() != null && userDto.getPaterno() != null) {
                                userDto.setNombreCompleto(userDto.getNombre() + " " + userDto.getPaterno());
                            } else if (userDto.getNombre() != null && userDto.getPaterno() != null && userDto.getMaterno() != null) {
                                userDto.setNombreCompleto(userDto.getNombre() + " " + userDto.getPaterno() + " " + userDto.getMaterno());
                            }

                            FacesContext context = FacesContext.getCurrentInstance();
                            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
                            session.setAttribute("usuario", userDto);

                            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                            ec.redirect(ec.getRequestContextPath() + "/dashboard.xhtml");

                        } catch (IOException ex) {
                            Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
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
     * @return the pass
     */
    public String getPass() {
        return pass;
    }

    /**
     * @param pass the pass to set
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

}
