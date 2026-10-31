import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.DescuentoNominaBo;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.PagosPersonalBo;
import mx.com.evoti.bo.ReporteAportacionesBo;
import mx.com.evoti.bo.administrador.finiquito.FiniquitosBo;
import mx.com.evoti.bo.administrador.finiquito.HistorialBajasBo;
import mx.com.evoti.bo.administrador.solicitud.ValidaSolicitudBo;
import mx.com.evoti.bo.altasCambiosEmpresa.AltasBo;
import mx.com.evoti.bo.bancos.BancoAjustesBo;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.dao.ReporteResultadosDao;
import mx.com.evoti.dao.ReporteUsuariosDao;
import mx.com.evoti.dao.UsuariosDao;
import mx.com.evoti.dao.creditos.ReporteDao;
import mx.com.evoti.dao.creditos.ReporteMorososDao;
import mx.com.evoti.dao.transacciones.TransaccionDao;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Usuarios;

/**
 * Ejecuta, SOLO LECTURA, las consultas que usan las pantallas de reportes, descuentos de nómina,
 * bancos, bajas, solicitudes, altas y el expediente del socio. Por cada consulta guarda:
 *   - el contenido ordenado (para saber si los datos son los mismos);
 *   - una huella del orden original (para saber si la pantalla los mostraría en el mismo orden).
 * CANDADO: igual que PruebaProcesos.
 * Uso: PruebaConsultas <control|ensayo> <ip_permitida:puerto> <dir_salida> <usuarios,separados,por,coma>
 */
public class PruebaConsultas {
    static StringBuilder contenido = new StringBuilder(), orden = new StringBuilder();
    static int ok = 0, err = 0;
    static SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        Path salida = Paths.get(a[2]);
        PrintStream consola = System.out;
        URL cfg = PruebaConsultas.class.getClassLoader().getResource("hibernate.cfg.xml");
        String cont;
        try (Scanner sc = new Scanner(cfg.openStream(), "UTF-8")) { cont = sc.useDelimiter("\\A").next(); }
        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/").matcher(cont);
        String destino = m.find() ? m.group(1) : "";
        consola.println("[" + modo + "] configuración cargada: " + cfg + " -> " + destino);
        if (destino.contains("35.225.67.182") || !destino.equals(permitido)) {
            consola.println("BLOQUEADO: la configuración no apunta al destino permitido (" + permitido + ")."); System.exit(2);
        }
        Files.createDirectories(salida);
        System.setOut(new PrintStream(new FileOutputStream(salida.resolve("hibernate-sql.log").toFile()), true));
        HibernateUtil.buildSessionFactory();

        Date c1015 = F.parse("2026-10-15"), c1029 = F.parse("2026-10-29"), c1112 = F.parse("2026-11-12"), c1001 = F.parse("2026-10-01");
        Date ago1 = F.parse("2026-08-01"), sep30 = F.parse("2026-09-30");

        // --- Reportes generales ---
        q("reporte de créditos", () -> new ReporteDao().getReporte());
        q("reporte de morosos al 2026-10-29", () -> new ReporteMorososDao().getReporteMorosos(c1029));
        DescuentoNominaBo dn = new DescuentoNominaBo();
        for (Date d : Arrays.asList(c1001, c1015, c1029, c1112)) {
            for (int e = 1; e <= 4; e++) {
                final int emp = e;
                q("descuentos de nómina " + F.format(d) + " empresa " + e, () -> dn.obtieneListaDescuentosNomina(d, emp));
                q("descuentos de nómina (otras) " + F.format(d) + " empresa " + e, () -> dn.obtieneListaDescuentosNominaOtras(d, emp));
            }
        }
        for (String s : Arrays.asList("2026-06-04", "2026-06-18", "2026-12-10", "2026-12-24")) {
            Date d = F.parse(s);
            q("descuentos FA/AG " + s, () -> dn.obtieneListaDescuentosNominaFAAG(d));
        }
        BancosBo bb = new BancosBo();
        q("descuentos por cobrar de créditos", () -> bb.getDesctosXCobrarCreds());
        q("reporte de bancos ago-sep 2026", () -> bb.getReporteBancos(ago1, sep30));
        BancoAjustesBo ba = new BancoAjustesBo();
        q("ajuste de bancos: carga de listas ago-sep 2026", () -> { ba.obtieneListasBanco(ago1, sep30); return "totales " + ba.getTotalBanco() + " / " + ba.getTotalEdoCta(); });
        q("ajuste de bancos: movimientos del sistema", () -> ba.getLstBancos());
        q("ajuste de bancos: estado de cuenta", () -> ba.getLstEstadosCuenta());
        q("ajuste de bancos: ya ajustados", () -> ba.getAjustadosOriginal());
        ReporteResultadosDao rr = new ReporteResultadosDao();
        for (Date d : Arrays.asList(c1015, c1029)) {
            q("resultado de pagos: situación " + F.format(d), () -> rr.obtieneSituacionPagosXCatorcena(d));
            q("resultado de pagos: amortización " + F.format(d), () -> rr.obtieneResultadoAmortizacion(d));
            q("resultado de pagos: créditos no pagados " + F.format(d), () -> rr.obtieneCreditosNoPagados(d));
        }
        ReporteAportacionesBo ra = new ReporteAportacionesBo();
        for (Date d : Arrays.asList(c1001, c1015)) {
            for (int e = 1; e <= 2; e++) {
                final int emp = e;
                String k = F.format(d) + " empresa " + e;
                q("aportaciones: datos del archivo " + k, () -> ra.obtieneDatosArchivo(d, emp));
                q("aportaciones: resumen " + k, () -> ra.obtieneResumenArchivo(d, emp));
                q("aportaciones: detalle " + k, () -> ra.obtieneDetalleArchivo(d, emp));
                q("aportaciones: no registrados " + k, () -> ra.obtieneUsuariosNoRegistrados(d, emp));
                q("aportaciones: con baja " + k, () -> ra.obtieneUsuariosConBaja(d, emp));
            }
        }
        HistorialBajasBo hb = new HistorialBajasBo();
        for (int s = 1; s <= 3; s++) { final int est = s; q("historial de bajas estatus " + s, () -> hb.getBajaUsuarios(est)); }
        q("bajas pendientes", () -> hb.getUsuariosBajaPendiente(1));
        q("solicitudes por validar", () -> new ValidaSolicitudBo().getSolsFValidando());
        AltasBo ab = new AltasBo();
        q("historial de cambios y altas", () -> ab.obtenerHistorialCambiosAltas());
        q("altas pendientes", () -> ab.getUsuarios());
        q("reporte de usuarios", () -> new ReporteUsuariosDao().getReporteUsuarios());
        q("bitácora de movimientos", () -> new TransaccionDao().getTransacciones());

