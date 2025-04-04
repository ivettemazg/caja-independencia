/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.bancos;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.bancos.BancosDao;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.bancos.BancosConceptosDto;
import mx.com.evoti.dto.bancos.BancosDto;
import mx.com.evoti.dto.bancos.EstadoCuentaDto;
import mx.com.evoti.dto.bancos.dao.BancosEstadoCtaDto;
import mx.com.evoti.hibernate.pojos.Bancos;
import mx.com.evoti.hibernate.pojos.BancosConceptos;
import mx.com.evoti.hibernate.pojos.EstadoCuenta;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class BancoAjustesBo implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BancoAjustesBo.class);
    private static final long serialVersionUID = -604848521024535467L;

    private BancosDao banDao;

    private List<BancosEstadoCtaDto> ajustadosOriginal;
    private List<BancosDto> lstBancos;
    private List<EstadoCuentaDto> lstEstadosCuenta;
    private Double totalBanco;
    private Double totalEdoCta;

    private List<BancosEstadoCtaDto> lstBorraBanco;
    private List<BancosEstadoCtaDto> lstBorraEC;

    public BancoAjustesBo() {
        banDao = new BancosDao();
    }

    /**
     * Obtiene las listas que se mostrarán el en el ajuste de bancos
     *
     * @param fechaInicio
     * @param fechaFin
     * @throws BusinessException
     */
    public void obtieneListasBanco(Date fechaInicio, Date fechaFin) throws BusinessException {
        try {
            Long idPantalla = 0L;
            lstBancos = new ArrayList<>();
            lstEstadosCuenta = new ArrayList<>();

            //Obtiene los registros de bancos uqe ya estan ajustados
            
            lstBancos = banDao.getBancoAjustados(fechaInicio, fechaFin);

            String rbeIds = "";

            List<BigInteger> rbeIdsBI = new ArrayList<>();
            rbeIdsBI = obtieneIdsRelacion(lstBancos);

            for (int i = 0; i < rbeIdsBI.size(); i++) {
                rbeIds = rbeIds.concat(rbeIdsBI.get(i).toString()).
                        concat(rbeIdsBI.size() - 1 > i ? "," : "");

            }
            //Se obtienen los estados de cuenta ajustados
            if (!rbeIdsBI.isEmpty()) {
                
                lstEstadosCuenta = banDao.getEdoCtaAjustados(rbeIds);
            }

            //Se les agrega a las listas un id de agrupacion para la pantalla
            for (int i = 0; i < rbeIdsBI.size(); i++) {
                idPantalla++;
                for (BancosDto banDto : lstBancos) {
                    if (banDto.getRbeId().equals(rbeIdsBI.get(i))) {
                        banDto.setIdPantalla(new BigInteger(idPantalla.toString()));
                    }

                }

                for (EstadoCuentaDto ecDto : lstEstadosCuenta) {
                    if (ecDto.getRbeId().equals(rbeIdsBI.get(i))) {
                        ecDto.setIdPantalla(new BigInteger(idPantalla.toString()));
                    }
                }

            }

            //Añade los registros que tienen estatus pendiente
            
            lstBancos.addAll(banDao.getBancoPendts(fechaInicio, fechaFin));
            lstEstadosCuenta.addAll(banDao.getEdoCtaPendts(fechaInicio, fechaFin));

            lstEstadosCuenta.forEach(x -> {

                if (x.getConcepto() == null) {
                    BancosConceptosDto bcDto = new BancosConceptosDto();
                    bcDto.setCbanId(x.getEcConcepto());
                    bcDto.setCbanNombre(x.getStrConcepto());
                    x.setConcepto(bcDto);
                }

                switch (x.getEcAjustado()) {
                    case 1:
                        x.setColor(Constantes.COL_AJUSTADO);
                        break;
                    case 2:
                        x.setColor(Constantes.COL_AJUSTADO_PARCIAL);
                        break;
                }

            });

            lstBancos.forEach(x -> {

                switch (x.getBanAjustado()) {
                    case 1:
                        x.setColor(Constantes.COL_AJUSTADO);
                        break;
                    case 2:
                        x.setColor(Constantes.COL_AJUSTADO_PARCIAL);
                        break;
                }

            });

        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public BancosDto creaBancoDto(BancosEstadoCtaDto becDto) {
        BancosDto banco = new BancosDto();

        banco.setBanAjustado(becDto.getBanAjustado());
        banco.setBanConcepto(becDto.getBanConcepto());
        banco.setBanEmpresa(becDto.getBanEmpresa());
        banco.setBanFechatransaccion(becDto.getBanFechatransaccion());
        banco.setBanId(becDto.getBanId());
        banco.setBanIdConceptoSistema(becDto.getBanIdConceptoSistema());
        banco.setBanMonto(becDto.getBanMonto() != null ? becDto.getBanMonto() : 0.0);
        banco.setConcepto(becDto.getConceptoBanco());
        banco.setBanEmpresaStri(becDto.getBanEmpresaStri());

        return banco;
    }

    public EstadoCuentaDto creaEdoCtaDto(BancosEstadoCtaDto becDto) {
        EstadoCuentaDto edoCta = new EstadoCuentaDto();

        BancosConceptosDto bcDto = new BancosConceptosDto();
        bcDto.setCbanId(becDto.getEcConcepto());
        bcDto.setCbanNombre(becDto.getConceptoEdoCta());

        edoCta.setConcepto(bcDto);
        edoCta.setEcAjustado(becDto.getEcAjustado());
        edoCta.setEcConcepto(becDto.getEcConcepto());
        edoCta.setEcDescripcion(becDto.getEcDescripcion());
        edoCta.setEcEmpresa(becDto.getEcEmpresa());
        edoCta.setEcFechatransaccion(becDto.getEcFechatransaccion());
        edoCta.setEcId(becDto.getEcId());
        edoCta.setEcMonto(becDto.getEcMonto() != null ? becDto.getEcMonto() : 0.0);
        edoCta.setEcPadre(becDto.getEcPadre());
        edoCta.setEdoCtaEmpresa(becDto.getEdoCtaEmpresa());

        return edoCta;
    }

    /**
     * Elimina los repetidos de las listas que se mostrarán en pantalla
     *
     * @param lstBan
     * @return
     */
    public List<BigInteger> obtieneIdsRelacion(List<BancosDto> lstBan) {
        List<BigInteger> lstIds = new ArrayList<>();
        Map<Integer, BigInteger> mapIds = new HashMap<>(lstBan.size());

        lstBan.stream().forEach((BancosDto mban) -> {
            mapIds.put(mban.getRbeId().intValue(), mban.getRbeId());
        });

        mapIds.forEach((id, ban) -> {
            lstIds.add(ban);
        });

        return lstIds;
    }

    /**
     *
     * @return @throws BusinessException
     */
    public List<BancosConceptos> getConceptos() throws BusinessException {
        try {
            return banDao.getConceptos();
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que guarda los registros necesarios en la tabla banco_edocta y
     * actualiza las tablas Bancos y Estado_cuenta con un nuevo estatus
     *
     * @param lstBan
     * @param lstEc
     * @throws BusinessException
     */
    public void persistRelBancoEC(List<BancosDto> lstBan, List<EstadoCuentaDto> lstEc) throws BusinessException {
        try {
            Long rbeId = Util.genUUID();

            for (BancosDto ban : lstBan) {

                ban.setRbeId(new BigInteger(rbeId.toString()));
                ban.setRbeFechaRel(new Date());
                banDao.updtBanco(transDtoToPojoBan(ban));

            }

            for (EstadoCuentaDto ec : lstEc) {

                ec.setRbeId(new BigInteger(rbeId.toString()));
                ec.setRbeFechaRel(new Date());
                banDao.updtEstadoCta(transDtoToPojoEC(ec));
            }

        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    private Bancos transDtoToPojoBan(BancosDto dto) {
        Bancos pojo = new Bancos();
        pojo.setBanAjustado(dto.getBanAjustado());
        pojo.setBanConcepto(dto.getBanConcepto());
        pojo.setBanEmpresa(dto.getBanEmpresa());
        pojo.setBanFechatransaccion(dto.getBanFechatransaccion());
        pojo.setBanId(dto.getBanId());
        pojo.setBanIdConceptoSistema(dto.getBanIdConceptoSistema());
        pojo.setBanMonto(dto.getBanMonto());
        pojo.setBanIdRelacion(dto.getRbeId().longValue());
        pojo.setBanFechaRelacion(dto.getRbeFechaRel());

        return pojo;
    }

    private EstadoCuenta transDtoToPojoEC(EstadoCuentaDto dto) {
        EstadoCuenta pojo = new EstadoCuenta();
        
        dto.setEcConcepto(dto.getBanConceptoId());
        pojo.setEcAjustado(dto.getEcAjustado());
        pojo.setEcConcepto(dto.getEcConcepto());
        pojo.setEcDescripcion(dto.getEcDescripcion());
        pojo.setEcEmpresa(dto.getEcEmpresa());
        pojo.setEcFechatransaccion(dto.getEcFechatransaccion());
        pojo.setEcId(dto.getEcId());
        pojo.setEcMonto(dto.getEcMonto());
        pojo.setEcPadre(dto.getEcPadre());
        pojo.setEcIdRelacion(dto.getRbeId().longValue());
        pojo.setEcFechaRelacion(dto.getRbeFechaRel());

        return pojo;
    }

    /**
     * Metodo que elimina una relacion y quita el ajuste
     *
     * @param idRelacion
     */
    public void borraRelacion(BigInteger idRelacion) throws IntegracionException {

        banDao.quitarAjusteBanco(idRelacion);
        banDao.quitarAjusteEc(idRelacion);

    }

   
    public void updtEstadoCuenta(EstadoCuentaDto dto) throws BusinessException {
        EstadoCuenta pojo = this.transDtoToPojoEC(dto);

        try {
            banDao.updtEstadoCta(pojo);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    public void eliminaEC(EstadoCuentaDto ec) throws BusinessException {
        try {
            banDao.eliminaEC(ec);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

    /**
     * @return the ajustadosOriginal
     */
    public List<BancosEstadoCtaDto> getAjustadosOriginal() {
        return ajustadosOriginal;
    }

    /**
     * @param ajustadosOriginal the ajustadosOriginal to set
     */
    public void setAjustadosOriginal(List<BancosEstadoCtaDto> ajustadosOriginal) {
        this.ajustadosOriginal = ajustadosOriginal;
    }

    /**
     * @return the lstBancos
     */
    public List<BancosDto> getLstBancos() {
        return lstBancos;
    }

    /**
     * @param lstBancos the lstBancos to set
     */
    public void setLstBancos(List<BancosDto> lstBancos) {
        this.lstBancos = lstBancos;
    }

    /**
     * @return the lstEstadosCuenta
     */
    public List<EstadoCuentaDto> getLstEstadosCuenta() {
        return lstEstadosCuenta;
    }

    /**
     * @param lstEstadosCuenta the lstEstadosCuenta to set
     */
    public void setLstEstadosCuenta(List<EstadoCuentaDto> lstEstadosCuenta) {
        this.lstEstadosCuenta = lstEstadosCuenta;
    }

    /**
     * @return the totalBanco
     */
    public Double getTotalBanco() {
        return totalBanco;
    }

    /**
     * @param totalBanco the totalBanco to set
     */
    public void setTotalBanco(Double totalBanco) {
        this.totalBanco = totalBanco;
    }

    /**
     * @return the totalEdoCta
     */
    public Double getTotalEdoCta() {
        return totalEdoCta;
    }

    /**
     * @param totalEdoCta the totalEdoCta to set
     */
    public void setTotalEdoCta(Double totalEdoCta) {
        this.totalEdoCta = totalEdoCta;
    }

    /**
     * Guarda en base de datos un dto de estado de cuenta
     *
     * @param nuevoEdoCta
     * @throws BusinessException
     */
    public void guardaEdoCta(EstadoCuentaDto nuevoEdoCta) throws BusinessException {
        try {
            this.banDao.saveEstadoCuenta(trfmDtoToPojo(nuevoEdoCta));
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    public EstadoCuenta trfmDtoToPojo(EstadoCuentaDto dto) {
        EstadoCuenta pojo = new EstadoCuenta();

        pojo.setEcAjustado(dto.getEcAjustado());
        pojo.setEcConcepto(dto.getBanConceptoId());
        pojo.setEcDescripcion(dto.getEcDescripcion());
        pojo.setEcEmpresa(dto.getEcEmpresa());
        pojo.setEcFechatransaccion(dto.getEcFechatransaccion());
        pojo.setEcMonto(dto.getEcMonto());

        return pojo;

    }

    /**
     * @return the lstBorraBanco
     */
    public List<BancosEstadoCtaDto> getLstBorraBanco() {
        return lstBorraBanco;
    }

    /**
     * @param lstBorraBanco the lstBorraBanco to set
     */
    public void setLstBorraBanco(List<BancosEstadoCtaDto> lstBorraBanco) {
        this.lstBorraBanco = lstBorraBanco;
    }

    /**
     * @return the lstBorraEC
     */
    public List<BancosEstadoCtaDto> getLstBorraEC() {
        return lstBorraEC;
    }

    /**
     * @param lstBorraEC the lstBorraEC to set
     */
    public void setLstBorraEC(List<BancosEstadoCtaDto> lstBorraEC) {
        this.lstBorraEC = lstBorraEC;
    }

    /**
     * Metodo que le asigna el id de la relacion a los registros que no estan
     * ajustados de la tabla
     *
     * @param idRelacion
     * @param banAuxLstSel
     * @param ecAuxLstSel
     * @throws mx.com.evoti.bo.exception.BusinessException
     */
    public void asignaRelacionNoAjustados(BigInteger idRelacion, List<BancosDto> banAuxLstSel, List<EstadoCuentaDto> ecAuxLstSel) throws BusinessException {
        try {
            for (BancosDto ban : banAuxLstSel) {
                this.banDao.updtBancoRelacion(idRelacion.longValue(), ban.getBanId());
            }

            for (EstadoCuentaDto ec : ecAuxLstSel) {
                this.banDao.updtECRelacion(idRelacion.longValue(), ec.getEcId());
            }
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }

    }

    /**
     * Actualiza el estatus de ajustado en todos los elementos de una relacion:
     * Bancos y estados de cuenta
     *
     * @param idRelacion
     * @param ajustadoBan
     * @param ajustadoEc
     * @throws BusinessException
     */
    public void updtAjustado(Long idRelacion, int ajustadoBan, int ajustadoEc) throws BusinessException {

        try {
            banDao.updtAjusteBanco(idRelacion, ajustadoBan);
            banDao.updtAjusteEdoCta(idRelacion, ajustadoEc);
        } catch (IntegracionException ex) {
            throw new BusinessException(ex.getMessage(), ex);
        }
    }

}
