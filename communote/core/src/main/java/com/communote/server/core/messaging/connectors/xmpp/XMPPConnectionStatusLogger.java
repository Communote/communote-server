package com.communote.server.core.messaging.connectors.xmpp;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection listener that logs changes connection failures
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class XMPPConnectionStatusLogger implements ConnectionListener {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMPPConnectionStatusLogger.class);

    @Override
    public void authenticated(XMPPConnection arg0) {
        LOGGER.debug("Authenticated");

    }

    @Override
    public void connected(XMPPConnection arg0) {
        LOGGER.debug("Connected to XMPP server");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectionClosed() {
        LOGGER.debug("Connection to XMPP server was closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectionClosedOnError(Exception e) {
        LOGGER.debug("Connection to XMPP server was closed because of an error", e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconnectingIn(int arg0) {
        LOGGER.debug("Reconnecting to XMPP server: ", arg0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconnectionFailed(Exception e) {
        LOGGER.error("Reconnecting to XMPP server failed", e);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconnectionSuccessful() {
        LOGGER.debug("Reconnecting to XMPP server succeeded");
    }

}
