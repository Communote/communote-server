package com.communote.server.core.mail;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.user.User;

/**
 * Preparator which interprets the content and subject templates of a MailMessage as Velocity
 * templates.
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 *
 */
public class VelocityMimeMessagePreparator implements MimeMessagePreparator {

    private static final String ENCODING = "UTF-8";
    private static final String HEADER_MESSAGE_ID = "Message-ID";

    private final MailMessage mailMessage;
    private VelocityEngine velocityEngine;

    /**
     * @param mailMessage
     *            the mail message to convert into a prepared mime message
     * @param velocityEngine
     *            the Velocity engine to render subject and content from the templates
     */
    public VelocityMimeMessagePreparator(MailMessage mailMessage, VelocityEngine velocityEngine) {
        this.mailMessage = mailMessage;
        this.velocityEngine = velocityEngine;
    }

    private void addTo(MimeMessageHelper message, String emailAddress, String personalName)
            throws MessagingException, UnsupportedEncodingException {
        if (personalName == null) {
            message.addTo(emailAddress);
        } else {
            message.addTo(emailAddress, personalName);
        }
    }

    @Override
    public void prepare(MimeMessage mimeMessage)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, ENCODING);
        // set some headers to avoid automatic OutOfOffice replies
        mimeMessage.setHeader("Precedence", "list");
        mimeMessage.setHeader("X-Auto-Response-Suppress", "OOF");
        String identifier = mailMessage.getMessageIdentifier();
        if (identifier != null) {
            mimeMessage.setHeader(HEADER_MESSAGE_ID,
                    MailMessageHelper.createMessageIdHeaderValue(identifier));
        }
        // set before as default, the velocity engine may overwrite this
        message.setFrom(mailMessage.getFromAddress(), mailMessage.getFromAddressName());
        message.setSentDate(new Date());

        String replyTo = mailMessage.getReplyToAddress();
        String replyToName = mailMessage.getReplyToAddressName();
        if (!StringUtils.isBlank(replyTo)) {
            message.setReplyTo(replyTo, replyToName);
        }

        // get global model
        Map<String, Object> model = mailMessage.getGlobalModel();
        mailMessage.prepareModel(model);
        model.put(MailModelPlaceholderConstants.MESSAGE, message);

        String text = renderVelocityTemplate(mailMessage.getContentTemplateId(),
                mailMessage.getContentTemplate(), model);
        message.setText(text, mailMessage.isHtmlMail());
        String subject = renderVelocityTemplate(mailMessage.getSubjectTemplateId(),
                mailMessage.getSubjectTemplate(), model);
        message.setSubject(subject);
        setRecipients(message);
    }

    private String renderVelocityTemplate(String templateId, String template,
            Map<String, Object> model) throws MessagingException {
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext(model);
        try {
            velocityEngine.evaluate(context, writer, templateId, template);
            return writer.toString().trim();
        } catch (ParseErrorException | MethodInvocationException | ResourceNotFoundException e) {
            throw new MessagingException(
                    "Rendering mail message data from template with ID " + templateId + " failed",
                    e);
        }
    }

    private void setRecipients(MimeMessageHelper message)
            throws MessagingException, UnsupportedEncodingException {
        boolean insertPersonalName = mailMessage.isInsertRecipientPersonalName();
        Collection<User> users = mailMessage.getTo();
        for (User user : users) {
            String personalName = null;
            if (insertPersonalName) {
                personalName = UserNameHelper.getCompleteSignature(user);
            }
            addTo(message, user.getEmail(), personalName);
        }
        Collection<String> toAddresses = mailMessage.getToAddresses();
        for (String address : toAddresses) {
            String personalName = null;
            if (insertPersonalName) {
                personalName = mailMessage.getToAddressPersonalName(address);
            }
            addTo(message, address, personalName);
        }
        users = mailMessage.getCc();
        for (User user : users) {
            String personalName = null;
            if (insertPersonalName) {
                personalName = UserNameHelper.getCompleteSignature(user);
            }
            if (personalName == null) {
                message.addCc(user.getEmail());
            } else {
                message.addCc(user.getEmail(), personalName);
            }
        }
        users = mailMessage.getBcc();
        for (User user : users) {
            String personalName = null;
            if (insertPersonalName) {
                personalName = UserNameHelper.getCompleteSignature(user);
            }
            if (personalName == null) {
                message.addBcc(user.getEmail());
            } else {
                message.addBcc(user.getEmail(), personalName);
            }
        }
    }

}
