import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.altasCambiosEmpresa.CambiosEmpresaBo;
import mx.com.evoti.dao.altasCambiosEmp.CambiosEmpresaDao;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Usuarios;

/**
 * SIMULA, SIN ESCRIBIR NADA, lo que haría el botón "Aplicar altas y cambios de empresa" (código ya
 * corregido) si se presiona en cada catorcena indicada, en ese orden.
 * Las lecturas son las de la aplicación: CambiosEmpresaDao.getPendtsCambiosAltas, CambiosEmpresaBo.comparaNombres
 * y CambiosEmpresaDao.getUltimosMovs. Las escrituras (updtCambioEmpDB: usuario, historial y
 * updateUsuIdMov de la catorcena) se simulan en memoria para que las decisiones siguientes vean su efecto.
 * CANDADO: contra producción (35.225.67.182) solo corre con el usuario claude_lectura (solo SELECT) y
 * sin hbm2ddl; contra cualquier otro destino, solo 127.0.0.1:3308 (control local).
 * Uso: SimulaCambiosEmpresa <modo> <ip:puerto> <dir_salida> <yyyy-MM-dd,yyyy-MM-dd,...|auto>
 */
public class SimulaCambiosEmpresa {
    static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        Path salida = Paths.get(a[2]);
        PrintStream consola = System.out;
        URL cfg = SimulaCambiosEmpresa.class.getClassLoader().getResource("hibernate.cfg.xml");
        String c;
        try (Scanner sc = new Scanner(cfg.openStream(), "UTF-8")) { c = sc.useDelimiter("\\A").next(); }
        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/([a-z_]+)").matcher(c);
        String destino = m.find() ? m.group(1) : "", esquema = m.group(2);
        String usuario = prop(c, "hibernate.connection.username"), pw = prop(c, "hibernate.connection.password");
        consola.println("[" + modo + "] configuración: " + cfg + " -> " + destino + "/" + esquema + " como " + usuario);
        boolean prod = destino.startsWith("35.225.67.182");
        if (!destino.equals(permitido)
                || (prod && (!"claude_lectura".equals(usuario) || c.contains("hbm2ddl")))
                || (!prod && !destino.equals("127.0.0.1:3308"))) {
            consola.println("BLOQUEADO: destino/usuario no permitido para simular."); System.exit(2);
        }
        Files.createDirectories(salida);
        System.setOut(new PrintStream(new FileOutputStream(salida.resolve("hibernate-sql.log").toFile()), true));

        Connection jdbc = DriverManager.getConnection("jdbc:mysql://" + destino + "/" + esquema + "?useSSL=false&allowPublicKeyRetrieval=true", usuario, pw);
        jdbc.setReadOnly(true);
        Statement st = jdbc.createStatement();
        List<String> fechas = new ArrayList<>();
        if (a[3].equals("auto")) {
            try (ResultSet rs = st.executeQuery("select distinct mov_fecha from movimientos where mov_usu_id is null and mov_arh_id is not null"
                    + " and mov_fecha >= curdate() - interval 1 year order by 1")) { while (rs.next()) fechas.add(F.format(rs.getDate(1))); }
        } else for (String s : a[3].split(",")) fechas.add(s.trim());
        // usuarios: usu_id -> {clave, empresa}, para simular updateUsuIdMov (asigna por clave + empresa)
        Map<Integer, int[]> usuarios = new HashMap<>();
        try (ResultSet rs = st.executeQuery("select usu_id, usu_clave_empleado, usu_empresa from usuarios")) {
            while (rs.next()) usuarios.put(rs.getInt(1), new int[]{rs.getInt(2), rs.getInt(3)});
        }

        HibernateUtil.buildSessionFactory();
        CambiosEmpresaBo bo = new CambiosEmpresaBo();
        CambiosEmpresaDao dao = new CambiosEmpresaDao();
        Map<Integer, Object[]> ultimoSimulado = new HashMap<>();   // usu_id -> {fecha, empresa} asignados en la simulación
        Map<Integer, Integer> cambiadoEn = new HashMap<>();        // usu_id -> veces que se le aplicó cambio
        StringBuilder res = new StringBuilder(), esperado = new StringBuilder();
        res.append("catorcena\tclave_archivo\tempresa_archivo\tnombre_archivo\tarh_id\tresultado\tusu_id\tclave_anterior\tempresa_anterior\tultimo_mov\tempresa_ultimo_mov\n");
        int totalAplica = 0;
        Map<String, int[]> resumen = new LinkedHashMap<>();
        java.util.Set<String> asignados = new java.util.HashSet<>();

