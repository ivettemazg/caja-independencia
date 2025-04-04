/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.admon;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.CreditosBo;
import mx.com.evoti.bo.MovimientosBo;
import mx.com.evoti.bo.PagosPersonalBo;
import mx.com.evoti.bo.administrador.algoritmopagos.BusquedaEmpleadoBo;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.transaccion.TransaccionBo;
import mx.com.evoti.bo.usuarioComun.AvalesSolicitudBo;
import mx.com.evoti.dao.bitacora.BitacoraDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.transacciones.TransaccionDao;
import mx.com.evoti.dto.AhorroVoluntarioDto;
import mx.com.evoti.dto.CreditoDto;
import mx.com.evoti.dto.DetalleCreditoDto;
import mx.com.evoti.dto.EmpresasDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.PagosPersonalDto;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.Bitacora;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.presentacion.usuariocomun.PerfilBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "repEmpleadoBean")
@ViewScoped
public class ReporteEmpleadoBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReporteEmpleadoBean.class);
    private static final long serialVersionUID = -902406380079698337L;

    @ManagedProperty("#{perfilBean}")
    private PerfilBean perfilBean;
    @ManagedProperty("#{detAdCreBean}")
    private DetalleAdeudoCreditosBean detAdeudoCredBean;
    @ManagedProperty("#{transAvalesBean}")
    private TransfiereAvalesBean transAvalesBean;
    @ManagedProperty("#{amoRepEmpBean}")
    private AmortizacionRepEmpBean amoRepBean;

    private BusquedaEmpleadoBo busqEmpBo;
    private BancosBo bancoBo;
    private AvalesSolicitudBo avalesBo;
    private CreditosBo creBo;

    private List<Usuarios> usuarios;
    private Usuarios usuario;
    private List<EmpresasDto> empresas;
    private EmpresasDto empresa;
    private Integer claveEmpleado;
    private String estEmpleado;
    private String habilitado;
    private String omiteValidaciones;

    private Boolean rdrFieldSets;

    private MovimientosBo movsBo;
    private TransaccionBo tranBo;
    private List<MovimientosDto> movimientos;
    private MovimientosDto movDevSelected;

    private Boolean editTable;

    /**
     * Bloque de tabla ahorros
     */
    private List<MovimientosDto> lstTotalesAhorro;
    private MovimientosDto movSelected;
    private Double totalDetalle;
    private Double totalAhorro;
    private Date fechaApoVol;
    private Double montoApoVol;
    private Boolean rdrFNF;
    private Boolean rdrV;
    private List<MovimientosDto> detalleMovs;
    private List<AhorroVoluntarioDto> ahorrosVol;
    private List<UsuarioDto> avales;

    private Boolean rdrAvalCredTransf;
    private CreditoDto creditoPadre;

    private Boolean rdrAvalCreditos;
    private List<CreditoDto> creditosAval;

    /**
     * BLOQUE NOTAS
     */
    private List<Bitacora> notas;
    private Bitacora notaSeleccionada;
    private String notaTitulo;
    private String notaContent;
    private BitacoraDao bitaDao;
    private final UsuarioDto userSession;

    /**
     * BLOQUE REPORTE PAGOS PERSONAL
     */
    private List<PagosPersonalDto> pagosAgrupados;
    private PagosPersonalBo pagosAgrupadosBo;
    private PagosPersonalDto estatusPagSelected;
    private List<PagosPersonalDto> detallePagosByEstatus;
    private List<PagosPersonalDto> acumulado;
    private Boolean rdrFechaBaja;

    public ReporteEmpleadoBean() {
        userSession = (UsuarioDto) this.getSession().getAttribute("usuario");
        busqEmpBo = new BusquedaEmpleadoBo();
        bancoBo = new BancosBo();
        movsBo = new MovimientosBo();
        avalesBo = new AvalesSolicitudBo();
        creBo = new CreditosBo();
        tranBo = new TransaccionBo();
        pagosAgrupadosBo = new PagosPersonalBo();
    }

    public void init() {
        try {

            rdrFieldSets = Boolean.FALSE;
            rdrFechaBaja = Boolean.FALSE;
            rdrAvalCreditos = Boolean.FALSE;

            //obtener las empresas
            empresas = busqEmpBo.getEmpresasDto();

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene el empleado de quien se está consultando
     */
    public void buscaEmpleado() {
        try {

            if (claveEmpleado != null && empresa != null) {
                usuarios = busqEmpBo.getUsuarioXCveYEmpresa(claveEmpleado, empresa.getEmpId());
                habilitado = Constantes.USR_DESHABILITADO;
                omiteValidaciones = Constantes.USR_OMITIR;
                
                //Cuando encuentra el usuario
                if (!usuarios.isEmpty()) {
                    usuario = usuarios.get(0);

                    if(usuario.getUsuHabilitado()!=null){
                        if(usuario.getUsuHabilitado()==1){
                            habilitado = Constantes.USR_HABILITADO;
                        }
                    }
                    
                    if(usuario.getUsuOmitirValidaciones()!= null){
                        if(usuario.getUsuOmitirValidaciones()!= 1){
                            omiteValidaciones = Constantes.USR_VAL_ACTIVAS;
                        }
                    }
                    
                    perfilBean.setOrigen(3);
                    perfilBean.setUsuarioId(usuario.getUsuId());
                    detAdeudoCredBean.setUsuario(usuario);

                       
                    /**
                     * Obtiene los movimientos de la tabla del detalle de los
                     * movimietnos que renderea la tabla de devoluciones
                     */
                    obtieneTotalesMovimientos();

                    /**
                     * Obtiene los movimientos que renderea la tabla de
                     * aportaciones voluntarias
                     */
                    obtieneTblAportacionVol();
                    obtAvalDeCreditos();

                    /**
                     * Obtiene la informacion del reporte pagos personal
                     */
                    obtieneTblPagos();
                    obtieneAcumulado();

                    rdrFieldSets = Boolean.TRUE;
                    

                    if (usuario.getUsuEstatus() == 1) {
                        estEmpleado = Constantes.USU_ACTIVO_STR;
                        rdrFechaBaja = Boolean.FALSE;
                    } else {
                        estEmpleado = Constantes.USU_BAJA_STR;
                        rdrFechaBaja = Boolean.TRUE;
                    }

                } else {
                    rdrFieldSets = Boolean.FALSE;
                    rdrFechaBaja = Boolean.FALSE;
                    rdrAvalCreditos = Boolean.FALSE;
                    super.muestraMensajeError("El empleado no se encuentra la base", "", "msjBusqueda");
                }

            }
        } catch (BusinessException ex) {
            rdrFieldSets = Boolean.FALSE;
            rdrFechaBaja = Boolean.FALSE;
            rdrAvalCreditos = Boolean.FALSE;
            super.muestraMensajeError("Hubo un error al consultar el empleado, por favor"
                    + " vuelva a intentarlo", "", "msjBusqueda");

            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los totales de los movimientos de un usuario
     */
    public void obtieneTotalesMovimientos() {
        try {

            movimientos = movsBo.getAhorrosByUsuId(usuario.getUsuId());

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene los totales de los movimientos de un usuario
     */
    public void obtieneTblAportacionVol() {
        try {
            totalAhorro = 0.0;
            lstTotalesAhorro = movsBo.getAhorrosTotales(usuario.getUsuId());

            lstTotalesAhorro.forEach(x -> {

                totalAhorro = Util.round(x.getTotalMovimiento() + totalAhorro);

            });

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Muestra el detaalle de la aportacion que se esta seleccionando
     */
    public void verDetalle() {
        try {
            totalDetalle = 0.0;
            //Cuando es ahorro fijo y no fijo
            if (movSelected.getMovProducto() != 3) {
                rdrFNF = Boolean.TRUE;
                rdrV = Boolean.FALSE;
                detalleMovs = movsBo.getAhorrosDetalle(usuario.getUsuId(), movSelected.getMovProducto(), movSelected.getMovAr(), null);

                detalleMovs.forEach(x -> {

                    totalDetalle = Util.round(x.getMovDeposito() + totalDetalle);

                });
            } else {
                rdrV = Boolean.TRUE;
                rdrFNF = Boolean.FALSE;
                this.ahorrosVol = movsBo.getAhorrosDetalle(usuario.getUsuId(), movSelected.getMovProducto(), movSelected.getMovAr(), movSelected.getMovId());

            }

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Guarda una aportacion voluntaria en la tabla movimientos Este metodo
     * afecta bancos
     */
    public void guardaAportacionVoluntaria() {
        try {
            Movimientos aVoluntaria = new Movimientos();

            aVoluntaria.setMovAr(Constantes.MOV_AR_APO);
            aVoluntaria.setMovClaveEmpleado(usuario.getUsuClaveEmpleado());
            aVoluntaria.setMovDeposito(this.montoApoVol);
            aVoluntaria.setMovFecha(this.fechaApoVol);
            aVoluntaria.setMovEmpresa(usuario.getEmpresas().getEmpId());
            aVoluntaria.setMovEstatus(1);
            aVoluntaria.setMovProducto(Constantes.MOV_PRODUCTO_VOL);
            aVoluntaria.setMovTipo(Constantes.MOV_TIPO_APORTACION);
            aVoluntaria.setMovUsuId(usuario.getUsuId());

            movsBo.guardaMovimiento(aVoluntaria);
            
            tranBo.guardaTransaccion(16, aVoluntaria.getMovId(), userSession.getId());
            
            BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
            
            Bancos banco = bancoBo.generaBancoAVol(aVoluntaria, idRelacion.longValue());

            //Guarda afectando en las 3 tablas de bancos (banco, estado_cuenta, banco_edocta)
            bancoBo.guardaBancoCEdoCta(banco, idRelacion.longValue());

            obtieneTotalesMovimientos();
            obtieneTblAportacionVol();
            /**
             * Actualiza las tablas de movimientos en pantalla
             */
            super.hideShowDlg("PF('dlgAhorrosW').hide();");
            super.muestraMensajeExito("La aportación fue realizada con éxito", "", null);
            super.updtComponent("frmAhorros");
            super.updtComponent("frmDevolucionesAhorro:tblAhorros");
            limpiaDlgApoVol();

        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al intentar guardar la aportacion", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    private void limpiaDlgApoVol() {
        this.montoApoVol = null;
        this.fechaApoVol = null;
        super.updtComponent("frmApoVol");
    }

    
    public void inicializaDialogDev(){
        
        if(this.movDevSelected != null){
            this.movDevSelected.setDevolucion(this.movDevSelected.getTotalMovimiento());
            this.movDevSelected.setDevolucionFecha(Util.restaHrToDate(new Date(), 6));
        }
        
    }
    
    
    /**
     * Al editar una fila y dar aceptar, introduce en base de datos un registro
     * como devolucion
     *
     * @param event
     */
    public void aplicaDevolucion() {

        System.out.println(movDevSelected.getTotalMovimiento()-movDevSelected.getDevolucion());
        if((movDevSelected.getTotalMovimiento()-movDevSelected.getDevolucion()) >= 0 ){
            Movimientos pojo = creaDevolucion(movDevSelected);

            guardaDevoluciones(pojo);
            obtieneTotalesMovimientos();
            obtieneTblAportacionVol();

            movDevSelected = null;
        }else{
            super.muestraMensajeError("No puede devolver más de lo que se tiene en el ahorro", "", "reDevs");
        }
        /**
         * Actualiza también la tabla de aportaciones voluntarias, ya que
         * tambien se alteran esos montos
         */
        super.updtComponent("frmAhorros");

    }

    /**
     * Crea un objeto de tipo movimiento y lo agrega a la lista de devoluciones
     * que se insertarán posteriormente
     *
     * @param dtoSelected
     * @return
     */
    public Movimientos creaDevolucion(MovimientosDto dtoSelected) {
        Movimientos pojo = new Movimientos();

        pojo.setMovAr(dtoSelected.getMovAr());
        pojo.setMovClaveEmpleado(dtoSelected.getMovClaveEmpleado());
        pojo.setMovDeposito((dtoSelected.getDevolucion() != null ? dtoSelected.getDevolucion() : 0.0) * -1);
        pojo.setMovEmpresa(empresa.getEmpId());
        pojo.setMovEstatus(0);
        pojo.setMovFecha(movDevSelected.getDevolucionFecha());
        pojo.setMovProducto(dtoSelected.getMovProducto());

        pojo.setMovTipo(Constantes.MOV_TIPO_DEVOLUCION);

        pojo.setMovUsuId(dtoSelected.getMovUsuId());

        if (pojo.getMovProducto() == 3) {
            pojo.setMovIdPadre(dtoSelected.getMovId());
        }

        return pojo;
    }

    /**
     * Guarda una lista de devoluciones
     *
     * @param mov
     */
    public void guardaDevoluciones(Object mov) {
        try {
            if (mov instanceof Movimientos) {
                movsBo.guardaMovimiento((Movimientos) mov);
                
                tranBo.guardaTransaccion(15,((Movimientos) mov).getMovId(), userSession.getId());
                
                Bancos pojo = bancoBo.generaBancoDev((Movimientos) mov);
                //Guarda en BANCOS, ESTADO_CUENTA Y BANCO_EDOCTA
                bancoBo.guardaBancoCEdoCta(pojo, pojo.getBanIdRelacion());
                super.muestraMensajeExito("La devolución fue realizada", "", "reDevs");
            }
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al realizar la devolución", "", "reDevs");
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void openDlgTransferir() {
        DetalleCreditoDto credito = detAdeudoCredBean.getCreditoSelected();
        transAvalesBean.setCredito(credito);
        transAvalesBean.setCmpntToUpdt("frmCredsEmpleado");
        transAvalesBean.init();

    }

    /**
     * Inicializa los valores necesarios antes de abrir el dialog de
     * amortizacion y tambien lo abre
     */
    public void initDlgAmortizacion() {
        DetalleCreditoDto credito = detAdeudoCredBean.getCreditoSelected();
        amoRepBean.setUsuario(usuario);
        amoRepBean.setCredito(credito);

        amoRepBean.obtieneAmortizacion();

        super.hideShowDlg("PF('dlgAmortizacionW').show()");
    }

    public void openDlgVerAvales() {
        DetalleCreditoDto credito = detAdeudoCredBean.getCreditoSelected();
        try {
            avales = avalesBo.getAvalesXCredito(credito.getCreId());

            if (credito.getCreEstatusId() == Constantes.CRE_EST_TRANSFERIDO) {
                rdrAvalCredTransf = Boolean.TRUE;
            }
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void openDlgCrePadre() {
        DetalleCreditoDto credito = detAdeudoCredBean.getCreditoSelected();
        try {
            creditoPadre = creBo.getCreditoPadre(credito);

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void updtPantallaAhorros(CloseEvent event) {
        obtieneTblAportacionVol();
        obtieneTotalesMovimientos();
        super.updtComponent("frmAhorros");
        super.updtComponent("frmDevolucionesAhorro");
    }
    
    public void updateMontosPagosYAcum(){
          obtieneTblPagos();
          obtieneAcumulado();
          super.updtComponent("frmPagos");
          super.updtComponent("frmDevolucionesPagos");
    }
    

    public void obtAvalDeCreditos() {
        try {
            creditosAval = creBo.getCreditosDeAval(usuario.getUsuId());
            if (!creditosAval.isEmpty()) {
                rdrAvalCreditos = Boolean.TRUE;
            }
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al traer los creditos de aval", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void initDlgAddNota() {
        notaContent = "";
        notaTitulo = "";
        super.hideShowDlg("PF('dlgAgregaNota').show()");
    }

    public void initDlgNotas() {
        bitaDao = new BitacoraDao();
        try {
            this.notas = bitaDao.getBitacoraXReferencia(new BigInteger(usuario.getUsuId() + ""), Constantes.BIT_NOTA_USUARIO);
            super.hideShowDlg("PF('dlgLstNotas').show()");
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void guardaRegBitacora() {
        try {
            bitaDao = new BitacoraDao();

            Bitacora bitacora = new Bitacora();
            bitacora.setBitNota(notaContent);
            bitacora.setBitFecha(new Date());
            bitacora.setBitReferencia(usuario.getUsuId().longValue());
            bitacora.setBitTipo(Constantes.BIT_NOTA_USUARIO);
            bitacora.setBitTitulo(notaTitulo);
            bitacora.setBitUsuario(userSession.getId());

            bitaDao.saveBitacora(bitacora);

            super.hideShowDlg("PF('dlgAgregaNota').hide()");
        } catch (IntegracionException ex) {
            LOGGER.error("Error al guardar la nota", ex);
            super.muestraMensajeError("Hubo un error al guardar la nota", "", null);
        }
    }

    private void obtieneAcumulado() {
        try {
            setAcumulado(pagosAgrupadosBo.obtieneAcumulado(usuario.getUsuId()));
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al traer los pagos del usuario", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void onEditAcumulado(RowEditEvent event) {

        PagosPersonalDto pag = (PagosPersonalDto) event.getObject();

        Pagos pojo = creaDevolucionPago(pag);

        guardaDevolucionPago(pojo);
        obtieneTblPagos();
        obtieneAcumulado();

        /**
         * Actualiza también la tabla de aportaciones voluntarias, ya que
         * tambien se alteran esos montos
         */
        super.updtComponent("frmPagos");

    }

    public Pagos creaDevolucionPago(PagosPersonalDto dtoSelected) {
        Pagos pojo = new Pagos();

        pojo.setPagClaveEmpleado(claveEmpleado);
        pojo.setPagEmpresa(empresa.getEmpId());
        pojo.setPagUsuId(dtoSelected.getUsuariosId());
        pojo.setPagDeposito(dtoSelected.getDevolucion());
        pojo.setPagFecha(dtoSelected.getFechaDevolucion());
        pojo.setPagAcumulado((dtoSelected.getDevolucion() != null ? dtoSelected.getDevolucion() : 0.0) * -1);
        pojo.setPagEstatus(11);

        return pojo;
    }

    /**
     * Guarda la devolución del acumulado
     * @param pag 
     */
    public void guardaDevolucionPago(Pagos pag) {
        try {

            pagosAgrupadosBo.guardaPago(pag);
            
            tranBo.guardaTransaccion(17, pag.getPagId(), userSession.getId());
             
            BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
            Bancos pojo = bancoBo.generaBancoDevAcumulado(pag, idRelacion.longValue());
            bancoBo.guardaBancoCEdoCta(pojo, idRelacion.longValue());
            
            
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al realizar la devolución", "", "reDevs");
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void obtieneTblPagos() {
        try {
            pagosAgrupados = pagosAgrupadosBo.obtienePagosAcumulados(usuario.getUsuId());
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al traer los pagos del usuario", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void verDetallePagos() {
        try {
            detallePagosByEstatus = pagosAgrupadosBo.getPagosDetalle(usuario.getUsuId(), estatusPagSelected.getClaveEstatus());
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que se manda llamar al ejecutar el boton de habilitar/deshabilitar usuario.
     * 
     * Cambia el estatus habilitado en la base de datos al estatus contrario dependiendo de
     * si su valor actual es nulo, cero o uno
     */
    public void habilitarDeshabilitar(){
        
        int habilita=0;
        
        if(usuario.getUsuHabilitado()==null){
            habilita = 0;
            habilitado = Constantes.USR_DESHABILITADO;
        }else if(usuario.getUsuHabilitado()==1){
            habilita = 0;
            habilitado = Constantes.USR_DESHABILITADO;
        }else if(usuario.getUsuHabilitado()==0){
            habilita = 1;
            habilitado = Constantes.USR_HABILITADO;
        }
        LOGGER.info("Cambia estatus habilita a "+habilita);
        usuario.setUsuHabilitado(habilita);
        
        try {
            busqEmpBo.updtUsrHabilitar(usuario.getUsuId(), habilita);
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al actualizar el estatus del usuario", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    /**
     * 
     */
    public void omitirValidaciones(){
        
        int omite=0;
        
        if(null==usuario.getUsuOmitirValidaciones()){
            omite = 0;
            omiteValidaciones = Constantes.USR_VAL_ACTIVAS;
        }else switch (usuario.getUsuOmitirValidaciones()) {
            case 1:
                omite = 0;
                omiteValidaciones = Constantes.USR_VAL_ACTIVAS;
                break;
            case 0:
                omite = 1;
                omiteValidaciones = Constantes.USR_OMITIR;
                break;
            default:
                break;
        }
        LOGGER.info("Cambia omitirValidaciones a "+omite);
        usuario.setUsuOmitirValidaciones(omite);
        
        try {
            busqEmpBo.updtUsrOmitirVals(usuario.getUsuId(), omite);
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al actualizar el estatus del usuario", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    
    /**
     * @return the empresas
     */
    public List<EmpresasDto> getEmpresas() {
        return empresas;
    }

    /**
     * @param empresas the empresas to set
     */
    public void setEmpresas(List<EmpresasDto> empresas) {
        this.empresas = empresas;
    }

    /**
     * @return the empresa
     */
    public EmpresasDto getEmpresa() {
        return empresa;
    }

    /**
     * @param empresa the empresa to set
     */
    public void setEmpresa(EmpresasDto empresa) {
        this.empresa = empresa;
    }

    /**
     * @return the claveEmpleado
     */
    public Integer getClaveEmpleado() {
        return claveEmpleado;
    }

    /**
     * @param claveEmpleado the claveEmpleado to set
     */
    public void setClaveEmpleado(Integer claveEmpleado) {
        this.claveEmpleado = claveEmpleado;
    }

    /**
     * @return the usuarios
     */
    public List<Usuarios> getUsuarios() {
        return usuarios;
    }

    /**
     * @param usuarios the usuarios to set
     */
    public void setUsuarios(List<Usuarios> usuarios) {
        this.usuarios = usuarios;
    }

    /**
     * @return the usuario
     */
    public Usuarios getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the perfilBean
     */
    public PerfilBean getPerfilBean() {
        return perfilBean;
    }

    /**
     * @param perfilBean the perfilBean to set
     */
    public void setPerfilBean(PerfilBean perfilBean) {
        this.perfilBean = perfilBean;
    }

    /**
     * @return the rdrFieldSets
     */
    public Boolean getRdrFieldSets() {
        return rdrFieldSets;
    }

    /**
     * @param rdrFieldSets the rdrFieldSets to set
     */
    public void setRdrFieldSets(Boolean rdrFieldSets) {
        this.rdrFieldSets = rdrFieldSets;
    }

    /**
     * @return the estEmpleado
     */
    public String getEstEmpleado() {
        return estEmpleado;
    }

    /**
     * @param estEmpleado the estEmpleado to set
     */
    public void setEstEmpleado(String estEmpleado) {
        this.estEmpleado = estEmpleado;
    }

    /**
     * @return the detAdeudoCredBean
     */
    public DetalleAdeudoCreditosBean getDetAdeudoCredBean() {
        return detAdeudoCredBean;
    }

    /**
     * @param detAdeudoCredBean the detAdeudoCredBean to set
     */
    public void setDetAdeudoCredBean(DetalleAdeudoCreditosBean detAdeudoCredBean) {
        this.detAdeudoCredBean = detAdeudoCredBean;
    }

    /**
     * @return the movimientos
     */
    public List<MovimientosDto> getMovimientos() {
        return movimientos;
    }

    /**
     * @param movimientos the movimientos to set
     */
    public void setMovimientos(List<MovimientosDto> movimientos) {
        this.movimientos = movimientos;
    }

    /**
     * @return the movSelected
     */
    public MovimientosDto getMovSelected() {
        return movSelected;
    }

    /**
     * @param movSelected the movSelected to set
     */
    public void setMovSelected(MovimientosDto movSelected) {
        this.movSelected = movSelected;
    }

    /**
     * @return the editTable
     */
    public Boolean getEditTable() {
        return editTable;
    }

    /**
     * @param editTable the editTable to set
     */
    public void setEditTable(Boolean editTable) {
        this.editTable = editTable;
    }

    /**
     * @return the lstTotalesAhorro
     */
    public List<MovimientosDto> getLstTotalesAhorro() {
        return lstTotalesAhorro;
    }

    /**
     * @param lstTotalesAhorro the lstTotalesAhorro to set
     */
    public void setLstTotalesAhorro(List<MovimientosDto> lstTotalesAhorro) {
        this.lstTotalesAhorro = lstTotalesAhorro;
    }

    /**
     * @return the totalAhorro
     */
    public Double getTotalAhorro() {
        return totalAhorro;
    }

    /**
     * @param totalAhorro the totalAhorro to set
     */
    public void setTotalAhorro(Double totalAhorro) {
        this.totalAhorro = totalAhorro;
    }

    /**
     * @return the fechaApoVol
     */
    public Date getFechaApoVol() {
        return fechaApoVol;
    }

    /**
     * @param fechaApoVol the fechaApoVol to set
     */
    public void setFechaApoVol(Date fechaApoVol) {
        this.fechaApoVol = fechaApoVol;
    }

    /**
     * @return the montoApoVol
     */
    public Double getMontoApoVol() {
        return montoApoVol;
    }

    /**
     * @param montoApoVol the montoApoVol to set
     */
    public void setMontoApoVol(Double montoApoVol) {
        this.montoApoVol = montoApoVol;
    }

    /**
     * @return the rdrFNF
     */
    public Boolean getRdrFNF() {
        return rdrFNF;
    }

    /**
     * @param rdrFNF the rdrFNF to set
     */
    public void setRdrFNF(Boolean rdrFNF) {
        this.rdrFNF = rdrFNF;
    }

    /**
     * @return the rdrV
     */
    public Boolean getRdrV() {
        return rdrV;
    }

    /**
     * @param rdrV the rdrV to set
     */
    public void setRdrV(Boolean rdrV) {
        this.rdrV = rdrV;
    }

    /**
     * @return the detalleMovs
     */
    public List getDetalleMovs() {
        return detalleMovs;
    }

    /**
     * @param detalleMovs the detalleMovs to set
     */
    public void setDetalleMovs(List detalleMovs) {
        this.detalleMovs = detalleMovs;
    }

    /**
     * @return the ahorrosVol
     */
    public List<AhorroVoluntarioDto> getAhorrosVol() {
        return ahorrosVol;
    }

    /**
     * @param ahorrosVol the ahorrosVol to set
     */
    public void setAhorrosVol(List<AhorroVoluntarioDto> ahorrosVol) {
        this.ahorrosVol = ahorrosVol;
    }

    /**
     * @return the transAvalesBean
     */
    public TransfiereAvalesBean getTransAvalesBean() {
        return transAvalesBean;
    }

    /**
     * @param transAvalesBean the transAvalesBean to set
     */
    public void setTransAvalesBean(TransfiereAvalesBean transAvalesBean) {
        this.transAvalesBean = transAvalesBean;
    }

    /**
     * @return the amoRepBean
     */
    public AmortizacionRepEmpBean getAmoRepBean() {
        return amoRepBean;
    }

    /**
     * @param amoRepBean the amoRepBean to set
     */
    public void setAmoRepBean(AmortizacionRepEmpBean amoRepBean) {
        this.amoRepBean = amoRepBean;
    }

    /**
     * @return the avales
     */
    public List<UsuarioDto> getAvales() {
        return avales;
    }

    /**
     * @param avales the avales to set
     */
    public void setAvales(List<UsuarioDto> avales) {
        this.avales = avales;
    }

    /**
     * @return the rdrAvalCredTransf
     */
    public Boolean getRdrAvalCredTransf() {
        return rdrAvalCredTransf;
    }

    /**
     * @param rdrAvalCredTransf the rdrAvalCredTransf to set
     */
    public void setRdrAvalCredTransf(Boolean rdrAvalCredTransf) {
        this.rdrAvalCredTransf = rdrAvalCredTransf;
    }

    /**
     * @return the creditoPadre
     */
    public CreditoDto getCreditoPadre() {
        return creditoPadre;
    }

    /**
     * @param creditoPadre the creditoPadre to set
     */
    public void setCreditoPadre(CreditoDto creditoPadre) {
        this.creditoPadre = creditoPadre;
    }

    /**
     * @return the rdrAvalCreditos
     */
    public Boolean getRdrAvalCreditos() {
        return rdrAvalCreditos;
    }

    /**
     * @param rdrAvalCreditos the rdrAvalCreditos to set
     */
    public void setRdrAvalCreditos(Boolean rdrAvalCreditos) {
        this.rdrAvalCreditos = rdrAvalCreditos;
    }

    /**
     * @return the creditosAval
     */
    public List<CreditoDto> getCreditosAval() {
        return creditosAval;
    }

    /**
     * @param creditosAval the creditosAval to set
     */
    public void setCreditosAval(List<CreditoDto> creditosAval) {
        this.creditosAval = creditosAval;
    }

    /**
     * @return the notas
     */
    public List<Bitacora> getNotas() {
        return notas;
    }

    /**
     * @param notas the notas to set
     */
    public void setNotas(List<Bitacora> notas) {
        this.notas = notas;
    }

    /**
     * @return the notaSeleccionada
     */
    public Bitacora getNotaSeleccionada() {
        return notaSeleccionada;
    }

    /**
     * @param notaSeleccionada the notaSeleccionada to set
     */
    public void setNotaSeleccionada(Bitacora notaSeleccionada) {
        this.notaSeleccionada = notaSeleccionada;
    }

    /**
     * @return the notaTitulo
     */
    public String getNotaTitulo() {
        return notaTitulo;
    }

    /**
     * @param notaTitulo the notaTitulo to set
     */
    public void setNotaTitulo(String notaTitulo) {
        this.notaTitulo = notaTitulo;
    }

    /**
     * @return the notaContent
     */
    public String getNotaContent() {
        return notaContent;
    }

    /**
     * @param notaContent the notaContent to set
     */
    public void setNotaContent(String notaContent) {
        this.notaContent = notaContent;
    }

    /**
     * @return the userSession
     */
    public UsuarioDto getUserSession() {
        return userSession;
    }

    /**
     * @return the pagosAgrupados
     */
    public List<PagosPersonalDto> getPagosAgrupados() {
        return pagosAgrupados;
    }

    /**
     * @param pagosAgrupados the pagosAgrupados to set
     */
    public void setPagosAgrupados(List<PagosPersonalDto> pagosAgrupados) {
        this.pagosAgrupados = pagosAgrupados;
    }

    /**
     * @return the pagosAgrupadosBo
     */
    public PagosPersonalBo getPagosAgrupadosBo() {
        return pagosAgrupadosBo;
    }

    /**
     * @param pagosAgrupadosBo the pagosAgrupadosBo to set
     */
    public void setPagosAgrupadosBo(PagosPersonalBo pagosAgrupadosBo) {
        this.pagosAgrupadosBo = pagosAgrupadosBo;
    }

    /**
     * @return the estatusPagSelected
     */
    public PagosPersonalDto getEstatusPagSelected() {
        return estatusPagSelected;
    }

    /**
     * @param estatusPagSelected the estatusPagSelected to set
     */
    public void setEstatusPagSelected(PagosPersonalDto estatusPagSelected) {
        this.estatusPagSelected = estatusPagSelected;
    }

    /**
     * @return the detallePagosByEstatus
     */
    public List<PagosPersonalDto> getDetallePagosByEstatus() {
        return detallePagosByEstatus;
    }

    /**
     * @param detallePagosByEstatus the detallePagosByEstatus to set
     */
    public void setDetallePagosByEstatus(List<PagosPersonalDto> detallePagosByEstatus) {
        this.detallePagosByEstatus = detallePagosByEstatus;
    }

    /**
     * @return the acumulado
     */
    public List<PagosPersonalDto> getAcumulado() {
        return acumulado;
    }

    /**
     * @param acumulado the acumulado to set
     */
    public void setAcumulado(List<PagosPersonalDto> acumulado) {
        this.acumulado = acumulado;
    }

    /**
     * @return the habilitado
     */
    public String getHabilitado() {
        return habilitado;
    }

    /**
     * @param habilitado the habilitado to set
     */
    public void setHabilitado(String habilitado) {
        this.habilitado = habilitado;
    }

    /**
     * @return the rdrFechaBaja
     */
    public Boolean getRdrFechaBaja() {
        return rdrFechaBaja;
    }

    /**
     * @param rdrFechaBaja the rdrFechaBaja to set
     */
    public void setRdrFechaBaja(Boolean rdrFechaBaja) {
        this.rdrFechaBaja = rdrFechaBaja;
    }

    /**
     * @return the movDevSelected
     */
    public MovimientosDto getMovDevSelected() {
        return movDevSelected;
    }

    /**
     * @param movDevSelected the movDevSelected to set
     */
    public void setMovDevSelected(MovimientosDto movDevSelected) {
        this.movDevSelected = movDevSelected;
    }

    /**
     * @return the omiteValidaciones
     */
    public String getOmiteValidaciones() {
        return omiteValidaciones;
    }

    /**
     * @param omiteValidaciones the omiteValidaciones to set
     */
    public void setOmiteValidaciones(String omiteValidaciones) {
        this.omiteValidaciones = omiteValidaciones;
    }

}
