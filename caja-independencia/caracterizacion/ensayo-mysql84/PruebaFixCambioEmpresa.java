import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.administrador.algoritmopagos.CargaPagosMovimientosBo;
import mx.com.evoti.bo.altasCambiosEmpresa.CambiosEmpresaBo;
import mx.com.evoti.hibernate.config.HibernateUtil;

/**
 * Prueba la corrección de CambiosEmpresaDao.getUltimosMovs llamando a los métodos REALES de la app:
 *   paso "carga":   CargaPagosMovimientosBo.procesaExcel(nombre, ruta, 2) (como la pantalla de carga);
 *   paso "cambios": CambiosEmpresaBo.realizaCambioEmpresa(fecha) (botón "Aplicar cambios" de
 *                   CambiosEmpresaBean); se puede correr varias veces.
 * CANDADO: igual que PruebaProcesos/PruebaAltasCambios: lee el hibernate.cfg.xml que efectivamente
 * carga el classloader, extrae ip:puerto de la URL JDBC y aborta (exit 2) si contiene la IP de
 * producción o si no es exactamente el destino permitido, ANTES de construir el SessionFactory.
 * Uso: PruebaFixCambioEmpresa <modo> <ip:puerto> <carga|cambios> <catorcena yyyy-MM-dd> <dir_salida> [archivo.xlsx nombre_archivo]
 */
public class PruebaFixCambioEmpresa {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1], paso = a[2];
        Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(a[3]);
        Path salida = Paths.get(a[4]);
        PrintStream consola = System.out;
        URL cfg = PruebaFixCambioEmpresa.class.getClassLoader().getResource("hibernate.cfg.xml");
        String contenido;
        try (Scanner sc = new Scanner(cfg.openStream(), "UTF-8")) { contenido = sc.useDelimiter("\\A").next(); }
        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/").matcher(contenido);
        String destino = m.find() ? m.group(1) : "";
        consola.println("[" + modo + "] configuración cargada: " + cfg + " -> " + destino);
        if (destino.contains("35.225.67.182") || !destino.equals(permitido)) {
            consola.println("BLOQUEADO: la configuración no apunta al destino permitido (" + permitido + ")."); System.exit(2);
        }
        Files.createDirectories(salida);
        System.setOut(new PrintStream(new FileOutputStream(salida.resolve("hibernate-sql-" + paso + ".log").toFile(), true), true));
        HibernateUtil.buildSessionFactory();
        StringBuilder res = new StringBuilder();

        if (paso.equals("carga")) {
            String xlsx = a[5], nombreArchivo = a[6];
            CargaPagosMovimientosBo carga = new CargaPagosMovimientosBo();
            Boolean esCatorcena = carga.validaSiEsCatorcena(fecha);
            Boolean yaExiste = carga.validaArchivoExiste(nombreArchivo);
            res.append("[carga] es catorcena: ").append(esCatorcena).append(" | nombre ya existe: ").append(yaExiste).append('\n');
            if (!Boolean.TRUE.equals(esCatorcena) || Boolean.TRUE.equals(yaExiste)) {
                consola.println("ABORTADO: no es catorcena o el archivo ya existe"); System.exit(3);
            }
            carga.procesaExcel(nombreArchivo, xlsx, 2);
            res.append("[carga] archivo procesado: ").append(nombreArchivo).append('\n');
        } else if (paso.equals("cambios")) {
            res.append("[cambios] conexiones ocupadas antes: ").append(ocupadas()).append('\n');
            int cambios = new CambiosEmpresaBo().realizaCambioEmpresa(fecha);
            res.append("[cambios] realizaCambioEmpresa devolvió: ").append(cambios).append('\n');
            res.append("[cambios] conexiones ocupadas después: ").append(ocupadas()).append('\n');
        } else {
            consola.println("paso desconocido: " + paso); System.exit(4);
        }
        Files.write(salida.resolve("resultado.txt"), res.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        consola.print(res);
        HibernateUtil.closeSessionFactory();
        System.exit(0);
    }

    /** Conexiones ocupadas del pool c3p0 (espera a que el hilo auxiliar termine de devolverlas). */
    static int ocupadas() throws Exception {
        Thread.sleep(2000);
        int n = 0;
        for (Object o : com.mchange.v2.c3p0.C3P0Registry.getPooledDataSources())
            n += ((com.mchange.v2.c3p0.PooledDataSource) o).getNumBusyConnectionsDefaultUser();
        return n;
    }
}
