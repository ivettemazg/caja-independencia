/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.algoritmopagos.PagoAmortizacionDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.ArchivosHistorial;
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
public class AlgoritmoAsignaPagosDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 5317094697306890488L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AlgoritmoAsignaPagosDao.class);

    /**
     * Asigna a amortizacion los pagos exactos
     *
     * @param arhId
     * @throws IntegracionException
     */
    public void updtAmortizacionExacto(int arhId) throws IntegracionException {
        String sql = String.format(
                "update pagos "
                + "inner join usuarios on usu_id = pag_usu_id "
                + "inner join creditos_final on  cre_usu_id = usu_id "
                + "inner join amortizacion on cre_id = amo_credito "
                + "set amo_pago_id = pag_id, amo_estatus = 'PAGADO', amo_estatus_int = 2 "
                + "where (amo_monto_pago-pag_deposito <= 1 and amo_monto_pago-pag_deposito >= -1) "
                + "and amo_fecha_pago = pagos.pag_fecha "
                + "and amo_pago_id is null "
                + "and pag_estatus = 1 "
                + "and amo_monto_pago > 0 "
                + "and amo_usu_id = pag_usu_id "
                + "and pag_arh_id =%1$s "
                + "and cre_producto in (6,7) "
                + "and cre_estatus = 1 "
                , arhId);

        super.executeUpdateSql(sql);

    }

    /**
     * Asigna a amortizacion los pagos mayores
     *
     * @param arhId
     * @throws IntegracionException
     */
    public void updtPagoMayor(int arhId) throws IntegracionException {
        String sql = String.format(
                "update pagos "
                + "inner join usuarios on usu_id = pag_usu_id "
                + "inner join creditos_final on  cre_usu_id = usu_id "
                + "inner join amortizacion on cre_id = amo_credito "
                + "set amo_pago_id = pag_id, amo_estatus = 'PAGO MAYOR' "
                + ", amo_estatus_int = 4 "
                + "where amo_monto_pago < pag_deposito "
                + "and amo_fecha_pago = pagos.pag_fecha "
                + "and amo_pago_id is null "
                + "and amo_monto_pago > 0 "
                + "and amortizacion.amo_estatus_int = 1 "
                + "and pag_estatus = 1 "
                + "and amo_usu_id = pag_usu_id "
                + "and pag_arh_id =%1$s "
                 + "and cre_producto in (6,7) "
                + "and cre_estatus =1  "
                , arhId);

        super.executeUpdateSql(sql);
    }

    /**
     * Asigna a amortizacion los pagos menores
     *
     * @param arhId
     * @throws IntegracionException
     */
    public void updtPagoMenor(int arhId) throws IntegracionException {
        String sql = String.format(
                "update pagos "
                + "inner join usuarios on usu_id = pag_usu_id "
                + "inner join creditos_final on  cre_usu_id = usu_id "
                + "inner join amortizacion on cre_id = amo_credito "
                + "set amo_pago_id = pagos.pag_id, amo_estatus = 'PAGO MENOR', "
                + "amo_estatus_int = 3 "
                + "where amo_monto_pago > pag_deposito "
                + "and amo_fecha_pago = pagos.pag_fecha "
                + "and amo_pago_id is null "
                + "and amo_monto_pago > 0 "
                + "and amortizacion.amo_estatus_int = 1 "
                + "and pag_estatus = 1 "
                + "and amo_usu_id = pag_usu_id "
                + "and pag_arh_id =%1$s "
                 + "and cre_producto in (6,7) "
                + "and cre_estatus = 1 "
                , arhId);

        super.executeUpdateSql(sql);
    }

    /**
     * Actualiza el estatus del registro en la tabla pagos, se actualiza también
     * el acumulado el cual siempre es obtenido de restarle el monto de la
     * amortizacion al monto del pago
     *
     * @param arhId
     * @param pagEstatus
     * @throws IntegracionException
     */
    public void updtPagosEstatus(int arhId, int pagEstatus) throws IntegracionException {

        String sqlAcumulado = "";
        //Cuando es pago menor todo el monto del pago se va a acumulado
        if (pagEstatus == 3) {
            sqlAcumulado = "pag_acumulado = pag_deposito ";
            //Cuando es pago mayor o exacto se guarda la diferencia del pago - amortizacion
        } else if (pagEstatus == 4 || pagEstatus == 2) {
            sqlAcumulado = "pag_acumulado = (pag_deposito - amo_monto_pago) ";

        }

        String sql = String.format(
                "update pagos inner join amortizacion on pag_id = amo_pago_id "
                + " set pag_credito = amo_credito, pag_estatus = %2$s,"
                + "%3$s "
                + "where pag_arh_id =%1$s "
                + "and pag_estatus = 1 ", arhId, pagEstatus, sqlAcumulado);

        super.executeUpdateSql(sql);
    }
    
    /**
     * Arregla los montos de los acumulados que estan incorrectos
     * @param arhId 
     */
    public void fixAcumPagosMayores(int arhId) throws IntegracionException{
          String sql = String.format(
              "update pagos inner join amortizacion on pag_id = amo_pago_id "
                + "set pag_credito = amo_credito, "
                + "pag_acumulado = (pag_deposito - amo_monto_pago) "
                + "where pag_arh_id =%1$s "
                + "and pag_estatus = 4 ", arhId);

        super.executeUpdateSql(sql);
    }

    /**
     * Actualiza todos los pagos que no tuvieron amortizacion a pag_estatus = 7
     * y pag_acumulado = pag_deposito
     *
     * @param arhId
     * @throws IntegracionException
     */
    public void updtPagEstSinAmortizacion(int arhId) throws IntegracionException {
        String sql = String.format(
                "update pagos set pag_estatus = 7, pag_acumulado = pag_deposito "
                + "where pag_estatus = 1 and pag_arh_id = %1$s ", arhId);

        super.executeUpdateSql(sql);
    }

    /**
     * Obtiene los pagos
     * @param arhId
     * @return
     * @throws IntegracionException 
     */
    public List getPagosRepetidosAmortizacion(int arhId) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select pagos.pag_id from pagos,  "
                    + "(select pag_id, pag_arh_id, count(amo_id) as amortizaciones "
                    + "from amortizacion inner join pagos on pag_id = amo_pago_id "
                    + "where pagos.pag_estatus in (2,3,4) "
                    + "group by pag_id, pag_arh_id) as aux "
                    + "where pagos.pag_id = aux.pag_id "
                    + "and aux.amortizaciones > 1 "
                    + "and pagos.pag_estatus in (2,3,4) "
                    + "and pagos.pag_arh_id =%1$s ",
                    arhId));

            List lstIdPagos = query.list();

            super.endTransaction();
            return lstIdPagos;

        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public List getAmortizacionesCPagRep(int pagId) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select amo_id "
                    + "from amortizacion "
                    + "where amo_pago_id = %1$s ",
                    pagId));

            List lstAmoId = query.list();

          
            return lstAmoId;

        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Actualiza los campos amo_estatus = PENDIENTE y amo_pago_id = null a
     * partir de un amo_id
     *
     * @param pagoId
     * @throws IntegracionException
     */
    public void updtAmortizacionPagoId(int pagoId) throws IntegracionException {
        String sql = String.format(
                "update amortizacion set amo_estatus = 'PENDIENTE', "
                          + "amo_estatus_int = 1, "
                + " amo_pago_id = null where amo_id = %1$s", pagoId);

        super.executeUpdateSql(sql);
    }

    /**
     * Metodo que obtiene los usuarios que tienen mas de un credito de nómina o auto en estatus 1
     * o 2 y que a su vez tienen pagos en la tabla Pagos que se asignaron como
     * mayores a uno de los creditos
     *
     * @param arhId
     * @return
     * @throws IntegracionException
     */
    public List<PagoAmortizacionDto> getUsuEst5y6(int arhId) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select amo_usu_id as amoUsuId,"
                    + "amo_monto_pago as amoMontoPago,"
                    + "pag_usu_id as pagUsuId, pag_deposito as pagDeposito, pag_id as pagId, "
                    + "pag_acumulado as pagAcumulado, pag_fecha as pagFecha "
                    + "from amortizacion inner join pagos on pag_id = amo_pago_id, "
                    + "(select usu_id, count(cre_id) as totalcreditos  "
                    + "from usuarios inner join creditos_final on cre_usu_id = usu_id  "
                    + "where cre_producto in (6,7) "
                    + "and cre_estatus in (1,2) "
                    + "group by usu_id) usucre "
                    + "where amo_usu_id = usucre.usu_id "
                    + "and usucre.totalcreditos > 1 "
                    + "and pag_acumulado >=100 "
                    + "and pag_estatus = 4  "
                    + "and pag_arh_id = %1$s",
                    arhId));

            List<PagoAmortizacionDto> lstPagos = query.setResultTransformer(Transformers.aliasToBean(PagoAmortizacionDto.class)).list();

           
            return lstPagos;

        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Obtiene las amortizaciones pendientes que podrían encajar en el estatus
     * de El pago contiene la suma de n creditos o más de n créditos
     *
     * @param usuId
     * @param diferencia
     * @param fechaPago
     * @return
     * @throws IntegracionException
     */
    public List<AmortizacionDto> getAmortEstatus5y6(int usuId, BigDecimal diferencia, Date fechaPago) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select amo_id as amoId,"
                    + "amo_numero_pago as amoNumeroPago,"
                    + "amo_capital as amoCapital, "
                    + "amo_monto_pago as amoMontoPago, amo_credito as amoCredito,"
                    + "amo_fecha_pago as amoFechaPago, amo_estatus as amoEstatus,"
                    + "amo_pago_id as amoPagoId,amo_usu_id as amoUsuId "
                    + "from amortizacion inner join creditos_final on cre_id = amo_credito "
                    + "where amo_usu_id = %1$s "
                    + "and ((amortizacion.amo_monto_pago<=%2$s) or "
                    + "(amortizacion.amo_monto_pago-%2$s <= 1 and amortizacion.amo_monto_pago-%2$s >= -1)) "
                    + "and amortizacion.amo_fecha_pago = '%3$s' "
                    + "and amo_pago_id is null "
                    + "and amo_monto_pago > 0 "
                    + "and cre_producto in (6,7)",
                    usuId, diferencia, Util.generaFechaFormateada(fechaPago)));

            List<AmortizacionDto> lstAmort = query.setResultTransformer(Transformers.aliasToBean(AmortizacionDto.class)).list();

         
            return lstAmort;

        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    /**
     * Actualizacion de amortizacion para estatus 5 y 6
     *
     * @param pagoId
     * @param amoId
     * @throws IntegracionException
     */
    public void updtAmortizacionPagoIdEst5y6(int pagoId, int amoId) throws IntegracionException {
        String sql = String.format(
                "update amortizacion set amo_estatus = 'PAGADO',amo_estatus_int = 2,"
                + " amo_pago_id = %1$s where amo_id = %2$s", pagoId, amoId);

        super.executeUpdateSql(sql);
    }

    /**
     * Actualizacion de pagos para estatus 5 y 6
     *
     * @param pagoId
     * @param estatus
     * @param acumulado
     * @throws IntegracionException
     */
    public void updtPagoEst5y6(int pagoId, int estatus, BigDecimal acumulado) throws IntegracionException {
        String sql = String.format(
                "update pagos set pag_estatus = %1$s,"
                + " pag_acumulado = %2$s where pag_id = %3$s", estatus, acumulado, pagoId);

        super.executeUpdateSql(sql);
    }
    
    /**
     * Se le pone Estatus 2 a las amortizaciones que tienen pagos
     * en 5 y 6
     * @param arhId
     * @throws IntegracionException 
     */
    public void updtAmortizacionPagadosIn5y6(int arhId) throws IntegracionException {
        String sql = String.format(
               "update amortizacion inner join pagos on pag_id = amo_pago_id "
                    + "set amo_estatus = 'PAGADO', amo_estatus_int = 2 "
                    + "where pag_estatus in (5,6) "
                    + "and pag_arh_id = %1$s", arhId);

        super.executeUpdateSql(sql);
    }
    
    /**
     * Actualiza los creditos que no tienen catorcenas pendientes a un
     * estatus 2
     * @throws IntegracionException 
     */
    public void updtCreditosPagados() throws IntegracionException {
       String sql = 
                    "update creditos_final left join ("
                        + "select cre_id, count(amo_id) as pendientes  "
                        + "from creditos_final left join amortizacion on (cre_id = amo_credito)  "
                        + "where amo_estatus_int = 1 and cre_estatus in (1,2)  "
                        + "and cre_catorcenas is not null  "
                        + "and cre_producto in (6,7) "
                        + "group by cre_id) pendts on pendts.cre_id = creditos_final.cre_id "
                    + "set cre_estatus = 2 "
                    + "where cre_estatus = 1 "
                    + "and cre_catorcenas is not null  "
                    + "and cre_producto in (6,7) "
                    + "and pendts.cre_id is null";

        super.executeUpdateSql(sql);
    }
    
    
    /**
     * Actualiza el estatus del archivo
     * @param arhId
     * @throws IntegracionException 
     */
    public void updtEstatusArchivo(int arhId) throws IntegracionException {
       String sql = String.format(
                    "update archivos_historial set arh_estatus = 2 "
                    + "where arh_id = %1$s", arhId);

        super.executeUpdateSql(sql);
    }
    
    
    public List<ArchivosHistorial> getArchivosPagos() throws IntegracionException{
         try{
                super.beginTransaction();
          
               Query query = session.createQuery("from ArchivosHistorial "
                + "where arhTipoArchivo= 1 "
                       + "order by arhId desc");
                  
                 List<ArchivosHistorial> creditos = query.list();
                 return creditos;
          }catch(HibernateException he){
              LOGGER.error(he.getMessage(), he);
              throw new IntegracionException(he);
          }finally{
            super.endTransaction();
        }
        
    }

}
