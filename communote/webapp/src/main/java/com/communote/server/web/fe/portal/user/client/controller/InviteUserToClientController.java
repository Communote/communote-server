package com.communote.server.web.fe.portal.user.client.controller;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.PermanentIdMissmatchException;
import com.communote.server.core.user.UserManagementException;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.InviteUserForm;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Invite user to client use case.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToClientController extends AbstractUserInviteController {

    private final static Logger LOG = Logger.getLogger(InviteUserToClientController.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView getModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors) throws Exception {
        if (errors.getErrorCount() > 0) {
            return showForm(request, errors, getFormView());
        }
        MessageHelper.saveMessage(request,
                ResourceBundleManager.instance().getText("client.invitation.email.send.succesful",
                        SessionHandler.instance().getCurrentLocale(request)));
        return ControllerHelper.replaceModuleInMAV(new ModelAndView(getSuccessView(), "command",
                formBackingObject(request)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InviteUserForm internalFormBackingObject(HttpServletRequest request) throws Exception {
        InviteUserForm form = new InviteUserForm();
        form.setLanguageCode(SessionHandler.instance().getCurrentLocale(request)
                .getLanguage());
        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean inviteUser(HttpServletRequest request, HttpServletResponse response,
            InviteUserForm invitationForm, BindException errors, UserVO userToInvite)
            throws Exception {

        List<String> errorFields = getErrorFields(invitationForm.getInvitationProvider());
        User invitedUser = null;
        try {
            userToInvite.setLanguage(new Locale(invitationForm.getLanguageCode()));
            invitedUser = getUserManagement().inviteUserToClient(userToInvite);
        } catch (EmailAlreadyExistsException e) {
            String field = getExternalUsernameErrorField(errorFields);
            String msgKey = "error.email.already.exists.unmergeable";
            if (field == null) {
                field = getEmailErrorField(errorFields);
                msgKey = "error.email.already.exists";
            }
            errors.rejectValue(field, msgKey, "The email address already exists!");
        } catch (EmailValidationException e) {
            errors.rejectValue(getEmailErrorField(errorFields), "error.email.not.valid",
                    "This email is invalid");
        } catch (AliasAlreadyExistsException e) {
            String field = getExternalUsernameErrorField(errorFields);
            String msgKey = "error.user.already.exists";
            if (field == null) {
                field = getAliasErrorField(errorFields);
                msgKey = "error.alias.already.exists";
            }
            errors.rejectValue(field, msgKey, "The login already exists!");
        } catch (PermanentIdMissmatchException e) {
            String field = getExternalUsernameErrorField(errorFields);
            String msgKey = "error.user.already.exists";
            if (field == null) {
                field = getAliasErrorField(errorFields);
                msgKey = "error.alias.already.exists";
            }
            errors.rejectValue(field, msgKey, "The login already exists!");
        } catch (IllegalArgumentException e) {
            errors.rejectValue(getAliasErrorField(errorFields),
                    "error.external.service.provided.incomplete.data",
                    "The external service provided incomplete data!");
        } catch (UserManagementException e) {
            LOG.error("Inviting user failed.", e);
            errors.rejectValue(getAliasErrorField(errorFields), "error.blog.invite.failed",
                    "An error occurred while creating the user rights!");
        }
        return invitedUser != null;
    }

}
