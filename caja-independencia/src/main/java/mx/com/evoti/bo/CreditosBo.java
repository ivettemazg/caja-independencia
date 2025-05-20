/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.com.evoti.bo.administrador.finiquito.FiniquitosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.impl.AmortizacionTransformerImpl;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.ArchivosHistorialDao;
import mx.com.evoti.dao.CatorcenasDao;
import mx.com.evoti.dao.CreditosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.dto.finiquito.AvalesCreditoDto;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;

/**
 *
 * @author Venette
 */
public class CreditosBo implements Serializable, AmortizacionTransformerImpl {

    private static final long serialVersionUID = 787366304654821056L;

    private CatorcenasDao catorcenaDao;
    private ArchivosHistorialDao arhDao;
    private CreditosDao creditoDao;
    private AmortizacionDao amoDao;

    public CreditosBo() {

        this.arhDao = new ArchivosHistorialDao();
        this.catorcenaDao = new CatorcenasDao();
        this.creditoDao = new CreditosDao();
        this.amoDao = new AmortizacionDao();

    }

    /**
     * Obtiene el detalle de los créditos que se mostrarán en la pantalla de
     * finiquitos
     *
     * @param usuario
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public List<DetalleCreditoDto> obtCreditosDetalle(Usuarios usuario) throws BusinessException {

        /**
         * Obtiene la fecha en que se subio el último archivo, esta se utilizará
         * para sacar la deuda de las catorcenas pendientes anteriores
         */
        Date catorcenaUltArchivo= null;
        
        if(usuario.getEmpresas().getEmpId()!= 5){
            catorcenaUltArchivo = getCatorcenaUltArchivo(usuario.getEmpresas().getEmpId());
        }else{
            catorcenaUltArchivo = getCatorcenaUltArchivo(Constantes.AMX);
        }
        

        /**
         * Obtiene la catorcena siguiente a la fecha en que se subió el ultimo
         * archivo, para sacar el monto del capital total que se debe
         */
        Date catorcenaCapital = obtCatorcenaSig(catorcenaUltArchivo);

        /**
         * Obtiene el detalle de créditos que se mostrará en la pantalla de
         * finiquitos
         */
        List<DetalleCreditoDto> creditos = getDetalleAdeudoCredito(usuario.getUsuId(), catorcenaUltArchivo, catorcenaCapital);

        try {

            List avales;
            
            for (DetalleCreditoDto cre : creditos) {
              avales =  creditoDao.getAvalesXCredito(cre.getCreId());
              cre.setNoAvales(avales.size());
            }
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

        return creditos;
    }

