/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Venus
 */
public class PagoDto implements Serializable{

    private static final long serialVersionUID = 5739355128448410424L;

    private Integer pagoId;
    private Integer pagClaveEmpleado;
    private Integer pagCredito;
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
    private Integer pagEstId;
    private Integer pagArhId;
    private String pagEstNombre;
    private String pagEstDescripcion;
    private String pagEstColor;
    private String empAbreviacion;
    

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

    /**
     * @return the pagEstId
     */
    public Integer getPagEstId() {
        return pagEstId;
    }

    /**
     * @param pagEstId the pagEstId to set
     */
    public void setPagEstId(Integer pagEstId) {
        this.pagEstId = pagEstId;
    }

    /**
     * @return the pagEstNombre
     */
    public String getPagEstNombre() {
        return pagEstNombre;
    }

    /**
     * @param pagEstNombre the pagEstNombre to set
     */
    public void setPagEstNombre(String pagEstNombre) {
        this.pagEstNombre = pagEstNombre;
    }

    /**
     * @return the pagEstDescripcion
     */
    public String getPagEstDescripcion() {
        return pagEstDescripcion;
    }

    /**
     * @param pagEstDescripcion the pagEstDescripcion to set
     */
    public void setPagEstDescripcion(String pagEstDescripcion) {
        this.pagEstDescripcion = pagEstDescripcion;
    }

    /**
     * @return the pagEstColor
     */
    public String getPagEstColor() {
        return pagEstColor;
    }

    /**
     * @param pagEstColor the pagEstColor to set
     */
    public void setPagEstColor(String pagEstColor) {
        this.pagEstColor = pagEstColor;
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
     * @return the pagArhId
     */
    public Integer getPagArhId() {
        return pagArhId;
    }

    /**
     * @param pagArhId the pagArhId to set
     */
    public void setPagArhId(Integer pagArhId) {
        this.pagArhId = pagArhId;
    }

    /**
     * @return the pagCredito
     */
    public Integer getPagCredito() {
        return pagCredito;
    }

    /**
     * @param pagCredito the pagCredito to set
     */
    public void setPagCredito(Integer pagCredito) {
        this.pagCredito = pagCredito;
    }
    
}
