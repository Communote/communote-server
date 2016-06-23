package com.communote.server.core.mail.fetching;

/**
 * Property constants that represents types of mail in protocol supports.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public enum MailInProtocolType {
    /** Internet Message Access Protocol */
    IMAP("imap"),

    /** Internet Message Access Protocol using SSL */
    IMAPS("imaps");

    /**
     * the protocol name
     */
    private String name;

    /**
     * @param name
     *            the protocol name for this element
     */
    private MailInProtocolType(String name) {
        this.name = name;
    }

    /**
     * @return the path element in the global id for this element
     */
    public String getName() {
        return name;
    }
}