/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.usuarioComun.SolicitudBo;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.dto.usuariocomun.SolicitudCreditoDto;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
public class TablaAmortizacionBo implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer idUsuario;
    private final Logger LOGGER = LoggerFactory.getLogger(TablaAmortizacionBo.class);
    
    private final Integer TIPO_PAGOS = 26;
//    private final Double TASA_MENSUAL = 0.006923077;
//    private final Double TASA_MENSUAL_AUTO = 0.1;
    private final Double TASA_PORCENTAJE = .18;
//    private final Double TASA_PORCENTAJE = .08;
    private final Double TASA_AUTO_PORCENTAJE = .12;
    private final Integer TASA = 18;
//    private final Integer TASA = 8;
    private final Integer TASA_AUTO = 12;
    private final Integer TASA_ESPECIAL = 10;
    private final Double TASA_ESPECIAL_PORCENTAJE = .10;
    private Double montoTotalPagar = 0.00;
    private Double montoTotalInteres = 0.00;
    private Double montoTotalAmortizacion = 0.00;
    private Integer totalCatorcenasFaAgSau = 0;
    private List<AmortizacionDto> listaTotales;
    private SolicitudBo solBo;
    private AmortizacionDto ultAmortFAAGSAU;

     /**
      * Metodo que genera la tabla de amortización
      * @param noPrimerPago - Sólo recibe un número diferente de cero cuando se manda llamar desde pagos a capital
      * @param montoPrestamo
      * @param noPagos
      * @param fechaPrimerPago
     * @param tipoCredito
      * @return 
      */
    public List<AmortizacionDto> generaTablaAmortizacion(Integer noPrimerPago, Double montoPrestamo, Integer noPagos, Date fechaPrimerPago, int tipoCredito, Integer tasaEspecial) {
        listaTotales = new ArrayList<>();
        List<AmortizacionDto> datos = new ArrayList<>();

        Double montoPago;
        Double capital = 0.0;
        Double interes;
        Double amortizacion = 0.0;
        Date fechaAux = fechaPrimerPago;

        Double totalMontoPago = 0.0;
        Double totalMontoPagoAux = 0.0;
        Double totalInteres = 0.0;
        Double totalAmortizacion = 0.0;
        Double totalIva = 0.0;

        for (int i = 0; i < noPagos; i++) {
            AmortizacionDto amoLcl = new AmortizacionDto();

            montoPago = generaMontoPago(montoPrestamo, noPagos, tipoCredito, tasaEspecial);
            
            if (i == 0) {
                capital = montoPrestamo;

            } else {
                capital = capital - amortizacion;

            }
            interes = generaInteres(capital, tipoCredito, tasaEspecial);
            amortizacion = generaAmortizacion(montoPago, 0.0, interes);

            amoLcl.setAmoNumeroPago(noPrimerPago++);
            amoLcl.setAmoIva(0.0);
            amoLcl.setAmoMontoPago(new BigDecimal(montoPago).setScale(2, RoundingMode.DOWN).doubleValue());

            amoLcl.setAmoInteres(new BigDecimal(interes).setScale(2, RoundingMode.DOWN).doubleValue());
            amoLcl.setAmoAmortizacion(new BigDecimal(amortizacion).setScale(2, RoundingMode.DOWN).doubleValue());
            amoLcl.setAmoCapital(new BigDecimal(capital).setScale(2, RoundingMode.UP).doubleValue());



            totalMontoPago += amoLcl.getAmoMontoPago();
            totalIva += amoLcl.getAmoIva();
            totalInteres += amoLcl.getAmoInteres();
            totalAmortizacion += amoLcl.getAmoAmortizacion();



            if (fechaPrimerPago != null) {

                fechaAux = generaFecha(fechaAux, i);
                amoLcl.setAmoFechaPagoCredito(fechaAux);
                

            }

           /*
            * Cuando el idUsuario es diferente de nulo se asigna a la amortizacion en amousuid
            * para que pueda insertarse a la base con los datos necesarios
            */
            if(idUsuario!=null){
                amoLcl.setAmoUsuId(idUsuario);
            }
            
            datos.add(amoLcl);

            LOGGER.info("  # pagos:  " + amoLcl.getAmoNumeroPago()+ "Capital:  " + amoLcl.getAmoCapital() + "  Amortizacion:  " + amoLcl.getAmoAmortizacion() + "  Interes:  " + amoLcl.getAmoInteres() + "  total pago:  " + amoLcl.getAmoMontoPago() + "  fechas:  " + amoLcl.getAmoFechaPago() +  "  fecha:  " + amoLcl.getAmoFechaPagoCredito());
            
            amoLcl = null;
        }
        totalMontoPagoAux = totalMontoPago;
        
        for(AmortizacionDto amoDto : datos){
            totalMontoPagoAux -=amoDto.getAmoMontoPago();
            amoDto.setAmoSaldo(totalMontoPagoAux);
        }

        setMontoTotalPagar(new BigDecimal(totalMontoPago).setScale(2, RoundingMode.DOWN).doubleValue());
        setMontoTotalInteres(new BigDecimal(totalInteres).setScale(2, RoundingMode.DOWN).doubleValue());
        setMontoTotalAmortizacion(new BigDecimal(totalAmortizacion).setScale(2, RoundingMode.DOWN).doubleValue());
       // totalMontoPagoMasInteres = new BigDecimal(totalInteres + totalMontoPago).setScale(2, RoundingMode.DOWN).doubleValue();
        AmortizacionDto lastCredit = new AmortizacionDto();

        lastCredit.setAmoIva(new BigDecimal(totalIva).setScale(2, RoundingMode.DOWN).doubleValue());
        lastCredit.setAmoMontoPago(new BigDecimal(totalMontoPago).setScale(2, RoundingMode.DOWN).doubleValue());
        lastCredit.setAmoInteres(new BigDecimal(totalInteres).setScale(2, RoundingMode.DOWN).doubleValue());
        lastCredit.setAmoAmortizacion(new BigDecimal(totalAmortizacion).setScale(2, RoundingMode.DOWN).doubleValue());


        listaTotales.add(lastCredit);
//        if (fechaPrimerPago != null) {
//            datos.add(lastCredit);
//        }
        return datos;


    }

    
    public Double generaMontoPago(Double montoPrestamo, Integer noPagos, int tipoCredito, int tasaEspecial) {
        
        Double tasaMensual = 0.0;
    
        
        if(tipoCredito == 7){
            if(tasaEspecial == 0)
            tasaMensual = new BigDecimal(TASA_AUTO_PORCENTAJE/TIPO_PAGOS.doubleValue()).setScale(9, RoundingMode.UP).doubleValue();
            else
            tasaMensual = new BigDecimal(TASA_ESPECIAL_PORCENTAJE/TIPO_PAGOS.doubleValue()).setScale(9, RoundingMode.UP).doubleValue();
        }else{
             if(tasaEspecial == 0)
            tasaMensual = new BigDecimal(TASA_PORCENTAJE/TIPO_PAGOS.doubleValue()).setScale(9, RoundingMode.UP).doubleValue();
             else
            tasaMensual = new BigDecimal(TASA_ESPECIAL_PORCENTAJE/TIPO_PAGOS.doubleValue()).setScale(9, RoundingMode.UP).doubleValue();
            
        }
        System.out.println(tasaMensual);
        Double  potencia = Math.pow(1 + tasaMensual, noPagos);

        Double  montoPago = montoPrestamo * (tasaMensual * (potencia) / ((potencia) - 1));
        
        return montoPago;
    }
    

    public List<AmortizacionDto> generaTablaAmortizacionAgFA(Double monto, List<Date> catorcenas) {
        List<AmortizacionDto> creditos = new ArrayList<>();
        Double sumaInteres = 0.0;
        Integer noPago = 1;
        
        for (Date fecha : catorcenas) {
            AmortizacionDto cred = new AmortizacionDto();
            cred.setAmoCapital(new BigDecimal(monto).setScale(2, RoundingMode.DOWN).doubleValue());
            cred.setAmoNumeroPago(noPago);
            cred.setAmoMontoPago(0.0);
            cred.setAmoIva(0.0);
            cred.setAmoAmortizacion(0.0);
            //Aqui le mando 45 para indicar que es producto 4 o 5
            cred.setAmoInteres(new BigDecimal(generaInteres(monto, 45, 0)).setScale(2, RoundingMode.DOWN).doubleValue());
            cred.setAmoFechaPago(fecha);
            cred.setAmoFechaPagoCredito(catorcenas.get(catorcenas.size() - 1));
            creditos.add(cred);
            sumaInteres += cred.getAmoInteres();
            noPago++;

        }
        /*
         * ESTE BLOQUE AÑADE LOS TOTALES
         */
        
        ultAmortFAAGSAU = new AmortizacionDto();

        setMontoTotalPagar(new BigDecimal(sumaInteres + monto).setScale(2, RoundingMode.DOWN).doubleValue());
        setMontoTotalInteres(new BigDecimal(sumaInteres).setScale(2, RoundingMode.DOWN).doubleValue());
        setTotalCatorcenasFaAgSau(catorcenas.size());
        
        ultAmortFAAGSAU.setAmoInteres(getMontoTotalInteres());
        ultAmortFAAGSAU.setAmoMontoPago(getMontoTotalPagar());
        ultAmortFAAGSAU.setAmoNumeroPago(1);
        ultAmortFAAGSAU.setAmoCapital(monto);
        ultAmortFAAGSAU.setAmoIva(0.0);
        ultAmortFAAGSAU.setAmoAmortizacion(0.0);
        ultAmortFAAGSAU.setAmoUsuId(idUsuario);
        if(!catorcenas.isEmpty()) {
            ultAmortFAAGSAU.setAmoFechaPagoCredito(catorcenas.get(catorcenas.size() - 1));
        }
//        creditos.add(cred); Se comento la linea para qe no añada los totales a la amortizacion
        return creditos;
    }

    private Double generaInteres(Double capital, Integer tipoCredito, int tasaEspecial) {
        if(tasaEspecial == 0){
       
            if(tipoCredito == 7){
            return capital * TASA_AUTO / (TIPO_PAGOS * 100);
            }
            return capital * TASA / (TIPO_PAGOS * 100);
        }else{
            return capital * TASA_ESPECIAL / (TIPO_PAGOS * 100);
        }
    }

    private Double generaAmortizacion(Double pago, Double iva, Double interes) {

        return pago - interes - iva;

    }

    private Date generaFecha(Date fecha, Integer i) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        if (i == 0) {
            calendar.add(Calendar.DAY_OF_MONTH, 0);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 14);
        }
        Date fechaFinal = calendar.getTime();

        return fechaFinal;

    }

    /**
     * Método que determina qué tipo de amortización aplicará dependiendo del tipo de crédito
     * @param solicitudDto
     * @param fechaPrimerPago
     * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException Cuando falla la consulta para amortizar creditos de FA o AG
     */
    public List<AmortizacionDto> aplicaCalcular(SolicitudCreditoDto solicitudDto, Date fechaPrimerPago, Integer tasaEspecial) throws BusinessException {
       List<AmortizacionDto> creditosAmort = null;
        
        if (solicitudDto.getSolProducto()== 7 || solicitudDto.getSolProducto() == 6) {
            creditosAmort = generaTablaAmortizacion(Constantes.PRIMER_PAGO,solicitudDto.getSolMontoSolicitado(), solicitudDto.getSolCatorcenas(), fechaPrimerPago, solicitudDto.getSolProducto(), tasaEspecial);
            
        } else if (solicitudDto.getSolProducto() == 5 || solicitudDto.getSolProducto() == 4) {
            List<Date> fechas;
          
               fechas = obtieneCatorcenasIntermedias(solicitudDto.getSolProducto(), solicitudDto.getSolFacatorcena(), solicitudDto.getSolFechaCreacion());
           
            creditosAmort = generaTablaAmortizacionAgFA(solicitudDto.getSolMontoSolicitado(), fechas);

        }
        
        return creditosAmort;
    }
    
   
  
    
      public List<AmortizacionDto> aplicaCalcular(Double prestamoOtorgado, Integer catorcenas, Date fechaPrimerPago, int idProducto, int tasaEspecial) {
       List<AmortizacionDto> creditosAmort ;
        
            creditosAmort = generaTablaAmortizacion(Constantes.PRIMER_PAGO,prestamoOtorgado, catorcenas, fechaPrimerPago, idProducto, tasaEspecial);
            
        
        return creditosAmort;
    }
      
      
  
