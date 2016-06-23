package com.communote.server.web.api.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.mvc.BaseCommandController;

import com.communote.common.util.VersionComparator;
import com.communote.server.persistence.user.client.ClientUrlHelper;


/**
 * BaseApiController is the base class for all API calls. It does the error handling and conversion
 * to json or other formats. If inheriting from this controller overwrite
 * {@link #execute(HttpServletRequest, HttpServletResponse)}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead
 */
@Deprecated
public abstract class BaseApiController extends BaseCommandController {
    private final static Logger LOG = Logger.getLogger(BaseApiController.class);

    /**
     * Compares the version. If compareVersion(request, 1.0.1) returns a value >=0 than the request
     * supports this version.
     * 
     * @param request
     *            the request to get the requested version from
     * @param version
     *            the version to compare (e.g. 1.0.1)
     * @return a value greater 0 if the given version is lower than the one out of the request<br>
     *         0 if the given version is equal to the one out of the request<br>
     *         a value less 0 if the given version is greater than the one out of the request<br>
     */
    protected int compareVersions(HttpServletRequest request, String version) {
        String requestedVersion = getVersion(request);

        VersionComparator versionComparator = new VersionComparator();
        return versionComparator.compare(requestedVersion,
                version);
    }

    /**
     * @param request
     *            the http request
     * @param parameter
     *            the parameter to get
     * @return the boolean
     * @throws IllegalRequestParameterException
     *             in case a request parameter has an illegal value
     */
    protected Boolean getBooleanParameter(HttpServletRequest request, String parameter)
            throws IllegalRequestParameterException {
        String value = request.getParameter(parameter);
        try {
            return Boolean.valueOf(value);
        } catch (Exception e) {
            throw new IllegalRequestParameterException(parameter, value,
                    "Parameter must be a Long!");
        }
    }

    /**
     * @param request
     *            the http request
     * @param parameter
     *            the parameter to get
     * @return the integer
     * @throws IllegalRequestParameterException
     *             in case a request parameter has an illegal value
     */
    protected Integer getIntegerParameter(HttpServletRequest request, String parameter)
            throws IllegalRequestParameterException {
        String value = request.getParameter(parameter);
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            throw new IllegalRequestParameterException(parameter, value,
                    "Parameter must be an Integer!");
        }
    }

    /**
     * @param request
     *            the http request
     * @param parameter
     *            the parameter to get
     * @return the long
     * @throws IllegalRequestParameterException
     *             in case a request parameter has an illegal value
     */
    protected Long getLongParameter(HttpServletRequest request, String parameter)
            throws IllegalRequestParameterException {
        String value = request.getParameter(parameter);
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            throw new IllegalRequestParameterException(parameter, value,
                    "Parameter must be a Long!");
        }
    }

    /**
     * @param request
     *            the http request
     * @param parameter
     *            the parameter to get
     * @return the parameter value
     * @throws IllegalRequestParameterException
     *             in case a request parameter has an illegal value
     */
    protected String getNonEmptyParameter(HttpServletRequest request, String parameter)
            throws IllegalRequestParameterException {
        String value = request.getParameter(parameter);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalRequestParameterException(parameter, value,
                    "Parameter cannot be null or empty!");
        }
        return value;
    }

    /**
     * Get the resource id out of the request uri. E.g. if the uri is
     * <code>/api/v1.0/posts/12.json?filterHtml=true</code> the method will return 12
     * 
     * @param request
     *            the request
     * @param throwOnError
     *            if true a {@link RequestedResourceNotFoundException} will be thrown if the id is
     *            not parseable
     * @return the id of the requested resource
     * @throws RequestedResourceNotFoundException
     *             in case the uri is not parseable to a resource id
     */
    protected Long getResourceId(HttpServletRequest request, boolean throwOnError)
            throws RequestedResourceNotFoundException {
        Long resourceId = null;
        String lastSegment = null;
        try {
            String[] segments = request.getRequestURI().split("/");
            if (segments == null || segments.length == 0) {
                return null;
            }
            lastSegment = segments[segments.length - 1];
            int extenstionPoint = lastSegment.indexOf(".");
            resourceId = null;
            if (extenstionPoint > -1) {
                lastSegment = lastSegment.substring(0, extenstionPoint);
            }
            resourceId = Long.valueOf(lastSegment);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error parsing long out of request uri! request.uri="
                        + request.getRequestURI() + " Error: " + e.getMessage());
            }
            if (throwOnError) {
                throw new RequestedResourceNotFoundException(getResourceType(), lastSegment,
                        "Request Uri not parseable to Long");
            }
        }
        return resourceId;
    }

    /**
     * @return the resource type, use for {@link #getResourceId(HttpServletRequest, boolean)}
     *         exceptions
     */
    protected String getResourceType() {
        return "resource";
    }

    /**
     * @param request
     *            the request to use
     * @return the version in the form "1.0.1"
     */
    private String getVersion(HttpServletRequest request) {
        String uri = request.getServletPath() + request.getPathInfo();
        uri = uri.replaceAll("/+", "/");
        String uri2 = ClientUrlHelper.removeIds(uri);
        /**
         * split will result from '/api/v1.0.1/filter/posts.json' to '[, api, v1.0.1, filter,
         * posts.json]'
         */
        String[] segments = uri2.split("/");
        String version = segments[2];
        return version.substring(1);
    }
}
