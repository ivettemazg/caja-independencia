/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dto.usuariocomun;

/**
 *
 * @author Venette
 */
public class DatosBancariosDto implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    
    private String banco;
    private String cuenta;
    private String clabe;
    private String nombreTarjetaHabiente;

    public DatosBancariosDto(String banco, String cuenta, String clabe, String nombreTarjetaHabiente) {
        this.banco = banco;
        this.cuenta = cuenta;
        this.clabe = clabe;
        this.nombreTarjetaHabiente = nombreTarjetaHabiente;
    }

    public DatosBancariosDto() {
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
     * @return the cuenta
     */
    public String getCuenta() {
        return cuenta;
    }

    /**
     * @param cuenta the cuenta to set
     */
    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    /**
     * @return the clabe
     */
    public String getClabe() {
        return clabe;
    }

    /**
     * @param clabe the clabe to set
     */
    public void setClabe(String clabe) {
        this.clabe = clabe;
    }

    /**
     * @return the nombreTarjetaHabiente
     */
    public String getNombreTarjetaHabiente() {
        return nombreTarjetaHabiente;
    }

    /**
     * @param nombreTarjetaHabiente the nombreTarjetaHabiente to set
     */
    public void setNombreTarjetaHabiente(String nombreTarjetaHabiente) {
        this.nombreTarjetaHabiente = nombreTarjetaHabiente;
    }
    
    
}
