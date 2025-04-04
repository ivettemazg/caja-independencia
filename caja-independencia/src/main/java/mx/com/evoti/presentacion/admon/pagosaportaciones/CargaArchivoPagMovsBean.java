/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon.pagosaportaciones;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import mx.com.evoti.bo.administrador.algoritmopagos.CargaPagosMovimientosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.ArchivoDto;
import mx.com.evoti.dto.PagoDto;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Constantes;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
@ManagedBean(name = "cargaPagosApoBean")
@SessionScoped
public class CargaArchivoPagMovsBean extends BaseBean implements Serializable {
 
    private static Logger LOGGER = LoggerFactory.getLogger(CargaArchivoPagMovsBean.class); 
    private static final long serialVersionUID = 8661770929926945842L;
    private Integer tipoArchivo;
    private CargaPagosMovimientosBo bo;
    private List<ArchivoDto> archivosLst;
    private List<PagoDto> pagos;
    private List<PagoDto> lstPagosFiltrados;
    private List<MovimientosDto> lstMovimientos;
    private ArchivoDto archivoSeleccionado;
    private Date catorcena;
    private Boolean rdrPnlExcel;
    private String finalPath;
    private String nombreArchivo;
    private List<PagoDto> lstPagos;
    private List<MovimientosDto> aportaciones;
    private Boolean disblFUBtn;

    public CargaArchivoPagMovsBean(){
        bo = new CargaPagosMovimientosBo();
        rdrPnlExcel = Boolean.FALSE;
    }

    public void init(){
        obtenerArchivosHistorico();
        limpiaValoresArchivo();
    }
    
    /**
     * Obtiene los archivos subidos según la catorcena que se elige
     * 
     */
    public void obtieneArchivosXCatorcena(){
        LOGGER.info("Dentro de validasiescatorcena");
        try {
            //Valida si es catorcena
            if(!bo.validaSiEsCatorcena(catorcena)){
                String error = "La fecha que ha seleccionado no es catorcena.";
                //Si no es catorcena muestra mensaje de error en pantalla
                muestraMensajeError(error, "fechaCato");
            }else{
                
                LOGGER.info("Catorcena encontrada");
                obtenerArchivosHistoricoXCatorcena(catorcena);
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex.getCause());
        }
    }
    
    /**
     * Habilita el uploadfile y el boton de procesar excel al elegir una opcion del p:selectOneRadio
     */
    public void habilitaFUpld(){
        disblFUBtn = Boolean.FALSE;
    }
    
    /**
     * Metodo que se ejecuta al subir el excel
     * @param event 
     */
    public void hfuExcel(FileUploadEvent event) {
        try {     
           
            UploadedFile file = event.getFile();
            nombreArchivo = file.getFileName().toLowerCase().trim();
            finalPath = Constantes.PATH_DOCTOS +File.separator +nombreArchivo;

            Boolean archivoExiste = bo.validaArchivoExiste(nombreArchivo);
                LOGGER.info(finalPath);
                LOGGER.info("#####################################");

            if (archivoExiste) {
                muestraMensajeError("El archivo que intenta subir ya se ha subido anteriormente, si necesita volver a subirlo, primero elimine los registros del archivo anterior siempres y cuando no se haya aplicado a la amortización.", null);
            } else {
                
                /**
                 * Se guarda el archivo en el servidor para poder cargarlo de ahi y leerlo
                 */
               
                gestionarArchivo(file.getContents(), finalPath);

                /*
                Si el archivo es nuevo entonces procesa el contenido del excel y se guarda 
                en las tablas correspondientes
                */
                bo.procesaExcel(nombreArchivo, finalPath, tipoArchivo);
                
              
                muestraMensajeExito(file);
            }
        } catch (BusinessException ex) {
            muestraMensajeError(ex.getMessage(), null);
            LOGGER.error(ex.getMessage(), ex);
        }
       
    }
    
    /**
     * Metodo que guarda el archivo en la ruta especificada
     * @param datos
     * @param rutaArchivo 
     */
    public void gestionarArchivo(byte[] datos, String rutaArchivo) {
         LOGGER.info("Guardando el archivo en "+rutaArchivo);
        File file = new File(rutaArchivo);

        try {
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(datos);
            fout.close();
            LOGGER.info("Archivo guardado exitosamente");
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
       
    }
    
      public void muestraMensajeExito(UploadedFile file) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "El archivo " + file.getFileName() + " se guardó correctamente.", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void muestraMensajeError(String error, String for_) {
        FacesMessage errorMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, error, "");
        FacesContext.getCurrentInstance().addMessage(for_, errorMsg);
    }
    
    public void rendereaPnlExcel(boolean bol){
        rdrPnlExcel = bol;
        limpiaValoresArchivo();
    }
    
