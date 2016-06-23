package com.communote.plugins.mq.service.provider;

import org.apache.felix.ipojo.annotations.Unbind;

import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.message.BaseMessage;

/**
 * 
 * Factory of the CNT message consumers.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ProviderMessageConsumerFactory {

    /**
     * Is invoked when new message handler is registered in the OSGi context. Instantiates
     * appropriate provider message consumer
     * 
     * @param messageHandler
     *            handler instance
     */
    public void bindMessageHandler(CommunoteMessageHandler<? extends BaseMessage> messageHandler);

    /**
     * 
     * Is invoked when a communote message handler is removed from the OSGi context. The method is
     * responsible for stopping appropriate provider message consumer instance
     * 
     * @param messageHandler
     *            message handler, that was removed
     */
    @Unbind
    public void unbindMessageHandler(
            CommunoteMessageHandler<? extends BaseMessage> messageHandler);
}
