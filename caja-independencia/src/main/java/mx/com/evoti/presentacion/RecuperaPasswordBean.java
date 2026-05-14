package mx.com.evoti.presentacion;

import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import mx.com.evoti.bo.PasswordRecoveryBo;
import mx.com.evoti.bo.administrador.algoritmopagos.BusquedaEmpleadoBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.EmpresasDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "recuperaPasswordBean")
@ViewScoped
public class RecuperaPasswordBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RecuperaPasswordBean.class);

    private Integer claveEmpleado;
    private EmpresasDto empresa;
    private String correo;
    private String token;
    private String nuevoPassword;
    private String confirmaPassword;
    private boolean tokenValido;
    private boolean solicitudEnviada;
    private boolean passwordActualizado;

    private List<EmpresasDto> empresas;

    private final PasswordRecoveryBo recoveryBo;
    private final BusquedaEmpleadoBo busquedaEmpleadoBo;

    public RecuperaPasswordBean() {
        recoveryBo = new PasswordRecoveryBo();
        busquedaEmpleadoBo = new BusquedaEmpleadoBo();
    }

    public void initSolicitud() {
        if (empresas == null) {
            try {
                empresas = busquedaEmpleadoBo.getEmpresasDto();
            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
                muestraMensajeError("No fue posible cargar las empresas.", "", null);
            }
        }
    }

    public void initRestablecimiento() {
        try {
            if (passwordActualizado) {
                return;
            }
            if (token == null || token.trim().isEmpty()) {
                tokenValido = false;
                return;
            }
            tokenValido = recoveryBo.tokenValido(token);
        } catch (BusinessException ex) {
            tokenValido = false;
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void solicitarRecuperacion() {
        try {
            Integer empresaId = empresa == null ? null : empresa.getEmpId();
            recoveryBo.solicitaRecuperacion(claveEmpleado, empresaId, correo,
                    getResetBaseUrl(), getRemoteAddr(), getUserAgent());
            solicitudEnviada = true;
            muestraMensajeGen("Solicitud recibida",
                    "Si los datos son correctos, recibiras un correo con instrucciones para restablecer tu password.",
                    null, FacesMessage.SEVERITY_INFO);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
            muestraMensajeError(ex.getMessage(), "", null);
        }
    }

    public String restablecerPassword() {
        try {
            recoveryBo.restablecePassword(token, nuevoPassword, confirmaPassword);
            passwordActualizado = true;
            tokenValido = false;
            nuevoPassword = null;
            confirmaPassword = null;
            muestraMensajeGen("Password actualizado",
                    "Tu password fue actualizado correctamente.<br/><br/>Ingresa con tu nuevo password.",
                    null, FacesMessage.SEVERITY_INFO);
            return null;
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
            muestraMensajeError(ex.getMessage(), "", null);
            return null;
        }
    }

    private String getResetBaseUrl() {
        HttpServletRequest request = getRequest();
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if (!defaultPort) {
            url.append(":").append(port);
        }
        url.append(request.getContextPath()).append("/restablecer-password.xhtml");
        return url.toString();
    }

    private String getRemoteAddr() {
        return getRequest().getRemoteAddr();
    }

    private String getUserAgent() {
        return getRequest().getHeader("User-Agent");
    }

    private HttpServletRequest getRequest() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        return (HttpServletRequest) ec.getRequest();
    }

    public Integer getClaveEmpleado() {
        return claveEmpleado;
    }

    public void setClaveEmpleado(Integer claveEmpleado) {
        this.claveEmpleado = claveEmpleado;
    }

    public EmpresasDto getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresasDto empresa) {
        this.empresa = empresa;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNuevoPassword() {
        return nuevoPassword;
    }

    public void setNuevoPassword(String nuevoPassword) {
        this.nuevoPassword = nuevoPassword;
    }

    public String getConfirmaPassword() {
        return confirmaPassword;
    }

    public void setConfirmaPassword(String confirmaPassword) {
        this.confirmaPassword = confirmaPassword;
    }

    public boolean isTokenValido() {
        return tokenValido;
    }

    public void setTokenValido(boolean tokenValido) {
        this.tokenValido = tokenValido;
    }

    public boolean isSolicitudEnviada() {
        return solicitudEnviada;
    }

    public void setSolicitudEnviada(boolean solicitudEnviada) {
        this.solicitudEnviada = solicitudEnviada;
    }

    public boolean isPasswordActualizado() {
        return passwordActualizado;
    }

    public void setPasswordActualizado(boolean passwordActualizado) {
        this.passwordActualizado = passwordActualizado;
    }

    public List<EmpresasDto> getEmpresas() {
        return empresas;
    }

    public void setEmpresas(List<EmpresasDto> empresas) {
        this.empresas = empresas;
    }
}
