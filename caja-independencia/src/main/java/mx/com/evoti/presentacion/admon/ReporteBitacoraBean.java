package mx.com.evoti.presentacion.admon;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.transacciones.TransaccionDao;
import mx.com.evoti.dto.TransaccionUsuarioDto;
import mx.com.evoti.presentacion.BaseBean;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "bitacoraAdmonBean")
@ViewScoped
public class ReporteBitacoraBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5216692439496179076L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteBitacoraBean.class);

    private List<TransaccionUsuarioDto> transacciones;
    private TransaccionDao dao;
    
    public ReporteBitacoraBean() {
        dao = new TransaccionDao();
    }

    public void init() {
        try {
            System.out.println("init");

            transacciones = dao.getTransacciones();
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return the transacciones
     */
    public List<TransaccionUsuarioDto> getTransacciones() {
        return transacciones;
    }

    /**
     * @param transacciones the transacciones to set
     */
    public void setTransacciones(List<TransaccionUsuarioDto> transacciones) {
        this.transacciones = transacciones;
    }

}
