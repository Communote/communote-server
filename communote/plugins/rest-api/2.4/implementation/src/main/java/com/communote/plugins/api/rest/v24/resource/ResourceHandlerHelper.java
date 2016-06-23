package com.communote.plugins.api.rest.v24.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Request;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v24.filter.FilterRestApiParameterNameProvider;
import com.communote.plugins.api.rest.v24.request.RequestHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * The abstract ResourceHandler supplies general methods for REST-API used by the concrete resource
 * handlers.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceHandlerHelper {

    /**
     * Possible Name providers
     */
    private enum NAME_PROVIDERS {
        RESTAPI(FilterRestApiParameterNameProvider.INSTANCE), WIDGET(
                FilterWidgetParameterNameProvider.INSTANCE);

        private final QueryParametersParameterNameProvider nameProvider;

        /**
         * @param nameProvider
         *            The name provider.
         */
        private NAME_PROVIDERS(QueryParametersParameterNameProvider nameProvider) {
            this.nameProvider = nameProvider;
        }

        /**
         * @return the nameProvider
         */
        public QueryParametersParameterNameProvider getNameProvider() {
            return nameProvider;
        }
    }

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHandlerHelper.class);

    private static ResourceHandlerHelper INSTANCE = new ResourceHandlerHelper();

    /** Name for the attachments attribute within the session . */
    public static final String PARAM_ATTACHMENTS_SESSION_ID = "attachmentUploadSessionId";

    /**
     * Get meta data for paging
     * 
     * @param pageOffset
     *            offset of page
     * @param pageMaxCount
     *            max cound of elements in page
     * @param numberOfElements
     *            number of found elements
     * @return map of metaData components for paging
     */
    public static Map<String, Object> generateMetaDataForPaging(Integer pageOffset,
            Integer pageMaxCount,
            int numberOfElements) {
        Map<String, Object> metaData = new HashMap<String, Object>();
        int offset = (pageOffset != null) ? pageOffset : 0;
        int maxCount = (pageMaxCount != null) ? pageMaxCount : 10;
        Boolean moreElementsAvailable = (offset + maxCount < numberOfElements) ? true : false;
        metaData.put("moreElementsAvailable", moreElementsAvailable);
        metaData.put("numberOfElements", numberOfElements);
        return metaData;
    }

    /**
     * Get the locale of the current user
     * 
     * @param request
     *            {@link Request}
     * @return {@link Locale}
     */
    public static Locale getCurrentUserLocale(Request request) {
        HttpServletRequest httpServletRequest = RequestHelper.getHttpServletRequest(request);
        return SessionHandler.instance().getCurrentLocale(httpServletRequest);
    }

    /**
     * Returns the name provider to use.
     * 
     * @param request
     *            The request
     * @return The {@link QueryParametersParameterNameProvider} for this request, default is
     *         {@link FilterRestApiParameterNameProvider}.
     */
    public static QueryParametersParameterNameProvider getNameProvider(Request request) {
        String parameter = RequestHelper.getHttpServletRequest(request)
                .getParameter("nameProvider");
        if (StringUtils.isNotBlank(parameter)) {
            try {
                return NAME_PROVIDERS.valueOf(parameter.toUpperCase()).getNameProvider();
            } catch (Exception e) {
                LOGGER.warn("Tried to load a non existing name provider: " + parameter);
            }
        }
        return NAME_PROVIDERS.RESTAPI.getNameProvider();
    }

    /**
     * Returns a set with attachment IDs uploaded in the current session. The list stored in the
     * session is identified by the value of the request parameter
     * {@link #PARAM_ATTACHMENTS_SESSION_ID}. If the parameter is not provided null is returned. If
     * the list is not yet in the session it will be put there.
     * 
     * @param request
     *            {@link Request}
     * @param attribute
     *            The attribute to use. If null, the attribute will be extracted from the request
     *            using {@link #PARAM_ATTACHMENTS_SESSION_ID}
     * @return the set with attachment IDs or null if the identifier is not one of the request
     *         parameters
     */
    @SuppressWarnings("unchecked")
    public static Set<Long> getUploadedAttachmentsFromSession(Request request,
            String attribute) {

        HttpServletRequest httpServletRequest = RequestHelper.getHttpServletRequest(request);

        if (attribute == null) {
            attribute = httpServletRequest.getParameter(PARAM_ATTACHMENTS_SESSION_ID);
        }
        Set<Long> attachmentIds = null;
        if (attribute != null) {
            HttpSession session = httpServletRequest.getSession(true);
            attachmentIds = (Set<Long>) session.getAttribute(attribute);
            if (attachmentIds == null) {
                attachmentIds = new HashSet<Long>();
                session.setAttribute(attribute, attachmentIds);
            }

        }
        return attachmentIds;
    }

    /**
     * @return instance of the ResourceHandlerHalper
     */
    public static ResourceHandlerHelper instance() {
        return INSTANCE;
    }

    /**
     * Removes the attachment IDs set from the current session.
     * 
     * @param request
     *            {@link Request}
     * @param attribute
     *            The attribute to use. If null, the attribute will be extracted from the request
     *            using {@link #PARAM_ATTACHMENTS_SESSION_ID}
     */
    public static void removeUploadedAttachmentsFromSession(Request request,
            String attribute) {

        HttpServletRequest httpServletRequest = RequestHelper.getHttpServletRequest(request);
        if (attribute == null) {
            attribute = httpServletRequest.getParameter(PARAM_ATTACHMENTS_SESSION_ID);
        }
        if (attribute != null) {
            if (httpServletRequest.getSession(false) != null) {
                httpServletRequest.getSession().removeAttribute(attribute);
            }
        }
    }

    /**
     * Get text of message key with consideration of locale and replace arguments
     * 
     * @param <T>
     *            type of resource
     * @param request
     *            {@link Request}
     * @param messageKey
     *            key of message
     * @param arguments
     *            elements witch should replace in message text
     * @return text of messageKey
     */
    protected <T> String getText(Request request, String messageKey, Object... arguments) {
        return ResourceBundleManager.instance().getText(messageKey, getCurrentUserLocale(request),
                arguments);
    }
}
