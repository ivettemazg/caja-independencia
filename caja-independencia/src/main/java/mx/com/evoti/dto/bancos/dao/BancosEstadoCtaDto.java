/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.bancos.dao;

import java.util.Date;

/**
 *
 * @author Venette
 */
public class BancosEstadoCtaDto {
    
     private Integer banId;
     private Integer banConcepto;
     private Integer banIdConceptoSistema;
     private Integer banAjustado;
     private Double banMonto;
     private Integer banEmpresa;
     private Date banFechatransaccion;
     private String conceptoBanco;
     private String banEmpresaStri;
      private Integer ecId;
      private Integer ecPadre;
     private Integer ecConcepto;
     private Double ecMonto;
     private Integer ecEmpresa;
     private Date ecFechatransaccion;
     private Integer ecAjustado;
     private String ecDescripcion;
     private String conceptoEdoCta;
     private String edoCtaEmpresa;
     private Date becFechaTransaccion;

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
    public Double getBanMonto() {
        return banMonto;
    }

    /**
     * @param banMonto the banMonto to set
     */
    public void setBanMonto(Double banMonto) {
        this.banMonto = banMonto;
    }

    /**
     * @return the banEmpresa
     */
    public Integer getBanEmpresa() {
        return banEmpresa;
    }

    /**
     * @param banEmpresa the banEmpresa to set
     */
    public void setBanEmpresa(Integer banEmpresa) {
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
     * @return the conceptoBanco
     */
    public String getConceptoBanco() {
        return conceptoBanco;
    }

    /**
     * @param conceptoBanco the conceptoBanco to set
     */
    public void setConceptoBanco(String conceptoBanco) {
        this.conceptoBanco = conceptoBanco;
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
     * @return the conceptoEdoCta
     */
    public String getConceptoEdoCta() {
        return conceptoEdoCta;
    }

    /**
     * @param conceptoEdoCta the conceptoEdoCta to set
     */
    public void setConceptoEdoCta(String conceptoEdoCta) {
        this.conceptoEdoCta = conceptoEdoCta;
    }

    /**
     * @return the becFechaTransaccion
     */
    public Date getBecFechaTransaccion() {
        return becFechaTransaccion;
    }

    /**
     * @param becFechaTransaccion the becFechaTransaccion to set
     */
    public void setBecFechaTransaccion(Date becFechaTransaccion) {
        this.becFechaTransaccion = becFechaTransaccion;
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
    
}
