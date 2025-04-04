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
 * @author Venette
 */
public class SolicitudDto implements Serializable{

    private static final long serialVersionUID = 5711747877578349616L;
    
     private BigInteger solId;
     private int proId;
     private String proDescripcion;
     private String proSiglas;
     private int solEstId;
     private String solEstNmbrEst;
     private Integer usuId;
     private Integer usuClaveEmpleado;
     private String usuNombreCompleto;
     private String usuNombre;
     private String usuPaterno;
     private String usuMaterno;
     private int empId;
     private String empAbreviacion;
     private String usuEmail;
     private Integer solClaveEmpleado;
     private Double solSueldoNeto;
     private Double solDeducciones;
     private Double solMontoSolicitado;
     private Integer solCatorcenas;
     private Double solPagoCredito;
     private String solBanco;
     private String solNumeroCuenta;
     private String solClabeInterbancaria;
     private String solNombreTarjetahabiente;
     private Double solPagoTotal;
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
     private Integer solEstatusDb;
     private Double tabCatorcenal;
     
     private Double solDisponible;
     private Double solCalcMontoSalario;
     private Double solCalcPagoSalario;
     private Double solMontoCatorcenal;


    /**
     * @return the proId
     */
    public int getProId() {
        return proId;
    }

    /**
     * @param proId the proId to set
     */
    public void setProId(int proId) {
        this.proId = proId;
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

    /**
     * @return the solEstId
     */
    public int getSolEstId() {
        return solEstId;
    }

    /**
     * @param solEstId the solEstId to set
     */
    public void setSolEstId(int solEstId) {
        this.solEstId = solEstId;
    }

    /**
     * @return the solEstNmbrEst
     */
    public String getSolEstNmbrEst() {
        return solEstNmbrEst;
    }

    /**
     * @param solEstNmbrEst the solEstNmbrEst to set
     */
    public void setSolEstNmbrEst(String solEstNmbrEst) {
        this.solEstNmbrEst = solEstNmbrEst;
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

    /**
     * @return the empId
     */
    public int getEmpId() {
        return empId;
    }

    /**
     * @param empId the empId to set
     */
    public void setEmpId(int empId) {
        this.empId = empId;
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
     * @return the solEstatusDb
     */
    public Integer getSolEstatusDb() {
        return solEstatusDb;
    }

    /**
     * @param solEstatusDb the solEstatusDb to set
     */
    public void setSolEstatusDb(Integer solEstatusDb) {
        this.solEstatusDb = solEstatusDb;
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
     * @return the tabCatorcenal
     */
    public Double getTabCatorcenal() {
        return tabCatorcenal;
    }

    /**
     * @param tabCatorcenal the tabCatorcenal to set
     */
    public void setTabCatorcenal(Double tabCatorcenal) {
        this.tabCatorcenal = tabCatorcenal;
    }

    /**
     * @return the solDisponible
     */
    public Double getSolDisponible() {
        return solDisponible;
    }

    /**
     * @param solDisponible the solDisponible to set
     */
    public void setSolDisponible(Double solDisponible) {
        this.solDisponible = solDisponible;
    }

    /**
     * @return the solCalcMontoSalario
     */
    public Double getSolCalcMontoSalario() {
        return solCalcMontoSalario;
    }

    /**
     * @param solCalcMontoSalario the solCalcMontoSalario to set
     */
    public void setSolCalcMontoSalario(Double solCalcMontoSalario) {
        this.solCalcMontoSalario = solCalcMontoSalario;
    }

    /**
     * @return the solCalcPagoSalario
     */
    public Double getSolCalcPagoSalario() {
        return solCalcPagoSalario;
    }

    /**
     * @param solCalcPagoSalario the solCalcPagoSalario to set
     */
    public void setSolCalcPagoSalario(Double solCalcPagoSalario) {
        this.solCalcPagoSalario = solCalcPagoSalario;
    }

    /**
     * @return the solMontoCatorcenal
     */
    public Double getSolMontoCatorcenal() {
        return solMontoCatorcenal;
    }

    /**
     * @param solMontoCatorcenal the solMontoCatorcenal to set
     */
    public void setSolMontoCatorcenal(Double solMontoCatorcenal) {
        this.solMontoCatorcenal = solMontoCatorcenal;
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
     * @return the usuEmail
     */
    public String getUsuEmail() {
        return usuEmail;
    }

    /**
     * @param usuEmail the usuEmail to set
     */
    public void setUsuEmail(String usuEmail) {
        this.usuEmail = usuEmail;
    }

    /**
     * @return the proSiglas
     */
    public String getProSiglas() {
        return proSiglas;
    }

    /**
     * @param proSiglas the proSiglas to set
     */
    public void setProSiglas(String proSiglas) {
        this.proSiglas = proSiglas;
    }

    /**
     * @return the usuNombre
     */
    public String getUsuNombre() {
        return usuNombre;
    }

    /**
     * @param usuNombre the usuNombre to set
     */
    public void setUsuNombre(String usuNombre) {
        this.usuNombre = usuNombre;
    }

    /**
     * @return the usuPaterno
     */
    public String getUsuPaterno() {
        return usuPaterno;
    }

    /**
     * @param usuPaterno the usuPaterno to set
     */
    public void setUsuPaterno(String usuPaterno) {
        this.usuPaterno = usuPaterno;
    }

    /**
     * @return the usuMaterno
     */
    public String getUsuMaterno() {
        return usuMaterno;
    }

    /**
     * @param usuMaterno the usuMaterno to set
     */
    public void setUsuMaterno(String usuMaterno) {
        this.usuMaterno = usuMaterno;
    }
    
}
