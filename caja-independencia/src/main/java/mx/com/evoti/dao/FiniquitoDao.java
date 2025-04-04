/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.BajaEmpleados;

/**
 *
 * @author Venette
 */
public class FiniquitoDao extends ManagerDB implements Serializable {
    
    private static final long serialVersionUID = 2332738512096635151L;
    
    
    
    public void insertBajaEmpleados(BajaEmpleados pojo) throws IntegracionException{
        super.savePojo(pojo);
    }

    
    /**
     * Actualiza el saldo de los creditos y los ahorros en la tabla BajaEmpleados
     * @param usuId
     * @param saldoCre
     * @param ahorro
     * @param baeEstatus
     * @throws IntegracionException 
     */
    public void updtBaeSaldoAhorro(Integer usuId, Double saldoCre, Double ahorro, Integer baeEstatus) throws IntegracionException {
           String hql = String.format("update BajaEmpleados set baeDeudaCreditos = %1$s, "
                   + "baeAhorros = %2$s, baeEstatus = %3$s "
                     + "where baeIdEmpleado = %4$s ",saldoCre, ahorro,baeEstatus, usuId);
        
        super.executeUpdate(hql);
    }
    
    
}
