package mx.com.evoti.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class TotalesAmortizacionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer creditoId;
    private Double saldoPendiente;
    private Double saldoCapital;
    private BigDecimal catPendAdeudo;
    private BigDecimal catPendCap;

    public Integer getCreditoId() {
        return creditoId;
    }

    public void setCreditoId(Integer creditoId) {
        this.creditoId = creditoId;
    }

    public Double getSaldoPendiente() {
        return saldoPendiente != null ? saldoPendiente : 0.0;
    }

    public void setSaldoPendiente(Double saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public Double getSaldoCapital() {
        return saldoCapital != null ? saldoCapital : 0.0;
    }

    public void setSaldoCapital(Double saldoCapital) {
        this.saldoCapital = saldoCapital;
    }

    public BigDecimal getCatPendAdeudo() {
        return catPendAdeudo != null ? catPendAdeudo : BigDecimal.ZERO;
    }

    public void setCatPendAdeudo(BigDecimal catPendAdeudo) {
        this.catPendAdeudo = catPendAdeudo;
    }

    public BigDecimal getCatPendCap() {
        return catPendCap != null ? catPendCap : BigDecimal.ZERO;
    }

    public void setCatPendCap(BigDecimal catPendCap) {
        this.catPendCap = catPendCap;
    }
} 
