package com.communote.server.core.mail.messages.user;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.communote.common.util.HTMLHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.blog.helper.MailBasedPostingHelper;
import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.retrieval.helper.AttachmentHelper;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;

/**
 * Notification message for notifying about created/modified user tagged posts.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotifyAboutNoteMailMessage extends MailMessage {

    private final Collection<User> receivers;

    private final Note note;

    private int receiverCounter = 0;

    private final Blog topic;

    private final String fromAddress;

    private final String content;

    private String additionalSubjectData;

    private final String localizedTopicTitle;

    private final boolean modified;

    private final Map<String, Object> model;

    /**
     * Creates a notification message for user tagged posts..
     * 
     * @param receivers
     *            the receivers
     * @param note
     *            the user tagged post
     * @param blog
     *            the associated blog
     * @param sender
     *            the sender
     * @param locale
     *            the locale
     * @param templatePlaceholderMessageKeys
     *            A map of placeholders to message keys, which will be replaced within the template
     *            with the loaded message. Use it in the template @@placeholderMessageKey@@.
     * @param model
     *            Additional elements used for the velocity context.
     */
    public NotifyAboutNoteMailMessage(Collection<User> receivers, User sender,
            Locale locale, Note note, Blog blog,
            Map<String, String> templatePlaceholderMessageKeys, Map<String, Object> model) {
        super("mail.message.user.notify-about-note", templatePlaceholderMessageKeys, locale);
        this.receivers = receivers;
        this.note = note;
        this.topic = blog;
        this.model = model;
        this.modified = note.getCreationDate().before(note.getLastModificationDate());
        this.localizedTopicTitle = this.topic.getTitle();
        this.fromAddress = MailBasedPostingHelper.getBlogEmailAddress(blog.getNameIdentifier());
        String htmlContent = note.getContent().getContent();
        this.content = HTMLHelper.htmlToPlaintextExt(htmlContent, true);
        if (MailBasedPostingHelper.isMailFetchingEnabled()
                && MailBasedPostingHelper.isRunningInSingleAddressMode()) {
            this.additionalSubjectData = "[" + blog.getNameIdentifier() + "] ";
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.mail.messages.MailMessage#getFromAddress()
     */
    @Override
    public String getFromAddress() {
        if (this.fromAddress == null) {
            return super.getFromAddress();
        }
        return this.fromAddress;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.mail.messages.MailMessage#getFromAddressName()
     */
    @Override
    public String getFromAddressName() {
        return this.localizedTopicTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMessageIdentifier() {
        return MailBasedPostingHelper.createMessageIdentifier(note);
    }

    /**
     * Returns the actual count of receivers.
     * 
     * @return receiver count.
     */
    public int getReceiverCount() {
        return receiverCounter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReplyToAddress() {
        if (this.fromAddress == null) {
            return super.getReplyToAddress();
        }
        return this.fromAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReplyToAddressName() {
        if (this.fromAddress == null) {
            return super.getReplyToAddressName();
        }
        return this.localizedTopicTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        if (this.model != null) {
            model.putAll(this.model);
        }
        model.put(MailModelPlaceholderConstants.USER, note.getUser());
        boolean renderAttachmentLinks = ClientProperty.NOTIFICATION_RENDER_ATTACHMENTLINKS
                .getValue(ClientProperty.DEFAULT_NOTIFICATION_RENDER_ATTACHMENTLINKS);

        if (renderAttachmentLinks && !note.getAttachments().isEmpty()) {
            String[] attachments = new String[note.getAttachments().size()];
            int i = 0;
            for (Attachment attachment : note.getAttachments()) {
                attachments[i] = "[" + (i + 1) + "] " + attachment.getName() + ": "
                        + AttachmentHelper.determineAbsoluteAttachmentUrl(attachment, true);
                i++;
            }
            model.put(MailModelPlaceholderConstants.ATTACHMENTS, attachments);
        }

        for (User user : receivers) {
            model.put(
                    MailModelPlaceholderConstants.UTI_FORMATED_DATE_CREATE,
                    UserManagementHelper.getDateFormat(user.getId(), user.getLanguageLocale())
                            .format(note.getCreationDate()));
            model.put(
                    MailModelPlaceholderConstants.UTI_FORMATED_DATE_MODIFY,
                    UserManagementHelper.getDateFormat(user.getId(), user.getLanguageLocale())
                            .format(note.getLastModificationDate()));
            // only one element should exists
            break;
        }

        Set<Tag> tags = note.getTags();
        List<String> tagList = new ArrayList<String>();
        if (tags != null) {
            for (Tag tag : tags) {
                tagList.add(tag.getName());
            }
        }
        model.put(MailModelPlaceholderConstants.UTI_TAGS, StringUtils.join(tagList, ", "));
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, this.localizedTopicTitle);
        boolean renderLink = ClientProperty.NOTIFICATION_RENDER_PERMALINKS.getValue(
                ClientProperty.DEFAULT_NOTIFICATION_RENDER_PERMALINKS);
        model.put(MailModelPlaceholderConstants.RENDER_PERMA_LINK, renderLink);
        model.put(MailModelPlaceholderConstants.PERMA_LINK_NOTE,
                ServiceLocator.findService(PermalinkGenerationManagement.class)
                        .getNoteLink(topic.getNameIdentifier(), note.getId(), true));
        model.put(MailModelPlaceholderConstants.UTP_CONTENT, content);
        model.put(MailModelPlaceholderConstants.IS_DIRECT, note.isDirect());
        model.put(MailModelPlaceholderConstants.IS_MODIFIED, modified);
        if (this.additionalSubjectData != null) {
            model.put(MailModelPlaceholderConstants.SUBJECT_PREPEND, additionalSubjectData);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReceivers(MimeMessageHelper message) throws MessagingException,
            UnsupportedEncodingException {
        for (User user : receivers) {
            // hide email addresses of users
            message.addBcc(user.getEmail());
            receiverCounter++;
        }
    }
}
