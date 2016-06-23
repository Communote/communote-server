package com.communote.server.web.commons.resolver;

import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationProperty;

/**
 * A multipart resolver that calculates the maxUploadSize from the configured attachment upload
 * limit and an additional configurable offset.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteMultipartResolver extends CommonsMultipartResolver {

    private long maxSizeOffset = 1000000;
    private long maxFileUploadSize;

    /**
     * Default constructor.
     */
    public CommunoteMultipartResolver() {
        long maxUploadSize;
        if (CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                .isInstallationDone()) {
            maxUploadSize = CommunoteRuntime
                    .getInstance()
                    .getConfigurationManager()
                    .getApplicationConfigurationProperties()
                    .getProperty(ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE,
                            ApplicationProperty.DEFAULT_ATTACHMENT_MAX_UPLOAD_SIZE);
        } else {
            maxUploadSize = ApplicationProperty.DEFAULT_ATTACHMENT_MAX_UPLOAD_SIZE;
        }
        this.setMaxUploadSize(maxUploadSize);
    }

    /**
     * Overrides the default offset (1000000 bytes) to be added to the file upload limit for
     * defining the maximum size of a multipart request.
     *
     * @param maxSizeOffset
     *            the maxSizeOffset to set
     */
    public void setMaxSizeOffset(long maxSizeOffset) {
        this.maxSizeOffset = maxSizeOffset;
        this.setMaxUploadSize(maxFileUploadSize + this.maxSizeOffset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxUploadSize(long maxUploadSize) {
        maxFileUploadSize = maxUploadSize;
        super.setMaxUploadSize(maxUploadSize + this.maxSizeOffset);
    }
}
