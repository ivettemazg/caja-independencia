package mx.com.evoti.dto;

import java.io.Serializable;

public class TotalesAmortizacionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer creditoId;
    private Double saldoPendiente;
    private Double saldoCapital;
    private Integer catPendAdeudo;
    private Integer catPendCap;

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

    public Integer getCatPendAdeudo() {
        return catPendAdeudo != null ? catPendAdeudo : 0;
    }

    public void setCatPendAdeudo(Integer catPendAdeudo) {
        this.catPendAdeudo = catPendAdeudo;
    }

    public Integer getCatPendCap() {
        return catPendCap != null ? catPendCap : 0;
    }

    public void setCatPendCap(Integer catPendCap) {
        this.catPendCap = catPendCap;
    }
} 
