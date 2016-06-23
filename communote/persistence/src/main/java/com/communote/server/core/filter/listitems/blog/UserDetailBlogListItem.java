package com.communote.server.core.filter.listitems.blog;

/**
 * <p>
 * Details about a blog plus information about the current user regarding this blog. That is the
 * user group role of the current user.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserDetailBlogListItem extends
        com.communote.server.core.filter.listitems.blog.DetailBlogListItem implements
        java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2697211401140079268L;

    private com.communote.server.model.blog.BlogRole userRole;

    public UserDetailBlogListItem() {
        super();
        this.userRole = null;
    }

    public UserDetailBlogListItem(com.communote.server.model.blog.BlogRole userRole,
            boolean allCanRead, boolean allCanWrite, String readingUserIds, String writingUserIds,
            String managingUserIds, String blogEmail, boolean createSystemNotes,
            String nameIdentifier, String description, Long blogId, String title,
            java.util.Date lastModificationDate) {
        super(allCanRead, allCanWrite, readingUserIds, writingUserIds, managingUserIds, blogEmail,
                createSystemNotes, nameIdentifier, description, blogId, title, lastModificationDate);
        this.userRole = userRole;
    }

    /**
     * <p>
     * User group role of the current user
     * </p>
     */
    public com.communote.server.model.blog.BlogRole getUserRole() {
        return this.userRole;
    }

    public void setUserRole(com.communote.server.model.blog.BlogRole userRole) {
        this.userRole = userRole;
    }

}
