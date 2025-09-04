/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.PagoDto;
import mx.com.evoti.dto.FormatoArchivoValidacion;
import mx.com.evoti.util.Util;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
// import org.apache.poi.ss.util.CellRangeAddress; // No se usa actualmente
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
public class LectorExcel implements Serializable{

    private static Logger LOGGER = LoggerFactory.getLogger(LectorExcel.class);
    static boolean antiguo = false;
    private static final long serialVersionUID = 8507779321588130043L;

    /**
     *
     * Este metodo es usado para leer archivos Excel El tipoArchivo indicará si
     * el archivo es de pagos (1) o aportaciones (2)
     *
     * @param rutaArchivo
     * @param tipoArchivo - Nombre de archivo Excel.
     * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public static List leerArchivoExcel(String rutaArchivo, int tipoArchivo) throws BusinessException {
        /**
         * Crea una nueva instancia de Lista_Datos_Celda
         */
        List listaDatosCelda = new ArrayList();
        List lstTransformadaFinal = new ArrayList();

        if (rutaArchivo != null && rutaArchivo.toLowerCase().endsWith(".xlsx")) {
            lstTransformadaFinal = leerXslx(rutaArchivo, listaDatosCelda, tipoArchivo);
            antiguo = false;
        } else {
            throw new BusinessException("Solo se admiten archivos .xlsx.");
        }

