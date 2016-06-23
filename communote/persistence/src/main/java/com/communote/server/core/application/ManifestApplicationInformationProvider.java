package com.communote.server.core.application;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.FastDateFormat;

import com.communote.common.io.IOHelper;
import com.communote.server.api.core.application.ApplicationInformation;
import com.communote.server.api.core.bootstrap.ApplicationInitializationException;

/**
 * Retrieves the information from the Manifest file of the application. The Manifest is loaded from
 * the directory where the application got extracted to.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ManifestApplicationInformationProvider implements ApplicationInformationProvider,
        ApplicationInformation {
    private static final String ATTRIBUTE_IMPLEMENTATION_BUILD_TIMESTAMP = "Implementation-Build-Timestamp";

    private static final String ATTRIBUTE_IMPLEMENTATION_BUILD = "Implementation-Build";

    private static final String ATTRIBUTE_PROJECT_VERSION = "Project-Version";

    private Manifest applicationManifest;

    private final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    private String projectVersion;

    private String revision;

    private String buildNumber;

    private String buildTime;

    private long buildTimestamp = -1L;

    private final String applicationRealPath;

    public ManifestApplicationInformationProvider(String applicationRealPath) {
        if (applicationRealPath != null && !applicationRealPath.endsWith(File.separator)) {
            applicationRealPath = applicationRealPath + File.separator;
        }
        this.applicationRealPath = applicationRealPath;
    }

    public Manifest getApplicationManifest() {
        return applicationManifest;
    }

    @Override
    public String getApplicationRealPath() {
        return applicationRealPath;
    }

    @Override
    public String getBuildNumberWithType() {
        return getBuildNumber() + "-" + (isStandalone() ? "ST" : "OS");
    }

    @Override
    public long getBuildTimestamp() {
        if (buildTimestamp < 0) {
            String value = getMainAttribute(ATTRIBUTE_IMPLEMENTATION_BUILD_TIMESTAMP);
            if (StringUtils.isNotEmpty(value)) {
                long timestamp = NumberUtils.toLong(value, 0);
                buildTimestamp = timestamp < 0 ? 0L : timestamp;
            } else {
                buildTimestamp = 0L;
            }
        }
        return buildTimestamp;
    }

    @Override
    public String getBuildNumber() {
        if (buildNumber == null) {
            buildNumber = getProjectVersion() + "." + getRevision();
        }
        return buildNumber;
    }

    @Override
    public String getBuildTime() {
        if (buildTime == null) {
            String value = getMainAttribute(ATTRIBUTE_IMPLEMENTATION_BUILD_TIMESTAMP);
            long time = -1L;
            if (StringUtils.isNotEmpty(value)) {
                time = NumberUtils.toLong(value, -1L);
            }
            if (time < 0) {
                buildTime = StringUtils.EMPTY;
            } else {
                buildTime = dateFormat.format(new Date(time));
            }
        }
        return buildTime;
    }

    /**
     * get an attribute value of the manifest
     *
     * @param name
     *            name of the attribute value
     * @return the value
     */
    private String getMainAttribute(String name) {
        return applicationManifest == null ? StringUtils.EMPTY : applicationManifest
                .getMainAttributes().getValue(name);
    }

    @Override
    public String getProjectVersion() {
        if (projectVersion == null) {
            projectVersion = getMainAttribute(ATTRIBUTE_PROJECT_VERSION);
            if (StringUtils.isNotEmpty(projectVersion)) {
                projectVersion = projectVersion.replace("-SNAPSHOT", StringUtils.EMPTY);
            }
        }
        return projectVersion;
    }

    @Override
    public String getRevision() {
        if (revision == null) {
            revision = getMainAttribute(ATTRIBUTE_IMPLEMENTATION_BUILD);
        }
        return revision;
    }

    @Override
    public boolean isStandalone() {
        return true;
    }

    @Override
    public ApplicationInformation load() {
        this.applicationManifest = readManifest(applicationRealPath);
        return this;
    }

    /**
     * Read the manifest and set it.
     *
     * @param applicationRealPath
     *            file system path to web application
     * @return the manifest
     */
    private Manifest readManifest(String applicationRealPath) {
        Manifest manifest = new Manifest();
        FileInputStream fis = null;
        File manifestFile = new File(applicationRealPath, "META-INF/MANIFEST.MF");
        try {

            fis = new FileInputStream(manifestFile);
            manifest.read(fis);

            fis.close();
        } catch (Exception e) {
            throw new ApplicationInitializationException("Reading manifest "
                    + manifestFile.getAbsolutePath() + " failed");
        } finally {
            IOHelper.close(fis);
        }
        return manifest;

    }

}
