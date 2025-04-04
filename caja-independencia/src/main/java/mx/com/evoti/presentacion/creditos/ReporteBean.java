package mx.com.evoti.presentacion.creditos;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.administrador.solicitud.FondeosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.AvalesSolicitudBo;
import mx.com.evoti.dao.creditos.ReporteDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.primefaces.context.RequestContext;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "reporteBean")
@ViewScoped
public class ReporteBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5216692439496179076L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteBean.class);

    private List<CreditoDto> reporte;
    private CreditoDto credSelected;

    private List<AmortizacionDto> amortizacion;
    private List<UsuarioDto> avales;
    private CreditoDto creditoPadre;

    private CreditosBo creBo;
    private AvalesSolicitudBo avalesBo;
    private Boolean rdrAvalCredTransf;
    private Boolean rdrIncob;

    private Imagenes doctoFirmado;
    private FondeosBo bo;
    private Date fechaIncobrable;

    public ReporteBean() {

    }

    public void init() {
        try {
            System.out.println("init");

            ReporteDao hdao = new ReporteDao();
            this.reporte = hdao.getReporte();
        } catch (IntegracionException ex) {
            Logger.getLogger(ReporteBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Inicializa los valores necesarios antes de abrir el dialog de
     * amortizacion y tambien lo abre
     */
    public void initDlgAmortizacion() {

        try {
            creBo = new CreditosBo();
            if (credSelected != null) {
                this.amortizacion = creBo.getAmortizacionXCredito(credSelected.getCreId());
            }
            super.hideShowDlg("PF('dlgAmortizacionW').show()");
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    public void openDlgVerAvales() {
        avalesBo = new AvalesSolicitudBo();

        try {
            avales = avalesBo.getAvalesXCredito(credSelected.getCreId());

            if (credSelected.getCreEstatus() == Constantes.CRE_EST_TRANSFERIDO) {
                rdrAvalCredTransf = Boolean.TRUE;
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void openDlgCrePadre() {

        try {
            creBo = new CreditosBo();
            DetalleCreditoDto credito = new DetalleCreditoDto();
            credito.setCreId(credSelected.getCreId());
            credito.setCreClave(credSelected.getCreClave());
            creditoPadre = creBo.getCreditoPadre(credito);

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
            bo = new FondeosBo();
            doctoFirmado = null;
            doctoFirmado = bo.getDocumentoByTipo(credSelected.getCreSol().longValue(), Constantes.DOC_FIRMADA_INT);

            if (doctoFirmado == null) {
                muestraMensajeError("No se encuentra el documento", "", null);

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra el documento", "");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else if (doctoFirmado.getImaImagen() == null) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "No se encuentra el documento", "");
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

    public void eliminaImagen() {
        FacesContext context = FacesContext.getCurrentInstance();
        String dest = context.getExternalContext().getRealPath("/");

        try {
            Util.deleteTempFile(doctoFirmado.getImaImagen(), dest);
        } catch (IOException ex) {
            LOGGER.error("No se encontró el documento ", ex);
        }

    }

    public void mandarAIncobrable() {
        try {
            creBo = new CreditosBo();

            creBo.updtCreditoEstatus(credSelected.getCreId(), Constantes.CRE_EST_INCOBRABLE, fechaIncobrable);
            //Actualiza la amortizacion del credito padre
            creBo.updtEstatusAmoInt(credSelected.getCreId(), Constantes.AMO_ESTATUS_INCOB_12);

            init();

            super.hideShowDlg("PF('dlgMandarIncob').hide()");

            super.muestraMensajeExito("El estatus del crédito se cambió a Incobrable", "", null);
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void initDlgIncobrable() {
        this.fechaIncobrable = null;
        this.rdrIncob = Boolean.FALSE;

        //Solamente se puede mandar a incobrable si esta activo o ajustado
        if (credSelected.getCreEstatus() == 1 || credSelected.getCreEstatus() == 6) {
            this.rdrIncob = Boolean.TRUE;
        } else {
            super.muestraMensajeGen("El estatus del crédito es " + credSelected.getCreEstatusStr(), "No puede mandarse a incobrable", null, FacesMessage.SEVERITY_WARN);
        }

        super.hideShowDlg("PF('dlgMandarIncob').show()");
    }

    /**
     * @return the amortizacion
     */
    public List<AmortizacionDto> getAmortizacion() {
        return amortizacion;
    }

    /**
     * @param amortizacion the amortizacion to set
     */
    public void setAmortizacion(List<AmortizacionDto> amortizacion) {
        this.amortizacion = amortizacion;
    }

    /**
     * @return the reporte
     */
    public List<CreditoDto> getReporte() {
        return reporte;
    }

    /**
     * @param reporte the reporte to set
     */
    public void setReporte(List<CreditoDto> reporte) {
        this.reporte = reporte;
    }

    /**
     * @return the credSelected
     */
    public CreditoDto getCredSelected() {
        return credSelected;
    }

    /**
     * @param credSelected the credSelected to set
     */
    public void setCredSelected(CreditoDto credSelected) {
        this.credSelected = credSelected;
    }

    /**
     * @return the avales
     */
    public List<UsuarioDto> getAvales() {
        return avales;
    }

    /**
     * @param avales the avales to set
     */
    public void setAvales(List<UsuarioDto> avales) {
        this.avales = avales;
    }

    /**
     * @return the creditoPadre
     */
    public CreditoDto getCreditoPadre() {
        return creditoPadre;
    }

    /**
     * @param creditoPadre the creditoPadre to set
     */
    public void setCreditoPadre(CreditoDto creditoPadre) {
        this.creditoPadre = creditoPadre;
    }

    /**
     * @return the rdrAvalCredTransf
     */
    public Boolean getRdrAvalCredTransf() {
        return rdrAvalCredTransf;
    }

    /**
     * @param rdrAvalCredTransf the rdrAvalCredTransf to set
     */
    public void setRdrAvalCredTransf(Boolean rdrAvalCredTransf) {
        this.rdrAvalCredTransf = rdrAvalCredTransf;
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
     * @return the fechaIncobrable
     */
    public Date getFechaIncobrable() {
        return fechaIncobrable;
    }

    /**
     * @param fechaIncobrable the fechaIncobrable to set
     */
    public void setFechaIncobrable(Date fechaIncobrable) {
        this.fechaIncobrable = fechaIncobrable;
    }

    /**
     * @return the rdrIncob
     */
    public Boolean getRdrIncob() {
        return rdrIncob;
    }

    /**
     * @param rdrIncob the rdrIncob to set
     */
    public void setRdrIncob(Boolean rdrIncob) {
        this.rdrIncob = rdrIncob;
    }

}
