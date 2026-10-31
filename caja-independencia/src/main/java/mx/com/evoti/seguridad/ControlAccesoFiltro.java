package mx.com.evoti.seguridad;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.util.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control de acceso a todas las páginas JSF.
 *
 * - Sin sesión solo se permiten las páginas públicas (login, recuperación y restablecimiento de
 *   contraseña, páginas de error) y los recursos de JSF (estilos, scripts, imágenes).
 * - El rol Usuario (socio) solo puede abrir el dashboard y las pantallas de navegacion/usuariocomun,
 *   que son las únicas a las que lo lleva el sistema.
 * - Administrador y Auxiliar pueden abrir todo, igual que lo que hoy les muestra el menú.
 *
 * Si se niega el acceso, sin sesión se redirige al login y un socio se redirige a su dashboard.
 * En las peticiones ajax de JSF la redirección se responde como partial-response, que es lo que
 * PrimeFaces sabe seguir; así la acción solicitada nunca se ejecuta.
 *
 * @author evoti
 */
public class ControlAccesoFiltro implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlAccesoFiltro.class);

    private static final Set<String> PAGINAS_PUBLICAS = new HashSet<>(Arrays.asList(
            "/login.xhtml",
            "/recuperar-password.xhtml",
            "/restablecer-password.xhtml",
            "/404.xhtml",
            "/access-denied.xhtml"));
    private static final String RECURSOS_JSF = "/javax.faces.resource/";
    private static final String LOGIN = "/login.xhtml";
    private static final String DASHBOARD = "/dashboard.xhtml";
    private static final String PANTALLAS_SOCIO = "/navegacion/usuariocomun/";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Control de acceso activo");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String ruta = req.getServletPath() == null ? "" : req.getServletPath();

        if (PAGINAS_PUBLICAS.contains(ruta) || ruta.startsWith(RECURSOS_JSF)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession sesion = req.getSession(false);
        Object usuario = sesion == null ? null : sesion.getAttribute("usuario");
        if (!(usuario instanceof UsuarioDto)) {
            niegaAcceso(req, resp, ruta, LOGIN, "sin sesión");
            return;
        }

        UsuarioDto usu = (UsuarioDto) usuario;
        if (!esPersonalDeCaja(usu) && !ruta.equals(DASHBOARD) && !ruta.startsWith(PANTALLAS_SOCIO)) {
            niegaAcceso(req, resp, ruta, DASHBOARD, "rol " + usu.getRol() + ", usuario " + usu.getId());
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Administrador y Auxiliar. Cualquier otro rol, o un rol desconocido, se trata como socio.
     */
    private boolean esPersonalDeCaja(UsuarioDto usu) {
        return Constantes.ROL_USR_ADMON_I.equals(usu.getRolId()) || Constantes.ROL_USR_AUX_I.equals(usu.getRolId());
    }

    private void niegaAcceso(HttpServletRequest req, HttpServletResponse resp, String ruta, String destino, String motivo)
            throws IOException {
        LOGGER.warn(String.format("Acceso negado (%s) a %s desde %s", motivo, ruta, req.getRemoteAddr()));
        String url = req.getContextPath() + destino;
        if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
            resp.setContentType("text/xml");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().print("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\""
                    + url + "\"></redirect></partial-response>");
        } else {
            resp.sendRedirect(url);
        }
    }

    @Override
    public void destroy() {
    }
}
