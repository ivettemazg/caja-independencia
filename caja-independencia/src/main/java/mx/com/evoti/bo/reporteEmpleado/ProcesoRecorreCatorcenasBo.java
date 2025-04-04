/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.reporteEmpleado;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.impl.AmortizacionTransformerImpl;
import mx.com.evoti.bo.util.Serialization;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;

/**
 *
 * @author Venus
 */
public class ProcesoRecorreCatorcenasBo implements Serializable,AmortizacionTransformerImpl {

    private static final long serialVersionUID = -6189322032038465474L;

    private AmortizacionDao amoDao;
    private CreditosBo creditoBo;

    public ProcesoRecorreCatorcenasBo() {
        amoDao = new AmortizacionDao();
        creditoBo = new CreditosBo();
    }

    public void principalRecorreCatorcenas(List<AmortizacionDto> amortizacion, List<AmortizacionDto> pagosSeleccionados, Integer usuId) throws BusinessException {
        //Agrega a la amortizacion las catorcenas que se añadirían al final de la lista
        System.out.println("Antes de preparaAmortizacion");
        preparaAmortizacion(amortizacion, pagosSeleccionados.size());
        System.out.println("Despues de preparaAmortizacion");
        //Recorre las listas intermedias de capital, amortizacion, interes, pago asignando ceros a las catorcenas recorridas
        System.out.println("Antes de recorre catorcenas");
        recorreCatorcenas(amortizacion, pagosSeleccionados);
        System.out.println("Despues de recorre catorcenas");

        System.out.println("Antes de actualizaTabla");
        actualizaTabla(amortizacion, pagosSeleccionados.get(0).getAmoCredito(), usuId);
        System.out.println("Despues de actualizaTabla");

    }

