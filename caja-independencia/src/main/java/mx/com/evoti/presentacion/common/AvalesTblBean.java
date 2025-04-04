/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.common;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.AvalesSolicitudBo;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.NavigationBean;
import mx.com.evoti.util.Constantes;
import org.primefaces.context.RequestContext;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "avalBean")
@ViewScoped
public class AvalesTblBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -588659309152755701L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AvalesTblBean.class);

    private BigInteger idSolicitud;
    private Integer idCredito;
    private Boolean avalesAutorizados;
    private List<UsuarioDto> avales;
    private UsuarioDto avalSeleccionados;
    private String motivoRechazo;
    private UsuarioDto usuarioGlobal;
    private Usuarios usuarioSolicitud;

    private AvalesSolicitudBo avalSolBo;
    private DetalleSolicitudBo detSolBo;

    //El rederer para cuando se está accediendo desde el detalle de solicitud
    private Boolean rdrSolicitud;
    //El rederer para cuando se está accediendo desde el detalle de credito
    private Boolean rdrCredito;

    
       @ManagedProperty("#{navigationController}")
     private NavigationBean navigationBean;
    
    public AvalesTblBean() {

    }

    public void init() {
        try {
            /**
             * Obtiene el usuario que se encuentra logueado
             */
              this.sesionGlobal = super.getSession();
                usuarioGlobal = (UsuarioDto) super.sesionGlobal.getAttribute("usuario");
                
            if (idSolicitud != null) {
                this.rdrSolicitud = Boolean.TRUE;
                this.rdrCredito = Boolean.FALSE;
                this.avalSolBo = new AvalesSolicitudBo();

                avales = getAvalesByIdSolicitud(idSolicitud);
                avalesAutorizados = Boolean.FALSE;

            }

        } catch (BusinessException ex) {
            LOGGER.error("Error al consultar los avales", ex);
        }
    }

    
    public List<UsuarioDto> getAvalesByIdSolicitud(BigInteger idSolicitud) throws BusinessException{
        return this.avalSolBo.getAvalesByIdSolicitud(idSolicitud);
    }
    
    
    public void aprobarAval() {
        LOGGER.info("Dentro de aprobar aval");
        if (validaAvalesSeleccionados()) {
            try {
                LOGGER.info("Aprueba aval");

                avalSolBo.updtAvalEstatus(avalSeleccionados, Constantes.SOLAVA_APROBADO_I);
            } catch (BusinessException ex) {
                LOGGER.error("Error al actualizar el estatus del aval", ex);
            }
        }
    }

    public void abrirDialogRechazo() {
        LOGGER.info("Dentro de rechazar aval");
        if (validaAvalesSeleccionados()) {
            abreDialogRechazo();
        }
    }

    public void rechazaAval() {
        try {
            LOGGER.info("Rechaza aval");
            avalSolBo.updtAvalEstatus(avalSeleccionados, Constantes.SOLAVA_RECHAZADO_I);
            
            detSolBo = new DetalleSolicitudBo();
            /**Se rechaza la solicitud*/
            detSolBo.updtEstatusSolicitud(idSolicitud, 9,null);
            Solicitudes sol =new Solicitudes();
            sol.setSolMotivoRechazo(motivoRechazo);
            
            enviaCorreoNotificacion(sol, usuarioSolicitud, 2);
            
            /*Inserta en bitacora el motivo de rechazo de la solicitud*/
            detSolBo.insertaMotivoRechazo(idSolicitud.longValue(), Constantes.BIT_SOL_RECHAZADA,usuarioGlobal.getId(),avalSeleccionados.getIdSolicitud().longValue(),motivoRechazo);
            
            navigationBean.goToValidaSolicitudes();
            
        } catch (BusinessException ex) {
            LOGGER.error("Error al actualizar el estatus del aval", ex);
        }
    }

    /**
     * Valida que haya un aval seleccionado
     *
     * @return
     */
    public boolean validaAvalesSeleccionados() {
        if (avalSeleccionados == null) {
            super.muestraMensajeError("Debe seleccionar un aval para continuar","", "msjAval");
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;

        }
    }

    private void abreDialogRechazo() {
        motivoRechazo="";
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('dlgMotivRechazoAvalW').show()");
    }

    /**
     * Envía correo de que la solicitud se aceptó o se rechazó
     * @param solicitud
     * @param usuario
     * @param tipoCorreo - 2 rechazada, 1 aceptada
     */
    public void enviaCorreoNotificacion(Solicitudes solicitud, Usuarios usuario, int tipoCorreo){
        detSolBo.enviaCorreoValSol(usuario, solicitud.getSolMontoSolicitado(),solicitud.getSolCatorcenas(),solicitud.getSolPagoCredito(), tipoCorreo, solicitud.getSolMotivoRechazo());
    }
    
    /**
     * @return the idSolicitud
     */
    public BigInteger getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * @param idSolicitud the idSolicitud to set
     */
    public void setIdSolicitud(BigInteger idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /**
     * @return the idCredito
     */
    public Integer getIdCredito() {
        return idCredito;
    }

    /**
     * @param idCredito the idCredito to set
     */
    public void setIdCredito(Integer idCredito) {
        this.idCredito = idCredito;
    }

    /**
     * @return the avales
     */
    public List<UsuarioDto> getAvales() {
        return avales;
    }

    /**
     * @param avales the avales to set
     */
    public void setAvales(List<UsuarioDto> avales) {
        this.avales = avales;
    }

    /**
     * @return the avalesAutorizados
     */
    public Boolean getAvalesAutorizados() {
        return avalesAutorizados;
    }

    /**
     * @param avalesAutorizados the avalesAutorizados to set
     */
    public void setAvalesAutorizados(Boolean avalesAutorizados) {
        this.avalesAutorizados = avalesAutorizados;
    }

    /**
     * @return the rdrSolicitud
     */
    public Boolean getRdrSolicitud() {
        return rdrSolicitud;
    }

    /**
     * @param rdrSolicitud the rdrSolicitud to set
     */
    public void setRdrSolicitud(Boolean rdrSolicitud) {
        this.rdrSolicitud = rdrSolicitud;
    }

    /**
     * @return the rdrCredito
     */
    public Boolean getRdrCredito() {
        return rdrCredito;
    }

    /**
     * @param rdrCredito the rdrCredito to set
     */
    public void setRdrCredito(Boolean rdrCredito) {
        this.rdrCredito = rdrCredito;
    }

    /**
     * @return the avalSeleccionados
     */
    public UsuarioDto getAvalSeleccionados() {
        return avalSeleccionados;
    }

    /**
     * @param avalSeleccionados the avalSeleccionados to set
     */
    public void setAvalSeleccionados(UsuarioDto avalSeleccionados) {
        this.avalSeleccionados = avalSeleccionados;
    }

    /**
     * @return the motivoRechazo
     */
    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    /**
     * @param motivoRechazo the motivoRechazo to set
     */
    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    /**
     * @return the usuarioSolicitud
     */
    public Usuarios getUsuarioSolicitud() {
        return usuarioSolicitud;
    }

    /**
     * @param usuarioSolicitud the usuarioSolicitud to set
     */
    public void setUsuarioSolicitud(Usuarios usuarioSolicitud) {
        this.usuarioSolicitud = usuarioSolicitud;
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
