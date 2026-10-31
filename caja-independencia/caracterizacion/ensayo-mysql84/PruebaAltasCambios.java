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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.administrador.algoritmopagos.CargaPagosMovimientosBo;
import mx.com.evoti.bo.altasCambiosEmpresa.AltasBo;
import mx.com.evoti.bo.altasCambiosEmpresa.CambiosEmpresaBo;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.config.HibernateUtil;

/**
 * Reproduce el flujo de aportaciones con cambios de empresa y altas:
 *   1. carga del archivo de aportaciones (CargaPagosMovimientosBo.procesaExcel tipo 2);
 *   2. "Aplicar cambios" (CambiosEmpresaBo.realizaCambioEmpresa), como CambiosEmpresaBean;
 *   3. alta de cada pendiente con clave 99010x, como AltasUsuarioBean (configuraNombre + guardarUsuario
 *      + insertaAltaHist + actualizaUsuIdEnPagosYMov).
 * CANDADO: igual que PruebaProcesos.
 * Uso: PruebaAltasCambios <modo> <ip:puerto> <archivo.xlsx> <nombre_archivo> <catorcena yyyy-MM-dd> <dir_salida>
 */
public class PruebaAltasCambios {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1], xlsx = a[2], nombreArchivo = a[3];
        Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(a[4]);
        Path salida = Paths.get(a[5]);
        PrintStream consola = System.out;
        URL cfg = PruebaAltasCambios.class.getClassLoader().getResource("hibernate.cfg.xml");
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
        StringBuilder res = new StringBuilder();

        CargaPagosMovimientosBo carga = new CargaPagosMovimientosBo();
        res.append("es catorcena: ").append(carga.validaSiEsCatorcena(fecha)).append(" | nombre ya existe: ").append(carga.validaArchivoExiste(nombreArchivo)).append('\n');
        carga.procesaExcel(nombreArchivo, xlsx, 2);

        int cambios = new CambiosEmpresaBo().realizaCambioEmpresa(fecha);
        res.append("cambios de empresa aplicados: ").append(cambios).append('\n');

        AltasBo altas = new AltasBo();
        String f = new SimpleDateFormat("yyyy-MM-dd").format(fecha);
        List<MovimientosDto> pendientes = altas.obtenerHistorialCambiosAltas();
        for (MovimientosDto p : pendientes) {
            boolean deEstaCatorcena = p.getMovFecha() != null && new SimpleDateFormat("yyyy-MM-dd").format(p.getMovFecha()).equals(f);
            if (!deEstaCatorcena || p.getMovClaveEmpleado() == null || p.getMovClaveEmpleado() < 990101 || p.getMovClaveEmpleado() > 990199) continue;
            // configuraNombre() de AltasUsuarioBean para nombres de tres palabras (paterno materno nombre)
            String[] w = p.getMovNombreEmpleado().split(" ", 10);
            UsuarioDto nuevo = new UsuarioDto();
            nuevo.setPaterno(w[0].toUpperCase());
            nuevo.setMaterno(w[1].toUpperCase());
            nuevo.setNombre(w[2].toUpperCase());
            nuevo.setPassword(w[0].toUpperCase());
            nuevo.setCveEmpleado(p.getMovClaveEmpleado());
            nuevo.setIdEmpresa(p.getMovEmpresa());
            Integer usuId = altas.guardarUsuario(nuevo);
            nuevo.setId(usuId);
            altas.insertaAltaHist(p, nuevo);
            altas.actualizaUsuIdEnPagosYMov(p.getMovFecha());
            res.append("alta ").append(p.getMovClaveEmpleado()).append(" -> usu_id ").append(usuId).append('\n');
        }
        Files.write(salida.resolve("altas-cambios.txt"), res.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] cambios de empresa: " + cambios);
        System.exit(0);
    }
}
