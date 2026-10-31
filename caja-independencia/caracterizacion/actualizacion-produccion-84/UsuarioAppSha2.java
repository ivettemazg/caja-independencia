import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Cambia el método de autenticación del usuario de la APLICACIÓN a caching_sha2_password conservando
 * la MISMA contraseña, que se toma del hibernate.cfg.xml del WAR desplegado (nunca se imprime).
 * Es lo que se probó en el ensayo de 8.4. Se puede revertir a mysql_native_password con "revertir".
 *   ver       -> solo muestra el método actual (lectura).
 *   cambiar   -> ALTER USER CURRENT_USER() ... caching_sha2_password y vuelve a conectar con la URL exacta de la app.
 *   revertir  -> ALTER USER CURRENT_USER() ... mysql_native_password y vuelve a conectar.
 * "cambiar" y "revertir" exigen el texto de confirmación exacto como último argumento.
 * Uso: java -cp mysql-connector-j.jar UsuarioAppSha2.java <ruta Sindicato.war | hibernate.cfg.xml> <ver|cambiar|revertir> [CONFIRMO-<host>]
 */
public class UsuarioAppSha2 {
    public static void main(String[] a) throws Exception {
        String cfg = leeCfg(a[0]);
        String url = prop(cfg, "hibernate.connection.url").replace("&amp;", "&");
        String usuario = prop(cfg, "hibernate.connection.username"), pw = prop(cfg, "hibernate.connection.password");
        Matcher m = Pattern.compile("jdbc:mysql://([^:/?]+)").matcher(url);
        String host = m.find() ? m.group(1) : "?";
        System.out.println("Base: " + host + " | usuario de la aplicación: " + usuario);
        System.out.println("Método actual: " + metodo(url, usuario, pw));
        if (a[1].equals("ver")) return;
        String plugin = a[1].equals("cambiar") ? "caching_sha2_password" : a[1].equals("revertir") ? "mysql_native_password" : null;
        if (plugin == null) throw new IllegalArgumentException("acción: ver | cambiar | revertir");
        String esperado = "CONFIRMO-" + host;
        if (a.length < 3 || !a[2].equals(esperado)) {
            System.out.println("NO SE HIZO NADA. Para " + a[1] + " escribe como último argumento: " + esperado); System.exit(3);
        }
        try (Connection c = DriverManager.getConnection(url, usuario, pw); Statement st = c.createStatement()) {
            st.execute("ALTER USER CURRENT_USER() IDENTIFIED WITH " + plugin + " BY '" + pw.replace("'", "''") + "'");
        }
        System.out.println("Hecho. Método nuevo (conexión nueva con la URL exacta de la app): " + metodo(url, usuario, pw));
    }

    static String metodo(String url, String usuario, String pw) throws Exception {
        try (Connection c = DriverManager.getConnection(url + (url.contains("?") ? "&" : "?") + "connectTimeout=15000", usuario, pw);
             Statement st = c.createStatement(); ResultSet rs = st.executeQuery("show create user current_user()")) {
            rs.next();
            Matcher m = Pattern.compile("IDENTIFIED WITH '([a-z_0-9]+)'").matcher(rs.getString(1));
            return m.find() ? m.group(1) : "(desconocido)";
        }
    }

    static String leeCfg(String ruta) throws Exception {
        if (ruta.endsWith(".war")) {
            try (ZipFile z = new ZipFile(ruta)) {
                ZipEntry e = z.getEntry("WEB-INF/classes/hibernate.cfg.xml");
                try (InputStream in = z.getInputStream(e)) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); }
            }
        }
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(ruta)), StandardCharsets.UTF_8);
    }

    static String prop(String c, String nombre) {
        Matcher m = Pattern.compile("<property name=\"" + Pattern.quote(nombre) + "\">\\s*(.*?)\\s*</property>", Pattern.DOTALL).matcher(c);
        if (!m.find()) throw new IllegalStateException("falta " + nombre);
        return m.group(1);
    }
}
