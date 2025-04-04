/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Venette
 */
public class ArchivoDto implements Serializable{

    private static final long serialVersionUID = 3240585073512052724L;
     private Integer arhId;
     private String arhNombreArchivo;
     private Date arhFechaSubida;
     private Integer arhEmpresa;
     private Integer arhEstatus;
     private Integer arhRegistros;
     private Integer arhTipoArchivo;
     private Date arhFechaCatorcena;
     private String empAbreviacion;

    /**
     * @return the arhId
     */
    public Integer getArhId() {
        return arhId;
    }

    /**
     * @param arhId the arhId to set
     */
    public void setArhId(Integer arhId) {
        this.arhId = arhId;
    }

    /**
     * @return the arhNombreArchivo
     */
    public String getArhNombreArchivo() {
        return arhNombreArchivo;
    }

    /**
     * @param arhNombreArchivo the arhNombreArchivo to set
     */
    public void setArhNombreArchivo(String arhNombreArchivo) {
        this.arhNombreArchivo = arhNombreArchivo;
    }

    /**
     * @return the arhFechaSubida
     */
    public Date getArhFechaSubida() {
        return arhFechaSubida;
    }

    /**
     * @param arhFechaSubida the arhFechaSubida to set
     */
    public void setArhFechaSubida(Date arhFechaSubida) {
        this.arhFechaSubida = arhFechaSubida;
    }

    /**
     * @return the arhEmpresa
     */
    public Integer getArhEmpresa() {
        return arhEmpresa;
    }

    /**
     * @param arhEmpresa the arhEmpresa to set
     */
    public void setArhEmpresa(Integer arhEmpresa) {
        this.arhEmpresa = arhEmpresa;
    }

    /**
     * @return the arhEstatus
     */
    public Integer getArhEstatus() {
        return arhEstatus;
    }

    /**
     * @param arhEstatus the arhEstatus to set
     */
    public void setArhEstatus(Integer arhEstatus) {
        this.arhEstatus = arhEstatus;
    }

    /**
     * @return the arhRegistros
     */
    public Integer getArhRegistros() {
        return arhRegistros;
    }

    /**
     * @param arhRegistros the arhRegistros to set
     */
    public void setArhRegistros(Integer arhRegistros) {
        this.arhRegistros = arhRegistros;
    }

    /**
     * @return the arhTipoArchivo
     */
    public Integer getArhTipoArchivo() {
        return arhTipoArchivo;
    }

    /**
     * @param arhTipoArchivo the arhTipoArchivo to set
     */
    public void setArhTipoArchivo(Integer arhTipoArchivo) {
        this.arhTipoArchivo = arhTipoArchivo;
    }

    /**
     * @return the arhFechaCatorcena
     */
    public Date getArhFechaCatorcena() {
        return arhFechaCatorcena;
    }

    /**
     * @param arhFechaCatorcena the arhFechaCatorcena to set
     */
    public void setArhFechaCatorcena(Date arhFechaCatorcena) {
        this.arhFechaCatorcena = arhFechaCatorcena;
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
}
