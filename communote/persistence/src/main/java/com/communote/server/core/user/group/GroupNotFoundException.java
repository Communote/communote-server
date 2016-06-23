package com.communote.server.core.user.group;

import com.communote.server.api.core.user.CommunoteEntityNotFoundException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupNotFoundException extends CommunoteEntityNotFoundException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -104889633713526788L;

    private boolean occuredDuringChangingBlogRights = true;

    /**
     * Constructs a new instance of GroupNotFoundException
     *
     */
    public GroupNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of GroupNotFoundException
     *
     */
    public GroupNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public boolean isOccuredDuringChangingBlogRights() {
        return occuredDuringChangingBlogRights;
    }

    public void setOccuredDuringChangingBlogRights(boolean occuredDuringChangingBlogRights) {
        this.occuredDuringChangingBlogRights = occuredDuringChangingBlogRights;
    }

}
