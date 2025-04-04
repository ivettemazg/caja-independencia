package mx.com.evoti.presentacion.solicitudes;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.HistorialSolicitudesBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.solicitudes.HistorialDao;
import mx.com.evoti.dto.HistorialSolicitudesDto;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.usuariocomun.DoctosSolBean;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "historialBean")
@ViewScoped
public class HistorialBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5216692439496179076L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HistorialBean.class);

     @ManagedProperty("#{doctosSolBean}")
    private DoctosSolBean doctosBean; 
    
    private List<HistorialSolicitudesDto> historialSolicitudes;
    private HistorialSolicitudesDto detalleSolicitud;
    private List<HistorialSolicitudesDto> avalesSolicitud;
    private HistorialSolicitudesDto solSelected;
    private HistorialSolicitudesDto solSelectedDetalle;

    private HistorialSolicitudesBo solBo;

    public void init() {
         if(super.validateUser()){
            try {
                System.out.println("init");

                HistorialDao hdao = new HistorialDao();
                this.historialSolicitudes = hdao.getHistorial();
            } catch (IntegracionException ex) {
                Logger.getLogger(HistorialBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void abreDetalleSolicitud() {

        try {
            solBo = new HistorialSolicitudesBo();
            if (solSelected != null) {
                this.detalleSolicitud = solBo.getDetalleSolicitud(solSelected.getFolio());
            }
            super.hideShowDlg("PF('dlgDetalleSolicitudW').show()");
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    public void verAvalesSolicitud() {

        try {
            solBo = new HistorialSolicitudesBo();
            if (solSelected != null) {
                this.avalesSolicitud = solBo.getAvalesSolicitud(solSelected.getFolio());
            }
            super.hideShowDlg("PF('dlgAvalesSolicitudW').show()");
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    
    public void verDocumentacion(){
        Solicitudes solicitud = new Solicitudes();
        solicitud.setSolId(solSelected.getFolio().longValue());
        this.doctosBean.setSolicitud(solicitud);
        this.doctosBean.init();
        super.hideShowDlg("PF('dlgGridDoctos').show()");
    }
    
    /**
     * @return the historialSolicitudes
     */
    public List<HistorialSolicitudesDto> getHistorialSolicitudes() {
        return historialSolicitudes;
    }

    /**
     * @param historialSolicitudes the historialSolicitudes to set
     */
    public void setHistorialSolicitudes(List<HistorialSolicitudesDto> historialSolicitudes) {
        this.historialSolicitudes = historialSolicitudes;
    }

    /**
     * @return the solSelected
     */
    public HistorialSolicitudesDto getSolSelected() {
        return solSelected;
    }

    /**
     * @param solSelected the solSelected to set
     */
    public void setSolSelected(HistorialSolicitudesDto solSelected) {
        this.solSelected = solSelected;
    }

    /**
     * @return the detalleSolicitud
     */
    public HistorialSolicitudesDto getDetalleSolicitud() {
        return detalleSolicitud;
    }

    /**
     * @param detalleSolicitud the detalleSolicitud to set
     */
    public void setDetalleSolicitud(HistorialSolicitudesDto detalleSolicitud) {
        this.detalleSolicitud = detalleSolicitud;
    }

    /**
     * @return the doctosBean
     */
    public DoctosSolBean getDoctosBean() {
        return doctosBean;
    }

    /**
     * @param doctosBean the doctosBean to set
     */
    public void setDoctosBean(DoctosSolBean doctosBean) {
        this.doctosBean = doctosBean;
    }

     /**
     * @return the solSelectedDetalle
     */
    public HistorialSolicitudesDto getSolSelectedDetalle() {
        return solSelectedDetalle;
    }

    /**
     * @param solSelectedDetalle the solSelectedDetalle to set
     */
    public void setSolSelectedDetalle(HistorialSolicitudesDto solSelectedDetalle) {
        this.solSelectedDetalle = solSelectedDetalle;
    }

    /**
     * @return the avalesSolicitud
     */
    public List<HistorialSolicitudesDto> getAvalesSolicitud() {
        return avalesSolicitud;
    }

    /**
     * @param avalesSolicitud the avalesSolicitud to set
     */
    public void setAvalesSolicitud(List<HistorialSolicitudesDto> avalesSolicitud) {
        this.avalesSolicitud = avalesSolicitud;
    }
    
}
