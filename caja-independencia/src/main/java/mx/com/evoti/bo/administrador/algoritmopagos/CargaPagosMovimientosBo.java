/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.administrador.algoritmopagos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import mx.com.evoti.bo.bancos.BancosBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.util.LectorExcel;
import mx.com.evoti.dao.CargaPagosAportacionesDao;
import mx.com.evoti.dao.bancos.BancosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.ArchivoDto;
import mx.com.evoti.dto.PagoDto;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.hibernate.pojos.ArchivosHistorial;
import mx.com.evoti.hibernate.pojos.Catorcenas;
import mx.com.evoti.util.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venus
 */
public class CargaPagosMovimientosBo implements Serializable {

    private static Logger LOGGER = LoggerFactory.getLogger(CargaPagosMovimientosBo.class);
    private static final long serialVersionUID = 8230108908765577150L;
    private CargaPagosAportacionesDao dao;
    private AlgoritmoAsignaPagosBo algoritmoPagosBo;
    private List<ArchivoDto> archivos;
    private boolean dlg1;

    public CargaPagosMovimientosBo() {
        dao = new CargaPagosAportacionesDao();
        algoritmoPagosBo = new AlgoritmoAsignaPagosBo();
    }

    public Boolean validaArchivoExiste(String nombreArchivo) throws BusinessException {
        LOGGER.error("Dentro de validarArchivoExiste()");
        ArchivosHistorial archivo;
       
        try {
            archivo = dao.buscaNombreArchivo(nombreArchivo);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
       

        if (archivo != null) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }

    public Boolean validaSiEsCatorcena(Date catorcena) throws BusinessException {
        Catorcenas catorcenaExacta = null;
        try {
            catorcenaExacta = dao.obtieneCatorcenaExacta(catorcena);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

        if (catorcenaExacta != null) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }

    /**
     * Metodo que lee el archivo excel e inserta los registros necesarios en la tabla archivos y pagos 
     * o movimientos
     * @param nombreArchivo
     * @param rutaArchivo
     * @param tipoArchivo
     * @throws BusinessException 
     */
    public void procesaExcel(String nombreArchivo, String rutaArchivo, int tipoArchivo) throws BusinessException {
        //Cuando el tipo archivo es de pagos
        if (tipoArchivo == 1) {
            List<PagoDto> pagos = LectorExcel.leerArchivoExcel(rutaArchivo, tipoArchivo);

            if (!pagos.isEmpty()) {
                try {
                    ArchivosHistorial archivo = generaArchivoDto(nombreArchivo, pagos.get(0), pagos.size(), 1);
                    Integer idArchivo = dao.insertaArchivo(archivo);
                    dao.insertaListaPagos(pagos, idArchivo);
                    dao.updateUsuIdPago(idArchivo);
                    
                    /**
                     * Guarda en bancos el registro correspondiente al archivo
                     */
                    guardaEnBancos(idArchivo, tipoArchivo, archivo);
                    
                    
                } catch (IntegracionException ex) {
                      LOGGER.error("Error al insertar los pagos en la base de datos");
                        throw new BusinessException(ex.getMessage(), ex);
                    }
            } else {
                throw new BusinessException("El archivo no tiene registros que procesar.");
            }
            //Cuando el tipoArchivo = 2 es de movimientos
        } else {
            List<MovimientosDto> aportaciones = LectorExcel.leerArchivoExcel(rutaArchivo, tipoArchivo);
            if (!aportaciones.isEmpty()) {
                try {
                    ArchivosHistorial archivo = generaArchivoDto(nombreArchivo, aportaciones.get(0), aportaciones.size(), 2);
                    Integer idArchivo = dao.insertaArchivo(archivo);
                    dao.insertaListaMovimientos(aportaciones, idArchivo);
                    dao.updateUsuIdMov(idArchivo);
                    
                    //Actualización de ahorros no fijos cuando no existe ahorro fijo
                    actualizacionAFyNF(idArchivo);
                     /**
                     * Guarda en bancos el registro correspondiente al archivo
                     */
                    guardaEnBancos(idArchivo, tipoArchivo, archivo);
                    
                } catch (IntegracionException ex) {
                    LOGGER.error("Error al insertar los movimientos en la base de datos");
                    throw new BusinessException("Error al insertar los movimientos", ex);
                }
            }
        }
    }

    public ArchivosHistorial generaArchivoDto(String nombreArchivo, Object pagoApo, int noPagos, int tipoArchivo) {
        ArchivosHistorial archivo = new ArchivosHistorial();
        if (tipoArchivo == 1) {
            PagoDto pago = (PagoDto) pagoApo;
            archivo.setArhEmpresa(pago.getPagEmpresa());
            archivo.setArhFechaCatorcena(pago.getPagFecha());
            archivo.setArhEstatus(1); //Se pone el estatus del archivo en pendiente cuando es archivo de pagos
        } else {
            MovimientosDto aportacion = (MovimientosDto) pagoApo;
            archivo.setArhEmpresa(aportacion.getMovEmpresa());
            archivo.setArhFechaCatorcena(aportacion.getMovFecha());
            archivo.setArhEstatus(2); //Se pone el estatus del archivo en procesado cuando son aportaciones
        }
        archivo.setArhFechaSubida(new Date());
        archivo.setArhNombreArchivo(nombreArchivo);
        archivo.setArhRegistros(noPagos);
        archivo.setArhTipoArchivo(tipoArchivo);

        return archivo;
    }

    public List<ArchivoDto> obtenerArchivosHistorico() throws BusinessException {
        try {
            return dao.obtenerArchivosHistorico();
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    public List<ArchivoDto> obtenerArchivoHistoricoXCatorcena(Date catorcena) throws BusinessException {
        try {
            return dao.obtenerArchivosHistoricoXCatorcena(catorcena);
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    public void eliminarArchivo(ArchivoDto archivo) throws BusinessException {
        try {
            if (archivo.getArhTipoArchivo() == 1) {
                dao.eliminarPagosXArchivo(archivo.getArhId());
            } else {
                dao.eliminarMovimientosXArchivo(archivo.getArhId());

            }
            dao.eliminarArchivoXId(archivo.getArhId());

        } catch (IntegracionException sqlE) {
            throw new BusinessException(sqlE.getMessage(), sqlE);
        }
    }

    
    /**
     * Método que lanza el algoritmo de pagos, iniciando con validaciones de archivos
     * @param catorcena
     * @return
     * @throws BusinessException 
     */
    public String procesaArchivosCargados(Date catorcena) throws BusinessException {
        String msj = null;
        dlg1 = false;
        try {
            boolean archivosPendientes = false;
            
            archivos = dao.buscaArchivosEnFechaConEstatusPendiente(catorcena);
            
            Long pagos = dao.buscaPagosSinUsuId(catorcena);

            for(ArchivoDto arcDto : archivos){
                if(arcDto.getArhEstatus()==1){
                    archivosPendientes = true;
                }
            }

            /**
             * Valida que haya archivos cargados en la catorcena solicitada
             */
            if (archivos.isEmpty()) {
                msj = ("No hay archivos cargados en esta catorcena.");
            } else if (pagos > 0) {
                msj = ("Hay cambios de empresa pendientes, favor de aplicarlos para poder procesar los pagos.");
            } else 
                //de aqui se quito la validacion que no dejaba pasar a menos uqe ya estuvieran cargados
                  //      todos los archivos
                /**
             * Debe validar si los archivos que se van a procesar tienen estatus
             * pendiente
             */
            if (!archivosPendientes) {
                msj = "Todos los archivos en esta catorcena ya han sido procesados, favor de revisar en la pantalla de resultados.";
                dlg1 = true;
            }  else {
                
                for(ArchivoDto arcDto : archivos){
                    LOGGER.info("Procesando archivo "+arcDto.getArhId()+"--------------------");
                    algoritmoPagosBo.initAlgoritmoAsignacion(arcDto.getArhId());
                }
                

            }

        } catch (IntegracionException ex) {
            throw new BusinessException("Error al consultar en la base de datos.", ex);
        }
        return msj;
    }

    private String recorreListaEmpresas(String msj, List<Integer> idEmpresas) {
        for (int i = 0; i < idEmpresas.size(); i++) {
            switch (idEmpresas.get(i)) {
                case 1:
                    msj += Constantes.AEROMEXICO + "<BR>";
                    break;
                case 2:
                    msj += Constantes.TECHOPS + "<BR>";
                    break;
                case 3:
                    msj += Constantes.SISTEM + "<BR>";
                    break;
                case 4:
                    msj += Constantes.CARGO + "<BR>";
                    break;
            }
        }
        return msj;
    }

    /**
     * Metodo que obtiene el detalle de los pagos o las aportaciones segun sea
     * el registro sobre el cual se dio click en la tabla
     * @param archivo
     * @return
     * @throws IntegracionException 
     */
    public List abreDetalleArchivo(ArchivoDto archivo) throws BusinessException{

        try {
            switch (archivo.getArhTipoArchivo()) {
                case 1:
                    return dao.obtenerPagosXArchivo(archivo.getArhId());
                case 2:
                    return dao.obtenerAportacionesXArchivo(archivo.getArhId());
            }
            return null;
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public static void main(String args[]) {
        CargaPagosMovimientosBo bo = new CargaPagosMovimientosBo();
        try {
            String mensaje = bo.procesaArchivosCargados(new Date());

            System.out.println(mensaje);
        } catch (BusinessException ex) {
            java.util.logging.Logger.getLogger(CargaPagosMovimientosBo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

     private void guardaEnBancos(Integer idArchivo, Integer tipoArchivo, ArchivosHistorial archivo) throws BusinessException {
        try {
            Double montoArchivo= dao.obtieneSumaArchivo(idArchivo,tipoArchivo);
            BancosBo banBo = new BancosBo();
            
            banBo.guardaBancosArchivo(tipoArchivo, idArchivo, archivo.getArhEmpresa(), montoArchivo);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }
     
 /**
     * Actualiza los ahorros fijos, los transforma a fijos cuando el usuario en cuestión
     * no tiene ahorro fijo en esa catorcena, en el caso de que tenga 2 aportaciones no fijas,
     * la menor 
     * @param idArchivo 
     * @throws mx.com.evoti.dao.exception.IntegracionException 
     */
    public void actualizacionAFyNF(Integer idArchivo) throws IntegracionException {
         //obtener usuarios con aportaciones no fijas 2
         List<MovimientosDto> aNoFijas = dao.getANoFijo(idArchivo);
         //recorrer lsita de usuarios con aportaciones no fijas y validar si tienen fijas
         for(MovimientosDto mov : aNoFijas){
             
             List<MovimientosDto> aF = dao.getAFijo(mov.getMovUsuId());
             
             //Cuando no tiene ahorro fijo
             if(aF.isEmpty()){
                 mov.setMovProducto(1);
                 mov.setMovCambioanfaf(1);
                 
                 dao.updtMovimiento(mov);
                 
             }
             
         }
    }

     
     
    
    /**
     * @return the dlg1
     */
    public boolean isDlg1() {
        return dlg1;
    }

    /**
     * @param dlg1 the dlg1 to set
     */
    public void setDlg1(boolean dlg1) {
        this.dlg1 = dlg1;
    }

    /**
     * @return the archivos
     */
    public List<ArchivoDto> getArchivos() {
        return archivos;
    }

    /**
     * @param archivos the archivos to set
     */
    public void setArchivos(List<ArchivoDto> archivos) {
        this.archivos = archivos;
    }


   
}
