package com.communote.server.web.fe.portal.blog.forms;

import java.util.Map;

import com.communote.server.core.blog.helper.MailBasedPostingHelper;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;

/**
 * Form for creating a new blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogManagementForm {
    private DetailBlogListItem listItem;
    private String action;
    private boolean currentUserFollowsBlog;
    private String tags;
    private Long parentTopicId;
    private boolean toplevelTopic;

    /**
     * Basic constructor
     * 
     * @param item
     *            optional list item to populate the form
     */
    public BlogManagementForm(DetailBlogListItem item) {
        if (item == null) {
            this.listItem = new DetailBlogListItem();
        } else {
            this.listItem = item;
        }
    }

    /**
     * Get the form action.
     * 
     * @return Current action of this form.
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the email address of the blog.
     * 
     * @return the email address
     */
    public String getBlogEmail() {
        return listItem.getBlogEmail() == null ? "" : listItem.getBlogEmail();
    }

    /**
     * wrapper for {@link MailBasedPostingHelper#getClientWideBlogEmailAddressSuffix()}
     * 
     * @return the email suffix for the current client
     */
    public String getBlogEmailSuffix() {
        return MailBasedPostingHelper.getClientWideBlogEmailAddressSuffix();
    }

    /**
     * @return the blogId
     */
    public Long getBlogId() {
        return listItem.getId();
    }

    /**
     * Returns the mailto string to be inserted as href value for the link of the email address of
     * the blog.
     * 
     * @return the mailto string or null if mail fetching is not enabled
     */
    public String getBlogMailTo() {
        return MailBasedPostingHelper.getBlogMailTo(listItem.getNameIdentifier());
    }

    /**
     * @return the blogProperties
     */
    public Map<String, Object> getBlogProperties() {
        return listItem.getProperties();
    }

    /**
     * @param <T>
     *            Type of the object.
     * @param key
     *            The key.
     * @return The object of type T or null.
     */
    public <T> T getBlogProperty(String key) {
        return (T) listItem.getProperties().get(key);
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return listItem.getDescription();
    }

    /**
     * @return the nameIdentifier
     */
    public String getNameIdentifier() {
        return listItem.getNameIdentifier();
    }

    /**
     * @return Id of the parent topic or.
     */
    public Long getParentTopicId() {
        return parentTopicId;
    }

    /**
     * Get the tags of the blog, separated by ","
     * 
     * @return The comma separated tags for this blog.
     */
    public String getTags() {
        return this.tags;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return listItem.getTitle();
    }

    /**
     * whether notes with creation source 'SYSTEM' will be created in this blog
     * 
     * @return true if notes with creation source 'SYSTEM' will be created in this blog
     */
    public boolean isCreateSystemNotes() {
        return listItem.isCreateSystemNotes();
    }

    /**
     * @return {@code true} if the current user follows this blog, otherwise {@code false}
     */
    public boolean isCurrentUserFollowsBlog() {
        return currentUserFollowsBlog;
    }

    /**
     * wrapper for {@link MailBasedPostingHelper#isMailFetchingEnabled()}
     * 
     * @return whether mail fetching is enabled
     */
    public boolean isMailFetchingEnabled() {
        return MailBasedPostingHelper.isMailFetchingEnabled();
    }

    /**
     * @return listItem.isRootTopic()
     */
    public boolean isRootTopic() {
        return this.listItem.isRootTopic();
    }

    /**
     * Wrapper for
     * 
     * @link {@link MailBasedPostingHelper#isRunningInSingleAddressMode()}.
     * 
     * @return true if running in single address mode
     */
    public boolean isSingleAddressMode() {
        return MailBasedPostingHelper.isRunningInSingleAddressMode();
    }

    /**
     * @return {@code true} if the topic is a top level topic (inside the directory), otherwise
     *         {@code false}
     */
    public boolean isToplevelTopic() {
        return toplevelTopic;
    }

    /**
     * Set the form action
     * 
     * @param action
     *            New action of this form
     */
    public void setAction(String action) {
        this.action = action == null ? null : action.trim();
    }

    /**
     * @param blogId
     *            the blogId to set
     */
    public void setBlogId(Long blogId) {
        this.listItem.setId(blogId);
    }

    /**
     * @param blogProperties
     *            the blogProperties to set
     */
    public void setBlogProperties(Map<String, Object> blogProperties) {
        for (Map.Entry<String, Object> entry : blogProperties.entrySet()) {
            listItem.setProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Define whether notes with creation source 'SYSTEM' should be created in this blog
     * 
     * @param createSystemNotes
     *            true if notes with creation source 'SYSTEM' should be created in this blog
     */
    public void setCreateSystemNotes(boolean createSystemNotes) {
        this.listItem.setCreateSystemNotes(createSystemNotes);
    }

    /**
     * define whether the current user follows this blog
     * 
     * @param currentUserFollowsBlog
     *            {@code true} if the current user follows this blog, otherwise {@code false}
     */
    public void setCurrentUserFollowsBlog(boolean currentUserFollowsBlog) {
        this.currentUserFollowsBlog = currentUserFollowsBlog;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.listItem.setDescription(description == null ? null : description.trim());
    }

    /**
     * @param listItem
     *            the listItem to set
     */
    public void setDetailBlogListItem(DetailBlogListItem listItem) {
        this.listItem = listItem;
    }

    /**
     * @param nameIdentifier
     *            the nameIdentifier to set
     */
    public void setNameIdentifier(String nameIdentifier) {
        this.listItem.setNameIdentifier(nameIdentifier == null ? null : nameIdentifier.trim());
        if (nameIdentifier != null) {
            this.listItem.setBlogEmail(MailBasedPostingHelper.getBlogEmailAddress(nameIdentifier));
        } else {
            this.listItem.setBlogEmail(null);
        }
    }

    /**
     * @param parentTopicId
     *            Id of parent topic.
     */
    public void setParentTopicId(Long parentTopicId) {
        this.parentTopicId = parentTopicId;
    }

    /**
     * Set the tags of the blog, where they are represented by a comma separated String.
     * 
     * @param tags
     *            Comma separated String of the blog tags
     */
    public void setTags(String tags) {
        this.tags = tags == null ? null : tags.trim();
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.listItem.setTitle(title == null ? null : title.trim());
    }

    /**
     * @param toplevelTopic
     *            true if the topic is a top level topic (inside the directory)
     */
    public void setToplevelTopic(boolean toplevelTopic) {
        this.toplevelTopic = toplevelTopic;
    }

}
