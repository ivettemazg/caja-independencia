/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.usuariocomun;

import java.io.Serializable;

/**
 *
 * @author Venette
 */
public class DocumentosDto implements Serializable{

    private static final long serialVersionUID = -6947474536901883709L;
    private Integer idImagen;
    private String nombreDocumento;
    private String tipoDocumento;
    private String strStatus;
    private Integer iStatus;
    private String observaciones;
    private String rutaArchivo;
    private Integer iTipoDocumento;

    /**
     * @return the tipoDocumento
     */
    public String getTipoDocumento() {
        return tipoDocumento;
    }

    /**
     * @param tipoDocumento the tipoDocumento to set
     */
    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    /**
     * @return the strStatus
     */
    public String getStrStatus() {
        return strStatus;
    }

    /**
     * @param strStatus the strStatus to set
     */
    public void setStrStatus(String strStatus) {
        this.strStatus = strStatus;
    }

    /**
     * @return the iStatus
     */
    public Integer getiStatus() {
        return iStatus;
    }

    /**
     * @param iStatus the iStatus to set
     */
    public void setiStatus(Integer iStatus) {
        this.iStatus = iStatus;
    }

    /**
     * @return the observaciones
     */
    public String getObservaciones() {
        return observaciones;
    }

    /**
     * @param observaciones the observaciones to set
     */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * @return the rutaArchivo
     */
    public String getRutaArchivo() {
        return rutaArchivo;
    }

    /**
     * @param rutaArchivo the rutaArchivo to set
     */
    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    /**
     * @return the iTipoDocumento
     */
    public Integer getiTipoDocumento() {
        return iTipoDocumento;
    }

    /**
     * @param iTipoDocumento the iTipoDocumento to set
     */
    public void setiTipoDocumento(Integer iTipoDocumento) {
        this.iTipoDocumento = iTipoDocumento;
    }

    /**
     * @return the nombreDocumento
     */
    public String getNombreDocumento() {
        return nombreDocumento;
    }

    /**
     * @param nombreDocumento the nombreDocumento to set
     */
    public void setNombreDocumento(String nombreDocumento) {
        this.nombreDocumento = nombreDocumento;
    }

    /**
     * @return the idImagen
     */
    public Integer getIdImagen() {
        return idImagen;
    }

    /**
     * @param idImagen the idImagen to set
     */
    public void setIdImagen(Integer idImagen) {
        this.idImagen = idImagen;
    }
    
}
