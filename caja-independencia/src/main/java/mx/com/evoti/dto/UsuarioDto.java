/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author Ivette Manzano Galvan
 */
public class UsuarioDto implements Serializable{

    private static final long serialVersionUID = 9061945557426869437L;
    
    private Integer id;
    private String nombre;
    private String paterno;
    private String materno;
    private Integer cveEmpleado;
    private String correo;
    private String telefono;
    private String password;
    private String nickName;
    private Integer rolId;
    private String rol;
    private Integer idEmpresa;
    private Integer empresaId;
    private Integer estatus;
    private Integer habilitado;
    private String estatusStr;
    private String estatusAvalStr;
    private Boolean estatusAvalBol;
    private Integer estatusAvalInt;
    private Integer omitirValidaciones;
    
    
    
    private String nombreCompleto;
    private String rfc;
    private String extension;
    private String empresa;
    private String puesto;
    private String departamento;
    private String areaTrabajo;
    private String estacion;
    private String edoCivil;
    private String estado;
    private Date fechaIngresoCaja;
    private Date fechaIngresoEmpresa;
    private Date fechaNacimiento;
    private Date fechaBaja;
    private String strFechaIngresoCaja;
    private String strFechaIngresoEmpresa;
    private String strFechafechaNacimiento;
    private String sexo;
    private String identificacion;
    private String celular;
    private String municipio;
    private String cp;
    private String colonia;
    private String calle;
    private String numext;
    private String empresaAbreviacion;
    private Double salarioNeto;
    private String domicilioConcat;
    private Integer primeraVez;
    
    //Campos necesarios para la pantalla de avales en solicitud de credito
    private Integer idSolicitudAval;
    private BigInteger idSolicitud;
    private Boolean selectCheckbox;
    private Boolean editable;
    private Boolean nuevo;
    
    private String numint;
    private Integer creditosXEmpleado;
    
    private Double acumulado;
    private Double acumuladoCorregido;
    private Integer idAcumulado;
    private Boolean tblEditable;
    
    private String claveCredTransf;
    
    
   
    

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
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
     * @return the paterno
     */
    public String getPaterno() {
        return paterno;
    }

    /**
     * @param paterno the paterno to set
     */
    public void setPaterno(String paterno) {
        this.paterno = paterno;
    }

    /**
     * @return the materno
     */
    public String getMaterno() {
        return materno;
    }

