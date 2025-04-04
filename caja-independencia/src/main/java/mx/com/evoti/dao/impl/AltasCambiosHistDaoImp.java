/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.impl;

import java.util.Date;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.AltasCambiosHistDto;

/**
 *
 * @author Venette
 */
public interface AltasCambiosHistDaoImp {
    
    /**
     * Metodo que se utiliza en todas aquellas consultas que requieran traer una lista basada
     * en el estatus de la tabla ALTAS_CAMBIOS_HIST
     * @param estatusReg
     * @return
     * @throws IntegracionException 
     */
    public List<AltasCambiosHistDto> obtenerHistorialCambiosAltas(int estatusReg) throws IntegracionException;
    
    /**
     * Metodo que se utiliza en todas aquellas consultas que requieran traer una lista basada
     * en la catorcena de la tabla ALTAS_CAMBIOS_HIST
     * @param catorcena
     * @return
     * @throws IntegracionException 
     */
    public List<AltasCambiosHistDto> obtenerHistorialCambiosAltas(Date catorcena) throws IntegracionException;
    
}
