package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.user.UserRole;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Handler for the AccessDeniedException which forwards the request to a configurable error page or
 * sends a 403.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteAccessDeniedHandler implements AccessDeniedHandler {

    /** The Constant ACEGI_SECURITY_ACCESS_DENIED_EXCEPTION_KEY. */
    public static final String ACEGI_SECURITY_ACCESS_DENIED_EXCEPTION_KEY = "ACEGI_SECURITY_403_EXCEPTION";

    /** The Constant logger. */
    protected static final Logger LOG = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);

    /** The error page. */
    private String errorPage;

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        boolean isUser = SecurityHelper.hasRole(UserRole.ROLE_KENMEI_USER);

        // only forward for normal users but not for system or crawl users because those are not
        // expected to use the portal (web interface)
        if (isUser && errorPage != null) {
            // Put exception into request scope (perhaps of use to a view)
            request.setAttribute(
                    ACEGI_SECURITY_ACCESS_DENIED_EXCEPTION_KEY, accessDeniedException);

            // Perform RequestDispatcher "forward"
            ControllerHelper.forwardRequest(request, response, errorPage);
        }

        if (!response.isCommitted()) {
            // Send 403 (we do this after response has been written)
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    accessDeniedException.getMessage());
        }
    }

    /**
     * The error page to use. Must begin with a "/" and is interpreted relative to the current
     * context root.
     *
     * @param errorPage
     *            the dispatcher path to display
     * @throws IllegalArgumentException
     *             if the argument doesn't comply with the above limitations
     */
    public void setErrorPage(String errorPage) {
        if (errorPage != null && !errorPage.startsWith("/")) {
            throw new IllegalArgumentException("ErrorPage must begin with '/'");
        }

        this.errorPage = errorPage;
    }

}
