package com.communote.server.web.fe.portal.user.client.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.encryption.EncryptionException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.core.security.authentication.confluence.ConfluenceAuthenticationRequest;
import com.communote.server.core.security.authentication.confluence.ConfluenceAuthenticator;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.client.forms.ConfluenceAuthConfigurationForm;

/**
 * The Class ClientConfluenceAuthConfigController.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientConfluenceAuthConfigController extends BaseFormController {

    private static final String SYNC_IS_AVAILABLE = "syncIsAvailable";

    /** The Constant UPDATE_LDAP_ACTION. */
    public static final String UPDATE_CONFLUENCE_AUTH_CONFIG_ACTION = "clientconfluenceauthconfig";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ClientConfluenceAuthConfigController.class);

    private final static String CONFLUENCE_URL_COMMUNOTE_AUTH = "plugins/servlet/communote-auth";
    private final static String CONFLUENCE_URL_IMAGES = "plugins/servlet/communote-user-picture?alias=";
    private final static String CONFLUENCE_URL_SERVICE = "rpc/soap-axis/confluenceservice-v1?wsdl";
    private final static String CONFLUENCE_URL_PERMISSION_SERVICE = "rpc/soap-axis/permission-service?wsdl";

    /** If set, the synchronization of users and groups with Confluence is available */
    // TODO dead-code since TCRAGS has been removed
    public static final boolean IS_SYNC_AVAILABLE = false;

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ClientConfigurationProperties properties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        ConfluenceConfiguration confluenceAuthConfig = properties.getConfluenceConfiguration();
        if (confluenceAuthConfig == null) {
            confluenceAuthConfig = ConfluenceConfiguration.Factory.newInstance();
        }
        request.setAttribute(SYNC_IS_AVAILABLE, IS_SYNC_AVAILABLE);
        return new ConfluenceAuthConfigurationForm(confluenceAuthConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ConfluenceAuthConfigurationForm form = (ConfluenceAuthConfigurationForm) command;

        if (form.getAction().equals(UPDATE_CONFLUENCE_AUTH_CONFIG_ACTION)) {

            try {
                testAndUpdateConfluenceConfiguration(form);
                MessageHelper
                .saveMessageFromKey(request, "client.authentication.update.successful");
            } catch (AuthorizationException e) {
                MessageHelper.saveErrorMessageFromKey(request, "common.not.authorized.operation");
            } catch (BadCredentialsException e) {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.authentication.confluence.invalid.credentials");
            } catch (org.springframework.security.core.AuthenticationException e) {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.authentication.confluence.access.failed", e.getMessage());
                LOGGER.debug("Error accessing confluence: " + e.getMessage());
            } catch (EncryptionException e) {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.authentication.update.error.encrypt.password");
                LOGGER.debug("Updating confluence configuration failed", e);
            } catch (PrimaryAuthenticationException e) {
                MessageHelper.saveErrorMessageFromKey(request, "client.integration.error.reason."
                        + e.getReason(), e.getSystemId());
                LOGGER.debug(e.getMessage());
            }
        }
        ModelAndView result;
        if (errors.getErrorCount() == 0) {
            result = new ModelAndView(getFormView(), getCommandName(), command);
        } else {
            result = showForm(request, errors, getFormView());
        }
        return result;
    }

    /**
     * @param form
     *            the form
     * @throws AuthorizationException
     *             in case the current user is not client manager
     * @throws EncryptionException
     *             in case of an encryption exception
     * @throws PrimaryAuthenticationException
     *             Exception.
     */
    private void testAndUpdateConfluenceConfiguration(ConfluenceAuthConfigurationForm form)
            throws AuthorizationException, EncryptionException, PrimaryAuthenticationException {
        ConfluenceConfiguration confluenceConfig = form.getConfig();
        if (!form.getConfig().getBasePath().endsWith("/")) {
            form.getConfig().setBasePath(form.getConfig().getBasePath() + "/");
        }
        String baseUrl = form.getConfig().getBasePath();
        form.getConfig().setPermissionsUrl(baseUrl + CONFLUENCE_URL_PERMISSION_SERVICE);
        form.getConfig().setServiceUrl(baseUrl + CONFLUENCE_URL_SERVICE);
        form.getConfig().setAuthenticationApiUrl(baseUrl + CONFLUENCE_URL_COMMUNOTE_AUTH);
        if (form.isUseConfluenceImages()) {
            form.getConfig().setImageApiUrl(baseUrl + CONFLUENCE_URL_IMAGES);
        } else {
            form.getConfig().setImageApiUrl(null);
        }
        ConfigurationManager propertiesManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        if (!form.isPasswordChanged()
                && propertiesManager.getClientConfigurationProperties()
                        .getConfluenceConfiguration() != null) {
            form.getConfig().setAdminPassword(
                    propertiesManager.getClientConfigurationProperties()
                    .getConfluenceConfiguration().getAdminPassword());
        }
        // only test the confluence authentication if config is active
        if (confluenceConfig.isAllowExternalAuthentication()
                || confluenceConfig.isSynchronizeUserGroups()) {
            ConfluenceAuthenticator authenticator = new ConfluenceAuthenticator(confluenceConfig);
            ConfluenceAuthenticationRequest apiRequest = new ConfluenceAuthenticationRequest(form
                    .getConfig().getAdminLogin(), form.getConfig().getAdminPassword());
            if (authenticator.authenticate(apiRequest) == null) {
                throw new AuthenticationServiceException("No user details retrieved.");
            }
        }
        propertiesManager.updateConfluenceConfig(confluenceConfig);
        if (form.isUseConfluenceImages()) {
            // TODO have to clear all caches because we do not know the ID of the confluence image
            // provider here which it is part of the plugin! Fix this when moving this controller to
            // the plugin too.
            ServiceLocator.findService(ImageManager.class).imageChanged(
                    UserImageDescriptor.IMAGE_TYPE_NAME, null, null);
        }
    }
}
