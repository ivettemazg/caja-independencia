package mx.com.evoti.presentacion.rendimiento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.rendimiento.ProcesoDao;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Rendimiento;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "procesoBean")
@ViewScoped
public class ProcesoBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 5216692439496179076L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProcesoBean.class);

    private List<String> dates;
    private List<Object[]> ahorroFyNFList;
    private List<Object[]> ahorroVoluntarioList;
    private List<Object[]> pagosList;
    private String selectedDate;
    private Double intereses;
    private Double comisiones;
    private HashMap datesMap;
    private HashMap valoresAcumulado;
    private String mensajeConfirmacion;

    public void init() {
        try {
            System.out.println("init");

            ProcesoDao pdao = new ProcesoDao();
            
            this.datesMap = pdao.getFechas();

            this.valoresAcumulado = new HashMap<>();
            dates = new ArrayList<>(datesMap.keySet());
        } catch (IntegracionException ex) {
            Logger.getLogger(ProcesoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void consultaInversionesComisiones(){
        ProcesoDao pdao = new ProcesoDao();
         
        Rendimiento rendimiento = null;
        try {
            rendimiento = pdao.getInversionesComisiones(this.selectedDate);
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex.getCause());
            super.muestraMensajeError("Hubo un error al consultar el rendimiento", "", null);
        }
        
        if(rendimiento != null){
            if(rendimiento.getRenInteresesInversion() != null){
                this.intereses = rendimiento.getRenInteresesInversion();
            }else{
                this.intereses = 0.0;
            }
            
            if(rendimiento.getRenComisionesBancarias() != null){
                this.comisiones = rendimiento.getRenComisionesBancarias();
            }else{
                this.comisiones = 0.0;
            }
        }
        
        
    }
    
    public void mesesRendimiento() {
        try {
            double currentValue;
            int currentMovProducto;
            ProcesoDao pdao = new ProcesoDao();
            this.ahorroFyNFList = pdao.getUsuariosMovimientos(this.selectedDate);
            this.ahorroVoluntarioList = pdao.getUsuariosMovimientosVol(this.selectedDate);
            this.pagosList = pdao.getPagos(this.selectedDate);

            double ahorroFijo = 0;
            double ahorroNoFijo = 0;
            double ahorroVoluntario = 0;
            double acumuladoTotal = 0;

            double exacto = 0;
            double mayor = 0;
            double mas1Credito = 0;
            double mas1CreditoAcumulado = 0;
            double acumulado = 0;
            double capital = 0;
            double extemporaneo = 0;
            double pagosTotal = 0;

            double interesTotalMensual = 0;
            double reserva = 0;
            double interesNetoMensual = 0;
            double factorRendimiento = 0;

            for (Object[] obj : this.ahorroFyNFList) {
                currentValue = Double.parseDouble(obj[2].toString());
                currentMovProducto = Integer.parseInt(obj[1].toString());

                if (currentMovProducto == 1) {
                    ahorroFijo += currentValue;
                } else if (currentMovProducto == 2) {
                    ahorroNoFijo += currentValue;
                }

                acumuladoTotal += currentValue;
            }

            for (Object[] obj : this.ahorroVoluntarioList) {
                currentValue = Double.parseDouble(obj[2].toString());
                ahorroVoluntario += currentValue;
                acumuladoTotal += currentValue;
            }

            for (Object[] obj : this.pagosList) {
                currentValue = Double.parseDouble(obj[1].toString());
                currentMovProducto = Integer.parseInt(obj[0].toString());

                switch (currentMovProducto) {
                    case 2:
                        exacto += currentValue;
                        break;
                    case 4:
                        mayor += currentValue;
                        break;
                    case 5:
                        mas1Credito += currentValue;
                        break;
                    case 6:
                        mas1CreditoAcumulado += currentValue;
                        break;
                    case 8:
                        acumulado += currentValue;
                        break;
                    case 9:
                        capital += currentValue;
                        break;
                    case 10:
                        extemporaneo += currentValue;
                        break;
                }

                pagosTotal += currentValue;
            }

            interesTotalMensual = pagosTotal + this.intereses - this.comisiones;
            reserva = interesTotalMensual * 0.1;
            interesNetoMensual = interesTotalMensual * 0.9;
            factorRendimiento = interesNetoMensual / acumuladoTotal;

            valoresAcumulado.put("ahorroFijo", ahorroFijo);
            valoresAcumulado.put("ahorroNoFijo", ahorroNoFijo);
            valoresAcumulado.put("ahorroVoluntario", ahorroVoluntario);
            valoresAcumulado.put("acumuladoTotal", acumuladoTotal);

            valoresAcumulado.put("exacto", exacto);
            valoresAcumulado.put("mayor", mayor);
            valoresAcumulado.put("mas1Credito", mas1Credito);
            valoresAcumulado.put("mas1CreditoAcumulado", mas1CreditoAcumulado);
            valoresAcumulado.put("acumulado", acumulado);
            valoresAcumulado.put("capital", capital);
            valoresAcumulado.put("extemporaneo", extemporaneo);
            valoresAcumulado.put("pagosTotal", pagosTotal);

            valoresAcumulado.put("interesInversion", this.intereses);
            valoresAcumulado.put("comision", this.comisiones);

            valoresAcumulado.put("interesTotalMensual", interesTotalMensual);
            valoresAcumulado.put("reserva", reserva);
            valoresAcumulado.put("interesNetoMensual", interesNetoMensual);
            valoresAcumulado.put("factorRendimiento", factorRendimiento);

            
            if(this.comisiones > 0.0){
                this.mensajeConfirmacion = "Se va a ejecutar el rendimiento, ¿Desea continuar?";
            }else{
                this.mensajeConfirmacion = "No ha registrado las comisiones, ¿Desea continuar con la ejecución del rendimiento?";
            }
            
        } catch (IntegracionException ex) {
            Logger.getLogger(ProcesoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void ejecutaCalculo() {
        try {
            
            if(this.intereses <= 0.0){
                super.muestraMensajeDialog("Error", "No ha registrado los intereses, no puede ejecutar el rendimiento",FacesMessage.SEVERITY_ERROR);
                
            }else{    
                List<Movimientos> afynf = new ArrayList<>();
                List<Movimientos> avol = new ArrayList<>();

                ProcesoDao pdao = new ProcesoDao();
                double f = Double.parseDouble(valoresAcumulado.get("factorRendimiento").toString());

                for (Object[] obj : this.ahorroFyNFList) {
                    Movimientos mov = new Movimientos();

                    mov.setMovUsuId((Integer) obj[0]);
                    mov.setMovProducto((Integer) obj[1]);
                    mov.setMovDeposito(((Double) obj[2]) * f);
                    mov.setMovFecha(Util.generaFechaDeString(selectedDate, "yyyy-MM-dd"));
                    mov.setMovTipo("RENDIMIENTO");
                    mov.setMovAr(2);
                    afynf.add(mov);
                }

                for (Object[] obj : this.ahorroVoluntarioList) {
                    Movimientos mov = new Movimientos();
                    mov.setMovUsuId((Integer) obj[0]);
                    mov.setMovProducto((Integer) obj[1]);
                    mov.setMovDeposito(((Double) obj[2]) * f);
                    mov.setMovIdPadre((Integer) obj[3]);
                    mov.setMovFecha(Util.generaFechaDeString(selectedDate, "yyyy-MM-dd"));
                    mov.setMovTipo("RENDIMIENTO");
                    mov.setMovAr(2);
                    avol.add(mov);
                }

                pdao.updateAmountsv2(afynf);
                pdao.updateAmountsv2(avol);
            System.out.println("******************* " + dates);
            pdao.updateAmounts(this.valoresAcumulado, this.selectedDate, this.datesMap.get(this.selectedDate).toString());

                hideShowDlg("PF('dlgAhorroAcumuladoTotal').hide()");
                
                super.muestraMensajeDialog("Proceso terminado", "El rendimiento se ejecutó correctamente",FacesMessage.SEVERITY_INFO);
                
            }
        } catch (IntegracionException ex) {
            Logger.getLogger(ProcesoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap getValoresAcumulado() {
        return this.valoresAcumulado;
    }

    public Double getIntereses() {
        return this.intereses;
    }

    public Double getComisiones() {
        return this.comisiones;
    }

    public void setIntereses(Double intereses) {
        this.intereses = intereses;
    }

    public void setComisiones(Double comisiones) {
        this.comisiones = comisiones;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    /**
     * @return the mensajeConfirmacion
     */
    public String getMensajeConfirmacion() {
        return mensajeConfirmacion;
    }

    /**
     * @param mensajeConfirmacion the mensajeConfirmacion to set
     */
    public void setMensajeConfirmacion(String mensajeConfirmacion) {
        this.mensajeConfirmacion = mensajeConfirmacion;
    }
}
