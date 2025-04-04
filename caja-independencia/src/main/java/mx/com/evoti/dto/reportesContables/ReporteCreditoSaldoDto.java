/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.reportesContables;

import java.util.Date;

/**
 *
 * @author Venette
 */
public class ReporteCreditoSaldoDto {
    private Integer idCredito;
    private String empresa;
    private Double capitalPagado;
    private Double montoPrestamo;
    private Double saldo;
    private Double pagoCatorcenal;
    private Date fechaPrimerPago;
    private Date fecha;
    

    /**
     * @return the idCredito
     */
    public Integer getIdCredito() {
        return idCredito;
    }

    /**
     * @param idCredito the idCredito to set
     */
    public void setIdCredito(Integer idCredito) {
        this.idCredito = idCredito;
    }

    /**
     * @return the capitalPagado
     */
    public Double getCapitalPagado() {
        return capitalPagado;
    }

    /**
     * @param capitalPagado the capitalPagado to set
     */
    public void setCapitalPagado(Double capitalPagado) {
        this.capitalPagado = capitalPagado;
    }

    /**
     * @return the montoPrestamo
     */
    public Double getMontoPrestamo() {
        return montoPrestamo;
    }

    /**
     * @param montoPrestamo the montoPrestamo to set
     */
    public void setMontoPrestamo(Double montoPrestamo) {
        this.montoPrestamo = montoPrestamo;
    }

    /**
     * @return the saldo
     */
    public Double getSaldo() {
        return saldo;
    }

    /**
     * @param saldo the saldo to set
     */
    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    /**
     * @return the pagoCatorcenal
     */
    public Double getPagoCatorcenal() {
        return pagoCatorcenal;
    }

    /**
     * @param pagoCatorcenal the pagoCatorcenal to set
     */
    public void setPagoCatorcenal(Double pagoCatorcenal) {
        this.pagoCatorcenal = pagoCatorcenal;
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
     * @return the fecha
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * @param fecha the fecha to set
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    
    
}
