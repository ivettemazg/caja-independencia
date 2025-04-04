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
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.PagosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.AcumuladoDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class ProcesoPagoAcumuladoBo implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProcesoPagoAcumuladoBo.class);
    private static final long serialVersionUID = 7604074470854500792L;

    private PagosDao pagDao;

    public ProcesoPagoAcumuladoBo() {
        pagDao = new PagosDao();
    }

    public void ejecutaPagoAcumulado(List<AmortizacionDto> amoSelected, Usuarios usuario,
            AcumuladoDto acumDto, Double montoPago,
            Date fechaPago, Integer creId) throws BusinessException {

        AmortizacionDao amoDao = new AmortizacionDao();

        CreditosBo credBo = new CreditosBo();

        //Guarda el pago con acumulado en la tabla pagos y en bancos
        Integer idPago = guardaPago(creId, montoPago, montoPago * -1,
                usuario.getEmpresas().getEmpId(), fechaPago, usuario.getUsuId(),
                usuario.getUsuClaveEmpleado());

        //Actualiza para cada amortizacion el id del pago y el estatus de la amortizacion
        amoSelected.forEach((AmortizacionDto amo) -> {

            try {
                amoDao.updtPagoIdEstInt(amo.getAmoId(), Constantes.AMO_ESTATUS_PACUM_5, idPago);
            } catch (IntegracionException ex) {
                LOGGER.error("ERROR AL ACTUALIZAR EL ID DEL PAGO EN AMORTIZACION", ex.getCause());
            }

        });

        /*
        Si despues de ejecutar la actualización ya no quedan catorcenas pendientes, este metodo
        se encarga de actualizar el estatus del credito a pgado
         */
        credBo.saldarCredito(creId);

    }

    public Integer guardaPago(Integer creId, Double montoPago, Double acumulado, Integer empId,
            Date fech, Integer usuId, Integer cveEmpleado) throws BusinessException {
        try {
            PagosDao pagoDao = new PagosDao();

            Pagos pago = new Pagos();

            pago.setPagAcumulado(acumulado);
            pago.setPagClaveEmpleado(cveEmpleado);
            pago.setPagCredito(creId);
            pago.setPagDeposito(montoPago);
            pago.setPagEmpresa(empId);
            pago.setPagEstatus(Constantes.PAGEST_ACUM_8);
            pago.setPagFecha(fech);
            pago.setPagUsuId(usuId);

            pagoDao.guardaPago(pago);

            return pago.getPagId();
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public AcumuladoDto obtieneAcumuladoXUsuario(Integer usuId) throws BusinessException {
        try {
            return pagDao.obtieneAcumuladoXUsuario(usuId);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

}
