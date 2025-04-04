/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.reporteEmpleado;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.transaccion.TransaccionBo;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.PagosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class ProcesoPagoExtemporaneoBo implements Serializable{
    
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProcesoPagoExtemporaneoBo.class);
    private static final long serialVersionUID = -2083146677070131638L;
    private TransaccionBo tranBo;
    
   
    public ProcesoPagoExtemporaneoBo() {
        tranBo = new TransaccionBo();
    }

    
    /**
     * Metodo que realiza el proceso de pago extemporaneo, el cual guarda un registro
     * en la tabla pagos y en bancos, actualiza el amoPagoId en la tabla amortizacion que 
     * hace referencia al id del pago previamente guardado, por ultimo hace una validación
     * de las catorcenas pendientes de un credito para poder saldarlo
     * @param amoSelected
     * @param usuario
     * @param montoPago
     * @param acumulado
     * @param fechaPago
     * @param creId
     * @throws BusinessException 
     */
    public Integer ejecutaProcesoPagoExt(List<AmortizacionDto> amoSelected, Usuarios usuario, Double montoPago,
            Double acumulado, Date fechaPago, Integer creId, Integer userSession) throws BusinessException {

        AmortizacionDao amoDao = new AmortizacionDao();
        CreditosBo credBo= new CreditosBo();

        //Guarda el pago extemporaneo en la tabla pagos y en bancos
        Integer idPago = guardaPago(creId, montoPago, acumulado, 
                usuario.getEmpresas().getEmpId(), fechaPago, usuario.getUsuId(),
                usuario.getUsuClaveEmpleado());
        
        tranBo.guardaTransaccion(20, idPago, userSession);

        
        //Actualiza para cada amortizacion el id del pago y el estatus de la amortizacion
        amoSelected.forEach((AmortizacionDto amo) -> {
         
            try {
                amoDao.updtPagoIdEstInt(amo.getAmoId(), Constantes.AMO_ESTATUS_PEXT_6, idPago);
            } catch (IntegracionException ex) {
                LOGGER.error("ERROR AL ACTUALIZAR EL ID DEL PAGO EN AMORTIZACION", ex.getCause());
            }
            
        });
        
        /*
        Si despues de ejecutar la actualización ya no quedan catorcenas pendientes, este metodo
        se encarga de actualizar el estatus del credito a pgado
        */
      credBo.saldarCredito(creId);
        
      return idPago;

    }
    
    /**
     * Guarda un registro en la tabla pagos de tipo pago extemporaneo, así como
     * tambien guarda su respectivo registro en bancos
     *
     * @param creId
     * @param montoPago
     * @param acumulado
     * @param empId
     * @param fech
     * @param usuId
     * @param cveEmpleado
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public Integer guardaPago(Integer creId, Double montoPago, Double acumulado, Integer empId, 
            Date fech, Integer usuId, Integer cveEmpleado) throws BusinessException {
        try {
            PagosDao pagoDao = new PagosDao();
            BancosBo banBo = new BancosBo();

            Pagos pago = new Pagos();

            pago.setPagAcumulado(acumulado);
            pago.setPagClaveEmpleado(cveEmpleado);
            pago.setPagCredito(creId);
            pago.setPagDeposito(montoPago);
            pago.setPagEmpresa(empId);
            pago.setPagEstatus(Constantes.PAGEST_EXTEMP_10);
            pago.setPagFecha(fech);
            pago.setPagUsuId(usuId);

            pagoDao.guardaPago(pago);

            /**
             * Crea un objeto tipo Banco que se insertará y hará referencia al
             * pago de finiquito que se creó anteriormente
             */
            Bancos banco = new Bancos();

            banco.setBanAjustado(Constantes.BAN_NO_AJUSTADO);
            banco.setBanConcepto(Constantes.BAN_PAGO_EXTMP);
            banco.setBanIdConceptoSistema(pago.getPagId());
            banco.setBanMonto(pago.getPagDeposito());
            banco.setBanEmpresa(empId);
            banco.setBanFechatransaccion(fech);

            banBo.guardaBanco(banco);
            
            return pago.getPagId();
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
    
}
