package com.communote.server.api.core.note;

/**
 * <p>
 * Thrown when a user has not the required authorization to create, update or delete a note.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteManagementAuthorizationException extends
com.communote.server.api.core.security.AuthorizationException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4966438244200315850L;

    private String blogTitle;

    /**
     * Constructs a new instance of NoteManagementAuthorizationException
     *
     */
    public NoteManagementAuthorizationException(String message, String blogTitle) {
        super(message);
        this.blogTitle = blogTitle;
    }

    /**
     * Constructs a new instance of NoteManagementAuthorizationException
     *
     */
    public NoteManagementAuthorizationException(String message, Throwable throwable,
            String blogTitle) {
        super(message, throwable);
        this.blogTitle = blogTitle;
    }

    /**
     * <p>
     * The title of the blog for which the user does not have the required access right.
     * </p>
     */
    public String getBlogTitle() {
        return this.blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

}
