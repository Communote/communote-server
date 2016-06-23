package com.communote.server.core.messaging.connector.xmpp;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.core.messaging.connector.xmpp.dummies.MockNoteService;
import com.communote.server.core.messaging.connector.xmpp.dummies.XMPPConnection;
import com.communote.server.core.messaging.connectors.xmpp.XMPPMessagePacketListener;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link XMPPMessagePacketListener}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPPacketListenerTest extends CommunoteIntegrationTest {

    private final static String USER_SUFFIX = "@domain";
    private final static String BLOG_SUFFIX = "@domain";
    private User user;
    private Blog blog;

    /**
     * This method sends a message without an alias, the listener should send an error message back
     * to the sender.
     *
     * @param listener
     *            The listener.
     * @param connection
     *            The connection.
     * @throws NotConnectedException
     */
    private void processMessageWithoutAlias(PacketListener listener, XMPPConnection connection)
            throws NotConnectedException {
        Message message = new Message();
        String sender = user.getAlias() + ".global" + USER_SUFFIX;
        message.setFrom(sender);
        message.setThread(blog.getNameIdentifier() + ".global" + BLOG_SUFFIX);
        message.setBody("Empty");
        listener.processPacket(message);
        Packet packet = connection.getLastPacket();
        Assert.assertEquals(packet.getTo(), sender);
        Assert.assertTrue(((Message) packet).getBody().contains("To post to a topic"));
    }

    /**
     * Send a packet that is not a Message. The listener should do nothing.
     *
     * @param listener
     *            The listener.
     * @param connection
     *            The connection.
     * @throws NotConnectedException
     */
    private void processNonMessagePacket(PacketListener listener, XMPPConnection connection)
            throws NotConnectedException {
        listener.processPacket(new Presence(Presence.Type.available));
        Assert.assertNull(connection.getLastPacket());
    }

    /**
     * Setup.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(groups = "integration-test-setup")
    public void setup() throws Exception {
        user = TestUtils.createRandomUser(true);
        blog = TestUtils.createRandomBlog(true, true, user);
        AuthenticationTestUtils.setSecurityContext(user);
        Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyXmpp.BLOG_SUFFIX, USER_SUFFIX);
        settings.put(ApplicationPropertyXmpp.USER_SUFFIX, BLOG_SUFFIX);
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateApplicationConfigurationProperties(settings);
    }

    /**
     * Test for
     * {@link XMPPMessagePacketListener#processPacket(org.jivesoftware.smack.packet.Packet)}.
     * 
     * @throws NotConnectedException
     */
    @Test
    public void testProcessPacket() throws NotConnectedException {
        XMPPConnection connection = new XMPPConnection();
        PacketListener listener = new XMPPMessagePacketListener(connection, new MockNoteService());
        processNonMessagePacket(listener, connection);
        processMessageWithoutAlias(listener, connection);
    }

}
