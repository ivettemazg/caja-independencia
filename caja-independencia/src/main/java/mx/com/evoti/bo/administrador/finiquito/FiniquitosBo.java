/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.administrador.finiquito;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.CatorcenasDao;
import mx.com.evoti.dao.FiniquitoDao;
import mx.com.evoti.dao.MovimientosDao;
import mx.com.evoti.dao.PagosDao;
import mx.com.evoti.dao.UsuariosDao;
import mx.com.evoti.dao.bancos.BancosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.BajaEmpleados;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;
import mx.com.evoti.bo.impl.PagoCapitalFiniquitoBoImpl;

/**
 *
 * @author Venette
 */
public class FiniquitosBo implements PagoCapitalFiniquitoBoImpl, Serializable {

    private static final long serialVersionUID = 141328638092848989L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FiniquitosBo.class);

    
    private AmortizacionDao amoDao;
    private PagosDao pagoDao;
    private UsuariosDao usuDao;
    private CatorcenasDao catDao;
    private CreditosBo creBo;
    private FiniquitoDao finDao;

    public FiniquitosBo() {

        this.usuDao = new UsuariosDao();
        catDao = new CatorcenasDao();
        finDao = new FiniquitoDao();
        creBo = new CreditosBo();
    }

    /**
     * Metodo que hace el ajuste de los créditos
     *
     * @param amoPagoCapital
     * @param amoPendts la lista de amortizaciones pendientes anteriores
     * @param abonoACapital
     * @param estatus
     * @throws BusinessException
     */
    public void ajustaCredito(Amortizacion amoPagoCapital, List<Amortizacion> amoPendts,
            Double abonoACapital, Integer estatus) throws BusinessException {

        amoDao = new AmortizacionDao();

        try {
            /**
             * Cuando la deuda esta conformada solamente por catorcenas pasadas
             */
            if (!amoPendts.isEmpty() && amoPagoCapital == null) {
                LOGGER.debug("La deuda esta conformada por CATORCENAS PASADAS");
                Double montoPago = 0.0;
                CreditosFinal credito = null;
                Integer usuId = null;

                for (Amortizacion x : amoPendts) {

                    /**
                     * Cuando la catorcena adeudada es menor o igual que el pago
                     * a capitaal o la diferencia entre ambos no sobrepase 1
                     * peso ni sea menor a -1
                     */
                    Double diferencia = abonoACapital - x.getAmoMontoPago();

                    if ((x.getAmoMontoPago() <= abonoACapital) || (diferencia >= -5 && diferencia <= 5)) {

                        x.setAmoEstatusInt(estatus);
                        abonoACapital = Util.round(abonoACapital - x.getAmoMontoPago());

                        amoDao.updatePojo(x);

                        if (abonoACapital < 2000) {
                            System.out.println("paraaqui");
                        }

                        //Si el abono a capital es mayor a cero
                    } else if (abonoACapital > 0) {

                        if (credito == null) {
                            credito = x.getCreditosFinal();
                        }

                        if (usuId == null) {
                            usuId = x.getAmoUsuId();
                        }

                        x.setAmoEstatusInt(Constantes.AMO_ESTATUS_DEUDA_FIN_10);

                        amoDao.updatePojo(x);

                        montoPago = Util.round(montoPago + x.getAmoMontoPago());
                    }
                }

                /**
                 * Si aún existe una deuda pendiente, se genera el pago a
                 * capital que se hará y se arreglará lo que queda de saldo en
                 * la amortizacion. Se valida el credito porque cuando no
                 * alcanza a cubrir catorcenas exactas, asigna el credito , si
                 * no lo asigna significa que no queda un saldo pendiente
                 */
                if (credito != null) {
                    Integer numeroPagoCapital = amoPendts.get(amoPendts.size() - 1).getAmoNumeroPago() + 1;
                    Amortizacion pagoCapital = creaPagoCapital(numeroPagoCapital, montoPago, abonoACapital, 0.0,
                            abonoACapital, estatus, usuId, credito);

                    /**
                     * Crea el ultimo pago de la amortizacion
                     */
                    Integer numeroPago = amoPendts.get(amoPendts.size() - 1).getAmoNumeroPago() + 2;
                    Date catorcenaCapital = amoPendts.get(amoPendts.size() - 1).getAmoFechaPago();

                    Double montoCapital = montoPago - abonoACapital;

                    Amortizacion ultimoPago = this.creaAmortUltimoPago(numeroPago + 1, catDao.getCatInmediataSiguiente(catorcenaCapital),
                            montoCapital, montoCapital, montoCapital, 0.0, usuId, credito);
                    /**
                     * Se guardan ambos registro en la tabla amortizacion
                     */

                    List<Amortizacion> ultimosPagos = new ArrayList();
                    ultimosPagos.add(pagoCapital);
                    ultimosPagos.add(ultimoPago);

                    amoDao.insertaAmortizacion(ultimosPagos);
//                    actualiza estatus credito ajustado
                    if (Objects.equals(estatus, Constantes.AMO_ESTATUS_ABONO_CRE_8)) {
                        creBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_AJUSTADO, null);
                    } else {
                        creBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_AJUST_FINIQ, null);

                    }

                } else {
//                    actualiza estatus credito pagado
                    creBo.updtCreditoEstatus(amoPendts.get(0).getCreditosFinal().getCreId(), Constantes.CRE_EST_PAGADO, null);
                }
                /**
                 * Cuando la deuda esta conformada de catorcenas pasadas y de
                 * capital
                 */
            } else if (!amoPendts.isEmpty() && amoPagoCapital != null) {
                LOGGER.debug("La deuda esta conformada por CATORCENAS PASADAS Y CAPITAL");

                Double capitalCatorcenasPasadas = 0.0;
                Double interesCatorcenasPasadas = 0.0;

                for (Amortizacion x : amoPendts) {

                    //Cuando el abono a credito alcanza a cubrir al menos una catorcena
                    if (x.getAmoMontoPago() < abonoACapital) {

                        x.setAmoEstatusInt(estatus);
                        abonoACapital = Util.round(abonoACapital - x.getAmoMontoPago());

                        amoDao.updatePojo(x);

                        //Cuando ya no alcanza a pagar una catorcena
                    } else {
                        x.setAmoEstatusInt(Constantes.AMO_ESTATUS_DEUDA_FIN_10);

                        amoDao.updatePojo(x);
                        capitalCatorcenasPasadas = Util.round(capitalCatorcenasPasadas + x.getAmoAmortizacion());
                        interesCatorcenasPasadas = Util.round(interesCatorcenasPasadas + x.getAmoInteres());
                    }
                }

                Double diferencia = abonoACapital - amoPagoCapital.getAmoCapital();
                /**
                 * Cuando la diferencia es hasta de 2 pesos, se considera
                 * saldado el credito
                 */
                if (diferencia <= 2 && diferencia >= -2) {
                    amoPagoCapital.setAmoAmortizacion(abonoACapital);
                    amoPagoCapital.setAmoInteres(0.0);
                    amoPagoCapital.setAmoMontoPago(abonoACapital);
                    amoPagoCapital.setAmoEstatusInt(estatus);

                    amoDao.mergePojo(amoPagoCapital);

                    amoDao.borraAmortizacionPosteriorCap(amoPagoCapital.getAmoNumeroPago(), amoPagoCapital.getCreditosFinal().getCreId());
//                    actualiza estatus credito pagado
                    creBo.updtCreditoEstatus(amoPagoCapital.getCreditosFinal().getCreId(), Constantes.CRE_EST_PAGADO, null);
                    /**
                     * Cuando existe una diferencia mayor lo trata como deuda
                     * finiquito
                     */
                } else {

                    /*Se actualizan las cifras del registro que corresponde al pago a capital y los actualiza
                en la base de datos*/
                    amoPagoCapital.setAmoAmortizacion(0.0);
                    amoPagoCapital.setAmoInteres(0.0);
                    amoPagoCapital.setAmoMontoPago(0.0);
                    amoPagoCapital.setAmoEstatusInt(Constantes.AMO_ESTATUS_DEUDA_FIN_10);

                    amoDao.mergePojo(amoPagoCapital);

                    /**
                     * Crea el pago a capital
                     */
                    Double capitalPagoCapital = amoPagoCapital.getAmoCapital() + capitalCatorcenasPasadas + interesCatorcenasPasadas;

                    /**
                     * Se actualiza el monto de capital que se debe
                     */
                    amoPagoCapital.setAmoCapital(capitalPagoCapital - abonoACapital);

                    Amortizacion amortNueva = new Amortizacion();
                    amortNueva.setAmoNumeroPago(amoPagoCapital.getAmoNumeroPago() + 1);
                    amortNueva.setAmoCapital(capitalPagoCapital);
                    amortNueva.setAmoAmortizacion(abonoACapital);
                    amortNueva.setAmoInteres(0.0);
                    amortNueva.setAmoIva(0.0);
                    amortNueva.setAmoMontoPago(abonoACapital);
                    amortNueva.setAmoFechaPago(catDao.getCatInmediataSiguiente(amoPagoCapital.getAmoFechaPago()));
                    amortNueva.setAmoEstatusInt(estatus);
                    amortNueva.setAmoUsuId(amoPagoCapital.getAmoUsuId());
                    amortNueva.setCreditosFinal(amoPagoCapital.getCreditosFinal());

                    this.ejecutaPagoCapital(amoPagoCapital, abonoACapital, 1, estatus, amortNueva);

//                actualiza estatus credito ajustado
                    if (Objects.equals(estatus, Constantes.AMO_ESTATUS_ABONO_CRE_8)) {
                        creBo.updtCreditoEstatus(amoPagoCapital.getCreditosFinal().getCreId(), Constantes.CRE_EST_AJUSTADO, null);
                    } else {
                        creBo.updtCreditoEstatus(amoPagoCapital.getCreditosFinal().getCreId(), Constantes.CRE_EST_AJUST_FINIQ, null);

                    }
                }
                /**
                 * Cuando la deuda está conformada unicamente de capital
                 */
            } else if (amoPendts.isEmpty() && amoPagoCapital != null) {
                LOGGER.debug("La deuda esta conformada por CAPITAL");

                Amortizacion amortNueva = new Amortizacion();
                amortNueva.setAmoNumeroPago(amoPagoCapital.getAmoNumeroPago());
                amortNueva.setAmoCapital(amoPagoCapital.getAmoCapital());
                amortNueva.setAmoAmortizacion(abonoACapital);
                amortNueva.setAmoInteres(0.0);
                amortNueva.setAmoIva(0.0);
                amortNueva.setAmoMontoPago(abonoACapital);
                amortNueva.setAmoFechaPago(catDao.getCatInmediataSiguiente(amoPagoCapital.getAmoFechaPago()));
                amortNueva.setAmoEstatusInt(estatus);
                amortNueva.setAmoUsuId(amoPagoCapital.getAmoUsuId());
                amortNueva.setCreditosFinal(amoPagoCapital.getCreditosFinal());

                Double diferencia = abonoACapital - amoPagoCapital.getAmoCapital();

                //Si no hay diferencia significa que el credito quedara pagado
                if (diferencia <= 2 && diferencia >= -2) {
                    amoPagoCapital.setAmoAmortizacion(abonoACapital);
                    amoPagoCapital.setAmoInteres(0.0);
                    amoPagoCapital.setAmoMontoPago(abonoACapital);
                    amoPagoCapital.setAmoEstatusInt(estatus);
                    amoDao.mergePojo(amoPagoCapital);

                    amoDao.borraAmortizacionPosteriorCap(amoPagoCapital.getAmoNumeroPago(), amoPagoCapital.getCreditosFinal().getCreId());
                    //  actualiza estatus credito ajustado
                    creBo.updtCreditoEstatus(amoPagoCapital.getCreditosFinal().getCreId(), Constantes.CRE_EST_PAGADO, null);

                    //Cuando hay diferencia signifca que se ajusta el credito y se genera el pago a capital
                } else {
                    /**
                     * Ejecuta el pago a capital cuando la diferencia es mayor a
                     * 2 pesos
                     */

                    amoPagoCapital.setAmoNumeroPago(amoPagoCapital.getAmoNumeroPago() - 1);
                    ejecutaPagoCapital(amoPagoCapital, abonoACapital, 1, estatus, amortNueva);
                    // actualiza estatus credito pagado
                    creBo.updtCreditoEstatus(amoPagoCapital.getCreditosFinal().getCreId(), Constantes.CRE_EST_AJUSTADO, null);
                }
            }
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public Amortizacion creaAmortUltimoPago(Integer numeroPago,
            Date catorcenaSeleccionada, Double capital, Double montoPago,
            Double amortizacion, Double interes, Integer usuId, CreditosFinal credito) throws BusinessException {
        try {
            Amortizacion amo = new Amortizacion();
            amo.setAmoNumeroPago(numeroPago);
            amo.setAmoFechaPago(catDao.getCatInmediataSiguiente(catorcenaSeleccionada));
            amo.setAmoEstatusInt(Constantes.AMO_ESTATUS_PEND_1);
            amo.setAmoCapital(capital);
            amo.setAmoMontoPago(montoPago);
            amo.setAmoAmortizacion(amortizacion);
            amo.setAmoIva(0.0);
            amo.setAmoInteres(interes);
            amo.setAmoUsuId(usuId);
            amo.setCreditosFinal(credito);

            return amo;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public Amortizacion creaPagoCapital(Integer numeroPago,
            Double capital, Double amortizacion,
            Double interes, Double montoPago, Integer estatusAmo,
            Integer usuId, CreditosFinal credito) {

        Amortizacion amo = new Amortizacion();
        amo.setAmoNumeroPago(numeroPago);
        amo.setAmoFechaPago(new Date());
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
     * Guarda un registro en la tabla pagos de tipo finiquito
     *
     * @param creId
     * @param montoFiniq
     * @param empId
     * @param fech
     * @param usuId
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public Integer guardaFiniquito(Integer creId, Double montoFiniq, Integer empId, Date fech, Integer usuId) throws BusinessException {
        try {
            BancosDao banDao =  new BancosDao();
            
            pagoDao = new PagosDao();
            Pagos pago = new Pagos();

            pago.setPagAcumulado(0.0);
            pago.setPagCredito(creId);
            pago.setPagDeposito(montoFiniq);
            pago.setPagEmpresa(empId);
            pago.setPagEstatus(Constantes.PAGEST_FINIQ_12);
            pago.setPagFecha(fech);
            pago.setPagUsuId(usuId);

            pagoDao.guardaPago(pago);
            
            /**
             * Crea un objeto tipo Banco que se insertará y hará referencia al pago de finiquito
             * que se creó anteriormente
             */
            Bancos banco = new Bancos();
            
            banco.setBanAjustado(Constantes.BAN_NO_AJUSTADO);
            banco.setBanConcepto(Constantes.BAN_DESCTOXCOB_CRED);
            banco.setBanIdConceptoSistema(pago.getPagId());
            banco.setBanMonto(pago.getPagDeposito());
            banco.setBanEmpresa(empId);
            
            banDao.saveBanco(banco);

            return pago.getPagId();
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que actualiza el pagid en la amortizacion de un credito
     *
     * @param pagId
     * @param creId
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public void updtAmoPagId(Integer pagId, Integer creId) throws BusinessException {
        try {
            amoDao.updtPagId(pagId, creId);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene un usuario por empresa y clave de empleado
     *
     * @param claveEmpleado
     * @param empresaId
     * @return
     * @throws BusinessException
     */
    public List<Usuarios> buscaUsuario(Integer claveEmpleado, Integer empresaId) throws BusinessException {
        try {
            return usuDao.getUsuarioXCveYEmpresa(claveEmpleado, empresaId);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que actualiza el estatus de un usuario a baja - 0
     *
     * @param usuario
     * @throws BusinessException
     */
    public void updtEstatusUsuario(Usuarios usuario) throws BusinessException {
        try {
            usuDao.updatePojo(usuario);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    

    public void insertBajaEmpleado(BajaEmpleados bae) throws BusinessException {
        try {
            finDao.insertBajaEmpleados(bae);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Actualiza el saldo del credito y el monto del ahorro en la baja de un
     * usuario
     *
     * @param usuId
     * @param saldoCre
     * @param ahorro
     * @param estatus
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public void updtBaeSaldoAhorro(Integer usuId, Double saldoCre, Double ahorro, Integer estatus) throws BusinessException {
        try {
            finDao.updtBaeSaldoAhorro(usuId, saldoCre, ahorro, estatus);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

}
