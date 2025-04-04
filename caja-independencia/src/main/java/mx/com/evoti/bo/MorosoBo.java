/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo;

import mx.com.evoti.dao.CatorcenasDao;
import mx.com.evoti.dao.MorosoDao;

/**
 *
 * @author Venette
 */
public class MorosoBo {
    
    private CatorcenasDao catorcenaDao;
    private MorosoDao morosoDao;

    public MorosoBo() {
        catorcenaDao = new CatorcenasDao();
        morosoDao = new MorosoDao();
    }
    
   
    
    
}
