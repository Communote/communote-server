package com.communote.plugins.mq.message.base.data.security;

/**
 * Identity context, representing execution context of a user
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserIdentityContext extends IdentityContext {

	private UserIdentity identity;

	/**
	 * @return the identity
	 */
	public UserIdentity getIdentity() {
		return identity;
	}

	/**
	 * @param identity
	 *            the identity to set
	 */
	public void setIdentity(UserIdentity identity) {
		this.identity = identity;
	}

}