    /**
     * Metodo que aplica los cambios de empresa
     */
    public void ejecutaCambiosEmpresa(){
//        try {
            if(finalPath==null){
                muestraMensajeError("Debe subir un Excel para continuar.", null);
            }else{
             
                rendereaPnlExcel(Boolean.FALSE);
            }
//        } catch (BusinessException ex) {
//            limpiaValoresArchivo();
//            muestraMensajeError(ex.getMessage(), null);
//        } catch(Exception e){
//            limpiaValoresArchivo();
//            muestraMensajeError("Uno de los valores está nulo", null);
//            LOGGER.error(e.getMessage(), e);
//        }
    }
    
    public void obtenerArchivosHistorico(){
        try {
            archivosLst = bo.obtenerArchivosHistorico();
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    /**
     * Obtiene los archivos que corresponden a la catorcena que se manda como
     * parámetro
     * @param cator 
     */
    public void obtenerArchivosHistoricoXCatorcena(Date cator){
        try {
            archivosLst.clear();
            archivosLst = bo.obtenerArchivoHistoricoXCatorcena(cator);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void limpiaValoresArchivo(){
        finalPath ="";
        nombreArchivo = "";
        disblFUBtn =  Boolean.TRUE;
    }
    
    public void eliminarArchivo(){
        try {
            bo.eliminarArchivo(archivoSeleccionado);
            obtenerArchivosHistorico();
        } catch (BusinessException ex) {
            muestraMensajeError("Error al borrar de la base de datos", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    
    public void abreDetalleArchivo(){
        pagos=null;
        lstMovimientos = null;
        try{
        if(archivoSeleccionado.getArhEstatus()==2  && archivoSeleccionado.getArhTipoArchivo()== 1){
            pagos = bo.abreDetalleArchivo(archivoSeleccionado);
             super.hideShowDlg("PF('dlgPagosDto').show()");
        }else if(archivoSeleccionado.getArhEstatus()==2  && archivoSeleccionado.getArhTipoArchivo() == 2){
            lstMovimientos = bo.abreDetalleArchivo(archivoSeleccionado);
           super.hideShowDlg("PF('dlgApoDto').show()");
        }
        }catch(BusinessException ex){
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    
    /**
     * @return the tipoArchivo
     */
    public Integer getTipoArchivo() {
        return tipoArchivo;
    }

    /**
     * @param tipoArchivo the tipoArchivo to set
     */
    public void setTipoArchivo(Integer tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    /**
     * @return the archivosLst
     */
    public List<ArchivoDto> getArchivosLst() {
        return archivosLst;
    }

    /**
     * @param archivosLst the archivosLst to set
     */
    public void setArchivosLst(List<ArchivoDto> archivosLst) {
        this.archivosLst = archivosLst;
    }

    /**
     * @return the archivoSeleccionado
     */
    public ArchivoDto getArchivoSeleccionado() {
        return archivoSeleccionado;
    }

    /**
     * @param archivoSeleccionado the archivoSeleccionado to set
     */
    public void setArchivoSeleccionado(ArchivoDto archivoSeleccionado) {
        this.archivoSeleccionado = archivoSeleccionado;
    }

    /**
     * @return the catorcena
     */
    public Date getCatorcena() {
        return catorcena;
    }

    /**
     * @param catorcena the catorcena to set
     */
    public void setCatorcena(Date catorcena) {
        this.catorcena = catorcena;
    }

    /**
     * @return the rdrPnlExcel
     */
    public Boolean getRdrPnlExcel() {
        return rdrPnlExcel;
    }

    /**
     * @param rdrPnlExcel the rdrPnlExcel to set
     */
    public void setRdrPnlExcel(Boolean rdrPnlExcel) {
        this.rdrPnlExcel = rdrPnlExcel;
    }

    /**
     * @return the pagos
     */
    public List<PagoDto> getPagos() {
        return pagos;
    }

    /**
     * @param pagos the pagos to set
     */
    public void setPagos(List<PagoDto> pagos) {
        this.pagos = pagos;
    }

    /**
     * @return the lstMovimientos
     */
    public List<MovimientosDto> getLstMovimientos() {
        return lstMovimientos;
    }

    /**
     * @param lstMovimientos the lstMovimientos to set
     */
    public void setLstMovimientos(List<MovimientosDto> lstMovimientos) {
        this.lstMovimientos = lstMovimientos;
    }

    /**
     * @return the lstPagosFiltrados
     */
    public List<PagoDto> getLstPagosFiltrados() {
        return lstPagosFiltrados;
    }

    /**
     * @param lstPagosFiltrados the lstPagosFiltrados to set
     */
    public void setLstPagosFiltrados(List<PagoDto> lstPagosFiltrados) {
        this.lstPagosFiltrados = lstPagosFiltrados;
    }

    /**
     * @return the disblFUBtn
     */
    public Boolean getDisblFUBtn() {
        return disblFUBtn;
    }

    /**
     * @param disblFUBtn the disblFUBtn to set
     */
    public void setDisblFUBtn(Boolean disblFUBtn) {
        this.disblFUBtn = disblFUBtn;
    }

}