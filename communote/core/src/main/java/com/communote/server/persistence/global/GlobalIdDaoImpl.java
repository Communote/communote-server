package com.communote.server.persistence.global;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.global.GlobalId;
import com.communote.server.model.global.GlobalIdConstants;
import com.communote.server.model.note.Note;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.global.GlobalIdDaoBase;
import com.communote.server.persistence.global.GlobalIdHelper;
import com.communote.server.persistence.global.GlobalIdType;

/**
 * @see com.communote.server.model.global.GlobalId
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GlobalIdDaoImpl extends GlobalIdDaoBase {

    /**
     * Create a global id
     * 
     * @param type
     *            the type of the global id
     * @param id
     *            the id of the element
     * @return the global id created
     */
    private GlobalId createGlobalId(GlobalIdType type, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null! type=" + type.name());
        }
        GlobalId globalId = GlobalId.Factory.newInstance();
        String globalIdString = GlobalIdHelper.buildGlobalIdIString(type, id);
        globalId.setGlobalIdentifier(globalIdString);
        GlobalId globalIdentifier = findByGlobalIdentifier(globalIdString);
        if (globalIdentifier != null) {
            return globalIdentifier;
        }
        create(globalId);
        return globalId;
    }

    /**
     * Create a global id for the attachment
     * 
     * @param attachment
     *            the attachment
     * @return the global id of the attachment
     */
    @Override
    protected GlobalId handleCreateGlobalId(Attachment attachment) {
        return createGlobalId(GlobalIdType.ATTACHMENT, attachment.getId());
    }

    /**
     * Create a global id for the blog
     * 
     * @param blog
     *            the blog
     * @return the global id of the blog
     */
    @Override
    protected GlobalId handleCreateGlobalId(Blog blog) {
        return createGlobalId(GlobalIdType.BLOG, blog.getId());
    }

    /**
     * Create a global id for the group
     * 
     * @param group
     *            the group
     * @return the global id of the group
     */
    @Override
    protected GlobalId handleCreateGlobalId(Group group) {
        return createGlobalId(GlobalIdType.GROUP, group.getId());
    }

    /**
     * Create a global id for the user
     * 
     * @param user
     *            the user
     * @return the global id of the user
     */
    @Override
    protected GlobalId handleCreateGlobalId(User user) {
        return createGlobalId(GlobalIdType.USER, user.getId());
    }

    /**
     * Create a global id for the note
     * 
     * @param note
     *            the note
     * @return the global id of the note
     */
    @Override
    protected GlobalId handleCreateGlobalId(Note note) {
        return createGlobalId(GlobalIdType.NOTE, note.getId());
    }

    /**
     * Create a global id for the tag
     * 
     * @param tag
     *            the tag
     * @return the global id of the tag
     */
    @Override
    protected GlobalId handleCreateGlobalId(Tag tag) {
        return createGlobalId(GlobalIdType.TAG, tag.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GlobalId handleFindByGlobalIdentifier(String globalIdentifier) {
        Criteria criteria = getSession().createCriteria(GlobalId.class);
        criteria.add(Expression.eq(GlobalIdConstants.GLOBALIDENTIFIER, globalIdentifier));
        List result = criteria.list();
        if (result.size() == 0) {
            return null;
        }
        return (GlobalId) result.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GlobalId handleFindLatestGlobalId() {
        Criteria criteria = getSession().createCriteria(GlobalId.class);
        ProjectionList projList = Projections.projectionList();
        projList.add(Projections.max(GlobalIdConstants.ID));
        criteria.setProjection(projList);
        List result = criteria.list();
        if (result != null && result.size() == 1 && result.get(0) != null) {
            Long id = (Long) result.get(0);
            return load(id);
        }
        return null;
    }

    @Override
    public void remove(GlobalId globalId) {
        for (User user : globalId.getFollowers()) {
            user.getFollowedItems().remove(globalId);
        }
        globalId.getFollowers().clear();
        super.remove(globalId);
    }
}
