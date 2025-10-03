/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import mx.com.evoti.dto.UsuarioDto;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ivette
 */
@ManagedBean(name = "navigationController", eager = true)
@SessionScoped
public class NavigationBean extends BaseBean implements Serializable{
    private static final long serialVersionUID = 9216692439496179076L;
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NavigationBean.class);
    
    private UsuarioDto usuario;
    
    
  
     public void goToHomeScreen(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                        ec.redirect(ec.getRequestContextPath() + "/dashboard.xhtml");
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     public void goToDetalleSolicitud(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/usuariocomun/detalle-solicitud.xhtml");
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     public void goToSolicitudCredito(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/usuariocomun/solicitud-credito.xhtml");
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     public void goToPerfil(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/usuariocomun/mi-perfil.xhtml");
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     public void goToMisAhorros(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/usuariocomun/mis-ahorros.xhtml");
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     public void goToMisSolicitudes(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/usuariocomun/mis-solicitudes.xhtml");
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     
     public void goToMisCreditos(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/usuariocomun/mis-creditos.xhtml");
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
    
     public void goToValidaSolicitudes(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/admonsolicitudes/validar-solicitudes.xhtml");
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     public void goToValSolDetalle(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/admonsolicitudes/valsol-detalle.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToCargaArchivos(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/cargapagosaportaciones/carga-archivo.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToAplicaCambios(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/cargapagosaportaciones/cambios-empresa.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     public void goToAlCamPends(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/cargapagosaportaciones/cam-al-pendts.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToAplicaPagos(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/cargapagosaportaciones/aplica-pagos.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     
     public void goToFondeos(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/admonsolicitudes/fondeo-principal.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToSeguimientoFondeo(){
        try {
             ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/admonsolicitudes/f-docs-seguimiento.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToBajaUsuario(){
        try {
             ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/finiquito/baja-empleado.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToHistBajas(){
        try {
             ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/finiquito/historial-bajas.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToBajasPendts(){
        try {
             ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/finiquito/bajas-pendientes.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToAhorrosXDevolver(){
        try {
             ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/finiquito/ahorrosxdevolver.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     public void goToFiniquito(){
        try {
             ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/finiquito/finiquito.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
    
     public void goToAjusteBanco(){
        try {
             ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/bancos/ajuste-banco.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
     
    public void goToHistorialSolicitudes() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/solicitudes/historial.xhtml");

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void goToProcesoRendimiento() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/rendimiento/proceso.xhtml");

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void goToReporteCreditos() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/creditos/reporte.xhtml");

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void goToReporteMorosos() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/creditos/reporte-morosos.xhtml");

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    
     public void goToDescuentoNomina() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/creditos/descuentoNomina.xhtml");

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
        public void goToReporteAportaciones() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/cargapagosaportaciones/reporteAportaciones.xhtml");

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void goToDescuentoNominaOtros() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/creditos/descuentoNominaFAAG.xhtml");

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void goToBitacoraMovimientos(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/administracion/reporte-bitacora.xhtml");
            
        } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
   }
    
     public void goToAdminPanel(){
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/navegacion/administracion/panel.xhtml");
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
   }
    
    /**
     * @return the usuario
     */
    public UsuarioDto getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(UsuarioDto usuario) {
        this.usuario = usuario;
    }
    
    
}
