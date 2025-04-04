/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.exception;

/**
 *
 * @author Ivette Manzano
 */
public class IntegracionException extends Exception{

    private static final long serialVersionUID = -492367529536137032L;

    /**
     * Creates a new instance of
     * <code>DataBaseException</code> without detail message.
     */
    public IntegracionException() {
        super();
    }

    /**
     * Constructs an instance of
     * <code>DataBaseException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public IntegracionException(String msg) {
        super(msg);
        this.msg = msg;
    }
    private String msg = "";
    private String causa = "";
    private Throwable throwable = null;
    private String hql = "";

    /**
     * @param throwable Es el trace del error
     */
    public IntegracionException(Throwable throwable) {
        super(throwable);
        this.throwable = throwable;
    }

    /**
     * @param msg Es el detalle del mensaje
     * @param causa Es el trace del error
     */
    public IntegracionException(String msg, String causa) {
        super(msg, new Throwable(causa));
        this.msg = msg;
        this.causa = causa;
    }

    /**
     * @param msg Es el detalle del mensaje
     * @param Throwable Es el trace del error
     */
    public IntegracionException(String msg, Throwable throwable) {
        super(msg, throwable);
        this.msg = msg;
        this.throwable = throwable;
    }

    public IntegracionException(String msg, String causa, Throwable throwable) {
        super(msg, throwable);
        this.msg = msg;
        this.causa = causa;
        this.throwable = throwable;
    }

   
}