package com.communote.server.web.commons.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.rest.CommunoteRestletServletAdapter;
import com.communote.server.core.rest.RestletApplicationManager;
import com.communote.server.web.commons.filter.CommunoteRestletForwardFilter;

/**
 * Servlet for triggering the rest-api servlet adapter.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteRestletServlet extends HttpServlet {

    /**
     * Context of servlet.
     */
    private ServletContext context;

    /**
     * Serial version identifier
     */
    private static final long serialVersionUID = -7631364486021085851L;

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

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String version = (String) request
                .getAttribute(CommunoteRestletForwardFilter.ATTRIBUTE_NAME_REST_API_VERSION);

        CommunoteRestletServletAdapter adapter = getRestletApplicationManager().getServletAdapter(
                version, context);

        if (adapter == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "ServletAdapter for version " + version + " is not initialized.");
        }

        if (adapter.getNext() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find API version "
                    + version + ".");
        }

        request.removeAttribute(CommunoteRestletForwardFilter.ATTRIBUTE_NAME_REST_API_VERSION);
        adapter.service(request, response);

    }
}