/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.creditos;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.CatorcenasBo;
import mx.com.evoti.bo.administrador.algoritmopagos.BusquedaEmpleadoBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.SolicitudBo;
import mx.com.evoti.dto.EmpresasDto;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.common.AmortizacionBean;
import mx.com.evoti.util.Constantes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "credSegAuBean")
@ViewScoped
public class CreditoSeguroAutoBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CreditoSeguroAutoBean.class);
    private static final long serialVersionUID = -8912693400907439151L;

    private Integer claveEmpleado;
    private List<EmpresasDto> empresas;
    private EmpresasDto empresa;
    private List<Usuarios> usuarios;
    private Usuarios usuario;

    private int iTipoSolicitud;

    private Double montoSolicitado;
    private Integer faCatorcena;
    private String banco;
    private String clabeInterbancaria;
    private String referencia;
    private String aseguradora;
    private String noPoliza;
    private Boolean rdrEnableJulio;
    private Boolean rdrEnableDiciembre;
    private Boolean rdrPnlSimulador;
    private Boolean dsblBtnCrear;

    @ManagedProperty("#{amortizacionBean}")
    private AmortizacionBean amortizacionBean;

    private final SolicitudBo solBo;
    private BusquedaEmpleadoBo busqEmpBo;
    private final CatorcenasBo catBo;

    private boolean julioSigAno;
    private Boolean rdrMuestrScrCred;

    public CreditoSeguroAutoBean() {

        solBo = new SolicitudBo();
        busqEmpBo = new BusquedaEmpleadoBo();
        catBo = new CatorcenasBo();
        rdrPnlSimulador = false;

    }

    public void init() {
        if(super.validateUser()){
            try {
                rdrMuestrScrCred = Boolean.FALSE;
                dsblBtnCrear = Boolean.TRUE;
                iTipoSolicitud = Constantes.SAU;
                empresas = busqEmpBo.getEmpresasDto();
                configuraSolCredAu();
            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
         }
    }

    /**
     * Obtiene el empleado de quien se está consultando
     */
    public void buscaEmpleado() {
        try {

                limpiaCampos();
            if (claveEmpleado != null && empresa != null) {
                usuarios = busqEmpBo.getUsuarioXCveYEmpresa(claveEmpleado, empresa.getEmpId());

                //Cuando encuentra el usuario
                if (!usuarios.isEmpty()) {
                    usuario = usuarios.get(0);

                    if (usuario.getUsuEstatus() == 1) {
                        LOGGER.info("Usuario activo");
                        rdrMuestrScrCred = Boolean.TRUE;
                    } else {
                        super.muestraMensajeError("El usuario que ha seleccionado está dado de baja", "", "msjBusqueda");
                    }

                } else {
                  
                    super.muestraMensajeError("El empleado no se encuentra la base", "", "msjBusqueda");
                }

            }
        } catch (BusinessException ex) {
         
            super.muestraMensajeError("Hubo un error al consultar el empleado, por favor"
                    + " vuelva a intentarlo", "", "msjBusqueda");

            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que crea la solicitud y la guarda si pasa las validaciones de
     * monto del prestamo. Cuando se guarda exitosamente y pasa las
     * validaciones, se redirecciona al detalle de la solicitud para que el
     * usuario prosiga con el envío de documentos, el complemento de la
     * información bancaria y eleccion de los avales
     */
    public void creaSolicitud() {
        LOGGER.debug("Dentro de creaSolicitud()");

        try {

            Double pagoCatorcenal = amortizacionBean.getAmortizacion() != null ? amortizacionBean.getAmortizacion().get(0).getAmoMontoPago() : 0.0;
            Double interesTotal = amortizacionBean.getMontoTotalInteres() != null ? amortizacionBean.getMontoTotalInteres() : 0.0;
           

            solBo.creaSolSegAuto(usuario,montoSolicitado,faCatorcena,banco,clabeInterbancaria,noPoliza,referencia,aseguradora,interesTotal,pagoCatorcenal,amortizacionBean.getUltimaFechaFaAG());
            
            limpiaCampos();
            super.muestraMensajeExito("La solicitud fue guardada exitosamente", "", "msjBusqueda");
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un problema al guardar la solicitud", "", "msjBusqueda");
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    /**
     * Renderea los campos de pantalla que se utilizan cuando se solicita
     * credito de FA o AG
     */
    private void configuraSolCredAu() throws BusinessException {
          Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);

        Date catMayo = catBo.getPrimerCatorcenaMesAno(year, 5);
        Date catNoviembre = catBo.getPrimerCatorcenaMesAno(year, 11);
        //Cuando se está solicitando antes de la primer catorcena de mayo del año en curso

        rdrEnableJulio = true;
        rdrEnableDiciembre = true;
        julioSigAno = Boolean.TRUE;

        if (date.after(catMayo)) {
            rdrEnableJulio =  Boolean.FALSE;
            julioSigAno = Boolean.FALSE;
        }

        if (date.after(catNoviembre)) {
            rdrEnableDiciembre = Boolean.FALSE;
        }

    }

    /**
     * Calcula la amortización en base a los campos llenos
     */
    public void aplicaCalcular() {
        LOGGER.info("Dentro de aplicaCalcular");

        try {
            this.amortizacionBean.generaAmortizacion(this.iTipoSolicitud, this.montoSolicitado, null,
                    this.faCatorcena, this.julioSigAno);
           dsblBtnCrear = Boolean.FALSE;
           rdrPnlSimulador=Boolean.TRUE;
        } catch (BusinessException ex) {
            LOGGER.error("Error al calcular la amortizacion del simulador", ex);
        }

    }

    public void limpiaCampos() {

        this.amortizacionBean.setAmortizacion(null);
        this.amortizacionBean.setMontoTotalAmortizacion(null);
        this.amortizacionBean.setMontoTotalInteres(null);
        this.amortizacionBean.setMontoTotalPagar(null);
        this.montoSolicitado = null;
        this.clabeInterbancaria = "";
        this.referencia = "";
        this.banco="";
        this.aseguradora ="";
        this.noPoliza="";
        dsblBtnCrear = Boolean.TRUE;
        rdrPnlSimulador=Boolean.FALSE;

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

    /**
     * @return the iTipoSolicitud
     */
    public int getiTipoSolicitud() {
        return iTipoSolicitud;
    }

    /**
     * @param iTipoSolicitud the iTipoSolicitud to set
     */
    public void setiTipoSolicitud(int iTipoSolicitud) {
        this.iTipoSolicitud = iTipoSolicitud;
    }

    /**
     * @return the montoSolicitado
     */
    public Double getMontoSolicitado() {
        return montoSolicitado;
    }

    /**
     * @param montoSolicitado the montoSolicitado to set
     */
    public void setMontoSolicitado(Double montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    /**
     * @return the faCatorcena
     */
    public Integer getFaCatorcena() {
        return faCatorcena;
    }

    /**
     * @param faCatorcena the faCatorcena to set
     */
    public void setFaCatorcena(Integer faCatorcena) {
        this.faCatorcena = faCatorcena;
    }

    /**
     * @return the banco
     */
    public String getBanco() {
        return banco;
    }

    /**
     * @param banco the banco to set
     */
    public void setBanco(String banco) {
        this.banco = banco;
    }

    /**
     * @return the clabeInterbancaria
     */
    public String getClabeInterbancaria() {
        return clabeInterbancaria;
    }

    /**
     * @param clabeInterbancaria the clabeInterbancaria to set
     */
    public void setClabeInterbancaria(String clabeInterbancaria) {
        this.clabeInterbancaria = clabeInterbancaria;
    }

    /**
     * @return the referencia
     */
    public String getReferencia() {
        return referencia;
    }

    /**
     * @param referencia the referencia to set
     */
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    /**
     * @return the aseguradora
     */
    public String getAseguradora() {
        return aseguradora;
    }

    /**
     * @param aseguradora the aseguradora to set
     */
    public void setAseguradora(String aseguradora) {
        this.aseguradora = aseguradora;
    }

    /**
     * @return the noPoliza
     */
    public String getNoPoliza() {
        return noPoliza;
    }

    /**
     * @param noPoliza the noPoliza to set
     */
    public void setNoPoliza(String noPoliza) {
        this.noPoliza = noPoliza;
    }

    /**
     * @return the rdrEnableJulio
     */
    public Boolean getRdrEnableJulio() {
        return rdrEnableJulio;
    }

    /**
     * @param rdrEnableJulio the rdrEnableJulio to set
     */
    public void setRdrEnableJulio(Boolean rdrEnableJulio) {
        this.rdrEnableJulio = rdrEnableJulio;
    }

    /**
     * @return the rdrEnableDiciembre
     */
    public Boolean getRdrEnableDiciembre() {
        return rdrEnableDiciembre;
    }

    /**
     * @param rdrEnableDiciembre the rdrEnableDiciembre to set
     */
    public void setRdrEnableDiciembre(Boolean rdrEnableDiciembre) {
        this.rdrEnableDiciembre = rdrEnableDiciembre;
    }

    /**
     * @return the rdrPnlSimulador
     */
    public Boolean getRdrPnlSimulador() {
        return rdrPnlSimulador;
    }

    /**
     * @param rdrPnlSimulador the rdrPnlSimulador to set
     */
    public void setRdrPnlSimulador(Boolean rdrPnlSimulador) {
        this.rdrPnlSimulador = rdrPnlSimulador;
    }

    /**
     * @return the amortizacionBean
     */
    public AmortizacionBean getAmortizacionBean() {
        return amortizacionBean;
    }

    /**
     * @param amortizacionBean the amortizacionBean to set
     */
    public void setAmortizacionBean(AmortizacionBean amortizacionBean) {
        this.amortizacionBean = amortizacionBean;
    }

    /**
     * @return the julioSigAno
     */
    public boolean isJulioSigAno() {
        return julioSigAno;
    }

    /**
     * @param julioSigAno the julioSigAno to set
     */
    public void setJulioSigAno(boolean julioSigAno) {
        this.julioSigAno = julioSigAno;
    }

    /**
     * @return the rdrMuestrScrCred
     */
    public Boolean getRdrMuestrScrCred() {
        return rdrMuestrScrCred;
    }

    /**
     * @param rdrMuestrScrCred the rdrMuestrScrCred to set
     */
    public void setRdrMuestrScrCred(Boolean rdrMuestrScrCred) {
        this.rdrMuestrScrCred = rdrMuestrScrCred;
    }

    /**
     * @return the dsblBtnCrear
     */
    public Boolean getDsblBtnCrear() {
        return dsblBtnCrear;
    }

    /**
     * @param dsblBtnCrear the dsblBtnCrear to set
     */
    public void setDsblBtnCrear(Boolean dsblBtnCrear) {
        this.dsblBtnCrear = dsblBtnCrear;
    }

}