    /**
     * Agrega los pagos de acuerdo a las catorcenas que se van a recorrer
     *
     * @param amortizacion
     * @param pagosAnadidos numero de catorcenas que se van a recorrer
     */
    private void preparaAmortizacion(List<AmortizacionDto> amortizacion, Integer pagosAnadidos) throws BusinessException {

        try {
            /*Se ordena para que quede el número de pago más grande hasta atrás
            * en vez de algún nulo o cero que corresponda a un total
             */
            System.out.println("antes de ordenar_____________________________________");
            for (AmortizacionDto dto : amortizacion) {
                System.out.println("Amo id  " + dto.getAmoId() + "  Amo nueor " + dto.getAmoNumeroPago() + "  Fecha " + dto.getAmoFechaPago());
            }

            Util.ordena(amortizacion, "amoNumeroPago");

            System.out.println("despues de ordenar_____________________________________");
            for (AmortizacionDto dto : amortizacion) {
                System.out.println("Amo id  " + dto.getAmoId() + "  Amo nueor " + dto.getAmoNumeroPago() + "  Fecha " + dto.getAmoFechaPago());
            }

            AmortizacionDto ultimoPago = amortizacion.get(amortizacion.size() - 1);
            Date ultimaFecha = ultimoPago.getAmoFechaPago();
            Integer noPago = ultimoPago.getAmoNumeroPago();
            Integer idAmo = amoDao.consultaUltimoPagoAmortizacion();

            for (int i = 0; i < pagosAnadidos; i++) {
                AmortizacionDto pagoAux = new AmortizacionDto();

                noPago++;
                ultimaFecha = Util.sumarORestarDiasFecha(ultimaFecha, 14);
                idAmo++;

                pagoAux.setAmoId(idAmo);
                pagoAux.setAmoNumeroPago(noPago);
                pagoAux.setAmoFechaPago(ultimaFecha);
                pagoAux.setAmoCapital(ultimoPago.getAmoCapital());
                pagoAux.setAmoAmortizacion(ultimoPago.getAmoAmortizacion());
                pagoAux.setAmoInteres(ultimoPago.getAmoInteres());
                pagoAux.setAmoMontoPago(ultimoPago.getAmoMontoPago());
                pagoAux.setAmoEstatusInt(ultimoPago.getAmoEstatusInt());
                pagoAux.setAmoPagoId(ultimoPago.getAmoPagoId());
                pagoAux.setAmoCredito(ultimoPago.getAmoCredito());

                amortizacion.add(pagoAux);

            }

            System.out.println("despues de añadir los pagos_____________________________________");
            for (AmortizacionDto dto : amortizacion) {
                System.out.println("Amo id  " + dto.getAmoId() + "  Amo nueor " + dto.getAmoNumeroPago() + "  Fecha " + dto.getAmoFechaPago());
            }
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    /**
     * Metodo que hace toda la lógica de recorrer las catorcenas
     *
     * @param amortizacion la amortizacio original
     * @param pagosSeleccionados los pagos seleccionados en la tabla
     */
    public void recorreCatorcenas(List<AmortizacionDto> amortizacion,
            List<AmortizacionDto> pagosSeleccionados) {

        Boolean coincidePago = Boolean.FALSE;
        List<AmortizacionDto> amortizacionAux = Serialization.copy(amortizacion);

        /**
         * Se guarda copia profunda de la amortizacion
         */
        for (AmortizacionDto pago : amortizacion) {
            AmortizacionDto pagoAux = new AmortizacionDto();
            pagoAux.setAmoNumeroPago(pago.getAmoNumeroPago());
            pagoAux.setAmoId(pago.getAmoId());
            pagoAux.setAmoCapital(pago.getAmoCapital());
            pagoAux.setAmoAmortizacion(pago.getAmoAmortizacion());
            pagoAux.setAmoInteres(pago.getAmoInteres());
            pagoAux.setAmoMontoPago(pago.getAmoMontoPago());
            pagoAux.setAmoFechaPago(pago.getAmoFechaPago());
            pagoAux.setAmoEstatusInt(pago.getAmoEstatusInt());
            pagoAux.setAmoPagoId(pago.getAmoPagoId());
            pagoAux.setAmoCredito(pago.getAmoCredito());

            amortizacionAux.add(pagoAux);
        }

        //Se recorre la amortizacion principal
        for (AmortizacionDto pago : amortizacion) {
            //Se recorren las catorcenas que se pondrán como RECORRIDAS
            for (AmortizacionDto pagoSel : pagosSeleccionados) {

                if (pago.getAmoNumeroPago().equals(pagoSel.getAmoNumeroPago())) {
                    coincidePago = Boolean.TRUE;
                    break;
                }
            }
            /**
             * Cuando coincide el id de la amortizacion principal con alguna de
             * las catorcenas que hay que recorrer, en la amortizacion
             * principal, se ponen ceros en capital, amortizacion, interes,
             * monto pago y en estatus se pone RECORRIDA Tambien aplica cuando
             * el pago en cuestión ya se ha recorrido anteriormente
             */
            if (coincidePago || Objects.equals(pago.getAmoEstatusInt(), Constantes.AMO_ESTATUS_RECORRIDA_13)) {

                pago.setAmoCapital(0.0);
                pago.setAmoAmortizacion(0.0);
                pago.setAmoInteres(0.0);
                pago.setAmoMontoPago(0.0);
                pago.setAmoEstatusInt(Constantes.AMO_ESTATUS_RECORRIDA_13);
                System.out.println(pago.getAmoCapital() + " " + pago.getAmoAmortizacion() + " " + pago.getAmoInteres() + " " + pago.getAmoMontoPago() + " " + pago.getAmoEstatusInt() + " " + pago.getAmoFechaPago());
            } else {

                Iterator<AmortizacionDto> credIt = amortizacionAux.iterator();
                while (credIt.hasNext()) {
                    AmortizacionDto cred = credIt.next();
                    //Se valida porque de lo contrario desacomoda la amortizacion cuando
                    //hay otras catorcenas recorridas anteriormente
                    if (!Objects.equals(cred.getAmoEstatusInt(), Constantes.AMO_ESTATUS_RECORRIDA_13)) {
                        pago.setAmoCapital(cred.getAmoCapital());
                        pago.setAmoAmortizacion(cred.getAmoAmortizacion());
                        pago.setAmoInteres(cred.getAmoInteres());
                        pago.setAmoIva(cred.getAmoIva());
                        pago.setAmoMontoPago(cred.getAmoMontoPago());
                        System.out.println(pago.getAmoCapital() + " " + pago.getAmoAmortizacion() + " " + pago.getAmoInteres() + " " + pago.getAmoMontoPago() + " " + pago.getAmoEstatusInt() + " " + pago.getAmoFechaPago());

                        credIt.remove();
                        break;
                    } else {
                        credIt.remove();
                    }

                }
            }

            coincidePago = Boolean.FALSE;
        }

    }

    /**
     * Actualiza la tabla con la nueva amortizacion, para lo cual primero borra
     * la amortizacion anterior y posteriormente inserta la nueva
     *
     * @param amortizacion
     * @param idCredito
     * @param usuId
     * @throws BusinessException
     */
    public void actualizaTabla(List<AmortizacionDto> amortizacion, Integer idCredito, Integer usuId) throws BusinessException {
        try {
            amoDao.borraAmortizacion(idCredito);

            amortizacion.forEach(amo -> {
                amo.setAmoCredito(idCredito);
                amo.setAmoUsuId(usuId);
            });

            CreditosFinal creditoPojo = creditoBo.obtieneCredXId(idCredito);
            
             List<Amortizacion> amoPojos =convertDtoToPojoAmortizacion(amortizacion, creditoPojo);
                    
            amoDao.insertaAmortizacion(amoPojos);

        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Metodo que valida si hay un pago a capital posterior a la fecha de la
     * catorcena que se va a recorrer, en cuyo caso regresaría un mensaje de
     * error, ya que no pueden haber pagos a capital posteriores a las
     * catorcenas que se van a recorrer. Es aceptable que hayan pagos a capital
     * anteriores, de ser así regresa un String vacío
     *
     * @param amortizacion la amortizacion que se muestra en la tabla
     * @param pagosSeleccionados los pagos seleccionados en la tabla
     * @return
     */
    public String validaPagoACapital(List<AmortizacionDto> amortizacion, List<AmortizacionDto> pagosSeleccionados) {
        System.out.println("Dentro de validaPagoACapital");
        String msjError = "Las siguientes catorcenas no se procesaron ya que existe en la amortización un pago a capital más reciente :\n";
        Boolean hasPagoCapitalAntes = Boolean.FALSE;
        System.out.println("----------------------pagosSeleccionados"+ pagosSeleccionados.size());
        Iterator<AmortizacionDto> pagoSel = pagosSeleccionados.iterator();

        while (pagoSel.hasNext()) {

            AmortizacionDto pago = pagoSel.next();

            if (!isValidPagoACapital(amortizacion, pago.getAmoNumeroPago())) {
                System.out.println("Entro al if !isValidPagoACapital");
                pagoSel.remove();
                hasPagoCapitalAntes = Boolean.TRUE;
                msjError = msjError.concat("No. Catorcena " + pago.getAmoNumeroPago() + " Fecha catorcena: " + pago.getAmoFechaPago() + "\n");
            }
            

        }
        if (hasPagoCapitalAntes) {
            return msjError;
        } else {
            return "";
        }
    }

    /**
     * Valida si el credito tiene un pago a capital, y de ser así para poder
     * recorrer, el pago a capital debe de estar antes de la primer catorcena
     * que vamos a recorrer. Para determinar si es un pago a capital debe
     * cumplir con las siguientes validaciones: -Que la amortizacion y el pago
     * sean del mismo monto (indica que es un pago a capital) -la amortizacion
     * debe ser mayor a cero (descarta las catorcenas recorridas) -el interes
     * siempre debe ser cero Para que sea un pago a capital aceptado para
     * recorrer catorcenas debe cumplir con: -que numero del pago a capital sea
     * anterior al numero de pago de la catorcena que se va a recorrer
     *
     * @param amortizaciones
     * @param primerPago
     * @return
     */
    public boolean isValidPagoACapital(List<AmortizacionDto> amortizaciones, Integer primerPago) {
        System.out.println("Dentro de isValidPagoACapital");
        System.out.println("primer pago  "+primerPago+ "  amortizaciones  "+amortizaciones.size());
        for (AmortizacionDto dto : amortizaciones) {
            if (dto.getAmoAmortizacion().equals(dto.getAmoMontoPago())) {
                if (dto.getAmoAmortizacion() > 0.0) {
                    if (dto.getAmoInteres() == 0.0) {

                        if (primerPago > dto.getAmoNumeroPago()) {
                            return Boolean.TRUE;
                        } else {
                            return Boolean.FALSE;
                        }
                    }
                }
            }
        }
        return Boolean.TRUE;

    }

    
     public void actualizaFechaNuevoMonto(Date catorcenaSiguiente, int idCredito) throws BusinessException{
         creditoBo.actualizaFechaNuevoMonto(catorcenaSiguiente, idCredito);
    }
    
    
    public static void main(String args[]) {
        try {
          
            List<AmortizacionDto> amortizacion = new ArrayList<>();
            List<AmortizacionDto> pagados = new ArrayList<>();
            AmortizacionDto cred = new AmortizacionDto();
            cred.setAmoId(1);
            cred.setAmoNumeroPago(1);
            cred.setAmoCapital(1000.1);
            cred.setAmoAmortizacion(2000.0);
            cred.setAmoInteres(100.0);
            cred.setAmoFechaPago(Util.generaFechaDeString("2014-11-13"));
            AmortizacionDto cred2 = new AmortizacionDto();
            cred2.setAmoId(2);
            cred2.setAmoNumeroPago(2);
            cred2.setAmoCapital(1200.1);
            cred2.setAmoAmortizacion(2200.0);
            cred2.setAmoInteres(100.0);
            cred2.setAmoFechaPago(Util.generaFechaDeString("2014-11-27"));
            AmortizacionDto cred3 = new AmortizacionDto();
            cred3.setAmoId(3);
            cred3.setAmoNumeroPago(3);
            cred3.setAmoCapital(1300.1);
            cred3.setAmoAmortizacion(2300.0);
            cred3.setAmoInteres(100.0);
            cred3.setAmoFechaPago(Util.generaFechaDeString("2014-12-11"));
            AmortizacionDto cred4 = new AmortizacionDto();
            cred4.setAmoNumeroPago(4);
            cred4.setAmoCapital(1300.1);
            cred4.setAmoAmortizacion(2300.0);
            cred4.setAmoInteres(100.0);

            amortizacion.add(cred4);
            amortizacion.add(cred3);
            amortizacion.add(cred2);
            amortizacion.add(cred);

            ProcesoRecorreCatorcenasBo bo = new ProcesoRecorreCatorcenasBo();

            bo.preparaAmortizacion(amortizacion, 4);
            System.out.println("pueadfasdf");
        } catch (BusinessException ex) {
            Logger.getLogger(ProcesoRecorreCatorcenasBo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

   
}
