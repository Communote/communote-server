package com.communote.server.core.messaging.connectors.xmpp;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * {@link PacketFilter} for use with the {@link XMPPMessagePacketListener}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPMessagePacketFilter implements PacketFilter {

    /**
     * Only packages of type {@link Message} are accepted.
     *
     * @param packet
     *            The {@link Packet} to check again.
     * @return True if the packet is a {@link Message}.
     */
    @Override
    public boolean accept(Packet packet) {
        return packet instanceof Message;
    }
}
