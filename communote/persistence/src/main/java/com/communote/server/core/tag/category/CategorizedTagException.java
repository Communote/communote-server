package com.communote.server.core.tag.category;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CategorizedTagException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1706538694342437299L;

    /**
     * Constructs a new instance of CategorizedTagException
     *
     */
    public CategorizedTagException(String message) {
        super(message);
    }

}
