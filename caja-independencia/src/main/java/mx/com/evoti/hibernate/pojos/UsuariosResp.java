package mx.com.evoti.hibernate.pojos;
// Generated 21/10/2016 12:57:00 PM by Hibernate Tools 4.3.1


import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * UsuariosResp generated by hbm2java
 */
@Entity
@Table(name="usuarios_resp"
)
public class UsuariosResp  implements java.io.Serializable {


     private Integer usuId;
     private Integer usuClaveEmpleado;
     private String usuNombre;
     private String usuPaterno;
     private String usuEdoCivil;
     private String usuCorreo;
     private String usuEstado;
     private String usuRfc;
     private String usuEmpresa;
     private String usuPuesto;
     private String usuTelefono;
     private String usuDepartamento;
     private String usuAreaTrabajo;
     private String usuEstacion;
     private Date usuFechaIngreso;
     private Date usuFechaNacimiento;
     private String usuSexo;
     private String usuIdentificacion;
     private String usuCelular;
     private String usuMunicipio;
     private String usuCp;
     private String usuColonia;
     private String usuCalle;
     private String usuNumext;
     private Double usuSalarioNeto;
     private String usuMaterno;
     private String usuPassword;
     private Integer usuPrimeravez;
     private String usuTemporal;
     private Date usuFechaIngresoEmpresa;

    public UsuariosResp() {
    }

    public UsuariosResp(Integer usuClaveEmpleado, String usuNombre, String usuPaterno, String usuEdoCivil, String usuCorreo, String usuEstado, String usuRfc, String usuEmpresa, String usuPuesto, String usuTelefono, String usuDepartamento, String usuAreaTrabajo, String usuEstacion, Date usuFechaIngreso, Date usuFechaNacimiento, String usuSexo, String usuIdentificacion, String usuCelular, String usuMunicipio, String usuCp, String usuColonia, String usuCalle, String usuNumext, Double usuSalarioNeto, String usuMaterno, String usuPassword, Integer usuPrimeravez, String usuTemporal, Date usuFechaIngresoEmpresa) {
       this.usuClaveEmpleado = usuClaveEmpleado;
       this.usuNombre = usuNombre;
       this.usuPaterno = usuPaterno;
       this.usuEdoCivil = usuEdoCivil;
       this.usuCorreo = usuCorreo;
       this.usuEstado = usuEstado;
       this.usuRfc = usuRfc;
       this.usuEmpresa = usuEmpresa;
       this.usuPuesto = usuPuesto;
       this.usuTelefono = usuTelefono;
       this.usuDepartamento = usuDepartamento;
       this.usuAreaTrabajo = usuAreaTrabajo;
       this.usuEstacion = usuEstacion;
       this.usuFechaIngreso = usuFechaIngreso;
       this.usuFechaNacimiento = usuFechaNacimiento;
       this.usuSexo = usuSexo;
       this.usuIdentificacion = usuIdentificacion;
       this.usuCelular = usuCelular;
       this.usuMunicipio = usuMunicipio;
       this.usuCp = usuCp;
       this.usuColonia = usuColonia;
       this.usuCalle = usuCalle;
       this.usuNumext = usuNumext;
       this.usuSalarioNeto = usuSalarioNeto;
       this.usuMaterno = usuMaterno;
       this.usuPassword = usuPassword;
       this.usuPrimeravez = usuPrimeravez;
       this.usuTemporal = usuTemporal;
       this.usuFechaIngresoEmpresa = usuFechaIngresoEmpresa;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)

    
    @Column(name="usu_id", nullable=false)
    public Integer getUsuId() {
        return this.usuId;
    }
    
    public void setUsuId(Integer usuId) {
        this.usuId = usuId;
    }

    
    @Column(name="usu_clave_empleado")
    public Integer getUsuClaveEmpleado() {
        return this.usuClaveEmpleado;
    }
    
    public void setUsuClaveEmpleado(Integer usuClaveEmpleado) {
        this.usuClaveEmpleado = usuClaveEmpleado;
    }

    
    @Column(name="usu_nombre", length=50)
    public String getUsuNombre() {
        return this.usuNombre;
    }
    
    public void setUsuNombre(String usuNombre) {
        this.usuNombre = usuNombre;
    }

    
    @Column(name="usu_paterno", length=50)
    public String getUsuPaterno() {
        return this.usuPaterno;
    }
    
    public void setUsuPaterno(String usuPaterno) {
        this.usuPaterno = usuPaterno;
    }

    
    @Column(name="usu_edo_civil", length=20)
    public String getUsuEdoCivil() {
        return this.usuEdoCivil;
    }
    
    public void setUsuEdoCivil(String usuEdoCivil) {
        this.usuEdoCivil = usuEdoCivil;
    }

    
    @Column(name="usu_correo", length=50)
    public String getUsuCorreo() {
        return this.usuCorreo;
    }
    
    public void setUsuCorreo(String usuCorreo) {
        this.usuCorreo = usuCorreo;
    }

    
    @Column(name="usu_estado", length=2)
    public String getUsuEstado() {
        return this.usuEstado;
    }
    
    public void setUsuEstado(String usuEstado) {
        this.usuEstado = usuEstado;
    }

    
    @Column(name="usu_rfc", length=15)
    public String getUsuRfc() {
        return this.usuRfc;
    }
    
    public void setUsuRfc(String usuRfc) {
        this.usuRfc = usuRfc;
    }

    
    @Column(name="usu_empresa", length=1)
    public String getUsuEmpresa() {
        return this.usuEmpresa;
    }
    
