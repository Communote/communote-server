package com.communote.server.core.security;

import com.communote.server.persistence.user.ExternalUserVO;

/**
 * The user identification provides information on identifying a particular user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface UserIdentification {

    /**
     *
     * @return the external system id
     */
    public String getExternalSystemId();

    /**
     *
     * @return the external user id
     */
    public String getExternalUserId();

    /**
     * States if this identification represents a system user. False means a definite no, true means
     * a definite yes. Null means it cannot be said.
     *
     * @return True if the identification represents a system user, false if not and null if it
     *         can't be told.
     */
    public Boolean getIsSystemUser();

    /**
     *
     * @return an external user with its data that has been authenticated and the data has been
     *         already obtained
     */
    public ExternalUserVO getPreviousExternalUser();

    /**
     *
     * @return the internal user alias
     */
    public String getUserAlias();

    /**
     *
     * @return the user id
     */
    public Long getUserId();
}
