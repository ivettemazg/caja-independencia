/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dto.common.AmortizacionDto;

/**
 *
 * @author Venette
 */
public class CreditoDto implements Serializable{
    
    private static final long serialVersionUID = 8105167908987459913L;
    
    private Integer creId;
    private Date creFechaDeposito;
    private String creEmpresa;
    private String creNombre;
    private String creTipo;
    private Double crePrestamo;
    private Double saldoTotalPendt;
    private Double capitalPendiente;
    private Integer creCatorcenas;
    private BigInteger creCatorcenasPendts;
    private Date creFechaPrimerPago;
    private Integer creClaveEmpleado;
    private Double crePago;
    private Integer creProducto;
    private Long creSolicitud;
    private Double crePagoQuincenal;
    private Double creSaldo;
    private String creClave;
    private Integer creEstatus;
    private String creEstatusStr;
    private Integer creUsuId;
    private Integer crePadre;
    private BigInteger creSol;
    private Date creFechaNuevoMonto;
    private String nombreEmpleado;
    private List<AmortizacionDto> amortizacionDto;

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
     * @return the creFechaDeposito
     */
    public Date getCreFechaDeposito() {
        return creFechaDeposito;
    }

    /**
     * @param creFechaDeposito the creFechaDeposito to set
     */
    public void setCreFechaDeposito(Date creFechaDeposito) {
        this.creFechaDeposito = creFechaDeposito;
    }

    /**
     * @return the creEmpresa
     */
    public String getCreEmpresa() {
        return creEmpresa;
    }

    /**
     * @param creEmpresa the creEmpresa to set
     */
    public void setCreEmpresa(String creEmpresa) {
        this.creEmpresa = creEmpresa;
    }

    /**
     * @return the creNombre
     */
    public String getCreNombre() {
        return creNombre;
    }

    /**
     * @param creNombre the creNombre to set
     */
    public void setCreNombre(String creNombre) {
        this.creNombre = creNombre;
    }

    /**
     * @return the creTipo
     */
    public String getCreTipo() {
        return creTipo;
    }

