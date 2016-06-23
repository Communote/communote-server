package com.communote.server.web.api.service;

import org.apache.commons.lang.StringUtils;

/**
 * Exception if a requested resource has not been found for the API
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RequestedResourceNotFoundException extends ApiException {
    private static final long serialVersionUID = 1L;

    private String resourceType;
    private String resourceId;

    /**
     * @param resourceType
     *            the type of the resource
     * @param resourceId
     *            the id of the not found resource
     * @param message
     *            an error message
     */
    public RequestedResourceNotFoundException(String resourceType, String resourceId, String message) {
        super(message);
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return "No resource found of type=" + resourceType + " for id='" + resourceId + "'! "
                + (super.getMessage() != null ? super.getMessage() : StringUtils.EMPTY);
    }

    /**
     * @return the non existing resource id
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * @return the resource type
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * @param resourceId
     *            the non existing resource id
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * @param resourceType
     *            the resource type
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

}
