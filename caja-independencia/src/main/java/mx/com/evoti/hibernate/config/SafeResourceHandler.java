package mx.com.evoti.hibernate.config;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SafeResourceHandler extends ResourceHandlerWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SafeResourceHandler.class);
    private final ResourceHandler wrapped;

    public SafeResourceHandler(ResourceHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Resource createResource(String name, String library) {
        Resource res = wrapped.createResource(name, library);
        if (res == null) {
            LOGGER.warn("Recurso no encontrado: {}/{}", library, name);
            res = new DummyResource(name, library);
        }
        return res;
    }

    @Override
    public ResourceHandler getWrapped() {
        return wrapped;
    }
}
