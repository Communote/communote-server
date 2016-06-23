package com.communote.server.web.fe.portal.user.client.forms;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;

/**
 * The Class ClientPermissionsForm handles the input from the change of client permissions form.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientPermissionsForm {

    /** */
    private boolean allowAllReadWriteForUsers;

    private boolean allowPublicAccess;

    private boolean disablePublicBlog;

    private boolean resetGlobalAccess = false;

    private boolean allowTopicCreateForAllUsers;
    private boolean createPersonalTopic;

    /**
     * Instantiates a new client permission form.
     */
    public ClientPermissionsForm() {
        ClientConfigurationProperties clientConfigurationProperties = CommunoteRuntime
                .getInstance().getConfigurationManager().getClientConfigurationProperties();
        allowAllReadWriteForUsers = clientConfigurationProperties.getProperty(
                ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS,
                ClientConfigurationHelper.DEFAULT_ALLOW_ALL_READ_WRITE_FOR_USERS);
        disablePublicBlog = !clientConfigurationProperties.isDefaultBlogEnabled();
        allowPublicAccess = clientConfigurationProperties.getProperty(
                ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                ClientConfigurationHelper.DEFAULT_ALLOW_PUBLIC_ACCESS);
        createPersonalTopic = clientConfigurationProperties.getProperty(
                ClientProperty.CREATE_PERSONAL_BLOG, ClientProperty.DEFAULT_CREATE_PERSONAL_BLOG);
        allowTopicCreateForAllUsers = clientConfigurationProperties.getProperty(
                ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS,
                ClientProperty.DEFAULT_ALLOW_TOPIC_CREATE_FOR_ALL_USERS);
    }

    /**
     * @return True if all users are able to create global channels (all can read/write).
     */
    public boolean isAllowAllReadWriteForUsers() {
        return allowAllReadWriteForUsers;
    }

    /**
     * @return True if all users are able to allow public to access a blog.
     */
    public boolean isAllowPublicAccess() {
        return allowPublicAccess;
    }

    /**
     *
     * @return true if all users are allowed to create topics. false only client admins are allowed
     *         to do so
     */
    public boolean isAllowTopicCreateForAllUsers() {
        return allowTopicCreateForAllUsers;
    }

    /**
     *
     * @return true if personal topics are created for new users
     */
    public boolean isCreatePersonalTopic() {
        return createPersonalTopic;
    }

    /**
     * @return the disablePublicBlog
     */
    public boolean isDisablePublicBlog() {
        return disablePublicBlog;
    }

    /**
     * @return the resetGlobalAccess
     */
    public boolean isResetGlobalAccess() {
        return resetGlobalAccess;
    }

    /**
     * @param allowAllReadWriteForUsers
     *            True or false.
     */
    public void setAllowAllReadWriteForUsers(boolean allowAllReadWriteForUsers) {
        this.allowAllReadWriteForUsers = allowAllReadWriteForUsers;
    }

    /**
     * @param allowPublicAccess
     *            True or false.
     */
    public void setAllowPublicAccess(boolean allowPublicAccess) {
        this.allowPublicAccess = allowPublicAccess;
    }

    /**
     *
     * @param allowTopicCreateForAllUsers
     *            see {@link #isAllowTopicCreateForAllUsers()}
     */
    public void setAllowTopicCreateForAllUsers(boolean allowTopicCreateForAllUsers) {
        this.allowTopicCreateForAllUsers = allowTopicCreateForAllUsers;
    }

    /**
     *
     * @param createPersonalTopic
     *            see {@link #isCreatePersonalTopic()}
     */
    public void setCreatePersonalTopic(boolean createPersonalTopic) {
        this.createPersonalTopic = createPersonalTopic;
    }

    /**
     * @param disablePublicBlog
     *            the disablePublicBlog to set
     */
    public void setDisablePublicBlog(boolean disablePublicBlog) {
        this.disablePublicBlog = disablePublicBlog;
    }

    /**
     * @param resetGlobalAccess
     *            the resetGlobalAccess to set
     */
    public void setResetGlobalAccess(boolean resetGlobalAccess) {
        this.resetGlobalAccess = resetGlobalAccess;
    }
}
