package com.communote.server.core.blog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.communote.common.io.MaxLengthReachedException;
import com.communote.common.util.HTMLHelper;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.helper.MailBasedPostingHelper;
import com.communote.server.core.blog.notes.ReplyNotDirectMessageException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageWrongRecipientForAnswerException;
import com.communote.server.core.blog.notes.processors.exceptions.NonMatchingParentTopicNotePreProcessorException;
import com.communote.server.core.crc.ContentRepositoryManagementHelper;
import com.communote.server.core.crc.ResourceSizeLimitReachedException;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.mail.fetching.MailFetcher;
import com.communote.server.core.mail.messages.GenericMailMessage;
import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.fetching.BlogsNotFoundMailMessage;
import com.communote.server.core.mail.messages.fetching.ContentProcessingErrorMailMessage;
import com.communote.server.core.mail.messages.fetching.DirectMessageMissingRecipientMailMessage;
import com.communote.server.core.mail.messages.fetching.GenericErrorMailMessage;
import com.communote.server.core.mail.messages.fetching.NoWriteAccessToBlogMailMessage;
import com.communote.server.core.mail.messages.fetching.UserNotInClientMailMessage;
import com.communote.server.core.mail.messages.fetching.WarningMailMessage;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.storing.NoteLimitReachedException;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.storing.ResourceStoringManagementException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.note.ProcessedMailNote;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.blog.CreateBlogPostHelper;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.service.NoteService;

