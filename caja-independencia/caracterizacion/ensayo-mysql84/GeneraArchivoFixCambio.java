import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Genera un archivo de APORTACIONES de prueba (clave, fecha, monto, empresa, nombre, producto) para
 * probar la corrección de CambiosEmpresaDao.getUltimosMovs. Lee SOLO la base de CONTROL local
 * (127.0.0.1:3308, conexión en solo lectura):
 *   - 10 socios activos de la empresa destino, con su clave real (se reconocen directo en la carga);
 *   - 3 socios activos de la empresa origen cuyo último movimiento es de la empresa origen, con
 *     claves nuevas 990021..990023 (cambio de empresa esperado);
 *   - 1 socio activo de la empresa destino (que no está entre los 10) con su nombre real y clave
 *     nueva 990031: su último movimiento es de la misma empresa, debe tratarse como homónimo;
 *   - 2 personas inexistentes con claves 990121 y 990122 (altas, quedan pendientes).
 * Replica la comparación de CambiosEmpresaBo.comparaNombres (prefijo de 3 letras del paterno con
 * inner join a empresas + Levenshtein <= 2) para exigir que cada nombre de cambio/homónimo coincida
 * con exactamente un usuario, y que las altas no coincidan con ninguno. Aborta si alguna clave nueva
 * ya existe en usuarios o en movimientos, o si la catorcena ya tiene movimientos.
 * Uso: GeneraArchivoFixCambio <catorcena yyyy-MM-dd> <empresa_destino> <empresa_origen> <salida.xlsx> <plan.txt>
 *          [<clave_cambio_inicial> <clave_homonimo> <clave_alta_inicial> <paterno_altas>]
 */
public class GeneraArchivoFixCambio {
    static int[] CLAVES_CAMBIO = {990021, 990022, 990023};
    static int CLAVE_HOMONIMO = 990031;
    static int[] CLAVES_ALTA = {990121, 990122};
    static String[] NOMBRES_ALTA = {"ENSAYOFIX ALTA UNO", "ENSAYOFIX ALTA DOS"};

