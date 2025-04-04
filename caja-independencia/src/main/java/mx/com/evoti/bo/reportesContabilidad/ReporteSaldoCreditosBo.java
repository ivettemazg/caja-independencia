/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.reportesContabilidad;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dao.reportesContables.ReporteSaldoCreditosDao;
import mx.com.evoti.dto.reportesContables.ReporteCreditoSaldoDto;
import mx.com.evoti.hibernate.config.HibernateUtil;
import mx.com.evoti.util.Util;

/**
 *
 * @author Venette
 */
public class ReporteSaldoCreditosBo {

    public static void main(String args[]) throws IntegracionException {
        try {
            System.setProperty("Log4J.configuration", new File(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "Log4J.properties").toURI().toURL().toString());
            HibernateUtil.buildSessionFactory2();

            ReporteSaldoCreditosDao dao = new ReporteSaldoCreditosDao();
            List<ReporteCreditoSaldoDto> saldoFinal = new ArrayList<>();
            List<ReporteCreditoSaldoDto> saldosFecha = new ArrayList<>();
            dao.beginTransaction();
            Double saldoFina =0.0;
            for (int a = 2016; a < 2018; a++) {
               
                for (int m = 1; m < 13; m++) {
                     System.out.println("*************************");
                System.out.println("*************************");
                System.out.println("*************************");

                    try {
                        int d = Util.obtenerUltimoDiaMes(a, m);
                        Date fecha = Util.generaFechaDeString(a + "-" + m + "-" + d);

                        for (int c = 1; c <= 3200; c++) {
                         
                            ReporteCreditoSaldoDto fp = dao.getSaldosCreXFechaPago(fecha, c);
                            ReporteCreditoSaldoDto fa = dao.getSaldosCreXFechaAmort(fecha, c);

                            if (fp != null && fa != null) {
                                 if(fp.getMontoPrestamo()==null){
                                    fp.setMontoPrestamo(0.0);
                                }
                                if(fp.getCapitalPagado()==null){
                                    fp.setCapitalPagado(0.0);
                                }
                                 if(fa.getMontoPrestamo()==null){
                                    fa.setMontoPrestamo(0.0);
                                }
                                if(fa.getCapitalPagado()==null){
                                    fa.setCapitalPagado(0.0);
                                }
                                ReporteCreditoSaldoDto credito = new ReporteCreditoSaldoDto();
                                credito.setIdCredito(c);
                                credito.setCapitalPagado(fp.getCapitalPagado() + fa.getCapitalPagado());
                                credito.setMontoPrestamo(fp.getMontoPrestamo());
                                credito.setPagoCatorcenal(fp.getPagoCatorcenal());
                                credito.setSaldo(credito.getMontoPrestamo() - credito.getCapitalPagado());
                                if(credito.getSaldo()<1 && credito.getSaldo()>-1){
                                    credito.setSaldo(0.0);
                                }
                                credito.setEmpresa(fp.getEmpresa());
                                saldoFina=saldoFina+credito.getSaldo();
                                saldoFinal.add(credito);
                                 System.out.println(Util.generaFechaFormateada(fecha, "dd/MM/yyyy")+","+credito.getIdCredito() + "," 
                                    + credito.getCapitalPagado() + "," + credito.getMontoPrestamo() + ","
                                    + credito.getSaldo()+","+credito.getEmpresa()+","+"AMBOS");
                                
                            }else if(fp!=null && fa ==null){
                                ReporteCreditoSaldoDto credito = new ReporteCreditoSaldoDto();
                                if(fp.getMontoPrestamo()==null){
                                    fp.setMontoPrestamo(0.0);
                                }
                                if(fp.getCapitalPagado()==null){
                                    fp.setCapitalPagado(0.0);
                                }
                                credito.setIdCredito(c);
                                credito.setCapitalPagado(fp.getCapitalPagado());
                                credito.setMontoPrestamo(fp.getMontoPrestamo());
                                credito.setPagoCatorcenal(fp.getPagoCatorcenal());
                                credito.setSaldo(fp.getMontoPrestamo() - fp.getCapitalPagado());
                                credito.setEmpresa(fp.getEmpresa());
                                saldoFina=saldoFina+credito.getSaldo();
                                if(credito.getSaldo()<1 && credito.getSaldo()>-1){
                                    credito.setSaldo(0.0);
                                }
                                saldoFinal.add(credito);
                                 System.out.println(Util.generaFechaFormateada(fecha, "dd/MM/yyyy")+","+credito.getIdCredito() + "," 
                                    + credito.getCapitalPagado() + "," + credito.getMontoPrestamo() + ","
                                    + credito.getSaldo()+","+credito.getEmpresa()+",FP");
                            
                            }else if(fp==null && fa !=null){
                                ReporteCreditoSaldoDto credito = new ReporteCreditoSaldoDto();
                                if(fa.getMontoPrestamo()==null){
                                    fa.setMontoPrestamo(0.0);
                                }
                                if(fa.getCapitalPagado()==null){
                                    fa.setCapitalPagado(0.0);
                                }
                                
                                credito.setIdCredito(c);
                                credito.setCapitalPagado(fa.getCapitalPagado());
                                credito.setMontoPrestamo(fa.getMontoPrestamo());
                                credito.setPagoCatorcenal(fa.getPagoCatorcenal());
                                credito.setSaldo(fa.getMontoPrestamo() - fa.getCapitalPagado());
                                credito.setEmpresa(fa.getEmpresa());
                                if(credito.getSaldo()<1 && credito.getSaldo()>-1){
                                    credito.setSaldo(0.0);
                                }
                                saldoFina=saldoFina+credito.getSaldo();
                                saldoFinal.add(credito);
                                 System.out.println(Util.generaFechaFormateada(fecha, "dd/MM/yyyy")+","+credito.getIdCredito() + "," 
                                    + credito.getCapitalPagado() + "," + credito.getMontoPrestamo() + ","
                                    + credito.getSaldo()+","+credito.getEmpresa()+",FA");
                            }
                            
                             
            
                            
                            
                        }

                        
                         ReporteCreditoSaldoDto dtoFinal = new ReporteCreditoSaldoDto();
                    
                    dtoFinal.setFecha(fecha);
                    dtoFinal.setSaldo(saldoFina);
                        
                        
                    } catch (IntegracionException ex) {
                        Logger.getLogger(ReporteSaldoCreditosBo.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    
                   
                    
                }

            }
            
            System.out.println("*****************************************************************");
            System.out.println("*****************************************************************");
            System.out.println("*****************************************************************");
            saldoFinal.stream().forEach((credito) -> {
                            System.out.println(credito.getFecha()+","+credito.getIdCredito() + "," 
                                    + credito.getCapitalPagado() + "," + credito.getMontoPrestamo() + ","
                                    + credito.getSaldo()+","+credito.getEmpresa());
                        });
            
            dao.endTransaction();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ReporteSaldoCreditosBo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
