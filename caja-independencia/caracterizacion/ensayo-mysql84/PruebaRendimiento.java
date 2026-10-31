import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.rendimiento.ProcesoDao;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Rendimiento;
import mx.com.evoti.util.Util;

/**
 * Ejecuta el rendimiento mensual exactamente como ProcesoBean (consultaInversionesComisiones,
 * mesesRendimiento y ejecutaCalculo), con el mismo DAO. Guarda los valores calculados y los
 * movimientos generados, ordenados, para comparar la corrida en 8.0 con la de 8.4.
 *
 * CANDADO: igual que PruebaProcesos; solo continúa si hibernate.cfg.xml apunta al destino permitido.
 * Uso: PruebaRendimiento <control|ensayo> <ip_permitida:puerto> <fecha yyyy-MM-dd> <dir_salida>
 */
public class PruebaRendimiento {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1], fecha = a[2];
        Path salida = Paths.get(a[3]);
        PrintStream consola = System.out;
        URL cfg = PruebaRendimiento.class.getClassLoader().getResource("hibernate.cfg.xml");
        String contenido;
        try (Scanner sc = new Scanner(cfg.openStream(), "UTF-8")) { contenido = sc.useDelimiter("\\A").next(); }
        Matcher m = Pattern.compile("jdbc:mysql://([^/?]+)/").matcher(contenido);
        String destino = m.find() ? m.group(1) : "";
        consola.println("[" + modo + "] configuración cargada: " + cfg + " -> " + destino);
        if (destino.contains("35.225.67.182") || !destino.equals(permitido)) {
            consola.println("BLOQUEADO: la configuración no apunta al destino permitido (" + permitido + ")."); System.exit(2);
        }
        Files.createDirectories(salida);
        System.setOut(new PrintStream(new FileOutputStream(salida.resolve("hibernate-sql.log").toFile()), true));
        HibernateUtil.buildSessionFactory();

        ProcesoDao pdao = new ProcesoDao();
        HashMap datesMap = pdao.getFechas();
        if (!datesMap.containsKey(fecha)) { consola.println("[" + modo + "] el mes " + fecha + " no está pendiente"); System.exit(3); }
        Rendimiento r = pdao.getInversionesComisiones(fecha);
        double intereses = r.getRenInteresesInversion() != null ? r.getRenInteresesInversion() : 0.0;
        double comisiones = r.getRenComisionesBancarias() != null ? r.getRenComisionesBancarias() : 0.0;

        // --- mesesRendimiento(), copiado de ProcesoBean ---
        long t0 = System.currentTimeMillis();
        List<Object[]> fynf = pdao.getUsuariosMovimientos(fecha);
        List<Object[]> vol = pdao.getUsuariosMovimientosVol(fecha);
        List<Object[]> pagos = pdao.getPagos(fecha);
        double ahorroFijo = 0, ahorroNoFijo = 0, ahorroVoluntario = 0, acumuladoTotal = 0;
        double exacto = 0, mayor = 0, mas1Credito = 0, mas1CreditoAcumulado = 0, acumulado = 0, capital = 0, extemporaneo = 0, pagosTotal = 0;
        for (Object[] o : fynf) {
            double v = Double.parseDouble(o[2].toString());
            int p = Integer.parseInt(o[1].toString());
            if (p == 1) ahorroFijo += v; else if (p == 2) ahorroNoFijo += v;
            acumuladoTotal += v;
        }
        for (Object[] o : vol) { double v = Double.parseDouble(o[2].toString()); ahorroVoluntario += v; acumuladoTotal += v; }
        for (Object[] o : pagos) {
            double v = Double.parseDouble(o[1].toString());
            switch (Integer.parseInt(o[0].toString())) {
                case 2: exacto += v; break; case 4: mayor += v; break; case 5: mas1Credito += v; break;
                case 6: mas1CreditoAcumulado += v; break; case 8: acumulado += v; break; case 9: capital += v; break;
                case 10: extemporaneo += v; break; default: break;
            }
            pagosTotal += v;
        }
        double interesTotalMensual = pagosTotal + intereses - comisiones;
        double reserva = interesTotalMensual * 0.1;
        double interesNetoMensual = interesTotalMensual * 0.9;
        double factor = interesNetoMensual / acumuladoTotal;
        TreeMap<String, Object> valores = new TreeMap<>();
        valores.put("ahorroFijo", ahorroFijo); valores.put("ahorroNoFijo", ahorroNoFijo); valores.put("ahorroVoluntario", ahorroVoluntario);
        valores.put("acumuladoTotal", acumuladoTotal); valores.put("exacto", exacto); valores.put("mayor", mayor);
        valores.put("mas1Credito", mas1Credito); valores.put("mas1CreditoAcumulado", mas1CreditoAcumulado); valores.put("acumulado", acumulado);
        valores.put("capital", capital); valores.put("extemporaneo", extemporaneo); valores.put("pagosTotal", pagosTotal);
        valores.put("interesInversion", intereses); valores.put("comision", comisiones); valores.put("interesTotalMensual", interesTotalMensual);
        valores.put("reserva", reserva); valores.put("interesNetoMensual", interesNetoMensual); valores.put("factorRendimiento", factor);
        StringBuilder sv = new StringBuilder();
        valores.forEach((k, v) -> sv.append(k).append(" = ").append(v).append('\n'));
        sv.append("socios_ahorro_fijo_y_no_fijo = ").append(fynf.size()).append('\n').append("aportaciones_voluntarias = ").append(vol.size()).append('\n');
        Files.write(salida.resolve("valores.txt"), sv.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] cálculo en " + (System.currentTimeMillis() - t0) + " ms | factor " + factor + " | movimientos a generar " + (fynf.size() + vol.size()));

        if (intereses <= 0.0) { consola.println("[" + modo + "] sin intereses: la pantalla no ejecutaría"); System.exit(4); }

        // --- ejecutaCalculo(), copiado de ProcesoBean ---
        List<Movimientos> afynf = new ArrayList<>(), avol = new ArrayList<>();
        List<String> lineas = new ArrayList<>();
        for (Object[] o : fynf) {
            Movimientos mov = new Movimientos();
            mov.setMovUsuId((Integer) o[0]); mov.setMovProducto((Integer) o[1]); mov.setMovDeposito(((Double) o[2]) * factor);
            mov.setMovFecha(Util.generaFechaDeString(fecha, "yyyy-MM-dd")); mov.setMovTipo("RENDIMIENTO"); mov.setMovAr(2);
            afynf.add(mov);
            lineas.add(mov.getMovUsuId() + "|" + mov.getMovProducto() + "|-|" + mov.getMovDeposito());
        }
        for (Object[] o : vol) {
            Movimientos mov = new Movimientos();
            mov.setMovUsuId((Integer) o[0]); mov.setMovProducto((Integer) o[1]); mov.setMovDeposito(((Double) o[2]) * factor);
            mov.setMovIdPadre((Integer) o[3]);
            mov.setMovFecha(Util.generaFechaDeString(fecha, "yyyy-MM-dd")); mov.setMovTipo("RENDIMIENTO"); mov.setMovAr(2);
            avol.add(mov);
            lineas.add(mov.getMovUsuId() + "|" + mov.getMovProducto() + "|" + mov.getMovIdPadre() + "|" + mov.getMovDeposito());
        }
        Collections.sort(lineas);
        Files.write(salida.resolve("movimientos.txt"), String.join("\n", lineas).getBytes(StandardCharsets.UTF_8));
        t0 = System.currentTimeMillis();
        pdao.updateAmountsv2(afynf);
        pdao.updateAmountsv2(avol);
        HashMap valoresBean = new HashMap(valores);
        pdao.updateAmounts(valoresBean, fecha, datesMap.get(fecha).toString());
        consola.println("[" + modo + "] rendimiento aplicado en " + (System.currentTimeMillis() - t0) + " ms");
        System.exit(0);
    }
}
