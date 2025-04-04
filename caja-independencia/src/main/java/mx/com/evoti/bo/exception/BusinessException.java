/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.exception;

import mx.com.evoti.dao.exception.IntegracionException;

/**
 *
 * @author Ivette Manzano
 */
public class BusinessException extends Exception {
      private String msg = "";
    private String causa = "";
    private Throwable throwable = null;
    private static final long serialVersionUID = 2549559953439294771L;




    /**
     * Creates a new instance of <code>BusinessException</code> without detail message.
     */
    public BusinessException() {
    }

    /**
     * Constructs an instance of <code>BusinessException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BusinessException(String msg) {
        super(msg);
    }

    /**
     * @param msg Es el detalle del mensaje
     * @param causa Es el trace del error
     */
    public BusinessException(String msg, Throwable throwable) {
        super(msg, throwable);
    }




    public BusinessException(String msg, String causa, Throwable throwable) {
        super(msg, throwable);
        this.msg = msg;
        this.causa = causa;
        this.throwable = throwable;
    }
    
    public BusinessException(String msg, String causa) {
        super(msg);
        this.msg = msg;
        this.causa = causa;
        
    }

    public BusinessException(IntegracionException ex, IntegracionException ex0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
