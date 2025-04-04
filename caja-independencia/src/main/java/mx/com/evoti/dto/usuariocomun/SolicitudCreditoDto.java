/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.usuariocomun;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author Venette
 */
public class SolicitudCreditoDto implements Serializable {

    private static final long serialVersionUID = -6541235054726240723L;
    
     private int solNumero;
     private BigInteger solId;
     private Integer solClaveEmpleado;
     private Double solSueldoNeto;
     private Double solDeducciones;
     private Double solMontoSolicitado;
     private Integer solCatorcenas;
     private Double solPagoCredito;
     private String solBanco;
     private String solNumeroCuenta;
     private String solClabeInterbancaria;
     private Integer solEstatus;
     private String solEstatusStr;
     private String solNombreTarjetahabiente;
     private Double solPagoTotal;
     private Integer solProducto;
     private Double solAguinaldo;
     private Double solFa;
     private Double solIntereses;
     private String solObservacion;
     private Date solFechaUltCatorcena;
     private Date solFechaAutorizacion;
     private Date solFechaCreacion;
     private Date solFechaCancelacion;
     private Date solFechaFondeo;
     private Date solFechaEnviodocumentos;
     private Integer solFacatorcena;
     private Date solFechaDeposito;
     private String solMotivoRechazo;
     private Integer solUsuId;
     private Integer solEstatusDB;
     private String proDescripcion;
     
     private String nombreEmpleado;

    /**
     * @return the solNumero
     */
    public int getSolNumero() {
        return solNumero;
    }

    /**
     * @param solNumero the solNumero to set
     */
    public void setSolNumero(int solNumero) {
        this.solNumero = solNumero;
    }

    /**
     * @return the solId
     */
    public BigInteger getSolId() {
        return solId;
    }

    /**
     * @param solId the solId to set
     */
    public void setSolId(BigInteger solId) {
        this.solId = solId;
    }

    /**
     * @return the solClaveEmpleado
     */
    public Integer getSolClaveEmpleado() {
        return solClaveEmpleado;
    }

    /**
     * @param solClaveEmpleado the solClaveEmpleado to set
     */
    public void setSolClaveEmpleado(Integer solClaveEmpleado) {
        this.solClaveEmpleado = solClaveEmpleado;
    }

    /**
     * @return the solSueldoNeto
     */
    public Double getSolSueldoNeto() {
        return solSueldoNeto;
    }

    /**
     * @param solSueldoNeto the solSueldoNeto to set
     */
    public void setSolSueldoNeto(Double solSueldoNeto) {
        this.solSueldoNeto = solSueldoNeto;
    }

    /**
     * @return the solDeducciones
     */
    public Double getSolDeducciones() {
        return solDeducciones;
    }

    /**
     * @param solDeducciones the solDeducciones to set
     */
    public void setSolDeducciones(Double solDeducciones) {
        this.solDeducciones = solDeducciones;
    }

    /**
     * @return the solMontoSolicitado
     */
    public Double getSolMontoSolicitado() {
        return solMontoSolicitado;
    }

    /**
     * @param solMontoSolicitado the solMontoSolicitado to set
     */
    public void setSolMontoSolicitado(Double solMontoSolicitado) {
        this.solMontoSolicitado = solMontoSolicitado;
    }

    /**
     * @return the solCatorcenas
     */
    public Integer getSolCatorcenas() {
        return solCatorcenas;
    }

    /**
     * @param solCatorcenas the solCatorcenas to set
     */
    public void setSolCatorcenas(Integer solCatorcenas) {
        this.solCatorcenas = solCatorcenas;
    }

    /**
     * @return the solPagoCredito
     */
    public Double getSolPagoCredito() {
        return solPagoCredito;
    }

    /**
     * @param solPagoCredito the solPagoCredito to set
     */
    public void setSolPagoCredito(Double solPagoCredito) {
        this.solPagoCredito = solPagoCredito;
    }

