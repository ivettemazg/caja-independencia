/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.finiquito.UsuarioBajaDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.Usuarios;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class UsuariosDao extends ManagerDB implements Serializable{
 
    private static final long serialVersionUID = 8958190915055950161L;
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UsuariosDao.class);

     /**
      * Actualiza un pojo del tipo Usuarios
      * @param usuario
      * @throws IntegracionException 
      */
    public void updtUsuario(Usuarios usuario) throws IntegracionException {
        
        super.updatePojo(usuario);

    }

    
    public Usuarios obtenerUsuario(Integer idUsuario) throws IntegracionException{
         try{
                super.beginTransaction();
                
                
            Usuarios usuarios = (Usuarios)session.get(Usuarios.class, idUsuario);
            
               return usuarios;
            }catch(HibernateException he){
                LOGGER.error(he.getMessage());
              throw new IntegracionException(he);
          }finally{
               super.endTransaction();
         }
    }

    
     /**
      * Actualiza el campo usuprimeravez de un usuario
      * @param idUsuario
      * @param primeraVez
      * @throws IntegracionException 
      */
     public void updtUsrPrimeraVez(Integer idUsuario, int primeraVez) throws IntegracionException{
             String hql = String.format("update Usuarios set usuPrimeravez = %1$s "
                     + "where usuId = %2$s ",primeraVez, idUsuario);
        
        super.executeUpdate(hql);
     }
    
     /**
      * Metodo que se utiliza en todos los filtros en que se manda
      * clve de empleado y empresa
      * @param claveEmpleado
      * @param idEmpresa
      * @return
      * @throws IntegracionException 
      */
     public List<Usuarios> getUsuarioXCveYEmpresa(Integer claveEmpleado, Integer idEmpresa) throws IntegracionException{
          super.beginTransaction();  
         Query sqlQuery = session.createQuery(
                String.format("from Usuarios usu "
                        + "where usu.usuClaveEmpleado = %1$s "
                        + "and usu.empresas.empId = %2$s ", claveEmpleado,idEmpresa));

        List<Usuarios> usuarios = sqlQuery.list();
           super.endTransaction();
        return usuarios;
     }
     
     /**
      * Obtiene un usuario buscando por su id, la diferencia con el metodo obtenerUsuario
      * es que este último no trae la entidad Empresa en el resultado, y el actual sí
      * @param id
      * @return
      * @throws IntegracionException 
      */
      public Usuarios getUsrById(Integer id) throws IntegracionException{
          super.beginTransaction();  
         Query qry = session.createQuery(
                String.format("from Usuarios usu "
                        + "where usu.usuId = %1$s ", id));

        Usuarios usuarios = (Usuarios)qry.uniqueResult();
        
        super.endTransaction();
        
        return usuarios;
     }
      
      
      /**
       * Obtiene todos los usuarios dados de baja
       * @return
       * @throws IntegracionException 
       */
      public List<UsuarioBajaDto> getUsrsBaja(Integer estatus) throws IntegracionException{
          super.beginTransaction(); 
          
         Query qry = session.createQuery(
                String.format("select usu.usuId as usuId, usu.usuClaveEmpleado as usuClaveEmpleado,"
                        + "usu.empresas.empAbreviacion as empAbreviacion, "
                        + "concat(usu.usuNombre, ' ', usu.usuPaterno,' ', usu.usuMaterno) as usuNombreCompleto, "
                        + "usu.usuFechaBaja as usuFechaBaja, bae.baeDeudaCreditos as baeDeudaCreditos,"
                        + "bae.baeAhorros as baeAhorros "
                        + "from Usuarios usu, BajaEmpleados bae "
                        + "where bae.baeEstatus = %1$s "
                        + "and usu.usuFechaBaja is not null "
                        + "and usu.usuId = bae.baeIdEmpleado "
                        + "order by usu.usuFechaBaja desc ", estatus));

        List<UsuarioBajaDto> usuarios = qry.setResultTransformer(Transformers.aliasToBean(UsuarioBajaDto.class)).list();
          System.out.println(usuarios.size());
        super.endTransaction();
       
        return usuarios;
     }
      
      public List<UsuarioBajaDto> getUsrsBajaPendiente(Integer estatus) throws IntegracionException{
          super.beginTransaction(); 
          
         Query qry = session.createSQLQuery(
                String.format("select usu.usu_id as usuId, usu.usu_clave_empleado as usuClaveEmpleado, " +
                        "emp.emp_abreviacion as empAbreviacion, " +
                        "concat(usu.usu_nombre, ' ', usu.usu_paterno,' ', usu.usu_materno) as usuNombreCompleto, " +
                        "usu.usu_fecha_baja as usuFechaBaja, bae.bae_deuda_creditos as baeDeudaCreditos, " +
                        "bae.bae_ahorros as baeAhorros " +
                        "from usuarios usu, empresas emp, baja_empleados bae, creditos_final cre " +
                        "where cre.cre_usu_id = usu.usu_id " +
                        "and bae.bae_estatus = %1$s " +
                        "and usu.usu_fecha_baja is not null " +
                        "and usu.usu_id = bae.bae_id_empleado " +
                        "and usu.usu_empresa = emp.emp_id " +
                        "and cre.cre_estatus in (1, 6, 7) "  +
                        "order by usu.usu_fecha_baja desc ", estatus));

        List<UsuarioBajaDto> usuarios = qry.setResultTransformer(Transformers.aliasToBean(UsuarioBajaDto.class)).list();
          System.out.println(usuarios.size());
        super.endTransaction();
       
        return usuarios;
     } 
      
    
       public UsuarioDto getUsrActualizado(Integer idUsuario) throws IntegracionException {
           try{
                super.beginTransaction();
          
               SQLQuery query = session.createSQLQuery(String.format("select usu_id as id, "
                       + "usu_clave_empleado as cveEmpleado, usu_nombre as nombre, usu_paterno as paterno, "
                       + "usu_materno as materno, usu_fecha_ingreso as fechaIngresoCaja,"
                       + "usu_fecha_ingreso_empresa as fechaIngresoEmpresa, usu_correo as correo, "
                       + "usu_empresa as empresaId, usu_password as password, "
                       + "usu_salario_neto as salarioNeto, rol_nombre as rol,"
                       + "rol_id as rolId, usu_primeravez as primeraVez, "
                       + "usu_fecha_baja as fechaBaja "
                       + "from usuarios inner join roles on rol_id = usu_rol "
                       + "where usu_id = '%1$s'" , 
                    idUsuario));
                  
                 UsuarioDto usuario = (UsuarioDto)query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).uniqueResult();
                 return usuario;
          }catch(Exception he){
              throw new IntegracionException(he);
          }finally{
            super.endTransaction();
        }
       
    }
       
       /**
        * Metodo que actualiza la columna usuOmitirValidaciones en la tabla de usuarios
        * @param idUsuario
        * @param omitir
        * @throws IntegracionException 
        */
       public void updtUsrOmitirValidaciones(Integer idUsuario, int omitir) throws IntegracionException{
             String hql = String.format("update Usuarios set usuOmitirValidaciones = %1$s "
                     + "where usuId = %2$s ",omitir, idUsuario);
        
        super.executeUpdate(hql);
     }
       
       
       /**
        * Metodo que actualiza la columna usuHabilitado en la tabla de usuarios
        * @param idUsuario
        * @param habilitar
        * @throws IntegracionException 
        */
       public void updtUsrHabilitar(Integer idUsuario, int habilitar) throws IntegracionException{
             String hql = String.format("update Usuarios set usuHabilitado = %1$s "
                     + "where usuId = %2$s ",habilitar, idUsuario);
        
        super.executeUpdate(hql);
     }
      
    public static void main(String args[]){
            HibernateUtil.buildSessionFactory2();
            
            UsuariosDao dao = new UsuariosDao();
        try {
            List<UsuarioBajaDto> usuario = dao.getUsrsBaja(3);
            System.out.println(usuario.size());
            usuario.stream().forEach((usu) -> {
                System.out.println(usu.getUsuFechaBaja()+","+usu.getUsuClaveEmpleado()+","+usu.getEmpAbreviacion());
                });  
        } catch (IntegracionException ex) {
           ex.printStackTrace();
        }
    }
    
}
