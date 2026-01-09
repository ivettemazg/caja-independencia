/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.hibernate.config;

import java.io.Serializable;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.PropertyConfigurator;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.time.Instant;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
@WebListener("application context listener")
public class ContextListener implements ServletContextListener, Serializable {  

    private static final long serialVersionUID = -51821345001739078L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);
  
   /*  @Override
    public void contextInitialized(ServletContextEvent event) {  
          // Inicializando Log4J
        ServletContext context = event.getServletContext();
        String log4jConfigFile = context.getInitParameter("log4j-config-location");
        String fullPath = context.getRealPath("") + File.separator + log4jConfigFile;
         
        PropertyConfigurator.configure(fullPath);
        
        //Inicializando Hibernate
         HibernateUtil.buildSessionFactory();
        
         
    }*/  

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        LOGGER.info("Iniciando contexto '{}' en {} (hilo={})",
                context.getContextPath(),
                Instant.now(),
                Thread.currentThread().getName());

        // Forzar carga del driver MySQL para evitar "No suitable driver"
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.debug("Driver MySQL cargado exitosamente");
        } catch (ClassNotFoundException e) {
            LOGGER.error("No se pudo cargar el driver MySQL", e);
        }
    
        // Inicializando Log4J

        PropertyConfigurator.configure(
        Thread.currentThread().getContextClassLoader().getResource("log4j.properties")
        );

    
        // Inicializando Hibernate
        HibernateUtil.buildSessionFactory();
        LOGGER.info("Contexto '{}' inicializado correctamente en {}", context.getContextPath(), Instant.now());
    }

  
    @Override
    public void contextDestroyed(ServletContextEvent event) {  
        ServletContext context = event.getServletContext();
        LOGGER.warn("Destruyendo contexto '{}' en {} (hilo={})",
                context.getContextPath(),
                Instant.now(),
                Thread.currentThread().getName());
        AbandonedConnectionCleanupThread.checkedShutdown();
        HibernateUtil.closeSessionFactory();
        LOGGER.warn("Contexto '{}' destruido en {}", context.getContextPath(), Instant.now());
    }  
    
}
