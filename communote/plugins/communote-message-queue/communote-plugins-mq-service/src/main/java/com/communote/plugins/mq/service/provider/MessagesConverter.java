package com.communote.plugins.mq.service.provider;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.service.exception.MessageParsingException;
import com.communote.plugins.mq.service.provider.TransferMessage.TMContentType;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface MessagesConverter {

    /**
     * Convert to cnt message.
     * 
     * @param <T>
     *            the generic type
     * @param messageToBeConverted
     *            transfer message to be converted
     * @param messageClass
     *            class of the CNT message
     * @throws MessageParsingException
     *             in case something went wrong parsing the message
     * @return The converted CNT message, never can be null
     */
    public <T extends BaseMessage> T convertToCommunoteMessage(
            TransferMessage messageToBeConverted, Class<T> messageClass)
            throws MessageParsingException;

    /**
     * Convert to transfer message.
     * 
     * @param <T>
     *            the generic type
     * @param messageToBeConverted
     *            message to be converted
     * @return transfer message
     */
    public <T extends BaseMessage> TransferMessage convertToTransferMessage(
            T messageToBeConverted);

    public TMContentType getContentType();
}