/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.TotalesAmortizacionDto;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.dto.finiquito.AvalesCreditoDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class CreditosDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = -8794233382960024622L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CreditosDao.class);

    /**
     * Obtiene los creditos activos por usuario
     *
     * @param idUsuario
     * @return
     * @throws IntegracionException
     */
    public List<CreditosFinal> obtieneCreditosXUsuario(int idUsuario) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(String.format(""
                    + "from CreditosFinal "
                    + "where creUsuId= %1$s "
                    + "and creEstatus = 1",
                    idUsuario));

            List<CreditosFinal> creditos = query.list();
            return creditos;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Actualiza el estatus de un credito
     *
     * @param idCredito
     * @param estatus
     * @param fechaIncob
     * @throws IntegracionException
     */
    public void updtCreditoEstatus(Integer idCredito, Integer estatus, Date fechaIncob) throws IntegracionException {

        String incob = "";
        if (estatus == Constantes.CRE_EST_INCOBRABLE) {
            incob = String.format(",cre.creFechaIncobrable = '%1$s' ", Util.generaFechaFormateada(fechaIncob));
        }

        String hql = String.format("update CreditosFinal cre "
                + "set cre.creEstatus = %2$s "
                + "%3$s"
                + "where creId = %1$s ", idCredito, estatus, incob);

        super.executeUpdate(hql);

    }

    /**
     * Obtiene un credito por id de solicitud
     *
     * @param idSolicitud
     * @return
     * @throws IntegracionException
     */
    public CreditosFinal obtieneCreditosXSolicitud(Long idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(String.format(""
                    + "from CreditosFinal "
                    + "where creSolicitud= %1$s ",
                    idSolicitud));

            CreditosFinal creditos = (CreditosFinal) query.setMaxResults(1).uniqueResult();
            return creditos;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Obtiene el detalle de los creditos de un usuario, regresando información
     * como saldos por catorcenas pendientes y por capital, dicha consulta es
     * utilizable en finiquitos y en solicitudes pendientes
     *
     * @param idUsuario
     * @param catAnterior
     * @param catSiguiente
     * @return
     * @throws IntegracionException
     */
    public List<DetalleCreditoDto> getDetalleAdeudoCredito(int idUsuario, Date catAnterior, Date catSiguiente) throws IntegracionException {
    try {
        super.beginTransaction();

        String fechaAnterior = Util.generaFechaFormateada(catAnterior);
        String fechaSiguiente = Util.generaFechaFormateada(catSiguiente);

        String sql = "SELECT cre_id AS creId, cre_clave AS creClave, " +
                "pro_descripcion AS proDescripcion, cre_prestamo AS crePrestamo, " +
                "cre_catorcenas AS creCatorcenas, " +
                "IFNULL(cat_pendts.saldoPendiente,0) AS saldoPendiente, " +
                "IFNULL(capital_pendt.saldoCapital,0) AS saldoCapital, " +
                "IFNULL(cat_pendts.saldoPendiente,0)+IFNULL(capital_pendt.saldoCapital,0) AS saldoTotal, " +
                "cre_est_nombre AS creEstatusNombre, " +
                "cre_est_id AS creEstatusId, " +
                "IFNULL(capital_pendt.catPendCap,0)+IFNULL(cat_pendts.catPendAdeudo,0) AS catorcenasPendientes " +
                "FROM creditos_final " +
                "LEFT JOIN ( " +
                "  SELECT amo_credito, SUM(amo_monto_pago) AS saldoPendiente, COUNT(amo_id) AS catPendAdeudo " +
                "  FROM amortizacion " +
                "  WHERE amo_estatus_int = 1 AND amo_fecha_pago <= :fechaAnterior " +
                "  GROUP BY amo_credito " +
                ") cat_pendts ON cat_pendts.amo_credito = creditos_final.cre_id " +
                "LEFT JOIN ( " +
                "  SELECT amo_credito, SUM(amo_amortizacion) AS saldoCapital, COUNT(amo_id) AS catPendCap " +
                "  FROM amortizacion " +
                "  WHERE amo_estatus_int = 1 AND amo_fecha_pago >= :fechaSiguiente " +
                "  GROUP BY amo_credito " +
                ") capital_pendt ON capital_pendt.amo_credito = creditos_final.cre_id " +
                "INNER JOIN productos ON creditos_final.cre_producto = productos.pro_id " +
                "INNER JOIN credito_estatus ON credito_estatus.cre_est_id = creditos_final.cre_estatus " +
                "WHERE cre_usu_id = :idUsuario AND cre_producto NOT IN (4,5,11) " +
                "UNION " +
                "SELECT cre_id AS creId, cre_clave AS creClave, " +
                "pro_descripcion AS proDescripcion, cre_prestamo AS crePrestamo, " +
                "IFNULL(cre_catorcenas, 1) AS creCatorcenas, " +
                "ROUND(IFNULL(cat_pendts.saldoPendiente, 0), 2) AS saldoPendiente, " +
                "ROUND(IFNULL(capital_pendt.saldoCapital, 0), 2) AS saldoCapital, " +
                "ROUND(IFNULL(cat_pendts.saldoPendiente, 0) + IFNULL(capital_pendt.saldoCapital, 0), 2) AS saldoTotal, " +
                "cre_est_nombre AS creEstatusNombre, " +
                "cre_est_id AS creEstatusId, " +
                "IFNULL(cre_catorcenas, 1) AS catorcenasPendientes " +
                "FROM creditos_final " +
                "LEFT JOIN ( " +
                "  SELECT amo_credito, COUNT(amo_id) AS catPendAdeudo, " +
                "         s.sol_fecha_Creacion AS FechaCreacion, c.cre_fecha_primer_pago AS FechaPrimerPago, " +
                "         ANY_VALUE(a.amo_interes) AS Interes, " +
                "         ((DATEDIFF(:fechaAnterior, s.sol_fecha_Creacion))/14) * " +
                "         (ANY_VALUE(a.amo_interes) / ((DATEDIFF(c.cre_fecha_primer_pago, s.sol_fecha_Creacion))/14)) AS saldoPendiente " +
                "  FROM amortizacion a " +
                "  LEFT JOIN creditos_final c ON a.amo_credito = c.cre_id " +
                "  LEFT JOIN solicitudes s ON c.cre_solicitud = s.sol_id " +
                "  WHERE amo_estatus_int = 1 " +
                "  GROUP BY amo_credito " +
                ") cat_pendts ON cat_pendts.amo_credito = creditos_final.cre_id " +
                "LEFT JOIN ( " +
                "  SELECT amo_credito, SUM(amo_capital) AS saldoCapital, COUNT(amo_id) AS catPendCap " +
                "  FROM amortizacion " +
                "  WHERE amo_estatus_int = 1 AND amo_fecha_pago >= :fechaSiguiente " +
                "  GROUP BY amo_credito " +
                ") capital_pendt ON capital_pendt.amo_credito = creditos_final.cre_id " +
                "INNER JOIN productos ON creditos_final.cre_producto = productos.pro_id " +
                "INNER JOIN credito_estatus ON credito_estatus.cre_est_id = creditos_final.cre_estatus " +
                "WHERE cre_usu_id = :idUsuario AND cre_producto IN (4,5,11)";

        SQLQuery query = session.createSQLQuery(sql);
        query.setParameter("fechaAnterior", fechaAnterior);
        query.setParameter("fechaSiguiente", fechaSiguiente);
        query.setParameter("idUsuario", idUsuario);

        List<DetalleCreditoDto> resultados = query.setResultTransformer(Transformers.aliasToBean(DetalleCreditoDto.class)).list();

        return resultados;
    } catch (Exception ex) {
        throw new IntegracionException("Error al obtener el detalle de adeudo de crédito", ex);
    } finally {
        super.endTransaction();
    }
}


   @SuppressWarnings("unchecked")
    public List<DetalleCreditoDto> getDetalleAdeudoCreditoFiniquito(
            int  usuId,
            Date catAnterior,
            Date catSiguiente) throws IntegracionException {

        LOGGER.info("► Inicia getDetalleAdeudoCreditoFiniquito para usuId=" + usuId);

        super.beginTransaction();
        try {
            /* 1. Fechas ------------------------------------------------------- */
            String fAnt = Util.generaFechaFormateada(catAnterior);
            String fSig = Util.generaFechaFormateada(catSiguiente);

            /* 2. Créditos base ------------------------------------------------ */
            final String SQL_CREDITOS =
                "SELECT cf.cre_id, cf.cre_clave, p.pro_descripcion,  " +
                "       cf.cre_prestamo, COALESCE(cf.cre_catorcenas,1), " +
                "       ce.cre_est_nombre, ce.cre_est_id " +
                "FROM   creditos_final cf " +
                "       JOIN productos       p  ON cf.cre_producto = p.pro_id " +
                "       JOIN credito_estatus ce ON cf.cre_estatus  = ce.cre_est_id " +
                "WHERE  cf.cre_usu_id = :uid   AND cf.cre_producto %s";

            List<Object[]> filasCreditosNorm = session.createSQLQuery(
                    String.format(SQL_CREDITOS, "NOT IN (4,5,11)"))
                .setParameter("uid", usuId)
                .list();
            LOGGER.info("   → creditosNormales  filas = " + filasCreditosNorm.size());

            List<Object[]> filasCreditosEsp = session.createSQLQuery(
                    String.format(SQL_CREDITOS, "IN (4,5,11)"))
                .setParameter("uid", usuId)
                .list();
            LOGGER.info("   → creditosEspeciales filas = " + filasCreditosEsp.size());

            /* 3-A. Pendientes ------------------------------------------------- */
            List<Object[]> pendientesRows = session.createSQLQuery(
                "SELECT amo_credito, SUM(amo_monto_pago), COUNT(amo_id) " +
                "FROM   amortizacion " +
                "WHERE  amo_estatus_int = 1 AND amo_fecha_pago <= :fAnt " +
                "GROUP  BY amo_credito")
                .setParameter("fAnt", fAnt)
                .list();
            LOGGER.info("   → pendientes         filas = " + pendientesRows.size());

            /* 3-B. Capitales -------------------------------------------------- */
            List<Object[]> capitalRows = session.createSQLQuery(
                "SELECT amo_credito, SUM(amo_amortizacion), COUNT(amo_id) " +
                "FROM   amortizacion " +
                "WHERE  amo_estatus_int = 1 AND amo_fecha_pago >= :fSig " +
                "GROUP  BY amo_credito")
                .setParameter("fSig", fSig)
                .list();
            LOGGER.info("   → capitales          filas = " + capitalRows.size());

            /* 3-C. Interés proyectado ---------------------------------------- */
            List<Object[]> interesRows = session.createSQLQuery(
                "SELECT a.amo_credito, ANY_VALUE(a.amo_interes), " +
                "       ((DATEDIFF(:fAnt, s.sol_fecha_creacion))/14) * " +
                "       (ANY_VALUE(a.amo_interes) / " +
                "        ((DATEDIFF(c.cre_fecha_primer_pago, s.sol_fecha_creacion))/14)) " +
                "         AS saldo_proyectado " +
                "FROM   amortizacion a " +
                "       JOIN creditos_final c ON c.cre_id = a.amo_credito " +
                "       JOIN solicitudes    s ON s.sol_id = c.cre_solicitud " +
                "WHERE  a.amo_estatus_int = 1 " +
                "GROUP  BY a.amo_credito")
                .setParameter("fAnt", fAnt)
                .list();
            LOGGER.info("   → intereses          filas = " + interesRows.size());

            /* 4. Mapas rápidos ----------------------------------------------- */
            Map<Integer, Aggregate> mapPend    = toAggregateMap(pendientesRows);
            Map<Integer, Aggregate> mapCapital = toAggregateMap(capitalRows);

            Map<Integer, Aggregate> mapInteres = new HashMap<>();
            for (Object[] r : interesRows) {
                int    creId = ((Number) r[0]).intValue();
                double saldo = r[2] != null ? ((Number) r[2]).doubleValue() : 0d;
                mapInteres.put(creId, new Aggregate(saldo, 0));
            }

            /* 5. Construcción de DTOs ---------------------------------------- */
            List<DetalleCreditoDto> resultado = new ArrayList<>();

            for (Object[] r : filasCreditosNorm) {
                resultado.add(crearDtoDesdeRow(r, mapPend, mapCapital, null));
            }
            for (Object[] r : filasCreditosEsp) {
                resultado.add(crearDtoDesdeRow(r, mapInteres, mapCapital, r));
            }

            LOGGER.info("► Fin getDetalleAdeudoCreditoFiniquito – DTOs=" + resultado.size());
            return resultado;

        } catch (Exception ex) {
            LOGGER.error("✖ Error en getDetalleAdeudoCreditoFiniquito", ex);
            throw new IntegracionException("Error al obtener detalle de adeudo de crédito", ex);
        } finally {
            super.endTransaction();
        }
    }

    /* ========================================================================== */
    /* Utilidades privadas                                                        */
    /* ========================================================================== */

    /** Convierte filas  [creditoId, suma, conteo]  →  Map<credId,Aggregate>. */
    private Map<Integer, Aggregate> toAggregateMap(List<Object[]> rows) {
        Map<Integer, Aggregate> map = new HashMap<>();
        for (Object[] r : rows) {
            int     cred   = ((Number) r[0]).intValue();
            double  suma   = r[1] != null ? ((Number) r[1]).doubleValue() : 0d;
            long    conteo = r[2] != null ? ((Number) r[2]).longValue()   : 0L;
            map.put(cred, new Aggregate(suma, conteo));
        }
        return map;
    }

    /** Construye un DetalleCreditoDto a partir de la fila base y los agregados. */
    private DetalleCreditoDto crearDtoDesdeRow(
            Object[] baseRow,
            Map<Integer, Aggregate> mapPend,
            Map<Integer, Aggregate> mapCapital,
            Object[] filaEspecial /* null para créditos normales */) {

        int    creId         = ((Number) baseRow[0]).intValue();
        String creClave      = (String)  baseRow[1];
        String proDesc       = (String)  baseRow[2];
        double crePrestamo   = baseRow[3] != null ? ((Number) baseRow[3]).doubleValue() : 0d;
        BigInteger creCatorcenas = (baseRow[4] != null) ? new BigInteger(((Number) baseRow[4]).toString()) : BigInteger.ONE;
        String estatusNom    = (String)  baseRow[5];
        int    estatusId     = ((Number) baseRow[6]).intValue();

        Aggregate pend = mapPend   .getOrDefault(creId, new Aggregate());
        Aggregate cap  = mapCapital.getOrDefault(creId, new Aggregate());

        DetalleCreditoDto dto = new DetalleCreditoDto();
        dto.setCreId(creId);
        dto.setCreClave(creClave);
        dto.setProDescripcion(proDesc);
        dto.setCrePrestamo(crePrestamo);
        dto.setCreCatorcenas(creCatorcenas);
        dto.setSaldoPendiente(pend.sum);
        dto.setSaldoCapital(cap.sum);
        dto.setSaldoTotal(pend.sum + cap.sum);
        dto.setCreEstatusNombre(estatusNom);
        dto.setCreEstatusId(estatusId);

        // ► catorcenas pendientes:
        //   - normales: pend.count + cap.count
        //   - especiales: la propia catorcena del crédito (filaEspecial != null)
        BigInteger catPend;
        if (filaEspecial == null) {                     // créditos normales
            long totalCats = pend.count + cap.count;
            catPend = BigInteger.valueOf(totalCats);
        } else {                                        // créditos especiales
            catPend = creCatorcenas;                    // ya es BigInteger
        }
        dto.setCatorcenasPendientes(catPend);

        return dto;
    }

    /** Estructura auxiliar - suma y conteo por crédito. */
    private static class Aggregate {
        double sum;
        long   count;
        Aggregate() { this(0,0); }
        Aggregate(double s, long c) { sum = s; count = c; }
    }



    /**
     * Obtiene el detalle de un credito de un usuario, regresando información
     * como saldos por catorcenas pendientes y por capital, dicha consulta es
     * utilizable en finiquitos y en solicitudes pendientes
     *
     * @param idCredito
     * @param catAnterior
     * @param catSiguiente
     * @return
     * @throws IntegracionException
     */
    public DetalleCreditoDto getDetalleAdeudoCreditoByCreId(int idCredito, Date catAnterior, Date catSiguiente) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(String.format("select cre_id as creId, cre_clave as creClave, "
                + "pro_descripcion as proDescripcion, cre_prestamo as crePrestamo,"
                + "cre_catorcenas as creCatorcenas, "
                + "IFNULL(cat_pendts.saldoPendiente,0) as saldoPendiente,"
                + "IFNULL(capital_pendt.saldoCapital,0) as saldoCapital,"
                + "IFNULL(cat_pendts.saldoPendiente,0)+IFNULL(capital_pendt.saldoCapital,0) as saldoTotal,"
                + "cre_est_nombre as creEstatusNombre "
                + "from creditos_final left join ("
                + "select amo_credito, sum(amo_monto_pago) as saldoPendiente from amortizacion "
                + "where amo_estatus_int = 1 "
                + "and amo_fecha_pago <= '%1$s' "
                + "group by amo_credito ) cat_pendts on cat_pendts.amo_Credito = creditos_final.cre_id "
                + "left join ( "
                + "select amo_credito, sum(amo_amortizacion) as saldoCapital from amortizacion "
                + "where amo_estatus_int = 1 "
                + "and amo_fecha_pago >= '%2$s' "
                + "group by amo_credito) capital_pendt on capital_pendt.amo_credito = creditos_final.cre_id "
                + "inner join productos on creditos_final.cre_producto = productos.pro_id "
                + "inner join credito_estatus on credito_estatus.cre_est_id = creditos_final.cre_estatus "
                + "where cre_id =%3$s ", Util.generaFechaFormateada(catAnterior), Util.generaFechaFormateada(catSiguiente), idCredito));

        DetalleCreditoDto lstPagos = (DetalleCreditoDto) query.setResultTransformer(Transformers.aliasToBean(DetalleCreditoDto.class)).uniqueResult();

        super.endTransaction();
        return lstPagos;

    }

    
      public void updtPagosPagados(int idCredito) throws IntegracionException {

            String sql = String.format(""
                    + "update pagos inner join amortizacion on pag_id = amo_pago_id " +
                        "set pag_credito = null, pag_estatus = 7, pag_acumulado = pag_deposito " +
                        "where amo_credito = %1$s ",
                    idCredito);
            
            super.executeUpdateSql(sql);
    }
    
    
    /**
     * *
     * Borra toda la amortizacion de un credito
     *
     * @param creId
     * @throws IntegracionException
     */
    public void removeAmortizacion(Integer creId) throws IntegracionException {
        String hql = String.format("delete from Amortizacion amo "
                + "where amo.creditosFinal.creId = %1$s and amo.amoEstatusInt = 1 ", creId);

        super.executeUpdate(hql);
    }

    /**
     * Obtiene los creditos de un usuario
     *
     * @param idUsuario
     * @return
     * @throws IntegracionException
     */
    public List<CreditoDto> getCreditosXIdUsr(int idUsuario) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format(""
                    + "select cre_id as creId, cre_clave as creClave, "
                    + "credito_estatus.cre_est_nombre as creEstatusStr, productos.pro_descripcion as creTipo, "
                    + "cre_prestamo as crePrestamo, cre_catorcenas as creCatorcenas "
                    + "from creditos_final inner join credito_estatus on creditos_final.cre_estatus = credito_estatus.cre_est_id "
                    + "inner join productos on pro_id = creditos_final.cre_producto "
                    + "where cre_usu_id = %1$s",
                    idUsuario));

            List<CreditoDto> creditos = query.setResultTransformer(Transformers.aliasToBean(CreditoDto.class)).list();

            return creditos;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Obtiene los creditos de los que es aval un usuario
     *
     * @param idUsuario
     * @return
     * @throws IntegracionException
     */
    public List<CreditoDto> getCreditosDeAval(int idUsuario) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format(""
                    + "select cre_clave as creClave, cre_prestamo as crePrestamo, cre_catorcenas as creCatorcenas, "
                    + "cre_est_nombre as creEstatusStr, productos.pro_siglas as creTipo, "
                    + "usu_clave_empleado as creClaveEmpleado, emp_abreviacion as creEmpresa "
                    + "from solicitud_avales inner join creditos_final on cre_id = solicitud_avales.sol_ava_credito "
                    + "inner join credito_estatus on creditos_final.cre_estatus = credito_estatus.cre_est_id "
                    + "inner join productos on creditos_final.cre_producto = productos.pro_id "
                    + "inner join usuarios on cre_usu_id = usu_id "
                    + "inner join empresas on emp_id = usu_empresa "
                    + "where solicitud_avales.sol_ava_id_empleado  = %1$s",
                    idUsuario));

            List<CreditoDto> creditos = query.setResultTransformer(Transformers.aliasToBean(CreditoDto.class)).list();

            return creditos;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Obtiene el credito transferido del aval al cual le correpsonde
     *
     * @param crePadre - id del credito padre
     * @param idUsuario - id del aval
     * @return
     * @throws IntegracionException
     */
    public CreditoDto getCreXPadreUsr(int crePadre, int idUsuario) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format(""
                    + "select cre_id as creId, cre_clave as creClave, "
                    + "credito_estatus.cre_est_nombre as creEstatusStr, productos.pro_descripcion as creTipo, "
                    + "cre_prestamo as crePrestamo, cre_catorcenas as creCatorcenas "
                    + "from creditos_final inner join credito_estatus on creditos_final.cre_estatus = credito_estatus.cre_est_id "
                    + "inner join productos on pro_id = creditos_final.cre_producto "
                    + "where cre_usu_id = %1$s "
                    + "and cre_padre = %2$s",
                    idUsuario, crePadre));

            CreditoDto creditos = (CreditoDto) query.setResultTransformer(Transformers.aliasToBean(CreditoDto.class)).uniqueResult();

            return creditos;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Obtiene los creditos de un usuario
     *
     * @param idCredito
     * @return
     * @throws IntegracionException
     */
    public List<AvalesCreditoDto> getAvalesXCredito(Integer idCredito) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select CONCAT_WS(' ',usu_nombre,usu_paterno,usu_materno) as avalNombreCompleto, "
                            + "sol_ava_credito as creId, "
                            + "usu_clave_empleado as avalCveEmpleado, "
                            + "sol_ava_solicitud as solAvaSolicitud, "
                            + "emp_abreviacion as empAbrev, "
                            + "usu_id as avalUsuId "
                            + "from solicitud_avales inner join usuarios on usu_id = sol_ava_id_empleado "
                            + "inner join empresas on emp_id = usu_empresa "
                            + "inner join creditos_final on cre_id = sol_ava_credito "
                            + "where cre_id = %1$s "
                            + "and usu_fecha_baja is null ", idCredito));

            List<AvalesCreditoDto> creditos = query.setResultTransformer(Transformers.aliasToBean(AvalesCreditoDto.class)).list();

            return creditos;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Guarda el credito
     *
     * @param credito
     * @return
     * @throws IntegracionException
     */
    public CreditosFinal guardaCredito(CreditosFinal credito) throws IntegracionException {
        super.beginTransaction();

        try {
            super.session.persist(credito);
            super.session.flush();
            super.session.getTransaction().commit();
            
            LOGGER.debug("El credito se guardó correctamente");
        } catch (Exception ex) {
            super.session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage() + " No se pudo guardar el credito", ex);

        }finally{
            super.endTransaction();
        }
        return credito;
    }

    public void updtCreClave(Integer creId, String creClave) throws IntegracionException {
        String hql = String.format("update CreditosFinal "
                + "set creClave = '%1$s' "
                + "where creId = %2$s ", creClave, creId);

        super.executeUpdate(hql);
    }

    /**
     * Actualiza el campo creFechaNuevoMonto de un credito
     *
     * @param catorcenaSiguiente
     * @param idCredito
     * @throws IntegracionException
     */
    public void actualizaFechaNuevoMonto(Date catorcenaSiguiente, int idCredito) throws IntegracionException {

        String hql = String.format("update CreditosFinal set creFechaNuevoMonto = '%1$s' where creId = %2$s",
                Util.generaFechaFormateada(catorcenaSiguiente), idCredito);

        super.executeUpdate(hql);

    }

    /**
     * Obtiene un credito por id
     *
     * @param idCredito
     * @return
     * @throws IntegracionException
     */
    public CreditosFinal obtieneCreditoXId(int idCredito) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(String.format(""
                    + "from CreditosFinal "
                    + "where creId= %1$s ",
                    idCredito));

            CreditosFinal credito = (CreditosFinal) query.uniqueResult();
            return credito;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Actualiza un credito en el monto de pago quincenal, en las catorcenas
     * totales, y se le asigna la fecha en que fue actualizado
     *
     * @param idCredito
     * @param pago
     * @param catorcenas
     * @param fechaActualizacion
     * @throws IntegracionException
     */
    public void actualizaCreditoEnPagoYCatorcenas(int idCredito, double pago, int catorcenas, Date fechaActualizacion) throws IntegracionException {

        String sql = String.format("update creditos_final "
                + "set creditos_final.cre_catorcenas = %1$s, "
                + "cre_pago_quincenal = %2$s, "
                + "cre_fecha_nuevo_monto = '%3$s'"
                + "where cre_id = %4$s",
                catorcenas, pago, Util.generaFechaFormateada(fechaActualizacion), idCredito);

        super.executeUpdateSql(sql);

    }

    /**
     * Obtiene los creditos de un usuario
     *
     * @param idCredito
     * @return
     * @throws IntegracionException
     */
    public CreditoDto getCreditoXCreID(int idCredito) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format(""
                    + "select cre_id as creId, cre_clave as creClave, usu_clave_empleado as creClaveEmpleado,"
                    + "CONCAT_WS(' ',usu_nombre,usu_paterno,usu_materno) as nombreEmpleado, "
                    + "credito_estatus.cre_est_nombre as creEstatusStr, productos.pro_descripcion as creTipo, "
                    + "cre_prestamo as crePrestamo, cre_catorcenas as creCatorcenas "
                    + "from creditos_final inner join credito_estatus on creditos_final.cre_estatus = credito_estatus.cre_est_id "
                    + "inner join productos on pro_id = creditos_final.cre_producto "
                    + "inner join usuarios on usu_id = cre_usu_id "
                    + "where cre_id = %1$s",
                    idCredito));

            CreditoDto creditos = (CreditoDto) query.setResultTransformer(Transformers.aliasToBean(CreditoDto.class)).uniqueResult();

            return creditos;
        } catch (Exception he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public List<DetalleCreditoDto> getCreditosBasicosPorUsuario(Integer usuId) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format(
            "SELECT "
            +"cre_id AS creId, "
            +"cre_clave AS creClave, "
            +"pro_descripcion AS proDescripcion, "
            +"cre_prestamo AS crePrestamo, "
            +"IFNULL(cre_catorcenas, 1) AS creCatorcenas, "
            +"cre_est_nombre AS creEstatusNombre, "
            +"cre_est_id AS creEstatusId  "
            +"FROM creditos_final "
            +"INNER JOIN productos ON creditos_final.cre_producto = productos.pro_id "
            +"INNER JOIN credito_estatus ON creditos_final.cre_estatus = credito_estatus.cre_est_id "
            +"WHERE cre_usu_id = %1$s "
            +"AND cre_producto NOT IN (4,5,11) ",
            usuId));

            List<DetalleCreditoDto> results = query.setResultTransformer(Transformers.aliasToBean(DetalleCreditoDto.class)).list();
            
            super.endTransaction();
            return results;
        } catch (Exception e) {
            super.endTransaction();
            throw new IntegracionException("Error al obtener créditos básicos por usuario", e);
        }
    }

   public Map<Integer, TotalesAmortizacionDto> getTotalesAmortizacion(List<Integer> idsCredito, Date catAnterior, Date catSiguiente) throws IntegracionException {
    try {

        System.out.println("Dentro de getTotalesAmortizacion: \n catorcena anterior:  "+catAnterior +"  \n catorcena siguiente:  "+ catSiguiente);
        idsCredito.forEach(c -> {
            System.out.println("Credito: " + c);
        });
        super.beginTransaction();

        String sql = ""
            + "SELECT "
            + "    amo_credito AS creditoId,"
            + "    SUM(CASE WHEN amo_fecha_pago <= :catAnterior THEN amo_monto_pago ELSE 0 END) AS saldoPendiente,"
            + "    SUM(CASE WHEN amo_fecha_pago <= :catAnterior THEN 1 ELSE 0 END) AS catPendAdeudo,"
            + "    SUM(CASE WHEN amo_fecha_pago >= :catSiguiente THEN amo_amortizacion ELSE 0 END) AS saldoCapital,"
            + "    SUM(CASE WHEN amo_fecha_pago >= :catSiguiente THEN 1 ELSE 0 END) AS catPendCap "
            + "FROM amortizacion "
            + "WHERE amo_estatus_int = 1 "
            + "  AND amo_credito IN (:idsCredito) "
            + "GROUP BY amo_credito";

        Query query = session.createSQLQuery(sql)
                .setParameterList("idsCredito", idsCredito)
                .setParameter("catAnterior", catAnterior)
                .setParameter("catSiguiente", catSiguiente);

        List<TotalesAmortizacionDto> resultados = query
                .setResultTransformer(Transformers.aliasToBean(TotalesAmortizacionDto.class))
                .list();

        Map<Integer, TotalesAmortizacionDto> map = new HashMap<>();
        for (TotalesAmortizacionDto dto : resultados) {
            map.put(dto.getCreditoId(), dto);
        }

        super.endTransaction();
        return map;
    } catch (Exception e) {
        super.endTransaction();
        throw new IntegracionException("Error al obtener los totales de amortización", e);
    }
}

    public static void main(String[] args) {
       
        CreditosDao dao = new CreditosDao();
        List<Integer> idsCredito = Arrays.asList(7911);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {

             HibernateUtil.buildSessionFactory2();
            Date catAnterior = sdf.parse("2024-08-08");
            Date catSiguiente = sdf.parse("2024-08-22");

            Map<Integer, TotalesAmortizacionDto> resultados = dao.getTotalesAmortizacion(idsCredito, catAnterior, catSiguiente);
            
            for (Map.Entry<Integer, TotalesAmortizacionDto> entry : resultados.entrySet()) {
                TotalesAmortizacionDto dto = entry.getValue();
                System.out.println("Credito ID: " + dto.getCreditoId());
                System.out.println("Saldo Pendiente: " + dto.getSaldoPendiente());
                System.out.println("Catorcenas Pendientes Adeudo: " + dto.getCatPendAdeudo());
                System.out.println("Saldo Capital: " + dto.getSaldoCapital());
                System.out.println("Catorcenas Pendientes Capital: " + dto.getCatPendCap());
            }

                HibernateUtil.closeSessionFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }

/*
        try {
            HibernateUtil.buildSessionFactory2();
            CreditosDao dao = new CreditosDao();

            List creditos = dao.getDetalleAdeudoCredito(828, Util.generaFechaDeString("2018-08-16","yyyy-MM-dd"),Util.generaFechaDeString("2018-08-30","yyyy-MM-dd"));

           

            HibernateUtil.closeSessionFactory();

        } catch (IntegracionException ex) {
            ex.printStackTrace();
        } 
*/

    }


}
