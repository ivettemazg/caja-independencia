/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon.pagosaportaciones;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.ReporteAportacionesBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.ReporteAportacionesDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author NeOleon
 */
@ManagedBean(name = "reporteAportacionesBean")
@ViewScoped
public class ReporteAportacionesBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5216692439496179076L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteAportacionesBean.class);

    private ReporteAportacionesBo RepApoBo;
    private List<ReporteAportacionesDto> descripcionArchivo;
    private List<ReporteAportacionesDto> resumenArchivo;
    private List<ReporteAportacionesDto> detalleArchivo;
    private List<ReporteAportacionesDto> usuariosNoRegistrados;
    private List<ReporteAportacionesDto> usuariosConBaja;
    private List<ReporteAportacionesDto> usuariosConBajaSeleccionados;

    private Date fechaCatorcena;
    private Integer empresa;

    public ReporteAportacionesBean() {
        RepApoBo = new ReporteAportacionesBo();
    }

    public void generaReporteAportacionesCatorcenal() {
        try {
            usuariosConBajaSeleccionados.clear();
            descripcionArchivo = RepApoBo.obtieneDatosArchivo(getFechaCatorcena(), getEmpresa());
            resumenArchivo = RepApoBo.obtieneResumenArchivo(getFechaCatorcena(), getEmpresa());
            detalleArchivo = RepApoBo.obtieneDetalleArchivo(getFechaCatorcena(), getEmpresa());
            usuariosNoRegistrados = RepApoBo.obtieneUsuariosNoRegistrados(getFechaCatorcena(), getEmpresa());
            usuariosConBaja = RepApoBo.obtieneUsuariosConBaja(getFechaCatorcena(), getEmpresa());
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

     public void rowSelect() {
        LOGGER.info("rowSelect");
    }

    public void rowUnselect() {
        LOGGER.info("rowUnselect");
    }
    
    public void reactivarUsuarios(){
        try {
            System.out.println("En reactivar usuarios");
            
            RepApoBo.reactivarUsuarios(usuariosConBajaSeleccionados);
            usuariosConBajaSeleccionados.clear();
            generaReporteAportacionesCatorcenal();
            
        } catch (BusinessException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }
        
    }
            
    
    /**
     * @return the descripcionArchivo
     */
    public List<ReporteAportacionesDto> getDescripcionArchivo() {
        return descripcionArchivo;
    }

    /**
     * @param descripcionArchivo the descripcionArchivo to set
     */
    public void setDescripcionArchivo(List<ReporteAportacionesDto> descripcionArchivo) {
        this.descripcionArchivo = descripcionArchivo;
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
     * @return the resumenArchivo
     */
    public List<ReporteAportacionesDto> getResumenArchivo() {
        return resumenArchivo;
    }

    /**
     * @param resumenArchivo the resumenArchivo to set
     */
    public void setResumenArchivo(List<ReporteAportacionesDto> resumenArchivo) {
        this.resumenArchivo = resumenArchivo;
    }

    /**
     * @return the detalleArchivo
     */
    public List<ReporteAportacionesDto> getDetalleArchivo() {
        return detalleArchivo;
    }

    /**
     * @param detalleArchivo the detalleArchivo to set
     */
    public void setDetalleArchivo(List<ReporteAportacionesDto> detalleArchivo) {
        this.detalleArchivo = detalleArchivo;
    }

    /**
     * @return the usuariosNoRegistrados
     */
    public List<ReporteAportacionesDto> getUsuariosNoRegistrados() {
        return usuariosNoRegistrados;
    }

    /**
     * @param usuariosNoRegistrados the usuariosNoRegistrados to set
     */
    public void setUsuariosNoRegistrados(List<ReporteAportacionesDto> usuariosNoRegistrados) {
        this.usuariosNoRegistrados = usuariosNoRegistrados;
    }

    /**
     * @return the usuariosConBaja
     */
    public List<ReporteAportacionesDto> getUsuariosConBaja() {
        return usuariosConBaja;
    }

    /**
     * @param usuariosConBaja the usuariosConBaja to set
     */
    public void setUsuariosConBaja(List<ReporteAportacionesDto> usuariosConBaja) {
        this.usuariosConBaja = usuariosConBaja;
    }

    /**
     * @return the usuariosConBajaSeleccionados
     */
    public List<ReporteAportacionesDto> getUsuariosConBajaSeleccionados() {
        return usuariosConBajaSeleccionados;
    }

    /**
     * @param usuariosConBajaSeleccionados the usuariosConBajaSeleccionados to set
     */
    public void setUsuariosConBajaSeleccionados(List<ReporteAportacionesDto> usuariosConBajaSeleccionados) {
        this.usuariosConBajaSeleccionados = usuariosConBajaSeleccionados;
    }

}
