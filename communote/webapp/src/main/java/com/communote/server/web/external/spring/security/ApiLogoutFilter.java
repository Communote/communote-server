package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

/**
 * The ApiLogoutFilter handles the logout of users of the REST-API.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ApiLogoutFilter extends CommunoteLogoutFilter {

    /**
     * Instantiates a new api logout filter.
     * 
     * @param handlers
     *            the handlers
     */
    public ApiLogoutFilter(LogoutHandler[] handlers) {
        super(new SimpleUrlLogoutSuccessHandler() {
            @Override
            protected void handle(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                response.getWriter().write(
                        "{\"message\":\"User logged out successfully.\",\"status\":\"OK\"}");
                super.handle(request, response, authentication);
            }
        }, handlers);
    }
}
