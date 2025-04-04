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
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Venus
 */
public class LoginDao extends ManagerDB implements Serializable{
    private static final long serialVersionUID = -4797722718851766181L;
    
      public List<UsuarioDto> login(String nickname) throws IntegracionException {
           try{
                super.beginTransaction();
          
               SQLQuery query = session.createSQLQuery(String.format("select usu_id as id, "
                       + "usu_clave_empleado as cveEmpleado, usu_nombre as nombre, usu_paterno as paterno, "
                       + "usu_materno as materno, usu_fecha_ingreso as fechaIngresoCaja,"
                       + "usu_fecha_ingreso_empresa as fechaIngresoEmpresa, usu_correo as correo, "
                       + "usu_empresa as empresaId, usu_password as password, usu_habilitado as habilitado,"
                       + "usu_omitir_validaciones as omitirValidaciones, "
                       + "usu_salario_neto as salarioNeto, rol_nombre as rol,"
                       + "rol_id as rolId, usu_primeravez as primeraVez, "
                       + "usu_fecha_baja as fechaBaja "
                       + "from usuarios inner join roles on rol_id = usu_rol "
                       + "where usu_clave_empleado = '%1$s'" , 
                    nickname));
                  
                 List<UsuarioDto> usuario = query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).list();
                 return usuario;
          }catch(Exception he){
              throw new IntegracionException(he);
          }finally{
            super.endTransaction();
        }
       
    }

      
    
}
