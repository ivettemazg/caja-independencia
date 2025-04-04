package mx.com.evoti.dto.usuariocomun;
// Generated 1/02/2017 05:08:41 PM by Hibernate Tools 4.3.1

import java.util.Objects;
import mx.com.evoti.hibernate.pojos.ParentescoBen;




public class BeneficiariosDto implements java.io.Serializable {

    private static final long serialVersionUID = 8262769761163163637L;

    private Integer benId;
    private Integer benUsuarios;
    private String benNombre;
    private String benPaterno;
    private String benMaterno;
    private Integer benParentesco;
    private String benParentescoStr;
    private Double benPct;
    private String benDireccion;
    private String benTelefono;
    private String benCelular;
    private Integer benUsuId;
    private ParentescoBen parentesco;

    public BeneficiariosDto() {
    }

    public BeneficiariosDto(Integer benUsuarios, String benNombre, String benPaterno, String benMaterno, Integer benParentesco, Double benPct, String benDireccion, String benTelefono, String benCelular, Integer benUsuId) {
        this.benUsuarios = benUsuarios;
        this.benNombre = benNombre;
        this.benPaterno = benPaterno;
        this.benMaterno = benMaterno;
        this.benParentesco = benParentesco;
        this.benPct = benPct;
        this.benDireccion = benDireccion;
        this.benTelefono = benTelefono;
        this.benCelular = benCelular;
        this.benUsuId = benUsuId;
    }

   
    public Integer getBenId() {
        return this.benId;
    }

    public void setBenId(Integer benId) {
        this.benId = benId;
    }

   
    public Integer getBenUsuarios() {
        return this.benUsuarios;
    }

    public void setBenUsuarios(Integer benUsuarios) {
        this.benUsuarios = benUsuarios;
    }

 
    public String getBenNombre() {
        return this.benNombre;
    }

    public void setBenNombre(String benNombre) {
        this.benNombre = benNombre;
    }

 
    public String getBenPaterno() {
        return this.benPaterno;
    }

    public void setBenPaterno(String benPaterno) {
        this.benPaterno = benPaterno;
    }


    public String getBenMaterno() {
        return this.benMaterno;
    }

    public void setBenMaterno(String benMaterno) {
        this.benMaterno = benMaterno;
    }


    public Integer getBenParentesco() {
        return this.benParentesco;
    }

    public void setBenParentesco(Integer benParentesco) {
        this.benParentesco = benParentesco;
    }

  
    public Double getBenPct() {
        return this.benPct;
    }

    public void setBenPct(Double benPct) {
        this.benPct = benPct;
    }

 
    public String getBenDireccion() {
        return this.benDireccion;
    }

    public void setBenDireccion(String benDireccion) {
        this.benDireccion = benDireccion;
    }

    
    public String getBenTelefono() {
        return this.benTelefono;
    }

    public void setBenTelefono(String benTelefono) {
        this.benTelefono = benTelefono;
    }

   
    public String getBenCelular() {
        return this.benCelular;
    }

    public void setBenCelular(String benCelular) {
        this.benCelular = benCelular;
    }

 
    public Integer getBenUsuId() {
        return this.benUsuId;
    }

    public void setBenUsuId(Integer benUsuId) {
        this.benUsuId = benUsuId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.benId);
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
        final BeneficiariosDto other = (BeneficiariosDto) obj;
        return Objects.equals(this.benId, other.benId);
    }

    /**
     * @return the benParentescoStr
     */
    public String getBenParentescoStr() {
        return benParentescoStr;
    }

    /**
     * @param benParentescoStr the benParentescoStr to set
     */
    public void setBenParentescoStr(String benParentescoStr) {
        this.benParentescoStr = benParentescoStr;
    }

    /**
     * @return the parentesco
     */
    public ParentescoBen getParentesco() {
        return parentesco;
    }

    /**
     * @param parentesco the parentesco to set
     */
    public void setParentesco(ParentescoBen parentesco) {
        this.parentesco = parentesco;
    }

}
