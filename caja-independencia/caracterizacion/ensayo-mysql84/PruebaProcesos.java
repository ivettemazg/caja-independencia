import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.administrador.algoritmopagos.CargaPagosMovimientosBo;
import mx.com.evoti.bo.jasper.DoctosJasperSolicitudBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.dto.jasper.FondeoDto;
import mx.com.evoti.dto.jasper.PendienteDto;
import mx.com.evoti.dto.jasper.ReporteAmortizacionDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Corre, fuera del servidor web, los mismos procesos que las pantallas:
 *   1. Los cinco documentos PDF de una lista de solicitudes (mismo armado que MisSolicitudesBean).
 *   2. La carga de un archivo de pagos y su aplicación con el algoritmo real (mismas llamadas que
 *      CargaArchivoPagMovsBean y AsignaPagosBean).
 * Guarda los datos de cada documento y los PDF para comparar la corrida en 8.0 con la de 8.4.
 *
 * CANDADO: antes de abrir la base revisa qué hibernate.cfg.xml cargó el classpath. Solo continúa si
 * apunta a la base de control local (127.0.0.1:3308) o a la IP de ensayo indicada; nunca a producción.
 *
 * Uso: PruebaProcesos <control|ensayo> <ip_permitida:puerto> <archivo.xlsx> <nombre_archivo> <catorcena yyyy-MM-dd>
 *                     <dir_salida> <dir_reportes> <solicitudes separadas por coma>
 */
