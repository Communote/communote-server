package com.communote.server.model.user.security;

import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.security.SecurityCodeImpl;
import com.communote.server.model.user.User;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class InviteUserToBlogSecurityCode extends SecurityCodeImpl {
    /**
     * Constructs new instances of {@link InviteUserToBlogSecurityCode}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link InviteUserToBlogSecurityCode}.
         */
        public static InviteUserToBlogSecurityCode newInstance() {
            InviteUserToBlogSecurityCode code = new InviteUserToBlogSecurityCodeImpl();
            // TODO is action still evaluated?
            code.setAction(SecurityCodeAction.INVITE_CLIENT);
            code.generateNewCode();
            code.setCreatingDate(new java.sql.Timestamp(new java.util.Date().getTime()));
            return code;
        }

        public static InviteUserToBlogSecurityCode newInstance(Long inviterId) {
            InviteUserToBlogSecurityCode code = newInstance();
            code.setInvitorId(inviterId);
            return code;
        }

        public static InviteUserToBlogSecurityCode newInstance(Long inviterId, User invitedUser) {
            InviteUserToBlogSecurityCode code = newInstance(inviterId);
            code.setUser(invitedUser);
            return code;
        }

    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2109711606427782655L;

    private Long invitorId;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("invitorId='");
        sb.append(invitorId);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>SecurityCodeImpl</code> class it will simply delegate the call up there.
     *
     * @see SecurityCode#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     * @return the ID of the inviting user
     */
    public Long getInvitorId() {
        return this.invitorId;
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>SecurityCodeImpl</code> class it will simply delegate the call up there.
     *
     * @see SecurityCode#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Set the ID of the user that invited the new user
     *
     * @param invitorId
     *            the ID of the inviter
     */
    public void setInvitorId(Long invitorId) {
        this.invitorId = invitorId;
    }
}