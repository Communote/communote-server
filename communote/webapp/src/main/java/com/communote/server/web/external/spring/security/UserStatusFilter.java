package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.InternalSystemUserDetails;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserProfileDetails;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.model.user.UserStatus;

/**
 * Tests whether the status of a user allows a log in. This filter evaluates the principal of an
 * authentication stored in the SecurityContext and thus must be chained after a SecurityContext
 * populating filter.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserStatusFilter implements Filter {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserStatusFilter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // nothing to be done
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof InternalSystemUserDetails) {
                AuthenticationHelper.removeAuthentication();
                LOGGER.warn("Removed internal system authentication of a WEB serving thread!");

            } else if (principal instanceof UserDetails && !SecurityHelper.isPublicUser()) {

                UserProfileDetails userProfileDetails = null;
                Long userId = ((UserDetails) principal).getUserId();

                if (userId != null) {
                    userProfileDetails = ServiceLocator.findService(UserProfileManagement.class)
                            .getUserProfileDetailsById(userId, false);
                }

                if (userProfileDetails == null
                        || !UserStatus.ACTIVE.equals(userProfileDetails.getUserStatus())) {
                    // user not existing or is not active invalidate authentication in
                    // SecurityContext
                    AuthenticationHelper.removeAuthentication();
                    LOGGER.debug(
                            "Removed authentication of user {} with status {}  from Security context.",
                            userId, userProfileDetails);
                }
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            // security check. If there is a internal system user left over, clean it.
            authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.getPrincipal() instanceof InternalSystemUserDetails) {
                AuthenticationHelper.removeAuthentication();
                LOGGER.warn("This thread left an internal system user authentication uncleaned!"
                        + " ThreadName: " + Thread.currentThread().getName()
                        + " Causing Request URI: " + ((HttpServletRequest) request).getRequestURI());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // nothing to be done
    }
}
