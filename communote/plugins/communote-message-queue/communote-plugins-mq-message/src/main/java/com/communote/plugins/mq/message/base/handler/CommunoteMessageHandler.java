package com.communote.plugins.mq.message.base.handler;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;

/**
 * Communote Message Handler is intended for processing a specified subset of messages. The subset
 * is specified through handledMessageType selector. The messages passing this selector should
 * deliver content of the specified handledMessageClass
 * 
 * @param <T>
 *            type of messages, that are handled by this handler
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class CommunoteMessageHandler<T extends BaseMessage> {

    /**
     * Class of the messages to be handled by this handler.
     * 
     * @return class of the handled message
     */
    public abstract Class<T> getHandledMessageClass();

    /**
     * returns header selector, to be used for the message filtering.
     * 
     * @return selector
     */
    public String getHandledMessageType() {
        return getHandledMessageClass().getSimpleName();
    }

    /**
     * Handler version is used to distinguish between different formats of the semantically same
     * message type.
     * 
     * @return the version
     */
    public abstract String getVersion();

    /**
     * handles message of the appropriate type. If the processing was successful the reply message
     * should be returned. Otherwise an appropriate exception should be thrown
     * 
     * @param message
     *            the message
     * 
     * @return Reply message if the processing was successful
     * 
     * @throws Exception
     *             any occurred exception
     */
    public abstract CommunoteReplyMessage handleMessage(T message)
            throws Exception;

}
