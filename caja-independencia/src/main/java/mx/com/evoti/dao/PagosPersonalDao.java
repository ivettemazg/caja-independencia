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

/**
 *
 * @author NeOleon
 */
public class PagosPersonalDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 3938836229899238731L;

    public List<PagosPersonalDto> obtienePagosAgrupados(Integer usuId) throws IntegracionException {

        super.beginTransaction();
        String sql = String.format("select pag_estatus as claveEstatus,pag_est_nombre as estatus,"
                + "sum(pag_deposito) as deposito,sum(pag_acumulado) as acumulado,count(*) as numeroPagos "
                + "from pagos p left join pagos_estatus e on p.pag_estatus=e.pag_est_id where pag_usu_id=%1$s "
                + "group by pag_est_nombre order by pag_estatus", usuId);

        SQLQuery query = session.createSQLQuery(sql);

        List<PagosPersonalDto> pagos = query.setResultTransformer(Transformers.aliasToBean(PagosPersonalDto.class)).list();

        super.endTransaction();
        return pagos;

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
