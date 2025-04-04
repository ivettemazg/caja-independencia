/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.presentacion.banco;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import mx.com.evoti.bo.bancos.BancoAjustesBo;
import mx.com.evoti.bo.exception.BusinessException;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.dto.bancos.BancosDto;
import mx.com.evoti.dto.bancos.EstadoCuentaDto;
import mx.com.evoti.hibernate.pojos.BancosConceptos;
import mx.com.evoti.presentacion.BaseBean;
import mx.com.evoti.util.Constantes;
import mx.com.evoti.util.Util;
import org.primefaces.event.RowEditEvent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
@ManagedBean(name = "bancoAjusteBean")
@ViewScoped
public class BancoAjusteBean extends BaseBean implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BancoAjusteBean.class);
    private static final long serialVersionUID = 5188478880986645102L;

    private BancoAjustesBo banAjBo;

    private List<BancosDto> conceptsSistem;
    private List<BancosDto> sistemSel;
    private List<BancosDto> bancoFiltradas;
    private List<EstadoCuentaDto> conceptEdoCta;
    private List<EstadoCuentaDto> edoCtaSel;
    private List<EstadoCuentaDto> edoCtaFiltradas;
    private EstadoCuentaDto nuevoEdoCta;

    private List<BancosConceptos> conceptos;

    private Date fechaInicio;
    private Date fechaFin;

    private Double totalEdoCta;
    private Double totalBanco;

    public BancoAjusteBean() {
        banAjBo = new BancoAjustesBo();
        getConceptosPantalla();
    }

    public void init() {

    }

    public void cargaReporte() {
        try {
           
            banAjBo.obtieneListasBanco(fechaInicio, fechaFin);

            conceptsSistem = banAjBo.getLstBancos();
            conceptEdoCta = banAjBo.getLstEstadosCuenta();
            sistemSel.clear();
            edoCtaSel.clear();
            
            this.totalBanco = 0.0;
            this.totalEdoCta = 0.0;

            conceptsSistem.forEach(x -> {
                this.totalBanco += x.getBanMonto();
                
            });
            conceptEdoCta.forEach(x -> {
                this.totalEdoCta += x.getEcMonto();
            });

            this.totalBanco = Util.round(totalBanco);
            this.totalEdoCta = Util.round(totalEdoCta);

        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Metodo que actualiza el pojo de EstadoCuenta, este metodo se llama cuando
     * se actualiza alguno de los valores del lado de estado de cuenta
     *
     * @param event
     */
    public void onRowEditEC(RowEditEvent event) {
        LOGGER.info("Dentro de onroweditec");

        EstadoCuentaDto dto = (EstadoCuentaDto) event.getObject();

        if (dto.getEcAjustado() == 0) {

            try {
                banAjBo.updtEstadoCuenta(dto);
            } catch (BusinessException ex) {
                super.muestraMensajeError("Hubo un error al actualizar el campo", "", null);
                LOGGER.error(ex.getMessage(), ex);
            }

        } else {
            super.muestraMensajeError("No puede actualizar un campo que ya está ajustado", "", null);
        }
        super.updtComponent("frmAjusteBanco:tblEC");

    }

    public void eliminaConcepto() {
        if (this.edoCtaSel.isEmpty()) {
            super.muestraMensajeError("Debe seleccionar un registro para borrar", "", null);
        } else {

            EstadoCuentaDto ecDto;

            edoCtaSel.forEach(ec -> {

                if (ec.getEcAjustado() == 0) {

                    try {
                        banAjBo.eliminaEC(ec);
                        super.muestraMensajeExito("El concepto fue eliminado", "", null);
                    } catch (BusinessException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }

                    
                } else {
                    super.muestraMensajeError("No puede eliminar un concepto que ya está ajustado", "", null);
                }
            });
            
            cargaReporte();
        }

    }

    public void rowECSelect() {

    }

    public void rowECUnselect() {

    }

    public void agregaConceptoEdoCta() {
        try {
            banAjBo.guardaEdoCta(nuevoEdoCta);

            cargaReporte();
            super.hideShowDlg("PF('dlgAddConceptW').hide()");
        } catch (BusinessException ex) {
            super.muestraMensajeError("Hubo un error al guardar el concepto", "", null);
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void getConceptosPantalla() {
        try {
            conceptos = banAjBo.getConceptos();
        } catch (BusinessException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void initDlgAddCncp() {
        this.nuevoEdoCta = new EstadoCuentaDto();
        nuevoEdoCta.setEcAjustado(Constantes.EC_NO_AJUSTADO);

        super.hideShowDlg("PF('dlgAddConceptW').show()");
        super.updtComponent("dlgFrmAddConcept");

    }

    /**
     * Metodo que se manda llamar al ejecutar el metodo Ajustar conceptos
     */
    public void ajustaConceptos() {
        LOGGER.info("Dentro de ajusta conceptos");

     
        //Valida que haya registros en ambas listas
        if (sistemSel != null && edoCtaSel != null) {

            /**
             * Valida que alguna de las dos listas tenga un solo registro y que
             * ambas tengan registros
             */
            if (sistemSel.size() > 0 && edoCtaSel.size() > 0) {

                boolean bAjustado = false;
                boolean ecAjustado = false;
                BancosDto bancoAjustado = null;
                EstadoCuentaDto edoctaAjustado = null;
                for (BancosDto bs : sistemSel) {
                    if (bs.getBanAjustado() != 0) {
                        bAjustado = true;
                        bancoAjustado = bs;
                    }
                }
                for (EstadoCuentaDto ecs : edoCtaSel) {
                    if (ecs.getEcAjustado() != 0) {
                        ecAjustado = true;
                        edoctaAjustado = ecs;
                    }
                }

                /*
                * Cuando alguno de los conceptos seleccionados ya fue ajustado
                 */
                if (bAjustado || ecAjustado) {

                    BigInteger idRelacion = new BigInteger(Util.genUUID().toString());

                    if (bancoAjustado != null) {
                        idRelacion = bancoAjustado.getRbeId();
                    } else if (edoctaAjustado != null) {
                        idRelacion = edoctaAjustado.getRbeId();
                    }

                    /*
                     * Obtiene todos los elementos de la relacion que se selecciono y va
                     * sumando los montos
                     */
                    List<BancosDto> banAuxLstSel = new ArrayList<>();
                    List<EstadoCuentaDto> ecAuxLstSel = new ArrayList<>();
                    Double sumaBanco = 0.0;
                    Double sumaEc = 0.0;

                    for (BancosDto cs : conceptsSistem) {
                        if (idRelacion.equals(cs.getRbeId())) {

                            sumaBanco += Util.round(cs.getBanMonto());
                        }
                    }

                    for (EstadoCuentaDto cs : conceptEdoCta) {
                        if (idRelacion.equals(cs.getRbeId())) {

                            sumaEc += Util.round(cs.getEcMonto());
                        }
                    }

                    /*
                     * Obtiene los elementos seleccionados que tienen estatus ajustado 0 
                     * y va sumando los montos
                     */
                    for (EstadoCuentaDto cs : edoCtaSel) {
                        if (cs.getEcAjustado().equals(Constantes.EC_NO_AJUSTADO)) {
                            ecAuxLstSel.add(cs);
                            sumaEc += Util.round(cs.getEcMonto());
                        }
                    }

                    for (BancosDto b : sistemSel) {
                        if (b.getBanAjustado().equals(Constantes.EC_NO_AJUSTADO)) {
                            banAuxLstSel.add(b);
                            sumaBanco += Util.round(b.getBanMonto());
                        }
                    }

                    try {
                        /**
                         * Actualiza el id de relacion en los elementos que
                         * tienen ajustado = 0
                         */
                        banAjBo.asignaRelacionNoAjustados(idRelacion, banAuxLstSel, ecAuxLstSel);

                        /**
                         * Actualiza los estatus de ajustado en ambas listas
                         */
                        int ajustadoBan = 0;
                        int ajustadoEc = 0;

                         if (sumaEc - sumaBanco < -1) {
                            ajustadoBan = Constantes.BAN_AJUSTADO_PARCIAL;
                            ajustadoEc = Constantes.EC_AJUSTADO;
                        } else if (sumaEc - sumaBanco > 1) {
                            ajustadoBan = Constantes.BAN_AJUSTADO;
                            ajustadoEc = Constantes.EC_AJUSTADO_PARCIAL;
                        } else if (sumaEc - sumaBanco >= -1 && sumaEc - sumaBanco <= 1) {
                            ajustadoBan = Constantes.BAN_AJUSTADO;
                            ajustadoEc = Constantes.EC_AJUSTADO;
                        }
                        

                        banAjBo.updtAjustado(idRelacion.longValue(), ajustadoBan, ajustadoEc);
                        cargaReporte();
                    } catch (BusinessException ex) {
                        LOGGER.error("Error al actualizar el id de relacion en los campos seleccionados sin ajustar", ex);
                        super.muestraMensajeError("Hubo un error al Ajustar los conceptos", "", null);
                    }

                } else {
                    try {
                        //Cuando ningun concepto ha sido ajustado anteriormente

                        Double sumaBanco = 0.0;
                        Double sumaEc = 0.0;
                        BigInteger idRelacion = new BigInteger(Util.genUUID().toString());
                        /**
                         * Actualiza el id de relacion en los elementos que
                         * se seleccionaron de la tabla
                         */
                        banAjBo.asignaRelacionNoAjustados(idRelacion, sistemSel, edoCtaSel);

                       /*
                        * Obtiene los elementos seleccionados que tienen estatus ajustado 0 
                        * y va sumando los montos
                        */
                        for (EstadoCuentaDto cs : edoCtaSel) {
                            sumaEc += Util.round(cs.getEcMonto());
                        }

                        for (BancosDto b : sistemSel) {
                            sumaBanco += Util.round(b.getBanMonto());
                        }

                        /**
                         * Actualiza los estatus de ajustado en ambas listas
                         */
                        int ajustadoBan = 0;
                        int ajustadoEc = 0;

                        if (sumaEc - sumaBanco < -1) {
                            ajustadoBan = Constantes.BAN_AJUSTADO_PARCIAL;
                            ajustadoEc = Constantes.EC_AJUSTADO;
                        } else if (sumaEc - sumaBanco > 1) {
                            ajustadoBan = Constantes.BAN_AJUSTADO;
                            ajustadoEc = Constantes.EC_AJUSTADO_PARCIAL;
                        } else if (sumaEc - sumaBanco >= -1 && sumaEc - sumaBanco <= 1) {
                            ajustadoBan = Constantes.BAN_AJUSTADO;
                            ajustadoEc = Constantes.EC_AJUSTADO;
                        }

                        banAjBo.updtAjustado(idRelacion.longValue(), ajustadoBan, ajustadoEc);
                        cargaReporte();
                    } catch (BusinessException ex) {
                        Logger.getLogger(BancoAjusteBean.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            } else {
                super.muestraMensajeError("No puede ajustar los campos seleccionados", "", null);
            }
            this.sistemSel.clear();
            this.edoCtaSel.clear();
        } else {
            super.muestraMensajeError("Debe seleccionar los conceptos que quiere relacionar", "", null);
        }
    }

    /**
     * Método que quita el ajuste de
     */
    public void quitarAjuste() {

        if ((sistemSel.size() > 0 && edoCtaSel.size() > 0) || sistemSel.size() > 1 || edoCtaSel.size() > 1) {
            super.muestraMensajeError("Solo puede seleccionar un registro para desajustar", "", null);

        } else {
            BigInteger idRelacion = new BigInteger("0");
            if(sistemSel.size() > 0){
                idRelacion = sistemSel.get(0).getRbeId();
            }else if(edoCtaSel.size() > 0){
                idRelacion = edoCtaSel.get(0).getRbeId();
                
            }
            
            if(idRelacion.longValue() != 0){
                try {
                    banAjBo.borraRelacion(idRelacion);
                } catch (IntegracionException ex) {
                   super.muestraMensajeError("Error al borrar la relación", "", null);
                   LOGGER.error(ex.getMessage(), ex);
                }
            }else{
                LOGGER.info("No se borro nada, idRelacion = 0");
            }
        }

        cargaReporte();

    }

    /**
     * Metodo que calcula los totales de las summary rows
     *
     * @param ajustado
     */
    public void calcTotalEC(BigInteger ajustado) {

        totalEdoCta = 0.0;
        if (edoCtaFiltradas != null) {
            if (!edoCtaFiltradas.isEmpty()) {
                edoCtaFiltradas.forEach(ec -> {

                    if (Objects.equals(ajustado, ec.getRbeId())) {
                        totalEdoCta += Util.round(ec.getEcMonto());
                    }

                });
            }
        } else {

            conceptEdoCta.forEach(ec -> {

                if (Objects.equals(ajustado, ec.getRbeId())) {
                    totalEdoCta += Util.round(ec.getEcMonto());
                }

            });
        }
    }

    public void calcTotalBan(BigInteger ajustado) {

        totalBanco = 0.0;

        if (this.bancoFiltradas != null) {
            if (!bancoFiltradas.isEmpty()) {
                bancoFiltradas.forEach(ban -> {

                    if (Objects.equals(ajustado, ban.getRbeId())) {
                        totalEdoCta += Util.round(ban.getBanMonto());
                    }

                });
            }
        } else {
            conceptsSistem.forEach(ban -> {

                if (Objects.equals(ajustado, ban.getRbeId())) {
                    totalBanco += Util.round(ban.getBanMonto());
                }

            });
        }
    }

    /**
     * @return the conceptsSistem
     */
    public List<BancosDto> getConceptsSistem() {
        return conceptsSistem;
    }

    /**
     * @param conceptsSistem the conceptsSistem to set
     */
    public void setConceptsSistem(List<BancosDto> conceptsSistem) {
        this.conceptsSistem = conceptsSistem;
    }

    /**
     * @return the sistemSel
     */
    public List<BancosDto> getSistemSel() {
        return sistemSel;
    }

    /**
     * @param sistemSel the sistemSel to set
     */
    public void setSistemSel(List<BancosDto> sistemSel) {
        this.sistemSel = sistemSel;
    }

    /**
     * @return the edoCtaSel
     */
    public List<EstadoCuentaDto> getEdoCtaSel() {
        return edoCtaSel;
    }

    /**
     * @param edoCtaSel the edoCtaSel to set
     */
    public void setEdoCtaSel(List<EstadoCuentaDto> edoCtaSel) {
        this.edoCtaSel = edoCtaSel;
    }

    /**
     * @return the conceptEdoCta
     */
    public List<EstadoCuentaDto> getConceptEdoCta() {
        return conceptEdoCta;
    }

    /**
     * @param conceptEdoCta the conceptEdoCta to set
     */
    public void setConceptEdoCta(List<EstadoCuentaDto> conceptEdoCta) {
        this.conceptEdoCta = conceptEdoCta;
    }

    /**
     * @return the fechaInicio
     */
    public Date getFechaInicio() {
        return fechaInicio;
    }

    /**
     * @param fechaInicio the fechaInicio to set
     */
    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /**
     * @return the fechaFin
     */
    public Date getFechaFin() {
        return fechaFin;
    }

    /**
     * @param fechaFin the fechaFin to set
     */
    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
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
     * @return the conceptos
     */
    public List<BancosConceptos> getConceptos() {
        return conceptos;
    }

    /**
     * @param conceptos the conceptos to set
     */
    public void setConceptos(List<BancosConceptos> conceptos) {
        this.conceptos = conceptos;
    }

    /**
     * @return the nuevoEdoCta
     */
    public EstadoCuentaDto getNuevoEdoCta() {
        return nuevoEdoCta;
    }

    /**
     * @param nuevoEdoCta the nuevoEdoCta to set
     */
    public void setNuevoEdoCta(EstadoCuentaDto nuevoEdoCta) {
        this.nuevoEdoCta = nuevoEdoCta;
    }

    /**
     * @return the bancoFiltradas
     */
    public List<BancosDto> getBancoFiltradas() {
        return bancoFiltradas;
    }

    /**
     * @param bancoFiltradas the bancoFiltradas to set
     */
    public void setBancoFiltradas(List<BancosDto> bancoFiltradas) {
        this.bancoFiltradas = bancoFiltradas;
    }

    /**
     * @return the edoCtaFiltradas
     */
    public List<EstadoCuentaDto> getEdoCtaFiltradas() {
        return edoCtaFiltradas;
    }

    /**
     * @param edoCtaFiltradas the edoCtaFiltradas to set
     */
    public void setEdoCtaFiltradas(List<EstadoCuentaDto> edoCtaFiltradas) {
        this.edoCtaFiltradas = edoCtaFiltradas;
    }

}
