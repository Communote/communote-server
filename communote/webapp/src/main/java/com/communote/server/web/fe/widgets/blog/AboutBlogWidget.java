package com.communote.server.web.fe.widgets.blog;

import static com.communote.server.web.fe.widgets.WidgetConstants.PARAM_BLOG_ID;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.blog.helper.MailBasedPostingHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.converter.blog.BlogToDetailBlogListItemConverter;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for showing information about a blog
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AboutBlogWidget extends AbstractWidget {

    private final static String LONG_WORDS_PATTERN = "(\\S{50})(\\S{10,20})";

    private DetailBlogListItem blog;

    private boolean access;
    private boolean currentUserFollowsBlog = false;

    /**
     * @return the email address or {@code null} if the mail fetcher is disabled blogIdentifier is
     *         {@code null} or getCurrentEmailServerDomain() returns {@code null}
     */
    public String getBlogEmailAddress() {
        return MailBasedPostingHelper.getBlogEmailAddress(blog.getBlogEmail());
    }

    /**
     * @return the mailto string or {@code null} if the mail fetcher is not enabled
     */
    public String getBlogMailTo() {
        return MailBasedPostingHelper.getBlogMailToFromEmailAddress(blog.getBlogEmail(),
                blog.getAlias());
    }

    /**
     * @return the userLocale
     */
    public Locale getCurrentUserLocale() {
        return SessionHandler.instance().getCurrentLocale(getRequest());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        Long blogId = getLongParameter(PARAM_BLOG_ID, 0);

        try {
            TopicPermissionManagement topicPermissionManagement = ServiceLocator.instance()
                    .getService(TopicPermissionManagement.class);
            BlogRightsManagement topicRightsManagement = ServiceLocator.instance()
                    .getService(BlogRightsManagement.class);
            BlogToDetailBlogListItemConverter<DetailBlogListItem> converter =
                    new BlogToDetailBlogListItemConverter<DetailBlogListItem>(
                            DetailBlogListItem.class, false, true, false, true, false,
                            topicRightsManagement, getCurrentUserLocale());
            this.blog = topicPermissionManagement.hasAndGetWithPermission(blogId,
                    TopicPermissionManagement.PERMISSION_VIEW_TOPIC_DETAILS, converter);
            this.access = true;
            if (blog.getDescription() != null) {
                blog.setDescription(blog.getDescription().replaceAll(LONG_WORDS_PATTERN, "$1 $2"));
            }
            currentUserFollowsBlog = ServiceLocator.instance().getService(FollowManagement.class)
                    .followsBlog(blogId);
            this.setResponseMetadata("blogAlias", blog.getAlias());
        } catch (BlogAccessException e) {
            // return null
        } catch (NotFoundException e) {
            this.access = true;
        }
        return blog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.blog.about";
    }

    /**
     * 
     * @return the time zone of the current user
     */
    public TimeZone getTimeZone() {
        return UserManagementHelper.getEffectiveUserTimeZone();
    }

    /**
     * @return the time zone id of the currend user
     */
    public String getTimeZoneId() {
        TimeZone timeZone = UserManagementHelper.getEffectiveUserTimeZone(SecurityHelper
                .assertCurrentUserId());
        return timeZone.getID();
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
        setParameter(PARAM_BLOG_ID, StringUtils.EMPTY);
    }

    /**
     * 
     * @return {@code true} if the current user follows this blog
     */
    public boolean isCurrentUserFollowsBlog() {
        return currentUserFollowsBlog;
    }

    /**
     * 
     * @return {@code true} if single address mode is enabled
     */
    public boolean isRunningInSingleAddressMode() {
        return MailBasedPostingHelper.isRunningInSingleAddressMode();
    }
}
