package com.communote.server.core.template;

import java.util.HashMap;
import java.util.Map;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.core.note.NoteRenderMode;


/**
 * Apart from user generated content notes can also have predefined content that is based on a
 * template. This object defines such a template.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class NoteTemplateDefinition {

    private final String templateId;
    private final LocalizedMessage templateName;
    private final LocalizedMessage template;
    private long expirationTimeout;
    private HashMap<NoteRenderMode, LocalizedMessage> specificTemplates;
    private NoteTemplatePropertiesValidator validator;

    /**
     * Creates a new template definition with default expiration timeout of zero.
     * 
     * @param templateId
     *            an ID which uniquely identifies the template
     * @param templateName
     *            a localizable name of the template
     * @param template
     *            the actual template. This will be the default template.
     */
    public NoteTemplateDefinition(String templateId, LocalizedMessage templateName,
            LocalizedMessage template) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.template = template;
    }

    /**
     * Creates a new template definition.
     * 
     * @param templateId
     *            an ID which uniquely identifies the template
     * @param templateName
     *            a localizable name of the template
     * @param template
     *            the actual template. This will be the default template.
     * @param expirationTimeout
     *            a timeout in milliseconds after which a note that uses this template should expire
     */
    public NoteTemplateDefinition(String templateId, LocalizedMessage templateName,
            LocalizedMessage template, long expirationTimeout) {
        this(templateId, templateName, template);
        setExpirationTimeout(expirationTimeout);
    }

    /**
     * Allows overriding the default template for other render modes.
     * 
     * @param renderMode
     *            the render mode for which the provided template should be used
     * @param template
     *            the template to use
     */
    public void addTemplate(NoteRenderMode renderMode, LocalizedMessage template) {
        if (specificTemplates == null) {
            specificTemplates = new HashMap<NoteRenderMode, LocalizedMessage>();
        }
        specificTemplates.put(renderMode, template);
    }

    /**
     * @return a timeout in milliseconds after which a note that uses this template should expire
     *         automatically. Expire in this context means that the note should be deleted
     *         automatically. A return value of zero or less means that the note won't expire.
     */
    public long getExpirationTimeout() {
        return expirationTimeout;
    }

    /**
     * @return the actual template. The template can be static or localizable text. The value of the
     *         template will be passed to a Velocity engine and thus can contain Velocity markup.
     */
    public LocalizedMessage getTemplate() {
        return template;
    }

    /**
     * Like {@link #getTemplate()} but return a template that was explicitly added via
     * {@link #addTemplate(NoteRenderMode, LocalizedMessage)} for the given renderMode. If there is
     * no template for that renderMode the default template will be returned.
     * 
     * @param renderMode
     *            the renderMode for which the template should be retrieved, if null the default
     *            template is returned.
     * @return the template
     */
    public LocalizedMessage getTemplate(NoteRenderMode renderMode) {
        LocalizedMessage template = null;
        if (renderMode == null || specificTemplates == null
                || (template = specificTemplates.get(renderMode)) == null) {
            template = getTemplate();
        }
        return template;
    }

    /**
     * @return an ID which uniquely identifies the template definition. For assigning a note with
     *         this template the templateId must be stored in the note properties.
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * @return a localizable name of the template. Could for instance be used for configuration
     *         screens and similar.
     */
    public LocalizedMessage getTemplateName() {
        return templateName;
    }

    /**
     * Test whether the definition has a template for the given render mode.
     * 
     * @param renderMode
     *            the render mode to test
     * @return true if a template was added via
     *         {@link #addTemplate(NoteRenderMode, LocalizedMessage)} for the given render mode
     */
    public boolean hasTemplate(NoteRenderMode renderMode) {
        return specificTemplates != null && specificTemplates.containsKey(renderMode);
    }

    /**
     * Removes a template previously added with
     * {@link #addTemplate(NoteRenderMode, LocalizedMessage)}. If there is no template for the
     * renderMode nothing will happen.
     * 
     * @param renderMode
     *            the renderMode for which an added template should be removed
     */
    public void removeTemplate(NoteRenderMode renderMode) {
        if (specificTemplates != null) {
            specificTemplates.remove(renderMode);
        }
    }

    /**
     * Sets the expiration timeout.
     * 
     * @param timeout
     *            a timeout in milliseconds after which a note that uses this template should expire
     *            (be deleted) automatically. A value of zero or less means no expiration.
     */
    public void setExpirationTimeout(long timeout) {
        if (timeout < 0L) {
            timeout = 0L;
        }
        this.expirationTimeout = timeout;
    }

    /**
     * Sets the validator to be used for validating the template properties.
     * 
     * @param validator
     *            the validator
     */
    public void setPropertiesValidator(NoteTemplatePropertiesValidator validator) {
        this.validator = validator;
    }

    /**
     * Validates the template properties using the validator of this definition, in case there is
     * one.
     * 
     * @param templatePropertiesJSON
     *            the JSON object, as nested map, holding the properties that should be validated,
     *            can be null. Any modifications made to the object won't be persisted.
     * @throws NoteTemplatePropertiesValidationException
     *             in case the validation failed
     */
    public void validateProperties(Map<String, Object> templatePropertiesJSON)
            throws NoteTemplatePropertiesValidationException {
        if (validator != null) {
            validator.validate(getTemplateId(), templatePropertiesJSON);
        }
    }
}
