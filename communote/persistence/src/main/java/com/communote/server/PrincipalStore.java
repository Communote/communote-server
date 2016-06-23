package com.communote.server;

/**
 * Stores the currently logged in Principal. The principal is passed from another tier of the
 * application (i.e. the web application).
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class PrincipalStore {
    private static final ThreadLocal<java.security.Principal> STORE = new ThreadLocal<java.security.Principal>();

    /**
     * Get the user <code>principal</code> for the currently executing thread.
     *
     * @return the current principal.
     */
    public static java.security.Principal get() {
        return STORE.get();
    }

    /**
     * Set the <code>principal</code> for the currently executing thread.
     *
     * @param principal
     *            the user principal
     */
    public static void set(final java.security.Principal principal) {
        STORE.set(principal);
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private PrincipalStore() {
        // Do nothing
    }
}
