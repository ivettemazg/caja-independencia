/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.usuariocomun;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import mx.com.evoti.bo.CatorcenasBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.SolicitudBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.NavigationBean;
import mx.com.evoti.presentacion.common.AmortizacionBean;
import mx.com.evoti.util.Constantes;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "solCreditoBean")
@ViewScoped
public class SolicitudCreditoBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SolicitudCreditoBean.class);
    private static final long serialVersionUID = -8912693400907439151L;

    private String tipoSolicitud;
    private int iTipoSolicitud;
    private boolean rdrPnlSimulador;
    private boolean rendererSolFondoAhorro;
    private boolean rendererSolAguinaldo;
    private boolean rdrEnableJulio;
    private boolean rdrEnableDiciembre;
    private boolean rdrCatorcenas;
    private boolean dsblBtnCrear;

    private boolean dsblBtnNo;
    private boolean dsblBtnAu;
    private boolean dsblBtnFa;
    private boolean dsblBtnAg;

    private Double sueldoNeto;
    private Double deducciones;
    private Double montoSolicitado;
    private Double montoAguinaldo;
    private Double montoFAhorro;
    private Integer catorcenas;
    private Integer catorcenasMax;
    private Integer faCatorcena;
    private Integer topeMaximoPermitido;

    @ManagedProperty("#{amortizacionBean}")
    private AmortizacionBean amortizacionBean;
    @ManagedProperty("#{navigationController}")
    private NavigationBean navigationBean;

    private final SolicitudBo solBo;
    private final CatorcenasBo catBo;

    private UsuarioDto usuarioDto;
    private boolean julioSigAno;

    public SolicitudCreditoBean() {
        usuarioDto = (UsuarioDto) super.getSession().getAttribute("usuario");
        solBo = new SolicitudBo();
        catBo = new CatorcenasBo();
        tipoSolicitud = "";
        rdrPnlSimulador = false;
        topeMaximoPermitido = 125000;
      
    }

    public void validacionesInicialesRdr() {
         if(super.validateUser()){
        
            try {
                //Se establece si el usuario esta habilitado o deshabilitado
                int usrHabilitado = 0;
                if(usuarioDto.getHabilitado()!= null){
                    usrHabilitado = usuarioDto.getHabilitado();
                }else{
                    usrHabilitado = 1;
                }

                /**
                 * Valida que el usuario ya haya actualizado sus datos en el sistema
                 */
                if (usuarioDto.getPrimeraVez() == 1) {
                    FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Para poder solicitar un crédito, es necesario que complete su información en la pantalla de \"Mi Perfil\"", "");
                    FacesContext.getCurrentInstance().addMessage("valIniMsj", errorMsg);

                }else if(usuarioDto.getOmitirValidaciones().equals(1) && usuarioDto.getHabilitado() == 1){
                    this.dsblBtnAg = Boolean.TRUE;
                    this.dsblBtnAu = Boolean.TRUE;
                    this.dsblBtnFa = Boolean.TRUE;
                    this.dsblBtnNo = Boolean.TRUE;
                    
                    FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Se le autorizó de manera extraordinaria a solicitar créditos", "");
                    FacesContext.getCurrentInstance().addMessage("valIniMsj", errorMsg); 
                    
                } else if (solBo.validaMorosidad(usuarioDto.getId(), usuarioDto.getEmpresaId())) {
                    FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Usted no puede solicitar créditos ya que tiene adeudos pendientes", "");
                    FacesContext.getCurrentInstance().addMessage("valIniMsj", errorMsg);

                } else if (usrHabilitado == 0) {
                    FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Usted no puede solicitar créditos ya que ha sido bloqueado, favor de contactar con la administración de la Caja de Ahorro", "");
                    FacesContext.getCurrentInstance().addMessage("valIniMsj", errorMsg); 

                } else {

                    //Se inicializarán las validaciones para determinar que creditos puede pedir el usuario
                    solBo.validaAntiguedadYCreditos(usuarioDto.getFechaIngresoEmpresa(), usuarioDto.getFechaIngresoCaja(), usuarioDto.getId());

                    //Se renderizan los valores de los botones de solicitud
                    this.dsblBtnAg = solBo.isDsblBtnAg();
                    this.dsblBtnAu = solBo.isDsblBtnAu();
                    this.dsblBtnFa = solBo.isDsblBtnFa();
                    this.dsblBtnNo = solBo.isDsblBtnNo();

                    if (!solBo.getMsjNotificacion().isEmpty()) {
                        muestraMensajeValidaciones();
                    }

//                    if(usuarioDto.getEmpresaId()==2){
//                        dsblBtnFa = false;
//                        dsblBtnAg = false;
//                    }
                }
                
                
                
                  System.out.println("btnfa = "+dsblBtnFa+"  btnag = "+dsblBtnAg +"  empresa = "+ usuarioDto.getEmpresaId());

            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    public void muestraMensajeValidaciones() {
        FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, solBo.getMsjNotificacion(), "");
        FacesContext.getCurrentInstance().addMessage("valIniMsj", errorMsg);
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

        if (aplicaValidacionesPreGuardado()) {

            try {

                Double pagoCatorcenal = amortizacionBean.getAmortizacion() != null ? amortizacionBean.getAmortizacion().get(0).getAmoMontoPago() : 0.0;
                Double interesTotal = amortizacionBean.getMontoTotalInteres() != null ? amortizacionBean.getMontoTotalInteres() : 0.0;
                amortizacionBean.getMontoTotalInteres();
                amortizacionBean.getUltimaFechaFaAG();
                
               if(iTipoSolicitud == Constantes.FA || iTipoSolicitud == Constantes.AG){
                  catorcenas = amortizacionBean.getTotalCatorcenasFaAgSau();
               }
               
               if(iTipoSolicitud == Constantes.AG){
                  faCatorcena = 12;
               }
                
                BigInteger idSolicitud = new BigInteger(solBo.creaSolicitud(usuarioDto, montoSolicitado, deducciones, catorcenas, faCatorcena, tipoSolicitud, interesTotal, pagoCatorcenal, amortizacionBean.getUltimaFechaFaAG()).toString());
                super.getSession().setAttribute("idSolicitud", idSolicitud);
                navigationBean.goToDetalleSolicitud();
            } catch (BusinessException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }

    }

    /**
     * Valida los montos antes de guardar la solicitud
     *
     * @return
     */
    private boolean aplicaValidacionesPreGuardado() {
        boolean pasaValidaciones = Boolean.FALSE;
        Double libreXCatorcena = usuarioDto.getSalarioNeto() - deducciones;

        if (iTipoSolicitud == 7) {
            if (montoSolicitado > 300000) {
                FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, "El monto solicitado no puede superar los $150,000 ", ""));
                //        } else if (solicitudDto.getSueldoNeto() * 2 < 12000 && (empresa!= 3 || empresa != 4)) {
                //            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, "El sueldo neto no puede ser menor a $12,000 mensuales ", ""));
                //        } else if (libreXCatorcena < 4000 && (empresa!= 3 || empresa != 4)) {
                //            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Para solicitar un crédito de auto debe tener por lo menos $4,000 libres por catorcena (percepciones - deducciones = $4,000) ", ""));
            } else {
                pasaValidaciones = Boolean.TRUE;
            }
        } else {
            pasaValidaciones = Boolean.TRUE;
        }
        return pasaValidaciones;
    }

    /**
     * Valida que tipo de solicitud selecciono el usuario y renderiza los campos
     * en pantalla
     *
     * @param event
     */
    public void muestraSimulador(AjaxBehaviorEvent event) {
        LOGGER.debug("Dentro de muestraSimulador");
        rdrPnlSimulador = Boolean.TRUE;

        switch (tipoSolicitud) {
            case "no":
                limpiaCampos();
                rdrCatorcenas = Boolean.TRUE;
                rendererSolFondoAhorro = Boolean.FALSE;
                rendererSolAguinaldo = Boolean.FALSE;
                catorcenasMax = 78;
                iTipoSolicitud = Constantes.NO;
                topeMaximoPermitido=130000;
                break;

            case "au":
                limpiaCampos();
                rdrCatorcenas = Boolean.TRUE;
                rendererSolFondoAhorro = Boolean.FALSE;
                rendererSolAguinaldo = Boolean.FALSE;
                catorcenasMax = 130;
                iTipoSolicitud = Constantes.AU;
                topeMaximoPermitido=300000;
                break;

            case "fa":
                limpiaCampos();
                rendererSolFondoAhorro = Boolean.TRUE;
                rendererSolAguinaldo = Boolean.FALSE;
                rdrCatorcenas = Boolean.FALSE;
                iTipoSolicitud = Constantes.FA;
                topeMaximoPermitido=125000;

                try {
                    configuraSolFa();
                } catch (BusinessException ex) {
                    super.muestraMensajeError("Hubo un error, favor de oprimir F5", "", null);
                    LOGGER.error(ex.getMessage(), ex);
                }

                break;

            case "ag":
                limpiaCampos();
                rendererSolAguinaldo = Boolean.TRUE;
                rendererSolFondoAhorro = Boolean.FALSE;
                rdrCatorcenas = Boolean.FALSE;
                iTipoSolicitud = Constantes.AG;
                topeMaximoPermitido=125000;
                break;
        }
    }

    /**
     * Renderea los campos de pantalla que se utilizan cuando se solicita
     * credito de FA o AG
     */
    private void configuraSolFa() throws BusinessException {
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

        boolean calcularAmortizacion = false;

        switch (tipoSolicitud) {
            case "no":
                if (montoSolicitado != null && catorcenas != null && deducciones != null) {
                    calcularAmortizacion = true;
                }
                break;

            case "au":
                if (montoSolicitado != null && catorcenas != null && deducciones != null) {
                    calcularAmortizacion = true;
                }
                break;

            case "fa":
                if (montoSolicitado != null && faCatorcena != null && deducciones != null) {
                    calcularAmortizacion = true;
                }

                break;

            case "ag":
                if (montoSolicitado != null && deducciones != null) {
                    calcularAmortizacion = true;
                }
                break;
        }

        if (calcularAmortizacion) {

            try {
                System.out.println("Antes de generamaortizackon");
                this.amortizacionBean.generaAmortizacion(this.iTipoSolicitud, this.montoSolicitado, this.catorcenas,
                        this.faCatorcena, this.julioSigAno);
            } catch (BusinessException ex) {
                LOGGER.error("Error al calcular la amortizacion del simulador", ex);
            }

        }

    }

    public void limpiaCampos() {

        this.amortizacionBean.setAmortizacion(null);
        this.amortizacionBean.setMontoTotalAmortizacion(null);
        this.amortizacionBean.setMontoTotalInteres(null);
        this.amortizacionBean.setMontoTotalPagar(null);
        this.montoSolicitado = null;
        this.catorcenas = null;

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
     * @return the tipoSolicitud
     */
    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    /**
     * @param tipoSolicitud the tipoSolicitud to set
     */
    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    /**
     * @return the rendererSolFondoAhorro
     */
    public boolean isRendererSolFondoAhorro() {
        return rendererSolFondoAhorro;
    }

    /**
     * @param rendererSolFondoAhorro the rendererSolFondoAhorro to set
     */
    public void setRendererSolFondoAhorro(boolean rendererSolFondoAhorro) {
        this.rendererSolFondoAhorro = rendererSolFondoAhorro;
    }

    /**
     * @return the rendererSolAguinaldo
     */
    public boolean isRendererSolAguinaldo() {
        return rendererSolAguinaldo;
    }

    /**
     * @param rendererSolAguinaldo the rendererSolAguinaldo to set
     */
    public void setRendererSolAguinaldo(boolean rendererSolAguinaldo) {
        this.rendererSolAguinaldo = rendererSolAguinaldo;
    }

    /**
     * @return the rdrEnableJulio
     */
    public boolean isRdrEnableJulio() {
        return rdrEnableJulio;
    }

    /**
     * @param rdrEnableJulio the rdrEnableJulio to set
     */
    public void setRdrEnableJulio(boolean rdrEnableJulio) {
        this.rdrEnableJulio = rdrEnableJulio;
    }

    /**
     * @return the rdrEnableDiciembre
     */
    public boolean isRdrEnableDiciembre() {
        return rdrEnableDiciembre;
    }

    /**
     * @param rdrEnableDiciembre the rdrEnableDiciembre to set
     */
    public void setRdrEnableDiciembre(boolean rdrEnableDiciembre) {
        this.rdrEnableDiciembre = rdrEnableDiciembre;
    }

    /**
     * @return the sueldoNeto
     */
    public Double getSueldoNeto() {
        return sueldoNeto;
    }

    /**
     * @param sueldoNeto the sueldoNeto to set
     */
    public void setSueldoNeto(Double sueldoNeto) {
        this.sueldoNeto = sueldoNeto;
    }

    /**
     * @return the deducciones
     */
    public Double getDeducciones() {
        return deducciones;
    }

    /**
     * @param deducciones the deducciones to set
     */
    public void setDeducciones(Double deducciones) {
        this.deducciones = deducciones;
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
     * @return the montoAguinaldo
     */
    public Double getMontoAguinaldo() {
        return montoAguinaldo;
    }

    /**
     * @param montoAguinaldo the montoAguinaldo to set
     */
    public void setMontoAguinaldo(Double montoAguinaldo) {
        this.montoAguinaldo = montoAguinaldo;
    }

    /**
     * @return the montoFAhorro
     */
    public Double getMontoFAhorro() {
        return montoFAhorro;
    }

    /**
     * @param montoFAhorro the montoFAhorro to set
     */
    public void setMontoFAhorro(Double montoFAhorro) {
        this.montoFAhorro = montoFAhorro;
    }

    /**
     * @return the catorcenas
     */
    public Integer getCatorcenas() {
        return catorcenas;
    }

    /**
     * @param catorcenas the catorcenas to set
     */
    public void setCatorcenas(Integer catorcenas) {
        this.catorcenas = catorcenas;
    }

    /**
     * @return the catorcenasMax
     */
    public Integer getCatorcenasMax() {
        return catorcenasMax;
    }

    /**
     * @param catorcenasMax the catorcenasMax to set
     */
    public void setCatorcenasMax(Integer catorcenasMax) {
        this.catorcenasMax = catorcenasMax;
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
     * @return the rdrCatorcenas
     */
    public boolean isRdrCatorcenas() {
        return rdrCatorcenas;
    }

    /**
     * @param rdrCatorcenas the rdrCatorcenas to set
     */
    public void setRdrCatorcenas(boolean rdrCatorcenas) {
        this.rdrCatorcenas = rdrCatorcenas;
    }

    /**
     * @return the rdrPnlSimulador
     */
    public boolean isRdrPnlSimulador() {
        return rdrPnlSimulador;
    }

    /**
     * @param rdrPnlSimulador the rdrPnlSimulador to set
     */
    public void setRdrPnlSimulador(boolean rdrPnlSimulador) {
        this.rdrPnlSimulador = rdrPnlSimulador;
    }

    /**
     * @return the dsblBtnCrear
     */
    public boolean isDsblBtnCrear() {
        return dsblBtnCrear;
    }

    /**
     * @param dsblBtnCrear the dsblBtnCrear to set
     */
    public void setDsblBtnCrear(boolean dsblBtnCrear) {
        this.dsblBtnCrear = dsblBtnCrear;
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
     * @return the dsblBtnNo
     */
    public boolean isDsblBtnNo() {
        return dsblBtnNo;
    }

    /**
     * @param dsblBtnNo the dsblBtnNo to set
     */
    public void setDsblBtnNo(boolean dsblBtnNo) {
        this.dsblBtnNo = dsblBtnNo;
    }

    /**
     * @return the dsblBtnAu
     */
    public boolean isDsblBtnAu() {
        return dsblBtnAu;
    }

    /**
     * @param dsblBtnAu the dsblBtnAu to set
     */
    public void setDsblBtnAu(boolean dsblBtnAu) {
        this.dsblBtnAu = dsblBtnAu;
    }

    /**
     * @return the dsblBtnFa
     */
    public boolean isDsblBtnFa() {
        return dsblBtnFa;
    }

    /**
     * @param dsblBtnFa the dsblBtnFa to set
     */
    public void setDsblBtnFa(boolean dsblBtnFa) {
        this.dsblBtnFa = dsblBtnFa;
    }

    /**
     * @return the dsblBtnAg
     */
    public boolean isDsblBtnAg() {
        return dsblBtnAg;
    }

    /**
     * @param dsblBtnAg the dsblBtnAg to set
     */
    public void setDsblBtnAg(boolean dsblBtnAg) {
        this.dsblBtnAg = dsblBtnAg;
    }

    /**
     * @return the usuario
     */
    public UsuarioDto getUsuario() {
        return usuarioDto;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(UsuarioDto usuario) {
        this.usuarioDto = usuario;
    }

    /**
     * @return the topeMaximoPermitido
     */
    public Integer getTopeMaximoPermitido() {
        return topeMaximoPermitido;
    }

    /**
     * @param topeMaximoPermitido the topeMaximoPermitido to set
     */
    public void setTopeMaximoPermitido(Integer topeMaximoPermitido) {
        this.topeMaximoPermitido = topeMaximoPermitido;
    }

}
