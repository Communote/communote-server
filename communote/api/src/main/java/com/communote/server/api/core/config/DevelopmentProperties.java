package com.communote.server.api.core.config;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

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

    private static final String MAILING_TEST_MODE = "mailing.test.mode";
    private static final String MAILING_TEST_ADDRESS = "mailing.test.address";

    private boolean isDevelopement = false;
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
        initPropertiesFromFile(configPath);
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
    private void initPropertiesFromFile(File configPath) {
        File propsFile = new File(configPath, DEVEL_PROPERTIES_FILENAME);
        if (!propsFile.exists()) {
            // return silently
            return;
        }
        LOG.info("Using development properties " + propsFile.getAbsolutePath());
        try {
            Properties developmentProperties = PropertiesUtils.loadPropertiesFromFile(propsFile);
            String mailingTestModeString = developmentProperties.getProperty(MAILING_TEST_MODE,
                    StringUtils.EMPTY);
            mailingTestMode = Boolean.parseBoolean(mailingTestModeString);
            if (mailingTestMode) {
                String testAddress = developmentProperties.getProperty(MAILING_TEST_ADDRESS,
                        StringUtils.EMPTY);
                if (EmailValidator.validateEmailAddressByRegex(testAddress)) {
                    mailingTestAddress = testAddress;
                    LOG.info("Mailing test mode is activated and uses email address "
                            + mailingTestAddress);
                } else {
                    LOG.error("Development property " + MAILING_TEST_ADDRESS
                            + " does not provide a leagal email address. "
                            + "The mailing test mode will be disabled.");
                    mailingTestMode = false;
                }
            } else {
                LOG.info("Mailing test mode is deactivated");
            }
            isDevelopement = Boolean.parseBoolean(developmentProperties.getProperty("development"));
            debugRestApi = Boolean.parseBoolean(developmentProperties
                    .getProperty("com.communote.debug.rest"));
        } catch (IOException e) {
            LOG.error("Reading properties file " + propsFile + " failed.", e);
            throw new ConfigurationInitializationException("Reading properties file " + propsFile
                    + " failed");
        }
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
        return isDevelopement;
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
