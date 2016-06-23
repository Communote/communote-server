package com.communote.server.core.messaging.vo;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Message implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2608162303831508941L;

    private java.util.Date date;

    private String sender;

    private String[] receivers;

    private String message;

    public Message() {
        this.date = null;
        this.sender = null;
        this.receivers = null;
        this.message = null;
    }

    public Message(java.util.Date date, String sender, String[] receivers, String message) {
        this.date = date;
        this.sender = sender;
        this.receivers = receivers;
        this.message = message;
    }

    /**
     * Copies constructor from other Message
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public Message(Message otherBean) {
        this(otherBean.getDate(), otherBean.getSender(), otherBean.getReceivers(), otherBean
                .getMessage());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(Message otherBean) {
        if (otherBean != null) {
            this.setDate(otherBean.getDate());
            this.setSender(otherBean.getSender());
            this.setReceivers(otherBean.getReceivers());
            this.setMessage(otherBean.getMessage());
        }
    }

    /**
     *
     */
    public java.util.Date getDate() {
        return this.date;
    }

    /**
     *
     */
    public String getMessage() {
        return this.message;
    }

    /**
     *
     */
    public String[] getReceivers() {
        return this.receivers;
    }

    /**
     *
     */
    public String getSender() {
        return this.sender;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReceivers(String[] receivers) {
        this.receivers = receivers;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

}