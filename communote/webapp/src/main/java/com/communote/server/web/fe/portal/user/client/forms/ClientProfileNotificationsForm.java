package com.communote.server.web.fe.portal.user.client.forms;

/**
 * Form backing object for client notification settings.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * 
 */
public class ClientProfileNotificationsForm {
    private boolean renderPermalink;
    private boolean renderAttachmentLinks;
    private boolean renderBlogPermalinkInInvitation;
    private int maxUsersToMention;

    /**
     * @return the maxUsersToMention
     */
    public int getMaxUsersToMention() {
        return maxUsersToMention;
    }

    /**
     * @return the renderAttachmentLinks
     */
    public boolean isRenderAttachmentLinks() {
        return renderAttachmentLinks;
    }

    /**
     * Whether a blog permalink should be generated in the blog invitation notification.
     * 
     * @return true if permalinks should be generated
     */
    public boolean isRenderBlogPermalinkInInvitation() {
        return renderBlogPermalinkInInvitation;
    }

    /**
     * Whether permalinks should be generated in notifications.
     * 
     * @return true if permalinks should be generated
     */
    public boolean isRenderPermalinks() {
        return renderPermalink;
    }

    /**
     * @param maxUsersToMention
     *            the maxUsersToMention to set
     */
    public void setMaxUsersToMention(int maxUsersToMention) {
        this.maxUsersToMention = maxUsersToMention;
    }

    /**
     * @param renderAttachmentLinks
     *            the renderAttachmentLinks to set
     */
    public void setRenderAttachmentLinks(boolean renderAttachmentLinks) {
        this.renderAttachmentLinks = renderAttachmentLinks;
    }

    /**
     * Set whether a blog permalink should be generated in the blog invitation notification.
     * 
     * @param renderPermanentlink
     *            true if permalinks should be generated
     */
    public void setRenderBlogPermalinkInInvitation(boolean renderPermanentlink) {
        this.renderBlogPermalinkInInvitation = renderPermanentlink;
    }

    /**
     * Set whether permalinks should be generated in notifications.
     * 
     * @param renderPermanentlink
     *            true if permalinks should be generated
     */
    public void setRenderPermalinks(boolean renderPermanentlink) {
        this.renderPermalink = renderPermanentlink;
    }

}
