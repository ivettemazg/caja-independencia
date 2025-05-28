/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.PagosPersonalDto;
import mx.com.evoti.hibernate.pojos.Pagos;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author NeOleon
 */
public class PagosPersonalDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 3938836229899238731L;
        private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PagosPersonalDao.class);


    @SuppressWarnings("unchecked")
    public List<PagosPersonalDto> obtienePagosAgrupados(Integer usuId) throws IntegracionException {

        LOGGER.info("► Inicia obtienePagosAgrupados – usuId=" + usuId);

        super.beginTransaction();
        try {
            String sql =
                "SELECT p.pag_estatus        AS claveEstatus, " +
                "       e.pag_est_nombre     AS estatus, " +
                "       SUM(p.pag_deposito)  AS deposito, " +
                "       SUM(p.pag_acumulado) AS acumulado, " +
                "       COUNT(*)             AS numeroPagos " +
                "FROM   pagos p " +
                "LEFT   JOIN pagos_estatus e ON e.pag_est_id = p.pag_estatus " +
                "WHERE  p.pag_usu_id = :usuId " +
                "GROUP  BY p.pag_estatus, e.pag_est_nombre " +
                "ORDER  BY p.pag_estatus";

            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("usuId", usuId);
            List<PagosPersonalDto> pagos = query
                .setResultTransformer(Transformers.aliasToBean(PagosPersonalDto.class))
                .list();

            LOGGER.info("   → filas devueltas = " + pagos.size());
            return pagos;

        } catch (Exception ex) {
            LOGGER.error("✖ Error en obtienePagosAgrupados", ex);
            throw new IntegracionException("Error al obtener pagos agrupados", ex);
        } finally {
            super.endTransaction();
            LOGGER.info("► Fin obtienePagosAgrupados");
        }
    }

    public void savePago(Pagos pago) throws IntegracionException {
        super.savePojo(pago);
    }

    public List<PagosPersonalDto> obtieneAcumulado(Integer usuId) throws IntegracionException {

        super.beginTransaction();
        String sql = String.format("select 'Acumulado' as leyendaAcumulado, sum(pag_acumulado) as acumulado,"
                + "u.usu_id as usuariosId from pagos p left join usuarios u on pag_usu_id=u.usu_id where pag_usu_id=%1$s "
                + "group by u.usu_id", usuId);

        SQLQuery query = session.createSQLQuery(sql);

        List<PagosPersonalDto> pagos = query.setResultTransformer(Transformers.aliasToBean(PagosPersonalDto.class)).list();

        super.endTransaction();
        return pagos;

    }

    public List<PagosPersonalDto> getPagosDetalle(Integer usuId, Integer pagEstatus) throws IntegracionException {

        super.beginTransaction();
        String sql = String.format("select pag_fecha as fechaPago,pag_deposito as deposito,"
                + "pag_acumulado as acumulado,amo_monto_pago as pagoCatorcena,a.amo_amortizacion as amortizacion, "
                + "a.amo_interes as interes, a.amo_fecha_pago as fechaCatorcena, "
                + "amortizacion_estatus.amo_est_nombre as estatusAmortizacion,"
                + "c.cre_clave as claveCredito "
                + "from pagos p left join amortizacion a on p.pag_id=a.amo_pago_id "
                + "left join creditos_final c on a.amo_credito=c.cre_id "
                + "inner join amortizacion_estatus on amo_estatus_int = amo_est_id "
                + "where pag_usu_id=%1$s and pag_estatus=%2$s order by pag_fecha desc ", usuId, pagEstatus);
        SQLQuery query = session.createSQLQuery(sql);

        List<PagosPersonalDto> pags = query.setResultTransformer(Transformers.aliasToBean(PagosPersonalDto.class)).list();

        super.endTransaction();
        return pags;

    }

}
