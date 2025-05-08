package mx.com.evoti.service.finiquito;

import java.util.List;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.finiquito.AvalesCreditoDto;
import mx.com.evoti.util.Constantes;

public class ValidadorFiniquito {

    /**
     * Valida si el monto a devolver no excede el adeudo
     */
    public static boolean validarMontoAbonoVsSaldo(Double montoAbono, Double saldoCredito) {
        if (montoAbono == null || saldoCredito == null) return false;
        return (montoAbono - saldoCredito) <= 3;
    }

    /**
     * Verifica si el crédito está en estado válido para ajustes
     */
    public static boolean esCreditoAjustable(Integer estatusId) {
        return estatusId != null && (
                estatusId.equals(Constantes.CRE_EST_ACTIVO) || // ACTIVO
                estatusId.equals(Constantes.CRE_EST_PAGADO ) || // PAGADO
                estatusId.equals(Constantes.CRE_EST_AJUSTADO));   // AJUSTADO
    }

    /**
     * Verifica si hay movimientos válidos para abono
     */
    public static boolean tieneAhorrosSuficientes(List<MovimientosDto> movimientos) {
        if (movimientos == null || movimientos.isEmpty()) return false;
        double total = movimientos.stream()
                .mapToDouble(m -> m.getTotalMovimiento() != null ? m.getTotalMovimiento() : 0.0)
                .sum();
        return total > 10;
    }

    public static boolean validarMontoFiniquito(Double montoFiniquito) {
        return montoFiniquito != null && montoFiniquito > 0.0;
    }

    public static boolean validarAbonosACredito(Double montoAbonoCredito) {
        return montoAbonoCredito != null && montoAbonoCredito > 0.0;
    }

    public static boolean validarTransferenciaAvales(List<AvalesCreditoDto> avales, Double saldoTotal) {
        if (avales == null || avales.isEmpty() || saldoTotal == null) {
            return false;
        }

        double suma = avales.stream()
                .mapToDouble(a -> a.getMontoCredito() != null ? a.getMontoCredito() : 0.0)
                .sum();

        double diferencia = Math.abs(suma - saldoTotal);
        return diferencia <= 2.0;
    }

    public static boolean esCreditoSinSaldo(Double saldoCredito) {
        return saldoCredito == null || saldoCredito <= 5.0;
    }

    public static boolean esCreditoSinAhorro(Double totalAhorros) {
        return totalAhorros == null || totalAhorros <= 5.0;
    }

    public static boolean esCreditoConSoloAhorro(List<DetalleCreditoDto> creditos) {
        if (creditos == null || creditos.isEmpty()) return true;

        boolean tieneActivos = creditos.stream().anyMatch(c -> !esCreditoPagado(c.getCreEstatusId()));
        return !tieneActivos;
    }

    private static boolean esCreditoPagado(Integer estatusId) {
        return estatusId != null && (estatusId.equals(2) || estatusId.equals(3) || estatusId.equals(4) || estatusId.equals(5));
    }

    public static boolean estaPagado(int estatusId) {
        return estatusId == Constantes.CRE_EST_PAGADO
            || estatusId == Constantes.CRE_EST_CANCELADO
            || estatusId == Constantes.CRE_EST_TRANSFERIDO
            || estatusId == Constantes.CRE_EST_INCOBRABLE;
    }

    public static boolean estaActivo(int estatusId) {
        return estatusId == Constantes.CRE_EST_ACTIVO;
    }
} 
