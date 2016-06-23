package com.communote.server.core.blog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.blog.helper.MailBasedPostingHelper;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.mail.fetching.MailFetcher;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * A {@link com.communote.server.core.mail.fetching.MailMessageWorker MailMessageWorker} that tries
 * to create Notes from email messages. This is done by extracting client ID and blog name ID from
 * the recipient address which must follow the pattern "blogNameID.clientID.microblog@host".
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteMultipleAddressesMailMessageWorker extends NoteMailMessageWorkerBase {

    // character separating blog and client part of email address
    private static final String BLOG_CLIENT_SEPARATOR = ".";

    /**
     * the log.
     */
    private final static Logger LOG = LoggerFactory
            .getLogger(NoteMultipleAddressesMailMessageWorker.class);
    private final boolean deleteMailsWithDifferentSuffix;

    /**
     * Creates a new instance of this worker.
     */
    public NoteMultipleAddressesMailMessageWorker() {
        deleteMailsWithDifferentSuffix = CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailfetching.DELETE_MAILS_NOT_FOR_THIS_INSTANCE,
                        false);
    }

    /**
     * Tries to create the note.
     *
     * @param clientId
     *            the client ID
     * @param message
     *            the message
     * @param blogAlias
     *            alias of blog that is going to receive the message
     * @param dropMessage
     *            whether to drop the message when done processing; can be overridden
     * @return whether to drop the message when done processing
     * @throws MessagingException
     *             may be thrown by mail api
     */
    private boolean createMessage(String clientId, Message message, String blogAlias,
            boolean dropMessage) throws MessagingException {
        // silently ignore mails without client
        if (clientId != null) {
            String sender = getMailAddressOfSender(message);
            try {
                ClientTO client = ServiceLocator.findService(ClientRetrievalService.class)
                        .findClient(clientId);

                // use facade for posting
                HashSet<String> blogAliases = new HashSet<String>();
                blogAliases.add(blogAlias);
                MailBasedPostingManagementFacade.instance().createNoteFromMail(client, message,
                        sender, blogAliases);
                // do not drop message here because this is handled by
                // MailBasedPostingManagement
                dropMessage = false;
            } catch (ClientNotFoundException e) {
                LOG.info("The client '"
                        + clientId
                        + "' was not found. A message from '"
                        + sender
                        + "' to '"
                        + StringUtils.join((MailMessageHelper.getAllRecipients(message, false,
                                false,
                                MailBasedPostingHelper.getClientWideBlogEmailAddressSuffix(""))))
                                + "' can't be processed.");
            } catch (MailBasedPostingManagementException e) {
                // only thrown by facade - don't drop
                LOG.error("Error while processing email message.", e);
                dropMessage = false;
            }
        } else {
            LOG.debug("Received message without client.");
        }
        return dropMessage;
    }

    /**
     * Returns the blog alias from blog client parts of email addresses.
     *
     * @param parts
     *            the blog client parts
     * @param clientId
     *            the clientId used for splitting the blog client part
     * @param noClientIdForGlobal
     *            true if mail addresses to the global client do not need the client ID
     * @return the first found blog alias
     */
    private String getBlogAliasFromBlogClientParts(List<String> parts, String clientId,
            boolean noClientIdForGlobal) {
        if (clientId == null) {
            return null;
        }
        String blogAlias = null;
        for (String blogClientPart : parts) {
            int sepIdx = blogClientPart.indexOf(BLOG_CLIENT_SEPARATOR + clientId);
            if (sepIdx > 0) {
                String blogNameId = blogClientPart.substring(0, sepIdx);
                if (StringUtils.isNotEmpty(blogNameId)) {
                    blogAlias = blogNameId;
                    break;
                }
            } else if (noClientIdForGlobal) {
                // take the whole string as blog alias (this can lead to wrong results because there
                // might just be another client id at the end of the blog alias but we cannot check
                // for last blog separator because the blog alias can contain the separator
                // character. Shouldn't be a problem because the blog will not be found.
                blogAlias = blogClientPart;
                break;
            }
        }
        return blogAlias;
    }

    /**
     * Returns the email address part before the '.microblog@'-pattern.
     *
     * @param address
     *            the email address
     * @return the local part
     */
    private String getBlogClientPartFormMailAddress(String address) {
        // get part before @
        int atIdx;
        String staticSuffix = MailBasedPostingHelper.getStaticMailAddressSuffix();
        if (StringUtils.isBlank(staticSuffix)) {
            staticSuffix = "";
        } else {
            staticSuffix = "." + staticSuffix;
        }
        if (address.contains("@")) {
            atIdx = address.toLowerCase().indexOf(staticSuffix + "@");
        } else {
            atIdx = address.toLowerCase().lastIndexOf(staticSuffix);
        }
        if (atIdx > 0) {
            return address.substring(0, atIdx);
        }
        return null;
    }

    /**
     * Filters recipients to those matching the email address pattern.
     *
     * @param message
     *            the message to check for recipients
     * @return the 'blogNameId.clientId' parts from the email addresses or null if there are no
     *         addresses matching the email pattern
     * @throws MessagingException
     *             Exception.
     */
    private List<String> getBlogClientParts(Message message) throws MessagingException {
        String[] recipients = MailMessageHelper.getAllRecipients(message, false, false,
                MailBasedPostingHelper.getClientWideBlogEmailAddressSuffix(""));
        if (recipients == null || recipients.length == 0) {
            return null;
        }
        List<String> parts = new ArrayList<String>();
        for (String address : recipients) {
            String blogClientPart = getBlogClientPartFormMailAddress(address);
            if (blogClientPart != null) {
                parts.add(blogClientPart);
            }
        }
        if (parts.size() == 0) {
            parts = null;
        }
        return parts;
    }

    /**
     * Returns the client id from the blog client part or just the default client id
     *
     * @param blogClientParts
     *            the blog client part
     * @param noClientIdForGlobal
     *            true if mail addresses to the global client do not need the client ID
     * @return the client id
     */
    private String getClientId(List<String> blogClientParts, boolean noClientIdForGlobal) {
        String clientId = null;
        // little optimization: directly return ID of global client if standalone and client ID is
        // not in address
        if (CommunoteRuntime.getInstance().getApplicationInformation().isStandalone()
                && noClientIdForGlobal) {
            return ClientHelper.getGlobalClientId();
        } else {
            for (String part : blogClientParts) {
                String cId = getClientIdFromBlogClientPart(part, noClientIdForGlobal);
                if (!StringUtils.isBlank(cId)) {
                    clientId = cId;
                    break;
                }
            }
            return clientId;
        }
    }

    /**
     * Extracts the clientId from a blog client part of an email address.
     *
     * @param blogClientPart
     *            the blog client part
     * @param noClientIdForGlobal
     *            true if mail addresses to the global client do not need the client ID
     * @return the client part
     */
    private String getClientIdFromBlogClientPart(String blogClientPart, boolean noClientIdForGlobal) {
        String clientPart = null;
        int sepIdx = blogClientPart.lastIndexOf(BLOG_CLIENT_SEPARATOR);
        if (sepIdx >= 0 && !blogClientPart.endsWith(BLOG_CLIENT_SEPARATOR)) {
            clientPart = blogClientPart.substring(sepIdx + 1);
        }
        if (clientPart == null && noClientIdForGlobal) {
            clientPart = ClientHelper.getGlobalClientId();
        }
        return clientPart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleProcessMailMessage(Message message) {
        boolean dropMessage = true;
        try {
            dropMessage = deleteMailsWithDifferentSuffix;
            List<String> blogClientParts = getBlogClientParts(message);
            // ignore messages without recipient
            if (blogClientParts != null) {
                boolean noClientIdForGlobal = CommunoteRuntime
                        .getInstance()
                        .getConfigurationManager()
                        .getApplicationConfigurationProperties()
                        .getProperty(
                                ApplicationPropertyMailfetching.NO_CLIENTID_IN_ADDRESS_FOR_GLOBAL,
                                false);
                // extract first non-empty client ID (no cross client posting
                // support)

                String clientId = getClientId(blogClientParts, noClientIdForGlobal);

                // get blog from recipients. Only one blog makes sense because in multi-address mode
                // the smtp sevice will deliver the email for every blog found in the recipients
                // list to the observed inbox. If we would consider every contained email address we
                // would double postings.

                String blogAlias = getBlogAliasFromBlogClientParts(blogClientParts, clientId,
                        noClientIdForGlobal);
                if (blogAlias != null) {
                    dropMessage = createMessage(clientId, message, blogAlias, dropMessage);
                } else {
                    LOG.debug("Received message without legal recipient");
                }
            } else {
                LOG.debug("Received message without legal recipient");
            }
        } catch (MessagingException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while processing email message.", e);
            }
        }
        if (dropMessage) {
            MailFetcher fetcher = MailFetcher.instance();
            if (fetcher != null) {
                fetcher.deleteMessage(message);
            }
        }
    }

}
