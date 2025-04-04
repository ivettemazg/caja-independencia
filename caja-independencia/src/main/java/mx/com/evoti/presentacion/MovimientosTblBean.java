/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "movsTblBean")
@ViewScoped
public class MovimientosTblBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -2518577288245190491L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MovimientosTblBean.class);

    private MovimientosBo movsBo;
    private List<MovimientosDto> movimientos;
    private MovimientosDto movSelected;
    private Usuarios user;

    private String tipoDevolucion;
    private Double montoDevolucion;
    private Date fechaDevolucion;

    private Double saldoCredito;

    /**
     * Si viene del dialog ajusta creditos es 2, si viene de finiquito sin
     * creditos o reporte por empleado es 1
     */
    private Integer origen;

    /**
     * Utilizadas cuando se están haciendo ajustes de crédito en finiquitos
     */
    private Double totalAbonoCredito;
    private List<Movimientos> abonosCredito;

    public MovimientosTblBean() {

        movsBo = new MovimientosBo();
    }

    /**
     * Obtiene los totales de los movimientos de un usuario
     */
    public void obtieneTotalesMovimientos() {
        try {
           
            movimientos = movsBo.getAhorrosByUsuId(user.getUsuId());

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void onEdit(RowEditEvent event) {

        MovimientosDto mov = (MovimientosDto) event.getObject();

        switch (origen) {

            /**
             * Cuando estamos en la pantalla de reporte por empleado o
             * finiquitos sin creditos
             */
            case 1:
                Movimientos pojo = creaDevolucion(mov);
                guardaDevoluciones(pojo);

                RequestContext.getCurrentInstance().update(":frmAhorros:tblAhorros");

                break;

            case 2:
                /**
                 * Valida que el monto de la devolucion no sea mayor al adeudo
                 * del credito, la validacion en que la devolucion no puede ser
                 * mas alta que el monto del ahorro se realizará en pantalla
                 */
                if (mov.getDevolucion() > saldoCredito) {
                    super.muestraMensajeError("El monto de la devolución no puede ser mayor al adeudo del crédito", "", null);
                } else {

                    /**
                     * Si pasa las validaciones, las guarda en una lista para
                     * más tarde insertarlas en la base de datos
                     */
                    if (abonosCredito == null) {
                        abonosCredito = new ArrayList<>();
                    }

                    abonosCredito.add(creaDevolucion(mov));

                    /**
                     * El monto total que se meterá como pago a capital y la
                     * suma de todos los abonos a credito
                     */
                   
                    totalAbonoCredito = (new BigDecimal(totalAbonoCredito).add(new BigDecimal(mov.getDevolucion())).setScale(2, RoundingMode.HALF_DOWN)).doubleValue();
                    saldoCredito = (Util.round(saldoCredito-totalAbonoCredito));
                }

                RequestContext.getCurrentInstance().update(":frmDlgAhorro:tblAhorros :frmDlgAhorro:otAbonoCred :frmDlgAhorro:otAbonoCred:otFiniquito :frmDlgAhorro:otAbonoCred:otAdeudoFinal");
                break;
        }
    }

    /**
     * Crea un objeto de tipo movimiento y lo agrega a la lista de devoluciones
     * que se insertarán posteriormente
     *
     * @param dtoSelected
     * @return
     */
    public Movimientos creaDevolucion(MovimientosDto dtoSelected) {
        Movimientos pojo = new Movimientos();

        pojo.setMovAr(dtoSelected.getMovAr());
        pojo.setMovClaveEmpleado(dtoSelected.getMovClaveEmpleado());
        pojo.setMovDeposito(dtoSelected.getDevolucion() * (-1));
        pojo.setMovEmpresa(dtoSelected.getMovEmpresa());
        pojo.setMovEstatus(0);
        pojo.setMovFecha(dtoSelected.getDevolucionFecha());
        pojo.setMovProducto(dtoSelected.getMovProducto());
        switch (origen) {
            case 1:
                pojo.setMovTipo(Constantes.MOV_TIPO_DEVOLUCION);
                break;
            case 2:
                pojo.setMovTipo(Constantes.MOV_ABONOCREDITO);
                break;

        }
        pojo.setMovUsuId(dtoSelected.getMovUsuId());

        if (pojo.getMovProducto() == 3) {
            pojo.setMovIdPadre(dtoSelected.getMovId());
        }

        return pojo;
    }

    /**
     * Guarda una lista de devoluciones
     *
     * @param mov
     */
    public void guardaDevoluciones(Object mov) {
        try {
            if (mov instanceof Movimientos) {
                movsBo.guardaMovimiento((Movimientos) mov);
            } else {
                movsBo.guardaDevoluciones((List) mov);
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the montoDevolucion
     */
    public Double getMontoDevolucion() {
        return montoDevolucion;
    }

    /**
     * @param montoDevolucion the montoDevolucion to set
     */
    public void setMontoDevolucion(Double montoDevolucion) {
        this.montoDevolucion = montoDevolucion;
    }

    /**
     * @return the fechaDevolucion
     */
    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    /**
     * @param fechaDevolucion the fechaDevolucion to set
     */
    public void setFechaDevolucion(Date fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    /**
     * @return the movimientos
     */
    public List<MovimientosDto> getMovimientos() {
        return movimientos;
    }

    /**
     * @param movimientos the movimientos to set
     */
    public void setMovimientos(List<MovimientosDto> movimientos) {
        this.movimientos = movimientos;
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
     * @return the user
     */
    public Usuarios getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(Usuarios user) {
        this.user = user;
    }

    /**
     * @return the tipoDevolucion
     */
    public String getTipoDevolucion() {
        return tipoDevolucion;
    }

    /**
     * @param tipoDevolucion the tipoDevolucion to set
     */
    public void setTipoDevolucion(String tipoDevolucion) {
        this.tipoDevolucion = tipoDevolucion;
    }

    /**
     * @return the origen
     */
    public Integer getOrigen() {
        return origen;
    }

    /**
     * @param origen the origen to set
     */
    public void setOrigen(Integer origen) {
        this.origen = origen;
    }

    /**
     * @return the abonosCredito
     */
    public List<Movimientos> getAbonosCredito() {
        return abonosCredito;
    }

    /**
     * @param abonosCredito the abonosCredito to set
     */
    public void setAbonosCredito(List<Movimientos> abonosCredito) {
        this.abonosCredito = abonosCredito;
    }

    /**
     * @return the saldoCredito
     */
    public Double getSaldoCredito() {
        return saldoCredito;
    }

    /**
     * @param saldoCredito the saldoCredito to set
     */
    public void setSaldoCredito(Double saldoCredito) {
        this.saldoCredito = saldoCredito;
    }

    /**
     * @return the totalAbonoCredito
     */
    public Double getTotalAbonoCredito() {
        return totalAbonoCredito;
    }

    /**
     * @param totalAbonoCredito the totalAbonoCredito to set
     */
    public void setTotalAbonoCredito(Double totalAbonoCredito) {
        this.totalAbonoCredito = totalAbonoCredito;
    }

}