        /**
         * Llama el metodo Imprimir_Consola para imprimir los datos de la celda
         * en la consola.
         */
        imprimeConsola(listaDatosCelda);
        return lstTransformadaFinal;
    }

    private static List leerXslx(String nombreArchivo, List<List> lstDatosCelda, int tipoArchivo) throws BusinessException {
        List lstResultadosFinal = new ArrayList();
        try (FileInputStream fileInputStream = new FileInputStream(nombreArchivo);
             XSSFWorkbook libroExcel = new XSSFWorkbook(fileInputStream)) {

            XSSFSheet hoja = libroExcel.getSheetAt(0);

            // Iterar filas y celdas
            Iterator filaIterator = hoja.rowIterator();
            while (filaIterator.hasNext()) {
                XSSFRow fila = (XSSFRow) filaIterator.next();
                Iterator filaIt = fila.cellIterator();
                List<XSSFCell> lstCeldaTemp = new ArrayList<>();

                while (filaIt.hasNext()) {
                    XSSFCell celda = (XSSFCell) filaIt.next();
                    lstCeldaTemp.add(celda);
                }

                // Omitir filas completamente vacías (sin celdas definidas)
                if (lstCeldaTemp.isEmpty()) {
                    continue;
                }

                if (tipoArchivo == 1) {
                    // Valida que el archivo de pagos tenga las 5 columnas necesarias
                    transformCellToPagoDto(lstCeldaTemp, lstResultadosFinal);
                } else {
                    // Valida que el archivo de aportaciones tenga las 6 columnas necesarias
                    transformCellToAportacionDto(lstCeldaTemp, lstResultadosFinal);
                }

                lstDatosCelda.add(lstCeldaTemp);
                System.out.println(lstCeldaTemp.size());
            }

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return lstResultadosFinal;
    }

    /**
     * Valida el formato de un archivo Excel antes de procesarlo.
     * Reglas inferidas por los archivos adjuntos:
     *  - Tipo 1 (Pagos): columnas [0..4] -> [ClaveEmpleado(NUM), Fecha(DATE), Monto(NUM/CURRENCY), Empresa(NUM), Nombre(TEXT)]
     *  - Tipo 2 (Aportaciones): columnas [0..5] -> anteriores + Producto(NUM)
     * Además contabiliza filas vacías (sin celdas definidas) para avisar al usuario.
     *
     * No modifica datos; solo reporta si el archivo es válido y cuántas filas vacías trae.
     */
    public static FormatoArchivoValidacion validarFormatoExcel(String rutaArchivo, int tipoArchivo) throws BusinessException {
        FormatoArchivoValidacion res = new FormatoArchivoValidacion();
        res.setValido(true);
        res.setFilasVacias(0);

        try (FileInputStream fis = new FileInputStream(rutaArchivo); XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            XSSFSheet sheet = wb.getSheetAt(0);

            int erroresMaxAMostrar = 10;
            int filasConsideradas = 0;
            int filasConCol6Presente = 0;
            int filasConCol6Numerica = 0;

            Iterator<?> filaIterator = sheet.rowIterator();
            while (filaIterator.hasNext()) {
                XSSFRow row = (XSSFRow) filaIterator.next();

                // Detectar fila totalmente vacía (sin celdas físicas)
                if (row == null || row.getPhysicalNumberOfCells() == 0) {
                    res.setFilasVacias(res.getFilasVacias() + 1);
                    continue;
                }

                filasConsideradas++;
                // Para detectar tipo por estructura (columna 6 = producto)
                  XSSFCell c6 = row.getCell(5);
                if (c6 != null) {
                    try {
                        // normalizar tipo (manejar fórmulas)
                        CellType t = c6.getCellType();
                        if (t == CellType.FORMULA) {
                            t = c6.getCachedFormulaResultType();
                        }
                        // contar como presente si no es BLANK
                        if (t != CellType.BLANK) {
                            filasConCol6Presente++;
                        }
                        if (t == CellType.NUMERIC) {
                            filasConCol6Numerica++;
                        }
                    } catch (Exception e) {
                        // ignorar fallas de lectura para heurística
                    }
                }

                // Validar columnas requeridas por tipo
                int requeridas = (tipoArchivo == 1) ? 5 : 6;
                for (int i = 0; i < requeridas; i++) {
                    XSSFCell cell = row.getCell(i);
                    int fila1 = row.getRowNum() + 1; // 1-based
                    int col1 = i + 1; // 1-based

                    if (cell == null) {
                        if (res.getErrores().size() < erroresMaxAMostrar) {
                            res.addError("Celda vacía en columna " + col1 + ", fila " + fila1 + ".");
                        }
                        continue;
                    }

                    try {
                        switch (i) {
                            case 0: // Clave empleado NUM
                                cell.getNumericCellValue();
                                break;
                            case 1: // Fecha DATE (Excel almacena como NUMERIC con formato)
                                cell.getDateCellValue();
                                break;
                            case 2: // Monto NUM (acepta currency como texto)
                                try {
                                    limpiaMontos(cell.getStringCellValue());
                                } catch (IllegalStateException iseMonto) {
                                    cell.getNumericCellValue();
                                }
                                break;
                            case 3: // Empresa NUM
                                cell.getNumericCellValue();
                                break;
                            case 4: // Nombre TEXT
                                cell.getStringCellValue();
                                break;
                            case 5: // Producto NUM (solo para tipo 2)
                                if (tipoArchivo == 2) {
                                    cell.getNumericCellValue();
                                }
                                break;
                        }
                    } catch (IllegalStateException ise) {
                        if (res.getErrores().size() < erroresMaxAMostrar) {
                            res.addError("Valor con tipo incorrecto en columna " + col1 + ", fila " + fila1 + ".");
                        }
                    } catch (Exception ex) {
                        if (res.getErrores().size() < erroresMaxAMostrar) {
                            res.addError("Valor inválido en columna " + col1 + ", fila " + fila1 + ".");
                        }
                    }
                }
            }

            // Heurística de detección de tipo real del archivo por presencia de columna 6 (Producto)
            double ratioPresente = (filasConsideradas == 0) ? 0.0 : (double) filasConCol6Presente / (double) filasConsideradas;
            double ratioNumerica = (filasConsideradas == 0) ? 0.0 : (double) filasConCol6Numerica / (double) filasConsideradas;
            Integer tipoDetectado = null; // 1 = Pagos, 2 = Aportaciones
            if (ratioNumerica >= 0.8) {
                tipoDetectado = 2;
            } else if (ratioPresente <= 0.2) {
                tipoDetectado = 1;
            }

            if (tipoDetectado != null && tipoDetectado.intValue() != tipoArchivo) {
                res.setValido(false);
                String esperado = (tipoArchivo == 1) ? "Pagos" : "Aportaciones";
                String detectado = (tipoDetectado == 1) ? "Pagos" : "Aportaciones";
                res.setMensajeUsuario("Ha seleccionado '" + esperado + "', pero el archivo parece ser de '" + detectado + "'. Suba el archivo correcto o cambie el tipo seleccionado.");
                return res;
            }

            if (!res.getErrores().isEmpty()) {
                res.setValido(false);
                StringBuilder sb = new StringBuilder();
                sb.append("El formato del archivo no es válido. Corrija los siguientes puntos y vuelva a intentar. ");
                for (int i = 0; i < res.getErrores().size(); i++) {
                    sb.append("\n").append("• ").append(res.getErrores().get(i));
                }
                if (res.getErrores().size() == erroresMaxAMostrar) {
                    sb.append("\n").append("(Se omitieron errores adicionales)");
                }
                res.setMensajeUsuario(sb.toString());
            } else {
                String tipoDesc = (tipoArchivo == 1) ? "Pagos" : "Aportaciones";
                if (res.getFilasVacias() > 0) {
                    res.setMensajeUsuario("Formato correcto para " + tipoDesc + ". Se detectaron " + res.getFilasVacias() + " filas vacías; serán ignoradas.");
                } else {
                    res.setMensajeUsuario("Formato correcto para " + tipoDesc + ".");
                }
            }

        } catch (IOException io) {
            throw new BusinessException("No fue posible leer el archivo para validación.", io);
        }

        return res;
    }

    /**
     * Metodo que transforma los datos de la celda obtenida en un objeto de tipo PagoDto
     * @param celda
     * @param lstPagosFinal
     * @throws BusinessException 
     */
    public static void transformCellToPagoDto(List<XSSFCell> celda, List lstPagosFinal) throws BusinessException {
        String error ;
        PagoDto pago = new PagoDto();
        for (int i = 0; i < celda.size(); i++) {
            switch (i) {
                
                case 0://Clave empleado
                    try {
                        Integer claveEmpleado = (int) celda.get(i).getNumericCellValue();

                        pago.setPagClaveEmpleado(claveEmpleado);
                    } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor " + celda.get(i).getStringCellValue() + " en la columna " + columna
                                + " y fila " + fila + " no tiene formato numérico, favor"
                                + " de verificar.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }
                    break;
                case 1://Fecha
                    try {
                        Date fechaPago = celda.get(i).getDateCellValue();
                        pago.setPagFecha(fechaPago);
                    } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor " + celda.get(i).getStringCellValue() + " en la columna " + columna
                                + " y fila " + fila + " no tiene formato de fecha, favor"
                                + " de verificar y subir de nuevo el archivo.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }
                    break;
                case 2://Monto
                    try {
                        Double montoPago = limpiaMontos(celda.get(i).getStringCellValue());
                        pago.setPagMonto(montoPago);
                      
                   } catch (IllegalStateException ise) {
                       
                        Double montoPago = celda.get(i).getNumericCellValue();
                        pago.setPagMonto(montoPago);
                        LOGGER.error(ise.getLocalizedMessage());
                    } catch (NumberFormatException nfe){
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor " + celda.get(i).getStringCellValue() + " en la columna " + columna
                                + " y fila " + fila + " no es una cantidad monetaria válida, favor"
                                + " de verificar y subir de nuevo el archivo.";
                        LOGGER.error(error);
                        throw new BusinessException(error, nfe.getCause());
                    }
                    break;
                case 3://Empresa
                    try {
                        Integer idEmpresa = (int) celda.get(i).getNumericCellValue();

                        pago.setPagEmpresa(idEmpresa);

                    } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor " + celda.get(i).getStringCellValue() + " en la columna " + columna
                                + " y fila " + fila + " no tiene formato numérico, favor"
                                + " de verificar y subir de nuevo el archivo.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }
                    break;
                case 4://Nombre
                    try{
                    String nombre = celda.get(i).getStringCellValue();
                    pago.setPagNombreUsuario(Util.limpiaCaracteres(nombre));
                    
                    break;
                        } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                          error = "El valor " + celda.get(i).getStringCellValue() + " en la columna " + columna
                                + " y fila " + fila + " que no tiene formato de texto, favor"
                                + " de verificar y subir de nuevo el archivo.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }

            }

            pago.setPagEstatus(1);
            pago.setPagAcumulado(0.0);
        }
        lstPagosFinal.add(pago);
    }

    public static void transformCellToAportacionDto(List<XSSFCell> celda, List lstPagosFinal) throws BusinessException {
         String error ;
        MovimientosDto aportacion = new MovimientosDto();
        for (int i = 0; i < celda.size(); i++) {
            switch (i) {
                 case 0: //Cve empleado
                    try {
                        Integer claveEmpleado = (int) celda.get(i).getNumericCellValue();
                        aportacion.setMovClaveEmpleado(claveEmpleado);
                    } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor en la columna " + columna
                                + " y fila " + fila + " no tiene formato numérico, favor"
                                + " de verificar.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }
                    
                    break;
                case 1://Fecha
                     try {
                        Date fechaPago = celda.get(i).getDateCellValue();
                        aportacion.setMovFecha(fechaPago);
                    } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor en la columna " + columna
                                + " y fila " + fila + " no tiene formato de fecha, favor"
                                + " de verificar y subir de nuevo el archivo.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }
                   
                    break;
                case 2://Monto aportacion
                    try {
                        Double montoPago = limpiaMontos(celda.get(i).getStringCellValue());
                        aportacion.setMovDeposito(montoPago);
                   
                        } catch (IllegalStateException ise) {
                       
                        Double montoPago = celda.get(i).getNumericCellValue();
                        aportacion.setMovDeposito(montoPago);
                        LOGGER.error(ise.getLocalizedMessage());
                    } catch (BusinessException nfe){
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor en la columna " + columna
                                + " y fila " + fila + " no tiene formato numérico, favor"
                                + " de verificar y subir de nuevo el archivo.";
                        LOGGER.error(error);
                        throw new BusinessException(error, nfe.getCause());
                        
                    }
                    
                    break;
                     case 3://Empresa
                    try {
                        Integer idEmpresa = (int) celda.get(i).getNumericCellValue();
                        aportacion.setMovEmpresa(idEmpresa);

                    } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor en la columna " + columna
                                + " y fila " + fila + " no tiene formato numérico, favor"
                                + " de verificar y subir de nuevo el archivo.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }
                    break;
                    
                case 4://Nombre
                    try{
                    String nombre = celda.get(i).getStringCellValue();
                    aportacion.setMovNombreEmpleado(Util.limpiaCaracteres(nombre));
                    break;
                        } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "Hay un valor en la columna " + columna
                                + " y fila " + fila + " que no tiene formato de texto, favor"
                                + " de verificar y subir de nuevo el archivo.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }
                case 5://Producto
                    try {
                        
                        Integer movProducto = (int) celda.get(i).getNumericCellValue();
                        aportacion.setMovProducto(movProducto);
                    } catch (IllegalStateException ise) {
                        int columna = celda.get(i).getColumnIndex() + 1;
                        int fila = celda.get(i).getRowIndex() + 1;
                        error = "El valor en la columna " + columna
                                + " y fila " + fila + " no tiene formato numérico, favor"
                                + " de verificar.";
                        LOGGER.error(error);
                        throw new BusinessException(error, ise.getCause());
                    }
                 break;
            }

            aportacion.setMovTipo("APORTACION");
        }
        lstPagosFinal.add(aportacion);
    }

    /**
     * Limpia un monto en formato "currency" para poderlo convertir en un Double
     * @param strMontoPago
     * @return 
     * @throws mx.com.evoti.bo.exception.BusinessException 
     */
    public static Double limpiaMontos(String strMontoPago) throws BusinessException{
        strMontoPago = strMontoPago.replace("$", "").trim();
        Double montoPago = null;
        try{
        /**
         * Cuando contiene puntos y comas, quita las comas
         */
        if (strMontoPago.contains(".") && strMontoPago.contains(",")) {

            strMontoPago = strMontoPago.replace(",", "");
            montoPago = Double.valueOf(strMontoPago);
            /**
             * Cuando no tiene puntos pero tiene comas, se reemplazan las comas
             * por puntos
             */
        } else if (!strMontoPago.contains(".") && strMontoPago.contains(",")) {
            strMontoPago = strMontoPago.replace(",", ".");
            montoPago = Double.valueOf(strMontoPago);
            /**
             * Cuando sólo tiene puntos pero no tiene comas, se convierte
             * directamente en double
             */
        } else if (strMontoPago.contains(".") && !strMontoPago.contains(",")) {
            montoPago = Double.valueOf(strMontoPago);
        }
        } catch (NumberFormatException nfe){
            montoPago = 0.0;
            throw new BusinessException(nfe.getMessage(), nfe);
        }
        return montoPago;
    }

    private static List leerXls(String nombreArchivo, List lstDatosCelda) {
        List lstResultadosFinal = new ArrayList();
        try {

            /**
             * Crea una nueva instancia de la clase FileInputStream
             */
            FileInputStream fileInputStream = new FileInputStream(
                    nombreArchivo);

            /**
             * Crea una nueva instancia de la clase POIFSFileSystem
             */
            POIFSFileSystem fsFileSystem = new POIFSFileSystem(fileInputStream);

            /**
             * Crea una nueva instancia de la clase HSSFWorkBook
             */
            HSSFWorkbook libro = new HSSFWorkbook(fsFileSystem);

            HSSFSheet hoja = libro.getSheetAt(0);

            /**
             * Iterar las filas y las celdas de la hoja de cálculo para obtener
             * toda la data.
             */
            Iterator filaIterator = hoja.rowIterator();

            while (filaIterator.hasNext()) {
                HSSFRow fila = (HSSFRow) filaIterator.next();
                Iterator iterador = fila.cellIterator();
                List lstCeldaTemp = new ArrayList();

//              CellRangeAddress cra = new CellRangeAddress(0, 0, 0, 0);
                 
                while (iterador.hasNext()) {

                    HSSFCell celda = (HSSFCell) iterador.next();
                    lstCeldaTemp.add(celda);

                }

                lstDatosCelda.add(lstCeldaTemp);
            }

        } catch (Exception e) {
           LOGGER.error(e.getMessage(), e);

        }
        return lstResultadosFinal;
    }

    /**
     * Este método se utiliza para imprimir los datos de la celda a la consola.
     *
     * @param datosCelda - Listado de los datos que hay en la hoja de cálculo.
     *
     */
    private static void imprimeConsola(List datosCelda) {

        String valorCelda;

        for (int i = 0; i < datosCelda.size(); i++) {

            List lstCeldaTemp = (List) datosCelda.get(i);

            for (int j = 0; j < lstCeldaTemp.size(); j++) {

                if (antiguo) {

                    HSSFCell hssfCell = (HSSFCell) lstCeldaTemp.get(j);
                    valorCelda = hssfCell.toString();

                } else {

                    XSSFCell hssfCell = (XSSFCell) lstCeldaTemp.get(j);
                    valorCelda = hssfCell.toString();
                }

                System.out.print(valorCelda + "\t");

            }

            System.out.println();

        }

    }

    public static void main(String[] args) throws BusinessException {

        String fileName = "C:" + File.separator + "Users" + File.separator
                + "Venus" + File.separator + "Downloads" + File.separator
                + "2014-05-29 PRESTAMO E INTERESES TECH OPS.xlsx";

        System.out.println(fileName);

        LectorExcel.leerArchivoExcel(fileName, 1);

        Double g = 10.9999999;
        System.out.println(g.intValue());

    }
}
