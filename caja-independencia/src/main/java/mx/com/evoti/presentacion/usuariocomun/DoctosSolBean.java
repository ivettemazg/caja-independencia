/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.usuariocomun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.LoggerFactory;

import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.bo.usuarioComun.DocumentosSolicitudBo;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.usuariocomun.DocumentosDto;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.NavigationBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "doctosSolBean")
@ViewScoped
public class DoctosSolBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DoctosSolBean.class);
    private static final long serialVersionUID = 4975288863444986442L;

    private final DocumentosSolicitudBo docSolBo;
    private final DetalleSolicitudBo detSolBo;
    private UsuarioDto usuarioGlobal;
    //Usuario que se asigna desde la pantalla de solicitar un credito
    private UsuarioDto usuarioSolicitante;
    //Se asigna ya que se esta autorizando o rechazando la solicitud
    private Usuarios usuario;
    private List<DocumentosDto> documentos;
    private DocumentosDto docSeleccionado;
    private List<Imagenes> imagenes;
    private Integer noAvales;
    private Solicitudes solicitud;
    private String motivoRechazo;

    private String urlPdf;

    @ManagedProperty("#{navigationController}")
    private NavigationBean navigationBean;

    public DoctosSolBean() {

        docSolBo = new DocumentosSolicitudBo();

        detSolBo = new DetalleSolicitudBo();
    }

    public void init() {
        
        LOGGER.info("Dentro de DoctosSolBean.init()");
        try {
            this.sesionGlobal = super.getSession();
            usuarioGlobal = (UsuarioDto) super.sesionGlobal.getAttribute("usuario");

            imagenes = getImgsXSol(BigInteger.valueOf(solicitud.getSolId()));

            preparaTabla();
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public List<Imagenes> getImgsXSol(BigInteger idSolicitud) throws BusinessException {
        return docSolBo.getImgsXSol(idSolicitud);
    }

    /**
     * Metodo que controla la carga de los documentos del componente p:fileupload
     * @param event
     * @throws IOException 
     */
    public void handleFileUpload(FileUploadEvent event) throws IOException {
        try {
            //FacesContext context = FacesContext.getCurrentInstance();
//            String path = context.getExternalContext().getRealPath("/");
            LOGGER.info("Dentro de handleFileUpload");
            UploadedFile file = event.getFile();

            String fileName = docSeleccionado.getNombreDocumento();
            String finalPath = Constantes.PATH_DOCTOS + "/" + fileName;
            LOGGER.info("Gestionando imagenes subidas");
            gestionarImgsSubidas(file.getContents(), finalPath);

            System.out.println(finalPath);
            System.out.println("#####################################");

            docSeleccionado.setiStatus(2);
            docSolBo.updtImgEstatus(docSeleccionado, solicitud);
            
            String message = null;
            
            /*
            * Cuando el documento rechazado no es del tipo documentacion firmada actualiza la solicitud a 
            * pendiente de revision
            */
            if(docSeleccionado.getiTipoDocumento() != 8){
                message = detSolBo.updtSolicitudEstatus(solicitud.getSolId(), usuarioSolicitante.getNombreCompleto(), usuarioSolicitante.getCorreo());
            }else{
                detSolBo.updtSolDocEnviada(solicitud.getSolId());
            }
            
            super.muestraMensajeExito("El archivo " + file.getFileName() + " se cargo correctamente.","","growl");

             if (message != null) {
                super.muestraMensajeDialog("Datos completados", message,FacesMessage.SEVERITY_INFO);
            }
            
        } catch (Exception e) {
            super.muestraMensajeError("Hubo un error al subir el archivo","Favor de subirlo de nuevo","growl");
            LOGGER.error(e.getMessage(), e);
        }

    }

    public String gestionarImgsSubidas(byte[] datos, String rutaArchivo) {
        LOGGER.info("Dentro de DoctosSolBean.gestionarImgsSubidas");
        File file = new File(rutaArchivo);

        try {
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(datos);
            fout.close();
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return rutaArchivo;
    }


    private void preparaTabla() {
        documentos = new ArrayList<>();

        for (Imagenes pojo : imagenes) {
            DocumentosDto dto = new DocumentosDto();
            dto.setIdImagen(pojo.getImaId());
            dto.setNombreDocumento(pojo.getImaImagen());

            dto.setiTipoDocumento(pojo.getImaTipoimagen());
            switch (pojo.getImaTipoimagen()) {
                case 1:
                    dto.setTipoDocumento(Constantes.DOC_ID);
                    break;
                case 2:
                    dto.setTipoDocumento(Constantes.DOC_DOMICILIO);
                    break;
                case 3:
                    dto.setTipoDocumento(Constantes.DOC_NOMINA);
                    break;
                case 4:
                    dto.setTipoDocumento(Constantes.DOC_EDOCTA);
                    break;
                case 5:
                    dto.setTipoDocumento(Constantes.DOC_AVAL);
                    break;
                case 6:
                    dto.setTipoDocumento(Constantes.DOC_FA);
                    break;
                case 7:
                    dto.setTipoDocumento(Constantes.DOC_AG);
                    break;
                case 8:
                    dto.setTipoDocumento(Constantes.DOC_FIRMADA);
                    break;

            }

            dto.setiStatus(pojo.getImaEstatus());

            switch (pojo.getImaEstatus()) {
                case 1:
                    dto.setStrStatus(Constantes.IMA_STT_PEND);
                    break;
                case 2:
                    dto.setStrStatus(Constantes.IMA_STT_VAL);
                    break;
                case 3:
                    dto.setStrStatus(Constantes.IMA_STT_APRO);
                    break;
                case 4:
                    dto.setStrStatus(Constantes.IMA_STT_RECH);
                    break;
            }

            documentos.add(dto);

        }

    }

    public void abrirDialogVisor(DocumentosDto doc) {
        LOGGER.info("Dentro de abrevisirdialog");

        
        FacesContext context = FacesContext.getCurrentInstance();
        String dest = context.getExternalContext().getRealPath("/");
        String origen = Constantes.PATH_DOCTOS+ File.separator  + doc.getNombreDocumento();
        
        File archivo = new File(origen);
        
        try {
            Util.copyFileTempPath(archivo, dest);
        } catch (IOException ex) {
           LOGGER.error("Error al copiar el archivo a la ruta", ex.getCause());
        }
        
        LOGGER.info(doc.getNombreDocumento());
        docSeleccionado = doc;

       super.hideShowDlg("PF('dlgDocPdf').show()");

    }
    
    public void eliminaImagen(){
        FacesContext context = FacesContext.getCurrentInstance();
        String dest = context.getExternalContext().getRealPath("/");
      
        
        try {
            Util.deleteTempFile(docSeleccionado.getNombreDocumento(), dest);
        } catch (IOException ex) {
            LOGGER.error("No se encontró el documento ", ex); 
        }
        
    }

    public void abrirDialogMotivRechazo() {
        LOGGER.info("Dentro de abrirDialogMotivRechazo");
        motivoRechazo = "";
        super.hideShowDlg("PF('dlgMotivRechazoDoc').show()");

    }

    /**
     * Actualiza en base de datos el estatus de la imagen
     *
     * @param estatus
     */
    public void updtEstatusDoc(Integer estatus) {
        try {
            LOGGER.debug("Dentro de updtEstatusDoc");
            docSeleccionado.setiStatus(estatus);
            docSolBo.updtEstatusImagen(docSeleccionado);

            /**
             * Si el estatus = 4, significa que se está rechazando el documento
             * y por lo tanto la solicitud
             */
            if (estatus == 4) {
                /**
                 * Se rechaza la solicitud
                 */
                detSolBo.updtEstatusSolicitud(BigInteger.valueOf(solicitud.getSolId()), 9,null);

                solicitud.setSolMotivoRechazo(motivoRechazo);
                enviaCorreoNotificacion(solicitud, usuario, 2);
                /*Inserta en bitacora el motivo de rechazo de la solicitud*/
                detSolBo.insertaMotivoRechazo(solicitud.getSolId(),
                        Constantes.BIT_SOL_RECHAZADA, usuarioGlobal.getId(), docSeleccionado.getIdImagen().longValue(), motivoRechazo);

                navigationBean.goToValidaSolicitudes();
                super.muestraMensajeError("La solicitud fue rechazada", "", null);
            }else if(estatus == 3){//Cuando se acepta la imagen, cierra el dialogo
               super.muestraMensajeExito("El documento fue aprobado", "", null);
            }
            
            eliminaImagen();
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Envía correo de que la solicitud se aceptó o se rechazó
     *
     * @param solicitud
     * @param usuario
     * @param tipoCorreo - 2 rechazada, 1 aceptada
     */
    public void enviaCorreoNotificacion(Solicitudes solicitud, Usuarios usuario, int tipoCorreo) {
        detSolBo.enviaCorreoValSol(usuario, solicitud.getSolMontoSolicitado(),solicitud.getSolCatorcenas(),solicitud.getSolPagoCredito(), tipoCorreo, solicitud.getSolMotivoRechazo());
    }

    /**
     * @return the documentos
     */
    public List<DocumentosDto> getDocumentos() {
        return documentos;
    }

    /**
     * @param documentos the documentos to set
     */
    public void setDocumentos(List<DocumentosDto> documentos) {
        this.documentos = documentos;
    }

    /**
     * @return the docSeleccionado
     */
    public DocumentosDto getDocSeleccionado() {
        return docSeleccionado;
    }

    /**
     * @param docSeleccionado the docSeleccionado to set
     */
    public void setDocSeleccionado(DocumentosDto docSeleccionado) {
        this.docSeleccionado = docSeleccionado;
    }

    /**
     * @return the imagenes
     */
    public List<Imagenes> getImagenes() {
        return imagenes;
    }

    /**
     * @param imagenes the imagenes to set
     */
    public void setImagenes(List<Imagenes> imagenes) {
        this.imagenes = imagenes;
    }

    /**
     * @return the noAvales
     */
    public Integer getNoAvales() {
        return noAvales;
    }

    /**
     * @param noAvales the noAvales to set
     */
    public void setNoAvales(Integer noAvales) {
        this.noAvales = noAvales;
    }

    /**
     * @return the usuarioSolicitante
     */
    public UsuarioDto getUsuarioSolicitante() {
        return usuarioSolicitante;
    }

    /**
     * @param usuarioSolicitante the usuarioSolicitante to set
     */
    public void setUsuarioSolicitante(UsuarioDto usuarioSolicitante) {
        LOGGER.info("Dentro de setUsuarioSolicitante");
        this.usuarioSolicitante = usuarioSolicitante;
    }

    /**
     * @return the solicitud
     */
    public Solicitudes getSolicitud() {
        return solicitud;
    }

    /**
     * @param solicitud the solicitud to set
     */
    public void setSolicitud(Solicitudes solicitud) {
        this.solicitud = solicitud;
    }

    /**
     * @return the urlPdf
     */
    public String getUrlPdf() {
        return urlPdf;
    }

    /**
     * @param urlPdf the urlPdf to set
     */
    public void setUrlPdf(String urlPdf) {
        this.urlPdf = urlPdf;
    }

    /**
     * @return the motivoRechazo
     */
    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    /**
     * @param motivoRechazo the motivoRechazo to set
     */
    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
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

}
