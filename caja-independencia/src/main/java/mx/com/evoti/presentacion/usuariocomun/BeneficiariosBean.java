/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.usuariocomun;

import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.BeneficiariosBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.usuariocomun.BeneficiariosDto;
import mx.com.evoti.hibernate.pojos.ParentescoBen;
import mx.com.evoti.presentacion.BaseBean;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "beneficiariosBean")
@ViewScoped
public class BeneficiariosBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SolicitudAvalesBean.class);
    private static final long serialVersionUID = 3786210198075145424L;

    
    private final int MAX_BENEFICIARIOS = 5;
    private List<BeneficiariosDto> beneficiarios;
    private BeneficiariosDto benefSelected;
    private BeneficiariosDto benefNuevo;
    private List<ParentescoBen> parentescos;
    private Integer origen;

    private UsuarioDto usuarioDto;
    private Boolean tblEditable;

    private BeneficiariosBo bo;

    public BeneficiariosBean() {
        usuarioDto = super.getUsuarioSesion();
        bo = new BeneficiariosBo();
    }

    public void init() {
        try {
           if(usuarioDto != null){
            beneficiarios = bo.getBeneficiariosByEmpleado(usuarioDto.getId());
            
            parentescos = bo.getParentescos();
           }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void onEditBen(RowEditEvent event) {

        LOGGER.debug("Dentro de onEdit");
        //Actualizar en base de datos cada vez que se de aceptar
        BeneficiariosDto dto = (BeneficiariosDto) event.getObject();
        saveOrUpdateBeneficiario(dto);
        super.muestraMensajeExito("El beneficiario fue actualizado", "", "tblBenef");

    }

    public void onCancelBen(RowEditEvent event) {

    }

    public void agregaBeneficiario() {

        if (beneficiarios.size() < this.MAX_BENEFICIARIOS) {
            benefNuevo = new BeneficiariosDto();
            benefNuevo.setBenUsuId(usuarioDto.getId());
            super.hideShowDlg("PF('dlgAddBenefW').show()");
            super.muestraMensajeExito("El beneficiario fue agregado correctamente", "", null);
        } else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Ya no puede dar de alta más benficiarios", "El máximo de beneficiarios permitidos por empleado es de 5");
            RequestContext.getCurrentInstance().showMessageInDialog(message);
        }

    }

    /**
     * Guarda el beneficiario desde el dialogo nuevo beneficiario
     */
    public void guardaBeneficiario() {
        /*Guarda el beneficiario*/
        saveOrUpdateBeneficiario(benefNuevo);

        super.hideShowDlg("PF('dlgAddBenefW').hide()");
    }

    /**
     * Metodo que borra de la lista de beneficiarios que se muestra en pantalla,
     * el beneficiario seleccionado en la tabla
     */
    public void borraBeneficiario() {
        if (benefSelected != null) {
            //Se borra el beneficiario seleccionado
            beneficiarios.removeIf(x -> x.equals(benefSelected));

            //Borra el beneficiario de la base de datos
            removeBeneficiario(benefSelected.getBenId());
            super.muestraMensajeExito("El beneficiario fue eliminado", "", "tblBenef");
        } else {
            super.muestraMensajeError("Debe seleccionar el beneficiario que desea eliminar", "", "tblBenef");
        }
    }

    /**
     * -Guarda o actualiza un beneficiario
     *
     * @param beneficiario
     */
    public void saveOrUpdateBeneficiario(BeneficiariosDto beneficiario) {
        try {
            bo.saveOrUpdate(beneficiario);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
            super.muestraMensajeError("Hubo un error al actualizar la información", "Por favor intente de nuevo", "tblBenef");
        }
    }

    /**
     * Elimina un beneficiario de la bse de datos
     *
     * @param benId
     */
    public void removeBeneficiario(Integer benId) {
        try {
            bo.borraBeneficiario(benId);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
            super.muestraMensajeError("Hubo un error al borrar el beneficiario", "Por favor intente de nuevo", "tblBenef");
        }
    }

    /**
     * @return the beneficiarios
     */
    public List<BeneficiariosDto> getBeneficiarios() {
        return beneficiarios;
    }

    /**
     * @param beneficiarios the beneficiarios to set
     */
    public void setBeneficiarios(List<BeneficiariosDto> beneficiarios) {
        this.beneficiarios = beneficiarios;
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
     * @return the parentescos
     */
    public List<ParentescoBen> getParentescos() {
        return parentescos;
    }

    /**
     * @param parentescos the parentescos to set
     */
    public void setParentescos(List<ParentescoBen> parentescos) {
        this.parentescos = parentescos;
    }

    /**
     * @return the usuario
     */
    public UsuarioDto getUsuario() {
        return usuarioDto;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(UsuarioDto usuario) {
        this.usuarioDto = usuario;
    }

    /**
     * @return the benefSelected
     */
    public BeneficiariosDto getBenefSelected() {
        return benefSelected;
    }

    /**
     * @param benefSelected the benefSelected to set
     */
    public void setBenefSelected(BeneficiariosDto benefSelected) {
        this.benefSelected = benefSelected;
    }

    /**
     * @return the benefNuevo
     */
    public BeneficiariosDto getBenefNuevo() {
        return benefNuevo;
    }

    /**
     * @param benefNuevo the benefNuevo to set
     */
    public void setBenefNuevo(BeneficiariosDto benefNuevo) {
        this.benefNuevo = benefNuevo;
    }

    /**
     * @return the origen
     */
    public Integer getOrigen() {
        return origen;
    }

    /**
     * @param origen the origen to set
     */
    public void setOrigen(Integer origen) {
        this.origen = origen;
    }

}
