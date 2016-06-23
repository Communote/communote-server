package de.communardo.kenmei.database.update;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.security.AuthenticationHelper;

/**
 * Update task to add an unique id to application properties into database.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AddUniqueIdToInstallation implements CustomTaskChange {

    private ApplicationConfigurationProperties appProperties;
    private String confirmationMessage;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
        String iid = appProperties.getProperty(ApplicationProperty.INSTALLATION_UNIQUE_ID);

        if (iid == null) {
            boolean installed = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getStartupProperties().isInstallationDone();

            iid = UUID.randomUUID().toString();

            Map<ApplicationConfigurationPropertyConstant, String> settings = null;
            settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
            settings.put(ApplicationProperty.INSTALLATION_UNIQUE_ID, iid);
            SecurityContext currentContext = null;
            try {
                if (installed) {
                    currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
                }
                CommunoteRuntime.getInstance().getConfigurationManager()
                        .updateApplicationConfigurationProperties(settings);
            } catch (ConfigurationUpdateException e) {
                throw new CustomChangeException("Storing the application properties failed", e);
            } finally {
                if (installed) {
                    AuthenticationHelper.setSecurityContext(currentContext);
                }
            }
        }

        confirmationMessage = "Add unique id to the installation.";
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
    public void setFileOpener(FileOpener arg0) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        appProperties = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
