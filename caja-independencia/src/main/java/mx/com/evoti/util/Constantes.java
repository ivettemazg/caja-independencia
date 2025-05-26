/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.util;

/**
 *
 * @author Venette
 */
public class Constantes {
    
    public static final String PATH_DOCTOS = "c:/Documentos";
    public static final String PATH_DOCTOS_LOCAL = "/Users/ivettemanzano/Projects/Documentos";
    
    public static final Integer USU_BAJA_0= 0;
    public static final Integer USU_ACTIVO_1 = 1;
    public static final String USU_BAJA_STR= "DADO DE BAJA";
    public static final String USU_ACTIVO_STR = "ACTIVO";
    
    public static final String ROL_USR_ADMON_S = "Administrador";
    public static final String ROL_USR_AUX_S = "Auxiliar";
    public static final String ROL_USR_USR_S = "Usuario";
    
    public static final Integer ROL_USR_ADMON_I = 1;
    public static final Integer ROL_USR_AUX_I = 2;
    public static final Integer ROL_USR_USR_I = 3;
    
    
     public static final String MSJ_VALIDACION3MESES="No puedes solicitar un préstamo ya que tu antigüedad en caja es menor de 3 meses.";
    public static final String MSJ_VALIDACION1ANO="No puedes solicitar un préstamo de nómina o de automóvil ya que tu antigüedad en la empresa es menor a 1 año. ";
    public static final String MSJ_VALIDACION5ANO="No puedes solicitar un préstamo de automovil ya que tu antigüedad en la empresa es menor a 5 años, sentimos las molestias que esto ocasione. ";
    public static final String MSJ_VALIDACION1NOMINAACTIVO="Ya tiene un crédito de nómina, por lo que no puedes solicitar un crédito de auto. ";
    public static final String MSJ_VALIDACION1NOMINAMENOSMITAD="Para solicitar otro crédito de nómina, es necesario haber liquidado el menos el 50% del total de su crédito de nómina actual.";
    public static final String MSJ_2CREDITOSACTIVOS="Usted ya tiene 2 créditos pendientes, por lo que no puede solicitar otro.";
    public static final String MSJ_CREDITOSAUTOACTIVOS="Usted ya tiene 1 crédito de auto activo, por lo que solamente puede solicitar crédito de Fondo de Ahorro o Aguinaldo.";
    public static final String MSJ_VALIDACIONSOLPENDIENTE="Ya tienes una solicitud pendiente de autorización, por lo que no puedes solicitar otra. ";
    public static final String MSJ_SOLICITUDENVIADAEXITO="Su solicitud ha sido enviada con éxito";
    
    public static final Integer PRIMER_PAGO = 1;
    
    public static final Integer FA = 4;
    public static final Integer AG = 5;
    public static final Integer AU = 7;
    public static final Integer NO = 6;
    public static final Integer SAU = 11;
    
    public static final String CUERPO = "Estimado(a): %1$s \n\n Tu solicitud de crédito fue registrada exitosamente bajo el folio "
                            + "No.%2$s, a partir de este momento se encuentra en proceso de revisión y autorización, "
                            + "en breve te estaremos informando vía correo electrónico el estatus de tu solicitud.\n\n "
                            + "Juan Bernardo Carmona Ávila.\n\nPresidente Caja de Ahorro";
    public static final String ASUNTO =  "Solicitud de Credito";
    public static final String CUERPO_EMAIL_DOCUMENTACIONFIRMADA="Envío de documentación de la solicitud %1$s del cliente %2$s. ";
    public static final String EMAIL_1 = "contacto@cajaindependencia.com";
    public static final String EMAIL_2 = "jeffrey.deltoro@cajaindependencia.com";
    public static final String EMAIL_3 = "asistente@cajaindependencia.com";
    
    
   public static final String MSJ_CUERPO_FONDEADA = "Su solicitud ha sido aprobada, a continuación requiere enviar la siguiente documentación, "
           + "\nmisma que podrá descargar del sistema en el apartado 'Mis Solicitudes' :"
           + "\nANEXO A, "
           + "\nANEXO B, "
           + "\nANEXO C, "
           + "\nAVISO, "
           + "\nSOLICITUD DE CREDITO. "
            + "\n\n Es necesario que tanto usted como sus avales firmen al calce, todos los documentos que se le indica en este correo."
            + "\n\n Una vez firmados los documentos, ingrese al sistema al recuadro 'Mis Solicitudes', y envíe la documentación desde la tabla, dando click en \"Enviar documentos firmados\" del folio correspondiente.";
    
    
    
    
    public static final String DOC_ID="IDENTIFICACION";
    public static final String DOC_DOMICILIO="DOMICILIO";
    public static final String DOC_NOMINA="NOMINA";
    public static final String DOC_EDOCTA="ESTADO DE CUENTA";
    public static final String DOC_AVAL="AVAL";
    public static final String DOC_FA="COMPROBANTE FONDO AHORRO";
    public static final String DOC_AG="COMPROBANTE AGUINALDO";
    public static final String DOC_FIRMADA="DOCUMENTOS FIRMADOS";
    public static final Integer DOC_ID_INT=1;
    public static final Integer DOC_DOMICILIO_INT=2;
    public static final Integer DOC_NOMINA_INT=3;
    public static final Integer DOC_EDOCTA_INT=4;
    public static final Integer DOC_AVAL_INT=5;
    public static final Integer DOC_FA_INT=6;
    public static final Integer DOC_AG_INT=7;
    public static final Integer DOC_FIRMADA_INT=8;
    
