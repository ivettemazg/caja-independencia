/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.usuariocomun;

import java.io.Serializable;
import java.math.BigInteger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.slf4j.LoggerFactory;

import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.presentacion.BaseBean;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "detSolBean")
@ViewScoped
public class DetalleSolicitudBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DetalleSolicitudBean.class);
    private static final long serialVersionUID = 4227664366864891143L;

    @ManagedProperty("#{dbancariosBean}")
    private DatosBancariosBean dBancariosBean;
    @ManagedProperty("#{avaSolBean}")
    private SolicitudAvalesBean avalesSolBean;
    @ManagedProperty("#{perfilBean}")
    private PerfilBean perfilBean;
    @ManagedProperty("#{doctosSolBean}")
    private DoctosSolBean doctosSolBean;

    private DetalleSolicitudBo detSolBo;

    private Solicitudes solicitud;
    private UsuarioDto usuario;

    public void init() {
        try {
            LOGGER.info("Iniciando DetalleSolicitudBean");

            System.out.println("Prueba de log en consola");
            LOGGER.debug("Prueba de log con SLF4J");
            LOGGER.info("Esto debería verse en consola y en el archivo de log");

            detSolBo = new DetalleSolicitudBo();
            
            BigInteger idSolicitud = (BigInteger) super.getSession().getAttribute("idSolicitud");
            usuario = (UsuarioDto) super.getSession().getAttribute("usuario");
            
            solicitud = detSolBo.getSolById(idSolicitud);   
            
            perfilBean.setSolicitud(solicitud);
            perfilBean.setUsuarioId(usuario.getId());
            perfilBean.setOrigen(2);

            dBancariosBean.setSolicitud(solicitud);
            dBancariosBean.setUsuario(usuario);
            
            avalesSolBean.setSolicitud(solicitud);
            avalesSolBean.setUsuario(usuario);
                   
            //Agrega las imagenes al bean de documentos
            doctosSolBean.setSolicitud(solicitud);
            doctosSolBean.setUsuarioSolicitante(usuario);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }
    
   

    /**
     * @return the dBancariosBean
     */
    public DatosBancariosBean getdBancariosBean() {
        return dBancariosBean;
    }

    /**
     * @param dBancariosBean the dBancariosBean to set
     */
    public void setdBancariosBean(DatosBancariosBean dBancariosBean) {
        this.dBancariosBean = dBancariosBean;
    }

    /**
     * @return the perfilBean
     */
    public PerfilBean getPerfilBean() {
        return perfilBean;
    }

    /**
     * @param perfilBean the perfilBean to set
     */
    public void setPerfilBean(PerfilBean perfilBean) {
        this.perfilBean = perfilBean;
    }

    /**
     * @return the doctosSolBean
     */
    public DoctosSolBean getDoctosSolBean() {
        return doctosSolBean;
    }

    /**
     * @param doctosSolBean the doctosSolBean to set
     */
    public void setDoctosSolBean(DoctosSolBean doctosSolBean) {
        this.doctosSolBean = doctosSolBean;
    }

    /**
     * @return the avalesSolBean
     */
    public SolicitudAvalesBean getAvalesSolBean() {
        return avalesSolBean;
    }

    /**
     * @param avalesSolBean the avalesSolBean to set
     */
    public void setAvalesSolBean(SolicitudAvalesBean avalesSolBean) {
        this.avalesSolBean = avalesSolBean;
    }

}
