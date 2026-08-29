package mx.com.evoti.bo;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.util.EnviaCorreo;
import mx.com.evoti.dao.PasswordRecoveryDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.PasswordResetTokens;
import mx.com.evoti.hibernate.pojos.Usuarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordRecoveryBo implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordRecoveryBo.class);
    private static final int TOKEN_MINUTOS_VIGENCIA = 10;
    private static final int TOKEN_ESTATUS_ACTIVO = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordRecoveryDao dao;

    public PasswordRecoveryBo() {
        dao = new PasswordRecoveryDao();
    }

    public void solicitaRecuperacion(Integer claveEmpleado, Integer empresaId, String correo,
            String resetBaseUrl, String ipSolicitud, String userAgent) throws BusinessException {
        validaSolicitud(claveEmpleado, empresaId, correo, resetBaseUrl);

        try {
            Usuarios usuario = dao.buscaUsuarioParaRecuperacion(claveEmpleado, empresaId);

            if (usuario == null) {
                LOGGER.info("Solicitud de recuperacion sin usuario coincidente para clave {} empresa {}",
                        claveEmpleado, empresaId);
                Usuarios usuarioPorClave = dao.buscaUsuarioActivoPorClave(claveEmpleado);
                if (usuarioPorClave != null) {
                    throw new BusinessException("La empresa no coincide con la que tienes registrada en el sistema.");
                }
                throw new BusinessException("No se encontro un usuario activo con la clave de empleado capturada.");
            }

            if (!Integer.valueOf(0).equals(usuario.getUsuPrimeravez())) {
                throw new BusinessException("Para poder hacer cambio de password, primero debes llenar tu información, entra al sistema y actualiza tu información por favor");
            }

            if (usuario.getUsuCorreo() == null || usuario.getUsuCorreo().trim().isEmpty()) {
                throw new BusinessException("El usuario no tiene un correo registrado. Contacta a administracion para recuperar tu acceso.");
            }

            if (!usuario.getUsuCorreo().trim().equalsIgnoreCase(correo.trim())) {
                throw new BusinessException("El correo no coincide con el que tienes registrado en el sistema.");
            }

            dao.cancelaTokensActivos(usuario.getUsuId());

            String tokenPlano = generaToken();
            PasswordResetTokens token = creaToken(usuario.getUsuId(), tokenPlano, ipSolicitud, userAgent);
            dao.guardaToken(token);

            String liga = resetBaseUrl + "?token="
                    + URLEncoder.encode(tokenPlano, StandardCharsets.UTF_8.name());
            String nombre = armaNombre(usuario);
            boolean enviado = EnviaCorreo.sendPasswordResetMessage(nombre, liga, usuario.getUsuCorreo());

            if (!enviado) {
                throw new BusinessException("No fue posible enviar el correo de recuperacion. Intenta mas tarde o contacta a administracion.");
            }
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        } catch (Exception ex) {
            if (ex instanceof BusinessException) {
                throw (BusinessException) ex;
            }
            throw new BusinessException("No fue posible generar la recuperacion de password.", ex);
        }
    }

    public boolean tokenValido(String tokenPlano) throws BusinessException {
        PasswordResetTokens token = obtieneTokenVigente(tokenPlano);
        return token != null;
    }

    public void restablecePassword(String tokenPlano, String nuevoPassword, String confirmaPassword)
            throws BusinessException {
        validaNuevoPassword(nuevoPassword, confirmaPassword);

        try {
            PasswordResetTokens token = obtieneTokenVigente(tokenPlano);
            if (token == null) {
                throw new BusinessException("La liga de recuperacion no es valida o ya expiro.");
            }

            dao.actualizaPasswordYUsaToken(token.getPrtUsuId(), token.getPrtId(), nuevoPassword.trim());
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    private PasswordResetTokens obtieneTokenVigente(String tokenPlano) throws BusinessException {
        if (tokenPlano == null || tokenPlano.trim().isEmpty()) {
            return null;
        }

        try {
            PasswordResetTokens token = dao.buscaTokenActivo(hashToken(tokenPlano));
            if (token == null) {
                return null;
            }

            Date ahora = new Date();
            if (token.getPrtFechaExpira() == null || token.getPrtFechaExpira().before(ahora)) {
                dao.marcaTokenExpirado(token.getPrtId());
                return null;
            }

            return token;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    private void validaSolicitud(Integer claveEmpleado, Integer empresaId, String correo, String resetBaseUrl)
            throws BusinessException {
        if (claveEmpleado == null) {
            throw new BusinessException("Debe capturar la clave de empleado.");
        }
        if (empresaId == null) {
            throw new BusinessException("Debe seleccionar la empresa.");
        }
        if (correo == null || correo.trim().isEmpty()) {
            throw new BusinessException("Debe capturar el correo registrado.");
        }
        if (resetBaseUrl == null || resetBaseUrl.trim().isEmpty()) {
            throw new BusinessException("No fue posible generar la liga de recuperacion.");
        }
    }

    private void validaNuevoPassword(String nuevoPassword, String confirmaPassword) throws BusinessException {
        if (nuevoPassword == null || nuevoPassword.trim().isEmpty()) {
            throw new BusinessException("Debe capturar la nueva contrasena.");
        }
        if (nuevoPassword.trim().length() < 6) {
            throw new BusinessException("La nueva contrasena debe tener al menos 6 caracteres.");
        }
        if (confirmaPassword == null || !nuevoPassword.trim().equals(confirmaPassword.trim())) {
            throw new BusinessException("La confirmacion de contrasena no coincide.");
        }
    }

    private PasswordResetTokens creaToken(Integer usuarioId, String tokenPlano, String ipSolicitud, String userAgent)
            throws BusinessException {
        Date ahora = new Date();
        Calendar expira = Calendar.getInstance();
        expira.setTime(ahora);
        expira.add(Calendar.MINUTE, TOKEN_MINUTOS_VIGENCIA);

        PasswordResetTokens token = new PasswordResetTokens();
        token.setPrtUsuId(usuarioId);
        token.setPrtTokenHash(hashToken(tokenPlano));
        token.setPrtFechaCreacion(ahora);
        token.setPrtFechaExpira(expira.getTime());
        token.setPrtEstatus(TOKEN_ESTATUS_ACTIVO);
        token.setPrtIpSolicitud(recorta(ipSolicitud, 45));
        token.setPrtUserAgent(recorta(userAgent, 255));
        return token;
    }

    private String generaToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String tokenPlano) throws BusinessException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new BusinessException("No fue posible proteger el token de recuperacion.", ex);
        }
    }

    private String armaNombre(Usuarios usuario) {
        StringBuilder nombre = new StringBuilder();
        if (usuario.getUsuNombre() != null) {
            nombre.append(usuario.getUsuNombre());
        }
        if (usuario.getUsuPaterno() != null) {
            nombre.append(" ").append(usuario.getUsuPaterno());
        }
        return nombre.toString().trim();
    }

    private String recorta(String valor, int longitud) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= longitud ? valor : valor.substring(0, longitud);
    }
}
