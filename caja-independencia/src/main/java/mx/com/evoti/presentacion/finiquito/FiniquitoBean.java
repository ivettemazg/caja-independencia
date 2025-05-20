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
public class FiniquitoBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FiniquitoBean.class);

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

    public void init() {

        finiquitoService = new FiniquitoService();
            try {
                Integer idUsuBaja = (Integer) getSession().getAttribute("usuBajaId");

                if (idUsuBaja == null) {
                LOGGER.error("El atributo usuBajaId no está presente en sesión");
                muestraMensajeError("Error", "No se pudo cargar la información del usuario en sesión", null);
                return;
            }

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


    public Usuarios getUsuarioBaja() { return usuarioBaja; }
    public void setUsuarioBaja(Usuarios usuarioBaja) { this.usuarioBaja = usuarioBaja; }

    public String getUsuBajaNombreCompleto() { return usuBajaNombreCompleto; }
    public void setUsuBajaNombreCompleto(String usuBajaNombreCompleto) { this.usuBajaNombreCompleto = usuBajaNombreCompleto; }

    public List<DetalleCreditoDto> getCreditos() { return creditos; }
    public void setCreditos(List<DetalleCreditoDto> creditos) { this.creditos = creditos; }

    public List<MovimientosDto> getMovimientos() { return movimientos; }
    public void setMovimientos(List<MovimientosDto> movimientos) { this.movimientos = movimientos; }

    public List<AvalesCreditoDto> getAvales() { return avales; }
    public void setAvales(List<AvalesCreditoDto> avales) { this.avales = avales; }

    public List<String> getCatorcenasSiguientes() { return catorcenasSiguientes; }
    public void setCatorcenasSiguientes(List<String> catorcenasSiguientes) { this.catorcenasSiguientes = catorcenasSiguientes; }

    public MovimientosDto getMovDevSelected() { return movDevSelected; }
    public void setMovDevSelected(MovimientosDto movDevSelected) { this.movDevSelected = movDevSelected; }

    public String getCveCredSeleccionado() { return cveCredSeleccionado; }
    public void setCveCredSeleccionado(String cveCredSeleccionado) { this.cveCredSeleccionado = cveCredSeleccionado; }

    public Double getAdeudoTotalCredito() { return adeudoTotalCredito; }
    public void setAdeudoTotalCredito(Double adeudoTotalCredito) { this.adeudoTotalCredito = adeudoTotalCredito; }

    public Double getAdeudoAjustado() { return adeudoAjustado; }
    public void setAdeudoAjustado(Double adeudoAjustado) { this.adeudoAjustado = adeudoAjustado; }

    public Double getMontoFiniquito() { return montoFiniquito; }
    public void setMontoFiniquito(Double montoFiniquito) { this.montoFiniquito = montoFiniquito; }

    public Double getSaldoCredito() { return saldoCredito; }
    public void setSaldoCredito(Double saldoCredito) { this.saldoCredito = saldoCredito; }

    public Date getFechaFiniquito() { return fechaFiniquito; }
    public void setFechaFiniquito(Date fechaFiniquito) { this.fechaFiniquito = fechaFiniquito; }

    public Date getFechaIncobrable() { return fechaIncobrable; }
    public void setFechaIncobrable(Date fechaIncobrable) { this.fechaIncobrable = fechaIncobrable; }

    public Boolean getRdrTblMovimientos() { return rdrTblMovimientos; }
    public void setRdrTblMovimientos(Boolean rdrTblMovimientos) { this.rdrTblMovimientos = rdrTblMovimientos; }

    public Boolean getRdrFiniquito() { return rdrFiniquito; }
    public void setRdrFiniquito(Boolean rdrFiniquito) { this.rdrFiniquito = rdrFiniquito; }

    public Boolean getRdrBtnAjustar() { return rdrBtnAjustar; }
    public void setRdrBtnAjustar(Boolean rdrBtnAjustar) { this.rdrBtnAjustar = rdrBtnAjustar; }

    public Boolean getRdrTblAvales() { return rdrTblAvales; }
    public void setRdrTblAvales(Boolean rdrTblAvales) { this.rdrTblAvales = rdrTblAvales; }

    public Boolean getRdrBtnTransferir() { return rdrBtnTransferir; }
    public void setRdrBtnTransferir(Boolean rdrBtnTransferir) { this.rdrBtnTransferir = rdrBtnTransferir; }

    public Boolean getRdrBtnIncobrable() { return rdrBtnIncobrable; }
    public void setRdrBtnIncobrable(Boolean rdrBtnIncobrable) { this.rdrBtnIncobrable = rdrBtnIncobrable; }

    public Boolean getEditTable() { return editTable; }
    public void setEditTable(Boolean editTable) { this.editTable = editTable; }

    public Integer getOrigen() { return origen; }
    public void setOrigen(Integer origen) { this.origen = origen; }


} 
