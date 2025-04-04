/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.common;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.administrador.algoritmopagos.BusquedaEmpleadoBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.EmpresasDto;
import mx.com.evoti.hibernate.pojos.Usuarios;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "busqEmplBean")
@ViewScoped
public class BusquedaEmpleadoBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BusquedaEmpleadoBean.class);
    private static final long serialVersionUID = 8976611548153084255L;

    private BusquedaEmpleadoBo busqEmpBo;

    private List<Usuarios> usuarios;
    private Usuarios usuario;
    private List<EmpresasDto> empresas;
    private EmpresasDto empresa;
    private Integer claveEmpleado;

    public BusquedaEmpleadoBean() {
        busqEmpBo = new BusquedaEmpleadoBo();
    }

    public void init() {
        try {
            //obtener las empresas
            empresas = busqEmpBo.getEmpresasDto();
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    
    /**
     * Obtiene el empleado
     */
    public void buscaEmpleado() {
        try {

            if (claveEmpleado != null && empresa != null) {
                usuarios = busqEmpBo.getUsuarioXCveYEmpresa(claveEmpleado, empresa.getEmpId());
                
                if(!usuarios.isEmpty()){
                    usuario = usuarios.get(0);
                }
                
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the empresas
     */
    public List<EmpresasDto> getEmpresas() {
        return empresas;
    }

    /**
     * @param empresas the empresas to set
     */
    public void setEmpresas(List<EmpresasDto> empresas) {
        this.empresas = empresas;
    }

    /**
     * @return the empresa
     */
    public EmpresasDto getEmpresa() {
        return empresa;
    }

    /**
     * @param empresa the empresa to set
     */
    public void setEmpresa(EmpresasDto empresa) {
        this.empresa = empresa;
    }

    /**
     * @return the claveEmpleado
     */
    public Integer getClaveEmpleado() {
        return claveEmpleado;
    }

    /**
     * @param claveEmpleado the claveEmpleado to set
     */
    public void setClaveEmpleado(Integer claveEmpleado) {
        this.claveEmpleado = claveEmpleado;
    }

    /**
     * @return the usuarios
     */
    public List<Usuarios> getUsuarios() {
        return usuarios;
    }

    /**
     * @param usuarios the usuarios to set
     */
    public void setUsuarios(List<Usuarios> usuarios) {
        this.usuarios = usuarios;
    }

    /**
     * @return the usuario
     */
    public Usuarios getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }


}
