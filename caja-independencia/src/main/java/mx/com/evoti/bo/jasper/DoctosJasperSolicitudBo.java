/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.jasper;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.jasper.DoctosJasperSolicitudDao;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.dto.jasper.FondeoDto;
import mx.com.evoti.dto.jasper.PendienteDto;

/**
 *
 * @author Venette
 */
public class DoctosJasperSolicitudBo implements Serializable {

    private static final long serialVersionUID = -6323729355432283079L;
    private DoctosJasperSolicitudDao dao;

    public DoctosJasperSolicitudBo() {
        dao = new DoctosJasperSolicitudDao();
    }

    public FondeoDto getCreditoBySolicitud(Long idSolicitud) throws BusinessException {
        try {
            return dao.getCreditoBySolicitud(idSolicitud);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    public List<AmortizacionDto> obtieneAmortizacion(Long idSolicitud) throws BusinessException {
        try {
            return dao.obtieneAmortizacion(idSolicitud);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public FondeoDto obtieneDatosAnexoB(Long solicitud) throws BusinessException {
        try {
            FondeoDto dataAnexoB = dao.obtieneDatosAnexoB(solicitud);
            return dataAnexoB;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);

        }
    }

    
    public FondeoDto obtieneDatosAnexoC(Long solicitud)  throws BusinessException {
        try {
            FondeoDto dataAnexoC = dao.obtieneDatosAnexoC(solicitud);
            return dataAnexoC;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);

        }
    }
    
    
    public List<UsuarioDto> obtieneAvalesAnexoC(Long solicitud) throws BusinessException{
        try {
            return dao.obtieneAvalesAnexoC(solicitud);
        } catch (IntegracionException ex) {
             throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
    public List<PendienteDto> obtieneAvalesSolicitud(Long solicitud) throws BusinessException{
        try {
            return dao.obtieneAvalesSolicitud(solicitud);
        } catch (IntegracionException ex) {
             throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
    public FondeoDto obtieneDatosSolicitud(Long solicitud) throws BusinessException{
        try {
            
            return dao.obtieneDatosSolicitud(solicitud);
        } catch (IntegracionException ex) {
             throw new BusinessException(ex.getMessage(), ex);
        }
    }

    
    public List<FondeoDto> obtieneDatosAviso(long idSolicitud) throws BusinessException {
        try {
            return dao.obtieneDatosAviso(idSolicitud);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

}
