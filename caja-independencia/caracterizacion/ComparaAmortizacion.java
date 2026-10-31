import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import mx.com.evoti.bo.TablaAmortizacionBo;
import mx.com.evoti.dto.common.AmortizacionDto;

/**
 * Piloto de caracterización: recalcula la tabla de amortización de cada crédito con el código
 * ACTUAL (TablaAmortizacionBo, sin modificar) y la compara contra los renglones guardados.
 * Lee únicamente del MySQL local. No escribe en ninguna base.
 */
public class ComparaAmortizacion {

    static class Fila {
        int num; Double capital, amort, interes, iva, pago; LocalDate fecha; Integer estatus;
    }

    static class Credito {
        int id; int producto; Double prestamo; Integer catorcenas; java.sql.Date primerPago;
        Integer estatus; LocalDate solCreacion; boolean nuevoMonto; Double solMonto; Integer solCat; List<Fila> filas = new ArrayList<>();
    }

    static final String[] CATEGORIAS = {
        "IDENTICO (montos y fechas)", "MONTOS IDENTICOS, FECHAS DISTINTAS", "PREFIJO IDENTICO (luego cambia)",
        "DISTINTO DESDE EL PRIMER RENGLON", "SIN RENGLONES GUARDADOS", "SIN INSUMOS PARA CALCULAR"};

    static boolean igual(Double x, Double y, boolean alCentavo) {
        double a = x == null ? 0.0 : x, b = y == null ? 0.0 : y;
        return alCentavo ? Math.abs(a - b) < 0.005 : Double.compare(a, b) == 0;
    }

    static Double dbl(ResultSet rs, int i) throws Exception {
        Number n = (Number) rs.getObject(i);
        return n == null ? null : n.doubleValue();
    }

    static Integer ent(ResultSet rs, int i) throws Exception {
        Number n = (Number) rs.getObject(i);
        return n == null ? null : n.intValue();
    }

    static void sumar(Map<String, int[]> m, String k, int cat) {
        m.computeIfAbsent(k, x -> new int[CATEGORIAS.length])[cat]++;
    }

    static void tabla(PrintStream out, String titulo, Map<String, int[]> m) {
        out.println("\n" + titulo);
        out.println("  grupo | total | identico | montos ok, fechas no | prefijo | distinto | sin renglones | sin insumos | % identico en montos");
        for (Map.Entry<String, int[]> e : m.entrySet()) {
            int[] v = e.getValue();
            int total = 0;
            for (int x : v) total += x;
            int comparables = v[0] + v[1] + v[2] + v[3];
            String pct = comparables == 0 ? "-" : String.format("%.1f%%", 100.0 * (v[0] + v[1]) / comparables);
            out.println("  " + e.getKey() + " | " + total + " | " + v[0] + " | " + v[1] + " | " + v[2] + " | " + v[3]
                    + " | " + v[4] + " | " + v[5] + " | " + pct);
        }
    }

