/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.usuariocomun;

import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.usuariocomun.DatosBancariosDto;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "dbancariosBean")
@ViewScoped
public class DatosBancariosBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DatosBancariosBean.class);
    private static final long serialVersionUID = 5488390430495492705L;

    private DetalleSolicitudBo detSolBo;

    private DatosBancariosDto dbancarios;
    private Solicitudes solicitud;
    private Integer estatusDB;
    private UsuarioDto usuario;

    private boolean disabledBtn;
    private boolean soloLectura;
    private boolean rdrBtn;

    public void init() {
        detSolBo = new DetalleSolicitudBo();

        if (dbancarios == null) {
            dbancarios = new DatosBancariosDto();
        }

        dbancarios.setBanco(solicitud.getSolBanco());
        dbancarios.setClabe(solicitud.getSolClabeInterbancaria());
        dbancarios.setCuenta(solicitud.getSolNumeroCuenta());
        dbancarios.setNombreTarjetaHabiente(solicitud.getSolNombreTarjetahabiente());

        /**
         * Si el estatus db es diferente de 1 el boton se inicializa deshabilitado
         */
        disabledBtn = solicitud.getSolEstatusDb() != 1 ? Boolean.TRUE : Boolean.FALSE;
        soloLectura = solicitud.getSolEstatusDb() != 1 ? Boolean.TRUE : Boolean.FALSE;

        rdrBtn = (solicitud.getSolicitudEstatus().getSolEstId() != 1 && 
                solicitud.getSolicitudEstatus().getSolEstId() != 9 ) ? Boolean.FALSE : Boolean.TRUE;

    }

    public void updtDatosBancarios() {
        try {

            solicitud.setSolBanco(dbancarios.getBanco());
            solicitud.setSolClabeInterbancaria(dbancarios.getClabe());
            solicitud.setSolNumeroCuenta(dbancarios.getCuenta());
            solicitud.setSolNombreTarjetahabiente(dbancarios.getNombreTarjetaHabiente());
            solicitud.setSolEstatusDb(2);

            //Al actualizar el estatus bancario, se actualiza tambien la solicitud
            detSolBo.updtDatosBancarios(solicitud);

            String message = detSolBo.updtSolicitudEstatus(solicitud.getSolId(), usuario.getNombreCompleto(), usuario.getCorreo());
            if (message != null) {
                super.muestraMensajeDialog(message, "",FacesMessage.SEVERITY_INFO);
            }
            this.disabledBtn = Boolean.TRUE;
            this.soloLectura = Boolean.TRUE;

        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al aprobar la solicitud", "", "growl");
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the dbancarios
     */
    public DatosBancariosDto getDbancarios() {
        return dbancarios;
    }

    /**
     * @param dbancarios the dbancarios to set
     */
    public void setDbancarios(DatosBancariosDto dbancarios) {
        this.dbancarios = dbancarios;
    }

    /**
     * @return the solicitud
     */
    public Solicitudes getSolicitud() {
        return solicitud;
    }

    /**
     * @param solicitud the solicitud to set
     */
    public void setSolicitud(Solicitudes solicitud) {
        this.solicitud = solicitud;
    }

    /**
     * @return the estatusDB
     */
    public Integer getEstatusDB() {
        return estatusDB;
    }

    /**
     * @param estatusDB the estatusDB to set
     */
    public void setEstatusDB(Integer estatusDB) {
        this.estatusDB = estatusDB;
    }

    /**
     * @return the disabledBtn
     */
    public boolean isDisabledBtn() {
        return disabledBtn;
    }

    /**
     * @param disabledBtn the disabledBtn to set
     */
    public void setDisabledBtn(boolean disabledBtn) {
        this.disabledBtn = disabledBtn;
    }

    /**
     * @return the soloLectura
     */
    public boolean isSoloLectura() {
        return soloLectura;
    }

    /**
     * @param soloLectura the soloLectura to set
     */
    public void setSoloLectura(boolean soloLectura) {
        this.soloLectura = soloLectura;
    }

    /**
     * @return the rdrBtn
     */
    public boolean isRdrBtn() {
        return rdrBtn;
    }

    /**
     * @param rdrBtn the rdrBtn to set
     */
    public void setRdrBtn(boolean rdrBtn) {
        this.rdrBtn = rdrBtn;
    }

    /**
     * @return the usuario
     */
    public UsuarioDto getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(UsuarioDto usuario) {
        this.usuario = usuario;
    }

}
