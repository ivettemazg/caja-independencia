package mx.com.evoti.hibernate.pojos;
// Generated 13/07/2019 11:50:34 PM by Hibernate Tools 4.3.1


import java.util.Date;

/**
 * RegistroTransaccion generated by hbm2java
 */
public class RegistroTransaccion  implements java.io.Serializable {

     private int tranId;
     private Integer tranIdUsuario;
     private Date tranFecha;
     private Integer tranTipoTran;
     private Integer tranIdSistema;

    public RegistroTransaccion() {
    }

	
    public RegistroTransaccion(int tranId) {
        this.tranId = tranId;
    }
    public RegistroTransaccion(int tranId, Integer tranIdUsuario, Date tranFecha, Integer tranTipoTran) {
       this.tranId = tranId;
       this.tranIdUsuario = tranIdUsuario;
       this.tranFecha = tranFecha;
       this.tranTipoTran = tranTipoTran;
    }
   
    public int getTranId() {
        return this.tranId;
    }
    
    public void setTranId(int tranId) {
        this.tranId = tranId;
    }
    public Integer getTranIdUsuario() {
        return this.tranIdUsuario;
    }
    
    public void setTranIdUsuario(Integer tranIdUsuario) {
        this.tranIdUsuario = tranIdUsuario;
    }
    public Date getTranFecha() {
        return this.tranFecha;
    }
    
    public void setTranFecha(Date tranFecha) {
        this.tranFecha = tranFecha;
    }
    public Integer getTranTipoTran() {
        return this.tranTipoTran;
    }
    
    public void setTranTipoTran(Integer tranTipoTran) {
        this.tranTipoTran = tranTipoTran;
    }

 /**
     * @return the tranIdSistema
     */
    public Integer getTranIdSistema() {
        return tranIdSistema;
    }

    /**
     * @param tranIdSistema the tranIdSistema to set
     */
    public void setTranIdSistema(Integer tranIdSistema) {
        this.tranIdSistema = tranIdSistema;
    }


}


