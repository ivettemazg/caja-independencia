/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.usuarioComun;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.SolicitudCreditoDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.usuariocomun.DocumentosDto;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class DocumentosSolicitudBo implements Serializable{

    
    private static final long serialVersionUID = 260282833900104845L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DocumentosSolicitudBo.class);

    
    private final SolicitudCreditoDao solDao;
    
    public DocumentosSolicitudBo(){
        solDao = new SolicitudCreditoDao();
    }
    
    /**
     * Actualiza el estatus de la Imagen en la base de datos
     * @param doc
     * @param solicitud
     * @throws BusinessException 
     */
    public void updtImgEstatus(DocumentosDto doc, Solicitudes solicitud) throws BusinessException{
        try {
            
            Imagenes ima = new Imagenes();
            
            ima.setImaEstatus(doc.getiStatus());
            ima.setImaId(doc.getIdImagen());
            ima.setImaImagen(doc.getNombreDocumento());
            ima.setImaObservaciones(doc.getObservaciones());
            ima.setImaTipoimagen(doc.getiTipoDocumento());
            
            ima.setSolicitudes(solicitud);
            
            
            solDao.updateImagen(ima);
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage());
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<Imagenes> getImgsXSol(BigInteger solId) throws BusinessException {
        try {
           return solDao.getImagenesXSolicitud(solId);
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage());
            throw new BusinessException(ex.getMessage(), ex);
        }

    }
    
    /**
     * Actualiza el estatus del documento en base de datos
     * @param doc
     * @throws BusinessException 
     */
    public void updtEstatusImagen(DocumentosDto doc) throws BusinessException{
        try {
            solDao.updtImagenEstatus(doc);
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage());
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
   
    
}
