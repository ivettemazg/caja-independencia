/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.impl.SolicitudUpdtDaoImpl;
import mx.com.evoti.dto.CatorcenaDto;
import mx.com.evoti.dto.ImagenesDto;
import mx.com.evoti.dto.SolicitudDto;
import mx.com.evoti.dto.usuariocomun.DocumentosDto;
import mx.com.evoti.dto.usuariocomun.SolicitudCreditoDto;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.hibernate.pojos.SolicitudAvales;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.util.Constantes;
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
public class SolicitudCreditoDao extends ManagerDB implements Serializable, SolicitudUpdtDaoImpl {

    private static final long serialVersionUID = 516517538449528910L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SolicitudCreditoDao.class);

    /**
     * Obtiene las solicitudes
     *
     * @param idEmpleado
     * @return
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public List<SolicitudCreditoDto> consultaSolicitudesPndtsXEmpl(int idEmpleado) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select sol_id as solId,"
                    + "sol_clave_empleado as solClaveEmpleado, sol_pago_credito as solPagoCredito, "
                    + "sol_monto_solicitado as solMontoSolicitado,sol_catorcenas as solCatorcenas,"
                    + "concat(u.usu_nombre,' ',u.usu_paterno,' ',u.usu_materno) as nombreEmpleado,"
                    + "sol_estatus as solEstatus, sol_pago_total as solPagoTotal, "
                    + "s.sol_fecha_creacion as solFechaCreacion,"
                    + "sol_usu_id as solUsuId "
                    + "from solicitudes s left join usuarios u on s.sol_usu_id=u.usu_id "
                    + "where s.sol_estatus not in (6,7) "
                    + "and s.sol_usu_id= %1$s order by s.sol_id asc",
                    idEmpleado));

            List<SolicitudCreditoDto> solsPndts = query.setResultTransformer(Transformers.aliasToBean(SolicitudCreditoDto.class)).list();

            return solsPndts;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

     /**
     * Obtiene las solicitudes
     *
     * @param idEmpleado
     * @return
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public List<SolicitudCreditoDto> consultaSolicitudesIncompletasXEmpl(int idEmpleado) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select sol_id as solId,"
                    + "sol_clave_empleado as solClaveEmpleado, sol_pago_credito as solPagoCredito, "
                    + "sol_monto_solicitado as solMontoSolicitado, sol_catorcenas as solCatorcenas, "
                    + "sol_estatus as solEstatus, sol_pago_total as solPagoTotal, "
                    + "sol_fecha_creacion as solFechaCreacion,"
                    + "sol_usu_id as solUsuId "
                    + "from solicitudes "
                    + "where sol_estatus = 1 "
                    + "and sol_usu_id= %1$s ",
                    idEmpleado));

            List<SolicitudCreditoDto> solsPndts = query.setResultTransformer(Transformers.aliasToBean(SolicitudCreditoDto.class)).list();

            return solsPndts;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }


    /**
     * Guarda la solicitud
     *
     * @param solicitud
     * @return la solicitud guardada con el id generado
     * @throws IntegracionException
     */
    public Solicitudes guardaSolicitud(Solicitudes solicitud) throws IntegracionException {
        super.beginTransaction();

        try {
            super.session.persist(solicitud);
            super.session.getTransaction().commit();
            super.session.flush();
            LOGGER.debug("La solicitud se guardó correctamente");
        } catch (HibernateException ex) {
            super.session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage() + " No se pudo guardar la solicitud", ex);

        }finally{
            super.endTransaction();
        }
        return solicitud;
    }

    /**
     * Obtiene la catorcena inmediata siguiente al dia en que se creó la solicitud
     * @param creacionSolicitud
     * @return
     * @throws IntegracionException 
     */
    public CatorcenaDto obtieneCatorcenas(Date creacionSolicitud) throws IntegracionException {

        if (creacionSolicitud == null) {
            creacionSolicitud = new Date();
        }

        super.beginTransaction();
        SQLQuery query = session.createSQLQuery(String.format("select car_fecha as carFecha "
                + "from catorcenas where car_fecha > '%1$s' "
                + "order by car_fecha asc limit 1", Util.generaFechaFormateada(creacionSolicitud)));

        CatorcenaDto catorcenas = (CatorcenaDto) query.setResultTransformer(Transformers.aliasToBean(CatorcenaDto.class)).uniqueResult();

        super.endTransaction();
        return catorcenas;

    }

    /**
     * Obtiene la fecha del pago del credito de fa, ag o sau
     * @param mes
     * @param julioSigAno
     * @return
     * @throws IntegracionException 
     */
    public CatorcenaDto obtieneCatorcenaUltima7Or12(int mes, Boolean julioSigAno) throws IntegracionException {

        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);

        if (mes == Constantes.FA_CAT_JUN && julioSigAno) {
            year++;
        }

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(String.format("select car_fecha as carFecha "
                + "from catorcenas where car_anio = '%1$s' "
                + "and car_mes = %2$s "
                + "order by car_fecha desc limit 1", year, mes));

        CatorcenaDto catorcenas =(CatorcenaDto) query.setResultTransformer(Transformers.aliasToBean(CatorcenaDto.class)).uniqueResult();

        super.endTransaction();
        return catorcenas;
    }

    public List<CatorcenaDto> obtieneCatorcenaUltima7Or12ProcesosIndependientes(int mes, Boolean julioSigAno, Date fecha) throws IntegracionException {

        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        int year = cal.get(Calendar.YEAR);

        super.beginTransaction();

        if (mes == 7 && julioSigAno) {
            year++;
        }

        SQLQuery query = session.createSQLQuery(String.format("select car_fecha as carFecha "
                + "from catorcenas where car_anio = '%1$s' "
                + "and car_mes = %2$s "
                + "order by car_fecha desc limit 1", year, mes));

        List<CatorcenaDto> catorcenas = query.setResultTransformer(Transformers.aliasToBean(CatorcenaDto.class)).list();

        super.endTransaction();
        return catorcenas;
    }

    public List<CatorcenaDto> obtieneCatorcenasIntermedias(Date fechaInicial, Date fechaFinal) throws IntegracionException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(String.format("select car_fecha as carFecha "
                + "from catorcenas where car_fecha >= '%1$s' "
                + "and car_fecha <= '%2$s' "
                + "order by car_fecha asc", sdf.format(fechaInicial), sdf.format(fechaFinal)));
        List<CatorcenaDto> catorcenas = query.setResultTransformer(Transformers.aliasToBean(CatorcenaDto.class)).list();
        super.endTransaction();
        return catorcenas;
    }

    public List<SolicitudCreditoDto> getSolsByUsuId(Integer idEmpleado) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select sol_id as solId,"
                    + "sol_clave_empleado as solClaveEmpleado, sol_pago_credito as solPagoCredito, "
                    + "sol_monto_solicitado as solMontoSolicitado,sol_catorcenas as solCatorcenas,"
                    + "concat(u.usu_nombre,' ',u.usu_paterno,' ',u.usu_materno) as nombreEmpleado,"
                    + "sol_estatus as solEstatus, sol_pago_total as solPagoTotal, "
                    + "s.sol_fecha_creacion as solFechaCreacion, pro.pro_descripcion as proDescripcion,"
                    + "sol_usu_id as solUsuId, se.sol_est_nmbr_est as solEstatusStr,"
                    + "se.sol_est_descripcion as solObservacion "
                    + "from solicitudes s left join usuarios u on s.sol_usu_id=u.usu_id "
                    + "left join solicitud_estatus se on se.sol_est_id = s.sol_estatus "
                    + "left join productos pro on pro.pro_id = sol_producto "
                    + "where s.sol_usu_id= %1$s ",
                    idEmpleado));

            List<SolicitudCreditoDto> solsPndts = query.setResultTransformer(Transformers.aliasToBean(SolicitudCreditoDto.class)).list();
            super.endTransaction();
            return solsPndts;

        } catch (HibernateException ex) {
            throw new IntegracionException(ex);

        }
    }

    /**
     * Obtiene una solicitud en base a su id
     *
     * @param idSolicitud
     * @return
     * @throws IntegracionException
     */
    public Solicitudes getSolicitudXId(BigInteger idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    String.format(
                            "from Solicitudes sol where sol.solId = %1$s",
                            idSolicitud));

            Solicitudes solicitud = (Solicitudes) query.uniqueResult();
            return solicitud;
        } catch (Exception he) {
            throw new IntegracionException(he);
        }finally{
            super.endTransaction();
            
        }

    }

    /**
     * Metodo que actualiza el estatus de la imagen
     *
     * @param doc
     * @throws IntegracionException
     */
    public void updtImagenEstatus(DocumentosDto doc) throws IntegracionException {
        String hql = String.format(
                "update Imagenes set imaEstatus = %1$s "
                + "where imaId = %2$s ", doc.getiStatus(), doc.getIdImagen());

        super.executeUpdate(hql);
    }

    public void updateImagen(Imagenes pojo) throws IntegracionException {
        super.updatePojo(pojo);
    }

    public List<Imagenes> getImagenesXSolicitud(BigInteger idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    String.format(
                            "from Imagenes ima where ima.solicitudes.solId = %1$s "
                                    + "and imaTipoimagen <> 8 "
                            + "order by ima.imaTipoimagen",
                            idSolicitud));

            List<Imagenes> solicitud = (List<Imagenes>) query.list();

            return solicitud;
        } catch (Exception he) {
            throw new IntegracionException(he);
        }finally{
            super.endTransaction();
        }
    }

    /**
     * Actualiza los datos bancarios de la solicitud
     *
     * @param solicitud
     * @throws IntegracionException
     */
    public void updtDatosBancarios(Solicitudes solicitud) throws IntegracionException {
        String hql = String.format(
                "update Solicitudes set solBanco = '%1$s', solNumeroCuenta = '%2$s',"
                + "solClabeInterbancaria = '%3$s', solNombreTarjetahabiente = '%4$s',"
                + "solEstatusDb = %5$s  "
                + "where solId = %6$s ", solicitud.getSolBanco(), solicitud.getSolNumeroCuenta(),
                solicitud.getSolClabeInterbancaria(), solicitud.getSolNombreTarjetahabiente(),
                solicitud.getSolEstatusDb(), solicitud.getSolId());

        super.executeUpdate(hql);

    }

    /**
     * Obtiene la cantidad de imagenes y avales de una solicitud dada que tienen
     * estatus 2 (validando)
     *
     * @param idSolicitud
     * @return
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public List getImgsAvsXSol(Long idSolicitud) throws IntegracionException {

        try {
            List list = new ArrayList();

            super.beginTransaction();

            Query query = session.createQuery(String.format("from Imagenes where solicitudes.solId = %1$s", idSolicitud));
            Query query2 = session.createQuery(String.format("from SolicitudAvales where solicitudes.solId = %1$s", idSolicitud));

            List<Imagenes> imagenes = (List<Imagenes>) query.list();
            List<SolicitudAvales> avales = (List<SolicitudAvales>) query2.list();

            list.add(imagenes);
            list.add(avales);
            return list;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        }finally{
            super.endTransaction();
        }

    }

    /**
     * Actualiza el estatus db de la solicitud
     *
     * @param idSolicitud
     * @throws IntegracionException
     */
    public void updtEstatusDbSolicitud(Long idSolicitud) throws IntegracionException {
        String hql = String.format("update Solicitudes sol "
                + "set sol.solicitudEstatus.solEstId = 2 "
                + "where sol.solId = %1$s "
                + "and sol.solEstatusDb = 2 ", idSolicitud);

        super.executeUpdate(hql);

    }
    
    /**
     * Actualiza el estatus db de la solicitud
     *
     * @param idSolicitud
     * @throws IntegracionException
     */
    public void updtEstatusSolDocEnviada(Long idSolicitud) throws IntegracionException {
        String hql = String.format("update Solicitudes sol "
                + "set sol.solicitudEstatus.solEstId = 5 "
                + "where sol.solId = %1$s ", idSolicitud);

        super.executeUpdate(hql);

    }
    

    /**
     * Metodo que obtiene todas las solicitudes con estatus 2 'Validando'
     *
     * @return
     * @throws IntegracionException
     */
    public List<SolicitudDto> getSolsFValidando() throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery("select sol.sol_Id as solId,pro.pro_Descripcion as proDescripcion, "
                + "sol.sol_Fecha_Creacion as solFechaCreacion, usu.usu_Clave_Empleado as usuClaveEmpleado, "
                + "concat(usu.usu_Nombre,' ',usu.usu_Paterno,' ',usu.usu_Materno) as usuNombreCompleto, "
                + "sol.sol_usu_id as usuId, "
                + "emp.emp_Abreviacion as empAbreviacion "
                + "from solicitudes as sol inner join solicitud_estatus as est on sol_estatus = est.sol_est_id "
                + "inner join productos as pro on pro.pro_id = sol.sol_producto "
                + "inner join usuarios as usu on usu.usu_id = sol.sol_usu_id "
                + "inner join empresas as emp on emp.emp_id = usu.usu_empresa "
                + "where est.sol_est_id = 2");

        List<SolicitudDto> solicitudes = query.setResultTransformer(Transformers.aliasToBean(SolicitudDto.class)).list();

        super.endTransaction();

        return solicitudes;
    }

    /**
     * Obtiene los detalles de la solicitud que se esta validando a partir de su
     * identificador
     *
     * @param idPendiente
     * @return
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public SolicitudDto obtieneDetalleSolicitud(BigInteger idPendiente) throws IntegracionException {

        super.beginTransaction();

        SQLQuery query = session.createSQLQuery(String.format("select s.sol_id as solId, s.sol_producto as proId, "
                + "s.sol_monto_solicitado as solMontoSolicitado,t.tab_catorcenal as tabCatorcenal, "
                + "s.sol_deducciones as solDeducciones,s.sol_catorcenas as solCatorcenas,s.sol_pago_credito as solPagoCredito, "
                + "s.sol_intereses as solIntereses, (tab_catorcenal*10) as solCalcMontoSalario, "
                + "((t.tab_catorcenal - s.sol_deducciones)*.3) as solCalcPagoSalario, "
                + "s.sol_fecha_ult_catorcena as solFechaUltCatorcena, s.sol_fecha_creacion as solFechaCreacion, "
                + "(t.tab_catorcenal - s.sol_deducciones) as solDisponible "
                + "from solicitudes s left join usuarios u on s.sol_usu_id=u.usu_id "
                + "left join tabulador t on u.usu_puesto=t.tab_descripcion "
                + "where sol_id= %1$s", idPendiente));

        SolicitudDto solicitud = (SolicitudDto) query.setResultTransformer(Transformers.aliasToBean(SolicitudDto.class)).uniqueResult();

        super.endTransaction();
        return solicitud;
    }

    public void updtSolicitudDetalle(SolicitudDto solicitud) throws IntegracionException {

        String hql = String.format(
                "update Solicitudes set solMontoSolicitado = '%1$s', solPagoCredito = '%2$s',"
                + "solCatorcenas = '%3$s' "
                + "where solId = %4$s ", solicitud.getSolMontoSolicitado(), solicitud.getSolPagoCredito(),
                solicitud.getSolCatorcenas(), solicitud.getSolId());

        super.executeUpdate(hql);
    }

    /**
     * Actualiza el estatus de la solicitud solo cuando es Autorizacion o
     * Cancelacion
     *
     * @param idSolicitud
     * @param estatus
     * @throws IntegracionException
     */
    public void updtEstSolAutCanc(BigInteger idSolicitud, Integer estatus) throws IntegracionException {
        String fechaAutorizacion = String.format(",sol.solFechaAutorizacion = '%1$s' ", Util.generaFechaFormateada(new Date()));
        String fechaCancelacion = String.format(",sol.solFechaCancelacion = '%1$s' ", Util.generaFechaFormateada(new Date()));

        String hql = String.format("update Solicitudes sol "
                + "set sol.solicitudEstatus.solEstId = %2$s "
                + "%3$s "
                + "where sol.solId = %1$s ", idSolicitud, estatus,
                estatus == 3 ? fechaAutorizacion : fechaCancelacion);

        super.executeUpdate(hql);

    }
    
    
    /**
     * Actualiza el estatus de la solicitud solo cuando es Documentaciónn aprobada
     *
     * @param idSolicitud
     * @param estatus
     * @param formatoRecepcion
     * @throws IntegracionException
     */
    public void updtDocFirmada(Long idSolicitud, Integer estatus, Integer formatoRecepcion) throws IntegracionException {

        String hql = String.format("update Solicitudes sol "
                + "set sol.solicitudEstatus.solEstId = %2$s,sol.solFormatoDocFirmada = %3$s,"
                + "sol.solFechaEnviodocumentos = '%4$s' "
                + "where sol.solId = %1$s ", idSolicitud, estatus,
                formatoRecepcion, Util.generaFechaFormateada(new Date()));

        super.executeUpdate(hql);

    }

    /**
     * Actualiza el estatus de la solicitud
     *
     * @param idSolicitud
     * @param estatus
     * @throws IntegracionException
     */
    public void updtEstatusSolicitud(Long idSolicitud, int estatus) throws IntegracionException {
        String hql = String.format("update Solicitudes sol "
                + "set sol.solicitudEstatus.solEstId = %2$s "
                + "where sol.solId = %1$s ", idSolicitud, estatus);

        super.executeUpdate(hql);

    }

    /**
     * Actualiza la fecha de deposito de un credito
     *
     * @param idSolicitud
     * @param fechaDeposito
     * @throws IntegracionException
     */
    public void updtFechaDepositoCredito(Long idSolicitud, Date fechaDeposito) throws IntegracionException {
        String hql = String.format("update CreditosFinal cre "
                + "set cre.creFechaDeposito = '%2$s' "
                + "where cre.creSolicitud = %1$s ", idSolicitud, Util.generaFechaFormateada(fechaDeposito));

        super.executeUpdate(hql);

    }

    public void guardaImagenes(ImagenesDto imagenes) throws IntegracionException {

        String sql = String.format(
                "insert into imagenes(ima_solicitud, ima_imagen, ima_tipoimagen, ima_estatus)"
                + " values (%1$s, '%2$s', %3$s, %4$s) ",
                imagenes.getImaSolicitud(), imagenes.getImagen(), imagenes.getTipoImagen(),imagenes.getImaEstatus());

        super.executeUpdateSql(sql);
    }
    
    
    public void eliminaSolicitud() throws IntegracionException {

        String sql = String.format(
                "delete from solicitudes where sol_id = %1$s and sol_estatus = 1 ");
              //  imagenes.getImaSolicitud(), imagenes.getImagen(), imagenes.getTipoImagen(),imagenes.getImaEstatus());

        super.executeUpdateSql(sql);
    }
    
    
     public static void main(String args[]) {
       
    }

    public Integer getEstatusDatosBancarios(Long idSolicitud) throws IntegracionException {
          super.beginTransaction();

        SQLQuery query = session.createSQLQuery(String.format("select sol_estatus_db as solEstatusDb "
                + "from solicitudes where sol_id= %1$s", idSolicitud));

        Integer estatusDb = (Integer) query.uniqueResult();

        super.endTransaction();
        return estatusDb;
    }

}
