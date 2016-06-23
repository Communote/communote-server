package com.communote.server.core.vo;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AbstractTransferObject implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4053923261327492830L;

    public AbstractTransferObject() {
    }

    /**
     * Copies constructor from other AbstractTransferObject
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public AbstractTransferObject(AbstractTransferObject otherBean) {
        this();
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(AbstractTransferObject otherBean) {
        if (otherBean != null) {
        }
    }

}