package com.communote.plugins.activity.base.processor;

import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * Exception, which is thrown when an activity is set to be created, but deactivated.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivityDeactivatedNotePreProcessorException extends NoteStoringPreProcessorException {

    private static final long serialVersionUID = -6630225285012646865L;
    private final String templateId;

    /**
     * Constructor.
     * 
     * @param templateId
     *            Id of the deactivated activity's template.
     */
    public ActivityDeactivatedNotePreProcessorException(String templateId) {
        super("Activities of type '" + templateId
                + "' are disabled, though creation will be aborted.");
        this.templateId = templateId;
    }

    /**
     * @return the templateId
     */
    public String getTemplateId() {
        return templateId;
    }

}
