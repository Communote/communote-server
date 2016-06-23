package com.communote.server.core.tag.category;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CategorizedTagAlreadyExists extends
        com.communote.server.core.tag.category.CategorizedTagException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3474012204836265361L;

    /**
     * Constructs a new instance of CategorizedTagAlreadyExists
     *
     */
    public CategorizedTagAlreadyExists(String message) {
        super(message);
    }

}
