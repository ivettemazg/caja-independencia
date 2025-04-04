/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.bancos.DesctosXCobrarCredsDto;
import mx.com.evoti.util.Util;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "desctosXCobrarBean")
@ViewScoped
public class DesctosXCobrarCredsBean extends BaseBean implements Serializable {
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DesctosXCobrarCredsBean.class);
    private static final long serialVersionUID = -6298604563542999029L;
    private BancosBo banBo;
    
    private List<DesctosXCobrarCredsDto> desctosPendts;
    private DesctosXCobrarCredsDto desctoSelected;

    public DesctosXCobrarCredsBean() {
        banBo = new BancosBo();
    }
    
    
    public void init(){
        try {
            desctosPendts = banBo.getDesctosXCobrarCreds();
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    
    public void onEdit(RowEditEvent event){
        
         DesctosXCobrarCredsDto dto = (DesctosXCobrarCredsDto) event.getObject();
     
         LOGGER.info(dto.getPagDeposito().toString());
         LOGGER.info(Util.generaFechaFormateada(dto.getPagFecha(), "dd/MM/yyyy"));
    }
    
    /**
     * Guarda un registro en la tabla Bancos con el concepto
     * 13 - DESCUENTO X COBRAR CREDITO
     */
    public void updtRegBanco(){
        try {
            
            banBo.updtRegBanco(this.desctoSelected);
            
            desctosPendts = banBo.getDesctosXCobrarCreds();
            
            super.muestraMensajeExito("El concepto fue ajustado", "", null);
            
        } catch (BusinessException ex) {
           LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the desctosPendts
     */
    public List<DesctosXCobrarCredsDto> getDesctosPendts() {
        return desctosPendts;
    }

    /**
     * @param desctosPendts the desctosPendts to set
     */
    public void setDesctosPendts(List<DesctosXCobrarCredsDto> desctosPendts) {
        this.desctosPendts = desctosPendts;
    }

    /**
     * @return the desctoSelected
     */
    public DesctosXCobrarCredsDto getDesctoSelected() {
        return desctoSelected;
    }

    /**
     * @param desctoSelected the desctoSelected to set
     */
    public void setDesctoSelected(DesctosXCobrarCredsDto desctoSelected) {
        this.desctoSelected = desctoSelected;
    }
    
}
