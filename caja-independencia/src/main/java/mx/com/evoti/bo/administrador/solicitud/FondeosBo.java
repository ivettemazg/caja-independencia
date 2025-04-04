/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.administrador.solicitud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mx.com.evoti.bo.TablaAmortizacionBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.impl.AmortizacionTransformerImpl;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.bo.util.EnviaCorreo;
import mx.com.evoti.dao.CreditosDao;
import mx.com.evoti.dao.FondeoDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.SolicitudDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class FondeosBo implements Serializable, AmortizacionTransformerImpl {

    private static final long serialVersionUID = -8429234771121480654L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FondeosBo.class);

    private TablaAmortizacionBo tamortizacionBo;
    private FondeoDao dao;
    private CreditosDao creDao;
    private DetalleSolicitudBo detSolBo;

    public FondeosBo() {
        dao = new FondeoDao();
        tamortizacionBo = new TablaAmortizacionBo();
        creDao = new CreditosDao();
        detSolBo= new DetalleSolicitudBo();
        
    }

    public List<SolicitudDto> getSolsPendtsFondeoScreen(Integer idSolicitud) throws BusinessException {
        try {
            return dao.getSolsPendtsFondeoScreen(idSolicitud);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo donde se hace el fondeo de una solicitud, el cual guarda un
     * credito con su respectiva amortizacion y actualiza el estatus de una
     * solicitud a Fondeada
     *
     * @param solicitud
     * @param fechaPrimerPago
     * @throws BusinessException
     */
    public void fondeaSolicitud(SolicitudDto solicitud, Date fechaPrimerPago) throws BusinessException {
        /**
         * Crea el credito
         */
        CreditosFinal credito = creaCredito(solicitud,fechaPrimerPago);
        
        /**
         * 
         */
        Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(Util.generaFechaDeString("2018-11-11"));
            cal2.setTime(solicitud.getSolFechaCreacion());
            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                  cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        
            int tasaEspecial = 0;
            
         if(sameDay){
             tasaEspecial = 1;
         }
        
        /**
         * Crea la amortizacion
         */        
        Set<Amortizacion> amortizacion = creaAmortizacion(solicitud,credito, fechaPrimerPago, tasaEspecial);
        credito.setAmortizacions(amortizacion);
       
        /**
         * Guarda el credito
         */
        CreditosFinal credFin = guardaCredito(credito);
        /**
         * Actualiza la solicitud y se fondea con el día de la transaccion
         * Cuando se trata de un credito de seguro de auto actualiza el estatus a documentos
         * aprobados
         */
        if(solicitud.getProId()==11){
            detSolBo.updtEstatusSolicitud(solicitud.getSolId(), Constantes.SOL_EST_DOCTOS_APROBADOS, 5);
        }else{
            updtSolicitudFondeada(solicitud.getSolId().longValue(), new Date());
        }
        updtSolAvalesCreId(solicitud.getSolId().longValue(), credFin.getCreId());
       
        /**
         * Envia correo de los documentos requeridos
         */
//        enviaMensajeFondeo("ivette.manzano.27@hotmail.com");
        enviaMensajeFondeo(solicitud.getUsuEmail());

    }

    /**
     * Genera la amortizacion que se guardará en la base de datos
     *
     * @param solicitud
     * @param credito
     * @param fechaPrimerPago Se obtiene de la pantalla y es indicada por
     * Jeffrey
     * @return
     * @throws BusinessException
     */
    public Set<Amortizacion> creaAmortizacion(SolicitudDto solicitud,CreditosFinal credito, Date fechaPrimerPago, int tasaEspecial) throws BusinessException {
        Set<Amortizacion> amortizacion;
        List<AmortizacionDto> lstAmo = null;
        tamortizacionBo.setIdUsuario(solicitud.getUsuId());

        if (solicitud.getProId() == 7 || solicitud.getProId() == 6) {
            lstAmo = this.tamortizacionBo.generaTablaAmortizacion(Constantes.PRIMER_PAGO, solicitud.getSolMontoSolicitado(), solicitud.getSolCatorcenas(), fechaPrimerPago, solicitud.getProId(), tasaEspecial);

        } else if (solicitud.getProId() == 5 || solicitud.getProId() == 4 || solicitud.getProId() == 11) {
            List<Date> fechas;
            
            if(solicitud.getProId() == 5){
                solicitud.setSolFacatorcena(12);
            }
            
            fechas = tamortizacionBo.obtieneCatorcenasIntermedias(solicitud.getProId(), solicitud.getSolFacatorcena(), solicitud.getSolFechaCreacion());

            tamortizacionBo.generaTablaAmortizacionAgFA(solicitud.getSolMontoSolicitado(), fechas);

            AmortizacionDto amo = tamortizacionBo.getUltAmortFAAGSAU();
            lstAmo = new ArrayList<>();
            lstAmo.add(amo);

        }
        
        amortizacion = new HashSet<>(this.convertDtoToPojoAmortizacion(lstAmo,credito,Constantes.AMO_ESTATUS_PEND_1));
        return amortizacion;
    }
   
    
    /**
     * Crea el objeto credito con todos los campos necesarios para guardarse en
     * la base de datos
     *
     * @param solicitud
     * @param fechaPrimerPago
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public CreditosFinal creaCredito(SolicitudDto solicitud, Date fechaPrimerPago) throws BusinessException {

        CreditosFinal credito = new CreditosFinal();
        credito.setCreCatorcenas(solicitud.getSolCatorcenas());
        credito.setCreClave(generaCreClave(solicitud));
        credito.setCreClaveEmpleado(solicitud.getSolClaveEmpleado());
        credito.setCreEmpresa(solicitud.getEmpAbreviacion());
        credito.setCreEstatus(Constantes.CRE_EST_ACTIVO);
        credito.setCrePrestamo(solicitud.getSolMontoSolicitado());
        credito.setCreProducto(solicitud.getProId());
        credito.setCreSolicitud(solicitud.getSolId().longValue());
        credito.setCreUsuId(solicitud.getUsuId());
        credito.setCrePagoQuincenal(solicitud.getSolPagoCredito());
        credito.setCreFechaPrimerPago(fechaPrimerPago);

        return credito;
    }

    /**
     * Metodo que genera la clave del credito que corresponde al campo cre_clave
     * de la tabla CREDITOS_FINAL
     *
     * @param sol
     * @return
     */
    public String generaCreClave(SolicitudDto sol) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        String stYear = ""+year;

        String claveCre = sol.getEmpAbreviacion() + stYear.substring(2) + sol.getProSiglas()+ sol.getSolId();

        return claveCre;
    }

    /**
     * Metodo que guarda un credito con su respectiva amortizacion
     *
     * @param credito
     * @return 
     * @throws BusinessException
     */
    public CreditosFinal guardaCredito(CreditosFinal credito) throws BusinessException {
        try {
           return dao.guardaCredito(credito);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Envía mensaje de los documentos requeridos para aprobar el credito
     * @param email 
     */
    public void enviaMensajeFondeo(String email) {

        EnviaCorreo.sendMessage(Constantes.MSJ_CUERPO_FONDEADA, "Envio de documentación", email);

    }

    /**
     * Actualiza el estatus de la solicitud a fondeada
     *
     * @param idSolicitud
     * @param fechFondEnv
     * @throws BusinessException
     */
    public void updtSolicitudFondeada(Long idSolicitud, Date fechFondEnv) throws BusinessException {
        try {
            dao.updtEstatusSolicitud4567(idSolicitud, Constantes.SOL_EST_FONDEADA, fechFondEnv);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }
    
    public void updtSolAvalesCreId(Long idSol, int creId) throws BusinessException{
        try {
            dao.updtAvalSolicitudCreId(idSol, creId);
        } catch (IntegracionException ex) {
              throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }
    
    /**
     * Actualiza el estatus de la solicitud a documentos enviados
     *
     * @param idSolicitud
     * @param fechFondEnv
     * @throws BusinessException
     */
    public void updtSolicitudDoctosEnviados(Long idSolicitud, Date fechFondEnv) throws BusinessException {
        try {
            dao.updtEstatusSolicitud4567(idSolicitud, Constantes.SOL_EST_DOCTOS_ENV, fechFondEnv);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Actualiza el estatus de la solicitud a deposito realizado y la fecha de
     * deposito en la tabla creditos_final
     *
     * @param idSolicitud
     * @param fechaDep
     * @throws BusinessException
     */
    public void updtSolicitudDeposito(Long idSolicitud, Date fechaDep) throws BusinessException {
        try {
            dao.updtEstatusSolicitud4567(idSolicitud, Constantes.SOL_EST_DOCTOS_ENV, null);
            dao.updtFechaDepositoCredito(idSolicitud, fechaDep);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }
    
    /**
     * La solicitud es cancelada antes de que el credito sea creado (la solicitud
     * nunca ha sido fondeada en este punto)
     * @param idSolicitud
     * @param fechaCanc
     * @throws BusinessException 
     */
    public void updtSolicitudCancelada(Long idSolicitud, Date fechaCanc) throws BusinessException {
        try {
            dao.updtEstatusSolicitud4567(idSolicitud, Constantes.SOL_EST_RECHAZADA, null);
           
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }
    
    public Imagenes getDocumentoByTipo(Long idSolicitud, int tipoDocto) throws BusinessException{
        try {
            return dao.getDoctoFirmado(idSolicitud, tipoDocto);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Proceso que cancela un crédito, cambia el estatus del credito a cancelado y borra
     * la amortizacion del mismo
     * @param idSolicitud
     * @return
     * @throws BusinessException 
     */
    public Integer cancelarCredito(long idSolicitud) throws BusinessException {
        try {
            CreditosFinal credito = creDao.obtieneCreditosXSolicitud(idSolicitud);
           
            if(credito != null){
            
                creDao.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_CANCELADO, null);

                 /*
                Se actualizan todos los pagos que estan referidos a la amortizacion que se va a eliminar
                y se cambia a estatus Siin amortizacion = 7 y se mandan a acumulado
                */
                creDao.updtPagosPagados(credito.getCreId());

                /**
                 * Borra amortizacion en pendiente
                 */
                creDao.removeAmortizacion(credito.getCreId());
                return credito.getCreId();
            }
            return null;
            
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }
    
    

}
