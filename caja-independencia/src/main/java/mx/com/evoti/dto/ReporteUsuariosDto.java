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
 * @author NeOleon
 */
public class ReporteUsuariosDto implements Serializable {

    private static final long serialVersionUID = 8105167908987459913L;
    
    private Integer claveEmpleado;
    private String nombre;
    private String paterno;
    private String materno;
    private String empresaAbreviacion;
    private String psw;
    private Date fechaIngresoCaja;
    private Date fechaIngresoEmpresa;
    private Date fechaBaja;

    /**
     * @return the claveEmpleado
     */
    public Integer getClaveEmpleado() {
        return claveEmpleado;
    }

    /**
     * @param claveEmpleado the claveEmpleado to set
     */
    public void setClaveEmpleado(Integer claveEmpleado) {
        this.claveEmpleado = claveEmpleado;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the paterno
     */
    public String getPaterno() {
        return paterno;
    }

    /**
     * @param paterno the paterno to set
     */
    public void setPaterno(String paterno) {
        this.paterno = paterno;
    }

    /**
     * @return the materno
     */
    public String getMaterno() {
        return materno;
    }

    /**
     * @param materno the materno to set
     */
    public void setMaterno(String materno) {
        this.materno = materno;
    }

    /**
     * @return the empresaAbreviacion
     */
    public String getEmpresaAbreviacion() {
        return empresaAbreviacion;
    }

    /**
     * @param empresaAbreviacion the empresaAbreviacion to set
     */
    public void setEmpresaAbreviacion(String empresaAbreviacion) {
        this.empresaAbreviacion = empresaAbreviacion;
    }

    /**
     * @return the psw
     */
    public String getPsw() {
        return psw;
    }

    /**
     * @param psw the psw to set
     */
    public void setPsw(String psw) {
        this.psw = psw;
    }

    /**
     * @return the fechaIngresoCaja
     */
    public Date getFechaIngresoCaja() {
        return fechaIngresoCaja;
    }

    /**
     * @param fechaIngresoCaja the fechaIngresoCaja to set
     */
    public void setFechaIngresoCaja(Date fechaIngresoCaja) {
        this.fechaIngresoCaja = fechaIngresoCaja;
    }

    /**
     * @return the fechaIngresoEmpresa
     */
    public Date getFechaIngresoEmpresa() {
        return fechaIngresoEmpresa;
    }

    /**
     * @param fechaIngresoEmpresa the fechaIngresoEmpresa to set
     */
    public void setFechaIngresoEmpresa(Date fechaIngresoEmpresa) {
        this.fechaIngresoEmpresa = fechaIngresoEmpresa;
    }

    /**
     * @return the fechaBaja
     */
    public Date getFechaBaja() {
        return fechaBaja;
    }

    /**
     * @param fechaBaja the fechaBaja to set
     */
    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

}
