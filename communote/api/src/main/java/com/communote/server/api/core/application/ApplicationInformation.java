package com.communote.server.api.core.application;

/**
 * Holds information, like the version, of the running application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface ApplicationInformation {
    /**
     * Version constant for 1.1.4
     */
    public final static String V_1_1_4 = "1.1.4";

    /**
     * Version constant for 1.1
     */
    public final static String V_1_1 = "1.1";

    /**
     * Version constant for 1.0.1
     */
    public final static String V_1_0_1 = "1.0.1";
    /**
     * Version constant for 1.0
     */
    public final static String V_1_0 = "1.0";

    /**
     * @return path to the directory where the application got extracted to. The path is terminated
     *         by a path separator.
     */
    public String getApplicationRealPath();

    /**
     * @return the build number including a suffix which marks he build as standalone (ST) or
     *         multi-tenant saas build (OS).
     */
    public String getBuildNumberWithType();

    /**
     *
     * @return Timestamp of the build time of the current version.
     */
    public long getBuildTimestamp();

    /**
     * @return the build number (e.g. 1.0.1.2222) - included the revision
     */
    public String getBuildNumber();

    /**
     * @return the time of the build as string in the format yyyy-MM-dd HH:mm:ss or an emty string
     *         if the build time is not available
     */
    public String getBuildTime();

    /**
     * @return the project version without a SNAPSHOT suffix
     */
    String getProjectVersion();

    /**
     * @return the revision or the GIT hash of the commit the build is based on
     */
    String getRevision();

    /**
     * @return true if this is the standalone version of Communote.
     */
    boolean isStandalone();

}
