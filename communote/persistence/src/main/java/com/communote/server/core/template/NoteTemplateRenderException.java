package com.communote.server.core.template;

/**
 * Thrown to indicate that a note template could not be rendered
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> *
 */
public class NoteTemplateRenderException extends Exception {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -4455270931784622736L;

    private final String templateId;

    /**
     * Creates a new exception with detail message and cause.
     * 
     * @param templateId
     *            the ID of the template definition whose template couldn't be rendered
     * @param message
     *            the detail message
     * @param cause
     *            the cause, can be null
     */
    public NoteTemplateRenderException(String templateId, String message, Throwable cause) {
        super(message, cause);
        this.templateId = templateId;
    }

    /**
     * Creates a new exception with cause.
     * 
     * @param templateId
     *            the ID of the template definition whose template couldn't be rendered
     * @param cause
     *            the cause, can be null
     */
    public NoteTemplateRenderException(String templateId, Throwable cause) {
        this(templateId, "The template with ID " + templateId + " couldn't be rendered", cause);

    }

    /**
     * @return the ID of the template definition whose template couldn't be rendered
     */
    public String getTemplateId() {
        return templateId;
    }
}