    /**
     * @return the solBanco
     */
    public String getSolBanco() {
        return solBanco;
    }

    /**
     * @param solBanco the solBanco to set
     */
    public void setSolBanco(String solBanco) {
        this.solBanco = solBanco;
    }

    /**
     * @return the solNumeroCuenta
     */
    public String getSolNumeroCuenta() {
        return solNumeroCuenta;
    }

    /**
     * @param solNumeroCuenta the solNumeroCuenta to set
     */
    public void setSolNumeroCuenta(String solNumeroCuenta) {
        this.solNumeroCuenta = solNumeroCuenta;
    }

    /**
     * @return the solClabeInterbancaria
     */
    public String getSolClabeInterbancaria() {
        return solClabeInterbancaria;
    }

    /**
     * @param solClabeInterbancaria the solClabeInterbancaria to set
     */
    public void setSolClabeInterbancaria(String solClabeInterbancaria) {
        this.solClabeInterbancaria = solClabeInterbancaria;
    }

    /**
     * @return the solEstatus
     */
    public Integer getSolEstatus() {
        return solEstatus;
    }

    /**
     * @param solEstatus the solEstatus to set
     */
    public void setSolEstatus(Integer solEstatus) {
        this.solEstatus = solEstatus;
    }

    /**
     * @return the solNombreTarjetahabiente
     */
    public String getSolNombreTarjetahabiente() {
        return solNombreTarjetahabiente;
    }

    /**
     * @param solNombreTarjetahabiente the solNombreTarjetahabiente to set
     */
    public void setSolNombreTarjetahabiente(String solNombreTarjetahabiente) {
        this.solNombreTarjetahabiente = solNombreTarjetahabiente;
    }

    /**
     * @return the solPagoTotal
     */
    public Double getSolPagoTotal() {
        return solPagoTotal;
    }

    /**
     * @param solPagoTotal the solPagoTotal to set
     */
    public void setSolPagoTotal(Double solPagoTotal) {
        this.solPagoTotal = solPagoTotal;
    }

    /**
     * @return the solProducto
     */
    public Integer getSolProducto() {
        return solProducto;
    }

    /**
     * @param solProducto the solProducto to set
     */
    public void setSolProducto(Integer solProducto) {
        this.solProducto = solProducto;
    }

    /**
     * @return the solAguinaldo
     */
    public Double getSolAguinaldo() {
        return solAguinaldo;
    }

    /**
     * @param solAguinaldo the solAguinaldo to set
     */
    public void setSolAguinaldo(Double solAguinaldo) {
        this.solAguinaldo = solAguinaldo;
    }

    /**
     * @return the solFa
     */
    public Double getSolFa() {
        return solFa;
    }

    /**
     * @param solFa the solFa to set
     */
    public void setSolFa(Double solFa) {
        this.solFa = solFa;
    }

    /**
     * @return the solIntereses
     */
    public Double getSolIntereses() {
        return solIntereses;
    }

    /**
     * @param solIntereses the solIntereses to set
     */
    public void setSolIntereses(Double solIntereses) {
        this.solIntereses = solIntereses;
    }

    /**
     * @return the solObservacion
     */
    public String getSolObservacion() {
        return solObservacion;
    }

    /**
     * @param solObservacion the solObservacion to set
     */
    public void setSolObservacion(String solObservacion) {
        this.solObservacion = solObservacion;
    }

    /**
     * @return the solFechaUltCatorcena
     */
    public Date getSolFechaUltCatorcena() {
        return solFechaUltCatorcena;
    }

    /**
     * @param solFechaUltCatorcena the solFechaUltCatorcena to set
     */
    public void setSolFechaUltCatorcena(Date solFechaUltCatorcena) {
        this.solFechaUltCatorcena = solFechaUltCatorcena;
    }

    /**
     * @return the solFechaAutorizacion
     */
    public Date getSolFechaAutorizacion() {
        return solFechaAutorizacion;
    }

