package com.communote.plugins.mq.service.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.service.exception.MessageQueueCommunicationException;
import com.communote.plugins.mq.service.message.CommunoteMessageSender;
import com.communote.plugins.mq.service.provider.MessagesConverter;
import com.communote.plugins.mq.service.provider.ProviderMessageSender;
import com.communote.plugins.mq.service.provider.TransferMessage;

/**
 * implements CntMessageSender service.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component(immediate = true)
@Provides
@Instantiate
public class CommunoteMessageSenderImpl implements CommunoteMessageSender {

    /** The sender. */
    @Requires
    private ProviderMessageSender sender;

    /** The converter. */
    @Requires
    private MessagesConverter converter;

    /**
     * {@inheritDoc}
     * 
     * @throws MessageQueueCommunicationException
     */
    @Override
    public void sendMessage(BaseMessage message, String messageType, String version)
            throws MessageQueueCommunicationException {

        TransferMessage tm = converter.convertToTransferMessage(message);
        // Destination destination = provider.getDestination();

        // tm.putHeader(TransferMessage.HEADER_REPLY_QUEUE, destination);
        tm.putHeader(TransferMessage.HEADER_MESSAGE_TYPE, messageType);
        tm.putHeader(TransferMessage.HEADER_MESSAGE_VERSION, version);
        tm.putHeader(TransferMessage.HEADER_CONTENT_TYPE, converter.getContentType().toString());

        sender.sendMessageInternal(tm);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.service.message.CntMessageSender#sendReplyMessage
     * (com.communote.plugins.mq.service.message.ReplyMessage)
     */
    @Override
    public void sendReplyMessage(CommunoteReplyMessage message)
            throws MessageQueueCommunicationException {
        sender.sendReplyMessage(converter.convertToTransferMessage(message));
    }

    /**
     * Sets the converter.
     * 
     * @param converter
     *            the new converter
     */
    void setConverter(MessagesConverter converter) {
        this.converter = converter;
    }

    /**
     * Sets the sender.
     * 
     * @param sender
     *            the new sender
     */
    void setSender(ProviderMessageSender sender) {
        this.sender = sender;
    }

}
