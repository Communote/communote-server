package com.communote.server.api.core.user;

import com.communote.server.api.core.common.NotFoundException;

/**
 * Thrown if a user or group is not found. Usually the more specific sub-type should be thrown.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteEntityNotFoundException extends NotFoundException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2982814501822200591L;

    /**
     * The default constructor.
     */
    // TODO remove lazy-programmer's no-args constructor
    public CommunoteEntityNotFoundException() {
        this(null);
    }

    /**
     * Constructs a new instance of CommunoteEntityNotFoundException
     *
     */
    public CommunoteEntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of CommunoteEntityNotFoundException
     *
     */
    public CommunoteEntityNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
