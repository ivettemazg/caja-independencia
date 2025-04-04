/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.bitacora;

import java.io.Serializable;
import java.util.Date;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.bitacora.BitacoraDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.Bitacora;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class BitacoraBo  implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BitacoraBo.class);
    private static final long serialVersionUID = -7367053935705066011L;
    private BitacoraDao dao;

    public BitacoraBo() {
        dao = new BitacoraDao();
    }
    
    
    
     /**
     * Guarda en bitacora 
     *
     * @param solId
     * @param tipoBit
     * @param usuId
     * @param subreferencia
     * @param motivoRechazo
     * @throws BusinessException
     */
    public void saveBitacora(Long solId, int tipoBit, int usuId, Long subreferencia, String motivoRechazo) throws BusinessException {
        try {

            dao.saveBitacora(construyeBitacora(solId, tipoBit, usuId, subreferencia, motivoRechazo));
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);
        }

    }
    
    
    /**
     * Construye un objeto tipo Bitacora para insertar el motivo de rechazo de
     * una solicitud, un aval o un documento
     *
     * @param solId
     * @param tipoBit
     * @param usuId
     * @param subreferencia
     * @param observacion
     * @return
     */
    public Bitacora construyeBitacora(Long solId, int tipoBit, int usuId, Long subreferencia, String observacion) {
        Bitacora bitacora = new Bitacora();
        bitacora.setBitFecha(new Date());
        bitacora.setBitNota(observacion);
        bitacora.setBitReferencia(solId);
        bitacora.setBitTipo(tipoBit);
        bitacora.setBitUsuario(usuId);
        bitacora.setBitSubreferencia(subreferencia);
        return bitacora;
    }
    
}
