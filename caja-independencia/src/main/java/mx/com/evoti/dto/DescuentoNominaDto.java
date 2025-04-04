/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.util.Date;

/**
 *
 * @author NeOleon
 */
public class DescuentoNominaDto implements java.io.Serializable {

    /**
     * @return the statusCredito
     */
    public String getStatusCredito() {
        return statusCredito;
    }

    /**
     * @param statusCredito the statusCredito to set
     */
    public void setStatusCredito(String statusCredito) {
        this.statusCredito = statusCredito;
    }
    
    private static final long serialVersionUID = 5897633744462879692L;
    
    private String claveEmpleado;
    private String claveEmpresa;
    private String fechaAMX;
    private String codigoAMX;
    private Integer catorcenas;
    private Double pagoTotal;
    
    private String empresa;
    private Date fechaDeposito;
    private Double prestamo;
    private String producto;
    private String claveCredito;
    private String nombre;
    private String tipoDescuento;
    private Date fechaPrimerPago;
    private Double pagoQuincenal;
    private String statusCredito;
    
    private Double interes;

    /**
     * @return the claveEmpleado
     */
    public String getClaveEmpleado() {
        return claveEmpleado;
    }

    /**
     * @param claveEmpleado the claveEmpleado to set
     */
    public void setClaveEmpleado(String claveEmpleado) {
        this.claveEmpleado = claveEmpleado;
    }

    /**
     * @return the claveEmpresa
     */
    public String getClaveEmpresa() {
        return claveEmpresa;
    }

    /**
     * @param claveEmpresa the claveEmpresa to set
     */
    public void setClaveEmpresa(String claveEmpresa) {
        this.claveEmpresa = claveEmpresa;
    }

    /**
     * @return the fechaAMX
     */
    public String getFechaAMX() {
        return fechaAMX;
    }

    /**
     * @param fechaAMX the fechaAMX to set
     */
    public void setFechaAMX(String fechaAMX) {
        this.fechaAMX = fechaAMX;
    }

    /**
     * @return the codigoAMX
     */
    public String getCodigoAMX() {
        return codigoAMX;
    }

    /**
     * @param codigoAMX the codigoAMX to set
     */
    public void setCodigoAMX(String codigoAMX) {
        this.codigoAMX = codigoAMX;
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
     * @return the pagoTotal
     */
    public Double getPagoTotal() {
        return pagoTotal;
    }

    /**
     * @param pagoTotal the pagoTotal to set
     */
    public void setPagoTotal(Double pagoTotal) {
        this.pagoTotal = pagoTotal;
    }

    /**
     * @return the empresa
     */
    public String getEmpresa() {
        return empresa;
    }

    /**
     * @param empresa the empresa to set
     */
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    /**
     * @return the fechaDeposito
     */
    public Date getFechaDeposito() {
        return fechaDeposito;
    }

    /**
     * @param fechaDeposito the fechaDeposito to set
     */
    public void setFechaDeposito(Date fechaDeposito) {
        this.fechaDeposito = fechaDeposito;
    }

    /**
     * @return the prestamo
     */
    public Double getPrestamo() {
        return prestamo;
    }

    /**
     * @param prestamo the prestamo to set
     */
    public void setPrestamo(Double prestamo) {
        this.prestamo = prestamo;
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
     * @return the claveCredito
     */
    public String getClaveCredito() {
        return claveCredito;
    }

    /**
     * @param claveCredito the claveCredito to set
     */
    public void setClaveCredito(String claveCredito) {
        this.claveCredito = claveCredito;
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
     * @return the tipoDescuento
     */
    public String getTipoDescuento() {
        return tipoDescuento;
    }

    /**
     * @param tipoDescuento the tipoDescuento to set
     */
    public void setTipoDescuento(String tipoDescuento) {
        this.tipoDescuento = tipoDescuento;
    }

    /**
     * @return the fechaPrimerPago
     */
    public Date getFechaPrimerPago() {
        return fechaPrimerPago;
    }

    /**
     * @param fechaPrimerPago the fechaPrimerPago to set
     */
    public void setFechaPrimerPago(Date fechaPrimerPago) {
        this.fechaPrimerPago = fechaPrimerPago;
    }

    /**
     * @return the pagoQuincenal
     */
    public Double getPagoQuincenal() {
        return pagoQuincenal;
    }

    /**
     * @param pagoQuincenal the pagoQuincenal to set
     */
    public void setPagoQuincenal(Double pagoQuincenal) {
        this.pagoQuincenal = pagoQuincenal;
    }

    /**
     * @return the interes
     */
    public Double getInteres() {
        return interes;
    }

    /**
     * @param interes the interes to set
     */
    public void setInteres(Double interes) {
        this.interes = interes;
    }
    
}
