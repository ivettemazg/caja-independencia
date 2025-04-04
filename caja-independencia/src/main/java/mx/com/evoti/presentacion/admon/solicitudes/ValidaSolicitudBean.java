/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon.solicitudes;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.administrador.solicitud.ValidaSolicitudBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.SolicitudDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.NavigationBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "valSolBean" )
@ViewScoped
public class ValidaSolicitudBean extends BaseBean implements Serializable{
    
    private static final long serialVersionUID = 526519261583177653L;
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ValidaSolicitudBean.class);
     
     private final ValidaSolicitudBo valSolBo;
     private List<SolicitudDto> solsPendientes;
     
     @ManagedProperty("#{navigationController}")
    private NavigationBean navigationBean;
    

    public ValidaSolicitudBean() {
        
        this.valSolBo = new ValidaSolicitudBo();
    }
     
    
    public void init(){
        if(super.validateUser()){
        
            this.sesionGlobal = this.getSession();

            try {
                solsPendientes = valSolBo.getSolsFValidando();
            } catch (BusinessException ex) {
               LOGGER.error(ex.getMessage(), ex);
            }
        }else{
            LOGGER.error("Se perdió la sesión");
        }
    }
    
    public void abreDetalle(SolicitudDto solicitud){
        LOGGER.info("Dentro de abreDetalle");
        super.sesionGlobal.setAttribute("idSolPendiente", solicitud.getSolId());
        super.sesionGlobal.setAttribute("solUsuId", solicitud.getUsuId());
        
        
        navigationBean.goToValSolDetalle();
        
        
    }

    /**
     * @return the solsPendientes
     */
    public List<SolicitudDto> getSolsPendientes() {
        return solsPendientes;
    }

    /**
     * @param solsPendientes the solsPendientes to set
     */
    public void setSolsPendientes(List<SolicitudDto> solsPendientes) {
        this.solsPendientes = solsPendientes;
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
