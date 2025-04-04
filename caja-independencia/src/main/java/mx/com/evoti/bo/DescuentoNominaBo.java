/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.DescuentoNominaDto;
import org.slf4j.LoggerFactory;
import mx.com.evoti.dao.DescuentoNominaDao;

/**
 *
 * @author NeOleon
 */
public class DescuentoNominaBo implements Serializable {

    private DescuentoNominaDao descNominaDao;

    private List<DescuentoNominaDto> descuentoNomina;

    public DescuentoNominaBo() {
        descNominaDao = new DescuentoNominaDao();
    }

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DescuentoNominaBo.class);
    private static final long serialVersionUID = -604848521024535467L;

    public List<DescuentoNominaDto> obtieneListaDescuentosNomina(Date fechaCatorcena, Integer empresa) throws BusinessException {
        try {
            descuentoNomina = descNominaDao.extraeDescuentoNominaEmpresas(fechaCatorcena, empresa);
            return descuentoNomina;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<DescuentoNominaDto> obtieneListaDescuentosNominaOtras(Date fechaCatorcena, Integer empresa) throws BusinessException {
        try {
            descuentoNomina = descNominaDao.extraeDescuentoNominaEmpresasOtras(fechaCatorcena, empresa);
            return descuentoNomina;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public List<DescuentoNominaDto> obtieneListaDescuentosNominaFAAG(Date fechaCatorcena) throws BusinessException {
        try {
            descuentoNomina = descNominaDao.extraeDescuentoNominaEmpresasFAAG(fechaCatorcena);
            return descuentoNomina;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

}
