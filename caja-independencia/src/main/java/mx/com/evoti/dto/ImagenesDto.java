/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

/**
 *
 * @author Ivette Manzano
 */
public class ImagenesDto implements Serializable{
    
    private String imagen;
    private InputStream isImg;
    private Integer tipoImagen;
    private Integer imaEstatus;
    private File file;
    private Long imaSolicitud;
    

   

    /**
     * @return the tipoImagen
     */
    public Integer getTipoImagen() {
        return tipoImagen;
    }

    /**
     * @param tipoImagen the tipoImagen to set
     */
    public void setTipoImagen(Integer tipoImagen) {
        this.tipoImagen = tipoImagen;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    
    /**
     * @return the isImg
     */
    public InputStream getIsImg() {
        return isImg;
    }

    /**
     * @param isImg the isImg to set
     */
    public void setIsImg(InputStream isImg) {
        this.isImg = isImg;
    }

    /**
     * @return the imagen
     */
    public String getImagen() {
        return imagen;
    }

    /**
     * @param imagen the imagen to set
     */
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    /**
     * @return the imaSolicitud
     */
    public Long getImaSolicitud() {
        return imaSolicitud;
    }

    /**
     * @param imaSolicitud the imaSolicitud to set
     */
    public void setImaSolicitud(Long imaSolicitud) {
        this.imaSolicitud = imaSolicitud;
    }

    /**
     * @return the imaEstatus
     */
    public Integer getImaEstatus() {
        return imaEstatus;
    }

    /**
     * @param imaEstatus the imaEstatus to set
     */
    public void setImaEstatus(Integer imaEstatus) {
        this.imaEstatus = imaEstatus;
    }

}