    /**
     * ESTATUS DE IMAGENES SOLICITUD
     */
    public static final String IMA_STT_PEND="PENDIENTE";
    public static final String IMA_STT_VAL="VALIDANDO";
    public static final String IMA_STT_APRO="APROBADA";
    public static final String IMA_STT_RECH="RECHAZADO";
    
    
    public static final String MSJ_LOGIN_PASS_ERR= "La contraseña es incorrecta";
    public static final String MSJ_LOGIN_USR_NO_EXST= "La clave de empleado no existe en el sistema";
    public static final String MSJ_ERR_LOGIN_USR_BAJA= "Usted se encuentra dado de baja de la caja desde el día %1$s, por lo tanto no puede accesar al sistema.";
    
    /**
     * ABREVIACIONES DE NOMBRES DE EMPRESA
     */
    public static final String AEROMEXICO = "AEROMEXICO";
    public static final String TECHOPS = "TECH OPS";
    public static final String SISTEM = "SISTEM";
    public static final String CARGO = "CARGO";
    
    public static final Integer AMX = 1;
    public static final Integer TECH = 2;
    public static final Integer SIS = 3;
    public static final Integer CAR = 4;
    public static final Integer SND = 5;
    
    /**
     * ESTATUS DE ALTAS Y CAMBIOS DE EMPRESA
     */
    public static final Integer CNH_NUEVO  = 1;
    public static final Integer CNH_CAMBIO  = 2;

    /**
    ESTATUS DE USUARIO
    */
     public static final String USR_ACTIVO = "ACTIVO";
    public static final String USR_BAJA = "BAJA";
    
    /**
     * ESTATUS DE SOLICITUD AVALES
     */
    public static final String SOLAVA_PENDIENTE = "PENDIENTE";
    public static final String SOLAVA_VALIDANDO = "VALIDANDO";
    public static final String SOLAVA_APROBADO = "APROBADO";
    public static final String SOLAVA_RECHAZADO = "RECHAZADO";
    public static final Integer SOLAVA_PENDIENTE_I = 1;
    public static final Integer SOLAVA_VALIDANDO_I = 2;
    public static final Integer SOLAVA_APROBADO_I = 3;
    public static final Integer SOLAVA_RECHAZADO_I = 4;
    
    
    public static final String ASUNTO_SOL_AUT = "SOLICITUD AUTORIZADA";
    public static final String ASUNTO_SOL_RECH = "SOLICITUD DENEGADA";
     public static final String SOLICITUD_AUTORIZADA = "Estimado(a): %1$s, \n\n le informamos que su solicitud de credito ha sido APROBADA con los siguientes datos: Monto del credito: %2$s, Catorcenas a liquidar: %3$s, Pagos catorcenales: %4$s. Aproximadamente de 3 a 5 dias habiles se le enviaran los siguientes pasos para el proceso de solicitud de credito. \n\n Juan Bernardo Carmona Avila.\n\n Presidente Caja de Ahorro";
    public static final String SOLICITUD_RECHAZADA = "Estimado(a): %1$s, \n\n le informamos que su solicitud de credito ha sido RECHAZADA por el siguiente motivo: %2$s.\n\n Juan Bernardo Carmona Ãvila.\n\n Presidente Caja de Ahorro";
    
