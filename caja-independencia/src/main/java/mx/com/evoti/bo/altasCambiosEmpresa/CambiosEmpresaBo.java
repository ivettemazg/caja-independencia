/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.altasCambiosEmpresa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.bo.util.AlgoritmoLevensthein;
import mx.com.evoti.dao.altasCambiosEmp.CambiosEmpresaDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.MovimientosDto;
import mx.com.evoti.dto.PagoDto;
import mx.com.evoti.hibernate.pojos.AltasCambiosHist;
import mx.com.evoti.hibernate.pojos.Empresas;
import mx.com.evoti.hibernate.pojos.Movimientos;
import mx.com.evoti.hibernate.pojos.Pagos;
import mx.com.evoti.hibernate.pojos.Usuarios;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class CambiosEmpresaBo implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CambiosEmpresaBo.class);
    private static final long serialVersionUID = -1444548766626506547L;

    private CambiosEmpresaDao dao;

    public CambiosEmpresaBo() {
        dao = new CambiosEmpresaDao();

    }

    /**
     * Realiza el proceso de cambio de empresa de un empleado
     *
     * @param catorcena
     * @return
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public int realizaCambioEmpresa(Date catorcena) throws BusinessException {
        LOGGER.info("Dentro de realizaCambioEmpresa");
        try {

            /**
             * Procesa los cambios de empresa para movimientos
             */
            List<MovimientosDto> movs = dao.getPendtsCambiosAltas(Util.generaFechaFormateada(catorcena));
            LOGGER.info("Posibles cambios de empresa: " +movs.size());
            int numCambios = 0;

            for (int i = 0; i < movs.size(); i++) {

                MovimientosDto movimiento = (MovimientosDto) movs.get(i);
                List<Usuarios> coincidencias;

                /**
                 * Obtiene los usuarios que coincidieron con el nombre
                 */
                coincidencias = comparaNombres(movimiento);

                /**
                 * Cuando existe una o varias coincidencias, significa que
                 * encontro otro usuario con el mismo nombre, por lo tanto hay
                 * que validar si se considera un cambio de empresa o un
                 * homonimo
                 */
                if (!coincidencias.isEmpty()) {

                    for (Usuarios usu : coincidencias) {
                        MovimientosDto ultimosMovs = dao.getUltimosMovs(usu.getUsuId(), movimiento.getMovId());

                        if (ultimosMovs != null) {

                            /**
                             * Si el movimiento de la coincidencia es despues del ultimo movimiento encontrado
                             * y la empresa es diferente significa que es cambio de empresa
                             */
                            if (movimiento.getMovFecha().after(ultimosMovs.getMovFecha())
                                    && !Objects.equals(movimiento.getMovEmpresa(), ultimosMovs.getMovEmpresa())) {
                               LOGGER.info("Se aplicó cambio de empresa, usuId = "+usu.getUsuId());
                                updtCambioEmpDB(usu, movimiento);
                                numCambios++;
                            }else{
                                LOGGER.info("Encontro un homonimo "+movimiento.getMovNombreEmpleado()+" en la empresa "+movimiento.getMovEmpresa());
                            }

                        } else {
                            LOGGER.error("El usuario " + usu.getUsuId() + " no tiene movimientos ");
                        }
                    }
                }
            }

            return numCambios;
        } catch (IntegracionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BusinessException(ex.getMessage(), ex);
        }

    }


    /**
     * Aplica todos los movimientos que corresponden a cambios de empresa en la
     * base de datos
     *
     * @param usuario
     * @param movimiento
     * @throws IntegracionException
     */
    public void updtCambioEmpDB(Usuarios usuario, MovimientosDto movimiento) throws IntegracionException {
        //Se construye el objeto AltasCambiosHistorial y se guarda
        AltasCambiosHist cambio = buildAltasCambios(usuario, movimiento.getMovArhId(), movimiento.getMovClaveEmpleado(),
                movimiento.getMovEmpresa(), movimiento.getMovFecha());

        //Se actualiza el usuario
        usuario.setUsuClaveEmpleado(movimiento.getMovClaveEmpleado());
        usuario.setEmpresas(new Empresas(movimiento.getMovEmpresa()));
        dao.updateUsuarioClaveEmpresa(usuario);

        //Se inserta el registro en altas_cambios_hist
        dao.insertCambioEmpresa(cambio);

        //Se actualizan los movimientos y pagos con base en los datos de usuario actualizados
        this.dao.updateUsuIdMov(movimiento.getMovFecha());
        this.dao.updateUsuIdPago(movimiento.getMovFecha());

    }

    /**
     * Si el usuario viene nulo (porque no se encontró con esa clave de empleado
     * y empresa) busca en la tabla de empleados por las primeras 3 letras de la
     * cadena (para que no se traiga toda la tabla) y se trae una lista pequeña
     * con la cual compara por cadena entera con Levensthein Si coincide el
     * nombre, regresa el usuario, si no, devuelve nulo
     *
     * @param obj
     * @return
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public List<Usuarios> comparaNombres(Object obj) throws IntegracionException {

        String nombreEmpleado = "";
        if (obj instanceof MovimientosDto) {
            MovimientosDto movimiento = (MovimientosDto) obj;
            nombreEmpleado = movimiento.getMovNombreEmpleado();

        }

        /**
         * Obtiene los usuarios que coinciden en las primeras 3 letras del
         * nombre concatenado
         */
        List<Object[]> usuarios
                = dao.obtieneUsuarioXNombre(nombreEmpleado.substring(0, 3));

        List<Usuarios> usuario = new ArrayList<>();

        /**
         * Recorre y hace comparación Levensthein para encontrar un usuario con
         * ese nombre
         */
        for (int i = 0; i < usuarios.size(); i++) {
            Object[] arr = usuarios.get(i);

            Usuarios usu = (Usuarios) arr[0];

            int distancia = 10;
            String nombreCompleto = usu.getUsuPaterno() + " " + usu.getUsuMaterno() + " " + usu.getUsuNombre();

            if (!nombreCompleto.isEmpty() && !nombreEmpleado.isEmpty()) {
//                System.out.println(dto.getNombreCompleto());
                distancia = AlgoritmoLevensthein.calculaDistanciaLevenshtein(nombreCompleto, nombreEmpleado.toUpperCase());
            }

            /**
             * Cuando la distancia es máximo de 2, la probabilidad que sea el
             * mismo es muy alta, por lo que se considera precisa la similitud y
             * se retorna el usuario
             */
            if (distancia <= 2) {

                usuario.add(usu);

            }
        }

        return usuario;
    }

    /**
     * Construye el pojo de AltasCambiosHist que se guardará como cambio de
     * empresa
     *
     * @param usuario
     * @param arhId
     * @param claveActual
     * @param empresaActual
     * @param catorcena
     * @return
     */
    public AltasCambiosHist buildAltasCambios(Usuarios usuario, Integer arhId, Integer claveActual,
            Integer empresaActual, Date catorcena) {
        AltasCambiosHist cambio = new AltasCambiosHist();

        cambio.setCnhArhId(arhId);
        cambio.setCnhCatorcenaTransaccion(catorcena);
        cambio.setCnhClaveActual(claveActual);
        cambio.setCnhClaveAnterior(usuario.getUsuClaveEmpleado());
        cambio.setCnhEmpresaActual(empresaActual);
        cambio.setCnhEmpresaAnterior(usuario.getEmpresas().getEmpId());
        cambio.setCnhTipo(Constantes.CNH_CAMBIO);
        cambio.setCnhUsuId(usuario.getUsuId());

        return cambio;

    }

    /**
     * Valida si la fecha que se esta mandando es catorcena
     *
     * @param catorcena
     * @return
     * @throws BusinessException
     */
    public Boolean validaSiEsCatorcena(Date catorcena) throws BusinessException {
        Date catorcenaExacta = null;
        try {
            catorcenaExacta = dao.obtieneCatorcenaExacta(catorcena);
        } catch (IntegracionException ex) {
            LOGGER.error("Hubo excepcion");
            throw new BusinessException(ex.getMessage(), ex);
        }

        if (catorcenaExacta != null) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }

}
