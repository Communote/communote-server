package com.communote.server.core.template;

import com.communote.server.api.core.common.NotFoundException;

/**
 * Thrown to indicate that a requested note template definition does not exist.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> *
 */
public class NoteTemplateNotFoundException extends NotFoundException {
    private final String templateId;
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 5793503128508135066L;

    /**
     * Creates a new exception
     * 
     * @param templateId
     *            the ID of the template definition that was not found
     */
    public NoteTemplateNotFoundException(String templateId) {
        super("NoteTemplateDefinition with ID " + templateId + " was not found.");
        this.templateId = templateId;

    }

    /**
     * @return the ID of the template definition that was not found
     */
    public String getTemplateId() {
        return templateId;
    }

}
