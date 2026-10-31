import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.reporteEmpleado.ProcesoRecorreCatorcenasBo;
import mx.com.evoti.dao.UsuariosDao;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Usuarios;

/**
 * Recorre catorcenas igual que AmortizacionRepEmpBean.recorreCatorcenas() con "mandar a reporte de
 * nómina" = no: valida pagos a capital, recorre las catorcenas seleccionadas, guarda la tabla y
 * ejecuta la misma revisión de saldar crédito. Los créditos de la lista de "dobles" recorren las dos
 * primeras catorcenas pendientes; los demás, solo la primera.
 * CANDADO: igual que PruebaProcesos.
 * Uso: PruebaRecorrido <control|ensayo> <ip_permitida:puerto> <dir_salida> <creditos_una> <creditos_dos>
 */
public class PruebaRecorrido {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        Path salida = Paths.get(a[2]);
        PrintStream consola = System.out;
        URL cfg = PruebaRecorrido.class.getClassLoader().getResource("hibernate.cfg.xml");
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

        CreditosBo creBo = new CreditosBo();
        UsuariosDao ud = new UsuariosDao();
        StringBuilder res = new StringBuilder();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        int procesados = 0;
        for (int grupo = 0; grupo < 2; grupo++) {
            int cuantas = grupo == 0 ? 1 : 2;
            for (String s : a[3 + grupo].split(",")) {
                int creId = Integer.parseInt(s.trim());
                List<AmortizacionDto> amortizacion = creBo.getAmortizacionXCredito(creId);
                List<AmortizacionDto> pendientes = new ArrayList<>();
                for (AmortizacionDto d : amortizacion) if (Integer.valueOf(1).equals(d.getAmoEstatusInt())) pendientes.add(d);
                pendientes.sort(Comparator.comparing(AmortizacionDto::getAmoNumeroPago));
                List<AmortizacionDto> sel = new ArrayList<>(pendientes.subList(0, Math.min(cuantas, pendientes.size())));
                ProcesoRecorreCatorcenasBo recorreBo = new ProcesoRecorreCatorcenasBo();
                String msj = recorreBo.validaPagoACapital(amortizacion, sel);
                CreditosFinal cre = creBo.obtieneCredXId(creId);
                Usuarios usuario = ud.obtenerUsuario(cre.getCreUsuId());
                res.append("credito ").append(creId).append(" | catorcenas a recorrer ").append(cuantas);
                if (!msj.isEmpty()) { res.append(" | NO PROCESADO: ").append(msj.replace('\n', ' ')).append('\n'); continue; }
                PrintStream antes = System.out;
                recorreBo.principalRecorreCatorcenas(amortizacion, sel, usuario.getUsuId());
                DetalleCreditoDto det = null;
                for (DetalleCreditoDto d : creBo.obtCreditosDetalle(usuario)) if (d.getCreId() != null && d.getCreId() == creId) det = d;
                boolean pagado = creBo.saldarCredito(det, amortizacion);
                res.append(" | saldado ").append(pagado).append('\n');
                for (AmortizacionDto d : creBo.getAmortizacionXCredito(creId)) {
                    res.append("  ").append(d.getAmoNumeroPago()).append(" | ").append(d.getAmoFechaPago() == null ? null : f.format(d.getAmoFechaPago()))
                       .append(" | cap ").append(d.getAmoCapital()).append(" | int ").append(d.getAmoInteres()).append(" | pago ").append(d.getAmoMontoPago())
                       .append(" | est ").append(d.getAmoEstatusInt()).append(" | pagoId ").append(d.getAmoPagoId()).append('\n');
                }
                procesados++;
            }
        }
        Files.write(salida.resolve("recorridos.txt"), res.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] créditos recorridos: " + procesados);
        System.exit(0);
    }
}
