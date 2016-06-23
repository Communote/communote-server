package com.communote.server.core.common.velocity;

import java.io.IOException;
import java.util.Collection;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.CollectionConverter;
import com.communote.common.converter.Converter;
import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.MinimalBlogData;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.BlogManagementException;
import com.communote.server.core.converter.blog.BlogToBlogDataConverter;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.user.User;

/**
 * Topic specific tools.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicTool {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicTool.class);

    /**
     * Return the details of a topic, the properties will not be contained.
     *
     * @param blogId
     *            the ID of the topic
     * @return a list item with details or null if the blog does not exist or the user is not
     *         allowed to read it
     */
    public BlogData getBlog(Number blogId) {
        BlogToBlogDataConverter<BlogData> converter = new BlogToBlogDataConverter<BlogData>(
                BlogData.class, false);
        try {
            // with the Number we are flexible towards IDs that were extracted from a JSON
            // object
            BlogData blog = ServiceLocator.findService(BlogManagement.class).getBlogById(
                    blogId.longValue(), converter);
            return blog;
        } catch (BlogManagementException e) {
            // silently ignore and treat as not found
            LOGGER.debug("Retrieving blog {} failed with exception: {}", blogId, e.getMessage());
        } catch (BlogAccessException e) {
            // silently ignore and treat as not found
            LOGGER.debug("Retrieving blog {} failed because current user has no access", blogId);
        }
        return null;
    }

    /**
     * @param blogId
     *            The id of the blog to test.
     * @return The role of the current user for the given blog, will be "NONE" if the blog does not
     *         exist or the user has no access.
     */
    public String getBlogRole(Long blogId) {
        if (blogId == null || blogId < 0) {
            return "NONE";
        }
        BlogRole roleOfCurrentUser = ServiceLocator.findService(BlogRightsManagement.class)
                .getRoleOfCurrentUser(blogId, false);
        if (roleOfCurrentUser != null) {
            return roleOfCurrentUser.getValue();
        }
        return "NONE";
    }

    /**
     * @return The default topic.
     */
    public Blog getDefaultBlog() {
        Long defaultBlogId = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getDefaultBlogId();
        if (defaultBlogId != null) {
            BlogManagement blogManagement = ServiceLocator.findService(BlogManagement.class);
            try {
                return blogManagement.getBlogById(defaultBlogId, true);
            } catch (BlogNotFoundException e) {
                // ignore
            } catch (BlogAccessException e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * @return The id of the default blog, or empty string if there is none.
     */
    public Long getDefaultBlogId() {
        // TODO directly get the ID (without access check)?
        Blog defaultBlog = getDefaultBlog();
        if (defaultBlog == null) {
            return null;
        }
        return defaultBlog.getId();
    }

    /**
     * @return The role of the current user for the default blog. Result will be "NONE" if the user
     *         has no access to that topic.
     */
    public String getDefaultBlogRole() {
        Long defaultBlogId = getDefaultBlogId();
        return getBlogRole(defaultBlogId);
    }

    /**
     * Return the minimal set of information on a topic, even if the current user has no access to
     * the topic.
     *
     * @param blogId
     *            the ID of the topic
     * @return the blog details or null if the topic does not exist
     */
    public MinimalBlogData getMinimalBlogInfo(Number blogId) {
        return ServiceLocator.findService(BlogManagement.class).getMinimalBlogInfo(
                blogId.longValue());
    }

    /**
     * Method to get a list of users with the given role from the given topic.
     *
     * @param topicId
     *            The topics id.
     * @param role
     *            The role to get.
     * @return Collection of user names encapsulated in '
     *
     */
    public String getUsersWithRoleAsJson(long topicId, String role) {
        BlogRole topicRole = BlogRole.fromString(role);
        final UserManagement userManagement = ServiceLocator.findService(UserManagement.class);
        Collection<String> users = ServiceLocator.findService(BlogRightsManagement.class)
                .getMappedUsers(topicId, new CollectionConverter<UserToBlogRoleMapping, String>() {
                    @Override
                    public String convert(UserToBlogRoleMapping source) {
                        return UserNameHelper.getSimpleDefaultUserSignature(userManagement
                                .getUserById(source.getUserId(), new IdentityConverter<User>()));
                    }
                }, topicRole);
        try {
            return JsonHelper.getSharedObjectMapper().writeValueAsString(users).replace("\"", "'");
        } catch (JsonGenerationException e) {
            LOGGER.error(e.getMessage());
        } catch (JsonMappingException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return "[]";
    }

    /**
     * @param topicId
     *            The topics id.
     * @return True, if the topic has children, else false.
     */
    public boolean hasChildren(Long topicId) {
        Boolean result = null;
        if (topicId != null) {
            try {
                result = ServiceLocator.findService(BlogManagement.class).getBlogById(topicId,
                        new Converter<Blog, Boolean>() {
                            @Override
                            public Boolean convert(Blog source) {
                                return !source.getChildren().isEmpty();
                            }
                        });
            } catch (BlogAccessException e) {
                // silently ignore
                LOGGER.debug("Current user has no access to topic {}", topicId);
            }
        }
        return result != null && result;
    }

    /**
     * Set the external objects for this blog.
     *
     * @param blogId
     *            The blogs id.
     * @return List of external objects.
     */
    public boolean hasExternalObjects(Long blogId) {
        try {
            if (blogId != null) {
                return ServiceLocator.findService(ExternalObjectManagement.class)
                        .hasExternalObjects(blogId);
            }
        } catch (BlogNotFoundException e) {
            // not implemented
        } catch (BlogAccessException e) {
            // not implemented
        }
        return false;
    }

    /**
     * Can be used to check if the current user has a specific permission on a given blog.
     *
     * @param topicId
     *            The topics id.
     * @param permissionIdentifier
     *            The permission as String.
     * @return True, if the current user has the given permission on the given blog.
     */
    public boolean hasPermission(long topicId, String permissionIdentifier) {
        return ServiceLocator.findService(TopicPermissionManagement.class).hasPermission(topicId,
                permissionIdentifier);
    }

    /**
     * @param readAccessRequired
     *            if true the default blog will only be enabled if the current user has read access
     *            to that blog
     * @param writeAccessRequired
     *            if true the default blog will only be enabled if the current user has write access
     *            to that blog
     * @return whether the default blog is enabled and the given access is granted for the current
     *         user
     */
    public boolean isDefaultBlogEnabled(boolean readAccessRequired, boolean writeAccessRequired) {
        boolean enabled = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().isDefaultBlogEnabled();
        if (enabled && (readAccessRequired || writeAccessRequired)) {
            Blog defaultBlog = getDefaultBlog();
            // if null, current user no read access
            if (defaultBlog == null) {
                enabled = false;
            } else if (writeAccessRequired) {
                enabled = ServiceLocator.findService(BlogRightsManagement.class)
                        .currentUserHasWriteAccess(defaultBlog.getId(), false);
            }
        }
        return enabled;
    }
}