    public static final String CANCELACIONCREDITO = "Estimado(a): %1$s \n\n le informamos que su credito con clave %2$s con numero de solicitud %3$s ha sido CANCELADO por el siguiente motivo: %4$s. \n\n Juan Bernardo Carmona Avila.\n\n Presidente Caja de Ahorro";
    public static final String CREDITOFONDEADO = "Estimado (a) %1$s \n\n para seguir con el proceso de solicitud de credito le pedimos de favor ingresar al sistema y dirigirse al modulo de ESTATUS DE SOLICITUD e imprimir todos los documentos del folio %2$s, es necesario que tanto usted como sus avales firmen al calce todos los documentos, una vez firmados ingrese de nuevo al sistema en el modulo de ESTATUS DE SOLICITUD de click en envio de documentos firmados y adjunte en un solo archivo PDF todos los documentos firmados";
    
    public static final String DOCTO_FIRMADO_RECHAZADO = "Estimado (a) %1$s, Debe volver a adjuntar su documentación firmada, ya que el archivo que envío no es válido porque %2$s";
    public static final String ASUNTO_DOCTO_FIRMADO_RECHAZADO= "Problema con la documentación firmada";
    
    /**
     * BLOQUE BITACORA
     */
    
    public static final int BIT_SOL_AVALRECH=1;
    public static final int BIT_SOL_DOCTORECH=2;
    public static final int BIT_SOL_RECHAZADA=3;
     public static final int BIT_CRE_CANCELADO = 4;
     public static final int BIT_NOTA_USUARIO = 5;
    
    /**
     * BLOQUE MIS SOLICITUDES
     */
    public static final String OBS_CREADA="Aún no ha terminado de llenar su solicitud, favor de llenar todos los requerimientos para enviarla";
    public static final String OBS_VALIDANDO="Su solicitud se encuentra pendiente de autorización";
    public static final String OBS_RECHAZADA="Motivo de rechazo: ";
    public static final String OBS_ACEPTADA="Su solicitud fue aceptada, se le enviará un correo para notificarle cuándo podrá enviar sus documentos firmados";
    public static final String OBS_FONDEADA="Por favor descargue los siguientes documentos ";
    public static final String OBS_DOCUMENTOS="Sus documentos ya fueron enviados";
    public static final String OBS_CERRADA="Fecha de depósito: ";


    public static final int SOL_EST_ACEPTADA=3;
    public static final int SOL_EST_FONDEADA=4; 
    public static final int SOL_EST_DOCTOS_ENV=5;
    public static final int SOL_EST_RECHAZADA=7;
     public static final Integer SOL_EST_DOCTOS_APROBADOS =8;
    
/**
 * BLOQUE CREDITO ESTATUS
 */
    public static final int CRE_EST_ACTIVO=1;
    public static final int CRE_EST_PAGADO=2;
    public static final int CRE_EST_CANCELADO=3;
    public static final int CRE_EST_TRANSFERIDO=4;
    public static final int CRE_EST_INCOBRABLE=5;
    public static final int CRE_EST_AJUSTADO=6;
    public static final int CRE_EST_AJUST_FINIQ=7;
    
    /**
     * BLOQUE AMORTIZACION ESTATUS
     */
    public static final Integer AMO_ESTATUS_PEND_1 =1;
    public static final Integer AMO_ESTATUS_PAG_2 =2;
    public static final Integer AMO_ESTATUS_PMEN_3 =3;
    public static final Integer AMO_ESTATUS_PMAY_4 =4;
    public static final Integer AMO_ESTATUS_PACUM_5 =5;
    public static final Integer AMO_ESTATUS_PEXT_6 =6;
    public static final Integer AMO_ESTATUS_CAPITAL_7 =7;
    public static final Integer AMO_ESTATUS_ABONO_CRE_8 =8;
    public static final Integer AMO_ESTATUS_FINIQ_9 =9;
    public static final Integer AMO_ESTATUS_DEUDA_FIN_10 =10;
    public static final Integer AMO_ESTATUS_TRANS_11 =11;
    public static final Integer AMO_ESTATUS_INCOB_12 =12;
    public static final Integer AMO_ESTATUS_RECORRIDA_13 =13;
    
    
    
