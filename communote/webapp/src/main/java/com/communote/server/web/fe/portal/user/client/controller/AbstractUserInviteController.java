package com.communote.server.web.fe.portal.user.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.security.authentication.BaseCommunoteAuthenticationProvider;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.UserRole;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.InviteUserForm;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Abstract Controller to invite a user
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractUserInviteController extends BaseFormController {
    private final static Logger LOG = Logger.getLogger(AbstractUserInviteController.class);

    /**
     * request attribute for the invitation fields. The value is a list of strings with the possible
     * names of {@link InvitationField}
     */
    public final static String ATTR_INVITATION_FIELDS = "invitationFields";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        BaseCommunoteAuthenticationProvider invitationProvider = WebServiceLocator.instance()
                .getInvitationProvider(request.getParameter("invitationProvider"));
        List<String> invitationFieldNames = new ArrayList<String>();
        for (InvitationField field : invitationProvider.getInvitationFields()) {
            invitationFieldNames.add(field.getName());
        }
        request.setAttribute(ATTR_INVITATION_FIELDS, invitationFieldNames);
        InviteUserForm form = internalFormBackingObject(request);
        form.setInvitationProvider(invitationProvider.getIdentifier());
        return form;
    }

    /**
     * @param availableFields
     *            all error fields
     * @return the error field matching the alias best
     */
    protected String getAliasErrorField(List<String> availableFields) {
        String field = null;
        if (availableFields.contains("alias")) {
            field = "alias";
        } else if (availableFields.contains("emailAlias")) {
            field = "emailAlias";
        } else if (availableFields.contains("email")) {
            field = "email";
        } else if (availableFields.contains("externalUsername")) {
            field = "externalUsername";
        }
        return field;
    }

    /**
     * @param availableFields
     *            all error fields
     * @return the error field matching the email best
     */
    protected String getEmailErrorField(List<String> availableFields) {
        String field = null;
        if (availableFields.contains("email")) {
            field = "email";
        } else if (availableFields.contains("emailAlias")) {
            field = "emailAlias";
        } else if (availableFields.contains("alias")) {
            field = "alias";
        }
        return field;
    }

    /**
     * @param invitationProvider
     *            Identifier of the invitation provider.
     * @return the list of all error fields for the current invitation provider
     */
    protected List<String> getErrorFields(String invitationProvider) {
        BaseCommunoteAuthenticationProvider provider = WebServiceLocator.instance()
                .getInvitationProvider(invitationProvider);
        List<String> errorFields = new ArrayList<String>();
        for (InvitationField field : provider.getInvitationFields()) {
            errorFields.add(field.getErrorField());
        }
        return errorFields;
    }

    /**
     * @param availableFields
     *            all error fields
     * @return the error field matching the external username best
     */
    protected String getExternalUsernameErrorField(List<String> availableFields) {
        return availableFields.contains("externalUsername") ? "externalUsername" : null;
    }

    /**
     * Get the model and view to show. Is called after the invitation has been made (or errors
     * occurred).
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param command
     *            the command
     * @param errors
     *            the error
     * @return the {@link ModelAndView} to show
     * @throws Exception
     *             in case of an error
     */
    protected ModelAndView getModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors) throws Exception {
        return showForm(request, errors, getFormView());
    }

    /**
     * Get the locale of the invited user. If there is no locale the fallback is the English locale.
     *
     * @param form
     *            the form
     * @return a locale
     */
    private Locale getUserLocaleWithFallback(InviteUserForm form) {
        if (form.getLanguageCode() != null) {
            return new Locale(form.getLanguageCode());
        }
        return Locale.ENGLISH;
    }

    /**
     * returns the user management service
     *
     * @return the user management service
     */
    protected UserManagement getUserManagement() {
        return ServiceLocator.findService(UserManagement.class);
    }

    /**
     * Does invitation of an external user if the user can be found.
     *
     * @param form
     *            the backing form object
     * @return whether the user was successfully invited
     */
    private UserVO getUserToInvite(InviteUserForm form) {

        BaseCommunoteAuthenticationProvider provider = WebServiceLocator.instance()
                .getInvitationProvider(form.getInvitationProvider());
        Map<InvitationField, String> queryData = new HashMap<InvitationField, String>();
        for (InvitationField field : provider.getInvitationFields()) {
            field.putIntoQueryData(form, queryData);
        }
        UserVO userToInvite = provider.queryUser(queryData);
        return userToInvite;
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception
     * @see com.communote.server.web.commons.controller.BaseFormController#handleOnSubmit(javax.servlet
     *      .http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object,
     *      org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        InviteUserForm form = (InviteUserForm) command;
        validateForm(form, errors);
        boolean success = false;
        if (errors.getErrorCount() == 0) {
            try {
                UserVO userToInvite = getUserToInvite(form);
                if (userToInvite != null) {
                    userToInvite.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER });
                    if (userToInvite.getLanguage() == null) {
                        userToInvite.setLanguage(getUserLocaleWithFallback(form));
                    }
                    success = inviteUser(request, response, form, errors, userToInvite);
                } else {
                    // well it only can be an external user
                    String field = getExternalUsernameErrorField(getErrorFields(form
                            .getInvitationProvider()));
                    if (field == null) {
                        field = getAliasErrorField(getErrorFields(form.getInvitationProvider()));
                    }
                    errors.rejectValue(field, "error.external.user.not.found",
                            "The external user does not exist!");
                }
            } catch (DataAccessException e) {
                LOG.warn(e.getMessage());
                MessageHelper.saveErrorMessageFromKey(request, "error.external.system.down");
                errors.reject("error.external.system.down");
            }
            if (success) {
                ControllerHelper.setApplicationSuccess(response);
            } else {
                ControllerHelper.setApplicationFailure(response);
            }
        }
        request.setAttribute("anErrorOccured", errors.getErrorCount() != 0);
        return getModelAndView(request, response, command, errors);
    }

    /**
     * see {@link #formBackingObject(HttpServletRequest)}
     *
     * @param request
     *            the request
     * @return the command object
     * @throws Exception
     *             in case of an error
     */
    protected abstract InviteUserForm internalFormBackingObject(HttpServletRequest request)
            throws Exception;

    /**
     * Invite the given user. Depends on the semantics of the sub class.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param abstractForm
     *            the invite form
     * @param errors
     *            the binding errors
     * @param userToInvite
     *            the user that should be invited
     * @return true if the invitation was successfull
     * @throws Exception
     *             in case of an error
     */
    protected abstract boolean inviteUser(HttpServletRequest request, HttpServletResponse response,
            InviteUserForm abstractForm, BindException errors, UserVO userToInvite)
                    throws Exception;

    /**
     * Validate the given form based on the expected invitation fields.
     *
     * @param form
     *            the form to validate
     * @param errors
     *            the errors
     */
    protected void validateForm(InviteUserForm form, Errors errors) {
        BaseCommunoteAuthenticationProvider provider = WebServiceLocator.instance()
                .getInvitationProvider(form.getInvitationProvider());
        for (InvitationField field : provider.getInvitationFields()) {
            field.validate(form, errors);
        }
    }
}
