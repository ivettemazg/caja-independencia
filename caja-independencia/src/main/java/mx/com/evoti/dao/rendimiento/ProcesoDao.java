package mx.com.evoti.dao.rendimiento;

import mx.com.evoti.dao.*;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Rendimiento;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class ProcesoDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 8958190915055950161L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProcesoDao.class);

    /**
     * Actualiza un pojo del tipo Rendimiento
     *
     * @param rendimiento
     * @throws IntegracionException
     */
    public void updtRendimiento(Rendimiento rendimiento) throws IntegracionException {
        super.updatePojo(rendimiento);
    }

    /**
     * Metodo que regresa las fechas de rendimiento
     *
     * @return
     * @throws IntegracionException
     */
    public HashMap getFechas() throws IntegracionException {
        super.beginTransaction();
        Query sqlQuery = session.createQuery(
                String.format("select renId,renFecha "
                        + "from Rendimiento "
                        + "where renEstatus=%1$s order by ren_fecha", 0));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        List<Object[]> results = (List<Object[]>) sqlQuery.list();
        HashMap hm = new HashMap();

        for (Object[] result : results) {
            try {
                hm.put(df.format(df.parse(result[1].toString())), result[0]);
            } catch (ParseException ex) {
                Logger.getLogger(ProcesoDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        super.endTransaction();
        return hm;
    }

    /**
     * Metodo que regresa los movimientos de usuarios para el ahorro fijo y no
     * fijo
     *
     * @param fecha
     * @return
     * @throws IntegracionException
     */
    public List getUsuariosMovimientos(String fecha) throws IntegracionException {
        String sql = String.format("select usu_id,mov_producto,sum(mov_deposito) as acumulado "
                + "from usuarios u left join movimientos m on u.usu_id=m.mov_usu_id "
                + "where (usu_fecha_baja is null or usu_fecha_baja>'%1$s') "
                + "and mov_fecha<date_sub('%1$s', interval DAY('%1$s') day) and m.mov_ar=1 and mov_producto in (1,2) "
                + "group by usu_id,mov_producto having acumulado>0", fecha);

        super.beginTransaction();
        SQLQuery sqlQuery = session.createSQLQuery(sql);

        List<Object[]> results = (List<Object[]>) sqlQuery.list();
        super.endTransaction();
        return results;
    }

    /**
     * Metodo que regresa los movimientos de usuarios para el ahorro voluntario
     *
     * @param fecha
     * @return
     * @throws IntegracionException
     */
    public List getUsuariosMovimientosVol(String fecha) throws IntegracionException {
        String sql = String.format("select mov_usu_id,mov_producto,mov_deposito+ifnull((select sum(v.mov_deposito) from movimientos v "
                + "where m.mov_id=v.mov_id_padre and v.mov_tipo='DEVOLUCION' and mov_ar=1 "
                + "and v.mov_fecha<date_sub('%1$s', interval day('%1$s') day)),0) as acumulado,mov_id "
                + "from movimientos m where mov_producto=3 and mov_tipo='APORTACION' "
                + "and mov_fecha<date_sub('%1$s', interval day('%1$s') day) "
                + "group by mov_usu_id,mov_producto,mov_id having acumulado>0", fecha);

        super.beginTransaction();
        SQLQuery sqlQuery = session.createSQLQuery(sql);

        List<Object[]> results = (List<Object[]>) sqlQuery.list();
        super.endTransaction();
        return results;
    }

    /**
     * Metodo que regresa los movimientos de usuarios para el ahorro voluntario
     *
     * @param fecha
     * @return
     * @throws IntegracionException
     */
    public List getPagos(String fecha) throws IntegracionException {
        String sql = String.format("select pag_estatus,sum(a.amo_interes) "
                + "from pagos p left join amortizacion a on p.pag_id=a.amo_pago_id "
                + "where year(pag_fecha)=year('%1$s') "
                + "and month(pag_fecha)=month('%1$s') and pag_estatus in (2,4,5,6,8,9,10) "
                + "group by pag_estatus", fecha);

        super.beginTransaction();
        SQLQuery sqlQuery = session.createSQLQuery(sql);

        List<Object[]> results = (List<Object[]>) sqlQuery.list();
        super.endTransaction();
        return results;
    }
    
        public void updateAmountsv2(List<Movimientos> pojos) throws IntegracionException {
        //super.save(pojo);
        for(Movimientos pojo:pojos){
            super.savePojo(pojo);
        }
    }

    public void updateAmounts(HashMap valores, String fecha, String idFecha) throws IntegracionException {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            Rendimiento rendimiento = new Rendimiento(df.parse(fecha), Double.parseDouble(valores.get("pagosTotal").toString()),
                    Double.parseDouble(valores.get("acumuladoTotal").toString()), Double.parseDouble(valores.get("factorRendimiento").toString()), 1,
                    Double.parseDouble(valores.get("interesInversion").toString()), Double.parseDouble(valores.get("comision").toString()),
                    Double.parseDouble(valores.get("reserva").toString()), Double.parseDouble(valores.get("interesNetoMensual").toString()));

            rendimiento.setRenId(Integer.parseInt(idFecha));

            updtRendimiento(rendimiento);
           
        } catch (ParseException ex) {
            Logger.getLogger(ProcesoDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Rendimiento getInversionesComisiones(String selectedDate) throws IntegracionException {
         try {
            super.beginTransaction();

            Query query = session.createQuery(String.format("from Rendimiento where ren_fecha = '%1$s' order by 1 desc", selectedDate));

            Rendimiento rto = (Rendimiento) query.uniqueResult();
            return rto;
        } catch (HibernateException he) {
            LOGGER.error(he.getMessage(), he);
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }
}
