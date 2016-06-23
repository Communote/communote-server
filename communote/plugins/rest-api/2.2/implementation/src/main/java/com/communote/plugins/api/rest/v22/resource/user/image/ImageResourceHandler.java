package com.communote.plugins.api.rest.v22.resource.user.image;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v22.resource.DefaultParameter;
import com.communote.plugins.api.rest.v22.resource.DefaultResourceHandler;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.model.user.ImageSizeType;

/**
 * Is the handler class to provide data for an image resource to the resource class. All the list
 * parameter are collected in a parameter map.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageResourceHandler
extends
DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, GetImageParameter, DefaultParameter> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageResourceHandler.class);

    /**
     * Returns the image of a user.
     *
     * @param getImageParameter
     *            the parameters of the attachment
     * @param requestedMimeType
     *            MIME type requested by client
     * @param uriInfo
     *            additional information about request
     * @param requestSessionId
     *            the session id
     * @param request
     *            - javax request
     * @return response for client
     */
    @Override
    public Response handleGetInternally(GetImageParameter getImageParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request) {
        ImageSizeType size;
        switch (getImageParameter.getSize()) {
        case SMALL:
            size = ImageSizeType.SMALL;
            break;
        case MEDIUM:
            size = ImageSizeType.MEDIUM;
            break;
        case LARGE:
            size = ImageSizeType.LARGE;
            ;
            break;
        default:
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        try {
            Image image = ServiceLocator.findService(ImageManager.class).getImage(
                    UserImageDescriptor.IMAGE_TYPE_NAME, getImageParameter.getUserId().toString(),
                    size);
            return Response.ok(image.openStream()).type(image.getMimeType()).build();
        } catch (Exception e) {
            LOGGER.debug("Was not able to get the image: " + e);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
