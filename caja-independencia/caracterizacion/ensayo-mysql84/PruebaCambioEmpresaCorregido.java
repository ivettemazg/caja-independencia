import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.altasCambiosEmpresa.CambiosEmpresaBo;
import mx.com.evoti.dao.altasCambiosEmp.CambiosEmpresaDao;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Util;

/**
 * Misma regla que CambiosEmpresaBo.realizaCambioEmpresa(), pero buscando el último movimiento con
 * getUltimosMovs(usuId, 0) en lugar de getUltimosMovs(usuId, null): con null el SQL queda
 * "mov_id <> null", nunca regresa filas y el cambio jamás se aplica (error del código actual).
 * Las escrituras son las de la aplicación: CambiosEmpresaBo.updtCambioEmpDB().
 * Solo considera los pendientes de la catorcena con clave 990001..990099 (los del archivo de prueba).
 * CANDADO: igual que PruebaProcesos.
 * Uso: PruebaCambioEmpresaCorregido <modo> <ip:puerto> <catorcena yyyy-MM-dd> <dir_salida>
 */
public class PruebaCambioEmpresaCorregido {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(a[2]);
        Path salida = Paths.get(a[3]);
        PrintStream consola = System.out;
        URL cfg = PruebaCambioEmpresaCorregido.class.getClassLoader().getResource("hibernate.cfg.xml");
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

        CambiosEmpresaBo bo = new CambiosEmpresaBo();
        CambiosEmpresaDao dao = new CambiosEmpresaDao();
        StringBuilder res = new StringBuilder();
        int cambios = 0;
        List<MovimientosDto> movs = dao.getPendtsCambiosAltas(Util.generaFechaFormateada(fecha));
        res.append("pendientes de la catorcena: ").append(movs.size()).append('\n');
        for (MovimientosDto mov : movs) {
            if (mov.getMovClaveEmpleado() == null || mov.getMovClaveEmpleado() < 990001 || mov.getMovClaveEmpleado() > 990099) continue;
            List<Usuarios> coincidencias = bo.comparaNombres(mov);
            res.append("clave ").append(mov.getMovClaveEmpleado()).append(" | coincidencias ").append(coincidencias.size()).append('\n');
            for (Usuarios usu : coincidencias) {
                MovimientosDto ultimo = dao.getUltimosMovs(usu.getUsuId(), 0);
                if (ultimo == null) { res.append("  usu ").append(usu.getUsuId()).append(": sin movimientos\n"); continue; }
                if (mov.getMovFecha().after(ultimo.getMovFecha()) && !Objects.equals(mov.getMovEmpresa(), ultimo.getMovEmpresa())) {
                    res.append("  usu ").append(usu.getUsuId()).append(": cambio de empresa ").append(ultimo.getMovEmpresa()).append(" -> ").append(mov.getMovEmpresa())
                       .append(" (último movimiento ").append(new SimpleDateFormat("yyyy-MM-dd").format(ultimo.getMovFecha())).append(")\n");
                    bo.updtCambioEmpDB(usu, mov);
                    cambios++;
                } else {
                    res.append("  usu ").append(usu.getUsuId()).append(": homónimo\n");
                }
            }
        }
        res.append("cambios aplicados: ").append(cambios).append('\n');
        Files.write(salida.resolve("cambios.txt"), res.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] cambios de empresa aplicados: " + cambios);
        System.exit(0);
    }
}
