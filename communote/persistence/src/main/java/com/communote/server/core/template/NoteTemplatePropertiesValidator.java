package com.communote.server.core.template;

import java.util.Map;

/**
 * Validator to ensure that the note template properties are complete and correct.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> *
 */
public interface NoteTemplatePropertiesValidator {

    /**
     * Validates the template properties for the template identified by the given ID. In case the
     * validation fails an exception will be thrown.
     * 
     * @param templateId
     *            the ID of the template definition for which the properties should be validated
     * @param templatePropertiesJSON
     *            the JSON object, as nested map, holding the properties that should be validated,
     *            can be null
     * @throws NoteTemplatePropertiesValidationException
     *             in case the validation failed
     */
    void validate(String templateId, Map<String, Object> templatePropertiesJSON)
            throws NoteTemplatePropertiesValidationException;
}
