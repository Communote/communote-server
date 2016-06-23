package com.communote.server.persistence.user;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.Contact</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.Contact
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ContactDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.ContactDao {

    /**
     * @see com.communote.server.persistence.user.ContactDao#create(com.communote.server.model.user.Contact)
     */
    public com.communote.server.model.user.Contact create(
            com.communote.server.model.user.Contact contact) {
        return (com.communote.server.model.user.Contact) this.create(TRANSFORM_NONE, contact);
    }

    /**
     * @see com.communote.server.persistence.user.ContactDao#create(int transform,
     *      com.communote.server.persistence.user.Contact)
     */
    public Object create(final int transform, final com.communote.server.model.user.Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact.create - 'contact' can not be null");
        }
        this.getHibernateTemplate().save(contact);
        return this.transformEntity(transform, contact);
    }

    /**
     * @see com.communote.server.persistence.user.ContactDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.Contact>)
     */
    public java.util.Collection<com.communote.server.model.user.Contact> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.Contact> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Contact.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.Contact>() {
                            public com.communote.server.model.user.Contact doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.Contact> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.ContactDao#create(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.Contact>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.Contact> create(
            final java.util.Collection<com.communote.server.model.user.Contact> entities) {
        return (java.util.Collection<com.communote.server.model.user.Contact>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.Contact entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.ContactDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Contact.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.ContactImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.user.Contact) entity);
    }

    /**
     * @see com.communote.server.persistence.user.ContactDao#load(Long)
     */
    public com.communote.server.model.user.Contact load(Long id) {
        return (com.communote.server.model.user.Contact) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.ContactDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.Contact> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.Contact>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.ContactDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.ContactImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.ContactDao#remove(com.communote.server.model.user.Contact)
     */
    public void remove(com.communote.server.model.user.Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact.remove - 'contact' can not be null");
        }
        this.getHibernateTemplate().delete(contact);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.ContactDao#remove(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.Contact>)
     */
    public void remove(java.util.Collection<com.communote.server.model.user.Contact> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Contact.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.ContactDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Contact.remove - 'id' can not be null");
        }
        com.communote.server.model.user.Contact entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.Contact)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.ContactDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.Contact)
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
     * <code>com.communote.server.persistence.user.ContactDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.ContactDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.Contact entity) {
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
     * @see com.communote.server.persistence.user.ContactDao#update(com.communote.server.model.user.Contact)
     */
    public void update(com.communote.server.model.user.Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact.update - 'contact' can not be null");
        }
        this.getHibernateTemplate().update(contact);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.ContactDao#update(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.Contact>)
     */
    public void update(final java.util.Collection<com.communote.server.model.user.Contact> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Contact.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.Contact>() {
                            public com.communote.server.model.user.Contact doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.Contact> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}