package com.communote.server.core.blog;

/**
 * Exception, which is thrown when the user tried to notify to many users.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TooManyMentionedUsersNoteManagementException extends NoteManagementException {

    private static final long serialVersionUID = -2076419670014758559L;

    public TooManyMentionedUsersNoteManagementException() {
        super("The number of mentioned users exceeds the limit of allowed mentions");
    }
}
