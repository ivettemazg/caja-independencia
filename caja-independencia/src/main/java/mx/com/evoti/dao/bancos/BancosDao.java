/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.bancos;

import mx.com.evoti.dto.bancos.ReporteBancosDto;
import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.AvalesSolicitudDao;
import mx.com.evoti.dao.ManagerDB;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.bancos.BancosDto;
import mx.com.evoti.dto.bancos.DesctosXCobrarCredsDto;
import mx.com.evoti.dto.bancos.EstadoCuentaDto;
import mx.com.evoti.dto.bancos.dao.BancosEstadoCtaDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.BancoEdocta;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.BancosConceptos;
import mx.com.evoti.hibernate.pojos.EstadoCuenta;
import mx.com.evoti.util.Util;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Venette
 */
public class BancosDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 7574553879515724967L;

    /**
     * Obtiene los descuentos por cobrar (los pagos que se hicieron en
     * finiquitos)
     *
     * @return
     * @throws IntegracionException
     */
    public List<DesctosXCobrarCredsDto> getDesctosXCobrarCreds() throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery("select cre_clave as creClave, "
                    + "usu_clave_empleado as usuClaveEmpleado, empresas.emp_abreviacion as empAbreviacion, "
                    + "pagos.pag_deposito as pagDeposito, pagos.pag_fecha as pagFecha,"
                    + "cre_id as creId, usu_id as usuId, pag_id as pagId, emp_id as empId, "
                    + "concat(usu_nombre,' ',usu_paterno,' ',usu_materno ) as nombreCompleto,"
                    + "ban_id as banId "
                    + "from creditos_final inner join pagos on pag_credito = cre_id "
                    + "inner join usuarios on usu_id = cre_usu_id "
                    + "inner join empresas on empresas.emp_id = usuarios.usu_empresa "
                    + "inner join bancos on ban_id_concepto_sistema = pag_id "
                    + "where pag_estatus = 12 " //Que el tipo de pago sea finiquito
                    + "and ban_concepto = 13 " //Que el concepto de banco sea DESCUENTO X COBRAR CREDITO
                    + "and ban_ajustado = 0 " //Que no esté ajustado aún
                    + "");

            List<DesctosXCobrarCredsDto> creds = query.setResultTransformer(Transformers.aliasToBean(DesctosXCobrarCredsDto.class)).list();

            return creds;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    public void saveBanco(Bancos banco) throws IntegracionException {
        super.savePojo(banco);
    }

    public void saveEstadoCuenta(EstadoCuenta edoCta) throws IntegracionException {
        super.savePojo(edoCta);
    }

    /**
     * Actualiza un registro de bancos y obtiene ese mismo registro
     *
     * @param empresa
     * @param fechaDeposito
     * @param solId
     * @param idRelacion
     * @return
     * @throws IntegracionException
     */
    public BancosDto updateBancoDepositoCredito(Integer empresa, Date fechaDeposito, BigInteger solId, Long idRelacion) throws IntegracionException {

        try {
            String sql = String.format(""
                    + "update bancos inner join creditos_final on creditos_final.cre_id = bancos.ban_id_concepto_sistema "
                    + "SET ban_empresa = %1$s, ban_fechatransaccion = '%2$s' ,ban_ajustado = 1 "
                    + "WHERE creditos_final.cre_solicitud = %3$s "
                    + "and ban_concepto = 12 "
                    + "and ban_id_relacion = %4$s "
                    + "and ban_fecha_relacion = '%2$s' "
                    + "", empresa, Util.generaFechaFormateada(fechaDeposito), solId, idRelacion);

            super.executeUpdateSql(sql);

            super.beginTransaction();

            Query sqlQry = session.createQuery(
                    String.format("select ban.banId as banId, ban.banConcepto as banConcepto, ban.banIdConceptoSistema as banIdConceptoSistema,"
                            + "ban.banAjustado as banAjustado, ban.banMonto as banMonto, ban.banEmpresa as banEmpresa, ban.banFechatransaccion as banFechatransaccion "
                            + "from Bancos ban, CreditosFinal cre "
                            + "where cre.creId = ban.banIdConceptoSistema "
                            + "and cre.creSolicitud = %1$s "
                            + "and ban.banConcepto = 12 ", solId));

            BancosDto banco = (BancosDto) sqlQry.setResultTransformer(Transformers.aliasToBean(BancosDto.class)).uniqueResult();

            return banco;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene los registros de bancos y estado de cuenta que ya están ajustados
     *
     * @param fechaInicio
     * @param fechaFin
     * @return
     * @throws IntegracionException
     */
    public List<BancosEstadoCtaDto> getBancoEdoCtaAjustados(Date fechaInicio, Date fechaFin) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select bancos.ban_id as banId, bancos.ban_concepto as banConcepto, bcb.cban_nombre as conceptoBanco, "
                            + "IFNULL(bancos.ban_monto,0) as banMonto, bancos.ban_empresa as banEmpresa, bemp.emp_abreviacion as banEmpresaStri, "
                            + "bancos.ban_fechatransaccion as banFechatransaccion, bancos.ban_id_concepto_sistema as banIdConceptoSistema, "
                            + "bancos.ban_ajustado as banAjustado, estado_cuenta.ec_descripcion as ecDescripcion, "
                            + "estado_cuenta.ec_ajustado as ecAjustado, estado_cuenta.ec_fechatransaccion as ecFechatransaccion, "
                            + "estado_cuenta.ec_empresa as ecEmpresa, ecemp.emp_abreviacion as edoCtaEmpresa, "
                            + "IFNULL(estado_cuenta.ec_monto,0) as ecMonto, estado_cuenta.ec_concepto as ecConcepto, bcec.cban_nombre as conceptoEdoCta, "
                            + "estado_cuenta.ec_id as ecId, banco_edocta.bec_fecha_transaccion as becFechaTransaccion "
                            + "from bancos inner join banco_edocta on bancos.ban_id = banco_edocta.bec_id_banco "
                            + "inner join bancos_conceptos bcb on bancos.ban_concepto = bcb.cban_id "
                            + "inner join empresas bemp on bancos.ban_empresa = bemp.emp_id "
                            + "inner join estado_cuenta on banco_edocta.bec_id_edocta = estado_cuenta.ec_id "
                            + "left join empresas ecemp on ecemp.emp_id = estado_cuenta.ec_empresa "
                            + "inner join bancos_conceptos bcec on bcec.cban_id = estado_cuenta.ec_concepto "
                            + "where estado_cuenta.ec_fechatransaccion between '%1$s' and '%2$s' "
                            + "", Util.generaFechaFormateada(fechaInicio), Util.generaFechaFormateada(fechaFin)));

            List<BancosEstadoCtaDto> creds = query.setResultTransformer(Transformers.aliasToBean(BancosEstadoCtaDto.class)).list();

            return creds;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene los registros de bancos pendientes de ajuste
     *
     * @param fechaInicio
     * @param fechaFin
     * @return
     * @throws IntegracionException
     */
    public List<BancosDto> getBancoPendts(Date fechaInicio, Date fechaFin) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select bancos.ban_id as banId, bancos.ban_concepto as banConcepto, bcb.cban_nombre as concepto, " +
