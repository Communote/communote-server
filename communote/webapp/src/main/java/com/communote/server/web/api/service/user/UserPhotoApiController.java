package com.communote.server.web.api.service.user;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.common.io.BinaryData;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BinaryApiController;
import com.communote.server.web.api.service.IllegalRequestParameterException;

/**
 * Api controller to return the user photo
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class UserPhotoApiController extends BinaryApiController {

    private static final String PARAM_IMAGE_SIZE = "imageSize";

    /**
     * {@inheritDoc}
     */
    @Override
    protected BinaryData execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, ApiException {
        // throw resource not found exception on error
        String userId = getResourceId(request, true).toString();
        ImageSizeType imageSizeType = getImageSizeType(request);
        BinaryData result;
        try {
            Image image = ServiceLocator.findService(ImageManager.class).getImage(
                    UserImageDescriptor.IMAGE_TYPE_NAME, userId, imageSizeType);
            result = new BinaryData(image.getMimeType(), image.getBytes(), image
                    .getLastModificationDate().getTime());
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
        return result;
    }

    /**
     *
     * @return the image cache manager
     */
    public ImageManager getImageManager() {
        return ServiceLocator.findService(ImageManager.class);
    }

    /**
     * @param request
     *            the request
     * @return Get the type of blogs to get out of the request
     * @throws IllegalRequestParameterException
     *             invalid blog list type
     */
    private ImageSizeType getImageSizeType(HttpServletRequest request)
            throws IllegalRequestParameterException {
        String parameterValue = getNonEmptyParameter(request, PARAM_IMAGE_SIZE);
        try {
            return ImageSizeType.fromString(parameterValue.toUpperCase());
        } catch (Exception e) {
            throw new IllegalRequestParameterException(PARAM_IMAGE_SIZE, parameterValue,
                    e.getMessage());
        }
    }

}
