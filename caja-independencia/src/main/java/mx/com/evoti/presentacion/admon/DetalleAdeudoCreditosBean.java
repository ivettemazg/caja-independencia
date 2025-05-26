/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.administrador.finiquito.FiniquitosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "detAdCreBean")
@ViewScoped
public class DetalleAdeudoCreditosBean extends BaseBean implements Serializable{
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DetalleAdeudoCreditosBean.class);
    private static final long serialVersionUID = -7455685082343704755L;
    
    private CreditosBo creditoBo;
    private FiniquitosBo finiquitosBo;
    
    private List<DetalleCreditoDto> creditos;
    private DetalleCreditoDto creditoSelected;
    private Usuarios usuario;
    private Amortizacion amoPagoCapital;
    private List<Amortizacion> amoPendientesAnteriores;

    public DetalleAdeudoCreditosBean() {
        finiquitosBo = new FiniquitosBo();
        creditoBo = new CreditosBo();
    }
    
 
     /**
     * Obtiene los créditos con su respectivo detalle para mostrar en la tabla de creditos pendientes
     * de ajustar
     */
    public void obtieneCreditosDetalle(){
        System.out.println("DENTRO DE OBTIENE CREDITOS DETALLE");
        try {
           creditos = finiquitosBo.obtCreditosDetalleFiniquito(usuario);
          
        } catch (BusinessException ex) {
           LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    
     /**
     * Actualiza la variable creditoSelected justo despues de haber hecho un ajuste del mismo
     * @return 
     */
    public DetalleCreditoDto refreshCreditoSeleccionado(){
        try {
           creditoSelected = creditoBo.getDetalleCreditoByCreId(usuario,creditoSelected.getCreId());
        } catch (BusinessException ex) {
           LOGGER.error(ex.getMessage(), ex);
        }
           return creditoSelected;
    }
    
    
    
    
    /**
     * Obtiene las amortizaciones pendientes anteriores
     */
    public void getAmortizacionesPendientes(){
        try {
            amoPendientesAnteriores = creditoBo.getCatPendtsAnteriores(creditoSelected.getCreId(), usuario);
        } catch (BusinessException ex) {
           LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    /**
     * Obtiene la amortizacion en la qeu se meterá el pago a capital
     */
    public void getAmortizacionPagoCap(){
        
         try {
            amoPagoCapital = creditoBo.getAmoPagoCapital(creditoSelected.getCreId(), usuario);
        } catch (BusinessException ex) {
           LOGGER.error(ex.getMessage(), ex);
        }
        
    } 
    
    

    /**
     * @return the creditos
     */
    public List<DetalleCreditoDto> getCreditos() {
        return creditos;
    }

    /**
     * @param creditos the creditos to set
     */
    public void setCreditos(List<DetalleCreditoDto> creditos) {
        this.creditos = creditos;
    }

    /**
     * @return the creditoSelected
     */
    public DetalleCreditoDto getCreditoSelected() {
        return creditoSelected;
    }

    /**
     * @param creditoSelected the creditoSelected to set
     */
    public void setCreditoSelected(DetalleCreditoDto creditoSelected) {
        this.creditoSelected = creditoSelected;
    }

    /**
     * @return the usuario
     */
    public Usuarios getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the amoPagoCapital
     */
    public Amortizacion getAmoPagoCapital() {
        return amoPagoCapital;
    }

    /**
     * @param amoPagoCapital the amoPagoCapital to set
     */
    public void setAmoPagoCapital(Amortizacion amoPagoCapital) {
        this.amoPagoCapital = amoPagoCapital;
    }

    /**
     * @return the amoPendientesAnteriores
     */
    public List<Amortizacion> getAmoPendientesAnteriores() {
        return amoPendientesAnteriores;
    }

    /**
     * @param amoPendientesAnteriores the amoPendientesAnteriores to set
     */
    public void setAmoPendientesAnteriores(List<Amortizacion> amoPendientesAnteriores) {
        this.amoPendientesAnteriores = amoPendientesAnteriores;
    }
    
    
}
