package com.communote.plugins.api.rest.v24.exception;

/**
 * Exception for not supported extension.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExtensionNotSupportedException
        extends Exception {

    /**
     * Getting the serial version identifier
     * 
     * @return serial version identifier
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * Extention witch is unsupported
     */
    private final String extension;

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8984648421251603448L;

    /**
     * The default constructor for <code>ExtensionNotExistException</code>.
     * 
     * @param extention
     *            unsupported extension
     */
    public ExtensionNotSupportedException(String extention) {
        this.extension = extention;
    }

    /**
     * Constructs a new instance of <code>ExtensionNotExistException</code>.
     * 
     * @param extension
     *            witch is unsupported
     * 
     * @param message
     *            the throwable message.
     */
    public ExtensionNotSupportedException(String extension, String message) {
        super(message);
        this.extension = extension;
    }

    /**
     * Getting unsupported extension
     * 
     * @return extentsion
     */
    public String getExtention() {
        return extension;
    }

}