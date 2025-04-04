/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.administrador.algoritmopagos;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.EmpresasDao;
import mx.com.evoti.dao.UsuariosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.EmpresasDto;
import mx.com.evoti.hibernate.pojos.Usuarios;

/**
 *
 * @author Venette
 */
public class BusquedaEmpleadoBo {
    
    private EmpresasDao empDao;
    private UsuariosDao usuDao;

    public BusquedaEmpleadoBo() {
         empDao = new EmpresasDao();
         usuDao = new UsuariosDao();
    }
    
    /**
     * Obtiene la lista de empresas
     * @return
     * @throws BusinessException 
     */
    public List<EmpresasDto> getEmpresasDto() throws BusinessException{
        try {
            return empDao.getEmpresasDto();
        } catch (IntegracionException ex) {
           throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
      /**
      * Metodo que se utiliza en todos los filtros en que se manda
      * clve de empleado y empresa
      * @param claveEmpleado
      * @param idEmpresa
      * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException 
      */
    public List<Usuarios> getUsuarioXCveYEmpresa(Integer claveEmpleado, Integer idEmpresa) throws BusinessException{
        
        try {
           return usuDao.getUsuarioXCveYEmpresa(claveEmpleado, idEmpresa);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
        
    }
    
    /**
     * Habilita o deshabilita a un usuario para pedir creditos
     * @param idUsuario
     * @param habilitar
     * @throws BusinessException 
     */
    public void updtUsrHabilitar(Integer idUsuario, Integer habilitar) throws BusinessException{
        try {
            usuDao.updtUsrHabilitar(idUsuario, habilitar);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Habilita o deshabilita para la parte de validaciones
     * @param idUsuario
     * @param habilitar
     * @throws BusinessException 
     */
    public void updtUsrOmitirVals(Integer idUsuario, Integer omitir) throws BusinessException{
        try {
            usuDao.updtUsrOmitirValidaciones(idUsuario, omitir);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
    
}
