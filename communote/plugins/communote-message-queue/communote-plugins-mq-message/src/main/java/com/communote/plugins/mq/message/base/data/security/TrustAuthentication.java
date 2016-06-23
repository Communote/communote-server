package com.communote.plugins.mq.message.base.data.security;

/**
 * Authentication indicating that the user identity can be trusted and no further password is
 * required.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TrustAuthentication extends Authentication {

    private UserIdentity identity;

    public TrustAuthentication() {

    }

    public TrustAuthentication(UsernamePasswordAuthentication authentication) {
        this.identity = authentication.getIdentity();
    }

    /**
     * @return the userIdentity
     */
    public UserIdentity getIdentity() {
        return identity;
    }

    /**
     * @param identity
     *            the userIdentity to set
     */
    public void setIdentity(UserIdentity identity) {
        this.identity = identity;
    }

}
