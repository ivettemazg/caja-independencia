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
 * Obtiene (SOLO LECTURA) el plan de ejecución de cada EXPLAIN de planes.sql y escribe dos archivos:
 *   <salida>.txt         el plan completo, sin costos ni estimaciones de renglones;
 *   <salida>.esqueleto   solo lo que decide el orden: tipo de acceso, tabla, índice y tipo de unión.
 * Destinos:
 *   produccion -> host de hibernate.cfg.xml de main, usuario claude_lectura (se niega con otro usuario).
 *   control    -> 127.0.0.1:3308 (copia local 8.0.31), root.
 *   local84    -> 127.0.0.1:3310 (contenedor temporal 8.4), root; contraseña en el archivo del 4o argumento.
 * Solo ejecuta sentencias que empiezan con "explain"; la conexión se marca de solo lectura.
 * Uso: java -cp mysql-connector-j.jar PlanesSql.java <produccion|control|local84> <planes.sql> <salida_sin_extension> [archivo_pw]
 */
public class PlanesSql {
    public static void main(String[] a) throws Exception {
        String home = System.getProperty("user.home"), destino, usuario, pw;
        switch (a[0]) {
            case "produccion": {
                Process p = new ProcessBuilder("git", "show", "main:caja-independencia/src/main/resources/hibernate.cfg.xml").start();
                String cfg = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/sindicato").matcher(cfg);
                if (!m.find()) throw new IllegalStateException("No se encontró la URL de producción en main");
                destino = m.group(1);
                Path env = Paths.get(home, ".config", "caja-independencia", "mysql-lectura.env");
                usuario = env(env, "MYSQL_USER"); pw = env(env, "MYSQL_PASSWORD");
                if (!"claude_lectura".equals(usuario)) throw new IllegalStateException("BLOQUEADO: en producción solo claude_lectura");
                break;
            }
            case "control":
                destino = "127.0.0.1:3308"; usuario = "root";
                pw = env(Paths.get(home, ".config", "caja-independencia", "mysql-control.env"), "MYSQL_ROOT_PASSWORD");
                break;
            case "local84":
                destino = "127.0.0.1:3310"; usuario = "root";
                pw = new String(Files.readAllBytes(Paths.get(a[3])), StandardCharsets.UTF_8).trim();
                break;
            default: throw new IllegalArgumentException("destino: produccion | control | local84");
        }
        List<String[]> sentencias = new ArrayList<>();
        String titulo = null; StringBuilder sb = new StringBuilder();
        for (String l : Files.readAllLines(Paths.get(a[1]), StandardCharsets.UTF_8)) {
            String t = l.trim();
            if (t.startsWith("-- P")) { titulo = t.substring(3); continue; }
            if (t.startsWith("--") || t.isEmpty()) continue;
            sb.append(t).append(' ');
            if (t.endsWith(";")) {
                String s = sb.toString().trim(); s = s.substring(0, s.length() - 1);
                if (!s.toLowerCase().startsWith("explain ")) throw new IllegalStateException("Solo se permiten EXPLAIN: " + titulo);
                sentencias.add(new String[]{titulo, s}); sb.setLength(0);
            }
        }
        List<String> completo = new ArrayList<>(), esqueleto = new ArrayList<>();
        try (Connection c = DriverManager.getConnection("jdbc:mysql://" + destino + "/sindicato?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=15000", usuario, pw);
             Statement st = c.createStatement()) {
            c.setReadOnly(true);
            try (ResultSet rs = st.executeQuery("select @@version")) { rs.next(); System.out.println("Versión: " + rs.getString(1)); }
            for (String[] s : sentencias) {
                String plan;
                try (ResultSet rs = st.executeQuery(s[1])) { rs.next(); plan = rs.getString(1); }
                completo.add("== " + s[0]); esqueleto.add("== " + s[0]);
                for (String l : plan.split("\n")) {
                    String sinCosto = l.replaceAll("\\s*\\((cost|rows)=[^)]*\\)", "").replaceAll("\\s+$", "");
                    completo.add(sinCosto);
                    esqueleto.add(esqueletoDe(sinCosto));
                }
            }
        }
        Files.write(Paths.get(a[2] + ".txt"), completo, StandardCharsets.UTF_8);
        Files.write(Paths.get(a[2] + ".esqueleto"), esqueleto, StandardCharsets.UTF_8);
        System.out.println(sentencias.size() + " planes guardados en " + a[2] + ".txt y .esqueleto");
    }

    /** Conserva la sangría y la operación; quita las condiciones (cuyo texto cambia entre versiones sin cambiar el plan). */
    static String esqueletoDe(String l) {
        int i = 0; while (i < l.length() && (l.charAt(i) == ' ' || l.charAt(i) == '-' || l.charAt(i) == '>')) i++;
        String sangria = l.substring(0, i), op = l.substring(i);
        // "covering" solo dice que no hace falta leer la tabla: mismo índice, mismo orden.
        op = op.replaceAll("^Covering index", "Index").replaceAll(" covering index", " index");
        op = op.replaceAll("^(Filter|Hash|Sort|Remove duplicates from input sorted on|Group aggregate|Aggregate):.*$", "$1")
               .replaceAll("^(Inner hash join|Left hash join|Hash semijoin|Hash antijoin|Nested loop [a-z ]+join|Hash [a-z ]+join)\\b.*$", "$1")
               .replaceAll("\\s*\\(([a-z_.]+) (=|<|>|<=|>=|is null|like) [^)]*\\)", "")
               .replaceAll(" over \\(.*$", "")
               .replaceAll(", with index condition: .*$", ", with index condition")
               .replaceAll("Limit: \\d+ row\\(s\\)", "Limit");
        return sangria + op;
    }

    static String env(Path f, String k) throws Exception {
        for (String l : Files.readAllLines(f)) if (l.startsWith(k + "=")) return l.substring(k.length() + 1).trim().replaceAll("^\"|\"$", "");
        throw new IllegalStateException("falta " + k + " en " + f);
    }
}
