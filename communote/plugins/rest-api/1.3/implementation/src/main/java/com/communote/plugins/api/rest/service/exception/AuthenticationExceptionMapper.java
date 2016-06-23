package com.communote.plugins.api.rest.service.exception;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.springframework.security.core.AuthenticationException;

import com.communote.server.core.security.TermsOfUseNotAcceptedException;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.external.acegi.UserAccountNotActivatedException;
import com.communote.server.external.acegi.UserAccountPermanentlyLockedException;
import com.communote.server.external.acegi.UserAccountTemporarilyLockedException;

/**
 * {@link AbstractExceptionMapper} for {@link AuthenticationException}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Provider
public class AuthenticationExceptionMapper extends AbstractExceptionMapper<AuthenticationException> {

    private final static Map<Class<? extends AuthenticationException>, String> EXCEPTION_TO_MESSAGE_KEY_MAP = new HashMap<Class<? extends AuthenticationException>, String>();
    static {
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(TermsOfUseNotAcceptedException.class,
                "login.error.termsOfUseNotAccepted");
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(UserAccountPermanentlyLockedException.class,
                "login.error.userPermLocked");
        EXCEPTION_TO_MESSAGE_KEY_MAP.put(UserAccountNotActivatedException.class,
                "login.error.notactivated");
    }

    @Override
    public String getErrorMessage(AuthenticationException exception) {
        if (exception instanceof UserAccountTemporarilyLockedException) {
            return getLocalizedMessage("login.error.userTempLocked",
                    ((UserAccountTemporarilyLockedException) exception).getFormattedLockedTimeout(
                            getCurrentUserLocale(org.restlet.Request.getCurrent()),
                            UserManagementHelper.getEffectiveUserTimeZone((Long) null)));
        } else {

            String messageKey = EXCEPTION_TO_MESSAGE_KEY_MAP.get(exception.getClass());
            return getLocalizedMessage(messageKey != null ? messageKey : "login.error.failed");
        }
    }

    /**
     * @return Status.UNAUTHORIZED
     */
    @Override
    public int getStatusCode() {
        return Status.UNAUTHORIZED.getStatusCode();
    }

}
