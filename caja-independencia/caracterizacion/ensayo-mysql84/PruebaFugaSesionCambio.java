import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;
import mx.com.evoti.dao.altasCambiosEmp.CambiosEmpresaDao;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Empresas;
import mx.com.evoti.hibernate.pojos.Usuarios;

/**
 * Diagnóstico (no corrige nada): mide si CambiosEmpresaDao.updateUsuarioClaveEmpresa deja una
 * conexión del pool c3p0 ocupada (abre sesión con beginTransaction() y nunca llama endTransaction()).
 * Ejecuta el UPDATE con los MISMOS valores que el usuario ya tiene (clave y empresa actuales), así
 * que no cambia datos. Compara contra getUltimosMovs, que sí cierra la sesión.
 * CANDADO: igual que PruebaProcesos/PruebaAltasCambios.
 * Uso: PruebaFugaSesionCambio <modo> <ip:puerto> <usu_id> <clave_actual> <empresa_actual> <dir_salida>
 */
public class PruebaFugaSesionCambio {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        int usuId = Integer.parseInt(a[2]), clave = Integer.parseInt(a[3]), empresa = Integer.parseInt(a[4]);
        Path salida = Paths.get(a[5]);
        PrintStream consola = System.out;
        URL cfg = PruebaFugaSesionCambio.class.getClassLoader().getResource("hibernate.cfg.xml");
        String contenido;
        try (Scanner sc = new Scanner(cfg.openStream(), "UTF-8")) { contenido = sc.useDelimiter("\\A").next(); }
        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/").matcher(contenido);
        String destino = m.find() ? m.group(1) : "";
        consola.println("[" + modo + "] configuración cargada: " + cfg + " -> " + destino);
        if (destino.contains("35.225.67.182") || !destino.equals(permitido)) {
            consola.println("BLOQUEADO: la configuración no apunta al destino permitido (" + permitido + ")."); System.exit(2);
        }
        Files.createDirectories(salida);
        System.setOut(new PrintStream(new FileOutputStream(salida.resolve("hibernate-sql-fuga.log").toFile(), true), true));
        HibernateUtil.buildSessionFactory();

        CambiosEmpresaDao dao = new CambiosEmpresaDao();
        consola.println("ocupadas al inicio: " + ocupadas());
        dao.getUltimosMovs(usuId);
        consola.println("ocupadas tras getUltimosMovs: " + ocupadas());
        Usuarios u = new Usuarios();
        u.setUsuId(usuId);
        u.setUsuClaveEmpleado(clave);
        u.setEmpresas(new Empresas(empresa));
        for (int i = 1; i <= 3; i++) {
            dao.updateUsuarioClaveEmpresa(u);
            consola.println("ocupadas tras updateUsuarioClaveEmpresa #" + i + ": " + ocupadas());
        }
        System.exit(0);
    }

    static int ocupadas() throws Exception {
        Thread.sleep(2000); // c3p0 devuelve las conexiones al pool en un hilo auxiliar
        int n = 0;
        for (Object o : C3P0Registry.getPooledDataSources()) n += ((PooledDataSource) o).getNumBusyConnectionsDefaultUser();
        return n;
    }
}
