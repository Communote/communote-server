package com.communote.server.web.fe.widgets.user;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagement.RegistrationType;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.widgets.annotations.AnnotatedSingleResultWidget;
import com.communote.server.widgets.annotations.ViewIdentifier;
import com.communote.server.widgets.annotations.WidgetAction;

/**
 * Widget to invite new user to the system via email.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@ViewIdentifier("core.widget.user.invite.via.email")
public class InviteUserViaEmailWidget extends AnnotatedSingleResultWidget {

    private static final String PARAMETER_EMAIL_ADDRESS = "invitationEmailAddress";
    private final UserManagement userManagement = ServiceLocator.instance().getService(
            UserManagement.class);

    /**
     * Does nothing.
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

    /**
     * This method is called, when the form is send.
     */
    @WidgetAction("Invite")
    public void inviteUser() {
        Locale locale = SessionHandler.instance().getCurrentLocale(getRequest());
        String email = getParameter(PARAMETER_EMAIL_ADDRESS);
        if (StringUtils.isBlank(email)) {
            return;
        }
        try {
            userManagement.registerUser(email, locale, RegistrationType.INVITED);
            MessageHelper.saveMessageFromKey(getRequest(), "widget.user.invite.via.email.success",
                    MessageHelper.MESSAGES_KEY, email);
        } catch (EmailAlreadyExistsException e) {
            MessageHelper.saveMessageFromKey(getRequest(),
                    "widget.user.invite.via.email.already-in", MessageHelper.WARNING_MESSAGES_KEY);
        } catch (EmailValidationException e) {
            MessageHelper.saveErrorMessageFromKey(getRequest(),
                    "widget.user.invite.via.email.invalid-address");
            getRequest().setAttribute(PARAMETER_EMAIL_ADDRESS, email);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return True, if the form should be showed, else false.
     */
    @Override
    protected Object processSingleResult() {
        ClientConfigurationProperties properties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        return properties.isRegistrationAllowed()
                || (SecurityHelper.isClientManager() && properties.isDBAuthenticationAllowed());
    }
}
