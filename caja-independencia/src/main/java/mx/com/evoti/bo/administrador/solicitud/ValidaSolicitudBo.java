package mx.com.evoti.bo.administrador.solicitud;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.SolicitudBo;
import mx.com.evoti.dao.SolicitudCreditoDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.SolicitudDto;
import org.slf4j.LoggerFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Venette
 */
public class ValidaSolicitudBo implements Serializable{
    
    private static final long serialVersionUID = -1685479095140524146L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ValidaSolicitudBo.class);

    private final SolicitudCreditoDao solDao;
     private final SolicitudBo solBo;
    
    public ValidaSolicitudBo() {
        solDao = new SolicitudCreditoDao();
         solBo = new SolicitudBo();
    }

    /**
     * Metodo que obtiene todas las solicitudes con estatus 2 'Validando'
     * @return
     * @throws BusinessException 
     */
    public List<SolicitudDto> getSolsFValidando() throws BusinessException{
        try {
            return solDao.getSolsFValidando();
        } catch (IntegracionException ex) {
            LOGGER.error("Error en la consulta de las solicitudes", ex);
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Obtiene los datos de la solicitud que se mandará a pantalla y que se
     * está validando
     * @param idPendiente
     * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException 
     */
     public SolicitudDto obtieneDetallePendiente(BigInteger idPendiente) throws BusinessException {
        try {
            return solDao.obtieneDetalleSolicitud(idPendiente);
        } catch (IntegracionException ex) {
             LOGGER.error("Error en obtieneDetallePendiente", ex);
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
      public List<Date> obtieneCatorcenasIntermedias(int tipoSol, Date catorcenaFinal, Date creacionSolicitud) {
        List<Date> fechas = null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(catorcenaFinal);
        Calendar calHoy = Calendar.getInstance();
        calHoy.setTime(new Date());

        Boolean julioSigAno = Boolean.FALSE;
        int month = cal.get(Calendar.MONTH);
        int yearActual = calHoy.get(Calendar.YEAR);
        int year = cal.get(Calendar.YEAR);
        if (month + 1 == 7 && (yearActual < year)) {

            julioSigAno = true;
        }

        try{
        if (tipoSol == 5) {
            fechas = solBo.consultaCatorcenasTotales(12, julioSigAno, creacionSolicitud);
        } else if (tipoSol == 4) {
            fechas = solBo.consultaCatorcenasTotales(month + 1, julioSigAno, creacionSolicitud);
        }

        }catch(BusinessException ex){
            LOGGER.error(ex.getMessage(), ex);
        }
        return fechas;
    }
    
      
      public void updtSolicitudDetalle(SolicitudDto solDto) throws BusinessException{
        try {
            solDao.updtSolicitudDetalle(solDto);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
      }
      
      
         
}
