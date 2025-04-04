/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.jasper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import mx.com.evoti.dto.UsuarioDto;

/**
 *
 * @author NeOleon Root
 */
public class FondeoDto implements Serializable{

    private static final long serialVersionUID = 6327021811593719844L;
    
   private Long idSolicitud;
   private Integer noSolicitud;
    private Integer claveEmpleado;
    private Double montoSolicitado;
    private Double montoPago;
    private String banco;
    private String numeroCuenta;
    private String clabeInterbancaria;
    private String nombreTarjetahabiente;
    
    private String nombre;
    private String nombreEmpresa;
    private String calle;
    private String numext;
    private String colonia;
    private String cp;
    private String municipio;
    private String estado;
    private Date fechaUltimoPago;
    private Date fecha;
    private String claveCredito;
    private Integer idCredito;
    
    private String empresa;
    private Double pagoCatorcenal;
    
    private Date fechaNacimiento;
    private Date fechaPrimerPago;
    private String sexo;
    private String rfc;
    private String identificacion;
    private String correo;
    private String correoOpt;
    private String edoCivil;
    private String telefono;
    private String celular;
    private String direccion;
    private Integer catorcenas;
    
    private String puesto;
    private Date fechaIngreso;
    private Date fechaCreacionSolicitud;
    private Double salarioNeto;
    private String tipoCredito;
    private Double montoTotalPagar;
    
    private String rfcEmpresa;
    private String telefonoEmpresa;
    private String direccionEmpresa;
    
    private String producto;
    
    private List<UsuarioDto> avales;
    private List<PendienteDto> avalesSolicitud;
    private Integer idEmpleado;
    
    private String empresaAbre;
    private String productoAbre;
    
    private Integer solicitudNumero;
    private Integer idProducto;
    private Integer estatusSolicitud;
    private String estatusSolStr;
    
    private String lineaUno;
    private String lineaDos;
    private String lineaTres;
    private String avalUno;
    private String avalDos;
    private String AvalTres;
    private Date fechaDeposito;
    private String imgEstadoCuenta;
    private Integer interesOrdAnual;
    private Integer creProducto;
    

    /**
     * @return the idSolicitud
     */
    public Long getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * @param idSolicitud the idSolicitud to set
     */
    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
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
     * @return the montoSolicitado
     */
    public Double getMontoSolicitado() {
        return montoSolicitado;
    }

