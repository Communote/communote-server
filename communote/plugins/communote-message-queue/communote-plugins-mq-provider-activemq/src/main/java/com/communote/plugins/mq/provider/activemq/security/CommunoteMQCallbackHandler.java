package com.communote.plugins.mq.provider.activemq.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.activemq.jaas.JassCredentialCallbackHandler;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteMQCallbackHandler implements CallbackHandler {

	private JassCredentialCallbackHandler credentialsCallBackHandler;
	private String remoteAddress;
	private String connectorURI;

	/**
	 * @param username
	 *            user name
	 * @param password
	 *            password
	 * @param remoteAddress
	 *            remote address
	 * @param connectorURI
	 *            connector uri
	 */
	public CommunoteMQCallbackHandler(String username, String password,
			String remoteAddress, String connectorURI) {
		credentialsCallBackHandler = new JassCredentialCallbackHandler(
				username, password);
		this.remoteAddress = remoteAddress;
		this.connectorURI = connectorURI;
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (Callback callback : callbacks) {
			if (callback instanceof RemoteAddressCallBack) {
				((RemoteAddressCallBack) callback)
						.setRemoteAddress(remoteAddress);
			} else if (callback instanceof ConnectorURICallBack) {
				((ConnectorURICallBack) callback).setConnectorURI(connectorURI);
			} else {
				credentialsCallBackHandler.handle(new Callback[] { callback });
			}

		}
	}

}
