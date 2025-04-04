/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.reportRendto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import mx.com.evoti.util.Util;

/**
 *
 * @author Venette
 */
public class CantidadFechaDto {
   
    private BigDecimal monto;
    private Date fecha;

    public CantidadFechaDto(){
        
    }
    
    public CantidadFechaDto(BigDecimal monto, Date fecha) {
        this.monto = monto;
        this.fecha = fecha;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.monto);
        hash = 29 * hash + Objects.hashCode(this.fecha);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CantidadFechaDto other = (CantidadFechaDto) obj;
        if (!Objects.equals(this.monto, other.monto)) {
            return false;
        }
        if (!Objects.equals(this.fecha, other.fecha)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return monto + "," + Util.generaFechaFormateada(fecha, "dd/MM/yyyy");
    }

    
    
    /**
     * @return the monto
     */
    public BigDecimal getMonto() {
        return monto;
    }

    /**
     * @param monto the monto to set
     */
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
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
    
}
