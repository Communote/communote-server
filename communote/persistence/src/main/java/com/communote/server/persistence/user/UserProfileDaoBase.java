package com.communote.server.persistence.user;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.KenmeiUserProfile</code>.
 * </p>
 *
 * @see com.communote.server.model.user.UserProfile
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserProfileDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.UserProfileDao {

    /**
     * This anonymous transformer is designed to transform entities or report query results (which
     * result in an array of objects) to {@link com.communote.server.persistence.user.UserProfileVO}
     * using the Jakarta Commons-Collections Transformation API.
     */
    private final org.apache.commons.collections.Transformer KENMEIUSERPROFILEVO_TRANSFORMER = new org.apache.commons.collections.Transformer() {
        @Override
        public Object transform(Object input) {
            Object result = null;
            if (input instanceof com.communote.server.model.user.UserProfile) {
                result = toUserProfileVO((com.communote.server.model.user.UserProfile) input);
            } else if (input instanceof Object[]) {
                result = toKenmeiUserProfileVO((Object[]) input);
            }
            return result;
        }
    };

    private final org.apache.commons.collections.Transformer KenmeiUserProfileVOToEntityTransformer = new org.apache.commons.collections.Transformer() {
        @Override
        public Object transform(Object input) {
            return userProfileVOToEntity((com.communote.server.persistence.user.UserProfileVO) input);
        }
    };

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#create(com.communote.server.model.user.UserProfile)
     */
    @Override
    public com.communote.server.model.user.UserProfile create(
            com.communote.server.model.user.UserProfile kenmeiUserProfile) {
        return (com.communote.server.model.user.UserProfile) this.create(TRANSFORM_NONE,
                kenmeiUserProfile);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#create(int transform,
     *      com.communote.server.persistence.user.KenmeiUserProfile)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.user.UserProfile kenmeiUserProfile) {
        if (kenmeiUserProfile == null) {
            throw new IllegalArgumentException(
                    "UserProfile.create - 'kenmeiUserProfile' can not be null");
        }
        this.getHibernateTemplate().save(kenmeiUserProfile);
        return this.transformEntity(transform, kenmeiUserProfile);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.KenmeiUserProfile>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.UserProfile> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.UserProfile> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserProfile.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.UserProfile>() {
                            @Override
                            public com.communote.server.model.user.UserProfile doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.UserProfile> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.KenmeiUserProfile>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.UserProfile> create(
            final java.util.Collection<com.communote.server.model.user.UserProfile> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.user.UserProfile entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#kenmeiUserProfileVOToEntityCollection(java.util.Collection)
     */
    @Override
    public final void kenmeiUserProfileVOToEntityCollection(java.util.Collection instances) {
        if (instances != null) {
            for (final java.util.Iterator iterator = instances.iterator(); iterator.hasNext();) {
                // - remove an objects that are null or not of the correct instance
                if (!(iterator.next() instanceof com.communote.server.persistence.user.UserProfileVO)) {
                    iterator.remove();
                }
            }
            org.apache.commons.collections.CollectionUtils.transform(instances,
                    KenmeiUserProfileVOToEntityTransformer);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserProfile.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.UserProfile.class, id);
        return transformEntity(transform, (com.communote.server.model.user.UserProfile) entity);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#load(Long)
     */
    @Override
    public com.communote.server.model.user.UserProfile load(Long id) {
        return (com.communote.server.model.user.UserProfile) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.UserProfile> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.UserProfile>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.UserProfile.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#remove(com.communote.server.model.user.UserProfile)
     */
    @Override
    public void remove(com.communote.server.model.user.UserProfile kenmeiUserProfile) {
        if (kenmeiUserProfile == null) {
            throw new IllegalArgumentException(
                    "UserProfile.remove - 'kenmeiUserProfile' can not be null");
        }
        this.getHibernateTemplate().delete(kenmeiUserProfile);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.KenmeiUserProfile>)
     */
    @Override
    public void remove(java.util.Collection<com.communote.server.model.user.UserProfile> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserProfile.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserProfile.remove - 'id' can not be null");
        }
        com.communote.server.model.user.UserProfile entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Default implementation for transforming the results of a report query into a value object.
     * This implementation exists for convenience reasons only. It needs only be overridden in the
     * {@link UserProfileDaoImpl} class if you intend to use reporting queries.
     *
     * @see com.communote.server.persistence.user.UserProfileDao#toUserProfileVO(com.communote.server.model.user.UserProfile)
     */
    protected com.communote.server.persistence.user.UserProfileVO toKenmeiUserProfileVO(Object[] row) {
        com.communote.server.persistence.user.UserProfileVO target = null;
        if (row != null) {
            final int numberOfObjects = row.length;
            for (int ctr = 0; ctr < numberOfObjects; ctr++) {
                final Object object = row[ctr];
                if (object instanceof com.communote.server.model.user.UserProfile) {
                    target = this
                            .toUserProfileVO((com.communote.server.model.user.UserProfile) object);
                    break;
                }
            }
        }
        return target;
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#toKenmeiUserProfileVOCollection(java.util.Collection)
     */
    @Override
    public final void toKenmeiUserProfileVOCollection(java.util.Collection entities) {
        if (entities != null) {
            org.apache.commons.collections.CollectionUtils.transform(entities,
                    KENMEIUSERPROFILEVO_TRANSFORMER);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#toUserProfileVO(com.communote.server.model.user.UserProfile)
     */
    @Override
    public com.communote.server.persistence.user.UserProfileVO toUserProfileVO(
            final com.communote.server.model.user.UserProfile entity) {
        final com.communote.server.persistence.user.UserProfileVO target = new com.communote.server.persistence.user.UserProfileVO();
        this.toUserProfileVO(entity, target);
        return target;
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#toUserProfileVO(com.communote.server.model.user.UserProfile,
     *      com.communote.server.persistence.user.UserProfileVO)
     */
    @Override
    public void toUserProfileVO(com.communote.server.model.user.UserProfile source,
            com.communote.server.persistence.user.UserProfileVO target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setSalutation(source.getSalutation());
        target.setPosition(source.getPosition());
        target.setCompany(source.getCompany());
        target.setTimeZoneId(source.getTimeZoneId());
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.UserProfile)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.UserProfileDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.UserProfile)
     */
    protected void transformEntities(final int transform, final java.util.Collection<?> entities) {
        switch (transform) {
        case TRANSFORM_KENMEIUSERPROFILEVO:
            toKenmeiUserProfileVOCollection(entities);
            break;
        case TRANSFORM_NONE: // fall-through
        default:
            // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter),
     * when the <code>transform</code> flag is set to one of the constants defined in
     * <code>com.communote.server.persistence.user.UserProfileDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * <p/>
     * This method will return instances of these types:
     * <ul>
     * <li>{@link com.communote.server.model.user.UserProfile} - {@link #TRANSFORM_NONE}</li>
     * <li>{@link com.communote.server.persistence.user.UserProfileVO} -
     * {@link TRANSFORM_KENMEIUSERPROFILEVO}</li>
     * </ul>
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.UserProfileDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.UserProfile entity) {
        Object target = null;
        if (entity != null) {
            switch (transform) {
            case TRANSFORM_KENMEIUSERPROFILEVO:
                target = toUserProfileVO(entity);
                break;
            case TRANSFORM_NONE: // fall-through
            default:
                target = entity;
            }
        }
        return target;
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#update(com.communote.server.model.user.UserProfile)
     */
    @Override
    public void update(com.communote.server.model.user.UserProfile kenmeiUserProfile) {
        if (kenmeiUserProfile == null) {
            throw new IllegalArgumentException(
                    "UserProfile.update - 'kenmeiUserProfile' can not be null");
        }
        this.getHibernateTemplate().update(kenmeiUserProfile);
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.KenmeiUserProfile>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.user.UserProfile> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserProfile.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.UserProfile>() {
                            @Override
                            public com.communote.server.model.user.UserProfile doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.UserProfile> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

    /**
     * @see com.communote.server.persistence.user.UserProfileDao#kenmeiUserProfileVOToEntity(com.communote.server.persistence.user.UserProfileVO,
     *      com.communote.server.model.user.UserProfile)
     */
    @Override
    public void userProfileVOToEntity(com.communote.server.persistence.user.UserProfileVO source,
            com.communote.server.model.user.UserProfile target, boolean copyIfNull) {
        if (copyIfNull || source.getLastName() != null) {
            target.setLastName(source.getLastName());
        }
        if (copyIfNull || source.getSalutation() != null) {
            target.setSalutation(source.getSalutation());
        }
        if (copyIfNull || source.getPosition() != null) {
            target.setPosition(source.getPosition());
        }
        if (copyIfNull || source.getCompany() != null) {
            target.setCompany(source.getCompany());
        }
        if (copyIfNull || source.getFirstName() != null) {
            target.setFirstName(source.getFirstName());
        }
        if (copyIfNull || source.getTimeZoneId() != null) {
            target.setTimeZoneId(source.getTimeZoneId());
        }
    }

}