/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import mx.com.evoti.dto.EmpresasDto;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@FacesConverter("empConverter")
public class EmpresaConverter implements Converter{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EmpresaConverter.class);
    

    @Override
    public EmpresasDto getAsObject(FacesContext fc, UIComponent uic, String string) {

        
          String [] propiedadesDto = string.split(",");
           EmpresasDto estado;
        try{
        estado = new EmpresasDto(Integer.parseInt(propiedadesDto[0]),propiedadesDto[1]);
        }catch(NumberFormatException nfe){
            LOGGER.error(nfe.getMessage(), "No se pudo convertir el valor");
            return null;
        }
        return estado;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
         if(o != null && !o.toString().isEmpty()) {
            return ((EmpresasDto) o).getEmpId()+","+((EmpresasDto) o).getEmpAbreviacion();
        }
        else {
            return null;
        }
        
    }
}
