/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.jasper;

import java.util.List;
import mx.com.evoti.dto.common.AmortizacionDto;

/**
 *
 * @author Ivette Manzano
 */
public class ReporteAmortizacionDto {
    
    private List<AmortizacionDto> amortizacion;
    private String nombre;
    private String claveCredito;


    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
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
     * @return the amortizacion
     */
    public List<AmortizacionDto> getAmortizacion() {
        return amortizacion;
    }

    /**
     * @param amortizacion the amortizacion to set
     */
    public void setAmortizacion(List<AmortizacionDto> amortizacion) {
        this.amortizacion = amortizacion;
    }
    
}
