package com.communote.plugins.mq.provider.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.service.provider.ProviderMessageConsumer;
import com.communote.plugins.mq.service.provider.TransferMessage;
import com.communote.plugins.mq.service.provider.TransferMessage.TMContentType;
import com.communote.server.core.security.FieldUserIdentification;
import com.communote.server.persistence.common.security.CommunoteUserCertificate;

/**
 * 
 * Plays the role of an adapter between JMS MQ consumer and CNT specific consumer.
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class JMSMessageConsumerImpl implements JMSMessageConsumer {

    /**
     * The Enum MessageDeliveryMode.
     */
    public enum MessageDeliveryMode {

        /** The PUSH. */
        PUSH,
        /** The PULL. */
        PULL
    }

    /** The Constant REQUEST_DELAY. */
    // TODO make runtime changes of delay possible
    // as soon as configuration mechanism is defined will be corrected
    private static final int REQUEST_DELAY = 5000;

    private JMSMessageSender jmsSender;

    /** The communote message consumer. */
    private ProviderMessageConsumer communoteMessageConsumer;

    /** The message delivery mode. */
    private MessageDeliveryMode messageDeliveryMode = MessageDeliveryMode.PUSH;

    /** The jms message consumer. */
    private MessageConsumer jmsMessageConsumer;

    /** The polling thread. */
    private Thread pollingThread = new Thread(new Runnable() {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Message message = jmsMessageConsumer.receiveNoWait();

                    if (message != null) {
                        processMessage(message);
                    } else {
                        Thread.sleep(REQUEST_DELAY);
                    }
                } catch (JMSException e) {
                    LOGGER.error("Error during message receiving. "
                            + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    });

    /**
     * I think this is ActiveMQ specific. To be correct: move the userIdentification stuff to the
     * active mq consumer, and set the header on the jms message and then just copy it to the
     * transfer.
     */
    private final static String JMS_USER_ID = "JMSXUserID";

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSMessageConsumerImpl.class);

    /**
     * converts JMS specific message to TransferMessage, used by the CNT plug-in.
     * 
     * @param message
     *            JMS message to be converted
     * @return instance of TransferMessage
     * @throws Exception
     *             exception
     */
    private TransferMessage convertToTransferMessage(Message message)
            throws Exception {
        TransferMessage transferMessage = new TransferMessage();
        transferMessage.setContentType(getContentType(message
                .getStringProperty(TransferMessage.HEADER_CONTENT_TYPE)));
        if (message instanceof TextMessage) {
            transferMessage.setContent(((TextMessage) message).getText());
        } else {
            throw new Exception("Unsupported type of the incoming message. "
                    + "The message of the type " + message.getClass().getName()
                    + " cannot be processed");
        }
        return transferMessage;
    }

    /**
     * parses content type.
     * 
     * @param contentTypeStr
     *            string representation of the content type
     * @return TMContentType, represented by the contentTypeStr, or JSON if no values was specified
     * @throws Exception
     *             an exception is thrown if specified content type could not be parsed
     */
    private TMContentType getContentType(String contentTypeStr)
            throws Exception {
        TMContentType res = null;
        try {
            res = TMContentType.valueOf(contentTypeStr);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Message ContentType " + contentTypeStr
                    + " is not supported.");
            throw new Exception("Message ContentType " + contentTypeStr
                    + " is not supported.", e);
        } catch (NullPointerException e) {
            LOGGER.warn("Message ContentType was not specified. It is assumed to be JSON.");
            res = TMContentType.JSON;
        }
        return res;
    }

    /**
     * Process message.
     * 
     * @param message
     *            the message
     */
    void processMessage(Message message) {
        try {
            LOGGER.debug("Message is delivered to JMS MessageConsumer. Processed message type == "
                    + communoteMessageConsumer.getConsumedMessageType());

            TransferMessage transferMessage = convertToTransferMessage(message);

            /**
             * I think this is ActiveMQ specific. To be correct: move the userIdentification stuff
             * to the active mq consumer, and set the header on the jms message and then just copy
             * it to the transfer.
             * 
             */
            String userId = message.getStringProperty(JMS_USER_ID);

            if (userId != null) {
                setPreAuthenticationHeaders(transferMessage, userId);
            }

            boolean trustUser = message.getBooleanProperty(JMSAdapter.MESSAGE_PROPERTY_TRUST_USER);
            transferMessage.putHeader(TransferMessage.HEADER_TRUST_USER, trustUser);

            TransferMessage reply = communoteMessageConsumer.receiveMessage(transferMessage);

            Destination replyDestination = message.getJMSReplyTo();
            if (replyDestination != null && reply != null) {
                reply.putHeader(TransferMessage.HEADER_CORRELATION_ID,
                        message.getJMSCorrelationID());
                reply.putHeader(TransferMessage.HEADER_REPLY_QUEUE,
                        replyDestination);

                jmsSender.sendReplyMessage(reply);
            }
        } catch (Exception e) {
            LOGGER.error("Error during incoming message processing.", e);
        }

    }

    /**
     * Sets CNT message consumer.
     * 
     * @param communoteMessageConsumer
     *            consumer to be set
     */
    @Override
    public void setCommunoteMessageConsumer(
            ProviderMessageConsumer communoteMessageConsumer) {
        this.communoteMessageConsumer = communoteMessageConsumer;
    }

    /**
     * Sets JMS message consumer.
     * 
     * @param jmsMessageConsumer
     *            consumer to be set
     */
    @Override
    public void setJmsMessageConsumer(MessageConsumer jmsMessageConsumer) {
        this.jmsMessageConsumer = jmsMessageConsumer;

    }

    /**
     * @param jmsSender
     *            the jmsSender to set
     */
    @Override
    public void setJmsSender(JMSMessageSender jmsSender) {
        this.jmsSender = jmsSender;
    }

    /**
     * Sets the message delivery mode.
     * 
     * @param messageDeliveryMode
     *            the messageDeliveryMode to set
     */
    @Override
    public void setMessageDeliveryMode(MessageDeliveryMode messageDeliveryMode) {
        this.messageDeliveryMode = messageDeliveryMode;
    }

    /**
     * only for test purposes.
     * 
     * @param pollingThread
     *            the new polling thread
     */
    @Override
    public void setPollingThread(Thread pollingThread) {
        this.pollingThread = pollingThread;
    }

    /**
     * format of userId is Subject name of the corresponding certificate, e.g.:
     * CN=sharepoint.system, OU=global.communote.user, O=Communote, L=Dresden, ST=Sachsen, C=DE
     * 
     * THe common name stands for internal communote alias: CN => alias <br>
     * and the organization unit for the client postfixed by communote.user: OU =>
     * <client>.communote.user <br>
     * All other (O, L, ST,C) is irrelevant
     * 
     * @param transferMessage
     *            the transfer message
     * @param userId
     *            so far only the certificate subject name is supported
     */
    private void setPreAuthenticationHeaders(TransferMessage transferMessage, String userId) {

        CommunoteUserCertificate cuc = null;
        try {
            cuc = new CommunoteUserCertificate(userId);
        } catch (IllegalArgumentException iae) {
            // assume that the userId is not from a certificate but a internal user
            LOGGER.debug("Ignoring provided user identification and will ignore pre authentication userId="
                    + userId);
        }

        if (cuc != null && cuc.containsValidCommunoteUser()) {

            FieldUserIdentification userIdentification = new FieldUserIdentification();
            userIdentification.setUserAlias(cuc.getCommunoteUserAlias());
            transferMessage.putHeader(
                    TransferMessage.HEADER_PRE_AUTHENTICATED_USER_IDENTIFICATION,
                    userIdentification);
            transferMessage.putHeader(
                    TransferMessage.HEADER_PRE_AUTHENTICATED_CLIENT_ID,
                    cuc.getClientId());
        }

    }

    /**
     * initializes the message consumer.
     * 
     * @throws JMSException
     *             the jMS exception
     */
    @Override
    public void start() throws JMSException {
        if (messageDeliveryMode == MessageDeliveryMode.PUSH) {
            this.jmsMessageConsumer
                    .setMessageListener(new JMSAdapterMessagesListener(this));
        } else {
            if (!pollingThread.isAlive()) {
                startMessagesPolling();
            }
        }
    }

    /**
     * Start messages polling.
     */
    private void startMessagesPolling() {
        pollingThread.start();
    }

    /**
     * stops JMS message consumer.
     */
    @Override
    public void stop() {
        if (pollingThread.isAlive()) {
            pollingThread.interrupt();
            try {
                /*
                 * wait until polling thread is stopped. It should happen quite fast if the thread
                 * sleeps. If it's processing some command, the message consumer might be safely
                 * stopped, the polling thread will be interrupted on the next iteration, and
                 * message consumer won't be used. Some time should be given for the polling thread
                 * in the case it is before the JMS message consumer usage
                 */
                pollingThread.join(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // stop JMS message consumer
        try {
            jmsMessageConsumer.close();
        } catch (JMSException e) {
            LOGGER.error("Exception during message consumer stopping."
                    + e.getMessage());
        }
    }

}
