/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.jasper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Venus
 */
public class CreditoDto implements Serializable{
private static final long serialVersionUID = 1L  ;
    
/**
 * Bloque de atributos que se utilizan para la tabla de amortizaciones
 */
       private Integer idPago;
       private Integer idSolicitud;
       private Integer idUsuario;
       private Integer idEmpresa;
    private Integer noPago;
    private Integer pagosPendientes;
    private Double capital;
    private Double amortizacion;
    private Double interes;
    private Double iva;
    private Double montoPago;
    private Double saldo;
    private Double sumaMontosPago;
    private String fechaPago;
    private Date fechaInicioCredito;
    
    /**Clave de registro del credito*/
    private String clave;
    /**El total que se presto al cliente*/
    private Double prestamo;
    /**El número total de pagos en que se liquidara el prestamo*/
    private Integer pagos; 
    /**Fecha vencimiento del prestamo*/
    private String sFechaVencimiento;
    /**Fecha vencimiento del prestamo*/
    private Date fechaVencimiento;
    /**El saldo del usuario*/
    private Double remanente;
    /**La fecha en que se otorgo el prestamo*/
    private String sFechaInicio;
    private Integer idCredito;
    private Date fechaTerminoCredito;
    private Date fechaDeposito;
    private String tipoCredito;
    private Double pago;
    private Date fechaPagoCredito;
    private Double totalMontoPago;
    private Double totalMontoPagar;
    private Integer claveEmpleado;
    private Integer cveEmpleadoPadre;
    private Integer noPagoActual;
    private String claveCredito;
    private String estatusPago;
    private CreditoDto pagosAmortizacion;
    private PagoDto pagosDto;
    private Integer estatusCredito;
    private String estatusCreditoDescripcion;
    private String empresaAbreviacion;
    private Integer creProducto;
    private Boolean tieneMas1Cred;
    private Integer estatusAmo;
    private Integer amoPagoId;
    private Integer padre;
    private boolean esHeredado;
    
    private String transferidoDeCred;
    private Integer transferidoDeEmp;
    
   private  List<CreditoDto> pagosPendientesLst;
   private  List<CreditoDto> creditosAvales;
   private  CreditoDto amortizacionAsignarPagoCapital;
   private String cveAvales;
   private Integer catorcenasPdtsXCred;
    

    public CreditoDto (){
        tieneMas1Cred = Boolean.FALSE;
    }
    
    public CreditoDto (String clave, Double prestamo, Integer pagos, String fechaVencimiento, Double remanente){
        this.clave= clave;
        this.prestamo = prestamo;
        this.pagos = pagos;
        this.sFechaVencimiento = fechaVencimiento;
        this.remanente = remanente;
    }
    /**
     * @return the idPago
     */
    public Integer getIdPago() {
        return idPago;
    }

    /**
     * @param idPago the idPago to set
     */
    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    /**
     * @return the noPago
     */
    public Integer getNoPago() {
        return noPago;
    }

    /**
     * @param noPago the noPago to set
     */
    public void setNoPago(Integer noPago) {
        this.noPago = noPago;
    }

    /**
     * @return the capital
     */
    public Double getCapital() {
        return capital;
    }

    /**
     * @param capital the capital to set
     */
    public void setCapital(Double capital) {
        this.capital = capital;
    }

    /**
     * @return the amortizacion
     */
    public Double getAmortizacion() {
        return amortizacion;
    }

    /**
     * @param amortizacion the amortizacion to set
     */
    public void setAmortizacion(Double amortizacion) {
        this.amortizacion = amortizacion;
    }

    /**
     * @return the interes
     */
    public Double getInteres() {
        return interes;
    }

    /**
     * @param interes the interes to set
     */
    public void setInteres(Double interes) {
        this.interes = interes;
    }

    /**
     * @return the iva
     */
    public Double getIva() {
        return iva;
    }

    /**
     * @param iva the iva to set
     */
    public void setIva(Double iva) {
        this.iva = iva;
    }

    /**
     * @return the montoPago
     */
    public Double getMontoPago() {
        return montoPago;
    }

    /**
     * @param montoPago the montoPago to set
     */
    public void setMontoPago(Double montoPago) {
        this.montoPago = montoPago;
    }

    /**
     * @return the fechaPago
     */
    public String getFechaPago() {
        return fechaPago;
    }

