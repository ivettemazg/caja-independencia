import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Prueba la conexión a la instancia de ENSAYO con las mismas opciones de URL que usa el sistema
 * (useSSL=false, allowPublicKeyRetrieval=true). Solo lee. Se niega a usar la IP de producción.
 * Uso: java -cp mysql-connector-j.jar PruebaConexionApp.java <ip_ensayo>
 */
public class PruebaConexionApp {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("35.225.67.182")) throw new IllegalStateException("BLOQUEADO: IP de produccion.");
        String usuario = null, pw = null;
        for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "ensayo.env"))) {
            if (l.startsWith("ENSAYO_APP_USER=")) usuario = l.substring("ENSAYO_APP_USER=".length());
            if (l.startsWith("ENSAYO_APP_PASSWORD=")) pw = l.substring("ENSAYO_APP_PASSWORD=".length());
        }
        String url = "jdbc:mysql://" + args[0] + ":3306/sindicato?zeroDateTimeBehavior=convertToNull&autoReconnect=true"
                + "&allowPublicKeyRetrieval=true&useSSL=false&connectTimeout=15000";
        try (Connection c = DriverManager.getConnection(url, usuario, pw); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("select current_user(), @@version, (select count(*) from usuarios)")) {
            rs.next();
            System.out.println("Conexion con opciones del sistema: OK | usuario " + rs.getString(1)
                    + " | MySQL " + rs.getString(2) + " | socios " + rs.getString(3));
        }
    }
}
