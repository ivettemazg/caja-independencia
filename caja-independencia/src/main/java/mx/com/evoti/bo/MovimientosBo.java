/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.MovimientosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.AhorroVoluntarioDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.RendimientoDto;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.util.Util;

/**
 *
 * @author Venette
 */
public class MovimientosBo implements Serializable {

    private static final long serialVersionUID = -3848296806318411044L;

    private MovimientosDao movDao;

    public MovimientosBo() {
        movDao = new MovimientosDao();
    }

    /**
     * Metodo que obtiene todos los ahorros (Fijo, no fijo, volumntario)
     * agrupados en totales con sus respectivos rendimientos de un usuario dado
     *
     * @param usuId
     * @return
     * @throws BusinessException
     */
    public List<MovimientosDto> getAhorrosByUsuId(Integer usuId) throws BusinessException {
        try {
            //Obtiene ahorro fijo y no fijo
            List<MovimientosDto> aFNF = movDao.getAFANFyR(usuId);

            aFNF.forEach(x -> {
                if (x.getMovProducto().equals(1) && x.getMovAr().equals(1)) {
                    x.setMovProductoStr("FIJO APORTACION");
                   
                } else if (x.getMovProducto().equals(1) && x.getMovAr().equals(2)) {
                    x.setMovProductoStr("FIJO RENDIMIENTO");
                } else if (x.getMovProducto().equals(2) && x.getMovAr().equals(1)) {
                    x.setMovProductoStr("NO FIJO APORTACION");
                } else {
                    x.setMovProductoStr("NO FIJO RENDIMIENTO");
                }

                 x.setTotalMovimiento(Util.round(x.getTotalMovimiento()));
                
            }
            );

            List<MovimientosDto> aV = movDao.getAVyR(usuId);

            //Obtiene ahorro voluntario
            aV.forEach(x -> {
                if (x.getMovProducto().equals(3) && x.getMovAr().equals(1)) {
                    x.setMovProductoStr("VOLUNTARIO APORTACION");
                } else {
                    x.setMovProductoStr("VOLUNTARIO RENDIMIENTO");
                }

                  x.setTotalMovimiento(Util.round(x.getTotalMovimiento()));
            });

            List<MovimientosDto> lstMovimientos = new ArrayList<>();
            lstMovimientos.addAll(aFNF);
            lstMovimientos.addAll(aV);

            return lstMovimientos;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Construye y guarda una lista de registros de devolucion
     *
     * @param pojos
     * @throws BusinessException
     */
    public void guardaDevoluciones(List<Movimientos> pojos) throws BusinessException {

        try {
            movDao.saveMovimientos(pojos);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    /**
     * Construye y guarda un registro de devolucion
     *
     * @param pojos
     * @throws BusinessException
     */
    public void guardaMovimiento(Movimientos pojos) throws BusinessException {

        try {
            movDao.saveMovimiento(pojos);
            
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    /**
     * Obtiene el detalle de los ahorros para la pantalla Mis Ahorros
     *
     * @param usuId
     * @param movProducto
     * @param movAr
     * @param movIdPadre
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public List getAhorrosDetalle(Integer usuId, Integer movProducto, Integer movAr, Integer movIdPadre) throws BusinessException {

        try {

            List movDetalle = null;

            if (movProducto == 3) {
                AhorroVoluntarioDto vol = movDao.getDetalleAhorroV(usuId, movProducto, movIdPadre);
                
                  List<RendimientoDto> rends = movDao.getDetalleRendVol(usuId, movProducto, vol.getId());
                  
                  vol.setRendimiento(rends);
               
                  movDetalle = new ArrayList();
                  movDetalle.add(vol);

            } else {

                movDetalle = movDao.getDetalleAhorros(usuId, movProducto);
            }

            return movDetalle;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los ahorros de un empleado
     *
     * @param usuId
     * @return
     * @throws BusinessException
     */
    public List<MovimientosDto> getAhorrosTotales(Integer usuId) throws BusinessException {
        try {
            List<MovimientosDto> ahorros = movDao.getAhorrosTotalesFNF(usuId);
            List<MovimientosDto> av = movDao.getAhorrosTotalesVol(usuId);
            
            
            
            ahorros.addAll(av);
         
            
            for(MovimientosDto x: ahorros) {

                switch (x.getMovProducto()) {
                    case 1:
                        x.setMovProductoStr("AHORRO FIJO");
                       
                        break;
                    case 2:
                        x.setMovProductoStr("AHORRO NO FIJO");
                      
                        break;
                    case 3:
                        x.setMovProductoStr("AHORRO VOLUNTARIO");
                      
                        break;
                }

            }

           
            
            return ahorros;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
}