        for (String f : fechas) {
            Date fecha = F.parse(f);
            int aplica = 0, homon = 0, sinCoinc = 0, sinMov = 0, porEfecto = 0;
            List<MovimientosDto> pend = dao.getPendtsCambiosAltas(f);
            for (MovimientosDto mov : pend) {
                String clave = mov.getMovClaveEmpleado() + "|" + mov.getMovEmpresa() + "|" + f + "|" + mov.getMovArhId();
                String base = f + "\t" + mov.getMovClaveEmpleado() + "\t" + mov.getMovEmpresa() + "\t" + mov.getMovNombreEmpleado() + "\t" + mov.getMovArhId() + "\t";
                if (asignados.contains(clave)) { res.append(base).append("YA_ASIGNADO_EN_ESTA_SIMULACION\n"); continue; }
                List<Usuarios> cand = bo.comparaNombres(mov);
                if (cand.isEmpty()) { res.append(base).append("SIN_COINCIDENCIA (queda para alta)\n"); sinCoinc++; continue; }
                for (Usuarios u : cand) {
                    MovimientosDto ult = dao.getUltimosMovs(u.getUsuId());
                    Date uf = ult == null ? null : ult.getMovFecha();
                    Integer ue = ult == null ? null : ult.getMovEmpresa();
                    Object[] sim = ultimoSimulado.get(u.getUsuId());
                    if (sim != null && (uf == null || !((Date) sim[0]).before(uf))) { uf = (Date) sim[0]; ue = (Integer) sim[1]; }
                    int[] actual = usuarios.get(u.getUsuId());
                    String det = u.getUsuId() + "\t" + actual[0] + "\t" + actual[1] + "\t" + (uf == null ? "" : F.format(uf)) + "\t" + (ue == null ? "" : ue);
                    if (uf == null) { res.append(base).append("NO_APLICA: el socio no tiene movimientos\t").append(det).append('\n'); sinMov++; continue; }
                    if (mov.getMovFecha().after(uf) && !Objects.equals(mov.getMovEmpresa(), ue)) {
                        boolean repetido = cambiadoEn.merge(u.getUsuId(), 1, Integer::sum) > 1;
                        res.append(base).append(repetido ? "APLICA (OJO: este socio ya se cambió antes en la simulación)\t" : (cand.size() > 1 ? "APLICA (OJO: el nombre coincide con " + cand.size() + " socios)\t" : "APLICA\t")).append(det).append('\n');
                        esperado.append(f).append('\t').append(u.getUsuId()).append('\t').append(mov.getMovClaveEmpleado()).append('\t').append(mov.getMovEmpresa()).append('\n');
                        usuarios.put(u.getUsuId(), new int[]{mov.getMovClaveEmpleado(), mov.getMovEmpresa()});
                        aplica++; totalAplica++;
                        // updateUsuIdMov(catorcena): asigna todos los pendientes de la fecha cuya clave + empresa coincidan con un usuario
                        for (MovimientosDto p : pend) {
                            String k = p.getMovClaveEmpleado() + "|" + p.getMovEmpresa() + "|" + f + "|" + p.getMovArhId();
                            if (asignados.contains(k)) continue;
                            for (Map.Entry<Integer, int[]> e : usuarios.entrySet()) {
                                if (e.getValue()[0] == p.getMovClaveEmpleado() && e.getValue()[1] == p.getMovEmpresa()) {
                                    asignados.add(k);
                                    ultimoSimulado.put(e.getKey(), new Object[]{fecha, p.getMovEmpresa()});
                                    if (p != mov) porEfecto++;
                                    break;
                                }
                            }
                        }
                    } else {
                        res.append(base).append(Objects.equals(mov.getMovEmpresa(), ue) ? "NO_APLICA: homónimo (su último movimiento es de la misma empresa)\t" : "NO_APLICA: su último movimiento no es anterior a la catorcena\t").append(det).append('\n');
                        homon++;
                    }
                }
            }
            resumen.put(f, new int[]{pend.size(), aplica, homon, sinCoinc, sinMov, porEfecto});
        }
        StringBuilder r = new StringBuilder("catorcena | pendientes | aplica | no aplica (homónimo/fecha) | sin coincidencia | socio sin movimientos | asignados de paso\n");
        for (Map.Entry<String, int[]> e : resumen.entrySet()) {
            int[] v = e.getValue();
            r.append(e.getKey()).append(" | ").append(v[0]).append(" | ").append(v[1]).append(" | ").append(v[2]).append(" | ").append(v[3]).append(" | ").append(v[4]).append(" | ").append(v[5]).append('\n');
        }
        Files.write(salida.resolve("simulacion.tsv"), res.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(salida.resolve("esperado.tsv"), esperado.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(salida.resolve("resumen.txt"), r.toString().getBytes(StandardCharsets.UTF_8));
        consola.print(r);
        consola.println("[" + modo + "] cambios que se aplicarían en total: " + totalAplica);
        System.exit(0);
    }

    static String prop(String c, String nombre) {
        Matcher m = Pattern.compile("<property name=\"" + Pattern.quote(nombre) + "\">\\s*(.*?)\\s*</property>", Pattern.DOTALL).matcher(c);
        return m.find() ? m.group(1) : null;
    }
}
