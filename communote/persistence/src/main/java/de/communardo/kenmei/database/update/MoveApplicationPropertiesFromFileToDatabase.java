package de.communardo.kenmei.database.update;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContext;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.common.properties.PropertiesUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.api.core.config.type.ApplicationPropertySecurity;
import com.communote.server.api.core.config.type.ApplicationPropertyVirusScanning;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.security.AuthenticationHelper;

/**
 * Update task to get application properties form *.properties files into database.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MoveApplicationPropertiesFromFileToDatabase implements CustomTaskChange {

    private final static Logger LOG = Logger
            .getLogger(MoveApplicationPropertiesFromFileToDatabase.class);
    private static final String PROP_PROPERTY_FILE_PREFIX = "kenmei.property.file.prefix";
    private static final String PROP_PROPERTY_DIR = "kenmei.property.dir";
    private final String[] propertyResourceNames = { "mail.properties", "crc.properties",
            "kenmei.application.properties", "mailfetching.properties", "av.properties" };

    private File propertyDir;
    private String propertyFilePrefix;
    private String confirmationMessage;

    /**
     * Creates a mapping from the old property names to the new application property
     * constants/enums.
     *
     * @return the mapping from the old property names to the new application property
     *         constants/enums
     */
    private Map<String, ApplicationConfigurationPropertyConstant> buildPropertyMapping() {
        Map<String, ApplicationConfigurationPropertyConstant> mapping;
        mapping = new HashMap<String, ApplicationConfigurationPropertyConstant>();
        // av.properties
        mapping.put("scanner.type", ApplicationPropertyVirusScanning.VIRUS_SCANNER_FACTORY_TYPE);
        mapping.put("command.line.string", ApplicationPropertyVirusScanning.COMMAND_LINE_STRING);
        mapping.put("command.line.exit.code",
                ApplicationPropertyVirusScanning.COMMAND_LINE_EXIT_CODE);
        mapping.put("command.line.temp.dir", ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_DIR);
        mapping.put("command.line.temp.file.prefix",
                ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_FILE_PREFIX);
        mapping.put("command.line.temp.file.suffix",
                ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_FILE_SUFFIX);
        mapping.put("command.line.process.timeout",
                ApplicationPropertyVirusScanning.COMMAND_LINE_PROCESS_TIMEOUT);
        mapping.put("clamav.daemon.host", ApplicationPropertyVirusScanning.CLAMAV_SCANNER_HOST);
        mapping.put("clamav.daemon.port", ApplicationPropertyVirusScanning.CLAMAV_SCANNER_PORT);
        mapping.put("clamav.daemon.connection.timeout",
                ApplicationPropertyVirusScanning.CLAMAV_SCANNER_CONNECTION_TIMEOUT);
        mapping.put("clamav.scanner.temp.dir",
                ApplicationPropertyVirusScanning.CLAMAV_SCANNER_TEMP_DIR);
        // crc.properties
        mapping.put("kenmei.crc.file.repository.storage.dir.root",
                ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT);
        // kenmei.application.properties
        mapping.put("kenmei.attachment.max.upload.size",
                ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE);
        mapping.put("kenmei.captcha.disable", ApplicationProperty.CAPTCHA_DISABLED);
        mapping.put("kenmei.application.global.client.alias",
                ApplicationProperty.GLOBAL_CLIENT_ALIAS);
        mapping.put("kenmei.image.max.upload.size", ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE);
        mapping.put("kenmei.integration.widget.include.footer.powered.by",
                ApplicationProperty.INTEGRATION_WIDGET_INCLUDE_FOOTER_POWERED_BY);
        mapping.put("kenmei.mobile.midp.file.name", ApplicationProperty.MOBILE_MIDP_FILE_NAME);
        mapping.put("kenmei.web.application.uri.prefix",
                OldUrlApplicationProperty.WEB_APPLICATION_URI_PREFIX);
        mapping.put("kenmei.web.application.url.prefix",
                OldUrlApplicationProperty.WEB_APPLICATION_URL_PREFIX);
        mapping.put("kenmei.web.application.url.prefix.secure",
                OldUrlApplicationProperty.WEB_APPLICATION_URL_PREFIX_SECURE);
        // xmpp stuff of kenmei.application.properties
        mapping.put("kenmei.xmpp.blog.suffix", ApplicationPropertyXmpp.BLOG_SUFFIX);
        mapping.put("kenmei.xmpp.bot.handle.subscription",
                ApplicationPropertyXmpp.HANDLE_SUBSCRIPTION_REQUESTS);
        mapping.put("kenmei.xmpp.bot.host", ApplicationPropertyXmpp.HOST);
        mapping.put("kenmei.xmpp.bot.ignore.incoming.messages",
                ApplicationPropertyXmpp.IGNORE_INCOMING_MESSAGES);
        mapping.put("kenmei.xmpp.bot.login", ApplicationPropertyXmpp.LOGIN);
        mapping.put("kenmei.xmpp.bot.password", ApplicationPropertyXmpp.PASSWORD);
        mapping.put("kenmei.xmpp.bot.port", ApplicationPropertyXmpp.PORT);
        mapping.put("kenmei.xmpp.bot.priority", ApplicationPropertyXmpp.PRIORITY);
        mapping.put("kenmei.xmpp.debug", ApplicationPropertyXmpp.DEBUG);
        mapping.put("kenmei.xmpp.enabled", ApplicationPropertyXmpp.ENABLED);
        mapping.put("kenmei.xmpp.message.wait", ApplicationPropertyXmpp.TIME_TO_WAIT);
        mapping.put("kenmei.xmpp.user.suffix", ApplicationPropertyXmpp.USER_SUFFIX);
        // mailing.properties
        mapping.put("mailing.from.address", ApplicationPropertyMailing.FROM_ADDRESS);
        mapping.put("mailing.from.address.name", ApplicationPropertyMailing.FROM_ADDRESS_NAME);
        mapping.put("mailing.host", ApplicationPropertyMailing.HOST);
        mapping.put("mailing.port", ApplicationPropertyMailing.PORT);
        // mailfetching properties
        mapping.put("mailfetching.enabled", ApplicationPropertyMailfetching.ENABLED);
        mapping.put("mailfetching.single.address", ApplicationPropertyMailfetching.SINGLE_ADDRESS);
        mapping.put("mailfetching.protocol", ApplicationPropertyMailfetching.PROTOCOL);
        mapping.put("mailfetching.secure.connection", ApplicationPropertyMailfetching.USE_STARTTLS);
        mapping.put("mailfetching.trusted.ca.keystore.file",
                ApplicationPropertySecurity.TRUSTED_CA_KEYSTORE_FILE);
        mapping.put("mailfetching.trusted.ca.keystore.password",
                ApplicationPropertySecurity.TRUSTED_CA_TRUSTSTORE_PASSWORD);
        mapping.put("mailfetching.host", ApplicationPropertyMailfetching.HOST);
        mapping.put("mailfetching.mailbox", ApplicationPropertyMailfetching.MAILBOX);
        mapping.put("mailfetching.user.login", ApplicationPropertyMailfetching.USER_LOGIN);
        mapping.put("mailfetching.user.password", ApplicationPropertyMailfetching.USER_PASSWORD);
        mapping.put("mailfetching.reconnect.timeout",
                ApplicationPropertyMailfetching.RECONNECT_TIMEOUT);
        mapping.put("mailfetching.domain", ApplicationPropertyMailfetching.DOMAIN);
        mapping.put("mailfetching.static.suffix", ApplicationPropertyMailfetching.STATIC_SUFFIX);
        mapping.put("mailfetching.no.clientid.in.address.for.global",
                ApplicationPropertyMailfetching.NO_CLIENTID_IN_ADDRESS_FOR_GLOBAL);
        return mapping;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
        // don't run it on newly installed systems (i.e. installed with installer); good metric is
        // whether to check whether there are clients, if not it's a new installation
        Collection<ClientTO> clients = ServiceLocator.findService(ClientRetrievalService.class)
                .getAllClients();
        if (clients == null || clients.size() == 0) {
            confirmationMessage = "Skipping "
                    + MoveApplicationPropertiesFromFileToDatabase.class.getSimpleName()
                    + " update task on new installation";
            return;
        }
        resolveEnvironmentProperties();
        Properties oldProps = loadOldPropertiesFromFiles();
        Map<ApplicationConfigurationPropertyConstant, String> transformedProperties = null;
        transformedProperties = new HashMap<ApplicationConfigurationPropertyConstant, String>();

        try {
            transformedProperties = transformProperties(oldProps);
        } catch (EncryptionException e) {
            throw new CustomChangeException(
                    "Was not able to encrypt a property while transforming the application properties.",
                    e);
        }

        int count = transformedProperties.size();
        SecurityContext currentContext = null;
        try {
            currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            CommunoteRuntime.getInstance().getConfigurationManager()
            .updateApplicationConfigurationProperties(transformedProperties);
        } catch (ConfigurationUpdateException e) {
            throw new CustomChangeException("Storing the application properties failed", e);
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
        confirmationMessage = "Moved " + count + " propterties to database";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    /**
     * Load properties from property files.
     *
     * @return the properties
     * @throws CustomChangeException
     *             if there is a problem while reading the property files
     */
    private Properties loadOldPropertiesFromFiles() throws CustomChangeException {
        Properties result = new Properties();

        if (propertyDir.exists() && propertyDir.isDirectory()) {
            for (String resourceName : propertyResourceNames) {
                String filename = propertyFilePrefix + resourceName;
                File file = new File(propertyDir, filename);
                try {
                    Properties tempProps = null;
                    tempProps = PropertiesUtils.loadPropertiesFromFile(file);

                    if (tempProps.size() == 0) {
                        throw new CustomChangeException("Property file " + file.getAbsolutePath()
                                + " does not contain any properties");
                    } else {
                        LOG.info("Read " + tempProps.size() + " properties from file "
                                + file.getAbsolutePath());
                        result.putAll(tempProps);
                    }
                } catch (IOException e) {
                    throw new CustomChangeException("Reading property file "
                            + file.getAbsolutePath() + " failed", e);
                }
            }
        } else {
            throw new CustomChangeException("Property configuration directory "
                    + propertyDir.getAbsolutePath() + " does not exist, or is not a directory");
        }
        return result;
    }

    /**
     * Load properties from named resources.
     */
    private void resolveEnvironmentProperties() {
        try {
            Context initCtx = new javax.naming.InitialContext();
            Context envContext = (Context) initCtx.lookup("java:comp/env");
            try {
                Object o = envContext.lookup(PROP_PROPERTY_DIR);
                if (o != null && StringUtils.isNotBlank(o.toString())) {
                    propertyDir = new File(o.toString());
                    LOG.info("Reading properties from directory " + propertyDir.getAbsolutePath()
                            + " specified via environment variable");
                }
                o = envContext.lookup(PROP_PROPERTY_FILE_PREFIX);
                if (o != null && StringUtils.isNotBlank(o.toString())) {
                    propertyFilePrefix = o.toString();
                    LOG.info("Property file prefix is set to " + propertyFilePrefix);
                }
            } catch (NamingException ne) {
                LOG.info("Naming excpetion retrieving property from environment: "
                        + ne.getMessage() + ". Ignoring property.");
            }

        } catch (NamingException ne) {
            LOG.warn("Naming excpetion retrieving properties from environment: " + ne.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // pre-init property dir with config dir; will be overridden with old environment variable
        // kenmei.property.dir
        propertyDir = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getConfigurationDirectory();
        // pre-init property filename prefix; will be overridden with old environment variable
        // kenmei.property.file.prefix
        propertyFilePrefix = "";
    }

    /**
     * Transforms the old properties definition into the new enum based format.
     *
     * @param properties
     *            the old properties
     * @return the transformed properties
     * @throws EncryptionException
     *             thrown in case of an error
     */
    private Map<ApplicationConfigurationPropertyConstant, String> transformProperties(
            Properties properties) throws EncryptionException {
        String iid = ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue();

        Map<ApplicationConfigurationPropertyConstant, String> transformedProperties;
        transformedProperties = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        Map<String, ApplicationConfigurationPropertyConstant> mapping = buildPropertyMapping();
        for (Map.Entry<String, ApplicationConfigurationPropertyConstant> entry : mapping.entrySet()) {
            String value = properties.getProperty(entry.getKey());

            if (ApplicationPropertyMailfetching.USER_PASSWORD.equals(entry.getValue())) {
                value = EncryptionUtils.encrypt(value, iid);
            } else if (ApplicationPropertyMailing.PASSWORD.equals(entry.getValue())) {
                value = EncryptionUtils.encrypt(value, iid);
            } else if (ApplicationPropertyXmpp.PASSWORD.equals(entry.getValue())) {
                value = EncryptionUtils.encrypt(value, iid);
            }

            if (ApplicationPropertyMailfetching.USE_STARTTLS.equals(entry.getValue())) {
                if (StringUtils.isBlank(value) || value.equals("false")) {
                    value = "true";
                } else {
                    value = "false";
                }
            }

            if (value != null) {
                transformedProperties.put(entry.getValue(), value);
            }
        }

        String useStartTLS = transformedProperties
                .get(ApplicationPropertyMailfetching.USE_STARTTLS);
        String fetchingProtocol = transformedProperties
                .get(ApplicationPropertyMailfetching.PROTOCOL);
        if (useStartTLS != null && useStartTLS.equals("false") && fetchingProtocol != null) {
            transformedProperties.put(ApplicationPropertyMailfetching.PROTOCOL, "imaps");
        }
        return transformedProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
