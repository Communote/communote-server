package com.communote.server.web.api.service.external;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.api.to.ApiResult.ResultStatus;

/**
 * This controller enables the widget to send the save action.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteExternalObjectsApiController extends BaseRestApiController {

    /**
     * Save the settings. {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws HttpRequestMethodNotSupportedException,
            ApiException {
        Long internalExternalObjectId = getResourceId(request, true);

        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        String message;
        try {
            getExternalObjectManagement().removeExternalObject(internalExternalObjectId);
        } catch (BlogAccessException e) {
            message = ResourceBundleManager.instance().getText(
                    "blog.error.access.required." + e.getRequiredRole() == null ? "none"
                            : e.getRequiredRole().getValue(), locale);
            apiResult.setMessage(message);
            apiResult.setStatus(ResultStatus.ERROR.name());
        } catch (BlogNotFoundException e) {
            message = e.getLocalizedMessage();
            apiResult.setMessage(message);
            apiResult.setStatus(ResultStatus.ERROR.name());
        } catch (NotFoundException e) {
            message = e.getLocalizedMessage();
            apiResult.setMessage(message);
            apiResult.setStatus(ResultStatus.ERROR.name());
        }
        // Success
        message = ResourceBundleManager.instance().getText(
                "blog.external.delete.external.object.success", locale);

        apiResult.setMessage(message);
        return null;
    }

    /**
     * @return the external object management
     */
    private ExternalObjectManagement getExternalObjectManagement() {
        return ServiceLocator.findService(ExternalObjectManagement.class);
    }
}
