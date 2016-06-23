package com.communote.server.persistence.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.global.GlobalId;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.property.PropertyConstants;
import com.communote.server.model.property.StringPropertyConstants;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.CommunoteEntityConstants;

/**
 * @see com.communote.server.model.note.Note
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteDaoImpl extends NoteDaoBase {

    private final static String AUTOSAVE_QUERY;
    private final static String POSTS_OF_USER_QUERY;
    private final static String DISCUSSION_NOTE_IDS_QUERY;
    private final static String DISCUSSION_NOTES_WITH_INCORRECT_TOPIC_QUERY;
    static {

        POSTS_OF_USER_QUERY = "from " + NoteConstants.CLASS_NAME + " where " + NoteConstants.USER
                + "." + CommunoteEntityConstants.ID + " = ? order by " + NoteConstants.ID + " DESC";

        AUTOSAVE_QUERY = "select note." + NoteConstants.ID + " from " + NoteConstants.CLASS_NAME
                + " note where note." + NoteConstants.STATUS + " = ? AND note."
                + NoteConstants.USER + "." + CommunoteEntityConstants.ID + " = ?";

        DISCUSSION_NOTES_WITH_INCORRECT_TOPIC_QUERY = "from " + NoteConstants.CLASS_NAME
                + " where " + NoteConstants.DISCUSSIONID + "=? and " + NoteConstants.BLOG
                + ".id !=?";

        DISCUSSION_NOTE_IDS_QUERY = "SELECT id FROM " + NoteConstants.CLASS_NAME
                + " note WHERE note." + NoteConstants.PARENT + " is not null AND note."
                + NoteConstants.STATUS + " = '" + NoteStatus.PUBLISHED + "'" + " AND note."
                + NoteConstants.DISCUSSIONID + "=? ORDER BY note." + NoteConstants.CREATIONDATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final int transform, final Note note) {
        if (note == null) {
            throw new IllegalArgumentException("Note.create - 'note' can not be null");
        }
        this.getHibernateTemplate().save(note);
        note.setGlobalId(getGlobalIdDao().createGlobalId(note));
        return this.transformEntity(transform, note);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Note forceLoad(Long id) {
        getHibernateTemplate().getSessionFactory().getCache()
                .evictEntity(NoteConstants.CLASS_NAME, id);
        return load(id);
    }

    /**
     *
     * @param discussionId
     *            discussion id
     * @return all note ids (including DMs etc.) belonging to the dicussion
     */
    private List<Number> getAllNoteIdsForDiscussionId(Long discussionId) {
        Query query;
        query = this.getSession().createQuery(
                "SELECT " + NoteConstants.ID + " from " + NoteConstants.CLASS_NAME + " note "
                        + " where note." + NoteConstants.DISCUSSIONID + " = ?");
        query.setParameter(0, discussionId);
        @SuppressWarnings("unchecked")
        List<Number> noteIds = query.list();
        return noteIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Note handleFindLatestNote() {
        Criteria criteria = getSession().createCriteria(Note.class);
        ProjectionList projList = Projections.projectionList();
        projList.add(Projections.max(NoteConstants.ID));
        criteria.setProjection(projList);
        List<?> result = criteria.list();
        if (result != null && result.size() == 1 && result.get(0) != null) {
            Long id = (Long) result.get(0);
            return load(id);
        }
        return null;
    }

    @Override
    protected Note handleFindNearestNote(long noteId, Date creationDate, boolean younger) {
        Query query = getSession().createQuery(
                "from " + NoteConstants.CLASS_NAME + " where " + NoteConstants.ID
                        + (younger ? ">" : "<") + " :noteId AND " + NoteConstants.CREATIONDATE
                        + " = :creationDate order by " + NoteConstants.ID
                        + (younger ? " ASC" : " DESC"));
        query.setLong("noteId", noteId);
        query.setParameter("creationDate", creationDate);
        query.setMaxResults(1);
        List<Note> list = query.list();
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Use CriteriaAPI
    protected Long handleGetAutosave(Long userId, Long noteId, Long parentNoteId,
            FilterNoteProperty[] properties) {

        StringBuilder query = new StringBuilder(AUTOSAVE_QUERY);
        List<Object> args = new ArrayList<Object>(5);
        args.add(NoteStatus.AUTOSAVED);
        args.add(userId);

        query.append(" AND note." + NoteConstants.ORIGIN);
        if (noteId == null) {
            query.append(" is null");
        } else {
            query.append("." + NoteConstants.ID + " = ?");
            args.add(noteId);
        }
        query.append(" AND note." + NoteConstants.PARENT);
        if (parentNoteId == null) {
            query.append(" is null");
        } else {
            query.append("." + NoteConstants.ID + " = ?");
            args.add(parentNoteId);
        }
        renderFilterProperties(properties, query, args);
        query.append(" GROUP BY note." + NoteConstants.ID);
        List<?> result = getHibernateTemplate().find(query.toString(), args.toArray());
        if (result.isEmpty()) {
            return null;
        } else {
            return (Long) result.get(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Long> handleGetFavoriteNoteIds(Long userId, Long lowerBound,
            Long upperBound) {
        List<Object> args = new ArrayList<Object>(2);
        args.add(userId);

        StringBuilder query = new StringBuilder("select note.id from " + NoteConstants.CLASS_NAME
                + " note left join note." + NoteConstants.FAVORITEUSERS
                + " favoriteUser where favoriteUser." + CommunoteEntityConstants.ID + " = ?");
        if (lowerBound >= 0) {
            query.append(" AND note." + NoteConstants.ID + " >= ?");
            args.add(lowerBound);
        }
        if (upperBound >= 0) {
            query.append(" AND note." + NoteConstants.ID + " <= ?");
            args.add(upperBound);
        }
        List<Long> result = getHibernateTemplate().find(query.toString(),
                args.toArray(new Object[args.size()]));
        return new HashSet<Long>(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Long> handleGetNoteIdsOfDiscussion(Long discussionId) {
        List<Long> result = getHibernateTemplate().find(DISCUSSION_NOTE_IDS_QUERY, discussionId);
        if (result == null) {
            return new ArrayList<Long>();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.tag.NoteDao#getTaggedItemsByTag(Long)
     */
    @Override
    protected List<Note> handleGetNotesByTag(Long tagId) {
        return getHibernateTemplate().find(
                "from " + NoteConstants.CLASS_NAME
                        + " uti left join fetch uti.tags as tags where tags.id = ?", tagId);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.tag.NoteDao#getTaggedItemCount()
     */
    @Override
    protected long handleGetNotesCount() {
        return (Long) getHibernateTemplate().find(
                "select count(*) from " + NoteConstants.CLASS_NAME).get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Note> handleGetNotesForBlog(Long blogId, Long firstNoteId, Integer limit) {
        Criteria criteria = getSession().createCriteria(Note.class);
        criteria.createAlias(NoteConstants.BLOG, "blog").setFetchMode("blog", FetchMode.SELECT);
        criteria.add(Restrictions.eq("blog." + BlogConstants.ID, blogId));
        if (firstNoteId != null && firstNoteId > 0) {
            criteria.add(Restrictions.ge(NoteConstants.ID, firstNoteId));
        }
        if (limit != null && limit > 0) {
            criteria.setMaxResults(limit);
        }
        criteria.addOrder(Order.asc(NoteConstants.ID));
        return criteria.list();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<Note> handleGetNotesOfUser(Long userId) {

        return getHibernateTemplate().find(POSTS_OF_USER_QUERY, userId);
    }

    @Override
    protected int handleGetNumberOfFavorites(Long noteId) {

        StringBuilder query = new StringBuilder("select count(*) from " + NoteConstants.CLASS_NAME
                + " note inner join note." + NoteConstants.FAVORITEUSERS
                + " favoriteUser where note." + CommunoteEntityConstants.ID + " = ?");

        List<Number> result = getHibernateTemplate().find(query.toString(), noteId);

        return result == null || result.size() == 0 ? 0 : result.get(0).intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUpdateFollowableItems(Note note, boolean updateChildren) {
        // do nothing if it's an autosave
        if (note.getStatus().equals(NoteStatus.AUTOSAVED)) {
            return;
        }
        note.getFollowableItems().clear();
        GlobalId followableItem = note.getUser().getFollowId();
        note.getFollowableItems().add(followableItem);
        followableItem = note.getBlog().getFollowId();
        note.getFollowableItems().add(followableItem);
        // add root of discussion as followable item
        followableItem = note.getFollowId();
        note.getFollowableItems().add(followableItem);
        // handle tags
        Set<Tag> tags = note.getTags();
        for (Tag tag : tags) {
            note.getFollowableItems().add(tag.getFollowId());
        }
        if (updateChildren && note.getChildren() != null) {
            for (Note child : note.getChildren()) {
                updateFollowableItems(child, updateChildren);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasInconsistentTopics(Long discussionId, Long topicId) {
        return getHibernateTemplate().find(DISCUSSION_NOTES_WITH_INCORRECT_TOPIC_QUERY,
                discussionId, topicId).size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveToTopic(Long discussionId, Blog newTopic) {
        if (newTopic == null) {
            throw new IllegalArgumentException("The new topic may not be null.");
        }

        // update the topic id on all notes of the discussion
        Query query = this.getSession().createQuery(
                "UPDATE " + NoteConstants.CLASS_NAME + " note SET note." + NoteConstants.BLOG
                        + "= ? WHERE note." + NoteConstants.DISCUSSIONID + " = ?");
        query.setParameter(0, newTopic);
        query.setParameter(1, discussionId);
        query.setCacheMode(CacheMode.IGNORE);
        query.executeUpdate();

        // update the crawl last modification date on all notes of the discussion
        query = this.getSession().createQuery(
                "UPDATE " + NoteConstants.CLASS_NAME + " note SET note."
                        + NoteConstants.CRAWLLASTMODIFICATIONDATE + "= ? WHERE note."
                        + NoteConstants.DISCUSSIONID + " = ?");
        query.setParameter(0, new Timestamp(new Date().getTime()));
        query.setParameter(1, discussionId);
        query.setCacheMode(CacheMode.IGNORE);
        query.executeUpdate();

        // evict all notes from the cache
        List<Number> noteIds = getAllNoteIdsForDiscussionId(discussionId);

        if (noteIds != null) {
            for (Number noteId : noteIds) {
                getHibernateTemplate().getSessionFactory().getCache()
                        .evictEntity(Note.class, noteId);
            }
        }
    }

    /**
     * Renders the property filters into the query.
     *
     * @param properties
     *            The properties.
     * @param query
     *            The query.
     * @param args
     *            The arguments.
     */
    private void renderFilterProperties(FilterNoteProperty[] properties, StringBuilder query,
            List<Object> args) {
        if (properties == null || properties.length == 0) {
            return;
        }
        List<FilterNoteProperty> genericExcludedFilter = new ArrayList<FilterNoteProperty>();
        int i = 0;
        propertiesLoop: for (; i < properties.length; i++) {
            FilterNoteProperty property = properties[i];
            if (StringUtils.isBlank(property.getPropertyValue()) && !property.isInclude()) {
                genericExcludedFilter.add(property);
                continue propertiesLoop;
            }
            args.add(property.getKeyGroup());
            args.add(property.getPropertyKey());
            String note = " note" + i;
            String noteProperty = " noteProperty" + i;
            query.append(" AND note." + NoteConstants.ID + " IN ( SELECT " + note + "."
                    + NoteConstants.ID + " FROM " + NoteConstants.CLASS_NAME + note);
            query.append(" left join " + note + "." + NoteConstants.PROPERTIES + noteProperty);
            query.append(" WHERE " + note + "." + NoteConstants.ID + " = note." + NoteConstants.ID
                    + " AND " + noteProperty + "." + PropertyConstants.KEYGROUP + " = ?  AND");
            query.append(noteProperty + "." + PropertyConstants.PROPERTYKEY + " = ? ");
            if (StringUtils.isNotBlank(property.getPropertyValue())) {
                query.append(" AND " + noteProperty + "." + StringPropertyConstants.PROPERTYVALUE);
                if (!property.isInclude()) {
                    query.append(" !");
                }
                query.append(" = ?");
                args.add(property.getPropertyValue());
            }
            query.append(")");
        }
        for (int e = 0; e < genericExcludedFilter.size(); e++) {
            String note = " note" + (e + i + 1);
            String noteProperty = " noteProperty" + (e + i + 1);
            query.append(" AND note." + NoteConstants.ID + " NOT IN ( SELECT " + note + "."
                    + NoteConstants.ID + " FROM " + NoteConstants.CLASS_NAME + note);
            query.append(" left join " + note + "." + NoteConstants.PROPERTIES + noteProperty);
            query.append(" WHERE " + note + "." + NoteConstants.ID + " = note." + NoteConstants.ID
                    + " AND " + noteProperty + "." + PropertyConstants.KEYGROUP + " = ?  AND");
            query.append(noteProperty + "." + PropertyConstants.PROPERTYKEY + " = ? )");
            FilterNoteProperty property = genericExcludedFilter.get(e);
            args.add(property.getKeyGroup());
            args.add(property.getPropertyKey());
        }
    }
}
