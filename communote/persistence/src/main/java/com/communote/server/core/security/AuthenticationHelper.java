package com.communote.server.core.security;

import javax.servlet.ServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationDetailsSourceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;

/**
 * Provides functionality to login a user not using the web frontend e.g. for a test. should only be
 * used rarely.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class AuthenticationHelper {

    /** The user role of the anonymous user */
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

    /** The user role of the public user */
    public static final String PUBLIC_USER_ROLE = "ROLE_PUBLIC_USER";

    /** The user role of the public user */
    public static final String ROLE_INTERNAL_SYSTEM = "ROLE_INTERNAL_SYSTEM";

    /** The user role of the user */
    public static final String KENMEI_USER_ROLE = UserRole.ROLE_KENMEI_USER.getValue();

    /**
     * Create an authentication for the user that can be used to be set in the security context
     *
     * @param user
     *            the user
     * @return the authentication
     */
    public static Authentication createAuthentication(User user) {

        org.springframework.security.core.userdetails.User authenticatedUser = new UserDetails(
                user, user.getAlias());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authenticatedUser, authenticatedUser.getPassword(),
                authenticatedUser.getAuthorities());
        authentication.setDetails(authenticatedUser);

        return authentication;

    }

    /**
     * Remove the authentication from the security context.
     *
     * @see AuthenticationHelper#setAuthentication(Authentication)
     */
    public static void removeAuthentication() {
        setAuthentication(null);
    }

    /**
     * Set the provided user as the currently authenticated user for the current thread. The
     * authentication object that is replaced with that of the provided user will be returned.
     * <p>
     * The caller is responsible for restoring the authentication object that was previously set for
     * the current thread.
     * </p>
     * <p>
     * NOTE: be careful when using this method from threads that originate from servlet requests as
     * in this context the authentication is shared among all threads of the current HTTP session.
     * </p>
     *
     * @param user
     *            the user to set as authenticated
     * @return the replaced authentication object, can be null
     */
    public static Authentication setAsAuthenticatedUser(User user) {
        Authentication newAuth = createAuthentication(user);
        return setAuthentication(newAuth);
    }

    /**
     * Set the provided authentication object as the currently authenticated principal for the
     * current thread. The authentication object that is replaced with the provided one will be
     * returned.
     * <p>
     * The caller is responsible for restoring the authentication object that was previously set for
     * the current thread.
     * </p>
     * <p>
     * NOTE: be careful when using this method from threads that originate from servlet requests as
     * in this context the authentication is shared among all threads of the current HTTP session.
     * </p>
     *
     *
     * @param newAuth
     *            the new authentication object, can be null to clear the authentication
     * @return the replaced authentication object, can be null
     */
    public static Authentication setAuthentication(Authentication newAuth) {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            if (newAuth == null) {
                return null;
            }
            context = SecurityContextHolder.createEmptyContext();
            SecurityContextHolder.setContext(context);
        }
        Authentication currentAuth = context.getAuthentication();
        context.setAuthentication(newAuth);
        return currentAuth;
    }

    /**
     * Login as the internal system user.
     * <p>
     * Since Communote uses the SecurityContextPersistenceFilter the SecurityContext is shared
     * between all threads of the current session. Therefore the internal system user is not just
     * set in the existing security context, instead a new security context containing the internal
     * system user is exposed to the SecurityContextHolder. The calling code has to ensure that it
     * resets the previously contained SecurityContext after completing the operation which should
     * have been run as internal system user. The recommended pattern is to use a try-finally block.
     * </p>
     *
     * @return the current security context
     * @see #setSecurityContext(SecurityContext)
     */
    public static SecurityContext setInternalSystemToSecurityContext() {
        SecurityContext current = SecurityContextHolder.getContext();

        SecurityContext internalSystemContext = SecurityContextHolder.createEmptyContext();
        org.springframework.security.core.userdetails.User user = new InternalSystemUserDetails();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities());
        internalSystemContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(internalSystemContext);
        return current;
    }

    /**
     * Set the public user as authenticated user to the current SecurityContext. If the
     * SecurityContext is shared between all threads of the current session.
     *
     * @param request
     *            the servlet request
     */
    public static void setPublicUserToSecurityContext(ServletRequest request) {
        org.springframework.security.core.userdetails.User user = new PublicUserDetails();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities());
        AuthenticationDetailsSource<Object, Object> authenticationDetailsSource = new AuthenticationDetailsSourceImpl();
        authentication.setDetails(authenticationDetailsSource.buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Set the given SecurityContext. Should be called after an operation that is run after calling
     * {@link #setInternalSystemToSecurityContext()} completed.
     *
     * @param securityContext
     *            The security context to restore
     */
    public static void setSecurityContext(SecurityContext securityContext) {
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Do not construct me
     */
    private AuthenticationHelper() {
        // Do nothing.
    }

}