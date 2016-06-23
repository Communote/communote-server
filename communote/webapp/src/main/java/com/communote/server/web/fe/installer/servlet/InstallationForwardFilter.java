package com.communote.server.web.fe.installer.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.StartupProperties;

/**
 * The Class <code>InstallationForwardFilter</code> forwards a requests to the installation wizard
 * if the setup is not completed or else it continues the filter chain.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InstallationForwardFilter implements Filter {

    /** the url of the installer start page */
    private final static String INSTALLER_WIZARD_URL_START = "/microblog/global/installer/welcome.do";

    /** content the filter will ignore in case of the installation process */
    private static final Set<String> INSTALLER_MIMETYPE_PATHS = new HashSet<String>(Arrays.asList(
            "installer/", "styles/", "images/", "javascript/", "favicon.ico"));

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        StartupProperties props = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties();

        if (props.isInstallationDone()) {
            filterChain.doFilter(request, response);
        } else {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String forward = httpRequest.getServletPath()
                    + (httpRequest.getPathInfo() != null ? httpRequest.getPathInfo() : "");

            outer: {
                for (String path : INSTALLER_MIMETYPE_PATHS) {
                    if (StringUtils.containsIgnoreCase(forward, path)) {
                        break outer;
                    }
                }
                httpResponse.sendRedirect(httpResponse.encodeURL(httpRequest.getContextPath()
                        + INSTALLER_WIZARD_URL_START));
                return;
            }
            request.getRequestDispatcher(forward).forward(request, response);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        // Do nothing.
    }
}
