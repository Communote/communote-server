package com.communote.server.web.fe.portal.user.client.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.encryption.EncryptionException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.common.ldap.RequiredAttributeNotContainedException;
import com.communote.server.core.common.ldap.caching.LdapServerCacheElementProvider;
import com.communote.server.core.common.ldap.caching.LdapServerCacheKey;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.core.plugin.PluginPropertyManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.authentication.ldap.LdapAuthenticator;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.service.UserService;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.client.forms.LdapConfigurationForm;

/**
 * The Class ClientLdapAuthenticationController.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientLdapAuthenticationController extends BaseFormController {

    // TODO As soon as this controller is part of the LDAP-Plugin remove these properties and use
    // the one from com.communote.plugin.ldap.PropertyKeys
    private static final String PROPERTY_INCREMENTAL_GROUP_ENABLED = "incremental.group.enabled";
    private static final String PROPERTY_INCREMENTAL_USER_ENABLED = "incremental.user.enabled";

    private static final String SYMBOLIC_NAME = "com.communote.plugins.communote-plugins-ldap";

    /** The Constant UPDATE_LDAP_ACTION. */
    public static final String UPDATE_LDAP_ACTION = "clientldapauthentication";

    private final static Logger LOG = LoggerFactory
            .getLogger(ClientLdapAuthenticationController.class);

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {

        LdapConfiguration configuration = (LdapConfiguration) getUserService()
                .getExternalSystemConfiguration(ConfigurationManagement.DEFAULT_LDAP_SYSTEM_ID);

        LdapConfigurationForm form = new LdapConfigurationForm(configuration);
        // when updating the configuration the search bases must be cleared so that the submitted
        // values can be set and old values that were removed in FE are removed from BE
        if (UPDATE_LDAP_ACTION.equals(request.getParameter("submitAction"))) {
            form.getUserSearchBases().clear();
            if (form.getGroupSearchBases() != null) {
                form.getGroupSearchBases().clear();
            }
        }

        UserService userService = ServiceLocator.instance().getService(UserService.class);
        ExternalUserRepository userRepo = userService.getAvailableUserRepository(form.getConfig()
                .getSystemId());
        if (userRepo != null) {
            if (userRepo.isIncrementalSynchronizationAvailable()) {
                PluginPropertyManagement pluginPropertyManagement = ServiceLocator.instance()
                        .getService(PluginPropertyManagement.class);
                form.setIncrementalGroupSync(Boolean.parseBoolean(pluginPropertyManagement
                        .getClientProperty(SYMBOLIC_NAME, PROPERTY_INCREMENTAL_GROUP_ENABLED)));
                form.setIncrementalUserSync(Boolean.parseBoolean(pluginPropertyManagement
                        .getClientProperty(SYMBOLIC_NAME, PROPERTY_INCREMENTAL_USER_ENABLED)));
                form.setAllowPaging(Boolean.parseBoolean(pluginPropertyManagement
                        .getClientProperty(SYMBOLIC_NAME, "allow.paging")));
                form.setPagingSize(Long.parseLong(pluginPropertyManagement.getClientProperty(
                        SYMBOLIC_NAME, "paging.size", "1000")));
                request.setAttribute("isAdPluginActive", true);
            }
        }

        return form;
    }

    private UserService getUserService() {
        return ServiceLocator.findService(UserService.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) {
        LdapConfigurationForm form = (LdapConfigurationForm) command;
        String submitAction = form.getSubmitAction();
        if (submitAction.equals(UPDATE_LDAP_ACTION)) {
            try {
                testAndUpdateLdapConfiguration(request, form);
                MessageHelper
                .saveMessageFromKey(request, "client.authentication.update.successful");
            } catch (AuthorizationException e) {
                MessageHelper.saveErrorMessageFromKey(request, "common.not.authorized.operation");
            } catch (BadCredentialsException e) {
                MessageHelper.saveErrorMessage(
                        request,
                        ResourceBundleManager.instance().getText(
                                "client.authentication.configuration.test.faild.error",
                                getLocale(request), e.getLocalizedMessage()));
            } catch (RequiredAttributeNotContainedException e) {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.authentication.ldap.properties.error.no.value",
                        e.getLdapAttributeName());
            } catch (LdapAttributeMappingException e) {
                LOG.error("update of ldap configuration failed", e);
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.authentication.ldap.properties.error");
            } catch (EncryptionException e) {
                MessageHelper.saveErrorMessage(
                        request,
                        ResourceBundleManager.instance().getText(
                                "client.authentication.update.error.encrypt.password",
                                getLocale(request)));
            } catch (PrimaryAuthenticationException e) {
                MessageHelper.saveErrorMessageFromKey(request, "client.integration.error.reason."
                        + e.getReason(), e.getSystemId());
                LOG.debug(e.getMessage());
            } catch (NumberFormatException e) {
                MessageHelper.saveErrorMessageFromKey(request, "Bitte nur Long");
                LOG.debug(e.getMessage());
            } catch (Exception e) {
                LOG.error("LDAP-Configuration: Unknown exception", e);
                MessageHelper.saveErrorMessage(
                        request,
                        ResourceBundleManager.instance().getText(
                                "client.authentication.configuration.test.faild.error",
                                getLocale(request), e.getLocalizedMessage()));
            }
        }
        return new ModelAndView(getFormView(), getCommandName(), command);
    }

    /**
     * Test the ldap config and to the update
     *
     * @param request
     *            the request
     * @param form
     *            the form
     * @throws LdapAttributeMappingException
     *             Exception.
     * @throws AuthorizationException
     *             Exception.
     * @throws EncryptionException
     *             Exception.
     * @throws PrimaryAuthenticationException
     *             Exception.
     */
    private void testAndUpdateLdapConfiguration(HttpServletRequest request,
            LdapConfigurationForm form) throws AuthorizationException, EncryptionException,
            LdapAttributeMappingException, PrimaryAuthenticationException {
        LdapConfiguration ldapConfig = form.getFilledConfig();
        if (!form.isPasswordChanged()) {
            form.getConfig().setManagerPassword(
                    CommunoteRuntime.getInstance().getConfigurationManager()
                            .getClientConfigurationProperties().getLdapConfiguration()
                            .getManagerPassword());
        }

        // only validate config if external authentication is enabled or a login is provided
        if (ldapConfig.isAllowExternalAuthentication()
                || StringUtils.isNotEmpty(form.getLdapLogin())) {
            String ldapLogin = form.getLdapLogin();
            String ldapPassword = form.getLdapPassword();
            SecurityHelper.assertCurrentUserId();

            LdapUserAttribute usernameAttribute = null;
            if (ldapLogin.contains("@")) {
                usernameAttribute = LdapUserAttribute.EMAIL;
            } else {
                usernameAttribute = LdapUserAttribute.ALIAS;
            }
            ServiceLocator
                    .findService(CacheManager.class)
            .getCache()
                    .invalidate(
                    new LdapServerCacheKey(ldapConfig.getServerDomain(),
                            ldapConfig.getQueryPrefix()),
                            new LdapServerCacheElementProvider());
            LdapAuthenticator authenticator = new LdapAuthenticator(ldapConfig);
            authenticator.authenticate(ldapLogin, ldapPassword, usernameAttribute);
            // do not synchronize, because it's only a test not an add user operation
        }

        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateLdapConfiguration(ldapConfig);

        /** configurations for the LDAP Plugin */
        PluginPropertyManagement pluginPropertyManagement = ServiceLocator.instance().getService(
                PluginPropertyManagement.class);
        Long pagingSize = form.getPagingSize();
        if (pagingSize != null) {
            // TODO default value ?
            pluginPropertyManagement.setClientProperty(SYMBOLIC_NAME, "paging.size",
                    pagingSize.toString());
        }

        pluginPropertyManagement.setClientProperty(SYMBOLIC_NAME, "allow.paging",
                Boolean.toString(form.isAllowPaging()));
        pluginPropertyManagement
                .setClientProperty(SYMBOLIC_NAME, PROPERTY_INCREMENTAL_GROUP_ENABLED,
                        Boolean.toString(form.isIncrementalGroupSync()));
        pluginPropertyManagement.setClientProperty(SYMBOLIC_NAME,
                PROPERTY_INCREMENTAL_USER_ENABLED, Boolean.toString(form.isIncrementalUserSync()));
    }
}
