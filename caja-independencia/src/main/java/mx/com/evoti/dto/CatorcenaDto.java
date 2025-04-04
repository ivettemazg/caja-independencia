/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto;

import java.util.Date;

/**
 *
 * @author Venette
 */
public class CatorcenaDto {
    
     private int carId;
     private Date carFecha;
     private Integer carDia;
     private Integer carMes;
     private Integer carAnio;
     private Integer carInversiones;

    /**
     * @return the carId
     */
    public int getCarId() {
        return carId;
    }

    /**
     * @param carId the carId to set
     */
    public void setCarId(int carId) {
        this.carId = carId;
    }

    /**
     * @return the carFecha
     */
    public Date getCarFecha() {
        return carFecha;
    }

    /**
     * @param carFecha the carFecha to set
     */
    public void setCarFecha(Date carFecha) {
        this.carFecha = carFecha;
    }

    /**
     * @return the carDia
     */
    public Integer getCarDia() {
        return carDia;
    }

    /**
     * @param carDia the carDia to set
     */
    public void setCarDia(Integer carDia) {
        this.carDia = carDia;
    }

    /**
     * @return the carMes
     */
    public Integer getCarMes() {
        return carMes;
    }

    /**
     * @param carMes the carMes to set
     */
    public void setCarMes(Integer carMes) {
        this.carMes = carMes;
    }

    /**
     * @return the carAnio
     */
    public Integer getCarAnio() {
        return carAnio;
    }

    /**
     * @param carAnio the carAnio to set
     */
    public void setCarAnio(Integer carAnio) {
        this.carAnio = carAnio;
    }

    /**
     * @return the carInversiones
     */
    public Integer getCarInversiones() {
        return carInversiones;
    }

    /**
     * @param carInversiones the carInversiones to set
     */
    public void setCarInversiones(Integer carInversiones) {
        this.carInversiones = carInversiones;
    }
    
}
