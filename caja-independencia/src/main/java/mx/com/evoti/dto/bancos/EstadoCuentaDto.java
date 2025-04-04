package mx.com.evoti.dto.bancos;
// Generated 27/03/2017 05:12:46 PM by Hibernate Tools 4.3.1


import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;


public class EstadoCuentaDto  implements java.io.Serializable {

    private static final long serialVersionUID = 2901883977552493088L;

     private Integer ecId;
     private Integer ecConcepto;
     private Double ecMonto;
     private Integer ecEmpresa;
     private Date ecFechatransaccion;
     private Integer ecAjustado;
     private Integer ecPadre;
     private String ecDescripcion;
     private String strConcepto;
     private BancosConceptosDto concepto;
     private String edoCtaEmpresa;
     private Integer banConceptoId;
     private String color;
     private BigInteger rbeId;
     private Date rbeFechaRel;
     private BigInteger idPantalla;

    public EstadoCuentaDto() {
    }

    /**
     * @return the ecId
     */
    public Integer getEcId() {
        return ecId;
    }

    /**
     * @param ecId the ecId to set
     */
    public void setEcId(Integer ecId) {
        this.ecId = ecId;
    }

    /**
     * @return the ecConcepto
     */
    public Integer getEcConcepto() {
        return ecConcepto;
    }

    /**
     * @param ecConcepto the ecConcepto to set
     */
    public void setEcConcepto(Integer ecConcepto) {
        this.ecConcepto = ecConcepto;
    }

    /**
     * @return the ecMonto
     */
    public Double getEcMonto() {
        return ecMonto;
    }

    /**
     * @param ecMonto the ecMonto to set
     */
    public void setEcMonto(Double ecMonto) {
        this.ecMonto = ecMonto;
    }

    /**
     * @return the ecEmpresa
     */
    public Integer getEcEmpresa() {
        return ecEmpresa;
    }

    /**
     * @param ecEmpresa the ecEmpresa to set
     */
    public void setEcEmpresa(Integer ecEmpresa) {
        this.ecEmpresa = ecEmpresa;
    }

    /**
     * @return the ecFechatransaccion
     */
    public Date getEcFechatransaccion() {
        return ecFechatransaccion;
    }

    /**
     * @param ecFechatransaccion the ecFechatransaccion to set
     */
    public void setEcFechatransaccion(Date ecFechatransaccion) {
        this.ecFechatransaccion = ecFechatransaccion;
    }

    /**
     * @return the ecAjustado
     */
    public Integer getEcAjustado() {
        return ecAjustado;
    }

    /**
     * @param ecAjustado the ecAjustado to set
     */
    public void setEcAjustado(Integer ecAjustado) {
        this.ecAjustado = ecAjustado;
    }

    /**
     * @return the ecDescripcion
     */
    public String getEcDescripcion() {
        return ecDescripcion;
    }

    /**
     * @param ecDescripcion the ecDescripcion to set
     */
    public void setEcDescripcion(String ecDescripcion) {
        this.ecDescripcion = ecDescripcion;
    }

   

    /**
     * @return the ecPadre
     */
    public Integer getEcPadre() {
        return ecPadre;
    }

    /**
     * @param ecPadre the ecPadre to set
     */
    public void setEcPadre(Integer ecPadre) {
        this.ecPadre = ecPadre;
    }

    /**
     * @return the edoCtaEmpresa
     */
    public String getEdoCtaEmpresa() {
        return edoCtaEmpresa;
    }

    /**
     * @param edoCtaEmpresa the edoCtaEmpresa to set
     */
    public void setEdoCtaEmpresa(String edoCtaEmpresa) {
        this.edoCtaEmpresa = edoCtaEmpresa;
    }

       @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.ecId);
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
        final EstadoCuentaDto other = (EstadoCuentaDto) obj;
        return Objects.equals(this.ecId, other.ecId);
    }

    /**
     * @return the concepto
     */
    public BancosConceptosDto getConcepto() {
        return concepto;
    }

    /**
     * @param concepto the concepto to set
     */
    public void setConcepto(BancosConceptosDto concepto) {
        this.concepto = concepto;
    }

    /**
     * @return the banConceptoId
     */
    public Integer getBanConceptoId() {
        return banConceptoId;
    }

    /**
     * @param banConceptoId the banConceptoId to set
     */
    public void setBanConceptoId(Integer banConceptoId) {
        this.banConceptoId = banConceptoId;
    }

    /**
     * @return the strConcepto
     */
    public String getStrConcepto() {
        return strConcepto;
    }

    /**
     * @param strConcepto the strConcepto to set
     */
    public void setStrConcepto(String strConcepto) {
        this.strConcepto = strConcepto;
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

   
}