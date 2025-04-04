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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.jasper.DoctosJasperSolicitudBo;
import mx.com.evoti.bo.jasper.GeneradorReportesBo;
import mx.com.evoti.bo.usuarioComun.DetalleSolicitudBo;
import mx.com.evoti.bo.util.EnviaCorreo;
import mx.com.evoti.dto.ImagenesDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.dto.jasper.FondeoDto;
import mx.com.evoti.dto.jasper.PendienteDto;
import mx.com.evoti.dto.jasper.ReporteAmortizacionDto;
import mx.com.evoti.dto.usuariocomun.SolicitudCreditoDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.NavigationBean;
import mx.com.evoti.presentacion.common.GeneradorReportesBean;
import mx.com.evoti.util.Constantes;
import net.sf.jasperreports.engine.JRException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "misSolsBean")
@ViewScoped
public class MisSolicitudesBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MisSolicitudesBean.class);
    private static final long serialVersionUID = 4529616878078635784L;

    @ManagedProperty("#{navigationController}")
    private NavigationBean navigationBean;

    private DetalleSolicitudBo detSolBo;
    private DoctosJasperSolicitudBo docJaspBo;
    private List<SolicitudCreditoDto> misSolicitudes;

    private StreamedContent anexoA;
    private StreamedContent anexoB;
    private StreamedContent anexoC;
    private StreamedContent solicitud;
    private StreamedContent aviso;
    private SolicitudCreditoDto solicitudSeleccionada;
    private List<String> urlPdfs;

    private GeneradorReportesBo genRepBo;

    public MisSolicitudesBean() {
        detSolBo = new DetalleSolicitudBo();
        docJaspBo = new DoctosJasperSolicitudBo();
        genRepBo = new GeneradorReportesBo();
    }

    /**
     * Obtiene todas las solicitudes del usuario que se encuentra logueado en el
     * sistema
     */
    public void obtieneSolicitudesCliente() {
        if(super.validateUser()){
            try {
                UsuarioDto usrDto = (UsuarioDto) super.getSession().getAttribute("usuario");
                misSolicitudes = detSolBo.getSolsByUsuId(usrDto.getId());
            } catch (BusinessException ex) {
                LOGGER.error("Error al obtener las solicitudes del usuario", ex);
            }
        }
    }

    public void generaAnexoA(SolicitudCreditoDto solicitud) {
        try {
            FondeoDto dto = docJaspBo.getCreditoBySolicitud(solicitud.getSolId().longValue());

            List<ReporteAmortizacionDto> lstReporteDto = new ArrayList<>();
            List<AmortizacionDto> amortizacion;
            ReporteAmortizacionDto reporteDto = new ReporteAmortizacionDto();
            reporteDto.setNombre(dto.getNombre());
            reporteDto.setClaveCredito(dto.getClaveCredito());

            amortizacion = this.docJaspBo.obtieneAmortizacion(dto.getIdSolicitud());

            reporteDto.setAmortizacion(amortizacion);
            lstReporteDto.add(reporteDto);
            // here bean populate with all data
//            String nombreArchivo = "/reportes/AnexoATblAmort.jrxml";
            String nombreArchivo = "/reportes/AnexoATblAmort.jasper";
            String archivoFinal = "AnexoATablaAmortizacion.pdf";

            anexoA = genRepBo.crearReporteGenericoSinCompilar(lstReporteDto, nombreArchivo, archivoFinal);

        } catch (BusinessException | JRException | IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void generaAnexoB(SolicitudCreditoDto solicitud) {
        try {

            List<FondeoDto> arrayList = new ArrayList<>();
            arrayList.add(docJaspBo.obtieneDatosAnexoB(solicitud.getSolId().longValue()));

            String nombreArchivo = "/reportes/AnexoBPagare.jasper";
            String archivoFinal = "AnexoB.pdf";

            this.setAnexoB(genRepBo.crearReporteGenericoSinCompilar(arrayList, nombreArchivo, archivoFinal));
        } catch (BusinessException | JRException | IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void generaAnexoC(SolicitudCreditoDto solicitud) {
        try {

            List<FondeoDto> arrayList = new ArrayList<>();
            List<UsuarioDto> avales = docJaspBo.obtieneAvalesAnexoC(solicitud.getSolId().longValue());
            FondeoDto credito = docJaspBo.obtieneDatosAnexoC(solicitud.getSolId().longValue());
            arrayList.add(credito);

            String nombreArchivo = "/reportes/AnexoCPagare.jasper";
            String archivoFinal = "AnexoCPagare.pdf";

            arrayList.get(0).setAvales(avales);

            if (avales.size() == 1) {

                arrayList.get(0).setLineaDos("_________________________");
                arrayList.get(0).setAvalDos(avales.get(0).getNombreCompleto());

            }

            if (avales.size() == 2) {

                arrayList.get(0).setLineaUno("_________________________");
                arrayList.get(0).setAvalUno(avales.get(0).getNombreCompleto());
                arrayList.get(0).setLineaTres("_________________________");
                arrayList.get(0).setAvalTres(avales.get(1).getNombreCompleto());

            }

            if (avales.size() == 3) {

                arrayList.get(0).setLineaUno("_________________________");
                arrayList.get(0).setAvalUno(avales.get(0).getNombreCompleto());
                arrayList.get(0).setLineaDos("_________________________");
                arrayList.get(0).setAvalDos(avales.get(1).getNombreCompleto());
                arrayList.get(0).setLineaTres("_________________________");
                arrayList.get(0).setAvalTres(avales.get(2).getNombreCompleto());

            }

            this.setAnexoC(genRepBo.crearReporteGenericoSinCompilar(arrayList, nombreArchivo, archivoFinal));
        } catch (JRException | IOException | BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void generaSolicitudCredito(SolicitudCreditoDto solicitud) {
        try {
            List<FondeoDto> arrayList = new ArrayList<>();

            List<PendienteDto> avales = docJaspBo.obtieneAvalesSolicitud(solicitud.getSolId().longValue());
            FondeoDto fondeo = docJaspBo.obtieneDatosSolicitud(solicitud.getSolId().longValue());
            arrayList.add(fondeo);
            arrayList.get(0).setAvalesSolicitud(avales);
            arrayList.get(0).setIdSolicitud(solicitud.getSolId().longValue());

            String nombreArchivo = "/reportes/SolicitudCredito.jasper";
            String archivoFinal = "SolicitudCredito.pdf";

            this.setSolicitud(genRepBo.crearReporteGenericoSinCompilar(arrayList, nombreArchivo, archivoFinal));
        } catch (JRException | IOException | BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void generaAviso(SolicitudCreditoDto solicitud) {

        try {
            List<FondeoDto> arrayList = docJaspBo.obtieneDatosAviso(solicitud.getSolId().longValue());

            String nombreArchivo = "/reportes/Aviso.jasper";
            String archivoFinal = "Aviso.pdf";

            this.setAviso(genRepBo.crearReporteGenericoSinCompilar(arrayList, nombreArchivo, archivoFinal));
        } catch (JRException | IOException | BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que administra el commponente
     *
     * @param event
     * @throws IOException
     */
    public void handleFileUpload(FileUploadEvent event) throws IOException {
        try {
            urlPdfs = new ArrayList<>();
           
            UploadedFile file = event.getFile();
            String fileName = generaNombreArchivo();
            String finalPath = Constantes.PATH_DOCTOS + "/" + fileName;

            urlPdfs.add(finalPath);
            gestionarImgsSubidas(file.getContents(), finalPath);

            System.out.println(finalPath);
            System.out.println("#####################################");

            //Se actualiza la solicitud al estatus DOCUMENTOS ENVIADOS
            detSolBo.updtEstatusSolicitud(solicitudSeleccionada.getSolId(), Constantes.SOL_EST_DOCTOS_ENV,null);

            ImagenesDto img = new ImagenesDto();
            img.setImagen(fileName);
            img.setTipoImagen(Constantes.DOC_FIRMADA_INT);
            img.setImaEstatus(2);
            img.setImaSolicitud(solicitudSeleccionada.getSolId().longValue());

            detSolBo.guardaImagen(img);
            
            super.hideShowDlg("PF('dlgEnviarDoctosW').hide()");

            super.muestraMensajeExito("El archivo " + file.getFileName() + " se cargo correctamente.","","docFirmada");
        } catch (Exception e) {
            super.muestraMensajeError("Error al cargar su documento",e.getMessage(),null);
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String generaNombreArchivo() {

        String nombreArchivo = solicitudSeleccionada.getSolId() + "_DOC_SOLICITUD.pdf";

        return nombreArchivo;
    }

    public void pasarDatosSolicitud(SolicitudCreditoDto solicitud) {
        solicitudSeleccionada = solicitud;
    }

    public String gestionarImgsSubidas(byte[] datos, String rutaArchivo) {
        File file = new File(rutaArchivo);

        try {
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(datos);
            fout.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneradorReportesBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneradorReportesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rutaArchivo;
    }

    public void eliminaSolicitud(){
        
    }
    
    
    public void muestraMensajeExito(UploadedFile file) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "El archivo " + file.getFileName() + " se cargo correctamente.", "");
        FacesContext.getCurrentInstance().addMessage("pdfs2", msg);
    }

    public void muestraMensajeError(Exception e) {
        FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar el archivo", e.getMessage());
        FacesContext.getCurrentInstance().addMessage("pdfs2", errorMsg);
    }

    public void goToDetalleSolicitud(SolicitudCreditoDto solicitud) {

        super.getSession().setAttribute("idSolicitud", solicitud.getSolId());
        navigationBean.goToDetalleSolicitud();
    }

    /**
     * @return the misSolicitudes
     */
    public List<SolicitudCreditoDto> getMisSolicitudes() {
        return misSolicitudes;
    }

    /**
     * @param misSolicitudes the misSolicitudes to set
     */
    public void setMisSolicitudes(List<SolicitudCreditoDto> misSolicitudes) {
        this.misSolicitudes = misSolicitudes;
    }

    /**
     * @return the anexoA
     */
    public StreamedContent getAnexoA() {
        return anexoA;
    }

    /**
     * @param anexoA the anexoA to set
     */
    public void setAnexoA(StreamedContent anexoA) {
        this.anexoA = anexoA;
    }

    /**
     * @return the anexoB
     */
    public StreamedContent getAnexoB() {
        return anexoB;
    }

    /**
     * @param anexoB the anexoB to set
     */
    public void setAnexoB(StreamedContent anexoB) {
        this.anexoB = anexoB;
    }

    /**
     * @return the anexoC
     */
    public StreamedContent getAnexoC() {
        return anexoC;
    }

    /**
     * @param anexoC the anexoC to set
     */
    public void setAnexoC(StreamedContent anexoC) {
        this.anexoC = anexoC;
    }

    /**
     * @return the solicitud
     */
    public StreamedContent getSolicitud() {
        return solicitud;
    }

    /**
     * @param solicitud the solicitud to set
     */
    public void setSolicitud(StreamedContent solicitud) {
        this.solicitud = solicitud;
    }

    /**
     * @return the aviso
     */
    public StreamedContent getAviso() {
        return aviso;
    }

    /**
     * @param aviso the aviso to set
     */
    public void setAviso(StreamedContent aviso) {
        this.aviso = aviso;
    }

    /**
     * @return the solicitudSeleccionada
     */
    public SolicitudCreditoDto getSolicitudSeleccionada() {
        return solicitudSeleccionada;
    }

    /**
     * @param solicitudSeleccionada the solicitudSeleccionada to set
     */
    public void setSolicitudSeleccionada(SolicitudCreditoDto solicitudSeleccionada) {
        this.solicitudSeleccionada = solicitudSeleccionada;
    }

    /**
     * @return the urlPdfs
     */
    public List<String> getUrlPdfs() {
        return urlPdfs;
    }

    /**
     * @param urlPdfs the urlPdfs to set
     */
    public void setUrlPdfs(List<String> urlPdfs) {
        this.urlPdfs = urlPdfs;
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
