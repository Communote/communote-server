package com.communote.plugins.mq.adapter.activemq.configuration;

/**
 * This configuration refers to an embedded broker. It provides the broker url and the
 * identification to be used as username and password.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ActiveMQConfiguration {

    /**
     * Default queue name for communote messages to process
     */
    public final static String COMMUNOTE_QUEUE_DEFAULT = "COMMUNOTE.QUEUE.DEFAULT";

    /**
     * Default queue name for communote messages to reply to
     */
    public final static String COMMUNOTE_QUEUE_DEFAULT_REPLY = "COMMUNOTE.QUEUE.DEFAULT.REPLY";

    /**
     * 
     * @return the broker url to connect to
     */
    public String getBrokerUrl();

    /**
     * 
     * @return the identification token that should be used as password
     */
    public String getPassword();

    /**
     * 
     * @return the username for the embedded connection
     */
    public String getUsername();

    /**
     * 
     * @return true if the configuration is active and hence a connection is useful
     */
    public boolean isActive();

}
