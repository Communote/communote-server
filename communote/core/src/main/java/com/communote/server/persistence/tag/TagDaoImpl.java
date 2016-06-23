package com.communote.server.persistence.tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.model.global.GlobalId;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.tag.TagConstants;
import com.communote.server.model.user.User;
import com.communote.server.persistence.helper.dao.ResultSpecificationHelper;

/**
 * @see com.communote.server.model.tag.Tag
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagDaoImpl extends TagDaoBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(TagDaoImpl.class);

    private final static String FIND_NOTES_WITH_TAG_QUERYS = "FROM " + NoteConstants.CLASS_NAME
            + " note JOIN note." + NoteConstants.TAGS + " tag WHERE tag."
            + TagConstants.ID + " = :tagId";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final int transform, final Tag tag) {
        if (tag.getName() == null) {
            throw new IllegalArgumentException("Tag.create - 'tag.name' can not be null");
        }
        if (!tag.getName().trim().equals(tag.getName())) {
            Exception e = new Exception("Tag name '" + tag.getName()
                    + "' is different then trimmed: " + tag.getName().trim());
            LOGGER.error("Ignoring but should still be fixed: {}", e.getMessage(), e);
        }
        tag.setDefaultName(tag.getDefaultName().trim());
        getHibernateTemplate().save(tag);
        return this.transformEntity(transform, tag);
    }

    @Override
    public List<Long> getFollowers(Long tagId) {
        Tag tag = load(tagId);
        ArrayList<Long> result = new ArrayList<>();
        if (tag != null) {
            Iterator<User> followersIterator = tag.getFollowId().getFollowers().iterator();
            while (followersIterator.hasNext()) {
                result.add(followersIterator.next().getId());
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TagData> handleFindByPrefix(String prefix,
            ResultSpecification filterSpecification) {
        String mainQuery = "select new " + TagData.class.getName() + "(tag."
                + TagConstants.DEFAULTNAME + ") from "
                + TagConstants.CLASS_NAME + " tag where tag." + TagConstants.TAGSTORETAGID
                + " like ? order by tag." + TagConstants.DEFAULTNAME;

        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
        Query query = session.createQuery(mainQuery);
        String tagQueryValue = MatchMode.START.toMatchString(prefix.toLowerCase(Locale.ENGLISH));
        query.setString(0, tagQueryValue);
        ResultSpecificationHelper.configureQuery(query, null, filterSpecification);
        return query.list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Tag handleFindByTagStore(String tagStoreTagId, String tagStoreAlias) {
        Criteria criteria = getSession().createCriteria(Tag.class);
        criteria.add(Restrictions.eq(TagConstants.TAGSTORETAGID, tagStoreTagId));
        criteria.add(Restrictions.eq(TagConstants.TAGSTOREALIAS, tagStoreAlias));
        return (Tag) criteria.uniqueResult();
    }

    /**
     * This merges the two tags if a second tag is given.
     * 
     * @param oldTagId
     *            Id of the old tag.
     * @param newTagId
     *            If of the new tag. If this is null, the method returns silently.
     */
    private void mergeTag(long oldTagId, Long newTagId) {
        if (newTagId == null) {
            return;
        }
        LOGGER.debug("Merge old tag {} with new tag {}", oldTagId, newTagId);
        Tag oldTag = load(oldTagId);
        Tag newTag = load(newTagId);
        if (oldTag == null || newTag == null) {
            throw new IllegalArgumentException("Either the old or new tag doesn't exist.");
        }
        Session session = getSession();

        // Update Tag Mapping with batch processing
        // TODO this will still put every Tag in the 2nd level cache. We should probably
        // setCacheMode(CacheMode.IGNORE).
        Query notesQuery = session.createQuery(FIND_NOTES_WITH_TAG_QUERYS);
        notesQuery.setLong("tagId", oldTagId);
        // TODO in MySQL this can lead to OutOfMemory because the MySQL JDBC driver simulates scroll
        // (fetches all and iterates through). See mysql jdbc driver manual (reference manual ->
        // implementation notes -> resultset) or
        // http://www.numerati.com/2012/06/26/reading-large-result-sets-with-hibernate-and-mysql/
        // for details and workarounds
        ScrollableResults notes = notesQuery.scroll(ScrollMode.FORWARD_ONLY);
        int count = 0;
        while (notes.next()) {
            Note note = (Note) notes.get(0);
            note.getTags().remove(oldTag);
            note.getFollowableItems().remove(oldTag.getFollowId());
            note.getTags().add(newTag);
            note.getFollowableItems().add(newTag.getFollowId());
            // TODO this not that useful. The flush and clear interval should be aligned with JDBC
            // batch size which we do not set anywhere via property hibernate.jdbc.batch_size. See
            // Hibernate docu for details. The default batch size is probably JDBC driver specific.
            // Additionally batch ordering could be enabled for more performance
            // (hibernate.order_updates).
            if (++count % 40 == 0) {
                session.flush();
                session.clear();
            }
        }

        // due to flush and clear we have to reload the old and new tag entities into the session
        /*
         * oldTag = load(oldTagId); newTag = load(newTagId); Iterator<User> followersIterator
         * = oldTag.getFollowId().getFollowers().iterator(); while (followersIterator.hasNext()) {
         * User oldTagFollower = followersIterator.next();
         * oldTagFollower.getFollowedItems().remove(oldTag.getFollowId());
         * followersIterator.remove(); oldTagFollower.getFollowedItems().add(newTag.getFollowId());
         * newTag.getFollowId().getFollowers().add(oldTagFollower); }
         */
        // better flush and clear so that there is enough memory for the remove operation
        session.flush();
        session.clear();
    }

    /**
     * @see TagDao#remove(Tag)
     */
    @Override
    public void remove(Tag tag) {
        if (tag == null) {
            return;
        }

        GlobalId followId = tag.getFollowId();

        Session session = getSession();
        Query notesQuery = session.createQuery(FIND_NOTES_WITH_TAG_QUERYS);
        notesQuery.setLong("tagId", tag.getId());
        // TODO see TODOs in mergeTag!
        ScrollableResults notes = notesQuery.scroll(ScrollMode.FORWARD_ONLY);
        int count = 0;
        while (notes.next()) {
            Note note = (Note) notes.get(0);
            note.getTags().remove(tag);
            note.getFollowableItems().remove(followId);
            if (++count % 40 == 0) { // From Hibernate reference manual
                session.flush();
                session.clear();
                // re-associate tag with session to avoid hibernate exceptions
                tag = load(tag.getId());
            }
        }
        this.getHibernateTemplate().delete(tag);
    }

    /**
     * This method removes the given tag.
     * 
     * @param oldTagId
     *            Id of the tag to delete.
     * @param newTagId
     *            Id of an optional new tag, the data of the old tag should be assigned to.
     */
    @Override
    public void removeNoteTag(long oldTagId, Long newTagId) {
        mergeTag(oldTagId, newTagId);
        remove(oldTagId);
    }
}
