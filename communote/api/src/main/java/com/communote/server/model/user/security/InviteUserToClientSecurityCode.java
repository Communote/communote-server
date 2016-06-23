package com.communote.server.model.user.security;

import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.security.SecurityCodeImpl;
import com.communote.server.model.user.User;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class InviteUserToClientSecurityCode extends SecurityCodeImpl {
    /**
     * Constructs new instances of {@link InviteUserToClientSecurityCode}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link InviteUserToClientSecurityCode}.
         */
        public static InviteUserToClientSecurityCode newInstance() {
            InviteUserToClientSecurityCode code = new InviteUserToClientSecurityCodeImpl();
            code.generateNewCode();
            code.setAction(SecurityCodeAction.INVITE_CLIENT);
            code.setCreatingDate(new java.sql.Timestamp(new java.util.Date().getTime()));
            return code;
        }

        public static InviteUserToClientSecurityCode newInstance(User invitedUser) {
            InviteUserToClientSecurityCode code = newInstance();
            code.setUser(invitedUser);
            return code;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4747999374057774372L;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
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
     * This entity does not have any identifiers but since it extends the
     * <code>SecurityCodeImpl</code> class it will simply delegate the call up there.
     *
     * @see SecurityCode#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

}