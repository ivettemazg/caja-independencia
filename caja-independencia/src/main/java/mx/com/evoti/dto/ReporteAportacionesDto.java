/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author NeOleon
 */
public class ReporteAportacionesDto implements java.io.Serializable {

    private static final long serialVersionUID = 5897633744462879692L;

    private Integer usuId;
    private String nombreArchivo;
    private Date fechaSubida;
    private Integer estatus;
    private String producto;
    private Double deposito;
    private BigInteger registros;
    private Integer claveEmpleado;
    private String nombre;
    private Date fechaBaja;

    /**
     * @return the nombreArchivo
     */
    public String getNombreArchivo() {
        return nombreArchivo;
    }

    /**
     * @param nombreArchivo the nombreArchivo to set
     */
    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    /**
     * @return the fechaSubida
     */
    public Date getFechaSubida() {
        return fechaSubida;
    }

    /**
     * @param fechaSubida the fechaSubida to set
     */
    public void setFechaSubida(Date fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    /**
     * @return the producto
     */
    public String getProducto() {
        return producto;
    }

    /**
     * @param producto the producto to set
     */
    public void setProducto(String producto) {
        this.producto = producto;
    }

    /**
     * @return the deposito
     */
    public Double getDeposito() {
        return deposito;
    }

    /**
     * @param deposito the deposito to set
     */
    public void setDeposito(Double deposito) {
        this.deposito = deposito;
    }

    /**
     * @return the estatus
     */
    public Integer getEstatus() {
        return estatus;
    }

    /**
     * @param estatus the estatus to set
     */
    public void setEstatus(Integer estatus) {
        this.estatus = estatus;
    }

    /**
     * @return the registros
     */
    public BigInteger getRegistros() {
        return registros;
    }

    /**
     * @param registros the registros to set
     */
    public void setRegistros(BigInteger registros) {
        this.registros = registros;
    }

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

}
