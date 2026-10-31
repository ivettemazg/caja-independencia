import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Consultas de SOLO LECTURA contra la instancia de la nube.
 *
 * - Toma host, usuario y contraseña de hibernate.cfg.xml en tiempo de ejecución; no se copian aquí.
 * - Conecta sin esquema por defecto: cada consulta debe calificar `sindicato.` o `pruebas.`.
 * - Abre la sesión en modo read-only del lado del servidor y rechaza cualquier sentencia que no sea
 *   select/show/describe/explain. Tope de 60 s por consulta. No escribe nada a disco.
 *
 * Uso: java -cp mysql-connector-j.jar ConsultaSoloLectura.java <hibernate.cfg.xml> <consultas.sql>
 * En el archivo, cada consulta termina en ";" al final de línea; las líneas "-- texto" son títulos.
 */
public class ConsultaSoloLectura {

    private static final int MAX_FILAS = 200;

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document doc = f.newDocumentBuilder().parse(new File(args[0]));
        Map<String, String> cfg = new HashMap<>();
        NodeList nl = doc.getElementsByTagName("property");
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            cfg.put(e.getAttribute("name"), e.getTextContent().trim());
        }

        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)").matcher(cfg.get("hibernate.connection.url"));
        if (!m.find()) {
            throw new IllegalStateException("No se pudo leer el host de hibernate.connection.url");
        }
        String url = "jdbc:mysql://" + m.group(1) + "/";

        // Usuario de solo lectura (claude_lectura) si existe su archivo; si no, el de la aplicación.
        String usuario = cfg.get("hibernate.connection.username");
        String password = cfg.get("hibernate.connection.password");
        File lectura = Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-lectura.env").toFile();
        if (lectura.exists()) {
            for (String l : Files.readAllLines(lectura.toPath())) {
                if (l.startsWith("MYSQL_USER=")) usuario = l.substring("MYSQL_USER=".length());
                if (l.startsWith("MYSQL_PASSWORD=")) password = l.substring("MYSQL_PASSWORD=".length());
            }
        }

        Properties p = new Properties();
        p.setProperty("user", usuario);
        p.setProperty("password", password);
        p.setProperty("sslMode", "PREFERRED");
        p.setProperty("allowPublicKeyRetrieval", "true");
        p.setProperty("zeroDateTimeBehavior", "CONVERT_TO_NULL");
        p.setProperty("connectTimeout", "15000");
        p.setProperty("socketTimeout", "120000");
        p.setProperty("sessionVariables", "max_execution_time=60000");
        DriverManager.setLoginTimeout(20);

        String texto = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);

        try (Connection c = DriverManager.getConnection(url, p)) {
            c.setReadOnly(true);
            System.out.println("Conectado a " + m.group(1) + " como " + usuario + " | readOnly=" + c.isReadOnly());
            for (String bloque : texto.split(";\\s*(\\n|$)")) {
                StringBuilder sql = new StringBuilder();
                for (String linea : bloque.split("\n")) {
                    if (linea.trim().startsWith("--")) {
                        System.out.println("\n=== " + linea.trim().substring(2).trim() + " ===");
                    } else if (!linea.trim().isEmpty()) {
                        sql.append(linea).append('\n');
                    }
                }
                String s = sql.toString().trim();
                if (s.isEmpty()) {
                    continue;
                }
                String verbo = s.split("\\s+")[0].toLowerCase();
                if (!verbo.matches("select|show|describe|desc|explain")) {
                    System.out.println("RECHAZADA (no es de lectura): " + verbo);
                    continue;
                }
                long t0 = System.currentTimeMillis();
                try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(s)) {
                    ResultSetMetaData md = rs.getMetaData();
                    int n = md.getColumnCount();
                    StringBuilder h = new StringBuilder();
                    for (int i = 1; i <= n; i++) {
                        h.append(i > 1 ? " | " : "").append(md.getColumnLabel(i));
                    }
                    System.out.println(h);
                    int filas = 0;
                    while (rs.next()) {
                        if (++filas > MAX_FILAS) {
                            continue;
                        }
                        StringBuilder r = new StringBuilder();
                        for (int i = 1; i <= n; i++) {
                            r.append(i > 1 ? " | " : "").append(rs.getString(i));
                        }
                        System.out.println(r);
                    }
                    System.out.println("(" + filas + " filas, " + (System.currentTimeMillis() - t0) + " ms)");
                } catch (Exception ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                }
            }
        }
    }
}
