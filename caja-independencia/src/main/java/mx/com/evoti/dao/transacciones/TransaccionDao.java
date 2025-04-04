/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.transacciones;

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
import mx.com.evoti.dto.TransaccionUsuarioDto;
import mx.com.evoti.dto.bancos.BancosDto;
import mx.com.evoti.dto.bancos.DesctosXCobrarCredsDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.RegistroTransaccion;
import mx.com.evoti.util.Util;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Venette
 */
public class TransaccionDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 7574553879515724967L;

    /**
     * Obtiene el reporte de bitacora de transacciones
     *
     * @return
     * @throws IntegracionException
     */
    public List<TransaccionUsuarioDto> getTransacciones() throws IntegracionException {
        try {
            super.beginTransaction();
            session.getTransaction();
            
            SQLQuery query = session.createSQLQuery("select rt.tran_id as tranId, rt.tran_id_usuario as tranIdUsuario, u.usu_clave_empleado as tranClaveUsuario, rt.tran_fecha as tranFecha, \n" +
            "rt.tran_tipo_tran as tranTipoTran, tt.tipo_tran_descripcion as ttranDescripcion,\n" +
            " rt.tran_id_sistema as tranIdSistema, m.mov_deposito as monto, m.mov_clave_empleado as userOfTran, e.emp_abreviacion as empresaUserOfTran\n" +
            " from registro_transaccion rt    \n" +
            " inner join tipo_transaccion tt on rt.tran_tipo_tran = tt.tipo_tran_id   \n" +
            " inner join movimientos m on m.mov_id = rt.tran_id_sistema   \n" +
            " inner join usuarios u on u.usu_id = rt.tran_id_usuario   \n" +
            " left join empresas e on e.emp_id = m.mov_empresa\n" +
            " where tipo_tran_tabla = 'MOVIMIENTOS'   \n" +
            " union all   \n" +
            " select rt.tran_id as tranId, rt.tran_id_usuario as tranIdUsuario, u.usu_clave_empleado as tranClaveUsuario, rt.tran_fecha as tranFecha, \n" +
            " rt.tran_tipo_tran as tranTipoTran, tt.tipo_tran_descripcion as ttranDescripcion,   \n" +
            " rt.tran_id_sistema as tranIdSistema, p.pag_deposito as monto, p.pag_clave_empleado as userOfTran, e.emp_abreviacion as empresaUserOfTran\n" +
            " from registro_transaccion rt    \n" +
            " inner join tipo_transaccion tt on rt.tran_tipo_tran = tt.tipo_tran_id   \n" +
            " inner join pagos p on p.pag_id = rt.tran_id_sistema   \n" +
            " inner join usuarios u on u.usu_id = rt.tran_id_usuario \n" +
            "  left join empresas e on e.emp_id = p.pag_empresa\n" +
            " where tipo_tran_tabla = 'PAGOS' \n" +
            "  ");

            List<TransaccionUsuarioDto> transacciones = query.setResultTransformer(Transformers.aliasToBean(TransaccionUsuarioDto.class)).list();

            return transacciones;
        } catch (IntegracionException ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IntegracionException(ex.getMessage(), ex);
        } finally {
            super.endTransaction();
        }
    }

    public void saveTransaccion(RegistroTransaccion tran) throws IntegracionException {
        super.savePojo(tran);
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

  
    public void updtBanco(Bancos banco) throws IntegracionException {
        super.updatePojo(banco);
    }
    
    

    public void updtEstatusBanco(Integer banId, Integer ajustado) throws IntegracionException {
        String sql = String.format(
                "update bancos set ban_ajustado = %2$s where ban_id = %1$s ", banId, ajustado);

        super.executeUpdateSql(sql);
    }
    
   

    public static void main(String args[]) {
        try {
            TransaccionDao dao = new TransaccionDao();
            System.setProperty("Log4J.configuration", new File(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "Log4J.properties").toURI().toURL().toString());
            HibernateUtil.buildSessionFactory2();
            List<ReporteBancosDto> lista = new ArrayList<>();

//            lista = dao.getReporteBancos(Util.generaFechaDeString("2017-04-01"), Util.generaFechaDeString("2017-04-30"));
//
//            lista.stream().forEach(l -> {
//
//                System.out.println(l.getAjustado() + ", " + l.getConceptoStr() + ", " + l.getEmpAbreviacion() + ", " + l.getEstatus());
//
//            });
//
//            HibernateUtil.closeSessionFactory();
//
        } catch (MalformedURLException ex) {
            Logger.getLogger(AvalesSolicitudDao.class.getName()).log(Level.SEVERE, null, ex);
//
//        } catch (IntegracionException ex) {
//            Logger.getLogger(TransaccionDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   

}
