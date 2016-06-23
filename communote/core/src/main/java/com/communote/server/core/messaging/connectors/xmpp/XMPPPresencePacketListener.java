package com.communote.server.core.messaging.connectors.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;

/**
 * This is a simple presences {@link PacketListener} to provide automatic subscription.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPPresencePacketListener implements PacketListener {

    private final XMPPConnection connection;

    /**
     * Constructor with {@link XMPPConnection} to send messages over.
     *
     * @param connection
     *            The connection.
     */
    public XMPPPresencePacketListener(XMPPConnection connection) {
        this.connection = connection;
    }

    /**
     * This only handles {@link Presence} packets with type "subscribe". {@inheritDoc}
     * 
     * @throws NotConnectedException
     */
    @Override
    public void processPacket(Packet packet) throws NotConnectedException {
        if (!(packet instanceof Presence)) {
            return;
        }
        Presence presence = (Presence) packet;
        switch (presence.getType()) {
        case subscribe:
            sendSubscriptionRequest(presence);
            break;
        }
    }

    /**
     * This method sends a subscription request to the sender.
     *
     * @param presence
     *            The presence used for sender and receiver information.
     * @throws NotConnectedException
     */
    private void sendSubscriptionRequest(Presence presence) throws NotConnectedException {
        int priority = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyXmpp.PRIORITY, 100);
        Presence subscription = new Presence(Presence.Type.subscribe);
        subscription.setPriority(priority);
        subscription.setFrom(presence.getTo());
        subscription.setTo(presence.getFrom());
        presence.setType(Presence.Type.subscribed);
        presence.setFrom(subscription.getFrom());
        presence.setTo(subscription.getTo());
        connection.sendPacket(presence);
        connection.sendPacket(subscription);
    }
}
