package com.communote.server.core.vo.query.blog;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.note.NoteConstants;

// TODO Please describe better what this class does. 
/**
 * Query instance to retrieve topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicTimelineParameters extends TimelineQueryParameters {

    /**
     * Instantiates a new user tagged blog query instance.
     */
    public TopicTimelineParameters() {
        super(new TaggingCoreItemUTPExtension());
    }

    /**
     * returns true.
     * 
     * @return true.
     */
    @Override
    public boolean needTransformListItem() {
        return true;
    }

    /**
     * Sort by the blog name ascending.
     */
    public void sortByBlogNameAsc() {
        addSortField(TaggingCoreItemUTPExtension.ALIAS_BLOG, BlogConstants.TITLE, SORT_ASCENDING);
        addSortField(TaggingCoreItemUTPExtension.ALIAS_BLOG, BlogConstants.NAMEIDENTIFIER,
                SORT_ASCENDING);
    }

    public void sortByDateDesc() {
        addSortField(TaggingCoreItemUTPExtension.ALIAS_BLOG, BlogConstants.CREATIONDATE,
                SORT_DESCENDING);
    }

    public void sortByLatestNote() {
        addSortField(TaggingCoreItemUTPExtension.ALIAS_NOTE, NoteConstants.CREATIONDATE,
                SORT_DESCENDING, "max");
    }

    /**
     * Transforms the BlogData. Sets the description for BlogData because the description is
     * not allow in query (clob in a oracle db environment)
     * 
     * @param resultItem
     *            The resultItem to transform
     * @return The transformed BlogData
     */
    @Override
    public IdentifiableEntityData transformResultItem(Object resultItem) {
        BlogData result = (BlogData) resultItem;
        Blog blog;
        try {
            blog = ServiceLocator.instance().getService(BlogManagement.class)
                    .getBlogById(result.getId(), false);
        } catch (BlogNotFoundException e) {
            throw BlogManagementHelper.convertException(e);
        } catch (BlogAccessException e) {
            throw BlogManagementHelper.convertException(e);
        }
        result.setDescription(blog.getDescription());
        return result;
    }

}
