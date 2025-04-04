/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.usuariocomun;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.common.AmortizacionBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "misCredBean")
@ViewScoped
public class MisCreditosBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5492268424613416965L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MisCreditosBean.class);

    private CreditosBo creBo;

    private List<CreditoDto> creditos;
    private CreditoDto creditoSelec;
    private List<AmortizacionDto> amortizacion;
    private UsuarioDto usuarioDto;

    public MisCreditosBean() {
        creBo = new CreditosBo();
    }

    public void init() {
        if(super.validateUser()){
            try {
                usuarioDto = super.getUsuarioSesion();
                creditos = creBo.getCreditosXIdUsr(usuarioDto.getId());

            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
         }
    }

    public void verDetalle() {
        try {
            this.amortizacion = creBo.getAmortizacionXCredito(creditoSelec.getCreId());
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the creditos
     */
    public List<CreditoDto> getCreditos() {
        return creditos;
    }

    /**
     * @param creditos the creditos to set
     */
    public void setCreditos(List<CreditoDto> creditos) {
        this.creditos = creditos;
    }

    /**
     * @return the creditoSelec
     */
    public CreditoDto getCreditoSelec() {
        return creditoSelec;
    }

    /**
     * @param creditoSelec the creditoSelec to set
     */
    public void setCreditoSelec(CreditoDto creditoSelec) {
        this.creditoSelec = creditoSelec;
    }

    /**
     * @return the amortizacion
     */
    public List<AmortizacionDto> getAmortizacion() {
        return amortizacion;
    }

    /**
     * @param amortizacion the amortizacion to set
     */
    public void setAmortizacion(List<AmortizacionDto> amortizacion) {
        this.amortizacion = amortizacion;
    }

}
