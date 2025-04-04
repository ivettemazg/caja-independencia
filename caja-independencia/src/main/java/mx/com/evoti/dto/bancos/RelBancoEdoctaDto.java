/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.bancos;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Venette
 */
public class RelBancoEdoctaDto implements Serializable{

    private static final long serialVersionUID = 1666206842035733559L;
    
     private Long rbeId;
     private Integer rbeIdBancoEc;
     private Integer rbeTipo;
     private Date rbeFechaRel;

    /**
     * @return the rbeId
     */
    public Long getRbeId() {
        return rbeId;
    }

    /**
     * @param rbeId the rbeId to set
     */
    public void setRbeId(Long rbeId) {
        this.rbeId = rbeId;
    }

    /**
     * @return the rbeIdBancoEc
     */
    public Integer getRbeIdBancoEc() {
        return rbeIdBancoEc;
    }

    /**
     * @param rbeIdBancoEc the rbeIdBancoEc to set
     */
    public void setRbeIdBancoEc(Integer rbeIdBancoEc) {
        this.rbeIdBancoEc = rbeIdBancoEc;
    }

    /**
     * @return the rbeTipo
     */
    public Integer getRbeTipo() {
        return rbeTipo;
    }

    /**
     * @param rbeTipo the rbeTipo to set
     */
    public void setRbeTipo(Integer rbeTipo) {
        this.rbeTipo = rbeTipo;
    }

    /**
     * @return the rbeFechaRel
     */
    public Date getRbeFechaRel() {
        return rbeFechaRel;
    }

    /**
     * @param rbeFechaRel the rbeFechaRel to set
     */
    public void setRbeFechaRel(Date rbeFechaRel) {
        this.rbeFechaRel = rbeFechaRel;
    }
    
}