"                            IFNULL(bancos.ban_monto,0) as banMonto, bancos.ban_empresa as banEmpresa, bemp.emp_abreviacion as banEmpresaStri, " +
"                            bancos.ban_fechatransaccion as banFechatransaccion, bancos.ban_id_concepto_sistema as banIdConceptoSistema, " +
"                            bancos.ban_ajustado as banAjustado, 0 as rbeId, 0 as idPantalla," +
"                            (" +
"                              case " +
"                                when ban_Concepto in (1, 2, 3) then arh_nombre_archivo" +
"                                  when ban_Concepto in (4, 5, 6, 13) then usuarioPago.usu_clave_empleado" +
"                                      when ban_Concepto in (7,8,9,10,11,14,15) then usuarioMov.usu_clave_empleado" +
"                                          else usuarioCred.usu_clave_empleado" +
"                              end" +
"                            ) as claveNombre" +
"                            from bancos inner join bancos_conceptos bcb on bancos.ban_concepto = bcb.cban_id " +
"                            left join archivos_historial on arh_id = ban_id_concepto_sistema " +
"                            left join pagos on pag_id = ban_id_concepto_sistema " +
"                            left join usuarios usuarioPago on usuarioPago.usu_id = pag_usu_id " +
"                            left join movimientos on mov_id = ban_id_concepto_sistema" +
"                            left join usuarios usuarioMov on usuarioMov.usu_id = mov_usu_id" +
"                            left join creditos_final on cre_id = ban_id_concepto_sistema" +
"                            left join usuarios usuarioCred on usuarioCred.usu_id = cre_usu_id" +
"                            inner join empresas bemp on bancos.ban_empresa = bemp.emp_id "
                            + "where bancos.ban_fechatransaccion between '%1$s' and '%2$s' "
                            + "and bancos.ban_ajustado = 0  "
                            + "", Util.generaFechaFormateada(fechaInicio), Util.generaFechaFormateada(fechaFin)));

            List<BancosDto> creds = query.setResultTransformer(Transformers.aliasToBean(BancosDto.class)).list();

            return creds;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    
    public List<BancosDto> getBancoAjustados(Date fechaInicio, Date fechaFin) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select ban_ajustado as banAjustado, ban_concepto as banConcepto, " +
                            "  cban_nombre as concepto, ban_empresa as banEmpresa,  " +
                            "  ban_fechatransaccion as banFechatransaccion, ban_id as banId, " +
                            "  ban_id_concepto_sistema as banIdConceptoSistema, " +
                            "  IFNULL(bancos.ban_monto,0) as banMonto, emp_abreviacion as banEmpresaStri, " +
                            "  ban_id_relacion as rbeId, ban_fecha_relacion as rbeFechaRel," +
                            "  (" +
                            "  case  " +
                            "    when ban_Concepto in (1, 2, 3) then arh_nombre_archivo " +
                            "      when ban_Concepto in (4, 5, 6, 13) then usuarioPago.usu_clave_empleado " +
                            "          when ban_Concepto in (7,8,9,10,11,14,15) then usuarioMov.usu_clave_empleado " +
                            "              else usuarioCred.usu_clave_empleado " +
                            "  end " +
                            " ) as claveNombre " +
                            "  from bancos " +
                            "  inner join bancos_conceptos on ban_concepto = cban_id " +
                            "  left join archivos_historial on arh_id = ban_id_concepto_sistema" +
                            "  left join pagos on pag_id = ban_id_concepto_sistema" +
                            "  left join usuarios usuarioPago on usuarioPago.usu_id = pag_usu_id" +
                            "  left join movimientos on mov_id = ban_id_concepto_sistema" +
                            "  left join usuarios usuarioMov on usuarioMov.usu_id = mov_usu_id" +
                            "  left join creditos_final on cre_id = ban_id_concepto_sistema" +
                            "  left join usuarios usuarioCred on usuarioCred.usu_id = cre_usu_id" +
                            "  inner join empresas on ban_empresa = emp_id "
                            + "where ban_fechatransaccion between '%1$s' and '%2$s'  "
                            + "and ban_ajustado > 0", Util.generaFechaFormateada(fechaInicio), Util.generaFechaFormateada(fechaFin)));

            List<BancosDto> banDto = query.setResultTransformer(Transformers.aliasToBean(BancosDto.class)).list();

            return banDto;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene los registros de estado de cuenta pendientes de ajuste
     *
     * @param fechaInicio
     * @param fechaFin
     * @return
     * @throws IntegracionException
     */
    public List<EstadoCuentaDto> getEdoCtaPendts(Date fechaInicio, Date fechaFin) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select estado_cuenta.ec_descripcion as ecDescripcion, "
                            + "estado_cuenta.ec_ajustado as ecAjustado,  "
                            + "estado_cuenta.ec_fechatransaccion as ecFechatransaccion, estado_cuenta.ec_empresa as ecEmpresa, "
                            + "ecemp.emp_abreviacion as edoCtaEmpresa, "
                            + "IFNULL(estado_cuenta.ec_monto,0) as ecMonto, estado_cuenta.ec_concepto as ecConcepto, bcec.cban_nombre as strConcepto, "
                            + "estado_cuenta.ec_id as ecId, ec_padre as ecPadre, 0 as rbeId, 0 as idPantalla "
                            + "from estado_cuenta left join empresas ecemp on ecemp.emp_id = estado_cuenta.ec_empresa "
                            + "inner join bancos_conceptos bcec on bcec.cban_id = estado_cuenta.ec_concepto "
                            + "where estado_cuenta.ec_fechatransaccion between '%1$s' and '%2$s' "
                            + "and estado_cuenta.ec_ajustado =0  "
                            + "", Util.generaFechaFormateada(fechaInicio), Util.generaFechaFormateada(fechaFin)));

            List<EstadoCuentaDto> creds = query.setResultTransformer(Transformers.aliasToBean(EstadoCuentaDto.class)).list();

            return creds;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    public List<EstadoCuentaDto> getEdoCtaAjustados(String rbeIds) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select ec_id as ecId, estado_cuenta.ec_concepto as ecConcepto, "
                            + "IFNULL(estado_cuenta.ec_monto,0) as ecMonto, ec_ajustado as ecAjustado, "
                            + "ec_empresa as ecEmpresa, ec_padre as ecPadre, ec_descripcion as ecDescripcion, "
                            + "estado_cuenta.ec_fechatransaccion as ecFechatransaccion, cban_nombre as strConcepto, "
                            + "empresas.emp_abreviacion as edoCtaEmpresa, bancos_conceptos.cban_id as banConceptoId, "
                            + "ec_id_relacion as rbeId, ec_fecha_relacion as rbeFechaRel "
                            + "from estado_cuenta "
                            + "inner join bancos_conceptos on ec_concepto = cban_id "
                            + "left join empresas on ec_empresa = emp_id "
                            + "where ec_id_relacion in (%1$s)  "
                            + "", rbeIds));

            List<EstadoCuentaDto> ecs = query.setResultTransformer(Transformers.aliasToBean(EstadoCuentaDto.class)).list();

            return ecs;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene la lista de conceptos de bancos de la base de datos
     *
     * @throws mx.com.evoti.dao.exception.IntegracionException
     * @return
     */
    public List<BancosConceptos> getConceptos() throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    "from BancosConceptos ");

            List<BancosConceptos> parentescos = query.list();

            return parentescos;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } catch (Exception ex) {
            throw new IntegracionException(ex);
        } finally {
            super.endTransaction();
        }
    }

    public void updtEstadoCta(EstadoCuenta edoCta) throws IntegracionException {
        super.updatePojo(edoCta);
    }

    public void updtBanco(Bancos banco) throws IntegracionException {
        super.updatePojo(banco);
    }
    
    

    public void updtEstatusBanco(Integer banId, Integer ajustado) throws IntegracionException {
        String sql = String.format(
                "update bancos set ban_ajustado = %2$s where ban_id = %1$s ", banId, ajustado);

        super.executeUpdateSql(sql);
    }
    
    public void updtBancoRelacion(Long rbcId, Integer banId) throws IntegracionException {
        String sql = String.format(
                "update bancos " +
                "set ban_id_relacion = %1$s, ban_fecha_relacion = '%3$s' " +
                "where ban_id = %2$s " ,rbcId, banId, Util.generaFechaFormateada(new Date()));

        super.executeUpdateSql(sql);
    }
    
    
    public void updtAjusteBanco(Long idRelacion, Integer ajustado) throws IntegracionException {
        String sql = String.format(
                "update bancos set ban_ajustado = %2$s where ban_id_relacion = %1$s ", idRelacion, ajustado);

        super.executeUpdateSql(sql);
    }
    
    public void updtECRelacion(Long rbcId, Integer ecId) throws IntegracionException {
        String sql = String.format(
                "update estado_cuenta " +
                "set ec_id_relacion = %1$s, ec_fecha_relacion = '%3$s' " +
                "where ec_id = %2$s "
                ,rbcId, ecId, Util.generaFechaFormateada(new Date()));

        super.executeUpdateSql(sql);
    }

    public void updtEstatusEC(Integer ecId, Integer ajustado) throws IntegracionException {
        String sql = String.format(
                "update estado_cuenta set ec_ajustado = %2$s where ec_id = %1$s ", ecId, ajustado);

        super.executeUpdateSql(sql);
    }
    
    public void updtAjusteEdoCta(Long idRelacion, Integer ajustado) throws IntegracionException {
        String sql = String.format(
                "update estado_cuenta set ec_ajustado = %2$s where ec_id_relacion = %1$s ", idRelacion, ajustado);

        super.executeUpdateSql(sql);
    }

   

    public void eliminaEC(EstadoCuentaDto ec) throws IntegracionException {
        String sql = String.format(
                "delete from estado_cuenta where ec_id = %1$s ", ec.getEcId());

        super.executeUpdateSql(sql);

    }
    
     public void quitarAjusteBanco(BigInteger idRelacion) throws IntegracionException {
           String sql = String.format(
                "update bancos set ban_ajustado = 0, ban_id_relacion = 0, ban_fecha_relacion = null "
                        + "where ban_id_relacion = %1$s ", idRelacion);

        super.executeUpdateSql(sql);

    }

    public void quitarAjusteEc(BigInteger idRelacion) throws IntegracionException {
        String sql = String.format(
                "update estado_cuenta set ec_ajustado = 0, ec_id_relacion = 0, ec_fecha_relacion = null "
                        + "where ec_id_relacion = %1$s ", idRelacion);

        super.executeUpdateSql(sql);

    }
    

    /**
     * Obtiene el reporte de bancos
     *
     * @param fechaInicio
     * @param fechaFin
     * @return
     * @throws IntegracionException
     */
    public List<ReporteBancosDto> getReporteBancos(Date fechaInicio, Date fechaFin) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select ec_id as ecId, empresas.emp_abreviacion as empAbreviacion, bancos_conceptos.cban_nombre as conceptoStr, "
                            + "estado_cuenta.ec_descripcion as ecDescripcion, estado_cuenta.ec_fechatransaccion as ecFechaConcepto, "
                            + "ec_monto as ecMonto, estado_cuenta.ec_ajustado as ajustado, IFNULL((ec_monto-sum(bancos.ban_monto)),0) as saldoPendienteAjuste "
                            + "from estado_cuenta left join banco_edocta on estado_cuenta.ec_id = banco_edocta.bec_id_edocta "
                            + "left join bancos on banco_edocta.bec_id_banco = bancos.ban_id "
                            + "left join empresas on bancos.ban_empresa = empresas.emp_id "
                            + "left join bancos_conceptos on estado_cuenta.ec_concepto = bancos_conceptos.cban_id "
                            + "where estado_cuenta.ec_fechatransaccion between '%1$s' and '%2$s' "
                            + "group by ec_id, empresas.emp_abreviacion, bancos_conceptos.cban_nombre, estado_cuenta.ec_descripcion, "
                            + "ec_monto, estado_cuenta.ec_ajustado "
                            + "order by estado_cuenta.ec_fechatransaccion asc "
                            + "", Util.generaFechaFormateada(fechaInicio), Util.generaFechaFormateada(fechaFin)));

            List<ReporteBancosDto> creds = query.setResultTransformer(Transformers.aliasToBean(ReporteBancosDto.class)).list();

            return creds;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    public static void main(String args[]) {
        try {
            BancosDao dao = new BancosDao();
            System.setProperty("Log4J.configuration", new File(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "Log4J.properties").toURI().toURL().toString());
            HibernateUtil.buildSessionFactory2();
            List<ReporteBancosDto> lista = new ArrayList<>();

            lista = dao.getReporteBancos(Util.generaFechaDeString("2017-04-01"), Util.generaFechaDeString("2017-04-30"));

            lista.stream().forEach(l -> {

                System.out.println(l.getAjustado() + ", " + l.getConceptoStr() + ", " + l.getEmpAbreviacion() + ", " + l.getEstatus());

            });

            HibernateUtil.closeSessionFactory();

        } catch (MalformedURLException ex) {
            Logger.getLogger(AvalesSolicitudDao.class.getName()).log(Level.SEVERE, null, ex);

        } catch (IntegracionException ex) {
            Logger.getLogger(BancosDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   

}
