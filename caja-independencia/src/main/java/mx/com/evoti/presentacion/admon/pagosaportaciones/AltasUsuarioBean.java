/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon.pagosaportaciones;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.altasCambiosEmpresa.AltasBo;
import mx.com.evoti.bo.altasCambiosEmpresa.CambiosEmpresaBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.Empresas;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "altasBean")
@ViewScoped
public class AltasUsuarioBean extends BaseBean implements Serializable {

    private static Logger LOGGER = LoggerFactory.getLogger(AltasUsuarioBean.class);
    private static final long serialVersionUID = 5213584426058703782L;

    private AltasBo altasBo;
    private CambiosEmpresaBo cambioBo;

    private List<MovimientosDto> lstAltasCambiosDto;
    private List<MovimientosDto> lstFiltradosAltasCambios;
    private MovimientosDto altaCambioSeleccionado;

    private List<UsuarioDto> lstUsuarios;
    private List<UsuarioDto> lstUsuariosFiltrados;

    private UsuarioDto nuevoUsuario;
    private UsuarioDto usuarioSeleccionado;

    private Boolean rdrPanelNuevoUsuario;
    private Boolean rdrBtnNuevoUsuario;
    private String tamanoDlg;
    private Boolean rdrBtnCambioEmpresa;

    public AltasUsuarioBean() {
        altasBo = new AltasBo();
        cambioBo = new CambiosEmpresaBo();
    }

