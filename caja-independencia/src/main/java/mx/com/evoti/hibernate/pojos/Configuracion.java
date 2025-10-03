package mx.com.evoti.hibernate.pojos;

import java.io.Serializable;
import java.util.Date;

public class Configuracion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer conId;
    private Integer conFaHabilitado; // 1 habilitado, 0 deshabilitado
    private Integer conAgHabilitado; // 1 habilitado, 0 deshabilitado
    private Date conFechaMod;        // opcional, informativo

    public Integer getConId() {
        return conId;
    }

    public void setConId(Integer conId) {
        this.conId = conId;
    }

    public Integer getConFaHabilitado() {
        return conFaHabilitado;
    }

    public void setConFaHabilitado(Integer conFaHabilitado) {
        this.conFaHabilitado = conFaHabilitado;
    }

    public Integer getConAgHabilitado() {
        return conAgHabilitado;
    }

    public void setConAgHabilitado(Integer conAgHabilitado) {
        this.conAgHabilitado = conAgHabilitado;
    }

    public Date getConFechaMod() {
        return conFechaMod;
    }

    public void setConFechaMod(Date conFechaMod) {
        this.conFechaMod = conFechaMod;
    }
}

