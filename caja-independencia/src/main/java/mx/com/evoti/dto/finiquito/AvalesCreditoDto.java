/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.finiquito;

import java.math.BigInteger;

/**
 *
 * @author Venette
 */
public class AvalesCreditoDto {
    private Integer creId;
    private Integer avalUsuId;
    private String avalNombreCompleto;
    private Integer avalCveEmpleado;
    private String empAbrev;
    private BigInteger solAvaSolicitud;
    private Double montoCredito;
    private Integer catorcenas;
    private String primerCatorcena;

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
     * @return the avalUsuId
     */
    public Integer getAvalUsuId() {
        return avalUsuId;
    }

    /**
     * @param avalUsuId the avalUsuId to set
     */
    public void setAvalUsuId(Integer avalUsuId) {
        this.avalUsuId = avalUsuId;
    }

    /**
     * @return the avalNombreCompleto
     */
    public String getAvalNombreCompleto() {
        return avalNombreCompleto;
    }

    /**
     * @param avalNombreCompleto the avalNombreCompleto to set
     */
    public void setAvalNombreCompleto(String avalNombreCompleto) {
        this.avalNombreCompleto = avalNombreCompleto;
    }

    /**
     * @return the avalCveEmpleado
     */
    public Integer getAvalCveEmpleado() {
        return avalCveEmpleado;
    }

    /**
     * @param avalCveEmpleado the avalCveEmpleado to set
     */
    public void setAvalCveEmpleado(Integer avalCveEmpleado) {
        this.avalCveEmpleado = avalCveEmpleado;
    }

    /**
     * @return the empAbrev
     */
    public String getEmpAbrev() {
        return empAbrev;
    }

    /**
     * @param empAbrev the empAbrev to set
     */
    public void setEmpAbrev(String empAbrev) {
        this.empAbrev = empAbrev;
    }

    /**
     * @return the solAvaSolicitud
     */
    public BigInteger getSolAvaSolicitud() {
        return solAvaSolicitud;
    }

    /**
     * @param solAvaSolicitud the solAvaSolicitud to set
     */
    public void setSolAvaSolicitud(BigInteger solAvaSolicitud) {
        this.solAvaSolicitud = solAvaSolicitud;
    }

    /**
     * @return the montoCredito
     */
    public Double getMontoCredito() {
        return montoCredito;
    }

    /**
     * @param montoCredito the montoCredito to set
     */
    public void setMontoCredito(Double montoCredito) {
        this.montoCredito = montoCredito;
    }

    /**
     * @return the catorcenas
     */
    public Integer getCatorcenas() {
        return catorcenas;
    }

    /**
     * @param catorcenas the catorcenas to set
     */
    public void setCatorcenas(Integer catorcenas) {
        this.catorcenas = catorcenas;
    }

    /**
     * @return the primerCatorcena
     */
    public String getPrimerCatorcena() {
        return primerCatorcena;
    }

    /**
     * @param primerCatorcena the primerCatorcena to set
     */
    public void setPrimerCatorcena(String primerCatorcena) {
        this.primerCatorcena = primerCatorcena;
    }
}
