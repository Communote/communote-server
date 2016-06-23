package com.communote.server.api.core.security.permission;

import java.io.Serializable;

/**
 * A permission which is identified by an unique identifier.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <T>
 *            Type of the entity the permission can be used for.
 */
public class Permission<T> implements Serializable {

    private static final long serialVersionUID = -37647063290190402L;

    private final String identifier;

    /**
     * Constructor.
     *
     * @param identifier
     *            This should be a unique identifier for this permission.
     */
    public Permission(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Permission<?> other = (Permission<?>) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        return true;
    }

    /**
     * @return the unique identifier of this permission
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Permission [identifier=" + identifier + "]";
    }

}
