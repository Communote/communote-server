package com.communote.server.web.fe.portal.user.profile.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.converter.Converter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.converter.user.UserToUserDataConverter;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfileFields;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.UserProfileVO;
import com.communote.server.service.UserService;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileForm;

/**
 * Controller for user profile manipulation.
 *
 * Also see UserManagementUserProfileController
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileDetailsController extends BaseFormController {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(UserProfileDetailsController.class);

    private final Converter<Tag, TagData> tagConverter = new Converter<Tag, TagData>() {

        @Override
        public TagData convert(Tag source) {
            TagData tagListItem = new TagData();

            // just the name needed
            tagListItem.setName(source.getName());

            return tagListItem;
        }
    };
    private final UserToUserDataConverter<UserData> userConverter = new UserToUserDataConverter<>(
            UserData.class, false, tagConverter);

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
    private ModelAndView doUpdateProfile(final HttpServletRequest request,
            final HttpServletResponse response, final UserProfileForm form, BindException errors,
            final Long userId) {
        if (errors.getErrorCount() != 0) {
            return null;
        }
        try {
            ServiceLocator.instance().getService(TransactionManagement.class)
                    .execute(new RunInTransaction() {
                        @Override
                        public void execute() throws TransactionException {
                            UserManagement userManagement = ServiceLocator.instance().getService(
                            UserManagement.class);
                            userManagement.updateLanguage(userId, form.getLanguageCode());
                            ServiceLocator.findService(UserProfileManagement.class)
                                    .updateUserProfile(userId, form.getUserProfile());
                            String[] tagsAsStringArray = TagParserFactory.instance()
                                    .getDefaultTagParser().parseTags(form.getTags());
                            Set<TagTO> tags = new HashSet<TagTO>();
                            for (String tag : tagsAsStringArray) {
                                tags.add(new TagTO(tag, Types.ENTITY.getDefaultTagStoreId()));
                            }
                            userManagement.updateUserTags(userId, tags);

                        }
                    });

            // change the language
            SessionHandler.instance().currentUserLocaleChanged(request);

            String message = MessageHelper.getText(request, "user.profile.update.success");
            MessageHelper.saveMessage(request, message);
            ControllerHelper.setApplicationSuccess(response);

            return new ModelAndView(getFormView(), getCommandName(), form);
        } catch (Exception e) {
            // TODO error messages is not informative
            errors.reject("user.profile.update.error", "Update error");
            LOGGER.error("Error updating profile: {}", e.getMessage(), e);
            ControllerHelper.setApplicationFailure(response);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        final Long userId = SecurityHelper.assertCurrentUserId();

        // it is not perfect to get the with tags and again for the locale
        final UserData userListItem = getUserManagement().getUserById(userId, userConverter);
        final User user = getUserManagement().findUserByUserId(userId, false);
        // and then again load the profile
        final UserProfileVO profile = getUserProfileManagement().findUserProfileVOByUserId(userId);

        Collection<String> tags = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(userListItem.getTags())) {
            for (TagData tagItem : userListItem.getTags()) {
                tags.add(tagItem.getName());
            }
        }
        // use the locale of the user as defined in the backend, the session locale might be just a
        // temporar change
        Locale locale = user.getLanguageLocale();
        UserProfileForm userProfileForm = new UserProfileForm(profile,
                locale == null ? Locale.ENGLISH.toString() : locale.toString(), StringUtils.join(
                        tags, ", "), new HashSet<String>());
        setFixedProfileFields(userId, userProfileForm);
        request.setAttribute("isUserLanguageUsed",
                ResourceBundleManager.instance().isUsedLanguage(user.getLanguageCode()));
        return userProfileForm;
    }

    private UserManagement getUserManagement() {
        return ServiceLocator.instance().getService(UserManagement.class);
    }

    private UserProfileManagement getUserProfileManagement() {
        return ServiceLocator.instance().getService(UserProfileManagement.class);
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
        ModelAndView mav = null;
        Long userId = SecurityHelper.getCurrentUserId();
        if (userId != null) {
            mav = doUpdateProfile(request, response, form, errors, userId);
        }
        if (errors.getErrorCount() > 0 || mav == null) {
            mav = showForm(request, errors, getFormView());
        }
        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView processFormSubmission(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors) throws Exception {
        if (errors.hasErrors()) {
            ControllerHelper.setApplicationFailure(response);
        }
        return super.processFormSubmission(request, response, command, errors);
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
