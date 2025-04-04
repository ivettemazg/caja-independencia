/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.bitacora.BitacoraDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.solicitudes.HistorialDao;
import mx.com.evoti.dto.HistorialSolicitudesDto;
import mx.com.evoti.hibernate.pojos.Bitacora;
import mx.com.evoti.util.Constantes;

/**
 *
 * @author NeOleon
 */
public class HistorialSolicitudesBo implements Serializable {

    private static final long serialVersionUID = 787366304654821056L;

    public HistorialSolicitudesBo() {

        this.historialSolicitudesDao = new HistorialDao();

    }

    private HistorialDao historialSolicitudesDao;

    public HistorialSolicitudesDto getDetalleSolicitud(BigInteger folio) throws BusinessException {
        try {
            HistorialSolicitudesDto detalleSolicitud = historialSolicitudesDao.getDetalleSolicitud(folio);
             BitacoraDao bitDao = new BitacoraDao();
            List<Bitacora> bits2 = bitDao.getBitacoraXReferencia(folio, Constantes.BIT_SOL_RECHAZADA);          

                        String msj2 = "";

                        for (Bitacora bit : bits2) {
                            msj2 += bit.getBitNota()+"\n" ;
                        }
                        
                        if(detalleSolicitud.getEstatusSol()==Constantes.SOL_EST_RECHAZADA){
                            if(detalleSolicitud.getMotivoRechazo()!=null){
                                if(detalleSolicitud.getMotivoRechazo().trim().isEmpty()){
                                    detalleSolicitud.setMotivoRechazo(msj2); 
                                }
                            }else{
                                detalleSolicitud.setMotivoRechazo(msj2);                                                                                             
                            }
                        }
            
            return detalleSolicitud;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);

        }
    }

    public List<HistorialSolicitudesDto> getAvalesSolicitud(BigInteger folio) throws BusinessException {
        try {
            List<HistorialSolicitudesDto> avalesSolicitud = historialSolicitudesDao.getAvalesSolicitud(folio);
            return avalesSolicitud;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);

        }
    }

}
