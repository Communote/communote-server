package com.communote.server.core.vo.query;

import com.communote.server.model.attachment.AttachmentConstants;
import com.communote.server.model.note.ContentConstants;

/**
 * Enum of available resource types which can be used to restrict query results to resources having
 * this type.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum ResourceTypeRestrictionEnum {

    /**
     * represents the content resources
     */
    Attachment(AttachmentConstants.CLASS_NAME),
    /**
     * represents text resources (blog posts)
     */
    Content(ContentConstants.CLASS_NAME);

    private String className;

    /**
     * @param className
     */
    ResourceTypeRestrictionEnum(String className) {
        this.className = className;
    }

    /**
     * Returns the class name of a resource type
     * 
     * @return the class name
     */
    public String getTypeClassName() {
        return className;
    }

}
