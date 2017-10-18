package com.communote.server.core.mail.messages;

import java.util.Locale;
import java.util.Map;


/**
 * Mail message, which allows to set the subject and content of the mail directly.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TextMailMessage extends MailMessage {

    private final String content;
    private final String subject;

    /**
     * @param subject
     *            The subject.
     * @param content
     *            The content.
     * @param to
     *            Email address of the recipient.
     */
    public TextMailMessage(String subject, String content, String to) {
        this(subject, content, to, null, null);
    }

    /**
     * @param subject
     *            The subject.
     * @param content
     *            The content.
     * @param to
     *            Email address of the recipient.
     * @param fromEmailAddress
     *            The senders email address. Might be null.
     * @param fromName
     *            The senders name. Might be null.
     */
    public TextMailMessage(String subject, String content, String to, String fromEmailAddress,
            String fromName) {
        super("mail.message.generic-template", Locale.ENGLISH);
        this.content = content;
        this.subject = subject;
        this.addTo(to);
        setFromAddress(fromEmailAddress);
        setFromAddressName(fromName);
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
        model.put("content", content);
        model.put("subject", subject);
    }

}
