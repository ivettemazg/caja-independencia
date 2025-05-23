package mx.com.evoti.hibernate.pojos;
// Generated 16/01/2017 04:22:44 PM by Hibernate Tools 4.3.1


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * BitacoraTransacciones generated by hbm2java
 */
@Entity
@Table(name="bitacora_transacciones"
)
public class BitacoraTransacciones  implements java.io.Serializable {


     private int bitraId;
     private String bitraNombre;
     private String bitraDescripcion;

    public BitacoraTransacciones() {
    }

    public BitacoraTransacciones(int bitraId, String bitraNombre, String bitraDescripcion) {
       this.bitraId = bitraId;
       this.bitraNombre = bitraNombre;
       this.bitraDescripcion = bitraDescripcion;
    }
   
     @Id 

    
    @Column(name="bitra_id", unique=true, nullable=false)
    public int getBitraId() {
        return this.bitraId;
    }
    
    public void setBitraId(int bitraId) {
        this.bitraId = bitraId;
    }

    
    @Column(name="bitra_nombre", nullable=false, length=45)
    public String getBitraNombre() {
        return this.bitraNombre;
    }
    
    public void setBitraNombre(String bitraNombre) {
        this.bitraNombre = bitraNombre;
    }

    
    @Column(name="bitra_descripcion", nullable=false, length=70)
    public String getBitraDescripcion() {
        return this.bitraDescripcion;
    }
    
    public void setBitraDescripcion(String bitraDescripcion) {
        this.bitraDescripcion = bitraDescripcion;
    }




}


