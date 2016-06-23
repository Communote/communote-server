package com.communote.plugins.mq.provider.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.service.exception.MessageQueueCommunicationException;
import com.communote.plugins.mq.service.provider.ProviderMessageSender;
import com.communote.plugins.mq.service.provider.TransferMessage;

/**
 * 
 * Plays the role of an adapter between JMS MQ sender and CNT specific sender.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate
@Provides(specifications = { JMSMessageSender.class, ProviderMessageSender.class })
public class JMSMessageSender implements ProviderMessageSender {

    /** The session. */
    private Session session;

    /** The provider. */
    @Requires
    private JMSAdapter provider;

    /**
     * MessageProducer for message send internal within Communote
     */
    private MessageProducer internalMessageProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSMessageSender.class);

    @Invalidate
    public void close() {
        if (internalMessageProducer != null) {
            try {
                internalMessageProducer.close();
            } catch (JMSException e) {
                LOGGER.error("Error closing internalMessageProducer " + e.getMessage(), e);
            }
            internalMessageProducer = null;
        }
    }

    /**
     * converts CNT transfer message to JMS message.
     * 
     * @param transferMessage
     *            the transfer message
     * @return the message
     * @throws JMSException
     *             the JMS exception
     */
    private Message convertToJMSMessage(TransferMessage transferMessage)
            throws JMSException {
        TextMessage message = session.createTextMessage(transferMessage.getContent());

        message.setJMSCorrelationID((String) transferMessage
                .getHeader(TransferMessage.HEADER_CORRELATION_ID));
        message.setStringProperty(TransferMessage.HEADER_MESSAGE_TYPE,
                (String) transferMessage.getHeader(TransferMessage.HEADER_MESSAGE_TYPE));
        message.setStringProperty(TransferMessage.HEADER_MESSAGE_VERSION,
                (String) transferMessage.getHeader(TransferMessage.HEADER_MESSAGE_VERSION));
        message.setStringProperty(TransferMessage.HEADER_CONTENT_TYPE,
                (String) transferMessage.getHeader(TransferMessage.HEADER_CONTENT_TYPE));

        return message;
    }

    /**
     * returns producer for the specified destination
     * 
     * @param dest
     *            destination
     * @return created producer
     * @throws JMSException
     *             exception
     */
    protected MessageProducer getProducer(Destination dest) throws JMSException {
        return session.createProducer(dest);
    }

    /**
     * initializes message sender, creating new session and connecting to the message queue.
     * 
     * @throws JMSException
     *             exception
     */
    @Validate
    public void init() throws JMSException {
        try {
            session = provider.getSession();

            internalMessageProducer = getProducer(provider.getDestination());

        } catch (JMSException e) {
            LOGGER.error("Exception while JMS message sender instantiation", e);
            throw e;
        }
    }

    /**
     * Sends a message to the message queue internally
     * 
     * @throws MessageQueueCommunicationException
     */
    @Override
    public void sendMessageInternal(TransferMessage transferMessage)
            throws MessageQueueCommunicationException {

        try {
            Message message = convertToJMSMessage(transferMessage);
            internalMessageProducer.send(message);
        } catch (JMSException e) {
            LOGGER.error("Exception while sending message to internal queue.", e);
            throw new MessageQueueCommunicationException(
                    "Error sending message to internal queue: "
                            + e.getMessage(), e);
        }
    }

    /**
     * sends received message to the MQ
     * 
     * @param transferMessage
     *            message to be sent
     * @throws MessageQueueCommunicationException
     */
    @Override
    public void sendReplyMessage(TransferMessage transferMessage)
            throws MessageQueueCommunicationException {
        try {
            MessageProducer producer = getProducer((Destination) transferMessage
                    .getHeader(TransferMessage.HEADER_REPLY_QUEUE));
            producer.send(convertToJMSMessage(transferMessage));
        } catch (JMSException e) {
            LOGGER.error("Exception while sending reply message.", e);
            throw new MessageQueueCommunicationException("Error while sending reply message: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Method to set the session.
     * 
     * @param session
     *            JMS session to be set
     */
    public void setSession(Session session) {
        this.session = session;
    }

}
