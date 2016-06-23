package com.communote.server.api.core.image;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.communote.common.image.ImageFormatType;
import com.communote.common.image.ImageSize;
import com.communote.server.model.user.ImageSizeType;

/**
 * Describes an image type. This includes the supported sizes and some details how scaling should
 * work (keep aspect-ratio, draw background, etc.).
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ImageTypeDescriptor {

    /**
     * Get the identifier of an image from a key value mapping, like the parameters of a request.
     *
     * @param parameters
     *            the parameters to parse
     * @return the image identifier
     */
    String extractImageIdentifier(Map<String, ? extends Object> parameters);

    /**
     * @return the background color to use to fill unoccupied pixels when scaling an image. Is only
     *         required when {@link #isDrawBackground()} returns true.
     */
    Color getBackgroundColor();

    /**
     * @return a value describing the horizontal alignment of the image within the background when
     *         it is scaled. A value of less than 0 means to place the image at the top left, a
     *         value of 0 leads to centering the image and a value greater than 0 means to place the
     *         image at the right edge. Is only required when {@link #isDrawBackground()} returns
     *         true.
     */
    int getHorizontalAlignment();

    /**
     *
     * @return the unique name of the image type
     */
    String getName();

    /**
     * Map an image size type to the actual size supported by this image type.
     *
     * @param sizeType
     *            the size type to map
     * @return the image size, must not be null
     */
    ImageSize getSizeForImageSizeType(ImageSizeType sizeType);

    /**
     * @return the valid mime types
     */
    List<ImageFormatType> getValidMimeTypes();

    /**
     * @return a string representing the version of the descriptor instance. The returned value
     *         should change each time the scaling properties are modified.
     */
    String getVersionString();

    /**
     * @return a value describing the vertical alignment of the image within the background when it
     *         is scaled. A value of less than 0 means to place the image at the top edge, a value
     *         of 0 leads to centering the image and a value greater than 0 means to place the image
     *         at the bottom edge. Is only required when {@link #isDrawBackground()} returns true.
     */
    int getVerticalAlignment();

    /**
     * @return whether to fill the background of a scaled image with the color provided by
     *         {@link #getBackgroundColor()}
     */
    boolean isDrawBackground();

    /**
     * @return whether to preserve the aspect ratio when scaling
     */
    boolean isPreserveAspectRation();
}
