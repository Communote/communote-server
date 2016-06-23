package com.communote.server.web.fe.widgets.admin.client.security;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.UnexpectedRollbackException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.ConfigurationManagementException;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for managing users.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientPermissionsWidget extends AbstractWidget {

    /**
     * Saves the user settings within a transaction.
     */
    private class SaveInTransaction implements RunInTransaction {

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute() throws TransactionException {
            Long defaultBlogId = getLongParameter(DEFAULT_BLOG_ID, -1);
            boolean allowAllReadWriteForUsers = BooleanUtils
                    .toBoolean(getParameter(ALLOW_ALL_READ_WRITE_FOR_USERS));
            boolean resetGlobalAccess = !allowAllReadWriteForUsers
                    && BooleanUtils.toBoolean(getParameter(RESET_ACCESS_GLOBAL_ACCESS));
            String allowPublicAccess = Boolean.toString(BooleanUtils
                    .toBoolean(getParameter(ALLOW_PUBLIC_ACCESS)));
            boolean activateDefaultBlog = BooleanUtils
                    .toBoolean(getParameter(ACTIVATE_DEFAULT_BLOG));

            boolean createPersonalTopic = BooleanUtils
                    .toBoolean(getParameter(CREATE_PERSONAL_TOPIC));
            boolean allowTopicCreateForAllUsers = BooleanUtils
                    .toBoolean(getParameter(ALLOW_TOPIC_CREATE_FOR_ALL_USERS));

            // set no default blog
            if (!activateDefaultBlog || defaultBlogId < 0) {
                CommunoteRuntime.getInstance().getConfigurationManager().deactivateDefaultBlog();
            } else {
                // set new Default Blog
                // TODO this is bad style: BE only works because the blog is loaded in same
                // Hibernate session. Moreover access check is missing in BE.
                Blog blog;
                try {
                    blog = ServiceLocator.instance().getService(BlogManagement.class)
                            .getBlogById(defaultBlogId, true);
                } catch (BlogNotFoundException e) {
                    throw BlogManagementHelper.convertException(e);
                } catch (BlogAccessException e) {
                    throw BlogManagementHelper.convertException(e);
                }
                CommunoteRuntime.getInstance().getConfigurationManager().setDefaultBlog(blog);
            }
            Map<ClientConfigurationPropertyConstant, String> map = new HashMap<ClientConfigurationPropertyConstant, String>();
            map.put(ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS,
                    Boolean.toString(allowAllReadWriteForUsers));
            map.put(ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS, allowPublicAccess);
            map.put(ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS,
                    Boolean.toString(allowTopicCreateForAllUsers));
            map.put(ClientProperty.CREATE_PERSONAL_BLOG, Boolean.toString(createPersonalTopic));
            if (resetGlobalAccess) {
                ServiceLocator.instance().getService(BlogManagement.class).resetGlobalPermissions();
                MessageHelper.saveMessageFromKey(getRequest(),
                        "client.user.management.save.reset.success");
            }
            try {
                CommunoteRuntime.getInstance().getConfigurationManager()
                .updateClientConfigurationProperties(map);
            } catch (ConfigurationManagementException e) {
                LOGGER.error("Error setting configuration", e);
                // TODO reset values?
            }

            MessageHelper.saveMessageFromKey(getRequest(), "client.user.management.save.success");
        }
    }

    /** default blog checkbox */
    private static final String ACTIVATE_DEFAULT_BLOG = "activateDefaultBlog";
    /** The default blog input field name */
    private static final String DEFAULT_BLOG_TITLE = "defaultBlogTitle";
    /** The default blog hidden field name (id) */
    private static final String DEFAULT_BLOG_ID = "defaultBlogId";
    /** ALLOW_ALL_READ_WRITE_FOR_USERS. */
    private static final String ALLOW_ALL_READ_WRITE_FOR_USERS = "allowAllReadWriteForUsers";
    /** RESET acccess controls for all users. */
    private static final String RESET_ACCESS_GLOBAL_ACCESS = "resetGlobalAccess";
    /** ALLOW_PUBLIC_ACCESS. */
    private static final String ALLOW_PUBLIC_ACCESS = "allowPublicAccess";
    /** allow topic create for all users */
    private static final String ALLOW_TOPIC_CREATE_FOR_ALL_USERS = "allowTopicCreateForAllUsers";
    /** create personal topic */
    private static final String CREATE_PERSONAL_TOPIC = "createPersonalTopic";

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(ClientPermissionsWidget.class);
    /** ACTION_UPDATE */
    private static final String ACTION_UPDATE = "update";
    /** PARAMETER_ACTION */
    private static final String PARAMETER_ACTION = "action";

    /** ACTION_UPDATE */
    private final Map<String, String> parameterMap = new HashMap<String, String>();

    private Blog getDefaultBlog() {
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
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.admin.client.security.permissions";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {

        String action = getParameter(PARAMETER_ACTION);
        if (ACTION_UPDATE.equals(action)) {
            saveUserSettings();
        }

        getRequest()
                .setAttribute(
                        ALLOW_ALL_READ_WRITE_FOR_USERS,
                        ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS
                                .getValue(ClientConfigurationHelper.DEFAULT_ALLOW_ALL_READ_WRITE_FOR_USERS));
        getRequest().setAttribute(
                ALLOW_PUBLIC_ACCESS,
                ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS
                        .getValue(ClientConfigurationHelper.DEFAULT_ALLOW_PUBLIC_ACCESS));
        getRequest().setAttribute(
                CREATE_PERSONAL_TOPIC,
                ClientProperty.CREATE_PERSONAL_BLOG
                        .getValue(ClientProperty.DEFAULT_CREATE_PERSONAL_BLOG));
        getRequest().setAttribute(
                ALLOW_TOPIC_CREATE_FOR_ALL_USERS,
                ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS
                        .getValue(ClientProperty.DEFAULT_ALLOW_TOPIC_CREATE_FOR_ALL_USERS));

        Blog blog = getDefaultBlog();

        getRequest().setAttribute(ACTIVATE_DEFAULT_BLOG, blog != null);
        getRequest().setAttribute(DEFAULT_BLOG_ID, blog != null ? blog.getId() : StringUtils.EMPTY);
        getRequest().setAttribute(DEFAULT_BLOG_TITLE,
                blog != null ? blog.getTitle() : StringUtils.EMPTY);

        return parameterMap;
    }

    @Override
    protected void initParameters() {
        setParameter(DEFAULT_BLOG_ID, StringUtils.EMPTY);
        setParameter(DEFAULT_BLOG_TITLE, StringUtils.EMPTY);
    }

    /**
     * This method saves the user settings.
     */
    private void saveUserSettings() {
        try {
            ServiceLocator.findService(TransactionManagement.class)
            .execute(new SaveInTransaction());
        } catch (UnexpectedRollbackException e) {
            LOGGER.warn("This is not a problem here: " + e.getMessage());
        }
    }

}
