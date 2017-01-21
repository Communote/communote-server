package com.communote.server.web.external.spring.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import com.communote.common.util.UriUtils;
import com.communote.server.external.acegi.UserAccountException;
import com.communote.server.external.acegi.UserAccountTemporarilyLockedException;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Redirects the request depending of the occurred authentication failure.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String authenticationFailureUrl;
    private final Map<String, String> failureUrlMap = new HashMap<String, String>();
    private String targetUrlParameter;

    /**
     * Constructor.
     *
     * @param authenticationFailureUrl
     *            The failure url.
     */
    public CommunoteAuthenticationFailureHandler(String authenticationFailureUrl) {
        super(authenticationFailureUrl);
        this.authenticationFailureUrl = authenticationFailureUrl;
    }

    /**
     * Append the target URL parameter if the targetUrlParameter is defined and is set in the
     * current request.
     *
     * @param url
     *            the URL to append the parameter to
     * @param request
     *            the current request
     * @return the new URL
     */
    private String appendTargetUrl(String url, HttpServletRequest request) {
        if (targetUrlParameter != null) {
            String targetUrlParamString = request.getParameter(targetUrlParameter);
            if (targetUrlParamString != null && targetUrlParamString.length() > 0) {
                targetUrlParamString = targetUrlParameter + "="
                        + UriUtils.encodeUriComponent(targetUrlParamString);
                if (url.indexOf('?') < 0) {
                    url += "?" + targetUrlParamString;
                } else {
                    url += "&" + targetUrlParamString;
                }
            }
        }
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authenticationException) throws IOException, ServletException {
        saveException(request, authenticationException);
        String url = failureUrlMap.get(authenticationException.getClass().getName());
        if (url != null) {
            if (authenticationException instanceof UserAccountTemporarilyLockedException) {
                url += "&lockedTimeout="
                        + ((UserAccountTemporarilyLockedException) authenticationException)
                                .getLockedTimeout().getTime();
            }
            if (authenticationException instanceof UserAccountException) {
                url += "&username="
                        + ((UserAccountException) authenticationException).getUsername();
            }
            ControllerHelper.sendInternalRedirect(request, response, appendTargetUrl(url, request));
            return;
        }

        String failureUrl = authenticationFailureUrl;
        String redirectUrl = ControllerHelper.renderAbsoluteUrl(request, null, failureUrl, false,
                false, false);
        new DefaultRedirectStrategy().sendRedirect(request, response,
                appendTargetUrl(redirectUrl, request));
    }

    /**
     * Borrowed from
     * {@link org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler}
     * .
     *
     * Sets the map of exception types (by name) to URLs.
     *
     * @param failureUrlMap
     *            the map keyed by the fully-qualified name of the exception class, with the
     *            corresponding failure URL as the value.
     *
     * @throws IllegalArgumentException
     *             if the entries are not Strings or the URL is not valid.
     */
    public void setExceptionMappings(Map<?, ?> failureUrlMap) {
        this.failureUrlMap.clear();
        for (Map.Entry<?, ?> entry : failureUrlMap.entrySet()) {
            Object exception = entry.getKey();
            Object url = entry.getValue();
            Assert.isInstanceOf(String.class, exception,
                    "Exception key must be a String (the exception classname).");
            Assert.isInstanceOf(String.class, url, "URL must be a String");
            Assert.isTrue(UrlUtils.isValidRedirectUrl((String) url), "Not a valid redirect URL: "
                    + url);
            this.failureUrlMap.put((String) exception, (String) url);
        }
    }

    /**
     * Set the parameter to check and if set expose again when redirecting to one of the failure
     * URLs
     *
     * @param targetUrlParameter
     *            name of the parameter that holds the target URL. The parameter name is expected to
     *            consist only of ASCII alpha-numeric characters.
     */
    public void setTargetUrlParameter(String targetUrlParameter) {
        this.targetUrlParameter = targetUrlParameter;
    }
}
