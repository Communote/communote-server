package com.communote.server.web.fe.widgets.blog;

import static com.communote.server.web.fe.widgets.WidgetConstants.PARAM_BLOG_ID;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.converter.blog.BlogToDetailBlogListItemConverter;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.core.filter.listitems.blog.member.BlogRoleEntityListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.blog.BlogMemberManagementQuery;
import com.communote.server.core.vo.query.blog.BlogMemberManagementQueryParameters;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;
import com.communote.server.web.fe.widgets.WidgetConstants;

/**
 * Widget for showing all members of a blog
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogMemberManagementWidget extends AbstractPagedListWidget<BlogRoleEntityListItem> {

    private static final BlogMemberManagementQuery QUERY = QueryDefinitionRepository.instance()
            .getQueryDefinition(BlogMemberManagementQuery.class);

    private BlogRole blogRole;

    private DetailBlogListItem blog;

    private boolean topicExists;
    private boolean access;

    /**
     * @return the blog role for all users
     */
    public BlogRole getAllUserGroupRole() {
        BlogRole role = null;
        if (blog != null) {

            if (blog.isAllCanWrite()) {
                role = BlogRole.MEMBER;
            } else if (blog.isAllCanRead()) {
                role = BlogRole.VIEWER;
            }
        }
        return role;
    }

    /**
     * @return the blog
     */
    public DetailBlogListItem getBlog() {
        return this.blog;
    }

    /**
     * @return the userGroupRole
     */
    public BlogRole getBlogRole() {
        return blogRole;
    }

    /**
     * {@inheritDoc}
     *
     * @return "core.widget.blog.group.member.management.list"
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.blog.group.member.management.list";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PageableList<BlogRoleEntityListItem> handleQueryList() {
        PageableList<BlogRoleEntityListItem> blogRoles = PageableList.emptyList();
        long blogId = getLongParameter(PARAM_BLOG_ID, 0);

        try {
            getRequest().setAttribute(WidgetConstants.PARAM_BLOG_ID, blogId);
            BlogMemberManagementQueryParameters queryParameters = QUERY.createInstance();
            queryParameters.setResultSpecification(getResultSpecification());
            queryParameters.setBlogId(blogId);
            queryParameters.setIncludeStatusFilter(new UserStatus[] { UserStatus.INVITED,
                    UserStatus.ACTIVE, UserStatus.CONFIRMED, UserStatus.TEMPORARILY_DISABLED });
            BlogToDetailBlogListItemConverter<DetailBlogListItem> converter = new BlogToDetailBlogListItemConverter<DetailBlogListItem>(
                    DetailBlogListItem.class, false, false, false, false, false, null);
            blog = ServiceLocator
                    .instance()
                    .getService(TopicPermissionManagement.class)
                    .hasAndGetWithPermission(blogId,
                            TopicPermissionManagement.PERMISSION_VIEW_TOPIC_DETAILS, converter);
            topicExists = true;
            blogRole = ServiceLocator.findService(BlogRightsManagement.class).getRoleOfCurrentUser(
                    blogId, true);
            getRequest().setAttribute("isBlogManager", BlogRole.MANAGER.equals(blogRole));
            getRequest().setAttribute("blogName", blog.getTitle());
            if (blogRole != null || blog.isAllCanRead() || blog.isAllCanWrite()) {
                access = true;
                blogRoles = ServiceLocator.findService(QueryManagement.class).query(QUERY,
                        queryParameters);
            }
            setPageInformation(queryParameters, blogRoles);
        } catch (BlogAccessException e) {
            topicExists = true;
        } catch (NotFoundException e) {
            // just return null
        }
        getRequest().setAttribute(
                "roles",
                BlogRoleHelper.getBlogRoles(SessionHandler.instance()
                        .getCurrentLocale(getRequest())));
        getRequest().setAttribute("userGroupRoleLiterals",
                BlogRoleHelper.getBlogRolesSortedByAccess());
        getRequest().setAttribute("editMode", getBooleanParameter("editMode", false));
        return blogRoles;
    }

    /**
     * Whether the current user has access to the topic.
     *
     * @return true if the topic exists and the user has access, otherwise false
     */
    public boolean hasAccess() {
        return access;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        super.initParameters();
        setParameter(PARAM_BLOG_ID, StringUtils.EMPTY);
    }

    /**
     * @return true if client manager allows to set all can read/write for blogs
     */
    public boolean isAllCanReadWriteAllowed() {
        ClientConfigurationProperties conf = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        return SecurityHelper.isClientManager()
                || conf.getProperty(ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS,
                        ClientConfigurationHelper.DEFAULT_ALLOW_ALL_READ_WRITE_FOR_USERS);
    }

    /**
     * @return true if the user can manage this blog
     */
    public boolean isManager() {
        return BlogRole.MANAGER.equals(blogRole);
    }

    /**
     * @return whether the topic for which the roles should be returned exists
     */
    public boolean topicExists() {
        return topicExists;
    }
}
