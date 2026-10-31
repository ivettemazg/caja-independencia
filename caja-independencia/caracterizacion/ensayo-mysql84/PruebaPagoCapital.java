import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.reporteEmpleado.ProcesoPagoCapitalBo;
import mx.com.evoti.dao.UsuariosDao;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Usuarios;

/**
 * Aplica pagos a capital igual que AmortizacionRepEmpBean.aplicarPagoCapital(): toma la tabla del
 * crédito, el primer renglón pendiente, calcula las catorcenas restantes como la pantalla y llama a
 * ProcesoPagoCapitalBo.recalculaAmortizacion. Después vuelca la tabla resultante de cada crédito.
 *
 * Casos por posición en la lista: 0 = paga todo el capital; 1 = paga capital + 100; resto = 25 %.
 * CANDADO: igual que PruebaProcesos.
 * Uso: PruebaPagoCapital <control|ensayo> <ip_permitida:puerto> <fecha yyyy-MM-dd> <id_usuario_sesion> <dir_salida> <creditos,separados,por,coma>
 */
public class PruebaPagoCapital {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(a[2]);
        int userSession = Integer.parseInt(a[3]);
        Path salida = Paths.get(a[4]);
        PrintStream consola = System.out;
        URL cfg = PruebaPagoCapital.class.getClassLoader().getResource("hibernate.cfg.xml");
        String contenido;
        try (Scanner sc = new Scanner(cfg.openStream(), "UTF-8")) { contenido = sc.useDelimiter("\\A").next(); }
        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/").matcher(contenido);
        String destino = m.find() ? m.group(1) : "";
        consola.println("[" + modo + "] configuración cargada: " + cfg + " -> " + destino);
        if (destino.contains("35.225.67.182") || !destino.equals(permitido)) {
            consola.println("BLOQUEADO: la configuración no apunta al destino permitido (" + permitido + ")."); System.exit(2);
        }
        Files.createDirectories(salida);
        System.setOut(new PrintStream(new FileOutputStream(salida.resolve("hibernate-sql.log").toFile()), true));
        HibernateUtil.buildSessionFactory();

        CreditosBo credBo = new CreditosBo();
        UsuariosDao usuDao = new UsuariosDao();
        StringBuilder res = new StringBuilder();
        String[] ids = a[5].split(",");
        for (int i = 0; i < ids.length; i++) {
            int creId = Integer.parseInt(ids[i].trim());
            List<AmortizacionDto> amort = credBo.getAmortizacionXCredito(creId);
            AmortizacionDto sel = null;
            for (AmortizacionDto d : amort) {
                if (Integer.valueOf(1).equals(d.getAmoEstatusInt()) && (sel == null || d.getAmoNumeroPago() < sel.getAmoNumeroPago())) sel = d;
            }
            if (sel == null) { res.append("credito ").append(creId).append(": sin renglón pendiente\n"); continue; }
            int catorcenasRestantes = amort.size() - (sel.getAmoNumeroPago() - 1);
            double monto = i == 0 ? sel.getAmoCapital() : i == 1 ? sel.getAmoCapital() + 100.0
                    : new BigDecimal(sel.getAmoCapital() * 0.25).setScale(2, RoundingMode.HALF_UP).doubleValue();
            CreditosFinal crePojo = credBo.obtieneCredXId(creId);
            Usuarios usuario = usuDao.obtenerUsuario(crePojo.getCreUsuId());
            List<String> errores = new ProcesoPagoCapitalBo().recalculaAmortizacion(monto, sel, crePojo, catorcenasRestantes, usuario, fecha, 1, userSession);
            res.append("credito ").append(creId).append(" producto ").append(crePojo.getCreProducto()).append(" | renglon ").append(sel.getAmoNumeroPago())
               .append(" | capital ").append(sel.getAmoCapital()).append(" | pago ").append(monto).append(" | restantes ").append(catorcenasRestantes)
               .append(" | errores ").append(errores).append('\n');
            CreditosFinal despues = credBo.obtieneCredXId(creId);
            res.append("  credito despues: estatus ").append(despues.getCreEstatus()).append(" | pago catorcenal ").append(despues.getCrePagoQuincenal())
               .append(" | catorcenas ").append(despues.getCreCatorcenas()).append('\n');
            for (AmortizacionDto d : credBo.getAmortizacionXCredito(creId)) {
                res.append("  ").append(d.getAmoNumeroPago()).append(" | ").append(d.getAmoFechaPago() == null ? null : new SimpleDateFormat("yyyy-MM-dd").format(d.getAmoFechaPago()))
                   .append(" | cap ").append(d.getAmoCapital()).append(" | amort ").append(d.getAmoAmortizacion()).append(" | int ").append(d.getAmoInteres())
                   .append(" | pago ").append(d.getAmoMontoPago()).append(" | est ").append(d.getAmoEstatusInt()).append('\n');
            }
        }
        Files.write(salida.resolve("pagos-capital.txt"), res.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] pagos a capital aplicados: " + ids.length);
        System.exit(0);
    }
}
