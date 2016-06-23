package com.communote.server.core.exception.mapper;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.Status;
import com.communote.server.core.security.TermsOfUseNotAcceptedException;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.external.acegi.UserAccountNotActivatedException;
import com.communote.server.external.acegi.UserAccountPermanentlyLockedException;
import com.communote.server.external.acegi.UserAccountTemporarilyLockedException;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    private final class LockedTimeoutErrorMessage implements LocalizedMessage {

        private final Date lockedTimeout;

        LockedTimeoutErrorMessage(Date lockedTimeout) {
            this.lockedTimeout = lockedTimeout;
        }

        @Override
        public String toString(Locale locale, Object... arguments) {
            DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                    DateFormat.MEDIUM, locale);
            // no user since authentication failed
            dateFormatter.setTimeZone(UserManagementHelper.getEffectiveUserTimeZone((Long) null));
            return ResourceBundleManager.instance().getText("login.error.userTempLocked", locale,
                    dateFormatter.format(lockedTimeout));
        }

    }

    private final static Map<Class<? extends AuthenticationException>, String> EXCEPTION_TO_MESSAGE_KEY_MAP = new HashMap<Class<? extends AuthenticationException>, String>();

    static {
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(TermsOfUseNotAcceptedException.class,
                "login.error.termsOfUseNotAccepted");
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(UserAccountPermanentlyLockedException.class,
                "login.error.userPermLocked");
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(UserAccountNotActivatedException.class,
                "login.error.notactivated");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<AuthenticationException> getExceptionClass() {
        return AuthenticationException.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status mapException(AuthenticationException exception) {
        if (exception instanceof UserAccountTemporarilyLockedException) {
            return new Status(new LockedTimeoutErrorMessage(
                    ((UserAccountTemporarilyLockedException) exception).getLockedTimeout()),
                    ErrorCodes.AUTHENTICATION_ERROR);
        }
        String messageKey = EXCEPTION_TO_MESSAGE_KEY_MAP.get(exception.getClass());
        return new Status(messageKey != null ? messageKey : "login.error.failed", null,
                ErrorCodes.AUTHENTICATION_ERROR);
    }
}
