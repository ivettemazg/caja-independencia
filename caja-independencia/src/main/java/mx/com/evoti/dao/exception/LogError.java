/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.exception;

/**
 * @author Ivette Manzano
 */
public final class LogError {

    public static final String SESSION_MSG = " LA SESSION ES INVALIDA. ";
    public static final String SESSION_CAUSA = " NO SE PUDO ESTABLECER COMUNICACION CON LA LA BD. ";
    public static final String OPEN_SESSION = " NO SE PUDO ESTABLECER EL FLUJO DE SESION CON LA BASE DE DATOS. ";
    public static final String SAVE_COMMIT = " LA TANSACCION NO PUDO SER EJECUTADA CORRECTAMENTE. ";
    public static final String ROLLBACK = " NO SE PUDO REALIZAR EL ROLLBACK DE LA TRANSACCION. ";
    public static final String CLOSE = " NO SE CERRO LA SESION DE LA BD O YA ESTA YA NO EXISTE. ";
    public static final String QUERY = " NO SE PUDO EJECUTAR EL SIGUIENTE QUERY: ";
    public static final String QUERY_NULO = " VERIFICAR SI EL QUERY QUE SE QUIERE EJECUTAR, NO ES NULO O VACIO. ";
    public static final String INSTANCEOF = " NO SE PUDO INSTANCIAR LA CLASE PORQUE NO SE ESTA HEREDANDO DE LA CLASE PRINCIPAL. ";
    public static final String COMMIT = " LA TRANSACCION NO PUDO SER EJECUTADA. ";
    public static final String MSG_ROLLBACK = "...SE REALIZO EL ROLLBACK DE MANERA CORRECTA...";
    public static final String PARSE_EXCEPTION = " NO SE PUDO PARSEAR LA FECHA. ";
    public static final String PARAMETER = " NO SE PUDO AGREGAR EL PARAMETRO. ";
    public static final String MENSAJE = " MENSAJE: ";
    public static final String CAUSA = " CAUSA: ";
    public static final String CLASS_NOT_FOUND_EXCEPTION = " NO SE PUDO LOCALIZAR LA CLASE SOLICITADA. ";

//    public static final String ARCHIVO = " NO SE PUDO PROCESAR EL ARCHIVO: ";
//    public static final String ARCHIVO_VACIO_ERROR = "************NO SE PUDO PROCESAR EL ARCHIVO PORQUE ESTA VACIO O PRESENTA ERRORES************";
//    public static final String JSCH_EXCEPTION = " NO SE PUDO OBTENER LA SESSION DEL SERVIDOR REMOTO. ";
//    public static final String SFTP_EXCEPTION = " NO PUDO OBTENER EL FLUJO DEL SFTP. ";
//
//    public static final String FILE_IO = " NO SE PUDO OBTENER EL FLUJO DEL ARCHIVO. ";
//    public static final String NUMBER_FORMAT_EXCEPTION = " NO SE PUDO PARSEAR DE MANERA CORRECTA EL SIGUIENTE (String).";
//    public static final String IO_EXCEPTION = " SE HA PRESENTADO UN ERROR EN EL FLUJO DEL STREAM.";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION = " ES INCORRECTO EL TIPO DE ARGUMENTO PARA ESTE METODO.";
    public static final String NULL_POINTER_EXCEPTION = " SE TIENE UN PARAMETRO NULO EN EL METODO QUE ES REQUERIDO.";
    public static final String EXCEPTION = "SE GENERO UNA EXCEPCION GENERAL. ";
//    public static final String OUT_OF_MEMORY_ERROR = "SE LLEGO AL LIMITE DEL CONSUMO DE MEMORIA PARA EL PROCESO DE LAYOUTS.";
//    public static final String INTERRUPTED_EXCEPTION="NO SE PUDO INTERRUMPIR EL PROCESO.";

    private LogError() {
        super();
    }
}
