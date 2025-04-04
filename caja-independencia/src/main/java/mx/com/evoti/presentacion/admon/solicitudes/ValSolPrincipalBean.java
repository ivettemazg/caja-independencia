/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon.solicitudes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import mx.com.evoti.bo.TablaAmortizacionBo;
import mx.com.evoti.bo.administrador.solicitud.ValidaSolicitudBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.SolicitudDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "valSolPpalBean")
@ViewScoped
public class ValSolPrincipalBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ValSolPrincipalBean.class);
    private static final long serialVersionUID = 3068626078094846684L;
    
    private final ValidaSolicitudBo valSolBo;
    private BigInteger idSolicitud;
    private SolicitudDto solDto;
    private final TablaAmortizacionBo amortBo;
   
    
    public ValSolPrincipalBean(){
        valSolBo = new ValidaSolicitudBo();
        amortBo = new TablaAmortizacionBo();
       
    }
    
    public void init(){
        try {
            /*
            * Obtiene el detalle de la solicitud que está mostrando
            */
            solDto = valSolBo.obtieneDetallePendiente(idSolicitud);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        
    }
    
    /**
     * Se ejecuta al actualizar algún campo de la solicitud
     * @param event 
     */
     public void actualizarDatos(AjaxBehaviorEvent event) {
         List<AmortizacionDto> amortizacionDto;
          /*
         * Cuando cambia el salario es necesario ajustar los campos de porcentajes
         */
        ajustaPctSalario();
        
        if (solDto.getProId()== 6 || solDto.getProId() == 7) {
             Double sumaInteres = 0.0;
            
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(Util.generaFechaDeString("2018-11-11"));
            cal2.setTime(solDto.getSolFechaCreacion());
            System.out.println(cal1.get(Calendar.DAY_OF_YEAR));
            System.out.println(cal2.get(Calendar.DAY_OF_YEAR));
            System.out.println(cal1.get(Calendar.YEAR));
            System.out.println(cal2.get(Calendar.YEAR));
            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                  cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        
            int tasaEspecial = 0;
            
         if(sameDay){
             tasaEspecial = 1;
         }
             
             
             amortizacionDto = amortBo.generaTablaAmortizacion(Constantes.PRIMER_PAGO, solDto.getSolMontoSolicitado(), solDto.getSolCatorcenas(), null, solDto.getProId(), tasaEspecial);
            solDto.setSolMontoCatorcenal(amortizacionDto.get(0).getAmoMontoPago());
            solDto.setSolPagoCredito(amortizacionDto.get(0).getAmoMontoPago());
             for (AmortizacionDto dto : amortizacionDto) {
                sumaInteres += dto.getAmoInteres();
            }
            
            solDto.setSolIntereses(new BigDecimal(sumaInteres).setScale(2, RoundingMode.DOWN).doubleValue());


        } else {
            Double sumaInteres = 0.0;
            List<Date> fechas = valSolBo.obtieneCatorcenasIntermedias(solDto.getProId(), solDto.getSolFechaUltCatorcena(),solDto.getSolFechaCreacion());
            amortizacionDto = amortBo.generaTablaAmortizacionAgFA(solDto.getSolMontoSolicitado(), fechas);

            for (int i=0; i<amortizacionDto.size()-1; i++) {
                System.out.println("interes: "+amortizacionDto.get(i).getAmoInteres()+" no pago: "+amortizacionDto.get(i).getAmoNumeroPago());
                sumaInteres += amortizacionDto.get(i).getAmoInteres();
            }

            solDto.setSolCatorcenas(amortizacionDto.size());
            solDto.setSolIntereses(new BigDecimal(sumaInteres).setScale(2, RoundingMode.DOWN).doubleValue());
            
        }
        solDto.setSolDisponible(solDto.getTabCatorcenal()- solDto.getSolDeducciones());
        
        /**
         * Se actualiza en bd los campos modificados
         */
         updtDatosSolicitud(solDto);
    
     }
     
    private void ajustaPctSalario() {
        solDto.setSolDisponible(solDto.getTabCatorcenal() - solDto.getSolDeducciones());
        solDto.setSolCalcMontoSalario(new BigDecimal(solDto.getTabCatorcenal() * 10).setScale(2, RoundingMode.DOWN).doubleValue());
        solDto.setSolCalcPagoSalario(new BigDecimal((solDto.getTabCatorcenal() - solDto.getSolDeducciones()) * .3).setScale(2, RoundingMode.DOWN).doubleValue());
    }

    
    public void updtDatosSolicitud(SolicitudDto solDto) {
        try {
            valSolBo.updtSolicitudDetalle(solDto);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
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
     * @return the solDto
     */
    public SolicitudDto getSolDto() {
        return solDto;
    }

    /**
     * @param solDto the solDto to set
     */
    public void setSolDto(SolicitudDto solDto) {
        this.solDto = solDto;
    }
    
}
