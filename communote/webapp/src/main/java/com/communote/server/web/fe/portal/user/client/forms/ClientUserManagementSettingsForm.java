package com.communote.server.web.fe.portal.user.client.forms;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * The Class ClientProfileForm handles the input from the change client logo formular.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientUserManagementSettingsForm {

    /**
     * This enum holds available action types for this form.
     */
    public enum Action {
        /** action for updating the delete options for users */
        UPDATE_CLIENT_USER_DELETE_CONFIG
    }

    private boolean automaticUserActivation = ClientConfigurationHelper.DEFAULT_AUTOMATIC_USER_ACTIVATION;

    private boolean createExternalUserAutomatically;

    private boolean notifyUsers = true;

    private boolean userRegistrationOnDBAuthAllowed;

    private boolean allowDisableUserAccount;

    private boolean allowAnonymizeUserAccount;

    private boolean noNotifyEmailsToUserWhenExternalAuth;
    private boolean dbAuthenticationAllowed;

    private String preselectedTab;
    private String preselectedTopicOverviewTab;

    private TimelineFilterViewType preselectedView;

    private String defaultLanguage;

    /**
     * Instantiates a new client profile form.
     *
     * @param client
     *            the client
     */
    public ClientUserManagementSettingsForm(ClientTO client) {
        ClientConfigurationProperties clientProperties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        noNotifyEmailsToUserWhenExternalAuth = clientProperties.getProperty(
                ClientProperty.NO_REGISTRATION_USER_NOTIFY_EMAILS_WHEN_EXTERNAL_AUTH, false);
        allowDisableUserAccount = clientProperties.getProperty(
                ClientProperty.DELETE_USER_BY_DISABLE_ENABLED,
                ClientProperty.DEFAULT_DELETE_USER_BY_DISABLE_ENABLED);
        allowAnonymizeUserAccount = clientProperties.getProperty(
                ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED,
                ClientProperty.DEFAULT_DELETE_USER_BY_ANONYMIZE_ENABLED);
        automaticUserActivation = clientProperties.getProperty(
                ClientProperty.AUTOMATIC_USER_ACTIVATION,
                ClientConfigurationHelper.DEFAULT_AUTOMATIC_USER_ACTIVATION);
        createExternalUserAutomatically = clientProperties.getProperty(
                ClientProperty.CREATE_EXTERNAL_USER_AUTOMATICALLY,
                ClientProperty.DEFAULT_CREATE_EXTERNAL_USER_AUTOMATICALLY);
        userRegistrationOnDBAuthAllowed = clientProperties.getProperty(
                ClientProperty.USER_REGISTRATION_ALLOWED,
                clientProperties.getPrimaryExternalAuthentication() == null);
        dbAuthenticationAllowed = clientProperties.isDBAuthenticationAllowed();
        preselectedTab = ClientProperty.PRESELECTED_TAB
                .getValue(ClientProperty.PRESELECTED_TABS_VALUES.ALL.name());
        preselectedTopicOverviewTab = ClientProperty.PRESELECTED_TOPIC_OVERVIEW_TAB
                .getValue(ClientProperty.PRESELECTED_TOPIC_OVERVIEW_TABS_VALUES.ALL.name());
        preselectedView = TimelineFilterViewType.valueOf(ClientProperty.PRESELECTED_VIEW
                .getValue(TimelineFilterViewType.COMMENT.name()));
        defaultLanguage = ClientProperty.DEFAULT_LANGUAGE.getValue(ClientHelper
                .getDefaultLanguage().getLanguage());

    }

    /**
     * @return the defaultLanguage
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * @return the preselectedTab
     */
    public String getPreselectedTab() {
        return preselectedTab;
    }

    /**
     * @return the preselectedTopicOverviewTab
     */
    public String getPreselectedTopicOverviewTab() {
        return preselectedTopicOverviewTab;
    }

    /**
     * @return the preselectedView
     */
    public TimelineFilterViewType getPreselectedView() {
        return preselectedView;
    }

    /**
     * @return the allowAnonymizeUserAccount
     */
    public boolean isAllowAnonymizeUserAccount() {
        return allowAnonymizeUserAccount;
    }

    /**
     * @return the allowDisableUserAccount
     */
    public boolean isAllowDisableUserAccount() {
        return allowDisableUserAccount;
    }

    /**
     * @return the automaticUserActivation
     */
    public boolean isAutomaticUserActivation() {
        return automaticUserActivation;
    }

    /**
     * @return the createExternalUserAutomatically
     */
    public boolean isCreateExternalUserAutomatically() {
        return createExternalUserAutomatically;
    }

    /**
     * @return the dbAuthenticationAllowed
     */
    public boolean isDbAuthenticationAllowed() {
        return dbAuthenticationAllowed;
    }

    /**
     * Whether users should not receive notification emails during registration when external
     * authentication is activated.
     *
     * @return true if the users should not receive emails, false otherwise
     */
    public boolean isNoNotifyEmailsToUserWhenExternalAuth() {
        return noNotifyEmailsToUserWhenExternalAuth;
    }

    /**
     * @return the notifyUsers
     */
    public boolean isNotifyUsers() {
        return notifyUsers;
    }

    /**
     * @return the userRegistrationOnDBAuthEnabled
     */
    public boolean isUserRegistrationOnDBAuthAllowed() {
        return userRegistrationOnDBAuthAllowed;
    }

    /**
     * @param allowAnonymizeUserAccount
     *            the allowAnonymizeUserAccount to set
     */
    public void setAllowAnonymizeUserAccount(boolean allowAnonymizeUserAccount) {
        this.allowAnonymizeUserAccount = allowAnonymizeUserAccount;
    }

    /**
     * @param allowDisableUserAccount
     *            the allowDisableUserAccount to set
     */
    public void setAllowDisableUserAccount(boolean allowDisableUserAccount) {
        this.allowDisableUserAccount = allowDisableUserAccount;
    }

    /**
     * @param automaticUserActivation
     *            the automaticUserActivation to set
     */
    public void setAutomaticUserActivation(boolean automaticUserActivation) {
        this.automaticUserActivation = automaticUserActivation;
    }

    /**
     * @param createExternalUserAutomatically
     *            the createExternalUserAutomatically to set
     */
    public void setCreateExternalUserAutomatically(boolean createExternalUserAutomatically) {
        this.createExternalUserAutomatically = createExternalUserAutomatically;
    }

    /**
     * @param dbAuthenticationAllowed
     *            the dbAuthenticationAllowed to set
     */
    public void setDbAuthenticationAllowed(boolean dbAuthenticationAllowed) {
        this.dbAuthenticationAllowed = dbAuthenticationAllowed;
    }

    /**
     * @param defaultLanguage
     *            the defaultLanguage to set
     */
    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage == null ? null : defaultLanguage.trim();
    }

    /**
     * Whether users should not receive notification emails during registration when external
     * authentication is activated.
     *
     * @param noNotifyEmailsToUserWhenExternalAuth
     *            true if the users should not receive emails
     */
    public void setNoNotifyEmailsToUserWhenExternalAuth(boolean noNotifyEmailsToUserWhenExternalAuth) {
        this.noNotifyEmailsToUserWhenExternalAuth = noNotifyEmailsToUserWhenExternalAuth;
    }

    /**
     * @param notifyUsers
     *            the notifyUsers to set
     */
    public void setNotifyUsers(boolean notifyUsers) {
        this.notifyUsers = notifyUsers;
    }

    /**
     * @param preselectedTab
     *            the preselectedTab to set
     */
    public void setPreselectedTab(String preselectedTab) {
        this.preselectedTab = preselectedTab == null ? null : preselectedTab.trim();
    }

    /**
     * @param preselectedTopicOverviewTab
     *            the preselectedTopicOverviewTab to set
     */
    public void setPreselectedTopicOverviewTab(String preselectedTopicOverviewTab) {
        this.preselectedTopicOverviewTab = preselectedTopicOverviewTab == null ? null
                : preselectedTopicOverviewTab.trim();
    }

    /**
     * @param preselectedView
     *            the preselectedView to set
     */
    public void setPreselectedView(TimelineFilterViewType preselectedView) {
        this.preselectedView = preselectedView;
    }

    /**
     * @param userRegistrationOnDBAuthAllowed
     *            the userRegistrationOnDBAuthAllowed to set
     */
    public void setUserRegistrationOnDBAuthAllowed(boolean userRegistrationOnDBAuthAllowed) {
        this.userRegistrationOnDBAuthAllowed = userRegistrationOnDBAuthAllowed;
    }
}
