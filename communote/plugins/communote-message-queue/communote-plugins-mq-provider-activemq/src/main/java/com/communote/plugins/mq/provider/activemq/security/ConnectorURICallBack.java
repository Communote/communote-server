package com.communote.plugins.mq.provider.activemq.security;

import javax.security.auth.callback.Callback;

/**
 * Connector Uri call back
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConnectorURICallBack implements Callback {

	private String connectorURI;

	/**
	 * @return the connectorURI
	 */
	public String getConnectorURI() {
		return connectorURI;
	}

	/**
	 * @param connectorURI
	 *            the connectorURI to set
	 */
	public void setConnectorURI(String connectorURI) {
		this.connectorURI = connectorURI;
	}

}
