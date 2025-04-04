/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.bitacora;

import java.io.Serializable;
import java.math.BigInteger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.bitacora.BitacoraBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "bitaBean" )
@ViewScoped
public class BitacoraBean extends BaseBean implements Serializable{
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BitacoraBean.class);
    private static final long serialVersionUID = 5188478880986645102L;
    
    private BitacoraBo bo;
    private DetalleSolicitudBo detSolBo;
    
    private String observacion;
    private Integer tipoTransaccion;
    private Long referencia;
    
    /**
     * BLOQUE DE CAMPOS QUE SE DEBEN LLENAR CUANDO SE VA A CANCELAR UNA SOLICITUD
     */
    private Long subreferencia;
    private Double montoSolicitado;
    private Integer catorcenas;
    private Double pagoCredito;
    private Usuarios usuario;
    private UsuarioDto usuarioActor;
    
    public BitacoraBean() {
        bo = new BitacoraBo();
        detSolBo = new DetalleSolicitudBo();
    }

    /**
     * Guarda un registro en la tabla Bitacora, todos los valores que se mandan como 
     * parametros al Bo ya están inicializados desde el bean origen.
     * Dependiendo del tipo de transaccion de la bitacora, se mandarán llamar metodos complementarios
     * como rechazarSolicitud()
     */
    public void guardaRegBitacora(){
        try{
        
            bo.saveBitacora(referencia, tipoTransaccion, usuarioActor.getId(), subreferencia, observacion);
         
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void limpiaCampos(){
        referencia = null;
            tipoTransaccion = null;
            usuario = null;
            subreferencia = null;
            observacion = null;
    }
    
    /**
     * Metodo que cancela una solicitud, puede ser llamado desde la pantalla de validar solicitudes,
     * en la parte de documentos, avales o desde fondeos.
     */
     public void rechazarSolicitud(){
         try {
            
            
            detSolBo.updtEstatusSolicitud(BigInteger.valueOf(referencia), 7,null);
            
            detSolBo.enviaCorreoValSol(usuario, montoSolicitado, catorcenas, pagoCredito, 2, observacion);
           
            guardaRegBitacora();
             muestraMensajeExito("La solicitud "+referencia+" fue cancelada.","",null);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    
    

    /**
     * @return the observacion
     */
    public String getObservacion() {
        return observacion;
    }

    /**
     * @param observacion the observacion to set
     */
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

  

    /**
     * @return the tipoTransaccion
     */
    public Integer getTipoTransaccion() {
        return tipoTransaccion;
    }

    /**
     * @param tipoTransaccion the tipoTransaccion to set
     */
    public void setTipoTransaccion(Integer tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion;
    }

    /**
     * @return the referencia
     */
    public Long getReferencia() {
        return referencia;
    }

    /**
     * @param referencia the referencia to set
     */
    public void setReferencia(Long referencia) {
        this.referencia = referencia;
    }

    /**
     * @return the subreferencia
     */
    public Long getSubreferencia() {
        return subreferencia;
    }

    /**
     * @param subreferencia the subreferencia to set
     */
    public void setSubreferencia(Long subreferencia) {
        this.subreferencia = subreferencia;
    }

    /**
     * @return the montoSolicitado
     */
    public Double getMontoSolicitado() {
        return montoSolicitado;
    }

    /**
     * @param montoSolicitado the montoSolicitado to set
     */
    public void setMontoSolicitado(Double montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    /**
     * @return the catorcenas
     */
    public Integer getCatorcenas() {
        return catorcenas;
    }

    /**
     * @param catorcenas the catorcenas to set
     */
    public void setCatorcenas(Integer catorcenas) {
        this.catorcenas = catorcenas;
    }

    /**
     * @return the pagoCredito
     */
    public Double getPagoCredito() {
        return pagoCredito;
    }

    /**
     * @param pagoCredito the pagoCredito to set
     */
    public void setPagoCredito(Double pagoCredito) {
        this.pagoCredito = pagoCredito;
    }

    /**
     * @return the usuario
     */
    public Usuarios getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the usuarioActor
     */
    public UsuarioDto getUsuarioActor() {
        return usuarioActor;
    }

    /**
     * @param usuarioActor the usuarioActor to set
     */
    public void setUsuarioActor(UsuarioDto usuarioActor) {
        this.usuarioActor = usuarioActor;
    }

}
