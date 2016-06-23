package com.communote.server.web.fe.portal.user.system.application;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CacheInvalidationForm {

    private Boolean mainCacheEnabled = false;
    private String[] invalidateCaches;

    /**
     * Returns the names of caches which have to be invalidated.
     * 
     * @return a list of names of caches
     */
    public String[] getInvalidateCaches() {
        return invalidateCaches;
    }

    /**
     * Returns whether main cache should be invalidated.
     * 
     * @return <tt>true</tt> if main cache should be invalidated
     */
    public Boolean getMainCacheEnabled() {
        return mainCacheEnabled;
    }

    /**
     * Sets a list of names of caches which have to be invalidated.
     * 
     * @param invalidateCaches
     *            a list of names of caches
     */
    public void setInvalidateCaches(String[] invalidateCaches) {
        this.invalidateCaches =
                invalidateCaches;
    }

    /**
     * Sets whether main cache should be invalidated.
     * 
     * @param mainCacheEnabled
     *            <tt>true</tt> if main cache should be invalidated
     */
    public void setMainCacheEnabled(Boolean mainCacheEnabled) {
        this.mainCacheEnabled = mainCacheEnabled;
    }

}