    /**
     * @param solFechaAutorizacion the solFechaAutorizacion to set
     */
    public void setSolFechaAutorizacion(Date solFechaAutorizacion) {
        this.solFechaAutorizacion = solFechaAutorizacion;
    }

    /**
     * @return the solFechaCreacion
     */
    public Date getSolFechaCreacion() {
        return solFechaCreacion;
    }

    /**
     * @param solFechaCreacion the solFechaCreacion to set
     */
    public void setSolFechaCreacion(Date solFechaCreacion) {
        this.solFechaCreacion = solFechaCreacion;
    }

    /**
     * @return the solFechaCancelacion
     */
    public Date getSolFechaCancelacion() {
        return solFechaCancelacion;
    }

    /**
     * @param solFechaCancelacion the solFechaCancelacion to set
     */
    public void setSolFechaCancelacion(Date solFechaCancelacion) {
        this.solFechaCancelacion = solFechaCancelacion;
    }

    /**
     * @return the solFechaFondeo
     */
    public Date getSolFechaFondeo() {
        return solFechaFondeo;
    }

    /**
     * @param solFechaFondeo the solFechaFondeo to set
     */
    public void setSolFechaFondeo(Date solFechaFondeo) {
        this.solFechaFondeo = solFechaFondeo;
    }

    /**
     * @return the solFechaEnviodocumentos
     */
    public Date getSolFechaEnviodocumentos() {
        return solFechaEnviodocumentos;
    }

    /**
     * @param solFechaEnviodocumentos the solFechaEnviodocumentos to set
     */
    public void setSolFechaEnviodocumentos(Date solFechaEnviodocumentos) {
        this.solFechaEnviodocumentos = solFechaEnviodocumentos;
    }

    /**
     * @return the solFacatorcena
     */
    public Integer getSolFacatorcena() {
        return solFacatorcena;
    }

    /**
     * @param solFacatorcena the solFacatorcena to set
     */
    public void setSolFacatorcena(Integer solFacatorcena) {
        this.solFacatorcena = solFacatorcena;
    }

    /**
     * @return the solFechaDeposito
     */
    public Date getSolFechaDeposito() {
        return solFechaDeposito;
    }

    /**
     * @param solFechaDeposito the solFechaDeposito to set
     */
    public void setSolFechaDeposito(Date solFechaDeposito) {
        this.solFechaDeposito = solFechaDeposito;
    }

    /**
     * @return the solMotivoRechazo
     */
    public String getSolMotivoRechazo() {
        return solMotivoRechazo;
    }

    /**
     * @param solMotivoRechazo the solMotivoRechazo to set
     */
    public void setSolMotivoRechazo(String solMotivoRechazo) {
        this.solMotivoRechazo = solMotivoRechazo;
    }

    /**
     * @return the solUsuId
     */
    public Integer getSolUsuId() {
        return solUsuId;
    }

    /**
     * @param solUsuId the solUsuId to set
     */
    public void setSolUsuId(Integer solUsuId) {
        this.solUsuId = solUsuId;
    }

    /**
     * @return the solEstatusDB
     */
    public Integer getSolEstatusDB() {
        return solEstatusDB;
    }

    /**
     * @param solEstatusDB the solEstatusDB to set
     */
    public void setSolEstatusDB(Integer solEstatusDB) {
        this.solEstatusDB = solEstatusDB;
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

    /**
     * @return the solEstatusStr
     */
    public String getSolEstatusStr() {
        return solEstatusStr;
    }

    /**
     * @param solEstatusStr the solEstatusStr to set
     */
    public void setSolEstatusStr(String solEstatusStr) {
        this.solEstatusStr = solEstatusStr;
    }

    /**
     * @return the proDescripcion
     */
    public String getProDescripcion() {
        return proDescripcion;
    }

    /**
     * @param proDescripcion the proDescripcion to set
     */
    public void setProDescripcion(String proDescripcion) {
        this.proDescripcion = proDescripcion;
    }
}
