/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.CatorcenasDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CatorcenaDto;

/**
 *
 * @author Venette
 */
public class CatorcenasBo implements Serializable{
    
    private static final long serialVersionUID = -2633117228578426310L;
    
    private CatorcenasDao catDao;

    public CatorcenasBo() {
        catDao = new CatorcenasDao();
    }
    
    
    /**
     * Obtiene todas las 6 catorcenas siguientes a una fecha dada
     *
     * @param fecha
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public List<String> getCatorcenasSiguientes(Date fecha) throws BusinessException {
        try {
            return catDao.getCatorcenasSiguientes(fecha);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    public Date getPrimerCatorcenaMesAno(int year, int month) throws BusinessException {
        try {
            CatorcenaDto catorcena = catDao.getPrimerCatorcenaMesAno(year, month);
            
            return catorcena.getCarFecha();
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
}
