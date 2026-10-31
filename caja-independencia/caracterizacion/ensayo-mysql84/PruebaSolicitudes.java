import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.administrador.solicitud.FondeosBo;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.bo.util.EnviaCorreo;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.dto.SolicitudDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.util.Constantes;

/**
 * Valida y fondea solicitudes con las mismas llamadas que ValSolDetalleBean y FondeosBean:
 *   autorizar = updtEstatusSolicitud(id, 3); rechazar = updtEstatusSolicitud(id, 7) + motivo en bitácora;
 *   fondear  = getSolsPendtsFondeoScreen(ACEPTADA) + fondeaSolicitud(solicitud, fechaPrimerPago).
 * No se ejecutan las validaciones previas de la pantalla de autorizar ni el envío de correos: la
 * clase EnviaCorreo de este classpath es un sustituto que no envía nada.
 * CANDADO: igual que PruebaProcesos; además se niega a correr si EnviaCorreo no es el sustituto.
 * Uso: PruebaSolicitudes <modo> <ip:puerto> <primer_pago yyyy-MM-dd> <dir_salida> <autorizar> <rechazar> <fondear_existentes>
 */
public class PruebaSolicitudes {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        Date primerPago = new SimpleDateFormat("yyyy-MM-dd").parse(a[2]);
        Path salida = Paths.get(a[3]);
        PrintStream consola = System.out;
        URL cfg = PruebaSolicitudes.class.getClassLoader().getResource("hibernate.cfg.xml");
        String contenido;
        try (Scanner sc = new Scanner(cfg.openStream(), "UTF-8")) { contenido = sc.useDelimiter("\\A").next(); }
        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/").matcher(contenido);
        String destino = m.find() ? m.group(1) : "";
        consola.println("[" + modo + "] configuración cargada: " + cfg + " -> " + destino);
        if (destino.contains("35.225.67.182") || !destino.equals(permitido)) {
            consola.println("BLOQUEADO: la configuración no apunta al destino permitido (" + permitido + ")."); System.exit(2);
        }
        URL correo = PruebaSolicitudes.class.getClassLoader().getResource("mx/com/evoti/bo/util/EnviaCorreo.class");
        if (correo == null || !correo.toString().contains("stub-classes")) {
            consola.println("BLOQUEADO: la clase de correo cargada no es el sustituto (" + correo + ")."); System.exit(2);
        }
        consola.println("[" + modo + "] clase de correo: " + correo);
        Files.createDirectories(salida);
        System.setOut(new PrintStream(new FileOutputStream(salida.resolve("hibernate-sql.log").toFile()), true));
        HibernateUtil.buildSessionFactory();

        DetalleSolicitudBo det = new DetalleSolicitudBo();
        FondeosBo fondeo = new FondeosBo();
        CreditosBo creBo = new CreditosBo();
        StringBuilder res = new StringBuilder();
        List<Long> aFondear = new ArrayList<>();
        for (String s : a[4].split(",")) {
            long id = Long.parseLong(s.trim());
            det.updtEstatusSolicitud(BigInteger.valueOf(id), 3, null);
            aFondear.add(id);
            res.append("autorizada ").append(id).append('\n');
        }
        for (String s : a[5].split(",")) {
            long id = Long.parseLong(s.trim());
            det.updtEstatusSolicitud(BigInteger.valueOf(id), 7, null);
            det.insertaMotivoRechazo(id, Constantes.BIT_SOL_RECHAZADA, 1, null, "Rechazo de prueba del ensayo MySQL 8.4");
            res.append("rechazada ").append(id).append('\n');
        }
        for (String s : a[6].split(",")) aFondear.add(Long.parseLong(s.trim()));

        List<SolicitudDto> pendientes = fondeo.getSolsPendtsFondeoScreen(Constantes.SOL_EST_ACEPTADA);
        res.append("pendientes de fondeo en pantalla: ").append(pendientes.size()).append('\n');
        for (Long id : aFondear) {
            SolicitudDto sel = null;
            for (SolicitudDto s : pendientes) if (s.getSolId() != null && s.getSolId().longValue() == id) sel = s;
            if (sel == null) { res.append("fondeo ").append(id).append(": no aparece en la pantalla de pendientes\n"); continue; }
            fondeo.fondeaSolicitud(sel, primerPago);
            res.append("fondeada ").append(id).append(" producto ").append(sel.getProId()).append(" monto ").append(sel.getSolMontoSolicitado())
               .append(" catorcenas ").append(sel.getSolCatorcenas()).append('\n');
            for (CreditoDto c : creBo.getCreditosXIdUsr(sel.getUsuId())) {
                Object solCre = c.getClass().getMethod("getCreSolicitud").invoke(c);
                if (solCre == null || ((Number) solCre).longValue() != id) continue;
                Integer creId = ((Number) c.getClass().getMethod("getCreId").invoke(c)).intValue();
                res.append("  credito ").append(creId).append(" clave ").append(c.getClass().getMethod("getCreClave").invoke(c)).append('\n');
                for (AmortizacionDto d : creBo.getAmortizacionXCredito(creId)) {
                    res.append("    ").append(d.getAmoNumeroPago()).append(" | ").append(new SimpleDateFormat("yyyy-MM-dd").format(d.getAmoFechaPago()))
                       .append(" | cap ").append(d.getAmoCapital()).append(" | int ").append(d.getAmoInteres()).append(" | pago ").append(d.getAmoMontoPago())
                       .append(" | est ").append(d.getAmoEstatusInt()).append('\n');
                }
            }
        }
        res.append("correos que se habrían enviado: ").append(EnviaCorreo.ENVIOS.size()).append('\n');
        Files.write(salida.resolve("solicitudes.txt"), res.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] solicitudes procesadas | correos que se habrían enviado: " + EnviaCorreo.ENVIOS.size());
        System.exit(0);
    }
}
