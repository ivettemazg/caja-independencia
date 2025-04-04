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
public class DesctosXCobrarCredsDto {
    
    private String creClave;
    private Integer usuClaveEmpleado;
    private String empAbreviacion;
    private Double pagDeposito;
    private Date pagFecha;
    private Integer creId;
    private Integer usuId;
    private Integer pagId;
    private Integer empId;
    private Integer banId;
    private String nombreCompleto;

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
     * @return the usuId
     */
    public Integer getUsuId() {
        return usuId;
    }

    /**
     * @param usuId the usuId to set
     */
    public void setUsuId(Integer usuId) {
        this.usuId = usuId;
    }

    /**
     * @return the nombreCompleto
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * @param nombreCompleto the nombreCompleto to set
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
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
     * @return the empId
     */
    public Integer getEmpId() {
        return empId;
    }

    /**
     * @param empId the empId to set
     */
    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    /**
     * @return the banId
     */
    public Integer getBanId() {
        return banId;
    }

    /**
     * @param banId the banId to set
     */
    public void setBanId(Integer banId) {
        this.banId = banId;
    }
    
}
