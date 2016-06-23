package com.communote.plugins.mq.message.core.util;

import java.util.HashSet;
import java.util.Set;

import com.communote.server.model.i18n.Message;

/**
 * Wrapper that unifies access to tag members of Communote tags and MQ tag POJOs
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagMemberAccessor {

    private com.communote.plugins.mq.message.core.data.tag.Tag messageQueueTag;

    private com.communote.server.model.tag.Tag communoteCoreTag;

    /**
     * Constructor.
     *
     * @param mqTag
     *            The message queue tag.
     */
    public TagMemberAccessor(com.communote.plugins.mq.message.core.data.tag.Tag mqTag) {
        this.messageQueueTag = mqTag;
    }

    /**
     * Constructor.
     *
     * @param cntCoreTag
     *            The Communote tag.
     */
    public TagMemberAccessor(com.communote.server.model.tag.Tag cntCoreTag) {
        this.communoteCoreTag = cntCoreTag;
    }

    /**
     * Convert to message core api.
     *
     * @param messageStr
     *            the message
     * @return the core message
     */
    private Message convertToCntCoreMessage(String messageStr) {
        Message message = Message.Factory.newInstance();
        message.setMessage(messageStr);
        return message;
    }

    /**
     * @return default name
     */
    public String getDefaultName() {
        if (messageQueueTag != null) {
            return messageQueueTag.getDefaultName();
        } else {
            return communoteCoreTag.getDefaultName();
        }
    }

    /**
     * @return description
     */
    public Set<Message> getDescriptions() {
        if (messageQueueTag != null) {
            Message message = convertToCntCoreMessage(messageQueueTag.getDescription());
            Set<Message> res = new HashSet<Message>();
            res.add(message);
            return res;

        } else {
            return communoteCoreTag.getDescriptions();
        }
    }

    /**
     * @return tag id
     */
    public Long getId() {
        if (messageQueueTag != null) {
            return messageQueueTag.getId();
        } else {
            return communoteCoreTag.getId();
        }
    }

    /**
     * @return names
     */
    public Set<Message> getNames() {
        if (messageQueueTag != null) {
            Message message = convertToCntCoreMessage(messageQueueTag.getName());
            Set<Message> res = new HashSet<Message>();
            res.add(message);
            return res;
        } else {
            return communoteCoreTag.getNames();
        }
    }

    /**
     * @return tag store alias
     */
    public String getTagStoreAlias() {
        if (messageQueueTag != null) {
            return messageQueueTag.getTagStoreAlias();
        } else {
            return communoteCoreTag.getTagStoreAlias();
        }
    }

    /**
     * @return tag store tag id
     */
    public String getTagStoreTagId() {
        if (messageQueueTag != null) {
            return messageQueueTag.getTagStoreTagId();
        } else {
            return communoteCoreTag.getTagStoreTagId();
        }
    }

}
