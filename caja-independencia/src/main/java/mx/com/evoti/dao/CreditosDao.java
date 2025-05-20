/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.TotalesAmortizacionDto;

import java.io.Serializable;
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

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(String.format("select cre_id as creId, cre_clave as creClave, " +
                " pro_descripcion as proDescripcion, cre_prestamo as crePrestamo, " +
"                 cre_catorcenas as creCatorcenas, " +
"                 IFNULL(cat_pendts.saldoPendiente,0) as saldoPendiente, " +
"                 IFNULL(capital_pendt.saldoCapital,0) as saldoCapital, " +
"                 IFNULL(cat_pendts.saldoPendiente,0)+IFNULL(capital_pendt.saldoCapital,0) as saldoTotal, " +
"                 cre_est_nombre as creEstatusNombre, " +
"                 cre_est_id as creEstatusId, " +
"                 IFNULL(capital_pendt.catPendCap,0)+IFNULL(cat_pendts.catPendAdeudo,0) as catorcenasPendientes " +
"                 from creditos_final left join ( " +
"                 select amo_credito, sum(amo_monto_pago) as saldoPendiente, count(amo_id) as catPendAdeudo from amortizacion " +
"                 where amo_estatus_int = 1 " +
"                 and amo_fecha_pago <= '%1$s' " +
"                 group by amo_credito ) cat_pendts on cat_pendts.amo_Credito = creditos_final.cre_id " +
"                 left join ( " +
"                 select amo_credito, sum(amo_amortizacion) as saldoCapital, count(amo_id) as catPendCap from amortizacion " +
"                 where amo_estatus_int = 1 " +
"                 and amo_fecha_pago >= '%2$s' " +
"                 group by amo_credito) capital_pendt on capital_pendt.amo_credito = creditos_final.cre_id " +
"                 inner join productos on creditos_final.cre_producto = productos.pro_id " +
"                 inner join credito_estatus on credito_estatus.cre_est_id = creditos_final.cre_estatus " +
"                where cre_usu_id =%3$s " +
"                and cre_producto not in (4,5,11) " +
"                " +
"                UNION " +
"                select cre_id as creId, cre_clave as creClave, " +
"                pro_descripcion as proDescripcion, cre_prestamo as crePrestamo, " +
"                IFNULL(cre_catorcenas, 1) as creCatorcenas, " +
"                round(IFNULL(cat_pendts.saldoPendiente,0), 2) as saldoPendiente," +
"                round(IFNULL(capital_pendt.saldoCapital,0),2) as saldoCapital," +
"                round(IFNULL(cat_pendts.saldoPendiente,0)+IFNULL(capital_pendt.saldoCapital,0),2) as saldoTotal," +
"                cre_est_nombre as creEstatusNombre, " +
"                cre_est_id as creEstatusId, " +
"                IFNULL(cre_catorcenas, 1) as catorcenasPendientes " +
"                 from creditos_final left join ( " +
"                select amo_credito, count(amo_id) as catPendAdeudo, " +
"                s.sol_fecha_Creacion as FechaCreacion,c.cre_fecha_primer_pago as FechaPrimerPago,a.amo_interes as Interes," +
"                 ((datediff('%1$s',s.sol_fecha_Creacion))/14)*((a.amo_interes)/ " +
"                 ((datediff(c.cre_fecha_primer_pago,s.sol_fecha_Creacion))/14)) as saldoPendiente " +
"                from amortizacion a left join creditos_final c on a.amo_credito=c.cre_id " +
"                left join solicitudes s on c.cre_solicitud=s.sol_id " +
"                where amo_estatus_int = 1 " +
"                group by amo_credito " +
"                ) cat_pendts on cat_pendts.amo_Credito = creditos_final.cre_id " +
"                left join ( " +
"                select amo_credito, sum(amo_capital) as saldoCapital, count(amo_id) as catPendCap from amortizacion " +
"                where amo_estatus_int = 1 " +
"                and amo_fecha_pago >= '%2$s' " +
"                group by amo_credito) capital_pendt on capital_pendt.amo_credito = creditos_final.cre_id " +
"                inner join productos on creditos_final.cre_producto = productos.pro_id " +
"                inner join credito_estatus on credito_estatus.cre_est_id = creditos_final.cre_estatus " +
"                where cre_usu_id =%3$s " +
"                and cre_producto in (4,5,11) ", Util.generaFechaFormateada(catAnterior), Util.generaFechaFormateada(catSiguiente), idUsuario));

        List<DetalleCreditoDto> lstPagos = query.setResultTransformer(Transformers.aliasToBean(DetalleCreditoDto.class)).list();

        super.endTransaction();
        return lstPagos;

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
            +"cre_id AS creId,"
            +"cre_clave AS creClave,"
            +"pro_descripcion AS proDescripcion,"
            +"cre_prestamo AS crePrestamo,"
            +"IFNULL(cre_catorcenas, 1) AS creCatorcenas,"
            +"cre_est_nombre AS creEstatusNombre,"
            +"cre_est_id AS creEstatusId"
            +"FROM creditos_final "
            +"INNER JOIN productos ON creditos_final.cre_producto = productos.pro_id "
            +"INNER JOIN credito_estatus ON creditos_final.cre_estatus = credito_estatus.cre_est_id "
            +"WHERE cre_usu_id = %1$s "
            +"AND cre_producto NOT IN (4,5,11)",
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


    public static void main(String args[]) {
        try {
            HibernateUtil.buildSessionFactory2();
            CreditosDao dao = new CreditosDao();

            List creditos = dao.getDetalleAdeudoCredito(828, Util.generaFechaDeString("2018-08-16","yyyy-MM-dd"),Util.generaFechaDeString("2018-08-30","yyyy-MM-dd"));

           

            HibernateUtil.closeSessionFactory();

        } catch (IntegracionException ex) {
            ex.printStackTrace();
        }
    }

}
