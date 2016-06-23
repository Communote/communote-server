package de.communardo.kenmei.database.update.v1_1_4;

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
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationPropertyVirusScanning;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.security.AuthenticationHelper;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AddVirusScannerEnableOption implements CustomTaskChange {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        // don't run it on newly installed systems (i.e. installed with installer); good metric is
        // whether to check whether there are clients, if not it's a new installation
        Collection<ClientTO> clients = ServiceLocator.findService(ClientRetrievalService.class)
                .getAllClients();
        if (clients == null || clients.size() == 0) {
            return;
        }

        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties();
        String enabledStr = props.getProperty(ApplicationPropertyVirusScanning.ENABLED);
        // if already set by installer keep it
        if (StringUtils.isBlank(enabledStr)) {
            String scannerType = props
                    .getProperty(ApplicationPropertyVirusScanning.VIRUS_SCANNER_FACTORY_TYPE);
            // if no scanner is configured or the scanner is the NoneScanner, set the flag to
            // disabled
            Map<ApplicationConfigurationPropertyConstant, String> newProps;
            newProps = new HashMap<ApplicationConfigurationPropertyConstant, String>();
            if (StringUtils.isBlank(scannerType)
                    || scannerType
                            .equals("NoneVirusScanner")) {
                newProps.put(ApplicationPropertyVirusScanning.VIRUS_SCANNER_FACTORY_TYPE, null);
                newProps.put(ApplicationPropertyVirusScanning.ENABLED, Boolean.toString(false));
            } else {
                // a real virus scanner is selected so enable the virus scanner
                newProps.put(ApplicationPropertyVirusScanning.ENABLED, Boolean.toString(true));
            }
            SecurityContext currentContext = null;
            try {
                currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
                CommunoteRuntime.getInstance().getConfigurationManager()
                        .updateApplicationConfigurationProperties(newProps);
            } catch (ConfigurationUpdateException e) {
                throw new CustomChangeException("Updating the application properties failed.", e);
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
        return "Virus scanner enable option added";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {

    }

}
