package com.communote.server.core.filter.listitems;

/**
 * <p>
 * Value object holding details about the followed items related to a note.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteFollowListItem
        implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1177100011549309539L;

    private boolean followsBlog;

    private boolean followsDiscussion;

    private boolean followsAuthor;

    public NoteFollowListItem() {
        this.followsBlog = false;
        this.followsDiscussion = false;
        this.followsAuthor = false;
    }

    public NoteFollowListItem(boolean followsBlog, boolean followsDiscussion, boolean followsAuthor) {
        this.followsBlog = followsBlog;
        this.followsDiscussion = followsDiscussion;
        this.followsAuthor = followsAuthor;
    }

    /**
     * Copies constructor from other NoteFollowListItem
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public NoteFollowListItem(NoteFollowListItem otherBean) {
        this(otherBean.isFollowsBlog(), otherBean.isFollowsDiscussion(), otherBean
                .isFollowsAuthor());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(NoteFollowListItem otherBean) {
        if (otherBean != null) {
            this.setFollowsBlog(otherBean.isFollowsBlog());
            this.setFollowsDiscussion(otherBean.isFollowsDiscussion());
            this.setFollowsAuthor(otherBean.isFollowsAuthor());
        }
    }

    /**
     * <p>
     * denotes whether the current user follows the author of the note
     * </p>
     */
    public boolean isFollowsAuthor() {
        return this.followsAuthor;
    }

    /**
     * <p>
     * denotes whether the current user follows the blog of the note
     * </p>
     */
    public boolean isFollowsBlog() {
        return this.followsBlog;
    }

    /**
     * <p>
     * denotes whether the current user follows the discussion of the note
     * </p>
     */
    public boolean isFollowsDiscussion() {
        return this.followsDiscussion;
    }

    public void setFollowsAuthor(boolean followsAuthor) {
        this.followsAuthor = followsAuthor;
    }

    public void setFollowsBlog(boolean followsBlog) {
        this.followsBlog = followsBlog;
    }

    public void setFollowsDiscussion(boolean followsDiscussion) {
        this.followsDiscussion = followsDiscussion;
    }

}