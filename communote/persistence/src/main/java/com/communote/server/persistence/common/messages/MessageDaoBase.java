package com.communote.server.persistence.common.messages;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>Message</code>.
 * </p>
 *
 * @see com.communote.server.model.i18n.Message
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class MessageDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.common.messages.MessageDao {

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#create(com.communote.server.model.i18n.Message)
     */
    @Override
    public com.communote.server.model.i18n.Message create(
            com.communote.server.model.i18n.Message message) {
        return (com.communote.server.model.i18n.Message) this.create(TRANSFORM_NONE, message);
    }

    @Override
    public Object create(final int transform, final com.communote.server.model.i18n.Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message.create - 'message' can not be null");
        }
        this.getHibernateTemplate().save(message);
        return this.transformEntity(transform, message);
    }

    @Override
    public java.util.Collection<com.communote.server.model.i18n.Message> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.i18n.Message> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Message.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
        .executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.i18n.Message>() {
                    @Override
                    public com.communote.server.model.i18n.Message doInHibernate(
                            org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                        for (java.util.Iterator<com.communote.server.model.i18n.Message> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
            }

    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.i18n.Message> create(
            final java.util.Collection<com.communote.server.model.i18n.Message> entities) {
        return create(TRANSFORM_NONE, entities);
            }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.i18n.Message entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#find(String)
     */
    @Override
    public java.util.Collection<com.communote.server.model.i18n.Message> find(final String key) {
        if (key == null) {
            throw new IllegalArgumentException(
                    "MessageDao.find(String key) - 'key' can not be null");
        }
        try {
            return this.handleFind(key);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'MessageDao.find(String key)' --> "
                            + rt, rt);
        }
            }

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#find(String, String)
     */
    @Override
    public com.communote.server.model.i18n.Message find(final String key, final String languageCode) {
        if (key == null) {
            throw new IllegalArgumentException(
                    "MessageDao.find(String key, String languageCode) - 'key' can not be null");
        }
        if (languageCode == null) {
            throw new IllegalArgumentException(
                    "MessageDao.find(String key, String languageCode) - 'languageCode' can not be null");
        }
        try {
            return this.handleFind(key, languageCode);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'MessageDao.find(String key, String languageCode)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#find(String, String, String)
     */
    @Override
    public com.communote.server.model.i18n.Message find(final String key,
            final String languageCode, final String fallbackLanguageCode) {
        if (key == null) {
            throw new IllegalArgumentException(
                    "MessageDao.find(String key, String languageCode, String fallbackLanguageCode) - 'key' can not be null");
        }
        if (languageCode == null) {
            throw new IllegalArgumentException(
                    "MessageDao.find(String key, String languageCode, String fallbackLanguageCode) - 'languageCode' can not be null");
        }
        if (fallbackLanguageCode == null) {
            throw new IllegalArgumentException(
                    "MessageDao.find(String key, String languageCode, String fallbackLanguageCode) - 'fallbackLanguageCode' can not be null");
        }
        try {
            return this.handleFind(key, languageCode, fallbackLanguageCode);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'MessageDao.find(String key, String languageCode, String fallbackLanguageCode)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #find(String)}
     */
    protected abstract java.util.Collection<com.communote.server.model.i18n.Message> handleFind(
            String key);

    /**
     * Performs the core logic for {@link #find(String, String)}
     */
    protected abstract com.communote.server.model.i18n.Message handleFind(String key,
            String languageCode);

    /**
     * Performs the core logic for {@link #find(String, String, String)}
     */
    protected abstract com.communote.server.model.i18n.Message handleFind(String key,
            String languageCode, String fallbackLanguageCode);

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Message.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.i18n.MessageImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.i18n.Message) entity);
    }

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#load(Long)
     */
    @Override
    public com.communote.server.model.i18n.Message load(Long id) {
        return (com.communote.server.model.i18n.Message) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.i18n.Message> loadAll() {
        return (java.util.Collection<com.communote.server.model.i18n.Message>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.i18n.MessageImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#remove(com.communote.server.model.i18n.Message)
     */
    @Override
    public void remove(com.communote.server.model.i18n.Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message.remove - 'message' can not be null");
        }
        this.getHibernateTemplate().delete(message);
    }

    @Override
    public void remove(java.util.Collection<com.communote.server.model.i18n.Message> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Message.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.common.messages.MessageDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Message.remove - 'id' can not be null");
        }
        com.communote.server.model.i18n.Message entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.i18n.Message)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>MessageDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.i18n.Message)
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
     * <code>MessageDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.common.messages.MessageDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.i18n.Message entity) {
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
     * @see com.communote.server.persistence.common.messages.MessageDao#update(com.communote.server.model.i18n.Message)
     */
    @Override
    public void update(com.communote.server.model.i18n.Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message.update - 'message' can not be null");
        }
        this.getHibernateTemplate().update(message);
    }

    @Override
    public void update(final java.util.Collection<com.communote.server.model.i18n.Message> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Message.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
        .executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.i18n.Message>() {
                    @Override
                    public com.communote.server.model.i18n.Message doInHibernate(
                            org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                        for (java.util.Iterator<com.communote.server.model.i18n.Message> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

}