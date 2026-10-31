package mx.com.evoti.bo.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

/**
 * SUSTITUTO PARA PRUEBAS: misma clase y mismas firmas que la de la aplicación, pero no envía
 * ningún correo; solo registra que se habría enviado. Va antes en el classpath de los programas de
 * prueba para que el fondeo y la validación no puedan mandar correos a nadie.
 */
public class EnviaCorreo implements Serializable {
    public static final List<String> ENVIOS = new ArrayList<>();

    public static boolean sendMessageAttachment(List<String> dirAttachmentLst, String mensaje, String subject, String correo) throws MessagingException {
        ENVIOS.add("adjunto: " + subject);
        return true;
    }

    public static boolean sendMessage(String mensaje, String subject, String email) {
        ENVIOS.add("mensaje: " + subject);
        return true;
    }

    public static boolean sendPasswordResetMessage(String nombre, String liga, String email) {
        ENVIOS.add("restablecer contraseña");
        return true;
    }
}
