package com.communote.server.persistence.hibernate;

import java.io.Serializable;
import java.util.Collection;

/**
 * Generic Interface for a Dao for general Hibernate support .
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            Type of the entity this dao is for.
 */
public interface HibernateDao<T extends Serializable> {

    /**
     * Loads all entities.
     * 
     * @return All entities for this type.
     */
    public Collection<T> loadAll();

    /**
     * Loads the requested entity.
     * 
     * @param id
     *            Id of the entity to load.
     * @return The entity or null, if not existent.
     */
    public T load(Long id);

    /**
     * Creates a new entity within the database.
     * 
     * @param entity
     *            The entity to create.
     * @return The database entity.
     */
    public T create(final T entity);

    /**
     * Creates all given entities within the database.
     * 
     * @param entities
     *            The entities to create.
     * @return The entities connected with the database.
     */
    public Collection<T> create(final Collection<T> entities);

    /**
     * Updates the given entity.
     * 
     * @param entity
     *            The entity to update.
     */
    public void update(T entity);

    /**
     * Updates all given entities.
     * 
     * @param entities
     *            The entities to update.
     */
    public void update(final Collection<T> entities);

    /**
     * Removes the given entity.
     * 
     * @param entity
     *            The entity to remove.
     */
    public void remove(T entity);

    /**
     * Removes the given entity.
     * 
     * @param id
     *            Id of the entity to remove.
     */
    public void remove(Long id);

    /**
     * Removes all given entities.
     * 
     * @param entities
     *            Collection of entities to remove.
     */
    public void remove(Collection<T> entities);

    /**
     * See HibernateTemplate#evit
     * 
     * @param entity
     *            The entity to evict.
     */
    public void evict(T entity);
}
