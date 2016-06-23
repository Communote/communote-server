package com.communote.plugins.mq.test;

import java.io.File;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.log4j.BasicConfigurator;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.base.message.ReplyType;
import com.communote.plugins.mq.service.provider.TransferMessage;
import com.communote.plugins.mq.service.provider.TransferMessage.TMContentType;
import com.communote.plugins.mq.test.util.MQMessageTestUtils;

/**
 * Abstract class with base functionality for message queue test. The main setup is done in
 * {@link #setupMessageQueue(String)} which is evaluating the {@link #testMode} set.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class MessageQueueTest {

    /**
     * The enum for the test mode
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     * 
     */
    private enum MessageQueueTestMode {
        /**
         * Do unsecure tcp using username and password
         */
        TCP,
        /**
         * Do ssl using username and password
         */
        SSL,
        /**
         * Do ssl using client certificate
         */
        SSLCERT;
    }

    /**
     * message listener
     */
    private static class WaitForAnswerMessageListener implements MessageListener {

        private boolean gotAnswer = false;
        private final ObjectMapper mapper = new ObjectMapper();
        private String content;

        /**
         * Returns the transformed result.
         * 
         * @param clazz
         *            The class specifying the results type.
         * @param <T>
         *            The type.
         * @return The result.
         */
        public <T> T getResult(Class<T> clazz) {
            LOGGER.debug("Received following JSON result for class "
                    + clazz.getSimpleName() + "\n" + content);
            try {
                return mapper.readValue(content, clazz);
            } catch (Exception e) {
                throw new AssertionError("JSON deserialization failed: " + e.getMessage()
                        + " content=" + content + " clazz=" + clazz.getSimpleName());
            }
        }

        /**
         * @param message
         *            message
         */
        @Override
        public void onMessage(Message message) {
            if (message instanceof ActiveMQTextMessage) {
                try {
                    content = ((ActiveMQTextMessage) message).getText();
                } catch (JMSException e) {
                    Assert.fail(e.getMessage());
                }
            }
            gotAnswer = true;
        }
    }

    private static final String COMMUNOTE_QUEUE_DEFAULT = "COMMUNOTE.QUEUE.DEFAULT";

    private static final String COMMUNOTE_QUEUE_NAME_REPLY = COMMUNOTE_QUEUE_DEFAULT + ".REPLY";

    private String communoteManagerAlias;

    private String communoteUserAlias;
    private final ObjectMapper mapper = new ObjectMapper();

    {
        BasicConfigurator.configure();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                JsonTypeInfo.As.PROPERTY);
        mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion(
                Inclusion.NON_NULL));
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(MessageQueueTest.class);

    private Connection connection;

    private MQMessageTestUtils messageQueueTestUtils;

    private String messageQueueLogin;

    private String messageQueuePassword;

    private String cntAuthenticationUsername;

    private String cntAuthenticationPassword;

    private String url;

    private String sslUrl;

    private Long communoteManagerId;

    private String externalGroupId;

    private MessageQueueTestMode testMode;

    /**
     * Method to setup the message queue.
     * 
     * @param url
     *            Url the connector should connect to.
     * @param messageQueueLogin
     *            Login of the MQ user.
     * @param messageQueuePassword
     *            Password of the MQ user.
     * @throws Exception
     *             exception.
     */
    private void createConnection(String url, String messageQueueLogin,
            String messageQueuePassword) throws Exception {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
        factory.setClientID(new Date() + "");

        if (messageQueueLogin != null && messageQueueLogin.trim().length() > 0
                && messageQueuePassword != null && messageQueuePassword.trim().length() > 0) {
            connection = factory.createConnection(messageQueueLogin, messageQueuePassword);
        } else {
            connection = factory.createConnection();
        }
        connection.start();
    }

    /**
     * 
     * @param reflectMode
     *            true if the {@link #testMode} should be reflected. That means if ssl client
     *            certificates should be used no password is needed and hence this method will
     *            return an empty string if the mode is SSLCERT
     * @return the password to be used in the authentication
     */
    public String getCntAuthenticationPassword(boolean reflectMode) {
        if (MessageQueueTestMode.SSLCERT.equals(testMode)) {
            return "";
        }
        return cntAuthenticationPassword;

    }

    /**
     * 
     * @param reflectMode
     *            true if the {@link #testMode} should be reflected. That means if ssl client
     *            certificates should be used no username is needed and hence this method will
     *            return an empty string if the mode is SSLCERT
     * @return the username to be used in the authentication
     */
    public String getCntAuthenticationUsername(boolean reflectMode) {
        if (MessageQueueTestMode.SSLCERT.equals(testMode)) {
            return "";
        }
        return cntAuthenticationUsername;
    }

    /**
     * @return the communoteManagerAlias
     */
    public String getCommunoteManagerAlias() {
        return communoteManagerAlias;
    }

    /**
     * @return the communoteManagerId
     */
    public Long getCommunoteManagerId() {
        return communoteManagerId;
    }

    /**
     * @return the communoteUserAlias
     */
    public String getCommunoteUserAlias() {
        return communoteUserAlias;
    }

    /**
     * @return the configured ID of a group in the external system
     */
    public String getExternalGroupId() {
        return externalGroupId;
    }

    /**
     * 
     * @return the object mapper
     */
    public ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * 
     * @return the utils to use
     */
    public MQMessageTestUtils getMessageQueueTestUtils() {
        return messageQueueTestUtils;
    }

    /**
     * 
     * @return the test mode to use
     */
    public MessageQueueTestMode getTestMode() {
        return testMode;
    }

    /**
     * Method to send a message to the queue.
     * 
     * @param baseMessage
     *            The message to send.
     * @param clazz
     *            Class representing the result type.
     * @param <T>
     *            Type of the result.
     * @return The answer.
     * @throws Exception
     *             Exception.
     */
    public <T> T sendMessage(BaseMessage baseMessage, Class<T> clazz) throws Exception {
        baseMessage.setReplyType(ReplyType.FULL);
        return sendMessage(mapper.writeValueAsString(baseMessage), baseMessage.getClass(), clazz);
    }

    /**
     * Method to send a message to the queue.
     * 
     * @param messageAsJSON
     *            The message to send as json.
     * @param classOfMessage
     *            The class of the message.
     * @param clazz
     *            Class representing the result type.
     * @param <T>
     *            Type of the result.
     * @return The answer.
     * @throws Exception
     *             Exception.
     */
    public <T> T sendMessage(String messageAsJSON, Class<?> classOfMessage, Class<T> clazz)
            throws Exception {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(COMMUNOTE_QUEUE_DEFAULT);
        MessageProducer producer = session.createProducer(destination);
        LOGGER.info("Going to send following JSON for message class "
                + classOfMessage.getSimpleName() + "\n" + messageAsJSON);
        Message message = session.createTextMessage(messageAsJSON);

        // create a new queue based on the message content
        Queue replyQueue = session.createQueue(COMMUNOTE_QUEUE_NAME_REPLY + "."
                + messageAsJSON.hashCode());
        message.setJMSReplyTo(replyQueue);
        message.setStringProperty("MESSAGE_TYPE", classOfMessage.getSimpleName());
        message.setStringProperty("MESSAGE_VERSION", "1.0.0");
        message.setStringProperty(TransferMessage.HEADER_CONTENT_TYPE,
                TMContentType.JSON.toString());
        MessageConsumer consumer = session.createConsumer(replyQueue);
        WaitForAnswerMessageListener waitForAnswerMessageListener = new WaitForAnswerMessageListener();
        consumer.setMessageListener(waitForAnswerMessageListener);
        producer.send(message);
        int i = 0;
        while (!waitForAnswerMessageListener.gotAnswer) {
            Thread.sleep(50);
            i++;
            if (i > 200) {
                Assert.fail("Receiving an answer took to long: " + (i - 1) * 50);
            }
        }
        consumer.close();
        producer.close();
        session.close();
        return waitForAnswerMessageListener.getResult(clazz);
    }

    /**
     * 
     * @param cntAuthenticationPassword
     *            set the password
     */
    public void setCntAuthenticationPassword(String cntAuthenticationPassword) {
        this.cntAuthenticationPassword = cntAuthenticationPassword;
    }

    /**
     * 
     * @param cntAuthenticationUsername
     *            set the authentication username
     */
    public void setCntAuthenticationUsername(String cntAuthenticationUsername) {
        this.cntAuthenticationUsername = cntAuthenticationUsername;
    }

    /**
     * @param communoteManagerAlias
     *            the communoteManagerAlias to set
     */
    public void setCommunoteManagerAlias(String communoteManagerAlias) {
        this.communoteManagerAlias = communoteManagerAlias;
    }

    /**
     * @param communoteManagerId
     *            the communoteManagerId to set
     */
    public void setCommunoteManagerId(Long communoteManagerId) {
        this.communoteManagerId = communoteManagerId;
    }

    /**
     * @param communoteUserAlias
     *            the communoteUserAlias to set
     */
    public void setCommunoteUserAlias(String communoteUserAlias) {
        this.communoteUserAlias = communoteUserAlias;
    }

    /**
     * Setup the communote parameters
     * 
     * @param cntAuthenticationUsername
     *            the username for authentication (within the CNT message)
     * @param cntAuthenticationPassword
     *            the password for authentication (within the CNT message)
     * @param communoteManagerAlias
     *            the manager alias for the test cases
     * @param communoteUserAlias
     *            the user alias for the test cases
     * @param communoteManagerId
     *            the id of the manager for the test cases
     * @param externalGroupId
     *            the ID of a group in the external system
     */
    @BeforeClass
    @Parameters({ "cntAuthenticationUsername", "cntAuthenticationPassword", "cntManagerAlias",
            "cntUserAlias", "cntManagerId", "externalGroupId" })
    public void setupCommunoteParameters(
            @Optional("sharepoint.system") String cntAuthenticationUsername,
            @Optional("123456") String cntAuthenticationPassword,
            @Optional("kenmei") String communoteManagerAlias,
            @Optional("kenmei") String communoteUserAlias, @Optional String communoteManagerId,
            @Optional("mqTestExternalGroup") String externalGroupId) {

        this.cntAuthenticationUsername = cntAuthenticationUsername;
        this.cntAuthenticationPassword = cntAuthenticationPassword;
        this.setCommunoteManagerAlias(communoteManagerAlias);
        this.setCommunoteUserAlias(communoteUserAlias);
        if (communoteManagerId == null || communoteManagerId.length() == 0) {
            this.setCommunoteManagerId(1L);
        } else {
            this.setCommunoteManagerId(Long.parseLong(communoteManagerId));
        }
        this.externalGroupId = externalGroupId;
    }

    /**
     * Setup the message queue, that is obtaining a nice connection and setup the
     * {@link #messageQueueTestUtils} depending on the testmode
     * 
     * @param mode
     *            the mode
     * @throws Exception
     *             in case something went wrong
     */
    @BeforeClass(dependsOnMethods = { "setupCommunoteParameters", "setupMessageQueueParameters" })
    @Parameters({ "mode" })
    public void setupMessageQueue(@Optional("TCP") String mode)
            throws Exception {
        testMode = MessageQueueTestMode.valueOf(mode.toUpperCase());

        switch (testMode) {
        case TCP:
            // use username and password
            messageQueueTestUtils = new MQMessageTestUtils(this.cntAuthenticationUsername,
                    this.cntAuthenticationPassword);
            // use non ssl url
            createConnection(url, messageQueueLogin, messageQueuePassword);
            break;
        case SSL:
            // setup the truststore (but not the keystore)
            setupSSL(true);
            // use username and password
            messageQueueTestUtils = new MQMessageTestUtils(this.cntAuthenticationUsername,
                    this.cntAuthenticationPassword);
            // use ssl url
            createConnection(sslUrl, messageQueueLogin, messageQueuePassword);
            break;
        case SSLCERT:
            // setup the truststore and the keystore
            setupSSL(false);
            // do NOT use username and password
            messageQueueTestUtils = new MQMessageTestUtils();
            // use the ssl url
            createConnection(sslUrl, null, null);
            break;

        }
    }

    /**
     * Setup the message queue parameters
     * 
     * @param url
     *            the tcp url
     * @param sslUrl
     *            the ssl url
     * @param messageQueueLogin
     *            the login for the mq
     * @param messageQueuePassword
     *            the password for the mq
     */
    @BeforeClass
    @Parameters({ "url", "sslUrl", "mqLogin", "mqPassword" })
    public void setupMessageQueueParameters(@Optional("tcp://localhost:61616") String url,
            @Optional("ssl://localhost:61616") String sslUrl,
            @Optional("kenmei") String messageQueueLogin,
            @Optional("123456") String messageQueuePassword) {
        this.url = url;
        this.sslUrl = sslUrl;
        this.messageQueueLogin = messageQueueLogin;
        this.messageQueuePassword = messageQueuePassword;

    }

    /**
     * Setup the trust and keystore paths
     * 
     * @param onlySetupTrustStore
     *            true if only the truststore is set
     */
    private void setupSSL(boolean onlySetupTrustStore) {

        String basePath = new File("src/test/resources/certs").getAbsolutePath()
                + File.separator;
        LOGGER.info("Using key and truststore in folder: {}", basePath);

        if (!onlySetupTrustStore) {
            System.setProperty("javax.net.ssl.keyStore",
                    basePath + "client.ks");
            System.setProperty("javax.net.ssl.keyStorePassword", "client");
        }
        System.setProperty("javax.net.ssl.trustStore",
                basePath + "client.ts");
        System.setProperty("javax.net.ssl.trustStorePassword", "client");

    }

    /**
     * Close the connection
     * 
     * @throws JMSException
     *             in case of an error
     */
    @AfterClass
    public void stop() throws JMSException {
        if (this.connection != null) {
            this.connection.close();
        }

    }

}
