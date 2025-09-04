package mx.com.evoti.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de validar el formato de un archivo Excel cargado.
 */
public class FormatoArchivoValidacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean valido;
    private int filasVacias;
    private String mensajeUsuario;
    private List<String> errores = new ArrayList<>();

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public int getFilasVacias() {
        return filasVacias;
    }

    public void setFilasVacias(int filasVacias) {
        this.filasVacias = filasVacias;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }

    public void setMensajeUsuario(String mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void addError(String error) {
        this.errores.add(error);
    }
}

