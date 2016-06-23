package com.communote.server.web.api.service.external.confluence;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.external.ExternalObjectConstants;
import com.communote.server.model.external.ExternalObjectProperty;
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
public class ConfluenceExternalObjectsApiController extends BaseRestApiController {

    private static final String BLOG = "blog";
    private static final String TYPE = "type";
    private static final String TYPE_PROPERTY_KEY_GROUP = "com.communote.plugins.confluence";
    /** Logger. */
    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(ConfluenceExternalObjectsApiController.class);

    /**
     * This method removes the external object from the blog.
     *
     * @param blogId
     *            The blog.
     * @param externalSystemId
     *            The external system id.
     * @param externalObjectId
     *            The external object id.
     * @throws ApiException
     *             Exception.
     */
    private void deleteExternalObject(Long blogId, String externalSystemId, String externalObjectId)
            throws ApiException {
        try {
            ServiceLocator.findService(ExternalObjectManagement.class).removeExternalObject(blogId,
                    externalSystemId, externalObjectId);
        } catch (BlogNotFoundException e) {
            LOG.error("Error saving settings.", e);
            throw new ApiException("Error saving settings");
        } catch (BlogAccessException e) {
            LOG.error("Error saving settings.", e);
            throw new ApiException("Error saving settings");
        }
    }

    /**
     * Save the settings. {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws HttpRequestMethodNotSupportedException,
            ApiException {

        apiResult.setStatus(ResultStatus.OK.name());
        String message = null;

        String externalSystemId = request.getParameter(ExternalObjectConstants.EXTERNALSYSTEMID);
        String externalObjectId = request.getParameter(ExternalObjectConstants.EXTERNALID);
        String externalName = request.getParameter(ExternalObjectConstants.EXTERNALNAME);
        if (externalName == null) {
            externalName = StringUtils.EMPTY;
        }
        Long blogId = getLongParameter(request, BLOG);
        String type = request.getParameter(TYPE);
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        if (StringUtils.isEmpty(type)) {
            deleteExternalObject(blogId, externalSystemId, externalObjectId);

            message = ResourceBundleManager.instance().getText(
                    "widget.composite.manage.sync.save.success", locale);

        } else {
            ExternalObjectProperty typeProperty = ExternalObjectProperty.Factory.newInstance();
            typeProperty.setPropertyKey(TYPE);
            typeProperty.setKeyGroup(TYPE_PROPERTY_KEY_GROUP);
            typeProperty.setPropertyValue(type);
            Set<ExternalObjectProperty> properties = new HashSet<ExternalObjectProperty>();
            properties.add(typeProperty);
            ExternalObject externalObject = ExternalObject.Factory.newInstance(externalSystemId,
                    externalObjectId, externalName, properties);

            message = ResourceBundleManager.instance().getText(
                    "widget.composite.manage.sync.save.success", locale);
            try {
                ServiceLocator.findService(ExternalObjectManagement.class).updateExternalObject(
                        blogId, externalObject);
            } catch (Exception e) {
                LOG.error("Error saving new blog synchronization settings.", e);
                apiResult.setStatus(ResultStatus.ERROR.name());
                message = "Error saving new blog synchronization settings.";
            }
        }

        apiResult.setMessage(message);

        return null;
    }
}
