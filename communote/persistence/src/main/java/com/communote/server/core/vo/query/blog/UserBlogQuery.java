package com.communote.server.core.vo.query.blog;

import java.util.ArrayList;
import java.util.Iterator;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.UserBlogData;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.blog.BlogRole;

/**
 * QueryDefinition to retrieve BlogListItems with details about access rights for a given user. If
 * the query instance does not define a user ID the ID of the current user will be taken.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserBlogQuery extends BlogQuery<BlogData, BlogQueryParameters> {
    /**
     * Constructor.
     */
    public UserBlogQuery() {
        super(BlogData.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(BlogQueryParameters queryParameters) {
        if (queryParameters.getUserId() == null && !SecurityHelper.isPublicUser()) {
            queryParameters.setUserId(SecurityHelper.assertCurrentUserId());
        }
        return super.buildQuery(queryParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableList postQueryExecution(BlogQueryParameters queryInstance, PageableList result) {
        ArrayList<UserBlogData> userBlogListItems = new ArrayList<UserBlogData>(result
                .size());
        for (Iterator it = result.iterator(); it.hasNext();) {
            BlogData item = (BlogData) it.next();
            userBlogListItems.add(transformListItem(item));
        }
        PageableList<UserBlogData> augmentedResult = new PageableList<UserBlogData>(
                userBlogListItems);
        augmentedResult.setMinNumberOfElements(result.getMinNumberOfElements());
        return augmentedResult;
    }

    /**
     * Transforms a BlogData into a UserBlogData.
     *
     * @param item
     *            the item to transform
     * @return the transformed item
     */
    private UserBlogData transformListItem(BlogData item) {
        BlogRole role = ServiceLocator.findService(BlogRightsManagement.class)
                .getRoleOfCurrentUser(item.getId(), false);
        return new UserBlogData(role, item.getNameIdentifier(), item.getDescription(), item
                .getId(), item.getTitle(), item.getLastModificationDate());
    }

}
