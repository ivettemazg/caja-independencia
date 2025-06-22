/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao.jasper;

import java.io.Serializable;
import java.util.List;
import mx.com.evoti.dao.ManagerDB;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.UsuarioDto;
import mx.com.evoti.dto.common.AmortizacionDto;
import mx.com.evoti.dto.jasper.FondeoDto;
import mx.com.evoti.dto.jasper.PendienteDto;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 *
 * @author Venette
 */
public class DoctosJasperSolicitudDao extends ManagerDB implements Serializable {

    private static final long serialVersionUID = -6150138052372029124L;

    public FondeoDto getCreditoBySolicitud(Long idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select sol_clave_empleado as claveEmpleado,"
                    + "sol_monto_solicitado as montoSolicitado,sol_pago_credito as montoPago, "
                    + "sol_banco as banco, sol_numero_cuenta as numeroCuenta, "
                    + "sol_clabe_interbancaria as clabeInterbancaria,"
                    + "sol_nombre_tarjetahabiente as nombreTarjetahabiente,"
                    + "sol_catorcenas as catorcenas, sol_producto as idProducto,"
                    + "sol_fecha_deposito as fechaDeposito, p.pro_descripcion as producto, "
                    + "sol_fecha_ult_catorcena as fechaUltimoPago, "
                    + "sol_estatus as estatusSolicitud, se.sol_est_nmbr_est as estatusSolStr, "
                    + "cre_id as idCredito,"
                    + "cre_fecha_primer_pago as fechaPrimerPago, u.usu_correo as correo, "
                    + "UPPER(CONCAT_WS(' ',usu_paterno,usu_materno,usu_nombre)) as nombre,"
                    + "sol_numero as solicitudNumero,"
                    + "cre_clave as claveCredito "
                    + "from solicitudes s inner join creditos_final c on s.sol_id = c.cre_solicitud "
                    + "inner join usuarios u on s.sol_usu_id=u.usu_id "
                    + "inner join productos p on s.sol_producto=p.pro_id "
                    + "inner join solicitud_estatus se on se.sol_est_id = s.sol_estatus "
                    + "where sol_id = %1$s "
                    + "order by sol_numero_cuenta",
                    idSolicitud));

            FondeoDto solFondeada = (FondeoDto) query.setResultTransformer(Transformers.aliasToBean(FondeoDto.class)).uniqueResult();
            solFondeada.setIdSolicitud(idSolicitud);
            return solFondeada;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public List<AmortizacionDto> obtieneAmortizacion(Long idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select amo_numero_pago as amoNumeroPago,"
                    + "amo_capital as amoCapital, amo_amortizacion as amoAmortizacion,"
                    + "amo_interes as amoInteres,amo_monto_pago as amoMontoPago,amo_fecha_pago as amoFechaPago,"
                    + "amo_estatus as amoEstatus "
                    + "from amortizacion inner join creditos_final on amo_credito = cre_id "
                    + "where cre_solicitud=%1$s "
                    + "order by amo_numero_pago asc ", idSolicitud));

            List<AmortizacionDto> amortizacion = query.setResultTransformer(Transformers.aliasToBean(AmortizacionDto.class)).list();

            return amortizacion;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public FondeoDto obtieneDatosAnexoB(Long idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(String.format("select CONCAT_WS(' ',usu_paterno,usu_materno,usu_nombre) as nombre,"
                    + "e.emp_descripcion as empresa,"
                    + "c.cre_clave as claveCredito ,s.sol_pago_credito as pagoCatorcenal "
                    + "from solicitudes s left join usuarios u "
                    + "on s.sol_usu_id=u.usu_id "
                    + "left join empresas e on u.usu_empresa=e.emp_id "
                    + "left join creditos_final c on s.sol_id=c.cre_solicitud where sol_id=%1$s "
                    + "group by CONCAT_WS(' ',usu_paterno,usu_materno,usu_nombre),e.emp_descripcion,c.cre_id,"
                    + "s.sol_pago_credito", idSolicitud));

            FondeoDto fondeo = (FondeoDto) query.setResultTransformer(Transformers.aliasToBean(FondeoDto.class)).uniqueResult();

            return fondeo;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public FondeoDto obtieneDatosAnexoC(Long solicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            String sql =
                "SELECT s.sol_monto_solicitado                           AS montoSolicitado, " +
                "       UPPER(CONCAT_WS(' ', u.usu_paterno, u.usu_materno, u.usu_nombre)) AS nombre, " +
                "       UPPER(u.usu_calle)                                AS calle, " +
                "       UPPER(u.usu_numext)                               AS numext, " +
                "       UPPER(u.usu_colonia)                              AS colonia, " +
                "       UPPER(u.usu_cp)                                   AS cp, " +
                "       UPPER(u.usu_municipio)                            AS municipio, " +
                "       u.usu_estado                                      AS estado, " +
                "       DATE_ADD(ANY_VALUE(c.cre_fecha_primer_pago), " +
                "                INTERVAL ANY_VALUE(c.cre_catorcenas)*14 DAY) AS fechaUltimoPago, " +
                "       ANY_VALUE(c.cre_clave)                            AS claveCredito " +
                "FROM   solicitudes s " +
                "JOIN   usuarios u ON s.sol_usu_id = u.usu_id " +
                "JOIN   creditos_final c ON s.sol_id = c.cre_solicitud " +
                "WHERE  s.sol_id = :solicitud";

            Query query = session.createSQLQuery(sql)
                .setParameter("solicitud", solicitud)
                .setResultTransformer(Transformers.aliasToBean(FondeoDto.class));

            FondeoDto fondeo = (FondeoDto) query.uniqueResult();
            return fondeo;

        } catch (Exception e) {
            throw new IntegracionException("Error al obtener datos del Anexo C", e);
        } finally {
            super.endTransaction();
        }
    }

    public List<UsuarioDto> obtieneAvalesAnexoC(Long solicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select s.sol_ava_clave_empleado as cveEmpleado,"
                            + "CONCAT_WS(' ',u.usu_nombre,u.usu_paterno,u.usu_materno) as nombreCompleto, "
                            + "CONCAT_WS(' ',u.usu_calle,u.usu_numext,u.usu_colonia,u.usu_cp,u.usu_municipio,u.usu_estado) as domicilioConcat "
                            + "from solicitud_avales s inner join usuarios u on s.sol_ava_id_empleado=u.usu_id "
                            + "where sol_ava_solicitud= %1$s", solicitud));

            List<UsuarioDto> fondeo = query.setResultTransformer(Transformers.aliasToBean(UsuarioDto.class)).list();

            return fondeo;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public List<PendienteDto> obtieneAvalesSolicitud(Long solicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select s.sol_ava_clave_empleado as claveEmpleadoAva "
                            + ",CONCAT_WS(' ',u.usu_nombre,u.usu_paterno,u.usu_materno) as nombreAva ,"
                            + "CONCAT_WS(' ',u.usu_calle,u.usu_numext,u.usu_colonia"
                            + ",u.usu_cp,u.usu_municipio,u.usu_estado) as direccion,"
                            + "u.usu_fecha_nacimiento as fechaNacimientoAva,"
                            + "u.usu_rfc as rfcAva,u.usu_sexo as sexoAva,u.usu_fecha_ingreso as fechaIngresoAva,"
                            + "u.usu_telefono as telefonoAva,u.usu_celular as celularAva,"
                            + "e.emp_descripcion as empresaAva,"
                            + "u.usu_calle as calleAva,u.usu_numext as numextAva,"
                            + "u.usu_colonia as coloniaAva,u.usu_cp as cpAva,u.usu_municipio as municipioAva,"
                            + "u.usu_estado as estadoAva "
                            + "from solicitud_avales s inner join usuarios u on s.sol_ava_id_empleado=u.usu_id "
                            + "inner join empresas e on u.usu_empresa=e.emp_id where sol_ava_solicitud= %1$s", solicitud));

            List<PendienteDto> fondeo = query.setResultTransformer(Transformers.aliasToBean(PendienteDto.class)).list();

            return fondeo;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public FondeoDto obtieneDatosSolicitud(Long solicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select sol_numero as noSolicitud,c.cre_clave as claveCredito,"
                            + "UPPER(CONCAT_WS(' ',usu_paterno,usu_materno,usu_nombre)) as nombre,u.usu_edo_civil as edoCivil, "
                            + "u.usu_fecha_nacimiento as fechaNacimiento,u.usu_sexo as sexo,"
                            + "UPPER(u.usu_rfc) as rfc,u.usu_identificacion as identificacion,"
                            + "u.usu_correo as correo,u.usu_estado as estado,u.usu_telefono as telefono, "
                            + "u.usu_celular as celular,"
                            + "UPPER(CONCAT_WS(' ',u.usu_calle,u.usu_numext,u.usu_colonia,"
                            + "u.usu_cp,u.usu_municipio,u.usu_estado)) as direccion, "
                            + "e.emp_descripcion as empresa,e.emp_rfc as rfcEmpresa,e.emp_telefono as telefonoEmpresa,"
                            + "u.usu_puesto as puesto,usu_fecha_ingreso_empresa as fechaIngreso,"
                            + "t.tab_mensual as salarioNeto,e.emp_direccion as direccionEmpresa, "
                            + "s.sol_monto_solicitado as montoSolicitado,"
                            + "s.sol_catorcenas as catorcenas,p.pro_descripcion as producto,"
                            + "s.sol_pago_total as montoTotalPagar,c.cre_pago_quincenal as pagoCatorcenal,"
                            + "s.sol_banco as banco,s.sol_nombre_tarjetahabiente as nombreTarjetahabiente,"
                            + "s.sol_numero_cuenta as numeroCuenta,s.sol_clabe_interbancaria as clabeInterbancaria, c.cre_producto as creProducto "                       
                            + "from solicitudes s "
                            + "inner join creditos_final c on s.sol_id=c.cre_solicitud "
                            + "inner join usuarios u on s.sol_usu_id=u.usu_id "
                            + "inner join empresas e on u.usu_empresa=e.emp_id "
                            + "inner join productos p on c.cre_producto=p.pro_id "
                            + "left join tabulador t on usu_puesto=t.tab_descripcion "
                            + "where sol_id=%1$s", solicitud));

            FondeoDto fondeo = (FondeoDto) query.setResultTransformer(Transformers.aliasToBean(FondeoDto.class)).uniqueResult();

            return fondeo;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

    public List<FondeoDto> obtieneDatosAviso(long idSolicitud) throws IntegracionException {
        try {
            super.beginTransaction();

            SQLQuery query = session.createSQLQuery(
                    String.format("select c.cre_clave as claveCredito,"
                            + "upper(CONCAT_WS(' ',usu_paterno,usu_materno,usu_nombre)) as nombre,"
                            + "s.sol_monto_solicitado as montoSolicitado,"
                            + "e.emp_descripcion as nombreEmpresa "
                            + "from solicitudes s inner join creditos_final c on s.sol_id=c.cre_solicitud "
                            + "inner join usuarios u on s.sol_usu_id=u.usu_id "
                            + "inner join empresas e on e.emp_id  = u.usu_empresa "
                            + "where sol_id=%1$s", idSolicitud));

            List<FondeoDto> fondeo = query.setResultTransformer(Transformers.aliasToBean(FondeoDto.class)).list();

            return fondeo;

        } catch (Exception he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }

    }

}