    public static final String DLG_MOTIV_RECHAZO = "Motivo de rechazo";
    public static final String DLG_REQUIRED_MESSAGE = "El campo motivo rechazo es obligatorio";
   
   
    public static final String MOV_TIPO_DEVOLUCION = "DEVOLUCION";
    public static final String MOV_TIPO_APORTACION = "APORTACION";
    public static final String MOV_TIPO_RENDTO = "RENDIMIENTO";
    public static final String MOV_ABONOCREDITO = "ABONO CREDITO";
    public static final String MOV_ABONOCAPITAL = "ABONO CAPITAL";
    
    public static final Integer MOV_AR_APO = 1;
    public static final Integer MOV_AR_RDTO = 2;
    
    public static final Integer MOV_PRODUCTO_F = 1;
    public static final Integer MOV_PRODUCTO_NF = 2;
    public static final Integer MOV_PRODUCTO_VOL = 3;
    
    
    public static final Integer PAGEST_PEND_1 =1;
    public static final Integer PAGEST_EXACT_2 =2;
    public static final Integer PAGEST_MENOR_3 =3;
    public static final Integer PAGEST_MAYOR_4 =4;
    public static final Integer PAGEST_MD1_5 =5;
    public static final Integer PAGEST_MD1YA_6 =6;
    public static final  Integer PAGEST_SAMO_7 =7;
    public static final Integer PAGEST_ACUM_8 =8;
    public static final Integer PAGEST_CAPITAL_9 =9;
    public static final Integer PAGEST_EXTEMP_10 =10;
    public static final Integer PAGEST_DEVOL_11 =11;
    public static final Integer PAGEST_FINIQ_12 =12;
    
    //ESTATUS BAJA_EMPLEADO
    public static final Integer BAJA_INICIADA = 0;
    public static final Integer BAJA_PENDIENTE = 1;
    public static final Integer BAJA_AHORROSXDEVOLVER = 2;
    public static final Integer BAJA_COMPLETADA = 3;
    
    
    //CONCEPTOS BANCOS
    public static final Integer BAN_PAGOSARCHIVO = 1;
    public static final Integer BAN_APORTACIONARCHIVO = 2;
    public static final Integer BAN_PAGO_EXTMP = 4;
    public static final Integer BAN_PAGO_CAPITAL = 5;
    public static final Integer BAN_DEV_ACUMULADO = 6;
    public static final Integer BAN_AP_VOL = 7;
    public static final Integer BAN_DEV_A_FIJO = 8;
    public static final Integer BAN_DEV_A_N_FIJO = 9;
    public static final Integer BAN_DEV_A_VOL = 10;
    public static final Integer BAN_DEV_RDTO_FIJO = 11;
    public static final Integer BAN_DEP_CREDITO = 12;
    public static final Integer BAN_DESCTOXCOB_CRED = 13;
    public static final Integer BAN_DEV_RDTO_N_FIJO = 14;
    public static final Integer BAN_DEV_RDTO_VOL = 15;
   
    
    //BANCO ESTATUS AJUSTADO NO AJUSTADO
    public static final Integer BAN_AJUSTADO_PARCIAL = 2;
    public static final Integer BAN_AJUSTADO = 1;
    public static final Integer BAN_NO_AJUSTADO = 0;
    //ESTADO DE CUENTA ESTATUS AJUSTADO NO AJUSTADO
    public static final Integer EC_AJUSTADO_PARCIAL = 2;
    public static final Integer EC_AJUSTADO = 1;
    public static final Integer EC_NO_AJUSTADO = 0;
    
    //Colores asigandos al estatus de estado de cuenta y bancos
    public static final String COL_AJUSTADO = "ajustado";
    public static final String COL_AJUSTADO_PARCIAL = "ajustado-parcial";
    
    public static final int RBE_BANCO = 1;
    public static final int RBE_EC = 2;
    
    
    //Valor de la catorcena de junio cuando se estan creando creditos de fa, ag o sau
    public static final int FA_CAT_JUN = 6;
    public static final String USR_HABILITADO= "Este usuario puede solicitar créditos";
    public static final String USR_DESHABILITADO= "El usuario no puede solicitar créditos";
    
    public static final String USR_OMITIR= "El usuario tiene desactivadas las validaciones";
    public static final String USR_VAL_ACTIVAS= "Este usuario tiene activas las validaciones";
    public static Integer PAGCAP_ACUMULADO = 2;
    public static Integer PAGCAP_AHORRO = 3;
    
 
    
    
    
   
    
    
    
}
