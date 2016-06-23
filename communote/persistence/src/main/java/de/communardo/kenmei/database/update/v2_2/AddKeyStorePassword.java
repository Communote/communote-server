package de.communardo.kenmei.database.update.v2_2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import liquibase.FileOpener;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationPropertySecurity;
import com.communote.server.core.security.AuthenticationHelper;

import de.communardo.kenmei.database.update.RunOnClientCustomTaskChange;

/**
 * Creates a new password for {@link ApplicationPropertySecurity.KEYSTORE_PASSWORD} if there is
 * none.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AddKeyStorePassword extends RunOnClientCustomTaskChange {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleExecute(Database database) throws CustomChangeException,
    UnsupportedChangeException {
        String currentPassword = ApplicationPropertySecurity.KEYSTORE_PASSWORD.getValue();
        if (currentPassword == null || currentPassword.length() == 0) {

            Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
            settings.put(ApplicationPropertySecurity.KEYSTORE_PASSWORD,
                    DigestUtils.md5Hex(UUID.randomUUID().toString()));
            SecurityContext currentContext = null;
            try {
                currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
                CommunoteRuntime.getInstance().getConfigurationManager()
                        .updateApplicationConfigurationProperties(settings);
            } catch (ConfigurationUpdateException e) {
                throw new CustomChangeException("Storing the keystore password failed", e);
            } finally {
                AuthenticationHelper.setSecurityContext(currentContext);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // Do nothing.
    }

}
