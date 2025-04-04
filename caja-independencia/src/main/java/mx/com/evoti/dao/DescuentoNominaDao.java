/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.ManagerDB;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.DescuentoNominaDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.util.Util;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author NeOleon
 */
public class DescuentoNominaDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 7574553879515724967L;

    public List<DescuentoNominaDto> extraeDescuentoNominaEmpresasOtras(Date fechaCatorcena, Integer empresa) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(
                String.format("select e.emp_abreviacion as empresa,cre_fecha_deposito as FechaDeposito,cre_prestamo as prestamo,"
                        + "cre_catorcenas as catorcenas,cre_pago_quincenal*cre_catorcenas as PagoTotal,"
                        + "cre_fecha_primer_pago as fechaPrimerPago,concat(u.usu_clave_empleado, '') as claveEmpleado,"
                        + "p.pro_descripcion as producto,cre_clave as claveCredito,"
                        + "concat_ws(' ',usu_paterno,usu_materno,usu_nombre) as nombre,cre_pago_quincenal as pagoQuincenal,"
                        + "'NUEVO' as tipoDescuento "
                        + "from creditos_final c left join usuarios u on c.cre_usu_id=u.usu_id "
                        + "left join empresas e on u.usu_empresa=e.emp_id "
                        + "left join productos p on c.cre_producto=p.pro_id "
                        + "where cre_producto in (6,7) and cre_fecha_primer_pago='%1$s' and u.usu_empresa=%2$s "
                        + "union "
                        + "select e.emp_abreviacion as empresa,cre_fecha_deposito as FechaDeposito,cre_prestamo as prestamo,"
                        + "cre_catorcenas as catorcenas,cre_pago_quincenal*cre_catorcenas as PagoTotal,"
                        + "cre_fecha_primer_pago as fechaPrimerPago,concat(u.usu_clave_empleado, '') as claveEmpleado,"
                        + "p.pro_descripcion as producto,cre_clave as claveCredito,"
                        + "concat_ws(' ',usu_paterno,usu_materno,usu_nombre) as nombre,cre_pago_quincenal as pagoQuincenal,"
                        + "'ACTUALIZACION' as tipoDescuento "
                        + "from creditos_final c left join usuarios u on c.cre_usu_id=u.usu_id "
                        + "left join empresas e on u.usu_empresa=e.emp_id "
                        + "left join productos p on c.cre_producto=p.pro_id "
                        + "where cre_producto in (6,7) and cre_fecha_nuevo_monto='%1$s' and u.usu_empresa=%2$s "
                        + "", Util.generaFechaFormateada(fechaCatorcena), empresa));

        List<DescuentoNominaDto> creeds = query.setResultTransformer(Transformers.aliasToBean(DescuentoNominaDto.class)).list();

        super.endTransaction();
        return creeds;

    }

    public List<DescuentoNominaDto> extraeDescuentoNominaEmpresas(Date fechaCatorcena, Integer empresa) throws IntegracionException {

        super.beginTransaction();
                
        String empresaNumero ="";
                
        switch (empresa){
                case 1:
                    empresaNumero = "02";
                    break;
                    
                case 3:
                    empresaNumero = "08";
                    break;
                    
                case 4:
                    empresaNumero = "09";
                    break;
        }

        SQLQuery query = session.createSQLQuery(
                String.format("select '%3$s' as claveEmpresa, LPAD(u.usu_clave_empleado,6,'0') as claveEmpleado, "
                        + "concat(LPAD(day(cre_fecha_primer_pago),2,'0'), LPAD(month(cre_fecha_primer_pago),2,'0'), substr(cast(cre_fecha_primer_pago as char(10)),3,2)) as fechaAMX, "
                        + "'D0410_PRES CAJA AHO SIND' as codigoAMX, cre_catorcenas as catorcenas, round(sum(a.amo_monto_pago), 2) as pagoTotal "
                        + "from creditos_final c left join usuarios u on c.cre_usu_id=u.usu_id "
                        + "left join amortizacion a on c.cre_id=a.amo_credito "
                        + "where cre_producto in (6,7) "
                        + "and cre_fecha_primer_pago='%1$s' and u.usu_empresa=%2$s "
                        + "group by '%3$s', LPAD(u.usu_clave_empleado,6,'0'), "
                        + "concat(LPAD(day(cre_fecha_primer_pago),2,'0'), LPAD(month(cre_fecha_primer_pago),2,'0'), substr(cast(cre_fecha_primer_pago as char(10)),3,2)), "
                        + "'D0410_PRES CAJA AHO SIND', cre_catorcenas"
                        + "", Util.generaFechaFormateada(fechaCatorcena), empresa, empresaNumero));

        List<DescuentoNominaDto> creeds = query.setResultTransformer(Transformers.aliasToBean(DescuentoNominaDto.class)).list();

        super.endTransaction();
        return creeds;

    }

    public List<DescuentoNominaDto> extraeDescuentoNominaEmpresasFAAG(Date fechaCatorcena) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(
                String.format("select cre_fecha_deposito as fechaDeposito,cre_prestamo as prestamo,cre_fecha_primer_pago as fechaPrimerPago,p.pro_siglas as producto,cre_clave as claveCredito,e.cre_est_nombre as statusCredito, concat(u.usu_clave_empleado, '') as claveEmpleado,concat_ws(' ',usu_nombre,usu_paterno,usu_materno) as nombre,m.emp_abreviacion as empresa,sum(a.amo_interes) as interes,sum(a.amo_monto_pago) as pagoTotal\n" +
"from creditos_final c left join productos p on c.cre_producto=p.pro_id\n" +
"left join credito_estatus e on c.cre_estatus=e.cre_est_id\n" +
"left join usuarios u on c.cre_usu_id=u.usu_id\n" +
"left join empresas m on u.usu_empresa=m.emp_id\n" +
"left join amortizacion a on c.cre_id=a.amo_credito\n" +
"where p.pro_id in (4,5) and cre_fecha_primer_pago='%1$s' and a.amo_estatus_int=1 and c.cre_estatus=1\n" +
"group by cre_fecha_deposito,cre_prestamo,cre_fecha_primer_pago,p.pro_siglas,cre_clave,e.cre_est_nombre,u.usu_clave_empleado,concat_ws(' ',usu_nombre,usu_paterno,usu_materno),m.emp_abreviacion\n" +
"order by cre_id "
                        + "", Util.generaFechaFormateada(fechaCatorcena)));

        List<DescuentoNominaDto> creeds = query.setResultTransformer(Transformers.aliasToBean(DescuentoNominaDto.class)).list();
        super.endTransaction();
        return creeds;

    }

    public static void main(String args[]) {
        try {
            System.setProperty("Log4J.configuration", new File(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "Log4J.properties").toURI().toURL().toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(DescuentoNominaDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        HibernateUtil.buildSessionFactory2();

        DescuentoNominaDao dao = new DescuentoNominaDao();

        try {
            System.out.println(dao.extraeDescuentoNominaEmpresas(Util.generaFechaDeString("2016-01-07"), 1).size());
            System.out.println(dao.extraeDescuentoNominaEmpresasOtras(Util.generaFechaDeString("2016-01-07"), 2).size());
            System.out.println(dao.extraeDescuentoNominaEmpresasFAAG(Util.generaFechaDeString("2016-12-22")).size());
        } catch (IntegracionException ex) {
            Logger.getLogger(DescuentoNominaDao.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
