package com.communote.server.core.vo.uti;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNotificationResult implements Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5799374826282973735L;

    private Collection<String> uninformableUsers;

    private Collection<String> unresolvableUsers;

    /**
     * Constructor.
     */
    public UserNotificationResult() {
        this(new HashSet<String>(), new HashSet<String>());
    }

    public UserNotificationResult(Collection<String> uninformableUsers,
            Collection<String> unresolvableUsers)
    {
        this.setUninformableUsers(uninformableUsers);
        this.setUnresolvableUsers(unresolvableUsers);
    }

    /**
     * Copies constructor from other UserNotificationResult
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public UserNotificationResult(UserNotificationResult otherBean) {
        this(otherBean.getUninformableUsers(), otherBean.getUnresolvableUsers());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(UserNotificationResult otherBean) {
        if (otherBean != null) {
            this.setUninformableUsers(otherBean.getUninformableUsers());
            this.setUnresolvableUsers(otherBean.getUnresolvableUsers());
        }
    }

    /**
     * <p>
     * A Collection of user aliases of users who cannot be informed. In case of a UTP these are
     * users who do not have read access to the target blog or in the context of a cross post they
     * do not have access to any target blog.
     * </p>
     */
    public Collection<String> getUninformableUsers() {
        return this.uninformableUsers;
    }

    /**
     * <p>
     * Collection of user aliases that could not be resolved to users.
     * </p>
     */
    public Collection<String> getUnresolvableUsers() {
        return this.unresolvableUsers;
    }

    /**
     * @param uninformableUsers
     *            the uninformableUsers to set
     */
    public void setUninformableUsers(Collection<String> uninformableUsers) {
        this.uninformableUsers = uninformableUsers;
    }

    /**
     * @param unresolvableUsers
     *            the unresolvableUsers to set
     */
    public void setUnresolvableUsers(Collection<String> unresolvableUsers) {
        this.unresolvableUsers = unresolvableUsers;
    }
}