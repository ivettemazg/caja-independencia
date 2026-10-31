import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Huella de la base `sindicato` (SOLO LECTURA): versión de MySQL, CHECKSUM TABLE de cada tabla,
 * eventos programados, rutinas y método de autenticación de los usuarios conocidos.
 * Sirve para comparar antes y después de la actualización a 8.4: las tablas deben quedar idénticas.
 *   produccion -> host de la configuración versionada en main, usuario claude_lectura
 *                 (~/.config/caja-independencia/mysql-lectura.env). La conexión se marca de solo lectura.
 *   control    -> 127.0.0.1:3308 (contenedor local caja-mysql80-control), para probar la herramienta.
 * Uso: java -cp mysql-connector-j.jar HuellasBase.java <produccion|control> <archivo_salida>
 */
public class HuellasBase {
    public static void main(String[] a) throws Exception {
        String destino, usuario, pw;
        Path cfgEnv;
        if (a[0].equals("produccion")) {
            Process p = new ProcessBuilder("git", "show", "main:caja-independencia/src/main/resources/hibernate.cfg.xml").start();
            String cfg = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/sindicato").matcher(cfg);
            if (!m.find()) throw new IllegalStateException("No se encontró la URL de producción en main");
            destino = m.group(1);
            cfgEnv = Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-lectura.env");
            usuario = env(cfgEnv, "MYSQL_USER"); pw = env(cfgEnv, "MYSQL_PASSWORD");
            if (!"claude_lectura".equals(usuario)) throw new IllegalStateException("BLOQUEADO: en producción solo se usa claude_lectura");
        } else if (a[0].equals("control")) {
            destino = "127.0.0.1:3308";
            usuario = "root"; pw = env(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-control.env"), "MYSQL_ROOT_PASSWORD");
        } else throw new IllegalArgumentException("destino: produccion | control");

        List<String> out = new ArrayList<>();
        try (Connection c = DriverManager.getConnection("jdbc:mysql://" + destino + "/sindicato?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=15000", usuario, pw);
             Statement st = c.createStatement()) {
            c.setReadOnly(true);
            try (ResultSet rs = st.executeQuery("select @@version, @@sql_mode, @@event_scheduler")) {
                rs.next(); out.add("version\t" + rs.getString(1)); out.add("sql_mode\t" + rs.getString(2)); out.add("event_scheduler\t" + rs.getString(3));
            }
            List<String> tablas = new ArrayList<>();
            try (ResultSet rs = st.executeQuery("select table_name from information_schema.tables where table_schema = 'sindicato' and table_type = 'BASE TABLE' order by 1")) {
                while (rs.next()) tablas.add(rs.getString(1));
            }
            out.add("tablas\t" + tablas.size());
            long t = System.currentTimeMillis();
            for (String tabla : tablas) {
                try (ResultSet rs = st.executeQuery("checksum table sindicato.`" + tabla + "`")) {
                    rs.next(); out.add("checksum\t" + tabla + "\t" + rs.getString(2));
                }
                try (ResultSet rs = st.executeQuery("select count(*) from sindicato.`" + tabla + "`")) {
                    rs.next(); out.add("filas\t" + tabla + "\t" + rs.getLong(1));
                }
            }
            try (ResultSet rs = st.executeQuery("select event_name, status, interval_value, interval_field from information_schema.events where event_schema = 'sindicato' order by 1")) {
                while (rs.next()) out.add("evento\t" + rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + " " + rs.getString(4));
            }
            try (ResultSet rs = st.executeQuery("select routine_name, routine_type from information_schema.routines where routine_schema = 'sindicato' order by 1")) {
                while (rs.next()) out.add("rutina\t" + rs.getString(1) + "\t" + rs.getString(2));
            }
            System.out.println("Huella de " + tablas.size() + " tablas en " + (System.currentTimeMillis() - t) / 1000 + " s");
        }
        Files.write(Paths.get(a[1]), out, StandardCharsets.UTF_8);
        for (String l : out) if (!l.startsWith("checksum") && !l.startsWith("filas")) System.out.println(l);
    }

    static String env(Path f, String k) throws Exception {
        for (String l : Files.readAllLines(f)) if (l.startsWith(k + "=")) return l.substring(k.length() + 1).trim().replaceAll("^\"|\"$", "");
        throw new IllegalStateException("falta " + k + " en " + f);
    }
}
