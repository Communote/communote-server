package com.communote.server.persistence.blog;

import com.communote.server.model.note.NoteProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>NoteProperty</code>.
 * </p>
 *
 * @see NoteProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class NotePropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements NotePropertyDao {

    /**
     * @see NotePropertyDao#create(int, java.util.Collection<NoteProperty>)
     */
    @Override
    public java.util.Collection<NoteProperty> create(final int transform,
            final java.util.Collection<NoteProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("NoteProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<NoteProperty>() {
                    @Override
                    public NoteProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<NoteProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see NotePropertyDao#create(int transform, NoteProperty)
     */
    @Override
    public Object create(final int transform, final NoteProperty noteProperty) {
        if (noteProperty == null) {
            throw new IllegalArgumentException(
                    "NoteProperty.create - 'noteProperty' can not be null");
        }
        this.getHibernateTemplate().save(noteProperty);
        return this.transformEntity(transform, noteProperty);
    }

    /**
     * @see NotePropertyDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.NoteProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<NoteProperty> create(
            final java.util.Collection<NoteProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see NotePropertyDao#create(NoteProperty)
     */
    @Override
    public NoteProperty create(NoteProperty noteProperty) {
        return (NoteProperty) this.create(TRANSFORM_NONE, noteProperty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(NoteProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see NotePropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("NoteProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(NoteProperty.class, id);
        return transformEntity(transform, (NoteProperty) entity);
    }

    /**
     * @see NotePropertyDao#load(Long)
     */
    @Override
    public NoteProperty load(Long id) {
        return (NoteProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see NotePropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<NoteProperty> loadAll() {
        return (java.util.Collection<NoteProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see NotePropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                NoteProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see NotePropertyDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.NoteProperty>)
     */
    @Override
    public void remove(java.util.Collection<NoteProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("NoteProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see NotePropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("NoteProperty.remove - 'id' can not be null");
        }
        NoteProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see NotePropertyDao#remove(NoteProperty)
     */
    @Override
    public void remove(NoteProperty noteProperty) {
        if (noteProperty == null) {
            throw new IllegalArgumentException(
                    "NoteProperty.remove - 'noteProperty' can not be null");
        }
        this.getHibernateTemplate().delete(noteProperty);
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,NoteProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>NotePropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,NoteProperty)
     */
    protected void transformEntities(final int transform, final java.util.Collection<?> entities) {
        switch (transform) {
        case TRANSFORM_NONE: // fall-through
        default:
            // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter),
     * when the <code>transform</code> flag is set to one of the constants defined in
     * <code>NotePropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link NotePropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final NoteProperty entity) {
        Object target = null;
        if (entity != null) {
            switch (transform) {
            case TRANSFORM_NONE: // fall-through
            default:
                target = entity;
            }
        }
        return target;
    }

    /**
     * @see NotePropertyDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.NoteProperty>)
     */
    @Override
    public void update(final java.util.Collection<NoteProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("NoteProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<NoteProperty>() {
                    @Override
                    public NoteProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<NoteProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see NotePropertyDao#update(NoteProperty)
     */
    @Override
    public void update(NoteProperty noteProperty) {
        if (noteProperty == null) {
            throw new IllegalArgumentException(
                    "NoteProperty.update - 'noteProperty' can not be null");
        }
        this.getHibernateTemplate().update(noteProperty);
    }

}