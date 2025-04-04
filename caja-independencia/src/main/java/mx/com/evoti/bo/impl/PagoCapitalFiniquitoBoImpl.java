/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import mx.com.evoti.bo.TablaAmortizacionBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.CatorcenasDao;
import mx.com.evoti.dao.PagosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.util.Constantes;

/**
 *
 * @author Venette
 */
public interface PagoCapitalFiniquitoBoImpl extends AmortizacionTransformerImpl {

    /**
     * Realiza toda la funcionalidad de introducir un pago a capital dentro de
     * una amortizacion
     *
     * @param amortizacion
     * @param pagoCapital
     * @param catorcenas
     * @param amoEstatus
     * @param amortNueva
     * @return
     * @throws BusinessException
     */
    default public boolean ejecutaPagoCapital(Amortizacion amortizacion, Double pagoCapital,
            Integer catorcenas, Integer amoEstatus, Amortizacion amortNueva) throws BusinessException {
        try {
            AmortizacionDao amoDao = new AmortizacionDao();
            CatorcenasDao catDao = new CatorcenasDao();
            TablaAmortizacionBo amoBo = new TablaAmortizacionBo();
            PagosDao pagoDao = new PagosDao();

            amoBo.setIdUsuario(amortizacion.getAmoUsuId());
            
            Date catorcenaSeleccionada = amortizacion.getAmoFechaPago();

            /**
             * Borra la amortización posterior al pago a capital
             */
            amoDao.borraAmortizacionPosteriorCap(amortizacion.getAmoNumeroPago(), amortizacion.getCreditosFinal().getCreId());

            BigDecimal saldoRestanteCredito = new BigDecimal(amortizacion.getAmoCapital() - pagoCapital).setScale(2, RoundingMode.HALF_DOWN);
            Date catorcenaSiguiente = catDao.getCatInmediataSiguiente(catorcenaSeleccionada);

            if (Objects.equals(amoEstatus, Constantes.AMO_ESTATUS_CAPITAL_7)) {

                /**
                 * NOTA: los pagos a capital solo pueden realizarse en creditos
                 * de auto y de nomina, no aplica en los creditos de fa y ag
                 */
                List<AmortizacionDto> amortizaciones = amoBo.generaTablaAmortizacion(amortizacion.getAmoNumeroPago() + 1, saldoRestanteCredito.doubleValue(), catorcenas, catorcenaSiguiente, amortizacion.getCreditosFinal().getCreProducto(), 0);

                /**
                 * Se transforma el resultado del metodo generatablaamortizacion
                 * que viene en formato dto a pojo para poder guardar
                 * directamente la lista
                 */
                List pojos = convertDtoToPojoAmortizacion(amortizaciones, amortizacion.getCreditosFinal(),Constantes.AMO_ESTATUS_PEND_1);
                amoDao.savePojos(pojos);

                /**
                 * Genera y guarda pago a capital en tabla pagos
                 */
                Pagos pago = new Pagos();
                pago.setPagAcumulado(0.0);
                pago.setPagCredito(amortizacion.getCreditosFinal().getCreId());
                pago.setPagDeposito(pagoCapital);
                pago.setPagUsuId(amortizacion.getAmoUsuId());
                //Tipo de pago "CAPITAL"
                pago.setPagEstatus(9);
                pago.setPagFecha(catorcenaSeleccionada);

                pagoDao.guardaPago(pago);
                
                
                //@TODO falta codigo que actualiza la amortizacionn en la que se metió el pago a capital

            } else {//Cuando es un abono a credito o finiquito
               
           

                //Se pone como tipo de credito nomina siempre, ya que aquí si pueden venir creditos de fa y ag
                List<AmortizacionDto> amortizaciones = amoBo.generaTablaAmortizacion(amortNueva.getAmoNumeroPago() + 1,
                        amortNueva.getAmoCapital()-amortNueva.getAmoAmortizacion(), catorcenas, catDao.getCatInmediataSiguiente(amortNueva.getAmoFechaPago()), 6,0);
                 
               
                
                List pojos = convertDtoToPojoAmortizacion(amortizaciones, amortizacion.getCreditosFinal(),Constantes.AMO_ESTATUS_PEND_1);
                
                pojos.add(0, amortNueva);
                
                amoDao.savePojos(pojos);

            }

            return Boolean.TRUE;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }

    }
    
}
