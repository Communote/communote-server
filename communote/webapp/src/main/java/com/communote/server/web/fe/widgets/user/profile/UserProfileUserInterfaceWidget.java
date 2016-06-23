package com.communote.server.web.fe.widgets.user.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.vo.user.preferences.UserInterfaceUserPreference;
import com.communote.server.service.UserPreferenceService;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.widgets.annotations.AnnotatedSingleResultWidget;
import com.communote.server.widgets.annotations.ViewIdentifier;
import com.communote.server.widgets.annotations.WidgetAction;

/**
 * Widget for displaying the form to change the user e-mail
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@ViewIdentifier("core.widget.user.profile.ui")
public class UserProfileUserInterfaceWidget extends AnnotatedSingleResultWidget {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(UserProfileUserInterfaceWidget.class);

    /**
     * Does nothing.
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

    /**
     * @return Returns the {@link UserInterfaceUserPreference}
     */
    @Override
    protected Object processSingleResult() {
        try {
            return ServiceLocator.findService(UserPreferenceService.class).getPreferences(
                    UserInterfaceUserPreference.class);
        } catch (AuthorizationException e) {
            LOGGER.warn("Error accessing user preference: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Method to save the settings.
     */
    @WidgetAction("SAVE")
    public void saveSetting() {
        UserPreferenceService userPreferenceService = ServiceLocator
                .findService(UserPreferenceService.class);
        try {
            UserInterfaceUserPreference preferences = userPreferenceService
                    .getPreferences(UserInterfaceUserPreference.class);
            preferences.setPreferences(getParameters());
            userPreferenceService.storePreferences(preferences);
            MessageHelper.saveMessageFromKey(getRequest(), "user.profile.ui.save.success");
        } catch (AuthorizationException e) {
            LOGGER.error("Error accessing user preference: {}", e.getMessage());
        }
    }
}
