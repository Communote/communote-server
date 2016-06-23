package com.communote.server.persistence.security.iprange;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.security.iprange.IpRangeChannel</code>.
 * </p>
 *
 * @see com.communote.server.model.security.IpRangeChannel
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class IpRangeChannelDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.security.iprange.IpRangeChannelDao {

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#create(com.communote.server.model.security.IpRangeChannel)
     */
    @Override
    public com.communote.server.model.security.IpRangeChannel create(
            com.communote.server.model.security.IpRangeChannel ipRangeChannel) {
        return (com.communote.server.model.security.IpRangeChannel) this.create(TRANSFORM_NONE,
                ipRangeChannel);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#create(int
     *      transform, com.communote.server.persistence.security.iprange.IpRangeChannel)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.security.IpRangeChannel ipRangeChannel) {
        if (ipRangeChannel == null) {
            throw new IllegalArgumentException(
                    "IpRangeChannel.create - 'ipRangeChannel' can not be null");
        }
        this.getHibernateTemplate().save(ipRangeChannel);
        return this.transformEntity(transform, ipRangeChannel);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.security.iprange.IpRangeChannel>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.security.IpRangeChannel> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.security.IpRangeChannel> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRangeChannel.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.IpRangeChannel>() {
                            @Override
                            public com.communote.server.model.security.IpRangeChannel doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.IpRangeChannel> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#create(java.util.
     *      Collection<com.communote.server.persistence.security.iprange.IpRangeChannel>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.IpRangeChannel> create(
            final java.util.Collection<com.communote.server.model.security.IpRangeChannel> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.security.IpRangeChannel entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#findEnabledExcludeRanges(com.communote.server.model.security.ChannelType)
     */
    @Override
    public java.util.List<com.communote.server.model.security.IpRange> findEnabledExcludeRanges(
            final com.communote.server.model.security.ChannelType channel) {
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.security.iprange.IpRangeChannelDao.findEnabledExcludeRanges(com.communote.server.persistence.security.ChannelType channel) - 'channel' can not be null");
        }
        try {
            return this.handleFindEnabledExcludeRanges(channel);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.security.iprange.IpRangeChannelDao.findEnabledExcludeRanges(com.communote.server.persistence.security.ChannelType channel)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#findEnabledIncludeRanges(com.communote.server.model.security.ChannelType)
     */
    @Override
    public java.util.List<com.communote.server.model.security.IpRange> findEnabledIncludeRanges(
            final com.communote.server.model.security.ChannelType channel) {
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.security.iprange.IpRangeChannelDao.findEnabledIncludeRanges(ChannelType channel) - 'channel' can not be null");
        }
        try {
            return this.handleFindEnabledIncludeRanges(channel);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.security.iprange.IpRangeChannelDao.findEnabledIncludeRanges(ChannelType channel)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #findEnabledExcludeRanges(com.communote.server.model.security.ChannelType)}
     */
    protected abstract java.util.List<com.communote.server.model.security.IpRange> handleFindEnabledExcludeRanges(
            com.communote.server.model.security.ChannelType channel);

    /**
     * Performs the core logic for
     * {@link #findEnabledIncludeRanges(com.communote.server.model.security.ChannelType)}
     */
    protected abstract java.util.List<com.communote.server.model.security.IpRange> handleFindEnabledIncludeRanges(
            com.communote.server.model.security.ChannelType channel);

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#load(int, String)
     */
    @Override
    public Object load(final int transform, final String type) {
        if (type == null) {
            throw new IllegalArgumentException("IpRangeChannel.load - 'type' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.security.IpRangeChannelImpl.class, type);
        return transformEntity(transform,
                (com.communote.server.model.security.IpRangeChannel) entity);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#load(String)
     */
    @Override
    public com.communote.server.model.security.IpRangeChannel load(String type) {
        return (com.communote.server.model.security.IpRangeChannel) this.load(TRANSFORM_NONE, type);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.IpRangeChannel> loadAll() {
        return (java.util.Collection<com.communote.server.model.security.IpRangeChannel>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.security.IpRangeChannelImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#remove(com.communote.server.model.security.IpRangeChannel)
     */
    @Override
    public void remove(com.communote.server.model.security.IpRangeChannel ipRangeChannel) {
        if (ipRangeChannel == null) {
            throw new IllegalArgumentException(
                    "IpRangeChannel.remove - 'ipRangeChannel' can not be null");
        }
        this.getHibernateTemplate().delete(ipRangeChannel);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#remove(java.util.
     *      Collection<com.communote.server.persistence.security.iprange.IpRangeChannel>)
     */
    @Override
    public void remove(
            java.util.Collection<com.communote.server.model.security.IpRangeChannel> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRangeChannel.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#remove(String)
     */
    @Override
    public void remove(String type) {
        if (type == null) {
            throw new IllegalArgumentException("IpRangeChannel.remove - 'type' can not be null");
        }
        com.communote.server.model.security.IpRangeChannel entity = this.load(type);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.security.IpRangeChannel)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.security.iprange.IpRangeChannelDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.security.IpRangeChannel)
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
     * <code>com.communote.server.persistence.security.iprange.IpRangeChannelDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.security.iprange.IpRangeChannelDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.security.IpRangeChannel entity) {
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
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#update(com.communote.server.model.security.IpRangeChannel)
     */
    @Override
    public void update(com.communote.server.model.security.IpRangeChannel ipRangeChannel) {
        if (ipRangeChannel == null) {
            throw new IllegalArgumentException(
                    "IpRangeChannel.update - 'ipRangeChannel' can not be null");
        }
        this.getHibernateTemplate().update(ipRangeChannel);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeChannelDao#update(java.util.
     *      Collection<com.communote.server.persistence.security.iprange.IpRangeChannel>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.security.IpRangeChannel> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRangeChannel.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.IpRangeChannel>() {
                            @Override
                            public com.communote.server.model.security.IpRangeChannel doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.IpRangeChannel> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}