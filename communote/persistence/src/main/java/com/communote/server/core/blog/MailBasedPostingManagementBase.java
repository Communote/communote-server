package com.communote.server.core.blog;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.service.blog.MailBasedPostingManagement</code>, provides
 * access to all services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.blog.MailBasedPostingManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class MailBasedPostingManagementBase
        implements com.communote.server.core.blog.MailBasedPostingManagement {

    private com.communote.server.persistence.user.UserDao userDao;

    /**
     * Sets the reference to <code>kenmeiUser</code>'s DAO.
     */
    public void setUserDao(com.communote.server.persistence.user.UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Gets the reference to <code>kenmeiUser</code>'s DAO.
     */
    protected com.communote.server.persistence.user.UserDao getUserDao() {
        return this.userDao;
    }

    private com.communote.server.persistence.blog.ProcessedMailNoteDao processedMailNoteDao;

    /**
     * Sets the reference to <code>processedMailNote</code>'s DAO.
     */
    public void setProcessedMailNoteDao(
            com.communote.server.persistence.blog.ProcessedMailNoteDao processedMailNoteDao) {
        this.processedMailNoteDao = processedMailNoteDao;
    }

    /**
     * Gets the reference to <code>processedMailNote</code>'s DAO.
     */
    protected com.communote.server.persistence.blog.ProcessedMailNoteDao getProcessedMailNoteDao() {
        return this.processedMailNoteDao;
    }

    private com.communote.server.persistence.blog.BlogDao blogDao;

    /**
     * Sets the reference to <code>blog</code>'s DAO.
     */
    public void setBlogDao(com.communote.server.persistence.blog.BlogDao blogDao) {
        this.blogDao = blogDao;
    }

    /**
     * Gets the reference to <code>blog</code>'s DAO.
     */
    protected com.communote.server.persistence.blog.BlogDao getBlogDao() {
        return this.blogDao;
    }

    /**
     * @see 
     *      com.communote.server.service.blog.MailBasedPostingManagement#createNoteFromMail(
     *      javax.mail.Message, String, java.util.Set<String>)
     */
    public void createNoteFromMail(javax.mail.Message message, String senderEmail,
            java.util.Set<String> blogNameIds) {
        if (message == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.blog.MailBasedPostingManagement.createNoteFromMail(javax.mail.Message message, String senderEmail, java.util.Set<String> blogNameIds) - 'message' can not be null");
        }
        if (senderEmail == null || senderEmail.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.blog.MailBasedPostingManagement.createNoteFromMail(javax.mail.Message message, String senderEmail, java.util.Set<String> blogNameIds) - 'senderEmail' can not be null or empty");
        }
        if (blogNameIds == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.blog.MailBasedPostingManagement.createNoteFromMail(javax.mail.Message message, String senderEmail, java.util.Set<String> blogNameIds) - 'blogNameIds' can not be null");
        }
        try {
            this.handleCreateNoteFromMail(message, senderEmail, blogNameIds);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.blog.MailBasedPostingManagementException(
                    "Error performing 'com.communote.server.service.blog.MailBasedPostingManagement.createNoteFromMail(javax.mail.Message message, String senderEmail, java.util.Set<String> blogNameIds)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for {@link #createNoteFromMail(javax.mail.Message, String,
     * java.util.Set<String>)}
     */
    protected abstract void handleCreateNoteFromMail(javax.mail.Message message,
            String senderEmail, java.util.Set<String> blogNameIds);

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }
}