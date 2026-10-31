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
 * Genera un archivo de pagos de prueba (mismo formato que el de la empresa: sin encabezado;
 * clave, fecha, monto, empresa, nombre) a partir de los pagos PENDIENTES de una catorcena en la
 * base de CONTROL local (127.0.0.1:3308). No usa nombres reales.
 *
 * Variación determinista para recorrer todas las fases del algoritmo:
 *   - la mayoría con el monto exacto del renglón;
 *   - cada 13.º renglón (resto 5) paga 50 pesos menos (pago menor);
 *   - cada 13.º renglón (resto 9) paga 150 pesos más (pago mayor);
 *   - 10 socios activos de la empresa sin pago pendiente esa catorcena (pago sin amortización).
 *
 * Uso: java -cp poi...:mysql.jar GeneraArchivoPagos.java <catorcena yyyy-MM-dd> <empresa> <salida.xlsx>
 */
public class GeneraArchivoPagos {
    public static void main(String[] args) throws Exception {
        String catorcena = args[0];
        int empresa = Integer.parseInt(args[1]);
        String pw = null;
        for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-control.env"))) {
            if (l.startsWith("MYSQL_ROOT_PASSWORD=")) pw = l.substring("MYSQL_ROOT_PASSWORD=".length());
        }
        Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(catorcena);
        int exactos = 0, menores = 0, mayores = 0, sinAmortizacion = 0;
        try (Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3308/sindicato?useSSL=false&allowPublicKeyRetrieval=true", "root", pw);
             Statement st = c.createStatement(); XSSFWorkbook wb = new XSSFWorkbook()) {
            c.setReadOnly(true);
            XSSFSheet hoja = wb.createSheet("pagos");
            CellStyle estiloFecha = wb.createCellStyle();
            estiloFecha.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
            int fila = 0, i = 0;
            try (ResultSet rs = st.executeQuery("select u.usu_clave_empleado, a.amo_monto_pago from amortizacion a "
                    + "join creditos_final cr on cr.cre_id = a.amo_credito join usuarios u on u.usu_id = cr.cre_usu_id "
                    + "where a.amo_fecha_pago = '" + catorcena + "' and a.amo_estatus_int = 1 and cr.cre_estatus = 1 "
                    + "and cr.cre_producto in (6,7) and u.usu_empresa = " + empresa + " order by a.amo_id")) {
                while (rs.next()) {
                    double monto = Math.round(rs.getDouble(2) * 100.0) / 100.0;
                    if (i % 13 == 5) { monto = monto - 50.0; menores++; }
                    else if (i % 13 == 9) { monto = monto + 150.0; mayores++; }
                    else exactos++;
                    escribe(hoja.createRow(fila++), rs.getInt(1), fecha, monto, empresa, estiloFecha);
                    i++;
                }
            }
            try (ResultSet rs = st.executeQuery("select u.usu_clave_empleado from usuarios u where u.usu_empresa = " + empresa
                    + " and u.usu_fecha_baja is null and not exists (select 1 from creditos_final cr join amortizacion a on a.amo_credito = cr.cre_id "
                    + "where cr.cre_usu_id = u.usu_id and a.amo_fecha_pago = '" + catorcena + "') order by u.usu_id limit 10")) {
                while (rs.next()) {
                    escribe(hoja.createRow(fila++), rs.getInt(1), fecha, 300.0, empresa, estiloFecha);
                    sinAmortizacion++;
                }
            }
            try (FileOutputStream out = new FileOutputStream(args[2])) { wb.write(out); }
        }
        System.out.println("Archivo " + args[2] + ": exactos " + exactos + ", menores " + menores + ", mayores " + mayores
                + ", sin amortizacion " + sinAmortizacion);
    }

    static void escribe(Row r, int clave, Date fecha, double monto, int empresa, CellStyle estiloFecha) {
        r.createCell(0).setCellValue(clave);
        r.createCell(1).setCellValue(fecha);
        r.getCell(1).setCellStyle(estiloFecha);
        r.createCell(2).setCellValue(monto);
        r.createCell(3).setCellValue(empresa);
        r.createCell(4).setCellValue("PRUEBA ENSAYO");
    }
}
