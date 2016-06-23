package com.communote.server.web.commons.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.rest.CommunoteRestletServletAdapter;
import com.communote.server.core.rest.RestletApplicationManager;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.user.UserRole;
import com.communote.server.web.commons.filter.CommunoteRestletForwardFilter;

/**
 * Servlet for triggering the rest-api servlet adapter. This servlet expects an authenticated
 * CRAWL_USER and only allows GET requests. The rest-api resources will be called as internal system
 * user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CommunoteRestletCrawlerServlet extends HttpServlet {

    /**
     * Context of servlet.
     */
    private ServletContext context;

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String version = (String) request
                .getAttribute(CommunoteRestletForwardFilter.ATTRIBUTE_NAME_REST_API_VERSION);

        CommunoteRestletServletAdapter adapter = getRestletApplicationManager().getServletAdapter(
                version, context);

        if (adapter == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "ServletAdapter for version " + version + " is not initialized.");
        }
        request.removeAttribute(CommunoteRestletForwardFilter.ATTRIBUTE_NAME_REST_API_VERSION);
        if (SecurityHelper.hasRole(UserRole.ROLE_CRAWL_USER)) {
            // invoke restlet resource as system user
            SecurityContext curContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            try {
                adapter.service(request, response);
            } finally {
                AuthenticationHelper.setSecurityContext(curContext);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only a crawl-user is allowed to use this API");
        }
    }

    /**
     * Get the application manager for rest-api
     *
     * @return {@link RestletApplicationManager}
     */
    private RestletApplicationManager<?> getRestletApplicationManager() {
        return ServiceLocator.instance().getService(RestletApplicationManager.class);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        context = getServletContext();
    }

}
