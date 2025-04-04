/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.usuariocomun;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.AvalesSolicitudBo;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.SolicitudAvales;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.presentacion.BaseBean;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "avaSolBean")
@ViewScoped
public class SolicitudAvalesBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SolicitudAvalesBean.class);
    private static final long serialVersionUID = 8565857776862491869L;

    private final AvalesSolicitudBo avaSolBo;
    private final DetalleSolicitudBo detSolBo;

    private Solicitudes solicitud;
    private List<UsuarioDto> avalesDto;
    private List<SolicitudAvales> avalesSolicitud;
    private Integer noAvales;

    private UsuarioDto usuario;
    private Boolean tblEditable;

    public SolicitudAvalesBean() {
        avaSolBo = new AvalesSolicitudBo();
        detSolBo = new DetalleSolicitudBo();
    }

    public void init() {

        try {
            /**
             * Se obtiene la variable de avalesSolicitud directamente de la base
             * de datos
             */

            Long solId = solicitud.getSolId();
            BigInteger bgSolId = BigInteger.valueOf(solId);
            avalesSolicitud = avaSolBo.getAvalesXSolicitud(bgSolId);

            avalesDto = avaSolBo.getInfoAvales(avalesSolicitud);
            
            noAvales = avalesDto.size();
            
            /**Cuando la solicitud está en estatus creada (1) o en revisión (9)
             debe mostrarse la columna de editable*/
            if(solicitud.getSolicitudEstatus().getSolEstId() == 1
                    || solicitud.getSolicitudEstatus().getSolEstId() == 9){
                tblEditable = Boolean.TRUE;
            }else{
                tblEditable = Boolean.FALSE;
                
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    public void onEditAval(RowEditEvent event) {
        LOGGER.debug("Dentro de onEdit");

        UsuarioDto avalDto = (UsuarioDto) event.getObject();

        if (avalDto.getCveEmpleado() != null) {

            Boolean isValidAv = validaAvales(avalDto);
            
                        
            /**Si pasa las validaciones*/
            if(isValidAv){
                UsuarioDto avalEncontrado = null;
                try {
                   avalEncontrado = avaSolBo.getAvalXCveEmpleado(avalDto.getCveEmpleado(), usuario.getEmpresaId());
                } catch (BusinessException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }

                if (avalEncontrado != null) {
                    /**
                     * Si el nombre de usuario no esta vacio ni es nulo y la
                     * fecha de baja es nula
                     */
                    if ((avalEncontrado.getNombre() != null || !avalEncontrado.getNombre().isEmpty()) && avalEncontrado.getFechaBaja() == null) {

                        avalDto.setNombreCompleto(avalEncontrado.getNombre().toUpperCase() + " "
                                + avalEncontrado.getPaterno().toUpperCase() + " " + avalEncontrado.getMaterno().toUpperCase());
                        avalDto.setId(avalEncontrado.getId());
                        avalDto.setCveEmpleado(avalEncontrado.getCveEmpleado());

                        //Se actualiza en la base de datos el estatus del aval
                        
                        uptdAvalesDB(avalDto, 2);
                        
                        avalDto.setTblEditable(Boolean.FALSE);
                        init();
                    } else {
                        muestraMensajeAvalInvalido();
                    }
                } else {

                    muestraMensajeAvalInvalido();
                }
            }
        }
    }
    
    /**
     * Valida que el usuario no se ponga a sí mismo como aval y que no repita un mismo aval varias veces
     * @param avalDto
     * @return 
     */
    public boolean validaAvales(UsuarioDto avalDto){
          //Valida que la clave de usuario no sea la misma que quien solicita el credito
            if (avalDto.getCveEmpleado().equals(usuario.getCveEmpleado())) {
                avalDto.setCveEmpleado(null);
                avalDto.setNombreCompleto(null);
                
                FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "No puede poner su número de empleado como aval", "");
                FacesContext.getCurrentInstance().addMessage("validAvales", errorMsg);
                return Boolean.FALSE;
            } else {
                /**
                 * Valida que no meta el mismo aval 2 veces
                 */
                for (UsuarioDto avalAux : avalesDto) {
                    if (Objects.equals(avalAux.getCveEmpleado(), avalDto.getCveEmpleado())
                            && !Objects.equals(avalAux.getIdSolicitudAval(), avalDto.getIdSolicitudAval())) {
                        avalDto.setCveEmpleado(null);
                        avalDto.setNombreCompleto(null);
                        FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "No puede repetir el aval, por favor capture una clave diferente", "");
                        FacesContext.getCurrentInstance().addMessage("validAvales", errorMsg);
                        return Boolean.FALSE;
                    }
                }
                return Boolean.TRUE;
            }
    }
    

    public void uptdAvalesDB(UsuarioDto aval, int estatus) {
        try {
            //Si el estatus de la solicitud no es "dado de alta" es un id que se puede actualizar

            avaSolBo.updtAval(aval, estatus);

            String message =  detSolBo.updtSolicitudEstatus(solicitud.getSolId(), usuario.getNombreCompleto(), usuario.getCorreo());
            
             if (message != null) {
                super.muestraMensajeDialog(message, "",FacesMessage.SEVERITY_INFO);
            }
            
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al aprobar la solicitud", "", "growl");
            LOGGER.error(ex.getMessage(), ex);
        }

    }
    
    
       public void muestraMensajeAvalInvalido() {
        FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "El número de aval que capturó no existe o ya está dado de baja de la caja, favor de verificarlo", "");
        FacesContext.getCurrentInstance().addMessage("validAvales", errorMsg);
    }
    
     public void muestraMensajeAvalNulo() {
        FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Uno de los campos de los avales está vacío, favor de introducir una clave de empleado válida para el aval", "");
        FacesContext.getCurrentInstance().addMessage("validAvales", errorMsg);
    }
    
    
    public void onCancelAval(RowEditEvent event) {

    }

    /**
     * @return the avalesDto
     */
    public List<UsuarioDto> getAvalesDto() {
        return avalesDto;
    }

    /**
     * @param avalesDto the avalesDto to set
     */
    public void setAvalesDto(List<UsuarioDto> avalesDto) {
        this.avalesDto = avalesDto;
    }

    /**
     * @return the noAvales
     */
    public Integer getNoAvales() {
        return noAvales;
    }

    /**
     * @param noAvales the noAvales to set
     */
    public void setNoAvales(Integer noAvales) {
        this.noAvales = noAvales;
    }

    /**
     * @return the solicitud
     */
    public Solicitudes getSolicitud() {
        return solicitud;
    }

    /**
     * @param solicitud the solicitud to set
     */
    public void setSolicitud(Solicitudes solicitud) {
        this.solicitud = solicitud;
    }

    /**
     * @return the tblEditable
     */
    public Boolean getTblEditable() {
        return tblEditable;
    }

    /**
     * @param tblEditable the tblEditable to set
     */
    public void setTblEditable(Boolean tblEditable) {
        this.tblEditable = tblEditable;
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
