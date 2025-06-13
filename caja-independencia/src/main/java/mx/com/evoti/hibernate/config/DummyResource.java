package mx.com.evoti.hibernate.config;

import javax.faces.application.Resource;
import javax.faces.context.FacesContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Recurso vacío que evita NPE cuando el archivo real no existe.
 * Devuelve 0 bytes y cabeceras mínimas.
 */
public class DummyResource extends Resource {

      private final String name;
    private final String library;

    public DummyResource(String name, String library) {
        this.name    = name  != null ? name    : "unknown";
        this.library = library != null ? library : "";
        // Tipo por defecto: text/plain (suficiente para CSS/JS inexistentes)
        setContentType("text/plain");
    }

    /* ---------- Métodos abstractos de Resource ---------- */

    @Override
    public String getRequestPath() {
        // Ruta “ficticia”; no se usará porque el contenido se envía vía InputStream
        return "/dummy/" + library + '/' + name;
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        return Collections.emptyMap();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // Contenido vacío ⇒ 0 bytes
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context) {
        // Nunca obliga a refrescar
        return false;
    }

     @Override
    public URL getURL() {
        // PrimeFaces/JSF no llamarán a este método en peticiones normales,
        // así que devolver null es suficiente.
        return null;
    }
}