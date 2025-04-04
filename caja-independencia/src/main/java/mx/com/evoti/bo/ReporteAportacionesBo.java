/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.DescuentoNominaBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.ReporteAportacionesDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.ReporteAportacionesDto;
import org.slf4j.LoggerFactory;

/**
 *
 * @author NeOleon
 */
public class ReporteAportacionesBo implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DescuentoNominaBo.class);
    private static final long serialVersionUID = -604848521024535467L;

    public ReporteAportacionesBo() {
        RepApoDao = new ReporteAportacionesDao();
    }

    private ReporteAportacionesDao RepApoDao;

    private List<ReporteAportacionesDto> descripcionArchivo;
    private List<ReporteAportacionesDto> resumenArchivo;
    private List<ReporteAportacionesDto> detalleArchivo;
    private List<ReporteAportacionesDto> usuariosNoRegistrados;
    private List<ReporteAportacionesDto> usuariosConBaja;

    public List<ReporteAportacionesDto> obtieneDatosArchivo(Date fechaCatorcena, Integer empresa) throws BusinessException {
        try {
            descripcionArchivo = RepApoDao.extraeDatosArchivo(fechaCatorcena, empresa);
            return descripcionArchivo;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<ReporteAportacionesDto> obtieneResumenArchivo(Date fechaCatorcena, Integer empresa) throws BusinessException {
        try {
            resumenArchivo = RepApoDao.extraeResumenArchivo(fechaCatorcena, empresa);
            return resumenArchivo;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<ReporteAportacionesDto> obtieneDetalleArchivo(Date fechaCatorcena, Integer empresa) throws BusinessException {
        try {
            detalleArchivo = RepApoDao.extraeDetalleArchivo(fechaCatorcena, empresa);
            return detalleArchivo;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<ReporteAportacionesDto> obtieneUsuariosNoRegistrados(Date fechaCatorcena, Integer empresa) throws BusinessException {
        try {
            usuariosNoRegistrados = RepApoDao.extraeUsuariosNoRegistrados(fechaCatorcena, empresa);
            return usuariosNoRegistrados;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<ReporteAportacionesDto> obtieneUsuariosConBaja(Date fechaCatorcena, Integer empresa) throws BusinessException {
        try {
            usuariosConBaja = RepApoDao.extraeUsuariosConBaja(fechaCatorcena, empresa);
            return usuariosConBaja;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
    public void reactivarUsuarios(List<ReporteAportacionesDto> usuariosBaja) throws BusinessException{
        for(ReporteAportacionesDto dto : usuariosBaja){
            try {
                RepApoDao.updtEstatusAltaUsr(dto.getUsuId());
            } catch (IntegracionException ex) {
               throw new BusinessException(ex.getMessage(), ex);
            }
        }
    }

}
