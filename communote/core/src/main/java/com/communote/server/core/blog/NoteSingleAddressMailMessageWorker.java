/**
 *
 */
package com.communote.server.core.blog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.validation.EmailValidator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.fetching.MailFetcher;
import com.communote.server.core.mail.messages.fetching.BlogNameIdMissingInSubjectMailMessage;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * This mail message worker creates posts from messages sent to one email address. The blog id is
 * searched within the subject. As client only the global client is supported.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteSingleAddressMailMessageWorker extends NoteMailMessageWorkerBase {

    /**
     * the log.
     */
    private final static Logger LOG = LoggerFactory
            .getLogger(NoteSingleAddressMailMessageWorker.class);

    private final String singleMailAddress;
    private final boolean dropMessages;

    /**
     * Initiates the worker.
     */
    public NoteSingleAddressMailMessageWorker() {
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties();
        singleMailAddress = props.getProperty(ApplicationPropertyMailfetching.SINGLE_ADDRESS);
        dropMessages = props.getProperty(
                ApplicationPropertyMailfetching.DELETE_MAILS_NOT_FOR_THIS_INSTANCE, false);
    }

    /**
     * Searches for a valid topic alias inside a string. Takes the first found.
     *
     * @param content
     *            the string to search
     * @return the topic alias or null if not found
     * @throws MessagingException
     *             when a messaging exception occurred
     */
    private String extractFirstValidBlogName(String content) throws MessagingException {
        if (content == null || content.length() == 0) {
            return null;
        }
        Pattern pattern = Pattern.compile(BlogManagementHelper.REG_EXP_TOPIC_NAME_IDENTIFIER);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (isValidBlogNameId(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleProcessMailMessage(Message message) {
        String clientId = ClientHelper.getGlobalClientId();
        boolean dropMessage = false;
        try {
            if (hasLegalRecipient(message)) {
                String senderEmail = getMailAddressOfSender(message);
                String blogAlias = null;
                if (StringUtils.isNotBlank(message.getSubject())) {
                    String bracketContent = extractSquareBracketContentFromSubject(message);
                    blogAlias = extractFirstValidBlogName(bracketContent);
                    // get blogname id; first search in square brackets, as fallback take last word
                    // of
                    // subject
                    if (blogAlias == null) {
                        blogAlias = searchBlogNameIdOutsideBrackets(message);
                    }
                }
                if (blogAlias != null) {
                    ClientTO client = ServiceLocator.findService(ClientRetrievalService.class)
                            .findClient(clientId);
                    HashSet<String> blogNameIds = new HashSet<String>(1);
                    blogNameIds.add(blogAlias);
                    MailBasedPostingManagementFacade.instance().createNoteFromMail(client, message,
                            senderEmail, blogNameIds);
                } else {
                    LOG.debug("Received message without blog alias in subject");
                    sendHelpEmail(senderEmail);
                    dropMessage = true;
                }
            } else {
                LOG.debug("Received message without legal recipient");
                dropMessage = dropMessages;
            }
        } catch (MailBasedPostingManagementException e) {
            LOG.error("Error during post creation from email.", e);
        } catch (ClientNotFoundException e) {
            LOG.error("Client with ID {} does not exist", clientId);
        } catch (MessagingException e) {
            LOG.error("Error while processing email message.", e);
        }
        if (dropMessage) {
            MailFetcher fetcher = MailFetcher.instance();
            if (fetcher != null) {
                fetcher.deleteMessage(message);
            }
        }
    }

    /**
     * Tests if the message has the single mail address as recipient.
     *
     * @param message
     *            the email message
     * @return true if the single email address is one of the recipients
     * @throws MessagingException
     *             when there is messaging exception
     */
    private boolean hasLegalRecipient(Message message) throws MessagingException {
        // check both header fields because we are only interested if the email address is contained
        String[] recipients = MailMessageHelper.getAllRecipients(message);
        if (recipients != null) {
            for (String r : recipients) {
                if (r.equalsIgnoreCase(singleMailAddress)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests whether a blog name identifier is valid regarding length and contained characters.
     *
     * @param blogNameId
     *            the id to check
     * @return true if the id is valid, false otherwise
     */
    private boolean isValidBlogNameId(String blogNameId) {
        return (blogNameId.length() > 1
                && blogNameId.length() < EmailValidator.MAX_SAFE_LENGTH_LOCAL_PART && blogNameId
                    .matches(BlogManagementHelper.REG_EXP_TOPIC_NAME_IDENTIFIER));
    }

    /**
     * Returns the last string of the subject that is a syntactically correct blog name identifier.
     *
     * @param message
     *            the message to examine
     * @return the blog name id
     */
    private String searchBlogNameIdOutsideBrackets(Message message) {
        String blogNameId = null;
        try {
            String subject = message.getSubject();
            Pattern p = Pattern.compile(BlogManagementHelper.REG_EXP_TOPIC_NAME_IDENTIFIER);
            Matcher m = p.matcher(subject);
            ArrayList<String> candidates = new ArrayList<String>();
            while (m.find()) {
                if (isValidBlogNameId(m.group())) {
                    candidates.add(m.group());
                }
            }
            if (candidates.size() > 0) {
                blogNameId = candidates.get(candidates.size() - 1);
            }
        } catch (MessagingException e) {
            LOG.error("Evaluating subject failed.", e);
        }
        return blogNameId;
    }

    /**
     * Sends an email informing the sender about the missing blog name identifier in the subject,
     * but only if he is a registered user.
     *
     * @param senderEmail
     *            the email address of the sender
     */
    private void sendHelpEmail(String senderEmail) {
        // if sender is registered user send a notification about the missing
        // blog-identifier
        User user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByEmail(senderEmail);
        if (user != null && user.getStatus().equals(UserStatus.ACTIVE)) {
            ServiceLocator.findService(MailSender.class).send(
                    new BlogNameIdMissingInSubjectMailMessage(user));
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received message from email " + senderEmail
                        + " which cannot be ressolved to an existing active user.");
            }
        }
    }
}
