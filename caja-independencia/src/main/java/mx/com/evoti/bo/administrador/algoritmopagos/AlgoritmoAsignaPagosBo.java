/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.administrador.algoritmopagos;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.AlgoritmoAsignaPagosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.algoritmopagos.PagoAmortizacionDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.hibernate.pojos.ArchivosHistorial;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class AlgoritmoAsignaPagosBo implements Serializable{
    
    private static final long serialVersionUID = 4143093977511796258L;
     private static final Logger LOGGER = LoggerFactory.getLogger(AlgoritmoAsignaPagosBo.class);
     
     private AlgoritmoAsignaPagosDao aAPagdao;

    public AlgoritmoAsignaPagosBo() {
        aAPagdao = new AlgoritmoAsignaPagosDao();
    }
    
    /**
     * Metodo que ejecuta el algoritmo de asignacion de pagos
     * @param arhId
     * @throws BusinessException 
     */
    public void initAlgoritmoAsignacion(int arhId) throws BusinessException{
        
         //Aplica pagos exactos
         LOGGER.info("________________________________________________________");
         LOGGER.info("Aplicando pagos exactos");
            aplicaPagos(arhId,2);
         //Aplica pagos menores
         LOGGER.info("________________________________________________________");
         LOGGER.info("Aplicando pagos menores");
            aplicaPagos(arhId,3);
         //Aplica pagos mayores       
         LOGGER.info("________________________________________________________");
         LOGGER.info("Aplicando pagos mayores");
            aplicaPagos(arhId,4);
            
         //Aplica estatus 7 a los pagos (pagos sin credito para asignar)
         LOGGER.info("________________________________________________________");
         LOGGER.info("Aplicando pagos sin creditos");
            updtPagEstSinAmortizacion(arhId);
            
         /*
            Este metodo vuelve a obtener los pagos mayores que ya ha asignado
            para determinar si alguno de ellos pueden convertirse en estatus
            5 o 6
         */
         LOGGER.info("________________________________________________________");
         LOGGER.info("Aplicando pagos 5 y 6");
            aplicaPagosEst5y6(arhId);
            
             
            /**
             * Actualiza el estatus del crédito, si es que ya se terminaron de pagar todas
             * sus catorcenas
             */
            
         LOGGER.info("________________________________________________________");
         LOGGER.info("Actualiza el estatus del crédito");
            actualizaEstatusCredito();
            
            /**
             * Actualiza el estatus del archivo que se está procesando
             */
         LOGGER.info("________________________________________________________");
         LOGGER.info("Actualizando el estatus del archivo");
            actualizaEstatusArchivo(arhId);
            
    }
    
    
    /**
     * Metodo que hace la asignacion de pagos a amortización con estatus 2, 3 y 4 
     * (exactos, mayores, menores)
     * @param arhId 
     */
    private void aplicaPagos(int arhId, int estatus) throws BusinessException{
        
        try {
            Boolean hayRepetidos = Boolean.TRUE;
            
             while(hayRepetidos){
                  List idPagosRepetidos = ejecutaUpdateXEstatus(arhId, estatus);
                  
                  if(!idPagosRepetidos.isEmpty()){
                    limpiaAmortizaciones(idPagosRepetidos);
                  }else{//Si no hay repetidos que salga del ciclo
                      hayRepetidos = Boolean.FALSE;
                  }
                  
                  
                 
             }
             
             /**
                   * Cuando es pago mayor, arregla los montos de los acumulados, ya que
                   * en ocasiones queda incorrecto al hacer la limpieza de amortizacion
                   */
                  if(estatus==4){
                      arreglaAcumuladoPagoMayor(arhId);
                  }
        } catch (IntegracionException ex) {
            LOGGER.error("Error al actualizar en estatus "+estatus);
            throw new BusinessException("Error al actualizar en estatus "+estatus, ex);
        }
        
    }
    
    
   
     
    /**
     * Actualiza las tablas amortizacion y pagos en los estatus
     * PAGO EXACTO, PAGO MENOR Y PAGO MAYOR
     * @param arhId
     * @param estatus
     * @return
     * @throws IntegracionException 
     */
    private List ejecutaUpdateXEstatus(int arhId, int estatus) throws IntegracionException{
        switch (estatus){
            case 2:
                aAPagdao.updtAmortizacionExacto(arhId);
                 
                break;
            case 3:
                aAPagdao.updtPagoMenor(arhId);
                break;
            case 4:
                aAPagdao.updtPagoMayor(arhId);
                break;
        }
        
        aAPagdao.updtPagosEstatus(arhId, estatus);
                    
        List idPagos = aAPagdao.getPagosRepetidosAmortizacion(arhId);
        return idPagos;
    }
 
    /**
     * Metodo que actualiza las amortizaciones que tienen el mismo id de pago
     * @param idPagosRepetidos
     * @throws IntegracionException 
     */
    private void limpiaAmortizaciones(List idPagosRepetidos ) throws IntegracionException{
         
                
                for(int i =0; i<idPagosRepetidos.size();i++){
                    
                    int idPago = (int) idPagosRepetidos.get(i);
                    
                    List idAmortizaciones = aAPagdao.getAmortizacionesCPagRep(idPago);
                    
                    for(int j=0; j<idAmortizaciones.size();j++){
                        //Que el primer registro no lo actualice
                        if(j>0){
                            aAPagdao.updtAmortizacionPagoId((int) idAmortizaciones.get(j));
                        }
                    }
                    
                }
                
            
    }

    /**
     * Actualiza todos los pagos que no tuvieron amortizacion a pag_estatus = 7
     * y pag_acumulado = pag_deposito
     * @param arhId
     * @throws BusinessException 
     */
    private void updtPagEstSinAmortizacion(int arhId) throws BusinessException{
        try {
            aAPagdao.updtPagEstSinAmortizacion(arhId);
        } catch (IntegracionException ex) {
           LOGGER.error("Error al actualizar los pagos estatus 7");
            throw new BusinessException("Error al actualizar los pagos estatus 7", ex);
        }
    }
   
    /**
     * Aplica los estatus 6 y 7 a la amortizacion y a los pagos
     * @param arhId
     * @throws BusinessException 
     */
     private void aplicaPagosEst5y6(int arhId) throws BusinessException {
        try {
            
            /*
            Se obtienen los usuarios que tienen pagos mayores y que tienen mas de un credito
            */
            List<PagoAmortizacionDto> lstUsrsPagosMayores = aAPagdao.getUsuEst5y6(arhId);
            
            /*
            Se recorre esa lista haciendo una busqueda de las amortizaciones que hay pendientes
            de ese usuario y que el monto de esas amortizaciones sea menor o igual al acumulado
            del pago mayor
            */
            for(PagoAmortizacionDto dtoPago: lstUsrsPagosMayores){
                
                if(dtoPago.getPagId() == 90298){
                    System.out.println("paraaqui");
                }
                BigDecimal acumBD = new BigDecimal(dtoPago.getPagAcumulado());
                acumBD = acumBD.setScale(2, BigDecimal.ROUND_UP);
                List<AmortizacionDto> lstAmortizacion = aAPagdao.getAmortEstatus5y6(dtoPago.getPagUsuId(), acumBD, dtoPago.getPagFecha());
                
                double diferencia;
                double acumulado = dtoPago.getPagAcumulado();
                
                /*
                    Se recorre la lista de amortizaciones pendientes
                */
                for(AmortizacionDto dtoAmort:lstAmortizacion){
                    
                   diferencia = acumulado-dtoAmort.getAmoMontoPago();
                   BigDecimal bdDiferencia = new BigDecimal(diferencia);
                   bdDiferencia = bdDiferencia.setScale(2, BigDecimal.ROUND_UP);
                    
                    if(diferencia <= 1 && diferencia >=-1){
                        aAPagdao.updtPagoEst5y6(dtoPago.getPagId(), 5, bdDiferencia);
                        aAPagdao.updtAmortizacionPagoIdEst5y6(dtoPago.getPagId(),dtoAmort.getAmoId());

                        acumulado = diferencia;
                        
                    }else if(diferencia > 1){
                        aAPagdao.updtPagoEst5y6(dtoPago.getPagId(), 6, bdDiferencia);
                        aAPagdao.updtAmortizacionPagoIdEst5y6(dtoPago.getPagId(),dtoAmort.getAmoId());
                        
                        acumulado = diferencia;
                    }
                    
                }
            }
            
            /*
            Actualiza las amortizaciones a estatus 2 PAGADO en todas aquellas
            catorcenas que coincidan con pagos con estatus 5 y 6
            */
             aAPagdao.updtAmortizacionPagadosIn5y6(arhId);
            
        } catch (IntegracionException ex) {
           LOGGER.error("Error al actualizar los pagos estatus 5,6");
            throw new BusinessException("Error al actualizar los pagos estatus 5,6", ex);
        }
    }
     
      public void actualizaEstatusCredito() throws BusinessException {
        try {
            aAPagdao.updtCreditosPagados();
        } catch (IntegracionException ex) {
           LOGGER.error("Error al actualizar el estatus del credito a 2");
            throw new BusinessException("Error al actualizar el estatus del credito a 2", ex);
        }
        
    }
 
      /**
       *  Actualiza el estatus del archivo
       * @param arhId
       * @throws BusinessException 
       */
      private void actualizaEstatusArchivo(int arhId) throws BusinessException {
        try {
            aAPagdao.updtEstatusArchivo(arhId);
        } catch (IntegracionException ex) {
           LOGGER.error("Error al actualizar el estatus del archivo a 2");
            throw new BusinessException("Error al actualizar el estatus del archivo a 2", ex);
        }
    }
      
      /**
     * En ocasiones el acumulado en pagos mayores es incorrecto, este metodo
     * funciona para poner el acumulado correctamente
     * @param arhId
     * @throws BusinessException 
     */
      private void arreglaAcumuladoPagoMayor(int arhId) throws BusinessException {
             try {
            aAPagdao.fixAcumPagosMayores(arhId);
        } catch (IntegracionException ex) {
           LOGGER.error("Error al actualizar el estatus del archivo a 2");
            throw new BusinessException("Error al actualizar el estatus del archivo a 2", ex);
        }
    }
      
    public static void main(String args[]) throws IOException{
        Properties props = new Properties();
        props.setProperty("log4j.rootLogger","console");
        props.setProperty("log4j.logger.org.hibernate","INFO");
        props.setProperty("log4j.logger.org.hibernate.type","ERROR");
        props.setProperty("log4j.appender.console","org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.console.Target","System.out");
        props.setProperty("log4j.appender.console.layout","org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.console.layout.ConversionPattern","%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");

        PropertyConfigurator.configure(props);
//        BasicConfigurator.configure();
        HibernateUtil.buildSessionFactory2();
        AlgoritmoAsignaPagosBo bo =  new AlgoritmoAsignaPagosBo();
        AlgoritmoAsignaPagosDao dao = new AlgoritmoAsignaPagosDao();
        try {
            
           
            List<ArchivosHistorial> archivos =dao.getArchivosPagos();
                for(ArchivosHistorial ah: archivos){
                    System.out.println("#########################################################");
                    System.out.println("Procesando archivo "+ah.getArhId());
                    bo.initAlgoritmoAsignacion(ah.getArhId());
                }
        } catch (BusinessException ex) {
           ex.printStackTrace();
        } catch (IntegracionException ex) {
             ex.printStackTrace();
        }finally{
             HibernateUtil.closeSessionFactory();
        }
    }

    

    

   
    
    
}
