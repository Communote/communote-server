package com.communote.server.persistence.hibernate;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generic Dao for general Hibernate support .
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            Type of the entity this dao is for.
 */
@Transactional
public abstract class HibernateDaoImpl<T extends Serializable> extends HibernateDaoSupport
        implements HibernateDao<T> {

    @Autowired
    private SessionFactory sessionFactory;

    private Class<? extends T> type;

    /**
     * Constructor.
     * 
     * @param type
     *            Type of the entity this dao is for.
     */
    public HibernateDaoImpl(Class<? extends T> type) {
        this.type = type;
    }

    /**
     * Method to finally initialize this class.
     */
    @PostConstruct
    public void postConstruct() {
        setSessionFactory(sessionFactory);
    }

    /**
     * Loads all entities.
     * 
     * @return All entities for this type.
     */
    @Transactional(readOnly = true)
    public Collection<T> loadAll() {
        return (Collection<T>) this.getHibernateTemplate().loadAll(type);
    }

    /**
     * Loads the requested entity.
     * 
     * @param id
     *            Id of the entity to load.
     * @return The entity or null, if not existent.
     */
    @Transactional(readOnly = true)
    public T load(Long id) {
        return this.getHibernateTemplate().get(type, id);
    }

    /**
     * Creates a new entity within the database.
     * 
     * @param entity
     *            The entity to create.
     * @return The database entity.
     */
    public T create(final T entity) {
        this.getHibernateTemplate().save(entity);
        return entity;
    }

    /**
     * Creates all given entities within the database.
     * 
     * @param entities
     *            The entities to create.
     * @return The entities connected with the database.
     */
    public Collection<T> create(final Collection<T> entities) {
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<T>()
                        {
                            public T doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<T> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * Updates the given entity.
     * 
     * @param entity
     *            The entity to update.
     */
    public void update(T entity) {
        this.getHibernateTemplate().update(entity);
    }

    /**
     * Updates all given entities.
     * 
     * @param entities
     *            The entities to update.
     */
    public void update(final Collection<T> entities) {
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<T>()
                        {
                            public T doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<T> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

    /**
     * Removes the given entity.
     * 
     * @param entity
     *            The entity to remove.
     */
    public void remove(T entity) {
        this.getHibernateTemplate().delete(entity);
    }

    /**
     * Removes the given entity.
     * 
     * @param id
     *            Id of the entity to remove.
     */
    public void remove(Long id) {
        T entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Removes all given entities.
     * 
     * @param entities
     *            Collection of entities to remove.
     */
    public void remove(Collection<T> entities) {
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * See HibernateTemplate#evit
     * 
     * @param entity
     *            The entity to evict.
     */
    public void evict(T entity) {
        this.getHibernateTemplate().evict(entity);
    }

}
