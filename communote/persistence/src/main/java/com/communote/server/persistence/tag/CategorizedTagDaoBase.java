package com.communote.server.persistence.tag;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.tag.CategorizedTag</code>.
 * </p>
 * 
 * @see com.communote.server.model.tag.CategorizedTag
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class CategorizedTagDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.tag.CategorizedTagDao {

    /**
     * This anonymous transformer is designed to transform entities or report query results (which
     * result in an array of objects) to {@link com.communote.server.core.vo.tag.CategorizedTagVO}
     * using the Jakarta Commons-Collections Transformation API.
     */
    private final org.apache.commons.collections.Transformer CATEGORIZEDTAGVO_TRANSFORMER = new org.apache.commons.collections.Transformer() {
        public Object transform(Object input) {
            Object result = null;
            if (input instanceof com.communote.server.model.tag.CategorizedTag) {
                result = toCategorizedTagVO((com.communote.server.model.tag.CategorizedTag) input);
            } else if (input instanceof Object[]) {
                result = toCategorizedTagVO((Object[]) input);
            }
            return result;
        }
    };

    private final org.apache.commons.collections.Transformer CategorizedTagVOToEntityTransformer = new org.apache.commons.collections.Transformer() {
        public Object transform(Object input) {
            return categorizedTagVOToEntity((com.communote.server.core.vo.tag.CategorizedTagVO) input);
        }
    };

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#categorizedTagVOToEntity(com.communote.server.core.vo.tag.CategorizedTagVO,
     *      com.communote.server.model.tag.CategorizedTag)
     */
    public void categorizedTagVOToEntity(com.communote.server.core.vo.tag.CategorizedTagVO source,
            com.communote.server.model.tag.CategorizedTag target, boolean copyIfNull) {
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#categorizedTagVOToEntityCollection(java.util.Collection)
     */
    public final void categorizedTagVOToEntityCollection(java.util.Collection instances) {
        if (instances != null) {
            for (final java.util.Iterator iterator = instances.iterator(); iterator.hasNext();) {
                // - remove an objects that are null or not of the correct instance
                if (!(iterator.next() instanceof com.communote.server.core.vo.tag.CategorizedTagVO)) {
                    iterator.remove();
                }
            }
            org.apache.commons.collections.CollectionUtils.transform(instances,
                    CategorizedTagVOToEntityTransformer);
        }
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#create(com.communote.server.model.tag.CategorizedTag)
     */
    public com.communote.server.model.tag.CategorizedTag create(
            com.communote.server.model.tag.CategorizedTag categorizedTag) {
        return (com.communote.server.model.tag.CategorizedTag) this.create(TRANSFORM_NONE,
                categorizedTag);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#create(int transform,
     *      com.communote.server.persistence.tag.CategorizedTag)
     */
    public Object create(final int transform,
            final com.communote.server.model.tag.CategorizedTag categorizedTag) {
        if (categorizedTag == null) {
            throw new IllegalArgumentException(
                    "CategorizedTag.create - 'categorizedTag' can not be null");
        }
        this.getHibernateTemplate().save(categorizedTag);
        return this.transformEntity(transform, categorizedTag);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.tag.CategorizedTag>)
     */
    public java.util.Collection<com.communote.server.model.tag.CategorizedTag> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.tag.CategorizedTag> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("CategorizedTag.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.tag.CategorizedTag>() {
                            public com.communote.server.model.tag.CategorizedTag doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.tag.CategorizedTag> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tag.CategorizedTag>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.tag.CategorizedTag> create(
            final java.util.Collection<com.communote.server.model.tag.CategorizedTag> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.tag.CategorizedTag entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("CategorizedTag.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.tag.CategorizedTagImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.tag.CategorizedTag) entity);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#load(Long)
     */
    public com.communote.server.model.tag.CategorizedTag load(Long id) {
        return (com.communote.server.model.tag.CategorizedTag) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.tag.CategorizedTag> loadAll() {
        return (java.util.Collection<com.communote.server.model.tag.CategorizedTag>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.tag.CategorizedTagImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#remove(com.communote.server.model.tag.CategorizedTag)
     */
    public void remove(com.communote.server.model.tag.CategorizedTag categorizedTag) {
        if (categorizedTag == null) {
            throw new IllegalArgumentException(
                    "CategorizedTag.remove - 'categorizedTag' can not be null");
        }
        this.getHibernateTemplate().delete(categorizedTag);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tag.CategorizedTag>)
     */
    public void remove(java.util.Collection<com.communote.server.model.tag.CategorizedTag> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("CategorizedTag.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("CategorizedTag.remove - 'id' can not be null");
        }
        com.communote.server.model.tag.CategorizedTag entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#toCategorizedTagVO(com.communote.server.model.tag.CategorizedTag)
     */
    public com.communote.server.core.vo.tag.CategorizedTagVO toCategorizedTagVO(
            final com.communote.server.model.tag.CategorizedTag entity) {
        final com.communote.server.core.vo.tag.CategorizedTagVO target = new com.communote.server.core.vo.tag.CategorizedTagVO();
        this.toCategorizedTagVO(entity, target);
        return target;
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#toCategorizedTagVO(com.communote.server.model.tag.CategorizedTag,
     *      com.communote.server.core.vo.tag.CategorizedTagVO)
     */
    public void toCategorizedTagVO(com.communote.server.model.tag.CategorizedTag source,
            com.communote.server.core.vo.tag.CategorizedTagVO target) {
    }

    /**
     * Default implementation for transforming the results of a report query into a value object.
     * This implementation exists for convenience reasons only. It needs only be overridden in the
     * {@link CategorizedTagDaoImpl} class if you intend to use reporting queries.
     * 
     * @see com.communote.server.persistence.tag.CategorizedTagDao#toCategorizedTagVO(com.communote.server.model.tag.CategorizedTag)
     */
    protected com.communote.server.core.vo.tag.CategorizedTagVO toCategorizedTagVO(Object[] row) {
        com.communote.server.core.vo.tag.CategorizedTagVO target = null;
        if (row != null) {
            final int numberOfObjects = row.length;
            for (int ctr = 0; ctr < numberOfObjects; ctr++) {
                final Object object = row[ctr];
                if (object instanceof com.communote.server.model.tag.CategorizedTag) {
                    target = this
                            .toCategorizedTagVO((com.communote.server.model.tag.CategorizedTag) object);
                    break;
                }
            }
        }
        return target;
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#toCategorizedTagVOCollection(java.util.Collection)
     */
    public final void toCategorizedTagVOCollection(java.util.Collection entities) {
        if (entities != null) {
            org.apache.commons.collections.CollectionUtils.transform(entities,
                    CATEGORIZEDTAGVO_TRANSFORMER);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.tag.CategorizedTag)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.tag.CategorizedTagDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.tag.CategorizedTag)
     */
    protected void transformEntities(final int transform, final java.util.Collection<?> entities) {
        switch (transform) {
        case TRANSFORM_CATEGORIZEDTAGVO:
            toCategorizedTagVOCollection(entities);
            break;
        case TRANSFORM_NONE: // fall-through
        default:
            // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter),
     * when the <code>transform</code> flag is set to one of the constants defined in
     * <code>com.communote.server.persistence.tag.CategorizedTagDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * <p/>
     * This method will return instances of these types:
     * <ul>
     * <li>{@link com.communote.server.model.tag.CategorizedTag} - {@link #TRANSFORM_NONE}</li>
     * <li>{@link com.communote.server.core.vo.tag.CategorizedTagVO} -
     * {@link TRANSFORM_CATEGORIZEDTAGVO}</li>
     * </ul>
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.tag.CategorizedTagDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.tag.CategorizedTag entity) {
        Object target = null;
        if (entity != null) {
            switch (transform) {
            case TRANSFORM_CATEGORIZEDTAGVO:
                target = toCategorizedTagVO(entity);
                break;
            case TRANSFORM_NONE: // fall-through
            default:
                target = entity;
            }
        }
        return target;
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#update(com.communote.server.model.tag.CategorizedTag)
     */
    public void update(com.communote.server.model.tag.CategorizedTag categorizedTag) {
        if (categorizedTag == null) {
            throw new IllegalArgumentException(
                    "CategorizedTag.update - 'categorizedTag' can not be null");
        }
        this.getHibernateTemplate().update(categorizedTag);
    }

    /**
     * @see com.communote.server.persistence.tag.CategorizedTagDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tag.CategorizedTag>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.tag.CategorizedTag> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("CategorizedTag.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.tag.CategorizedTag>() {
                            public com.communote.server.model.tag.CategorizedTag doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.tag.CategorizedTag> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}