/**
 * Se utiliza para crear la tabla de amortizacion de credito de FA o AG siempre y cuando
 * se cuente con la fecha de creacion de la solicitud (para usar este metodo la solicitud
 * ya debe estar previamente creada)
 * @param tipoSol
 * @param faCatorcena
 * @param fechaCreacion
 * @return
 * @throws IntegracionException 
 */
    public List<Date> obtieneCatorcenasIntermedias(int tipoSol,int faCatorcena, Date fechaCreacion) throws BusinessException {
        solBo = new SolicitudBo();
        Boolean julioSigAno = Util.determinarJulioAno();
        List<Date> fechas = null;


        if (tipoSol == 5) {
            fechas = solBo.consultaCatorcenasTotales(12, julioSigAno, fechaCreacion);
        } else if (tipoSol == 4 || tipoSol == 11) {
            fechas = solBo.consultaCatorcenasTotales(faCatorcena, julioSigAno, fechaCreacion);
        }


        
        return fechas;
    }
    
    
    /***
     * Se utiliza para el simulador de creditos en pantalla, para mostrar la amortizacion de un credito de FA
     * o AG que no ha sido creado en base de datos
     * @param tipoSol
     * @param faCatorcena
     * @param junioSigAno
     * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException 
     */
       public List<Date> obtieneCatIntrmdsSmldr(int tipoSol, Integer faCatorcena, Boolean junioSigAno) throws BusinessException{
            solBo = new SolicitudBo();
           List<Date> fechas = null;

        
            /*Cuando el prestamo es de aguinaldo*/
            if (tipoSol == 5) {
                fechas = solBo.consultaCatorcenasTotales(12, junioSigAno, null);
            /*Cuando el prestamo es de fondo de ahorro o seguro de auto*/
            } else if (tipoSol == 4 || tipoSol ==11) {
                fechas = solBo.consultaCatorcenasTotales(faCatorcena, junioSigAno, null);
            }

        
        return fechas;
    }
       
       

    public static void main(String args[]) {

        TablaAmortizacionBo bo = new TablaAmortizacionBo();
        Date fecha = new Date();
        System.out.println(fecha);
        List<AmortizacionDto> datos = bo.generaTablaAmortizacion(1, 150000.0, 78, new Date(), 7, 0);

        for (AmortizacionDto dato : datos) {
            System.out.println("  # pagos:  " + dato.getAmoNumeroPago() + "Capital:  " + dato.getAmoCapital() + "  Amortizacion:  " + dato.getAmoAmortizacion() + "  Interes:  " + dato.getAmoInteres() + "  total pago:  " + dato.getAmoMontoPago() + "  fechas:  " + dato.getAmoFechaPago() +  "  fecha:  " + dato.getAmoFechaPagoCredito());

        }
        System.out.println("xd" + bo.getMontoTotalPagar());


    }

    /**
     * @return the montoTotalPagar
     */
    public Double getMontoTotalPagar() {
        return montoTotalPagar;
    }

    /**
     * @param montoTotalPagar the montoTotalPagar to set
     */
    public void setMontoTotalPagar(Double montoTotalPagar) {
        this.montoTotalPagar = montoTotalPagar;
    }

    /**
     * @return the listaTotales
     */
    public List<AmortizacionDto> getListaTotales() {
        return listaTotales;
    }

    /**
     * @param listaTotales the listaTotales to set
     */
    public void setListaTotales(List<AmortizacionDto> listaTotales) {
        this.listaTotales = listaTotales;
    }

    /**
     * @return the montoTotalInteres
     */
    public Double getMontoTotalInteres() {
        return montoTotalInteres;
    }

    /**
     * @param montoTotalInteres the montoTotalInteres to set
     */
    public void setMontoTotalInteres(Double montoTotalInteres) {
        this.montoTotalInteres = montoTotalInteres;
    }

    /**
     * @return the montoTotalAmortizacion
     */
    public Double getMontoTotalAmortizacion() {
        return montoTotalAmortizacion;
    }

    /**
     * @param montoTotalAmortizacion the montoTotalAmortizacion to set
     */
    public void setMontoTotalAmortizacion(Double montoTotalAmortizacion) {
        this.montoTotalAmortizacion = montoTotalAmortizacion;
    }

    /**
     * @return the idUsuario
     */
    public Integer getIdUsuario() {
        return idUsuario;
    }

    /**
     * @param idUsuario the idUsuario to set
     */
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * @return the ultAmortFAAGSAU
     */
    public AmortizacionDto getUltAmortFAAGSAU() {
        return ultAmortFAAGSAU;
    }

    /**
     * @param ultAmortFAAGSAU the ultAmortFAAGSAU to set
     */
    public void setUltAmortFAAGSAU(AmortizacionDto ultAmortFAAGSAU) {
        this.ultAmortFAAGSAU = ultAmortFAAGSAU;
    }

    /**
     * @return the totalCatorcenasFaAgSau
     */
    public Integer getTotalCatorcenasFaAgSau() {
        return totalCatorcenasFaAgSau;
    }

    /**
     * @param totalCatorcenasFaAgSau the totalCatorcenasFaAgSau to set
     */
    public void setTotalCatorcenasFaAgSau(Integer totalCatorcenasFaAgSau) {
        this.totalCatorcenasFaAgSau = totalCatorcenasFaAgSau;
    }
}
