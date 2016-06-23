package com.communote.server.persistence.blog;

import java.util.Date;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.communote.server.model.note.Note;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>Note</code>.
 * </p>
 *
 * @see Note
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class NoteDaoBase extends HibernateDaoSupport implements NoteDao {

    private com.communote.server.persistence.global.GlobalIdDao globalIdDao;

    /**
     * @see NoteDao#create(int, java.util.Collection<Note>)
     */
    @Override
    public java.util.Collection<Note> create(final int transform,
            final java.util.Collection<Note> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Note.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Note>() {
                    @Override
                    public Note doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Note> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see NoteDao#create(int transform, Note)
     */
    @Override
    public Object create(final int transform, final Note note) {
        if (note == null) {
            throw new IllegalArgumentException("Note.create - 'note' can not be null");
        }
        this.getHibernateTemplate().save(note);
        return this.transformEntity(transform, note);
    }

    /**
     * @see NoteDao#create(java.util.Collection<de.communardo. kenmei.core.api.bo.blog.Note>)
     */
    @Override
    public java.util.Collection<Note> create(final java.util.Collection<Note> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see NoteDao#create(Note)
     */
    @Override
    public Note create(Note note) {
        return (Note) this.create(TRANSFORM_NONE, note);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(Note entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see NoteDao#findLatestNote()
     */
    @Override
    public Note findLatestNote() {
        try {
            return this.handleFindLatestNote();
        } catch (RuntimeException rt) {
            throw new RuntimeException("Error performing 'NoteDao.findLatestNote()' --> " + rt, rt);
        }
    }

    /**
     * @see NoteDao#findNearestNote(long, java.util.Date, boolean)
     */
    @Override
    public Note findNearestNote(final long noteId, final java.util.Date creationDate,
            final boolean younger) {
        if (creationDate == null) {
            throw new IllegalArgumentException(
                    "NoteDao.findNearestNote(long noteId, java.util.Date creationDate, boolean younger) - 'creationDate' can not be null");
        }
        try {
            return this.handleFindNearestNote(noteId, creationDate, younger);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'NoteDao.findNearestNote(long noteId, java.util.Date creationDate, boolean younger)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see NoteDao#getAutosave(Long, Long, Long,
     *      com.communote.server.persistence.blog.FilterNoteProperty[])
     */
    @Override
    public Long getAutosave(final Long userId, final Long noteId, final Long parentNoteId,
            final com.communote.server.persistence.blog.FilterNoteProperty[] properties) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getAutosave(Long userId, Long noteId, Long parentNoteId, com.communote.server.persistence.blog.FilterNoteProperty[] properties) - 'userId' can not be null");
        }
        try {
            return this.handleGetAutosave(userId, noteId, parentNoteId, properties);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'NoteDao.getAutosave(Long userId, Long noteId, Long parentNoteId, com.communote.server.persistence.blog.FilterNoteProperty[] properties)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see NoteDao#getFavoriteNoteIds(Long, Long, Long)
     */
    @Override
    public java.util.Collection<Long> getFavoriteNoteIds(final Long userId, final Long lowerBound,
            final Long upperBound) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getFavoriteNoteIds(Long userId, Long lowerBound, Long upperBound) - 'userId' can not be null");
        }
        if (lowerBound == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getFavoriteNoteIds(Long userId, Long lowerBound, Long upperBound) - 'lowerBound' can not be null");
        }
        if (upperBound == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getFavoriteNoteIds(Long userId, Long lowerBound, Long upperBound) - 'upperBound' can not be null");
        }
        try {
            return this.handleGetFavoriteNoteIds(userId, lowerBound, upperBound);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'NoteDao.getFavoriteNoteIds(Long userId, Long lowerBound, Long upperBound)' --> "
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
     * @see NoteDao#getNoteIdsOfDiscussion(Long)
     */
    @Override
    public java.util.List<Long> getNoteIdsOfDiscussion(final Long discussionId) {
        if (discussionId == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getNoteIdsOfDiscussion(Long discussionId) - 'discussionId' can not be null");
        }
        try {
            return this.handleGetNoteIdsOfDiscussion(discussionId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'NoteDao.getNoteIdsOfDiscussion(Long discussionId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see NoteDao#getNotesByTag(Long)
     */
    @Override
    public java.util.List<Note> getNotesByTag(final Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getNotesByTag(Long tagId) - 'tagId' can not be null");
        }
        try {
            return this.handleGetNotesByTag(tagId);
        } catch (RuntimeException rt) {
            throw new RuntimeException("Error performing 'NoteDao.getNotesByTag(Long tagId)' --> "
                    + rt, rt);
        }
    }

    /**
     * @see NoteDao#getNotesCount()
     */
    @Override
    public long getNotesCount() {
        try {
            return this.handleGetNotesCount();
        } catch (RuntimeException rt) {
            throw new RuntimeException("Error performing 'NoteDao.getNotesCount()' --> " + rt, rt);
        }
    }

    /**
     * @see NoteDao#getNotesForBlog(Long, Long, Integer)
     */
    @Override
    public java.util.List<Note> getNotesForBlog(final Long blogId, final Long firstNoteId,
            final Integer limit) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getNotesForBlog(Long blogId, Long firstNoteId, Integer limit) - 'blogId' can not be null");
        }
        try {
            return this.handleGetNotesForBlog(blogId, firstNoteId, limit);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'NoteDao.getNotesForBlog(Long blogId, Long firstNoteId, Integer limit)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see NoteDao#getNotesOfUser(Long)
     */
    @Override
    public java.util.List<Note> getNotesOfUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getNotesOfUser(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleGetNotesOfUser(userId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'NoteDao.getNotesOfUser(Long userId)' --> " + rt, rt);
        }
    }

    /**
     * @see NoteDao#getNumberOfFavorites(Long)
     */
    @Override
    public int getNumberOfFavorites(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "NoteDao.getNumberOfFavorites(Long noteId) - 'noteId' can not be null");
        }
        try {
            return this.handleGetNumberOfFavorites(noteId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'NoteDao.getNumberOfFavorites(Long noteId)' --> " + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findLatestNote()}
     */
    protected abstract Note handleFindLatestNote();

    /**
     * Performs the core logic for {@link #findNearestNote(long, java.util.Date, boolean)}
     */
    protected abstract Note handleFindNearestNote(long noteId, Date creationDate, boolean younger);

    /**
     * Performs the core logic for
     * {@link #getAutosave(Long, Long, Long, com.communote.server.persistence.blog.FilterNoteProperty[])}
     */
    protected abstract Long handleGetAutosave(Long userId, Long noteId, Long parentNoteId,
            com.communote.server.persistence.blog.FilterNoteProperty[] properties);

    /**
     * Performs the core logic for {@link #getFavoriteNoteIds(Long, Long, Long)}
     */
    protected abstract java.util.Collection<Long> handleGetFavoriteNoteIds(Long userId,
            Long lowerBound, Long upperBound);

    /**
     * Performs the core logic for {@link #getNoteIdsOfDiscussion(Long)}
     */
    protected abstract java.util.List<Long> handleGetNoteIdsOfDiscussion(Long discussionId);

    /**
     * Performs the core logic for {@link #getNotesByTag(Long)}
     */
    protected abstract java.util.List<Note> handleGetNotesByTag(Long tagId);

    /**
     * Performs the core logic for {@link #getNotesCount()}
     */
    protected abstract long handleGetNotesCount();

    /**
     * Performs the core logic for {@link #getNotesForBlog(Long, Long, Integer)}
     */
    protected abstract java.util.List<Note> handleGetNotesForBlog(Long blogId, Long firstNoteId,
            Integer limit);

    /**
     * Performs the core logic for {@link #getNotesOfUser(Long)}
     */
    protected abstract java.util.List<Note> handleGetNotesOfUser(Long userId);

    protected abstract int handleGetNumberOfFavorites(Long noteId);

    /**
     * Performs the core logic for {@link #updateFollowableItems(Note)}
     */
    protected abstract void handleUpdateFollowableItems(Note note, boolean updateChildren);

    /**
     * @see NoteDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Note.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(Note.class, id);
        return transformEntity(transform, (Note) entity);
    }

    /**
     * @see NoteDao#load(Long)
     */
    @Override
    public Note load(Long id) {
        return (Note) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see NoteDao#loadAll()
     */
    @Override
    public java.util.Collection<Note> loadAll() {
        return (java.util.Collection<Note>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see NoteDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(Note.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see NoteDao#remove(java.util.Collection<de.communardo. kenmei.core.api.bo.blog.Note>)
     */
    @Override
    public void remove(java.util.Collection<Note> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Note.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see NoteDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Note.remove - 'id' can not be null");
        }
        Note entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see NoteDao#remove(Note)
     */
    @Override
    public void remove(Note note) {
        if (note == null) {
            throw new IllegalArgumentException("Note.remove - 'note' can not be null");
        }
        this.getHibernateTemplate().delete(note);
    }

    /**
     * Sets the reference to <code>globalIdDao</code>.
     */
    public void setGlobalIdDao(com.communote.server.persistence.global.GlobalIdDao globalIdDao) {
        this.globalIdDao = globalIdDao;
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,Note)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>NoteDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,Note)
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
     * <code>NoteDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes no
     * transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link NoteDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final Note entity) {
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
     * @see NoteDao#update(java.util.Collection<de.communardo. kenmei.core.api.bo.blog.Note>)
     */
    @Override
    public void update(final java.util.Collection<Note> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Note.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Note>() {
                    @Override
                    public Note doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Note> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see NoteDao#update(Note)
     */
    @Override
    public void update(Note note) {
        if (note == null) {
            throw new IllegalArgumentException("Note.update - 'note' can not be null");
        }
        this.getHibernateTemplate().update(note);
    }

    /**
     * @see NoteDao#updateFollowableItems(Note)
     */
    @Override
    public void updateFollowableItems(final Note note, boolean updateChildren) {
        if (note == null) {
            throw new IllegalArgumentException(
                    "NoteDao.updateFollowableItems(Note note) - 'note' can not be null");
        }
        try {
            this.handleUpdateFollowableItems(note, updateChildren);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'NoteDao.updateFollowableItems(Note note)' --> " + rt, rt);
        }
    }

}
