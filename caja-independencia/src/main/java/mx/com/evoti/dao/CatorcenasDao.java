/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CatorcenaDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.util.Util;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class CatorcenasDao extends ManagerDB implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CatorcenasDao.class);
    private static final long serialVersionUID = -137610328094261875L;

    public Date obtieneCatorcenaExacta(Date fecha) throws IntegracionException {
        Date catorcena;

        super.beginTransaction();

        String sql = String.format("select carFecha "
                + "from Catorcenas where carFecha = '%1$s' ", Util.generaFechaFormateada(fecha));

        Query query = session.createQuery(sql);

        catorcena = (Date) query.uniqueResult();

        super.endTransaction();

        return catorcena;

    }

    /**
     * Obtiene la catorcena anterior más próxima a la fecha dada
     *
     * @param fecha
     * @return
     * @throws IntegracionException
     */
    public Date getCatInmediataAnterior(Date fecha) throws IntegracionException {
        Date catorcena;
        super.beginTransaction();

        String sql = String.format(
                "select carFecha from catorcenas "
                + "where car_fecha < '%1$s' order by car_fecha desc "
                + "limit 1 ", Util.generaFechaFormateada(fecha));

        Query query = session.createQuery(sql);

        catorcena = (Date) query.uniqueResult();

        super.endTransaction();

        return catorcena;
    }

    /**
     * Obtiene la catorcena siguiente más próxima a la fecha dada
     *
     * @param fecha
     * @return
     * @throws IntegracionException
     */
    public Date getCatInmediataSiguiente(Date fecha) throws IntegracionException {
        Date catorcena;
        super.beginTransaction();

        String sql = String.format(
                "select car_fecha from catorcenas "
                + "where car_fecha > '%1$s' order by car_fecha asc "
                + "limit 1 ", Util.generaFechaFormateada(fecha));

        SQLQuery query = session.createSQLQuery(sql);

        catorcena = (Date) query.uniqueResult();

        super.endTransaction();

        return catorcena;
    }

    /**
     * Obtiene todas las catorcenas después de la fecha dada
     *
     * @param fecha
     * @return
     * @throws IntegracionException
     */
    public List<String> getCatorcenasSiguientes(Date fecha) throws IntegracionException {
        List<String> catorcenas;
        super.beginTransaction();

        String sqlFirst = "select date_format(car_fecha, '%d/%m/%Y') from catorcenas ";

        String sql = String.format(
                "where car_fecha > '%1$s' order by car_fecha asc "
                + "limit 6 ", Util.generaFechaFormateada(fecha));

        sql = sqlFirst + sql;

        SQLQuery query = session.createSQLQuery(sql);

        catorcenas = query.list();

        super.endTransaction();

        return catorcenas;
    }

    public CatorcenaDto getPrimerCatorcenaMesAno(int year, int month) throws IntegracionException {

        super.beginTransaction();
        SQLQuery query = session.createSQLQuery(String.format("select car_fecha as carFecha "
                + "from catorcenas WHERE year(catorcenas.car_fecha) = %1$s and month(catorcenas.car_fecha)=%2$s "
                + "order by car_fecha asc limit 1", year, month));

        CatorcenaDto catorcenas = (CatorcenaDto) query.setResultTransformer(Transformers.aliasToBean(CatorcenaDto.class)).uniqueResult();

        super.endTransaction();
        return catorcenas;

    }

    public static void main(String args[]) {
        try {
            System.setProperty("Log4J.configuration", new File(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "Log4J.properties").toURI().toURL().toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(mx.com.evoti.dao.DescuentoNominaDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        HibernateUtil.buildSessionFactory2();

        CatorcenasDao catDao = new CatorcenasDao();

        try {

            Date date = new Date(); // your date
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            CatorcenaDto dto = catDao.getPrimerCatorcenaMesAno(year, month);

            System.out.println(Util.generaFechaFormateada(dto.getCarFecha()));
        } catch (IntegracionException ex) {
            Logger.getLogger(CatorcenasDao.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