/**
 * Implementation of {@link MailBasedPostingManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailBasedPostingManagementImpl extends MailBasedPostingManagementBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MailBasedPostingManagementImpl.class);

    /**
     * @param message
     *            the message
     * @param sender
     *            the sender
     * @param blogId
     *            the id of the blog
     * @param additionalBlogs
     *            the aliases of additional blogs
     * @return whether the creation has been completed successfully
     */
    private boolean createNoteOrReply(Message message, User sender, Long blogId,
            Set<String> additionalBlogs) {
        // flag indicating whether the creation completed; is also true if
        // creation failed due to one of UTP management exceptions
        boolean creationCompleted = true;
        try {
            NoteStoringTO storingTO = createNoteStoringTO(message, sender, blogId);
            if (storingTO != null) {
                creationCompleted = createNoteOrReply(message, storingTO, sender, additionalBlogs);
            } else {
                LOGGER.debug("Recieved message with no content.");
                sendErrorMailMessage(new ContentProcessingErrorMailMessage(sender,
                        getBlogTitle(blogId), ContentProcessingErrorMailMessage.Type.NO_CONTENT));
            }
        } catch (MessagingException e) {
            LOGGER.error("Creating the post failed.", e);
            creationCompleted = false;
        } catch (IOException e) {
            LOGGER.error("Creating the post failed.", e);
            creationCompleted = false;
        } catch (ResourceStoringManagementException e) {
            LOGGER.error("Creating the post failed.", e);
            creationCompleted = false;
            Throwable cause = e.getCause();
            String errorMessage = ResourceBundleManager.instance().getText(
                    "error.blogpost.upload.failed", sender.getLanguageLocale());
            if (cause instanceof InitializeException) {
                errorMessage = ResourceBundleManager.instance().getText(
                        "error.blogpost.file.upload.virus.config", sender.getLanguageLocale());
            } else if (cause instanceof VirusFoundException) {
                creationCompleted = true;
                errorMessage = ResourceBundleManager.instance().getText(
                        "error.blogpost.file.upload.virus.found", sender.getLanguageLocale());
            } else if (cause instanceof ResourceSizeLimitReachedException) {
                errorMessage = ResourceBundleManager.instance().getText(
                        "error.blogpost.upload.limit.reached",
                        sender.getLanguageLocale(),
                        FileUtils.byteCountToDisplaySize(ContentRepositoryManagementHelper
                                .getSizeLimit()));
            } else if (cause instanceof MaxLengthReachedException) {
                creationCompleted = true;
                errorMessage = ResourceBundleManager.instance().getText(
                        "error.blogpost.upload.filesize.limit",
                        sender.getLanguageLocale(),
                        FileUtils.byteCountToDisplaySize(NumberUtils.toLong(
                                ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE.getValue(), -1)));
            }
            GenericErrorMailMessage errorMail = new GenericErrorMailMessage(sender, errorMessage,
                    getBlogTitle(blogId));
            sendErrorMailMessage(errorMail);
        } catch (AuthorizationException e) {
            // should not occur since the current user is the internal system user and the sender
            // exists
            creationCompleted = false;
            LOGGER.error("Uexpected exception while creating a note from an e-mail", e);
        }
        return creationCompleted;
    }

    /**
     *
     * @param message
     *            the email message
     * @param storingTO
     *            TO holding details of the note to store
     * @param sender
     *            the user that is the sender of the email
     * @param additionalBlogs
     *            the aliases of additional blogs
     * @return whether the creation has been completed successfully
     * @throws MessagingException
     *             in case of an error while reading the email header data
     */
    private boolean createNoteOrReply(Message message, NoteStoringTO storingTO, User sender,
            Set<String> additionalBlogs) throws MessagingException {
        Long parentNoteId = getParentNoteId(message);
        boolean creationCompleted;
        boolean retry = true;
        while (true) {
            try {
                creationCompleted = doCreateNoteOrReply(storingTO, parentNoteId, sender,
                        additionalBlogs);
                break;
            } catch (NoteStoringPreProcessorException e) {
                LOGGER.debug("Recieved message with unprocessable content.", e);
                if (retry) {
                    // try again with pure plain text
                    storingTO = replaceContentWithPlainText(message, storingTO);
                    retry = false;
                } else {
                    // stop and send mail that content was not processable
                    sendErrorMailMessage(new ContentProcessingErrorMailMessage(sender,
                            getBlogTitle(storingTO.getBlogId()),
                            ContentProcessingErrorMailMessage.Type.CONTENT_UNPROCESSABLE));
                    creationCompleted = true;
                    break;
                }
            } catch (NoteNotFoundException e) {
                LOGGER.debug("Recieved reply to note that does not exist ", e);
                sendErrorMailMessage(new GenericMailMessage(
                        "mail.message.fetching.parent-note-not-found",
                        sender.getLanguageLocale(), sender));
                // Reset the parentNoteId to creating a new note.
                parentNoteId = null;
            }
        }
        return creationCompleted;
    }

    /**
     * Creates a user tagged post storing transfer object from a mail message.
     *
     * @param message
     *            the mail message
     * @param sender
     *            the sender of the email
     * @param blogId
     *            the blogId
     * @return the transfer object or null if the message had no content to post (text or html)
     * @throws MessagingException
     *             thrown when there is an exception in the java mail API
     * @throws IOException
     *             might happen when reading from the mailbox
     * @throws AuthorizationException
     *             in case there is no authenticated user
     */
    private NoteStoringTO createNoteStoringTO(Message message, User sender, Long blogId)
            throws MessagingException, IOException, AuthorizationException {
        NoteStoringTO storingTO = null;
        String txt = MailMessageHelper.getMessageText(message, true, false);
        if (StringUtils.isNotBlank(txt)) {
            storingTO = new NoteStoringTO();
            String htmlContent = MailMessageHelper.getMessageText(message, false, true);
            if (StringUtils.isBlank(htmlContent)) {
                storingTO.setContent(txt);
                storingTO.setContentType(NoteContentType.PLAIN_TEXT);
            } else {
                storingTO.setContent(htmlContent);
                storingTO.setContentType(NoteContentType.HTML);
            }
            storingTO.setCreatorId(sender.getId());
            storingTO.setCreationSource(NoteCreationSource.MAIL);
            storingTO.setBlogId(blogId);
            CreateBlogPostHelper.setDefaultFailLevel(storingTO);
            storingTO.setSendNotifications(true);
            storingTO.setPublish(true);
            storingTO.setVersion(0L);
            Collection<AttachmentTO> attachments = MailMessageHelper.getAttachments(message);
            Long[] attachmentIds = new Long[attachments.size()];
            int i = 0;
            for (AttachmentTO attachment : attachments) {
                attachment.setUploaderId(sender.getId());
                Attachment storedAttachment = ServiceLocator.findService(
                        ResourceStoringManagement.class).storeAttachment(attachment);
                attachmentIds[i] = storedAttachment.getId();
                i++;
            }
            storingTO.setAttachmentIds(attachmentIds);
        }
        return storingTO;
    }

    /**
     * @param storingTO
     *            TO holding details of the note to store
     * @param parentNoteId
     *            the ID of the parent note or null if it is not a reply
     * @param sender
     *            the user that is the sender of the email
     * @param additionalBlogs
     *            the aliases of additional blogs
     * @return whether the creation has been completed successfully
     * @throws NoteStoringPreProcessorException
     *             in case the content is not processable
     * @throws NoteNotFoundException
     *             if the note referenced by parentnoteId does not exist
     */
    private boolean doCreateNoteOrReply(NoteStoringTO storingTO, Long parentNoteId,
            User sender, Set<String> additionalBlogs) throws NoteStoringPreProcessorException,
            NoteNotFoundException {
        NoteService noteService = ServiceLocator.findService(NoteService.class);
        boolean creationCompleted = true;

        try {
            NoteModificationResult result;
            storingTO.setParentNoteId(parentNoteId);
            result = noteService.createNote(storingTO, additionalBlogs);
            evaluateNoteModificationResult(result, sender, storingTO.getBlogId(),
                    storingTO.getCreatorId());
        } catch (BlogNotFoundException e) {
            handleBlogNotFoundException(e, sender);
        } catch (NoteManagementAuthorizationException e) {
            LOGGER.debug("Recieved message addressing blogs for which the "
                    + "user has no write access.");
            sendErrorMailMessage(new NoWriteAccessToBlogMailMessage(sender, e.getBlogTitle()));
        } catch (OnlyCrosspostMarkupException e) {
            sendErrorMailMessage(new ContentProcessingErrorMailMessage(sender,
                    getBlogTitle(storingTO.getBlogId()),
                    ContentProcessingErrorMailMessage.Type.NO_CONTENT));
        } catch (ReplyNotDirectMessageException e) {
            LOGGER.debug("Recieved message has no direct message syntax to create a correct reply.");
            sendErrorMailMessage(new GenericMailMessage(
                    "mail.message.fetching.reply-is-no-directmessage", sender.getLanguageLocale(),
                    sender));
        } catch (DirectMessageMissingRecipientException e) {
            LOGGER.debug("Recieved message contains recipients which do not have read access or do not exist. "
                    + "A direct message could not be created.");
            sendErrorMailMessage(new DirectMessageMissingRecipientMailMessage(sender,
                    e.getUninformableUsers(), e.getUnresolvableUsers()));
        } catch (DirectMessageWrongRecipientForAnswerException e) {
            LOGGER.debug("Recieved message contains wrong recipients to create a direct message.");
            sendErrorMailMessage(new GenericMailMessage(
                    "mail.message.fetching.directmessage-wrong-recipient-message",
                    sender.getLanguageLocale(), sender));
        } catch (NoteManagementException e) {
            Throwable cause = ExceptionUtils.getCause(e);
            if (cause instanceof NoteLimitReachedException) {
                sendErrorMailMessage(new GenericMailMessage(
                        "mail.message.fetching.post-limit-reached", sender.getLanguageLocale(),
                        sender));
            } else if (cause instanceof NonMatchingParentTopicNotePreProcessorException) {
                sendErrorMailMessage(new GenericMailMessage(
                        "mail.message.fetching.parent-topic-not-matching",
                        sender.getLanguageLocale(), sender));
            } else {
                LOGGER.error("Storing a note from an email message failed: {}", e);
                creationCompleted = false;
            }
        }
        return creationCompleted;
    }

    /**
     * Evaluate the result of the note creation operation and send appropriate feedback messages.
     *
     * @param result
     *            the note creation result
     * @param sender
     *            the user that sent the processed enauk
     * @param blogId
     *            the ID of the target blog
     * @param creatorId
     *            Id of the messages author.
     */
    private void evaluateNoteModificationResult(NoteModificationResult result, User sender,
            Long blogId, Long creatorId) {
        String alias = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByUserId(creatorId).getAlias();
        result.getUserNotificationResult().getUnresolvableUsers().remove(alias);
        result.getUserNotificationResult().getUninformableUsers().remove(alias);
        if (result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            if (result.getUserNotificationResult().getUnresolvableUsers().size() != 0
                    || result.getUserNotificationResult().getUninformableUsers().size() != 0
                    || result.getUnresolvableBlogs().size() != 0
                    || result.getUnwritableBlogs().size() != 0) {
                sendErrorMailMessage(new WarningMailMessage(sender, result
                        .getUserNotificationResult().getUnresolvableUsers(), result
                        .getUserNotificationResult().getUninformableUsers(),
                        result.getUnresolvableBlogs(), result.getUnwritableBlogs(),
                        getBlogTitle(blogId), result.isDirect()));
            }
        } else {
            if (TransactionInterceptor.currentTransactionStatus().isRollbackOnly()) {
                // explicitly mark this transaction as rollback-only because inner transaction
                // is marked too
                TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            }
            String errorMessage = CreateBlogPostHelper.getFeedbackMessageAfterModification(result,
                    sender.getLanguageLocale());
            sendErrorMailMessage(new GenericErrorMailMessage(sender, errorMessage,
                    getBlogTitle(blogId)));
        }
    }

    /**
     * Helper to get the blog title of a blog
     *
     * @param blogId
     *            the blog searched
     * @return the title of the blog
     */
    private String getBlogTitle(Long blogId) {
        Blog blog = getBlogDao().load(blogId);
        if (blog != null) {
            return blog.getTitle();
        }
        return "";
    }

    /**
     * Checks if the message is a reply to an email notification by examining the email headers.
     *
     * @param message
     *            the message
     * @return the note ID of the parent note or null if the message does not seem to be a reply
     * @throws MessagingException
     *             in case of an error while reading the email headers
     */
    private Long getParentNoteId(Message message) throws MessagingException {
        Long parentNoteId = null;
        String parentMessageId = MailMessageHelper
                .extractMessageIdentifierFromReplyHeaders(message);
        if (parentMessageId != null) {
            parentNoteId = MailBasedPostingHelper
                    .extractNoteIdFromMessageIdentifier(parentMessageId);
        }
        return parentNoteId;
    }

    /**
     * Send an email that blog could not be found.
     *
     * @param e
     *            the exception
     * @param sender
     *            the user that sent the email
     */
    private void handleBlogNotFoundException(BlogNotFoundException e, User sender) {
        LOGGER.debug("Recieved message addressing blogs that do not exist.");
        // send mail about blog not found (one of '&blog's)
        // fallback for null
        String blogIdStr = "???";
        if (e.getBlogNameId() != null) {
            blogIdStr = e.getBlogNameId();
        } else if (e.getBlogId() != null) {
            blogIdStr = e.getBlogId().toString();
        }
        sendErrorMailMessage(new BlogsNotFoundMailMessage(sender, Arrays.asList(blogIdStr), true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleCreateNoteFromMail(Message message, String senderEmail,
            Set<String> blogNameIds) {
        // test if there is a message with this message id
        String messageId = MailFetcher.instance().getUniqueMessageId(message);
        if (messageId == null) {
            LOGGER.error("Failed to retrieve unique message id.");
            return;
        }
        LOGGER.debug("Start post creation from email message with id {}", messageId);
        ProcessedMailNote processedMail = getProcessedMailNoteDao().findByMailMessageId(
                messageId);
        if (processedMail != null) {
            // message already processed -> delete it
            LOGGER.debug("Post from email message with id {} has already been created", messageId);
            if (MailFetcher.instance().deleteMessage(message)) {
                // TODO what to do if next call fails -> we collect lots of
                // superfluous data
                getProcessedMailNoteDao().remove(processedMail.getId());
            }
            // in case deleting fails -> just keep entry in db
        } else {
            SecurityContext currentContext = AuthenticationHelper
                    .setInternalSystemToSecurityContext();
            try {
                processNewMail(message, messageId, senderEmail, blogNameIds);
            } finally {
                AuthenticationHelper.setSecurityContext(currentContext);
            }
        }
    }

    /**
     * The email message was not yet processed, so try to create a note.
     *
     * @param message
     *            the message to process
     * @param messageId
     *            the ID returned from the email server that identifies the message to process
     * @param senderEmail
     *            the email address of the sender of the email
     * @param blogNameIds
     *            additional blog aliases
     */
    private void processNewMail(Message message, String messageId, String senderEmail,
            Set<String> blogNameIds) {
        User user = resolveUser(senderEmail);
        if (user != null) {
            // check for existence of blogs
            ArrayList<String> blogsNotFound = new ArrayList<String>();
            Long firstBlogId = testExistanceOfBlogs(blogNameIds, blogsNotFound);
            if (firstBlogId == null || blogsNotFound.size() > 0) {
                LOGGER.debug("Recieved a message for topics that do not exist");
                sendErrorMailMessage(new BlogsNotFoundMailMessage(user, blogsNotFound, false));
            } else if (!createNoteOrReply(message, user, firstBlogId,
                    blogNameIds)) {
                return; // The message was not successfully posted.
            }
        }
        // the message was processed successfully (i.e. stored in db or rejected due to some
        // error like blog not found, size limits exceeded ...) try to delete message
        LOGGER.debug("Deleting message with id {}", messageId);
        if (!MailFetcher.instance().deleteMessage(message)) {
            // create an entry that the message was processed
            LOGGER.debug("Deleting message with id " + messageId + " failed.");
            ProcessedMailNote newProcessedMail = ProcessedMailNote.Factory
                    .newInstance(messageId);
            getProcessedMailNoteDao().create(newProcessedMail);
        }
    }

    /**
     * Replaces the content of the storing TO with plain text, by first trying to get plain text
     * from the mail message and in case this fails by removing HTML from current content.
     *
     * @param message
     *            the mail message
     * @param storingTO
     *            the transfer object
     * @return the modified transfer object
     */
    private NoteStoringTO replaceContentWithPlainText(Message message, NoteStoringTO storingTO) {
        String txt = null;
        try {
            txt = MailMessageHelper.getMessageText(message, true, true);
        } catch (MessagingException e) {
            LOGGER.error("Error during replacing content with plain text.", e);
        } catch (IOException e) {
            LOGGER.error("Error during replacing content with plain text.", e);
        }
        if (txt == null) {
            // fallback which takes current content and strips HTML markup
            String oldContent = storingTO.getContent();
            // TODO optimize this (directly convert to minimized HTML because
            // this will be converted HTML again)
            txt = HTMLHelper.htmlToPlaintext(oldContent);
        }
        storingTO.setContent(txt);
        storingTO.setContentType(NoteContentType.PLAIN_TEXT);
        return storingTO;
    }

    /**
     * Tests whether there is a user on this client with the given email address and whether this
     * user is allowed to write a note, i.e. must not be null and must have status ACTIVE. In case
     * the user is not allowed feedback messages will be sent.
     *
     * @param senderEmail
     *            the email address of the user to test
     * @return the user if the conditions are met, null otherwise
     */
    private User resolveUser(String senderEmail) {
        if (senderEmail == null) {
            LOGGER.debug("Recieved message without sender");
            return null;
        }
        User user = getUserDao().findByEmail(senderEmail);
        if (user != null) {
            if (UserStatus.ACTIVE.equals(user.getStatus())) {
                if (!user.isTermsAccepted()
                        && ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT
                        .getValue(ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT)) {
                    LOGGER.debug("Received message from a user who has not accepted the changed terms of use.");
                    sendErrorMailMessage(new GenericMailMessage(
                            "mail.message.fetching.reaccept-terms-of-use",
                            user.getLanguageLocale(), user));
                } else {
                    return user;
                }
            } else if (UserStatus.TEMPORARILY_DISABLED.equals(user.getStatus())) {
                sendErrorMailMessage(new GenericMailMessage(
                        "mail.message.fetching.user-temporarily-disabled",
                        user.getLanguageLocale(), user));
            } else {
                // TODO specific error messages if user is CONFIRMED or TERMS_NOT_ACCEPTED? Well
                // actually not necessary because user is not notified via e-mail.
                sendErrorMailMessage(new UserNotInClientMailMessage(senderEmail,
                        user.getLanguageLocale()));
            }
        } else {
            LOGGER.info("Recieved message from non-existing user " + senderEmail);
            sendErrorMailMessage(new UserNotInClientMailMessage(senderEmail));
        }
        return null;
    }

    /**
     * Sends an error mail message.
     *
     * @param message
     *            the error mail message
     */
    private void sendErrorMailMessage(MailMessage message) {
        ServiceLocator.instance().getService(MailManagement.class).sendMail(message);
    }

    /**
     * returns id of first blog and removes this blog from the blogNameIds
     *
     * @param blogNameIds
     *            the blog name identifiers to check
     * @param blogsNotFound
     *            will be filled with those members of blogNameIds which do not exist
     * @return the (db) id of the first blog in the blogNameIds array
     */
    private Long testExistanceOfBlogs(Set<String> blogNameIds, Collection<String> blogsNotFound) {
        if (blogNameIds == null) {
            return null;
        }
        Long firstBlogId = null;
        String firstBlogNameId = null;
        for (String blogNameId : blogNameIds) {
            Blog blog = getBlogDao().findByNameIdentifier(blogNameId);
            if (blog != null) {
                if (firstBlogId == null) {
                    firstBlogId = blog.getId();
                    firstBlogNameId = blogNameId;
                }
            } else {
                blogsNotFound.add(blogNameId);
            }
        }
        // remove the firstBlog from the blogNameIds
        if (firstBlogNameId != null) {
            blogNameIds.remove(firstBlogNameId);
        }
        return firstBlogId;
    }

}
