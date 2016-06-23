package com.communote.server.web.fe.portal.blog.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.PermanentIdMissmatchException;
import com.communote.server.core.user.UserManagementException;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.InviteUserForm;
import com.communote.server.web.fe.portal.blog.forms.BlogMemberInviteForm;
import com.communote.server.web.fe.portal.user.client.controller.AbstractUserInviteController;

/**
 * Controller to invite user to a blog.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>,
 *         Torsten Lunze
 */
public class BlogMemberInviteController extends AbstractUserInviteController {

    private final static Logger LOG = Logger.getLogger(BlogMemberInviteController.class);

    /** The Constant PARAM_BLOG. */
    public final static String PARAM_BLOG = "blogId";

    /**
     * {@inheritDoc}
     */
    @Override
    protected InviteUserForm internalFormBackingObject(HttpServletRequest request) throws Exception {
        throw new IllegalArgumentException("Should not be called anymore.");
    }

    /**
     * Well, well this is a week before release hack. Allow direct access to the interna of this
     * controller to be reused by the api controller.
     *
     * @param request
     *            the request to use
     * @param response
     *            the response to use
     * @param command
     *            the form
     * @param errors
     *            the errors
     * @throws Exception
     *             in case of an error
     */
    public void invite(HttpServletRequest request, HttpServletResponse response,
            InviteUserForm command, BindException errors) throws Exception {
        this.handleOnSubmit(request, response, command, errors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean inviteUser(HttpServletRequest request, HttpServletResponse response,
            InviteUserForm invitationForm, BindException errors, UserVO userToInvite)
                    throws BlogNotFoundException {
        BlogMemberInviteForm form = (BlogMemberInviteForm) invitationForm;

        Long blogId = form.getBlogId();

        if (blogId == null || blogId < 1) {
            throw new BlogNotFoundException("blog id not set", blogId, "");
        }
        if (!ServiceLocator.instance().getService(TopicPermissionManagement.class)
                .hasPermission(blogId, TopicPermissionManagement.PERMISSION_INVITE_USER)) {
            errors.reject("error.blogpost.blog.no.access.no.manager");
        }
        if (errors.getErrorCount() > 0) {
            return false;
        }

        List<String> errorFields = getErrorFields(invitationForm.getInvitationProvider());
        User invitedUser = null;
        try {
            invitedUser = getUserManagement().inviteUserToBlog(blogId, userToInvite,
                    BlogRole.fromString(form.getRole()));
        } catch (AuthorizationException e) {
            errors.reject("error.blogpost.blog.no.access.no.manager");
        } catch (EmailValidationException e) {
            errors.rejectValue(getEmailErrorField(errorFields), "error.email.not.valid",
                    "The entered email address is not valid!");
        } catch (AliasAlreadyExistsException ea) {
            String field = getExternalUsernameErrorField(errorFields);
            String msgKey = "error.user.already.exists";
            if (field == null) {
                field = getAliasErrorField(errorFields);
                msgKey = "error.alias.already.exists";
            }
            errors.rejectValue(field, msgKey, "The login already exists!");
        } catch (EmailAlreadyExistsException ea) {
            String field = getExternalUsernameErrorField(errorFields);
            String msgKey = "error.email.already.exists.unmergeable";
            if (field == null) {
                field = getEmailErrorField(errorFields);
                msgKey = "error.email.already.exists";
            }
            errors.rejectValue(field, msgKey, "The email address already exists!");
        } catch (PermanentIdMissmatchException e) {
            // show same message like existing alias because the details won't clarify the situation
            String field = getExternalUsernameErrorField(errorFields);
            String msgKey = "error.user.already.exists";
            if (field == null) {
                field = getAliasErrorField(errorFields);
                msgKey = "error.alias.already.exists";
            }
            errors.rejectValue(field, msgKey,
                    "The login already exists but has another permanentId!");
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
