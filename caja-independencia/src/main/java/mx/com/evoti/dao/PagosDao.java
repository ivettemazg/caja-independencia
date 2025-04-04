/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.AcumuladoDto;
import mx.com.evoti.dto.PagoDto;
import mx.com.evoti.hibernate.pojos.Pagos;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Venette
 */
public class PagosDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = 5334271892674833842L;

    /**
     * Guarda un registro en la tabla pagos
     *
     * @param pago
     * @throws IntegracionException
     */
    public void guardaPago(Pagos pago) throws IntegracionException {
        super.savePojo(pago);
    }

    /**
     * Obtiene el acumulado de un usuario
     *
     * @param idUsuario
     * @return
     * @throws IntegracionException
     */
    public AcumuladoDto obtieneAcumuladoXUsuario(Integer idUsuario) throws IntegracionException {
        super.beginTransaction();

        SQLQuery sqlQuery = session.createSQLQuery(
                String.format("select pag_usu_id as acuUsuarioId, sum(pag_acumulado) as acuMonto "
                        + "from pagos where pag_usu_id = %1$s "
                        + "group by pag_usu_id ", idUsuario));

        AcumuladoDto acumulado = (AcumuladoDto) sqlQuery.setResultTransformer(Transformers.aliasToBean(AcumuladoDto.class)).uniqueResult();
        super.endTransaction();
        return acumulado;
    }

    /**
     * Obtiene el acumulado de un usuario
     *
     * @param idUsuario
     * @return
     * @throws IntegracionException
     */
    public List<PagoDto> getPagosSinRegistrar(Integer idUsuario) throws IntegracionException {
        super.beginTransaction();

        SQLQuery sqlQuery = session.createSQLQuery(
                String.format("select pag_clave_empleado as pagClaveEmpleado,pag_fecha as pagFecha,"
                        + "pag_deposito as pagMonto,"
                        + "emp_abreviacion as pagNombreEmpresa from pagos left join empresas on emp_id = pag_empresa "
                        + "where pag_usu_id=%1$s "
                        + "and pag_estatus in (1,7) "
                        + "and pag_credito is null ", idUsuario));

        List<PagoDto> pagos = sqlQuery.setResultTransformer(Transformers.aliasToBean(PagoDto.class)).list();
        super.endTransaction();
        return pagos;
    }

}
