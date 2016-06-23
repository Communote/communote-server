package com.communote.server.core.mail;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * MimeMessage implementation that does not override the Message-ID header.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MessageIdPreservingMimeMessage extends MimeMessage {

    /**
     * Creates a new instance.
     * 
     * @param session
     *            the session to use
     */
    public MessageIdPreservingMimeMessage(Session session) {
        super(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateMessageID() throws MessagingException {
        String messageId = getMessageID();
        if (messageId == null) {
            super.updateMessageID();
        }
    }
}
