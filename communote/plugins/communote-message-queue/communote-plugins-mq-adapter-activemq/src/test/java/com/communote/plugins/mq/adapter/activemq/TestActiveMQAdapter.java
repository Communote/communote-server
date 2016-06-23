package com.communote.plugins.mq.adapter.activemq;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TestActiveMQAdapter {

    private Connection connectionMock;
    private Session sessionMock;

    /**
     * init mocks
     */
    @BeforeMethod
    public void initMocks() {
        connectionMock = EasyMock.createMock(Connection.class);
        sessionMock = EasyMock.createMock(Session.class);
    }

    /**
     * @throws JMSException
     *             exception
     */
    @Test
    public void testCreateSession() throws JMSException {
        EasyMock.expect(
                connectionMock.createSession(false, Session.AUTO_ACKNOWLEDGE))
                .andReturn(sessionMock);

        EasyMock.replay(connectionMock, sessionMock);

        ActiveMQAdapter adapter = new ActiveMQAdapter();
        adapter.setConnection(connectionMock);

        Session session = adapter.getSession();
        Assert.assertEquals(session, sessionMock);
    }
}
