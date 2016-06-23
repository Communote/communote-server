package com.communote.plugins.mq.message.base.data.security;

/**
 * Authentication method with login and password
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UsernamePasswordAuthentication extends Authentication {

	private UserIdentity identity;

	private char[] password;

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

	/**
	 * @return the password
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}

}
