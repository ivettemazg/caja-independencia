/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.bancos;

import java.util.Date;

/**
 *
 * @author Venette
 */
public class ReporteBancosDto {
    
    private Integer ecId;
    private String empAbreviacion;
    private String conceptoStr;
    private String ecDescripcion;
    private Date ecFechaConcepto;
    private Double ecMonto;
    private Integer ajustado;
    private Double saldoPendienteAjuste;
    private String estatus;
    private String color;
    private String summary;

    /**
     * @return the ecId
     */
    public Integer getEcId() {
        return ecId;
    }

    /**
     * @param ecId the ecId to set
     */
    public void setEcId(Integer ecId) {
        this.ecId = ecId;
    }

    /**
     * @return the empAbreviacion
     */
    public String getEmpAbreviacion() {
        return empAbreviacion;
    }

    /**
     * @param empAbreviacion the empAbreviacion to set
     */
    public void setEmpAbreviacion(String empAbreviacion) {
        this.empAbreviacion = empAbreviacion;
    }

    /**
     * @return the conceptoStr
     */
    public String getConceptoStr() {
        return conceptoStr;
    }

    /**
     * @param conceptoStr the conceptoStr to set
     */
    public void setConceptoStr(String conceptoStr) {
        this.conceptoStr = conceptoStr;
    }

    /**
     * @return the ecDescripcion
     */
    public String getEcDescripcion() {
        return ecDescripcion;
    }

    /**
     * @param ecDescripcion the ecDescripcion to set
     */
    public void setEcDescripcion(String ecDescripcion) {
        this.ecDescripcion = ecDescripcion;
    }

    /**
     * @return the ecFechaConcepto
     */
    public Date getEcFechaConcepto() {
        return ecFechaConcepto;
    }

    /**
     * @param ecFechaConcepto the ecFechaConcepto to set
     */
    public void setEcFechaConcepto(Date ecFechaConcepto) {
        this.ecFechaConcepto = ecFechaConcepto;
    }

    /**
     * @return the ecMonto
     */
    public Double getEcMonto() {
        return ecMonto;
    }

    /**
     * @param ecMonto the ecMonto to set
     */
    public void setEcMonto(Double ecMonto) {
        this.ecMonto = ecMonto;
    }

    /**
     * @return the ajustado
     */
    public Integer getAjustado() {
        return ajustado;
    }

    /**
     * @param ajustado the ajustado to set
     */
    public void setAjustado(Integer ajustado) {
        this.ajustado = ajustado;
        
        switch(ajustado){
            case 0:
                setEstatus("PENDIENTE");
                break;
            case 1:
                setEstatus("AJUSTADO");
                setColor("ajustado");
                break;
            case 2:
                setEstatus("AJUSTADO PARCIAL");
                setColor("ajustado-parcial");
                break;
        }
        
         setSummary("DEFAULT");
    }

    /**
     * @return the saldoPendienteAjuste
     */
    public Double getSaldoPendienteAjuste() {
        return saldoPendienteAjuste;
    }

    /**
     * @param saldoPendienteAjuste the saldoPendienteAjuste to set
     */
    public void setSaldoPendienteAjuste(Double saldoPendienteAjuste) {
        this.saldoPendienteAjuste = saldoPendienteAjuste;
    }

    /**
     * @return the estatus
     */
    public String getEstatus() {
        return estatus;
    }

    /**
     * @param estatus the estatus to set
     */
    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
}
