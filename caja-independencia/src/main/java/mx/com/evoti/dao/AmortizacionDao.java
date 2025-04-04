/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Util;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class AmortizacionDao extends ManagerDB implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AmortizacionDao.class);
    private static final long serialVersionUID = -3150215348228466408L;

    /**
     * Actualiza un registro de amortizacion
     *
     * @param pojo
     * @throws IntegracionException
     */
    public void updtAmortizacion(Amortizacion pojo) throws IntegracionException {
        super.updatePojo(pojo);
    }

    public void borraAmortizacionPosteriorCap(Integer numeroPago, Integer creId) throws IntegracionException {
        String hql = String.format("delete from Amortizacion where "
                + "amoNumeroPago > %1$s "
                + "and amoEstatusInt = 1 "
                + "and creditosFinal.creId = %2$s", numeroPago, creId);

        super.executeUpdate(hql);
    }

    public void insertaAmortizacion(List amortizacion) throws IntegracionException {
        super.savePojos(amortizacion);
    }

    /**
     * Obtiene las catorcenas anteriores
     *
     * @param idCredito
     * @param catorcena
     * @return
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public List<Amortizacion> getCatorcenasPendtsAnteriores(int idCredito, Date catorcena) throws IntegracionException {
        try {
            super.beginTransaction();

            Query hqlQry = session.createQuery(
                    String.format("from Amortizacion amo "
                            + "where amo.amoEstatusInt = 1 "
                            + "and amo.creditosFinal.creId = %1$s "
                            + "and amo.amoFechaPago <= '%2$s' "
                            + "order by amo.amoNumeroPago asc ", idCredito, Util.generaFechaFormateada(catorcena)));

            List<Amortizacion> creditos = hqlQry.list();

            return creditos;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Obtiene la amortizacion donde se meterá el pago a capital
     *
     * @param idCredito
     * @param catorcena
     * @return
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public Amortizacion getAmortizacionPCap(int idCredito, Date catorcena) throws IntegracionException {
        super.beginTransaction();

        Query hqlQuery = session.createQuery(
                String.format("from Amortizacion amo "
                        + "where amo.amoEstatusInt = 1 "
                        + "and amo.creditosFinal.creId = %1$s "
                        + "and amo.amoFechaPago >= '%2$s' "
                        + "order by amo.amoFechaPago asc ", idCredito, Util.generaFechaFormateada(catorcena)));

        List<Amortizacion> lst = hqlQuery.list();

        if (!lst.isEmpty()) {
            return lst.get(0);

        }

        super.endTransaction();
        return null;

    }

    /**
     * Obtiene la cantidad de catorcenas que tiene pendientes un credito
     *
     * @param idCredito
     * @return
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public Integer getAmoPendienteXCred(int idCredito) throws IntegracionException {
        super.beginTransaction();

        Query hqlQuery = session.createQuery(
                String.format("select count(*) from Amortizacion amo "
                        + "where amoEstatusInt in (1, 3) "
                        + "and amo.creditosFinal.creId = %1$s ", idCredito));

        Long numPendientes = (Long) hqlQuery.uniqueResult();

        super.endTransaction();
        return numPendientes.intValue();

    }

    public List<AmortizacionDto> getAmortizacionXCredito(int idCredito) throws IntegracionException {
        super.beginTransaction();

        SQLQuery sqlQuery = session.createSQLQuery(
                String.format("select amo_id as amoId, amo_numero_pago as amoNumeroPago, amo_capital as amoCapital,"
                        + "amo_amortizacion as amoAmortizacion, amo_interes as amoInteres, amo_iva as amoIva, amo_monto_pago as amoMontoPago,"
                        + "amo_credito as amoCredito, amo_fecha_pago as amoFechaPago, amortizacion_estatus.amo_est_nombre as amoEstatus,"
                        + "amo_estatus_int as amoEstatusInt, amo_est_color as amoEstColor, amo_producto as amoProducto, amo_clave_empleado as amoClaveEmpleado,"
                        + "amo_pago_id as amoPagoId "
                        + "from amortizacion inner join amortizacion_estatus on amo_estatus_int = amo_est_id "
                        + "where amo_credito = %1$s "
                        + "order by amo_numero_pago asc", idCredito));

        List<AmortizacionDto> amortizacion = sqlQuery.setResultTransformer(Transformers.aliasToBean(AmortizacionDto.class)).list();
        super.endTransaction();
        return amortizacion;

    }

    /**
     * Metodo que actualiza el pagid en la amortizacion de un credito
     *
     * @param pagId
     * @param creId
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public void updtPagId(Integer pagId, Integer creId) throws IntegracionException {
        String hql = String.format("update Amortizacion "
                + "set amoPagoId = %1$s "
                + "where amoEstatusInt = 9 "
                + "and creditosFinal.creId = %2$s", pagId, creId);

        super.executeUpdate(hql);

    }

    /**
     * Actualiza la amortización cuando el crédito se ha transferido
     *
     * @param creId
     * @param estatus
     * @throws IntegracionException
     */
    public void updtAmoEstatusInt(Integer creId, Integer estatus) throws IntegracionException {
        String hql = String.format("update Amortizacion "
                + "set amoEstatusInt = %2$s "
                + "where creditosFinal.creId = %1$s "
                + "and amoEstatusInt = 1 ", creId, estatus);

        super.executeUpdate(hql);

    }

    /**
     * Actualiza el estatus de una amortizacion por amo_id
     *
     * @param amoId
     * @param estatus
     * @throws IntegracionException
     */
    public void updtAmoEstIntByAmoId(Integer amoId, Integer estatus) throws IntegracionException {
        String hql = String.format("update Amortizacion "
                + "set amoEstatusInt = %2$s "
                + "where amoId = %1$s ", amoId, estatus);

        super.executeUpdate(hql);

    }

    public Integer consultaUltimoPagoAmortizacion() throws IntegracionException {

        super.beginTransaction();

        SQLQuery hqlQuery = session.createSQLQuery(
                " select max(amo_id) from amortizacion ");

        Integer maxId = (Integer) hqlQuery.uniqueResult();

        super.endTransaction();

        return maxId;
    }

    /**
     * Borra la amortización de un credito especifico
     *
     * @param creditoId
     * @throws IntegracionException
     */
    public void borraAmortizacion(Integer creditoId) throws IntegracionException {
        String sql = String.format("delete from Amortizacion where "
                + "creditosFinal.creId = %1$s", creditoId);
        super.executeUpdate(sql);

    }

    /**
     * Consulta el ultimo pago de la amortizacion de un credito, se utiliza en
     * pagos a capital
     *
     * @param idCredito
     * @return
     * @throws IntegracionException
     */
    public AmortizacionDto consultaUltimoPagoAmortizacion(Integer idCredito) throws IntegracionException {
        AmortizacionDto amortizacion;

        String sql = String.format("select amo_numero_pago as amoNumeroPago,amo_capital as amoCapital,"
                + "amo_amortizacion as amoAmortizacion,amo_interes as amoInteres,"
                + "amo_monto_pago as amoMontoPago,amo_fecha_pago as amoFechaPago,"
                + "amo_estatus_int as amoEstatusInt,amo_credito as amoCredito "
                + "from amortizacion "
                + "where amo_credito = %1$s "
                + "and amo_numero_pago = "
                + "(select max(amo_numero_pago) from amortizacion where amortizacion.amo_credito = %1$s )", idCredito);

        super.beginTransaction();

        SQLQuery sqlQuery = session.createSQLQuery(
                sql);

        amortizacion = (AmortizacionDto) sqlQuery.setResultTransformer(Transformers.aliasToBean(AmortizacionDto.class)).uniqueResult();
        super.endTransaction();
        return amortizacion;
    }

    /**
     * Actualiza la clave del usuario, el usu id y el producto de la
     * amortizacion de un credito
     *
     * @param usuario
     * @param credito
     * @throws IntegracionException
     */
    public void updtCveEmpUsuIdProd(Usuarios usuario, CreditosFinal credito) throws IntegracionException {
        String sql = String.format("update Amortizacion set amoClaveEmpleado = %1$s,"
                + "amoUsuId = %2$s, amoProducto= %3$s "
                + "where creditosFinal.creId = %4$s", usuario.getUsuClaveEmpleado(), usuario.getUsuId(),
                credito.getCreProducto(), credito.getCreId());
        super.executeUpdate(sql);
    }

    /**
     *
     * @param amoId
     * @param estatusInt
     * @param pagId
     * @throws IntegracionException
     */
    public void updtPagoIdEstInt(Integer amoId, Integer estatusInt, Integer pagId) throws IntegracionException {
        String hql = String.format("update Amortizacion set amoPagoId = %1$s, "
                + "amoEstatusInt = %2$s where amoId= %3$s",
                pagId, estatusInt, amoId);
        super.executeUpdate(hql);
    }

    /**
     * Obtiene la lista de amortizacion de un credito
     *
     * @param creId
     * @return
     * @throws IntegracionException
     */
    public List<Amortizacion> getAmortizacionPojo(Integer creId) throws IntegracionException {

        try {
            super.beginTransaction();

            Query query = session.createQuery(String.format(""
                    + "from Amortizacion "
                    + "where creditosFinal.creId = %1$s ",
                    creId));

            List<Amortizacion> creditos = query.list();
            return creditos;
        } catch (HibernateException he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public static void main(String args[]) {
        try {
            HibernateUtil.buildSessionFactory2();
            AmortizacionDao dao = new AmortizacionDao();

            AmortizacionDto amortizacion = dao.consultaUltimoPagoAmortizacion(9);

            System.out.println(amortizacion.getAmoNumeroPago());

            HibernateUtil.closeSessionFactory();

        } catch (IntegracionException ex) {
            ex.printStackTrace();
        }
    }
}
