/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.common;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.dao.ReporteUsuariosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.ReporteUsuariosDto;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author NeOleon
 */
@ManagedBean(name = "reporteUsuariosBean")
@ViewScoped
public class ReporteUsuariosBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5216692439496179076L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteUsuariosBean.class);

    private List<ReporteUsuariosDto> reporteUsuarios;

    public void init() {
        try {
            System.out.println("init");
            ReporteUsuariosDao repUsuariosDao = new ReporteUsuariosDao();
            this.reporteUsuarios = repUsuariosDao.getReporteUsuarios();
        } catch (IntegracionException ex) {
            Logger.getLogger(ReporteUsuariosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the reporteUsuarios
     */
    public List<ReporteUsuariosDto> getReporteUsuarios() {
        return reporteUsuarios;
    }

    /**
     * @param reporteUsuarios the reporteUsuarios to set
     */
    public void setReporteUsuarios(List<ReporteUsuariosDto> reporteUsuarios) {
        this.reporteUsuarios = reporteUsuarios;
    }

}
