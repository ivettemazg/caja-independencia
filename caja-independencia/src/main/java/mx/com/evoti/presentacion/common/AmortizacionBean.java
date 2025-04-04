/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.common;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.TablaAmortizacionBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "amortizacionBean")
@ViewScoped
public class AmortizacionBean implements Serializable{

    private static final long serialVersionUID = -8633559433211698871L;

    private List<AmortizacionDto> amortizacion;
    private Double montoTotalInteres;
    private Double montoTotalPagar;
    private Double montoTotalAmortizacion;
    private TablaAmortizacionBo amoBo;
    private Date ultimaFechaFaAG;
    
    private boolean rdrTabla;
    
    /*Se utiliza para ocultar las columnas de la tabla de acuerdo 
    al tipo de prestamo que se va a mostrar*/
    private boolean rdrColumns;
    private Integer totalCatorcenasFaAgSau;
   
    
    /**
     * Metodo que genera la amortizacion que se mostrará en pantalla
     * @param iTipoSolicitud- Tipo de crédito NO, AU, FA, AG, SAU
     * @param montoSolicitado - El monto original del prestamo
     * @param catorcenas - Las catorcenas en que se liquidará el prestamo
     * @param faCatorcena - Solo se utiliza cuando el credito es de fondo de ahorro
     * @param junioSigAno- se utiliza solamente en creditos de fa y ag
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public void generaAmortizacion(int iTipoSolicitud, Double montoSolicitado,Integer catorcenas,
            Integer faCatorcena, Boolean junioSigAno) throws BusinessException{
        amoBo = new TablaAmortizacionBo();
        
        Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(Util.generaFechaDeString("2018-11-11"));
            cal2.setTime(Util.restaHrToDate(new Date(), 6));
             System.out.println(cal1.get(Calendar.DAY_OF_YEAR));
            System.out.println(cal2.get(Calendar.DAY_OF_YEAR));
            System.out.println(cal1.get(Calendar.YEAR));
            System.out.println(cal2.get(Calendar.YEAR));
            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                  cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        
            int tasaEspecial = 0;
            System.out.println("Same day"+sameDay);
         if(sameDay){
             tasaEspecial = 1;
         }
        
          if (iTipoSolicitud== 7 || iTipoSolicitud == 6) {
            amortizacion = amoBo.generaTablaAmortizacion(Constantes.PRIMER_PAGO,montoSolicitado,catorcenas,null,iTipoSolicitud, tasaEspecial);
            
        } else if (iTipoSolicitud == 5 || iTipoSolicitud == 4 || iTipoSolicitud == 11) {
            List<Date> fechas;
            fechas = amoBo.obtieneCatIntrmdsSmldr(iTipoSolicitud, faCatorcena,junioSigAno);
            
            amortizacion = amoBo.generaTablaAmortizacionAgFA(montoSolicitado, fechas);
            ultimaFechaFaAG = amortizacion != null ? amortizacion.get(0).getAmoFechaPagoCredito() : null;

        }
        
        montoTotalAmortizacion = amoBo.getMontoTotalAmortizacion();
        montoTotalInteres = amoBo.getMontoTotalInteres();
        montoTotalPagar = amoBo.getMontoTotalPagar();
        totalCatorcenasFaAgSau = amoBo.getTotalCatorcenasFaAgSau();
        
        rdrTabla=Boolean.TRUE;
        
    }
   

    /**
     * @return the montoTotalInteres
     */
    public Double getMontoTotalInteres() {
        return montoTotalInteres;
    }

    /**
     * @param montoTotalInteres the montoTotalInteres to set
     */
    public void setMontoTotalInteres(Double montoTotalInteres) {
        this.montoTotalInteres = montoTotalInteres;
    }

    /**
     * @return the montoTotalPagar
     */
    public Double getMontoTotalPagar() {
        return montoTotalPagar;
    }

    /**
     * @param montoTotalPagar the montoTotalPagar to set
     */
    public void setMontoTotalPagar(Double montoTotalPagar) {
        this.montoTotalPagar = montoTotalPagar;
    }

    /**
     * @return the montoTotalAmortizacion
     */
    public Double getMontoTotalAmortizacion() {
        return montoTotalAmortizacion;
    }

    /**
     * @param montoTotalAmortizacion the montoTotalAmortizacion to set
     */
    public void setMontoTotalAmortizacion(Double montoTotalAmortizacion) {
        this.montoTotalAmortizacion = montoTotalAmortizacion;
    }

    /**
     * @return the rdrTabla
     */
    public boolean isRdrTabla() {
        return rdrTabla;
    }

    /**
     * @param rdrTabla the rdrTabla to set
     */
    public void setRdrTabla(boolean rdrTabla) {
        this.rdrTabla = rdrTabla;
    }

    /**
     * @param amortizacion the amortizacion to set
     */
    public void setAmortizacion(List<AmortizacionDto> amortizacion) {
        this.amortizacion = amortizacion;
    }

    /**
     * @return the amortizacion
     */
    public List<AmortizacionDto> getAmortizacion() {
        return amortizacion;
    }

    /**
     * @return the rdrColumns
     */
    public boolean isRdrColumns() {
        return rdrColumns;
    }

    /**
     * @param rdrColumns the rdrColumns to set
     */
    public void setRdrColumns(boolean rdrColumns) {
        this.rdrColumns = rdrColumns;
    }

    /**
     * @return the ultimaFechaFaAG
     */
    public Date getUltimaFechaFaAG() {
        return ultimaFechaFaAG;
    }

    /**
     * @param ultimaFechaFaAG the ultimaFechaFaAG to set
     */
    public void setUltimaFechaFaAG(Date ultimaFechaFaAG) {
        this.ultimaFechaFaAG = ultimaFechaFaAG;
    }

    /**
     * @return the totalCatorcenasFaAgSau
     */
    public Integer getTotalCatorcenasFaAgSau() {
        return totalCatorcenasFaAgSau;
    }

    /**
     * @param totalCatorcenasFaAgSau the totalCatorcenasFaAgSau to set
     */
    public void setTotalCatorcenasFaAgSau(Integer totalCatorcenasFaAgSau) {
        this.totalCatorcenasFaAgSau = totalCatorcenasFaAgSau;
    }

}
