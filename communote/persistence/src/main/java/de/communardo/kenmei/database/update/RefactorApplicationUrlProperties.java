package de.communardo.kenmei.database.update;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.security.AuthenticationHelper;

/**
 * Custom task to refactor the URL related ApplicationProperties.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class RefactorApplicationUrlProperties implements CustomTaskChange {
    private final static Logger LOG = Logger.getLogger(RefactorApplicationUrlProperties.class);
    private String confirmationMessage;

    /**
     * Builds the settings for the update.
     *
     * @param urlPrefix
     *            the old URL prefix
     * @param secureUrlPrefix
     *            the old URL prefix for secure connections
     * @param context
     *            the servlet context
     * @return the settings to update
     */
    private Map<ApplicationConfigurationPropertyConstant, String> buildUpdateSettings(
            String urlPrefix, String secureUrlPrefix, String context) {
        boolean supportsHttps = !StringUtils.equals(urlPrefix, secureUrlPrefix);
        // get host name
        String hostName = null;
        String httpPort = null;
        String httpsPort = null;
        if (StringUtils.isBlank(urlPrefix)) {
            LOG.warn("The property "
                    + OldUrlApplicationProperty.WEB_APPLICATION_URL_PREFIX.getKeyString()
                    + " is not defined the hostName will not be set correctly.");
        } else {
            hostName = urlPrefix.substring(7);
            int slashIdx = hostName.indexOf("/");
            if (slashIdx != -1) {
                hostName = hostName.substring(0, slashIdx);
            }
            String[] urlParts = hostName.split(":");
            hostName = urlParts[0];
            if (urlParts.length > 1) {
                // port is contained
                httpPort = urlParts[1].trim();
                if (!StringUtils.isNumeric(httpPort)) {
                    httpPort = null;
                }
            }
        }
        if (supportsHttps) {
            httpsPort = extractHttpsPort(secureUrlPrefix, hostName);
        }
        Map<ApplicationConfigurationPropertyConstant, String> settings;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationProperty.WEB_HTTP_PORT, httpPort);
        settings.put(ApplicationProperty.WEB_HTTPS_PORT, httpsPort);
        settings.put(ApplicationProperty.WEB_HTTPS_SUPPORTED, String.valueOf(supportsHttps));
        settings.put(ApplicationProperty.WEB_SERVER_HOST_NAME, hostName);
        settings.put(ApplicationProperty.WEB_SERVER_CONTEXT_NAME, context);
        // add properties to remove
        settings.put(OldUrlApplicationProperty.WEB_APPLICATION_URI_PREFIX, null);
        settings.put(OldUrlApplicationProperty.WEB_APPLICATION_URL_PREFIX, null);
        settings.put(OldUrlApplicationProperty.WEB_APPLICATION_URL_PREFIX_SECURE, null);
        return settings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database db) throws CustomChangeException, UnsupportedChangeException {
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
        ConfigurationManager propsManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        String context = propsManager.getApplicationConfigurationProperties().getProperty(
                OldUrlApplicationProperty.WEB_APPLICATION_URI_PREFIX);
        if (StringUtils.isBlank(context) || context.equals("/")) {
            context = null;
        }
        String urlPrefix = propsManager.getApplicationConfigurationProperties().getProperty(
                OldUrlApplicationProperty.WEB_APPLICATION_URL_PREFIX);
        String secureUrlPrefix = propsManager.getApplicationConfigurationProperties().getProperty(
                OldUrlApplicationProperty.WEB_APPLICATION_URL_PREFIX_SECURE);
        Map<ApplicationConfigurationPropertyConstant, String> settings = buildUpdateSettings(
                urlPrefix, secureUrlPrefix, context);
        SecurityContext currentContext = null;
        try {
            currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            propsManager.updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            throw new CustomChangeException("Refactoring the properties failed", e);
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
        confirmationMessage = "Refactored URL properties.";
    }

    /**
     * Extracts the HTTPS port from the old secure URL prefix.
     *
     * @param secureUrlPrefix
     *            the old URL prefix
     * @param hostName
     *            the host name
     * @return the HTTPS port or null if there is no port
     */
    private String extractHttpsPort(String secureUrlPrefix, String hostName) {
        // check for https port
        String httpsPort = null;
        String portContextPart = StringUtils.removeStart(secureUrlPrefix, "https://" + hostName);
        if (portContextPart.startsWith(":")) {
            int slashIdx = portContextPart.indexOf("/");
            if (slashIdx != -1) {
                httpsPort = portContextPart.substring(1, slashIdx).trim();
            } else {
                httpsPort = portContextPart.substring(1).trim();
            }
            if (!StringUtils.isNumeric(httpsPort)) {
                httpsPort = null;
            }
        }
        return httpsPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener opener) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database database) throws InvalidChangeDefinitionException {
        // nothing
    }

}
