package com.communote.common.image;

/**
 * Wrapper for width and height of an image.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageSize {
    private final int width;
    private final int height;

    /**
     * Create a new image size wrapper
     * 
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     * @throws IllegalArgumentException
     *             in case height or width is a negative number
     */
    public ImageSize(int width, int height) {
        if (width < 0) {
            throw new IllegalArgumentException("Width cannot be a negative number");
        }
        if (height < 0) {
            throw new IllegalArgumentException("Width cannot be a negative number");
        }
        this.width = width;
        this.height = height;
    }

    /**
     * @return the height of the image
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the width of the image
     */
    public int getWidth() {
        return width;
    }
}
