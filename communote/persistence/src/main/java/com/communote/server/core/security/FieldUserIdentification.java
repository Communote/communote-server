package com.communote.server.core.security;

import java.util.HashMap;
import java.util.Map;

import com.communote.server.persistence.user.ExternalUserVO;


/**
 * A UserIdentification that is map holding some attributes. For now only the alias is supported by
 * the core. However other plugins may store additional attributes that ExternalUserRepos may
 * interpret.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class FieldUserIdentification implements
        UserIdentification {

    private final Map<String, Object> properties = new HashMap<String, Object>();

    private String externalSystemId;

    private String externalUserId;

    private ExternalUserVO previousExternalUser;

    private String userAlias;

    private Long userId;

    private Boolean isSystemUser;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExternalSystemId() {
        return externalSystemId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExternalUserId() {
        return externalUserId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getIsSystemUser() {
        return isSystemUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalUserVO getPreviousExternalUser() {
        return previousExternalUser;
    }

    /**
     * 
     * @return some addtional properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAlias() {
        return userAlias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUserId() {
        return userId;
    }

    /**
     * 
     * @param externalSystemId
     *            the external system
     * @return this
     */
    public FieldUserIdentification setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
        return this;
    }

    /**
     * 
     * @param externalUserId
     *            the id of the user in an external system
     * @return this
     */
    public FieldUserIdentification setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
        return this;
    }

    /**
     * @param isSystemUser
     *            states if this identification represents a system user. false means a definite no,
     *            true means a definite yes. Null means it cannot be said.
     * @return this
     */
    public FieldUserIdentification setIsSystemUser(Boolean isSystemUser) {
        this.isSystemUser = isSystemUser;
        return this;
    }

    /**
     * Also set the externalUserId if it has not been set before
     * 
     * @param previousExternalUser
     *            user information that have been obtained before
     * @return this
     */
    public FieldUserIdentification setPreviousExternalUser(ExternalUserVO previousExternalUser) {
        if (this.externalUserId == null) {
            this.externalUserId = previousExternalUser.getExternalUserName();
        }
        this.previousExternalUser = previousExternalUser;
        return this;
    }

    /**
     * 
     * @param userAlias
     *            the internal user alias
     * @return this
     */
    public FieldUserIdentification setUserAlias(String userAlias) {
        this.userAlias = userAlias;
        return this;
    }

    /**
     * 
     * @param userId
     *            the user id
     * @return this
     */
    public FieldUserIdentification setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "userId=" + userId + " "
                + "userAlias=" + userAlias + " "
                + "externalUserId=" + externalUserId + " "
                + "externalSystemId=" + externalSystemId + " "
                + "previousExternalUser=" + previousExternalUser + " "
                + "properties=" + properties;
    }
}