    /**
     * @param montoSolicitado the montoSolicitado to set
     */
    public void setMontoSolicitado(Double montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
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
     * @return the banco
     */
    public String getBanco() {
        return banco;
    }

    /**
     * @param banco the banco to set
     */
    public void setBanco(String banco) {
        this.banco = banco;
    }

    /**
     * @return the numeroCuenta
     */
    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    /**
     * @param numeroCuenta the numeroCuenta to set
     */
    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    /**
     * @return the clabeInterbancaria
     */
    public String getClabeInterbancaria() {
        return clabeInterbancaria;
    }

    /**
     * @param clabeInterbancaria the clabeInterbancaria to set
     */
    public void setClabeInterbancaria(String clabeInterbancaria) {
        this.clabeInterbancaria = clabeInterbancaria;
    }

    /**
     * @return the nombreTarjetahabiente
     */
    public String getNombreTarjetahabiente() {
        return nombreTarjetahabiente;
    }

    /**
     * @param nombreTarjetahabiente the nombreTarjetahabiente to set
     */
    public void setNombreTarjetahabiente(String nombreTarjetahabiente) {
        this.nombreTarjetahabiente = nombreTarjetahabiente;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the calle
     */
    public String getCalle() {
        return calle;
    }

    /**
     * @param calle the calle to set
     */
    public void setCalle(String calle) {
        this.calle = calle;
    }

    /**
     * @return the numext
     */
    public String getNumext() {
        return numext;
    }

    /**
     * @param numext the numext to set
     */
    public void setNumext(String numext) {
        this.numext = numext;
    }

    /**
     * @return the colonia
     */
    public String getColonia() {
        return colonia;
    }

    /**
     * @param colonia the colonia to set
     */
    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    /**
     * @return the cp
     */
    public String getCp() {
        return cp;
    }

    /**
     * @param cp the cp to set
     */
    public void setCp(String cp) {
        this.cp = cp;
    }

    /**
     * @return the municipio
     */
    public String getMunicipio() {
        return municipio;
    }

    /**
     * @param municipio the municipio to set
     */
    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    /**
     * @return the estado
     */
    public String getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(String estado) {
        this.estado = estado;
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
     * @return the fechaUltimoPago
     */
    public Date getFechaUltimoPago() {
        return fechaUltimoPago;
    }

    /**
     * @param fechaUltimoPago the fechaUltimoPago to set
     */
    public void setFechaUltimoPago(Date fechaUltimoPago) {
        this.fechaUltimoPago = fechaUltimoPago;
    }

    /**
     * @return the empresa
     */
    public String getEmpresa() {
        return empresa;
    }

    /**
     * @param empresa the empresa to set
     */
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    /**
     * @return the pagoCatorcenal
     */
    public Double getPagoCatorcenal() {
        return pagoCatorcenal;
    }

    /**
     * @param pagoCatorcenal the pagoCatorcenal to set
     */
    public void setPagoCatorcenal(Double pagoCatorcenal) {
        this.pagoCatorcenal = pagoCatorcenal;
    }

    /**
     * @return the fechaNacimiento
     */
    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * @param fechaNacimiento the fechaNacimiento to set
     */
    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * @return the sexo
     */
    public String getSexo() {
        return sexo;
    }

    /**
     * @param sexo the sexo to set
     */
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    /**
     * @return the rfc
     */
    public String getRfc() {
        return rfc;
    }

    /**
     * @param rfc the rfc to set
     */
    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    /**
     * @return the identificacion
     */
    public String getIdentificacion() {
        return identificacion;
    }

    /**
     * @param identificacion the identificacion to set
     */
    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    /**
     * @return the correo
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * @param correo the correo to set
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * @return the edoCivil
     */
    public String getEdoCivil() {
        return edoCivil;
    }

    /**
     * @param edoCivil the edoCivil to set
     */
    public void setEdoCivil(String edoCivil) {
        this.edoCivil = edoCivil;
    }

    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * @return the celular
     */
    public String getCelular() {
        return celular;
    }

    /**
     * @param celular the celular to set
     */
    public void setCelular(String celular) {
        this.celular = celular;
    }

    /**
     * @return the direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    /**
     * @return the fechaPrimerPago
     */
    public Date getFechaPrimerPago() {
        return fechaPrimerPago;
    }

    /**
     * @param fechaPrimerPago the fechaPrimerPago to set
     */
    public void setFechaPrimerPago(Date fechaPrimerPago) {
        this.fechaPrimerPago = fechaPrimerPago;
    }

    /**
     * @return the catorcenas
     */
    public Integer getCatorcenas() {
        return catorcenas;
    }

    /**
     * @param catorcenas the catorcenas to set
     */
    public void setCatorcenas(Integer catorcenas) {
        this.catorcenas = catorcenas;
    }

    /**
     * @return the puesto
     */
    public String getPuesto() {
        return puesto;
    }

    /**
     * @param puesto the puesto to set
     */
    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    /**
     * @return the fechaIngreso
     */
    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    /**
     * @param fechaIngreso the fechaIngreso to set
     */
    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    /**
     * @return the salarioNeto
     */
    public Double getSalarioNeto() {
        return salarioNeto;
    }

    /**
     * @param salarioNeto the salarioNeto to set
     */
    public void setSalarioNeto(Double salarioNeto) {
        this.salarioNeto = salarioNeto;
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
     * @return the rfcEmpresa
     */
    public String getRfcEmpresa() {
        return rfcEmpresa;
    }

    /**
     * @param rfcEmpresa the rfcEmpresa to set
     */
    public void setRfcEmpresa(String rfcEmpresa) {
        this.rfcEmpresa = rfcEmpresa;
    }

    /**
     * @return the telefonoEmpresa
     */
    public String getTelefonoEmpresa() {
        return telefonoEmpresa;
    }

    /**
     * @param telefonoEmpresa the telefonoEmpresa to set
     */
    public void setTelefonoEmpresa(String telefonoEmpresa) {
        this.telefonoEmpresa = telefonoEmpresa;
    }

    /**
     * @return the direccionEmpresa
     */
    public String getDireccionEmpresa() {
        return direccionEmpresa;
    }

    /**
     * @param direccionEmpresa the direccionEmpresa to set
     */
    public void setDireccionEmpresa(String direccionEmpresa) {
        this.direccionEmpresa = direccionEmpresa;
    }

    /**
     * @return the producto
     */
    public String getProducto() {
        return producto;
    }

    /**
     * @param producto the producto to set
     */
    public void setProducto(String producto) {
        this.producto = producto;
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
     * @return the fecha
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * @param fecha the fecha to set
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    /**
     * @return the empresaAbre
     */
    public String getEmpresaAbre() {
        return empresaAbre;
    }

    /**
     * @param empresaAbre the empresaAbre to set
     */
    public void setEmpresaAbre(String empresaAbre) {
        this.empresaAbre = empresaAbre;
    }

    /**
     * @return the productoAbre
     */
    public String getProductoAbre() {
        return productoAbre;
    }

    /**
     * @param productoAbre the productoAbre to set
     */
    public void setProductoAbre(String productoAbre) {
        this.productoAbre = productoAbre;
    }

    /**
     * @return the solicitudNumero
     */
    public Integer getSolicitudNumero() {
        return solicitudNumero;
    }

    /**
     * @param solicitudNumero the solicitudNumero to set
     */
    public void setSolicitudNumero(Integer solicitudNumero) {
        this.solicitudNumero = solicitudNumero;
    }

   
   

    /**
     * @return the lineaUno
     */
    public String getLineaUno() {
        return lineaUno;
    }

    /**
     * @param lineaUno the lineaUno to set
     */
    public void setLineaUno(String lineaUno) {
        this.lineaUno = lineaUno;
    }

    /**
     * @return the lineaDos
     */
    public String getLineaDos() {
        return lineaDos;
    }

    /**
     * @param lineaDos the lineaDos to set
     */
    public void setLineaDos(String lineaDos) {
        this.lineaDos = lineaDos;
    }

    /**
     * @return the lineaTres
     */
    public String getLineaTres() {
        return lineaTres;
    }

    /**
     * @param lineaTres the lineaTres to set
     */
    public void setLineaTres(String lineaTres) {
        this.lineaTres = lineaTres;
    }

    /**
     * @return the avalUno
     */
    public String getAvalUno() {
        return avalUno;
    }

    /**
     * @param avalUno the avalUno to set
     */
    public void setAvalUno(String avalUno) {
        this.avalUno = avalUno;
    }

    /**
     * @return the avalDos
     */
    public String getAvalDos() {
        return avalDos;
    }

    /**
     * @param avalDos the avalDos to set
     */
    public void setAvalDos(String avalDos) {
        this.avalDos = avalDos;
    }

    /**
     * @return the AvalTres
     */
    public String getAvalTres() {
        return AvalTres;
    }

    /**
     * @param AvalTres the AvalTres to set
     */
    public void setAvalTres(String AvalTres) {
        this.AvalTres = AvalTres;
    }

    /**
     * @return the imgEstadoCuenta
     */
    public String getImgEstadoCuenta() {
        return imgEstadoCuenta;
    }

    /**
     * @param imgEstadoCuenta the imgEstadoCuenta to set
     */
    public void setImgEstadoCuenta(String imgEstadoCuenta) {
        this.imgEstadoCuenta = imgEstadoCuenta;
    }

    /**
     * @return the idProducto
     */
    public Integer getIdProducto() {
        return idProducto;
    }

    /**
     * @param idProducto the idProducto to set
     */
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    /**
     * @return the correoOpt
     */
    public String getCorreoOpt() {
        return correoOpt;
    }

    /**
     * @param correoOpt the correoOpt to set
     */
    public void setCorreoOpt(String correoOpt) {
        this.correoOpt = correoOpt;
    }

    /**
     * @return the estatusSolicitud
     */
    public int getEstatusSolicitud() {
        return estatusSolicitud;
    }

    /**
     * @param estatusSolicitud the estatusSolicitud to set
     */
    public void setEstatusSolicitud(int estatusSolicitud) {
        this.estatusSolicitud = estatusSolicitud;
    }

    /**
     * @return the estatusSolStr
     */
    public String getEstatusSolStr() {
        return estatusSolStr;
    }

    /**
     * @param estatusSolStr the estatusSolStr to set
     */
    public void setEstatusSolStr(String estatusSolStr) {
        this.estatusSolStr = estatusSolStr;
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
     * @return the idEmpleado
     */
    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    /**
     * @param idEmpleado the idEmpleado to set
     */
    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    /**
     * @return the fechaCreacionSolicitud
     */
    public Date getFechaCreacionSolicitud() {
        return fechaCreacionSolicitud;
    }

    /**
     * @param fechaCreacionSolicitud the fechaCreacionSolicitud to set
     */
    public void setFechaCreacionSolicitud(Date fechaCreacionSolicitud) {
        this.fechaCreacionSolicitud = fechaCreacionSolicitud;
    }

    /**
     * @return the nombreEmpresa
     */
    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    /**
     * @param nombreEmpresa the nombreEmpresa to set
     */
    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    /**
     * @return the avalesSolicitud
     */
    public List<PendienteDto> getAvalesSolicitud() {
        return avalesSolicitud;
    }

    /**
     * @param avalesSolicitud the avalesSolicitud to set
     */
    public void setAvalesSolicitud(List<PendienteDto> avalesSolicitud) {
        this.avalesSolicitud = avalesSolicitud;
    }

    /**
     * @return the noSolicitud
     */
    public Integer getNoSolicitud() {
        return noSolicitud;
    }

    /**
     * @param noSolicitud the noSolicitud to set
     */
    public void setNoSolicitud(Integer noSolicitud) {
        this.noSolicitud = noSolicitud;
    }

    /**
     * @return the interesOrdAnual
     */
    public Integer getInteresOrdAnual() {
        return interesOrdAnual;
    }

    /**
     * @param interesOrdAnual the interesOrdAnual to set
     */
    public void setInteresOrdAnual(Integer interesOrdAnual) {
        this.interesOrdAnual = interesOrdAnual;
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

   
    
}