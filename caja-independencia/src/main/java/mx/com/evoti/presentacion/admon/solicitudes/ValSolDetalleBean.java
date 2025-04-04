/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon.solicitudes;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.bo.usuarioComun.PerfilBo;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.NavigationBean;
import mx.com.evoti.presentacion.admon.AmortizacionRepEmpBean;
import mx.com.evoti.presentacion.admon.SolCreditosActivosBean;
import mx.com.evoti.presentacion.admon.ValSolCreditosHistorialBean;
import mx.com.evoti.presentacion.common.AvalesTblBean;
import mx.com.evoti.presentacion.usuariocomun.DoctosSolBean;
import mx.com.evoti.presentacion.usuariocomun.PerfilBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "valSolDetBean" )
@ViewScoped
public class ValSolDetalleBean extends BaseBean implements Serializable{
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ValSolDetalleBean.class);
    private static final long serialVersionUID = 5128207593287176615L;
    
    private BigInteger idSolicitud;
    private Integer usuId;
    private Solicitudes solicitud;
    private String motivoRechazo;
    private UsuarioDto userDto;
    private Usuarios usuarioSolicitud;
    
    private DetalleSolicitudBo detSolBo;
    private final PerfilBo perfilBo;
    
    private List<MovimientosDto> lstTotalesAhorro;
    private MovimientosBo movsBo;
    private Double totalAhorro;
    
    @ManagedProperty("#{valSolPpalBean}")
    private ValSolPrincipalBean valSolPpalBean;
     @ManagedProperty("#{perfilBean}")
    private PerfilBean perfilBean;
     @ManagedProperty("#{avalBean}")
    private AvalesTblBean avalesBean;
     @ManagedProperty("#{credsActivBean}")
    private SolCreditosActivosBean credsActivBean; 
      @ManagedProperty("#{valsolCredHistorial}")
    private ValSolCreditosHistorialBean valsolCredHistorial; 
     @ManagedProperty("#{amoRepEmpBean}")
    private AmortizacionRepEmpBean amoRepBean;
     @ManagedProperty("#{doctosSolBean}")
    private DoctosSolBean doctosBean;    
     @ManagedProperty("#{navigationController}")
     private NavigationBean navigationBean;

    public ValSolDetalleBean(){
        detSolBo =  new DetalleSolicitudBo();
        perfilBo =  new PerfilBo();
    }
    
    public void init(){
      this.sesionGlobal = super.getSession();
       idSolicitud = (BigInteger) sesionGlobal.getAttribute("idSolPendiente");
       /**El usuario que solicitó el crédito*/
       usuId = (Integer) sesionGlobal.getAttribute("solUsuId");
       /**El usuario que se ha logueado*/
       userDto = (UsuarioDto) super.sesionGlobal.getAttribute("usuario");
       
        try {   
            solicitud = detSolBo.getSolById(idSolicitud);
            usuarioSolicitud = perfilBo.getUsuarioById(usuId);
        } catch (BusinessException ex) {
            LOGGER.error("Error al obtener la solicitud", ex);
        }
       
        
       valSolPpalBean.setIdSolicitud(idSolicitud);
       perfilBean.setUsuarioId(usuId);
       perfilBean.setOrigen(4);
       
       avalesBean.setIdSolicitud(idSolicitud);
       avalesBean.setUsuarioSolicitud(usuarioSolicitud);
       doctosBean.setSolicitud(solicitud);
       doctosBean.setUsuario(usuarioSolicitud);
       credsActivBean.setUsuario(usuarioSolicitud);
       valsolCredHistorial.setUsuario(usuarioSolicitud);
    
       obtenerMovimientos();
    }
    
    /**
     * Proceso para aprobar la solicitud
     */
    public void aprobarSolicitud(){
        try {
            solicitud = detSolBo.getSolById(idSolicitud);
            
            List<Imagenes> imagenes = doctosBean.getImgsXSol(idSolicitud);
            List<UsuarioDto> avales = avalesBean.getAvalesByIdSolicitud(idSolicitud);
            
            Boolean imaRechazo = Boolean.FALSE;
            Boolean avaRechazo = Boolean.FALSE;
            Boolean imaValidando = Boolean.FALSE;
            Boolean avaValidando = Boolean.FALSE;
            String msjError = "";
            
            for(Imagenes ima:imagenes){
                if(ima.getImaEstatus()==4){
                    imaRechazo = Boolean.TRUE;
                }else if(ima.getImaEstatus()==2){
                    imaValidando = Boolean.TRUE;
                }
            }
            
            for(UsuarioDto aval:avales){
                if(aval.getEstatusAvalInt()==4){
                      avaRechazo = Boolean.TRUE;
                }else if(aval.getEstatusAvalInt()==2){
                    avaValidando = Boolean.TRUE;
                }
            }
            
            if(imaRechazo){
               msjError += " Hay documentos rechazados";
            }
            
            if(avaRechazo){
               msjError += " Hay avales rechazados";
            }

            if(imaValidando){
               msjError += " Hay documentos sin aprobar";
            }

            if(avaValidando){
               msjError += " Hay avales sin aprobar";
            }
                
           
            if(!msjError.isEmpty()){
                super.muestraMensajeError("No puede autorizar la solicitud ", msjError, null);
            }else{
                
            //ACTUALIZAR ESTATUS SOLICITUD
                detSolBo.updtEstatusSolicitud(idSolicitud, 3,null);
                enviaCorreoNotificacion(solicitud, perfilBean.getUsuario(),1);
                super.muestraMensajeExito("La solicitud fue aprobada", "", "msjAutorizacion");
                super.hideShowDlg("PF('dlgMessageAprovW').show()");
            }
            
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void goToValidaSolicitudes(){
         navigationBean.goToValidaSolicitudes();
    }
    
    
     public void abrirDialogMotivRechazo() {
        LOGGER.info("Dentro de abrirDialogMotivRechazo");
        
        
        super.hideShowDlg("PF('dlgMotivRechazoSol').show()");
        
    }
    
    public void rechazarSolicitud(){
        try {
            
            
            detSolBo.updtEstatusSolicitud(idSolicitud, 7,null);
            solicitud.setSolMotivoRechazo(motivoRechazo);
            enviaCorreoNotificacion(solicitud, perfilBean.getUsuario(), 2);
            /*Inserta en bitacora el motivo de rechazo de la solicitud*/
            detSolBo.insertaMotivoRechazo(solicitud.getSolId(), Constantes.BIT_SOL_RECHAZADA,userDto.getId(),null,motivoRechazo);
            navigationBean.goToValidaSolicitudes();
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    
    /**
     * Envía correo de que la solicitud se aceptó o se rechazó
     * @param solicitud
     * @param usuario
     * @param tipoCorreo - 2 rechazada, 1 aceptada
     */
    public void enviaCorreoNotificacion(Solicitudes solicitud, Usuarios usuario, int tipoCorreo){
        detSolBo.enviaCorreoValSol(usuario, solicitud.getSolMontoSolicitado(), solicitud.getSolCatorcenas(), solicitud.getSolPagoCredito(), tipoCorreo, solicitud.getSolMotivoRechazo());
    }
    
    
    public void obtenerMovimientos(){
          try {
              movsBo = new MovimientosBo();
            totalAhorro = 0.0;
            lstTotalesAhorro = movsBo.getAhorrosTotales(usuId);

            lstTotalesAhorro.forEach(x -> {

                totalAhorro = Util.round(x.getTotalMovimiento() + totalAhorro);

            });

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        
    }
    
    
      public void obtieneAmortizacion() {
        DetalleCreditoDto credito = valsolCredHistorial.getCreditoSelected();
        amoRepBean.setUsuario(usuarioSolicitud);
        amoRepBean.setCredito(credito);

        amoRepBean.obtieneAmortizacion();

        super.hideShowDlg("PF('dlgAmortizacionW').show()");
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
     * @return the valSolPpalBean
     */
    public ValSolPrincipalBean getValSolPpalBean() {
        return valSolPpalBean;
    }

    /**
     * @param valSolPpalBean the valSolPpalBean to set
     */
    public void setValSolPpalBean(ValSolPrincipalBean valSolPpalBean) {
        this.valSolPpalBean = valSolPpalBean;
    }

    /**
     * @return the perfilBean
     */
    public PerfilBean getPerfilBean() {
        return perfilBean;
    }

    /**
     * @param perfilBean the perfilBean to set
     */
    public void setPerfilBean(PerfilBean perfilBean) {
        this.perfilBean = perfilBean;
    }

    /**
     * @return the avalesBean
     */
    public AvalesTblBean getAvalesBean() {
        return avalesBean;
    }

    /**
     * @param avalesBean the avalesBean to set
     */
    public void setAvalesBean(AvalesTblBean avalesBean) {
        this.avalesBean = avalesBean;
    }

    /**
     * @return the doctosBean
     */
    public DoctosSolBean getDoctosBean() {
        return doctosBean;
    }

    /**
     * @param doctosBean the doctosBean to set
     */
    public void setDoctosBean(DoctosSolBean doctosBean) {
        this.doctosBean = doctosBean;
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
     * @return the userDto
     */
    public UsuarioDto getUserDto() {
        return userDto;
    }

    /**
     * @param userDto the userDto to set
     */
    public void setUserDto(UsuarioDto userDto) {
        this.userDto = userDto;
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
     * @return the credsActivBean
     */
    public SolCreditosActivosBean getCredsActivBean() {
        return credsActivBean;
    }

    /**
     * @param credsActivBean the credsActivBean to set
     */
    public void setCredsActivBean(SolCreditosActivosBean credsActivBean) {
        this.credsActivBean = credsActivBean;
    }

    /**
     * @return the totalAhorro
     */
    public Double getTotalAhorro() {
        return totalAhorro;
    }

    /**
     * @param totalAhorro the totalAhorro to set
     */
    public void setTotalAhorro(Double totalAhorro) {
        this.totalAhorro = totalAhorro;
    }

    /**
     * @return the lstTotalesAhorro
     */
    public List<MovimientosDto> getLstTotalesAhorro() {
        return lstTotalesAhorro;
    }

    /**
     * @param lstTotalesAhorro the lstTotalesAhorro to set
     */
    public void setLstTotalesAhorro(List<MovimientosDto> lstTotalesAhorro) {
        this.lstTotalesAhorro = lstTotalesAhorro;
    }

    /**
     * @return the valsolCredHistorial
     */
    public ValSolCreditosHistorialBean getValsolCredHistorial() {
        return valsolCredHistorial;
    }

    /**
     * @param valsolCredHistorial the valsolCredHistorial to set
     */
    public void setValsolCredHistorial(ValSolCreditosHistorialBean valsolCredHistorial) {
        this.valsolCredHistorial = valsolCredHistorial;
    }

    /**
     * @return the amoRepBean
     */
    public AmortizacionRepEmpBean getAmoRepBean() {
        return amoRepBean;
    }

    /**
     * @param amoRepBean the amoRepBean to set
     */
    public void setAmoRepBean(AmortizacionRepEmpBean amoRepBean) {
        this.amoRepBean = amoRepBean;
    }
}