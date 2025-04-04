/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author ivett
 */
public class TransaccionUsuarioDto implements Serializable{

    

    private int tranId;
     private Integer tranIdUsuario;
     private Integer tranClaveUsuario;
     private Date tranFecha;
     private Integer tranTipoTran;
     private Integer tranIdSistema;
     private String ttranDescripcion;
     private String folioCredito;
     private String empresaUserOfTran;
     private Integer userOfTran;
     private Double monto;
    
    /**
     * @return the tranId
     */
    public int getTranId() {
        return tranId;
    }

    /**
     * @param tranId the tranId to set
     */
    public void setTranId(int tranId) {
        this.tranId = tranId;
    }

    /**
     * @return the tranIdUsuario
     */
    public Integer getTranIdUsuario() {
        return tranIdUsuario;
    }

    /**
     * @param tranIdUsuario the tranIdUsuario to set
     */
    public void setTranIdUsuario(Integer tranIdUsuario) {
        this.tranIdUsuario = tranIdUsuario;
    }

    /**
     * @return the tranFecha
     */
    public Date getTranFecha() {
        return tranFecha;
    }

    /**
     * @param tranFecha the tranFecha to set
     */
    public void setTranFecha(Date tranFecha) {
        this.tranFecha = tranFecha;
    }

    /**
     * @return the tranTipoTran
     */
    public Integer getTranTipoTran() {
        return tranTipoTran;
    }

    /**
     * @param tranTipoTran the tranTipoTran to set
     */
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

    /**
     * @return the ttranDescripcion
     */
    public String getTtranDescripcion() {
        return ttranDescripcion;
    }

    /**
     * @param ttranDescripcion the ttranDescripcion to set
     */
    public void setTtranDescripcion(String ttranDescripcion) {
        this.ttranDescripcion = ttranDescripcion;
    }

    /**
     * @return the userOfTran
     */
    public Integer getUserOfTran() {
        return userOfTran;
    }

    /**
     * @param userOfTran the userOfTran to set
     */
    public void setUserOfTran(Integer userOfTran) {
        this.userOfTran = userOfTran;
    }

    /**
     * @return the tranClaveUsuario
     */
    public Integer getTranClaveUsuario() {
        return tranClaveUsuario;
    }

    /**
     * @param tranClaveUsuario the tranClaveUsuario to set
     */
    public void setTranClaveUsuario(Integer tranClaveUsuario) {
        this.tranClaveUsuario = tranClaveUsuario;
    }
     
    
    /**
     * @return the monto
     */
    public Double getMonto() {
        return monto;
    }

    /**
     * @param monto the monto to set
     */
    public void setMonto(Double monto) {
        this.monto = monto;
    }

    /**
     * @return the folioCredito
     */
    public String getFolioCredito() {
        return folioCredito;
    }

    /**
     * @param folioCredito the folioCredito to set
     */
    public void setFolioCredito(String folioCredito) {
        this.folioCredito = folioCredito;
    }

    /**
     * @return the empresaUserOfTran
     */
    public String getEmpresaUserOfTran() {
        return empresaUserOfTran;
    }

    /**
     * @param empresaUserOfTran the empresaUserOfTran to set
     */
    public void setEmpresaUserOfTran(String empresaUserOfTran) {
        this.empresaUserOfTran = empresaUserOfTran;
    }
}
