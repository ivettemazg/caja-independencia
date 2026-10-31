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
 * Ejecuta un archivo de SQL ÚNICAMENTE contra la base de CONTROL local (docker caja-mysql80-control,
 * 127.0.0.1:3308, esquema sindicato).
 *
 * Candados:
 *  1. El destino está fijo en el código (127.0.0.1:3308); no acepta otro host.
 *  2. Usa la contraseña root del control (~/.config/caja-independencia/mysql-control.env), que no
 *     existe en producción.
 *  3. Modo "lectura" (por omisión) abre la conexión en solo lectura; "escritura" hay que pedirlo.
 *
 * Uso: java -cp mysql-connector-j.jar SqlControl.java <lectura|escritura> <archivo.sql>
 * En el archivo, cada sentencia termina en ";" al final de línea; las líneas "-- texto" son títulos.
 * La contraseña nunca se imprime.
 */
public class SqlControl {
    private static final String DESTINO = "127.0.0.1:3308";

    public static void main(String[] args) throws Exception {
        boolean escritura = args[0].equals("escritura");
        String pw = null;
        for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-control.env"))) {
            if (l.startsWith("MYSQL_ROOT_PASSWORD=")) pw = l.substring("MYSQL_ROOT_PASSWORD=".length());
        }
        String texto = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);
        Properties p = new Properties();
        p.setProperty("user", "root");
        p.setProperty("password", pw);
        p.setProperty("useSSL", "false");
        p.setProperty("allowPublicKeyRetrieval", "true");
        p.setProperty("connectTimeout", "10000");
        try (Connection c = DriverManager.getConnection("jdbc:mysql://" + DESTINO + "/sindicato", p); Statement st = c.createStatement()) {
            c.setReadOnly(!escritura);
            try (ResultSet rs = st.executeQuery("select @@port, @@version")) {
                rs.next();
                System.out.println("Conectado a CONTROL " + DESTINO + " | MySQL " + rs.getString(2) + " | " + (escritura ? "escritura" : "solo lectura"));
            }
            for (String bloque : texto.split(";\\s*(\\n|$)")) {
                StringBuilder sql = new StringBuilder();
                for (String linea : bloque.split("\n")) {
                    if (linea.trim().startsWith("--")) System.out.println("\n=== " + linea.trim().substring(2).trim() + " ===");
                    else if (!linea.trim().isEmpty()) sql.append(linea).append('\n');
                }
                String s = sql.toString().trim();
                if (s.isEmpty()) continue;
                System.out.println("> " + s.replace('\n', ' '));
                try {
                    if (st.execute(s)) {
                        try (ResultSet rs = st.getResultSet()) {
                            ResultSetMetaData md = rs.getMetaData();
                            StringBuilder cab = new StringBuilder();
                            for (int i = 1; i <= md.getColumnCount(); i++) cab.append(i > 1 ? " | " : "").append(md.getColumnLabel(i));
                            System.out.println(cab);
                            int filas = 0;
                            while (rs.next()) {
                                StringBuilder r = new StringBuilder();
                                for (int i = 1; i <= md.getColumnCount(); i++) r.append(i > 1 ? " | " : "").append(rs.getString(i));
                                System.out.println(r);
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
