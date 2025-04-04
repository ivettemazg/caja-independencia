/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.finiquito;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpSession;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.administrador.finiquito.FiniquitosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.EmpresasDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.EmpresasDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.hibernate.pojos.BajaEmpleados;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.NavigationBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "baeBean")
@ViewScoped
public class BajaEmpleadoBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 2143872566991683216L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BajaEmpleadoBean.class);

    public FiniquitosBo finBo;
    private EmpresasDao empDao;
    private CreditosBo creditoBo;
    private MovimientosBo movsBo;

    private Usuarios usuarioBaja;
    private List<Usuarios> usuariosBaja;
    private List<EmpresasDto> empresas;
    private EmpresasDto empresa;
    private Integer claveEmpleado;

    private Integer empresaId;
    private String nombreCompleto;
    private Date fechaBaja;
    private Boolean rdrDatosBajaEmpleado;

    @ManagedProperty("#{navigationController}")
    private NavigationBean navigationBean;

    @ManagedProperty("#{finBean}")
    private FiniquitoBean finBean;

    public BajaEmpleadoBean() {
        finBo = new FiniquitosBo();
        empDao = new EmpresasDao();
        creditoBo = new CreditosBo();
        movsBo = new MovimientosBo();
    }

    public void init() {
        try {
            empresas = empDao.getEmpresasDto();
            rdrDatosBajaEmpleado = Boolean.FALSE;
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Busca un usuario por clave de empleado y empresa
     */
    public void buscarUsuario() {
        try {

            if (claveEmpleado != null && empresa != null) {
                usuariosBaja = finBo.buscaUsuario(claveEmpleado, empresa.getEmpId());

                /**
                 * cuando encuentra al menos un usuario
                 */
                if (usuariosBaja.size() == 1) {
                    usuarioBaja = usuariosBaja.get(0);

                    //Si se cumple es porque el usuario ya se ha dado de baja anteriormente
                    if (usuarioBaja.getUsuFechaBaja() != null && usuarioBaja.getUsuEstatus() == 0) {
                        nombreCompleto = "";
                        rdrDatosBajaEmpleado = Boolean.FALSE;
                        super.muestraMensajeError("Ese número de empleado corresponde a un usuario dado de baja con anterioridad", "", "msjBajaInicial");

                    } else {
                        nombreCompleto = usuarioBaja.getUsuNombre().concat(" " + usuarioBaja.getUsuPaterno().concat(" " + usuarioBaja.getUsuMaterno()));
                        rdrDatosBajaEmpleado = Boolean.TRUE;

                    }

                    /**
                     * Cuando encuentra mas de un usuario
                     */
                } else if (usuariosBaja.size() > 1) {

                    usuariosBaja.stream().forEach((usu) -> {
                        usu.setUsuNombre(usu.getUsuNombre() + " " + usu.getUsuPaterno() + " " + usu.getUsuMaterno());
                    });

                    rdrDatosBajaEmpleado = Boolean.FALSE;
                    nombreCompleto = "";
                    super.hideShowDlg("PF('dlgEligeUsrW').show()");
                    /**
                     * Cuando no encuentra el usuario
                     */
                } else {
                    rdrDatosBajaEmpleado = Boolean.FALSE;
                    nombreCompleto = "";
                    super.muestraMensajeError("El número de usuario no existe o no corresponde a esa empresa", "", "msjBajaInicial");
                }
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void seleccionaUsuario() {
        rdrDatosBajaEmpleado = Boolean.TRUE;
        nombreCompleto = usuarioBaja.getUsuNombre() + " " + usuarioBaja.getUsuPaterno() + " " + usuarioBaja.getUsuMaterno();
    }

    /**
     * Actualiza el estatus de un usuario a 0 - Baja
     */
    public void darDeBaja() {

        try {

            /**
             * *
             * Genera baja
             */
            BajaEmpleados baja = new BajaEmpleados();
            baja.setBaeFechaBaja(this.fechaBaja);
            baja.setBaeIdEmpleado(usuarioBaja.getUsuId());

            List<DetalleCreditoDto> creditos = creditoBo.obtCreditosDetalle(usuarioBaja);
            List<MovimientosDto> ahorros = movsBo.getAhorrosByUsuId(usuarioBaja.getUsuId());

            Double saldoTotal = 0.0;
            Double totalAhorro = 0.0;
            for (DetalleCreditoDto cred : creditos) {
                saldoTotal += cred.getSaldoTotal();
            }

            for (MovimientosDto mov : ahorros) {
                totalAhorro += mov.getTotalMovimiento();
            }

            //Cuando el saldo del credito es mayor a 10
            if (saldoTotal >= 5) {
                baja.setBaeEstatus(Constantes.BAJA_PENDIENTE);
            }

            //Si ya no tiene saldo en credito pero si tiene ahorro
            if (saldoTotal <= 5 && totalAhorro >= 5) {
                baja.setBaeEstatus(Constantes.BAJA_AHORROSXDEVOLVER);
            }
            
            //Cuando no tiene saldo en credito ni en ahorro
            if(saldoTotal <= 5 && totalAhorro <= 5){
                baja.setBaeEstatus(Constantes.BAJA_COMPLETADA);
            }
            

            baja.setBaeDeudaCreditos(Util.round(saldoTotal));
            baja.setBaeAhorros(Util.round(totalAhorro));

            finBo.insertBajaEmpleado(baja);

            usuarioBaja.setUsuEstatus(Constantes.USU_BAJA_0);
            usuarioBaja.setUsuFechaBaja(this.fechaBaja);

            finBo.updtEstatusUsuario(usuarioBaja);

            super.muestraMensajeExito("El usuario fue dado de baja", "", "msjDadoBaja");
            super.hideShowDlg("PF('dlgMessageExito').show()");
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void goToFiniquito() {
        HttpSession session = super.getSession();
        session.setAttribute("usuBajaId", usuarioBaja.getUsuId());
        navigationBean.goToFiniquito();

    }

    /**
     * @return the usuarioBaja
     */
    public Usuarios getUsuarioBaja() {
        return usuarioBaja;
    }

    /**
     * @param usuarioBaja the usuarioBaja to set
     */
    public void setUsuarioBaja(Usuarios usuarioBaja) {
        this.usuarioBaja = usuarioBaja;
    }

    /**
     * @return the usuariosBaja
     */
    public List<Usuarios> getUsuariosBaja() {
        return usuariosBaja;
    }

    /**
     * @param usuariosBaja the usuariosBaja to set
     */
    public void setUsuariosBaja(List<Usuarios> usuariosBaja) {
        this.usuariosBaja = usuariosBaja;
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
     * @return the rdrDatosBajaEmpleado
     */
    public Boolean getRdrDatosBajaEmpleado() {
        return rdrDatosBajaEmpleado;
    }

    /**
     * @param rdrDatosBajaEmpleado the rdrDatosBajaEmpleado to set
     */
    public void setRdrDatosBajaEmpleado(Boolean rdrDatosBajaEmpleado) {
        this.rdrDatosBajaEmpleado = rdrDatosBajaEmpleado;
    }

    /**
     * @return the nombreCompleto
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * @param nombreCompleto the nombreCompleto to set
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    /**
     * @return the fechaBaja
     */
    public Date getFechaBaja() {
        return fechaBaja;
    }

    /**
     * @param fechaBaja the fechaBaja to set
     */
    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    /**
     * @return the empresa
     */
    public Integer getEmpresaId() {
        return empresaId;
    }

    /**
     * @param empresaId
     */
    public void setEmpresaId(Integer empresaId) {
        this.empresaId = empresaId;
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
     * @return the navigationBean
     */
    public NavigationBean getNavigationBean() {
        return navigationBean;
    }

    /**
     * @param navigationBean the navigationBean to set
     */
    public void setNavigationBean(NavigationBean navigationBean) {
        this.navigationBean = navigationBean;
    }

    /**
     * @return the finBean
     */
    public FiniquitoBean getFinBean() {
        return finBean;
    }

    /**
     * @param finBean the finBean to set
     */
    public void setFinBean(FiniquitoBean finBean) {
        this.finBean = finBean;
    }

}
