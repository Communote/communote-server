package com.communote.server.web.fe.widgets.image;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.exception.ExceptionMapperManagement;
import com.communote.server.core.exception.Status;
import com.communote.server.core.image.CoreImageType;
import com.communote.server.core.image.type.EntityImageManagement;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.helper.ImageUrlHelper;
import com.communote.server.web.commons.helper.JsonRequestHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@RequestMapping(value = "/microblog/*/widgets/image/GlobalIdUploadImageUploader.json")
public class GlobalIdUploadImageController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GlobalIdUploadImageController.class);

    /**
     * Create the result object to be returned to the client
     *
     * @param request
     *            The request
     * @param entityId
     *            The entities id.
     * @param imageType
     *            The type.
     * @return the JSON result
     */
    private ObjectNode createResult(HttpServletRequest request, String entityId, String imageType) {
        ObjectNode result = JsonHelper.getSharedObjectMapper().createObjectNode();
        String url = ImageUrlHelper.buildImageUrl(entityId,
                "banner".equals(imageType) ? CoreImageType.entityBanner
                        : CoreImageType.entityProfile, ImageSizeType.LARGE);
        result.put("imageUrl", ControllerHelper.renderRelativeUrl(request, url, false, false));
        return result;
    }

    /**
     * Method to reset the image of the given entity to the default type.
     *
     * @param request
     *            The send request.
     * @param response
     *            The response.
     * @param entityId
     *            Id of the entity to change.
     * @param imageType
     *            Type of the image.
     * @throws IOException
     *             Thrown, when the output can't be written.
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", params = "widgetAction=reset_default")
    public void setDefault(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "entityId", required = true) String entityId,
            @RequestParam(value = "imageType", required = true) String imageType)
            throws IOException {
        ObjectNode jsonResponse;
        try {
            ServiceLocator.findService(EntityImageManagement.class).setDefault(entityId,
                    EntityImageManagement.ImageType.getType(imageType));
            jsonResponse = JsonRequestHelper.createJsonSuccessResponse(
                    ResourceBundleManager.instance().getText(
                            "widget.globalid-upload-image.reset.success",
                            SessionHandler.instance().getCurrentLocale(request)),
                    createResult(request, entityId, imageType));
        } catch (Exception e) {
            LOGGER.error("Setting default image for entity with ID {} and type {} failed",
                    entityId, imageType, e);
            Status status = ServiceLocator.findService(ExceptionMapperManagement.class)
                    .mapException(e);
            jsonResponse = JsonRequestHelper.createJsonErrorResponse(status.getMessage().toString(
                    SessionHandler.instance().getCurrentLocale(request)));
        }
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * @param request
     *            The send request.
     * @param response
     *            The response
     * @param image
     *            The image.
     * @param entityId
     *            The entities id.
     * @param imageType
     *            Type of the image.
     * @throws IOException
     *             Thrown, when the output can't be written.
     */
    @RequestMapping(method = RequestMethod.POST, params = "widgetAction=upload_image")
    public ModelAndView uploadImage(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "file", required = true) MultipartFile image,
            @RequestParam(value = "entityId", required = true) String entityId,
            @RequestParam(value = "imageType", required = true) String imageType,
            @RequestParam(value = "htmlResponse", required = false) String sendHtmlResponse)
            throws IOException {
        ObjectNode jsonResponse;
        try {
            ServiceLocator.findService(EntityImageManagement.class).storeImage(entityId,
                    EntityImageManagement.ImageType.getType(imageType), image.getBytes());
            jsonResponse = JsonRequestHelper.createJsonSuccessResponse(
                    ResourceBundleManager.instance().getText(
                            "widget.globalid-upload-image.upload.success",
                            SessionHandler.instance().getCurrentLocale(request)),
                    createResult(request, entityId, imageType));
        } catch (Exception e) {
            LOGGER.error("Uploading image for entity with ID {} and type {} failed", entityId,
                    imageType, e);
            Status status = ServiceLocator.findService(ExceptionMapperManagement.class)
                    .mapException(e);
            jsonResponse = JsonRequestHelper.createJsonErrorResponse(status.getMessage().toString(
                    SessionHandler.instance().getCurrentLocale(request)));
        }
        return ControllerHelper.prepareModelAndViewForJsonResponse(request, response, jsonResponse,
                true);
    }
}
