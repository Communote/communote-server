package com.communote.server.core.user.group;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CantAddParentAsChildException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5423194782571788123L;

    /**
     * Constructs a new instance of CantAddParentAsChildException
     *
     */
    public CantAddParentAsChildException(String message) {
        super(message);
    }

}