    /**
     * Obtiene las catorcena pendientes anteriores
     *
     * @param idCredito
     * @param usuario
     * @return
     * @throws BusinessException
     */
    public List<Amortizacion> getCatPendtsAnteriores(Integer idCredito, Usuarios usuario) throws BusinessException {
        try {
            /**
             * Obtiene la fecha en que se subio el último archivo, esta se
             * utilizará para sacar la deuda de las catorcenas pendientes
             * anteriores
             */
            Date catorcenaUltArchivo = getCatorcenaUltArchivo(usuario.getEmpresas().getEmpId());

            return amoDao.getCatorcenasPendtsAnteriores(idCredito, catorcenaUltArchivo);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene la catorcena donde se meterá el pago a capital
     *
     * @param idCredito
     * @param usuario
     * @return
     * @throws BusinessException
     */
    public Amortizacion getAmoPagoCapital(Integer idCredito, Usuarios usuario) throws BusinessException {
        try {

            /**
             * Obtiene la fecha en que se subio el último archivo, esta se
             * utilizará para sacar la deuda de las catorcenas pendientes
             * anteriores
             */
            Date catorcenaUltArchivo = getCatorcenaUltArchivo(usuario.getEmpresas().getEmpId());

            /**
             * Obtiene la catorcena siguiente a la fecha en que se subió el
             * ultimo archivo, para sacar el monto del capital total que se debe
             */
            Date catorcenaCapital = obtCatorcenaSig(catorcenaUltArchivo);

            /**
             * Obtiene la amortizacion en donde se meterá el pago a capital
             */
            return amoDao.getAmortizacionPCap(idCredito, catorcenaCapital);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene el detalle de créditos que se mostrara en pantalla
     *
     * @param idUsuario
     * @param catAnterior
     * @param catSiguiente
     * @return
     * @throws BusinessException
     */
    private List<DetalleCreditoDto> getDetalleAdeudoCredito(int idUsuario, Date catAnterior, Date catSiguiente) throws BusinessException {
        try {
            return creditoDao.getDetalleAdeudoCredito(idUsuario, catAnterior, catSiguiente);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    /**
     * Obtiene el detalle de un credito que se mostrara en pantalla
     *
     * @param usuario
     * @param creId
     * @return
     * @throws BusinessException
     */
    public DetalleCreditoDto getDetalleCreditoByCreId(Usuarios usuario, int creId) throws BusinessException {
        try {

            /**
             * Obtiene la fecha en que se subio el último archivo, esta se
             * utilizará para sacar la deuda de las catorcenas pendientes
             * anteriores
             */
            Date catorcenaUltArchivo = getCatorcenaUltArchivo(usuario.getEmpresas().getEmpId());
            System.out.println("******Catorcena ultimo archivo  " + catorcenaUltArchivo);
            /**
             * Obtiene la catorcena siguiente a la fecha en que se subió el
             * ultimo archivo, para sacar el monto del capital total que se debe
             */
            Date catorcenaCapital = obtCatorcenaSig(catorcenaUltArchivo);
            System.out.println("******Catorcena capital  " + catorcenaCapital);

            /**
             * Obtiene el detalle de créditos que se mostrará en la pantalla de
             * finiquitos
             */
            DetalleCreditoDto credito = creditoDao.getDetalleAdeudoCreditoByCreId(creId, catorcenaUltArchivo, catorcenaCapital);

            return credito;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    /**
     * Obtiene la catorcena siguiente a una fecha dada
     *
     * @param fecha
     * @return
     * @throws BusinessException
     */
    public Date obtCatorcenaSig(Date fecha) throws BusinessException {

        try {
            return catorcenaDao.getCatInmediataSiguiente(fecha);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    /**
     * Obtiene la última catorcena en que se subió el archivo de una empresa
     *
     * @param empresa
     * @return
     * @throws BusinessException
     */
    public Date getCatorcenaUltArchivo(int empresa) throws BusinessException {
        try {
            return arhDao.getCatorUltArchivo(empresa);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los creditos de un usuario, con layout para mostrarse directo en
     * pantalla
     *
     * @param idUsuario
     * @return
     * @throws BusinessException
     */
    public List<CreditoDto> getCreditosXIdUsr(int idUsuario) throws BusinessException {

        try {
            return creditoDao.getCreditosXIdUsr(idUsuario);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<AmortizacionDto> getAmortizacionXCredito(Integer creId) throws BusinessException {
        try {
            List<AmortizacionDto> amortizacion = amoDao.getAmortizacionXCredito(creId);

            return amortizacion;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);

        }
    }

    /**
     * Crea y guarda en la base de datos un crédito transferido con todo y su
     * amortizacion
     *
     * @param credito
     * @param primerPago
     * @return
     * @throws BusinessException
     */
    public CreditosFinal creaCreditoTransferido(CreditosFinal credito, String primerPago) throws BusinessException {
        try {

            Set<Amortizacion> amortizacion = creaAmortizacion(credito, Util.generaFechaDeString(primerPago, "dd/MM/yyyy"), 0);
            credito.setAmortizacions(amortizacion);

            credito.setCreFechaPrimerPago(Util.generaFechaDeString(primerPago, "dd/MM/yyyy"));
            
            creditoDao.guardaCredito(credito);
            
            credito.setCreClave("TRA_NO_" + credito.getCreId());

            creditoDao.updtCreClave(credito.getCreId(), credito.getCreClave());

            //Actualiza la amortizacion del credito padre
            updtEstatusAmoInt(credito.getCrePadre(), Constantes.AMO_ESTATUS_TRANS_11);

            return credito;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Genera la amortizacion para un crédito transferido
     *
     * @param credito
     * @param fechaPrimerPago Se obtiene de la pantalla y es indicada por
     * Jeffrey
     * @return
     * @throws BusinessException
     */
    public Set<Amortizacion> creaAmortizacion(CreditosFinal credito, Date fechaPrimerPago, int tasaEspecial) throws BusinessException {
        TablaAmortizacionBo tamortizacionBo = new TablaAmortizacionBo();

        Set<Amortizacion> amortizacion;
        List<AmortizacionDto> lstAmo = null;
        tamortizacionBo.setIdUsuario(credito.getCreUsuId());

        lstAmo = tamortizacionBo.generaTablaAmortizacion(Constantes.PRIMER_PAGO, credito.getCrePrestamo(), credito.getCreCatorcenas(),
                fechaPrimerPago, credito.getCreProducto(), tasaEspecial);

        amortizacion = new HashSet<>(this.convertDtoToPojoAmortizacion(lstAmo, credito, Constantes.AMO_ESTATUS_PEND_1));
        return amortizacion;
    }

    /**
     * Actualiza el estatus de toda la amortizacion de un credito
     *
     * @param idCredito
     * @param estatus
     * @throws BusinessException
     */
    public void updtEstatusAmoInt(Integer idCredito, Integer estatus) throws BusinessException {
        try {
            amoDao.updtAmoEstatusInt(idCredito, estatus);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    
     public List<AvalesCreditoDto> getAvalesCredito(Integer idCredito) throws BusinessException {
        try {
            return creditoDao.getAvalesXCredito(idCredito);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
     public void updtCreditoEstatus(Integer creId, Integer creEstatus, Date fechaIncobrable) throws BusinessException {
        try {
            creditoDao.updtCreditoEstatus(creId, creEstatus, fechaIncobrable);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

     /**
      * Metodo que recorre la amortizacion de un credito para validar si se puede poner como pagado
      * @param creditoSeleccionado
      * @param amortizacion
     * @return 
      * @throws BusinessException 
      */
       public boolean saldarCredito(DetalleCreditoDto creditoSeleccionado, List<AmortizacionDto> amortizacion) throws BusinessException {
        boolean estaPagado = Boolean.TRUE;

        /**
         * Recorre y valida si alguno de los pagos del credito estan pendientes o son
         * equivalentes a pendiente, de ser asi no puede saldar el credito
         */
        for (AmortizacionDto dto : amortizacion) {
           
           if (Objects.equals(Constantes.AMO_ESTATUS_PEND_1, dto.getAmoEstatusInt()) || Objects.equals(Constantes.AMO_ESTATUS_PMEN_3, dto.getAmoEstatusInt())) {
                estaPagado = Boolean.FALSE;
                break;
            }
        }


        if (estaPagado) {
            try {           
                creditoDao.updtCreditoEstatus(creditoSeleccionado.getCreId() , Constantes.CRE_EST_PAGADO, null);
            } catch (IntegracionException ex) {
               throw new BusinessException(ex.getMessage(), ex);
            }
          
        }

        return estaPagado;
    }

       /**
        * Obtiene un credito por su id
        * @param creId
        * @return
        * @throws BusinessException 
        */
    public CreditosFinal obtieneCredXId(Integer creId) throws BusinessException {
        try {
            CreditosFinal credito = creditoDao.obtieneCreditoXId(creId);
            
            return credito;
        } catch (IntegracionException ex) {
              throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public void actualizaFechaNuevoMonto(Date catorcenaSiguiente, int idCredito) throws BusinessException {
        try {
            creditoDao.actualizaFechaNuevoMonto(catorcenaSiguiente, idCredito);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
     
    /**
     * Metodo que cambia el estatus de un credito a 2 "PAGADO" siempre y cuando no tenga
     * catorcenas pendientes
     * @param idCredito 
     */
    public void saldarCredito(Integer idCredito) throws BusinessException{
        
        try {
            //Obtiene la cantidad de catorcenas pendientes de un credito
            Integer numPendientes = amoDao.getAmoPendienteXCred(idCredito);
            
            /**
             * Cuando el valor es mayor a cero, implica que hay catorcenas pendientes
             * si es igual a cero, implica que no hay pendientes, por lo tanto se salda el credito
             */
            if(numPendientes==0){
                updtCreditoEstatus(idCredito, Constantes.CRE_EST_PAGADO, null);
            }
        
        } catch (IntegracionException ex) {
           throw new BusinessException(ex.getMessage(), ex);
        }
        
    }
    
     /**
      * Obtiene un credito a partir de su id, con los campos necesaairos para
      * mostrar en una tabla
      * @param credito
      * @return
      * @throws BusinessException 
      */
    public CreditoDto getCreditoPadre(DetalleCreditoDto credito) throws BusinessException{
        
        try {
            if(credito.getCreClave().contains("TRA")){
                CreditosFinal pojo = creditoDao.obtieneCreditoXId(credito.getCreId());
              
                return creditoDao.getCreditoXCreID( pojo.getCrePadre());
            }
            
            return null;
        } catch (IntegracionException ex) {
             throw new BusinessException(ex.getMessage(), ex);
        }
        
    }
    
     
    
    /**
     * Actualiza el estatus de una amortizacion a partir del amo_id
     * @param amoId
     * @param estatus
     * @throws BusinessException 
     */
    public void updtAmoEstatusIntByAmoId(Integer amoId, Integer estatus) throws BusinessException{
        
        try {
            
            amoDao.updtAmoEstIntByAmoId(amoId, estatus);
        
        } catch (IntegracionException ex) {
           throw new BusinessException(ex.getMessage(), ex);
        }
        
    }
    
    /**
     * Obtiene los creditos de los que es aval un usuario
     * @param idUsuario
     * @return
     * @throws BusinessException 
     */
    public List<CreditoDto> getCreditosDeAval(int idUsuario) throws BusinessException{
        try {
            return  creditoDao.getCreditosDeAval(idUsuario);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
}
