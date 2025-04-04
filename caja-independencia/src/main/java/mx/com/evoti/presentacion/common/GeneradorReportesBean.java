/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import mx.com.evoti.dto.ImagenesDto;
import mx.com.evoti.dto.usuariocomun.SolicitudCreditoDto;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
@ManagedBean(name = "generadorReportesBean")
@SessionScoped
public class GeneradorReportesBean {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GeneradorReportesBean.class);
    
    private List<SolicitudCreditoDto> solicitud = new ArrayList<>();
    private StreamedContent file;
      private List<ImagenesDto> imagenes;
      private Date fechaPrimerPago;
      private String img = "/phoenix.jpg";


    public GeneradorReportesBean() {


        SolicitudCreditoDto dto = new SolicitudCreditoDto();
        try {
//            dto.setNombre("Ivette Manzano");
//            dto.setMontoSolicitado(50000.0);
            ArrayList<SolicitudCreditoDto> arrayList = new ArrayList<>();
            arrayList.add(dto); // here bean populate with all data

            String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reportes/AnexoCPagare.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(reportPath);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);



            JRBeanCollectionDataSource collectionDataSource = new JRBeanCollectionDataSource(arrayList, false);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), collectionDataSource);



            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outstream);
            byte[] buffer = outstream.toByteArray();
            outstream.close();
            InputStream instream = new ByteArrayInputStream(buffer);

             
//            JRPdfExporter exporter = new JRPdfExporter();
//            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
//            exporter.setParameter(JRExporterParameter.INPUT_STREAM, instream);
//            exporter.exportReport();


           
            file = new DefaultStreamedContent(instream, "image/jpg", "downloadedFile.pdf");
        } catch (IOException | JRException ex) {
           LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void handleFileUpload(FileUploadEvent event) {
        try {
             FacesContext context = FacesContext.getCurrentInstance();
             String path = context.getExternalContext().getRealPath("/");
           
            
           
            UploadedFile uplFile = event.getFile();
            String fileName = uplFile.getFileName();
              
            
            gestionarImgsSubidas(uplFile.getContents(), "../imgs/"+fileName);
            
            System.out.println(path+fileName);
            System.out.println("#####################################");
//            imagen.setImagen(bytes);
//            imagen.setIsImg(is);
//            imagen.setTipoImagen(6);
//            imagenes.add(imagen);
          
        } catch (Exception e) {
           
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void gestionarImgsSubidas(byte[] datos, String rutaArchivo){
        File uplFile = new File(rutaArchivo);
        try {
            uplFile.createNewFile();
            FileOutputStream fout = new FileOutputStream(uplFile);
            fout.write(datos);
            fout.close();
        } catch (FileNotFoundException ex) {
             LOGGER.error(ex.getMessage(), ex);
        }catch (IOException ex) {
              LOGGER.error(ex.getMessage(), ex);
        } 
    }
    
    /**
     * @return the file
     */
    public StreamedContent getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(StreamedContent file) {
        this.file = file;
    }

    /**
     * @return the img
     */
    public String getImg() {
        return img;
    }

    /**
     * @param img the img to set
     */
    public void setImg(String img) {
        this.img = img;
    }

    /**
     * @return the imagenes
     */
    public List<ImagenesDto> getImagenes() {
        return imagenes;
    }

    /**
     * @param imagenes the imagenes to set
     */
    public void setImagenes(List<ImagenesDto> imagenes) {
        this.imagenes = imagenes;
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
}
