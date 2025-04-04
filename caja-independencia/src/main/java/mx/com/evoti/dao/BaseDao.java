/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Venus
 */
public class BaseDao {

      public static final Conexion CONEXION = new Conexion();
      
      public void cerrarConexion(Connection connection){
          if (connection != null) {
              try {
                  connection.close();
              } catch (SQLException ex) {
                 
              }
          }
      }
    
}