        // --- Expediente de cada socio de la muestra ---
        MovimientosBo mb = new MovimientosBo();
        PagosPersonalBo pp = new PagosPersonalBo();
        CreditosBo cb = new CreditosBo();
        FiniquitosBo fb = new FiniquitosBo();
        UsuariosDao ud = new UsuariosDao();
        for (String s : a[3].split(",")) {
            int u = Integer.parseInt(s.trim());
            String k = "socio " + u + ": ";
            q(k + "ahorros", () -> mb.getAhorrosByUsuId(u));
            q(k + "ahorros totales", () -> mb.getAhorrosTotales(u));
            q(k + "detalle ahorro fijo", () -> mb.getAhorrosDetalle(u, 1, 1, null));
            q(k + "detalle ahorro no fijo", () -> mb.getAhorrosDetalle(u, 2, 1, null));
            q(k + "pagos acumulados", () -> pp.obtienePagosAcumulados(u));
            q(k + "acumulado", () -> pp.obtieneAcumulado(u));
            q(k + "pagos exactos", () -> pp.getPagosDetalle(u, 2));
            q(k + "créditos", () -> cb.getCreditosXIdUsr(u));
            q(k + "créditos como aval", () -> cb.getCreditosDeAval(u));
            Usuarios usu = ud.obtenerUsuario(u);
            q(k + "detalle de créditos (expediente)", () -> cb.obtCreditosDetalle(usu));
            q(k + "detalle de créditos (finiquito)", () -> fb.obtCreditosDetalleFiniquito(usu));
            for (Object c : cb.getCreditosXIdUsr(u)) {
                Object id = c.getClass().getMethod("getCreId").invoke(c);
                if (id != null) q(k + "amortización del crédito " + id, () -> cb.getAmortizacionXCredito(((Number) id).intValue()));
            }
        }

        Files.write(salida.resolve("contenido.txt"), contenido.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(salida.resolve("orden.txt"), orden.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] consultas ejecutadas: " + (ok + err) + " | con error: " + err);
        System.exit(0);
    }

    static void q(String nombre, Callable<Object> c) {
        try {
            Object r = c.call();
            List<String> filas = new ArrayList<>();
            if (r instanceof Collection) for (Object o : (Collection<?>) r) filas.add(volcado(o, 0));
            else filas.add(volcado(r, 0));
            List<String> ordenadas = new ArrayList<>(filas);
            Collections.sort(ordenadas);
            contenido.append("## ").append(nombre).append(" (").append(filas.size()).append(" filas)\n");
            for (String f : ordenadas) contenido.append(f).append('\n');
            orden.append(nombre).append(" | ").append(md5(String.join("\n", filas))).append('\n');
            ok++;
        } catch (Exception ex) {
            err++;
            Throwable t = ex.getCause() != null ? ex.getCause() : ex;
            contenido.append("## ").append(nombre).append(" ERROR ").append(t).append('\n');
            orden.append(nombre).append(" | ERROR\n");
        }
    }

    static String md5(String s) throws Exception {
        byte[] d = MessageDigest.getInstance("MD5").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static String volcado(Object o, int nivel) throws Exception {
        if (o == null) return "null";
        if (o instanceof Object[]) return volcado(Arrays.asList((Object[]) o), nivel);
        if (o instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            try {
                for (Object e : (Collection<?>) o) sb.append(volcado(e, nivel + 1)).append(", ");
            } catch (RuntimeException ex) {
                return "<colección no cargada>";
            }
            return sb.append(']').toString();
        }
        if (o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Character) return String.valueOf(o);
        if (o instanceof Date) return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) o);
        if (nivel > 3 || o.getClass().getName().startsWith("org.hibernate")) return o.getClass().getSimpleName();
        Method[] ms = o.getClass().getMethods();
        Arrays.sort(ms, Comparator.comparing(Method::getName));
        StringBuilder sb = new StringBuilder(o.getClass().getSimpleName()).append('{');
        for (Method mt : ms) {
            if (mt.getParameterCount() != 0 || !mt.getName().startsWith("get") || mt.getName().equals("getClass")) continue;
            Object v;
            try { v = mt.invoke(o); } catch (Exception ex) { sb.append(mt.getName().substring(3)).append("=<excepción>; "); continue; }
            sb.append(mt.getName().substring(3)).append('=').append(volcado(v, nivel + 1)).append("; ");
        }
        return sb.append('}').toString();
    }
}
