/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.hibernate.config;

import java.io.File;
import java.io.Serializable;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Venus
 */
@WebListener("application context listener")
public class ContextListener implements ServletContextListener, Serializable {  

    private static final long serialVersionUID = -51821345001739078L;
  
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
        // Forzar carga del driver MySQL para evitar "No suitable driver"
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL cargado exitosamente");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    
        // Inicializando Log4J
        ServletContext context = event.getServletContext();
        String log4jConfigFile = context.getInitParameter("log4j-config-location");
        String fullPath = context.getRealPath("") + File.separator + log4jConfigFile;
    
        PropertyConfigurator.configure(fullPath);
    
        // Inicializando Hibernate
        HibernateUtil.buildSessionFactory();
    }

  
    @Override
    public void contextDestroyed(ServletContextEvent event) {  
       HibernateUtil.closeSessionFactory();
    }  
    
}