    /**
     * @param fechaPago the fechaPago to set
     */
    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    

    /**
     * @return the clave
     */
    public String getClave() {
        return clave;
    }

    /**
     * @param clave the clave to set
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

    /**
     * @return the prestamo
     */
    public Double getPrestamo() {
        return prestamo;
    }

    /**
     * @param prestamo the prestamo to set
     */
    public void setPrestamo(Double prestamo) {
        this.prestamo = prestamo;
    }

    /**
     * @return the pagos
     */
    public Integer getPagos() {
        return pagos;
    }

    /**
     * @param pagos the pagos to set
     */
    public void setPagos(Integer pagos) {
        this.pagos = pagos;
    }

    /**
     * @return the sFechaVencimiento
     */
    public String getsFechaVencimiento() {
        return sFechaVencimiento;
    }

    /**
     * @param sFechaVencimiento the sFechaVencimiento to set
     */
    public void setsFechaVencimiento(String sFechaVencimiento) {
        this.sFechaVencimiento = sFechaVencimiento;
    }

    /**
     * @return the fechaVencimiento
     */
    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    /**
     * @param fechaVencimiento the fechaVencimiento to set
     */
    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    /**
     * @return the remanente
     */
    public Double getRemanente() {
        return remanente;
    }

    /**
     * @param remanente the remanente to set
     */
    public void setRemanente(Double remanente) {
        this.remanente = remanente;
    }

    /**
     * @return the sFechaInicio
     */
    public String getsFechaInicio() {
        return sFechaInicio;
    }

    /**
     * @param sFechaInicio the sFechaInicio to set
     */
    public void setsFechaInicio(String sFechaInicio) {
        this.sFechaInicio = sFechaInicio;
    }

    /**
     * @return the fechaInicioCredito
     */
    public Date getFechaInicioCredito() {
        return fechaInicioCredito;
    }

    /**
     * @param fechaInicioCredito the fechaInicioCredito to set
     */
    public void setFechaInicioCredito(Date fechaInicioCredito) {
        this.fechaInicioCredito = fechaInicioCredito;
    }

    /**
     * @return the idCredito
     */
    public Integer getIdCredito() {
        return idCredito;
    }

    /**
     * @param idCredito the idCredito to set
     */
    public void setIdCredito(Integer idCredito) {
        this.idCredito = idCredito;
    }

    /**
     * @return the fechaTerminoCredito
     */
    public Date getFechaTerminoCredito() {
        return fechaTerminoCredito;
    }

    /**
     * @param fechaTerminoCredito the fechaTerminoCredito to set
     */
    public void setFechaTerminoCredito(Date fechaTerminoCredito) {
        this.fechaTerminoCredito = fechaTerminoCredito;
    }

    /**
     * @return the fechaDeposito
     */
    public Date getFechaDeposito() {
        return fechaDeposito;
    }

    /**
     * @param fechaDeposito the fechaDeposito to set
     */
    public void setFechaDeposito(Date fechaDeposito) {
        this.fechaDeposito = fechaDeposito;
    }

    /**
     * @return the tipoCredito
     */
    public String getTipoCredito() {
        return tipoCredito;
    }

    /**
     * @param tipoCredito the tipoCredito to set
     */
    public void setTipoCredito(String tipoCredito) {
        this.tipoCredito = tipoCredito;
    }

    /**
     * @return the pago
     */
    public Double getPago() {
        return pago;
    }

    /**
     * @param pago the pago to set
     */
    public void setPago(Double pago) {
        this.pago = pago;
    }

    /**
     * @return the fechaPagoCredito
     */
    public Date getFechaPagoCredito() {
        return fechaPagoCredito;
    }

    /**
     * @param fechaPagoCredito the fechaPagoCredito to set
     */
    public void setFechaPagoCredito(Date fechaPagoCredito) {
        this.fechaPagoCredito = fechaPagoCredito;
    }

    /**
     * @return the totalMontoPago
     */
    public Double getTotalMontoPago() {
        return totalMontoPago;
    }

    /**
     * @param totalMontoPago the totalMontoPago to set
     */
    public void setTotalMontoPago(Double totalMontoPago) {
        this.totalMontoPago = totalMontoPago;
    }

    /**
     * @return the totalMontoPagar
     */
    public Double getTotalMontoPagar() {
        return totalMontoPagar;
    }

