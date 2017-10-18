package com.communote.server.core.mail;

/**
 * Thrown when an email address of a recipient is not valid.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InvalidRecipientMailAddressException extends MailingException {

    private static final long serialVersionUID = 8798261416835862848L;

    private String email;

    /**
     * Constructs a new instance of InvalidRecipientMailAddressException
     *
     * @param message
     *            the throwable message.
     */
    public InvalidRecipientMailAddressException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of InvalidRecipientMailAddressException
     *
     * @param message
     *            the throwable message.
     * @param emailAddress
     *            the address that is not valid
     */
    public InvalidRecipientMailAddressException(String message, String emailAddress) {
        super(message);
        this.email = emailAddress;
    }

    /**
     * @return the invalid email address or null if not set
     */
    public String getEmailAddress() {
        return email;
    }

    public void setEmailAddress(String email) {
        this.email = email;
    }

}
