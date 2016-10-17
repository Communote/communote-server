package com.communote.common.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.IOHelper;
import com.communote.common.io.MimeTypeHelper;

/**
 * Utility class for image transformations.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageHelper {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageHelper.class);

    /**
     * The maximum of the image size is 5000 bytes
     */
    public final static int USER_IMAGE_MAX_SIZE = 5000;

    /**
     * Converts the byte array to an image.
     *
     * @param data
     *            Image as binary data.
     * @return Returns a BufferedImage object or null.
     */
    public static BufferedImage byteToImage(byte[] data) {
        try {
            return Imaging.getBufferedImage(data);
        } catch (Exception e) {
            LOGGER.warn("Error reading image with Imaging: {}", e.getMessage());
            try {
                return ImageIO.read(new ByteArrayInputStream(data));
            } catch (IOException e1) {
                LOGGER.warn("Error reading image with ImageIO: {} ", e1.getMessage());
                LOGGER.debug(e1.getMessage(), e1);
            }
        }
        return null;
    }

    /**
     * This method will copy the Exif "orientation" information to the resulting image, if the
     * original image contains this data too.
     *
     * @param sourceImage
     *            The source image.
     * @param result
     *            The original result.
     * @return The new result containing the Exif orientation.
     */
    public static byte[] copyExifOrientation(byte[] sourceImage, byte[] result) {
        try {
            ImageMetadata imageMetadata = Imaging.getMetadata(sourceImage);
            if (imageMetadata == null) {
                return result;
            }
            List<? extends ImageMetadata.ImageMetadataItem> metadataItems = imageMetadata
                    .getItems();
            for (ImageMetadata.ImageMetadataItem metadataItem : metadataItems) {
                if (metadataItem instanceof TiffImageMetadata.TiffMetadataItem) {
                    TiffField tiffField = ((TiffImageMetadata.TiffMetadataItem) metadataItem)
                            .getTiffField();
                    if (!tiffField.getTagInfo().equals(TiffTagConstants.TIFF_TAG_ORIENTATION)) {
                        continue;
                    }
                    Object orientationValue = tiffField.getValue();
                    if (orientationValue == null) {
                        break;
                    }
                    TiffOutputSet outputSet = new TiffOutputSet();
                    TiffOutputDirectory outputDirectory = outputSet.getOrCreateRootDirectory();
                    outputDirectory.add(TiffTagConstants.TIFF_TAG_ORIENTATION,
                            ((Number) orientationValue).shortValue());
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    new ExifRewriter().updateExifMetadataLossy(result, outputStream, outputSet);
                    return outputStream.toByteArray();
                }
            }
        } catch (IOException | ImageWriteException | ImageReadException e) {
            LOGGER.warn("Error reading image: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Creates an image file and save it to the cache directory
     *
     * @param data
     *            Image binary data
     * @param filename
     *            Destination path of the given file
     * @return If the creation of image file was successful, then returns <code>true</code>, else
     *         <code>false</code>
     */
    public static boolean createImageFile(byte[] data, String filename) {
        FileOutputStream fos = null;

        try {
            File file = new File(filename);
            if (!file.exists()) {
                new File(file.getAbsolutePath()).mkdirs();
            }
            fos = new FileOutputStream(file);
            fos.write(data);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error writing image to file: {} ", filename, e);
        } catch (RuntimeException e) {
            LOGGER.error("Error writing image to file: {}", filename, e);
        } finally {
            IOHelper.close(fos);
        }
        return false;
    }

    /**
     *
     * Method to get the best mime type for the given image.
     *
     * @param image
     *            The image to check.
     * @return The mime type of the image or null if the bytes are not a valid image.
     */
    public static String getMimeType(byte[] image) {
        String mimeType = MimeTypeHelper.getMimeType(image);
        if (mimeType.startsWith("image")) {
            return mimeType;
        }
        return null;
    }

    /**
     * Return the given image file as byte array
     *
     * @param file
     *            Image file
     * @param format
     *            Image format
     * @return Byte array
     */
    public static byte[] imageFileToByteArray(File file, String format) {
        BufferedImage image = loadImage(file);
        return imageToByte(image, format);
    }

    /**
     * Converts an image to a byte array in the given image format. The image is not compressed.
     *
     * @param image
     *            BufferedImage object
     * @param format
     *            Format for the transformation
     * @return Returns the converted byte array
     */
    public static byte[] imageToByte(BufferedImage image, String format) {
        return imageToByte(image, format, null);
    }

    /**
     * Converts an image to a byte array in the given image format
     *
     * @param image
     *            BufferedImage object
     * @param format
     *            Format for the transformation
     * @param compressionQuality
     *            optional parameter to set a compression quality. The value is expected to be
     *            between 0 and 1 where 0 is interpreted as high compression and 1 as high quality.
     *            When passing null the image won't be compressed.
     * @return Returns the converted byte array
     */
    public static byte[] imageToByte(BufferedImage image, String format, Float compressionQuality) {
        if (image == null) {
            throw new IllegalArgumentException("image cannnot be null");
        }
        if (format == null) {
            throw new IllegalArgumentException("format cannnot be null");
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            if (compressionQuality != null) {
                ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(format).next();
                ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
                imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imageWriteParam.setCompressionQuality(compressionQuality);
                imageWriter.setOutput(new MemoryCacheImageOutputStream(stream));
                imageWriter.write(null, new IIOImage(image, null, null), imageWriteParam);
                imageWriter.dispose();
            } else {
                ImageIO.write(image, format, stream);
            }
        } catch (IOException e) {
            // throw new IOException("Cannot write image to " + format, e);
            return null;
        }
        byte[] bytesOut = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        return bytesOut;
    }

    /**
     * Loads an image from a file.
     *
     * @param file
     *            File
     * @return BufferedImage object
     */
    public static BufferedImage loadImage(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            // throw new IOException("Cannot read the image file", e);
            LOGGER.error("Can't read image file '{}'", file.getAbsolutePath(), e);
        }
        return image;
    }

    /**
     * Private constructor
     */
    private ImageHelper() {

    }
}
