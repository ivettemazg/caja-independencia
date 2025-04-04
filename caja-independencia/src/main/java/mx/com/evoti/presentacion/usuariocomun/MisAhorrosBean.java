/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.usuariocomun;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.AhorroVoluntarioDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "misMovBean")
@ViewScoped
public class MisAhorrosBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5492268424613416965L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MisAhorrosBean.class);

    private MovimientosBo movBo;
    private List<MovimientosDto> detalleMovs;
    private List<MovimientosDto> ahorroTotal;
    private MovimientosDto movSelected;
    private UsuarioDto usrDto;
    private Double totalAhorro;
    private Double totalDetalle;
    private Boolean rdrFNF;
    private Boolean rdrV;

    private List<AhorroVoluntarioDto> ahorrosVol;
    
    public MisAhorrosBean() {
        movBo = new MovimientosBo();
        usrDto = super.getUsuarioSesion();
    }

    /**
     * Obtiene los totales de los detalleMovs de un usuario
     */
    public void init() {
        if(super.validateUser()){
            try {
                /*
                Si la variable user está nula, significa que viene de Mis Ahorros, por lo tanto
                se asigna la variable en este momento, si no viene nula significa que fue asignada
                desde otro bean y puede venir de Finiquitos o Reporte Personal
                 */

                totalAhorro = 0.0;
                ahorroTotal = movBo.getAhorrosTotales(usrDto.getId());

                ahorroTotal.forEach(x -> {

                    totalAhorro = Util.round(x.getTotalMovimiento() + totalAhorro);

                });

                rdrFNF = Boolean.FALSE;
                rdrV = Boolean.FALSE;

            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    public void verDetalle() {
        try {
            totalDetalle = 0.0;
            //Cuando es ahorro fijo y no fijo
            if (movSelected.getMovProducto() != 3) {
                rdrFNF = Boolean.TRUE;
                rdrV = Boolean.FALSE;
                detalleMovs = movBo.getAhorrosDetalle(usrDto.getId(), movSelected.getMovProducto(), movSelected.getMovAr(), null);

                detalleMovs.forEach(x -> {

                    totalDetalle = Util.round(x.getMovDeposito() + totalDetalle);

                });
            } else {
                rdrV = Boolean.TRUE;
                rdrFNF = Boolean.FALSE;
                this.ahorrosVol = movBo.getAhorrosDetalle(usrDto.getId(), movSelected.getMovProducto(), movSelected.getMovAr(), movSelected.getMovId());

            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the detalleMovs
     */
    public List<MovimientosDto> getDetalleMovs() {
        return detalleMovs;
    }

    /**
     * @param detalleMovs the detalleMovs to set
     */
    public void setDetalleMovs(List<MovimientosDto> detalleMovs) {
        this.detalleMovs = detalleMovs;
    }

    /**
     * @return the movSelected
     */
    public MovimientosDto getMovSelected() {
        return movSelected;
    }

    /**
     * @param movSelected the movSelected to set
     */
    public void setMovSelected(MovimientosDto movSelected) {
        this.movSelected = movSelected;
    }

    /**
     * @return the ahorroTotal
     */
    public List<MovimientosDto> getAhorroTotal() {
        return ahorroTotal;
    }

    /**
     * @param ahorroTotal the ahorroTotal to set
     */
    public void setAhorroTotal(List<MovimientosDto> ahorroTotal) {
        this.ahorroTotal = ahorroTotal;
    }

    /**
     * @return the totalAhorro
     */
    public Double getTotalAhorro() {
        return totalAhorro;
    }

    /**
     * @param totalAhorro the totalAhorro to set
     */
    public void setTotalAhorro(Double totalAhorro) {
        this.totalAhorro = totalAhorro;
    }

    /**
     * @return the totalDetalle
     */
    public Double getTotalDetalle() {
        return totalDetalle;
    }

    /**
     * @param totalDetalle the totalDetalle to set
     */
    public void setTotalDetalle(Double totalDetalle) {
        this.totalDetalle = totalDetalle;
    }

    /**
     * @return the rdrFNF
     */
    public Boolean getRdrFNF() {
        return rdrFNF;
    }

    /**
     * @param rdrFNF the rdrFNF to set
     */
    public void setRdrFNF(Boolean rdrFNF) {
        this.rdrFNF = rdrFNF;
    }

    /**
     * @return the rdrV
     */
    public Boolean getRdrV() {
        return rdrV;
    }

    /**
     * @param rdrV the rdrV to set
     */
    public void setRdrV(Boolean rdrV) {
        this.rdrV = rdrV;
    }

    /**
     * @return the ahorrosVol
     */
    public List<AhorroVoluntarioDto> getAhorrosVol() {
        return ahorrosVol;
    }

    /**
     * @param ahorrosVol the ahorrosVol to set
     */
    public void setAhorrosVol(List<AhorroVoluntarioDto> ahorrosVol) {
        this.ahorrosVol = ahorrosVol;
    }

}