    public static void main(String[] args) throws Exception {
        PrintStream out = System.out;
        PrintStream nulo = new PrintStream(OutputStream.nullOutputStream());

        String pw = null;
        for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".config", "caja-independencia", "mysql-local.env"))) {
            if (l.startsWith("MYSQL_ROOT_PASSWORD=")) pw = l.substring("MYSQL_ROOT_PASSWORD=".length());
        }

        Map<Integer, Credito> creditos = new LinkedHashMap<>();
        String url = "jdbc:mysql://127.0.0.1:3307/caja_local?sslMode=DISABLED&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=CONVERT_TO_NULL";
        try (Connection c = DriverManager.getConnection(url, "root", pw); Statement st = c.createStatement()) {
            c.setReadOnly(true);
            try (ResultSet rs = st.executeQuery("select c.cre_id, c.cre_producto, c.cre_prestamo, c.cre_catorcenas, c.cre_fecha_primer_pago, "
                    + "c.cre_estatus, c.cre_fecha_nuevo_monto, s.sol_fecha_creacion, s.sol_monto_solicitado, s.sol_catorcenas "
                    + "from creditos_final c left join solicitudes s on s.sol_id = c.cre_solicitud")) {
                while (rs.next()) {
                    Credito k = new Credito();
                    k.id = rs.getInt(1);
                    k.producto = rs.getInt(2);
                    k.prestamo = dbl(rs, 3);
                    k.catorcenas = ent(rs, 4);
                    k.primerPago = rs.getDate(5);
                    k.estatus = ent(rs, 6);
                    k.nuevoMonto = rs.getDate(7) != null;
                    java.sql.Date sc = rs.getDate(8);
                    k.solCreacion = sc == null ? null : sc.toLocalDate();
                    k.solMonto = dbl(rs, 9);
                    k.solCat = ent(rs, 10);
                    creditos.put(k.id, k);
                }
            }
            try (ResultSet rs = st.executeQuery("select amo_credito, amo_numero_pago, amo_capital, amo_amortizacion, amo_interes, amo_iva, "
                    + "amo_monto_pago, amo_fecha_pago, amo_estatus_int from amortizacion order by amo_credito, amo_numero_pago, amo_id")) {
                while (rs.next()) {
                    Credito k = creditos.get(rs.getInt(1));
                    if (k == null) continue;
                    Fila f = new Fila();
                    f.num = rs.getInt(2);
                    f.capital = dbl(rs, 3);
                    f.amort = dbl(rs, 4);
                    f.interes = dbl(rs, 5);
                    f.iva = dbl(rs, 6);
                    f.pago = dbl(rs, 7);
                    java.sql.Date d = rs.getDate(8);
                    f.fecha = d == null ? null : d.toLocalDate();
                    f.estatus = ent(rs, 9);
                    k.filas.add(f);
                }
            }
        }

        ZoneId zona = ZoneId.systemDefault();
        LocalDate fechaTasaEspecial = LocalDate.of(2018, 11, 11);
        Map<String, int[]> porProducto = new TreeMap<>(), porAnio = new TreeMap<>(), porEstatus = new TreeMap<>(), general = new TreeMap<>();
        Map<String, int[]> generalCentavo = new TreeMap<>();
        Map<String, Integer> tasasImplicitas = new TreeMap<>(), estatusDivergencia = new TreeMap<>(), motivoDistinto = new TreeMap<>();
        Map<Integer, List<Integer>> ejemplos = new TreeMap<>();
        long filasComparadas = 0, filasIdenticas = 0, filasIdenticasCentavo = 0, filasGuardadas = 0;
        int conNumerosDuplicados = 0, conMasFilasGuardadas = 0, conMenosFilasGuardadas = 0, tasaEspecial1 = 0;
        int sinSolicitud = 0, insumosCambiaron = 0;
        Map<String, Integer> difRenglon1 = new TreeMap<>(), mesDesfase = new TreeMap<>(), diasDesfase = new TreeMap<>();

        for (Credito k : creditos.values()) {
            if (k.producto != 6 && k.producto != 7) continue;
            filasGuardadas += k.filas.size();
            int cat, catCentavo;
            // Insumos de origen: los de la solicitud (lo que uso el fondeo); si no hay solicitud, los del credito.
            Double prestamoIn = k.solMonto != null ? k.solMonto : k.prestamo;
            Integer catIn = (k.solCat != null && k.solCat > 0) ? k.solCat : k.catorcenas;
            if (k.solMonto == null) sinSolicitud++;
            else if (!igual(k.solMonto, k.prestamo, true) || (k.solCat != null && !k.solCat.equals(k.catorcenas))) insumosCambiaron++;
            if (prestamoIn == null || catIn == null || catIn <= 0 || k.primerPago == null) {
                cat = catCentavo = 5;
            } else if (k.filas.isEmpty()) {
                cat = catCentavo = 4;
            } else {
                int tasaEsp = fechaTasaEspecial.equals(k.solCreacion) ? 1 : 0;
                tasaEspecial1 += tasaEsp;
                System.setOut(nulo);
                List<AmortizacionDto> calc;
                try {
                    calc = new TablaAmortizacionBo().generaTablaAmortizacion(1, prestamoIn, catIn,
                            new Date(k.primerPago.getTime()), k.producto, tasaEsp);
                } finally {
                    System.setOut(out);
                }
                Map<Integer, Fila> porNum = new TreeMap<>();
                boolean dup = false;
                for (Fila f : k.filas) {
                    if (porNum.putIfAbsent(f.num, f) != null) dup = true;
                }
                if (dup) conNumerosDuplicados++;
                if (k.filas.size() > calc.size()) conMasFilasGuardadas++;
                if (k.filas.size() < calc.size()) conMenosFilasGuardadas++;

                int prefijo = 0, prefijoCentavo = 0;
                boolean fechasOk = true;
                LocalDate primeraFechaDistinta = null;
                long desfase = 0;
                Integer estatusPrimeraDivergencia = null;
                for (int i = 0; i < calc.size(); i++) {
                    AmortizacionDto d = calc.get(i);
                    Fila f = porNum.get(i + 1);
                    if (f == null) {
                        if (estatusPrimeraDivergencia == null && prefijo == i) estatusPrimeraDivergencia = -1;
                        continue;
                    }
                    filasComparadas++;
                    boolean ex = igual(f.capital, d.getAmoCapital(), false) && igual(f.amort, d.getAmoAmortizacion(), false)
                            && igual(f.interes, d.getAmoInteres(), false) && igual(f.pago, d.getAmoMontoPago(), false)
                            && igual(f.iva, d.getAmoIva(), false);
                    boolean ce = igual(f.capital, d.getAmoCapital(), true) && igual(f.amort, d.getAmoAmortizacion(), true)
                            && igual(f.interes, d.getAmoInteres(), true) && igual(f.pago, d.getAmoMontoPago(), true)
                            && igual(f.iva, d.getAmoIva(), true);
                    if (ex) filasIdenticas++;
                    if (ce) filasIdenticasCentavo++;
                    if (ex && prefijo == i) prefijo++;
                    if (ce && prefijoCentavo == i) prefijoCentavo++;
                    if (!ex && prefijo == i && estatusPrimeraDivergencia == null) estatusPrimeraDivergencia = f.estatus == null ? 0 : f.estatus;
                    LocalDate fc = Instant.ofEpochMilli(d.getAmoFechaPagoCredito().getTime()).atZone(zona).toLocalDate();
                    if (ex && !fc.equals(f.fecha)) {
                        fechasOk = false;
                        if (primeraFechaDistinta == null && f.fecha != null) {
                            primeraFechaDistinta = f.fecha;
                            desfase = java.time.temporal.ChronoUnit.DAYS.between(fc, f.fecha);
                        }
                    }
                }
                boolean mismasFilas = k.filas.size() == calc.size() && !dup;
                cat = (prefijo == calc.size() && mismasFilas) ? (fechasOk ? 0 : 1) : (prefijo == 0 ? 3 : 2);
                catCentavo = (prefijoCentavo == calc.size() && mismasFilas) ? (fechasOk ? 0 : 1) : (prefijoCentavo == 0 ? 3 : 2);

                if (cat == 1 && primeraFechaDistinta != null) {
                    mesDesfase.merge(primeraFechaDistinta.toString().substring(0, 7), 1, Integer::sum);
                    diasDesfase.merge(String.format("%+d dias", desfase), 1, Integer::sum);
                }
                if (cat == 2 && estatusPrimeraDivergencia != null) {
                    String e = estatusPrimeraDivergencia == -1 ? "renglon faltante" : "estatus " + estatusPrimeraDivergencia;
                    estatusDivergencia.merge(e, 1, Integer::sum);
                }
                if (cat == 3) {
                    Fila f1 = porNum.get(1);
                    if (f1 == null) {
                        motivoDistinto.merge("no existe el renglon 1", 1, Integer::sum);
                    } else {
                        boolean capitalIgual = igual(f1.capital, new BigDecimal(prestamoIn).setScale(2, RoundingMode.UP).doubleValue(), true);
                        AmortizacionDto c1 = calc.get(0);
                        double d1 = Math.max(Math.max(Math.abs((f1.capital == null ? 0 : f1.capital) - c1.getAmoCapital()), Math.abs((f1.amort == null ? 0 : f1.amort) - c1.getAmoAmortizacion())),
                                Math.max(Math.abs((f1.interes == null ? 0 : f1.interes) - c1.getAmoInteres()), Math.abs((f1.pago == null ? 0 : f1.pago) - c1.getAmoMontoPago())));
                        String periodo = k.primerPago.toLocalDate().getYear() <= 2014 ? "2012-2014" : "2015 en adelante";
                        String rango = d1 < 0.0101 ? "a) hasta 1 centavo" : d1 < 0.0501 ? "b) hasta 5 centavos" : d1 < 1.0001 ? "c) hasta 1 peso" : "d) mas de 1 peso";
                        difRenglon1.merge(periodo + " | diferencia en el renglon 1: " + rango, 1, Integer::sum);
                        motivoDistinto.merge(capitalIgual ? "capital inicial = prestamo (cambia tasa o plazo)" : "capital inicial <> prestamo actual", 1, Integer::sum);
                        if (f1.capital != null && f1.capital > 0 && f1.interes != null) {
                            double tasa = f1.interes / f1.capital * 2600.0;
                            String anio = String.valueOf(k.primerPago.toLocalDate().getYear());
                            tasasImplicitas.merge(String.format("producto %d | tasa implicita %.1f%% | %s", k.producto, tasa, anio), 1, Integer::sum);
                        }
                    }
                }
            }
            sumar(general, "productos 6 y 7", cat);
            sumar(generalCentavo, "productos 6 y 7", catCentavo);
            sumar(porProducto, "producto " + k.producto, cat);
            sumar(porAnio, k.primerPago == null ? "sin fecha" : String.valueOf(k.primerPago.toLocalDate().getYear()), cat);
            sumar(porEstatus, "cre_estatus " + k.estatus + (k.nuevoMonto ? " (con nuevo monto)" : ""), cat);
            List<Integer> ej = ejemplos.computeIfAbsent(cat, x -> new ArrayList<>());
            if (ej.size() < 6) ej.add(k.id);
        }

        out.println("=== PILOTO: tabla de amortizacion recalculada con el codigo actual vs. renglones guardados ===");
        out.println("Creditos en la copia local: " + creditos.size() + " | renglones guardados de productos 6 y 7: " + filasGuardadas);
        tabla(out, "A. Resultado general (comparacion EXACTA de montos, bit a bit)", general);
        tabla(out, "B. Resultado general (comparacion al centavo)", generalCentavo);
        tabla(out, "C. Por producto (exacta)", porProducto);
        tabla(out, "D. Por anio del primer pago (exacta)", porAnio);
        tabla(out, "E. Por estatus del credito (exacta)", porEstatus);
        out.println("\nF. Renglones: comparados " + filasComparadas + " | identicos exactos " + filasIdenticas
                + String.format(" (%.2f%%)", 100.0 * filasIdenticas / Math.max(1, filasComparadas))
                + " | identicos al centavo " + filasIdenticasCentavo
                + String.format(" (%.2f%%)", 100.0 * filasIdenticasCentavo / Math.max(1, filasComparadas)));
        out.println("   Creditos con numeros de pago duplicados: " + conNumerosDuplicados + " | con mas renglones guardados que el plazo: "
                + conMasFilasGuardadas + " | con menos: " + conMenosFilasGuardadas + " | con tasa especial (2018-11-11): " + tasaEspecial1);
        int[] g = general.get("productos 6 y 7");
        int comparables = g[0] + g[1] + g[2] + g[3];
        out.println(String.format("\n>> Formula de origen confirmada (identico + solo fechas + prefijo): %d de %d comparables = %.1f%%", g[0] + g[1] + g[2], comparables, 100.0 * (g[0] + g[1] + g[2]) / comparables));
        out.println("   Creditos sin solicitud ligada (se uso el dato del credito): " + sinSolicitud + " | con monto o plazo del credito distinto al de su solicitud: " + insumosCambiaron);
        out.println("\nF2. MONTOS IDENTICOS, FECHAS DISTINTAS: mes del primer renglon desfasado y tamano del desfase:");
        mesDesfase.forEach((e, n) -> { if (n >= 8) out.println("   desde " + e + ": " + n); });
        diasDesfase.forEach((e, n) -> { if (n >= 8) out.println("   " + e + ": " + n); });
        out.println("\nH2. DISTINTOS DESDE EL INICIO: tamano de la diferencia en el primer renglon:");
        difRenglon1.forEach((e, n) -> out.println("   " + e + ": " + n));
        out.println("\nG. En los de PREFIJO IDENTICO, estatus del primer renglon que cambia:");
        estatusDivergencia.forEach((e, n) -> out.println("   " + e + ": " + n));
        out.println("\nH. En los DISTINTOS DESDE EL INICIO, motivo aparente:");
        motivoDistinto.forEach((e, n) -> out.println("   " + e + ": " + n));
        out.println("\nI. Tasa implicita del primer renglon guardado en los DISTINTOS (producto | tasa | anio primer pago):");
        tasasImplicitas.forEach((e, n) -> out.println("   " + e + ": " + n));
        out.println("\nJ. Ejemplos de cre_id por categoria (para revisar a mano):");
        ejemplos.forEach((cat, ids) -> out.println("   " + CATEGORIAS[cat] + ": " + ids));

        // Productos a vencimiento (FA 4, AG 5, SAU 11): se guarda un solo renglon con el total.
        // Aqui solo se verifica la coherencia de la formula: interes total = n * interes por catorcena.
        int vto = 0, unRenglon = 0, pagoCoherente = 0, nEntero = 0;
        for (Credito k : creditos.values()) {
            if (k.producto != 4 && k.producto != 5 && k.producto != 11) continue;
            vto++;
            if (k.filas.size() != 1 || k.prestamo == null) continue;
            unRenglon++;
            Fila f = k.filas.get(0);
            if (f.interes == null || f.pago == null) continue;
            if (Math.abs(f.pago - (k.prestamo + f.interes)) < 0.011) pagoCoherente++;
            double porCatorcena = new BigDecimal(k.prestamo * 18 / (26 * 100)).setScale(2, RoundingMode.DOWN).doubleValue();
            if (porCatorcena > 0) {
                double n = f.interes / porCatorcena;
                if (Math.abs(n - Math.rint(n)) < 0.02) nEntero++;
            }
        }
        out.println("\nK. Productos a vencimiento (4, 5, 11): creditos " + vto + " | con un solo renglon " + unRenglon
                + " | pago = prestamo + interes: " + pagoCoherente + " | interes = n catorcenas enteras al 18%: " + nEntero);
    }
}
