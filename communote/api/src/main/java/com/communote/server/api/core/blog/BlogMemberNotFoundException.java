package com.communote.server.api.core.blog;

import com.communote.server.api.core.common.NotFoundException;

/**
 * Thrown if a member of a blog/topic member does not exist
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogMemberNotFoundException extends NotFoundException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4279514820023812397L;

    /**
     * Constructs a new instance of BlogMemberNotFoundException
     *
     */
    public BlogMemberNotFoundException(String message) {
        super(message);
    }

}
