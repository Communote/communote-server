package com.communote.server.web.fe.portal.user.forms;

import java.io.Serializable;

/**
 * Form container for the accept terms form.
 * 
 * @see com.communote.server.web.fe.portal.user.controller.AcceptTermsController
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AcceptTermsForm implements Serializable {
    static final long serialVersionUID = 1L;

    /** The terms agreed. */
    private boolean termsAgreed;

    /**
     * Checks if is terms agreed.
     * 
     * @return true, if is terms agreed
     */
    public boolean isTermsAgreed() {
        return termsAgreed;
    }

    /**
     * Sets the terms agreed.
     * 
     * @param termsOfUseAccepted
     *            the new terms agreed
     */
    public void setTermsAgreed(boolean termsOfUseAccepted) {
        this.termsAgreed = termsOfUseAccepted;
    }

}
