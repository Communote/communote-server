package com.communote.server.core.tag;

import com.communote.server.api.core.common.NotFoundException;

/**
 * Exception to indicate, when a TagStore was not found.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagStoreNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -5089679368783279854L;

    /**
     * @param message
     *            The message.
     */
    public TagStoreNotFoundException(String message) {
        super(message);
    }

}
