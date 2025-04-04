/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.usuarioComun;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.util.EnviaCorreo;
import mx.com.evoti.dao.AvalesSolicitudDao;
import mx.com.evoti.dao.SolicitudCreditoDao;
import mx.com.evoti.dao.bitacora.BitacoraDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.ImagenesDto;
import mx.com.evoti.dto.usuariocomun.SolicitudCreditoDto;
import mx.com.evoti.hibernate.pojos.Bitacora;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.hibernate.pojos.SolicitudAvales;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class DetalleSolicitudBo implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DetalleSolicitudBo.class);
    private static final long serialVersionUID = 4799079387639148228L;

    private final SolicitudCreditoDao solDao;
    private final BitacoraDao bitDao;
    private final AvalesSolicitudDao avaSolDao;

    public DetalleSolicitudBo() {
        solDao = new SolicitudCreditoDao();
        bitDao = new BitacoraDao();
        avaSolDao = new AvalesSolicitudDao();
    }

    /**
     * Obtiene las solicitudes para la pantalla Mis solicitudes
     *
     * @param idEmpleado
     * @return
     * @throws BusinessException
     */
    public List<SolicitudCreditoDto> getSolsByUsuId(Integer idEmpleado) throws BusinessException {
        try {
            List<SolicitudCreditoDto> solicitudes = solDao.getSolsByUsuId(idEmpleado);

            for (int i = 0; i < solicitudes.size(); i++) {
                switch (solicitudes.get(i).getSolEstatus()) {
                    case 1:
                        solicitudes.get(i).setSolObservacion(Constantes.OBS_CREADA);
                        break;
                    case 2:
                        solicitudes.get(i).setSolObservacion(Constantes.OBS_VALIDANDO);
                        break;
                    case 3:
                        solicitudes.get(i).setSolObservacion(Constantes.OBS_ACEPTADA);
                        break;
                    case 4:
                        solicitudes.get(i).setSolObservacion(Constantes.OBS_FONDEADA);
                        break;
                    case 5:
                        solicitudes.get(i).setSolObservacion(Constantes.OBS_DOCUMENTOS);
                        break;
                    case 6:
                        solicitudes.get(i).setSolObservacion(Constantes.OBS_CERRADA);

                        break;
                    case 7:

                        List<Bitacora> bits2 = bitDao.getBitacoraXReferencia(solicitudes.get(i).getSolId(), Constantes.BIT_SOL_RECHAZADA);

                        String msj2 = Constantes.OBS_RECHAZADA;

                        for (Bitacora bit : bits2) {
                            msj2 += "\n" + bit.getBitNota();
                        }
                        solicitudes.get(i).setSolObservacion(msj2);

                        break;

                }
            }

            return solicitudes;
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene una solicitud a traves de su id
     *
     * @param idSolicitud
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public Solicitudes getSolById(BigInteger idSolicitud) throws BusinessException {
        try {
            Solicitudes sol = solDao.getSolicitudXId(idSolicitud);

            return sol;
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);

        }
    }

    /**
     * Actualiza los datos bancarios de la solicitud
     *
     * @param solicitud
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public void updtDatosBancarios(Solicitudes solicitud) throws BusinessException {
        try {
            solDao.updtDatosBancarios(solicitud);
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);

        }
    }

    /**
     * Actualiza el estatus de la solicitud a 2 (Validando)
     *
     * @param idSolicitud
     * @param nombreCompleto
     * @param emailUsuario
     * @return 
     * @throws BusinessException
     */
    public String updtSolicitudEstatus(Long idSolicitud, String nombreCompleto, String emailUsuario) throws BusinessException {

        try {
            /**
             * Valida que el estatus de las imagenes y avales sea diferente a
             * pendiente
             */
            Boolean validacion = validaImgsAvals(idSolicitud);

            //Si pasa las validaciones se actualiza la solicitud y envía el correo
            if (validacion) {
                String cuerpoMensaje;
                solDao.updtEstatusDbSolicitud(idSolicitud);
                avaSolDao.updtAvalEstatusXSolId(BigInteger.valueOf(idSolicitud), 3);

                cuerpoMensaje = String.format(Constantes.CUERPO, nombreCompleto, idSolicitud);
                EnviaCorreo.sendMessage(cuerpoMensaje, Constantes.ASUNTO, emailUsuario);
                return "La solcitud ha sido enviada para revisión";
            }
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);

        }
        return null;
    }
    
    public void updtSolDocEnviada(Long idSolicitud) throws BusinessException{
        try {
            solDao.updtEstatusSolDocEnviada(idSolicitud);
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    

    /**
     * Valida que todas las imagenes y todos los avales esten en estatus
     * diferente de 1
     *
     * @param idSolicitud
     * @return
     * @throws BusinessException
     */
    public boolean validaImgsAvals(Long idSolicitud) throws BusinessException {
        try {
            List<List> list = solDao.getImgsAvsXSol(idSolicitud);
            int imaSt2 = 0;
            int avalsSt2 = 0;
            int imaTotales = 0;
            int avalsTotales = 0;

            for (int i = 0; i < list.size(); i++) {

                switch (i) {
                    case 0://Se obtiene la lista de imagenes
                        List<Imagenes> imas = list.get(i);
                        imaTotales = imas.size();
                        for (Imagenes ima : imas) {
                            if (ima.getImaEstatus() != 1) {
                                imaSt2++;
                            } else {
                                break;
                            }
                        }

                        break;

                    case 1://Se obtiene la lista de avales
                        List<SolicitudAvales> avals = list.get(i);
                        avalsTotales = avals.size();
                        for (SolicitudAvales aval : avals) {
                            if (aval.getSolAvaEstatus() != 1) {
                                avalsSt2++;
                            }
                        }

                        break;
                }

            }

            Integer estatusDB =solDao.getEstatusDatosBancarios(idSolicitud);
            
            /**
             * Si todas las imagenes tienen estatus 2, todos los avales tienen
             * estatus 2 y los datos bancarios tambien tienen estatus 2 regresa true
             */
            if (imaSt2 == imaTotales && avalsSt2 == avalsTotales && estatusDB == 2) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }

        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Actualiza los diferentes estatus de la solicitud
     * @param idSolicitud
     * @param estatus
     * @param formatoRecepcion
     * @throws BusinessException 
     */
    public void updtEstatusSolicitud(BigInteger idSolicitud, Integer estatus, Integer formatoRecepcion) throws BusinessException {
        try {
            switch (estatus) {
                case 3://Cuando es aceptada
                    solDao.updtEstSolAutCanc(idSolicitud, estatus);
                    break;
                case 7://Cuando es rechazada
                    solDao.updtEstSolAutCanc(idSolicitud, estatus);
                    break;
                case 4://Cuando es fondeo
                    solDao.updtEstatusSolicitud(idSolicitud.longValue(), estatus);
                    break;
                case 5://Cuando es firma documentos
                    solDao.updtEstatusSolicitud(idSolicitud.longValue(), estatus);
                    break;
                case 8://Cuando se aprueban los doctos firmados
                    solDao.updtDocFirmada(idSolicitud.longValue(), estatus, formatoRecepcion);
                    break;
                case 6://Cuando se marca la fecha de deposito
                    solDao.updtEstatusSolicitud(idSolicitud.longValue(), estatus);
                    break;
                case 9://Cuando se pone la solicitud en estatus en revisión
                    solDao.updtEstatusSolicitud(idSolicitud.longValue(), 9);
                    break;
                    
            }
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    public void updtFechaDepositoCre(Long idSolicitud, Date fechaDeposito) throws BusinessException{
        try {
            solDao.updtFechaDepositoCredito(idSolicitud, fechaDeposito);
        } catch (IntegracionException ex) {
           throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public void enviaCorreoValSol(Usuarios usuario, Double montoSolicitado, Integer catorcenas, Double pagoCredito, int tipoCorreo, String motivoRechazo) {

        String nombreEmpleado = usuario.getUsuNombre() + " " + usuario.getUsuPaterno()
                + " " + usuario.getUsuMaterno();

        String cuerpoMensaje = "";
        String asunto = "";

        switch (tipoCorreo) {
            case 1:
                cuerpoMensaje = Constantes.SOLICITUD_AUTORIZADA;
                asunto = Constantes.ASUNTO_SOL_AUT;
                cuerpoMensaje = String.format(cuerpoMensaje, nombreEmpleado, montoSolicitado, catorcenas, pagoCredito);
                break;
            case 2:
                cuerpoMensaje = Constantes.SOLICITUD_RECHAZADA;
                asunto = Constantes.ASUNTO_SOL_RECH;
                cuerpoMensaje = String.format(cuerpoMensaje, nombreEmpleado, motivoRechazo);
                break;
            case 3:
                //Cuando se rechaza un documento firmado
                cuerpoMensaje = Constantes.DOCTO_FIRMADO_RECHAZADO;
                asunto = Constantes.ASUNTO_DOCTO_FIRMADO_RECHAZADO;
                cuerpoMensaje = String.format(cuerpoMensaje,nombreEmpleado, motivoRechazo);
                break;
            
        }
        EnviaCorreo.sendMessage(cuerpoMensaje, asunto, usuario.getUsuCorreo());
    }

    /**
     * Guarda en bitacora el motivo de rechazo de una solicitud, aval o
     * documento
     *
     * @param solId
     * @param tipoBit
     * @param usuId
     * @param subreferencia
     * @param motivoRechazo
     * @throws BusinessException
     */
    public void insertaMotivoRechazo(Long solId, int tipoBit, int usuId, Long subreferencia, String motivoRechazo) throws BusinessException {
        try {

            bitDao.saveBitacora(construyeBitacora(solId, tipoBit, usuId, subreferencia, motivoRechazo));
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
     * @param motivoRechazo
     * @return
     */
    public Bitacora construyeBitacora(Long solId, int tipoBit, int usuId, Long subreferencia, String motivoRechazo) {
        Bitacora bitacora = new Bitacora();
        bitacora.setBitFecha(new Date());
        bitacora.setBitNota(motivoRechazo);
        bitacora.setBitReferencia(solId);
        bitacora.setBitTipo(tipoBit);
        bitacora.setBitUsuario(usuId);
        bitacora.setBitSubreferencia(subreferencia);
        return bitacora;
    }

    public void guardaImagen(ImagenesDto imagen) throws BusinessException {
        try {
            solDao.guardaImagenes( imagen);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public void eliminaSolicitud(){
        try {
            solDao.eliminaSolicitud();
        } catch (IntegracionException ex) {
            Logger.getLogger(DetalleSolicitudBo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
