package com.communote.plugins.mq.service.impl;

import java.util.UUID;

import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.service.exception.MessageQueueCommunicationException;
import com.communote.plugins.mq.service.provider.ProviderMessageSender;
import com.communote.plugins.mq.service.provider.TransferMessage;
import com.communote.plugins.mq.service.provider.TransferMessage.TMContentType;

/**
 * The Class TestCntMessageSenderImpl.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteMessageSenderImplTest {

    public class MockTestMessage extends BaseMessage {

        private String test;

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }
    }

    /** The converter mock. */
    private MessagesConverterImpl converterMock;

    /** The sender mock. */
    private ProviderMessageSender senderMock;

    /**
     * Inits the mocks.
     */
    @BeforeMethod
    public void initMocks() {
        converterMock = EasyMock.createMock(MessagesConverterImpl.class);
        senderMock = EasyMock.createMock(ProviderMessageSender.class);
    }

    /**
     * Test reply message.
     * 
     * @throws MessageQueueCommunicationException
     */
    @Test
    public void testReplyMessage() throws MessageQueueCommunicationException {
        CommunoteReplyMessage message = new CommunoteReplyMessage();
        TransferMessage tm = new TransferMessage();
        EasyMock.expect(converterMock.convertToTransferMessage(message))
                .andReturn(tm);
        senderMock.sendReplyMessage(tm);
        EasyMock.replay(converterMock, senderMock);

        CommunoteMessageSenderImpl impl = new CommunoteMessageSenderImpl();
        impl.setConverter(converterMock);
        impl.setSender(senderMock);
        impl.sendReplyMessage(message);

        EasyMock.verify(senderMock);
    }

    /**
     * Test reply message.
     * 
     * @throws MessageQueueCommunicationException
     */
    @Test
    public void testSendMessage() throws MessageQueueCommunicationException {
        MockTestMessage message = new MockTestMessage();
        message.setTest(UUID.randomUUID().toString());

        TransferMessage tm = new TransferMessage();
        EasyMock.expect(converterMock.convertToTransferMessage(message)).andReturn(tm);
        EasyMock.expect(converterMock.getContentType()).andReturn(TMContentType.JSON);
        senderMock.sendMessageInternal(tm);
        EasyMock.replay(converterMock, senderMock);

        CommunoteMessageSenderImpl impl = new CommunoteMessageSenderImpl();
        impl.setConverter(converterMock);
        impl.setSender(senderMock);
        impl.sendMessage(message, message.getClass().getSimpleName(), "1.0");

        EasyMock.verify(senderMock);
    }
}
