/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.reporteEmpleado;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.TablaAmortizacionBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.impl.AmortizacionTransformerImpl;
import mx.com.evoti.bo.transaccion.TransaccionBo;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.CatorcenasDao;
import mx.com.evoti.dao.CreditosDao;
import mx.com.evoti.dao.PagosDao;
import mx.com.evoti.dao.bancos.BancosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class ProcesoPagoCapitalBo implements Serializable,AmortizacionTransformerImpl {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProcesoPagoCapitalBo.class);
    private static final long serialVersionUID = 5928929369588045384L;

    private AmortizacionDao amoDao;
    private TablaAmortizacionBo amoBo;
    private CatorcenasDao catDao;
    private CreditosDao creDao;
    private TransaccionBo tranBo;

    
    public ProcesoPagoCapitalBo() {
        tranBo = new TransaccionBo();
        amoDao = new AmortizacionDao();
        amoBo = new TablaAmortizacionBo();
        catDao = new CatorcenasDao();
        creDao = new CreditosDao();
    }

    public List<String> recalculaAmortizacion(Double montoPagoACapital, AmortizacionDto amoSelected, 
            CreditosFinal credito, Integer catorcenasRestantes, Usuarios usuario, Date fechaPagoCap, 
            Integer tipoPagCap, Integer userSession) {
        List<String> lstErrores = new ArrayList<>();

        if (Constantes.AMO_ESTATUS_PEND_1.equals(amoSelected.getAmoEstatusInt()) || 
                Constantes.AMO_ESTATUS_PMEN_3.equals(amoSelected.getAmoEstatusInt())) {

            Double saldoTotal = amoSelected.getAmoCapital() - montoPagoACapital;

            amoSelected.setAmoClaveEmpleado(usuario.getUsuClaveEmpleado());
            amoSelected.setAmoCredito(credito.getCreId());

            
                /**
                 * Cuando el monto a capital es mayor al pago que se está metiendo
                 */
            if (montoPagoACapital < amoSelected.getAmoCapital() ) {

                if (!this.guardaCambiosBd(montoPagoACapital, amoSelected, credito, usuario, fechaPagoCap, userSession)) {

                    lstErrores.add("Hubo un error al hacer el cálculo, por favor intente de nuevo");
                    
                } else {

                    try {
                        
                        amoBo.setIdUsuario(amoSelected.getAmoUsuId());
            
                        Date catorcenaSiguiente = amoSelected.getAmoFechaPago();
//                        Date catorcenaSiguiente = catDao.getCatInmediataSiguiente(catorcenaSeleccionada);
                        
                        List<AmortizacionDto> amortizaciones = amoBo.generaTablaAmortizacion(amoSelected.getAmoNumeroPago() + 1, saldoTotal, catorcenasRestantes, catorcenaSiguiente, credito.getCreProducto(),0);

                        amortizaciones.forEach( dto -> {
                            dto.setAmoFechaPago(dto.getAmoFechaPagoCredito());
                        });
                        
                        /**
                         * Se transforma el resultado del metodo
                         * generatablaamortizacion que viene en formato dto a
                         * pojo para poder guardar directamente la lista
                         */
                        List pojos = convertDtoToPojoAmortizacion(amortizaciones, credito, Constantes.AMO_ESTATUS_PEND_1);
                        amoDao.savePojos(pojos);

                        LOGGER.info("Se genero correctamente la amortizacion nueva...");

                        //Actualiza la clave del empleado, el usu id y el producto dentro de la amortizacion nueva
                        amoDao.updtCveEmpUsuIdProd(usuario, credito);
                        
                        AmortizacionDto ultimoPago = amoDao.consultaUltimoPagoAmortizacion(credito.getCreId());
                        creDao.actualizaCreditoEnPagoYCatorcenas(credito.getCreId(), ultimoPago.getAmoMontoPago(), ultimoPago.getAmoNumeroPago(), amoSelected.getAmoFechaPago());

                    } catch (IntegracionException ex) {
                        LOGGER.info(ex.getMessage(), ex.getCause());
                    }
                }

                /**
                 * Cuando el saldo total es cero o
                 * Si el monto de pago es mayor al capital
                 * se salda el credito y el restante se va a acumulado
                 */
           
            } else if (saldoTotal == 0.0 || montoPagoACapital > amoSelected.getAmoCapital()) {
                if (!guardaCambiosBd(montoPagoACapital, amoSelected, credito, usuario, fechaPagoCap, userSession)) {

                    lstErrores.add("Hubo un error al hacer el cálculo, por favor intente de nuevo");
                } else {
                    try {
                        /**
                         * Actualiza el credito a pagado
                         */
                        creDao.updtCreditoEstatus(credito.getCreId(),Constantes.CRE_EST_PAGADO,null);
                    } catch (IntegracionException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                }
                return lstErrores;
            }
        }
        return lstErrores;
    }

    /**
     * Borra la amortizacion, inserta el pago en tabla pagos y su referencia en
     * bancos
     *
     * @param montoPago
     * @param amortizacion
     * @param credito
     * @param usuario
     * @param fechaPagoCap
     * @return
     */
    public boolean guardaCambiosBd(Double montoPago, AmortizacionDto amortizacion, CreditosFinal credito, Usuarios usuario, Date fechaPagoCap, Integer userSession) {
        LOGGER.trace("En AdministracionBo.calculoSaldoCero()");
        try {

            Double diferencia = montoPago - amortizacion.getAmoCapital();
            if(diferencia <= 1 && diferencia >= -1 || amortizacion.getAmoCapital() > montoPago){
                diferencia = 0.0;
            }
            
            /**
             * Elimina la amortizacion hacia adelante
             */
            amoDao.borraAmortizacionPosteriorCap(amortizacion.getAmoNumeroPago()-1, credito.getCreId());

            /**
             * Guarda el pago en la tabla pagos y en bancos para obtener el id
             * del pago y poder pegarselo a la amortizacion
             */
            Integer pagId = guardaPago(credito.getCreId(), montoPago, diferencia, 
                    usuario.getEmpresas().getEmpId(), 
                    fechaPagoCap, usuario.getUsuId(), usuario.getUsuClaveEmpleado(), userSession);

            if(amortizacion.getAmoCapital() < montoPago){
                montoPago = amortizacion.getAmoCapital();
            }
            
            Amortizacion pojo = creaPagoCapital(amortizacion.getAmoNumeroPago(), amortizacion.getAmoCapital(), montoPago, 0.0, montoPago,
                    Constantes.AMO_ESTATUS_CAPITAL_7, usuario.getUsuId(),fechaPagoCap, credito);

            pojo.setAmoPagoId(pagId);

            /**
             * Guarda el pago a capital dentro de la amortizacion
             */
            amoDao.savePojo(pojo);

        } catch (IntegracionException | BusinessException ex) {
            Logger.getLogger(ProcesoPagoCapitalBo.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public Amortizacion creaPagoCapital(Integer numeroPago,
            Double capital, Double amortizacion,
            Double interes, Double montoPago, Integer estatusAmo,
            Integer usuId, Date fechaPago, CreditosFinal credito) {

        Amortizacion amo = new Amortizacion();
        amo.setAmoNumeroPago(numeroPago);
        amo.setAmoFechaPago(fechaPago);
        amo.setAmoEstatusInt(estatusAmo);
        amo.setAmoCapital(capital);
        amo.setAmoMontoPago(montoPago);
        amo.setAmoAmortizacion(amortizacion);
        amo.setAmoIva(0.0);
        amo.setAmoInteres(interes);
        amo.setAmoUsuId(usuId);
        amo.setCreditosFinal(credito);

        return amo;

    }

    /**
     * Guarda un registro en la tabla pagos de tipo pago a capital, así como
     * tambien guarda su respectivo registro en bancos
     *
     * @param creId
     * @param montoFiniq
     * @param acumulado
     * @param empId
     * @param fech
     * @param usuId
     * @param cveEmpleado
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public Integer guardaPago(Integer creId, Double montoFiniq, Double acumulado, 
            Integer empId, Date fech, Integer usuId, Integer cveEmpleado, Integer userSession) throws BusinessException {
        try {
            PagosDao pagoDao =  new PagosDao();
            BancosDao banDao = new BancosDao();

            Pagos pago = new Pagos();

            pago.setPagAcumulado(acumulado);
            pago.setPagClaveEmpleado(cveEmpleado);
            pago.setPagCredito(creId);
            pago.setPagDeposito(montoFiniq);
            pago.setPagEmpresa(empId);
            pago.setPagEstatus(Constantes.PAGEST_CAPITAL_9);
            pago.setPagFecha(fech);
            pago.setPagUsuId(usuId);

            pagoDao.guardaPago(pago);

            /**
             * Crea un objeto tipo Banco que se insertará y hará referencia al
             * pago de finiquito que se creó anteriormente
             */
            Bancos banco = new Bancos();

            banco.setBanAjustado(Constantes.BAN_NO_AJUSTADO);
            banco.setBanConcepto(Constantes.BAN_PAGO_CAPITAL);
            banco.setBanIdConceptoSistema(pago.getPagId());
            banco.setBanMonto(pago.getPagDeposito());
            banco.setBanEmpresa(empId);
            banco.setBanFechatransaccion(fech);

            banDao.saveBanco(banco);
            
            tranBo.guardaTransaccion(18, pago.getPagId(), userSession);

            return pago.getPagId();
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

}
