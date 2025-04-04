/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.bancos;

import java.util.Date;
import mx.com.evoti.dto.bancos.BancosDto;
import mx.com.evoti.dto.bancos.EstadoCuentaDto;

/**
 *
 * @author Venette
 */
public class BancosEstadoCtaDto {
    
    private BancosDto bancoDto;
    private EstadoCuentaDto edoCtaDto;
    private Date becFechaTransaccion;

    /**
     * @return the bancoDto
     */
    public BancosDto getBancoDto() {
        return bancoDto;
    }

    /**
     * @param bancoDto the bancoDto to set
     */
    public void setBancoDto(BancosDto bancoDto) {
        this.bancoDto = bancoDto;
    }

    /**
     * @return the edoCtaDto
     */
    public EstadoCuentaDto getEdoCtaDto() {
        return edoCtaDto;
    }

    /**
     * @param edoCtaDto the edoCtaDto to set
     */
    public void setEdoCtaDto(EstadoCuentaDto edoCtaDto) {
        this.edoCtaDto = edoCtaDto;
    }

    /**
     * @return the becFechaTransaccion
     */
    public Date getBecFechaTransaccion() {
        return becFechaTransaccion;
    }

    /**
     * @param becFechaTransaccion the becFechaTransaccion to set
     */
    public void setBecFechaTransaccion(Date becFechaTransaccion) {
        this.becFechaTransaccion = becFechaTransaccion;
    }
}
