import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

/**
 * Ejecuta un archivo de SQL ÚNICAMENTE contra la instancia de ensayo.
 *
 * Doble candado:
 *  1. Se niega a conectarse si la IP es la de producción.
 *  2. Usa las contraseñas de ensayo (~/.config/caja-independencia/ensayo.env), que son distintas a las
 *     de producción: aunque la IP fuera la equivocada, producción rechazaría la conexión.
 *
 * Uso: java -cp mysql-connector-j.jar SqlEnsayo.java <ip_ensayo> <app|lectura> <archivo.sql>
 * En el archivo, cada sentencia termina en ";" al final de línea; las líneas "-- texto" son títulos.
 * La contraseña nunca se imprime: en la salida aparece como ********.
 */
public class SqlEnsayo {
    private static final String IP_PRODUCCION = "35.225.67.182";

    public static void main(String[] args) throws Exception {
        String ip = args[0];
        if (ip.equals(IP_PRODUCCION) || ip.isEmpty()) {
            throw new IllegalStateException("BLOQUEADO: esa es la IP de produccion. No se ejecuto nada.");
        }
        String prefijo = args[1].equals("app") ? "ENSAYO_APP_" : "ENSAYO_LECTURA_";
        String usuario = null, pw = null;
        for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "ensayo.env"))) {
            if (l.startsWith(prefijo + "USER=")) usuario = l.substring((prefijo + "USER=").length());
            if (l.startsWith(prefijo + "PASSWORD=")) pw = l.substring((prefijo + "PASSWORD=").length());
        }
        String texto = new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8);
        // Permite usar {{PW_APP}} en el archivo para la contraseña de ensayo de la aplicación.
        String pwApp = null;
        for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "ensayo.env"))) {
            if (l.startsWith("ENSAYO_APP_PASSWORD=")) pwApp = l.substring("ENSAYO_APP_PASSWORD=".length());
        }

        Properties p = new Properties();
        p.setProperty("user", usuario);
        p.setProperty("password", pw);
        p.setProperty("sslMode", "PREFERRED");
        p.setProperty("allowPublicKeyRetrieval", "true");
        p.setProperty("connectTimeout", "15000");
        p.setProperty("socketTimeout", "600000");

        try (Connection c = DriverManager.getConnection("jdbc:mysql://" + ip + "/", p); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("select @@hostname, @@version")) {
                rs.next();
                System.out.println("Conectado a la instancia de ENSAYO " + ip + " como " + usuario + " | MySQL " + rs.getString(2));
            }
            for (String bloque : texto.split(";\\s*(\\n|$)")) {
                StringBuilder sql = new StringBuilder();
                for (String linea : bloque.split("\n")) {
                    if (linea.trim().startsWith("--")) System.out.println("\n=== " + linea.trim().substring(2).trim() + " ===");
                    else if (!linea.trim().isEmpty()) sql.append(linea).append('\n');
                }
                String s = sql.toString().trim();
                if (s.isEmpty()) continue;
                String real = s.replace("{{PW_APP}}", pwApp == null ? "" : pwApp);
                System.out.println("> " + s);
                try {
                    if (st.execute(real)) {
                        try (ResultSet rs = st.getResultSet()) {
                            ResultSetMetaData md = rs.getMetaData();
                            int filas = 0;
                            while (rs.next()) {
                                StringBuilder r = new StringBuilder();
                                for (int i = 1; i <= md.getColumnCount(); i++) r.append(i > 1 ? " | " : "").append(rs.getString(i));
                                String linea = r.toString();
                                if (pwApp != null && !pwApp.isEmpty()) linea = linea.replace(pwApp, "********");
                                System.out.println(linea);
                                filas++;
                            }
                            System.out.println("(" + filas + " filas)");
                        }
                    } else {
                        System.out.println("(ok, " + st.getUpdateCount() + " filas afectadas)");
                    }
                } catch (Exception ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                }
            }
        }
    }
}
