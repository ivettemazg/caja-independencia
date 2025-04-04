/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.bancos;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.AvalesSolicitudDao;
import mx.com.evoti.dao.CargaPagosAportacionesDao;
import mx.com.evoti.dao.bancos.BancosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.SolicitudDto;
import mx.com.evoti.dto.bancos.BancosDto;
import mx.com.evoti.dto.bancos.DesctosXCobrarCredsDto;
import mx.com.evoti.dto.bancos.ReporteBancosDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.EstadoCuenta;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class BancosBo implements Serializable {

    private static final long serialVersionUID = 8988366398817748282L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BancosBo.class);

    private BancosDao bancoDao;

    public BancosBo() {
        bancoDao = new BancosDao();
    }

    /**
     * Obtiene los pagos de finiquito que se hicieron para cada credito
     *
     * @return
     * @throws BusinessException
     */
    public List<DesctosXCobrarCredsDto> getDesctosXCobrarCreds() throws BusinessException {
        try {
            return bancoDao.getDesctosXCobrarCreds();
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Actualiza el descuento por aplicar cuando un finiquito es otorgado para
     * pagar un credito
     *
     * @param dto
     * @throws BusinessException
     */
    public void updtRegBanco(DesctosXCobrarCredsDto dto) throws BusinessException {
        try {
            BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
            Bancos banco = new Bancos();

            banco.setBanId(dto.getBanId());
            banco.setBanEmpresa(dto.getEmpId());
            banco.setBanFechatransaccion(dto.getPagFecha());
            banco.setBanMonto(dto.getPagDeposito());
            banco.setBanAjustado(Constantes.BAN_AJUSTADO);
            banco.setBanConcepto(Constantes.BAN_DESCTOXCOB_CRED);
            banco.setBanIdConceptoSistema(dto.getPagId());
            banco.setBanIdRelacion(idRelacion.longValue());
            banco.setBanFechaRelacion(new Date());
            bancoDao.updatePojo(banco);
            LOGGER.debug("se actualizó en BANCOS");

            guardaEdoCtaYBEC(banco, idRelacion.longValue());
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

  
    public void guardaEdoCtaYBEC(Bancos banco, Long idRelacion) throws BusinessException {
        try {
            EstadoCuenta edoCta = new EstadoCuenta();
            edoCta.setEcAjustado(Constantes.EC_AJUSTADO);
            edoCta.setEcConcepto(banco.getBanConcepto());
            edoCta.setEcEmpresa(banco.getBanEmpresa());
            edoCta.setEcFechatransaccion(banco.getBanFechatransaccion());
            edoCta.setEcMonto(banco.getBanMonto());
            edoCta.setEcIdRelacion(idRelacion);
            edoCta.setEcFechaRelacion(new Date());

            //Guarda estado_cuenta
            bancoDao.saveEstadoCuenta(edoCta);
            LOGGER.debug("se guardo en ESTADO_CUENTA");
            
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public void guardaBanco(Bancos pojo) throws BusinessException {
        try {
            bancoDao.saveBanco(pojo);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Guarda afectando las 3 tablas relacionadas con bancos, aplica en
     * transacciones que realiza Jeffrey
     *
     * @param pojo
     * @param idRelacion
     * @throws BusinessException
     */
    public void guardaBancoCEdoCta(Bancos pojo, Long idRelacion) throws BusinessException {
        try {
            //Guarda banco
            bancoDao.saveBanco(pojo);
            LOGGER.debug("se guardo en BANCOS");
            EstadoCuenta edoCta = new EstadoCuenta();
            edoCta.setEcAjustado(Constantes.EC_AJUSTADO);
            edoCta.setEcConcepto(pojo.getBanConcepto());
            edoCta.setEcEmpresa(pojo.getBanEmpresa());
            edoCta.setEcFechatransaccion(pojo.getBanFechatransaccion());
            edoCta.setEcMonto(pojo.getBanMonto());
            edoCta.setEcIdRelacion(idRelacion);
            edoCta.setEcFechaRelacion(new Date());

            //Guarda estado_cuenta
            bancoDao.saveEstadoCuenta(edoCta);
            LOGGER.debug("se guardo en ESTADO_CUENTA");
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Genera un registro de bancos de tipo Devolucion
     *
     * @param mov
     * @return
     */
    public Bancos generaBancoDev(Movimientos mov) {
        BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
        Bancos banco = new Bancos();
        banco.setBanAjustado(Constantes.BAN_AJUSTADO);
        banco.setBanIdConceptoSistema(mov.getMovId());
        banco.setBanFechatransaccion(mov.getMovFecha());
        banco.setBanEmpresa(mov.getMovEmpresa());
        banco.setBanMonto(mov.getMovDeposito());
        banco.setBanIdRelacion(idRelacion.longValue());
        banco.setBanFechaRelacion(new Date());
        switch (mov.getMovProducto()) {
            case 1:
                if (mov.getMovAr() == 1) {
                    banco.setBanConcepto(Constantes.BAN_DEV_A_FIJO);
                } else {
                    banco.setBanConcepto(Constantes.BAN_DEV_RDTO_FIJO);

                }
                break;
            case 2:
                if (mov.getMovAr() == 1) {
                    banco.setBanConcepto(Constantes.BAN_DEV_A_N_FIJO);
                } else {
                    banco.setBanConcepto(Constantes.BAN_DEV_RDTO_N_FIJO);

                }
                break;
            case 3:
                if (mov.getMovAr() == 1) {
                    banco.setBanConcepto(Constantes.BAN_DEV_A_VOL);
                } else {
                    banco.setBanConcepto(Constantes.BAN_DEV_RDTO_VOL);

                }
                break;
        }

        return banco;
    }

    /**
     * Genera un registro en la tabla bancos que corresponde a una aportacion
     * voluntaria, afecta también la tabla de estado_cuenta
     *
     * @param mov
     * @param idRelacion
     * @return
     */
    public Bancos generaBancoAVol(Movimientos mov, Long idRelacion) {
        Bancos banco = new Bancos();
        banco.setBanConcepto(Constantes.BAN_AP_VOL);
        banco.setBanAjustado(Constantes.BAN_AJUSTADO);
        banco.setBanIdConceptoSistema(mov.getMovId());
        banco.setBanFechatransaccion(mov.getMovFecha());
        banco.setBanEmpresa(mov.getMovEmpresa());
        banco.setBanMonto(mov.getMovDeposito());
        banco.setBanIdRelacion(idRelacion);
        banco.setBanFechaRelacion(new Date());

        return banco;
    }
    
    /**
     * Genera el concepto de bancos que se guardará en la tabla de bancos
     * con referencia al deposito de un credito a un usuario
     * @param idSolicitud
     * @param idEmpresa
     * @param deposito
     * @param idRelacion
     * @return 
     */
    public Bancos generaBancoDepositoCredito(Long idSolicitud, Integer idEmpresa, Double deposito, Long idRelacion) {
        Bancos banco = new Bancos();
        banco.setBanConcepto(Constantes.BAN_DEP_CREDITO);
        banco.setBanAjustado(Constantes.BAN_AJUSTADO);
        banco.setBanIdConceptoSistema(idSolicitud.intValue());
        banco.setBanFechatransaccion(new Date());
        banco.setBanEmpresa(idEmpresa);
        banco.setBanMonto(deposito*(-1));
        banco.setBanIdRelacion(idRelacion);
        banco.setBanFechaRelacion(new Date());

        return banco;
    }
    /**
     * Genera un registro en la tabla bancos que corresponde a una aportacion
     * voluntaria, afecta también la tabla de estado_cuenta
     *
     * @param pag
     * @param idRelacion
     * @return
     */
    public Bancos generaBancoDevAcumulado(Pagos pag, Long idRelacion) {
        Bancos banco = new Bancos();
        banco.setBanConcepto(Constantes.BAN_DEV_ACUMULADO);
        banco.setBanAjustado(Constantes.BAN_AJUSTADO);
        banco.setBanIdConceptoSistema(pag.getPagId());
        banco.setBanFechatransaccion(pag.getPagFecha());
        banco.setBanEmpresa(pag.getPagEmpresa());
        banco.setBanMonto(pag.getPagDeposito()*-1);
        banco.setBanIdRelacion(idRelacion);
        banco.setBanFechaRelacion(new Date());

        return banco;
    }

    public Bancos generaBancoCredito(CreditosFinal cre) {
        Bancos banco = new Bancos();
        banco.setBanConcepto(Constantes.BAN_DEP_CREDITO);
        //Se pone no ajustado porque aun no se asigna la fecha de deposito
        banco.setBanAjustado(Constantes.BAN_NO_AJUSTADO);
        banco.setBanIdConceptoSistema(cre.getCreId());
        banco.setBanMonto(cre.getCrePrestamo());

        return banco;
    }

   
    /**
     * Genera un registro para la tabla bancos que hace referencia al total
     * de un archivo de pagos o aportaciones
     * @param tipoArchivo
     * @param idArchivo
     * @param empresa
     * @param monto
     * @return
     * @throws BusinessException 
     */
    public Bancos guardaBancosArchivo(Integer tipoArchivo, Integer idArchivo, 
            Integer empresa, Double monto) throws BusinessException {
        Bancos banco = new Bancos();
        if (tipoArchivo == 1) {
            banco.setBanConcepto(Constantes.BAN_PAGOSARCHIVO);

        } else if (tipoArchivo == 2) {
            banco.setBanConcepto(Constantes.BAN_APORTACIONARCHIVO);
        }
        
        
        banco.setBanAjustado(Constantes.BAN_NO_AJUSTADO);
        banco.setBanIdConceptoSistema(idArchivo);
        banco.setBanEmpresa(empresa);
        banco.setBanMonto(monto);
        banco.setBanFechatransaccion(new Date());
        
        guardaBanco(banco);
        
        return banco;
    }

    private Bancos transformDtoToPojo(BancosDto dto) {

        Bancos pojo = new Bancos();

        pojo.setBanAjustado(dto.getBanAjustado());
        pojo.setBanConcepto(dto.getBanConcepto());
        pojo.setBanEmpresa(dto.getBanEmpresa());
        pojo.setBanFechatransaccion(dto.getBanFechatransaccion());
        pojo.setBanId(dto.getBanId());
        pojo.setBanIdConceptoSistema(dto.getBanIdConceptoSistema());
        pojo.setBanMonto(dto.getBanMonto());

        return pojo;

    }

    public List<ReporteBancosDto> getReporteBancos(Date fechaInicial, Date fechaFinal) throws BusinessException{
        try {
           return bancoDao.getReporteBancos(fechaInicial, fechaFinal);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
    public static void main(String args[]) throws IntegracionException {
        try {
            BancosBo bo = new BancosBo();
            System.setProperty("Log4J.configuration", new File(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "Log4J.properties").toURI().toURL().toString());
            HibernateUtil.buildSessionFactory2();
            Bancos pojo = new Bancos();

            pojo.setBanAjustado(1);
            pojo.setBanConcepto(1);
            pojo.setBanEmpresa(10);
            pojo.setBanFechatransaccion(new Date());
            pojo.setBanIdConceptoSistema(666);
            pojo.setBanMonto(12345.00);
//            bo.guardaBancoCEdoCta(pojo);

            CargaPagosAportacionesDao dao = new CargaPagosAportacionesDao();

            System.out.println(dao.obtieneSumaArchivo(22, 1));

            HibernateUtil.closeSessionFactory();

        } catch (MalformedURLException ex) {
            Logger.getLogger(AvalesSolicitudDao.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    
    
    
}
