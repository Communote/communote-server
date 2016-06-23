package com.communote.server.core.common.velocity;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.config.DefaultKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.string.StringEscapeHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.ApplicationInformation;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserData;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.common.time.SimplifiedTimeZone;
import com.communote.server.core.common.velocity.tools.ClientTool;
import com.communote.server.core.common.velocity.tools.MessageTool;
import com.communote.server.core.converter.user.UserToUserDataConverter;
import com.communote.server.core.filter.listitems.UserProfileDetailListItem;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.core.user.MasterDataManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.user.helper.UserNameFormat;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.core.vo.user.preferences.UserPreference;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.Country;
import com.communote.server.model.user.Language;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.UserProfileVO;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.service.UserPreferenceService;

/**
 * Facade to different helper functions.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@DefaultKey("communoteTool")
public class CommunoteTool {

    /**
     * Authentication specific tool.
     */
    public class AuthenticationTool {
        /**
         * @return True, if there is no primary external authentication or database fallback is
         *         activated.
         */
        public boolean isDBAuthenticationPossible() {
            ClientConfigurationProperties props = getClientConfigurationProperties();
            return props.getPrimaryExternalAuthentication() == null
                    || props.getProperty(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
                            ClientPropertySecurity.DEFAULT_ALLOW_DB_AUTH_ON_EXTERNAL);
        }

        /**
         * @return True, when the primary authentication is activated.
         */
        public boolean isPrimaryAuthentication() {
            ClientConfigurationProperties props = getClientConfigurationProperties();
            return props.getPrimaryExternalAuthentication() != null;
        }

        /**
         * @return True, if is allowed for users to register themselves.
         */
        public boolean isRegistrationAllowed() {
            return getClientConfigurationProperties().isRegistrationAllowed();
        }
    }

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunoteTool.class);
    private final AuthenticationTool authenticationTool = new AuthenticationTool();
    private final TopicTool topicTool = new TopicTool();
    private final ClientTool clientTool = new ClientTool();
    private final boolean isStandalone = CommunoteRuntime.getInstance().getApplicationInformation()
            .isStandalone();
    private final MessageTool messageTool = new MessageTool();
    private final TagTool tagTool = new TagTool();

    /**
     * Convert the map to a serialized JSON string.
     *
     * @param mapping
     *            the mapping to convert. The mapping should only contain simple types (numbers,
     *            strings, booleans and arrays of these types) as values.
     * @param xmlEscape
     *            whether to {@link StringEscapeHelper#escapeXml(String)} before returning the
     *            result. When using the serialized JSON as content of an HTML attribute, this
     *            parameter should be true.
     * @return the serialized JSON. If null is provided or the serialization fails an empty object
     *         literal ({}) is returned.
     */
    public String convertToJsonString(Map<String, Object> mapping, boolean xmlEscape) {
        if (mapping != null) {
            try {
                String jsonString = JsonHelper.getSharedObjectMapper().writeValueAsString(mapping);
                if (xmlEscape) {
                    jsonString = StringEscapeHelper.escapeXml(jsonString);
                }
                return jsonString;
            } catch (IOException e) {
                LOGGER.error("Failed converting map to JSON string");
            }
        }
        return "{}";
    }

    /**
     * @return The current active user count.
     */
    public Long getActiveUserCount() {
        return ServiceLocator.findService(UserManagement.class).getActiveUserCount();
    }

    /**
     *
     * @return The preferences as JSON String.
     */
    public String getAllUserPreferencesAsJson() {
        return ServiceLocator.findService(UserPreferenceService.class).getPreferencesAsJson();
    }

    /**
     * @return the application information.
     */
    public ApplicationInformation getApplicationInformation() {
        return CommunoteRuntime.getInstance().getApplicationInformation();
    }

    /**
     * @return the AuthenticationTool
     */
    public AuthenticationTool getAuth() {
        return authenticationTool;
    }

    /**
     * @return the TopicTool
     * @deprecated Use {@link #getTopics()} instead.
     */
    @Deprecated
    public TopicTool getBlogs() {
        return getTopics();
    }

    /**
     * @return the ClientTool
     */
    public ClientTool getClient() {
        return clientTool;
    }

    private ClientConfigurationProperties getClientConfigurationProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties();
    }

    /**
     * Get the value of a configuration property.
     *
     * @param key
     *            the key of the property
     * @return the value or null if not defined. Be careful about null value handling in velocity.
     *         Default behavior is printing the call string when the value is null.
     * @see com.communote.server.api.core.config.ClientConfigurationProperties#getProperty(ClientConfigurationPropertyConstant)
     */
    public String getConfigurationProperty(ClientConfigurationPropertyConstant key) {
        return getClientConfigurationProperties().getProperty(key);
    }

    /**
     * Get the value of a configuration property. If the property is not defined the provided
     * fallback will be returned.
     *
     * @param key
     *            the key of the property
     * @param fallback
     *            the fallback
     * @return the value
     * @see com.communote.server.api.core.config.ClientConfigurationProperties#getProperty(ClientConfigurationPropertyConstant,
     *      int)
     */
    public int getConfigurationProperty(ClientConfigurationPropertyConstant key, int fallback) {
        return getClientConfigurationProperties().getProperty(key, fallback);
    }

    /**
     * Get the value of a configuration property as String. If the property is not defined the
     * provided fallback will be returned.
     *
     * @param key
     *            the key of the property
     * @param fallback
     *            the fallback as String
     * @return the value as String
     * @see com.communote.server.api.core.config.ClientConfigurationProperties#getProperty(ClientConfigurationPropertyConstant,
     *      String)
     */
    public String getConfigurationPropertyAsString(ClientConfigurationPropertyConstant key,
            String fallback) {
        return getClientConfigurationProperties().getProperty(key, fallback);
    }

    /**
     * Delegates to {@link MasterDataManagement.getCountries()}
     *
     * @param request
     *            the http servlet request
     * @return the list of countries
     */
    public List<Country> getCountries(HttpServletRequest request) {
        return ServiceLocator.findService(MasterDataManagement.class).getCountries(
                SessionHandler.instance().getCurrentLocale(request));
    }

    /**
     * Delegates to {@link ClientHelper#getCurrentClientId()}
     *
     * @return the current client alias
     */
    public String getCurrentClientId() {
        return ClientHelper.getCurrentClientId();
    }

    /**
     * Delegates to {@link ClientHelper#getCurrentClient()}
     *
     * @return the name of the current client
     */
    public String getCurrentClientName() {
        try {
            return ClientHelper.getCurrentClient().getName();
        } catch (NullPointerException e) { // Should only happen in installer
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

    /**
     * Delegates to {@link UserManagementHelper.getCurrentOffsetOfEffectiveUserTimeZone()}
     *
     * @return the timezone offset
     */
    public int getCurrentOffsetOfEffectiveUserTimeZone() {
        return UserManagementHelper.getCurrentOffsetOfEffectiveUserTimeZone();
    }

    /**
     * Delegates to {@link SecurityHelper#getCurrentUserAlias()}
     *
     * @return the current user alias
     */
    public String getCurrentUserAlias() {
        return SecurityHelper.getCurrentUserAlias();
    }

    /**
     * @return The current effective Timezone of the user.
     */
    public TimeZone getCurrentUserEffectiveTimeZone() {
        return UserManagementHelper.getEffectiveUserTimeZone();
    }

    /**
     * Delegates to {@link SecurityHelper#getCurrentUserId()}
     *
     * @return the current user ID
     */
    public Long getCurrentUserId() {
        return SecurityHelper.getCurrentUserId();
    }

    /**
     * @param request
     *            the current request
     * @return The locale of the current user.
     */
    public Locale getCurrentUserLocale(HttpServletRequest request) {
        return SessionHandler.instance().getCurrentLocale(request);
    }

    /**
     * * @return The currents user profile.
     */
    public UserProfileVO getCurrentUserProfile() {
        return ServiceLocator.findService(UserProfileManagement.class).findUserProfileVOByUserId(
                SecurityHelper.getCurrentUserId());
    }

    /**
     * To get the signature of the current user.
     *
     * @param format
     *            the format of the user signature
     * @return the user signature
     */
    public String getCurrentUserSignature(UserNameFormat format) {
        return UserNameHelper.getUserSignature(SecurityHelper.assertCurrentKenmeiUser(), format);
    }

    /**
     * Get the display name for an external object linked to an topic. If the system id is unknown,
     * the system id or 'unknown' will be returned.
     *
     * @param request
     *            the repuest
     * @param externalSystemId
     *            the external system id of the external object
     * @return the display name of the external object
     */
    public String getDisplayNameForExternalObject(HttpServletRequest request,
            String externalSystemId) {
        String displayName = StringUtils.EMPTY;
        String messageKey = "external.object.name.for.";

        if (StringUtils.isBlank(externalSystemId)) {
            messageKey += "unknown";
        } else {
            messageKey += externalSystemId;
        }

        displayName = ResourceBundleManager.instance().getText(messageKey,
                getCurrentUserLocale(request));

        // check if a message for the given key were found
        if (StringUtils.isBlank(displayName)) {
            displayName = externalSystemId;
        }

        return displayName;
    }

    /**
     * Delegates to {@link MasterDataManagement.getLanguages()}
     *
     * @return the list of supported languages
     */
    public Collection<Language> getLanguages() {
        return ServiceLocator.findService(MasterDataManagement.class).getLanguages();
    }

    /**
     * @return the MessageTool
     */
    public MessageTool getMessages() {
        return messageTool;
    }

    /**
     * @return the TagTool
     */
    public TagTool getTags() {
        return tagTool;
    }

    /**
     * Delegates to {@link MasterDataManagement.getTimeZones()}
     *
     * @return the list of timezone ids
     */
    public List<SimplifiedTimeZone> getTimeZones() {
        return ServiceLocator.findService(MasterDataManagement.class).getTimeZones();
    }

    /**
     * @return the BlogTool
     */
    public TopicTool getTopics() {
        return topicTool;
    }

    /**
     * Delegates to {@link MasterDataManagement.getUsedLanguages()}
     *
     * @return the list of supported languages
     */
    public Collection<Language> getUsedLanguages() {
        return ServiceLocator.findService(MasterDataManagement.class).getUsedLanguages();
    }

    /**
     * Get the details of a user. The tags and the properties of the user won't be included.
     *
     * @param id
     *            the ID of the user
     * @return the user details or null
     */
    public UserData getUser(Number id) {
        if (id == null) {
            return null;
        }
        UserToUserDataConverter<UserData> converter = new UserToUserDataConverter<UserData>(
                UserData.class, false, null);
        UserData user = ServiceLocator.instance().getService(UserManagement.class)
                .getUserById(id.longValue(), converter);
        return user;
    }

    /**
     * @param className
     *            FQCN of the user preferences.
     * @return The preferences.
     */
    public UserPreference getUserPreferences(String className) {
        try {
            return ServiceLocator.findService(UserPreferenceService.class)
                    .getPreferences(className);
        } catch (AuthorizationException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * @param className
     *            FQCN of the user preferences.
     * @return The preferences as JSON String.
     */
    public String getUserPreferencesAsJson(String className) {
        try {
            return ServiceLocator.findService(UserPreferenceService.class).getPreferencesAsJson(
                    className);
        } catch (AuthorizationException e) {
            LOGGER.error(e.getMessage());
            return "{}";
        }
    }

    /**
     * To get the signature of an author.
     *
     * @param user
     *            the user to use
     * @param format
     *            the format of the user signature
     * @return the user signature
     */
    public String getUserSignature(User user, UserNameFormat format) {
        return UserNameHelper.getUserSignature(user, format);
    }

    /**
     * To get the signature of an author.
     *
     * @param userListItem
     *            the user list item to use
     * @param format
     *            the format of the user signature
     * @return the user signature
     */
    public String getUserSignature(UserData userListItem, UserNameFormat format) {
        return UserNameHelper.getUserSignature(userListItem, format);
    }

    /**
     * To get the signature of an author.
     *
     * @param listItem
     *            the user list item to use
     * @param format
     *            the format of the user signature
     * @return the user signature
     */
    public String getUserSignature(UserProfileDetailListItem listItem, UserNameFormat format) {
        return UserNameHelper.getUserSignature(listItem, format);
    }

    /**
     * @return whether the minimized versions of the CSS files should be delivered
     */
    public boolean isCompressCss() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.STYLES_COMPRESS, true);
    }

    /**
     * @return whether the minimized versions of the JavaScript files should be delivered
     */
    public boolean isCompressJavaScript() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.SCRIPTS_COMPRESS, true);
    }

    /**
     * @return True, if the current user is a client manager.
     */
    public boolean isCurrentUserClientManager() {
        return SecurityHelper.isClientManager();
    }

    /**
     * @return True, if the application runs in development mode.
     */
    public boolean isDevelopment() {
        return CommunoteRuntime.getInstance().getConfigurationManager().getDevelopmentProperties()
                .isDevelopement();
    }

    /**
     * Delegates to {@link
     * com.communote.server.core.security.ssl.ChannelManagement.isForceSsl(channelType)}
     *
     * @param type
     *            the channel type
     * @return whether SSL is required for the requested channel
     */
    public boolean isForceSsl(ChannelType type) {
        return ServiceLocator.findService(ChannelManagement.class).isForceSsl(type);
    }

    /**
     * @return whether CSS files should be delivered as one aggregated file
     */
    public boolean isPackCss() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.STYLES_PACK, true);
    }

    /**
     * @return whether JavaScript files should be delivered as one aggregated file
     */
    public boolean isPackJavaScript() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.SCRIPTS_PACK, true);
    }

    /**
     * @return True, if this is the standalone version, else false.
     */
    public boolean isStandalone() {
        return isStandalone;
    }
}
