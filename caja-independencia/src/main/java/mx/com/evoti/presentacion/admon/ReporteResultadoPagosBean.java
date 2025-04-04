/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.dao.ReporteResultadosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.ResultadoAmoDto;
import mx.com.evoti.dto.ResultadoPagosDto;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "resultadoPagosBean")
@ViewScoped
public class ReporteResultadoPagosBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteResultadoPagosBean.class);
    private static final long serialVersionUID = -7455685082343704755L;

    private ReporteResultadosDao rrPagDao;
    private boolean rdrTablas;
    private Date fecha;
    private List<ResultadoPagosDto> pagosLst;
    private List<ResultadoPagosDto> pagosLstFiltrados;
    private List<ResultadoAmoDto> amortizacionLst;
    private List<ResultadoAmoDto> amortizacionLstFiltrada;
    private List<ResultadoAmoDto> amortizacionNoPagadosLst;
    private List<ResultadoAmoDto> amoNoPagadosLstFiltradas;

    public ReporteResultadoPagosBean() {
        rrPagDao = new ReporteResultadosDao();
    }

    
    public void obtenerResultados() {
        try {
            LOGGER.info("Dentro de obtenerResultados");
            pagosLst = rrPagDao.obtieneSituacionPagosXCatorcena(fecha);
            amortizacionLst = rrPagDao.obtieneResultadoAmortizacion(fecha);
            amortizacionNoPagadosLst = rrPagDao.obtieneCreditosNoPagados(fecha);
            amortizacionLst.addAll(amortizacionNoPagadosLst);

            if (amortizacionLst.isEmpty() || pagosLst.isEmpty()) {
                super.muestraMensajeError("No hay registros que mostrar en la fecha elegida.", "", null);
            } else {
                rdrTablas = true;
            }
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    /**
     * @return the rdrTablas
     */
    public boolean isRdrTablas() {
        return rdrTablas;
    }

    /**
     * @param rdrTablas the rdrTablas to set
     */
    public void setRdrTablas(boolean rdrTablas) {
        this.rdrTablas = rdrTablas;
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

    /**
     * @return the pagosLst
     */
    public List<ResultadoPagosDto> getPagosLst() {
        return pagosLst;
    }

    /**
     * @param pagosLst the pagosLst to set
     */
    public void setPagosLst(List<ResultadoPagosDto> pagosLst) {
        this.pagosLst = pagosLst;
    }

    /**
     * @return the amortizacionLst
     */
    public List<ResultadoAmoDto> getAmortizacionLst() {
        return amortizacionLst;
    }

    /**
     * @param amortizacionLst the amortizacionLst to set
     */
    public void setAmortizacionLst(List<ResultadoAmoDto> amortizacionLst) {
        this.amortizacionLst = amortizacionLst;
    }

    /**
     * @return the amortizacionNoPagadosLst
     */
    public List<ResultadoAmoDto> getAmortizacionNoPagadosLst() {
        return amortizacionNoPagadosLst;
    }

    /**
     * @param amortizacionNoPagadosLst the amortizacionNoPagadosLst to set
     */
    public void setAmortizacionNoPagadosLst(List<ResultadoAmoDto> amortizacionNoPagadosLst) {
        this.amortizacionNoPagadosLst = amortizacionNoPagadosLst;
    }

    /**
     * @return the amortizacionLstFiltrada
     */
    public List<ResultadoAmoDto> getAmortizacionLstFiltrada() {
        return amortizacionLstFiltrada;
    }

    /**
     * @param amortizacionLstFiltrada the amortizacionLstFiltrada to set
     */
    public void setAmortizacionLstFiltrada(List<ResultadoAmoDto> amortizacionLstFiltrada) {
        this.amortizacionLstFiltrada = amortizacionLstFiltrada;
    }

    /**
     * @return the amoNoPagadosLstFiltradas
     */
    public List<ResultadoAmoDto> getAmoNoPagadosLstFiltradas() {
        return amoNoPagadosLstFiltradas;
    }

    /**
     * @param amoNoPagadosLstFiltradas the amoNoPagadosLstFiltradas to set
     */
    public void setAmoNoPagadosLstFiltradas(List<ResultadoAmoDto> amoNoPagadosLstFiltradas) {
        this.amoNoPagadosLstFiltradas = amoNoPagadosLstFiltradas;
    }

    /**
     * @return the pagosLstFiltrados
     */
    public List<ResultadoPagosDto> getPagosLstFiltrados() {
        return pagosLstFiltrados;
    }

    /**
     * @param pagosLstFiltrados the pagosLstFiltrados to set
     */
    public void setPagosLstFiltrados(List<ResultadoPagosDto> pagosLstFiltrados) {
        this.pagosLstFiltrados = pagosLstFiltrados;
    }

}
