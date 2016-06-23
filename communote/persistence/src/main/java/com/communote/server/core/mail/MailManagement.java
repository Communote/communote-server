package com.communote.server.core.mail;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface MailManagement {

    /**
     * <p>
     * Resets the settings of this service.
     * </p>
     */
    public void resetSettings();

    /**
     * 
     */
    public void sendMail(org.springframework.mail.javamail.MimeMessagePreparator mailMessage)
            throws com.communote.server.core.mail.MailingException;

}
