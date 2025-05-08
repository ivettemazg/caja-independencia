package mx.com.evoti.presentacion.finiquito;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.finiquito.AvalesCreditoDto;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.admon.DetalleAdeudoCreditosBean;
import mx.com.evoti.service.finiquito.FiniquitoService;
import mx.com.evoti.service.finiquito.ValidadorFiniquito;
import mx.com.evoti.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "finBean")
@ViewScoped
public class FiniquitoRefBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FiniquitoRefBean.class);

    @ManagedProperty("#{detAdCreBean}")
    private DetalleAdeudoCreditosBean detAdCreBean;

    private FiniquitoService finiquitoService;

    private Usuarios usuarioBaja;
    private String usuBajaNombreCompleto;

    private List<DetalleCreditoDto> creditos;
    private List<MovimientosDto> movimientos;
    private List<AvalesCreditoDto> avales;
    private List<String> catorcenasSiguientes;

    private MovimientosDto movDevSelected;
    private String cveCredSeleccionado;

    private Double adeudoTotalCredito;
    private Double adeudoAjustado;
    private Double montoFiniquito;
    private Double saldoCredito;
    private Date fechaFiniquito;
    private Date fechaIncobrable;

    private Boolean rdrTblMovimientos;
    private Boolean rdrFiniquito;
    private Boolean rdrBtnAjustar;
    private Boolean rdrTblAvales;
    private Boolean rdrBtnTransferir;
    private Boolean rdrBtnIncobrable;
    private Boolean editTable;

    private Integer origen;

    @PostConstruct
    public void init() {
        finiquitoService = new FiniquitoService();
        try {
            Integer idUsuBaja = (Integer) getSession().getAttribute("usuBajaId");
            this.usuarioBaja = finiquitoService.cargarUsuario(idUsuBaja);
            this.usuBajaNombreCompleto = usuarioBaja.getUsuNombre() + " " + usuarioBaja.getUsuPaterno() + " " + usuarioBaja.getUsuMaterno();

            detAdCreBean.setUsuario(usuarioBaja);
            detAdCreBean.obtieneCreditosDetalle();
            this.creditos = detAdCreBean.getCreditos();
            this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());

            boolean todosPagados = creditos.stream().allMatch(c -> ValidadorFiniquito.estaPagado(c.getCreEstatusId()));
            LOGGER.debug("***************** Todos pagados: {}", todosPagados);
            boolean hayActivos = creditos.stream().anyMatch(c -> ValidadorFiniquito.estaActivo(c.getCreEstatusId()));
            LOGGER.debug("***************** Hay activos: {}", hayActivos);

            //Cuando todos estan pagados y no hay activos, solo muestra tabla de ahorros (origen 1)
            //Cuando hay activos, muestra tabla de ahorros y creditos (origen 2)
            this.rdrTblMovimientos = creditos.isEmpty() || (todosPagados && !hayActivos);
            LOGGER.debug("***************** rdrTblMovimientos: ", rdrTblMovimientos);
            this.origen = rdrTblMovimientos ? 1 : 2;
        } catch (BusinessException ex) {
            LOGGER.error("Error inicializando bean de finiquito", ex);
            muestraMensajeError("Error al cargar información de baja", ex.getMessage(), null);
        }
    }

    public void initDlgAjuste() {
        try {
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
            this.cveCredSeleccionado = credito.getCreClave();

            if (!ValidadorFiniquito.esCreditoAjustable(credito.getCreEstatusId())) {
                super.muestraMensajeGen("El crédito se encuentra " + credito.getCreEstatusNombre(), "Por lo tanto ya no puede ser ajustado", null, FacesMessage.SEVERITY_WARN);
                super.updtComponent("frmDlgAjustar:msj");
                return;
            }

            this.adeudoTotalCredito = Util.round(credito.getSaldoTotal());
            this.adeudoAjustado = Util.round(credito.getSaldoTotal());
            this.saldoCredito = Util.round(credito.getSaldoTotal());
            this.montoFiniquito = 0.0;

            boolean tieneAhorros = ValidadorFiniquito.tieneAhorrosSuficientes(movimientos);
            this.rdrFiniquito = !tieneAhorros && adeudoTotalCredito > 1;
            this.rdrBtnAjustar = this.rdrFiniquito;
            this.editTable = tieneAhorros && adeudoTotalCredito > 1;

            detAdCreBean.getAmortizacionPagoCap();
            detAdCreBean.getAmortizacionesPendientes();
        } catch (Exception ex) {
            LOGGER.error("Error al inicializar dialogo de ajuste", ex);
            muestraMensajeError("No fue posible cargar el diálogo de ajuste", ex.getMessage(), null);
        }
    }

    public void aplicaDevolucion() {
        try {
            if (origen == 1) {
                finiquitoService.devolverAhorroConBancos(movDevSelected, usuarioBaja);
            } else {
                finiquitoService.devolverAhorroAbonoCredito(movDevSelected, usuarioBaja, movimientos, saldoCredito);
            }
            this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());
            updtComponent("frmCreditoDetalle:tblAhorros");
            muestraMensajeExito("La devolución fue realizada", "", null);
        } catch (BusinessException ex) {
            LOGGER.error("Error aplicando devolución", ex);
            muestraMensajeError("Error aplicando devolución", ex.getMessage(), null);
        }
    }

    public void ajustaCredito() {
        try {
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();

            if (Boolean.TRUE.equals(rdrFiniquito)) {
                finiquitoService.aplicarFiniquito(usuarioBaja, credito, montoFiniquito, fechaFiniquito, detAdCreBean);
                muestraMensajeExito("El crédito fue ajustado por finiquito", "", null);
            } else {
                finiquitoService.aplicarAbono(usuarioBaja, movimientos, saldoCredito, detAdCreBean);
                muestraMensajeExito("El crédito fue ajustado por abono", "", null);
            }

            this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());
            updtComponent("frmCreditoDetalle:tblCreditoAdeudos");
        } catch (BusinessException ex) {
            LOGGER.error("Error al ajustar crédito", ex);
            muestraMensajeError("Error al ajustar crédito", ex.getMessage(), null);
        }
    }

    public void initDlgTransferir() {
        try {
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
            this.cveCredSeleccionado = credito.getCreClave();
            this.adeudoTotalCredito = credito.getSaldoTotal();

            if (!ValidadorFiniquito.esCreditoAjustable(credito.getCreEstatusId())) {
                muestraMensajeGen("El crédito se encuentra " + credito.getCreEstatusNombre(), "Por lo tanto no puede ser transferido", null, FacesMessage.SEVERITY_WARN);
                updtComponent("frmDlgTransferir:msjDlgAjusteCredito");
                return;
            }

            this.avales = finiquitoService.obtenerAvales(credito.getCreId());
            this.catorcenasSiguientes = finiquitoService.obtenerCatorcenas();
            this.rdrTblAvales = !avales.isEmpty();
            this.rdrBtnTransferir = !avales.isEmpty();
            this.rdrBtnIncobrable = avales.isEmpty();

            finiquitoService.asignarMontosAvalesProporcion(avales, adeudoTotalCredito);
        } catch (BusinessException ex) {
            LOGGER.error("Error al cargar diálogo de transferencia", ex);
            muestraMensajeError("Error al transferir crédito", ex.getMessage(), null);
        }
    }

    public void transfiereCreditos() {
        try {
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
            finiquitoService.transferir(credito, avales);
            this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());
            muestraMensajeExito("El crédito fue transferido", "", null);
        } catch (BusinessException ex) {
            LOGGER.error("Error al transferir crédito", ex);
            muestraMensajeError("Error al transferir crédito", ex.getMessage(), null);
        }
    }

    public void mandarAIncobrable() {
        try {
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
            finiquitoService.marcarIncobrable(credito, fechaIncobrable);
            this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());
            muestraMensajeExito("El estatus del crédito se cambió a Incobrable", "", null);
        } catch (BusinessException ex) {
            LOGGER.error("Error al mandar a incobrable", ex);
            muestraMensajeError("Error al mandar a incobrable", ex.getMessage(), null);
        }
    }

    public void setDetAdCreBean(DetalleAdeudoCreditosBean detAdCreBean) {
        this.detAdCreBean = detAdCreBean;
    }

    public DetalleAdeudoCreditosBean getDetAdCreBean() {
        return detAdCreBean;
    }

    // Getters para propiedades usadas en la vista (omitidos aquí por brevedad)
    // ...
} 
