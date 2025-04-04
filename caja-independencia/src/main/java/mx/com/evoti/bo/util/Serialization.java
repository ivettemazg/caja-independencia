/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Venus
 */
public class Serialization {
    
    
    /***
     *  lo que se hace aquí es serializar ( convertir a una representación binaria portable ) 
     * los objetos a memoria hacia un arreglo de bytes, 
     * y después deserializar ( obtener objetos java a partir de la representación binaria portable ) 
     * desde dicho arreglo de bytes.

        El manejo del arreglo de bytes es parte de la razón por la que este método puede ser inconveniente en jerarquías de objetos muy grandes; 
        * sin embargo, dichos escenarios no son sencillos de resolver en ningún caso.
     * @param <T>
     * @param source
     * @return 
     */
    @SuppressWarnings( "unchecked" )
  public static <T> T copy( T source )
  {
    try
    {
      ByteArrayOutputStream ostream = new ByteArrayOutputStream();
      ObjectOutputStream oostream = new ObjectOutputStream( ostream );
   
      oostream.writeObject( source );
      oostream.flush();
   
      byte[] bytes = ostream.toByteArray();
       
      InputStream istream = new ByteArrayInputStream( bytes );
      ObjectInputStream oistream = new ObjectInputStream( istream );
       
      return ( T )oistream.readObject();
    }
    catch( RuntimeException e )
      { throw e; }
    catch( Exception e )
      { throw new IllegalArgumentException( source.getClass().getName(), e ); }
  }
}