    /**
     * Obtiene todos los registros de movimientos que no tienen movusuid y por
     * lo tanto son considerados como pendientes
     */
    public void obtenerHistorialCambiosAltas() {
        try {
//                rdrBtnNuevoUsuario = Boolean.FALSE;
            lstAltasCambiosDto = altasBo.obtenerHistorialCambiosAltas();
        } catch (BusinessException ex) {
            muestraMensajeError("Hubo error al consultar la base de datos","", null);
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    /**
     * Obtiene la lista de usuarios que existen en base de datos
     */
    public void obtenerUsuarios() {
        try {
            rdrBtnCambioEmpresa = Boolean.TRUE;
            tamanoDlg = "500px";
            lstUsuarios = altasBo.getUsuarios();
            super.hideShowDlg("PF('dlgUsuariosTbl').show()");
        } catch (BusinessException ex) {
            muestraMensajeError("Hubo error al consultar la base de datos","", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que distribuye el nombre del usuario en los campos de la pantalla
     */
    public void inicializaAlta() {
        rdrPanelNuevoUsuario = Boolean.TRUE;
        rdrBtnCambioEmpresa = Boolean.FALSE;
        tamanoDlg = "300px";
        configuraNombre(altaCambioSeleccionado.getMovNombreEmpleado());
    }

    /**
     * Ejecuta el cambio de empresa, cuando se presiona el botón Realizar cambio de empresa
     */
    public void realizarCambioEmpresa() {
        try {
            if (usuarioSeleccionado != null) {
                Usuarios usuario = new Usuarios();
                usuario.setUsuId(usuarioSeleccionado.getId());
                usuario.setUsuClaveEmpleado(usuarioSeleccionado.getCveEmpleado());
                usuario.setEmpresas(new Empresas(usuarioSeleccionado.getIdEmpresa()));
                cambioBo.updtCambioEmpDB(usuario, altaCambioSeleccionado);
                super.hideShowDlg("PF('dlgUsuariosTbl').hide()");
            } else {
                muestraMensajeError("Debe seleccionar el usuario en la tabla","", null);
            }
       
        } catch (IntegracionException ex) {
            muestraMensajeError("Hubo error al hacer el cambio de empresa", "",null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Guarda el usuario cuando es una alta
     */
    public void guardarUsuario() {
        try {
            nuevoUsuario.setCveEmpleado(altaCambioSeleccionado.getMovClaveEmpleado());
            nuevoUsuario.setIdEmpresa(altaCambioSeleccionado.getMovEmpresa());
           
            Integer usuId = altasBo.guardarUsuario(nuevoUsuario);

            nuevoUsuario.setId(usuId);
          
            altasBo.insertaAltaHist(altaCambioSeleccionado, nuevoUsuario);

            altasBo.actualizaUsuIdEnPagosYMov(altaCambioSeleccionado.getMovFecha());
            
            rdrPanelNuevoUsuario = Boolean.FALSE;
            
            muestraMensajeExito("El usuario se guardó correctamente","",null);
        } catch (BusinessException ex) {
            muestraMensajeError("Hubo error al guardar el usuario","", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void cancelar() {
        this.rdrPanelNuevoUsuario = Boolean.FALSE;
    }

    /**
     * Acomoda el nombre del usuario en los campos Nombre, Paterno, Materno en
     * la pantalla para facilitar el guardado
     *
     * @param nombreCompleto
     */
    public void configuraNombre(String nombreCompleto) {
        nuevoUsuario = new UsuarioDto();
        String[] arrPalabras = nombreCompleto.split(" ", 10);

        int noPalabras = arrPalabras.length;

        if (noPalabras == 3) {
            nuevoUsuario.setPaterno(arrPalabras[0].toUpperCase());
            nuevoUsuario.setMaterno(arrPalabras[1].toUpperCase());
            nuevoUsuario.setNombre(arrPalabras[2].toUpperCase());
            nuevoUsuario.setPassword(arrPalabras[0].toUpperCase());
            /**
             * Si el nombre tiene más de 3 palabras
             */
        } else if (noPalabras > 3) {
           
            String nombre = "";
            for (int i = 2; i < arrPalabras.length; i++) {
                /**
                 * Si alguna de las palabras tiene 3 o menos letras se atribuye
                 * a que es apellido y se pone la variable masDe1Nombre como negativo
                 */
                nombre += arrPalabras[i] + " ";
            }

            nuevoUsuario.setPaterno(arrPalabras[0].toUpperCase());
            nuevoUsuario.setMaterno(arrPalabras[1].toUpperCase());

            nuevoUsuario.setNombre(nombre.toUpperCase());
            nuevoUsuario.setPassword(arrPalabras[0].toUpperCase());

        }

    }

    /**
     * @return the lstAltasCambiosDto
     */
    public List<MovimientosDto> getLstAltasCambiosDto() {
        return lstAltasCambiosDto;
    }

    /**
     * @param lstAltasCambiosDto the lstAltasCambiosDto to set
     */
    public void setLstAltasCambiosDto(List<MovimientosDto> lstAltasCambiosDto) {
        this.lstAltasCambiosDto = lstAltasCambiosDto;
    }

    /**
     * @return the rdrPanelNuevoUsuario
     */
    public Boolean getRdrPanelNuevoUsuario() {
        return rdrPanelNuevoUsuario;
    }

    /**
     * @param rdrPanelNuevoUsuario the rdrPanelNuevoUsuario to set
     */
    public void setRdrPanelNuevoUsuario(Boolean rdrPanelNuevoUsuario) {
        this.rdrPanelNuevoUsuario = rdrPanelNuevoUsuario;
    }

    /**
     * @return the rdrBtnNuevoUsuario
     */
    public Boolean getRdrBtnNuevoUsuario() {
        return rdrBtnNuevoUsuario;
    }

    /**
     * @param rdrBtnNuevoUsuario the rdrBtnNuevoUsuario to set
     */
    public void setRdrBtnNuevoUsuario(Boolean rdrBtnNuevoUsuario) {
        this.rdrBtnNuevoUsuario = rdrBtnNuevoUsuario;
    }

    /**
     * @return the lstFiltradosAltasCambios
     */
    public List<MovimientosDto> getLstFiltradosAltasCambios() {
        return lstFiltradosAltasCambios;
    }

    /**
     * @param lstFiltradosAltasCambios the lstFiltradosAltasCambios to set
     */
    public void setLstFiltradosAltasCambios(List<MovimientosDto> lstFiltradosAltasCambios) {
        this.lstFiltradosAltasCambios = lstFiltradosAltasCambios;
    }

    /**
     * @return the altaCambioSeleccionado
     */
    public MovimientosDto getAltaCambioSeleccionado() {
        return altaCambioSeleccionado;
    }

    /**
     * @param altaCambioSeleccionado the altaCambioSeleccionado to set
     */
    public void setAltaCambioSeleccionado(MovimientosDto altaCambioSeleccionado) {
        this.altaCambioSeleccionado = altaCambioSeleccionado;
    }

    /**
     * @return the lstUsuarios
     */
    public List<UsuarioDto> getLstUsuarios() {
        return lstUsuarios;
    }

    /**
     * @param lstUsuarios the lstUsuarios to set
     */
    public void setLstUsuarios(List<UsuarioDto> lstUsuarios) {
        this.lstUsuarios = lstUsuarios;
    }

    /**
     * @return the lstUsuariosFiltrados
     */
    public List<UsuarioDto> getLstUsuariosFiltrados() {
        return lstUsuariosFiltrados;
    }

    /**
     * @param lstUsuariosFiltrados the lstUsuariosFiltrados to set
     */
    public void setLstUsuariosFiltrados(List<UsuarioDto> lstUsuariosFiltrados) {
        this.lstUsuariosFiltrados = lstUsuariosFiltrados;
    }

    /**
     * @return the nuevoUsuario
     */
    public UsuarioDto getNuevoUsuario() {
        return nuevoUsuario;
    }

    /**
     * @param nuevoUsuario the nuevoUsuario to set
     */
    public void setNuevoUsuario(UsuarioDto nuevoUsuario) {
        this.nuevoUsuario = nuevoUsuario;
    }

    /**
     * @return the usuarioSeleccionado
     */
    public UsuarioDto getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    /**
     * @param usuarioSeleccionado the usuarioSeleccionado to set
     */
    public void setUsuarioSeleccionado(UsuarioDto usuarioSeleccionado) {
        this.usuarioSeleccionado = usuarioSeleccionado;
    }

    /**
     * @return the tamanoDlg
     */
    public String getTamanoDlg() {
        return tamanoDlg;
    }

    /**
     * @param tamanoDlg the tamanoDlg to set
     */
    public void setTamanoDlg(String tamanoDlg) {
        this.tamanoDlg = tamanoDlg;
    }

    /**
     * @return the rdrBtnCambioEmpresa
     */
    public Boolean getRdrBtnCambioEmpresa() {
        return rdrBtnCambioEmpresa;
    }

    /**
     * @param rdrBtnCambioEmpresa the rdrBtnCambioEmpresa to set
     */
    public void setRdrBtnCambioEmpresa(Boolean rdrBtnCambioEmpresa) {
        this.rdrBtnCambioEmpresa = rdrBtnCambioEmpresa;
    }

}
