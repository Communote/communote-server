package com.communote.server.web.commons.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.security.iprange.InvalidIpAddressException;
import com.communote.server.core.security.iprange.IpRangeFilterManagement;
import com.communote.server.model.security.ChannelType;
import com.communote.server.persistence.security.ChannelTypeEnum;
import com.communote.server.web.commons.MessageHelper;

/**
 * IP range channel filter
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeChannelFilter implements Filter, InitializingBean {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(IpRangeChannelFilter.class);

    private ChannelType channelType;

    private String errorPage;
    // error code to return
    private int httpErrorCode;
    // error message to return when sending the error code
    private String httpErrorMessageKey;

    private IpRangeFilterManagement ipRangeFilterManagement;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(httpErrorMessageKey, "httpErrorMessageKey must be set.");
        Assert.notNull(httpErrorCode, "httpErrorCode must be set.");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // nothing to do

    }

    /**
     * set channel type to {@link ClientAndChannelContextHolder} check ip {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        ClientAndChannelContextHolder.setChannel(channelType);
        String remoteAddr = request.getRemoteAddr();
        try {
            boolean isInRange = getIpRangeFilterManagement().isInRange(remoteAddr, channelType);
            if (isInRange) {
                filterChain.doFilter(request, response);
            } else {
                handleIpBlocked((HttpServletRequest) request, (HttpServletResponse) response,
                        remoteAddr);
            }
        } catch (InvalidIpAddressException e) {
            throw new ServletException("IP: " + remoteAddr, e);
        }
    }

    /**
     * Gets the channel name.
     *
     * @return channel name
     */
    public String getChannelName() {
        return channelType.getValue();
    }

    /**
     * Returns the IP-range filter management.
     *
     * @return the IP-range filter management
     */
    private IpRangeFilterManagement getIpRangeFilterManagement() {
        if (ipRangeFilterManagement == null) {
            ipRangeFilterManagement = ServiceLocator.instance().getService(
                    IpRangeFilterManagement.class);
        }

        return ipRangeFilterManagement;
    }

    /**
     * If an errorPage is defined a forward will occur and the the messages to be displayed will be
     * stored in the request as error messages. If the errorPage is undefined the httpErrorCode will
     * be sent and the message identified by httpErrorMessageKey will be set.
     *
     * @param request
     *            the blocked request
     * @param response
     *            the response to return
     * @param remoteAddr
     *            the blocked IP address
     * @throws ServletException
     *             when the forward fails
     * @throws IOException
     *             when writing the response fails
     */
    private void handleIpBlocked(HttpServletRequest request, HttpServletResponse response,
            String remoteAddr) throws ServletException, IOException {
        LOGGER.info("Access denied for blocked IP: " + remoteAddr);
        if (this.errorPage != null) {
            MessageHelper.saveErrorMessage(request, MessageHelper.getText(request,
                    "blog.portal.ip.blocked.message", new Object[] { remoteAddr }));
            String supportEmail = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties()
                    .getProperty(ClientProperty.SUPPORT_EMAIL_ADDRESS);
            String link = "<a href=\"mailto:" + supportEmail + "\">" + supportEmail + "</a>";
            MessageHelper.saveErrorMessage(request, MessageHelper.getText(request,
                    "blog.portal.ip.blocked.support", new Object[] { link }));
            // set status code and not sending error to avoid forward to application-wide
            // error-page
            response.setStatus(httpErrorCode);
            RequestDispatcher rd = request.getRequestDispatcher(response.encodeURL(this.errorPage));
            rd.forward(request, response);
        }
        if (!response.isCommitted()) {
            response.sendError(httpErrorCode, MessageHelper.getText(request, httpErrorMessageKey,
                    new Object[] { remoteAddr }));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    /**
     * Sets channel type.
     *
     * @param channelName
     *            name of channel
     */
    public void setChannelName(String channelName) {
        this.channelType = ChannelTypeEnum.fromString(channelName);
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

    /**
     * Sets the HTTP error code to be returned when the IP is blocked.
     *
     * @param code
     *            the code, must be convertible to an integer
     */
    public void setHttpErrorCode(String code) {
        try {
            httpErrorCode = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("HttpErrorCode must be an integer.");
        }
    }

    /**
     * Sets the HTTP error message to be returned alongside the HTTP error code when the IP is
     * blocked. The message can contain a place-holder which will be replaced with the blocked IP.
     *
     * @param errorMsgKey
     *            the message key
     */
    public void setHttpErrorMessageKey(String errorMsgKey) {
        httpErrorMessageKey = errorMsgKey;
    }

}
