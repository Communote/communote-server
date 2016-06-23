package com.communote.server.api.core.note;

/**
 * Transfer object which defines the cases that will lead to a failure instead of a warning during
 * note creation / modification.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteStoringFailDefinition implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 7600276307877701290L;

    private boolean failOnUnresolvableUsers;

    private boolean failOnUnresolvableBlogs;

    private boolean failOnMissingBlogWriteAccess;

    private boolean failOnUninformableUser;

    /**
     * Creates a fail definition with default settings, that is nothing will lead to a failure.
     */
    public NoteStoringFailDefinition() {
        this.failOnUnresolvableUsers = false;
        this.failOnUnresolvableBlogs = false;
        this.failOnMissingBlogWriteAccess = false;
        this.failOnUninformableUser = false;
    }

    /**
     * Creates a fail definition.
     *
     * @param failOnUnresolvableUsers
     *            whether to fail if a user cannot be resolved to an existing user
     * @param failOnUnresolvableBlogs
     *            whether to fail if a blog cannot be resolved to an existing blog
     * @param failOnMissingBlogWriteAccess
     *            whether to fail if a crosspost blog is not writable
     * @param failOnUninformableUser
     *            whether to fail if a user to be notified has no read access to a blog
     */
    public NoteStoringFailDefinition(boolean failOnUnresolvableUsers,
            boolean failOnUnresolvableBlogs, boolean failOnMissingBlogWriteAccess,
            boolean failOnUninformableUser) {
        this.failOnUnresolvableUsers = failOnUnresolvableUsers;
        this.failOnUnresolvableBlogs = failOnUnresolvableBlogs;
        this.failOnMissingBlogWriteAccess = failOnMissingBlogWriteAccess;
        this.failOnUninformableUser = failOnUninformableUser;
    }

    /**
     * @return whether to fail when the current user has no write access to one of the cross posting
     *         blogs. Failing means that the note will not be created in any blog. When set to
     *         false, the note will be created in all the writable blogs and the unwritable blogs
     *         will be listed in the result object.
     */
    public boolean isFailOnMissingBlogWriteAccess() {
        return this.failOnMissingBlogWriteAccess;
    }

    /**
     * @return whether to fail when one of the users that should be informed cannot be informed
     *         because of missing read access rights for the blog. Fail means that the note will not
     *         be created.
     */
    public boolean isFailOnUninformableUser() {
        return this.failOnUninformableUser;
    }

    /**
     * @return whether to fail when one of the cross posting blogs cannot be resolved to an existing
     *         blog. Failing means that the note will not be created in any blog. When set to false
     *         the note will be created in all found blogs and the result object will contain the
     *         unresolvable blogs.
     */
    public boolean isFailOnUnresolvableBlogs() {
        return this.failOnUnresolvableBlogs;
    }

    /**
     * @return whether to fail when one of the notification targets cannot be resolved to an
     *         existing user. Failing means that the note will not be created.
     */
    public boolean isFailOnUnresolvableUsers() {
        return this.failOnUnresolvableUsers;
    }

    /**
     * Specify whether to fail when the current user has no write access to one of the cross posting
     * blogs. Failing means that the note will not be created in any blog. When set to false, the
     * post will be created in all the writable blogs and the unwritable blogs will be listed in the
     * result object.
     *
     * @param failOnMissingBlogWriteAccess
     *            true if the situation should lead to failure
     */
    public void setFailOnMissingBlogWriteAccess(boolean failOnMissingBlogWriteAccess) {
        this.failOnMissingBlogWriteAccess = failOnMissingBlogWriteAccess;
    }

    /**
     * Specify whether to fail when one of the users that should be informed cannot be informed
     * because of missing read access rights for the blog. Fail means that the note will not be
     * created.
     *
     * @param failOnUninformableUser
     *            true if the situation should lead to failure
     */
    public void setFailOnUninformableUser(boolean failOnUninformableUser) {
        this.failOnUninformableUser = failOnUninformableUser;
    }

    /**
     * Specify whether to fail when one of the cross posting blogs cannot be resolved to an existing
     * blog. Failing means that the note will not be created in any blog. When set to false the note
     * will be created in all found blogs and the result object will contain the unresolvable blogs.
     *
     * @param failOnUnresolvableBlogs
     *            true if the situation should lead to failure
     */
    public void setFailOnUnresolvableBlogs(boolean failOnUnresolvableBlogs) {
        this.failOnUnresolvableBlogs = failOnUnresolvableBlogs;
    }

    /**
     * Specify whether to fail when one of the notification targets cannot be resolved to an
     * existing user. Failing means that the note will not be created.
     *
     * @param failOnUnresolvableUsers
     *            true if the situation should lead to failure
     */
    public void setFailOnUnresolvableUsers(boolean failOnUnresolvableUsers) {
        this.failOnUnresolvableUsers = failOnUnresolvableUsers;
    }

}