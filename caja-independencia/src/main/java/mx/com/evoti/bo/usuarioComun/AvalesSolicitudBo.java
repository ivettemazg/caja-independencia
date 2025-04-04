/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.usuarioComun;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.AvalesSolicitudDao;
import mx.com.evoti.dao.CreditosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.SolicitudAvales;
import mx.com.evoti.util.Constantes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class AvalesSolicitudBo implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AvalesSolicitudBo.class);
    private static final long serialVersionUID = -5430736294851365435L;

    private final AvalesSolicitudDao avaSolDao;
    private final CreditosDao creDao;

    public AvalesSolicitudBo() {
        avaSolDao = new AvalesSolicitudDao();
        creDao = new CreditosDao();
    }

    public UsuarioDto getAvalXCveEmpleado(Integer idEmpleado, Integer idEmpresa) throws BusinessException {
        try {
            return avaSolDao.getPerfilAvales(idEmpleado, idEmpresa);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<UsuarioDto> obtieneAvales() throws BusinessException {
        try {
            List<UsuarioDto> avales = avaSolDao.consultaClavesAval();
            return avales;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los avales con la informacion requerida para mostrar en la
     * pantalla detalle solicitud en la parte de avales
     *
     * @param solicitudAvaleses
     * @return
     */
    public List<UsuarioDto> getInfoAvales(List<SolicitudAvales> solicitudAvaleses) {
        List<UsuarioDto> avales = new ArrayList<>();

        for (SolicitudAvales pojo : solicitudAvaleses) {
            /**
             * Cuando el aval ya viene en estatus 2, 3 o 4
             */
            if (pojo.getSolAvaIdEmpleado() != null) {

                try {
                    UsuarioDto aval = avaSolDao.getInfCmpltAval(pojo.getSolAvaIdEmpleado());
                    aval.setIdSolicitudAval(pojo.getIdSolAva());
                    aval.setEstatusAvalStr(getAvalEstatusStr(pojo.getSolAvaEstatus()));
                    
                    if(pojo.getSolAvaEstatus()==4){
                        aval.setTblEditable(Boolean.TRUE);
                    }else{
                        aval.setTblEditable(Boolean.FALSE);
                        
                    }
                    avales.add(aval);
                } catch (IntegracionException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }

            } else {
                UsuarioDto aval = new UsuarioDto();
                aval.setIdSolicitudAval(pojo.getIdSolAva());
                aval.setEstatusAvalStr(getAvalEstatusStr(pojo.getSolAvaEstatus()));
                aval.setTblEditable(Boolean.TRUE);
                
                avales.add(aval);
            }
        }
        return avales;
    }
    
    private String getAvalEstatusStr(int estatusI){
        String estatus = null;
        switch (estatusI) {
                        case 1:
                            estatus = Constantes.SOLAVA_PENDIENTE;
                            break;
                        case 2:
                            estatus =Constantes.SOLAVA_VALIDANDO ;
                            break;
                        case 3:
                            estatus = Constantes.SOLAVA_APROBADO;
                            break;
                        case 4:
                            estatus = Constantes.SOLAVA_RECHAZADO;
                            break;

                    }
        return estatus;
    }

    /**
     * Actualiza el estatus del aval
     *
     * @param aval
     * @param estatus
     * @throws BusinessException
     */
    public void updtAval(UsuarioDto aval, int estatus) throws BusinessException {

        try {
            avaSolDao.updtAval(aval, estatus);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los avales de una solicitud
     *
     * @param idSolicitud
     * @return
     * @throws BusinessException
     */
    public List<SolicitudAvales> getAvalesXSolicitud(BigInteger idSolicitud) throws BusinessException {
        try {
            return avaSolDao.getAvalesXSolicitud(idSolicitud);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los avales de una solicitud
     *
     * @param idSolicitud
     * @return
     * @throws BusinessException
     */
    public List<UsuarioDto> getAvalesByIdSolicitud(BigInteger idSolicitud) throws BusinessException {
        try {

            List<UsuarioDto> avales = avaSolDao.getAvalesByIdSolicitud(idSolicitud);

            for (UsuarioDto aval : avales) {
                /*Cuando el usuario está activo*/
                if(aval.getEstatus()==null){
                    aval.setEstatus(0);
                }
                
                if (aval.getEstatus() == 1) {
                    aval.setEstatusStr(Constantes.USR_ACTIVO);
                } else {
                    aval.setEstatusStr(Constantes.USR_BAJA);
                }

                switch (aval.getEstatusAvalInt()) {
                    case 1:
                        aval.setEstatusAvalStr(Constantes.SOLAVA_PENDIENTE);
                        break;
                    case 2:
                        aval.setEstatusAvalStr(Constantes.SOLAVA_VALIDANDO);
                        break;
                    case 3:
                        aval.setEstatusAvalStr(Constantes.SOLAVA_APROBADO);
                        break;
                    case 4:
                        aval.setEstatusAvalStr(Constantes.SOLAVA_RECHAZADO);
                        break;
                }
            }

            return avales;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public void updtAvalEstatus(UsuarioDto dto, int estatus) throws BusinessException {
        try {
            avaSolDao.updtAvalEstatus(dto, estatus);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los avales de un credito que se mostrarán en el reporte por empleado
     * @param idCredito
     * @return
     * @throws BusinessException 
     */
    public List<UsuarioDto> getAvalesXCredito(Integer idCredito) throws BusinessException {
        try {
            List<UsuarioDto> avales = avaSolDao.getAvalesByCredito(idCredito);
            
            for(UsuarioDto aval : avales){ 
                
                try {
                    CreditoDto credDto = creDao.getCreXPadreUsr(idCredito,aval.getId());
                    
                    aval.setClaveCredTransf(credDto != null ? credDto.getCreClave() : "");
                    aval.setEstatusStr(Objects.equals(aval.getEstatus(), Constantes.USU_BAJA_0) ? "DADO DE BAJA" : "ACTIVO");
                    
                } catch (IntegracionException ex) {
                   LOGGER.error(ex.getMessage(), ex);
                }
            
            }
            
            return avales;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
}
