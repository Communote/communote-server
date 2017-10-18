package com.communote.server.core.mail;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.config.type.ApplicationPropertyMailing;

/**
 * SMTP based MimeMessageSender which replaces the recipients with a configurable (catch-all) email
 * address before sending the message. The original recipients are saved in custom headers:
 * Communote-Original-To, Communote-Original-Bcc and Communote-Original-Cc
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 *
 */
public class CatchAllSmtpMimeMessageSender extends SmtpMimeMessageSender {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CatchAllSmtpMimeMessageSender.class);

    // not using X- based headers as proposed in rfc6648
    private static final String HEADER_ORIG_TO = "Communote-Original-To";
    private static final String HEADER_ORIG_BCC = "Communote-Original-Bcc";
    private static final String HEADER_ORIG_CC = "Communote-Original-Cc";

    private final InternetAddress catchAllAddress;

    public CatchAllSmtpMimeMessageSender(Map<ApplicationPropertyMailing, String> settings,
            String catchAllAddress) {
        super(settings);
        this.catchAllAddress = parseCatchAllAddress(catchAllAddress);
    }

    private void moveHeaderValues(MimeMessage message, String sourceHeaderName,
            String targetHeaderName) {
        try {
            String[] values = message.getHeader(sourceHeaderName);
            if (values != null) {
                for (String value : values) {
                    message.addHeader(targetHeaderName, value);
                }
                message.removeHeader(sourceHeaderName);
            }
        } catch (MessagingException e) {
            throw new MailingException("Moving values of header " + sourceHeaderName + " to header "
                    + targetHeaderName + " failed", e);
        }
    }

    private InternetAddress parseCatchAllAddress(String catchAllAddress) {
        InternetAddress[] parsedAddresses;
        try {
            parsedAddresses = InternetAddress.parse(catchAllAddress, true);
            if (parsedAddresses.length == 0) {
                throw new MailingException("No catch-all address provided");
            }
            return new InternetAddress(parsedAddresses[0].getAddress(),
                    parsedAddresses[0].getPersonal(), "UTF-8");
        } catch (AddressException e) {
            throw new MailingException("Parsing catch-all address '" + catchAllAddress + "' failed",
                    e);
        } catch (UnsupportedEncodingException e) {
            throw new MailingException("Encoding personal component of catch-all address '"
                    + catchAllAddress + "' failed", e);
        }
    }

    private void replaceRecipients(MimeMessage message) throws MailingException {
        // directly copy headers so we don't have to worry about encoding and folding
        moveHeaderValues(message, MimeMessage.RecipientType.TO.toString(), HEADER_ORIG_TO);
        moveHeaderValues(message, MimeMessage.RecipientType.CC.toString(), HEADER_ORIG_CC);
        moveHeaderValues(message, MimeMessage.RecipientType.BCC.toString(), HEADER_ORIG_BCC);
        try {
            message.setRecipient(MimeMessage.RecipientType.TO, catchAllAddress);
        } catch (MessagingException e) {
            throw new MailingException("Replacing recipient with catch-all address failed", e);
        }
    }

    @Override
    public void send(MimeMessage message) throws MailingException {
        replaceRecipients(message);
        super.send(message);
    }

    @Override
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings,
            MimeMessage testMessage) {
        if (testMessage != null) {
            try {
                replaceRecipients(testMessage);
            } catch (MailingException e) {
                LOGGER.error("Sending test message failed", e);
                return false;
            }
        }
        return super.testSettings(settings, testMessage);
    }
}
