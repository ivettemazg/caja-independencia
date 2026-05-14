package mx.com.evoti.hibernate.pojos;

import java.io.Serializable;
import java.util.Date;

public class PasswordResetTokens implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer prtId;
    private Integer prtUsuId;
    private String prtTokenHash;
    private Date prtFechaCreacion;
    private Date prtFechaExpira;
    private Date prtFechaUso;
    private Integer prtEstatus;
    private String prtIpSolicitud;
    private String prtUserAgent;

    public Integer getPrtId() {
        return prtId;
    }

    public void setPrtId(Integer prtId) {
        this.prtId = prtId;
    }

    public Integer getPrtUsuId() {
        return prtUsuId;
    }

    public void setPrtUsuId(Integer prtUsuId) {
        this.prtUsuId = prtUsuId;
    }

    public String getPrtTokenHash() {
        return prtTokenHash;
    }

    public void setPrtTokenHash(String prtTokenHash) {
        this.prtTokenHash = prtTokenHash;
    }

    public Date getPrtFechaCreacion() {
        return prtFechaCreacion;
    }

    public void setPrtFechaCreacion(Date prtFechaCreacion) {
        this.prtFechaCreacion = prtFechaCreacion;
    }

    public Date getPrtFechaExpira() {
        return prtFechaExpira;
    }

    public void setPrtFechaExpira(Date prtFechaExpira) {
        this.prtFechaExpira = prtFechaExpira;
    }

    public Date getPrtFechaUso() {
        return prtFechaUso;
    }

    public void setPrtFechaUso(Date prtFechaUso) {
        this.prtFechaUso = prtFechaUso;
    }

    public Integer getPrtEstatus() {
        return prtEstatus;
    }

    public void setPrtEstatus(Integer prtEstatus) {
        this.prtEstatus = prtEstatus;
    }

    public String getPrtIpSolicitud() {
        return prtIpSolicitud;
    }

    public void setPrtIpSolicitud(String prtIpSolicitud) {
        this.prtIpSolicitud = prtIpSolicitud;
    }

    public String getPrtUserAgent() {
        return prtUserAgent;
    }

    public void setPrtUserAgent(String prtUserAgent) {
        this.prtUserAgent = prtUserAgent;
    }
}