public class PruebaProcesos {
    private static final String IP_PRODUCCION = "35.225.67.182";

    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1], xlsx = a[2], nombreArchivo = a[3], catorcena = a[4];
        Path salida = Paths.get(a[5]);
        String reportes = a[6];
        List<Long> solicitudes = new ArrayList<>();
        for (String s : a[7].split(",")) solicitudes.add(Long.parseLong(s.trim()));
        PrintStream consola = System.out;

        // --- Candado sobre la configuración realmente cargada ---
        URL cfg = PruebaProcesos.class.getClassLoader().getResource("hibernate.cfg.xml");
        String contenido;
        try (Scanner sc = new Scanner(cfg.openStream(), "UTF-8")) { contenido = sc.useDelimiter("\\A").next(); }
        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/").matcher(contenido);
        String destino = m.find() ? m.group(1) : "";
        consola.println("[" + modo + "] configuración cargada: " + cfg + " -> " + destino);
        if (destino.contains(IP_PRODUCCION) || !destino.equals(permitido)) {
            consola.println("BLOQUEADO: la configuración no apunta al destino permitido (" + permitido + "). No se ejecutó nada.");
            System.exit(2);
        }

        Files.createDirectories(salida.resolve("pdf"));
        System.setOut(new PrintStream(new FileOutputStream(salida.resolve("hibernate-sql.log").toFile()), true));
        long t0 = System.currentTimeMillis();
        HibernateUtil.buildSessionFactory();
        consola.println("[" + modo + "] Hibernate listo en " + (System.currentTimeMillis() - t0) + " ms");

        // --- 1. Documentos PDF ---
        DoctosJasperSolicitudBo bo = new DoctosJasperSolicitudBo();
        StringBuilder datos = new StringBuilder();
        int ok = 0, err = 0;
        for (Long sol : solicitudes) {
            String[] docs = {"AnexoATblAmort", "AnexoBPagare", "AnexoCPagare", "SolicitudCredito", "Aviso"};
            for (String doc : docs) {
                try {
                    List<?> lista = armaDatos(bo, doc, sol);
                    datos.append("## ").append(sol).append(' ').append(doc).append('\n').append(volcado(lista, 0)).append('\n');
                    JasperReport rep = (JasperReport) JRLoader.loadObjectFromFile(reportes + "/" + doc + ".jasper");
                    JasperPrint jp = JasperFillManager.fillReport(rep, new HashMap<>(), new JRBeanCollectionDataSource(lista, false));
                    JasperExportManager.exportReportToPdfFile(jp, salida.resolve("pdf/" + sol + "-" + doc + ".pdf").toString());
                    ok++;
                } catch (Exception ex) {
                    err++;
                    datos.append("## ").append(sol).append(' ').append(doc).append(" ERROR ")
                         .append(ex.getCause() != null ? ex.getCause() : ex).append('\n');
                }
            }
        }
        Files.write(salida.resolve("datos-documentos.txt"), datos.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] documentos generados: " + ok + " | con error: " + err);

        if (xlsx.equals("-")) {
            consola.println("[" + modo + "] modo solo documentos: no se carga ningún archivo de pagos");
            System.exit(0);
        }

        // --- 2. Carga y aplicación del archivo de pagos ---
        CargaPagosMovimientosBo carga = new CargaPagosMovimientosBo();
        Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(catorcena);
        consola.println("[" + modo + "] es catorcena: " + carga.validaSiEsCatorcena(fecha)
                + " | el nombre ya existe: " + carga.validaArchivoExiste(nombreArchivo));
        t0 = System.currentTimeMillis();
        carga.procesaExcel(nombreArchivo, xlsx, 1);
        consola.println("[" + modo + "] archivo cargado en " + (System.currentTimeMillis() - t0) + " ms");
        t0 = System.currentTimeMillis();
        String msj = carga.procesaArchivosCargados(fecha);
        consola.println("[" + modo + "] pagos aplicados en " + (System.currentTimeMillis() - t0) + " ms | mensaje: "
                + (msj == null ? "(ninguno: se procesó)" : msj));
        System.exit(0);
    }

    /** Arma la lista de datos de cada documento igual que MisSolicitudesBean. */
    static List<?> armaDatos(DoctosJasperSolicitudBo bo, String doc, Long sol) throws Exception {
        switch (doc) {
            case "AnexoATblAmort": {
                FondeoDto dto = bo.getCreditoBySolicitud(sol);
                ReporteAmortizacionDto r = new ReporteAmortizacionDto();
                r.setNombre(dto.getNombre());
                r.setClaveCredito(dto.getClaveCredito());
                r.setAmortizacion(bo.obtieneAmortizacion(dto.getIdSolicitud()));
                return new ArrayList<>(Arrays.asList(r));
            }
            case "AnexoBPagare":
                return new ArrayList<>(Arrays.asList(bo.obtieneDatosAnexoB(sol)));
            case "AnexoCPagare": {
                List<UsuarioDto> avales = bo.obtieneAvalesAnexoC(sol);
                FondeoDto credito = bo.obtieneDatosAnexoC(sol);
                credito.setAvales(avales);
                if (avales.size() == 1) { credito.setLineaDos("_________________________"); credito.setAvalDos(avales.get(0).getNombreCompleto()); }
                if (avales.size() == 2) { credito.setLineaUno("_________________________"); credito.setAvalUno(avales.get(0).getNombreCompleto());
                    credito.setLineaTres("_________________________"); credito.setAvalTres(avales.get(1).getNombreCompleto()); }
                if (avales.size() == 3) { credito.setLineaUno("_________________________"); credito.setAvalUno(avales.get(0).getNombreCompleto());
                    credito.setLineaDos("_________________________"); credito.setAvalDos(avales.get(1).getNombreCompleto());
                    credito.setLineaTres("_________________________"); credito.setAvalTres(avales.get(2).getNombreCompleto()); }
                return new ArrayList<>(Arrays.asList(credito));
            }
            case "SolicitudCredito": {
                List<PendienteDto> avales = bo.obtieneAvalesSolicitud(sol);
                FondeoDto f = bo.obtieneDatosSolicitud(sol);
                f.setAvalesSolicitud(avales);
                f.setIdSolicitud(sol);
                return new ArrayList<>(Arrays.asList(f));
            }
            default:
                return bo.obtieneDatosAviso(sol);
        }
    }

    /** Vuelca recursivamente los getters de los DTO, en orden alfabético, para comparar corridas. */
    static String volcado(Object o, int nivel) throws Exception {
        if (o == null) return "null";
        if (o instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            for (Object e : (List<?>) o) sb.append('\n').append("  ".repeat(nivel + 1)).append(volcado(e, nivel + 1));
            return sb.append(']').toString();
        }
        if (o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Date || o.getClass().isPrimitive()) {
            return o instanceof Date ? new SimpleDateFormat("yyyy-MM-dd").format((Date) o) : String.valueOf(o);
        }
        Method[] ms = o.getClass().getMethods();
        Arrays.sort(ms, Comparator.comparing(Method::getName));
        StringBuilder sb = new StringBuilder(o.getClass().getSimpleName()).append('{');
        for (Method mt : ms) {
            if (mt.getParameterCount() == 0 && mt.getName().startsWith("get") && !mt.getName().equals("getClass")) {
                Object v;
                try {
                    v = mt.invoke(o);
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    sb.append(mt.getName().substring(3)).append("=<excepción: ").append(ex.getCause()).append(">; ");
                    continue;
                }
                sb.append(mt.getName().substring(3)).append('=').append(volcado(v, nivel + 1)).append("; ");
            }
        }
        return sb.append('}').toString();
    }
}
