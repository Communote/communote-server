package com.communote.common.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.medsea.mimeutil.MimeUtil2;

/**
 * Helper class for getting the MimeType of a file.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MimeTypeHelper {

    private static Map<String, String> FIX_MIMETYPE_EXTENSIONS = new HashMap<String, String>();

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeHelper.class);

    private static MimeUtil2 MIME_UTIL;

    static {
        MIME_UTIL = new MimeUtil2();
        MIME_UTIL.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        // add our custom detector which respects the order. Also note that this detector will be
        // called first since the detectors are sorted alphabetically by their FQCN.
        MIME_UTIL.registerMimeDetector("com.communote.common.io.ExtensionMimeDetector");

        // Based on
        // http://office.microsoft.com/en-au/help/introduction-to-new-file-name-extensions-HA010006935.aspx
        // http://technet.microsoft.com/de-de/library/ee309278(office.12).aspx
        // http://filext.com/file-extension
        String prefix = "application/vnd.openxmlformats-officedocument.";
        FIX_MIMETYPE_EXTENSIONS.put("docx", prefix + "wordprocessingml.document");
        FIX_MIMETYPE_EXTENSIONS.put("dotx", prefix + "wordprocessingml.template");
        FIX_MIMETYPE_EXTENSIONS.put("docm", "application/vnd.ms-word.document.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("dotm", "application/vnd.ms-word.template.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("xlsx", prefix + "spreadsheetml.sheet");
        FIX_MIMETYPE_EXTENSIONS.put("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("xltx", prefix + "spreadsheetml.template");
        FIX_MIMETYPE_EXTENSIONS.put("xltm", "application/vnd.ms-excel.template.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS
                .put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("pptx", prefix + "presentationml.presentation");
        FIX_MIMETYPE_EXTENSIONS
                .put("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("potx", prefix + "presentationml.template");
        FIX_MIMETYPE_EXTENSIONS.put("potm",
                "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("ppsx", prefix + "presentationml.slideshow");
        FIX_MIMETYPE_EXTENSIONS
                .put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS
                .put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        FIX_MIMETYPE_EXTENSIONS.put("sldm", "application/vnd.ms-powerpoint.slide.macroEnabled.12");
        FIX_MIMETYPE_EXTENSIONS.put("thmx", "application/vnd.ms-officetheme");

        // Additional
        FIX_MIMETYPE_EXTENSIONS.put("wmv", "video/x-ms-wmv");

    }

    /**
     * Returns the MimeType for the file given as a byte array. For images it will be checked
     * whether they can be processed.
     *
     * @param fileData
     *            The file as byte array
     * @return The type of the file.
     */
    public static String getMimeType(byte[] fileData) {
        @SuppressWarnings("rawtypes")
        Collection mimeTypes = MIME_UTIL.getMimeTypes(fileData);
        String mimeType = MimeUtil2.getMostSpecificMimeType(mimeTypes).toString();
        if (mimeType.startsWith("image/")) {
            try {
                Imaging.getImageInfo(fileData);
                return mimeType;
            } catch (ImageReadException e) {
                LOGGER.error("Imaging: Image seems to be invalid or unsupported", e);
            } catch (IOException e) {
                LOGGER.error("Imaging: There was an error reading the image", e);
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
            try {
                // Fallback. Read the image the Java standard way.
                ImageIO.read(inputStream).getAlphaRaster();
            } catch (Exception e) {
                LOGGER.error("There was an error reading the image: {}", e);
                mimeType = "image/x-unknown";
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        return mimeType;
    }

    /**
     * Returns the MimeType for the given file. For images it will be checked whether they can be
     * processed.
     *
     * @param file
     *            The file to check.
     * @return The type of the file.
     */
    public static String getMimeType(File file) {
        String fileExtension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        if (FIX_MIMETYPE_EXTENSIONS.containsKey(fileExtension)) {
            return FIX_MIMETYPE_EXTENSIONS.get(fileExtension);
        }
        @SuppressWarnings("rawtypes")
        Collection mimeTypes = MIME_UTIL.getMimeTypes(file);
        String mimeType = MimeUtil2.getMostSpecificMimeType(mimeTypes).toString();
        if (mimeType.startsWith("image/")) {
            if (file.exists()) {
                try {
                    Imaging.getImageInfo(file);
                    return mimeType;
                } catch (ImageReadException e) {
                    LOGGER.warn("Imaging: Image seems to be invalid or unsupported: {} : {}",
                            e.getMessage(),
                            file.getAbsolutePath());
                } catch (IOException e) {
                    LOGGER.warn("Imaging: There was an error reading the image: {} : {}",
                            e.getMessage(),
                            file.getAbsolutePath());
                }
                try {
                    // Fallback. Read the image the Java standard way.
                    ImageIO.read(file).getAlphaRaster();
                } catch (Exception e) {
                    LOGGER.warn("There was an error reading the image: {} : {}", e.getMessage(),
                            file.getAbsolutePath());
                    mimeType = "image/x-unknown";
                }
            } else {
                LOGGER.debug("Image file {} does not exist. Marking it as unknown image type.",
                        file.getAbsolutePath());
                mimeType = "image/x-unknown";
            }
        }
        return mimeType;
    }

    /**
     * Returns the MimeType for the given file. For images it will be checked whether they can be
     * processed.
     *
     * @param fileStream
     *            The file as stream
     * @param filename
     *            the local filename the stream was loaded from, can be null
     * @return The type of the file.
     */
    public static String getMimeType(InputStream fileStream, String filename) {
        if (filename != null) {
            String fileExtension = FilenameUtils.getExtension(filename).toLowerCase();
            if (FIX_MIMETYPE_EXTENSIONS.containsKey(fileExtension)) {
                return FIX_MIMETYPE_EXTENSIONS.get(fileExtension);
            }
        }
        @SuppressWarnings("rawtypes")
        Collection mimeTypes = MIME_UTIL.getMimeTypes(fileStream);
        String mimeType = MimeUtil2.getMostSpecificMimeType(mimeTypes).toString();
        if (mimeType.startsWith("image/")) {
            try {
                Imaging.getImageInfo(fileStream, filename);
                return mimeType;
            } catch (ImageReadException e) {
                LOGGER.warn("Imaging: Byte array image seems to be invalid or unsupported: {}, {}",
                        e.getMessage(), filename);
            } catch (IOException e) {
                LOGGER.warn("Imaging: There was an error reading the image data: {}, {}",
                        e.getMessage(), filename);
            }
            try {
                // Fallback. Read the image the Java standard way.
                ImageIO.read(fileStream).getAlphaRaster();
            } catch (Exception e) {
                LOGGER.warn("There was an error reading the image stream: {}, {}", e.getMessage(),
                        filename);
                mimeType = "image/x-unknown";
            }
        }
        return mimeType;
    }

    /**
     * Returns the MimeType for the given file. Uses getMimeType(File).
     *
     * @param absolutePathToFile
     *            The absolute path to the file to be used.
     * @return The MimeType.
     */
    public static String getMimeType(String absolutePathToFile) {
        return getMimeType(new File(absolutePathToFile));
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private MimeTypeHelper() {
        // Do nothing
    }
}
