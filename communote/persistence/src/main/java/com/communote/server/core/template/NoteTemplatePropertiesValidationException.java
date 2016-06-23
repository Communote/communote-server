package com.communote.server.core.template;

/**
 * Thrown to indicate that the template properties are complete or correct
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteTemplatePropertiesValidationException extends NoteTemplateRenderException {

    /**
     * the serial version UID
     */
    private static final long serialVersionUID = 5951065874390419779L;

    /**
     * Creates a new instance with detail message and optional cause
     * 
     * @param templateId
     *            the ID of the template definition for which the properties validation failed
     * @param message
     *            the detail message
     * @param cause
     *            the cause, can be null
     */
    public NoteTemplatePropertiesValidationException(String templateId, String message,
            Throwable cause) {
        super(templateId, message, cause);
    }

}
