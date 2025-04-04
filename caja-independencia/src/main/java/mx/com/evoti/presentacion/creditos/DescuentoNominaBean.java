/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.creditos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.DescuentoNominaBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.DescuentoNominaDto;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author NeOleon
 */
@ManagedBean(name = "descuentoNominaBean")
@ViewScoped
public class DescuentoNominaBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5216692439496179076L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DescuentoNominaBean.class);

    private final DescuentoNominaBo descNomBo;
    private List<DescuentoNominaDto> descuentoNominaAMX;
    private List<DescuentoNominaDto> descuentoNominaOtras;
    private List<DescuentoNominaDto> descuentoNominaFAAG;
    private Boolean rdrTblFormatoNumero;
    private Boolean rdrTblOtras;

    private Date fechaCatorcena;
    private Integer empresa;

    public DescuentoNominaBean() {
        descNomBo = new DescuentoNominaBo();
        rdrTblFormatoNumero = Boolean.FALSE;
        rdrTblOtras = Boolean.FALSE;
    }

    public void generaDescuentoNominaCatorcenal() {
        System.out.println("dentro de generaDescuentoNominaCatorcenal");
        try {
            if (empresa == 1 || empresa == 3 || empresa == 4) {
                descuentoNominaAMX = descNomBo.obtieneListaDescuentosNomina(fechaCatorcena, empresa);
                rdrTblFormatoNumero = true;
                rdrTblOtras = false;
                 System.out.println(descuentoNominaAMX.size());
            } else {
                descuentoNominaOtras = descNomBo.obtieneListaDescuentosNominaOtras(fechaCatorcena, empresa);
                rdrTblFormatoNumero = false;
                rdrTblOtras = true;
            }
           
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void generaDescuentoNominaFAAG() {
        System.out.println("hola");
        try {

            descuentoNominaFAAG = descNomBo.obtieneListaDescuentosNominaFAAG(fechaCatorcena);

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the fechaCatorcena
     */
    public Date getFechaCatorcena() {
        return fechaCatorcena;
    }

    /**
     * @param fechaCatorcena the fechaCatorcena to set
     */
    public void setFechaCatorcena(Date fechaCatorcena) {
        this.fechaCatorcena = fechaCatorcena;
    }

    /**
     * @return the empresa
     */
    public Integer getEmpresa() {
        return empresa;
    }

    /**
     * @param empresa the empresa to set
     */
    public void setEmpresa(Integer empresa) {
        this.empresa = empresa;
    }

    /**
     * @return the descuentoNominaAMX
     */
    public List<DescuentoNominaDto> getDescuentoNominaAMX() {
        return descuentoNominaAMX;
    }

    public List<DescuentoNominaDto> getDescuentoNominaOtras() {
        return descuentoNominaOtras;
    }

    /**
     * @param descuentoNominaAMX the descuentoNominaAMX to set
     */
    public void setDescuentoNominaAMX(List<DescuentoNominaDto> descuentoNominaAMX) {
        this.descuentoNominaAMX = descuentoNominaAMX;
    }

    public void setDescuentoNominaOtras(List<DescuentoNominaDto> descuentoNominaOtras) {
        this.descuentoNominaOtras = descuentoNominaOtras;
    }

    /**
     * @return the rdrTblAmx
     */
    public Boolean getRdrTblFormatoNumero() {
        return rdrTblFormatoNumero;
    }

    /**
     * @param rdrTblFormatoNumero the rdrTblFormatoNumero to set
     */
    public void setRdrTblFormatoNumero(Boolean rdrTblFormatoNumero) {
        this.rdrTblFormatoNumero = rdrTblFormatoNumero;
    }

    /**
     * @return the rdrTblOtras
     */
    public Boolean getRdrTblOtras() {
        return rdrTblOtras;
    }

    /**
     * @param rdrTblOtras the rdrTblOtras to set
     */
    public void setRdrTblOtras(Boolean rdrTblOtras) {
        this.rdrTblOtras = rdrTblOtras;
    }

    /**
     * @return the descuentoNominaFAAG
     */
    public List<DescuentoNominaDto> getDescuentoNominaFAAG() {
        return descuentoNominaFAAG;
    }

    /**
     * @param descuentoNominaFAAG the descuentoNominaFAAG to set
     */
    public void setDescuentoNominaFAAG(List<DescuentoNominaDto> descuentoNominaFAAG) {
        this.descuentoNominaFAAG = descuentoNominaFAAG;
    }

}
