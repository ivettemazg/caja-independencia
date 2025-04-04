/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion;

import java.io.IOException;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.util.Util;
import org.primefaces.context.RequestContext;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
@ManagedBean
@SessionScoped
public class BaseBean{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BaseBean.class);
    public HttpSession sesionGlobal;
    public UsuarioDto usuario;
    

    /**
     * Creates a new instance of BaseBean
     */
    public BaseBean() {
    }
    
    public HttpSession getSession(){
         HttpSession session = ( HttpSession ) FacesContext.getCurrentInstance().getExternalContext().getSession( true );
         
         return session;
    }
    
     public void logout() throws IOException {
       invalidateSession();
        
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        ec.redirect(ec.getRequestContextPath() + "/login.xhtml");
        
    }
     
     public void invalidateSession(){
          HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
        session.invalidate();
     }
     
     
    public void muestraMensajeExito(String mensaje, String detalle, String sfor) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, detalle);
        FacesContext.getCurrentInstance().addMessage(sfor, msg);
    }

    public void muestraMensajeError(String error,  String detalle, String for_) {
        FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, error, detalle);
        FacesContext.getCurrentInstance().addMessage(for_, errorMsg);
    }
    
    public void muestraMensajeGen(String error,  String detalle, String for_,Severity sev) {
        FacesMessage errorMsg = new FacesMessage(sev, error, detalle);
        FacesContext.getCurrentInstance().addMessage(for_, errorMsg);
    }
    
    
    public void muestraMensajeDialog(String message,  String detalle, Severity sev) {
        FacesMessage fmsg = new FacesMessage(sev, message, detalle);
        RequestContext.getCurrentInstance().showMessageInDialog(fmsg);
    }
    
    
     
    
    /**
     * Valida que una fecha dada sea catorcena
     * @return 
     */
      protected boolean validaSiEsCatorcena(Date fecha) {
        LOGGER.info("Dentro de validasiescatorcena");
        try {
            if (!Util.validaSiEsCatorcena(fecha)) {
                String error = "La fecha que ha seleccionado no es catorcena.";
                muestraMensajeError(error, null,null);
                return Boolean.FALSE;
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex.getCause());
        }
        return Boolean.TRUE;
    }
      
      /**
       * Metodo que determina si un dialogo se cierra o se abre
       * @param jsHideDlg 
       */
      protected void hideShowDlg(String jsHideDlg){
          RequestContext context = RequestContext.getCurrentInstance();
            context.execute(jsHideDlg);
      }
     
      protected void updtComponent(String component){
          RequestContext.getCurrentInstance().update(component);
      }
      
      public UsuarioDto getUsuarioSesion(){
          return (UsuarioDto) getSession().getAttribute("usuario");
      }

    public String redirectErrorPage() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true"; 
    }
      
    /**
     * Método que valida que el objeto usuario exista en sesion para permitir
     * @return 
     */
    public boolean validateUser(){
        this.sesionGlobal = getSession();
        usuario = (UsuarioDto) getSession().getAttribute("usuario");
         
         if(usuario == null){
            hideShowDlg("PF('dlgSessionLost').show()");  
            LOGGER.info("Se perdio la informacion de sesion de usuario en DashboardBean");
            return false;
        }
         return true;
    }
    
}
