package com.communote.server.core.security;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AccountTemporarilyLockedException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4259422070316078121L;

    private Date lockedTimeout;

    /**
     * Constructs a new instance of AccountTemporarilyLockedException
     *
     */
    public AccountTemporarilyLockedException(Date lockedTimeout) {
        super("The account is temporarily locked until "
                + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                        Locale.ENGLISH).format(lockedTimeout));
        this.lockedTimeout = lockedTimeout;
    }

    /**
     *
     */
    public Date getLockedTimeout() {
        return this.lockedTimeout;
    }

    public void setLockedTimeout(Date lockedTimeout) {
        this.lockedTimeout = lockedTimeout;
    }

}