    public void setUsuEmpresa(String usuEmpresa) {
        this.usuEmpresa = usuEmpresa;
    }

    
    @Column(name="usu_puesto", length=50)
    public String getUsuPuesto() {
        return this.usuPuesto;
    }
    
    public void setUsuPuesto(String usuPuesto) {
        this.usuPuesto = usuPuesto;
    }

    
    @Column(name="usu_telefono", length=20)
    public String getUsuTelefono() {
        return this.usuTelefono;
    }
    
    public void setUsuTelefono(String usuTelefono) {
        this.usuTelefono = usuTelefono;
    }

    
    @Column(name="usu_departamento", length=50)
    public String getUsuDepartamento() {
        return this.usuDepartamento;
    }
    
    public void setUsuDepartamento(String usuDepartamento) {
        this.usuDepartamento = usuDepartamento;
    }

    
    @Column(name="usu_area_trabajo", length=50)
    public String getUsuAreaTrabajo() {
        return this.usuAreaTrabajo;
    }
    
    public void setUsuAreaTrabajo(String usuAreaTrabajo) {
        this.usuAreaTrabajo = usuAreaTrabajo;
    }

    
    @Column(name="usu_estacion", length=50)
    public String getUsuEstacion() {
        return this.usuEstacion;
    }
    
    public void setUsuEstacion(String usuEstacion) {
        this.usuEstacion = usuEstacion;
    }

    @Temporal(TemporalType.DATE)
    @Column(name="usu_fecha_ingreso", length=10)
    public Date getUsuFechaIngreso() {
        return this.usuFechaIngreso;
    }
    
    public void setUsuFechaIngreso(Date usuFechaIngreso) {
        this.usuFechaIngreso = usuFechaIngreso;
    }

    @Temporal(TemporalType.DATE)
    @Column(name="usu_fecha_nacimiento", length=10)
    public Date getUsuFechaNacimiento() {
        return this.usuFechaNacimiento;
    }
    
    public void setUsuFechaNacimiento(Date usuFechaNacimiento) {
        this.usuFechaNacimiento = usuFechaNacimiento;
    }

    
    @Column(name="usu_sexo", length=1)
    public String getUsuSexo() {
        return this.usuSexo;
    }
    
    public void setUsuSexo(String usuSexo) {
        this.usuSexo = usuSexo;
    }

    
    @Column(name="usu_identificacion", length=30)
    public String getUsuIdentificacion() {
        return this.usuIdentificacion;
    }
    
    public void setUsuIdentificacion(String usuIdentificacion) {
        this.usuIdentificacion = usuIdentificacion;
    }

    
    @Column(name="usu_celular", length=20)
    public String getUsuCelular() {
        return this.usuCelular;
    }
    
    public void setUsuCelular(String usuCelular) {
        this.usuCelular = usuCelular;
    }

    
    @Column(name="usu_municipio", length=50)
    public String getUsuMunicipio() {
        return this.usuMunicipio;
    }
    
    public void setUsuMunicipio(String usuMunicipio) {
        this.usuMunicipio = usuMunicipio;
    }

    
    @Column(name="usu_cp", length=5)
    public String getUsuCp() {
        return this.usuCp;
    }
    
    public void setUsuCp(String usuCp) {
        this.usuCp = usuCp;
    }

    
    @Column(name="usu_colonia", length=50)
    public String getUsuColonia() {
        return this.usuColonia;
    }
    
    public void setUsuColonia(String usuColonia) {
        this.usuColonia = usuColonia;
    }

    
    @Column(name="usu_calle", length=50)
    public String getUsuCalle() {
        return this.usuCalle;
    }
    
    public void setUsuCalle(String usuCalle) {
        this.usuCalle = usuCalle;
    }

    
    @Column(name="usu_numext", length=10)
    public String getUsuNumext() {
        return this.usuNumext;
    }
    
    public void setUsuNumext(String usuNumext) {
        this.usuNumext = usuNumext;
    }

    
    @Column(name="usu_salario_neto", precision=22, scale=0)
    public Double getUsuSalarioNeto() {
        return this.usuSalarioNeto;
    }
    
    public void setUsuSalarioNeto(Double usuSalarioNeto) {
        this.usuSalarioNeto = usuSalarioNeto;
    }

    
    @Column(name="usu_materno", length=50)
    public String getUsuMaterno() {
        return this.usuMaterno;
    }
    
    public void setUsuMaterno(String usuMaterno) {
        this.usuMaterno = usuMaterno;
    }

    
    @Column(name="usu_password", length=200)
    public String getUsuPassword() {
        return this.usuPassword;
    }
    
    public void setUsuPassword(String usuPassword) {
        this.usuPassword = usuPassword;
    }

    
    @Column(name="usu_primeravez")
    public Integer getUsuPrimeravez() {
        return this.usuPrimeravez;
    }
    
    public void setUsuPrimeravez(Integer usuPrimeravez) {
        this.usuPrimeravez = usuPrimeravez;
    }

    
    @Column(name="usu_temporal", length=50)
    public String getUsuTemporal() {
        return this.usuTemporal;
    }
    
    public void setUsuTemporal(String usuTemporal) {
        this.usuTemporal = usuTemporal;
    }

    @Temporal(TemporalType.DATE)
    @Column(name="usu_fecha_ingreso_empresa", length=10)
    public Date getUsuFechaIngresoEmpresa() {
        return this.usuFechaIngresoEmpresa;
    }
    
    public void setUsuFechaIngresoEmpresa(Date usuFechaIngresoEmpresa) {
        this.usuFechaIngresoEmpresa = usuFechaIngresoEmpresa;
    }




}


