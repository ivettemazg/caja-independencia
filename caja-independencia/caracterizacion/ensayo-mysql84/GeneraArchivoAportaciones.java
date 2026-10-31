import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Genera un archivo de APORTACIONES de prueba (clave, fecha, monto, empresa, nombre, producto) para
 * una empresa y catorcena, leyendo la base de CONTROL local:
 *   - 10 socios activos de la empresa, con su clave y nombre (se reconocen directo);
 *   - 3 socios activos de OTRA empresa, con clave nueva 99000x y su nombre (cambio de empresa);
 *   - 2 personas inexistentes con clave 99010x (altas).
 * El archivo queda solo en la máquina local.
 * Uso: GeneraArchivoAportaciones <catorcena yyyy-MM-dd> <empresa> <empresa_origen_cambios> <salida.xlsx>
 */
public class GeneraArchivoAportaciones {
    public static void main(String[] a) throws Exception {
        int empresa = Integer.parseInt(a[1]), origen = Integer.parseInt(a[2]);
        String pw = null;
        for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-control.env"))) {
            if (l.startsWith("MYSQL_ROOT_PASSWORD=")) pw = l.substring("MYSQL_ROOT_PASSWORD=".length());
        }
        Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(a[0]);
        int filas = 0, normales = 0, cambios = 0, altas = 0;
        try (Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3308/sindicato?useSSL=false&allowPublicKeyRetrieval=true", "root", pw);
             Statement st = c.createStatement(); XSSFWorkbook wb = new XSSFWorkbook()) {
            c.setReadOnly(true);
            XSSFSheet hoja = wb.createSheet("aportaciones");
            CellStyle ef = wb.createCellStyle();
            ef.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
            String nombre = "concat_ws(' ', u.usu_paterno, u.usu_materno, u.usu_nombre)";
            try (ResultSet rs = st.executeQuery("select u.usu_clave_empleado, " + nombre + " from usuarios u where u.usu_empresa = " + empresa
                    + " and u.usu_fecha_baja is null and u.usu_estatus = 1 and u.usu_paterno is not null and u.usu_materno is not null order by u.usu_id limit 10")) {
                while (rs.next()) { escribe(hoja.createRow(filas++), rs.getInt(1), fecha, 500.0, empresa, rs.getString(2), ef); normales++; }
            }
            try (ResultSet rs = st.executeQuery("select " + nombre + " from usuarios u where u.usu_empresa = " + origen
                    + " and u.usu_fecha_baja is null and u.usu_estatus = 1 and u.usu_paterno is not null and u.usu_materno is not null"
                    + " and length(u.usu_paterno) > 3 and exists (select 1 from movimientos m where m.mov_usu_id = u.usu_id and m.mov_fecha >= '2026-09-01')"
                    + " and (select count(*) from usuarios x where left(x.usu_paterno, 3) = left(u.usu_paterno, 3)) < 40"
                    + " order by u.usu_id limit 3")) {
                int clave = 990001;
                while (rs.next()) { escribe(hoja.createRow(filas++), clave++, fecha, 500.0, empresa, rs.getString(1), ef); cambios++; }
            }
            escribe(hoja.createRow(filas++), 990101, fecha, 500.0, empresa, "ENSAYO PRUEBA UNO", ef); altas++;
            escribe(hoja.createRow(filas++), 990102, fecha, 500.0, empresa, "ENSAYO PRUEBA DOS", ef); altas++;
            try (FileOutputStream out = new FileOutputStream(a[3])) { wb.write(out); }
        }
        System.out.println("Archivo de aportaciones: " + normales + " socios de la empresa, " + cambios + " cambios de empresa, " + altas + " altas");
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
