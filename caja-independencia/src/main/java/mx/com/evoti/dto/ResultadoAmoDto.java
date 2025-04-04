/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Venette
 */
public class ResultadoAmoDto implements Serializable {

    private static final long serialVersionUID = -1382182357529000131L;

    private Integer amoId;
    private Integer amoNumeroPago;
    private Double amoCapital;
    private Double amoAmortizacion;
    private Double amoInteres;
    private Double amoMontoPago;
    private Date amoFechaPago;
    private Integer amoEstatus;
    private String amoEstNombre;
    private Integer amoCredito;
    private Integer creUsuId;
    private Integer usuClaveEmpleado;
    private Integer usuEmpresa;
    private String creTipo;
    private Double pagDeposito;
    private String creClave;
    private String empAbreviacion;
    private String creNombre;
    private Double diferencia;

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
    public Integer getAmoEstatus() {
        return amoEstatus;
    }

    /**
     * @param amoEstatus the amoEstatus to set
     */
    public void setAmoEstatus(Integer amoEstatus) {
        this.amoEstatus = amoEstatus;
    }

    /**
     * @return the amoEstNombre
     */
    public String getAmoEstNombre() {
        return amoEstNombre;
    }

    /**
     * @param amoEstNombre the amoEstNombre to set
     */
    public void setAmoEstNombre(String amoEstNombre) {
        this.amoEstNombre = amoEstNombre;
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
     * @return the creUsuId
     */
    public Integer getCreUsuId() {
        return creUsuId;
    }

    /**
     * @param creUsuId the creUsuId to set
     */
    public void setCreUsuId(Integer creUsuId) {
        this.creUsuId = creUsuId;
    }

    /**
     * @return the usuClaveEmpleado
     */
    public Integer getUsuClaveEmpleado() {
        return usuClaveEmpleado;
    }

    /**
     * @param usuClaveEmpleado the usuClaveEmpleado to set
     */
    public void setUsuClaveEmpleado(Integer usuClaveEmpleado) {
        this.usuClaveEmpleado = usuClaveEmpleado;
    }

    /**
     * @return the usuEmpresa
     */
    public Integer getUsuEmpresa() {
        return usuEmpresa;
    }

    /**
     * @param usuEmpresa the usuEmpresa to set
     */
    public void setUsuEmpresa(Integer usuEmpresa) {
        this.usuEmpresa = usuEmpresa;
    }

    /**
     * @return the creTipo
     */
    public String getCreTipo() {
        return creTipo;
    }

    /**
     * @param creTipo the creTipo to set
     */
    public void setCreTipo(String creTipo) {
        this.creTipo = creTipo;
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
     * @return the creClave
     */
    public String getCreClave() {
        return creClave;
    }

    /**
     * @param creClave the creClave to set
     */
    public void setCreClave(String creClave) {
        this.creClave = creClave;
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
     * @return the creNombre
     */
    public String getCreNombre() {
        return creNombre;
    }

    /**
     * @param creNombre the creNombre to set
     */
    public void setCreNombre(String creNombre) {
        this.creNombre = creNombre;
    }

    /**
     * @return the diferencia
     */
    public Double getDiferencia() {
        return diferencia;
    }

    /**
     * @param diferencia the diferencia to set
     */
    public void setDiferencia(Double diferencia) {
        this.diferencia = diferencia;
    }
}
