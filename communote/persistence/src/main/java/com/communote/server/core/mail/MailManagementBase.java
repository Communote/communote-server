package com.communote.server.core.mail;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Spring Service base class for <code>com.communote.server.service.mail.MailManagement</code>,
 * provides access to all services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.mail.MailManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class MailManagementBase
        implements com.communote.server.core.mail.MailManagement {

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * Performs the core logic for {@link #resetSettings()}
     */
    protected abstract void handleResetSettings();

    /**
     * Performs the core logic for
     * {@link #sendMail(org.springframework.mail.javamail.MimeMessagePreparator)}
     */
    protected abstract void handleSendMail(
            org.springframework.mail.javamail.MimeMessagePreparator mailMessage)
            throws com.communote.server.core.mail.MailingException;

    /**
     * @see com.communote.server.core.mail.MailManagement#resetSettings()
     */
    public void resetSettings() {
        try {
            this.handleResetSettings();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.mail.MailManagementException(
                    "Error performing 'com.communote.server.service.mail.MailManagement.resetSettings()' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.mail.MailManagement#sendMail(org.springframework.mail.javamail.MimeMessagePreparator)
     */
    public void sendMail(org.springframework.mail.javamail.MimeMessagePreparator mailMessage)
            throws com.communote.server.core.mail.MailingException {
        if (mailMessage == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.mail.MailManagement.sendMail(org.springframework.mail.javamail.MimeMessagePreparator mailMessage) - 'mailMessage' can not be null");
        }
        try {
            this.handleSendMail(mailMessage);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.mail.MailManagementException(
                    "Error performing 'com.communote.server.service.mail.MailManagement.sendMail(org.springframework.mail.javamail.MimeMessagePreparator mailMessage)' --> "
                            + rt,
                    rt);
        }
    }
}