/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.algoritmopagos;

import java.util.Date;

/**
 *
 * @author Venette
 */
public class PagoAmortizacionDto {
     private Integer amoUsuId;
     private Double amoMontoPago;
     private Double pagDeposito;
     private Double pagAcumulado;
     private Integer pagUsuId;
     private Integer pagId;
     private Date pagFecha;

    /**
     * @return the amoUsuId
     */
    public Integer getAmoUsuId() {
        return amoUsuId;
    }

    /**
     * @param amoUsuId the amoUsuId to set
     */
    public void setAmoUsuId(Integer amoUsuId) {
        this.amoUsuId = amoUsuId;
    }

    /**
     * @return the amoMontoPago
     */
    public Double getAmoMontoPago() {
        return amoMontoPago;
    }

    /**
     * @param amoMontoPago the amoMontoPago to set
     */
    public void setAmoMontoPago(Double amoMontoPago) {
        this.amoMontoPago = amoMontoPago;
    }

    /**
     * @return the pagDeposito
     */
    public Double getPagDeposito() {
        return pagDeposito;
    }

    /**
     * @param pagDeposito the pagDeposito to set
     */
    public void setPagDeposito(Double pagDeposito) {
        this.pagDeposito = pagDeposito;
    }

    /**
     * @return the pagAcumulado
     */
    public Double getPagAcumulado() {
        return pagAcumulado;
    }

    /**
     * @param pagAcumulado the pagAcumulado to set
     */
    public void setPagAcumulado(Double pagAcumulado) {
        this.pagAcumulado = pagAcumulado;
    }

    /**
     * @return the pagUsuId
     */
    public Integer getPagUsuId() {
        return pagUsuId;
    }

    /**
     * @param pagUsuId the pagUsuId to set
     */
    public void setPagUsuId(Integer pagUsuId) {
        this.pagUsuId = pagUsuId;
    }

    /**
     * @return the pagId
     */
    public Integer getPagId() {
        return pagId;
    }

    /**
     * @param pagId the pagId to set
     */
    public void setPagId(Integer pagId) {
        this.pagId = pagId;
    }

    /**
     * @return the pagFecha
     */
    public Date getPagFecha() {
        return pagFecha;
    }

    /**
     * @param pagFecha the pagFecha to set
     */
    public void setPagFecha(Date pagFecha) {
        this.pagFecha = pagFecha;
    }
    
}
