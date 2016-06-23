package com.communote.server.core.osgi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.CommunoteRuntime;

/**
 * Utility class for helping with bundles.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class OSGiHelper {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OSGiHelper.class);

    private final static Map<String, String> BUNDLE_TO_PATHS = new HashMap<String, String>();

    /**
     * Clears the given content.
     *
     * @param symbolicName
     *            The bundles symbolic name.
     */
    public static void clearBundleStorage(String symbolicName) {
        try {
            FileUtils.deleteDirectory(new File(getBundleStorage(symbolicName)));
        } catch (IOException e) {
            LOGGER.error("Was not able to delete a plugins resource directory.", e);
        }
    }

    /**
     * @return the base directory for caching data of bundles.
     */
    public static File getBundleBasePath() {
        File cacheRootDirectory = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getCacheRootDirectory();
        return new File(cacheRootDirectory.getAbsolutePath() + File.separator + "plugins");
    }

    private static String getBundleDirName(String symbolicName) {
        return symbolicName.toLowerCase(Locale.ENGLISH).replace('.', '-');
    }

    /**
     * @return the directory where the resources of all bundles will be cached
     */
    public static File getBundleResourcesBasePath() {
        return new File(getBundleBasePath(), "resources");
    }

    /**
     * Return the absolute path of the directory a bundle can use for for storing content. This will
     * be a subdirectory of the resources base path.
     *
     * @param symbolicName
     *            The symbolic name of the bundle.
     * @return The path to the storage directory without a trailing file separator.
     */
    public static String getBundleStorage(String symbolicName) {
        symbolicName = getBundleDirName(symbolicName);
        String path = BUNDLE_TO_PATHS.get(symbolicName);
        if (path == null) {

            File storage = new File(getBundleResourcesBasePath(), symbolicName);
            if (!storage.exists()) {
                storage.mkdirs();
            }
            path = storage.getAbsolutePath();
            BUNDLE_TO_PATHS.put(symbolicName, path);
        }
        return path;
    }

    /**
     * Get the path of a bundle resource relative to the resources base path as returned by
     * {@link #getBundleResourcesBasePath()}.
     *
     * @param symbolicName
     *            the symbolic name of the bundle
     * @param resourceLocation
     *            location of a resource of a bundle relative to the META-INF/resources directory
     * @return the location of the provided resource relative to the resources base path or null if
     *         the resource location was null or blank
     */
    public static String getRelativeBundleResourceLocation(String symbolicName,
            String resourceLocation) {
        if (StringUtils.isBlank(resourceLocation)) {
            return null;
        }
        StringBuilder location = new StringBuilder(File.separator);
        location.append(getBundleDirName(symbolicName));
        if (File.separatorChar != '/') {
            resourceLocation = resourceLocation.replace('/', File.separatorChar);
        }
        if (resourceLocation.charAt(0) != File.separatorChar) {
            location.append(File.separator);
        }
        location.append(resourceLocation);
        return location.toString();
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private OSGiHelper() {
        // Do nothing
    }
}
