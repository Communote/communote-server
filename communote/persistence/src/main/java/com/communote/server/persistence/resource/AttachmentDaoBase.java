package com.communote.server.persistence.resource;

import java.util.Collection;
import java.util.Set;

import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentConstants;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.resource.Attachment</code>.
 * </p>
 *
 * @see com.communote.server.model.attachment.Attachment
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AttachmentDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.resource.AttachmentDao {

    private com.communote.server.persistence.global.GlobalIdDao globalIdDao;

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#create(com.communote.server.model.attachment.Attachment)
     */
    @Override
    public com.communote.server.model.attachment.Attachment create(
            com.communote.server.model.attachment.Attachment attachment) {
        return (com.communote.server.model.attachment.Attachment) this.create(TRANSFORM_NONE,
                attachment);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#create(int transform,
     *      com.communote.server.persistence.resource.Attachment)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.attachment.Attachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment.create - 'attachment' can not be null");
        }
        this.getHibernateTemplate().save(attachment);
        return this.transformEntity(transform, attachment);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.resource.Attachment>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.attachment.Attachment> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.attachment.Attachment> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Attachment.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.attachment.Attachment>() {
                            @Override
                            public com.communote.server.model.attachment.Attachment doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.attachment.Attachment> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#create(java.util.Collection<
     *      Attachment>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.attachment.Attachment> create(
            final java.util.Collection<com.communote.server.model.attachment.Attachment> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.attachment.Attachment entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#find(int, String, String)
     */
    @Override
    public Object find(final int transform, final String contentIdentifier,
            final String repositoryIdentifier) {
        return this
                .find(transform,
                        "from "
                                + AttachmentConstants.CLASS_NAME
                                + " as attachment where attachment.contentIdentifier = :contentIdentifier and attachment.repositoryIdentifier = :repositoryIdentifier",
                        contentIdentifier, repositoryIdentifier);
    }

    /**
     * helper to fetch an attachment
     *
     * @param transform
     *            transformation instructions
     * @param queryString
     *            the query
     * @param contentIdentifier
     *            ID of the content within the repository
     * @param repositoryIdentifier
     *            ID of the reposotory
     * @return the found attachment
     */
    private Object find(final int transform, final String queryString,
            final String contentIdentifier, final String repositoryIdentifier) {
        try {
            org.hibernate.Query queryObject = super.getSession(false).createQuery(queryString);
            queryObject.setParameter("contentIdentifier", contentIdentifier);
            queryObject.setParameter("repositoryIdentifier", repositoryIdentifier);
            Set results = new java.util.LinkedHashSet(queryObject.list());
            Object result = null;
            if (results.size() > 1) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'Attachment"
                                + "' was found when executing query --> '" + queryString + "'");
            } else if (results.size() == 1) {
                result = results.iterator().next();
            }
            result = transformEntity(transform,
                    (com.communote.server.model.attachment.Attachment) result);
            return result;
        } catch (org.hibernate.HibernateException ex) {
            throw super.convertHibernateAccessException(ex);
        }
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#find(String, String)
     */
    @Override
    public com.communote.server.model.attachment.Attachment find(String contentIdentifier,
            String repositoryIdentifier) {
        return (com.communote.server.model.attachment.Attachment) this.find(TRANSFORM_NONE,
                contentIdentifier, repositoryIdentifier);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#findContentTypeNull()
     */
    @Override
    public com.communote.server.model.attachment.Attachment findContentTypeNull() {
        try {
            return this.handleFindContentTypeNull();
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.resource.AttachmentDao.findContentTypeNull()' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#findNoteByContentId(com.communote.server.core.crc.vo.ContentId)
     */
    @Override
    public com.communote.server.model.note.Note findNoteByContentId(
            final com.communote.server.core.crc.vo.ContentId contentId) {
        if (contentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.resource.AttachmentDao.findNoteByContentId(ContentId contentId) - 'contentId' can not be null");
        }
        try {
            return this.handleFindNoteByContentId(contentId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.resource.AttachmentDao.findNoteByContentId(ContentId contentId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Gets the reference to <code>globalIdDao</code>.
     */
    protected com.communote.server.persistence.global.GlobalIdDao getGlobalIdDao() {
        return this.globalIdDao;
    }

    /**
     * Performs the core logic for {@link #findContentTypeNull()}
     */
    protected abstract com.communote.server.model.attachment.Attachment handleFindContentTypeNull();

    /**
     * Performs the core logic for
     * {@link #findNoteByContentId(com.communote.server.core.crc.vo.ContentId)}
     */
    protected abstract com.communote.server.model.note.Note handleFindNoteByContentId(
            com.communote.server.core.crc.vo.ContentId contentId);

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Attachment.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.attachment.Attachment.class, id);
        return transformEntity(transform, (com.communote.server.model.attachment.Attachment) entity);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#load(Long)
     */
    @Override
    public com.communote.server.model.attachment.Attachment load(Long id) {
        return (com.communote.server.model.attachment.Attachment) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.attachment.Attachment> loadAll() {
        return (java.util.Collection<com.communote.server.model.attachment.Attachment>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.attachment.Attachment.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#remove(java.util.Collection<
     *      Attachment>)
     */
    @Override
    public void remove(Collection<Attachment> attachments) {
        if (attachments == null) {
            throw new IllegalArgumentException("Attachment.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(attachments);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#remove(com.communote.server.model.attachment.Attachment)
     */
    @Override
    public void remove(com.communote.server.model.attachment.Attachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment.remove - 'attachment' can not be null");
        }
        this.getHibernateTemplate().delete(attachment);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Attachment.remove - 'id' can not be null");
        }
        com.communote.server.model.attachment.Attachment entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Sets the reference to <code>globalIdDao</code>.
     */
    public void setGlobalIdDao(com.communote.server.persistence.global.GlobalIdDao globalIdDao) {
        this.globalIdDao = globalIdDao;
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.attachment.Attachment)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.resource.AttachmentDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.attachment.Attachment)
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
     * <code>com.communote.server.persistence.resource.AttachmentDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.resource.AttachmentDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.attachment.Attachment entity) {
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
     * @see com.communote.server.persistence.resource.AttachmentDao#update(com.communote.server.model.attachment.Attachment)
     */
    @Override
    public void update(com.communote.server.model.attachment.Attachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment.update - 'attachment' can not be null");
        }
        this.getHibernateTemplate().update(attachment);
    }

    /**
     * @see com.communote.server.persistence.resource.AttachmentDao#update(java.util.Collection<
     *      Attachment>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.attachment.Attachment> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Attachment.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.attachment.Attachment>() {
                            @Override
                            public com.communote.server.model.attachment.Attachment doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.attachment.Attachment> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}