package mx.com.evoti.dto;
// Generated 21/10/2016 12:57:00 PM by Hibernate Tools 4.3.1


import java.util.Objects;


public class EmpresasDto  implements java.io.Serializable {

    private static final long serialVersionUID = -5388549277820876434L;


     private Integer empId;
     private String empAbreviacion;
     private String empDescripcion;


    public EmpresasDto() {
    }
    
     public EmpresasDto(Integer empId, String empAbreviacion) {
        this.empId = empId;
        this.empAbreviacion = empAbreviacion;
    }
     public EmpresasDto(Integer empId, String empAbreviacion, String empDescripcion) {
        this.empId = empId;
        this.empAbreviacion = empAbreviacion;
        this.empDescripcion = empDescripcion;
    }


    /**
     * @return the empId
     */
    public Integer getEmpId() {
        return empId;
    }

    /**
     * @param empId the empId to set
     */
    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    /**
     * @return the empAbreviacion
     */
    public String getEmpAbreviacion() {
        return empAbreviacion;
    }

    /**
     * @param empAbreviacion the empAbreviacion to set
     */
    public void setEmpAbreviacion(String empAbreviacion) {
        this.empAbreviacion = empAbreviacion;
    }

    /**
     * @return the empDescripcion
     */
    public String getEmpDescripcion() {
        return empDescripcion;
    }

    /**
     * @param empDescripcion the empDescripcion to set
     */
    public void setEmpDescripcion(String empDescripcion) {
        this.empDescripcion = empDescripcion;
    }

    @Override
    public int hashCode() {
        int hash = 0;
	hash += (empId!= null ? empId.hashCode() : 0);
	return hash;
	}

@Override
    public boolean equals(Object object) {
        if (!(object instanceof EmpresasDto)) {
            return false;
        }
        EmpresasDto other = (EmpresasDto) object;
        if ((this.empId == null && other.empId != null) || (this.empId != null && !this.empId.equals(other.empId))) {
            return false;
        }
        return true;
    }
	
    
}