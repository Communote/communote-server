package com.communote.plugins.mq.message.base.data.security;

/**
 * User identity, used to specify the user, who is going to perform some action with message
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserIdentity {

    /**
     * Field type for internal user alias
     */
    public final static String IDENTITY_TYPE_ALIAS = "userAlias";

    /**
     * Field type for internal user alias
     */
    public final static String IDENTITY_TYPE_EXTERNAL_ID = "externalUserId";

    /**
     * Field type for internal database id
     */
    public final static String IDENTITY_TYPE_USER_ID = "userId";

    private String identity;
    private String identityType;

    /**
     * @return the identity
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @return the identityType
     */
    public String getIdentityType() {
        return identityType;
    }

    /**
     * @param identity
     *            the identity to set
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /**
     * @param identityType
     *            the identityType to set
     */
    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

}
