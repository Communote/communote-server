package com.communote.server.web.api.service.blog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.service.IllegalRequestParameterException;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.api.to.ApiResult.ResultStatus;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.portal.blog.controller.BlogMemberInviteController;
import com.communote.server.web.fe.portal.blog.forms.BlogMemberInviteForm;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class InviteUserToBlogApiController extends BaseRestApiController {

    /**
     * Wrapping object for an error field
     */
    public class ErrorField {
        private String name;
        private String newValue;
        private String message;

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the newValue
         */
        public String getNewValue() {
            return newValue;
        }

        /**
         * @param message
         *            the message to set
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * @param name
         *            the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @param newValue
         *            the newValue to set
         */
        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }
    }

    private final BlogMemberInviteController controller = new BlogMemberInviteController();

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws IllegalRequestParameterException {
        String roleParam = getNonEmptyParameter(request, "role");
        Long blogId = getLongParameter(request, "blogId");

        roleParam = roleParam.toUpperCase();

        BlogMemberInviteForm inviteForm = new BlogMemberInviteForm();

        inviteForm.setAlias(request.getParameter("alias"));
        inviteForm.setFirstName(request.getParameter("firstName"));
        inviteForm.setLastName(request.getParameter("lastName"));
        inviteForm.setEmail(request.getParameter("email"));
        inviteForm.setLanguageCode(request.getParameter("languageCode"));
        inviteForm.setEmailAlias(request.getParameter("emailAlias"));
        inviteForm.setExternalUsername(request.getParameter("externalUsername"));
        inviteForm.setBlogId(blogId);
        inviteForm.setRole(roleParam);

        List<ErrorField> errorFields = new ArrayList<ErrorField>();
        BindException errors = new BindException(inviteForm, "inviteForm");

        try {
            controller.invite(request, response, inviteForm, errors);
            for (Object fieldError : errors.getFieldErrors()) {
                FieldError error = (FieldError) fieldError;
                ErrorField errorField = new ErrorField();
                errorField.setMessage(resolveErrorMessage(request, error));
                errorField.setName(error.getField());
                errorField.setNewValue(getNewValue(inviteForm, error.getField()));
                errorFields.add(errorField);
            }

            if (errorFields.size() > 0) {
                apiResult.setStatus(ResultStatus.ERROR.name());
            }

            if (errors.getGlobalErrorCount() > 0) {
                apiResult.setStatus(ResultStatus.ERROR.name());
                apiResult.setMessage(MessageHelper.getText(request,
                        "error.blogpost.blog.no.access.no.manager"));
            }

        } catch (Exception e) {
            apiResult.setStatus(ResultStatus.ERROR.name());
            apiResult.setMessage(ResourceBundleManager.instance().getText(
                    "error.invite.client.user",
                    SessionHandler.instance().getCurrentLocale(request)));
        }

        return errorFields.toArray();
    }

    /**
     * @param form
     *            the form
     * @param fieldName
     *            the field name
     * @return the new value of the field
     */
    private String getNewValue(BlogMemberInviteForm form, String fieldName) {
        String value;
        if (fieldName.equals("alias")) {
            value = form.getAlias();
        } else if (fieldName.equals("firstName")) {
            value = form.getFirstName();
        } else if (fieldName.equals("lastName")) {
            value = form.getLastName();
        } else if (fieldName.equals("email")) {
            value = form.getEmail();
        } else if (fieldName.equals("languageCode")) {
            value = form.getLanguageCode();
        } else if (fieldName.equals("emailAlias")) {
            value = form.getEmailAlias();
        } else if (fieldName.equals("externalUsername")) {
            value = form.getExternalUsername();
        } else {
            value = null;
        }
        return value;
    }

    /**
     * @param request
     *            the request
     * @param error
     *            the field error
     * @return the localized error message
     */
    private String resolveErrorMessage(HttpServletRequest request, FieldError error) {
        ResourceBundleManager bundleManager = ResourceBundleManager.instance();
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        for (String code : error.getCodes()) {
            if (bundleManager.knowsMessageKey(code, error.getArguments(), locale)) {
                return bundleManager.getText(code, locale, error.getArguments());
            }
        }
        return "";
    }
}
