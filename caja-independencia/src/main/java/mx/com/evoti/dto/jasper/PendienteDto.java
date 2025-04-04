/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.jasper;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author NeOleon Root
 */
public class PendienteDto implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private Double montoSolicitado;
    private Double sueldoQuincenal;
    private Double deducciones;
    private Double calculoMontoSalario;
    private Integer catorcenas;
    private Double montoCatorcenal;
    private Double calculoPagoSalario;
    private Long idSolicitud;
    
    private String nombreSol;
    private Integer claveEmpleadoSol;
    private Integer solProducto;
    private String estadoSol;
    private String municipioSol;
    private String cpSol;
    private String solPaterno;
    private String solMaterno;
    private String coloniaSol;
    private String calleSol;
    private String numextSol;
    private Date fechaNacimientoSol;
    private String rfcSol;
    private String empresaSol;
    private Date fechaIngresoSol;
    private Date fechaUltCatorcena;
    private String telefonoSol;
    private String celularSol;
    private String sexo;
    private Integer solNumero;
    
    private String nombreAva;
    private Integer claveEmpleadoAva;
    private String estadoAva;
    private String municipioAva;
    private String cpAva;
    private String coloniaAva;
    private String calleAva;
    private String numextAva;
    private Date fechaNacimientoAva;
    private String rfcAva;
    private String empresaAva;
    private Date fechaIngresoAva;
    private String telefonoAva;
    private String correoAva;
    private String celularAva;
    private String sexoAva;
    private String direccion;
    
    private String correoSol;
    
    private Double disponible;
    private Double totalInteres;
    
    private String numintSol;
    private Date fechaCreacionSolicitud;
    

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
     * @return the sueldoQuincenal
     */
    public Double getSueldoQuincenal() {
        return sueldoQuincenal;
    }

    /**
     * @param sueldoQuincenal the sueldoQuincenal to set
     */
    public void setSueldoQuincenal(Double sueldoQuincenal) {
        this.sueldoQuincenal = sueldoQuincenal;
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
     * @return the calculoMontoSalario
     */
    public Double getCalculoMontoSalario() {
        return calculoMontoSalario;
    }

    /**
     * @param calculoMontoSalario the calculoMontoSalario to set
     */
    public void setCalculoMontoSalario(Double calculoMontoSalario) {
        this.calculoMontoSalario = calculoMontoSalario;
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
     * @return the montoCatorcenal
     */
    public Double getMontoCatorcenal() {
        return montoCatorcenal;
    }

    /**
     * @param montoCatorcenal the montoCatorcenal to set
     */
    public void setMontoCatorcenal(Double montoCatorcenal) {
        this.montoCatorcenal = montoCatorcenal;
    }

    /**
     * @return the calculoPagoSalario
     */
    public Double getCalculoPagoSalario() {
        return calculoPagoSalario;
    }

    /**
     * @param calculoPagoSalario the calculoPagoSalario to set
     */
    public void setCalculoPagoSalario(Double calculoPagoSalario) {
        this.calculoPagoSalario = calculoPagoSalario;
    }

    /**
     * @return the idSolicitud
     */
    public Long getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * @param idSolicitud the idSolicitud to set
     */
    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /**
     * @return the nombreSol
     */
    public String getNombreSol() {
        return nombreSol;
    }

    /**
     * @param nombreSol the nombreSol to set
     */
    public void setNombreSol(String nombreSol) {
        this.nombreSol = nombreSol;
    }

    /**
     * @return the claveEmpleadoSol
     */
    public Integer getClaveEmpleadoSol() {
        return claveEmpleadoSol;
    }

    /**
     * @param claveEmpleadoSol the claveEmpleadoSol to set
     */
    public void setClaveEmpleadoSol(Integer claveEmpleadoSol) {
        this.claveEmpleadoSol = claveEmpleadoSol;
    }

    /**
     * @return the estadoSol
     */
    public String getEstadoSol() {
        return estadoSol;
    }

    /**
     * @param estadoSol the estadoSol to set
     */
    public void setEstadoSol(String estadoSol) {
        this.estadoSol = estadoSol;
    }

    /**
     * @return the municipioSol
     */
    public String getMunicipioSol() {
        return municipioSol;
    }

    /**
     * @param municipioSol the municipioSol to set
     */
    public void setMunicipioSol(String municipioSol) {
        this.municipioSol = municipioSol;
    }

    /**
     * @return the cpSol
     */
    public String getCpSol() {
        return cpSol;
    }

    /**
     * @param cpSol the cpSol to set
     */
    public void setCpSol(String cpSol) {
        this.cpSol = cpSol;
    }

    /**
     * @return the coloniaSol
     */
    public String getColoniaSol() {
        return coloniaSol;
    }

    /**
     * @param coloniaSol the coloniaSol to set
     */
    public void setColoniaSol(String coloniaSol) {
        this.coloniaSol = coloniaSol;
    }

    /**
     * @return the calleSol
     */
    public String getCalleSol() {
        return calleSol;
    }

    /**
     * @param calleSol the calleSol to set
     */
    public void setCalleSol(String calleSol) {
        this.calleSol = calleSol;
    }

    /**
     * @return the numextSol
     */
    public String getNumextSol() {
        return numextSol;
    }

    /**
     * @param numextSol the numextSol to set
     */
    public void setNumextSol(String numextSol) {
        this.numextSol = numextSol;
    }

    /**
     * @return the fechaNacimientoSol
     */
    public Date getFechaNacimientoSol() {
        return fechaNacimientoSol;
    }

    /**
     * @param fechaNacimientoSol the fechaNacimientoSol to set
     */
    public void setFechaNacimientoSol(Date fechaNacimientoSol) {
        this.fechaNacimientoSol = fechaNacimientoSol;
    }

    /**
     * @return the rfcSol
     */
    public String getRfcSol() {
        return rfcSol;
    }

    /**
     * @param rfcSol the rfcSol to set
     */
    public void setRfcSol(String rfcSol) {
        this.rfcSol = rfcSol;
    }

    /**
     * @return the empresaSol
     */
    public String getEmpresaSol() {
        return empresaSol;
    }

    /**
     * @param empresaSol the empresaSol to set
     */
    public void setEmpresaSol(String empresaSol) {
        this.empresaSol = empresaSol;
    }

    /**
     * @return the fechaIngresoSol
     */
    public Date getFechaIngresoSol() {
        return fechaIngresoSol;
    }

    /**
     * @param fechaIngresoSol the fechaIngresoSol to set
     */
    public void setFechaIngresoSol(Date fechaIngresoSol) {
        this.fechaIngresoSol = fechaIngresoSol;
    }

    /**
     * @return the telefonoSol
     */
    public String getTelefonoSol() {
        return telefonoSol;
    }

    /**
     * @param telefonoSol the telefonoSol to set
     */
    public void setTelefonoSol(String telefonoSol) {
        this.telefonoSol = telefonoSol;
    }

    /**
     * @return the celularSol
     */
    public String getCelularSol() {
        return celularSol;
    }

    /**
     * @param celularSol the celularSol to set
     */
    public void setCelularSol(String celularSol) {
        this.celularSol = celularSol;
    }

    /**
     * @return the sexo
     */
    public String getSexo() {
        return sexo;
    }

    /**
     * @param sexo the sexo to set
     */
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    /**
     * @return the nombreAva
     */
    public String getNombreAva() {
        return nombreAva;
    }

    /**
     * @param nombreAva the nombreAva to set
     */
    public void setNombreAva(String nombreAva) {
        this.nombreAva = nombreAva;
    }

    /**
     * @return the claveEmpleadoAva
     */
    public Integer getClaveEmpleadoAva() {
        return claveEmpleadoAva;
    }

    /**
     * @param claveEmpleadoAva the claveEmpleadoAva to set
     */
    public void setClaveEmpleadoAva(Integer claveEmpleadoAva) {
        this.claveEmpleadoAva = claveEmpleadoAva;
    }

    /**
     * @return the estadoAva
     */
    public String getEstadoAva() {
        return estadoAva;
    }

    /**
     * @param estadoAva the estadoAva to set
     */
    public void setEstadoAva(String estadoAva) {
        this.estadoAva = estadoAva;
    }

    /**
     * @return the municipioAva
     */
    public String getMunicipioAva() {
        return municipioAva;
    }

    /**
     * @param municipioAva the municipioAva to set
     */
    public void setMunicipioAva(String municipioAva) {
        this.municipioAva = municipioAva;
    }

    /**
     * @return the cpAva
     */
    public String getCpAva() {
        return cpAva;
    }

    /**
     * @param cpAva the cpAva to set
     */
    public void setCpAva(String cpAva) {
        this.cpAva = cpAva;
    }

    /**
     * @return the coloniaAva
     */
    public String getColoniaAva() {
        return coloniaAva;
    }

    /**
     * @param coloniaAva the coloniaAva to set
     */
    public void setColoniaAva(String coloniaAva) {
        this.coloniaAva = coloniaAva;
    }

    /**
     * @return the calleAva
     */
    public String getCalleAva() {
        return calleAva;
    }

    /**
     * @param calleAva the calleAva to set
     */
    public void setCalleAva(String calleAva) {
        this.calleAva = calleAva;
    }

    /**
     * @return the numextAva
     */
    public String getNumextAva() {
        return numextAva;
    }

    /**
     * @param numextAva the numextAva to set
     */
    public void setNumextAva(String numextAva) {
        this.numextAva = numextAva;
    }

    /**
     * @return the fechaNacimientoAva
     */
    public Date getFechaNacimientoAva() {
        return fechaNacimientoAva;
    }

    /**
     * @param fechaNacimientoAva the fechaNacimientoAva to set
     */
    public void setFechaNacimientoAva(Date fechaNacimientoAva) {
        this.fechaNacimientoAva = fechaNacimientoAva;
    }

    /**
     * @return the rfcAva
     */
    public String getRfcAva() {
        return rfcAva;
    }

    /**
     * @param rfcAva the rfcAva to set
     */
    public void setRfcAva(String rfcAva) {
        this.rfcAva = rfcAva;
    }

    /**
     * @return the empresaAva
     */
    public String getEmpresaAva() {
        return empresaAva;
    }

    /**
     * @param empresaAva the empresaAva to set
     */
    public void setEmpresaAva(String empresaAva) {
        this.empresaAva = empresaAva;
    }

    /**
     * @return the fechaIngresoAva
     */
    public Date getFechaIngresoAva() {
        return fechaIngresoAva;
    }

    /**
     * @param fechaIngresoAva the fechaIngresoAva to set
     */
    public void setFechaIngresoAva(Date fechaIngresoAva) {
        this.fechaIngresoAva = fechaIngresoAva;
    }

    /**
     * @return the telefonoAva
     */
    public String getTelefonoAva() {
        return telefonoAva;
    }

    /**
     * @param telefonoAva the telefonoAva to set
     */
    public void setTelefonoAva(String telefonoAva) {
        this.telefonoAva = telefonoAva;
    }

    /**
     * @return the celularAva
     */
    public String getCelularAva() {
        return celularAva;
    }

    /**
     * @param celularAva the celularAva to set
     */
    public void setCelularAva(String celularAva) {
        this.celularAva = celularAva;
    }

    /**
     * @return the sexoAva
     */
    public String getSexoAva() {
        return sexoAva;
    }

    /**
     * @param sexoAva the sexoAva to set
     */
    public void setSexoAva(String sexoAva) {
        this.sexoAva = sexoAva;
    }

    /**
     * @return the correoAva
     */
    public String getCorreoAva() {
        return correoAva;
    }

    /**
     * @param correoAva the correoAva to set
     */
    public void setCorreoAva(String correoAva) {
        this.correoAva = correoAva;
    }

    /**
     * @return the direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    /**
     * @return the solPaterno
     */
    public String getSolPaterno() {
        return solPaterno;
    }

    /**
     * @param solPaterno the solPaterno to set
     */
    public void setSolPaterno(String solPaterno) {
        this.solPaterno = solPaterno;
    }

    /**
     * @return the solMaterno
     */
    public String getSolMaterno() {
        return solMaterno;
    }

    /**
     * @param solMaterno the solMaterno to set
     */
    public void setSolMaterno(String solMaterno) {
        this.solMaterno = solMaterno;
    }

    /**
     * @return the correoSol
     */
    public String getCorreoSol() {
        return correoSol;
    }

    /**
     * @param correoSol the correoSol to set
     */
    public void setCorreoSol(String correoSol) {
        this.correoSol = correoSol;
    }

    /**
     * @return the disponible
     */
    public Double getDisponible() {
        return disponible;
    }

    /**
     * @param disponible the disponible to set
     */
    public void setDisponible(Double disponible) {
        this.disponible = disponible;
    }

    /**
     * @return the totalInteres
     */
    public Double getTotalInteres() {
        return totalInteres;
    }

    /**
     * @param totalInteres the totalInteres to set
     */
    public void setTotalInteres(Double totalInteres) {
        this.totalInteres = totalInteres;
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
     * @return the fechaUltCatorcena
     */
    public Date getFechaUltCatorcena() {
        return fechaUltCatorcena;
    }

    /**
     * @param fechaUltCatorcena the fechaUltCatorcena to set
     */
    public void setFechaUltCatorcena(Date fechaUltCatorcena) {
        this.fechaUltCatorcena = fechaUltCatorcena;
    }

    /**
     * @return the numintSol
     */
    public String getNumintSol() {
        return numintSol;
    }

    /**
     * @param numintSol the numintSol to set
     */
    public void setNumintSol(String numintSol) {
        this.numintSol = numintSol;
    }

    /**
     * @return the solNumero
     */
    public Integer getSolNumero() {
        return solNumero;
    }

    /**
     * @param solNumero the solNumero to set
     */
    public void setSolNumero(Integer solNumero) {
        this.solNumero = solNumero;
    }

    /**
     * @return the fechaCreacionSolicitud
     */
    public Date getFechaCreacionSolicitud() {
        return fechaCreacionSolicitud;
    }

    /**
     * @param fechaCreacionSolicitud the fechaCreacionSolicitud to set
     */
    public void setFechaCreacionSolicitud(Date fechaCreacionSolicitud) {
        this.fechaCreacionSolicitud = fechaCreacionSolicitud;
    }
    
    
}