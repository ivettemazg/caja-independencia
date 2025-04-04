/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.banco;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.bancos.ReporteBancosDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "repBancosBean")
@ViewScoped
public class ReporteBancosBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteBancosBean.class);
    private static final long serialVersionUID = 5188478880986645102L;

    private BancosBo banBo;

    private Date fechaInicio;
    private Date fechaFin;

    private List<ReporteBancosDto> lstBancos;
    private List<ReporteBancosDto> lstBancosFiltradas;

    private Double totalMonto;
    private Double totalSaldo;

    public ReporteBancosBean() {
        banBo = new BancosBo();
    }

    public void init() {

    }

    public void cargaReporte() {
        try {
            lstBancos = banBo.getReporteBancos(fechaInicio, fechaFin);

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
   
    public void calculaSummary(Object obj){
        totalMonto =0.0;
        totalSaldo =0.0;
        
        
        if(lstBancosFiltradas!=null){
            if(!lstBancosFiltradas.isEmpty()){
            lstBancosFiltradas.stream().forEach(ban -> {
            totalMonto+=Util.round(ban.getEcMonto());
            totalSaldo+=Util.round(ban.getSaldoPendienteAjuste());
            });
            }
        }else{
            lstBancos.stream().forEach(ban -> {
            totalMonto+=Util.round(ban.getEcMonto());
            totalSaldo+=Util.round(ban.getSaldoPendienteAjuste());
            });
        }
        
    }
   

    /**
     * @return the fechaInicio
     */
    public Date getFechaInicio() {
        return fechaInicio;
    }

    /**
     * @param fechaInicio the fechaInicio to set
     */
    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /**
     * @return the fechaFin
     */
    public Date getFechaFin() {
        return fechaFin;
    }

    /**
     * @param fechaFin the fechaFin to set
     */
    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    /**
     * @return the lstBancos
     */
    public List<ReporteBancosDto> getLstBancos() {
        return lstBancos;
    }

    /**
     * @param lstBancos the lstBancos to set
     */
    public void setLstBancos(List<ReporteBancosDto> lstBancos) {
        this.lstBancos = lstBancos;
    }

    /**
     * @return the lstBancosFiltradas
     */
    public List<ReporteBancosDto> getLstBancosFiltradas() {
        return lstBancosFiltradas;
    }

    /**
     * @param lstBancosFiltradas the lstBancosFiltradas to set
     */
    public void setLstBancosFiltradas(List<ReporteBancosDto> lstBancosFiltradas) {
        this.lstBancosFiltradas = lstBancosFiltradas;
    }

    /**
     * @return the totalMonto
     */
    public Double getTotalMonto() {
        return totalMonto;
    }

    /**
     * @param totalMonto the totalMonto to set
     */
    public void setTotalMonto(Double totalMonto) {
        this.totalMonto = totalMonto;
    }

    /**
     * @return the totalSaldo
     */
    public Double getTotalSaldo() {
        return totalSaldo;
    }

    /**
     * @param totalSaldo the totalSaldo to set
     */
    public void setTotalSaldo(Double totalSaldo) {
        this.totalSaldo = totalSaldo;
    }

   

}
