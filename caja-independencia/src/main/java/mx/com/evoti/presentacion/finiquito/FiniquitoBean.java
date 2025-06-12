package mx.com.evoti.presentacion.finiquito;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.finiquito.AvalesCreditoDto;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.admon.DetalleAdeudoCreditosBean;
import mx.com.evoti.service.finiquito.FiniquitoService;
import mx.com.evoti.service.finiquito.ValidadorFiniquito;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;

import org.primefaces.event.RowEditEvent;
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

    private Double sumaTotalDevolucion;
    private Double adeudoTotalCredito;
    private Double adeudoAjustado;
    private Double montoFiniquito;
    private Date fechaFiniquito;
    private Date fechaIncobrable;
    private Double deudaOriginal; 
    private Double totalAplicadoDesdeAhorros;


    public Double getDeudaOriginal() {
        return deudaOriginal;
    }

    public void setDeudaOriginal(Double deudaOriginal) {
        this.deudaOriginal = deudaOriginal;
    }

    public Double getTotalAplicadoDesdeAhorros() {
        return totalAplicadoDesdeAhorros;
    }

    public void setTotalAplicadoDesdeAhorros(Double totalAplicadoDesdeAhorros) {
        this.totalAplicadoDesdeAhorros = totalAplicadoDesdeAhorros;
    }

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

            refrescarVista();
        } catch (BusinessException ex) {
            LOGGER.error("Error inicializando bean de finiquito", ex);
            muestraMensajeError("Error al cargar información de baja", ex.getMessage(), null);
        }
    }

    public void obtieneTotalesMovimientos() {
        try {
                LOGGER.debug("***************** OBTIENETOTALESMOVIMIENTO++++++++++++++++++++++++++++++");
                this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());
    
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
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

            // Inicializar campos nuevos de resumen
            this.deudaOriginal = credito.getSaldoTotal();
            this.adeudoTotalCredito = Util.round(credito.getSaldoTotal());
            this.adeudoAjustado = Util.round(credito.getSaldoTotal());
            this.totalAplicadoDesdeAhorros = 0.0;

            this.montoFiniquito = 0.0;

            // Limpiar devoluciones previas si existen
            for (MovimientosDto mov : movimientos) {
                mov.setDevolucion(null);
                mov.setDevolucionFecha(null);
                mov.setEditadoFiniquitos(false);
            }

            boolean tieneAhorros = ValidadorFiniquito.tieneAhorrosSuficientes(movimientos);
            this.rdrFiniquito = !tieneAhorros && adeudoTotalCredito > 1;
            this.rdrBtnAjustar = this.rdrFiniquito;
            this.editTable = tieneAhorros && adeudoTotalCredito > 1;

            // Datos auxiliares para amortización
            detAdCreBean.getAmortizacionPagoCap();
            detAdCreBean.getAmortizacionesPendientes();

            // Asegurar actualización del resumen visual
            super.updtComponent("frmDlgAjustar:pgResumenAjuste");

        } catch (Exception ex) {
            LOGGER.error("Error al inicializar dialogo de ajuste", ex);
            muestraMensajeError("No fue posible cargar el diálogo de ajuste", ex.getMessage(), null);
        }
    }


    public void aplicaDevolucion() {
        try {
            if (movDevSelected != null) {
                if (movDevSelected.getDevolucion() == null) {
                    movDevSelected.setDevolucion(movDevSelected.getTotalMovimiento());
                }
                if (movDevSelected.getDevolucionFecha() == null) {
                    movDevSelected.setDevolucionFecha(new Date());
                }

                // Determina el tipo de movimiento según el origen
                String tipoMovimiento = (origen == 1) ? Constantes.MOV_TIPO_DEVOLUCION : Constantes.MOV_ABONOCREDITO;
                boolean afectarBanco = (origen == 1);

                Movimientos mov = finiquitoService.crearMovimientoDesdeDto(movDevSelected, tipoMovimiento);
                finiquitoService.guardarMovimientoIndividual(mov, afectarBanco);

                muestraMensajeExito("La devolución fue realizada exitosamente", "", null);
            }

            this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());

            // Recalcular total abono
            double montoAbonoCredito = movimientos.stream()
                    .mapToDouble(m -> m.getDevolucion() != null ? m.getDevolucion() : 0.0)
                    .sum();

            this.rdrBtnAjustar = montoAbonoCredito > 5.0;
            this.adeudoAjustado = this.adeudoAjustado - montoAbonoCredito;

            actualizarBajaEmpleadoFinal();

            updtComponent("frmCreditoDetalle:tblAhorros");
            updtComponent("frmDlgAjustar:btnAjusta");
            updtComponent("frmDlgAjustar:pgResumenAjuste");

        } catch (Exception ex) {
            LOGGER.error("Error aplicando devolución", ex);
            muestraMensajeError("Error aplicando devolución", ex.getMessage(), null);
        }
    }



    public void onEdit(RowEditEvent event) {
            try {
                MovimientosDto mov = (MovimientosDto) event.getObject();

                if (mov.getDevolucion() == null || mov.getDevolucion() < 0) {
                    mov.setDevolucion(0.0);
                }

                // Ajustar valores válidos dentro del rango
                double max = mov.getTotalMovimiento() != null ? mov.getTotalMovimiento() : 0.0;
                if (mov.getDevolucion() > max) {
                    mov.setDevolucion(max);
                }

                // Si no tiene fecha, asignar fecha actual
                if (mov.getDevolucionFecha() == null) {
                    mov.setDevolucionFecha(new Date());
                }

                // Marcar como editado para ser considerado en el ajuste
                mov.setEditadoFiniquitos(true);

                // Recalcular totales y estado del botón
                recalcularTotales();

                // Actualizar UI
                updtComponent("frmDlgAjustar:btnAjusta");
                updtComponent("frmDlgAjustar:pgDlgAjustar1");
                updtComponent("frmDlgAjustar:pgResumenAjuste");

            } catch (Exception ex) {
                LOGGER.error("Error en edición de fila de ahorros", ex);
                muestraMensajeError("Error aplicando devolución", ex.getMessage(), null);
            }
        }


    public void ajustaCredito() {
        try {
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();

            if (Boolean.TRUE.equals(rdrFiniquito)) {
                aplicarModoFiniquito(credito);
            } else {
                if (!aplicarModoAbono(credito)) {
                    return; // Detuvo el flujo por validación o error
                }
            }

            actualizarVistaPostAjuste();
            actualizarBajaEmpleadoFinal();

            updtComponent("frmCreditoDetalle:growlDevsAho");
            super.hideShowDlg("PF('dlgAjustaCreditoW').hide()");

        } catch (BusinessException ex) {
            LOGGER.error("Error al ajustar crédito", ex);
            muestraMensajeError("Error al ajustar crédito", ex.getMessage(), null);
        }
    }

    private void aplicarModoFiniquito(DetalleCreditoDto credito) throws BusinessException {
        finiquitoService.aplicarFiniquito(usuarioBaja, credito, montoFiniquito, fechaFiniquito, detAdCreBean);
        muestraMensajeExito("El crédito fue ajustado por finiquito", "", null);
    }

    private boolean aplicarModoAbono(DetalleCreditoDto credito) {
        List<MovimientosDto> abonosValidos = movimientos.stream()
            .filter(m -> Boolean.TRUE.equals(m.isEditadoFiniquitos()) &&
                        m.getDevolucion() != null && m.getDevolucion() > 0)
            .peek(m -> {
                if (m.getDevolucionFecha() == null) {
                    m.setDevolucionFecha(new Date());
                }
            })
            .collect(Collectors.toList());

        if (abonosValidos.isEmpty()) {
            muestraMensajeGen("No hay devoluciones válidas", "Debes registrar al menos una devolución válida para continuar", null, FacesMessage.SEVERITY_WARN);
            return false;
        }

        double totalAbono = abonosValidos.stream()
            .mapToDouble(MovimientosDto::getDevolucion)
            .sum();

        if (totalAbono > adeudoTotalCredito) {
            muestraMensajeError("Error de validación", "El total de devoluciones excede el monto adeudado", null);
            return false;
        }

        try {
            if (origen == 1) {
                for (MovimientosDto dto : abonosValidos) {
                    Movimientos movimiento = finiquitoService.crearMovimientoDesdeDto(dto, Constantes.MOV_TIPO_DEVOLUCION);
                    finiquitoService.guardarMovimientoIndividual(movimiento, true);
                }
                muestraMensajeExito("Las devoluciones fueron aplicadas", "", null);
            } else {
                double saldoRestante = adeudoTotalCredito - totalAplicadoDesdeAhorros;
                finiquitoService.aplicarAbono(usuarioBaja, abonosValidos, saldoRestante, detAdCreBean);
                muestraMensajeExito("El crédito fue ajustado por abono", "", null);
            }
        } catch (BusinessException e) {
            LOGGER.error("Error guardando abono", e);
            muestraMensajeError("Error guardando abono", e.getMessage(), null);
            return false;
        }

        return true;
    }

    private void actualizarVistaPostAjuste() {
        try {
            this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());
            detAdCreBean.obtieneCreditosDetalle();
            this.creditos = detAdCreBean.getCreditos();

            updtComponent("frmCreditoDetalle:tblCreditoAdeudos");
            updtComponent("frmCreditoDetalle:tblAhorros");

        } catch (BusinessException e) {
            LOGGER.error("Error actualizando vista post-ajuste", e);
            muestraMensajeError("Error post-ajuste", e.getMessage(), null);
        }
    }


    private void recalcularTotales() {
        if (movimientos == null || movimientos.isEmpty()) {
            totalAplicadoDesdeAhorros = 0.0;
            adeudoAjustado = adeudoTotalCredito;
            rdrBtnAjustar = false;
            return;
        }

        double totalAbono = movimientos.stream()
            .filter(m -> m.getDevolucion() != null && m.getDevolucion() > 0)
            .mapToDouble(MovimientosDto::getDevolucion)
            .sum();

        this.totalAplicadoDesdeAhorros = totalAbono;
        this.adeudoAjustado = this.adeudoTotalCredito - totalAbono;
        this.rdrBtnAjustar = totalAbono > 5.0;

        updtComponent("frmDlgAjustar:pgResumenAjuste");
    }


    public double sumaTotalAplicado() {
        return movimientos.stream()
                .mapToDouble(m -> m.getDevolucion() != null ? m.getDevolucion() : 0.0)
                .sum();
    }

    public void setDevolucionTotal(MovimientosDto mov) {
        mov.setDevolucion(mov.getTotalMovimiento());
        mov.setDevolucionFecha(new Date());
        mov.setEditadoFiniquitos(true);
        recalcularTotales();
    }

    public void devolverTotalAhorro() {
        try {
            // Ejecuta devolución total en el servicio
            finiquitoService.devolverTotalesAhorros(usuarioBaja, movimientos);

            // Refresca la lista de movimientos y recalcúla totales
            movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());
            recalcularTotales();
            actualizarBajaEmpleadoFinal();
            // Actualiza componentes de la vista
            updtComponent("frmCreditoDetalle:tblAhorros");
            updtComponent("frmDlgAjustar:pgResumenAjuste");

            muestraMensajeExito("Todas las devoluciones fueron aplicadas correctamente", "", null);
        } catch (BusinessException ex) {
            LOGGER.error("Error al devolver total de ahorro", ex);
            muestraMensajeError("Error procesando devoluciones", ex.getMessage(), null);
        }
    }

    public void onEditTransfiere(RowEditEvent event) {

        AvalesCreditoDto aval = (AvalesCreditoDto) event.getObject();

        LOGGER.debug("" + aval.getMontoCredito());
        LOGGER.debug("" + aval.getPrimerCatorcena());
        LOGGER.debug("" + aval.getCatorcenas());

    }


    public void initDlgTransferir() {
        try {
            DetalleCreditoDto credito = detAdCreBean.getCreditoSelected();
            this.cveCredSeleccionado = credito.getCreClave();
            
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

            finiquitoService.asignarMontosAvalesProporcion(avales, credito.getSaldoTotal());
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
        
            actualizarVistaPostAjuste();
            actualizarBajaEmpleadoFinal();
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


    public void inicializaDialogDev(MovimientosDto mov) {
        this.movDevSelected = mov;

        if (this.movDevSelected != null) {
            this.movDevSelected.setDevolucion(this.movDevSelected.getTotalMovimiento());
            this.movDevSelected.setDevolucionFecha(Util.restaHrToDate(new Date(), 6));
        }

        updtComponent("frmDlgDev");
    }

    public void actualizarBajaEmpleadoFinal() {
        try {
            double saldoCreditos = this.creditos.stream()
                    .mapToDouble(c -> c.getSaldoTotal() != null ? c.getSaldoTotal() : 0.0)
                    .sum();
            double saldoAhorros = this.movimientos.stream()
                    .mapToDouble(m -> m.getTotalMovimiento() != null ? m.getTotalMovimiento() : 0.0)
                    .sum();

            int estatus;
            if (saldoCreditos >= 5.0) {
                estatus = Constantes.BAJA_PENDIENTE;
            } else if (saldoAhorros >= 5.0) {
                estatus = Constantes.BAJA_AHORROSXDEVOLVER;
            } else {
                estatus = Constantes.BAJA_COMPLETADA;
            }

            finiquitoService.actualizarBajaEmpleadoConFiniquito(
                usuarioBaja.getUsuId(),
                Util.round(saldoCreditos),
                Util.round(saldoAhorros),
                estatus,
                new Date()
            );
            LOGGER.info("   → Baja actualizada con estatus final=" + estatus);

        } catch (Exception e) {
            LOGGER.error("Error al actualizar baja_empleados desde finiquito", e);
        }
    }


        public void refrescarVista() {
        try {
            detAdCreBean.setUsuario(usuarioBaja);
            detAdCreBean.obtieneCreditosDetalle();
            this.creditos = detAdCreBean.getCreditos();

            if (creditos == null) {
                creditos = new java.util.ArrayList<>();
            }

            boolean todosPagados = creditos.stream().allMatch(c -> ValidadorFiniquito.estaPagado(c.getCreEstatusId()));
            boolean hayActivos = creditos.stream().anyMatch(c -> ValidadorFiniquito.estaActivo(c.getCreEstatusId()));

            this.rdrTblMovimientos = creditos.isEmpty() || (todosPagados && !hayActivos);
            this.origen = rdrTblMovimientos ? 1 : 2;

            this.movimientos = finiquitoService.obtenerAhorrosPorUsuario(usuarioBaja.getUsuId());
        } catch (BusinessException ex) {
            LOGGER.error("Error refrescando vista", ex);
            muestraMensajeError("Error actualizando vista", ex.getMessage(), null);
        }
    }

    public void ejecutarRefrescoDesdeVista() {
        refrescarVista();
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

    public double getSumaTotalDevolucion() {
        if (movimientos == null) {
            return 0.0;
        }
        return movimientos.stream()
                .mapToDouble(m -> m.getTotalMovimiento() != null ? m.getTotalMovimiento() : 0.0)
                .sum();
    }

    public void setSumaTotalDevolucion(Double sumaTotalDevolucion) {
        this.sumaTotalDevolucion = sumaTotalDevolucion;
    }


} 
