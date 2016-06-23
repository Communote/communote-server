package com.communote.server.web.commons.resolver;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.persistence.user.client.ClientUrlHelper;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.helper.JsonRequestHelper;

/**
 * Exception resolver that handles the {@link MaxUploadSizeExceededException} occurring during
 * multipart request resolution. The handler provides a localized error message to the view which
 * can be defined using request-mappings. The error message is available under the attribute
 * {@link MessageHelper#ERROR_MESSAGES_KEY}. If there is no mapping for a request it will be
 * interpreted as an AJAX request and the response will be a JSON object holding the error message.
 * If the requests Accept header does not include application/json the JSON response should be
 * bundled in HTML. This must be handled by the view provided with
 * {@link #setAjaxResponseView(String)}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MaxUploadSizeExceededExceptionResolver implements HandlerExceptionResolver {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MaxUploadSizeExceededExceptionResolver.class);
    private Map<String, String> requestMappings;
    private String humanReadableUploadSizeLimit;

    /**
     * Creates a human readable string for the attachment upload size limit.
     *
     * @return the human readable string of the max upload size
     */
    private String getHumanReadableUploadSizeLimit() {
        if (humanReadableUploadSizeLimit == null) {
            long fileUploadLimit = Long.parseLong(CommunoteRuntime.getInstance()
                    .getConfigurationManager().getApplicationConfigurationProperties()
                    .getProperty(ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE));
            humanReadableUploadSizeLimit = FileUtils.byteCountToDisplaySize(fileUploadLimit);
        }
        return humanReadableUploadSizeLimit;
    }

    /**
     * Creates a model and view to transport the error message
     *
     * @param request
     *            the current HTTP request
     * @param response
     *            the current HTTP response
     * @param view
     *            the name of the view to use, can be null if the request should be handled as Ajax
     *            upload or there is no mapping for the request and the default non-ajax upload view
     *            should be used
     * @return the created model and view or null if the response was written as JSON
     */
    private ModelAndView prepareAjaxModelAndView(HttpServletRequest request,
            HttpServletResponse response, String errorMessage) {
        ObjectNode jsonResponse = JsonRequestHelper.createJsonErrorResponse(errorMessage);
        try {
            // Note: the JSON cannot be written directly to the response since this is not supported
            // by exception resolvers. Main problem is that some headers like encoding won't be set
            // correctly and response is written twice
            return ControllerHelper.prepareModelAndViewForJsonResponse(request, response,
                    jsonResponse, false);
        } catch (IOException e) {
            // should not occur because response is not written directly
            LOGGER.error("Unexpected exception handling MaxUploadSizeExceededException", e);
            return null;
        }
    }

    /**
     * Implementation of the exception resolving method which will only handle the
     * {@link MaxUploadSizeExceededException}.
     *
     * @param request
     *            the current HTTP request
     * @param response
     *            the response
     * @param handler
     *            the executed handler, which is null in case of an MaxUploadSizeExceededException
     * @param ex
     *            the exception that got thrown
     * @return the model and view or null if the exception is not a MaxUploadSizeExceededException
     *         or no view is defined
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        if (ex instanceof MaxUploadSizeExceededException) {
            String errorMessage = MessageHelper.getText(request,
                    "error.blogpost.upload.filesize.limit",
                    new Object[] { getHumanReadableUploadSizeLimit() });
            String targetPath = ClientUrlHelper.removeIds(request.getServletPath()
                    + request.getPathInfo());
            String view = null;
            if (requestMappings != null) {
                view = requestMappings.get(targetPath);
            }
            if (view != null) {
                MessageHelper.saveErrorMessage(request, errorMessage);
                MessageHelper.setMessageTarget(request, targetPath);
                return new ModelAndView(ControllerHelper.replaceModuleInViewName(view));
            }
            return prepareAjaxModelAndView(request, response, errorMessage);
        }
        return null;
    }

    /**
     * Define the resources which should not be handled as AJAX Uploads but instead should respond
     * with a custom view.
     *
     * @param requestMappings
     *            a mapping from resource URI starting with a / after the client part to view name
     */
    public void setNonAjaxUploadRequestMappings(Map<String, String> requestMappings) {
        this.requestMappings = requestMappings;
    }
}
