/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.transaccion;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.reportesContabilidad.ReporteSaldoCreditosBo;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.transacciones.TransaccionDao;
import mx.com.evoti.hibernate.pojos.RegistroTransaccion;

/**
 *
 * @author ivett
 */
public class TransaccionBo {


    private TransaccionDao tranDao;
    
    public TransaccionBo() {
        tranDao = new TransaccionDao();
    }
    
    public void guardaTransaccion(Integer idTipoTran, Integer idSistema, Integer idUsu) throws BusinessException{
     
    try{
        RegistroTransaccion regTran = new RegistroTransaccion();
        
        regTran.setTranIdSistema(idSistema);
        regTran.setTranIdUsuario(idUsu);
        regTran.setTranTipoTran(idTipoTran);
        regTran.setTranFecha(new Date());
        
        tranDao.saveTransaccion(regTran);
        
    } catch (IntegracionException ex) {
        throw new BusinessException(ex.getMessage(), ex.getCause());
    }

    }
    
    
}
