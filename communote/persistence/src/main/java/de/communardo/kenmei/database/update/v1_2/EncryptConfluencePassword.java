package de.communardo.kenmei.database.update.v1_2;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.springframework.security.core.context.SecurityContext;

import com.communote.common.encryption.EncryptionException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.model.config.Configuration;
import com.communote.server.model.config.ConfluenceConfiguration;

/**
 * Update task that encrypts the administrator password of an existing Confluence configuration.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class EncryptConfluencePassword implements CustomTaskChange {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        // load the configuration without decrypting the password, which would happen by using
        // ClientConfigurationProperties.getConfluenceConfiguration()
        Configuration config = ServiceLocator.findService(ConfigurationManagement.class)
                .getConfiguration();
        ConfluenceConfiguration existingConfig = config.getConfluenceConfig();

        if (existingConfig != null) {
            SecurityContext currentContext = null;
            try {
                currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
                CommunoteRuntime.getInstance().getConfigurationManager()
                        .updateConfluenceConfig(existingConfig);
            } catch (AuthorizationException e) {
                throw new CustomChangeException("unexpected exception", e);
            } catch (EncryptionException e) {
                throw new CustomChangeException("unexpected exception", e);
            } catch (PrimaryAuthenticationException e) {
                throw new CustomChangeException("unexpected exception", e);
            } finally {
                AuthenticationHelper.setSecurityContext(currentContext);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return null;
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
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
