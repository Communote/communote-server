package com.communote.server.web.fe.portal.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.SecurityCodeManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.user.forms.ForgottenPWForm;

/**
 * Controller for confirming <code>SecurityCode</code>s. Can be extended with custom handlers.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SecurityCodeConfirmationController extends AbstractController {

    private final static Logger LOG = LoggerFactory
            .getLogger(SecurityCodeConfirmationController.class);

    private static final String PARAM_SECURITY_CODE = "securityCode";

    private Map<SecurityCodeAction, SecurityCodeConfirmationHandler> handlers;

    private String formView;

    private String loggedInView;

    public synchronized void addHandler(SecurityCodeConfirmationHandler handler) {
        if (handler.getAction() == null) {
            throw new IllegalArgumentException(
                    "The action of a SecurityCodeConfirmationHandler must not be null");
        }
        if (this.handlers == null) {
            this.handlers = new HashMap<>();
        }
        if (!handlers.containsKey(handler.getAction())) {
            handlers.put(handler.getAction(), handler);
            LOG.debug("Added handler for action {}", handler.getAction());
        } else {
            LOG.warn("There is already a handler for the action {}", handler.getAction());
        }
    }

    /**
     * confirm the new password link
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param securityCode
     *            the code with all necessary information
     * @throws IOException
     *             io exception on redirect
     */
    private SecurityCodeConfirmationResult confirmForgottenPW(HttpServletRequest request,
            HttpServletResponse response, SecurityCode securityCode) throws IOException {
        // delegate to custom controller
        String link = "/user/sendforgottenpw.do?pwaction=" + ForgottenPWForm.CONFIRM_NEW_PASSWORD
                + "&code=" + securityCode.getCode();
        ControllerHelper.sendInternalRedirect(request, response, link);
        return null;
    }

    /**
     * confirm a new email address
     *
     * @param request
     *            the request
     * @param securityCode
     *            the code with all necessary information
     * @throws SecurityCodeNotFoundException
     *             in case the code does not exist
     */
    private SecurityCodeConfirmationResult confirmNewEmailAddress(HttpServletRequest request,
            SecurityCode securityCode) throws SecurityCodeNotFoundException {
        try {
            ServiceLocator.instance().getService(UserManagement.class)
                    .confirmNewEmailAddress(securityCode.getCode());
            return new SecurityCodeConfirmationResult(true,
                    new MessageKeyLocalizedMessage("code.confirmation.new.email.success"));
        } catch (EmailAlreadyExistsException e) {
            return new SecurityCodeConfirmationResult(false,
                    new MessageKeyLocalizedMessage("error.email.already.exists"));
        }
    }

    /**
     * confirm unlocking locked user account
     *
     * @param request
     *            the request
     * @param securityCode
     *            the code with all necessary information
     *
     * @throws SecurityCodeNotFoundException
     *             in case the code does not exist
     */
    private SecurityCodeConfirmationResult confirmUnlockUserSecurityCode(HttpServletRequest request,
            SecurityCode securityCode) throws SecurityCodeNotFoundException {
        ServiceLocator.instance().getService(UserManagement.class)
                .unlockUser(securityCode.getCode());
        return new SecurityCodeConfirmationResult(true,
                new MessageKeyLocalizedMessage("code.confirmation.unlock.user.success"));
    }

    /**
     * Get the form view
     *
     * @return the form view
     */
    public String getFormView() {
        return formView;
    }

    /**
     * @return view name to show if the user is logged in
     */
    public String getLoggedInView() {
        return loggedInView;
    }

    private LocalizedMessage getWarnMessageForMissingCode(HttpServletRequest request, String code) {
        String action = request.getParameter("action");
        if (action != null) {
            try {
                SecurityCodeAction codeAction = SecurityCodeAction.fromString(action);
                SecurityCodeConfirmationHandler handler = handlers != null
                        ? handlers.get(codeAction) : null;
                if (handler != null) {
                    return handler.getWarningForMissingCode(request, code);
                }
            } catch (IllegalArgumentException e) {
                LOG.debug("Unknown security code action {}", action);
            }
        }
        return null;
    }

    private void handleConfirmationResult(HttpServletRequest request,
            SecurityCodeConfirmationResult confirmationResult) {
        if (confirmationResult != null) {
            if (confirmationResult.isSuccess()) {
                if (confirmationResult.getSuccessMessage() != null) {
                    MessageHelper.saveMessage(request, confirmationResult.getSuccessMessage());
                }
                // TODO generic success message?
            } else {
                if (confirmationResult.getErrorMessage() != null) {
                    MessageHelper.saveErrorMessage(request, confirmationResult.getErrorMessage());
                } else {
                    MessageHelper.saveErrorMessage(request, "common.error.unspecified");
                }
            }
        }
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) {

        String code = request.getParameter(PARAM_SECURITY_CODE);
        SecurityCode securityCode = null;
        ModelAndView mav;
        try {
            if (StringUtils.isNotBlank(code)) {
                securityCode = ServiceLocator.findService(SecurityCodeManagement.class)
                        .findByCode(code);
            }
            if (securityCode != null) {
                // try handlers first, if there isn't one to handle the code fallback to built-in
                // handling
                if (!invokeHandler(request, response, securityCode)) {
                    handleSecurityCode(request, response, securityCode);
                }
            } else {
                LocalizedMessage warnMessage = getWarnMessageForMissingCode(request, code);
                if (warnMessage != null) {
                    MessageHelper.saveMessage(request, MessageHelper.getText(request, warnMessage),
                            MessageHelper.WARNING_MESSAGES_KEY);
                } else {
                    MessageHelper.saveErrorMessageFromKey(request, "error.security.code.not.found");
                }
            }
        } catch (SecurityCodeNotFoundException e) {
            MessageHelper.saveErrorMessageFromKey(request, "error.security.code.not.found");
        } catch (Exception e) {
            LOG.error("Unexpected error while code confirmation!" + " Code is: "
                    + (securityCode == null ? "'null'" : securityCode.attributesToString())
                    + ", Url: " + request.getRequestURI() + ", Query: " + request.getQueryString()
                    + ", Message: " + e.getMessage(), e);
            MessageHelper.saveErrorMessageFromKey(request, "code.confirmation.unexpected.error");
        }

        Long userId = SecurityHelper.getCurrentUserId();
        String viewName = userId == null && loggedInView == null ? formView : loggedInView;
        mav = new ModelAndView(viewName);
        ControllerHelper.replaceModuleInMAV(mav);
        return mav;
    }

    /**
     * Handle the security code
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param securityCode
     *            the code. cannot be null.
     * @throws SecurityCodeNotFoundException
     *             in case of code not found exception
     * @throws IOException
     *             in case of an IO exception
     */
    private void handleSecurityCode(HttpServletRequest request, HttpServletResponse response,
            SecurityCode securityCode) throws IOException, SecurityCodeNotFoundException {
        SecurityCodeConfirmationResult confirmationResult;
        if (SecurityCodeAction.CONFIRM_EMAIL.equals(securityCode.getAction())) {
            confirmationResult = confirmNewEmailAddress(request, securityCode);
        } else if (SecurityCodeAction.FORGOTTEN_PASSWORD.equals(securityCode.getAction())) {
            confirmationResult = confirmForgottenPW(request, response, securityCode);
        } else if (SecurityCodeAction.UNLOCK_USER.equals(securityCode.getAction())) {
            confirmationResult = confirmUnlockUserSecurityCode(request, securityCode);
        } else {
            LOG.info("There is no handler for the SecurityCode action {}",
                    securityCode.getAction().getValue());
            throw new SecurityCodeNotFoundException(
                    "The action " + securityCode.getAction() + " of the code cannot be handled");
        }
        handleConfirmationResult(request, confirmationResult);
    }

    private boolean invokeHandler(HttpServletRequest request, HttpServletResponse response,
            SecurityCode securityCode) throws IOException, SecurityCodeNotFoundException {
        if (handlers != null) {
            SecurityCodeConfirmationHandler handler = handlers.get(securityCode.getAction());
            if (handler != null) {
                SecurityCodeConfirmationResult confirmationResult = handler.confirm(request,
                        response, securityCode);
                handleConfirmationResult(request, confirmationResult);
                return true;
            }
        }
        return false;
    }

    public synchronized void removeHandler(SecurityCodeConfirmationHandler handler) {
        if (handler.getAction() == null) {
            throw new IllegalArgumentException(
                    "The action of a SecurityCodeConfirmationHandler must not be null");
        }
        if (this.handlers != null) {
            SecurityCodeConfirmationHandler existingHandler = this.handlers
                    .get(handler.getAction());
            if (existingHandler != null && existingHandler.equals(handler)) {
                this.handlers.remove(handler);
                LOG.debug("Removed handler for action {}", handler.getAction());
            }
        }
    }

    /**
     * Set the form view
     *
     * @param formView
     *            the default result view
     */
    public void setFormView(String formView) {
        this.formView = formView;
    }

    /**
     * set the view name to show if the user is logged in
     *
     * @param loggedInView
     *            sets the logged in view name
     */
    public void setLoggedInView(String loggedInView) {
        this.loggedInView = loggedInView;
    }
}
