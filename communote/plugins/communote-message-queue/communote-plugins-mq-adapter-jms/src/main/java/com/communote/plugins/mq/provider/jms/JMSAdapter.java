package com.communote.plugins.mq.provider.jms;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

import com.communote.plugins.mq.service.provider.TransferMessage;

/**
 * Provides service interface for the JMS compatible adapters. To be implemented by external JMS
 * adapters
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface JMSAdapter {

    /**
     * Boolean property on a JMS message, indicating that the user can be trusted and no password
     * authentication within communote must be done. This is the case if the connection is using an
     * embedded non network connection (e.g. vm protocol in activemq)
     */
    public final static String MESSAGE_PROPERTY_TRUST_USER = TransferMessage.HEADER_TRUST_USER;

    /**
     * 
     * @return destination
     * @throws JMSException
     *             exception
     */
    Queue getDestination() throws JMSException;

    /**
     * returns a <a herf="http://docs.oracle.com/javaee/5/api/javax/jms/Session.html" >Session</a>
     * object
     * 
     * @return created session instance
     * @throws JMSException
     *             exception
     */
    Session getSession() throws JMSException;

}
