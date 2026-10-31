import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.com.evoti.bo.bancos.BancoAjustesBo;
import mx.com.evoti.dto.bancos.BancosDto;
import mx.com.evoti.dto.bancos.EstadoCuentaDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;

/**
 * Ajusta pares de movimientos (sistema contra estado de cuenta) con la rama "ningún concepto ajustado
 * antes" de BancoAjusteBean.ajustaConceptos(): asignaRelacionNoAjustados + cálculo de sumas + updtAjustado.
 * Elige de forma determinista, entre los no ajustados del periodo, 4 pares con el mismo monto y 1 par
 * con montos distintos. El id de relación es aleatorio como en la pantalla (Util.genUUID).
 * CANDADO: igual que PruebaProcesos.
 * Uso: PruebaAjusteBancos <modo> <ip:puerto> <inicio yyyy-MM-dd> <fin yyyy-MM-dd> <dir_salida>
 */
public class PruebaAjusteBancos {
    public static void main(String[] a) throws Exception {
        String modo = a[0], permitido = a[1];
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Path salida = Paths.get(a[4]);
        PrintStream consola = System.out;
        URL cfg = PruebaAjusteBancos.class.getClassLoader().getResource("hibernate.cfg.xml");
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

        BancoAjustesBo bo = new BancoAjustesBo();
        bo.obtieneListasBanco(f.parse(a[2]), f.parse(a[3]));
        List<BancosDto> bancos = new ArrayList<>();
        for (BancosDto b : bo.getLstBancos()) if (b.getBanAjustado() != null && b.getBanAjustado() == 0) bancos.add(b);
        List<EstadoCuentaDto> ecs = new ArrayList<>();
        for (EstadoCuentaDto e : bo.getLstEstadosCuenta()) if (e.getEcAjustado() != null && e.getEcAjustado() == 0 && e.getEcMonto() != null) ecs.add(e);
        bancos.sort(Comparator.comparing(BancosDto::getBanId));
        ecs.sort(Comparator.comparing(EstadoCuentaDto::getEcId));

        StringBuilder res = new StringBuilder("no ajustados en el periodo: sistema " + bancos.size() + " | estado de cuenta " + ecs.size() + "\n");
        List<BancosDto[]> usados = new ArrayList<>();
        List<Integer> ecUsados = new ArrayList<>();
        List<Integer> banUsados = new ArrayList<>();
        int iguales = 0;
        for (BancosDto b : bancos) {
            if (iguales == 4) break;
            for (EstadoCuentaDto e : ecs) {
                if (ecUsados.contains(e.getEcId())) continue;
                if (Math.abs(Util.round(e.getEcMonto()) - Util.round(b.getBanMonto())) < 0.005) {
                    ajusta(bo, b, e, res);
                    ecUsados.add(e.getEcId());
                    banUsados.add(b.getBanId());
                    iguales++;
                    break;
                }
            }
        }
        // Un par con montos distintos (ajuste parcial): el primer no ajustado de cada lado que quede.
        for (BancosDto b : bancos) {
            if (banUsados.contains(b.getBanId())) continue;
            EstadoCuentaDto e = null;
            for (EstadoCuentaDto x : ecs) if (!ecUsados.contains(x.getEcId()) && Math.abs(x.getEcMonto() - b.getBanMonto()) > 1.0) { e = x; break; }
            if (e != null) { ajusta(bo, b, e, res); iguales++; break; }
        }
        Files.write(salida.resolve("ajustes.txt"), res.toString().getBytes(StandardCharsets.UTF_8));
        consola.println("[" + modo + "] pares ajustados: " + iguales);
        System.exit(0);
    }

    /** Copia de la rama "else" de BancoAjusteBean.ajustaConceptos() con un elemento por lado. */
    static void ajusta(BancoAjustesBo bo, BancosDto b, EstadoCuentaDto e, StringBuilder res) throws Exception {
        List<BancosDto> sistemSel = new ArrayList<>();
        sistemSel.add(b);
        List<EstadoCuentaDto> edoCtaSel = new ArrayList<>();
        edoCtaSel.add(e);
        Double sumaBanco = 0.0, sumaEc = 0.0;
        BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
        bo.asignaRelacionNoAjustados(idRelacion, sistemSel, edoCtaSel);
        for (EstadoCuentaDto cs : edoCtaSel) sumaEc += Util.round(cs.getEcMonto());
        for (BancosDto x : sistemSel) sumaBanco += Util.round(x.getBanMonto());
        int ajustadoBan = 0, ajustadoEc = 0;
        if (sumaEc - sumaBanco < -1) { ajustadoBan = Constantes.BAN_AJUSTADO_PARCIAL; ajustadoEc = Constantes.EC_AJUSTADO; }
        else if (sumaEc - sumaBanco > 1) { ajustadoBan = Constantes.BAN_AJUSTADO; ajustadoEc = Constantes.EC_AJUSTADO_PARCIAL; }
        else { ajustadoBan = Constantes.BAN_AJUSTADO; ajustadoEc = Constantes.EC_AJUSTADO; }
        bo.updtAjustado(idRelacion.longValue(), ajustadoBan, ajustadoEc);
        b.setRbeId(idRelacion);
        res.append("banco ").append(b.getBanId()).append(" (").append(b.getBanMonto()).append(") con estado de cuenta ").append(e.getEcId())
           .append(" (").append(e.getEcMonto()).append(") -> ajustado banco ").append(ajustadoBan).append(" / estado de cuenta ").append(ajustadoEc).append('\n');
    }
}
