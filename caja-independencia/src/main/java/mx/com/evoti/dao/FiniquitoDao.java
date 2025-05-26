/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.Query;
import org.hibernate.HibernateException;

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
    
    public void actualizarBajaEmpleadoConFiniquito(Integer usuId, double deudaCreditos, double ahorros, int estatus, Date fechaFiniquito) throws IntegracionException {
        try {
            this.beginTransaction();

            String hql = "FROM BajaEmpleados WHERE baeIdEmpleado = :usuId ORDER BY bae_id DESC";
            Query consulta = this.session.createQuery(hql)
                    .setParameter("usuId", usuId)
                    .setMaxResults(1);

            BajaEmpleados baja = (BajaEmpleados) consulta.uniqueResult();

            if (baja == null) {
                throw new IntegracionException("No se encontró un registro en baja_empleados para el usuario: " + usuId);
            }

            baja.setBaeDeudaCreditos(deudaCreditos);
            baja.setBaeAhorros(ahorros);
            baja.setBaeEstatus(estatus);
            baja.setBaeFechaAdministracion(fechaFiniquito);
            baja.setBaeFechaPdf(new Date());      // opcional
            baja.setBaeFechaDeposito(new Date()); // opcional

            this.session.update(baja);
            this.session.getTransaction().commit();
            this.session.flush();

        } catch (HibernateException ex) {
            this.session.getTransaction().rollback();
            throw new IntegracionException("Error al actualizar baja_empleados", ex);
        } finally {
            this.endTransaction();
        }
    }
}
