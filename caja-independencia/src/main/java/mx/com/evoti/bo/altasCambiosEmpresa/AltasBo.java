/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.altasCambiosEmpresa;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.altasCambiosEmp.AltasUsuarioDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.AltasCambiosHist;
import mx.com.evoti.hibernate.pojos.Empresas;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
public class AltasBo implements Serializable {

    private static final long serialVersionUID = -1853426770257756832L;

    private Logger logger = LoggerFactory.getLogger(AltasBo.class);
    private AltasUsuarioDao altasDao;

    public AltasBo() {
        altasDao = new AltasUsuarioDao();

    }

    /**
     * Obtiene el listado de registros de nuevos usuarios y cambios pendientes
     *
     * @return
     * @throws BusinessException
     */
    public List<MovimientosDto> obtenerHistorialCambiosAltas() throws BusinessException {
        try {
            List<MovimientosDto> altasCambios = altasDao.obtenerHistorialCambiosAltas();
            return altasCambios;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    /**
     * Obtiene todoslos usuarios de la base de datos
     *
     * @return
     * @throws BusinessException
     */
    public List<UsuarioDto> getUsuarios() throws BusinessException {
        try {
            return altasDao.getUsuarios();
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Guarda el usuario cuando es un nuevo usuario
     *
     * @param usuario
     * @return 
     * @throws BusinessException
     */
    public Integer guardarUsuario(UsuarioDto usuario) throws BusinessException {
        try {
            Usuarios pojo = transformDtoToUsuario(usuario);

            Integer id = altasDao.guardarUsuario(pojo);
           
            return id;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
    
    
    public void actualizaUsuIdEnPagosYMov(Date catorcena) throws BusinessException{
        try {
            this.altasDao.updateUsuIdMov(catorcena);
            this.altasDao.updateUsuIdPago(catorcena);
        } catch (IntegracionException ex) {
             throw new BusinessException(ex.getMessage(), ex);
        }
    }

    private Usuarios transformDtoToUsuario(UsuarioDto dto) {
        Usuarios usuario = new Usuarios();
        Empresas empresa = new Empresas(dto.getIdEmpresa());

        usuario.setEmpresas(empresa);
        usuario.setUsuClaveEmpleado(dto.getCveEmpleado());
        usuario.setUsuPrimeravez(1);
        usuario.setUsuFechaIngreso(new Date());
        usuario.setUsuNombre(dto.getNombre());
        usuario.setUsuPaterno(dto.getPaterno());
        usuario.setUsuMaterno(dto.getMaterno());
        usuario.setUsuPassword(dto.getPaterno());
        usuario.setUsuRol(Constantes.ROL_USR_USR_I);
        usuario.setUsuHabilitado(1);
        usuario.setUsuEstatus(1);
        usuario.setUsuOmitirValidaciones(0);
        return usuario;
    }

    public void insertaAltaHist( MovimientosDto movimiento, UsuarioDto usuario) throws BusinessException {
        try {
            AltasCambiosHist achDto = new AltasCambiosHist();

            achDto.setCnhArhId(movimiento.getMovArhId());
            achDto.setCnhCatorcenaTransaccion(movimiento.getMovFecha());
            achDto.setCnhClaveActual(movimiento.getMovClaveEmpleado());
            achDto.setCnhEmpresaActual(movimiento.getMovEmpresa());
            achDto.setCnhFecha(new Date());
            achDto.setCnhTipo(Constantes.CNH_NUEVO);
            achDto.setCnhUsuId(usuario.getId());
            altasDao.insertAltaCambioHist(achDto);

        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
//        AltasBo bo = new AltasBo();
//        try {
//            bo.procesoAltasYCambios(Util.generaFechaDeString("2014-10-02"));
////            System.out.println(bo.obtenerTotalPendientes("2014-10-02"));
//        } catch (BusinessException ex) {
//            ex.printStackTrace();
//        }
    }

}
