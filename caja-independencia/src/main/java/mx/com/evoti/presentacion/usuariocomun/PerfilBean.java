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
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.BeneficiariosBo;
import mx.com.evoti.bo.usuarioComun.PerfilBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.usuariocomun.BeneficiariosDto;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "perfilBean")
@ViewScoped
public class PerfilBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 4087011969669552567L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PerfilBean.class);

    private final PerfilBo bo;
    private BeneficiariosBo beneBo;
    private Usuarios usuario;
    private List<BeneficiariosDto> beneficiarios;
    private Integer usuarioId;
    private Integer origen;

    private Solicitudes solicitud;
    private boolean rdrBtn;
    private boolean soloLectura;

    public PerfilBean() {
        bo = new PerfilBo();
    }

    public void init() {
        if(super.validateUser()){
             UsuarioDto usrDto;

            /**
             * Cuando el origen es nulo, significa que viene del login, por lo tanto
             * se le asina el origen 1 y usuario se llena desde sesion
             */
            if (origen == null) {
                beneBo = new BeneficiariosBo();
                origen = 1;
                usrDto = (UsuarioDto) this.getSession().getAttribute("usuario");
                usuarioId = usrDto.getId();

            }

            try {
                usuario = bo.getUsuarioById(usuarioId);

                if (usuario == null) {
                    usuario = new Usuarios();
                }


                if (null != origen) /**
                 * Solo aplican estas validaciones cuando la pantalla de perfil esta
                 * siendo abierta desde el Detalle de la solicitud (cuando el
                 * usuario está visualizando la solicitud, ya sea desde la creación
                 * de la misma o desde la pantalla "Mis Solicitudes"
                 */ switch (origen) {
                    case 2:
                        if (solicitud.getSolicitudEstatus().getSolEstId() != 1) {
                            rdrBtn = Boolean.FALSE;
                            soloLectura = Boolean.TRUE;
                        } else {
                            rdrBtn = Boolean.TRUE;
                            soloLectura = Boolean.FALSE;

                        }
                        /**
                         * Cuando la pantalla se abre desde reporte por empleado
                         * los datos serán de solo lectura
                         */ break;
                    case 3:
                         rdrBtn = Boolean.FALSE;
                         soloLectura = Boolean.TRUE;
                        break;
                    default:
                        rdrBtn = Boolean.TRUE;
                        soloLectura = Boolean.FALSE;
                        break;
                }

            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
       
    }

    /**
     * Llama al dao para actualizar la información del usuario, puede venir de
     * Solicitud de Crédito o de Mi Perfil
     */
    public void actualizaPerfil() {
        LOGGER.debug("Dentro de actualizarPerfil()");
        try {
            
            if(origen==1){
                actualizaPerfilMiPerfil();
            }else{
            
                bo.updtUsuario(usuario);
            }

            UsuarioDto usrDto = bo.getUsuarioActualizado(usuario.getUsuId());
            if(usrDto != null){
                this.getSession().setAttribute("usuario", usrDto);
            }
            
            
        } catch (BusinessException ex) {
            LOGGER.error("Error al actualizar el perfil del usuario", ex);
        }
    }

    /**
     * Actualiza el perfil cuando el origen es Mi Perfil
     */
    public void actualizaPerfilMiPerfil() {
        try {
              beneficiarios = beneBo.getBeneficiariosByEmpleado(usuarioId);
            
            /**
             * Cuando beneficiarios no está vacío significa que ya pasó las validaciones de pantalla
             * en perfil y puede guardar correctamente todo, porque ya añadió incluso sus beneficiarios
             */
            if (!beneficiarios.isEmpty()) {
                usuario.setUsuPrimeravez(0);
                bo.updtUsuario(usuario);
                super.muestraMensajeExito("Los datos fueron actualizados correctamente", "", null);

            } else if (beneficiarios.isEmpty()) {
                muestraMensajeError("Debe dar de alta al menos 1 beneficiario", "", null);
            } 
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

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
     * @return the usuarioId
     */
    public Integer getUsuarioId() {
        return usuarioId;
    }

    /**
     * @param usuarioId the usuarioId to set
     */
    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    /**
     * @return the origen
     */
    public Integer getOrigen() {
        return origen;
    }

    /**
     * @param origen the origen to set
     */
    public void setOrigen(Integer origen) {
        this.origen = origen;
    }

}
