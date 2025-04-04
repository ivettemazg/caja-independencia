/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.jasper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import mx.com.evoti.dto.SolicitudDto;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Venus
 */
public class GeneradorReportesBo implements Serializable{
     private List<SolicitudDto> solicitud = new ArrayList<SolicitudDto>();
    
     public GeneradorReportesBo(){
      
     }
     
     /**
      * Metodo que genera un reporte de jasperreport haciendo la compilacion en tiempo real
      * @param lista
      * @param rutaFinalArchivo
      * @param nombreArchivo
      * @return
      * @throws JRException
      * @throws IOException 
      */
     public StreamedContent crearReporteGenerico(List lista, String rutaFinalArchivo, String nombreArchivo) throws JRException, IOException{
            StreamedContent file;

           
            String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(rutaFinalArchivo);
            JasperDesign jasperDesign = JRXmlLoader.load(reportPath);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            JRBeanCollectionDataSource collectionDataSource = new JRBeanCollectionDataSource(lista, false);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<String, Object>(), collectionDataSource);

            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outstream);
            byte[] buffer = outstream.toByteArray();
            outstream.close();
            InputStream instream = new ByteArrayInputStream(buffer);

             
            file = new DefaultStreamedContent(instream, "image/jpg", nombreArchivo);
            
            return file;
     }
     
     public StreamedContent crearReporteGenericoSinCompilar(List lista, String rutaFinalArchivo, String nombreArchivo) throws JRException, IOException{
            StreamedContent file;

           String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(rutaFinalArchivo);
           
           JasperReport reporte = (JasperReport) JRLoader.loadObjectFromFile(reportPath);

            JRBeanCollectionDataSource collectionDataSource = new JRBeanCollectionDataSource(lista, false);
            JasperPrint jasperPrint = JasperFillManager.fillReport(reporte, new HashMap<String, Object>(), collectionDataSource);

            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outstream);
            byte[] buffer = outstream.toByteArray();
            outstream.close();
            InputStream instream = new ByteArrayInputStream(buffer);

             
            file = new DefaultStreamedContent(instream, "image/jpg", nombreArchivo);
            
            return file;
     }
    
      public void generaAnexoC() {


        String strPathPDF = "C:\\cajalast_270913\\cajalast\\web\\reportes\\AnexoCPagare.jasper";

        JRBeanCollectionDataSource data = new JRBeanCollectionDataSource(solicitud, false);
        Map<String, String> parametros = new HashMap();
        parametros.put("nombre", "nombre");

        try {

            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(strPathPDF);
            JasperPrint print = JasperFillManager.fillReport(jasperReport, new HashMap(), data);
            

                JRExporter exporter = new JRPdfExporter(); 
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
                exporter.setParameter(JRExporterParameter.OUTPUT_FILE, new java.io.File("c:/reporte.pdf")); 
                exporter.exportReport(); 

                //System.out.println("LLEGA LINEA 246");
          
            } catch (JRException ex) {
               ex.printStackTrace();
            }
    }
      
      
      public static void main(String args[]){
        
      }

     
      
}


