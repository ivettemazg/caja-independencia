/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.hibernate.config;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;


/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author Venus
 */
public class HibernateUtil implements Serializable{
    private static final Logger logger = Logger.getLogger(HibernateUtil.class); 
    
    private static SessionFactory sessionFactory;
    private static Configuration configuration;
    private static ServiceRegistry serviceRegistry;
    private static final long serialVersionUID = 5979809436895022658L;

    /**
     * Inicializa la Configuracion y el ServiceRegistry, este método se manda llamar
     * desde la clase principal del proyecto para que solo se carguen una vez las configuraciones
     * del archivo hibernate.cfg.xml
     */
    public static void buildSessionFactory() {
        try {
           // Create the SessionFactory from hibernate.cfg.xml
            configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            System.out.println("Hibernate Configuration loaded");
             
            //apply configuration property settings to StandardServiceRegistryBuilder
            serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            System.out.println("Hibernate serviceRegistry created");
             
            sessionFactory = configuration
                                    .buildSessionFactory(serviceRegistry);
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
       return sessionFactory;
    }

    /*
    public static void closeSessionFactory() {

        if (!HibernateUtil.sessionFactory.isClosed()) {
           logger.info("Se cierra el pool de conexiones ");
           HibernateUtil.sessionFactory.close();
           HibernateUtil.sessionFactory = null;
          }
    }*/

    public static void closeSessionFactory() {
        if (sessionFactory != null) {
            try {
                if (!sessionFactory.isClosed()) {
                    logger.info("Cerrando el pool de conexiones...");
                    sessionFactory.close();
                    sessionFactory = null;
                }
            } catch (Exception e) {
                logger.error("Error al cerrar la SessionFactory", e);
            }
        }
    }
 
    
    
    public static void buildSessionFactory2() {
        try {
           // Create the SessionFactory from hibernate.cfg.xml
            configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            System.out.println("Hibernate Configuration loaded");
             
            //apply configuration property settings to StandardServiceRegistryBuilder
            serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            System.out.println("Hibernate serviceRegistry created");
             
            sessionFactory = configuration
                                    .buildSessionFactory(serviceRegistry);
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

   
    
}
