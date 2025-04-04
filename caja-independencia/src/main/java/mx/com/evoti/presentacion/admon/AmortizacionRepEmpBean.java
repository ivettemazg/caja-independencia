/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import mx.com.evoti.bo.CatorcenasBo;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.TablaAmortizacionBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.reporteEmpleado.ProcesoPagoAcumuladoBo;
import mx.com.evoti.bo.reporteEmpleado.ProcesoPagoCapitalBo;
import mx.com.evoti.bo.reporteEmpleado.ProcesoPagoExtemporaneoBo;
import mx.com.evoti.bo.reporteEmpleado.ProcesoRecorreCatorcenasBo;
import mx.com.evoti.bo.transaccion.TransaccionBo;
import mx.com.evoti.dto.AcumuladoDto;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "amoRepEmpBean")
@ViewScoped
public class AmortizacionRepEmpBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AmortizacionRepEmpBean.class);
    private static final long serialVersionUID = 6268596514136628795L;

    private CreditosBo credBo;
    private CatorcenasBo catBo;
    private ProcesoRecorreCatorcenasBo recorreBo;
    private TablaAmortizacionBo tAmortizacionBo;
    private TransaccionBo tranBo;
    
    private Usuarios usuario;
    private final UsuarioDto userSession;
    
    private DetalleCreditoDto credito;
