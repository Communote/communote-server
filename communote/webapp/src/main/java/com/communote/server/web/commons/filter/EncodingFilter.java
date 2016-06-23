package com.communote.server.web.commons.filter;

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

/**
 * Filter to set the encoding on the request and response
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EncodingFilter implements Filter {

    private final static Logger LOG = LoggerFactory.getLogger(EncodingFilter.class);

    private final static String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    private String encoding;

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain next)
            throws IOException, ServletException {
        try {
            // Respect the client-specified character encoding
            // (see HTTP specification section 3.4.1)
            /*
             * if (null == request.getCharacterEncoding()) { request.setCharacterEncoding(encoding);
             * response.setCharacterEncoding(encoding); }
             */

            request.setCharacterEncoding(encoding);
            // need to set for json returns
            response.setCharacterEncoding(encoding);
            next.doFilter(request, response);
        } catch (RuntimeException e) {
            LOG.error(
                    "Error doing request for uri: "
                            + ((HttpServletRequest) request).getRequestURI() + " " + e.getMessage(),
                            e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        encoding = config.getInitParameter("encoding");

        if (encoding == null) {
            encoding = DEFAULT_CHARACTER_ENCODING;
        }
    }

}
