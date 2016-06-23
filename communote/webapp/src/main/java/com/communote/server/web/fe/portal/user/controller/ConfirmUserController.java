package com.communote.server.web.fe.portal.user.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.security.SecurityCodeManagement;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.security.InviteUserToBlogSecurityCode;
import com.communote.server.model.user.security.InviteUserToClientSecurityCode;
import com.communote.server.model.user.security.UserSecurityCode;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.helper.RegistrationHelper;
import com.communote.server.web.fe.portal.user.forms.RegistrationForm;
import com.communote.server.web.fe.portal.user.validator.RegisterUserValidator;

/**
 * Controller for the user registration process. Confirms given security codes and finish the
 * registration.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfirmUserController extends AbstractWizardFormController {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(ConfirmUserController.class);

    /** the parameter key for the code. */
    public static final String PARAM_SECURITY_CODE = "securityCode";

    /** The cancel view. */
    private String cancelView = StringUtils.EMPTY;

    /** The finish view. */
    private String finishView = StringUtils.EMPTY;

    private String errorView = StringUtils.EMPTY;

    /**
     * Retruns true if the code is accepted by the controller.
     *
     * @param code
     *            the code
     * @return true, if successful
     */
    private boolean acceptsCode(SecurityCode code) {
        return code != null
                && (code instanceof UserSecurityCode
                        || code instanceof InviteUserToBlogSecurityCode || code instanceof InviteUserToClientSecurityCode);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        RegistrationForm form = (RegistrationForm) super.formBackingObject(request);
        String code = request.getParameter(PARAM_SECURITY_CODE);
        if (code == null) {
            return form;
        }
        form.setConfirmationCode(code);
        SecurityCode securityCode = ServiceLocator.instance()
                .getService(SecurityCodeManagement.class).findByCode(code);
        if (securityCode != null) {
            User user = securityCode.getUser();
            String language = request.getParameter("lang");
            if (StringUtils.isEmpty(language)) {
                language = user.getLanguageCode();
            }
            form.setLanguageCode(language);
            form.setEmail(user.getEmail());
            if (securityCode instanceof InviteUserToClientSecurityCode
                    || securityCode instanceof InviteUserToBlogSecurityCode) {
                form.setFirstName(user.getProfile().getFirstName());
                form.setLastName(user.getProfile().getLastName());
                form.setAlias(user.getAlias());
            } else {
                String[] userData = StringUtils.substringBefore(user.getEmail(), "@").split("\\.",
                        2);
                form.setFirstName(StringUtils.capitalize(userData[0]));
                if (userData.length > 1) {
                    form.setLastName(StringUtils.capitalize(userData[1]));
                }
                form.setAlias(ServiceLocator.findService(UserManagement.class).generateUniqueAlias(
                        null, user.getEmail()));
            }
        }
        form.setTimeZoneOffsetList(RegistrationHelper.buildTimeZoneOffsetList());
        form.setTimeZoneId(CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getClientTimeZoneId());
        return form;
    }

    /**
     * Gets the cancel view.
     *
     * @return the cancel view
     */
    public String getCancelView() {
        return cancelView;
    }

    /**
     * Gets the error view.
     *
     * @return the error view
     */
    public String getErrorView() {
        return errorView;
    }

    /**
     * Gets the finish view.
     *
     * @return the finish view
     */
    public String getFinishView() {
        return finishView;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#getViewName(javax.servlet
     *      .http.HttpServletRequest, Object, int)
     */
    @Override
    protected String getViewName(HttpServletRequest request, Object command, int page) {
        return ControllerHelper.replaceModuleInViewName(super.getViewName(request, command, page));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractFormController#handleRequestInternal(javax.servlet
     *      .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ClientConfigurationProperties clientConfigurationProperties = CommunoteRuntime
                .getInstance().getConfigurationManager().getClientConfigurationProperties();
        String code = request.getParameter(PARAM_SECURITY_CODE);
        if (code == null) {
            MessageHelper.saveErrorMessage(
                    request,
                    ResourceBundleManager.instance().getText("error.security.code.not.provided",
                            SessionHandler.instance().getCurrentLocale(request)));
            return ControllerHelper.replaceModuleInMAV(new ModelAndView(getErrorView()));
        }
        SecurityCode securityCode = ServiceLocator.instance()
                .getService(SecurityCodeManagement.class).findByCode(code);
        if (!acceptsCode(securityCode)) {
            MessageHelper.saveErrorMessage(
                    request,
                    ResourceBundleManager.instance().getText("error.security.code.not.found",
                            SessionHandler.instance().getCurrentLocale(request)));
            MessageHelper.saveErrorMessage(
                    request,
                    ResourceBundleManager.instance().getText("error.register.already.registered",
                            SessionHandler.instance().getCurrentLocale(request)));
            return ControllerHelper.replaceModuleInMAV(new ModelAndView(getErrorView()));
        }
        if (!clientConfigurationProperties.isRegistrationAllowed()
                && !(securityCode instanceof InviteUserToClientSecurityCode && clientConfigurationProperties
                        .isDBAuthenticationAllowed())
                && !(securityCode instanceof InviteUserToBlogSecurityCode && invitationByClientManager((InviteUserToBlogSecurityCode) securityCode))) {
            MessageHelper.saveErrorMessage(
                    request,
                    ResourceBundleManager.instance().getText(
                            "error.register.external.auth.activated",
                            SessionHandler.instance().getCurrentLocale(request)));
            return ControllerHelper.replaceModuleInMAV(new ModelAndView(getErrorView()));
        }
        return super.handleRequestInternal(request, response);
    }

    /**
     * @param securityCode
     *            The security code.
     * @return True, if this invitation was send from a client manager.
     */
    private boolean invitationByClientManager(InviteUserToBlogSecurityCode securityCode) {
        if (securityCode.getInvitorId() == null) {
            return false;
        }
        UserRole[] rolesOfUser = ServiceLocator.instance().getService(UserManagement.class)
                .getRolesOfUser(securityCode.getInvitorId());
        if (rolesOfUser != null) {
            for (UserRole role : rolesOfUser) {
                if (UserRole.ROLE_KENMEI_CLIENT_MANAGER.equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#postProcessPage(javax.servlet
     *      .http.HttpServletRequest, Object, org.springframework.validation.Errors, int)
     */
    @Override
    protected void postProcessPage(HttpServletRequest request, Object command, Errors errors,
            int page) throws Exception {
        switch (page) {
        case 0:
            postProcessUserRegistration(request, command, errors);
        }
    }

    /**
     * Called after user registration form is submitted.
     *
     * @param request
     *            the request
     * @param command
     *            the command
     * @param errors
     *            the errors
     */
    private void postProcessUserRegistration(HttpServletRequest request, Object command,
            Errors errors) {
        RegistrationForm form = (RegistrationForm) command;
        SessionHandler.instance().overrideCurrentUserLocale(request,
                new Locale(form.getLanguageCode()));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#processCancel(javax.servlet
     *      .http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object,
     *      org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView processCancel(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        return ControllerHelper.replaceModuleInMAV(showForm(request, errors, getCancelView()));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#processFinish(javax.servlet
     *      .http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object,
     *      org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView processFinish(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        RegistrationForm form = (RegistrationForm) command;

        // user registration not allowed if external authentication is activated, but internal
        // authentication disabled.
        ClientConfigurationProperties clientConfigurationProperties = CommunoteRuntime
                .getInstance().getConfigurationManager().getClientConfigurationProperties();
        if (!clientConfigurationProperties.isDBAuthenticationAllowed()) {
            ControllerHelper.sendInternalRedirectToStartPage(request, response);
            return null;
        }
        Locale pageLocale = form.getLocale();
        String errorMessage = null;
        try {
            UserVO userVo = new UserVO();
            userVo.setEmail(form.getEmail());
            userVo.setPlainPassword(form.isPlainPassword());
            userVo.setPassword(form.getPassword());
            userVo.setLanguage(form.getLocale());
            userVo.setAlias(form.getAlias());
            userVo.setFirstName(form.getFirstName());
            userVo.setLastName(form.getLastName());
            userVo.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER });
            userVo.setTimeZoneId(form.getTimeZoneId());

            User user = ServiceLocator.findService(UserManagement.class).confirmUser(
                    form.getConfirmationCode(), userVo);
            if (user.hasStatus(UserStatus.ACTIVE) || user.hasStatus(UserStatus.TERMS_NOT_ACCEPTED)) {
                MessageHelper.saveMessage(
                        request,
                        ResourceBundleManager.instance().getText("code.confirmation.new.user",
                                pageLocale));
            } else {
                MessageHelper.saveMessage(
                        request,
                        ResourceBundleManager.instance().getText(
                                "code.confirmation.new.user.not.activated", pageLocale));
            }
        } catch (EmailValidationException e) {
            errors.rejectValue("email", "error.email.not.valid",
                    "The entered email address is not valid!");
        } catch (AliasAlreadyExistsException ea) {
            errors.rejectValue("alias", "error.alias.already.exists", "The login already exists!");
        } catch (EmailAlreadyExistsException ea) {
            errorMessage = ResourceBundleManager.instance().getText("error.email.already.exists",
                    pageLocale);
        } catch (PasswordLengthException pl) {
            errors.rejectValue("password", "user.forgotten.password.no.securitycode.found",
                    "The given security code is null");
        } catch (SecurityCodeNotFoundException e) {
            errorMessage = ResourceBundleManager.instance().getText(
                    "error.security.code.not.found", pageLocale);
        } catch (Exception e) {
            LOG.error("error while confirming user", e);
            errorMessage = ResourceBundleManager.instance().getText(
                    "code.confirmation.unexpected.error", pageLocale);
        }
        ModelAndView result = processResult(request, errors, errorMessage);
        result = ControllerHelper.replaceModuleInMAV(result);
        return result;
    }

    /**
     *
     * @param request
     *            The request.
     * @param errors
     *            The errors.
     * @param errorMessage
     *            The error message.
     * @return The result.
     * @throws Exception
     *             Exception.
     */
    private ModelAndView processResult(HttpServletRequest request, BindException errors,
            String errorMessage) throws Exception {
        if (errors.hasErrors()) {
            // return to initial page if errors occurred.
            return showPage(request, errors, 0);
        }
        ModelAndView result;
        if (StringUtils.isNotEmpty(errorMessage)) {
            result = new ModelAndView(getErrorView());
        } else {
            result = showForm(request, errors, getFinishView());
        }
        return result;
    }

    /**
     * Sets the cancel view.
     *
     * @param cancelView
     *            the new cancel view
     */
    public void setCancelView(String cancelView) {
        this.cancelView = cancelView;
    }

    /**
     * Sets the error view.
     *
     * @param errorView
     *            the new error view
     */
    public void setErrorView(String errorView) {
        this.errorView = errorView;
    }

    /**
     * Sets the finish view.
     *
     * @param finishView
     *            the new finish view
     */
    public void setFinishView(String finishView) {
        this.finishView = finishView;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#validatePage(Object ,
     *      org.springframework.validation.Errors, int, boolean)
     */
    @Override
    protected void validatePage(Object command, Errors errors, int page, boolean finish) {
        switch (page) {
        case 0:
            validateUserRegistration(command, errors, finish);
            break;
        case 1:
            validateTerms(command, errors, finish);
            break;
        default:
            LOG.warn("no validator found for page " + page);
            break;
        }
    }

    /**
     * Validate terms of use and private policy agreement.
     *
     * @param command
     *            the command
     * @param errors
     *            the errors
     * @param finish
     *            true if finish validation
     */
    private void validateTerms(Object command, Errors errors, boolean finish) {
        RegistrationForm form = (RegistrationForm) command;
        if (!errors.hasErrors()) {
            if (!form.getTermsAgreed()) {
                errors.rejectValue("termsAgreed", "error.register.agree.terms.conditions",
                        "The terms of use are not agreed!");
            }
        }
    }

    /**
     * Validate entered data in the user registration form.
     *
     * @param command
     *            the command
     * @param errors
     *            the errors
     * @param finish
     *            true if finish validation
     */
    private void validateUserRegistration(Object command, Errors errors, boolean finish) {
        RegisterUserValidator validator = new RegisterUserValidator();
        RegistrationForm form = (RegistrationForm) command;
        UserManagement um = ServiceLocator.findService(UserManagement.class);
        validator.validate(form, errors);
        if (!errors.hasErrors()) {
            String code = form.getConfirmationCode();
            SecurityCode securityCode = ServiceLocator.findService(SecurityCodeManagement.class)
                    .findByCode(code);
            User userByCode = securityCode.getUser();
            // can not use the alias if another user alreay exists with the
            // alias
            User userByAliasFound = um.findUserByAlias(form.getAlias());
            if (!errors.hasFieldErrors("alias") && userByAliasFound != null
                    && (userByCode == null || !userByAliasFound.getId().equals(userByCode.getId()))) {
                errors.rejectValue("alias", "error.alias.already.exists",
                        "The login already exists!");
            }
        }
    }
}
