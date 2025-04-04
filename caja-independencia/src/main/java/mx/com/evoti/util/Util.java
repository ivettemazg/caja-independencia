/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.util.Biblioteca;
import mx.com.evoti.dao.CatorcenasDao;
import mx.com.evoti.dao.exception.IntegracionException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ivette Manzano
 */
public class Util {

     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Util.class);
    public static final BigDecimal ONEHUNDERT = new BigDecimal(100);
   private static Biblioteca bib = new Biblioteca();

    public static BigDecimal percentage(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).divide(ONEHUNDERT);
    }

    public static final Long genUUID() {
        UUID myuuid = UUID.randomUUID();
        
        long highbits = myuuid.getLeastSignificantBits();

        return highbits;
    }

    
    public static int diferenciaEnDias(Date fechaMayor, Date fechaMenor) {
        /* CREAMOS LOS OBJETOS GREGORIAN CALENDAR PARA EFECTUAR LA RESTA */
        GregorianCalendar date1 = new GregorianCalendar();
        date1.setTime(fechaMenor); //dateIni es el objeto Date
        GregorianCalendar date2 = new GregorianCalendar();
        date2.setTime(fechaMayor); //dateFin es el objeto Date
        int rango;
        /* COMPROBAMOS SI ESTAMOS EN EL MISMO AÑO */
        if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)) {
            rango = date2.get(Calendar.DAY_OF_YEAR) - date1.get(Calendar.DAY_OF_YEAR);
        } else {
            /* SI ESTAMOS EN DISTINTO AÑO COMPROBAMOS QUE EL AÑO DEL DATEINI NO SEA BISIESTO
             * SI ES BISIESTO SON 366 DIAS EL AÑO
             * SINO SON 365
             */
            int diasAnyo = date1.isLeapYear(date1.get(Calendar.YEAR)) ? 366 : 365;
            /* CALCULAMOS EL RANGO DE AÑOS */
            int rangoAnyos = date2.get(Calendar.YEAR) - date1.get(Calendar.YEAR);
            /* CALCULAMOS EL RANGO DE DIAS QUE HAY */
            rango = (rangoAnyos * diasAnyo) + (date2.get(Calendar.DAY_OF_YEAR) - date1.get(Calendar.DAY_OF_YEAR));
        }
        return rango;
    }
    
     public static Boolean determinarJulioAno() {
        boolean julioSigAno = Boolean.FALSE;
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int month = cal.get(Calendar.MONTH)+1;


        if (month >= 7 && month <= 12) {
            julioSigAno = true;
        }
        return julioSigAno;
    }
     
     /**
      * Obtiene el año al que se va a calcular cuando es julio, recibe
      * una fecha
      * @param fecha
      * @return 
      */
      public static Boolean determinarJulioAno(Date fecha) {
        boolean julioSigAno = Boolean.FALSE;
    
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);

        int month = cal.get(Calendar.MONTH)+1;


        if (month >= 7 && month <= 12) {
            julioSigAno = true;
        }
        return julioSigAno;
    }
    
    public static String generaFechaFormateada(){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fechaHoy = sdf.format(new Date());
            
            return fechaHoy;
       }
    public static String generaFechaFormateada(Date fecha){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String sFecha = sdf.format(fecha);
            
            return sFecha;
       }
    public static String generaFechaFormateada(Date fecha,String formato){
            SimpleDateFormat sdf = new SimpleDateFormat(formato);
            String sFecha = sdf.format(fecha);
            
            return sFecha;
       }
    public static Date generaFechaDeString(String sFecha){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fecha = null;
            try {
                fecha = sdf.parse(sFecha);
            } catch (ParseException ex) {
                LOGGER.info("Error al intentar convertir String en DAte");
            }
            
            return fecha;
       }
    
     public static Date generaFechaDeString(String sFecha, String format){
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date fecha = null;
            try {
                fecha = sdf.parse(sFecha);
            } catch (ParseException ex) {
                LOGGER.info("Error al intentar convertir String en DAte");
            }
            
            return fecha;
       }
    
    
    public static int obtenerUltimoDiaMes (int anio, int mes) {

        Calendar cal=Calendar.getInstance();
        cal.set(anio, mes-1, 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);

   }
   
    
     public static void ordena(List lista, final String propiedad) {
        Collections.sort(lista, new Comparator() {
            @Override
            public int compare(Object obj1, Object obj2) {

                Class clase = obj1.getClass();
                String getter = "get" + Character.toUpperCase(propiedad.charAt(0)) + propiedad.substring(1);
                try {
                    Method getPropiedad = clase.getMethod(getter);

                    Object propiedad1 = getPropiedad.invoke(obj1);
                    Object propiedad2 = getPropiedad.invoke(obj2);

                    if (propiedad1 instanceof Comparable && propiedad2 instanceof Comparable) {
                        Comparable prop1 = (Comparable) propiedad1;
                        Comparable prop2 = (Comparable) propiedad2;
                        return prop1.compareTo(prop2);
                    }//CASO DE QUE NO SEA COMPARABLE  
                    else {
                        if (propiedad1.equals(propiedad2)) {
                            return 0;
                        } else {
                            return 1;
                        }

                        /**
                         * Este metodo asigna el cluster validando la ambiguedad
                         */
                    }
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    LOGGER.info(e.getMessage(), e);

                }
                return 0;
            }
        });
    }
    
     /**
      * Si se manda un número negativo de días los restará, si se manda un número positivo
      * sumara días
      * @param fecha
      * @param dias
      * @return fechaModificada
      */
     public static Date sumarORestarDiasFecha(Date fecha, int dias){
	
      Calendar calendar = Calendar.getInstance();
	
      calendar.setTime(fecha); // Configuramos la fecha que se recibe
	
      calendar.add(Calendar.DAY_OF_YEAR, dias);  // numero de días a añadir, o restar en caso de días<0
	
      return calendar.getTime(); // Devuelve el objeto Date con los nuevos días añadidos
	
 }
    
     /**
      * Si se manda un número negativo de meses los restará, si se manda un número positivo
      * sumara meses
      * @param fecha
      * @param meses
      * @return fechaModificada
      */
     public static Date sumarORestarMesesFecha(Date fecha, int meses){
	
      Calendar calendar = Calendar.getInstance();
	
      calendar.setTime(fecha); // Configuramos la fecha que se recibe
	
      calendar.add(Calendar.MONTH, meses);  // numero de meses a añadir, o restar en caso de meses<0
	
      return calendar.getTime(); // Devuelve el objeto Date con los nuevos meses añadidos
	
 }
     
     
            /**
     * Este metodo limpia la cadena de caracteres especiales
     * @param cadena
     * @return 
     */
    public static String limpiaCaracteres(String cadena){
       
        if(cadena==null){
            cadena="";
        }
        for(int i=0; i<bib.SIMBOLOS_OMITIBLES.length; i++){
           cadena= cadena.replace(bib.SIMBOLOS_OMITIBLES[i], "");
        }
        return cadena.trim().toLowerCase();
    }
     
     /**
     * Valida si la fecha que se esta mandando es catorcena
     *
     * @param catorcena
     * @return
     * @throws BusinessException
     */
    public static Boolean validaSiEsCatorcena(Date catorcena) throws BusinessException {
        CatorcenasDao dao = new CatorcenasDao();
        Date catorcenaExacta = null;
        try {
            catorcenaExacta = dao.obtieneCatorcenaExacta(catorcena);
        } catch (IntegracionException ex) {
           
            throw new BusinessException(ex.getMessage(), ex);
        }

        if (catorcenaExacta != null) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }
    
    /**
     * Redondea un valor double utilizando las funciones de BigDecimal
     * setscale(2, RoundingMode.HALF_DOWN)
     * @param dbl
     * @return 
     */
    public static Double round(Double dbl){
        BigDecimal bd = new BigDecimal(dbl);
     return bd.setScale(2, RoundingMode.HALF_DOWN).doubleValue();
    }
    
    
    /**
     * Copia y pega un archivo
     * @param src - ruta de donde se copia
     * @param dst - ruta destino
     * @throws IOException 
     */
    public static void copyFileTempPath(File src, String dst) throws IOException{
 
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst+src.getName());
 
		byte[] b = new byte[1024];
		int l;
		while((l = in.read(b)) >0){
			out.write(b, 0, l);
		}
 
                out.close();
                
	} 
    
    
    public static void deleteTempFile(String fileName, String dst) throws IOException{
            File file = new File(dst+fileName);
            
            if(file.exists()){
                
                file.delete(); 
            }
            
            LOGGER.info("El archivo "+file.getAbsolutePath()+" ha sido borrado.");
    } 
    
    public static Date restaHrToDate(Date fecha, Integer horas){
        Calendar cal = Calendar.getInstance();
		cal.setTime(fecha);
        
        System.out.println(fecha);        
                
        Date tempDate = cal.getTime();
                
        cal.set(Calendar.HOUR, cal.get(Calendar.HOUR)- horas);
        tempDate = cal.getTime();
	System.out.println("Hora modificada: " + tempDate);
        
        return tempDate;
    }
    
    public static void main(String args []){
        Util.restaHrToDate(new Date(), 6);
    }
    
}
