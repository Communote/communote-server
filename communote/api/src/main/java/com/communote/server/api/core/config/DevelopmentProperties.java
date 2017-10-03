package com.communote.server.api.core.config;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.properties.PropertiesUtils;
import com.communote.common.validation.EmailValidator;

/**
 * Object holding some special properties when running a development system.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DevelopmentProperties {
    private static final String DEVEL_PROPERTIES_FILENAME = "development.properties";

    private final static Logger LOG = LoggerFactory.getLogger(DevelopmentProperties.class);

    private final Properties developmentProperties;

    private boolean developementMode = false;
    private boolean mailingTestMode = false;
    private boolean debugRestApi = false;
    private String mailingTestAddress;

    /**
     * Creates and initializes the development properties. A
     * {@link ConfigurationInitializationException} will be thrown if the initialization failed.
     *
     * @param configPath
     *            path to the configuration directory
     */
    public DevelopmentProperties(File configPath) {
        mailingTestAddress = StringUtils.EMPTY;
        mailingTestMode = false;
        developmentProperties = initPropertiesFromFile(configPath);
    }

    /**
     * Returns the test email address. This will only return a useful value if
     * {@link DevelopmentProperties#isMailingTestMode()} returns true.
     *
     * @return the test email address
     */
    public String getMailingTestAddress() {
        return mailingTestAddress;
    }

    /**
     * Initializes the development properties from file. A
     * {@link ConfigurationInitializationException} will be thrown if the initialization failed.
     *
     * @param configPath
     *            path to the configuration directory
     */
    private Properties initPropertiesFromFile(File configPath) {
        File propsFile = new File(configPath, DEVEL_PROPERTIES_FILENAME);
        if (!propsFile.exists()) {
            // ignore silently
            return new Properties();
        }
        LOG.info("Using development properties " + propsFile.getAbsolutePath());
        try {
            Properties props = PropertiesUtils.loadPropertiesFromFile(propsFile);
            developementMode = Boolean.parseBoolean(props.getProperty("development"));
            return props;
        } catch (IOException e) {
            LOG.error("Reading properties file " + propsFile + " failed.", e);
            throw new ConfigurationInitializationException("Reading properties file " + propsFile
                    + " failed");
        }
    }

    /**
     * Returns the property value for the key as String.
     * 
     * @param key
     *            the key
     * @return the property value or null if there is no property for the key
     */
    public String getProperty(String key) {
        return developmentProperties.getProperty(key);
    }

    /**
     * Returns the property for the key as boolean. 'true', 'on' or 'yes' (case insensitive) will
     * return true. 'false', 'off' or 'no' (case insensitive) will return false. If none of these
     * values is set the fallback will be returned.
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to return if there is no property for the key
     * @return the property value or the fallback if there is no property for the key or it cannot
     *         be converted into a boolean
     */
    public boolean getProperty(String key, boolean fallback) {
        Boolean result = BooleanUtils.toBooleanObject(getProperty(key));
        if (result == null) {
            result = fallback;
        }
        return result;
    }

    /**
     * Returns the property for the key as integer.
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to return if there is no property for the key
     * @return the property value or the fallback if there is no property for the key or it cannot
     *         be converted into an integer
     */
    public int getProperty(String key, int fallback) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Returns the property for the key as long.
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to return if there is no property for the key
     * @return the property value or the fallback if there is no property for the key or it cannot
     *         be converted into a long
     */
    public long getProperty(String key, long fallback) {
        try {
            return Long.parseLong(getProperty(key));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Returns the property for the key as string.
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to return if there is no property for the key
     * @return the property value or the fallback if there is no property for the key
     */
    public String getProperty(String key, String fallback) {
        String prop = getProperty(key);
        if (prop == null) {
            return fallback;
        }
        return prop;
    }

    /**
     * @return the debugRestApi
     */
    public boolean isDebugRestApi() {
        return debugRestApi;
    }

    /**
     * @return the isDevelopemnt
     */
    public boolean isDevelopement() {
        return developementMode;
    }

    /**
     * Returns whether the mailing test mode is enabled.
     *
     * @return true if the mailing test mode is enabled
     */
    public boolean isMailingTestMode() {
        return mailingTestMode;
    }
}
