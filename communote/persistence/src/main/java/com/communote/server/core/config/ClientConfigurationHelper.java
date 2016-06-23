package com.communote.server.core.config;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientProperty;

/**
 * Helper class to check and create default client configuration properties. Moreover, it provides
 * shortcut methods to get some client configuration properties.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ClientConfigurationHelper {

    /** default value for the client user activation. */
    public final static Boolean DEFAULT_AUTOMATIC_USER_ACTIVATION = false;

    /** default value for the client settings, if all users could create global blogs. */
    public final static Boolean DEFAULT_ALLOW_ALL_READ_WRITE_FOR_USERS = true;

    /** default value for the client settings to allow public access for blogs. */
    public final static Boolean DEFAULT_ALLOW_PUBLIC_ACCESS = false;

    /** default value for the public blog of a communote client. */
    public final static Boolean DEFAULT_DISABLE_PUBLIC_BLOG = false;

    /** default value for the file system repository size limit in BYTES. */
    // no limit per default
    public final static Long DEFAULT_FILE_SYSTEM_REPOSITORY_SIZE_LIMIT = 0L;

    /** default value for the resource count limit */
    public final static Long DEFAULT_CLIENT_USER_TAGGED_COUNT_LIMIT = 0L;
    /** default value for the blog count limit */
    public final static Long DEFAULT_CLIENT_BLOG_COUNT_LIMIT = 0L;

    /** TODO For what is this for? More descriptive name please. */
    public final static String DEFAULT_CLIENT_BLOG_COUNT_100_MAIL = StringUtils.EMPTY;
    /** TODO For what is this for? More descriptive name please. */
    public final static String DEFAULT_CLIENT_BLOG_COUNT_90_MAIL = StringUtils.EMPTY;
    /** TODO For what is this for? More descriptive name please. */
    public final static String DEFAULT_CLIENT_USER_TAGGED_COUNT_100_MAIL = StringUtils.EMPTY;
    /** TODO For what is this for? More descriptive name please. */
    public final static String DEFAULT_CLIENT_USER_TAGGED_COUNT_90_MAIL = StringUtils.EMPTY;

    /**
     * Creates standard client settings.
     *
     * @return the default client settings
     */
    public static Map<ClientConfigurationPropertyConstant, String> createStandardClientSettings() {
        Map<ClientConfigurationPropertyConstant, String> result;
        result = new HashMap<ClientConfigurationPropertyConstant, String>();

        // user activation
        result.put(ClientProperty.AUTOMATIC_USER_ACTIVATION,
                DEFAULT_AUTOMATIC_USER_ACTIVATION.toString());

        // repository size limit
        String repositorySizeLimit = DEFAULT_FILE_SYSTEM_REPOSITORY_SIZE_LIMIT.toString();
        result.put(ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_LIMIT, repositorySizeLimit);
        result.put(ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_90_MAIL, Boolean.FALSE.toString());

        // blog count limit
        result.put(ClientProperty.CLIENT_BLOG_COUNT_LIMIT,
                DEFAULT_CLIENT_BLOG_COUNT_LIMIT.toString());
        result.put(ClientProperty.CLIENT_BLOG_COUNT_100_MAIL, DEFAULT_CLIENT_BLOG_COUNT_100_MAIL);
        result.put(ClientProperty.CLIENT_BLOG_COUNT_90_MAIL, DEFAULT_CLIENT_BLOG_COUNT_90_MAIL);

        // user tagged item limit
        result.put(ClientProperty.CLIENT_USER_TAGGED_COUNT_LIMIT,
                DEFAULT_CLIENT_USER_TAGGED_COUNT_LIMIT.toString());
        result.put(ClientProperty.CLIENT_USER_TAGGED_COUNT_100_MAIL,
                DEFAULT_CLIENT_USER_TAGGED_COUNT_100_MAIL);
        result.put(ClientProperty.CLIENT_USER_TAGGED_COUNT_90_MAIL,
                DEFAULT_CLIENT_USER_TAGGED_COUNT_90_MAIL);

        // allow all can read, write
        result.put(ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS, Boolean.TRUE.toString());

        result.put(ClientProperty.REPLY_TO_ADDRESS, StringUtils.EMPTY);
        result.put(ClientProperty.REPLY_TO_ADDRESS_NAME, StringUtils.EMPTY);
        result.put(ClientProperty.SUPPORT_EMAIL_ADDRESS, StringUtils.EMPTY);
        result.put(ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                Boolean.toString(DEFAULT_ALLOW_PUBLIC_ACCESS));
        result.put(ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED,
                Boolean.toString(ClientProperty.DEFAULT_DELETE_USER_BY_ANONYMIZE_ENABLED));
        result.put(ClientProperty.DELETE_USER_BY_DISABLE_ENABLED,
                Boolean.toString(ClientProperty.DEFAULT_DELETE_USER_BY_DISABLE_ENABLED));
        /**
         * initial value for the email send today to telling the client repository size reached
         * 100%, a new file could not be added
         */
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1); // yesterday, so the tests can also work.
        java.sql.Date sqlDate = new java.sql.Date(cal.getTime().getTime());
        result.put(ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_100_MAIL, sqlDate.toString());
        result.put(ClientProperty.NOTIFICATION_RENDER_PERMALINKS,
                Boolean.toString(ClientProperty.DEFAULT_NOTIFICATION_RENDER_PERMALINKS));
        return result;
    }

    /**
     * Returns whether a user can delete his account.
     *
     * @return true if one of the user deletion modes (e.g. permanently disable) is enabled
     */
    public static boolean isUserDeletionAllowed() {
        ClientConfigurationProperties cp = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties();
        return cp.getProperty(ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED,
                ClientProperty.DEFAULT_DELETE_USER_BY_ANONYMIZE_ENABLED)
                || cp.getProperty(ClientProperty.DELETE_USER_BY_DISABLE_ENABLED,
                        ClientProperty.DEFAULT_DELETE_USER_BY_DISABLE_ENABLED);
    }

    /**
     * Instantiates a new client configuration helper.
     */
    private ClientConfigurationHelper() {
        // Do nothing.
    }
}
