package com.communote.server.core.mail.messages;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Mail message, which allows to set the subject and content of the mail directly.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TextMailMessage extends MailMessage {

    private final String content;
    private final String subject;
    private final String to;
    private final String fromEmailAddress;
    private final String fromName;

    /**
     * @param subject
     *            The subject.
     * @param content
     *            The content.
     * @param to
     *            Email address of the receiver.
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
     *            Email address of the receiver.
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
        this.to = to;
        this.fromEmailAddress = fromEmailAddress;
        this.fromName = fromName;
    }

    /**
     * @return Value of {@link #fromEmailAddress} if not null else
     *         {@link MailMessage#getFromAddress()}
     */
    @Override
    public String getFromAddress() {
        return fromEmailAddress != null ? fromEmailAddress : super.getFromAddress();
    }

    /**
     * @return Value of {@link #fromName} if not null else {@link MailMessage#getFromAddressName()}
     */
    @Override
    public String getFromAddressName() {
        return fromName != null ? fromName : super.getFromAddressName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put("content", content);
        model.put("subject", subject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReceivers(MimeMessageHelper message) throws MessagingException,
            UnsupportedEncodingException {
        message.addTo(to);
    }
}