private List<AmortizacionDto> amortizacion;
    private List<AmortizacionDto> amoSelected;
    private AmortizacionDto pagoSeleccionado;

    private Boolean dsbActions;

    //BLOQUE RECORRER CATORCENAS
    private List<String> catorcenas;
    private Boolean rdrRecorreCat;
    private Date catorcenaSiguiente;
    private String mandarAReporte;

    //BLOQUE PAGOS A CAPITAL
    private String msjErrorPagos;
    private Boolean rdrPagoCapital;
    private Double montoNuevoPago;
    private Integer catorcenasRestantes;
    private Double saldoDespuesPagoCapital;
    private Double montoPagoACapital;
    private Date fechaPagoCapital;

    //BLOQUE PAGOS EXTEMPORANEOS
    private Double montoTotalCatorcenas;
    private Boolean rdrPagoExtemp;
    private Integer catorcenasSeleccionadas;
    private Double restantePago;
    private Double montoPagoExtemp;
    private Date fechaPagoExtmp;

    //BLOQUE SALDO A FAVOR
    private Integer catSeleccionadasSaldoF;
    private Double montoTotalCatSaldoF;
    private AcumuladoDto acumuladoCliente;
    private ProcesoPagoAcumuladoBo ppacBo;
    private Boolean rdrAcumulado;
    private Double restanteAcumulado;
    private Double catorcenasCubiertas;
    private String msjErrorAcumulado;
    private Date fechaPagoAcum;
    private Boolean rdrScrPagAcumXPagCap;

    //Bloque pago con ahorro
    private Boolean rdrPagoAhorro;
    private Double montoTotalCatSelected;
     private MovimientosBo movsBo;
    private List<MovimientosDto> movimientos;
    private List<Movimientos> abonosCredito;
    private Boolean esAhorro;
    private Boolean rdrBtnAjustar;
    private Integer montoFiniquito;
    private String cveCredSeleccionado;
    private Double montoAbonoCredito;
    private Double saldoCredito;
   

    public AmortizacionRepEmpBean() {
        userSession = (UsuarioDto) this.getSession().getAttribute("usuario");
        catBo = new CatorcenasBo();
        credBo = new CreditosBo();
        recorreBo = new ProcesoRecorreCatorcenasBo();
        tAmortizacionBo = new TablaAmortizacionBo();
        ppacBo = new ProcesoPagoAcumuladoBo();
        movsBo = new MovimientosBo();
        tranBo = new TransaccionBo();
       
    }

    public void obtieneAmortizacion() {
        try {
            if (amoSelected != null) {
                amoSelected.clear();
            }

            amortizacion = credBo.getAmortizacionXCredito(credito.getCreId());

            if (credito.getCreEstatusId() == 2 || credito.getCreEstatusId() == 3
                    || credito.getCreEstatusId() == 4 || credito.getCreEstatusId() == 5) {
                dsbActions = Boolean.TRUE;
            } else {
                dsbActions = Boolean.FALSE;
            }

        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al consultar la amortización", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void rowSelect() {
        LOGGER.info("rowSelect");
    }

    public void rowUnselect() {
        LOGGER.info("rowUnselect");
    }

    /**
     * *
     * COMIENZA BLOQUE RECORRER CATORCENAS
     */
    public void inicializaRecorrerCatorcenas() {
        catorcenaSiguiente = null;
        //Inicializa las catorcenas siguientes
        obtieneCatorcenasProx();

        if (amoSelected.size() < 1) {
            super.muestraMensajeError("Debe seleccionar al menos una catorcena", "", null);
            rdrRecorreCat = Boolean.FALSE;
        } else {

            pagoSeleccionado = amoSelected.get(0);

            for (AmortizacionDto amo : amoSelected) {
                //Solo cuando las catorcenas estan en estatus PENDIENTE o equivalente, se pueden recorrer
                if (Objects.equals(Constantes.AMO_ESTATUS_PEND_1, amo.getAmoEstatusInt()) || Objects.equals(Constantes.AMO_ESTATUS_PMEN_3, amo.getAmoEstatusInt())) {
                    rdrRecorreCat = Boolean.TRUE;

                } else if (Objects.equals(Constantes.AMO_ESTATUS_RECORRIDA_13, amo.getAmoEstatusInt())) {
                    rdrRecorreCat = Boolean.FALSE;
                    super.muestraMensajeError("Debe elegir una catorcena que no esté recorrida", "", null);
                } else {
                    rdrRecorreCat = Boolean.FALSE;
                    super.muestraMensajeError("Debe elegir catorcenas que estén pendientes", "", null);

                }
            }
        }
    }

    /**
     * Metodo que ejecuta el algoritmo de recorrer catorcenas
     */
    public void recorreCatorcenas() {
        LOGGER.info("Dentro de recorrerCatorcenas");
        try {
            //Se valida si hay un pago a capital más reciente
            String msj = recorreBo.validaPagoACapital(amortizacion, amoSelected);
            System.out.println("Despues de validaPagoACapital");
            if (msj.isEmpty()) {

                //Valida que haya seleccionado la opcion de mandar al reporte de nomina
                if (mandarAReporte.isEmpty()) {
                    String error = "Debe seleccionar una opción.";
                    super.muestraMensajeError(error, "", null);

                } else {

                    recorreBo.principalRecorreCatorcenas(amortizacion, amoSelected, usuario.getUsuId());

                    /**
                     * Aqui se manda al reporte de nomina
                     */
                    if (mandarAReporte.equals("true")) {
                        recorreBo.actualizaFechaNuevoMonto(catorcenaSiguiente, credito.getCreId());
                    }

                    boolean estaPagado = credBo.saldarCredito(credito, amortizacion);
                    //Si se actualizo el credito, actualiza la tabla de creditos
                    if (estaPagado) {
                        super.updtComponent("frmCredsEmpleado");
                    }
                    super.hideShowDlg("PF('dlgRecorrerCatorcenas').hide()");
                    super.muestraMensajeExito("Las catorcenas fueron recorridas", "", null);
                    /**
                     * Actualiza el form de la tabla amortizacion para que
                     * vuelva a cargar los valores en pantalla
                     */
                    this.obtieneAmortizacion();
                    super.updtComponent("frmAmortizacion:tblAmortizacion");
                    super.updtComponent("frmAmortizacion:dlgAmoRep");

                }
            } else {
                String error = "No pueden recorrerse las catorcenas cuando hay un pago a capital más reciente.";
                super.muestraMensajeError(error, "", null);
            }
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al realizar el proceso de recorrer catorcenas. ", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    public void obtieneCatorcenasProx() {
        try {
            catorcenas = catBo.getCatorcenasSiguientes(new Date());
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void inicializaPagoACapital() {
        LOGGER.info("Action del botón activado. Dentro de inicializaPagoACapital ");

        if (amoSelected.size() > 1) {
            msjErrorPagos = "Para agregar un pago a capital es necesario seleccionar sólo la última catorcena y usted seleccionó " + amoSelected.size() + " catorcenas.";
            super.muestraMensajeError(msjErrorPagos, "", null);
            rdrPagoCapital = Boolean.FALSE;
        } else if (amoSelected.size() < 1) {
            msjErrorPagos = "Debe seleccionar una catorcena para aplicar un pago a capital.";
            super.muestraMensajeError(msjErrorPagos, "", null);
            rdrPagoCapital = Boolean.FALSE;
        } else {

            pagoSeleccionado = amoSelected.get(0);

            /**
             * Valida que el estatus de la catorcena seleccionada sea pendiente
             * o equivalente a pendiente
             */
            if (Objects.equals(Constantes.AMO_ESTATUS_PEND_1, pagoSeleccionado.getAmoEstatusInt()) || Objects.equals(Constantes.AMO_ESTATUS_PMEN_3, pagoSeleccionado.getAmoEstatusInt())) {
                rdrPagoCapital = Boolean.TRUE;
                catorcenasRestantes = (Integer) amortizacion.size() - (pagoSeleccionado.getAmoNumeroPago() - 1);
                montoNuevoPago = 0.0;
                saldoDespuesPagoCapital = 0.0;
                montoPagoACapital = 0.0;

            } else if (Objects.equals(Constantes.AMO_ESTATUS_RECORRIDA_13, pagoSeleccionado.getAmoEstatusInt())) {
                rdrPagoCapital = Boolean.FALSE;
                msjErrorPagos = "No puede aplicar un pago a capital sobre una catorcena que se recorrió. Por favor elija la última catorcena pendiente.";
                super.muestraMensajeError(msjErrorPagos, "", null);
            } else {
                rdrPagoCapital = (Boolean.FALSE);
                super.muestraMensajeError("No puede aplicar un pago a capital sobre una catorcena que ya está pagada. Por favor elija la última catorcena pendiente.", "", null);;
            }
        }

    }
    
    
    public void inicializaPagoACapitalAhorro() {
        inicializaPagoAhorro();
        LOGGER.info("Action del botón activado. Dentro de inicializaPagoACapital ");

        if (amoSelected.size() > 1) {
            msjErrorPagos = "Para agregar un pago a capital es necesario seleccionar sólo la última catorcena y usted seleccionó " + amoSelected.size() + " catorcenas.";
            super.muestraMensajeError(msjErrorPagos, "", null);
            rdrPagoCapital = Boolean.FALSE;
        } else if (amoSelected.size() < 1) {
            msjErrorPagos = "Debe seleccionar una catorcena para aplicar un pago a capital.";
            super.muestraMensajeError(msjErrorPagos, "", null);
            rdrPagoCapital = Boolean.FALSE;
        } else {

            pagoSeleccionado = amoSelected.get(0);

            /**
             * Valida que el estatus de la catorcena seleccionada sea pendiente
             * o equivalente a pendiente
             */
            if (Objects.equals(Constantes.AMO_ESTATUS_PEND_1, pagoSeleccionado.getAmoEstatusInt()) || Objects.equals(Constantes.AMO_ESTATUS_PMEN_3, pagoSeleccionado.getAmoEstatusInt())) {
                rdrPagoCapital = Boolean.TRUE;
                catorcenasRestantes = (Integer) amortizacion.size() - (pagoSeleccionado.getAmoNumeroPago() - 1);
                montoNuevoPago = 0.0;
                saldoDespuesPagoCapital = 0.0;
                montoPagoACapital = 0.0;

            } else if (Objects.equals(Constantes.AMO_ESTATUS_RECORRIDA_13, pagoSeleccionado.getAmoEstatusInt())) {
                rdrPagoCapital = Boolean.FALSE;
                msjErrorPagos = "No puede aplicar un pago a capital sobre una catorcena que se recorrió. Por favor elija la última catorcena pendiente.";
                super.muestraMensajeError(msjErrorPagos, "", null);
            } else {
                rdrPagoCapital = (Boolean.FALSE);
                super.muestraMensajeError("No puede aplicar un pago a capital sobre una catorcena que ya está pagada. Por favor elija la última catorcena pendiente.", "", null);;
            }
        }

    }
    
    public void onEditPagoCapitalAhorro(RowEditEvent event) {

        MovimientosDto mov = (MovimientosDto) event.getObject();

       if (mov.getDevolucion() == null) {
                    mov.setDevolucion(0.0);
                }
                montoAbonoCredito = 0.0;
                for (MovimientosDto movs : movimientos) {
                    montoAbonoCredito += movs.getDevolucion() != null ? movs.getDevolucion() : 0.0;
                }

                Double diferencia = montoAbonoCredito - pagoSeleccionado.getAmoCapital();
                if (diferencia > 3) {

                    mov.setDevolucion(0.0);
                    Double montoDevolucion = 0.0;
                    for (MovimientosDto movs : movimientos) {
                        if (movs.equals(mov)) {
                            montoDevolucion = movs.getDevolucion();
                        }
                    }
                    montoAbonoCredito -= montoDevolucion;
                    super.muestraMensajeError("El monto de la devolución no puede ser mayor al adeudo del crédito", "", null);
                } else {

                    /**
                     * Si pasa las validaciones, las guarda en una variable de
                     * entorno para que puedan guardarse en base de datos
                     * posteriormente
                     */
                   
                    abonosCredito = new ArrayList<>();

                    for (MovimientosDto movs : movimientos) {
//                          montoAbonoCredito+=movs.getDevolucion()!= null?movs.getDevolucion():0.0;
                        Movimientos pojo2 = creaDevolucion(movs, 2);

                        if (pojo2.getMovDeposito() < 0) {
                            abonosCredito.add(pojo2);
                        }
                    }

                    saldoCredito = montoTotalCatSelected - montoAbonoCredito;

                    if (montoAbonoCredito > 5) {
                        rdrBtnAjustar = Boolean.TRUE;
                    }
                    
                    /**
                     * Asigna el monto de abono a credito y lo pasa al texto monto pago a capital
                     */
                    montoPagoACapital = montoAbonoCredito;
                    actualizarSaldo(event);

                }

    }
    
    
    

    public void inicializaPagoExtemporaneo() {
        LOGGER.info("Dentro de inicializaPagoExtemporaneo ");

        boolean estaPagado = false;

        if (amoSelected.size() > 0) {
            for (AmortizacionDto amo : amoSelected) {
                if (!Objects.equals(Constantes.AMO_ESTATUS_PEND_1, amo.getAmoEstatusInt()) && !Objects.equals(Constantes.AMO_ESTATUS_PMEN_3, amo.getAmoEstatusInt())) {
                    estaPagado = true;
                    break;
                }
            }

            if (!estaPagado) {
                montoTotalCatorcenas = 0.0;
                catorcenasSeleccionadas = amoSelected.size();

                amoSelected.forEach(pago -> {
                    montoTotalCatorcenas += pago.getAmoMontoPago();
                });
                rdrPagoExtemp = Boolean.TRUE;

                montoTotalCatorcenas = Util.round(montoTotalCatorcenas);
                restantePago = 0.0;
                montoPagoExtemp = 0.0;
            } else {
                super.muestraMensajeError("No puede aplicar un pago a una catorcena que ya está pagada o recorrida, favor de verificar.", "", null);
                rdrPagoExtemp = Boolean.FALSE;
            }
        } else {
            super.muestraMensajeError("Para aplicar un pago extemporáneo debe seleccionar por lo menos 1 catorcena.", "", null);
            rdrPagoExtemp = Boolean.FALSE;
        }
    }

    /**
     * Metodo que actualiza el saldo del pago catorcenal a partir de las
     * catorcenas introducidas
     *
     * @param event
     */
    public void actualizarSaldo(AjaxBehaviorEvent event) {
        LOGGER.info("Dentro de actualizar saldo");
        if (catorcenasRestantes != null && montoPagoACapital != null && !amoSelected.isEmpty()) {
            try {
                pagoSeleccionado = amoSelected.get(0);

                saldoDespuesPagoCapital = pagoSeleccionado.getAmoCapital() - montoPagoACapital;

                CreditosFinal credPojo = credBo.obtieneCredXId(credito.getCreId());

                montoNuevoPago = tAmortizacionBo.generaMontoPago(saldoDespuesPagoCapital, catorcenasRestantes, credPojo.getCreProducto(),0);
            } catch (Exception ex) {
                super.muestraMensajeError("Hubo un error al actualizar el nuevo monto del pago", "", null);
                LOGGER.error(ex.getMessage(), ex);
            }

        }

    }

    public void aplicarPagoCapital() {
        ProcesoPagoCapitalBo ppcBo = new ProcesoPagoCapitalBo();

        List<String> msjError = new ArrayList<>();

        pagoSeleccionado = amoSelected.get(0);
        try {
            CreditosFinal crePojo;

            crePojo = credBo.obtieneCredXId(credito.getCreId());

            msjError = ppcBo.recalculaAmortizacion(montoPagoACapital, pagoSeleccionado, crePojo, catorcenasRestantes, usuario, fechaPagoCapital,1, userSession.getId());

            if (!msjError.isEmpty()) {
                //TODO meter el manejo de errores en pantalla
                String errors = "";

                for (String msj : msjError) {
                    errors = errors.concat(msj).concat("\n");
                }

                super.muestraMensajeError(errors, "", null);
                super.updtComponent("frmPagoCapital:msjErrPagosCapital");
            } else {

                super.hideShowDlg("PF('dlgPagosCapital').hide()");
                super.muestraMensajeExito("El pago a capital fue aplicado", "", null);
            }
            /**
             * Actualiza el form de la tabla amortizacion para que vuelva a
             * cargar los valores en pantalla
             */
            this.obtieneAmortizacion();
            super.updtComponent("frmAmortizacion:tblAmortizacion");
            super.updtComponent("frmCredsEmpleado");
            /**
             * Actualiza el growl
             */
            super.updtComponent("frmAmortizacion:dlgAmoRep");
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    
     public void aplicarPagoCapitalAhorro() {
        ProcesoPagoCapitalBo ppcBo = new ProcesoPagoCapitalBo();
        
         //Guarda las devoluciones como abono credito
        this.guardaDevoluciones(abonosCredito);

        List<String> msjError = new ArrayList<>();

        pagoSeleccionado = amoSelected.get(0);
        try {
            CreditosFinal crePojo;

            crePojo = credBo.obtieneCredXId(credito.getCreId());

            msjError = ppcBo.recalculaAmortizacion(montoPagoACapital, pagoSeleccionado, crePojo, catorcenasRestantes, usuario, fechaPagoCapital,1, userSession.getId());

            if (!msjError.isEmpty()) {
                //TODO meter el manejo de errores en pantalla
                String errors = "";

                for (String msj : msjError) {
                    errors = errors.concat(msj).concat("\n");
                }

                super.muestraMensajeError(errors, "", null);
                super.updtComponent("frmPagoCapitalAhorro:msjErrPagosCapital");
            } else {

                super.hideShowDlg("PF('dlgPagosCapitalAhorro').hide()");
                super.muestraMensajeExito("El pago a capital fue aplicado", "", null);
            }
            /**
             * Actualiza el form de la tabla amortizacion para que vuelva a
             * cargar los valores en pantalla
             */
            this.obtieneAmortizacion();
            super.updtComponent("frmAmortizacion:tblAmortizacion");
            super.updtComponent("frmCredsEmpleado");
            /**
             * Actualiza el growl
             */
            super.updtComponent("frmAmortizacion:dlgAmoRep");
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que se ejecuta al introducir el monto del pago extemporaneo
     *
     * @param event
     */
    public void actualizarRestantePago(AjaxBehaviorEvent event) {
        LOGGER.info("Dentro de actualizarRestantePago");
        Double diferenciaEntreMontos = montoPagoExtemp - montoTotalCatorcenas;

        /**
         * Cuando la diferencia es hasta de 1 peso el pago es considerado exacto
         */
        if (diferenciaEntreMontos >= -1.0) {

            restantePago = Util.round(montoPagoExtemp - montoTotalCatorcenas);

        } else if (montoPagoExtemp < montoTotalCatorcenas) {
            super.muestraMensajeError("El pago debe ser mayor o igual a la totalidad de la suma de las catorcenas. ", "", null);

        }
    }

    /**
     * Realiza el pago extemporaneo
     */
    public void realizarPago() {

        /**
         * Solamente cuando todas las catorcenas estan pendientes se puede
         * aplicar un pago extemporaneo
         */
        ProcesoPagoExtemporaneoBo pagoExtmpBo = new ProcesoPagoExtemporaneoBo();
        Double diferenciaEntreMontos = Util.round(montoPagoExtemp - montoTotalCatorcenas);
        try {
            if (diferenciaEntreMontos >= -1.0) {

                pagoExtmpBo.ejecutaProcesoPagoExt(amoSelected, usuario, montoPagoExtemp,
                        diferenciaEntreMontos, fechaPagoExtmp, credito.getCreId(), userSession.getId());
                
                super.muestraMensajeExito("El pago fue efectuado con éxito", "", null);
                super.hideShowDlg("PF('dlgPagoExtemp').hide()");
                this.obtieneAmortizacion();
                super.updtComponent("frmAmortizacion:tblAmortizacion");
                super.updtComponent("frmAmortizacion:dlgAmoRep");
                super.updtComponent("frmCredsEmpleado");
            } else if (montoPagoExtemp < montoTotalCatorcenas) {
                super.muestraMensajeError("El pago debe ser mayor o igual a la totalidad de la suma de las catorcenas. ", "", null);
            }
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al realizar el pago extemporaneo", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    public void inicializaSaldoAFavor() {

        obtieneAcumulado();

        boolean estaPagado = false;
        double totalDelPagoCatorcenal = 0.0;

        if (amoSelected.size() > 0) {
            totalDelPagoCatorcenal = amoSelected.get(amoSelected.size() - 1).getAmoMontoPago();
        }

        Double diferenciaEntreMontos = acumuladoCliente.getAcuMonto() - totalDelPagoCatorcenal;

        if (diferenciaEntreMontos >= -1.0) {

            if (amoSelected.size() > 0) {
                for (AmortizacionDto amo : amoSelected) {
                    if (!Objects.equals(Constantes.AMO_ESTATUS_PEND_1, amo.getAmoEstatusInt()) && !Objects.equals(Constantes.AMO_ESTATUS_PMEN_3, amo.getAmoEstatusInt())) {
                        estaPagado = true;
                        break;
                    }
                }

                if (!estaPagado) {
                    montoTotalCatSaldoF = 0.0;
                    catSeleccionadasSaldoF = amoSelected.size();

                    for (AmortizacionDto pago : amoSelected) {
                        montoTotalCatSaldoF += pago.getAmoMontoPago();
                        totalDelPagoCatorcenal = pago.getAmoMontoPago();
                    }

                    montoTotalCatSaldoF = Util.round(montoTotalCatSaldoF);

                    rdrAcumulado = Boolean.TRUE;

                    restanteAcumulado = 0.0;
                    restanteAcumulado = Util.round(acumuladoCliente.getAcuMonto() - montoTotalCatSaldoF);

                    catorcenasCubiertas = Util.round(acumuladoCliente.getAcuMonto() / totalDelPagoCatorcenal);

                    if (diferenciaEntreMontos < -1.0) {
                        rdrAcumulado = Boolean.FALSE;
                        msjErrorAcumulado = "El número de catorcenas que elija debe ser menor o igual a " + catorcenasCubiertas + " catorcenas.";
                        super.muestraMensajeError(msjErrorAcumulado, "", null);
                    }

                } else {
                    msjErrorAcumulado = "No puede aplicar un pago a una catorcena que ya está pagada o recorrida, favor de verificar.";
                    rdrAcumulado = Boolean.FALSE;
                    super.muestraMensajeError(msjErrorAcumulado, "", null);
                }
            } else {
                msjErrorAcumulado = "Para aplicar el acumulado debe seleccionar por lo menos 1 catorcena.";
                rdrAcumulado = Boolean.FALSE;
                super.muestraMensajeError(msjErrorAcumulado, "", null);
            }

        } else {

            msjErrorAcumulado = "El cliente tiene " + acumuladoCliente.getAcuMonto() + " de saldo a favor el cual es insuficiente.";
            rdrAcumulado = Boolean.FALSE;
            super.muestraMensajeError(msjErrorAcumulado, "", null);
        }
    }

    
    public void rdrPantallaPagAcumXPagCap(){
        
        
        
    }
    
    
    public void pagarConAcumulado() {

        Double diferenciaEntreMontos = acumuladoCliente.getAcuMonto() - montoTotalCatSaldoF;
        try {
            if (diferenciaEntreMontos >= -1.0) {

                ppacBo.ejecutaPagoAcumulado(amoSelected, usuario, acumuladoCliente, montoTotalCatSaldoF, fechaPagoAcum, credito.getCreId());

                super.muestraMensajeExito("El pago fue efectuado correctamente.", "", null);
                super.hideShowDlg("PF('dlgSalFavor').hide()");
                this.obtieneAmortizacion();
                super.updtComponent("frmAmortizacion:tblAmortizacion");
                super.updtComponent("frmAmortizacion:dlgAmoRep");
                super.updtComponent("frmCredsEmpleado");

            } else if (acumuladoCliente.getAcuMonto() < montoTotalCatorcenas) {
                super.muestraMensajeError("Debe elegir menos catorcenas ya que el monto que intenta cubrir es mayor al acumulado. ", "", null);
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void obtieneAcumulado() {
        try {
            acumuladoCliente = ppacBo.obtieneAcumuladoXUsuario(usuario.getUsuId());
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void inicializaPagoAhorro() {
        boolean estaPagado = false;

        if (amoSelected.size() > 0) {
            for (AmortizacionDto amo : amoSelected) {
                if (!Objects.equals(Constantes.AMO_ESTATUS_PEND_1, amo.getAmoEstatusInt()) && !Objects.equals(Constantes.AMO_ESTATUS_PMEN_3, amo.getAmoEstatusInt())) {
                    estaPagado = true;
                    break;
                }
            }
            //Valida que no se esté aplicando a catorcenas pagadas o equivalentes a pagado
            if (!estaPagado) {

                obtieneTotalesMovimientos();
                Double montoAhorros = 0.0;
                Boolean tieneAhorro = Boolean.FALSE;

                //Valida que tenga ahorros arriba de 5 pesos
                if (!movimientos.isEmpty()) {

                    for (MovimientosDto movs : movimientos) {
                        montoAhorros += movs.getTotalMovimiento() != null ? movs.getTotalMovimiento() : 0.0;
                    }

                    if (montoAhorros > 5) {
                        tieneAhorro = Boolean.TRUE;
                    } else {
                        tieneAhorro = Boolean.FALSE;
                    }

                }

                //Si pasa la validacion de los ahorros mayores a 5 pesos
                if (tieneAhorro) {

                    montoTotalCatSelected = 0.0;

                    amoSelected.forEach(pago -> {
                        montoTotalCatSelected += pago.getAmoMontoPago();
                    });
                   

                    montoTotalCatSelected = Util.round(montoTotalCatSelected);
                    Double diferencia = montoAhorros - montoTotalCatSelected;

                    //Valida si el monto del ahorro alcanza a cubrir el monto de las catorcenas seleccionadas
                    if (Util.round(diferencia) >= -2 ) {
                       
                        cveCredSeleccionado = credito.getCreClave();
                        saldoCredito = montoTotalCatSelected;
                        
                         rdrPagoAhorro = Boolean.TRUE;
                    } else {
                          super.muestraMensajeError("Los ahorros del usuario no alcanzan a cubrir el monto de las catorcenas seleccionadas", "", null);
                         rdrPagoAhorro = Boolean.FALSE;
                    }
                } else {
                    super.muestraMensajeError("El usuario no tiene ahorros", "", null);
                    rdrPagoAhorro = Boolean.FALSE;
                }

            } else {
                super.muestraMensajeError("No puede aplicar un pago a una catorcena que ya está pagada o recorrida, favor de verificar.", "", null);
                rdrPagoAhorro = Boolean.FALSE;
            }
        } else {
            super.muestraMensajeError("Para aplicar un pago con ahorro debe seleccionar por lo menos 1 catorcena.", "", null);
            rdrPagoAhorro = Boolean.FALSE;
        }
    }

    
     /**
     * Hace un ajuste de credito, el cual implica que ya se hicieron las
     * devoluciones como abonos a credito, guardar un pago de tipo finiquito (si
     * es que hay) y realizar un pago a capital Para que el metodo sea
     * alcanzable, debe haber devoluciones o finiquito
     */
    public void ajustaCredito() {
       
        Double diferencia = montoAbonoCredito - montoTotalCatSelected;
        //Si el monto del abono es mayor a cero y se alcanza a pagar el total de las catorcenas
            if (montoAbonoCredito > 0 && (Util.round(diferencia) >= -2 && Util.round(diferencia) <= 2 )) {

                //Guarda las devoluciones como abono credito
                this.guardaDevoluciones(abonosCredito);
                //Actualiza las amortizaciones poniendoles estatus abono credito
                this.updtAmortizacion(amoSelected);

                try {
                    credBo.saldarCredito(credito.getCreId());
                } catch (BusinessException ex) {
                    super.muestraMensajeError("Hubo un error al saldar el credito", "", null);
                    LOGGER.error(ex.getMessage(), ex);
                }

                super.muestraMensajeExito("El pago fue efectuado correctamente.", "", null);
                super.hideShowDlg("PF('dlgAjustaCreditoW').hide()");
                this.obtieneAmortizacion();
                super.updtComponent("frmAmortizacion:tblAmortizacion");
                super.updtComponent("frmAmortizacion:dlgAmoRep");
                super.updtComponent("frmCredsEmpleado");
                

            } else {
                super.muestraMensajeError("El monto del abono debe cubrir la totalidad de las catorcenas seleccionadas", "", null);
            }

       
    }

    /**
     * Obtiene los totales de los movimientos de un usuario
     */
    public void obtieneTotalesMovimientos() {
        try {

            movimientos = movsBo.getAhorrosByUsuId(usuario.getUsuId());

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void onEdit(RowEditEvent event) {

        MovimientosDto mov = (MovimientosDto) event.getObject();

       if (mov.getDevolucion() == null) {
                    mov.setDevolucion(0.0);
                }
                montoAbonoCredito = 0.0;
                for (MovimientosDto movs : movimientos) {
                    montoAbonoCredito += movs.getDevolucion() != null ? movs.getDevolucion() : 0.0;
                }

                Double diferencia = montoAbonoCredito - montoTotalCatSelected;
                if (diferencia > 3) {

                    mov.setDevolucion(0.0);
                    Double montoDevolucion = 0.0;
                    for (MovimientosDto movs : movimientos) {
                        if (movs.equals(mov)) {
                            montoDevolucion = movs.getDevolucion();
                        }
                    }
                    montoAbonoCredito -= montoDevolucion;
                    super.muestraMensajeError("El monto de la devolución no puede ser mayor al adeudo del crédito", "", null);
                } else {

                    /**
                     * Si pasa las validaciones, las guarda en una variable de
                     * entorno para que puedan guardarse en base de datos
                     * posteriormente
                     */
                    abonosCredito = new ArrayList<>();

                    for (MovimientosDto movs : movimientos) {
//                          montoAbonoCredito+=movs.getDevolucion()!= null?movs.getDevolucion():0.0;
                        Movimientos pojo2 = creaDevolucion(movs, 1);

                        if (pojo2.getMovDeposito() < 0) {
                            abonosCredito.add(pojo2);
                        }
                    }

                    saldoCredito = montoTotalCatSelected - montoAbonoCredito;

                    if (montoAbonoCredito > 5) {
                        rdrBtnAjustar = Boolean.TRUE;
                    }

                }

    }

    /**
     * Crea un objeto de tipo movimiento y lo agrega a la lista de devoluciones
     * que se insertarán posteriormente
     *
     * @param dtoSelected
     * @param origen: 1 ahorro, 2 capital
     * @return
     */
    public Movimientos creaDevolucion(MovimientosDto dtoSelected, int origen) {
        Movimientos pojo = new Movimientos();

        pojo.setMovAr(dtoSelected.getMovAr());
        pojo.setMovClaveEmpleado(dtoSelected.getMovClaveEmpleado());
        pojo.setMovDeposito((dtoSelected.getDevolucion() != null ? dtoSelected.getDevolucion() : 0.0) * -1);
        pojo.setMovEmpresa(dtoSelected.getMovEmpresa());
        pojo.setMovEstatus(1);
        pojo.setMovFecha(dtoSelected.getMovFecha());
        pojo.setMovProducto(dtoSelected.getMovProducto());

        pojo.setMovTipo(origen == 1 ? Constantes.MOV_ABONOCREDITO : Constantes.MOV_ABONOCAPITAL);

        pojo.setMovUsuId(dtoSelected.getMovUsuId());

        if (pojo.getMovProducto() == 3) {
            pojo.setMovIdPadre(dtoSelected.getMovId());
        }

        return pojo;
    }

    /**
     * Guarda una lista de devoluciones
     *
     * @param mov
     */
    public void guardaDevoluciones(Object mov) {
        try {
            if (mov instanceof Movimientos) {
                movsBo.guardaMovimiento((Movimientos) mov);
            } else {
                movsBo.guardaDevoluciones((List) mov);
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Actualiza el estatus de una amortizacion
     * @param amortizacion 
     */
     private void updtAmortizacion(List<AmortizacionDto> amortizacion) {
         
         amortizacion.forEach(amo -> {
             try {
                 credBo.updtAmoEstatusIntByAmoId(amo.getAmoId(), Constantes.AMO_ESTATUS_ABONO_CRE_8);
             } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
             }
          
         });
         
    }
    
    
    /**
     * @return the credito
     */
    public DetalleCreditoDto getCredito() {
        return credito;
    }

    /**
     * @param credito the credito to set
     */
    public void setCredito(DetalleCreditoDto credito) {
        this.credito = credito;
    }

    /**
     * @return the amortizacion
     */
    public List<AmortizacionDto> getAmortizacion() {
        return amortizacion;
    }

    /**
     * @param amortizacion the amortizacion to set
     */
    public void setAmortizacion(List<AmortizacionDto> amortizacion) {
        this.amortizacion = amortizacion;
    }

    /**
     * @return the amoSelected
     */
    public List<AmortizacionDto> getAmoSelected() {
        return amoSelected;
    }

    /**
     * @param amoSelected the amoSelected to set
     */
    public void setAmoSelected(List<AmortizacionDto> amoSelected) {
        this.amoSelected = amoSelected;
    }

    /**
     * @return the catorcenas
     */
    public List<String> getCatorcenas() {
        return catorcenas;
    }

    /**
     * @param catorcenas the catorcenas to set
     */
    public void setCatorcenas(List<String> catorcenas) {
        this.catorcenas = catorcenas;
    }

    /**
     * @return the rdrRecorreCat
     */
    public Boolean getRdrRecorreCat() {
        return rdrRecorreCat;
    }

    /**
     * @param rdrRecorreCat the rdrRecorreCat to set
     */
    public void setRdrRecorreCat(Boolean rdrRecorreCat) {
        this.rdrRecorreCat = rdrRecorreCat;
    }

    /**
     * @return the catorcenaSiguiente
     */
    public Date getCatorcenaSiguiente() {
        return catorcenaSiguiente;
    }

    /**
     * @param catorcenaSiguiente the catorcenaSiguiente to set
     */
    public void setCatorcenaSiguiente(Date catorcenaSiguiente) {
        this.catorcenaSiguiente = catorcenaSiguiente;
    }

    /**
     * @return the pagoSeleccionado
     */
    public AmortizacionDto getPagoSeleccionado() {
        return pagoSeleccionado;
    }

    /**
     * @param pagoSeleccionado the pagoSeleccionado to set
     */
    public void setPagoSeleccionado(AmortizacionDto pagoSeleccionado) {
        this.pagoSeleccionado = pagoSeleccionado;
    }

    /**
     * @return the mandarAReporte
     */
    public String getMandarAReporte() {
        return mandarAReporte;
    }

    /**
     * @param mandarAReporte the mandarAReporte to set
     */
    public void setMandarAReporte(String mandarAReporte) {
        this.mandarAReporte = mandarAReporte;
    }

    /**
     * @return the usuario
     */
    public Usuarios getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the msjErrorPagos
     */
    public String getMsjErrorPagos() {
        return msjErrorPagos;
    }

    /**
     * @param msjErrorPagos the msjErrorPagos to set
     */
    public void setMsjErrorPagos(String msjErrorPagos) {
        this.msjErrorPagos = msjErrorPagos;
    }

    /**
     * @return the rdrPagoCapital
     */
    public Boolean getRdrPagoCapital() {
        return rdrPagoCapital;
    }

    /**
     * @param rdrPagoCapital the rdrPagoCapital to set
     */
    public void setRdrPagoCapital(Boolean rdrPagoCapital) {
        this.rdrPagoCapital = rdrPagoCapital;
    }

    /**
     * @return the montoNuevoPago
     */
    public double getMontoNuevoPago() {
        return montoNuevoPago;
    }

    /**
     * @param montoNuevoPago the montoNuevoPago to set
     */
    public void setMontoNuevoPago(double montoNuevoPago) {
        this.montoNuevoPago = montoNuevoPago;
    }

    /**
     * @return the catorcenasRestantes
     */
    public int getCatorcenasRestantes() {
        return catorcenasRestantes;
    }

    /**
     * @param catorcenasRestantes the catorcenasRestantes to set
     */
    public void setCatorcenasRestantes(int catorcenasRestantes) {
        this.catorcenasRestantes = catorcenasRestantes;
    }

    /**
     * @return the saldoDespuesPagoCapital
     */
    public double getSaldoDespuesPagoCapital() {
        return saldoDespuesPagoCapital;
    }

    /**
     * @param saldoDespuesPagoCapital the saldoDespuesPagoCapital to set
     */
    public void setSaldoDespuesPagoCapital(double saldoDespuesPagoCapital) {
        this.saldoDespuesPagoCapital = saldoDespuesPagoCapital;
    }

    /**
     * @return the montoPagoACapital
     */
    public double getMontoPagoACapital() {
        return montoPagoACapital;
    }

    /**
     * @param montoPagoACapital the montoPagoACapital to set
     */
    public void setMontoPagoACapital(double montoPagoACapital) {
        this.montoPagoACapital = montoPagoACapital;
    }

    /**
     * @return the fechaPagoCapital
     */
    public Date getFechaPagoCapital() {
        return fechaPagoCapital;
    }

    /**
     * @param fechaPagoCapital the fechaPagoCapital to set
     */
    public void setFechaPagoCapital(Date fechaPagoCapital) {
        this.fechaPagoCapital = fechaPagoCapital;
    }

    /**
     * @return the rdrPagoExtemp
     */
    public Boolean getRdrPagoExtemp() {
        return rdrPagoExtemp;
    }

    /**
     * @param rdrPagoExtemp the rdrPagoExtemp to set
     */
    public void setRdrPagoExtemp(Boolean rdrPagoExtemp) {
        this.rdrPagoExtemp = rdrPagoExtemp;
    }

    /**
     * @return the catorcenasSeleccionadas
     */
    public Integer getCatorcenasSeleccionadas() {
        return catorcenasSeleccionadas;
    }

    /**
     * @param catorcenasSeleccionadas the catorcenasSeleccionadas to set
     */
    public void setCatorcenasSeleccionadas(Integer catorcenasSeleccionadas) {
        this.catorcenasSeleccionadas = catorcenasSeleccionadas;
    }

    /**
     * @return the restantePago
     */
    public Double getRestantePago() {
        return restantePago;
    }

    /**
     * @param restantePago the restantePago to set
     */
    public void setRestantePago(Double restantePago) {
        this.restantePago = restantePago;
    }

    /**
     * @return the montoPagoExtemp
     */
    public Double getMontoPagoExtemp() {
        return montoPagoExtemp;
    }

    /**
     * @param montoPagoExtemp the montoPagoExtemp to set
     */
    public void setMontoPagoExtemp(Double montoPagoExtemp) {
        this.montoPagoExtemp = montoPagoExtemp;
    }

    /**
     * @return the montoTotalCatorcenas
     */
    public Double getMontoTotalCatorcenas() {
        return montoTotalCatorcenas;
    }

    /**
     * @param montoTotalCatorcenas the montoTotalCatorcenas to set
     */
    public void setMontoTotalCatorcenas(Double montoTotalCatorcenas) {
        this.montoTotalCatorcenas = montoTotalCatorcenas;
    }

    /**
     * @return the fechaPagoExtmp
     */
    public Date getFechaPagoExtmp() {
        return fechaPagoExtmp;
    }

    /**
     * @param fechaPagoExtmp the fechaPagoExtmp to set
     */
    public void setFechaPagoExtmp(Date fechaPagoExtmp) {
        this.fechaPagoExtmp = fechaPagoExtmp;
    }

    /**
     * @return the catSeleccionadasSaldoF
     */
    public Integer getCatSeleccionadasSaldoF() {
        return catSeleccionadasSaldoF;
    }

    /**
     * @param catSeleccionadasSaldoF the catSeleccionadasSaldoF to set
     */
    public void setCatSeleccionadasSaldoF(Integer catSeleccionadasSaldoF) {
        this.catSeleccionadasSaldoF = catSeleccionadasSaldoF;
    }

    /**
     * @return the montoTotalCatSaldoF
     */
    public Double getMontoTotalCatSaldoF() {
        return montoTotalCatSaldoF;
    }

    /**
     * @param montoTotalCatSaldoF the montoTotalCatSaldoF to set
     */
    public void setMontoTotalCatSaldoF(Double montoTotalCatSaldoF) {
        this.montoTotalCatSaldoF = montoTotalCatSaldoF;
    }

    /**
     * @return the acumuladoCliente
     */
    public AcumuladoDto getAcumuladoCliente() {
        return acumuladoCliente;
    }

    /**
     * @param acumuladoCliente the acumuladoCliente to set
     */
    public void setAcumuladoCliente(AcumuladoDto acumuladoCliente) {
        this.acumuladoCliente = acumuladoCliente;
    }

    /**
     * @return the rdrAcumulado
     */
    public Boolean getRdrAcumulado() {
        return rdrAcumulado;
    }

    /**
     * @param rdrAcumulado the rdrAcumulado to set
     */
    public void setRdrAcumulado(Boolean rdrAcumulado) {
        this.rdrAcumulado = rdrAcumulado;
    }

    /**
     * @return the restanteAcumulado
     */
    public Double getRestanteAcumulado() {
        return restanteAcumulado;
    }

    /**
     * @param restanteAcumulado the restanteAcumulado to set
     */
    public void setRestanteAcumulado(Double restanteAcumulado) {
        this.restanteAcumulado = restanteAcumulado;
    }

    /**
     * @return the catorcenasCubiertas
     */
    public Double getCatorcenasCubiertas() {
        return catorcenasCubiertas;
    }

    /**
     * @param catorcenasCubiertas the catorcenasCubiertas to set
     */
    public void setCatorcenasCubiertas(Double catorcenasCubiertas) {
        this.catorcenasCubiertas = catorcenasCubiertas;
    }

    /**
     * @return the fechaPagoAcum
     */
    public Date getFechaPagoAcum() {
        return fechaPagoAcum;
    }

    /**
     * @param fechaPagoAcum the fechaPagoAcum to set
     */
    public void setFechaPagoAcum(Date fechaPagoAcum) {
        this.fechaPagoAcum = fechaPagoAcum;
    }

    /**
     * @return the dsbActions
     */
    public Boolean getDsbActions() {
        return dsbActions;
    }

    /**
     * @param dsbActions the dsbActions to set
     */
    public void setDsbActions(Boolean dsbActions) {
        this.dsbActions = dsbActions;
    }

    /**
     * @return the rdrPagoAhorro
     */
    public Boolean getRdrPagoAhorro() {
        return rdrPagoAhorro;
    }

    /**
     * @param rdrPagoAhorro the rdrPagoAhorro to set
     */
    public void setRdrPagoAhorro(Boolean rdrPagoAhorro) {
        this.rdrPagoAhorro = rdrPagoAhorro;
    }

    /**
     * @return the montoTotalCatSelected
     */
    public Double getMontoTotalCatSelected() {
        return montoTotalCatSelected;
    }

    /**
     * @param montoTotalCatSelected the montoTotalCatSelected to set
     */
    public void setMontoTotalCatSelected(Double montoTotalCatSelected) {
        this.montoTotalCatSelected = montoTotalCatSelected;
    }

    /**
     * @return the movimientos
     */
    public List<MovimientosDto> getMovimientos() {
        return movimientos;
    }

    /**
     * @param movimientos the movimientos to set
     */
    public void setMovimientos(List<MovimientosDto> movimientos) {
        this.movimientos = movimientos;
    }

    /**
     * @return the abonosCredito
     */
    public List<Movimientos> getAbonosCredito() {
        return abonosCredito;
    }

    /**
     * @param abonosCredito the abonosCredito to set
     */
    public void setAbonosCredito(List<Movimientos> abonosCredito) {
        this.abonosCredito = abonosCredito;
    }

    /**
     * @return the esAhorro
     */
    public Boolean getEsAhorro() {
        return esAhorro;
    }

    /**
     * @param esAhorro the esAhorro to set
     */
    public void setEsAhorro(Boolean esAhorro) {
        this.esAhorro = esAhorro;
    }

    /**
     * @return the rdrBtnAjustar
     */
    public Boolean getRdrBtnAjustar() {
        return rdrBtnAjustar;
    }

    /**
     * @param rdrBtnAjustar the rdrBtnAjustar to set
     */
    public void setRdrBtnAjustar(Boolean rdrBtnAjustar) {
        this.rdrBtnAjustar = rdrBtnAjustar;
    }

    /**
     * @return the montoFiniquito
     */
    public Integer getMontoFiniquito() {
        return montoFiniquito;
    }

    /**
     * @param montoFiniquito the montoFiniquito to set
     */
    public void setMontoFiniquito(Integer montoFiniquito) {
        this.montoFiniquito = montoFiniquito;
    }

    /**
     * @return the cveCredSeleccionado
     */
    public String getCveCredSeleccionado() {
        return cveCredSeleccionado;
    }

    /**
     * @param cveCredSeleccionado the cveCredSeleccionado to set
     */
    public void setCveCredSeleccionado(String cveCredSeleccionado) {
        this.cveCredSeleccionado = cveCredSeleccionado;
    }

    

    /**
     * @return the montoAbonoCredito
     */
    public Double getMontoAbonoCredito() {
        return montoAbonoCredito;
    }

    /**
     * @param montoAbonoCredito the montoAbonoCredito to set
     */
    public void setMontoAbonoCredito(Double montoAbonoCredito) {
        this.montoAbonoCredito = montoAbonoCredito;
    }

    /**
     * @return the saldoCredito
     */
    public Double getSaldoCredito() {
        return saldoCredito;
    }

    /**
     * @param saldoCredito the saldoCredito to set
     */
    public void setSaldoCredito(Double saldoCredito) {
        this.saldoCredito = saldoCredito;
    }

    /**
     * @return the rdrScrPagAcumXPagCap
     */
    public Boolean getRdrScrPagAcumXPagCap() {
        return rdrScrPagAcumXPagCap;
    }

    /**
     * @param rdrScrPagAcumXPagCap the rdrScrPagAcumXPagCap to set
     */
    public void setRdrScrPagAcumXPagCap(Boolean rdrScrPagAcumXPagCap) {
        this.rdrScrPagAcumXPagCap = rdrScrPagAcumXPagCap;
    }

}
