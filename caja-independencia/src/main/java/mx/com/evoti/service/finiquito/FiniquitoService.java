package mx.com.evoti.service.finiquito;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mx.com.evoti.bo.administrador.finiquito.FiniquitosBo;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.bo.CatorcenasBo;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.PerfilBo;
import mx.com.evoti.dao.FiniquitoDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.finiquito.AvalesCreditoDto;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.admon.DetalleAdeudoCreditosBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiniquitoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiniquitoService.class);

    private final FiniquitosBo finiquitoBo;
    private final PerfilBo perfilService;
    private final CreditosBo creditoBo;
    private final BancosBo bancoBo;
    private final MovimientosBo movimientosBo;
    private final CatorcenasBo catorcenasBo;

    public FiniquitoService() {
        this.finiquitoBo = new FiniquitosBo();
        this.perfilService = new PerfilBo();
        this.creditoBo = new CreditosBo();
        this.bancoBo = new BancosBo();
        this.movimientosBo = new MovimientosBo();
        this.catorcenasBo = new CatorcenasBo();
    }

    public Usuarios cargarUsuario(Integer usuId) throws BusinessException {
        return perfilService.getUsuarioById(usuId);
    }

    public List<MovimientosDto> obtenerAhorrosPorUsuario(Integer usuId) throws BusinessException {
        return movimientosBo.getAhorrosByUsuId(usuId);
    }

    public void aplicarFiniquito(Usuarios usuario, DetalleCreditoDto credito, Double montoFiniquito,
                                 Date fechaFiniquito, DetalleAdeudoCreditosBean detAdCreBean) throws BusinessException {

        Integer idPago = finiquitoBo.guardaFiniquito(credito.getCreId(), montoFiniquito,
                usuario.getEmpresas().getEmpId(), fechaFiniquito, usuario.getUsuId());

        finiquitoBo.ajustaCredito(detAdCreBean.getAmoPagoCapital(),
                                  detAdCreBean.getAmoPendientesAnteriores(),
                                  montoFiniquito,
                                  Constantes.AMO_ESTATUS_FINIQ_9);

        finiquitoBo.updtAmoPagId(idPago, credito.getCreId());
    }

    public void aplicarAbono(Usuarios usuario, List<MovimientosDto> movimientos,
                             Double saldoCredito, DetalleAdeudoCreditosBean detAdCreBean) throws BusinessException {

        List<Movimientos> abonosCredito = new ArrayList<>();
        double montoAbonoCredito = 0.0;

        for (MovimientosDto dto : movimientos) {
            if (dto.getDevolucion() != null && dto.getDevolucion() > 0) {
                Movimientos mov = new Movimientos();
                mov.setMovAr(dto.getMovAr());
                mov.setMovClaveEmpleado(dto.getMovClaveEmpleado());
                mov.setMovDeposito(dto.getDevolucion() * -1);
                mov.setMovEmpresa(dto.getMovEmpresa());
                mov.setMovEstatus(1);
                mov.setMovFecha(dto.getDevolucionFecha());
                mov.setMovProducto(dto.getMovProducto());
                mov.setMovTipo(Constantes.MOV_ABONOCREDITO);
                mov.setMovUsuId(dto.getMovUsuId());
                if (dto.getMovProducto() == 3) {
                    mov.setMovIdPadre(dto.getMovId());
                }
                abonosCredito.add(mov);
                montoAbonoCredito += dto.getDevolucion();
            }
        }

        if (!abonosCredito.isEmpty()) {
            movimientosBo.guardaDevoluciones(abonosCredito);

            finiquitoBo.ajustaCredito(detAdCreBean.getAmoPagoCapital(),
                                      detAdCreBean.getAmoPendientesAnteriores(),
                                      montoAbonoCredito,
                                      Constantes.AMO_ESTATUS_ABONO_CRE_8);
        }
    }

    public void guardarMovimientoIndividual(Movimientos movimiento, boolean afectarBanco) throws BusinessException {
        movimientosBo.guardaMovimiento(movimiento);
        if (afectarBanco) {
            Long idRelacion = new BigInteger(Util.genUUID().toString()).longValue();
            Bancos banco = generarBancoDesdeMovimiento(movimiento, idRelacion);
            bancoBo.guardaBancoCEdoCta(banco, idRelacion);
        }
    }

    public Bancos generarBancoDesdeMovimiento(Movimientos mov, Long idRelacion) {
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
                banco.setBanConcepto(mov.getMovAr() == 1 ? Constantes.BAN_DEV_A_FIJO : Constantes.BAN_DEV_RDTO_FIJO);
                break;
            case 2:
                banco.setBanConcepto(mov.getMovAr() == 1 ? Constantes.BAN_DEV_A_N_FIJO : Constantes.BAN_DEV_RDTO_N_FIJO);
                break;
            case 3:
                banco.setBanConcepto(mov.getMovAr() == 1 ? Constantes.BAN_DEV_A_VOL : Constantes.BAN_DEV_RDTO_VOL);
                break;
        }

        return banco;
    }

    public void transferirCreditoAAvAles(DetalleCreditoDto credito, List<AvalesCreditoDto> avales, List<String> catorcenas) throws BusinessException {
        for (AvalesCreditoDto aval : avales) {
            CreditosFinal credTransfer = new CreditosFinal();
            credTransfer.setCreUsuId(aval.getAvalUsuId());
            credTransfer.setCreClaveEmpleado(aval.getAvalCveEmpleado());
            credTransfer.setCreEmpresa(aval.getEmpAbrev());
            credTransfer.setCrePrestamo(aval.getMontoCredito());
            credTransfer.setCreEstatus(1);
            credTransfer.setCreCatorcenas(aval.getCatorcenas());
            credTransfer.setCreTipo("NOMINA");
            credTransfer.setCreProducto(6);
            credTransfer.setCrePadre(credito.getCreId());

            creditoBo.creaCreditoTransferido(credTransfer, aval.getPrimerCatorcena());
        }

        creditoBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_TRANSFERIDO, null);
    }

    public void transferir(DetalleCreditoDto credito, List<AvalesCreditoDto> avales) throws BusinessException {
        Double totalAvales = avales.stream()
            .mapToDouble(a -> a.getMontoCredito() != null ? a.getMontoCredito() : 0.0)
            .sum();
    
        Double diferencia = Math.abs(totalAvales - credito.getSaldoTotal());
    
        if (diferencia > 2.0) {
            throw new BusinessException("Debe transferir el monto total del saldo del crédito.");
        }
    
        for (AvalesCreditoDto aval : avales) {
            CreditosFinal credTransfer = new CreditosFinal();
            credTransfer.setCreUsuId(aval.getAvalUsuId());
            credTransfer.setCreClaveEmpleado(aval.getAvalCveEmpleado());
            credTransfer.setCreEmpresa(aval.getEmpAbrev());
            credTransfer.setCrePrestamo(aval.getMontoCredito());
            credTransfer.setCreEstatus(1);
            credTransfer.setCreCatorcenas(aval.getCatorcenas());
            credTransfer.setCreTipo("NOMINA");
            credTransfer.setCreProducto(6); // producto de transferencia, revisar porque tiene 6 que corresponde a ajustado
            credTransfer.setCrePadre(credito.getCreId());
    
            creditoBo.creaCreditoTransferido(credTransfer, aval.getPrimerCatorcena());
        }
    
        creditoBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_TRANSFERIDO, null);
    }

    public void marcarCreditoComoIncobrable(DetalleCreditoDto credito, Date fechaIncobrable) throws BusinessException {
        creditoBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_INCOBRABLE, fechaIncobrable);
        creditoBo.updtEstatusAmoInt(credito.getCreId(), Constantes.AMO_ESTATUS_INCOB_12);
    }

    public List<AvalesCreditoDto> obtenerAvales(Integer creditoId) throws BusinessException {
        return creditoBo.getAvalesCredito(creditoId);
    }

    public List<String> obtenerCatorcenasSiguientes(Date fechaBase) throws BusinessException {
        return catorcenasBo.getCatorcenasSiguientes(fechaBase);
    }


    public void devolverAhorroConBancos(MovimientosDto dto, Usuarios usuario) throws BusinessException {
        Movimientos pojo = crearMovimientoDesdeDto(dto, Constantes.MOV_TIPO_DEVOLUCION);
        movimientosBo.guardaMovimiento(pojo);
        Long idRelacion = new BigInteger(Util.genUUID().toString()).longValue();
        Bancos banco = generarBancoDesdeMovimiento(pojo, idRelacion);
        bancoBo.guardaBancoCEdoCta(banco, idRelacion);
    }

    public void devolverAhorroAbonoCredito(MovimientosDto dto, Usuarios usuario,
                                           List<MovimientosDto> movimientos, Double saldoCredito) throws BusinessException {
        if (dto.getDevolucion() == null) dto.setDevolucion(0.0);

        List<Movimientos> abonos = new ArrayList<>();
        double montoAbono = movimientos.stream()
            .mapToDouble(m -> m.getDevolucion() != null ? m.getDevolucion() : 0.0).sum();

        double diferencia = montoAbono - saldoCredito;
        if (diferencia <= 3) {
            for (MovimientosDto m : movimientos) {
                if (m.getDevolucionFecha() == null) {
                    m.setDevolucionFecha(m.getMovFecha());
                }
                Movimientos mov = crearMovimientoDesdeDto(m, Constantes.MOV_ABONOCREDITO);
                if (mov.getMovDeposito() < 0) {
                    abonos.add(mov);
                }
            }
            movimientosBo.guardaDevoluciones(abonos);
        } else {
            throw new BusinessException("El monto de la devolución no puede ser mayor al adeudo del crédito");
        }
    }

    public Movimientos crearMovimientoDesdeDto(MovimientosDto dto, String tipo) {
        Movimientos mov = new Movimientos();
        mov.setMovAr(dto.getMovAr());
        mov.setMovClaveEmpleado(dto.getMovClaveEmpleado());
        mov.setMovDeposito((dto.getDevolucion() != null ? dto.getDevolucion() : 0.0) * -1);
        mov.setMovEmpresa(dto.getMovEmpresa());
        mov.setMovEstatus(1);
        mov.setMovFecha(dto.getDevolucionFecha());
        mov.setMovProducto(dto.getMovProducto());
        mov.setMovTipo(tipo);
        mov.setMovUsuId(dto.getMovUsuId());
        if (dto.getMovProducto() == 3) {
            mov.setMovIdPadre(dto.getMovId());
        }
        return mov;
    }

    public List<String> obtenerCatorcenas() throws BusinessException {
        return catorcenasBo.getCatorcenasSiguientes(new Date());
    }

    public void asignarMontosAvalesProporcion(List<AvalesCreditoDto> avales, Double adeudoTotalCredito) {
        if (avales == null || avales.isEmpty() || adeudoTotalCredito == null || avales.size() == 0) return;

        Double xAval = adeudoTotalCredito / avales.size();
        for (AvalesCreditoDto aval : avales) {
            aval.setMontoCredito(Util.round(xAval));
        }
    }


    public void marcarIncobrable(DetalleCreditoDto credito, Date fechaIncobrable) throws BusinessException {
        creditoBo.updtCreditoEstatus(credito.getCreId(), Constantes.CRE_EST_INCOBRABLE, fechaIncobrable);
        creditoBo.updtEstatusAmoInt(credito.getCreId(), Constantes.AMO_ESTATUS_INCOB_12);
    }
    
    public void actualizarBajaEmpleadoConFiniquito(Integer usuId, double deudaCreditos, double ahorros, int estatus, Date fechaFiniquito) throws IntegracionException {
        FiniquitoDao dao = new FiniquitoDao();
        dao.actualizarBajaEmpleadoConFiniquito(usuId, deudaCreditos, ahorros, estatus, fechaFiniquito);
    }


} 
