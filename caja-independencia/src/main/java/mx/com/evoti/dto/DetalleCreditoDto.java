/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.math.BigInteger;

/**
 *
 * @author Venette
 */
public class DetalleCreditoDto {
    
    private Integer creId;
    private String creClave;
    private String proDescripcion;
    private Double crePrestamo;
    private BigInteger creCatorcenas;
    private Double saldoPendiente;
    private Double saldoCapital;
    private Double saldoTotal;
    private String creEstatusNombre;
    private Integer creEstatusId;
    private Integer noAvales;
    private BigInteger catorcenasPendientes;

    /**
     * @return the creId
     */
    public Integer getCreId() {
        return creId;
    }

    /**
     * @param creId the creId to set
     */
    public void setCreId(Integer creId) {
        this.creId = creId;
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
     * @return the proDescripcion
     */
    public String getProDescripcion() {
        return proDescripcion;
    }

    /**
     * @param proDescripcion the proDescripcion to set
     */
    public void setProDescripcion(String proDescripcion) {
        this.proDescripcion = proDescripcion;
    }

    /**
     * @return the crePrestamo
     */
    public Double getCrePrestamo() {
        return crePrestamo;
    }

    /**
     * @param crePrestamo the crePrestamo to set
     */
    public void setCrePrestamo(Double crePrestamo) {
        this.crePrestamo = crePrestamo;
    }

    /**
     * @return the creCatorcenas
     */
    public BigInteger getCreCatorcenas() {
        return creCatorcenas;
    }

    /**
     * @param creCatorcenas the creCatorcenas to set
     */
    public void setCreCatorcenas(BigInteger creCatorcenas) {
        this.creCatorcenas = creCatorcenas;
    }

    /**
     * @return the saldoPendiente
     */
    public Double getSaldoPendiente() {
        return saldoPendiente;
    }

    /**
     * @param saldoPendiente the saldoPendiente to set
     */
    public void setSaldoPendiente(Double saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    /**
     * @return the saldoCapital
     */
    public Double getSaldoCapital() {
        return saldoCapital;
    }

    /**
     * @param saldoCapital the saldoCapital to set
     */
    public void setSaldoCapital(Double saldoCapital) {
        this.saldoCapital = saldoCapital;
    }

    /**
     * @return the saldoTotal
     */
    public Double getSaldoTotal() {
        return saldoTotal;
    }

    /**
     * @param saldoTotal the saldoTotal to set
     */
    public void setSaldoTotal(Double saldoTotal) {
        this.saldoTotal = saldoTotal;
    }

    /**
     * @return the creEstatusNombre
     */
    public String getCreEstatusNombre() {
        return creEstatusNombre;
    }

    /**
     * @param creEstatusNombre the creEstatusNombre to set
     */
    public void setCreEstatusNombre(String creEstatusNombre) {
        this.creEstatusNombre = creEstatusNombre;
    }

    /**
     * @return the creEstatusId
     */
    public Integer getCreEstatusId() {
        return creEstatusId;
    }

    /**
     * @param creEstatusId the creEstatusId to set
     */
    public void setCreEstatusId(Integer creEstatusId) {
        this.creEstatusId = creEstatusId;
    }

    /**
     * @return the noAvales
     */
    public Integer getNoAvales() {
        return noAvales;
    }

    /**
     * @param noAvales the noAvales to set
     */
    public void setNoAvales(Integer noAvales) {
        this.noAvales = noAvales;
    }

    /**
     * @return the catorcenasPendientes
     */
    public BigInteger getCatorcenasPendientes() {
        return catorcenasPendientes;
    }

    /**
     * @param catorcenasPendientes the catorcenasPendientes to set
     */
    public void setCatorcenasPendientes(BigInteger catorcenasPendientes) {
        this.catorcenasPendientes = catorcenasPendientes;
    }
    
    
}
