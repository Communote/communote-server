package com.communote.plugins.mq.message.base.message;

import com.communote.plugins.mq.message.base.data.security.Authentication;
import com.communote.plugins.mq.message.base.data.security.IdentityContext;

/**
 * Base class for the messages in the context of Communote.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class BaseMessage {

    private String creationDate;

    private String messageId;

    private String clientAlias;

    private Authentication authentication;

    /**
     * The message processing context. Represents the execution context of a particular user in a
     * client system (in communote)
     */
    private IdentityContext identityContext;

    private ReplyType replyType;

    /**
     * External system identifier (e.g. "DefaultLDAP", "DefaultSharePoint", "DefaultConfluence")
     */
    private String externalSystemId;

    /**
     * @return the authentication
     */
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * @return the clientAlias
     */
    public String getClientAlias() {
        return clientAlias;
    }

    /**
     * @return the creationDate
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * @return the externalSystemId
     */
    public String getExternalSystemId() {
        return externalSystemId;
    }

    /**
     * Gets the cnt user alias.
     * 
     * @return the cntUserId
     */
    public IdentityContext getIdentityContext() {
        return identityContext;
    }

    /**
     * @return the messageId
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @return the replyType
     */
    public ReplyType getReplyType() {
        return replyType;
    }

    /**
     * @param authentication
     *            the authentication to set
     */
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    /**
     * @param clientAlias
     *            the clientAlias to set
     */
    public void setClientAlias(String clientAlias) {
        this.clientAlias = clientAlias;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @param externalSystemId
     *            the externalSystemId to set
     */
    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    /**
     * Sets the cnt user alias.
     * 
     * @param identityContext
     *            Identity context.
     */
    public void setIdentityContext(IdentityContext identityContext) {
        this.identityContext = identityContext;
    }

    /**
     * @param messageId
     *            the messageId to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * @param replyType
     *            the replyType to set
     */
    public void setReplyType(ReplyType replyType) {
        this.replyType = replyType;
    }

}
