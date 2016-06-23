package com.communote.server.core.blog;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface MailBasedPostingManagement {

    /**
     * <p>
     * Creates a UserTaggedPost from an email message.
     * </p>
     */
    public void createNoteFromMail(javax.mail.Message message, String senderEmail,
            java.util.Set<String> blogNameIds);

}
