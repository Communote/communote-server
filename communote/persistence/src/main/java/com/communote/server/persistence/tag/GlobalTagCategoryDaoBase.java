package com.communote.server.persistence.tag;

import com.communote.server.model.tag.GlobalTagCategoryConstants;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.tag.GlobalTagCategory</code>.
 * </p>
 *
 * @see com.communote.server.model.tag.GlobalTagCategory
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class GlobalTagCategoryDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.tag.GlobalTagCategoryDao {

    /**
     * This anonymous transformer is designed to transform entities or report query results (which
     * result in an array of objects) to
     * {@link com.communote.server.core.vo.tag.GlobalTagCategoryVO} using the Jakarta
     * Commons-Collections Transformation API.
     */
    private final org.apache.commons.collections.Transformer GLOBALTAGCATEGORYVO_TRANSFORMER = new org.apache.commons.collections.Transformer() {
        @Override
        public Object transform(Object input) {
            Object result = null;
            if (input instanceof com.communote.server.model.tag.GlobalTagCategory) {
                result = toGlobalTagCategoryVO((com.communote.server.model.tag.GlobalTagCategory) input);
            } else if (input instanceof Object[]) {
                result = toGlobalTagCategoryVO((Object[]) input);
            }
            return result;
        }
    };

    private final org.apache.commons.collections.Transformer GlobalTagCategoryVOToEntityTransformer = new org.apache.commons.collections.Transformer() {
        @Override
        public Object transform(Object input) {
            return globalTagCategoryVOToEntity((com.communote.server.core.vo.tag.GlobalTagCategoryVO) input);
        }
    };

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#create(com.communote.server.model.tag.GlobalTagCategory)
     */
    @Override
    public com.communote.server.model.tag.GlobalTagCategory create(
            com.communote.server.model.tag.GlobalTagCategory globalTagCategory) {
        return (com.communote.server.model.tag.GlobalTagCategory) this.create(TRANSFORM_NONE,
                globalTagCategory);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#create(int transform,
     *      com.communote.server.persistence.tag.GlobalTagCategory)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.tag.GlobalTagCategory globalTagCategory) {
        if (globalTagCategory == null) {
            throw new IllegalArgumentException(
                    "GlobalTagCategory.create - 'globalTagCategory' can not be null");
        }
        this.getHibernateTemplate().save(globalTagCategory);
        return this.transformEntity(transform, globalTagCategory);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.tag.GlobalTagCategory>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "GlobalTagCategory.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.tag.GlobalTagCategory>() {
                            @Override
                            public com.communote.server.model.tag.GlobalTagCategory doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.tag.GlobalTagCategory> entityIterator = entities
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
     *      com.communote.server.persistence.tag.GlobalTagCategoryDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tag.GlobalTagCategory>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> create(
            final java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.tag.GlobalTagCategory entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#findByName(int, String)
     */
    @Override
    public Object findByName(final int transform, final String name) {
        return this.findByName(transform, "from " + GlobalTagCategoryConstants.CLASS_NAME
                + " as globalTagCategory where globalTagCategory.name = :name", name);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#findByName(int, String,
     *      String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object findByName(final int transform, final String queryString, final String name) {
        try {
            org.hibernate.Query queryObject = super.getSession(false).createQuery(queryString);
            queryObject.setParameter("name", name);
            java.util.Set results = new java.util.LinkedHashSet(queryObject.list());
            Object result = null;
            if (results != null) {
                if (results.size() > 1) {
                    throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                            "More than one instance of 'GlobalTagCategory"
                                    + "' was found when executing query --> '" + queryString + "'");
                } else if (results.size() == 1) {
                    result = results.iterator().next();
                }
            }
            result = transformEntity(transform,
                    (com.communote.server.model.tag.GlobalTagCategory) result);
            return result;
        } catch (org.hibernate.HibernateException ex) {
            throw super.convertHibernateAccessException(ex);
        }
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#findByName(String)
     */
    @Override
    public com.communote.server.model.tag.GlobalTagCategory findByName(String name) {
        return (com.communote.server.model.tag.GlobalTagCategory) this.findByName(TRANSFORM_NONE,
                name);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#findByName(String, String)
     */
    @Override
    public com.communote.server.model.tag.GlobalTagCategory findByName(final String queryString,
            final String name) {
        return (com.communote.server.model.tag.GlobalTagCategory) this.findByName(TRANSFORM_NONE,
                queryString, name);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#findByPrefix(int, String)
     */
    @Override
    public Object findByPrefix(final int transform, final String prefix) {
        return this.findByPrefix(transform, "from " + GlobalTagCategoryConstants.CLASS_NAME
                + " as globalTagCategory where globalTagCategory.prefix = :prefix", prefix);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#findByPrefix(int, String,
     *      String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object findByPrefix(final int transform, final String queryString, final String prefix) {
        try {
            org.hibernate.Query queryObject = super.getSession(false).createQuery(queryString);
            queryObject.setParameter("prefix", prefix);
            java.util.Set results = new java.util.LinkedHashSet(queryObject.list());
            Object result = null;
            if (results != null) {
                if (results.size() > 1) {
                    throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                            "More than one instance of 'GlobalTagCategory"
                                    + "' was found when executing query --> '" + queryString + "'");
                } else if (results.size() == 1) {
                    result = results.iterator().next();
                }
            }
            result = transformEntity(transform,
                    (com.communote.server.model.tag.GlobalTagCategory) result);
            return result;
        } catch (org.hibernate.HibernateException ex) {
            throw super.convertHibernateAccessException(ex);
        }
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#findByPrefix(String)
     */
    @Override
    public com.communote.server.model.tag.GlobalTagCategory findByPrefix(String prefix) {
        return (com.communote.server.model.tag.GlobalTagCategory) this.findByPrefix(TRANSFORM_NONE,
                prefix);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#findByPrefix(String, String)
     */
    @Override
    public com.communote.server.model.tag.GlobalTagCategory findByPrefix(final String queryString,
            final String prefix) {
        return (com.communote.server.model.tag.GlobalTagCategory) this.findByPrefix(TRANSFORM_NONE,
                queryString, prefix);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#globalTagCategoryVOToEntity(com.communote.server.core.vo.tag.GlobalTagCategoryVO,
     *      com.communote.server.model.tag.GlobalTagCategory)
     */
    @Override
    public void globalTagCategoryVOToEntity(
            com.communote.server.core.vo.tag.GlobalTagCategoryVO source,
            com.communote.server.model.tag.GlobalTagCategory target, boolean copyIfNull) {
        if (copyIfNull || source.getName() != null) {
            target.setName(source.getName());
        }
        if (copyIfNull || source.getPrefix() != null) {
            target.setPrefix(source.getPrefix());
        }
        if (copyIfNull || source.getDescription() != null) {
            target.setDescription(source.getDescription());
        }
        if (copyIfNull || source.isMultipleTags() != false) {
            target.setMultipleTags(source.isMultipleTags());
        }
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#globalTagCategoryVOToEntityCollection(java.util.Collection)
     */
    @Override
    public final void globalTagCategoryVOToEntityCollection(java.util.Collection instances) {
        if (instances != null) {
            for (final java.util.Iterator iterator = instances.iterator(); iterator.hasNext();) {
                // - remove an objects that are null or not of the correct instance
                if (!(iterator.next() instanceof com.communote.server.core.vo.tag.GlobalTagCategoryVO)) {
                    iterator.remove();
                }
            }
            org.apache.commons.collections.CollectionUtils.transform(instances,
                    GlobalTagCategoryVOToEntityTransformer);
        }
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("GlobalTagCategory.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.tag.GlobalTagCategoryImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.tag.GlobalTagCategory) entity);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#load(Long)
     */
    @Override
    public com.communote.server.model.tag.GlobalTagCategory load(Long id) {
        return (com.communote.server.model.tag.GlobalTagCategory) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> loadAll() {
        return (java.util.Collection<com.communote.server.model.tag.GlobalTagCategory>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.tag.GlobalTagCategoryImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#remove(com.communote.server.model.tag.GlobalTagCategory)
     */
    @Override
    public void remove(com.communote.server.model.tag.GlobalTagCategory globalTagCategory) {
        if (globalTagCategory == null) {
            throw new IllegalArgumentException(
                    "GlobalTagCategory.remove - 'globalTagCategory' can not be null");
        }
        this.getHibernateTemplate().delete(globalTagCategory);
    }

    /**
     * @see 
     *      com.communote.server.persistence.tag.GlobalTagCategoryDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tag.GlobalTagCategory>)
     */
    @Override
    public void remove(
            java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "GlobalTagCategory.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("GlobalTagCategory.remove - 'id' can not be null");
        }
        com.communote.server.model.tag.GlobalTagCategory entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#toGlobalTagCategoryVO(com.communote.server.model.tag.GlobalTagCategory)
     */
    @Override
    public com.communote.server.core.vo.tag.GlobalTagCategoryVO toGlobalTagCategoryVO(
            final com.communote.server.model.tag.GlobalTagCategory entity) {
        final com.communote.server.core.vo.tag.GlobalTagCategoryVO target = new com.communote.server.core.vo.tag.GlobalTagCategoryVO();
        this.toGlobalTagCategoryVO(entity, target);
        return target;
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#toGlobalTagCategoryVO(com.communote.server.model.tag.GlobalTagCategory,
     *      com.communote.server.core.vo.tag.GlobalTagCategoryVO)
     */
    @Override
    public void toGlobalTagCategoryVO(com.communote.server.model.tag.GlobalTagCategory source,
            com.communote.server.core.vo.tag.GlobalTagCategoryVO target) {
        target.setName(source.getName());
        target.setPrefix(source.getPrefix());
        target.setDescription(source.getDescription());
        target.setMultipleTags(source.isMultipleTags());
    }

    /**
     * Default implementation for transforming the results of a report query into a value object.
     * This implementation exists for convenience reasons only. It needs only be overridden in the
     * {@link GlobalTagCategoryDaoImpl} class if you intend to use reporting queries.
     *
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#toGlobalTagCategoryVO(com.communote.server.model.tag.GlobalTagCategory)
     */
    protected com.communote.server.core.vo.tag.GlobalTagCategoryVO toGlobalTagCategoryVO(
            Object[] row) {
        com.communote.server.core.vo.tag.GlobalTagCategoryVO target = null;
        if (row != null) {
            final int numberOfObjects = row.length;
            for (int ctr = 0; ctr < numberOfObjects; ctr++) {
                final Object object = row[ctr];
                if (object instanceof com.communote.server.model.tag.GlobalTagCategory) {
                    target = this
                            .toGlobalTagCategoryVO((com.communote.server.model.tag.GlobalTagCategory) object);
                    break;
                }
            }
        }
        return target;
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#toGlobalTagCategoryVOCollection(java.util.Collection)
     */
    @Override
    public final void toGlobalTagCategoryVOCollection(java.util.Collection entities) {
        if (entities != null) {
            org.apache.commons.collections.CollectionUtils.transform(entities,
                    GLOBALTAGCATEGORYVO_TRANSFORMER);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.tag.GlobalTagCategory)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.tag.GlobalTagCategoryDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.tag.GlobalTagCategory)
     */
    protected void transformEntities(final int transform, final java.util.Collection<?> entities) {
        switch (transform) {
        case TRANSFORM_GLOBALTAGCATEGORYVO:
            toGlobalTagCategoryVOCollection(entities);
            break;
        case TRANSFORM_NONE: // fall-through
        default:
            // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter),
     * when the <code>transform</code> flag is set to one of the constants defined in
     * <code>com.communote.server.persistence.tag.GlobalTagCategoryDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * <p/>
     * This method will return instances of these types:
     * <ul>
     * <li>{@link com.communote.server.model.tag.GlobalTagCategory} - {@link #TRANSFORM_NONE}</li>
     * <li>{@link com.communote.server.core.vo.tag.GlobalTagCategoryVO} -
     * {@link TRANSFORM_GLOBALTAGCATEGORYVO}</li>
     * </ul>
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.tag.GlobalTagCategoryDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.tag.GlobalTagCategory entity) {
        Object target = null;
        if (entity != null) {
            switch (transform) {
            case TRANSFORM_GLOBALTAGCATEGORYVO:
                target = toGlobalTagCategoryVO(entity);
                break;
            case TRANSFORM_NONE: // fall-through
            default:
                target = entity;
            }
        }
        return target;
    }

    /**
     * @see com.communote.server.persistence.tag.GlobalTagCategoryDao#update(com.communote.server.model.tag.GlobalTagCategory)
     */
    @Override
    public void update(com.communote.server.model.tag.GlobalTagCategory globalTagCategory) {
        if (globalTagCategory == null) {
            throw new IllegalArgumentException(
                    "GlobalTagCategory.update - 'globalTagCategory' can not be null");
        }
        this.getHibernateTemplate().update(globalTagCategory);
    }

    /**
     * @see 
     *      com.communote.server.persistence.tag.GlobalTagCategoryDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tag.GlobalTagCategory>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "GlobalTagCategory.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.tag.GlobalTagCategory>() {
                            @Override
                            public com.communote.server.model.tag.GlobalTagCategory doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.tag.GlobalTagCategory> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}