    /**
     * @param totalMontoPagar the totalMontoPagar to set
     */
    public void setTotalMontoPagar(Double totalMontoPagar) {
        this.totalMontoPagar = totalMontoPagar;
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
     * @return the noPagoActual
     */
    public Integer getNoPagoActual() {
        return noPagoActual;
    }

    /**
     * @param noPagoActual the noPagoActual to set
     */
    public void setNoPagoActual(Integer noPagoActual) {
        this.noPagoActual = noPagoActual;
    }

    /**
     * @return the idSolicitud
     */
    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * @param idSolicitud the idSolicitud to set
     */
    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /**
     * @return the saldo
     */
    public Double getSaldo() {
        return saldo;
    }

    /**
     * @param saldo the saldo to set
     */
    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    /**
     * @return the claveCredito
     */
    public String getClaveCredito() {
        return claveCredito;
    }

    /**
     * @param claveCredito the claveCredito to set
     */
    public void setClaveCredito(String claveCredito) {
        this.claveCredito = claveCredito;
    }

    /**
     * @return the estatusPago
     */
    public String getEstatusPago() {
        return estatusPago;
    }

    /**
     * @param estatusPago the estatusPago to set
     */
    public void setEstatusPago(String estatusPago) {
        this.estatusPago = estatusPago;
    }

    /**
     * @return the pagosAmortizacion
     */
    public CreditoDto getPagosAmortizacion() {
        return pagosAmortizacion;
    }

    /**
     * @param pagosAmortizacion the pagosAmortizacion to set
     */
    public void setPagosAmortizacion(CreditoDto pagosAmortizacion) {
        this.pagosAmortizacion = pagosAmortizacion;
    }

    /**
     * @return the pagosPendientes
     */
    public Integer getPagosPendientes() {
        return pagosPendientes;
    }

    /**
     * @param pagosPendientes the pagosPendientes to set
     */
    public void setPagosPendientes(Integer pagosPendientes) {
        this.pagosPendientes = pagosPendientes;
    }

    /**
     * @return the estatusCredito
     */
    public Integer getEstatusCredito() {
        return estatusCredito;
    }

    /**
     * @param estatusCredito the estatusCredito to set
     */
    public void setEstatusCredito(Integer estatusCredito) {
        this.estatusCredito = estatusCredito;
    }

    /**
     * @return the estatusCreditoDescripcion
     */
    public String getEstatusCreditoDescripcion() {
        return estatusCreditoDescripcion;
    }

    /**
     * @param estatusCreditoDescripcion the estatusCreditoDescripcion to set
     */
    public void setEstatusCreditoDescripcion(String estatusCreditoDescripcion) {
        this.estatusCreditoDescripcion = estatusCreditoDescripcion;
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
     * @return the idEmpresa
     */
    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    /**
     * @param idEmpresa the idEmpresa to set
     */
    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    /**
     * @return the pagosDto
     */
    public PagoDto getPagosDto() {
        return pagosDto;
    }

    /**
     * @param pagosDto the pagosDto to set
     */
    public void setPagosDto(PagoDto pagosDto) {
        this.pagosDto = pagosDto;
    }

    /**
     * @return the creProducto
     */
    public Integer getCreProducto() {
        return creProducto;
    }

    /**
     * @param creProducto the creProducto to set
     */
    public void setCreProducto(Integer creProducto) {
        this.creProducto = creProducto;
    }

    /**
     * @return the tieneMas1Cred
     */
    public Boolean getTieneMas1Cred() {
        return tieneMas1Cred;
    }

    /**
     * @param tieneMas1Cred the tieneMas1Cred to set
     */
    public void setTieneMas1Cred(Boolean tieneMas1Cred) {
        this.tieneMas1Cred = tieneMas1Cred;
    }

    /**
     * @return the estatusAmo
     */
    public Integer getEstatusAmo() {
        return estatusAmo;
    }

    /**
     * @param estatusAmo the estatusAmo to set
     */
    public void setEstatusAmo(Integer estatusAmo) {
        this.estatusAmo = estatusAmo;
    }

    /**
     * @return the empresaAbreviacion
     */
    public String getEmpresaAbreviacion() {
        return empresaAbreviacion;
    }

    /**
     * @param empresaAbreviacion the empresaAbreviacion to set
     */
    public void setEmpresaAbreviacion(String empresaAbreviacion) {
        this.empresaAbreviacion = empresaAbreviacion;
    }

    /**
     * @return the sumaMontosPago
     */
    public Double getSumaMontosPago() {
        return sumaMontosPago;
    }

    /**
     * @param sumaMontosPago the sumaMontosPago to set
     */
    public void setSumaMontosPago(Double sumaMontosPago) {
        this.sumaMontosPago = sumaMontosPago;
    }

    /**
     * @return the pagosPendientesLst
     */
    public List<CreditoDto> getPagosPendientesLst() {
        return pagosPendientesLst;
    }

    /**
     * @param pagosPendientesLst the pagosPendientesLst to set
     */
    public void setPagosPendientesLst(List<CreditoDto> pagosPendientesLst) {
        this.pagosPendientesLst = pagosPendientesLst;
    }

    /**
     * @return the amortizacionAsignarPagoCapital
     */
    public CreditoDto getAmortizacionAsignarPagoCapital() {
        return amortizacionAsignarPagoCapital;
    }

    /**
     * @param amortizacionAsignarPagoCapital the amortizacionAsignarPagoCapital to set
     */
    public void setAmortizacionAsignarPagoCapital(CreditoDto amortizacionAsignarPagoCapital) {
        this.amortizacionAsignarPagoCapital = amortizacionAsignarPagoCapital;
    }

    /**
     * @return the amoPagoId
     */
    public Integer getAmoPagoId() {
        return amoPagoId;
    }

    /**
     * @param amoPagoId the amoPagoId to set
     */
    public void setAmoPagoId(Integer amoPagoId) {
        this.amoPagoId = amoPagoId;
    }

       /**
     * @return the padre
     */
    public Integer getPadre() {
        return padre;
    }

    /**
     * @param padre the padre to set
     */
    public void setPadre(Integer padre) {
        this.padre = padre;
    }

    /**
     * @return the esHeredado
     */
    public boolean isEsHeredado() {
        return esHeredado;
    }

    /**
     * @param esHeredado the esHeredado to set
     */
    public void setEsHeredado(boolean esHeredado) {
        this.esHeredado = esHeredado;
    }

    /**
     * @return the cveEmpleadoPadre
     */
    public Integer getCveEmpleadoPadre() {
        return cveEmpleadoPadre;
    }

    /**
     * @param cveEmpleadoPadre the cveEmpleadoPadre to set
     */
    public void setCveEmpleadoPadre(Integer cveEmpleadoPadre) {
        this.cveEmpleadoPadre = cveEmpleadoPadre;
    }

    /**
     * @return the creditosAvales
     */
    public List<CreditoDto> getCreditosAvales() {
        return creditosAvales;
    }

    /**
     * @param creditosAvales the creditosAvales to set
     */
    public void setCreditosAvales(List<CreditoDto> creditosAvales) {
        this.creditosAvales = creditosAvales;
    }

    /**
     * @return the transferidoDeCred
     */
    public String getTransferidoDeCred() {
        return transferidoDeCred;
    }

    /**
     * @param transferidoDeCred the transferidoDeCred to set
     */
    public void setTransferidoDeCred(String transferidoDeCred) {
        this.transferidoDeCred = transferidoDeCred;
    }

    /**
     * @return the transferidoDeEmp
     */
    public Integer getTransferidoDeEmp() {
        return transferidoDeEmp;
    }

    /**
     * @param transferidoDeEmp the transferidoDeEmp to set
     */
    public void setTransferidoDeEmp(Integer transferidoDeEmp) {
        this.transferidoDeEmp = transferidoDeEmp;
    }

    /**
     * @return the cveAvales
     */
    public String getCveAvales() {
        return cveAvales;
    }

    /**
     * @param cveAvales the cveAvales to set
     */
    public void setCveAvales(String cveAvales) {
        this.cveAvales = cveAvales;
    }

    /**
     * @return the catorcenasPdtsXCred
     */
    public Integer getCatorcenasPdtsXCred() {
        return catorcenasPdtsXCred;
    }

    /**
     * @param catorcenasPdtsXCred the catorcenasPdtsXCred to set
     */
    public void setCatorcenasPdtsXCred(Integer catorcenasPdtsXCred) {
        this.catorcenasPdtsXCred = catorcenasPdtsXCred;
    }

    
   
}
