/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.Serializable;

/**
 *
 * @author Venus
 */
public class AcumuladoDto implements Serializable {

    private static final long serialVersionUID = 2767580396016989211L;

    private Integer acuUsuarioId;
    private Double acuMonto;

    /**
     * @return the acuId
     */
   
    /**
     * @return the acuUsuarioId
     */
    public Integer getAcuUsuarioId() {
        return acuUsuarioId;
    }

    /**
     * @param acuUsuarioId the acuUsuarioId to set
     */
    public void setAcuUsuarioId(Integer acuUsuarioId) {
        this.acuUsuarioId = acuUsuarioId;
    }

    /**
     * @return the acuMonto
     */
    public Double getAcuMonto() {
        return acuMonto;
    }

    /**
     * @param acuMonto the acuMonto to set
     */
    public void setAcuMonto(Double acuMonto) {
        this.acuMonto = acuMonto;
    }
    
    
}
