package com.communote.server.web.commons.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.access.channel.RetryWithHttpsEntryPoint;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.security.ssl.ChannelManagement;

/**
 * Filter for forcing SSL if enabled.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ForceSslChannelFilter implements Filter, InitializingBean {

    /** Custom PortMapper for mapping ports out of the config. */
    private class ForceSslPortMapper implements PortMapper {

        private final PortMapper fallbackMapper;

        /**
         * @param fallbackMapper
         *            Fallback, if no setting available.
         */
        public ForceSslPortMapper(PortMapper fallbackMapper) {
            if (fallbackMapper == null) {
                fallbackMapper = new PortMapperImpl();
            }
            this.fallbackMapper = fallbackMapper;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer lookupHttpPort(Integer httpsPort) {
            String value = ApplicationProperty.WEB_HTTP_PORT.getValue();
            int httpPort = NumberUtils.toInt(value, -1);
            if (httpPort != -1) {
                return httpPort;
            }
            return fallbackMapper.lookupHttpPort(httpsPort);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer lookupHttpsPort(Integer httpPort) {
            String value = ApplicationProperty.WEB_HTTPS_PORT.getValue();
            int httpsPort = NumberUtils.toInt(value, -1);
            if (httpsPort != -1) {
                return httpsPort;
            }
            return fallbackMapper.lookupHttpsPort(httpPort);
        }

    }

    /** The http protocol name. */
    public final static String PROTOCOL_HTTP = "http";

    /** The https protocol name. */
    public final static String PROTOCOL_HTTPS = "https";

    private final static SessionHandler SESSION_HANDLER = SessionHandler.instance();

    /** The channel management instance. */
    private ChannelManagement channelManagement;

    private final RetryWithHttpsEntryPoint entryPointHttps = new RetryWithHttpsEntryPoint();

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        ForceSslPortMapper portMapper = new ForceSslPortMapper(null);
        PortResolverImpl portResolver = new PortResolverImpl();
        portResolver.setPortMapper(portMapper);
        entryPointHttps.setPortMapper(portMapper);
        entryPointHttps.setPortResolver(portResolver);
    }

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    /**
     * @param request
     *            The servlet requst.
     * @param response
     *            The servlet response.
     * @param filterChain
     *            The filter chain object.
     * @throws IOException
     *             The IOExeption
     * @throws ServletException
     *             The ServletExeption
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        boolean secureRequest = httpServletRequest.isSecure();

        Boolean firstRequestedWasSecure = SESSION_HANDLER
                .getFirstRequestedWasSecure(httpServletRequest);
        if (firstRequestedWasSecure == null) {
            SESSION_HANDLER.setFirstRequestedWasSecure(httpServletRequest, secureRequest);
        }

        boolean isForceSsl = getChannelManagement().isForceSsl();

        if (isForceSsl && !secureRequest) {
            // redirect using the secure channel 'https'
            entryPointHttps.commence(httpServletRequest, httpServletResponse);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * @return the channel management
     */
    private ChannelManagement getChannelManagement() {
        if (channelManagement == null) {
            channelManagement = ServiceLocator.findService(ChannelManagement.class);
        }
        return channelManagement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }
}
