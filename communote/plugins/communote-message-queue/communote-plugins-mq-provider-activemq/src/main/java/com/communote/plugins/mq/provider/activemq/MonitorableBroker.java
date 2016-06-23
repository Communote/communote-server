package com.communote.plugins.mq.provider.activemq;

import java.util.Set;

import com.communote.plugins.mq.provider.activemq.monitor.data.BrokerQueue;
import com.communote.plugins.mq.provider.activemq.monitor.data.MessageHandlerMQConsumer;

/**
 * Monitorable broker interface
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface MonitorableBroker {

    /**
     * @return broker name
     * @throws MessageQueueJmxException
     */
    String getBrokerName() throws MessageQueueJmxException;

    /**
     * @return existing message queues
     * @throws MessageQueueJmxException
     */
    Set<BrokerQueue> getBrokerQueues() throws MessageQueueJmxException;

    /**
     * @return consumers, which are used to deliver message to an appropriate message handler
     * @throws MessageQueueJmxException
     */
    MessageHandlerMQConsumer[] getMessageHandlerConsumers() throws MessageQueueJmxException;

    /**
     * @return broker status
     */
    boolean isActive();

}
