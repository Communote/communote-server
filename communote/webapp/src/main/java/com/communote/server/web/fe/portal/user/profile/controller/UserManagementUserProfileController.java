package com.communote.server.web.fe.portal.user.profile.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfileFields;
import com.communote.server.persistence.user.UserProfileVO;
import com.communote.server.service.UserService;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileForm;

/**
 * Controller for user profile manipulation.
 *
 * Also see UserProfileDetailsController.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserManagementUserProfileController extends BaseFormController {
    /**
     * Helper class to save all user settings within on transaction.
     */
    private class SaveInTransaction implements RunInTransaction {
        private final Long userId;
        private final UserProfileForm form;

        /**
         * Constructor.
         *
         * @param userId
         *            The user id.
         * @param form
         *            Form with user information.
         */
        public SaveInTransaction(Long userId, UserProfileForm form) {
            this.userId = userId;
            this.form = form;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute() throws TransactionException {
            ServiceLocator.instance().getService(UserManagement.class)
            .updateLanguage(userId, form.getLanguageCode());
            ServiceLocator.findService(UserProfileManagement.class).updateUserProfile(userId,
                    form.getUserProfile());

        }

    }

    private static final String PARAM_USER_ID = "userId";

    /** Logger. */
    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(UserManagementUserProfileController.class);

    /**
     * Default constructor
     */
    public UserManagementUserProfileController() {
        super();
    }

    /**
     * Update a user profile
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param userId
     *            the ID of the user whose profile will be updated
     * @param form
     *            the form backing object
     * @param errors
     *            object to bind errors
     * @return the new model and view or null in case of exception
     */
    private ModelAndView doUpdateProfile(HttpServletRequest request, HttpServletResponse response,
            UserProfileForm form, BindException errors, Long userId) {

        try {
            if (errors.getErrorCount() == 0) {
                SaveInTransaction inTransaction = new SaveInTransaction(userId, form);

                ServiceLocator.findService(TransactionManagement.class).execute(inTransaction);

                ModelAndView modelAndView = new ModelAndView(getFormView(), getCommandName(), form);

                if (userId == SecurityHelper.getCurrentUserId()) {
                    SessionHandler.instance().currentUserLocaleChanged(request);
                }

                ControllerHelper.setApplicationSuccess(response);
                MessageHelper.saveMessageFromKey(request,
                        "client.user.management.save.profile.success");

                return modelAndView;
            }
        } catch (Exception e) {
            errors.rejectValue("globalError", "user.profile.update.error", "Update error");

            ControllerHelper.setApplicationFailure(response);
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.user.management.save.profile.error");

            LOG.error("Error updating profile: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Long userId = ServletRequestUtils.getLongParameter(request, PARAM_USER_ID);
        User user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByUserId(userId, true);
        Collection<String> tags = new HashSet<String>();
        if (user.getTags() != null) {
            for (Tag tag : user.getTags()) {
                tags.add(tag.getName());
            }
        }
        UserProfileVO profile = ServiceLocator.instance().getService(UserProfileManagement.class)
                .findUserProfileVOByUserId(user.getId());
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        UserProfileForm userProfileForm = new UserProfileForm(profile, locale.getLanguage(),
                StringUtils.join(tags, ", "), new HashSet<String>());
        setFixedProfileFields(userId, userProfileForm);
        request.setAttribute("userId", user.getId());
        request.setAttribute("userStatus", user.getStatus());
        request.setAttribute("alias", user.getAlias());
        request.setAttribute("isClientManager", SecurityHelper.isClientManager());
        return userProfileForm;
    }

    private UserService getUserService() {
        return ServiceLocator.findService(UserService.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        UserProfileForm form = (UserProfileForm) command;
        Long userId = ServletRequestUtils.getLongParameter(request, PARAM_USER_ID);
        if (userId != null) {
            return doUpdateProfile(request, response, form, errors, userId);
        }
        return null;
    }

    /**
     * Sets the fixed fields for the given user.
     *
     * @param user
     *            The user.
     * @param userProfileForm
     *            The users profile form.
     */
    private void setFixedProfileFields(Long userId, UserProfileForm userProfileForm) {

        Collection<UserProfileFields> fields = getUserService().getImmutableProfileFieldsOfUser(
                userId);
        for (UserProfileFields field : fields) {
            userProfileForm.getFixedProfileFields().add(field.name());
        }

    }
}