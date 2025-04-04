/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.reporteRendto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.RendimientoReportDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.reportRendto.CantidadFechaDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class RendimientoReportBo implements Serializable {

    private static final long serialVersionUID = 516517538449528910L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RendimientoReportBo.class);
    
    private RendimientoReportDao dao;

    public RendimientoReportBo() {
        dao = new RendimientoReportDao();
    }
    
    public List<CantidadFechaDto> getAcumuladoXMes(Date fechaInicial, Date fechaFin) throws BusinessException{
          HibernateUtil.buildSessionFactory2();
        
        
        Date fechaAux = fechaInicial;
        List<CantidadFechaDto> cantidadesFecha = new ArrayList<>();
        /**
         * Mientras la fecha auxiliar no pase de la fecha final o sea igual a  la misma
         */
         while(fechaAux.before(fechaFin) || fechaAux.equals(fechaFin)){
            try {
                BigDecimal montoMes= dao.obtieneAcumuladoXMes(fechaAux);
                CantidadFechaDto dto =  new CantidadFechaDto(montoMes, fechaAux);
                cantidadesFecha.add(dto);
                fechaAux = Util.sumarORestarMesesFecha(fechaAux, 1);
            } catch (IntegracionException ex) {
               throw new BusinessException(ex.getMessage(), ex);
            }
            
         }
        return cantidadesFecha;
        
    }
    
    public static void main(String args[]){
        
        try {
            RendimientoReportBo bo = new RendimientoReportBo();
            
            List<CantidadFechaDto> acumuladoXMes = bo.getAcumuladoXMes(Util.generaFechaDeString("2011-01-01"), Util.generaFechaDeString("2017-01-31"));
            
            acumuladoXMes.stream().forEach((dto) -> {
                LOGGER.info(dto.toString());
            });
           
        } catch (BusinessException ex) {
          LOGGER.error(ex.getMessage(), ex);
        }
        
        
       
    }
            
    
}
