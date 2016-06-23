package com.communote.plugins.mq.service.provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Uniform representation of the message notion, to be used independently on any MQ implementation.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TransferMessage {

    /**
     * Supported Transfer Message content types.
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    public enum TMContentType {
        /** The JSON. */
        JSON,
        /** The XML. */
        XML
    }

    /** The Constant HEADER_CONTENT_TYPE. */
    public static final String HEADER_CONTENT_TYPE = "CONTENT_TYPE";

    /** The Constant HEADER_MESSAGE_TYPE. */
    public static final String HEADER_MESSAGE_TYPE = "MESSAGE_TYPE";

    /** The Constant HEADER_MESSAGE_VERSION. */
    public static final String HEADER_MESSAGE_VERSION = "MESSAGE_VERSION";

    /** The Constant HEADER_CORRELATION_ID. */
    public static final String HEADER_CORRELATION_ID = "CORRELATION_ID";

    /** The Constant HEADER_REPLY_QUEUE_ID. */
    public static final String HEADER_REPLY_QUEUE = "REPLY_QUEUE";

    /**
     * Boolean property on a JMS message, indicating that the user can be trusted and no password
     * authentication within communote must be done. This is the case if the connection is using an
     * embedded non network connection (e.g. vm protocol in activemq)
     */
    public static final String HEADER_TRUST_USER = "TRUST_USER";

    /**
     * Header constant holding a user identification of a pre authenticated user. pre authentication
     * means that the user has been authenticated through the message broker.
     */
    public static final String HEADER_PRE_AUTHENTICATED_USER_IDENTIFICATION = "PRE_AUTHENTICATED_USER_IDENTIFICATION";

    /**
     * Header constant corresponding to HEADER_PRE_AUTHENTICATED_USER_IDENTIFICATION. The value
     * holds the client id the identification is valid for.
     */
    public static final String HEADER_PRE_AUTHENTICATED_CLIENT_ID = "PRE_AUTHENTICATED_CLIENT_ID";

    /** The headers. */
    private final Map<String, Object> headers = new HashMap<String, Object>();

    /** The content. */
    private String content;

    /** The content type. */
    private TMContentType contentType;

    /**
     * returns message content.
     *
     * @return message content
     */
    public String getContent() {
        return content;
    }

    /**
     * returns message's content type.
     *
     * @return message content
     */
    public TMContentType getContentType() {
        return contentType;
    }

    /**
     * Gets the header.
     *
     * @param key
     *            header key
     * @return returns specified header or null if not found
     */
    public Object getHeader(String key) {
        return this.headers.get(key);
    }

    /**
     * Put header.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void putHeader(String key, Object value) {
        this.headers.put(key, value);
    }

    /**
     * Sets the content.
     *
     * @param content
     *            the new content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the content type.
     *
     * @param contentType
     *            the new content type
     */
    public void setContentType(TMContentType contentType) {
        this.contentType = contentType;
    }

}
