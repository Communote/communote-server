package com.communote.common.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scales an image to a predefined size. By default the aspect ratio will be preserved. If
 * {@link #isDrawBackground()} is activated the resulting image will have the defined size. Any non
 * image data will be filled with the color defined by {@link #setBackgroundColor(Color)},
 * {@link #isSameAspectRatio()} must return true in that case. In case the image is smaller than the
 * target size it won't be upscaled but the remaining data will be filled with the background color
 * if {@link #isDrawBackground()} returns true.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// Use internal factory, scalers must not be public.
public class ImageScaler {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageScaler.class);

    /**
     * Defines the quality, which should be used for JPEG images. Can be set as system property
     * using "com.communote.image.scale.jpeg.quality". Default is 0.95.
     */
    private static final Float SYSTEM_PROPERTY_JPEG_QUALITY;

    static {
        float quality = 1;
        String key = "com.communote.image.scale.jpeg.quality";
        String value = System.getProperty(key, "" + quality);
        try {
            quality = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("The value {} is not allowed for {}", value, key);
        }
        SYSTEM_PROPERTY_JPEG_QUALITY = quality;
    }

    private final Color defaultBackgroundColor = Color.WHITE;

    private int height;

    private int width;

    private boolean sameAspectRatio;
    private boolean drawBackground;
    private Color backgroundColor = null;
    private ImageFormatType formatType;

    private int verticalAlignment = -1;

    private int horizontalAlignment = -1;

    /**
     * Construct a new scaler
     *
     * @param size
     *            the maximum size of the resulting image
     * @param imageFormatType
     *            the image format type
     */
    public ImageScaler(ImageSize size, ImageFormatType imageFormatType) {
        this(size.getHeight(), size.getWidth(), imageFormatType);
    }

    /**
     * Construct a new scaler
     *
     * @param height
     *            maximal height of the resulting image
     * @param width
     *            maximal width of the resulting image
     * @param imageFormatType
     *            the image format of the scaled image
     */
    public ImageScaler(int height, int width, ImageFormatType imageFormatType) {
        sameAspectRatio = true;
        drawBackground = false;

        this.height = height;
        this.width = width;
        this.formatType = imageFormatType;
    }

    /**
     * Convert the buffered image to a byte array in the correct image format
     *
     * @param image
     *            the image to convert
     * @return the converted image or null if the provided image was null
     */
    private byte[] convertImageToBytes(BufferedImage image) {
        if (image == null) {
            return null;
        }

        if (!formatType.isSupportsTransparency()) {
            // JPEG for instance cannot handle transparency so we must fill the transparent pixels
            if (image.getColorModel().getTransparency() != Transparency.OPAQUE) {
                BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D gfx = newImage.createGraphics();
                gfx.setBackground(getEffectiveBackgroundColor());
                gfx.clearRect(0, 0, image.getWidth(), image.getHeight());
                gfx.drawImage(image, 0, 0, null);
                gfx.dispose();
                image = newImage;
            }
        }
        Float compressionQuality = null;
        if (ImageFormatType.jpeg.equals(formatType)) {
            compressionQuality = SYSTEM_PROPERTY_JPEG_QUALITY;
        }
        return ImageHelper.imageToByte(image, formatType.name(), compressionQuality);
    }

    /**
     * Create a new image that as the background color defined in {@link #backgroundColor}. The
     * given image will be placed inside that image
     *
     * @param image
     *            the image to use
     * @return the new image with the background color
     */
    private BufferedImage drawBackgroundToImage(BufferedImage image) {
        BufferedImage bufferedImage;
        Graphics2D gfx;

        // Note: actually it is not necessary to differ between transparency support here because
        // the convertImageToBytes would handle this but it is faster if we write the correct format
        // here because this avoids another image operation
        if (formatType.isSupportsTransparency()) {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        gfx = bufferedImage.createGraphics();
        gfx.setBackground(getEffectiveBackgroundColor());
        gfx.clearRect(0, 0, width, height);
        int x;
        if (this.horizontalAlignment == -1) {
            x = 0;
        } else if (this.horizontalAlignment == 0) {
            x = (width - image.getWidth()) / 2;
        } else {
            x = width - image.getWidth();
        }
        if (x < 0) {
            x = 0;
        }
        int y;
        if (this.verticalAlignment == -1) {
            y = 0;
        } else if (this.verticalAlignment == 0) {
            y = (height - image.getHeight()) / 2;
        } else {
            y = height - image.getHeight();
        }
        if (y < 0) {
            y = 0;
        }
        gfx.drawImage(image, x, y, image.getWidth(), image.getHeight(), null);
        gfx.dispose();
        return bufferedImage;
    }

    /**
     *
     * @return the color to use of {@link #isDrawBackground()} is true
     */
    private Color getEffectiveBackgroundColor() {
        if (!drawBackground || backgroundColor == null) {
            return defaultBackgroundColor;
        } else {
            return backgroundColor;
        }
    }

    /**
     *
     * @return the image format type
     */
    public ImageFormatType getFormatType() {
        return formatType;
    }

    /**
     *
     * @return the maximum height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the transparency. Returns either OPAQUE, BITMASK, or TRANSLUCENT.
     *
     * @param image
     *            The image.
     * @return the transparency of this ColorModel.
     */
    private int getTransparency(Image image) {
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().getTransparency();
        }
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            if (pg.grabPixels() && pg.getColorModel() != null) {
                return pg.getColorModel().getTransparency();
            }
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }
        // fallback to generic type
        return Transparency.TRANSLUCENT;
    }

    /**
     *
     * @return the maximum width
     */
    public int getWidth() {
        return width;
    }

    /**
     * This method returns {@code true} if the specified image has transparent pixels Found:
     * http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
     *
     * @param image
     *            The image.
     * @return True, if the the image has an alpha value.
     */
    private boolean hasAlpha(Image image) {
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }
        PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pixelGrabber.grabPixels();
        } catch (InterruptedException e) {
            LOGGER.debug(e.getMessage());
        }
        ColorModel colorModel = pixelGrabber.getColorModel();
        return colorModel != null && colorModel.hasAlpha();
    }

    /**
     *
     * @return true to draw a background
     */
    public boolean isDrawBackground() {
        return drawBackground;
    }

    /**
     *
     * @return if the aspect ratio will be preserved
     */
    public boolean isSameAspectRatio() {
        return sameAspectRatio;
    }

    private int normalizeAlignment(int alignment) {
        if (alignment < 0) {
            return -1;
        }
        if (alignment == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * Resizes a byte array of an image and returns the transformed byte array image. If
     * {@code sameAspectRatio} is {@code true} the resulting image is scaled proportionally to the
     * specified width or height. Images smaller than the defined dimensions will not be changed.
     *
     * @param data
     *            Binary image data
     * @return returns the scaled byte array or null if the image data couldn't be parsed
     */
    public byte[] resizeImage(byte[] data) {
        BufferedImage image = ImageHelper.byteToImage(data);
        if (image != null) {
            if (image.getHeight() > height || image.getWidth() > width) {
                image = scaleImage(image, height, width, sameAspectRatio);
            }
            if (drawBackground) {
                image = drawBackgroundToImage(image);
            }
        }
        byte[] result = convertImageToBytes(image);
        if (result != null) {
            result = ImageHelper.copyExifOrientation(data, result);
        }
        return result;
    }

    /**
     * Scale the image and take care of the aspect ratio if {@code sameAspectRatio} is {@code true}.
     *
     * @param image
     *            the image to scale
     * @param newHeight
     *            The new height
     * @param newWidth
     *            The new width
     * @param preserveAspectRatio
     *            preserve aspect ratio
     * @return the scaled imaged
     */
    private BufferedImage scaleImage(BufferedImage image, int newHeight, int newWidth,
            boolean preserveAspectRatio) {
        int height = -1;
        int width = -1;

        if (preserveAspectRatio) {
            double fixedRatio = (double) newWidth / (double) newHeight;
            if ((double) image.getWidth() / (double) image.getHeight() >= fixedRatio) {
                width = newWidth;
            } else {
                height = newHeight;
            }
        } else {
            width = newWidth;
            height = newHeight;
        }

        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        return toBufferedImage(scaledImage);
    }

    /**
     *
     * @param backgroundColor
     *            the background color to use when {@link #isDrawBackground()} is true
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     *
     * @param size
     *            the maximum size of the resulting image
     */
    public void setDimension(ImageSize size) {
        this.height = size.getHeight();
        this.width = size.getWidth();
    }

    /**
     *
     * @param drawBackground
     *            true to draw a background to reach the exact size as defined (only used if
     *            {@link #setSameAspectRatio(boolean)} is true)
     */
    public void setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
    }

    /**
     *
     * @param formatType
     *            the {@link ImageFormatType} to use
     */
    public void setFormatType(ImageFormatType formatType) {
        this.formatType = formatType;
    }

    /**
     *
     * @param height
     *            the maximum height of the image
     */
    public void setHeight(int height) {
        this.height = height;
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
        this.horizontalAlignment = normalizeAlignment(alignment);
    }

    /**
     *
     * @param sameAspectRatio
     *            Set to <code>true</code> to preserve the aspect ratio.
     */
    public void setSameAspectRatio(boolean sameAspectRatio) {
        this.sameAspectRatio = sameAspectRatio;
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
        this.verticalAlignment = normalizeAlignment(alignment);
    }

    /**
     *
     * @param width
     *            the maximum width for the resulting image
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Found on: http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
     *
     * @param image
     *            The image.
     * @return An BufferedImage.
     */
    private BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // ensure that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        boolean hasAlpha = hasAlpha(image);
        BufferedImage bufferdImage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = getTransparency(image);
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bufferdImage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null),
                    transparency);
        } catch (HeadlessException e) {
            LOGGER.debug("No screen available, so no detection possible: " + e.getMessage());
        }
        if (bufferdImage == null) {
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bufferdImage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        // draw the image into the BufferedImage
        Graphics2D graphics = bufferdImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return bufferdImage;
    }
}
