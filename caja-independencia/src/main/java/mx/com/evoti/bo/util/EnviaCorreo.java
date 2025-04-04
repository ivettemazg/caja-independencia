package mx.com.evoti.bo.util;

import java.io.Serializable;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.Multipart;

/**
 *
 * @author NeOleon Root
 */
public class EnviaCorreo implements Serializable {

    private static final long serialVersionUID = -4706846354048194365L;

    public static boolean sendMessageAttachment(List<String> dirAttachmentLst, String mensaje, String subject, String correo) throws MessagingException{
        boolean rpta = false;
        Properties props = new Properties();
        boolean Attach = false;
//        props.setProperty("mail.smtp.host", "mail.evoti.com.mx");
        props.setProperty("mail.smtp.host", "mail.cajaindependencia.com");
        props.setProperty("mail.smtp.port", "26");
//        props.setProperty("mail.smtp.user", "caja@evoti.com.mx");
        props.setProperty("mail.smtp.user", "contacto@cajaindependencia.com");
        props.setProperty("mail.smtp.auth", "true");
        Session session = Session.getInstance(props);

        MimeMessage message = new MimeMessage(session);
        try {
            String dt = "<table style='border: 2px solid gray;' width='500px' cellspacing=0 cellpadding=0><tr><td style='background-color: #4a5ba8;font-family: helvetica;font-size: 25px;color: white;' align='right'>SINDICATO DE INDEPENDENCIA &nbsp;&nbsp;<br>DE AVIACI&Oacute;N &nbsp;&nbsp;</td></tr><tr><td style='background-color: #fbfbfb;font-family: helvetica;font-size: 12px;color: #353545;border: 1px solid gray;'><b>" + mensaje + "</b></td></tr></table>";

            //message.setText(mensaje);
            message.setFrom(new InternetAddress("contacto@cajaindependencia.com"));
//            message.setFrom(new InternetAddress("caja@evoti.com.mx"));
            //La direccion de la persona a enviar
            Address address2 = new InternetAddress(correo);
            message.addRecipient(Message.RecipientType.TO, address2);
            message.setSubject(subject, "utf-8");
            MimeBodyPart mbp1 = null, 
                    mbp2 = null;
                    
            Multipart mp = null;
            FileDataSource fds = null;


            if (dirAttachmentLst != null && dirAttachmentLst.size() > 0) {

                                Attach=true;  
                mbp1=new MimeBodyPart();   
                mbp1.setText(dt,"ISO-8859-1","html");
                mp=new MimeMultipart();
                mp.addBodyPart(mbp1);
                
                for (String dirAttachment : dirAttachmentLst) {

             
                      
  
                            String nvoit = dirAttachment;
                            java.io.File nvofile = new java.io.File(nvoit);
                            boolean isDirectory = nvofile.isDirectory();
                            if (!isDirectory) {
                                mbp2 = new MimeBodyPart();
                                fds = new FileDataSource(nvofile);
                                mbp2.setDataHandler(new DataHandler(fds));
                                mbp2.setFileName(fds.getName());
                                mp.addBodyPart(mbp2);
                            }
                        

                    
                }
            }
            if (!Attach) {

                message.setText(dt, "ISO-8859-1", "html");
            } else {
                message.setContent(mp);
                //message.setSendDate(new java.util.Date());     
            }

            Transport t = session.getTransport("smtp");
//            t.connect("mail.evoti.com.mx", "caja@evoti.com.mx", "02102013");
            t.connect("mail.cajaindependencia.com", "contacto@cajaindependencia.com", "contacto2013");
            t.sendMessage(message, message.getAllRecipients());
            t.close();
            rpta = true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
            return rpta;
        }
        return rpta;
    }

    public static boolean sendMessage(String mensaje, String subject, String email) {
        boolean rpta = false;
        Properties props = new Properties();
        // Nombre del host de correo, es smtp.gmail.com
        props.setProperty("mail.smtp.host", "mail.cajaindependencia.com");
        // TLS si está disponible
//        props.setProperty("mail.smtp.starttls.enable", "true");
        //props.setProperty("mail.smtp.ssl.trust", "smtp.gmail.com");
        // Puerto de gmail para envio de correos
        props.setProperty("mail.smtp.port", "26");
        // Cuenta de correo en gmail
        props.setProperty("mail.smtp.user", "contacto@cajaindependencia.com");
        // Si requiere o no usuario y password para conectarse.
        props.setProperty("mail.smtp.auth", "true");
        //Session session = Session.getDefaultInstance(props);
        Session session = Session.getInstance(props);
        //Verficiar el envio
        //session.setDebug(true);
        MimeMessage message = new MimeMessage(session);
        try {

            String dt = "<table style='border: 2px solid gray;' width='500px' cellspacing=0 cellpadding=0><tr><td style='background-color: #4a5ba8;font-family: helvetica;font-size: 25px;color: white;' align='right'>SINDICATO DE INDEPENDENCIA &nbsp;&nbsp;<br>DE AVIACI&Oacute;N &nbsp;&nbsp;</td></tr><tr><td style='background-color: #fbfbfb;font-family: helvetica;font-size: 12px;color: #353545;border-top: 2px solid gray;'><b>" + mensaje + "</b></td></tr></table>";

            //message.setSubject(subject);
            //message.setText(mensaje);
            message.setText(dt, "ISO-8859-1", "html");
            //Address address = new InternetAddress(dt,"Sindicato Independencia de Aviacion");
            //message.setFrom(address);
            message.setFrom(new InternetAddress("contacto@cajaindependencia.com"));
            //La direccion de la persona a enviar
            Address address2 = new InternetAddress(email);
            message.addRecipient(Message.RecipientType.TO, address2);
            message.setSubject(subject, "utf-8");
            Transport t = session.getTransport("smtp");
            t.connect("mail.cajaindependencia.com", "contacto@cajaindependencia.com", "contacto2013");
            t.sendMessage(message, message.getAllRecipients());
            t.close();
            rpta = true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
            return rpta;
        } /*catch (UnsupportedEncodingException ex) {
         ex.printStackTrace();
         return rpta;
         }*/
        return rpta;
    }

    public static void main(String args[]) {

        System.out.println("HOLA");
        List<String> str = new ArrayList<String>();
        str.add("C:\\Users\\Venus\\Desktop\\caja081013\\caja081013\\cajalast\\build\\web\\101493_201013Koala.jpg");
        str.add("C:\\Users\\Venus\\Desktop\\caja081013\\caja081013\\cajalast\\build\\web\\AnexoB.pdf");
        try {
            EnviaCorreo.sendMessageAttachment(str, "Estimado(a): Usuario\n\n Tu solicitud de crédito fue registrada exitosamente bajo el folio "
                    + "No.xxxx, a partir de este momento se encuentra en proceso de revisión y autorización, "
                    + "en breve te estaremos informando vía correo electrónico el estatus de tu solicitud.\n\n "
                    + "Juan Bernardo Carmona Ávila.\n\nPresidente Caja de Ahorro",
                    "Solicitud de Credito", "venus_de_milo_19@hotmail.com");
        } catch (MessagingException ex) {
            Logger.getLogger(EnviaCorreo.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
