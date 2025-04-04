/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.jasper;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Venus
 */
public class PagoDto implements Serializable{

    private Integer pagoId;
    private Integer pagClaveEmpleado;
    private Integer pagEmpresa;
    private Integer pagIdEmpleado;
    private Integer pagEstatus;
    private Date pagFecha;
    private Double pagMonto;
    private Double pagAcumulado;
    private Integer pagIdCredito;
    private Integer pagIdArchivo;
    private String pagNombreUsuario;
    private String pagNombreEmpresa;
    private String pagEstatusAmortizacion;
    private String pagEstatusTexto;
    private String estatusColor;
    private String claveCredito;

    /**
     * @return the pagoId
     */
    public Integer getPagoId() {
        return pagoId;
    }

    /**
     * @param pagoId the pagoId to set
     */
    public void setPagoId(Integer pagoId) {
        this.pagoId = pagoId;
    }

    /**
     * @return the pagClaveEmpleado
     */
    public Integer getPagClaveEmpleado() {
        return pagClaveEmpleado;
    }

    /**
     * @param pagClaveEmpleado the pagClaveEmpleado to set
     */
    public void setPagClaveEmpleado(Integer pagClaveEmpleado) {
        this.pagClaveEmpleado = pagClaveEmpleado;
    }

    /**
     * @return the pagEmpresa
     */
    public Integer getPagEmpresa() {
        return pagEmpresa;
    }

    /**
     * @param pagEmpresa the pagEmpresa to set
     */
    public void setPagEmpresa(Integer pagEmpresa) {
        this.pagEmpresa = pagEmpresa;
    }

    /**
     * @return the pagIdEmpleado
     */
    public Integer getPagIdEmpleado() {
        return pagIdEmpleado;
    }

    /**
     * @param pagIdEmpleado the pagIdEmpleado to set
     */
    public void setPagIdEmpleado(Integer pagIdEmpleado) {
        this.pagIdEmpleado = pagIdEmpleado;
    }

    /**
     * @return the pagEstatus
     */
    public Integer getPagEstatus() {
        return pagEstatus;
    }

    /**
     * @param pagEstatus the pagEstatus to set
     */
    public void setPagEstatus(Integer pagEstatus) {
        this.pagEstatus = pagEstatus;
    }

    /**
     * @return the pagFecha
     */
    public Date getPagFecha() {
        return pagFecha;
    }

    /**
     * @param pagFecha the pagFecha to set
     */
    public void setPagFecha(Date pagFecha) {
        this.pagFecha = pagFecha;
    }

    /**
     * @return the pagMonto
     */
    public Double getPagMonto() {
        return pagMonto;
    }

    /**
     * @param pagMonto the pagMonto to set
     */
    public void setPagMonto(Double pagMonto) {
        this.pagMonto = pagMonto;
    }

    /**
     * @return the pagIdCredito
     */
    public Integer getPagIdCredito() {
        return pagIdCredito;
    }

    /**
     * @param pagIdCredito the pagIdCredito to set
     */
    public void setPagIdCredito(Integer pagIdCredito) {
        this.pagIdCredito = pagIdCredito;
    }

    /**
     * @return the pagIdArchivo
     */
    public Integer getPagIdArchivo() {
        return pagIdArchivo;
    }

    /**
     * @param pagIdArchivo the pagIdArchivo to set
     */
    public void setPagIdArchivo(Integer pagIdArchivo) {
        this.pagIdArchivo = pagIdArchivo;
    }

    /**
     * @return the pagNombreUsuario
     */
    public String getPagNombreUsuario() {
        return pagNombreUsuario;
    }

    /**
     * @param pagNombreUsuario the pagNombreUsuario to set
     */
    public void setPagNombreUsuario(String pagNombreUsuario) {
        this.pagNombreUsuario = pagNombreUsuario;
    }

    /**
     * @return the pagNombreEmpresa
     */
    public String getPagNombreEmpresa() {
        return pagNombreEmpresa;
    }

    /**
     * @param pagNombreEmpresa the pagNombreEmpresa to set
     */
    public void setPagNombreEmpresa(String pagNombreEmpresa) {
        this.pagNombreEmpresa = pagNombreEmpresa;
    }

    /**
     * @return the pagEstatusAmortizacion
     */
    public String getPagEstatusAmortizacion() {
        return pagEstatusAmortizacion;
    }

    /**
     * @param pagEstatusAmortizacion the pagEstatusAmortizacion to set
     */
    public void setPagEstatusAmortizacion(String pagEstatusAmortizacion) {
        this.pagEstatusAmortizacion = pagEstatusAmortizacion;
    }

    /**
     * @return the pagEstatusTexto
     */
    public String getPagEstatusTexto() {
        return pagEstatusTexto;
    }

    /**
     * @param pagEstatusTexto the pagEstatusTexto to set
     */
    public void setPagEstatusTexto(String pagEstatusTexto) {
        this.pagEstatusTexto = pagEstatusTexto;
    }

    /**
     * @return the estatusColor
     */
    public String getEstatusColor() {
        return estatusColor;
    }

    /**
     * @param estatusColor the estatusColor to set
     */
    public void setEstatusColor(String estatusColor) {
        this.estatusColor = estatusColor;
    }

    /**
     * @return the claveCredito
     */
    public String getClaveCredito() {
        return claveCredito;
    }

    /**
     * @param claveCredito the claveCredito to set
     */
    public void setClaveCredito(String claveCredito) {
        this.claveCredito = claveCredito;
    }

    /**
     * @return the pagAcumulado
     */
    public Double getPagAcumulado() {
        return pagAcumulado;
    }

    /**
     * @param pagAcumulado the pagAcumulado to set
     */
    public void setPagAcumulado(Double pagAcumulado) {
        this.pagAcumulado = pagAcumulado;
    }
    
}
