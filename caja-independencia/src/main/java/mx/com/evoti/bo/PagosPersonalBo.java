/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.PagosPersonalDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.PagosPersonalDto;
import mx.com.evoti.hibernate.pojos.Pagos;

/**
 *
 * @author NeOleon
 */
public class PagosPersonalBo implements Serializable {

    private static final long serialVersionUID = 787366304654821056L;

    private PagosPersonalDao pagosAgrupadosDao;

    public PagosPersonalBo() {
        pagosAgrupadosDao = new PagosPersonalDao();
    }

    public List<PagosPersonalDto> obtienePagosAcumulados(Integer usuId) throws BusinessException {
        try {
            List<PagosPersonalDto> pagosAgrupados = pagosAgrupadosDao.obtienePagosAgrupados(usuId);
            return pagosAgrupados;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<PagosPersonalDto> obtieneAcumulado(Integer usuId) throws BusinessException {
        try {
            List<PagosPersonalDto> acumulado = pagosAgrupadosDao.obtieneAcumulado(usuId);
            return acumulado;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List getPagosDetalle(Integer usuId, Integer pagEstatus) throws BusinessException {

        try {
            List pagDetalle = null;
            pagDetalle = pagosAgrupadosDao.getPagosDetalle(usuId, pagEstatus);
            return pagDetalle;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public void guardaPago(Pagos pojos) throws BusinessException {

        try {
            pagosAgrupadosDao.savePago(pojos);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }
}
