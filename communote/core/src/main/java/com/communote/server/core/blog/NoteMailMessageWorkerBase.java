package com.communote.server.core.blog;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.mail.fetching.MailMessageWorker;


/**
 * Parent class for mail message workers that create posts from emails.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class NoteMailMessageWorkerBase extends MailMessageWorker {

    private static final Pattern SQUARE_BRACKET_PATTERN = Pattern
            .compile("\\[\\s*([\\w-]+[^\\]]*)\\]");

    /**
     * Returns the white-space trimmed content of the first square brackets pair found inside the
     * email subject which encapsulates some word characters.
     * 
     * @param message
     *            the email message whose subject will be examined
     * @return the content of the brackets
     * @throws MessagingException
     *             when a messaging exception occurred
     */
    protected String extractSquareBracketContentFromSubject(Message message)
            throws MessagingException {
        String contentFound = null;

        String subject = message.getSubject();
        if (subject != null) {
            subject = subject.toLowerCase(Locale.ENGLISH);
            Matcher matcher = SQUARE_BRACKET_PATTERN.matcher(subject);
            if (matcher.find()) {
                contentFound = StringUtils.strip(matcher.group(1));
            }
        }
        return contentFound;
    }

    /**
     * Extracts the email address of the sender. In case more than one FROM fields are set, the
     * first InternetAddress is taken.
     * 
     * @param message
     *            the message to examine
     * @return the email address
     * @throws MessagingException
     *             in case of an error
     */
    protected String getMailAddressOfSender(Message message) throws MessagingException {
        Address[] senders = message.getFrom();
        if (senders != null) {
            for (Address a : senders) {
                if (a instanceof InternetAddress) {
                    return ((InternetAddress) a).getAddress();
                }
            }
        }
        return null;
    }
}
