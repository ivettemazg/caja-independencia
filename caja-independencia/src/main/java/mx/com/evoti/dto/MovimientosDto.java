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
public class MovimientosDto implements Serializable{

    private static final long serialVersionUID = -1845054397256035329L;
    
    private Integer movId;
     private Integer movUsuId;
     private Date movFecha;
     private Double movDeposito;
     private Integer movProducto;
     private Integer movClaveEmpleado;
     private Integer movEmpresa;
     private String movTipo;
     private Integer movArhId;
     private String movNombreEmpleado;
     private Integer movIdPadre;
     private Integer movEstatus;
     private Integer movAr;
     private Integer movBandera;
     private String movProductoStr;
     private Double totalMovimiento;
     private String empAbreviacion;
     private Integer movCambioanfaf;


     /**
      * Se utilizan en las tablas cuando se van a realizar devoluciones
      */
     private Double devolucion;
     private Date devolucionFecha;

    /**
     * @return the movId
     */
    public Integer getMovId() {
        return movId;
    }

    /**
     * @param movId the movId to set
     */
    public void setMovId(Integer movId) {
        this.movId = movId;
    }

    /**
     * @return the movFecha
     */
    public Date getMovFecha() {
        return movFecha;
    }

    /**
     * @param movFecha the movFecha to set
     */
    public void setMovFecha(Date movFecha) {
        this.movFecha = movFecha;
    }

    /**
     * @return the movDeposito
     */
    public Double getMovDeposito() {
        return movDeposito;
    }

    /**
     * @param movDeposito the movDeposito to set
     */
    public void setMovDeposito(Double movDeposito) {
        this.movDeposito = movDeposito;
    }

    /**
     * @return the movProducto
     */
    public Integer getMovProducto() {
        return movProducto;
    }

    /**
     * @param movProducto the movProducto to set
     */
    public void setMovProducto(Integer movProducto) {
        this.movProducto = movProducto;
    }

    /**
     * @return the movClaveEmpleado
     */
    public Integer getMovClaveEmpleado() {
        return movClaveEmpleado;
    }

    /**
     * @param movClaveEmpleado the movClaveEmpleado to set
     */
    public void setMovClaveEmpleado(Integer movClaveEmpleado) {
        this.movClaveEmpleado = movClaveEmpleado;
    }

    /**
     * @return the movEmpresa
     */
    public Integer getMovEmpresa() {
        return movEmpresa;
    }

    /**
     * @param movEmpresa the movEmpresa to set
     */
    public void setMovEmpresa(Integer movEmpresa) {
        this.movEmpresa = movEmpresa;
    }

    /**
     * @return the movTipo
     */
    public String getMovTipo() {
        return movTipo;
    }

    /**
     * @param movTipo the movTipo to set
     */
    public void setMovTipo(String movTipo) {
        this.movTipo = movTipo;
    }

    /**
     * @return the movArhId
     */
    public Integer getMovArhId() {
        return movArhId;
    }

    /**
     * @param movArhId the movArhId to set
     */
    public void setMovArhId(Integer movArhId) {
        this.movArhId = movArhId;
    }

    /**
     * @return the movNombreEmpleado
     */
    public String getMovNombreEmpleado() {
        return movNombreEmpleado;
    }

    /**
     * @param movNombreEmpleado the movNombreEmpleado to set
     */
    public void setMovNombreEmpleado(String movNombreEmpleado) {
        this.movNombreEmpleado = movNombreEmpleado;
    }

    /**
     * @return the movIdPadre
     */
    public Integer getMovIdPadre() {
        return movIdPadre;
    }

    /**
     * @param movIdPadre the movIdPadre to set
     */
    public void setMovIdPadre(Integer movIdPadre) {
        this.movIdPadre = movIdPadre;
    }

    /**
     * @return the movEstatus
     */
    public Integer getMovEstatus() {
        return movEstatus;
    }

    /**
     * @param movEstatus the movEstatus to set
     */
    public void setMovEstatus(Integer movEstatus) {
        this.movEstatus = movEstatus;
    }

    /**
     * @return the movAr
     */
    public Integer getMovAr() {
        return movAr;
    }

    /**
     * @param movAr the movAr to set
     */
    public void setMovAr(Integer movAr) {
        this.movAr = movAr;
    }

    /**
     * @return the movBandera
     */
    public Integer getMovBandera() {
        return movBandera;
    }

    /**
     * @param movBandera the movBandera to set
     */
    public void setMovBandera(Integer movBandera) {
        this.movBandera = movBandera;
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
     * @return the movUsuId
     */
    public Integer getMovUsuId() {
        return movUsuId;
    }

    /**
     * @param movUsuId the movUsuId to set
     */
    public void setMovUsuId(Integer movUsuId) {
        this.movUsuId = movUsuId;
    }

    /**
     * @return the movProductoStr
     */
    public String getMovProductoStr() {
        return movProductoStr;
    }

    /**
     * @param movProductoStr the movProductoStr to set
     */
    public void setMovProductoStr(String movProductoStr) {
        this.movProductoStr = movProductoStr;
    }

    /**
     * @return the totalMovimiento
     */
    public Double getTotalMovimiento() {
        return totalMovimiento;
    }

    /**
     * @param totalMovimiento the totalMovimiento to set
     */
    public void setTotalMovimiento(Double totalMovimiento) {
        this.totalMovimiento = totalMovimiento;
    }

    /**
     * @return the devolucion
     */
    public Double getDevolucion() {
        return devolucion;
    }

    /**
     * @param devolucion the devolucion to set
     */
    public void setDevolucion(Double devolucion) {
        this.devolucion = devolucion;
    }

    /**
     * @return the devolucionFecha
     */
    public Date getDevolucionFecha() {
        return devolucionFecha;
    }

    /**
     * @param devolucionFecha the devolucionFecha to set
     */
    public void setDevolucionFecha(Date devolucionFecha) {
        this.devolucionFecha = devolucionFecha;
    }

    /**
     * @return the movCambioanfaf
     */
    public Integer getMovCambioanfaf() {
        return movCambioanfaf;
    }

    /**
     * @param movCambioanfaf the movCambioanfaf to set
     */
    public void setMovCambioanfaf(Integer movCambioanfaf) {
        this.movCambioanfaf = movCambioanfaf;
    }
}
