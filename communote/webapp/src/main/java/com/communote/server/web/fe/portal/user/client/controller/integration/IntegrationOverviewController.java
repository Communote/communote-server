package com.communote.server.web.fe.portal.user.client.controller.integration;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.config.type.ClientProperty.REPOSITORY_MODE;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.user.UserRole;
import com.communote.server.service.UserService;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * This controller is used for setting the selected authentication method.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class IntegrationOverviewController extends BaseFormController {

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(IntegrationOverviewController.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        UserManagement userManagement = ServiceLocator.instance().getService(UserManagement.class);
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        IntegrationOverviewForm integrationOverviewForm = new IntegrationOverviewForm();
        repos: for (ExternalUserRepository userRepository : ServiceLocator.findService(
                UserService.class).getRegistedExternalUserRepositories()) {

            if (!userRepository.showInIntegrationOverview()) {
                // skip repos that do not want to be shown here
                continue repos;
            }
            long activeAdmins = userManagement.getActiveUserCount(
                    userRepository.getExternalSystemId(), UserRole.ROLE_KENMEI_CLIENT_MANAGER);
            integrationOverviewForm.setNumberOfAdmins(userRepository.getExternalSystemId(),
                    activeAdmins);

            ExternalSystemConfiguration config = userRepository.getConfiguration();
            if (config == null) {
                config = userRepository.createConfiguration();
            }
            integrationOverviewForm.setConfiguration(userRepository.getExternalSystemId(), config,
                    getLabelOfExternalUserRepo(userRepository, locale));
        }

        ClientConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        integrationOverviewForm.setAllowAuthOverDbOnExternal(props.getProperty(
                ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
                ClientPropertySecurity.DEFAULT_ALLOW_DB_AUTH_ON_EXTERNAL));
        integrationOverviewForm.setSelectedAuthenticationType(props
                .getPrimaryExternalAuthentication());
        integrationOverviewForm.setUserServiceRepositoryMode(props.getProperty(
                ClientProperty.USER_SERVICE_REPOSITORY_MODE, REPOSITORY_MODE.FLEXIBLE.name()));
        return integrationOverviewForm;
    }

    private String getLabelOfExternalUserRepo(ExternalUserRepository repo, Locale locale) {
        LocalizedMessage name = repo.getName();
        if (name != null) {
            return name.toString(locale);
        }
        return "";
    }

    /**
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param command
     *            The form.
     * @param errors
     *            The object to store errors in.
     * @return The model and view for this page.
     *
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) {
        IntegrationOverviewForm form = (IntegrationOverviewForm) command;
        String externalSystemId = form.getSelectedAuthenticationType();
        if ("default".equals(externalSystemId)) {
            externalSystemId = null;
        }
        try {
            CommunoteRuntime
                    .getInstance()
                    .getConfigurationManager()
                    .updateClientConfigurationProperty(ClientProperty.USER_SERVICE_REPOSITORY_MODE,
                            form.getUserServiceRepositoryMode());
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .setPrimaryAuthentication(externalSystemId, form.isAllowAuthOverDbOnExternal());
            MessageHelper.saveMessageFromKey(request, "client.authentication.update.successful");
        } catch (PrimaryAuthenticationException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.integration.error.reason." + e.getReason(), e.getSystemId());
            LOG.debug(e.getMessage());
        }
        return new ModelAndView(getFormView(), "command", command);
    }
}
