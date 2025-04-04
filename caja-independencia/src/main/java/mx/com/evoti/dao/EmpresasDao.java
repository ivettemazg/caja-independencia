/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.altasCambiosEmp.AltasUsuarioDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.EmpresasDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Empresas;
import org.hibernate.Query;

/**
 *
 * @author Venette
 */
public class EmpresasDao extends ManagerDB implements Serializable{

    private static final long serialVersionUID = 6302557396925161008L;
    
    public EmpresasDao(){
        
    }
    
    
    /**
     * Obtiene todas las empresas de la base de datos
     * @return
     * @throws IntegracionException 
     */
     public List<Empresas> getEmpresas() throws IntegracionException{
          super.beginTransaction();  
         Query hql = session.createQuery("from Empresas order by empId asc");

        List<Empresas> empresas = hql.list();
           super.endTransaction();
           
           
        return empresas;
     }
    
     
     public List<EmpresasDto> getEmpresasDto() throws IntegracionException{
         
         List<Empresas> empresas = getEmpresas();
         List<EmpresasDto> dtos = new ArrayList<>();
         
         empresas.stream().forEach((pojo) -> {
             dtos.add(new EmpresasDto(pojo.getEmpId(), 
                     pojo.getEmpAbreviacion(), 
                     pojo.getEmpDescripcion()));
        });
         
     
    return dtos;
     }
 
     
      public static void main(String args[]) {
        try {
            HibernateUtil.buildSessionFactory2();
            EmpresasDao dao = new EmpresasDao();

            System.out.println(dao.getEmpresasDto().size());
            
            HibernateUtil.closeSessionFactory();
        } catch (IntegracionException ex) {
            Logger.getLogger(AltasUsuarioDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
}
