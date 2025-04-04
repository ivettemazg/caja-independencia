/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.common;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Venette
 */
public class AmortizacionDto implements Serializable{

    private static final long serialVersionUID = -7580408532738629786L;
    
     private Integer amoId;
     private Integer amoNumeroPago;
     private Integer amoEstatusInt;
     private Double amoCapital;
     private Double amoAmortizacion;
     private Double amoInteres;
     private Double amoIva;
     private Double amoMontoPago;
     private Integer amoCredito;
     private Double amoSaldo;
     private Date amoFechaPago;
     private String amoEstatus;
     private String amoEstColor;
     private Integer amoClaveEmpleado;
     private Integer amoPagoId;
     private Integer amoUsuId;
     private Integer amoProducto;
     
     private Date amoFechaPagoCredito;

    /**
     * @return the amoId
     */
    public Integer getAmoId() {
        return amoId;
    }

    /**
     * @param amoId the amoId to set
     */
    public void setAmoId(Integer amoId) {
        this.amoId = amoId;
    }

    /**
     * @return the amoNumeroPago
     */
    public Integer getAmoNumeroPago() {
        return amoNumeroPago;
    }

    /**
     * @param amoNumeroPago the amoNumeroPago to set
     */
    public void setAmoNumeroPago(Integer amoNumeroPago) {
        this.amoNumeroPago = amoNumeroPago;
    }

    /**
     * @return the amoCapital
     */
    public Double getAmoCapital() {
        return amoCapital;
    }

    /**
     * @param amoCapital the amoCapital to set
     */
    public void setAmoCapital(Double amoCapital) {
        this.amoCapital = amoCapital;
    }

    /**
     * @return the amoAmortizacion
     */
    public Double getAmoAmortizacion() {
        return amoAmortizacion;
    }

    /**
     * @param amoAmortizacion the amoAmortizacion to set
     */
    public void setAmoAmortizacion(Double amoAmortizacion) {
        this.amoAmortizacion = amoAmortizacion;
    }

    /**
     * @return the amoInteres
     */
    public Double getAmoInteres() {
        return amoInteres;
    }

    /**
     * @param amoInteres the amoInteres to set
     */
    public void setAmoInteres(Double amoInteres) {
        this.amoInteres = amoInteres;
    }

    /**
     * @return the amoIva
     */
    public Double getAmoIva() {
        return amoIva;
    }

    /**
     * @param amoIva the amoIva to set
     */
    public void setAmoIva(Double amoIva) {
        this.amoIva = amoIva;
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
     * @return the amoCredito
     */
    public Integer getAmoCredito() {
        return amoCredito;
    }

    /**
     * @param amoCredito the amoCredito to set
     */
    public void setAmoCredito(Integer amoCredito) {
        this.amoCredito = amoCredito;
    }

    /**
     * @return the amoSaldo
     */
    public Double getAmoSaldo() {
        return amoSaldo;
    }

    /**
     * @param amoSaldo the amoSaldo to set
     */
    public void setAmoSaldo(Double amoSaldo) {
        this.amoSaldo = amoSaldo;
    }

    /**
     * @return the amoFechaPago
     */
    public Date getAmoFechaPago() {
        return amoFechaPago;
    }

    /**
     * @param amoFechaPago the amoFechaPago to set
     */
    public void setAmoFechaPago(Date amoFechaPago) {
        this.amoFechaPago = amoFechaPago;
    }

    /**
     * @return the amoEstatus
     */
    public String getAmoEstatus() {
        return amoEstatus;
    }

    /**
     * @param amoEstatus the amoEstatus to set
     */
    public void setAmoEstatus(String amoEstatus) {
        this.amoEstatus = amoEstatus;
    }

    /**
     * @return the amoClaveEmpleado
     */
    public Integer getAmoClaveEmpleado() {
        return amoClaveEmpleado;
    }

    /**
     * @param amoClaveEmpleado the amoClaveEmpleado to set
     */
    public void setAmoClaveEmpleado(Integer amoClaveEmpleado) {
        this.amoClaveEmpleado = amoClaveEmpleado;
    }

    /**
     * @return the amoPagoId
     */
    public Integer getAmoPagoId() {
        return amoPagoId;
    }

    /**
     * @param amoPagoId the amoPagoId to set
     */
    public void setAmoPagoId(Integer amoPagoId) {
        this.amoPagoId = amoPagoId;
    }

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
     * @return the amoFechaPagoCredito
     */
    public Date getAmoFechaPagoCredito() {
        return amoFechaPagoCredito;
    }

    /**
     * @param amoFechaPagoCredito the amoFechaPagoCredito to set
     */
    public void setAmoFechaPagoCredito(Date amoFechaPagoCredito) {
        this.amoFechaPagoCredito = amoFechaPagoCredito;
    }

    /**
     * @return the amoEstatusInt
     */
    public Integer getAmoEstatusInt() {
        return amoEstatusInt;
    }

    /**
     * @param amoEstatusInt the amoEstatusInt to set
     */
    public void setAmoEstatusInt(Integer amoEstatusInt) {
        this.amoEstatusInt = amoEstatusInt;
    }

    /**
     * @return the amoProducto
     */
    public Integer getAmoProducto() {
        return amoProducto;
    }

    /**
     * @param amoProducto the amoProducto to set
     */
    public void setAmoProducto(Integer amoProducto) {
        this.amoProducto = amoProducto;
    }

    /**
     * @return the amoEstColor
     */
    public String getAmoEstColor() {
        return amoEstColor;
    }

    /**
     * @param amoEstColor the amoEstColor to set
     */
    public void setAmoEstColor(String amoEstColor) {
        this.amoEstColor = amoEstColor;
    }
    
}
