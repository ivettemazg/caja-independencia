package mx.com.evoti.dto.bancos;
// Generated 21/10/2016 12:57:00 PM by Hibernate Tools 4.3.1


import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;



public class BancosDto  implements java.io.Serializable {

    private static final long serialVersionUID = 5897633744462879692L;


     private Integer banId;
     private Integer banConcepto;
     private Integer banIdConceptoSistema;
     private Integer banAjustado;
     private Double banMonto;
     private Integer banEmpresa;
     private Date banFechatransaccion;
     private String concepto;
     private String banEmpresaStri;
     private String color;
     private String ecId;
     private BigInteger rbeId;
     private Date rbeFechaRel;
     private BigInteger idPantalla;
     private String claveNombre;

    public BancosDto() {
    }

    /**
     * @return the banId
     */
    public Integer getBanId() {
        return banId;
    }

    /**
     * @param banId the banId to set
     */
    public void setBanId(Integer banId) {
        this.banId = banId;
    }

    /**
     * @return the banConcepto
     */
    public Integer getBanConcepto() {
        return banConcepto;
    }

    /**
     * @param banConcepto the banConcepto to set
     */
    public void setBanConcepto(Integer banConcepto) {
        this.banConcepto = banConcepto;
    }

    /**
     * @return the banIdConceptoSistema
     */
    public Integer getBanIdConceptoSistema() {
        return banIdConceptoSistema;
    }

    /**
     * @param banIdConceptoSistema the banIdConceptoSistema to set
     */
    public void setBanIdConceptoSistema(Integer banIdConceptoSistema) {
        this.banIdConceptoSistema = banIdConceptoSistema;
    }

    /**
     * @return the banAjustado
     */
    public Integer getBanAjustado() {
        return banAjustado;
    }

    /**
     * @param banAjustado the banAjustado to set
     */
    public void setBanAjustado(Integer banAjustado) {
        this.banAjustado = banAjustado;
    }

    /**
     * @return the banMonto
     */
    public double getBanMonto() {
        return banMonto;
    }

    /**
     * @param banMonto the banMonto to set
     */
    public void setBanMonto(double banMonto) {
        this.banMonto = banMonto;
    }

    /**
     * @return the banEmpresa
     */
    public int getBanEmpresa() {
        return banEmpresa;
    }

    /**
     * @param banEmpresa the banEmpresa to set
     */
    public void setBanEmpresa(int banEmpresa) {
        this.banEmpresa = banEmpresa;
    }

    /**
     * @return the banFechatransaccion
     */
    public Date getBanFechatransaccion() {
        return banFechatransaccion;
    }

    /**
     * @param banFechatransaccion the banFechatransaccion to set
     */
    public void setBanFechatransaccion(Date banFechatransaccion) {
        this.banFechatransaccion = banFechatransaccion;
    }

    /**
     * @return the concepto
     */
    public String getConcepto() {
        return concepto;
    }

    /**
     * @param concepto the concepto to set
     */
    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    /**
     * @return the banEmpresaStri
     */
    public String getBanEmpresaStri() {
        return banEmpresaStri;
    }

    /**
     * @param banEmpresaStri the banEmpresaStri to set
     */
    public void setBanEmpresaStri(String banEmpresaStri) {
        this.banEmpresaStri = banEmpresaStri;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.banId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BancosDto other = (BancosDto) obj;
        return Objects.equals(this.banId, other.banId);
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the ecId
     */
    public String getEcId() {
        return ecId;
    }

    /**
     * @param ecId the ecId to set
     */
    public void setEcId(String ecId) {
        this.ecId = ecId;
    }

    /**
     * @return the rbeId
     */
    public BigInteger getRbeId() {
        return rbeId;
    }

    /**
     * @param rbeId the rbeId to set
     */
    public void setRbeId(BigInteger rbeId) {
        this.rbeId = rbeId;
    }

    /**
     * @return the rbeFechaRel
     */
    public Date getRbeFechaRel() {
        return rbeFechaRel;
    }

    /**
     * @param rbeFechaRel the rbeFechaRel to set
     */
    public void setRbeFechaRel(Date rbeFechaRel) {
        this.rbeFechaRel = rbeFechaRel;
    }

    /**
     * @return the idPantalla
     */
    public BigInteger getIdPantalla() {
        return idPantalla;
    }

    /**
     * @param idPantalla the idPantalla to set
     */
    public void setIdPantalla(BigInteger idPantalla) {
        this.idPantalla = idPantalla;
    }

    /**
     * @return the claveNombre
     */
    public String getClaveNombre() {
        return claveNombre;
    }

    /**
     * @param claveNombre the claveNombre to set
     */
    public void setClaveNombre(String claveNombre) {
        this.claveNombre = claveNombre;
    }

   
}


