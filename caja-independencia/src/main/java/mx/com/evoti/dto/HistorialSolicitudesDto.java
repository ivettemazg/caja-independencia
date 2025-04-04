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
public class HistorialSolicitudesDto implements Serializable {

    private static final long serialVersionUID = 8105167908987459913L;

    private BigInteger folio;
    private Double montoSolicitado;
    private Integer catorcenas;
    private String estatus;
    private Date fechaCreacion;
    private Integer usuClaveEmpleado;
    private String empresa;
    private String nombre;
    private String producto;

    private Double sueldoNeto;
    private Double deducciones;
    private Double pagoCredito;
    private String banco;
    private String numeroCuenta;
    private String clabe;
    private String tarjetaHabiente;
    private Date fechaAutorizacion;
    private Date fechaCancelacion;
    private Date fechaFondeo;
    private Date fechaEnvioDoc;
    private Date fechaDeposito;
    private String motivoRechazo;
    private String referencia;
    private String aseguradora;
    private String poliza;
    private String correo;
    private String telefono;
    private String celular;
    private Date fechaBaja;
    private Integer estatusSol;
    /**
     * @return the correo
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * @param correo the correo to set
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * @return the celular
     */
    public String getCelular() {
        return celular;
    }

    /**
     * @param celular the celular to set
     */
    public void setCelular(String celular) {
        this.celular = celular;
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
     * @return the montoSolicitado
     */
    public Double getMontoSolicitado() {
        return montoSolicitado;
    }

    /**
     * @param montoSolicitado the montoSolicitado to set
     */
    public void setMontoSolicitado(Double montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
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
     * @return the fechaCreacion
     */
    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * @param fechaCreacion the fechaCreacion to set
     */
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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
     * @return the sueldoNeto
     */
    public Double getSueldoNeto() {
        return sueldoNeto;
    }

    /**
     * @param sueldoNeto the sueldoNeto to set
     */
    public void setSueldoNeto(Double sueldoNeto) {
        this.sueldoNeto = sueldoNeto;
    }

    /**
     * @return the deducciones
     */
    public Double getDeducciones() {
        return deducciones;
    }

    /**
     * @param deducciones the deducciones to set
     */
    public void setDeducciones(Double deducciones) {
        this.deducciones = deducciones;
    }

    /**
     * @return the pagoCredito
     */
    public Double getPagoCredito() {
        return pagoCredito;
    }

    /**
     * @param pagoCredito the pagoCredito to set
     */
    public void setPagoCredito(Double pagoCredito) {
        this.pagoCredito = pagoCredito;
    }

    /**
     * @return the banco
     */
    public String getBanco() {
        return banco;
    }

    /**
     * @param banco the banco to set
     */
    public void setBanco(String banco) {
        this.banco = banco;
    }

    /**
     * @return the numeroCuenta
     */
    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    /**
     * @param numeroCuenta the numeroCuenta to set
     */
    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    /**
     * @return the clabe
     */
    public String getClabe() {
        return clabe;
    }

    /**
     * @param clabe the clabe to set
     */
    public void setClabe(String clabe) {
        this.clabe = clabe;
    }

    /**
     * @return the tarjetaHabiente
     */
    public String getTarjetaHabiente() {
        return tarjetaHabiente;
    }

    /**
     * @param tarjetaHabiente the tarjetaHabiente to set
     */
    public void setTarjetaHabiente(String tarjetaHabiente) {
        this.tarjetaHabiente = tarjetaHabiente;
    }

    /**
     * @return the fechaAutorizacion
     */
    public Date getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    /**
     * @param fechaAutorizacion the fechaAutorizacion to set
     */
    public void setFechaAutorizacion(Date fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    /**
     * @return the fechaCancelacion
     */
    public Date getFechaCancelacion() {
        return fechaCancelacion;
    }

    /**
     * @param fechaCancelacion the fechaCancelacion to set
     */
    public void setFechaCancelacion(Date fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    /**
     * @return the fechaFondeo
     */
    public Date getFechaFondeo() {
        return fechaFondeo;
    }

    /**
     * @param fechaFondeo the fechaFondeo to set
     */
    public void setFechaFondeo(Date fechaFondeo) {
        this.fechaFondeo = fechaFondeo;
    }

    /**
     * @return the fechaEnvioDoc
     */
    public Date getFechaEnvioDoc() {
        return fechaEnvioDoc;
    }

    /**
     * @param fechaEnvioDoc the fechaEnvioDoc to set
     */
    public void setFechaEnvioDoc(Date fechaEnvioDoc) {
        this.fechaEnvioDoc = fechaEnvioDoc;
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
     * @return the motivoRechazo
     */
    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    /**
     * @param motivoRechazo the motivoRechazo to set
     */
    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    /**
     * @return the referencia
     */
    public String getReferencia() {
        return referencia;
    }

    /**
     * @param referencia the referencia to set
     */
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    /**
     * @return the aseguradora
     */
    public String getAseguradora() {
        return aseguradora;
    }

    /**
     * @param aseguradora the aseguradora to set
     */
    public void setAseguradora(String aseguradora) {
        this.aseguradora = aseguradora;
    }

    /**
     * @return the poliza
     */
    public String getPoliza() {
        return poliza;
    }

    /**
     * @param poliza the poliza to set
     */
    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    /**
     * @return the folio
     */
    public BigInteger getFolio() {
        return folio;
    }

    /**
     * @param folio the folio to set
     */
    public void setFolio(BigInteger folio) {
        this.folio = folio;
    }

    /**
     * @return the estatusSol
     */
    public Integer getEstatusSol() {
        return estatusSol;
    }

    /**
     * @param estatusSol the estatusSol to set
     */
    public void setEstatusSol(Integer estatusSol) {
        this.estatusSol = estatusSol;
    }

}
