/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.finiquito;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.CatorcenasBo;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.administrador.finiquito.FiniquitosBo;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.PerfilBo;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.finiquito.AvalesCreditoDto;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.admon.DetalleAdeudoCreditosBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "finBean")
@ViewScoped
public class FiniquitoBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 136919579732262838L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FiniquitoBean.class);

    @ManagedProperty("#{detAdCreBean}")
    private DetalleAdeudoCreditosBean detAdCreBean;

    private final PerfilBo perfilBo;
    private FiniquitosBo finiquitoBo;
    private CreditosBo creBo;
    private BancosBo bancoBo;
    private Usuarios usuarioBaja;
    private CatorcenasBo catBo;

    /**
     * Campos que se mostrarán en el dialog de ajustar credito
     */
    private Double adeudoTotalCredito;
    private Double montoFiniquito;
    private Date fechaFiniquito;

    //El adeudo del credito una vez que se quitaron los abonos a credito y el finiquito
    private Double adeudoAjustado;

    private Double montoAbonoCredito;

    private MovimientosBo movsBo;
    private List<MovimientosDto> movimientos;
    private MovimientosDto movSelected;
    private MovimientosDto movDevSelected;

    private String tipoDevolucion;
    private Date fechaDevolucion;

    private Double saldoCredito;

    private Integer origen;

    private Boolean rdrMenuAjusta;

    private List<Movimientos> abonosCredito;
    private Boolean rdrBtnAjustar;
    private Boolean rdrFiniquito;
    private Boolean editTable;

    private Boolean esAhorro;

    private List<AvalesCreditoDto> avales;
    private List<String> catorcenasSiguientes;
    private Boolean rdrBtnTransferir;
    private Boolean rdrBtnIncobrable;
    private Boolean rdrTblAvales;

    private String cveCredSeleccionado;

    private Boolean rdrTblMovimientos;

    private Date fechaIncobrable;
    private String usuBajaNombreCompleto;
    private Boolean rdrTblCreditos;
    private Double sumaTotalDevolucion;

    public FiniquitoBean() {
        finiquitoBo = new FiniquitosBo();
        perfilBo = new PerfilBo();
        movsBo = new MovimientosBo();
        creBo = new CreditosBo();
        bancoBo = new BancosBo();
        catBo = new CatorcenasBo();

    }

    public void init() {

        try {
            Integer idUsuBaja = (Integer) getSession().getAttribute("usuBajaId");
            usuarioBaja = perfilBo.getUsuarioById(idUsuBaja);

            usuBajaNombreCompleto = usuarioBaja.getUsuNombre() + " " + usuarioBaja.getUsuPaterno() + " " + usuarioBaja.getUsuMaterno();

            detAdCreBean.setUsuario(usuarioBaja);
            detAdCreBean.obtieneCreditosDetalle();

            List<DetalleCreditoDto> creditos = detAdCreBean.getCreditos();

            rdrTblMovimientos = Boolean.FALSE;

            boolean isCrdPags = false;
            boolean isCrdAct = false;

            for (DetalleCreditoDto cred : creditos) {
                //Si el credito tiene un estatus equivalente a pagado
                if (cred.getCreEstatusId() == 2 || cred.getCreEstatusId() == 3
                        || cred.getCreEstatusId() == 4 || cred.getCreEstatusId() == 5) {
                    isCrdPags = true;
                    //Si no tiene estatus equivalente a pagado entonces hay creditos activos
                } else {
                    isCrdAct = true;
                }
            }

            /**
             * Cuando no hay creditos, se muestra la tabla de ahorros, o todos
             * los creditos estan pagados y ninguno activo se muestra la tabla
             * de ahorros y se oculta la tabla de creditos
             */
            if (creditos.isEmpty() || (isCrdPags && !isCrdAct)) {
                origen = 1;
                rdrTblMovimientos = Boolean.TRUE;
                editTable = Boolean.TRUE;
                rdrTblCreditos = Boolean.FALSE;
                this.obtieneTotalesMovimientos();
                //Si toods los creditos estan pagados y ninguno activo se muestra la tabla de ahorros
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    public void initDlgAjuste() {
        
        //LLamada al microservicio enviando el DetalleCreditoDto credito

        DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
        cveCredSeleccionado = credito.getCreClave();
        rdrBtnAjustar = Boolean.FALSE;
        rdrFiniquito = Boolean.FALSE;
        editTable = Boolean.FALSE;

        //Solo realiza las validaciones si el credito es estatus activo o pagaado o ajustado
        if (credito.getCreEstatusId() == Constantes.CRE_EST_ACTIVO 
        || credito.getCreEstatusId() == Constantes.CRE_EST_PAGADO 
        || credito.getCreEstatusId() == Constantes.CRE_EST_AJUSTADO) {     

            /**
             * Carga los movimientos en la tabla
             */
            abonosCredito = null;
            adeudoTotalCredito = Util.round(credito.getSaldoTotal());
            adeudoAjustado = Util.round(credito.getSaldoTotal());
            montoAbonoCredito = 0.0;
            fechaFiniquito = null;

            Double totalAhorros = 0.0;
            for (MovimientosDto m : movimientos) {
                totalAhorros += Util.round(m.getTotalMovimiento()) != null ? Util.round(m.getTotalMovimiento()) : 0.0;
            }

            /**
             * No tengo ahorros arriba de 10 y el adeudo total del credito es
             * mayor a 1 se renderiza finiquitos
             */
            if ((movimientos.isEmpty() || totalAhorros <= 10) && adeudoTotalCredito > 1) {
                rdrBtnAjustar = Boolean.TRUE;
                rdrFiniquito = Boolean.TRUE;
                editTable = Boolean.FALSE;
                montoFiniquito = 0.0;
                esAhorro = Boolean.FALSE;

                /**
                 * Tengo ahorros arriba de 10 y tengo adeudo en credito
                 */
            } else if (!movimientos.isEmpty() && adeudoTotalCredito > 1) {
                rdrBtnAjustar = Boolean.FALSE;
                rdrFiniquito = Boolean.FALSE;
                editTable = Boolean.TRUE;
                esAhorro = Boolean.TRUE;
            }

            this.origen = 2;

            this.saldoCredito = adeudoTotalCredito;
            /**
             * Obtiene la amortizacion que se enviará al dialog por si se ajusta
             * el crédito
             */
            detAdCreBean.getAmortizacionPagoCap();
            /**
             * Obtiene las catorcenas pendientes anteriores
             */
            detAdCreBean.getAmortizacionesPendientes();
        } else {

            super.muestraMensajeGen("El credito se encuentra " + credito.getCreEstatusNombre(), "Por lo tanto ya no puede ser ajustado", null, FacesMessage.SEVERITY_WARN);
            super.updtComponent("frmDlgAjustar:msj");
        }
    }

    /**
     * Hace un ajuste de credito, el cual implica que ya se hicieron las
     * devoluciones como abonos a credito, guardar un pago de tipo finiquito (si
     * es que hay) y realizar un pago a capital 
     * Para que el metodo sea alcanzable, debe haber devoluciones o finiquito
     */
    public void ajustaCredito() {
        try {
            Integer idPago = null;
            //Cuando es finiquito
            if (!esAhorro) {
                if (montoFiniquito > 0) {
                    DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
                    idPago = finiquitoBo.guardaFiniquito(credito.getCreId(), this.montoFiniquito,
                            this.usuarioBaja.getEmpresas().getEmpId(),
                            this.fechaFiniquito, this.usuarioBaja.getUsuId());

                    finiquitoBo.ajustaCredito(detAdCreBean.getAmoPagoCapital(),
                            detAdCreBean.getAmoPendientesAnteriores(),
                            montoFiniquito, Constantes.AMO_ESTATUS_FINIQ_9);

                    finiquitoBo.updtAmoPagId(idPago, credito.getCreId());

                    /**
                     * Se actualizan los campos saldoCredito y ahorro de
                     * baja_empleados
                     */
                    updtSaldoAhorro();

                    super.updtComponent("frmCreditoDetalle");

                    super.hideShowDlg("PF('dlgAjustaCreditoW').hide()");
                    super.muestraMensajeExito("El crédito fue ajustado", "", null);

                } else {
                    super.muestraMensajeError("El monto del finiquito debe ser mayor a 0", "", null);
                }
                //Cuando es abono a credito
            } else if (montoAbonoCredito > 0) {

                this.guardaDevoluciones(abonosCredito);

                finiquitoBo.ajustaCredito(detAdCreBean.getAmoPagoCapital(),
                        detAdCreBean.getAmoPendientesAnteriores(),
                        montoAbonoCredito,
                        Constantes.AMO_ESTATUS_ABONO_CRE_8);

                /**
                 * Se actualizan los campos saldoCredito y ahorro de
                 * baja_empleados
                 */
                updtSaldoAhorro();

                super.updtComponent("frmCreditoDetalle:tblCreditoAdeudos");

                super.hideShowDlg("PF('dlgAjustaCreditoW').hide()");
                super.muestraMensajeExito("El crédito fue ajustado", "", null);

            } else {
                super.muestraMensajeError("El monto del abono debe ser mayor a 0", "", null);
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los totales de los movimientos de un usuario
     */
    public void obtieneTotalesMovimientos() {
        try {

            movimientos = movsBo.getAhorrosByUsuId(usuarioBaja.getUsuId());
            sumaTotalDevolucion = 0.0;
            if(movimientos != null){
                if(!movimientos.isEmpty()){
                    movimientos.stream().forEach((dto) -> {
                        sumaTotalDevolucion += dto.getTotalMovimiento();
                    });
                }
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void onEdit(RowEditEvent event) {

        MovimientosDto mov = (MovimientosDto) event.getObject();

        switch (origen) {

            /**
             * Cuando estamos en la pantalla de reporte por empleado o
             * finiquitos sin creditos, por lo tanto, dentro de esta validación
             * se afecta bancos
             */
            case 1:
                Movimientos pojo = creaDevolucion(mov);
                BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
                
                guardaDevoluciones(pojo);
                Bancos banco = generaBancoDev(pojo, idRelacion.longValue());
                try {
                    //Guarda en las tablas BANCOS, ESTADO_CUENTA, BANCO_EDOCTA
                    bancoBo.guardaBancoCEdoCta(banco, idRelacion.longValue());

                    updtSaldoAhorro();
                } catch (BusinessException ex) {
                    LOGGER.error(ex.getMessage(), ex.getCause());
                }

                super.muestraMensajeExito("La devolución fue realizada", "", null);
                RequestContext.getCurrentInstance().update("frmAhoO1:tblAhorros");

                break;

            /**
             * Cuando se están usando los ahorros para abonar al credito, en este 
             * caso NO se afecta bancos
             */
            case 2:
                /**
                 * Valida que el monto de la devolucion no sea mayor al adeudo
                 * del credito, la validacion en que la devolucion no puede ser
                 * mas alta que el monto del ahorro se realizará en pantalla
                 */
                if (mov.getDevolucion() == null) {
                    mov.setDevolucion(0.0);
                }
                montoAbonoCredito = 0.0;
                for (MovimientosDto movs : movimientos) {
                    montoAbonoCredito += movs.getDevolucion() != null ? movs.getDevolucion() : 0.0;
                }

                Double diferencia = montoAbonoCredito - saldoCredito;
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

                        if(movs.getDevolucionFecha() == null){
                            movs.setDevolucionFecha(movs.getMovFecha());
                        }

                        Movimientos pojo2 = creaDevolucion(movs);

                        if (pojo2.getMovDeposito() < 0) {
                            abonosCredito.add(pojo2);
                        }
                    }

                    adeudoAjustado = adeudoTotalCredito - montoAbonoCredito;

                    if (montoAbonoCredito > 5) {
                        rdrBtnAjustar = Boolean.TRUE;
                    }

                }

                break;
        }

    }

    /**
     * Método que realiza todas las devoluciones del usuario que se están
     * mostrando en la tabla de ahorros.
     * Este metodo se creo para que no se tenga que devolver 1 x1 en la tabla
     */
    public void devolverTotalAhorro(){
        
        for(MovimientosDto dto : movimientos){
        dto.setDevolucion(dto.getTotalMovimiento());
        dto.setTotalMovimiento(0.0);
        dto.setMovFecha(new Date());
        Movimientos pojo = creaDevolucion(dto);
                BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
                
                guardaDevoluciones(pojo);
                Bancos banco = generaBancoDev(pojo, idRelacion.longValue());
                try {
                    //Guarda en las tablas BANCOS, ESTADO_CUENTA, BANCO_EDOCTA
                    bancoBo.guardaBancoCEdoCta(banco, idRelacion.longValue());

                    
                } catch (BusinessException ex) {
                    LOGGER.error(ex.getMessage(), ex.getCause());
                }
        }
        
        try {
            updtSaldoAhorro();
            sumaTotalDevolucion = 0.0;
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex.getCause());
        }
    }
    
    
    /**
     * Crea un objeto de tipo movimiento y lo agrega a la lista de devoluciones
     * que se insertarán posteriormente
     *
     * @param dtoSelected
     * @return
     */
    public Movimientos creaDevolucion(MovimientosDto dtoSelected) {
        Movimientos pojo = new Movimientos();

        pojo.setMovAr(dtoSelected.getMovAr());
        pojo.setMovClaveEmpleado(dtoSelected.getMovClaveEmpleado());
        pojo.setMovDeposito((dtoSelected.getDevolucion() != null ? dtoSelected.getDevolucion() : 0.0) * -1);
        pojo.setMovEmpresa(dtoSelected.getMovEmpresa());
        pojo.setMovEstatus(1);
        pojo.setMovFecha(dtoSelected.getDevolucionFecha());
        pojo.setMovProducto(dtoSelected.getMovProducto());
        switch (origen) {
            case 1:
                pojo.setMovTipo(Constantes.MOV_TIPO_DEVOLUCION);
                break;
            case 2:
                pojo.setMovTipo(Constantes.MOV_ABONOCREDITO);
                break;

        }
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

    public void initDlgTransferir() {
        try {
            //Obtiene los avales
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
            cveCredSeleccionado = credito.getCreClave();
            adeudoTotalCredito = credito.getSaldoTotal();

            if (credito.getCreEstatusId() == Constantes.CRE_EST_ACTIVO || credito.getCreEstatusId() == Constantes.CRE_EST_PAGADO || credito.getCreEstatusId() == Constantes.CRE_EST_AJUSTADO) {

                rdrTblAvales = Boolean.TRUE;
                avales = creBo.getAvalesCredito(credito.getCreId());
                catorcenasSiguientes = catBo.getCatorcenasSiguientes(new Date());

                if (avales.isEmpty()) {
                    rdrBtnTransferir = Boolean.FALSE;
                    rdrBtnIncobrable = Boolean.TRUE;
                } else {
                    rdrBtnTransferir = Boolean.TRUE;
                    rdrBtnIncobrable = Boolean.FALSE;

                    Double xAval = adeudoTotalCredito / avales.size();

                    avales.forEach(aval -> {
                        aval.setMontoCredito(Util.round(xAval));
                    });

                }

            } else {
                rdrBtnTransferir = Boolean.FALSE;
                rdrTblAvales = Boolean.FALSE;
                rdrBtnIncobrable = Boolean.FALSE;
                super.muestraMensajeGen("El credito se encuentra " + credito.getCreEstatusNombre(), "Por lo tanto no puede ser transferido", null, FacesMessage.SEVERITY_WARN);
                super.updtComponent("frmDlgTransferir:msjDlgAjusteCredito");
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Genera el registro de bancos para cuando se va a devolver un ahorro
     *
     * @param mov
     * @param idRelacion
     * @return
     */
    public Bancos generaBancoDev(Movimientos mov, Long idRelacion) {
        Bancos banco = new Bancos();
        banco.setBanAjustado(Constantes.BAN_AJUSTADO);
        banco.setBanIdConceptoSistema(mov.getMovId());
        banco.setBanFechatransaccion(mov.getMovFecha());
        banco.setBanEmpresa(mov.getMovEmpresa());
        banco.setBanMonto(mov.getMovDeposito());
        banco.setBanIdRelacion(idRelacion);
        banco.setBanFechaRelacion(new Date());

        switch (mov.getMovProducto()) {
            case 1:
                if (mov.getMovAr() == 1) {
                    banco.setBanConcepto(Constantes.BAN_DEV_A_FIJO);
                } else {
                    banco.setBanConcepto(Constantes.BAN_DEV_RDTO_FIJO);

                }
                break;
            case 2:
                if (mov.getMovAr() == 1) {
                    banco.setBanConcepto(Constantes.BAN_DEV_A_N_FIJO);
                } else {
                    banco.setBanConcepto(Constantes.BAN_DEV_RDTO_N_FIJO);

                }
                break;
            case 3:
                if (mov.getMovAr() == 1) {
                    banco.setBanConcepto(Constantes.BAN_DEV_A_VOL);
                } else {
                    banco.setBanConcepto(Constantes.BAN_DEV_RDTO_VOL);

                }
                break;
        }

        return banco;
    }

    public void onEditTransfiere(RowEditEvent event) {

        AvalesCreditoDto aval = (AvalesCreditoDto) event.getObject();

        LOGGER.debug("" + aval.getMontoCredito());
        LOGGER.debug("" + aval.getPrimerCatorcena());
        LOGGER.debug("" + aval.getCatorcenas());

    }

    public void transfiereCreditos() {

        DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
        Double montoAvales = 0.0;
        for (AvalesCreditoDto aval : avales) {
            montoAvales += aval.getMontoCredito();
        }

        Double diferencia = Util.round(adeudoTotalCredito - montoAvales);

        /**
         * Si el monto introducido salda la deuda del credito en su totalidad
         */
        if (diferencia <= 2 && diferencia >= -2) {

            try {
                for (AvalesCreditoDto aval : avales) {
                    montoAvales += aval.getMontoCredito();

                    CreditosFinal credTransfer = new CreditosFinal();

                    credTransfer.setCreUsuId(aval.getAvalUsuId());
                    credTransfer.setCreClaveEmpleado(aval.getAvalCveEmpleado());
                    credTransfer.setCreEmpresa(aval.getEmpAbrev());
                    credTransfer.setCrePrestamo(aval.getMontoCredito());
                    credTransfer.setCreEstatus(1); //
                    credTransfer.setCreCatorcenas(aval.getCatorcenas());
                    credTransfer.setCreTipo("NOMINA");
                    credTransfer.setCreProducto(6);
                    credTransfer.setCrePadre(credito.getCreId());

                    creBo.creaCreditoTransferido(credTransfer, aval.getPrimerCatorcena());

                }

                creBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_TRANSFERIDO, null);

                /**
                 * Actualiza el saldo de los creditos y el ahorro en la tabla
                 * baja_empleados
                 */
                updtSaldoAhorro();

                super.hideShowDlg("PF('dlgTransferirW').hide()");

                super.updtComponent("frmCreditoDetalle:tblCreditoAdeudos");
                super.updtComponent("frmCreditoDetalle:growlDevsAho");
                super.muestraMensajeExito("El crédito fue transferido", "", null);
            } catch (BusinessException ex) {
                super.updtComponent("frmCreditoDetalle:growlDevsAho");
                super.muestraMensajeError("Hubo un error al transferir el credito", "", null);
                LOGGER.error(ex.getMessage(), ex);
            }

        } else {
            super.muestraMensajeGen("Debe transferir el monto del saldo total del crédito", "", "", FacesMessage.SEVERITY_WARN);

        }

    }

    /**
     * Actualiza en la base de datos el saldo total y el ahorro en la tabla
     * baja_empleados
     *
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public void updtSaldoAhorro() throws BusinessException {
        detAdCreBean.obtieneCreditosDetalle();
        this.obtieneTotalesMovimientos();

        List<DetalleCreditoDto> creditos = detAdCreBean.getCreditos();
        Double saldoTotalCreditos = 0.0;
        Integer estatus = 0;

        for (DetalleCreditoDto cre : creditos) {
            saldoTotalCreditos += cre.getSaldoTotal();
        }

        Double ahorroTotal = 0.0;

        for (MovimientosDto mov : movimientos) {
            ahorroTotal += mov.getTotalMovimiento();
        }

        //Cuando el saldo del credito es mayor a 10
        if (saldoTotalCreditos >= 5) {
            estatus = Constantes.BAJA_PENDIENTE;
        }

        //Si ya no tiene saldo en credito pero si tiene ahorro
        if (saldoTotalCreditos <= 5 && ahorroTotal >= 5) {
            estatus = Constantes.BAJA_AHORROSXDEVOLVER;
        }

        //Cuando no tiene saldo en credito ni en ahorro
        if (saldoTotalCreditos <= 5 && ahorroTotal <= 5) {
            estatus = Constantes.BAJA_COMPLETADA;
        }

        finiquitoBo.updtBaeSaldoAhorro(usuarioBaja.getUsuId(), saldoTotalCreditos, ahorroTotal, estatus);
    }

    public void mandarAIncobrable() {
        try {
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();

            creBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_INCOBRABLE, fechaIncobrable);
            //Actualiza la amortizacion del credito padre
            creBo.updtEstatusAmoInt(credito.getCreId(), Constantes.AMO_ESTATUS_INCOB_12);
            /**
             * Actualiza el saldo de los creditos y el ahorro en la tabla
             * baja_empleados
             */
            updtSaldoAhorro();

            super.hideShowDlg("PF('dlgTransferirW').hide()");

            super.updtComponent("frmCreditoDetalle:tblCreditoAdeudos");
             super.updtComponent("frmCreditoDetalle:growlDevsAho");
            super.muestraMensajeExito("El estatus del crédito se cambió a Incobrable", "", null);
            
        } catch (BusinessException ex) {
             super.updtComponent("frmCreditoDetalle:growlDevsAho");
             super.muestraMensajeError("Hubo un error al mandar a incobrable el crédito", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    
     public void inicializaDialogDev(MovimientosDto mov){
        
         this.movDevSelected = mov;
         
        if(this.movDevSelected != null){
            this.movDevSelected.setDevolucion(this.movDevSelected.getTotalMovimiento());
            this.movDevSelected.setDevolucionFecha(Util.restaHrToDate(new Date(), 6));
        }
        
        super.updtComponent("frmDlgDev");
        
    }
    
     
      public void aplicaDevolucion() {

        switch (origen) {

            /**
             * Cuando estamos en la pantalla de reporte por empleado o
             * finiquitos sin creditos, por lo tanto, dentro de esta validación
             * se afecta bancos
             */
            case 1:
                Movimientos pojo = creaDevolucion(movDevSelected);
                BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
                
                guardaDevoluciones(pojo);
                Bancos banco = generaBancoDev(pojo, idRelacion.longValue());
                try {
                    //Guarda en las tablas BANCOS, ESTADO_CUENTA, BANCO_EDOCTA
                    bancoBo.guardaBancoCEdoCta(banco, idRelacion.longValue());

                    updtSaldoAhorro();
                } catch (BusinessException ex) {
                    LOGGER.error(ex.getMessage(), ex.getCause());
                }

                super.muestraMensajeExito("La devolución fue realizada", "", null);
                RequestContext.getCurrentInstance().update("frmAhoO1:tblAhorros");

                break;

            /**
             * Cuando se están usando los ahorros para abonar al credito, en este 
             * caso NO se afecta bancos
             */
            case 2:
                /**
                 * Valida que el monto de la devolucion no sea mayor al adeudo
                 * del credito, la validacion en que la devolucion no puede ser
                 * mas alta que el monto del ahorro se realizará en pantalla
                 */
                if (movDevSelected.getDevolucion() == null) {
                    movDevSelected.setDevolucion(0.0);
                }
                montoAbonoCredito = 0.0;
                for (MovimientosDto movs : movimientos) {
                    montoAbonoCredito += movs.getDevolucion() != null ? movs.getDevolucion() : 0.0;
                }

                Double diferencia = montoAbonoCredito - saldoCredito;
                if (diferencia > 3) {

                    movDevSelected.setDevolucion(0.0);
                    Double montoDevolucion = 0.0;
                    for (MovimientosDto movs : movimientos) {
                        if (movs.equals(movDevSelected)) {
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
                        
                        Movimientos pojo2 = creaDevolucion(movs);

                        if (pojo2.getMovDeposito() < 0) {
                            abonosCredito.add(pojo2);
                        }
                    }

                    adeudoAjustado = adeudoTotalCredito - montoAbonoCredito;

                    if (montoAbonoCredito > 5) {
                        rdrBtnAjustar = Boolean.TRUE;
                    }

                }

                break;
        }

    }
     
    /**
     * @return the detAdCreBean
     */
    public DetalleAdeudoCreditosBean getDetAdCreBean() {
        return detAdCreBean;
    }

    /**
     * @param detAdCreBean the detAdCreBean to set
     */
    public void setDetAdCreBean(DetalleAdeudoCreditosBean detAdCreBean) {
        this.detAdCreBean = detAdCreBean;
    }

    /**
     * @return the adeudoTotalCredito
     */
    public Double getAdeudoTotalCredito() {
        return adeudoTotalCredito;
    }

    /**
     * @param adeudoTotalCredito the adeudoTotalCredito to set
     */
    public void setAdeudoTotalCredito(Double adeudoTotalCredito) {
        this.adeudoTotalCredito = adeudoTotalCredito;
    }

    /**
     * @return the montoFiniquito
     */
    public Double getMontoFiniquito() {
        return montoFiniquito;
    }

    /**
     * @param montoFiniquito the montoFiniquito to set
     */
    public void setMontoFiniquito(Double montoFiniquito) {
        this.montoFiniquito = montoFiniquito;
    }

    /**
     * @return the adeudoAjustado
     */
    public Double getAdeudoAjustado() {
        return adeudoAjustado;
    }

    /**
     * @param adeudoAjustado the adeudoAjustado to set
     */
    public void setAdeudoAjustado(Double adeudoAjustado) {
        this.adeudoAjustado = adeudoAjustado;
    }

    /**
     * @return the fechaFiniquito
     */
    public Date getFechaFiniquito() {
        return fechaFiniquito;
    }

    /**
     * @param fechaFiniquito the fechaFiniquito to set
     */
    public void setFechaFiniquito(Date fechaFiniquito) {
        this.fechaFiniquito = fechaFiniquito;
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
     * @return the movSelected
     */
    public MovimientosDto getMovSelected() {
        return movSelected;
    }

    /**
     * @param movSelected the movSelected to set
     */
    public void setMovSelected(MovimientosDto movSelected) {
        this.movSelected = movSelected;
    }

    /**
     * @return the tipoDevolucion
     */
    public String getTipoDevolucion() {
        return tipoDevolucion;
    }

    /**
     * @param tipoDevolucion the tipoDevolucion to set
     */
    public void setTipoDevolucion(String tipoDevolucion) {
        this.tipoDevolucion = tipoDevolucion;
    }

    /**
     * @return the fechaDevolucion
     */
    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    /**
     * @param fechaDevolucion the fechaDevolucion to set
     */
    public void setFechaDevolucion(Date fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
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
     * @return the rdrFiniquito
     */
    public Boolean getRdrFiniquito() {
        return rdrFiniquito;
    }

    /**
     * @param rdrFiniquito the rdrFiniquito to set
     */
    public void setRdrFiniquito(Boolean rdrFiniquito) {
        this.rdrFiniquito = rdrFiniquito;
    }

    /**
     * @return the editTable
     */
    public Boolean getEditTable() {
        return editTable;
    }

    /**
     * @param editTable the editTable to set
     */
    public void setEditTable(Boolean editTable) {
        this.editTable = editTable;
    }

    /**
     * @return the rdrMenuAjusta
     */
    public Boolean getRdrMenuAjusta() {
        return rdrMenuAjusta;
    }

    /**
     * @param rdrMenuAjusta the rdrMenuAjusta to set
     */
    public void setRdrMenuAjusta(Boolean rdrMenuAjusta) {
        this.rdrMenuAjusta = rdrMenuAjusta;
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
     * @return the avales
     */
    public List<AvalesCreditoDto> getAvales() {
        return avales;
    }

    /**
     * @param avales the avales to set
     */
    public void setAvales(List<AvalesCreditoDto> avales) {
        this.avales = avales;
    }

    /**
     * @return the catorcenasSiguientes
     */
    public List<String> getCatorcenasSiguientes() {
        return catorcenasSiguientes;
    }

    /**
     * @param catorcenasSiguientes the catorcenasSiguientes to set
     */
    public void setCatorcenasSiguientes(List<String> catorcenasSiguientes) {
        this.catorcenasSiguientes = catorcenasSiguientes;
    }

    /**
     * @return the rdrBtnTransferir
     */
    public Boolean getRdrBtnTransferir() {
        return rdrBtnTransferir;
    }

    /**
     * @param rdrBtnTransferir the rdrBtnTransferir to set
     */
    public void setRdrBtnTransferir(Boolean rdrBtnTransferir) {
        this.rdrBtnTransferir = rdrBtnTransferir;
    }

    /**
     * @return the rdrTblAvales
     */
    public Boolean getRdrTblAvales() {
        return rdrTblAvales;
    }

    /**
     * @param rdrTblAvales the rdrTblAvales to set
     */
    public void setRdrTblAvales(Boolean rdrTblAvales) {
        this.rdrTblAvales = rdrTblAvales;
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
     * @return the rdrTblMovimientos
     */
    public Boolean getRdrTblMovimientos() {
        return rdrTblMovimientos;
    }

    /**
     * @param rdrTblMovimientos the rdrTblMovimientos to set
     */
    public void setRdrTblMovimientos(Boolean rdrTblMovimientos) {
        this.rdrTblMovimientos = rdrTblMovimientos;
    }

    /**
     * @return the fechaIncobrable
     */
    public Date getFechaIncobrable() {
        return fechaIncobrable;
    }

    /**
     * @param fechaIncobrable the fechaIncobrable to set
     */
    public void setFechaIncobrable(Date fechaIncobrable) {
        this.fechaIncobrable = fechaIncobrable;
    }

    /**
     * @return the usuarioBaja
     */
    public Usuarios getUsuarioBaja() {
        return usuarioBaja;
    }

    /**
     * @param usuarioBaja the usuarioBaja to set
     */
    public void setUsuarioBaja(Usuarios usuarioBaja) {
        this.usuarioBaja = usuarioBaja;
    }

    /**
     * @return the usuBajaNombreCompleto
     */
    public String getUsuBajaNombreCompleto() {
        return usuBajaNombreCompleto;
    }

    /**
     * @param usuBajaNombreCompleto the usuBajaNombreCompleto to set
     */
    public void setUsuBajaNombreCompleto(String usuBajaNombreCompleto) {
        this.usuBajaNombreCompleto = usuBajaNombreCompleto;
    }

    /**
     * @return the rdrBtnIncobrable
     */
    public Boolean getRdrBtnIncobrable() {
        return rdrBtnIncobrable;
    }

    /**
     * @param rdrBtnIncobrable the rdrBtnIncobrable to set
     */
    public void setRdrBtnIncobrable(Boolean rdrBtnIncobrable) {
        this.rdrBtnIncobrable = rdrBtnIncobrable;
    }

    /**
     * @return the rdrTblCreditos
     */
    public Boolean getRdrTblCreditos() {
        return rdrTblCreditos;
    }

    /**
     * @param rdrTblCreditos the rdrTblCreditos to set
     */
    public void setRdrTblCreditos(Boolean rdrTblCreditos) {
        this.rdrTblCreditos = rdrTblCreditos;
    }

    /**
     * @return the sumaTotalDevolucion
     */
    public Double getSumaTotalDevolucion() {
        return sumaTotalDevolucion;
    }

    /**
     * @param sumaTotalDevolucion the sumaTotalDevolucion to set
     */
    public void setSumaTotalDevolucion(Double sumaTotalDevolucion) {
        this.sumaTotalDevolucion = sumaTotalDevolucion;
    }

    /**
     * @return the movDevSelected
     */
    public MovimientosDto getMovDevSelected() {
        return movDevSelected;
    }

    /**
     * @param movDevSelected the movDevSelected to set
     */
    public void setMovDevSelected(MovimientosDto movDevSelected) {
        this.movDevSelected = movDevSelected;
    }

}
