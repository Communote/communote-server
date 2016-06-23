package com.communote.server.core.tag;

import com.communote.server.api.core.common.NotFoundException;

/**
 * Thrown when a tag does not exist.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagNotFoundException extends NotFoundException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2433238753820247077L;

    /**
     * Constructs a new instance of TagNotFoundException
     *
     */
    public TagNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of TagNotFoundException
     *
     */
    public TagNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
