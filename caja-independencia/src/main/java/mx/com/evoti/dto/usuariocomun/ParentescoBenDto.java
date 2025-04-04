/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.usuariocomun;

import java.util.Objects;

/**
 *
 * @author Venette
 */
public class ParentescoBenDto {
    private Integer parId;
    private String parNombre;

    @Override
    public String toString() {
        return parId+"-"+parNombre;
    }

    public ParentescoBenDto() {
        
    }

    public ParentescoBenDto(Integer parId, String parNombre) {
        this.parId = parId;
        this.parNombre = parNombre;
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
        final ParentescoBenDto other = (ParentescoBenDto) obj;
        if (!Objects.equals(this.parNombre, other.parNombre)) {
            return false;
        }
        if (!Objects.equals(this.parId, other.parId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.parId);
        hash = 67 * hash + Objects.hashCode(this.parNombre);
        return hash;
    }

    /**
     * @return the parId
     */
    public Integer getParId() {
        return parId;
    }

    /**
     * @param parId the parId to set
     */
    public void setParId(Integer parId) {
        this.parId = parId;
    }

    /**
     * @return the parNombre
     */
    public String getParNombre() {
        return parNombre;
    }

    /**
     * @param parNombre the parNombre to set
     */
    public void setParNombre(String parNombre) {
        this.parNombre = parNombre;
    }
    
    
}
