/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.finiquito;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpSession;
import mx.com.evoti.bo.administrador.finiquito.HistorialBajasBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.finiquito.UsuarioBajaDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.NavigationBean;
import mx.com.evoti.util.Constantes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "ahorrosXDevBean")
@ViewScoped
public class AhorrosXDevolverBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AhorrosXDevolverBean.class);
    private static final long serialVersionUID = -2778736954390098395L;

    private HistorialBajasBo bo;
    private List<UsuarioBajaDto> bajas;
    private UsuarioBajaDto bajaSelected;

    @ManagedProperty("#{navigationController}")
    private NavigationBean navigationBean;

    public AhorrosXDevolverBean() {
        this.bo = new HistorialBajasBo();
    }

    public void init() {
        try {
            bajas = bo.getBajaUsuarios(Constantes.BAJA_AHORROSXDEVOLVER);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void goToFiniquito() {

        HttpSession session = super.getSession();
        session.setAttribute("usuBajaId", bajaSelected.getUsuId());
        navigationBean.goToFiniquito();

    }

    /**
     * @return the bajas
     */
    public List<UsuarioBajaDto> getBajas() {
        return bajas;
    }

    /**
     * @param bajas the bajas to set
     */
    public void setBajas(List<UsuarioBajaDto> bajas) {
        this.bajas = bajas;
    }

    /**
     * @return the bajaSelected
     */
    public UsuarioBajaDto getBajaSelected() {
        return bajaSelected;
    }

    /**
     * @param bajaSelected the bajaSelected to set
     */
    public void setBajaSelected(UsuarioBajaDto bajaSelected) {
        this.bajaSelected = bajaSelected;
    }

    /**
     * @return the navigationBean
     */
    public NavigationBean getNavigationBean() {
        return navigationBean;
    }

    /**
     * @param navigationBean the navigationBean to set
     */
    public void setNavigationBean(NavigationBean navigationBean) {
        this.navigationBean = navigationBean;
    }

}
