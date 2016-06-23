package com.communote.server.web.commons.resource.impl;

import com.communote.common.io.DelimiterFilterMatchProcessor;

/**
 * Filter match processor that is aware of the currently processed resource
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class ResourceFilterMatchProcessor implements DelimiterFilterMatchProcessor {

    private ResourceDescriptor processedResource;

    /**
     * @return the currently processed resource
     */
    public ResourceDescriptor getProcessedResource() {
        return processedResource;
    }

    /**
     * Set the currently processed resource
     * 
     * @param processedResource
     *            the resource to be processed
     */
    public void setProcessedResource(ResourceDescriptor processedResource) {
        this.processedResource = processedResource;
    }

}
