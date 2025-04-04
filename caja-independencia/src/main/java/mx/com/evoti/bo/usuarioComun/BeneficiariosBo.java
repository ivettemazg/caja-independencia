/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.usuarioComun;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.BeneficiariosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.usuariocomun.BeneficiariosDto;
import mx.com.evoti.hibernate.pojos.Beneficiarios;
import mx.com.evoti.hibernate.pojos.ParentescoBen;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class BeneficiariosBo implements Serializable{
    
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BeneficiariosBo.class);
    private static final long serialVersionUID = 5078363761875360035L;

    BeneficiariosDao dao;
    
    public BeneficiariosBo() {
        
        dao = new BeneficiariosDao();
    }
    
    
     /**
     * Obtiene los beneficiarios de un usuario en especifico
     * @param idEmpleado
     * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException 
     */
    public List<BeneficiariosDto> getBeneficiariosByEmpleado(Integer idEmpleado) throws BusinessException {
        try {
            List<Beneficiarios> beneficiarios  =dao.getBeneficiariosByEmpleado(idEmpleado);
            List<BeneficiariosDto> benefDtos = new ArrayList<>();
            
            beneficiarios.forEach(x ->            
                    benefDtos.add(transformPojoToDto(x)));
            
            return benefDtos;
            
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
    /**
     * Guarda o actualiza un beneficiario
     * @param beneficiario
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     *  
     */
     public BeneficiariosDto saveOrUpdate(BeneficiariosDto beneficiario) throws BusinessException {
         try {
             
             Beneficiarios pojo = dao.saveOrUpdate(this.transformDtoToPojo(beneficiario));
             
             return transformPojoToDto(pojo);
         } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
         }
    }
    
    /**
     * Borra un beneficiario de la base de datos
     * @param idBeneficiario 
     * @throws mx.com.evoti.bo.exception.BusinessException 
     */
     public void borraBeneficiario(Integer idBeneficiario) throws BusinessException{
         try {
             dao.borraBeneficiario(idBeneficiario);
         } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
         }
     }
     
     /**
      * Obtiene la lista de parentescos de la base de datos
      * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
      */
     public List<ParentescoBen> getParentescos() throws BusinessException {
         try {
             return dao.getParentescos();
         } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
         }
     }
     
     /**
      * Obtiene un parentesco por id
      * @param idParentesco
      * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException 
      */
     public ParentescoBen getParentescoById(Integer idParentesco) throws BusinessException {
         try {
             return dao.getParentescoById(idParentesco);
         } catch (IntegracionException ex) {
             throw new BusinessException(ex.getMessage(), ex);
         }
     }
    
     /**
      * Transforma un dto en un pojo
      * @param pojo
      * @return 
      */
     public BeneficiariosDto transformPojoToDto(Beneficiarios pojo) {
         try {
             BeneficiariosDto dto = new BeneficiariosDto();
             dto.setBenId(pojo.getBenId());
             dto.setBenCelular(pojo.getBenCelular());
             dto.setBenDireccion(pojo.getBenDireccion());
             dto.setBenNombre(pojo.getBenNombre());
             dto.setBenMaterno(pojo.getBenMaterno());
             dto.setBenPaterno(pojo.getBenPaterno());
             ParentescoBen parentesco = getParentescoById(pojo.getBenParentesco());
             dto.setParentesco(parentesco);
             dto.setBenPct(pojo.getBenPct());
             dto.setBenTelefono(pojo.getBenTelefono());
             dto.setBenUsuId(pojo.getBenUsuId());
             
             return dto;
         } catch (BusinessException ex) {
             LOGGER.error(ex.getMessage(), ex);
         }
         return null;
     }
     
     /**
      * Transforma un pojo en un dto
      * @param dto
      * @return
      * @throws BusinessException 
      */
     public Beneficiarios transformDtoToPojo(BeneficiariosDto dto) throws BusinessException{
         Beneficiarios pojo = new Beneficiarios();
         pojo.setBenUsuId(dto.getBenUsuId());
         pojo.setBenId(dto.getBenId());
         pojo.setBenCelular(dto.getBenCelular());
         pojo.setBenDireccion(dto.getBenDireccion());
         pojo.setBenNombre(dto.getBenNombre());
         pojo.setBenMaterno(dto.getBenMaterno());
         pojo.setBenPaterno(dto.getBenPaterno());
         pojo.setBenParentesco(dto.getBenParentesco());
         pojo.setBenPct(dto.getBenPct());
         pojo.setBenTelefono(dto.getBenTelefono());
        
         return pojo;
     }
    
    
}
