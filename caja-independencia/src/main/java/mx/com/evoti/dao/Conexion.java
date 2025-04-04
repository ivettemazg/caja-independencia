/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Ivette Manzano
 */
public class Conexion {
 
     private Logger logger = LoggerFactory.getLogger(Conexion.class); 
    
     private java.sql.Connection  con = null;
     private final String url = "jdbc:mysql://";
     
     /**
      * BASE DE DATOS LAPTOP PRINCIPAL
      */
//     private final String serverName= "localhost";
//     private final String portNumber = "3306";
//     private final String databaseName= "cajaahorro";
//     private final String userName = "root";
//     private final String password = "admin";
//     
     /**
      * Base de datos en CAJAINDEPENDENCIA.COM
      */
//     private final String serverName= "localhost";
//     private final String portNumber = "3306";
//     private final String databaseName= "cajainde_bd";
//     private final String userName = "cajainde_root";
//     private final String password = "adm1n0143#";
////     
     
     private final String serverName= "evoti.com.mx";
     private final String portNumber = "3306";
     private final String databaseName= "evoti_Inmobiliaria";
//     private final String databaseName= "cajainde_resp";
     private final String userName = "evoti_root";
     private final String password = "uczP442#";
//     
//        private final String serverName= "216.224.176.117";
//     private final String portNumber = "3306";
//     private final String databaseName= "evoti_cajaahorro";
//     private final String userName = "evoti_root";
//     private final String password = "admin";
     // Informs the driver to use server a side-cursor, 
     // which permits more than one active statement 
     // on a connection.
 
     // Constructor
     public Conexion(){}
 
     private String getConnectionUrl(){
          return url + serverName + ":" + portNumber + "/" + databaseName +
           "?useSSL=true&requireSSL=true&verifyServerCertificate=true";
      }
 
     public java.sql.Connection getConnection(){
          try{
               Class.forName("com.mysql.cj.jdbc.Driver"); 
                                
               con = java.sql.DriverManager.getConnection(getConnectionUrl(),userName,password);
               if(con!=null) {
                  logger.info("Conexión exitosa!");
              }
          }catch(Exception e){
               
               System.out.println("Error Trace in getConnection() : " + e.getMessage());
         }
          return con;
      }
 
     /*
          Display the driver properties, database details 
     */ 
 
     public void displayDbProperties(){
          java.sql.DatabaseMetaData dm;
          java.sql.ResultSet rs;
          try{
               con= this.getConnection();
               if(con!=null){
                    dm = con.getMetaData();
                    System.out.println("Driver Information");
                    System.out.println("tDriver Name: "+ dm.getDriverName());
                    System.out.println("tDriver Version: "+ dm.getDriverVersion ());
                    System.out.println("nDatabase Information ");
                    System.out.println("tDatabase Name: "+ dm.getDatabaseProductName());
                    System.out.println("tDatabase Version: "+ dm.getDatabaseProductVersion());
                    System.out.println("Avalilable Catalogs ");
                    rs = dm.getCatalogs();
                    while(rs.next()){
                         System.out.println("tcatalog: "+ rs.getString(1));
                    } 
                    rs.close();
                    closeConnection();
               }else {
                  System.out.println("Error: No active Connection");
              }
          }catch(Exception e){
               e.printStackTrace();
          }
     }     
 
     public void closeConnection(){
          try{
               if(con!=null) {
                  con.close();
              }
               con=null;
          }catch(Exception e){
                e.printStackTrace();
          }
     }
    
}