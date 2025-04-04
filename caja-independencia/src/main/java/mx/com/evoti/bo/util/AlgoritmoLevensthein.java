/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Ivette Manzano
 */
public class AlgoritmoLevensthein {
    
    private static final Logger logger = Logger.getLogger(AlgoritmoLevensthein.class);
    
    private static int minimo(int a, int b, int c) {
        if(a<=b && a<=c) {
            return a;
        }
        if(b<=a && b<=c) {
            return b;
        }
        return c;
    }

    public static int calculaDistanciaLevenshtein(String str1, String str2) {
        return calculaDistanciaLevenshtein(str1.toCharArray(),str2.toCharArray());
    }

    private static int calculaDistanciaLevenshtein(char [] str1, char [] str2) {
        int [][]distance = new int[str1.length+1][str2.length+1];

        for(int i=0;i<=str1.length;i++) {
            distance[i][0]=i;
        }

        for(int j=0;j<=str2.length;j++) {
            distance[0][j]=j;
        }

        for(int i=1;i<=str1.length;i++) {
            for(int j=1;j<=str2.length;j++) {
                distance[i][j]= minimo(distance[i-1][j]+1,
                distance[i][j-1]+1,
                distance[i-1][j-1]+
                ((str1[i-1]==str2[j-1])?0:1));
            }
        }

        return distance[str1.length][str2.length];
    }
    
    
    public static void main(String args[]){
     
        String cad1 = "ivette manzano galvan";
        
        String cad2 ="IVETTE MANZANO GALVAN";
        
        int result = calculaDistanciaLevenshtein(cad1, cad2);
        
        
        AlgoritmoLevensthein al = new AlgoritmoLevensthein();
        
        
//        String resultado = al.eliminaPalabrasCortas("El perro negro de mi amigo fue mio");
//                
//                
//        
//        logger.setLevel(Level.INFO);
//        
       logger.info(result);
//       logger.info(result);
//       
//       logger.info("********************************");
//       logger.info(resultado);
//       logger.info(resultado);
//       logger.info(resultado);
//       logger.info(resultado.length());
       
    }
}
