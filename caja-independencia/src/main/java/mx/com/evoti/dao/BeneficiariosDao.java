/*
 * To change super license header, choose License Headers in Project Properties.
 * To change super template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.dao;

import java.io.Serializable;
import java.util.List;
import mx.com.evoti.dao.exception.IntegracionException;
import mx.com.evoti.hibernate.pojos.Beneficiarios;
import mx.com.evoti.hibernate.pojos.ParentescoBen;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Venette
 */
public class BeneficiariosDao extends ManagerDB implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BeneficiariosDao.class);
    private static final long serialVersionUID = 8937799280672155208L;

    /**
     * Obtiene los beneficiarios de un usuario en especifico
     *
     * @param idEmpleado
     * @return
     * @throws IntegracionException
     */
    public List<Beneficiarios> getBeneficiariosByEmpleado(Integer idEmpleado) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    String.format(
                            "from Beneficiarios where benUsuId = %1$s ",
                            idEmpleado));

            List<Beneficiarios> beneficiarios = query.list();

            return beneficiarios;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Guarda o actualiza un beneficiario
     *
     * @param beneficiario
     * @return
     * @throws IntegracionException
     */
    public Beneficiarios saveOrUpdate(Beneficiarios beneficiario) throws IntegracionException {
        super.beginTransaction();

        try {
            super.session.saveOrUpdate(beneficiario);
            super.session.getTransaction().commit();
            
            LOGGER.debug("El beneficiario");
            return beneficiario;
        } catch (HibernateException ex) {
            super.session.getTransaction().rollback();
            throw new IntegracionException(ex.getMessage() + " No se pudo guardar el beneficiario", ex);
        } finally {
            super.endTransaction();

        }

    }

    /**
     * Borra un beneficiario de la base de datos
     *
     * @param idBeneficiario
     * @throws mx.com.evoti.dao.exception.IntegracionException
     */
    public void borraBeneficiario(Integer idBeneficiario) throws IntegracionException {
        String hql = String.format("delete Beneficiarios "
                + "where benId = %1$s ", idBeneficiario);

        super.executeUpdate(hql);
    }

    /**
     * Obtiene la lista de parentescos de la base de datos
     *
     * @return
     * @throws IntegracionException
     */
    public List<ParentescoBen> getParentescos() throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(
                    "from ParentescoBen ");

            List<ParentescoBen> parentescos = query.list();

            return parentescos;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

    /**
     * Obtiene un parentesco por id
     *
     * @param idParentesco
     * @return
     * @throws IntegracionException
     */
    public ParentescoBen getParentescoById(Integer idParentesco) throws IntegracionException {
        try {
            super.beginTransaction();

            Query query = session.createQuery(String.format(
                    "from ParentescoBen where parId = %1$s ", idParentesco));

            ParentescoBen parentesco = (ParentescoBen) query.uniqueResult();

            return parentesco;
        } catch (HibernateException he) {
            throw new IntegracionException(he);
        } finally {
            super.endTransaction();
        }
    }

}
