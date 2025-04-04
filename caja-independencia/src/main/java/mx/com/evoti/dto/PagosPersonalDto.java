/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author NeOleon
 */
public class PagosPersonalDto implements Serializable {

    /**
     * @return the empresa
     */
    public Integer getEmpresa() {
        return empresa;
    }

    /**
     * @param empresa the empresa to set
     */
    public void setEmpresa(Integer empresa) {
        this.empresa = empresa;
    }

    /**
     * @return the usuariosId
     */
    public Integer getUsuariosId() {
        return usuariosId;
    }

    /**
     * @param usuariosId the usuariosId to set
     */
    public void setUsuariosId(Integer usuariosId) {
        this.usuariosId = usuariosId;
    }

    private static final long serialVersionUID = 5739355128448410424L;

    private String estatus;
    private Double deposito;
    private Double acumulado;
    private BigInteger numeroPagos;
    private Integer claveEstatus;
    private BigInteger idPago;
    private Date fechaPago;
    private Date fechaCatorcena;
    private Double amortizacion;
    private Double interes;
    private Double pagoCatorcena;
    private String estatusAmortizacion;
    private String claveCredito;
    private String leyendaAcumulado;
    private Integer claveEmpleado;
    private Double devolucion;
    private Date fechaDevolucion;
        
    private Integer empresa;
    private Integer usuariosId;

    /**
     * @return the estatus
     */
    public String getEstatus() {
        return estatus;
    }

    /**
     * @param estatus the estatus to set
     */
    public void setEstatus(String estatus) {
        this.estatus = estatus;
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
     * @return the acumulado
     */
    public Double getAcumulado() {
        return acumulado;
    }

    /**
     * @param acumulado the acumulado to set
     */
    public void setAcumulado(Double acumulado) {
        this.acumulado = acumulado;
    }

    /**
     * @return the numeroPagos
     */
    public BigInteger getNumeroPagos() {
        return numeroPagos;
    }

    /**
     * @param numeroPagos the numeroPagos to set
     */
    public void setNumeroPagos(BigInteger numeroPagos) {
        this.numeroPagos = numeroPagos;
    }

    /**
     * @return the claveEstatus
     */
    public Integer getClaveEstatus() {
        return claveEstatus;
    }

    /**
     * @param claveEstatus the claveEstatus to set
     */
    public void setClaveEstatus(Integer claveEstatus) {
        this.claveEstatus = claveEstatus;
    }

    /**
     * @return the fechaPago
     */
    public Date getFechaPago() {
        return fechaPago;
    }

    /**
     * @param fechaPago the fechaPago to set
     */
    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    /**
     * @return the fechaCatorcena
     */
    public Date getFechaCatorcena() {
        return fechaCatorcena;
    }

    /**
     * @param fechaCatorcena the fechaCatorcena to set
     */
    public void setFechaCatorcena(Date fechaCatorcena) {
        this.fechaCatorcena = fechaCatorcena;
    }

    /**
     * @return the amortizacion
     */
    public Double getAmortizacion() {
        return amortizacion;
    }

    /**
     * @param amortizacion the amortizacion to set
     */
    public void setAmortizacion(Double amortizacion) {
        this.amortizacion = amortizacion;
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

    /**
     * @return the pagoCatorcena
     */
    public Double getPagoCatorcena() {
        return pagoCatorcena;
    }

    /**
     * @param pagoCatorcena the pagoCatorcena to set
     */
    public void setPagoCatorcena(Double pagoCatorcena) {
        this.pagoCatorcena = pagoCatorcena;
    }

    /**
     * @return the estatusAmortizacion
     */
    public String getEstatusAmortizacion() {
        return estatusAmortizacion;
    }

    /**
     * @param estatusAmortizacion the estatusAmortizacion to set
     */
    public void setEstatusAmortizacion(String estatusAmortizacion) {
        this.estatusAmortizacion = estatusAmortizacion;
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
     * @return the idPago
     */
    public BigInteger getIdPago() {
        return idPago;
    }

    /**
     * @param idPago the idPago to set
     */
    public void setIdPago(BigInteger idPago) {
        this.idPago = idPago;
    }

    /**
     * @return the leyendaAcumulado
     */
    public String getLeyendaAcumulado() {
        return leyendaAcumulado;
    }

    /**
     * @param leyendaAcumulado the leyendaAcumulado to set
     */
    public void setLeyendaAcumulado(String leyendaAcumulado) {
        this.leyendaAcumulado = leyendaAcumulado;
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
     * @return the devolucion
     */
    public Double getDevolucion() {
        return devolucion;
    }

    /**
     * @param devolucion the devolucion to set
     */
    public void setDevolucion(Double devolucion) {
        this.devolucion = devolucion;
    }

    /**
     * @return the fechaDevolucion
     */
    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    /**
     * @param fechaDevolucion the fechaDevolucion to set
     */
    public void setFechaDevolucion(Date fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

}
