package com.communote.server.core.plugin;

import java.io.Serializable;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Property implements Serializable {
    private static final long serialVersionUID = -8489151457988930956L;
    private String value;

    /**
     * Empty construtor for serialization.
     */
    public Property() {
        // Do nothing.
    }

    /**
     * Constructor.
     * 
     * @param value
     *            The value.
     */
    public Property(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
