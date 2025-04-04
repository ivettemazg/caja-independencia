package mx.com.evoti.presentacion.creditos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.PagosDao;
import mx.com.evoti.dao.creditos.ReporteMorososDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.dto.PagoDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "reporteMorososBean")
@ViewScoped
public class ReporteMorososBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5216692439496179076L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteMorososBean.class);

    private List<CreditoDto> creditos;
    private List<CreditoDto> credFiltered;
    private CreditoDto credSelected;
  
    private ReporteMorososDao dao;
    private AmortizacionDao amoDao;
    private PagosDao pagDao;
    private Date fechaCorte;
    private boolean rendererTablaMorosos;
    private List<AmortizacionDto> amortizacion;
    private List<PagoDto> pagosSinRegistrar;
   

    
    public ReporteMorososBean() {
        dao = new ReporteMorososDao();
        amoDao = new AmortizacionDao();
        pagDao = new PagosDao();
    }

    public void getReporte() {
        try {
            System.out.println("init");
            this.creditos = dao.getReporteMorosos(fechaCorte);
             rendererTablaMorosos = true;
        } catch (IntegracionException ex) {
            Logger.getLogger(ReporteMorososBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     public void obtieneTblAmortizacionPagos() {

        try {
            
           amortizacion=  amoDao.getAmortizacionXCredito(credSelected.getCreId());
           pagosSinRegistrar = pagDao.getPagosSinRegistrar(credSelected.getCreUsuId());
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
         

    }
    

    /**
     * @return the creditos
     */
    public List<CreditoDto> getCreditos() {
        return creditos;
    }

    /**
     * @param creditos the creditos to set
     */
    public void setCreditos(List<CreditoDto> creditos) {
        this.creditos = creditos;
    }

    /**
     * @return the credFiltered
     */
    public List<CreditoDto> getCredFiltered() {
        return credFiltered;
    }

    /**
     * @param credFiltered the credFiltered to set
     */
    public void setCredFiltered(List<CreditoDto> credFiltered) {
        this.credFiltered = credFiltered;
    }

    /**
     * @return the fechaCorte
     */
    public Date getFechaCorte() {
        return fechaCorte;
    }

    /**
     * @param fechaCorte the fechaCorte to set
     */
    public void setFechaCorte(Date fechaCorte) {
        this.fechaCorte = fechaCorte;
    }

    /**
     * @return the rendererTablaMorosos
     */
    public boolean isRendererTablaMorosos() {
        return rendererTablaMorosos;
    }

    /**
     * @param rendererTablaMorosos the rendererTablaMorosos to set
     */
    public void setRendererTablaMorosos(boolean rendererTablaMorosos) {
        this.rendererTablaMorosos = rendererTablaMorosos;
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
     * @return the pagosSinRegistrar
     */
    public List<PagoDto> getPagosSinRegistrar() {
        return pagosSinRegistrar;
    }

    /**
     * @param pagosSinRegistrar the pagosSinRegistrar to set
     */
    public void setPagosSinRegistrar(List<PagoDto> pagosSinRegistrar) {
        this.pagosSinRegistrar = pagosSinRegistrar;
    }
}