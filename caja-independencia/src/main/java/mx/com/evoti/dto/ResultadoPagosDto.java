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
public class ResultadoPagosDto implements Serializable {

    private static final long serialVersionUID = -4986939040065200309L;

    private Integer pagClaveEmpleado;
    private Integer pagCredito;
    private Double pagDeposito;
    private Double pagAcumulado;
    private Integer pagEmpresa;
    private Integer pagEstatus;
    private Date pagFecha;
    private Integer pagId;
    private Integer pagUsuId;
    private Integer amoEstatus;
    private String amoEstNombre;
    private String empAbreviacion;
    private String pagEstNombre;
    private String pagEstColor;
    private String pagUsuNombre;
    private String creClave;

    /**
     * @return the pagClaveEmpleado
     */
    public Integer getPagClaveEmpleado() {
        return pagClaveEmpleado;
    }

    /**
     * @param pagClaveEmpleado the pagClaveEmpleado to set
     */
    public void setPagClaveEmpleado(Integer pagClaveEmpleado) {
        this.pagClaveEmpleado = pagClaveEmpleado;
    }

    /**
     * @return the pagCredito
     */
    public Integer getPagCredito() {
        return pagCredito;
    }

    /**
     * @param pagCredito the pagCredito to set
     */
    public void setPagCredito(Integer pagCredito) {
        this.pagCredito = pagCredito;
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
     * @return the pagEmpresa
     */
    public Integer getPagEmpresa() {
        return pagEmpresa;
    }

    /**
     * @param pagEmpresa the pagEmpresa to set
     */
    public void setPagEmpresa(Integer pagEmpresa) {
        this.pagEmpresa = pagEmpresa;
    }

    /**
     * @return the pagEstatus
     */
    public Integer getPagEstatus() {
        return pagEstatus;
    }

    /**
     * @param pagEstatus the pagEstatus to set
     */
    public void setPagEstatus(Integer pagEstatus) {
        this.pagEstatus = pagEstatus;
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
     * @return the pagEstNombre
     */
    public String getPagEstNombre() {
        return pagEstNombre;
    }

    /**
     * @param pagEstNombre the pagEstNombre to set
     */
    public void setPagEstNombre(String pagEstNombre) {
        this.pagEstNombre = pagEstNombre;
    }

    /**
     * @return the pagUsuNombre
     */
    public String getPagUsuNombre() {
        return pagUsuNombre;
    }

    /**
     * @param pagUsuNombre the pagUsuNombre to set
     */
    public void setPagUsuNombre(String pagUsuNombre) {
        this.pagUsuNombre = pagUsuNombre;
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
     * @return the pagEstColor
     */
    public String getPagEstColor() {
        return pagEstColor;
    }

    /**
     * @param pagEstColor the pagEstColor to set
     */
    public void setPagEstColor(String pagEstColor) {
        this.pagEstColor = pagEstColor;
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

}
