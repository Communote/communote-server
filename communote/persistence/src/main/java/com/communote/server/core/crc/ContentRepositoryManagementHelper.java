package com.communote.server.core.crc;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;

/**
 * The Class ContentRepositoryManagementHelper offers helper methods for the repository management.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ContentRepositoryManagementHelper {

    private static final int MEGABYTE_IN_BYTES = 1048576;

    /**
     * Gets the current size as string.
     *
     * @param size
     *            the current size
     * @return the current size as string
     */
    // TODO GiB,KiB, localized floating point value...
    public static String getSizeAsString(long size) {
        if (size != 0) {
            size = (long) (size / (float) MEGABYTE_IN_BYTES);
        }
        return String.valueOf(size) + " MiB";
    }

    /**
     * Get the repository size limit.
     *
     * @return the size limit. Will be 0 if there is no limit.
     */
    public static long getSizeLimit() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.FILE_SYSTEM_REPOSITORY_SIZE_LIMIT,
                        ClientConfigurationHelper.DEFAULT_FILE_SYSTEM_REPOSITORY_SIZE_LIMIT);

    }

    /**
     * Gets the size limit as string.
     *
     * @param limit
     *            the size limit
     * @return the size limit as string
     */
    public static String getSizeLimitAsString(long limit) {
        String result;
        if (limit > 0) {
            limit = (long) (limit / (float) MEGABYTE_IN_BYTES);
            result = String.valueOf(limit) + " MiB";
        } else {
            result = "unlimited";
        }
        return result;
    }

    /**
     * private constructor, because it's not allowed to use it.
     */
    private ContentRepositoryManagementHelper() {
        // Do nothing.
    }
}
