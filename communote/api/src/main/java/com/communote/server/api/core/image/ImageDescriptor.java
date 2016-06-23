package com.communote.server.api.core.image;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.communote.common.image.ImageFormatType;
import com.communote.common.image.ImageSize;

/**
 * Describes the image.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageDescriptor {

    /** Default background color. */
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private final static List<ImageFormatType> DEFAULT_MIMETYPES = new ArrayList<>();
    static {
        DEFAULT_MIMETYPES.add(ImageFormatType.jpeg);
        DEFAULT_MIMETYPES.add(ImageFormatType.png);
    }
    private final ImageSize size;
    private final boolean scaled;
    private final boolean drawBackground;
    private final boolean preserveAspectRation;
    private final String identifier;
    private final Color backgroundColor;

    private final List<ImageFormatType> validMimeTypes;
    private int verticalAlignment;
    private int horizontalAlignment;

    /**
     * Constructor, where scaled and drawBackground are not set (false).
     *
     * @param size
     *            The size as {@link ImageSize}.
     * @param identifier
     *            The unique identifier of the image.
     */
    public ImageDescriptor(ImageSize size, String identifier) {
        this(size, identifier, false, false, DEFAULT_BACKGROUND_COLOR);
    }

    /**
     * @param size
     *            The size as {@link ImageSize}.
     * @param identifier
     *            The unique identifier of the image.
     * @param scaled
     *            If true, the image is scaled.
     * @param drawBackground
     *            If true, the image will be extended to size and a background will be drawn.
     */
    public ImageDescriptor(ImageSize size, String identifier, boolean scaled, boolean drawBackground) {
        this(size, identifier, scaled, drawBackground, DEFAULT_BACKGROUND_COLOR);
    }

    /**
     * @param size
     *            The size as {@link ImageSize}.
     * @param identifier
     *            The unique identifier of the image.
     * @param scaled
     *            If true, the image is scaled.
     * @param drawBackground
     *            If true, the image will be extended to size and a background will be drawn.
     * @param backgroundColor
     *            The background color.
     */
    public ImageDescriptor(ImageSize size, String identifier, boolean scaled,
            boolean drawBackground, Color backgroundColor) {
        this(size, identifier, scaled, drawBackground, backgroundColor, true);
    }

    /**
     * @param size
     *            The size as {@link ImageSize}.
     * @param identifier
     *            The unique identifier of the image.
     * @param scaled
     *            If true, the image is scaled.
     * @param drawBackground
     *            If true, the image will be extended to size and a background will be drawn.
     * @param backgroundColor
     *            The background color.
     * @param preserveAspectRation
     *            If true, the aspect ration of the final image will be respected.
     */
    public ImageDescriptor(ImageSize size, String identifier, boolean scaled,
            boolean drawBackground, Color backgroundColor, boolean preserveAspectRation) {
        this(size, identifier, scaled, drawBackground, backgroundColor, preserveAspectRation,
                DEFAULT_MIMETYPES);
    }

    /**
     * @param size
     *            The size as {@link ImageSize}.
     * @param identifier
     *            The unique identifier of the image.
     * @param scaled
     *            If true, the image is scaled.
     * @param drawBackground
     *            If true, the image will be extended to size and a background will be drawn.
     * @param backgroundColor
     *            The background color.
     * @param preserveAspectRation
     *            If true, the aspect ration of the final image will be respected.
     * @param validMimeTypes
     *            The mime types the image can have. If the image doesn't match any of the, the
     *            first will be used.
     */
    public ImageDescriptor(ImageSize size, String identifier, boolean scaled,
            boolean drawBackground, Color backgroundColor, boolean preserveAspectRation,
            List<ImageFormatType> validMimeTypes) {
        this.size = size;
        this.identifier = identifier;
        this.scaled = scaled;
        this.drawBackground = drawBackground;
        this.backgroundColor = backgroundColor != null ? backgroundColor : DEFAULT_BACKGROUND_COLOR;
        this.preserveAspectRation = preserveAspectRation;
        this.validMimeTypes = validMimeTypes != null && validMimeTypes.size() > 0 ? validMimeTypes
                : DEFAULT_MIMETYPES;
    }

    /**
     *
     * @param width
     *            The width.
     * @param height
     *            The height.
     * @param identifier
     *            The unique identifier of the image.
     */
    public ImageDescriptor(int width, int height, String identifier) {
        this(new ImageSize(width, height), identifier, false, false, DEFAULT_BACKGROUND_COLOR);
    }

    /**
     *
     * @param width
     *            The width.
     * @param height
     *            The height.
     * @param identifier
     *            The unique identifier of the image.
     * @param scaled
     *            If true, the image is scaled.
     * @param drawBackground
     *            If true, the image will be extended to size and a background will be drawn.
     * @param backgroundColor
     *            The background color.
     */
    public ImageDescriptor(int width, int height, String identifier, boolean scaled,
            boolean drawBackground, Color backgroundColor) {
        this(new ImageSize(width, height), identifier, scaled, drawBackground, backgroundColor);
    }

    /**
     * @return the backgroundColor
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     *
     * @return a value describing the horizontal alignment of the image
     * @see #setHorizontalAlignment(int)
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the size
     */
    public ImageSize getSize() {
        return size;
    }

    /**
     * @return Set of valid mime types.
     */
    public List<ImageFormatType> getValidMimeTypes() {
        return validMimeTypes;
    }

    /**
     * @return a value describing the vertical alignment of the image
     * @see #setVerticalAlignment(int)
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * @return the drawBackground
     */
    public boolean isDrawBackground() {
        return drawBackground;
    }

    /**
     * @return the preserveAspectRation
     */
    public boolean isPreserveAspectRation() {
        return preserveAspectRation;
    }

    /**
     * @return <code>true</code> if the image is scaled, else not.
     */
    public boolean isScaled() {
        return scaled;
    }

    /**
     * Set the alignment to use for positioning the image horizontally within the scaled image. A
     * value of less than 0 means to place the image at the left edge, a value of 0 leads to
     * centering the image and a value greater than 0 means to place the image at the right edge.
     * This setting only has an effect if a background should be drawn.
     *
     * @param alignment
     *            a value describing the horizontal alignment of the image
     */
    public void setHorizontalAlignment(int alignment) {
        this.horizontalAlignment = alignment;
    }

    /**
     * Set the alignment to use for positioning the image vertically within the scaled image. A
     * value of less than 0 means to place the image at the top edge, a value of 0 leads to
     * centering the image and a value greater than 0 means to place the image at the bottom edge.
     * This setting only has an effect if a background should be drawn.
     *
     * @param alignment
     *            a value describing the vertical alignment of the image
     */
    public void setVerticalAlignment(int alignment) {
        this.verticalAlignment = alignment;
    }
}
