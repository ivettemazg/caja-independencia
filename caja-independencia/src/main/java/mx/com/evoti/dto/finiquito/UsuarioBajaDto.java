/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.finiquito;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Venette
 */
public class UsuarioBajaDto implements Serializable{

    private static final long serialVersionUID = -1962748276548628378L;
     private Integer usuId;
     private Integer usuClaveEmpleado;
     private String usuNombreCompleto;
     private Date usuFechaIngreso;
     private Date usuFechaNacimiento;
     private Date usuFechaIngresoEmpresa;
     private Date usuFechaBaja;
     private String usuNumint;
     private Integer usuEstatus;
     private Integer usuRol;
     private String empAbreviacion;
     private Double baeDeudaCreditos;
     private Double baeAhorros;

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
     * @return the usuFechaIngreso
     */
    public Date getUsuFechaIngreso() {
        return usuFechaIngreso;
    }

    /**
     * @param usuFechaIngreso the usuFechaIngreso to set
     */
    public void setUsuFechaIngreso(Date usuFechaIngreso) {
        this.usuFechaIngreso = usuFechaIngreso;
    }

    /**
     * @return the usuFechaNacimiento
     */
    public Date getUsuFechaNacimiento() {
        return usuFechaNacimiento;
    }

    /**
     * @param usuFechaNacimiento the usuFechaNacimiento to set
     */
    public void setUsuFechaNacimiento(Date usuFechaNacimiento) {
        this.usuFechaNacimiento = usuFechaNacimiento;
    }

    /**
     * @return the usuFechaIngresoEmpresa
     */
    public Date getUsuFechaIngresoEmpresa() {
        return usuFechaIngresoEmpresa;
    }

    /**
     * @param usuFechaIngresoEmpresa the usuFechaIngresoEmpresa to set
     */
    public void setUsuFechaIngresoEmpresa(Date usuFechaIngresoEmpresa) {
        this.usuFechaIngresoEmpresa = usuFechaIngresoEmpresa;
    }

    /**
     * @return the usuFechaBaja
     */
    public Date getUsuFechaBaja() {
        return usuFechaBaja;
    }

    /**
     * @param usuFechaBaja the usuFechaBaja to set
     */
    public void setUsuFechaBaja(Date usuFechaBaja) {
        this.usuFechaBaja = usuFechaBaja;
    }

    /**
     * @return the usuNumint
     */
    public String getUsuNumint() {
        return usuNumint;
    }

    /**
     * @param usuNumint the usuNumint to set
     */
    public void setUsuNumint(String usuNumint) {
        this.usuNumint = usuNumint;
    }

    /**
     * @return the usuEstatus
     */
    public Integer getUsuEstatus() {
        return usuEstatus;
    }

    /**
     * @param usuEstatus the usuEstatus to set
     */
    public void setUsuEstatus(Integer usuEstatus) {
        this.usuEstatus = usuEstatus;
    }

    /**
     * @return the usuRol
     */
    public Integer getUsuRol() {
        return usuRol;
    }

    /**
     * @param usuRol the usuRol to set
     */
    public void setUsuRol(Integer usuRol) {
        this.usuRol = usuRol;
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
     * @return the baeDeudaCreditos
     */
    public Double getBaeDeudaCreditos() {
        return baeDeudaCreditos;
    }

    /**
     * @param baeDeudaCreditos the baeDeudaCreditos to set
     */
    public void setBaeDeudaCreditos(Double baeDeudaCreditos) {
        this.baeDeudaCreditos = baeDeudaCreditos;
    }

    /**
     * @return the baeAhorros
     */
    public Double getBaeAhorros() {
        return baeAhorros;
    }

    /**
     * @param baeAhorros the baeAhorros to set
     */
    public void setBaeAhorros(Double baeAhorros) {
        this.baeAhorros = baeAhorros;
    }

    /**
     * @return the usuNombreCompleto
     */
    public String getUsuNombreCompleto() {
        return usuNombreCompleto;
    }

    /**
     * @param usuNombreCompleto the usuNombreCompleto to set
     */
    public void setUsuNombreCompleto(String usuNombreCompleto) {
        this.usuNombreCompleto = usuNombreCompleto;
    }
}
