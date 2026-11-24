import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

/**
 * Simula, SIN EJECUTAR NADA, lo que hace hbm2ddl.auto=update al arrancar Tomcat: con los mappings y el
 * hibernate.cfg.xml del WAR desplegado, compara contra el esquema de una base y escribe los ALTER/CREATE
 * que Hibernate intentaría. SchemaUpdate.execute(script=true, doUpdate=false): solo genera el texto.
 *   produccion -> host de la URL del WAR, usuario claude_lectura (solo SELECT: aunque quisiera, no podría
 *                 alterar nada). Se niega si el usuario no es claude_lectura.
 *   control    -> 127.0.0.1:3308 (copia local 8.0.31), root.
 *   local84    -> 127.0.0.1:3310 (contenedor temporal 8.4), root; contraseña en el archivo del 4o argumento.
 * Se corre con el classpath del WAR desempacado: WEB-INF/classes y WEB-INF/lib/*.
 * Uso: java -cp "classes:lib/*" SimulaHbm2ddl.java <produccion|control|local84> <archivo_salida> [archivo_pw_local84]
 */
public class SimulaHbm2ddl {
    public static void main(String[] a) throws Exception {
        Configuration cfg = new Configuration().configure();
        String urlApp = cfg.getProperty("hibernate.connection.url").trim();
        Matcher m = Pattern.compile("jdbc:mysql://([^/]+)/(\\w+)(\\?.*)?").matcher(urlApp);
        if (!m.matches()) throw new IllegalStateException("URL de la app no reconocida");
        String hostApp = m.group(1), base = m.group(2), params = m.group(3) == null ? "" : m.group(3);
        String home = System.getProperty("user.home");
        String url, usuario, pw;
        switch (a[0]) {
            case "produccion": {
                Path env = Paths.get(home, ".config", "caja-independencia", "mysql-lectura.env");
                url = "jdbc:mysql://" + hostApp + "/" + base + params;
                usuario = env(env, "MYSQL_USER"); pw = env(env, "MYSQL_PASSWORD");
                if (!"claude_lectura".equals(usuario)) throw new IllegalStateException("BLOQUEADO: en producción solo claude_lectura");
                break;
            }
            case "control":
                url = "jdbc:mysql://127.0.0.1:3308/" + base + params; usuario = "root";
                pw = env(Paths.get(home, ".config", "caja-independencia", "mysql-control.env"), "MYSQL_ROOT_PASSWORD");
                break;
            case "local84":
                url = "jdbc:mysql://127.0.0.1:3310/" + base + params; usuario = "root";
                pw = new String(Files.readAllBytes(Paths.get(a[2])), StandardCharsets.UTF_8).trim();
                break;
            default: throw new IllegalArgumentException("destino: produccion | control | local84");
        }
        // Conexión directa y sencilla: sin pool c3p0 y sin las propiedades SSL sueltas del cfg.
        List<String> quitar = new ArrayList<>();
        for (Object k : cfg.getProperties().keySet()) {
            String s = k.toString();
            if (s.startsWith("hibernate.c3p0") || s.equals("hibernate.connection.useSSL") || s.equals("hibernate.connection.requireSSL")
                    || s.equals("hibernate.connection.verifyServerCertificate") || s.equals("hibernate.hbm2ddl.auto") || s.equals("hbm2ddl.auto")) quitar.add(s);
        }
        for (String k : quitar) cfg.getProperties().remove(k);
        cfg.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        cfg.setProperty("hibernate.connection.url", url);
        cfg.setProperty("hibernate.connection.username", usuario);
        cfg.setProperty("hibernate.connection.password", pw);
        cfg.setProperty("hibernate.connection.pool_size", "1");
        cfg.setProperty("hibernate.format_sql", "false"); // una sentencia por línea

        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        java.io.PrintStream original = System.out;
        System.setOut(new java.io.PrintStream(buf, true, "UTF-8"));
        SchemaUpdate su = new SchemaUpdate(cfg);
        su.setHaltOnError(false);
        try {
            su.execute(true, false); // genera el script; NO lo ejecuta
        } finally {
            System.setOut(original);
        }
        List<String> out = new ArrayList<>();
        for (String l : buf.toString("UTF-8").split("\n")) {
            String t = l.trim();
            if (t.isEmpty()) continue;
            if (t.matches("(?is)^(alter|create|drop)\\s.*")) out.add(t);
            else if (!out.isEmpty() && Character.isWhitespace(l.charAt(0))) out.set(out.size() - 1, out.get(out.size() - 1) + " " + t);
        }
        for (Object e : su.getExceptions()) out.add("EXCEPCION\t" + e);
        Files.write(Paths.get(a[1]), out, StandardCharsets.UTF_8);
        System.out.println("Destino: " + a[0] + " | sentencias que Hibernate intentaría al arrancar: "
                + out.stream().filter(s -> !s.startsWith("EXCEPCION")).count() + " | errores: " + su.getExceptions().size());
        for (String l : out) System.out.println("  " + l);
    }

    static String env(Path f, String k) throws Exception {
        for (String l : Files.readAllLines(f)) if (l.startsWith(k + "=")) return l.substring(k.length() + 1).trim().replaceAll("^\"|\"$", "");
        throw new IllegalStateException("falta " + k + " en " + f);
    }
}