    /**
     * @param materno the materno to set
     */
    public void setMaterno(String materno) {
        this.materno = materno;
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
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the nickName
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * @param nickName the nickName to set
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * @return the rolId
     */
    public Integer getRolId() {
        return rolId;
    }

    /**
     * @param rolId the rolId to set
     */
    public void setRolId(Integer rolId) {
        this.rolId = rolId;
    }

    /**
     * @return the rol
     */
    public String getRol() {
        return rol;
    }

    /**
     * @param rol the rol to set
     */
    public void setRol(String rol) {
        this.rol = rol;
    }

    /**
     * @return the nombreCompleto
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * @param nombreCompleto the nombreCompleto to set
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
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
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
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
     * @return the departamento
     */
    public String getDepartamento() {
        return departamento;
    }

    /**
     * @param departamento the departamento to set
     */
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    /**
     * @return the areaTrabajo
     */
    public String getAreaTrabajo() {
        return areaTrabajo;
    }

    /**
     * @param areaTrabajo the areaTrabajo to set
     */
    public void setAreaTrabajo(String areaTrabajo) {
        this.areaTrabajo = areaTrabajo;
    }

    /**
     * @return the estacion
     */
    public String getEstacion() {
        return estacion;
    }

    /**
     * @param estacion the estacion to set
     */
    public void setEstacion(String estacion) {
        this.estacion = estacion;
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
     * @return the fechaIngresoCaja
     */
    public Date getFechaIngresoCaja() {
        return fechaIngresoCaja;
    }

    /**
     * @param fechaIngresoCaja the fechaIngresoCaja to set
     */
    public void setFechaIngresoCaja(Date fechaIngresoCaja) {
        this.fechaIngresoCaja = fechaIngresoCaja;
    }

    /**
     * @return the fechaIngresoEmpresa
     */
    public Date getFechaIngresoEmpresa() {
        return fechaIngresoEmpresa;
    }

    /**
     * @param fechaIngresoEmpresa the fechaIngresoEmpresa to set
     */
    public void setFechaIngresoEmpresa(Date fechaIngresoEmpresa) {
        this.fechaIngresoEmpresa = fechaIngresoEmpresa;
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
     * @return the fechaBaja
     */
    public Date getFechaBaja() {
        return fechaBaja;
    }

    /**
     * @param fechaBaja the fechaBaja to set
     */
    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    /**
     * @return the strFechaIngresoCaja
     */
    public String getStrFechaIngresoCaja() {
        return strFechaIngresoCaja;
    }

    /**
     * @param strFechaIngresoCaja the strFechaIngresoCaja to set
     */
    public void setStrFechaIngresoCaja(String strFechaIngresoCaja) {
        this.strFechaIngresoCaja = strFechaIngresoCaja;
    }

    /**
     * @return the strFechaIngresoEmpresa
     */
    public String getStrFechaIngresoEmpresa() {
        return strFechaIngresoEmpresa;
    }

    /**
     * @param strFechaIngresoEmpresa the strFechaIngresoEmpresa to set
     */
    public void setStrFechaIngresoEmpresa(String strFechaIngresoEmpresa) {
        this.strFechaIngresoEmpresa = strFechaIngresoEmpresa;
    }

    /**
     * @return the strFechafechaNacimiento
     */
    public String getStrFechafechaNacimiento() {
        return strFechafechaNacimiento;
    }

    /**
     * @param strFechafechaNacimiento the strFechafechaNacimiento to set
     */
    public void setStrFechafechaNacimiento(String strFechafechaNacimiento) {
        this.strFechafechaNacimiento = strFechafechaNacimiento;
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
     * @return the domicilioConcat
     */
    public String getDomicilioConcat() {
        return domicilioConcat;
    }

    /**
     * @param domicilioConcat the domicilioConcat to set
     */
    public void setDomicilioConcat(String domicilioConcat) {
        this.domicilioConcat = domicilioConcat;
    }

    /**
     * @return the idEmpresa
     */
    public int getIdEmpresa() {
        return idEmpresa;
    }

    /**
     * @param idEmpresa the idEmpresa to set
     */
    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    /**
     * @return the primeraVez
     */
    public Integer getPrimeraVez() {
        return primeraVez;
    }

    /**
     * @param primeraVez the primeraVez to set
     */
    public void setPrimeraVez(Integer primeraVez) {
        this.primeraVez = primeraVez;
    }

    /**
     * @return the selectCheckbox
     */
    public Boolean getSelectCheckbox() {
        return selectCheckbox;
    }

    /**
     * @param selectCheckbox the selectCheckbox to set
     */
    public void setSelectCheckbox(Boolean selectCheckbox) {
        this.selectCheckbox = selectCheckbox;
    }

    /**
     * @return the editable
     */
    public Boolean getEditable() {
        return editable;
    }

    /**
     * @param editable the editable to set
     */
    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    /**
     * @return the nuevo
     */
    public Boolean getNuevo() {
        return nuevo;
    }

    /**
     * @param nuevo the nuevo to set
     */
    public void setNuevo(Boolean nuevo) {
        this.nuevo = nuevo;
    }

    /**
     * @return the numint
     */
    public String getNumint() {
        return numint;
    }

    /**
     * @param numint the numint to set
     */
    public void setNumint(String numint) {
        this.numint = numint;
    }

    /**
     * @return the creditosXEmpleado
     */
    public Integer getCreditosXEmpleado() {
        return creditosXEmpleado;
    }

    /**
     * @param creditosXEmpleado the creditosXEmpleado to set
     */
    public void setCreditosXEmpleado(Integer creditosXEmpleado) {
        this.creditosXEmpleado = creditosXEmpleado;
    }

    /**
     * @return the acumulado
     */
    public Double getAcumulado() {
        return acumulado;
    }

    /**
     * @param acumulado the acumulado to set
     */
    public void setAcumulado(Double acumulado) {
        this.acumulado = acumulado;
    }

    /**
     * @return the acumuladoCorregido
     */
    public Double getAcumuladoCorregido() {
        return acumuladoCorregido;
    }

    /**
     * @param acumuladoCorregido the acumuladoCorregido to set
     */
    public void setAcumuladoCorregido(Double acumuladoCorregido) {
        this.acumuladoCorregido = acumuladoCorregido;
    }

    /**
     * @return the idAcumulado
     */
    public Integer getIdAcumulado() {
        return idAcumulado;
    }

    /**
     * @param idAcumulado the idAcumulado to set
     */
    public void setIdAcumulado(Integer idAcumulado) {
        this.idAcumulado = idAcumulado;
    }

    /**
     * @return the cveEmpleado
     */
    public Integer getCveEmpleado() {
        return cveEmpleado;
    }

    /**
     * @param cveEmpleado the cveEmpleado to set
     */
    public void setCveEmpleado(Integer cveEmpleado) {
        this.cveEmpleado = cveEmpleado;
    }

    /**
     * @return the idSolicitudAval
     */
    public Integer getIdSolicitudAval() {
        return idSolicitudAval;
    }

    /**
     * @param idSolicitudAval the idSolicitudAval to set
     */
    public void setIdSolicitudAval(Integer idSolicitudAval) {
        this.idSolicitudAval = idSolicitudAval;
    }

    /**
     * @return the tblEditable
     */
    public Boolean getTblEditable() {
        return tblEditable;
    }

    /**
     * @param tblEditable the tblEditable to set
     */
    public void setTblEditable(Boolean tblEditable) {
        this.tblEditable = tblEditable;
    }

    /**
     * @return the empresaId
     */
    public Integer getEmpresaId() {
        return empresaId;
    }

    /**
     * @param empresaId the empresaId to set
     */
    public void setEmpresaId(Integer empresaId) {
        this.empresaId = empresaId;
    }

    /**
     * @return the estatus
     */
    public Integer getEstatus() {
        return estatus;
    }

    /**
     * @param estatus the estatus to set
     */
    public void setEstatus(Integer estatus) {
        this.estatus = estatus;
    }

    /**
     * @return the estatusStr
     */
    public String getEstatusStr() {
        return estatusStr;
    }

    /**
     * @param estatusStr the estatusStr to set
     */
    public void setEstatusStr(String estatusStr) {
        this.estatusStr = estatusStr;
    }

    /**
     * @return the idSolicitud
     */
    public BigInteger getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * @param idSolicitud the idSolicitud to set
     */
    public void setIdSolicitud(BigInteger idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /**
     * @return the estatusAvalStr
     */
    public String getEstatusAvalStr() {
        return estatusAvalStr;
    }

    /**
     * @param estatusAvalStr the estatusAvalStr to set
     */
    public void setEstatusAvalStr(String estatusAvalStr) {
        this.estatusAvalStr = estatusAvalStr;
    }

    /**
     * @return the estatusAvalBol
     */
    public Boolean getEstatusAvalBol() {
        return estatusAvalBol;
    }

    /**
     * @param estatusAvalBol the estatusAvalBol to set
     */
    public void setEstatusAvalBol(Boolean estatusAvalBol) {
        this.estatusAvalBol = estatusAvalBol;
    }

    /**
     * @return the estatusAvalInt
     */
    public Integer getEstatusAvalInt() {
        return estatusAvalInt;
    }

    /**
     * @param estatusAvalInt the estatusAvalInt to set
     */
    public void setEstatusAvalInt(Integer estatusAvalInt) {
        this.estatusAvalInt = estatusAvalInt;
    }

    /**
     * @return the claveCredTransf
     */
    public String getClaveCredTransf() {
        return claveCredTransf;
    }

    /**
     * @param claveCredTransf the claveCredTransf to set
     */
    public void setClaveCredTransf(String claveCredTransf) {
        this.claveCredTransf = claveCredTransf;
    }

    /**
     * @return the habilitado
     */
    public Integer getHabilitado() {
        return habilitado;
    }

    /**
     * @param habilitado the habilitado to set
     */
    public void setHabilitado(Integer habilitado) {
        this.habilitado = habilitado;
    }

    /**
     * @return the omitirValidaciones
     */
    public Integer getOmitirValidaciones() {
        return omitirValidaciones;
    }

    /**
     * @param omitirValidaciones the omitirValidaciones to set
     */
    public void setOmitirValidaciones(Integer omitirValidaciones) {
        this.omitirValidaciones = omitirValidaciones;
    }
    
}