    /**
     * @param creTipo the creTipo to set
     */
    public void setCreTipo(String creTipo) {
        this.creTipo = creTipo;
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
    public Integer getCreCatorcenas() {
        return creCatorcenas;
    }

    /**
     * @param creCatorcenas the creCatorcenas to set
     */
    public void setCreCatorcenas(Integer creCatorcenas) {
        this.creCatorcenas = creCatorcenas;
    }

    /**
     * @return the creFechaPrimerPago
     */
    public Date getCreFechaPrimerPago() {
        return creFechaPrimerPago;
    }

    /**
     * @param creFechaPrimerPago the creFechaPrimerPago to set
     */
    public void setCreFechaPrimerPago(Date creFechaPrimerPago) {
        this.creFechaPrimerPago = creFechaPrimerPago;
    }

    /**
     * @return the creClaveEmpleado
     */
    public Integer getCreClaveEmpleado() {
        return creClaveEmpleado;
    }

    /**
     * @param creClaveEmpleado the creClaveEmpleado to set
     */
    public void setCreClaveEmpleado(Integer creClaveEmpleado) {
        this.creClaveEmpleado = creClaveEmpleado;
    }

    /**
     * @return the crePago
     */
    public Double getCrePago() {
        return crePago;
    }

    /**
     * @param crePago the crePago to set
     */
    public void setCrePago(Double crePago) {
        this.crePago = crePago;
    }

    /**
     * @return the creProducto
     */
    public Integer getCreProducto() {
        return creProducto;
    }

    /**
     * @param creProducto the creProducto to set
     */
    public void setCreProducto(Integer creProducto) {
        this.creProducto = creProducto;
    }

    /**
     * @return the creSolicitud
     */
    public Long getCreSolicitud() {
        return creSolicitud;
    }

    /**
     * @param creSolicitud the creSolicitud to set
     */
    public void setCreSolicitud(Long creSolicitud) {
        this.creSolicitud = creSolicitud;
    }

    /**
     * @return the crePagoQuincenal
     */
    public Double getCrePagoQuincenal() {
        return crePagoQuincenal;
    }

    /**
     * @param crePagoQuincenal the crePagoQuincenal to set
     */
    public void setCrePagoQuincenal(Double crePagoQuincenal) {
        this.crePagoQuincenal = crePagoQuincenal;
    }

    /**
     * @return the creSaldo
     */
    public Double getCreSaldo() {
        return creSaldo;
    }

    /**
     * @param creSaldo the creSaldo to set
     */
    public void setCreSaldo(Double creSaldo) {
        this.creSaldo = creSaldo;
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
     * @return the creEstatus
     */
    public Integer getCreEstatus() {
        return creEstatus;
    }

    /**
     * @param creEstatus the creEstatus to set
     */
    public void setCreEstatus(Integer creEstatus) {
        this.creEstatus = creEstatus;
    }

    /**
     * @return the creUsuId
     */
    public Integer getCreUsuId() {
        return creUsuId;
    }

    /**
     * @param creUsuId the creUsuId to set
     */
    public void setCreUsuId(Integer creUsuId) {
        this.creUsuId = creUsuId;
    }

    /**
     * @return the crePadre
     */
    public Integer getCrePadre() {
        return crePadre;
    }

    /**
     * @param crePadre the crePadre to set
     */
    public void setCrePadre(Integer crePadre) {
        this.crePadre = crePadre;
    }

    /**
     * @return the creFechaNuevoMonto
     */
    public Date getCreFechaNuevoMonto() {
        return creFechaNuevoMonto;
    }

    /**
     * @param creFechaNuevoMonto the creFechaNuevoMonto to set
     */
    public void setCreFechaNuevoMonto(Date creFechaNuevoMonto) {
        this.creFechaNuevoMonto = creFechaNuevoMonto;
    }

    /**
     * @return the amortizacionDto
     */
    public List<AmortizacionDto> getAmortizacionDto() {
        return amortizacionDto;
    }

    /**
     * @param amortizacionDto the amortizacionDto to set
     */
    public void setAmortizacionDto(List<AmortizacionDto> amortizacionDto) {
        this.amortizacionDto = amortizacionDto;
    }

    /**
     * @return the creEstatusStr
     */
    public String getCreEstatusStr() {
        return creEstatusStr;
    }

    /**
     * @param creEstatusStr the creEstatusStr to set
     */
    public void setCreEstatusStr(String creEstatusStr) {
        this.creEstatusStr = creEstatusStr;
    }

    /**
     * @return the creCatorcenasPendts
     */
    public BigInteger getCreCatorcenasPendts() {
        return creCatorcenasPendts;
    }

    /**
     * @param creCatorcenasPendts the creCatorcenasPendts to set
     */
    public void setCreCatorcenasPendts(BigInteger creCatorcenasPendts) {
        this.creCatorcenasPendts = creCatorcenasPendts;
    }

    /**
     * @return the saldoTotalPendt
     */
    public Double getSaldoTotalPendt() {
        return saldoTotalPendt;
    }

    /**
     * @param saldoTotalPendt the saldoTotalPendt to set
     */
    public void setSaldoTotalPendt(Double saldoTotalPendt) {
        this.saldoTotalPendt = saldoTotalPendt;
    }

    /**
     * @return the capitalPendiente
     */
    public Double getCapitalPendiente() {
        return capitalPendiente;
    }

    /**
     * @param capitalPendiente the capitalPendiente to set
     */
    public void setCapitalPendiente(Double capitalPendiente) {
        this.capitalPendiente = capitalPendiente;
    }

    /**
     * @return the creSol
     */
    public BigInteger getCreSol() {
        return creSol;
    }

    /**
     * @param creSol the creSol to set
     */
    public void setCreSol(BigInteger creSol) {
        this.creSol = creSol;
    }

    /**
     * @return the nombreEmpleado
     */
    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    /**
     * @param nombreEmpleado the nombreEmpleado to set
     */
    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    
}
