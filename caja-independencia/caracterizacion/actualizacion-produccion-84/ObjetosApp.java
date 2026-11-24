import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Objetos programados de la base `sindicato` (SOLO LECTURA): eventos, rutinas (con huella de su cuerpo) y
 * disparadores. Usa el usuario de la APLICACIÓN porque claude_lectura no alcanza a ver el evento ni la rutina
 * (no es su DEFINER). La conexión se marca de solo lectura y solo consulta information_schema.
 *   <ruta Sindicato.war | hibernate.cfg.xml> -> credenciales de la app (nunca se imprimen).
 *   control                                -> 127.0.0.1:3308 con root, para probar la herramienta.
 * Uso: java -cp mysql-connector-j.jar ObjetosApp.java <war|cfg|control> <archivo_salida>
 */
public class ObjetosApp {
    public static void main(String[] a) throws Exception {
        String url, usuario, pw;
        if (a[0].equals("control")) {
            url = "jdbc:mysql://127.0.0.1:3308/sindicato?useSSL=false&allowPublicKeyRetrieval=true";
            usuario = "root";
            pw = env(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-control.env"), "MYSQL_ROOT_PASSWORD");
        } else {
            String cfg = leeCfg(a[0]);
            url = prop(cfg, "hibernate.connection.url").replace("&amp;", "&");
            usuario = prop(cfg, "hibernate.connection.username");
            pw = prop(cfg, "hibernate.connection.password");
        }
        List<String> out = new ArrayList<>(), info = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(url + (url.contains("?") ? "&" : "?") + "connectTimeout=15000", usuario, pw);
             Statement st = c.createStatement()) {
            c.setReadOnly(true);
            try (ResultSet rs = st.executeQuery("select @@version, @@event_scheduler")) {
                rs.next(); info.add("version\t" + rs.getString(1)); out.add("event_scheduler\t" + rs.getString(2));
            }
            try (ResultSet rs = st.executeQuery("select event_name, status, interval_value, interval_field, starts, event_definition, last_executed"
                    + " from information_schema.events where event_schema = 'sindicato' order by 1")) {
                while (rs.next()) {
                    out.add("evento\t" + rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + " " + rs.getString(4)
                            + "\tdesde " + rs.getString(5) + "\tcuerpo " + huella(rs.getString(6)));
                    info.add("ultima_ejecucion\t" + rs.getString(1) + "\t" + rs.getString(7));
                }
            }
            try (ResultSet rs = st.executeQuery("select routine_name, routine_type, routine_definition"
                    + " from information_schema.routines where routine_schema = 'sindicato' order by 1")) {
                while (rs.next()) out.add("rutina\t" + rs.getString(1) + "\t" + rs.getString(2) + "\tcuerpo " + huella(rs.getString(3)));
            }
            try (ResultSet rs = st.executeQuery("select count(*) from information_schema.triggers where trigger_schema = 'sindicato'")) {
                rs.next(); out.add("disparadores\t" + rs.getLong(1));
            }
        }
        List<String> todo = new ArrayList<>(info);
        todo.addAll(out);
        Files.write(Paths.get(a[1]), todo, StandardCharsets.UTF_8);
        for (String l : todo) System.out.println(l);
    }

    static String huella(String s) throws Exception {
        if (s == null) return "(no visible)";
        byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 6; i++) b.append(String.format("%02x", h[i]));
        return b.toString();
    }

    static String leeCfg(String ruta) throws Exception {
        if (ruta.endsWith(".war")) {
            try (ZipFile z = new ZipFile(ruta)) {
                ZipEntry e = z.getEntry("WEB-INF/classes/hibernate.cfg.xml");
                try (InputStream in = z.getInputStream(e)) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); }
            }
        }
        return new String(Files.readAllBytes(Paths.get(ruta)), StandardCharsets.UTF_8);
    }

    static String prop(String c, String nombre) {
        Matcher m = Pattern.compile("<property name=\"" + Pattern.quote(nombre) + "\">\\s*(.*?)\\s*</property>", Pattern.DOTALL).matcher(c);
        if (!m.find()) throw new IllegalStateException("falta " + nombre);
        return m.group(1);
    }

    static String env(Path f, String k) throws Exception {
        for (String l : Files.readAllLines(f)) if (l.startsWith(k + "=")) return l.substring(k.length() + 1).trim().replaceAll("^\"|\"$", "");
        throw new IllegalStateException("falta " + k + " en " + f);
    }
}
