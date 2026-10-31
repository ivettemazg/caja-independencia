import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.administrador.finiquito.FiniquitosBo;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.BajaEmpleados;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.admon.DetalleAdeudoCreditosBean;
import mx.com.evoti.service.finiquito.FiniquitoService;
import mx.com.evoti.util.Constantes;

/**
 * Reproduce el flujo de bajas y finiquitos con el mismo servicio que usan las pantallas:
 *   - darDeBaja() de BajaEmpleadoBean (alta en baja_empleados, estatus del socio y snapshot);
 *   - devolución total de ahorros (FiniquitoService.devolverTotalesAhorros);
 *   - abono de ahorros al crédito (FiniquitoService.aplicarAbono);
 *   - finiquito de la empresa (FiniquitoService.aplicarFiniquito).
 * Los valores del bean de adeudos se obtienen con las mismas consultas que DetalleAdeudoCreditosBean.
 * Las fechas se fijan para que ambas corridas sean comparables.
 *
 * CANDADO: igual que PruebaProcesos.
 * Uso: PruebaFiniquito <control|ensayo> <ip_permitida:puerto> <fecha yyyy-MM-dd> <dir_salida>
 *                      <socios_solo_ahorro> <socios_abono> <socios_finiquito>   (listas separadas por coma)
 */
public class PruebaFiniquito {
    static FiniquitosBo finBo;
    static FiniquitoService fs;
    static CreditosBo creBo;
    static Date fecha;
    static StringBuilder res = new StringBuilder();

    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        fecha = new SimpleDateFormat("yyyy-MM-dd").parse(a[2]);
        Path salida = Paths.get(a[3]);
        PrintStream consola = System.out;
        URL cfg = PruebaFiniquito.class.getClassLoader().getResource("hibernate.cfg.xml");
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
        finBo = new FiniquitosBo();
        fs = new FiniquitoService();
        creBo = new CreditosBo();

        for (String s : a[4].split(",")) soloAhorro(Integer.parseInt(s.trim()));
        for (String s : a[5].split(",")) abono(Integer.parseInt(s.trim()));
        for (String s : a[6].split(",")) finiquito(Integer.parseInt(s.trim()));

        Files.write(salida.resolve("finiquitos.txt"), res.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] bajas procesadas: " + (a[4].split(",").length + a[5].split(",").length + a[6].split(",").length));
        System.exit(0);
    }

    /** Copia de BajaEmpleadoBean.darDeBaja(), sin los mensajes de pantalla. */
    static Usuarios darDeBaja(int usuId) throws Exception {
        Usuarios u = fs.cargarUsuario(usuId);
        BajaEmpleados baja = new BajaEmpleados();
        baja.setBaeFechaBaja(fecha);
        baja.setBaeIdEmpleado(usuId);
        baja.setBaeEstatus(Constantes.BAJA_INICIADA);
        finBo.insertBajaEmpleado(baja);
        u.setUsuEstatus(Constantes.USU_BAJA_0);
        u.setUsuFechaBaja(fecha);
        finBo.updtEstatusUsuario(u);
        fs.actualizarSnapshotBajaInicial(u);
        return u;
    }

    static void soloAhorro(int usuId) throws Exception {
        Usuarios u = darDeBaja(usuId);
        List<MovimientosDto> movs = fs.obtenerAhorrosPorUsuario(usuId);
        double total = 0;
        for (MovimientosDto d : movs) { d.setDevolucionFecha(fecha); total += d.getTotalMovimiento() == null ? 0 : d.getTotalMovimiento(); }
        fs.devolverTotalesAhorros(u, movs);
        res.append("solo ahorro ").append(usuId).append(" | productos ").append(movs.size()).append(" | devuelto ").append(total).append('\n');
    }

    static void abono(int usuId) throws Exception {
        Usuarios u = darDeBaja(usuId);
        DetalleCreditoDto cred = primerCreditoConSaldo(u);
        if (cred == null) { res.append("abono ").append(usuId).append(" | sin crédito con saldo\n"); return; }
        DetalleAdeudoCreditosBean det = adeudos(cred.getCreId(), u);
        double adeudo = cred.getSaldoTotal();
        List<MovimientosDto> abonos = new ArrayList<>();
        double restante = adeudo, total = 0;
        for (MovimientosDto d : fs.obtenerAhorrosPorUsuario(usuId)) {
            double disp = d.getTotalMovimiento() == null ? 0 : d.getTotalMovimiento();
            if (disp <= 0 || restante <= 0) continue;
            double x = Math.min(disp, restante);
            x = Math.floor(x * 100) / 100.0;
            d.setDevolucion(x);
            d.setDevolucionFecha(fecha);
            abonos.add(d);
            restante -= x; total += x;
        }
        fs.aplicarAbono(u, abonos, adeudo, det);
        res.append("abono ").append(usuId).append(" | credito ").append(cred.getCreId()).append(" | adeudo ").append(adeudo)
           .append(" | abonado ").append(total).append('\n');
        vuelcaCredito(cred.getCreId());
    }

    static void finiquito(int usuId) throws Exception {
        Usuarios u = darDeBaja(usuId);
        DetalleCreditoDto cred = primerCreditoConSaldo(u);
        if (cred == null) { res.append("finiquito ").append(usuId).append(" | sin crédito con saldo\n"); return; }
        DetalleAdeudoCreditosBean det = adeudos(cred.getCreId(), u);
        double monto = Math.round(cred.getSaldoTotal() * 100) / 100.0;
        fs.aplicarFiniquito(u, cred, monto, fecha, det);
        res.append("finiquito ").append(usuId).append(" | credito ").append(cred.getCreId()).append(" | monto ").append(monto).append('\n');
        vuelcaCredito(cred.getCreId());
    }

    static DetalleCreditoDto primerCreditoConSaldo(Usuarios u) throws Exception {
        for (DetalleCreditoDto c : finBo.obtCreditosDetalleFiniquito(u)) {
            if (c.getSaldoTotal() != null && c.getSaldoTotal() > 5.0) return c;
        }
        return null;
    }

    /** Mismas consultas que DetalleAdeudoCreditosBean.getAmortizacionesPendientes() y getAmortizacionPagoCap(). */
    static DetalleAdeudoCreditosBean adeudos(int creId, Usuarios u) throws Exception {
        DetalleAdeudoCreditosBean det = new DetalleAdeudoCreditosBean();
        det.setAmoPendientesAnteriores(creBo.getCatPendtsAnteriores(creId, u));
        det.setAmoPagoCapital(creBo.getAmoPagoCapital(creId, u));
        return det;
    }

    static void vuelcaCredito(int creId) throws Exception {
        res.append("  estatus del crédito: ").append(creBo.obtieneCredXId(creId).getCreEstatus()).append('\n');
        for (AmortizacionDto d : creBo.getAmortizacionXCredito(creId)) {
            res.append("  ").append(d.getAmoNumeroPago()).append(" | cap ").append(d.getAmoCapital()).append(" | amort ").append(d.getAmoAmortizacion())
               .append(" | int ").append(d.getAmoInteres()).append(" | pago ").append(d.getAmoMontoPago()).append(" | est ").append(d.getAmoEstatusInt()).append('\n');
        }
    }
}