    public static void main(String[] a) throws Exception {
        // Opcionales: <clave_cambio_inicial> <clave_homonimo> <clave_alta_inicial> <paterno_altas> [<numero_de_cambios>]
        if (a.length >= 9) {
            int c0 = Integer.parseInt(a[5]), al = Integer.parseInt(a[7]);
            int n = a.length >= 10 ? Integer.parseInt(a[9]) : 3;
            CLAVES_CAMBIO = new int[n];
            for (int i = 0; i < n; i++) CLAVES_CAMBIO[i] = c0 + i;
            CLAVE_HOMONIMO = Integer.parseInt(a[6]);
            CLAVES_ALTA = new int[]{al, al + 1};
            NOMBRES_ALTA = new String[]{a[8] + " ALTA UNO", a[8] + " ALTA DOS"};
        }
        String cat = a[0];
        int empresa = Integer.parseInt(a[1]), origen = Integer.parseInt(a[2]);
        String pw = null;
        for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-control.env"))) {
            if (l.startsWith("MYSQL_ROOT_PASSWORD=")) pw = l.substring("MYSQL_ROOT_PASSWORD=".length());
        }
        Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(cat);
        StringBuilder plan = new StringBuilder();
        try (Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3308/sindicato?useSSL=false&allowPublicKeyRetrieval=true", "root", pw);
             Statement st = c.createStatement(); XSSFWorkbook wb = new XSSFWorkbook()) {
            c.setReadOnly(true);

            // Verificaciones previas
            List<Integer> nuevas = new ArrayList<>();
            for (int k : CLAVES_CAMBIO) nuevas.add(k);
            nuevas.add(CLAVE_HOMONIMO);
            for (int k : CLAVES_ALTA) nuevas.add(k);
            String lista = nuevas.toString().replace("[", "").replace("]", "");
            try (ResultSet rs = st.executeQuery("select (select count(*) from usuarios where usu_clave_empleado in (" + lista + ")),"
                    + " (select count(*) from movimientos where mov_clave_empleado in (" + lista + ")),"
                    + " (select count(*) from movimientos where mov_fecha = '" + cat + "'),"
                    + " (select count(*) from catorcenas where car_fecha = '" + cat + "')")) {
                rs.next();
                plan.append("claves nuevas en usuarios: ").append(rs.getInt(1)).append(" | en movimientos: ").append(rs.getInt(2))
                    .append(" | movimientos de la catorcena: ").append(rs.getInt(3)).append(" | es catorcena: ").append(rs.getInt(4)).append('\n');
                if (rs.getInt(1) != 0 || rs.getInt(2) != 0 || rs.getInt(3) != 0 || rs.getInt(4) != 1) {
                    System.out.println("ABORTADO: precondiciones no cumplidas\n" + plan); System.exit(3);
                }
            }

            XSSFSheet hoja = wb.createSheet("aportaciones");
            CellStyle ef = wb.createCellStyle();
            ef.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
            String nombre = "concat_ws(' ', u.usu_paterno, u.usu_materno, u.usu_nombre)";
            // último movimiento cargado desde archivo (igual que getUltimosMovs)
            String ultEmp = "(select m.mov_empresa from movimientos m where m.mov_usu_id = u.usu_id and m.mov_arh_id is not null order by m.mov_fecha desc limit 1)";
            String ultFec = "(select m.mov_fecha from movimientos m where m.mov_usu_id = u.usu_id and m.mov_arh_id is not null order by m.mov_fecha desc limit 1)";
            String base = " from usuarios u where u.usu_fecha_baja is null and u.usu_estatus = 1"
                    + " and u.usu_paterno is not null and u.usu_materno is not null and u.usu_nombre is not null"
                    + " and u.usu_clave_empleado not between 990000 and 990999";
            int filas = 0;
            List<Integer> normales = new ArrayList<>();

            // 1. socios normales de la empresa destino
            try (ResultSet rs = st.executeQuery("select u.usu_id, u.usu_clave_empleado, " + nombre + base + " and u.usu_empresa = " + empresa
                    + " and " + ultFec + " >= '2026-09-01' order by u.usu_id limit 10")) {
                while (rs.next()) {
                    escribe(hoja.createRow(filas++), rs.getInt(2), fecha, 500.0, empresa, rs.getString(3), ef);
                    normales.add(rs.getInt(1));
                    plan.append("NORMAL usu_id=").append(rs.getInt(1)).append(" clave=").append(rs.getInt(2)).append('\n');
                }
            }

            // 2. cambios de empresa: socios de la empresa origen con último movimiento en la empresa origen
            int puestos = 0;
            try (ResultSet rs = st.executeQuery("select u.usu_id, u.usu_clave_empleado, u.usu_paterno, u.usu_materno, u.usu_nombre, "
                    + ultEmp + " ue, " + ultFec + " uf" + base + " and u.usu_empresa = " + origen
                    + " and length(u.usu_paterno) > 3 and " + nombre + " regexp '^[A-Z ]+$'"
                    + " and (select count(*) from usuarios x where left(x.usu_paterno, 3) = left(u.usu_paterno, 3)) < 40"
                    + " having ue = " + origen + " and uf >= '2026-09-01' and uf < '" + cat + "' order by u.usu_id limit 60")) {
                List<String[]> cand = new ArrayList<>();
                while (rs.next()) cand.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)});
                for (String[] u : cand) {
                    if (puestos == CLAVES_CAMBIO.length) break;
                    String completo = u[2] + " " + u[3] + " " + u[4];
                    if (coincidencias(c, completo) != 1) continue;
                    escribe(hoja.createRow(filas++), CLAVES_CAMBIO[puestos], fecha, 500.0, empresa, completo, ef);
                    plan.append("CAMBIO usu_id=").append(u[0]).append(" clave_anterior=").append(u[1]).append(" clave_nueva=").append(CLAVES_CAMBIO[puestos])
                        .append(" ultimo_mov=").append(u[6]).append(" empresa_ultimo_mov=").append(u[5]).append('\n');
                    puestos++;
                }
            }
            if (puestos != CLAVES_CAMBIO.length) { System.out.println("ABORTADO: no hubo " + CLAVES_CAMBIO.length + " candidatos a cambio\n" + plan); System.exit(3); }

            // 3. homónimo: socio de la empresa destino (no está entre los normales) con último movimiento en la misma empresa
            boolean homonimo = false;
            try (ResultSet rs = st.executeQuery("select u.usu_id, u.usu_clave_empleado, u.usu_paterno, u.usu_materno, u.usu_nombre, "
                    + ultEmp + " ue, " + ultFec + " uf" + base + " and u.usu_empresa = " + empresa
                    + " and u.usu_id not in (" + normales.toString().replace("[", "").replace("]", "") + ")"
                    + " and length(u.usu_paterno) > 3 and " + nombre + " regexp '^[A-Z ]+$'"
                    + " and (select count(*) from usuarios x where left(x.usu_paterno, 3) = left(u.usu_paterno, 3)) < 40"
                    + " having ue = " + empresa + " and uf >= '2026-09-01' and uf < '" + cat + "' order by u.usu_id limit 60")) {
                List<String[]> cand = new ArrayList<>();
                while (rs.next()) cand.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)});
                for (String[] u : cand) {
                    String completo = u[2] + " " + u[3] + " " + u[4];
                    if (coincidencias(c, completo) != 1) continue;
                    escribe(hoja.createRow(filas++), CLAVE_HOMONIMO, fecha, 500.0, empresa, completo, ef);
                    plan.append("HOMONIMO usu_id=").append(u[0]).append(" clave_real=").append(u[1]).append(" clave_archivo=").append(CLAVE_HOMONIMO)
                        .append(" ultimo_mov=").append(u[6]).append(" empresa_ultimo_mov=").append(u[5]).append('\n');
                    homonimo = true;
                    break;
                }
            }
            if (!homonimo) { System.out.println("ABORTADO: no hubo candidato a homónimo\n" + plan); System.exit(3); }

            // 4. altas: nombres sin ninguna coincidencia
            for (int i = 0; i < CLAVES_ALTA.length; i++) {
                if (coincidencias(c, NOMBRES_ALTA[i]) != 0) { System.out.println("ABORTADO: el nombre de alta coincide con un usuario: " + NOMBRES_ALTA[i]); System.exit(3); }
                escribe(hoja.createRow(filas++), CLAVES_ALTA[i], fecha, 500.0, empresa, NOMBRES_ALTA[i], ef);
                plan.append("ALTA clave=").append(CLAVES_ALTA[i]).append(" nombre=").append(NOMBRES_ALTA[i]).append('\n');
            }
            try (FileOutputStream out = new FileOutputStream(a[3])) { wb.write(out); }
            plan.append("filas: ").append(filas).append('\n');
        }
        Files.write(Paths.get(a[4]), plan.toString().getBytes(StandardCharsets.UTF_8));
        System.out.print(plan);
    }

    /** Igual que comparaNombres: usuarios con paterno like 'xxx%' (con empresa) cuya distancia es <= 2. */
    static int coincidencias(Connection c, String nombreArchivo) throws Exception {
        String archivo = nombreArchivo.trim().toLowerCase();
        int n = 0;
        try (PreparedStatement ps = c.prepareStatement("select u.usu_paterno, u.usu_materno, u.usu_nombre from usuarios u"
                + " inner join empresas e on e.emp_id = u.usu_empresa where u.usu_paterno like ?")) {
            ps.setString(1, archivo.substring(0, 3) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String completo = rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3);
                    if (levenshtein(completo, archivo.toUpperCase()) <= 2) n++;
                }
            }
        }
        return n;
    }

    static int levenshtein(String s1, String s2) {
        char[] a = s1.toCharArray(), b = s2.toCharArray();
        int[][] d = new int[a.length + 1][b.length + 1];
        for (int i = 0; i <= a.length; i++) d[i][0] = i;
        for (int j = 0; j <= b.length; j++) d[0][j] = j;
        for (int i = 1; i <= a.length; i++)
            for (int j = 1; j <= b.length; j++)
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + (a[i - 1] == b[j - 1] ? 0 : 1));
        return d[a.length][b.length];
    }

    static void escribe(Row r, int clave, Date fecha, double monto, int empresa, String nombre, CellStyle ef) {
        r.createCell(0).setCellValue(clave);
        r.createCell(1).setCellValue(fecha);
        r.getCell(1).setCellStyle(ef);
        r.createCell(2).setCellValue(monto);
        r.createCell(3).setCellValue(empresa);
        r.createCell(4).setCellValue(nombre);
        r.createCell(5).setCellValue(1);
    }
}
