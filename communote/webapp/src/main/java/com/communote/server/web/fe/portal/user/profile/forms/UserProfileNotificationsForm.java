package com.communote.server.web.fe.portal.user.profile.forms;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.messaging.NotificationManagement;
import com.communote.server.core.messaging.connectors.xmpp.XMPPPatternUtils;
import com.communote.server.core.service.BuiltInServiceNames;
import com.communote.server.core.service.CommunoteServiceManager;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileNotificationsForm implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private Boolean xmpp = false;
    private Boolean xmppFail = false;
    private Boolean mail = false;

    private String action;

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the xmppBlogSuffix
     */
    public String getXmppBlogSuffix() {
        return XMPPPatternUtils.getBlogSuffix();
    }

    /**
     *
     * @return the XMPP ID the user has to use to connect to, e.g.
     *         tlu.comunardo@jabber.communote.com
     */
    public String getXmppId() {
        return ServiceLocator.findService(NotificationManagement.class).getXMPPId();
    }

    /**
     * @return the mail
     */
    public boolean isMail() {
        return mail;
    }

    /**
     * @return the xmpp
     */
    public boolean isXmpp() {
        return xmpp;
    }

    /**
     * @return {@code true} if the client supports XMPP otherwise {@code false}
     */
    public boolean isXmppEnabled() {
        return ServiceLocator.findService(CommunoteServiceManager.class).isRunning(
                BuiltInServiceNames.XMPP_MESSAGING);
    }

    /**
     * @return the xmppFail
     */
    public boolean isXmppFail() {
        return xmppFail;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = StringUtils.trim(action);
    }

    /**
     * @param mail
     *            the mail to set
     */
    public void setMail(boolean mail) {
        this.mail = mail;
    }

    /**
     * @param xmpp
     *            the xmpp to set
     */
    public void setXmpp(boolean xmpp) {
        this.xmpp = xmpp;
    }

    /**
     * @param xmppFail
     *            the xmppFail to set
     */
    public void setXmppFail(boolean xmppFail) {
        this.xmppFail = xmppFail;
    }
}
