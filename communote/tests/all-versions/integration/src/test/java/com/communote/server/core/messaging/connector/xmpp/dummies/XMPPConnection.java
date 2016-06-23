package com.communote.server.core.messaging.connector.xmpp.dummies;

import java.io.IOException;

import javax.security.sasl.SaslException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;

/**
 * This is a dummy connection used for unit tests.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPConnection extends org.jivesoftware.smack.XMPPConnection {
    private Packet lastPacket = null;

    /**
     * Constructor.
     */
    public XMPPConnection() {
        super(new ConnectionConfiguration("dummy"));
    }

    @Override
    protected void connectInternal() throws SmackException, IOException, XMPPException {

    }

    @Override
    public String getConnectionID() {
        return "dummy-connection";
    }

    /**
     *
     * @return The last packet.
     */
    public Packet getLastPacket() {
        return lastPacket;
    }

    @Override
    public String getUser() {
        return "dummy";
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isSecureConnection() {
        return false;
    }

    @Override
    public boolean isUsingCompression() {
        return false;
    }

    @Override
    public void login(String arg0, String arg1, String arg2) throws XMPPException, SmackException,
    SaslException, IOException {
    }

    @Override
    public void loginAnonymously() throws XMPPException, SmackException, SaslException, IOException {

    }

    /**
     * This method checks the send packet.
     *
     * @param packet
     *            The packet to send.
     */
    @Override
    public void sendPacket(Packet packet) {
        lastPacket = packet;
    }

    @Override
    protected void sendPacketInternal(Packet arg0) throws NotConnectedException {

    }

    @Override
    protected void shutdown() {

    }
}
