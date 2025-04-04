/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon.solicitudes;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import mx.com.evoti.bo.administrador.solicitud.FondeosBo;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.bo.usuarioComun.DocumentosSolicitudBo;
import mx.com.evoti.dto.SolicitudDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.usuariocomun.DocumentosDto;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.bitacora.BitacoraBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.primefaces.context.RequestContext;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "fondeoBean")
@ViewScoped
public class FondeosBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FondeosBean.class);
    private static final long serialVersionUID = 2857543228202281693L;

    @ManagedProperty("#{bitaBean}")
    private BitacoraBean bitaBean;

    private FondeosBo bo;
    private DocumentosSolicitudBo docSolBo;
    private DetalleSolicitudBo detSolBo;
    private BancosBo bancoBo;

    private List<SolicitudDto> pendtsXFondear;
    private List<SolicitudDto> pendtsXFondearFltr;
    private SolicitudDto pendtsXFondearSelected;
    private List<SolicitudDto> solsFondeadas;
    private List<SolicitudDto> solsFondeadasFltr;
    private SolicitudDto solsFondeadasSelected;
    private List<SolicitudDto> doctosEnviados;
    private List<SolicitudDto> doctosEnviadosFltr;
    private SolicitudDto doctosEnviadosSelected;
    private List<SolicitudDto> firmadas;
    private List<SolicitudDto> firmadasFltr;
    private SolicitudDto firmadaSelected;

    private Date fechaPrimerPago;

    private Date fechaDepositoCredito;
    private String motivoCancelacion;

    private String motivoCancelacionCredito;

    private Imagenes doctoFirmado;
    private Imagenes estadoCta;

    private String[] formatoDocFirmada;
    private Integer tipoRecepcionDocto;

    private UsuarioDto usrDto;

    public FondeosBean() {
        bo = new FondeosBo();
        docSolBo = new DocumentosSolicitudBo();
        detSolBo = new DetalleSolicitudBo();
        bancoBo = new BancosBo();
        usrDto = (UsuarioDto) super.getSession().getAttribute("usuario");

    }

    public void getSolicitudesAceptadas() {

        try {
            pendtsXFondear = bo.getSolsPendtsFondeoScreen(Constantes.SOL_EST_ACEPTADA);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void inicializaDialogFondeo() {
        this.fechaPrimerPago = null;
        
         if (pendtsXFondearSelected.getProId() == 4 || pendtsXFondearSelected.getProId() == 5 || 
                 pendtsXFondearSelected.getProId() == 11) {
            fechaPrimerPago = this.pendtsXFondearSelected.getSolFechaUltCatorcena();
        }
        
        

    }

    /**
     * Obtiene las solicitudes de las que el usuario ya envio sus documentos
     * firmados
     */
    public void getSolDoctosEnviados() {
        try {
            doctosEnviados = bo.getSolsPendtsFondeoScreen(Constantes.SOL_EST_DOCTOS_ENV);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene las solicitudes que ya han sido fondeadas firmados
     */
    public void getSolFondeadas() {
        try {
            solsFondeadas = bo.getSolsPendtsFondeoScreen(Constantes.SOL_EST_FONDEADA);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene las solicitudes de las que ya se han aprobado los documentos
     * firmados para llenar la tabla pendientes de depósito
     */
    public void getSolicitudesFirmDoctos() {
        try {

            firmadas = bo.getSolsPendtsFondeoScreen(Constantes.SOL_EST_DOCTOS_APROBADOS);

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Realiza el fondeo de la solicitud
     */
    public void fondeaSolicitud() {
        try {
            if (super.validaSiEsCatorcena(fechaPrimerPago)) {
                bo.fondeaSolicitud(pendtsXFondearSelected, fechaPrimerPago);

                super.hideShowDlg("PF('dlgFndSolW').hide()");
                muestraMensajeExito("La solicitud fue fondeada", "", null);
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Se manda a abrir el dialog de bitacora para cancelar la solicitud
     */
    public void abrirCancelarSol() {
        bitaBean.setObservacion("");
        bitaBean.setReferencia(this.pendtsXFondearSelected.getSolId().longValue());
        bitaBean.setSubreferencia(null);
        bitaBean.setTipoTransaccion(Constantes.BIT_SOL_RECHAZADA);
        bitaBean.setCatorcenas(this.pendtsXFondearSelected.getSolCatorcenas());
        bitaBean.setMontoSolicitado(this.pendtsXFondearSelected.getSolMontoSolicitado());
        bitaBean.setPagoCredito(this.pendtsXFondearSelected.getSolPagoCredito());
        Usuarios usuario = new Usuarios(this.pendtsXFondearSelected.getUsuId());
        usuario.setUsuCorreo(this.pendtsXFondearSelected.getUsuEmail());
        usuario.setUsuNombre(this.pendtsXFondearSelected.getUsuNombre());
        usuario.setUsuPaterno(this.pendtsXFondearSelected.getUsuPaterno());
        usuario.setUsuMaterno(this.pendtsXFondearSelected.getUsuMaterno());
        bitaBean.setUsuario(usuario);
        bitaBean.setUsuarioActor(usrDto);

        super.hideShowDlg("PF('dlgMotivRechazoSol').show()");

    }

    public void initDoctoFirmado() {
        try {
            doctoFirmado = bo.getDocumentoByTipo(doctosEnviadosSelected.getSolId().longValue(), Constantes.DOC_FIRMADA_INT);

            if (doctoFirmado == null) {
                muestraMensajeError("No se encuentra el documento", "", null);

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra el documento", "Favor de ponerse en contacto con el usuario para que adjunte nuevamente el documento");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else if (doctoFirmado.getImaImagen() == null) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra el documento", "Favor de ponerse en contacto con el usuario para que adjunte nuevamente el documento");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else {

                FacesContext context = FacesContext.getCurrentInstance();
                String dest = context.getExternalContext().getRealPath("/");
                String origen = Constantes.PATH_DOCTOS + File.separator + doctoFirmado.getImaImagen();

                File archivo = new File(origen);

                try {
                    Util.copyFileTempPath(archivo, dest);
                } catch (IOException ex) {
                    LOGGER.error("Error al copiar el archivo a la ruta", ex.getCause());
                }

                super.hideShowDlg("PF('dlgDocPdfW').show()");
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void eliminaImagen() {
        FacesContext context = FacesContext.getCurrentInstance();
        String dest = context.getExternalContext().getRealPath("/");

        try {
            Util.deleteTempFile(doctoFirmado.getImaImagen(), dest);
        } catch (IOException ex) {
            LOGGER.error("No se encontró el documento ", ex);
        }

    }
    
    
    public void eliminaEstadoCuenta() {
        FacesContext context = FacesContext.getCurrentInstance();
        String dest = context.getExternalContext().getRealPath("/");

        try {
            Util.deleteTempFile(estadoCta.getImaImagen(), dest);
        } catch (IOException ex) {
            LOGGER.error("No se encontró el documento ", ex);
        }

    }
    
   

    public void initMarcarDocRecibida() {

    }

    /**
     * Actualiza en base de datos el estatus de la imagen y de la solicitud
     *
     * @param estatus
     */
    public void updtEstatusDocSol(Integer estatus) {
        try {
            LOGGER.debug("Dentro de updtEstatusDoc");

            DocumentosDto doc = new DocumentosDto();
            doc.setIdImagen(doctoFirmado.getImaId());
            doc.setiStatus(estatus);
            docSolBo.updtEstatusImagen(doc);

            /**
             * Si el estatus = 4, significa que se está rechazando el documento
             * y por lo tanto se actualiza la solicitud a estatus 4 fondeada
             */
            if (estatus == 4) {

                detSolBo.updtEstatusSolicitud(doctosEnviadosSelected.getSolId(), 4, null);

                Usuarios usuario = new Usuarios(this.doctosEnviadosSelected.getUsuId());
                usuario.setUsuCorreo(this.doctosEnviadosSelected.getUsuEmail());
                usuario.setUsuNombre(this.doctosEnviadosSelected.getUsuNombre());
                usuario.setUsuPaterno(this.doctosEnviadosSelected.getUsuPaterno());
                usuario.setUsuMaterno(this.doctosEnviadosSelected.getUsuMaterno());

                detSolBo.enviaCorreoValSol(usuario, null, null, null, 3, motivoCancelacion);
                muestraMensajeError("El documento fue rechazado", "", null);
            } else if (estatus == 3) {
                /**
                 * se actualiza estatus de solicitud a 8 documentacion enviada
                 * aceptda y en formato de recepcion se manda 1 que significa
                 * por SISTEMA
                 */
                detSolBo.updtEstatusSolicitud(doctosEnviadosSelected.getSolId(), 8, 1);
                muestraMensajeExito("El documento fue aprobado", "", null);
            }
            
            eliminaImagen();
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Actualiza elestatus de la solicitud a 8 cuando se va a asignar un tipo de
     * formato recibido de documentacion diferente a sistema, metodo de la tabla
     * fondeadas
     */
    public void updtEstatusSolicitudDocsFC() {
        try {
            configuraFormaRecepcionDoc();
            detSolBo.updtEstatusSolicitud(solsFondeadasSelected.getSolId(), Constantes.SOL_EST_DOCTOS_APROBADOS, tipoRecepcionDocto);
            tipoRecepcionDocto = null;
            super.hideShowDlg("PF('dlgMarcaDocRecibidaW').hide()");
            muestraMensajeExito("La solicitud fue actualizada", "", null);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    public void configuraFormaRecepcionDoc() {

        switch (formatoDocFirmada.length) {
            case 2:
                tipoRecepcionDocto = 4;
                formatoDocFirmada[0] = null;
                formatoDocFirmada[1] = null;
                break;
            case 1:
                tipoRecepcionDocto = Integer.valueOf(formatoDocFirmada[0]);
                formatoDocFirmada[0] = null;
                break;

        }

    }

    public void abrirDialogRechazoDocFirmado() {
        LOGGER.info("Dentro de abrirDialogMotivRechazo");
        motivoCancelacion = "";
        super.hideShowDlg("PF('dlgRechazoDocFirm').show()");

    }

    public void inicializaFechaDepositoCredito() {
        fechaDepositoCredito = null;
    }

    /**
     * Metodo que actualiza el estatus de solicitud a 6 cerrada y la fecha de
     * deposito del credito
     */
    public void marcaDeposito() {
        try {
            detSolBo.updtEstatusSolicitud(firmadaSelected.getSolId(), 6, null);
            detSolBo.updtFechaDepositoCre(firmadaSelected.getSolId().longValue(), fechaDepositoCredito);

            BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
            //Actualiza la tabla BANCOS y llena las tablas BANCO_EDOCTA y ESTADO_CUENTA

            Bancos banco = bancoBo.generaBancoDepositoCredito(firmadaSelected.getSolId().longValue(), firmadaSelected.getEmpId(),
                    firmadaSelected.getSolMontoSolicitado(), idRelacion.longValue());
            bancoBo.guardaBanco(banco);
            bancoBo.guardaEdoCtaYBEC(banco, idRelacion.longValue());

            muestraMensajeExito("La fecha de depósito fue actualizada", "", null);
            super.hideShowDlg("PF('dlgMarcaDeposito').hide()");

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene el estado de cuenta de la solicitud seleccionada para mostrarlo
     * en la pantalla de pendientes de marcar deposito
     */
    public void obtenerEstadoCta() {
        try {
            this.estadoCta = null;
            this.estadoCta = bo.getDocumentoByTipo(firmadaSelected.getSolId().longValue(), Constantes.DOC_EDOCTA_INT);

            if (estadoCta == null) {
                muestraMensajeError("No se encuentra el documento", "", null);

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra el documento", "Favor de ponerse en contacto con el usuario para que adjunte nuevamente el documento");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else if (estadoCta.getImaImagen() == null) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra el documento", "Favor de ponerse en contacto con el usuario para que adjunte nuevamente el documento");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                String dest = context.getExternalContext().getRealPath("/");
                String origen = Constantes.PATH_DOCTOS + File.separator + estadoCta.getImaImagen();

                File archivo = new File(origen);

                try {
                    Util.copyFileTempPath(archivo, dest);
                } catch (IOException ex) {
                    LOGGER.error("Error al copiar el archivo a la ruta", ex.getCause());
                }
                super.hideShowDlg("PF('dlgEdoCtaW').show()");
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    /**
     * Obtiene la documentacion firmada de la solicitud seleccionada para
     * mostrarlo en la pantalla de pendientes de deposito
     */
    public void obtenerDoctoFirmado() {
        try {
            doctoFirmado = null;
            doctoFirmado = bo.getDocumentoByTipo(firmadaSelected.getSolId().longValue(), Constantes.DOC_FIRMADA_INT);

            if (doctoFirmado == null) {
                muestraMensajeError("No se encuentra el documento", "", null);

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra el documento", "Favor de ponerse en contacto con el usuario para que adjunte nuevamente el documento");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else if (doctoFirmado.getImaImagen() == null) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra el documento", "Favor de ponerse en contacto con el usuario para que adjunte nuevamente el documento");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else {
                 FacesContext context = FacesContext.getCurrentInstance();
                String dest = context.getExternalContext().getRealPath("/");
                String origen = Constantes.PATH_DOCTOS + File.separator + doctoFirmado.getImaImagen();

                File archivo = new File(origen);

                try {
                    Util.copyFileTempPath(archivo, dest);
                } catch (IOException ex) {
                    LOGGER.error("Error al copiar el archivo a la ruta", ex.getCause());
                }
                super.hideShowDlg("PF('dlgVisorDocFirms').show()");
            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void cancelarCredito() {
        try {
            Integer idCredito = bo.cancelarCredito(firmadaSelected.getSolId().longValue());
            bitaBean.limpiaCampos();
            bitaBean.setObservacion(motivoCancelacionCredito);
            bitaBean.setReferencia(idCredito.longValue());
            bitaBean.setTipoTransaccion(Constantes.BIT_CRE_CANCELADO);
            bitaBean.setUsuarioActor(usrDto);

            detSolBo.updtEstatusSolicitud(firmadaSelected.getSolId(), 6, null);
            bitaBean.guardaRegBitacora();
            motivoCancelacionCredito = "";
            super.muestraMensajeExito("El crédito fue cancelado", "", null);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void cancelarCreditoDoc1() {
        try {
            Integer idCredito = bo.cancelarCredito(solsFondeadasSelected.getSolId().longValue());
            bitaBean.limpiaCampos();
            bitaBean.setObservacion(motivoCancelacionCredito);
            if(idCredito != null){
                bitaBean.setReferencia(idCredito.longValue());
                bitaBean.setTipoTransaccion(Constantes.BIT_CRE_CANCELADO);
            }else{
                bitaBean.setReferencia(solsFondeadasSelected.getSolId().longValue());
                bitaBean.setTipoTransaccion(Constantes.BIT_SOL_RECHAZADA);
            }
            bitaBean.setUsuarioActor(usrDto);

            detSolBo.updtEstatusSolicitud(solsFondeadasSelected.getSolId(), 6, null);
            bitaBean.guardaRegBitacora();
            motivoCancelacionCredito = "";
            super.muestraMensajeExito("El crédito fue cancelado", "", null);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void cancelarCreditoDoc2() {
        try {
            Integer idCredito = bo.cancelarCredito(doctosEnviadosSelected.getSolId().longValue());
            bitaBean.limpiaCampos();
            bitaBean.setObservacion(motivoCancelacionCredito);
             if(idCredito != null){
                bitaBean.setReferencia(idCredito.longValue());
                bitaBean.setTipoTransaccion(Constantes.BIT_CRE_CANCELADO);
            }else{
                bitaBean.setReferencia(solsFondeadasSelected.getSolId().longValue());
                bitaBean.setTipoTransaccion(Constantes.BIT_SOL_RECHAZADA);
            }
            bitaBean.setUsuarioActor(usrDto);

            detSolBo.updtEstatusSolicitud(doctosEnviadosSelected.getSolId(), 6, null);
            bitaBean.guardaRegBitacora();
            motivoCancelacionCredito = "";
            super.muestraMensajeExito("El crédito fue cancelado", "", null);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the pendtsXFondear
     */
    public List<SolicitudDto> getPendtsXFondear() {
        return pendtsXFondear;
    }

    /**
     * @param pendtsXFondear the pendtsXFondear to set
     */
    public void setPendtsXFondear(List<SolicitudDto> pendtsXFondear) {
        this.pendtsXFondear = pendtsXFondear;
    }

    /**
     * @return the doctosEnviados
     */
    public List<SolicitudDto> getDoctosEnviados() {
        return doctosEnviados;
    }

    /**
     * @param doctosEnviados the doctosEnviados to set
     */
    public void setDoctosEnviados(List<SolicitudDto> doctosEnviados) {
        this.doctosEnviados = doctosEnviados;
    }

    /**
     * @return the firmadas
     */
    public List<SolicitudDto> getFirmadas() {
        return firmadas;
    }

    /**
     * @param firmadas the firmadas to set
     */
    public void setFirmadas(List<SolicitudDto> firmadas) {
        this.firmadas = firmadas;
    }

    /**
     * @return the pendtsXFondearFltr
     */
    public List<SolicitudDto> getPendtsXFondearFltr() {
        return pendtsXFondearFltr;
    }

    /**
     * @param pendtsXFondearFltr the pendtsXFondearFltr to set
     */
    public void setPendtsXFondearFltr(List<SolicitudDto> pendtsXFondearFltr) {
        this.pendtsXFondearFltr = pendtsXFondearFltr;
    }

    /**
     * @return the pendtsXFondearSelected
     */
    public SolicitudDto getPendtsXFondearSelected() {
        return pendtsXFondearSelected;
    }

    /**
     * @param pendtsXFondearSelected the pendtsXFondearSelected to set
     */
    public void setPendtsXFondearSelected(SolicitudDto pendtsXFondearSelected) {
        this.pendtsXFondearSelected = pendtsXFondearSelected;
    }

    /**
     * @return the doctosEnviadosFltr
     */
    public List<SolicitudDto> getDoctosEnviadosFltr() {
        return doctosEnviadosFltr;
    }

    /**
     * @param doctosEnviadosFltr the doctosEnviadosFltr to set
     */
    public void setDoctosEnviadosFltr(List<SolicitudDto> doctosEnviadosFltr) {
        this.doctosEnviadosFltr = doctosEnviadosFltr;
    }

    /**
     * @return the doctosEnviadosSelected
     */
    public SolicitudDto getDoctosEnviadosSelected() {
        return doctosEnviadosSelected;
    }

    /**
     * @param doctosEnviadosSelected the doctosEnviadosSelected to set
     */
    public void setDoctosEnviadosSelected(SolicitudDto doctosEnviadosSelected) {
        this.doctosEnviadosSelected = doctosEnviadosSelected;
    }

    /**
     * @return the firmadasFltr
     */
    public List<SolicitudDto> getFirmadasFltr() {
        return firmadasFltr;
    }

    /**
     * @param firmadasFltr the firmadasFltr to set
     */
    public void setFirmadasFltr(List<SolicitudDto> firmadasFltr) {
        this.firmadasFltr = firmadasFltr;
    }

    /**
     * @return the firmadaSelected
     */
    public SolicitudDto getFirmadaSelected() {
        return firmadaSelected;
    }

    /**
     * @param firmadaSelected the firmadaSelected to set
     */
    public void setFirmadaSelected(SolicitudDto firmadaSelected) {
        this.firmadaSelected = firmadaSelected;
    }

    /**
     * @return the fechaPrimerPago
     */
    public Date getFechaPrimerPago() {
        return fechaPrimerPago;
    }

    /**
     * @param fechaPrimerPago the fechaPrimerPago to set
     */
    public void setFechaPrimerPago(Date fechaPrimerPago) {
        this.fechaPrimerPago = fechaPrimerPago;
    }

    /**
     * @return the motivoCancelacion
     */
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    /**
     * @param motivoCancelacion the motivoCancelacion to set
     */
    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    /**
     * @return the bitaBean
     */
    public BitacoraBean getBitaBean() {
        return bitaBean;
    }

    /**
     * @param bitaBean the bitaBean to set
     */
    public void setBitaBean(BitacoraBean bitaBean) {
        this.bitaBean = bitaBean;
    }

    /**
     * @return the doctoFirmado
     */
    public Imagenes getDoctoFirmado() {
        return doctoFirmado;
    }

    /**
     * @param doctoFirmado the doctoFirmado to set
     */
    public void setDoctoFirmado(Imagenes doctoFirmado) {
        this.doctoFirmado = doctoFirmado;
    }

    /**
     * @return the fechaDepositoCredito
     */
    public Date getFechaDepositoCredito() {
        return fechaDepositoCredito;
    }

    /**
     * @param fechaDepositoCredito the fechaDepositoCredito to set
     */
    public void setFechaDepositoCredito(Date fechaDepositoCredito) {
        this.fechaDepositoCredito = fechaDepositoCredito;
    }

    /**
     * @return the estadoCta
     */
    public Imagenes getEstadoCta() {
        return estadoCta;
    }

    /**
     * @param estadoCta the estadoCta to set
     */
    public void setEstadoCta(Imagenes estadoCta) {
        this.estadoCta = estadoCta;
    }

    /**
     * @return the motivoCancelacionCredito
     */
    public String getMotivoCancelacionCredito() {
        return motivoCancelacionCredito;
    }

    /**
     * @param motivoCancelacionCredito the motivoCancelacionCredito to set
     */
    public void setMotivoCancelacionCredito(String motivoCancelacionCredito) {
        this.motivoCancelacionCredito = motivoCancelacionCredito;
    }

    /**
     * @return the solsFondeadas
     */
    public List<SolicitudDto> getSolsFondeadas() {
        return solsFondeadas;
    }

    /**
     * @param solsFondeadas the solsFondeadas to set
     */
    public void setSolsFondeadas(List<SolicitudDto> solsFondeadas) {
        this.solsFondeadas = solsFondeadas;
    }

    /**
     * @return the solsFondeadasFltr
     */
    public List<SolicitudDto> getSolsFondeadasFltr() {
        return solsFondeadasFltr;
    }

    /**
     * @param solsFondeadasFltr the solsFondeadasFltr to set
     */
    public void setSolsFondeadasFltr(List<SolicitudDto> solsFondeadasFltr) {
        this.solsFondeadasFltr = solsFondeadasFltr;
    }

    /**
     * @return the solsFondeadasSelected
     */
    public SolicitudDto getSolsFondeadasSelected() {
        return solsFondeadasSelected;
    }

    /**
     * @param solsFondeadasSelected the solsFondeadasSelected to set
     */
    public void setSolsFondeadasSelected(SolicitudDto solsFondeadasSelected) {
        this.solsFondeadasSelected = solsFondeadasSelected;
    }

    /**
     * @return the formatoDocFirmada
     */
    public String[] getFormatoDocFirmada() {
        return formatoDocFirmada;
    }

    /**
     * @param formatoDocFirmada the formatoDocFirmada to set
     */
    public void setFormatoDocFirmada(String[] formatoDocFirmada) {
        this.formatoDocFirmada = formatoDocFirmada;
    }

    /**
     * @return the tipoRecepcionDocto
     */
    public Integer getTipoRecepcionDocto() {
        return tipoRecepcionDocto;
    }

    /**
     * @param tipoRecepcionDocto the tipoRecepcionDocto to set
     */
    public void setTipoRecepcionDocto(Integer tipoRecepcionDocto) {
        this.tipoRecepcionDocto = tipoRecepcionDocto;
    }

}
