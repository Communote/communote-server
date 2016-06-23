package com.communote.plugins.mq.message.base.message;

/**
 * Type of the reply, expected after the message processing
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum ReplyType {

    /**
     * no reply is expected
     */
    NONE,

    /**
     * error status reply, if any, no answer otherwise
     */
    STATUS_ERRORS_ONLY,

    /**
     * reply with the processing status, always
     */
    STATUS_ALWAYS,

    /**
     * reply, containing results of execution, is message context dependent
     */
    FULL
}
