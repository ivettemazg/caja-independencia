/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.usuarioComun;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.AmortizacionDao;
import mx.com.evoti.dao.ArchivosHistorialDao;
import mx.com.evoti.dao.CreditosDao;
import mx.com.evoti.dao.MorosoDao;
import mx.com.evoti.dao.SolicitudCreditoDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.CatorcenaDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.usuariocomun.SolicitudCreditoDto;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.CreditosFinal;
import mx.com.evoti.hibernate.pojos.Imagenes;
import mx.com.evoti.hibernate.pojos.Productos;
import mx.com.evoti.hibernate.pojos.SolicitudAvales;
import mx.com.evoti.hibernate.pojos.SolicitudEstatus;
import mx.com.evoti.hibernate.pojos.Solicitudes;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class SolicitudBo implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SolicitudBo.class);
    private static final long serialVersionUID = -5481985320516138118L;

    private boolean dsblBtnNo;
    private boolean dsblBtnAu;
    private boolean dsblBtnFa;
    private boolean dsblBtnAg;
    /**
     * Bloque que aplica en las validaciones de fecha de antiguedad
     */
    private boolean dsblBtnNoA;//Nomina
    private boolean dsblBtnAuA;//Auto
    private boolean dsblBtnFaA;//Fondo ahrro
    private boolean dsblBtnAgA;//Aguinaldo
    private boolean dsblBtnNoC;
    private boolean dsblBtnAuC;
    private boolean dsblBtnFaC;
    private boolean dsblBtnAgC;
    private String msjNotificacion;
    private Date fechaIngresoCaja;
    private Date fechaIngresoEmpresa;
    private final SolicitudCreditoDao solDao;
    private CreditosDao creDao;

    private ArchivosHistorialDao arhDao;
    private MorosoDao morosoDao;
    private AmortizacionDao amoDao;

    public SolicitudBo() {
        arhDao = new ArchivosHistorialDao();
        morosoDao = new MorosoDao();
        solDao = new SolicitudCreditoDao();
        amoDao = new AmortizacionDao();
        msjNotificacion = "";
    }

    /**
     * Metodo que aplica todas las validaciones referentes a antiguedad del
     * usuario, solicitudes pendientes previas y creditos activos del usuario,
     * aquí se determina qué tipo de crédito puede solicitar un usuario
     *
     * @param fechaIngresoEmpresa
     * @param fechaIngresoCaja
     * @param idUsuario
     * @throws BusinessException
     */
    public void validaAntiguedadYCreditos(Date fechaIngresoEmpresa, Date fechaIngresoCaja, int idUsuario) throws BusinessException {
        //@TODO quitar estas validaciones cuando esté lista la pantalla de perfil, ya que no será necesario validar esta parte porqeu las fechas nunca deben llegar nulas a la solicitud de credito
       
        if (fechaIngresoEmpresa == null) {
            this.fechaIngresoEmpresa = new Date();
        } else {
            this.fechaIngresoEmpresa = fechaIngresoEmpresa;
        }

        if (fechaIngresoCaja == null) {
            this.fechaIngresoCaja = new Date();
        } else {
            this.fechaIngresoCaja = fechaIngresoCaja;
        }

       

        validaAntiguedad();

        validaSolicitudesCreditosActivos(idUsuario);

        /**
         * Se concatenan ambas validaciones para determinar las opciones de
         * solicitud que quedan activas
         */
        if (this.dsblBtnAgA && this.dsblBtnAgC) {
            this.dsblBtnAg = Boolean.TRUE;
        } else {
            this.dsblBtnAg = Boolean.FALSE;
        }
        if (this.dsblBtnFaA && this.dsblBtnFaC) {
            this.dsblBtnFa = Boolean.TRUE;
        } else {
            this.dsblBtnFa = Boolean.FALSE;
        }
        if (this.dsblBtnNoA && this.dsblBtnNoC) {
            this.dsblBtnNo = Boolean.TRUE;
        } else {
            this.dsblBtnNo = Boolean.FALSE;
        }
        if (this.dsblBtnAuA && this.dsblBtnAuC) {
            this.dsblBtnAu = Boolean.TRUE;
        } else {
            this.dsblBtnAu = Boolean.FALSE;
        }

    }

    /**
     * Realiza las validaciones que tienen que ver con la fecha de ingreso a la
     * caja y a la empresa del usuario
     *
     * @param fechaIngreso
     */
    private void validaAntiguedad() {
        dsblBtnAgA = Boolean.TRUE;
        dsblBtnAuA = Boolean.TRUE;
        dsblBtnFaA = Boolean.TRUE;
        dsblBtnNoA = Boolean.TRUE;

        /*Valida si la antiguedad en caja es menor a 3 meses*/
        if (!validaMesesAntiguedadCaja(90)) {
                msjNotificacion = Constantes.MSJ_VALIDACION3MESES + "\n";
                dsblBtnAgA = Boolean.FALSE;
                dsblBtnAuA = Boolean.FALSE;
                dsblBtnFaA = Boolean.FALSE;
                dsblBtnNoA = Boolean.FALSE;

            /**
             * Si la antiguedad en caja es mayor a 3 meses
             */
        } else if (!validaMesesAntiguedadEmpresa(365)) /*Valida si la antiguedad en la empresa es menor a 12 meses no puede pedir ni nomina ni auto*/
        {
                msjNotificacion = Constantes.MSJ_VALIDACION1ANO + "\n";
                dsblBtnAuA = Boolean.FALSE;
                dsblBtnNoA = Boolean.FALSE;

        } else if (!validaMesesAntiguedadEmpresa(1825) || !validaMesesAntiguedadCaja(365)) /*Valida si la antiguedad en la empresa es menor a 5 anos y la antiguedad en caja menor a 1 ano*/
        {
                msjNotificacion = Constantes.MSJ_VALIDACION5ANO + "\n";
                dsblBtnAuA = Boolean.FALSE;

        } else {
                dsblBtnAgA = Boolean.TRUE;
                dsblBtnAuA = Boolean.TRUE;
                dsblBtnFaA = Boolean.TRUE;
                dsblBtnNoA = Boolean.TRUE;
        }
    }

    /**
     * Valida que la fecha de ingreso a la caja sea menor a 3 meses
     *
     * @param fechaIngreso
     * @return false cuando es menor a 3 meses y true cuando es mayor
     */
    private boolean validaMesesAntiguedadCaja(int dias) {
        
         boolean validador = Boolean.TRUE;
        int diferenciaDias = Util.diferenciaEnDias(new Date(), fechaIngresoCaja);
        if (diferenciaDias <= dias) {
            validador = Boolean.FALSE;
        }
        LOGGER.debug("Dias de validacion de caja: "+dias);
        System.out.println("Dias en la caja: "+diferenciaDias);
        return validador;
    }

    private boolean validaMesesAntiguedadEmpresa(int dias) {
        boolean validador = Boolean.TRUE;
        int diferenciaDias = Util.diferenciaEnDias(new Date(), fechaIngresoEmpresa);
        if (diferenciaDias <= dias) {
            validador = Boolean.FALSE;
        }
        
        LOGGER.debug("Dias de validacion de empresa: "+dias);
        LOGGER.debug("Dias en la empresa: "+diferenciaDias);
        
        return validador;
    }

    private void validaSolicitudesCreditosActivos(int idUsuario) throws BusinessException {

        /**
         * Cuando el usuario tiene solicitudes pendientes
         */
        if (!consultarSolicitudesPendientesByUsuario(idUsuario)) {
            msjNotificacion += Constantes.MSJ_VALIDACIONSOLPENDIENTE;
            dsblBtnAgC = Boolean.FALSE;
            dsblBtnAuC = Boolean.FALSE;
            dsblBtnFaC = Boolean.FALSE;
            dsblBtnNoC = Boolean.FALSE;
            /*Al no tener solicitudes pendientes, valida los créditos del usuario*/
        } else {

            List<CreditosFinal> creds = obtieneCreditos(idUsuario);

            /*Cuando el usuario tiene 2 o mas creditos activos*/
            if (creds.size() >= 2) {
                msjNotificacion += Constantes.MSJ_2CREDITOSACTIVOS;
                dsblBtnAgC = Boolean.FALSE;
                dsblBtnAuC = Boolean.FALSE;
                dsblBtnFaC = Boolean.FALSE;
                dsblBtnNoC = Boolean.FALSE;

                /**
                 * Cuando el usuario tiene sólo 1 credito activo
                 */
            } else if (creds.size() == 1) {

                try {
                    CreditosFinal credito = creds.get(0);
                    
                    switch (credito.getCreProducto()) {
                        /**
                         * Si el credito es de nomina solamente puede pedir AG o FA,
                         * y NO siempre y cuando ya se haya pagado la mitad del
                         * mismo
                         */
                        case 6:
                            
                            msjNotificacion += Constantes.MSJ_VALIDACION1NOMINAACTIVO;
                            dsblBtnAgC = Boolean.TRUE;
                            dsblBtnAuC = Boolean.FALSE;
                            dsblBtnFaC = Boolean.TRUE;
                            
                            
                            
                            List<Amortizacion> amortizacion = amoDao.getAmortizacionPojo(credito.getCreId());
                            
                            BigDecimal sumaAmortizacionPagada = new BigDecimal(0.0);
                            for (Amortizacion amo : amortizacion) {
                                
                                /**
                                 * Se suma la amortizacion de todos los pagos
                                 * PAGADOS para tener el total de capital que ya se
                                 * liquido (PAGADO, PAGO_MAYOR y PAGO_MENOR)
                                 */
                                if ((2 == amo.getAmoEstatusInt()) 
                                        || (3 == amo.getAmoEstatusInt())
                                        || (4 == amo.getAmoEstatusInt())
                                        || (5 == amo.getAmoEstatusInt())
                                        || (6 == amo.getAmoEstatusInt())
                                        || (7 == amo.getAmoEstatusInt())
                                        || (8 == amo.getAmoEstatusInt())) {
                                    
                                    sumaAmortizacionPagada = sumaAmortizacionPagada.add(new BigDecimal(amo.getAmoAmortizacion()).setScale(2, BigDecimal.ROUND_HALF_EVEN)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                }
                            }
                            
                            /*-1 es cuando la cantidad de la izquierda es menor que la derecha,
                            por lo tanto valida que la cantidad a la izquierda (amortizacion pagada hasta ahora * 2)
                            sea mayor o igual que la cantidad de la derecha (capital total).
                            Como la amortizacion esta multiplicada x 2, si esta cantidad sobrepasa o iguala el total de capital
                            significa que se ha pagado por lo menos la mitad del credito*/
                            if (sumaAmortizacionPagada.add(sumaAmortizacionPagada).compareTo(
                                    new BigDecimal(credito.getCrePrestamo()).setScale(2, BigDecimal.ROUND_HALF_EVEN)) != -1) {
                                dsblBtnNoC = Boolean.TRUE;
                                
                            } else {
                                msjNotificacion += Constantes.MSJ_VALIDACION1NOMINAMENOSMITAD;
                                dsblBtnNoC = Boolean.FALSE;
                            }
                            
                            break;
                            /**
                             * Si ya tiene un credito de auto, solo puede pedir de FA o
                             * AG
                             */
                        case 7:
                            msjNotificacion += Constantes.MSJ_CREDITOSAUTOACTIVOS;
                            
                            dsblBtnAgC = Boolean.TRUE;
                            dsblBtnAuC = Boolean.FALSE;
                            dsblBtnFaC = Boolean.TRUE;
                            dsblBtnNoC = Boolean.FALSE;
                            break;
                            
                            /*Cuando tiene un credito de FA o AG*/
                        default:
                            dsblBtnAgC = Boolean.TRUE;
                            dsblBtnAuC = Boolean.TRUE;
                            dsblBtnFaC = Boolean.TRUE;
                            dsblBtnNoC = Boolean.TRUE;
                            break;
                            
                    }
                    
                    /*Cuando no tiene ningun credito*/
                } catch (IntegracionException ex) {
                    throw new BusinessException(ex.getMessage(), ex);
                }
            } else {
                dsblBtnAgC = Boolean.TRUE;
                dsblBtnAuC = Boolean.TRUE;
                dsblBtnFaC = Boolean.TRUE;
                dsblBtnNoC = Boolean.TRUE;
            }

        }

    }

    /**
     * Valida si el usuario tiene otras solicitudes pendientes
     *
     * @param idUsuario
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    private boolean consultarSolicitudesPendientesByUsuario(Integer idUsuario) throws BusinessException {
        try {
            boolean validador = true;
            List<SolicitudCreditoDto> lista = solDao.consultaSolicitudesPndtsXEmpl(idUsuario);

            if (lista != null && lista.size() > 0) {
                validador = false;
            }
            return validador;
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException("Error al consultar las solicitudes pendientes", ex);
        }
    }

    private List<CreditosFinal> obtieneCreditos(int usuId) throws BusinessException {
        List<CreditosFinal> creds = null;
        try {
            creDao = new CreditosDao();

            creds = creDao.obtieneCreditosXUsuario(usuId);

        } catch (IntegracionException ex) {
            throw new BusinessException("Error al consultar los creditos activos", ex);
        }
        return creds;
    }

    public List<Date> consultaCatorcenasTotales(int dicOrJul, Boolean julioSigAno, Date creacionSolicitud) throws BusinessException {
        try {
            List<CatorcenaDto> catIntermedias;

            //Obtiene la catorcena inmediata siguiente al dia en que se creó la solicitud
            CatorcenaDto catSig = solDao.obtieneCatorcenas(creacionSolicitud);
            //Variable que guarda la catorcena en que se liquidará el credito
            CatorcenaDto catDicLast = null;
            if (dicOrJul == 12) {
                catDicLast = solDao.obtieneCatorcenaUltima7Or12(12, julioSigAno);
            } else if (dicOrJul == Constantes.FA_CAT_JUN) {

                catDicLast = solDao.obtieneCatorcenaUltima7Or12(Constantes.FA_CAT_JUN, julioSigAno);

            }

            /*Obtiene todas las catorcenas intermedias del credito*/
            catIntermedias = solDao.obtieneCatorcenasIntermedias(catSig.getCarFecha(), catDicLast.getCarFecha());

            List<Date> fechasFin = new ArrayList<>();
            for (int i = 0; i < catIntermedias.size(); i++) {
                fechasFin.add(catIntermedias.get(i).getCarFecha());
            }

            return fechasFin;

        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Valida si un usuario es moroso
     *
     * @param idUsuario
     * @param empresa
     * @return true si es moroso, false si no lo es
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public boolean validaMorosidad(int idUsuario, int empresa) throws BusinessException {
        try {
            Date catorcena = arhDao.getCatorUltArchivo(empresa);
            
            if(catorcena == null){
                 return Boolean.FALSE;
            }
            
            Integer catorcenasAdeudadas = morosoDao.getCatorcenasAdeudadas(catorcena, idUsuario);
            
            if(catorcenasAdeudadas!=null){
                if(catorcenasAdeudadas > 0){
                    return Boolean.TRUE;
                }else{
                    return Boolean.FALSE;
                }
            }else{
                 return Boolean.FALSE;
            }
            
        } catch (IntegracionException ex) {
              throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo donde se configura la solicitud y se manda a guardar
     * posteriormente
     *
     * @param usuario
     * @param montoSolicitado
     * @param deducciones
     * @param catorcenas
     * @param faCatorcena
     * @param tipoSolicitud
     * @param totalInteres
     * @param pagoCatorcenal
     * @param fechaUltimoPago
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public Long creaSolicitud(UsuarioDto usuario,
            Double montoSolicitado,
            Double deducciones,
            Integer catorcenas,
            Integer faCatorcena,
            String tipoSolicitud,
            Double totalInteres,
            Double pagoCatorcenal,
            Date fechaUltimoPago) throws BusinessException {

        try {
            Solicitudes solicitud = new Solicitudes();
            Usuarios usuSolicitud = new Usuarios(usuario.getId());
            Productos producto = new Productos();
            SolicitudEstatus estatus = new SolicitudEstatus(1);

            solicitud.setSolClaveEmpleado(usuario.getCveEmpleado());
            solicitud.setSolSueldoNeto(usuario.getSalarioNeto());
            solicitud.setSolDeducciones(deducciones);
            solicitud.setSolMontoSolicitado(montoSolicitado);
            solicitud.setSolEstatusDb(1);
            solicitud.setSolFechaCreacion(Util.restaHrToDate(new Date(), 6));
            solicitud.setUsuarios(usuSolicitud);
            solicitud.setSolicitudEstatus(estatus);
            solicitud.setSolIntereses(totalInteres);
            solicitud.setSolPagoCredito(pagoCatorcenal);

            Integer noAvales = determinaAvalNoAgFaXEmpresa(usuario.getEmpresaId(), montoSolicitado);

            Set<Imagenes> imagenes = new HashSet<>();

            Set<Imagenes> avales = new HashSet<>();

            Set<SolicitudAvales> solAvales = new HashSet<>();

            switch (tipoSolicitud) {
                case "no":
                    solicitud.setSolCatorcenas(catorcenas);
                    producto.setProId(Constantes.NO);
                    solicitud.setProductos(producto);
                    break;
                case "au":
                    solicitud.setSolCatorcenas(catorcenas);
                    producto.setProId(Constantes.AU);
                    solicitud.setProductos(producto);
                    noAvales = 3;
                    break;
                case "fa":
                    solicitud.setSolFacatorcena(faCatorcena);
                    producto.setProId(Constantes.FA);
                    solicitud.setProductos(producto);
                    solicitud.setSolFechaUltCatorcena(fechaUltimoPago);
                    Imagenes fAhorro = new Imagenes(solicitud, generaNombreImg(usuario.getId(), "FAHORRO"), 6, 1, "");
                    imagenes.add(fAhorro);
                    break;
                case "ag":
                    solicitud.setSolFacatorcena(faCatorcena);
                    producto.setProId(Constantes.AG);
                    solicitud.setProductos(producto);
                    solicitud.setSolFechaUltCatorcena(fechaUltimoPago);
                    Imagenes aguinaldo = new Imagenes(solicitud, generaNombreImg(usuario.getId(), "AGUINALDO"), 7, 1, "");
                    imagenes.add(aguinaldo);
                    break;
            }

            /**
             * Se crean los objetos imagenes y solicitudAvales para los avales necesarios
             */
            for (int i = 1; i <= noAvales; i++) {
                avales.add(new Imagenes(solicitud, generaNombreImg(usuario.getId(), "AVAL" + i), 5, 1, ""));
                solAvales.add(new SolicitudAvales(solicitud, 1));
            }

            Imagenes ife = new Imagenes(solicitud, generaNombreImg(usuario.getId(), "IFE"), 1, 1, "");
            Imagenes domicilio = new Imagenes(solicitud, generaNombreImg(usuario.getId(), "DOMICILIO"), 2, 1, "");
            Imagenes nomina = new Imagenes(solicitud, generaNombreImg(usuario.getId(), "NOMINA"), 3, 1, "");
            Imagenes edoCta = new Imagenes(solicitud, generaNombreImg(usuario.getId(), "EDOCTA"), 4, 1, "");

            imagenes.add(ife);
            imagenes.add(domicilio);
            imagenes.add(nomina);
            imagenes.add(edoCta);
            imagenes.addAll(avales);

            /**
             * Se asignan los datos globales que aplican para cualquier tipo de solicitud
             */
            solicitud.setImageneses(imagenes);
            solicitud.setSolicitudAvaleses(solAvales);

            Solicitudes solActualizada = solDao.guardaSolicitud(solicitud);
            return solActualizada.getSolId();
        } catch (IntegracionException ex) {
            throw new BusinessException("Error al crear la solicitud", ex);
        }

    }

    /**
     * Genera el nombre del documento con el que se guardara en el servidor y en
     * la base de datos
     *
     * @param idUsuario
     * @param tipoDocumento
     * @return
     */
    private String generaNombreImg(int idUsuario, String tipoDocumento) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
        String fechaHoy = sdf.format(new Date());

        String nombreImg = idUsuario + "_" + tipoDocumento + "_" + fechaHoy + ".pdf";

        return nombreImg;
    }

    /**
     * Regresa la cantidad de avales que se tendrán que introducir en la
     * solicitud dependiendo de la cantidad solicitada y la empresa
     * siempre y cuando sea solicitud de nomina, aguinaldo y fondo de ahorro
     *
     * @param empresa
     * @param montoSolicitado
     * @return
     */
    private Integer determinaAvalNoAgFaXEmpresa(Integer empresa, Double montoSolicitado) {

        Integer noAvales = 0;
         if (null != empresa ) switch (empresa) {
            case 1:
                if (montoSolicitado < 20000.0) {
                    noAvales = 1;

                } else {
                    noAvales = 2;
                    
                }
                break;
            case 5:
                noAvales = 2;
                break;
            default:
                noAvales = 3;
                break;
        }

        return noAvales;
    }

   
    public void creaSolSegAuto(Usuarios usuario,
            Double montoSolicitado,
            Integer faCatorcena,
            String banco, String clabe, String noPoliza,
            String referencia, String aseguradora,
            Double totalInteres, Double pagoCatorcenal,
            Date fechaUltPago
    ) throws BusinessException{
        try {
            
            Solicitudes solicitud = new Solicitudes();
            Productos prod = new Productos(Constantes.SAU);
            //Queda como solicitud aceptada para que se muestre directamente en fondeos pendientes
            SolicitudEstatus estatus = new SolicitudEstatus(Constantes.SOL_EST_ACEPTADA);
            
            solicitud.setProductos(prod);

            solicitud.setSolClaveEmpleado(usuario.getUsuClaveEmpleado());
            solicitud.setSolMontoSolicitado(montoSolicitado);
            solicitud.setSolEstatusDb(3); //Aprobados
            solicitud.setSolBanco(banco);
            solicitud.setSolClabeInterbancaria(clabe);
            solicitud.setSolNoPoliza(noPoliza);
            solicitud.setSolReferencia(referencia);
            solicitud.setSolAseguradora(aseguradora);
            solicitud.setSolFechaCreacion(new Date());
            solicitud.setUsuarios(usuario);
            solicitud.setSolicitudEstatus(estatus);
            solicitud.setSolIntereses(totalInteres);
            solicitud.setSolPagoCredito(pagoCatorcenal);
            solicitud.setSolFacatorcena(faCatorcena);
            solicitud.setSolFechaUltCatorcena(fechaUltPago);
            
            solDao.guardaSolicitud(solicitud);
        } catch (IntegracionException ex) {
           throw new BusinessException(ex.getMessage(), ex);
        }
        
    }
    
    
    /**
     * @return the dsblBtnNo
     */
    public boolean isDsblBtnNo() {
        return dsblBtnNo;
    }

    /**
     * @return the dsblBtnAu
     */
    public boolean isDsblBtnAu() {
        return dsblBtnAu;
    }

    /**
     * @return the dsblBtnFa
     */
    public boolean isDsblBtnFa() {
        return dsblBtnFa;
    }

    /**
     * @return the dsblBtnAg
     */
    public boolean isDsblBtnAg() {
        return dsblBtnAg;
    }

    /**
     * @return the msjNotificacion
     */
    public String getMsjNotificacion() {
        return msjNotificacion;
    }

}
