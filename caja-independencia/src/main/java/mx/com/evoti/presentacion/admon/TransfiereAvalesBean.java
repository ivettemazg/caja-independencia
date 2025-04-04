/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.CatorcenasBo;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.finiquito.AvalesCreditoDto;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "transAvalesBean")
@ViewScoped
public class TransfiereAvalesBean extends BaseBean implements Serializable{
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TransfiereAvalesBean.class);
    private static final long serialVersionUID = -1312608651184691541L;
   
    private CreditosBo creBo;
    private CatorcenasBo catBo;
    
    private DetalleCreditoDto credito;
    private String cveCredSeleccionado;
    private Double adeudoTotalCredito;
    private Boolean rdrTblAvales;
    private List<AvalesCreditoDto> avales;
    private List<String> catorcenasSiguientes;
    private Boolean rdrBtnTransferir;
    private Boolean rdrBtnIncobrable;
    private String cmpntToUpdt;
    private Date fechaIncobrable;

    public TransfiereAvalesBean() {
        creBo = new CreditosBo();
        catBo = new CatorcenasBo();
    }
    
    
    
    
     public void init() {
        try {
            //Obtiene los avales
           
            //Campos que se mostrarán en el dialog
            cveCredSeleccionado = credito.getCreClave();
            adeudoTotalCredito = credito.getSaldoTotal();

            /**
             * Cuando el credito esta activo, pagado o ajustado *considerar quitar el pagado
             * cuando se encuentre en produccion
             */
            if (credito.getCreEstatusId() == Constantes.CRE_EST_ACTIVO || credito.getCreEstatusId() == Constantes.CRE_EST_PAGADO || credito.getCreEstatusId() == Constantes.CRE_EST_AJUSTADO) {

                rdrTblAvales = Boolean.TRUE;
                avales = creBo.getAvalesCredito(credito.getCreId());
                catorcenasSiguientes = catBo.getCatorcenasSiguientes(new Date());

                if (avales.isEmpty()) {
                    rdrBtnTransferir = Boolean.FALSE;
                    rdrBtnIncobrable = Boolean.TRUE;
                } else {
                    rdrBtnTransferir = Boolean.TRUE;
                    rdrBtnIncobrable = Boolean.FALSE;

                    Double xAval = adeudoTotalCredito / avales.size();

                    avales.forEach(aval -> {
                        aval.setMontoCredito(Util.round(xAval));
                    });

                }

                super.updtComponent("frmDlgTransferir");
                
            } else {
                rdrBtnTransferir = Boolean.FALSE;
                rdrTblAvales = Boolean.FALSE;
                rdrBtnIncobrable = Boolean.FALSE;
                super.muestraMensajeGen("El credito se encuentra " + credito.getCreEstatusNombre(), "Por lo tanto no puede ser transferido", null, FacesMessage.SEVERITY_WARN);
                super.updtComponent("frmDlgTransferir:msjDlgAjusteCredito");
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

     
      public void onEditTransfiere(RowEditEvent event) {

        AvalesCreditoDto aval = (AvalesCreditoDto) event.getObject();

        LOGGER.debug("" + aval.getMontoCredito());
        LOGGER.debug("" + aval.getPrimerCatorcena());
        LOGGER.debug("" + aval.getCatorcenas());

    }

    public void transfiereCreditos() {

       
        Double montoAvales = 0.0;
        for (AvalesCreditoDto aval : avales) {
            montoAvales += aval.getMontoCredito();
        }

        Double diferencia = Util.round(adeudoTotalCredito - montoAvales);

        /**
         * Si el monto introducido salda la deuda del credito en su totalidad
         */
        if (diferencia <= 2 && diferencia >= -2) {

            try {
                for (AvalesCreditoDto aval : avales) {
                    montoAvales += aval.getMontoCredito();

                    CreditosFinal credTransfer = new CreditosFinal();

                    credTransfer.setCreUsuId(aval.getAvalUsuId());
                    credTransfer.setCreClaveEmpleado(aval.getAvalCveEmpleado());
                    credTransfer.setCreEmpresa(aval.getEmpAbrev());
                    credTransfer.setCrePrestamo(aval.getMontoCredito());
                    credTransfer.setCreEstatus(1); //
                    credTransfer.setCreCatorcenas(aval.getCatorcenas());
                    credTransfer.setCreTipo("NOMINA");
                    credTransfer.setCreProducto(6);
                    credTransfer.setCrePadre(credito.getCreId());

                    creBo.creaCreditoTransferido(credTransfer, aval.getPrimerCatorcena());

                }

                creBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_TRANSFERIDO, null);


                super.hideShowDlg("PF('dlgTransferirW').hide()");

                super.updtComponent(cmpntToUpdt);
                super.muestraMensajeExito("El credito fue transferido", "", null);
            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

        } else {
            super.muestraMensajeGen("Debe transferir el monto del saldo total del crédito", "", "", FacesMessage.SEVERITY_WARN);

        }

    }
     
    
    
    public void mandarAIncobrable() {
        try {
         

            creBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_INCOBRABLE, fechaIncobrable);
            //Actualiza la amortizacion del credito padre
            creBo.updtEstatusAmoInt(credito.getCreId(), Constantes.AMO_ESTATUS_INCOB_12);
            
            super.hideShowDlg("PF('dlgTransferirW').hide()");

            super.updtComponent(cmpntToUpdt);
            super.muestraMensajeExito("El estatus del crédito se cambió a Incobrable", "", null);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
     
     
    /**
     * @return the cveCredSeleccionado
     */
    public String getCveCredSeleccionado() {
        return cveCredSeleccionado;
    }

    /**
     * @param cveCredSeleccionado the cveCredSeleccionado to set
     */
    public void setCveCredSeleccionado(String cveCredSeleccionado) {
        this.cveCredSeleccionado = cveCredSeleccionado;
    }

    /**
     * @return the adeudoTotalCredito
     */
    public Double getAdeudoTotalCredito() {
        return adeudoTotalCredito;
    }

    /**
     * @param adeudoTotalCredito the adeudoTotalCredito to set
     */
    public void setAdeudoTotalCredito(Double adeudoTotalCredito) {
        this.adeudoTotalCredito = adeudoTotalCredito;
    }

    /**
     * @return the rdrTblAvales
     */
    public Boolean getRdrTblAvales() {
        return rdrTblAvales;
    }

    /**
     * @param rdrTblAvales the rdrTblAvales to set
     */
    public void setRdrTblAvales(Boolean rdrTblAvales) {
        this.rdrTblAvales = rdrTblAvales;
    }

    /**
     * @return the avales
     */
    public List<AvalesCreditoDto> getAvales() {
        return avales;
    }

    /**
     * @param avales the avales to set
     */
    public void setAvales(List<AvalesCreditoDto> avales) {
        this.avales = avales;
    }

    /**
     * @return the catorcenasSiguientes
     */
    public List<String> getCatorcenasSiguientes() {
        return catorcenasSiguientes;
    }

    /**
     * @param catorcenasSiguientes the catorcenasSiguientes to set
     */
    public void setCatorcenasSiguientes(List<String> catorcenasSiguientes) {
        this.catorcenasSiguientes = catorcenasSiguientes;
    }

    /**
     * @return the rdrBtnTransferir
     */
    public Boolean getRdrBtnTransferir() {
        return rdrBtnTransferir;
    }

    /**
     * @param rdrBtnTransferir the rdrBtnTransferir to set
     */
    public void setRdrBtnTransferir(Boolean rdrBtnTransferir) {
        this.rdrBtnTransferir = rdrBtnTransferir;
    }

    /**
     * @return the rdrBtnIncobrable
     */
    public Boolean getRdrBtnIncobrable() {
        return rdrBtnIncobrable;
    }

    /**
     * @param rdrBtnIncobrable the rdrBtnIncobrable to set
     */
    public void setRdrBtnIncobrable(Boolean rdrBtnIncobrable) {
        this.rdrBtnIncobrable = rdrBtnIncobrable;
    }

    /**
     * @return the cmpntToUpdt
     */
    public String getCmpntToUpdt() {
        return cmpntToUpdt;
    }

    /**
     * @param cmpntToUpdt the cmpntToUpdt to set
     */
    public void setCmpntToUpdt(String cmpntToUpdt) {
        this.cmpntToUpdt = cmpntToUpdt;
    }

    /**
     * @return the credito
     */
    public DetalleCreditoDto getCredito() {
        return credito;
    }

    /**
     * @param credito the credito to set
     */
    public void setCredito(DetalleCreditoDto credito) {
        this.credito = credito;
    }

    /**
     * @return the fechaIncobrable
     */
    public Date getFechaIncobrable() {
        return fechaIncobrable;
    }

    /**
     * @param fechaIncobrable the fechaIncobrable to set
     */
    public void setFechaIncobrable(Date fechaIncobrable) {
        this.fechaIncobrable = fechaIncobrable;
    }
    
}
