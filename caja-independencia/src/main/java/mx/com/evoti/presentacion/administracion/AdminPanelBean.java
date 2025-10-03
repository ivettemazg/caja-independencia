package mx.com.evoti.presentacion.administracion;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.administracion.AdminUsuariosBo;
import mx.com.evoti.bo.administracion.ConfiguracionBo;
import mx.com.evoti.bo.administrador.algoritmopagos.BusquedaEmpleadoBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.EmpresasDto;
import mx.com.evoti.hibernate.pojos.Configuracion;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.usuariocomun.PerfilBean;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "adminPanelBean")
@ViewScoped
public class AdminPanelBean extends BaseBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AdminPanelBean.class);

    // Tabs: Configuración de créditos
    private boolean faHabilitado;
    private boolean agHabilitado;

    // Busqueda/Edición de empleados
    private List<EmpresasDto> empresas;
    private EmpresasDto empresa;
    private Integer claveEmpleado;
    private Usuarios usuario; // seleccionado

    private boolean mostrarPerfil;
    private String nuevoPassword;

    private final ConfiguracionBo configuracionBo;
    private final BusquedaEmpleadoBo busquedaEmpleadoBo;
    private final AdminUsuariosBo adminUsuariosBo;

    @ManagedProperty("#{perfilBean}")
    private PerfilBean perfilBean;

    public AdminPanelBean() {
        configuracionBo = new ConfiguracionBo();
        busquedaEmpleadoBo = new BusquedaEmpleadoBo();
        adminUsuariosBo = new AdminUsuariosBo();
        mostrarPerfil = false;
    }

    public void init() {
        try {
            // Carga banderas globales
            Configuracion cfg = configuracionBo.getConfig();
            this.faHabilitado = cfg.getConFaHabilitado() != null ? cfg.getConFaHabilitado() == 1 : true;
            this.agHabilitado = cfg.getConAgHabilitado() != null ? cfg.getConAgHabilitado() == 1 : true;
            // Carga empresas para búsqueda de empleado
            empresas = busquedaEmpleadoBo.getEmpresasDto();
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(), e);
            super.muestraMensajeError("No fue posible cargar la configuración", "", null);
        }
    }

    // Acciones Configuración
    public void guardarBanderas() {
        try {
            configuracionBo.updateFlags(faHabilitado, agHabilitado);
            super.muestraMensajeExito("Se guardaron las banderas de créditos", "", null);
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(), e);
            super.muestraMensajeError("No fue posible guardar las banderas", "", null);
        }
    }

    // Acciones Empleados
    public void buscarEmpleado() {
        try {
            mostrarPerfil = false;
            if (claveEmpleado != null && empresa != null) {
                List<Usuarios> usrs = busquedaEmpleadoBo.getUsuarioXCveYEmpresa(claveEmpleado, empresa.getEmpId());
                if (!usrs.isEmpty()) {
                    usuario = usrs.get(0);
                    // Configura PerfilBean en modo edición admin
                    perfilBean.setUsuarioId(usuario.getUsuId());
                    perfilBean.setOrigen(4); // modo edición admin
                    perfilBean.init();
                    mostrarPerfil = true;
                } else {
                    super.muestraMensajeError("No se encontró el empleado", "", null);
                }
            } else {
                super.muestraMensajeError("Capture clave y empresa", "", null);
            }
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(), e);
            super.muestraMensajeError("Error al buscar empleado", "", null);
        }
    }

    public void toggleHabilitado() {
        try {
            int nuevo = (usuario.getUsuHabilitado() != null && usuario.getUsuHabilitado() == 1) ? 0 : 1;
            busquedaEmpleadoBo.updtUsrHabilitar(usuario.getUsuId(), nuevo);
            usuario.setUsuHabilitado(nuevo);
            super.muestraMensajeExito("Estatus de habilitado actualizado", "", null);
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(), e);
            super.muestraMensajeError("No fue posible actualizar el estatus", "", null);
        }
    }

    public void resetearPassword() {
        try {
            String tmp = (nuevoPassword != null && !nuevoPassword.trim().isEmpty())
                    ? nuevoPassword
                    : "Tmp-" + UUID.randomUUID().toString().substring(0, 6);
            adminUsuariosBo.updatePassword(usuario.getUsuId(), tmp);
            nuevoPassword = tmp;
            super.muestraMensajeExito("Contraseña restablecida: " + tmp, "", null);
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(), e);
            super.muestraMensajeError("No fue posible restablecer la contraseña", "", null);
        }
    }

    // Getters/Setters
    public boolean isFaHabilitado() { return faHabilitado; }
    public void setFaHabilitado(boolean faHabilitado) { this.faHabilitado = faHabilitado; }
    public boolean isAgHabilitado() { return agHabilitado; }
    public void setAgHabilitado(boolean agHabilitado) { this.agHabilitado = agHabilitado; }
    public List<EmpresasDto> getEmpresas() { return empresas; }
    public void setEmpresas(List<EmpresasDto> empresas) { this.empresas = empresas; }
    public EmpresasDto getEmpresa() { return empresa; }
    public void setEmpresa(EmpresasDto empresa) { this.empresa = empresa; }
    public Integer getClaveEmpleado() { return claveEmpleado; }
    public void setClaveEmpleado(Integer claveEmpleado) { this.claveEmpleado = claveEmpleado; }
    public Usuarios getUsuario() { return usuario; }
    public void setUsuario(Usuarios usuario) { this.usuario = usuario; }
    public boolean isMostrarPerfil() { return mostrarPerfil; }
    public void setMostrarPerfil(boolean mostrarPerfil) { this.mostrarPerfil = mostrarPerfil; }
    public PerfilBean getPerfilBean() { return perfilBean; }
    public void setPerfilBean(PerfilBean perfilBean) { this.perfilBean = perfilBean; }
    public String getNuevoPassword() { return nuevoPassword; }
    public void setNuevoPassword(String nuevoPassword) { this.nuevoPassword = nuevoPassword; }
}

