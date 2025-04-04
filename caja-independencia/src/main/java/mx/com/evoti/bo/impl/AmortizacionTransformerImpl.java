/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.impl;

import java.util.ArrayList;
import java.util.List;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.hibernate.pojos.Amortizacion;
import mx.com.evoti.hibernate.pojos.CreditosFinal;

/**
 *
 * @author Venette
 */
public interface AmortizacionTransformerImpl {

    /**
     * Metodo que se utiliza en el proceso de finiquitos Convierte una lista de
     * AdministracionDto a una lista de Administracion
     *
     * @param lstDto
     * @param credito
     * @return
     */
    default public List<Amortizacion> convertDtoToPojoAmortizacion(List<AmortizacionDto> lstDto, CreditosFinal credito, Integer estatusAmo) {
        List<Amortizacion> amortizacion = new ArrayList<>();
        for (AmortizacionDto dto : lstDto) {
            Amortizacion pojo = new Amortizacion();
            pojo.setAmoAmortizacion(dto.getAmoAmortizacion());
            pojo.setAmoCapital(dto.getAmoCapital());
            pojo.setAmoFechaPago(dto.getAmoFechaPagoCredito());
            pojo.setAmoClaveEmpleado(dto.getAmoClaveEmpleado());
            pojo.setAmoInteres(dto.getAmoInteres());
            pojo.setAmoIva(dto.getAmoIva());
            pojo.setAmoMontoPago(dto.getAmoMontoPago());
            pojo.setAmoNumeroPago(dto.getAmoNumeroPago());
            pojo.setAmoUsuId(dto.getAmoUsuId());
            pojo.setAmoEstatusInt(estatusAmo);
            pojo.setAmoProducto(dto.getAmoProducto());
            pojo.setAmoPagoId(dto.getAmoPagoId());
            pojo.setCreditosFinal(credito);

            amortizacion.add(pojo);
        }

        return amortizacion;
    }

    /**
     * Se utiliza en procesos de pagos a capital, extemporaneos, recorrer
     * catorcenas
     *
     * @param lstDto
     * @param credito
     * @return
     */
    default public List<Amortizacion> convertDtoToPojoAmortizacion(List<AmortizacionDto> lstDto, CreditosFinal credito) {
        List<Amortizacion> amortizacion = new ArrayList<>();
        lstDto.forEach(dto -> {
            Amortizacion pojo = new Amortizacion();
            pojo.setAmoAmortizacion(dto.getAmoAmortizacion());
            pojo.setAmoCapital(dto.getAmoCapital());
            pojo.setAmoFechaPago(dto.getAmoFechaPago());
            pojo.setAmoClaveEmpleado(dto.getAmoClaveEmpleado());
            pojo.setAmoInteres(dto.getAmoInteres());
            pojo.setAmoIva(dto.getAmoIva());
            pojo.setAmoMontoPago(dto.getAmoMontoPago());
            pojo.setAmoNumeroPago(dto.getAmoNumeroPago());
            pojo.setAmoProducto(dto.getAmoProducto());
            pojo.setAmoUsuId(dto.getAmoUsuId());
            pojo.setAmoEstatusInt(dto.getAmoEstatusInt());
            pojo.setAmoPagoId(dto.getAmoPagoId());
            pojo.setCreditosFinal(credito);

            amortizacion.add(pojo);
        });

        return amortizacion;
    }

}
