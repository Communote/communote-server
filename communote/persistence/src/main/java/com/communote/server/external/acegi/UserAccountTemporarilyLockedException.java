package com.communote.server.external.acegi;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception to be thrown if the user account was temporarily locked
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAccountTemporarilyLockedException extends AuthenticationException {

    private static final long serialVersionUID = -5832088339667410062L;

    private static final DateFormat LOCKED_TIMEOUT_FORMATTER = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.ENGLISH);

    private final Date lockedTimeout;

    /**
     * specific constructor
     *
     * @param msg
     *            exception message
     * @param lockedTimeout
     *            timeout of locked user
     */
    public UserAccountTemporarilyLockedException(Date lockedTimeout) {
        super("The account is temporarily locked until "
                + LOCKED_TIMEOUT_FORMATTER.format(lockedTimeout));
        this.lockedTimeout = lockedTimeout;
    }

    /**
     * A formatted locked timeout
     *
     * @return properly formatted locked timeout
     */
    public String getFormattedLockedTimeout(Locale locale, TimeZone timezone) {
        DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.MEDIUM, locale);
        if (timezone != null) {
            dateFormatter.setTimeZone(timezone);
        }
        return dateFormatter.format(lockedTimeout);
    }

    public Date getLockedTimeout() {
        return lockedTimeout;
    }
}
