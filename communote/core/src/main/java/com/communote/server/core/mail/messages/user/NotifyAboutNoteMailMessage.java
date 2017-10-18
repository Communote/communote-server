package com.communote.server.core.mail.messages.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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
 * Notification message for notifying about created/modified note.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotifyAboutNoteMailMessage extends MailMessage {

    private final User recipient;

    private final Note note;

    private final Blog topic;

    private final String content;

    private String additionalSubjectData;

    private final boolean modified;

    private final Map<String, Object> model;

    /**
     * Create a notification message for user a note.
     * 
     * @param recipient
     *            the user to notify about the note
     * @param note
     *            the user tagged post
     * @param blog
     *            the associated topic/blog
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
    public NotifyAboutNoteMailMessage(User recipient, User sender,
            Locale locale, Note note, Blog blog,
            Map<String, String> templatePlaceholderMessageKeys, Map<String, Object> model) {
        super("mail.message.user.notify-about-note", templatePlaceholderMessageKeys, locale);
        this.recipient = recipient;
        this.addBcc(recipient);
        this.note = note;
        this.topic = blog;
        this.model = model;
        this.modified = note.getCreationDate().before(note.getLastModificationDate());
        setFromAddressName(this.topic.getTitle());
        setFromAddress(MailBasedPostingHelper.getBlogEmailAddress(blog.getNameIdentifier()));
        String htmlContent = note.getContent().getContent();
        this.content = HTMLHelper.htmlToPlaintextExt(htmlContent, true);
        if (MailBasedPostingHelper.isMailFetchingEnabled()
                && MailBasedPostingHelper.isRunningInSingleAddressMode()) {
            this.additionalSubjectData = "[" + blog.getNameIdentifier() + "] ";
        }
    }

    @Override
    public String getMessageIdentifier() {
        return MailBasedPostingHelper.createMessageIdentifier(note);
    }

    @Override
    public String getReplyToAddress() {
        // if mail fetching is enabled, the from address will be set to the topic's email address.
        // Replies should be sent to this topic.
        if (this.getFromAddress() == null) {
            return super.getReplyToAddress();
        }
        return this.getFromAddress();
    }

    @Override
    public String getReplyToAddressName() {
        if (this.getFromAddress() == null) {
            return super.getReplyToAddressName();
        }
        return this.topic.getTitle();
    }

    @Override
    public void prepareModel(Map<String, Object> model) {
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

        model.put(
                MailModelPlaceholderConstants.UTI_FORMATED_DATE_CREATE,
                UserManagementHelper.getDateFormat(recipient.getId(), recipient.getLanguageLocale())
                        .format(note.getCreationDate()));
        model.put(
                MailModelPlaceholderConstants.UTI_FORMATED_DATE_MODIFY,
                UserManagementHelper.getDateFormat(recipient.getId(), recipient.getLanguageLocale())
                        .format(note.getLastModificationDate()));

        Set<Tag> tags = note.getTags();
        List<String> tagList = new ArrayList<String>();
        if (tags != null) {
            for (Tag tag : tags) {
                tagList.add(tag.getName());
            }
        }
        model.put(MailModelPlaceholderConstants.UTI_TAGS, StringUtils.join(tagList, ", "));
        model.put(MailModelPlaceholderConstants.BLOG_TITLE, this.topic.getTitle());
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
}
