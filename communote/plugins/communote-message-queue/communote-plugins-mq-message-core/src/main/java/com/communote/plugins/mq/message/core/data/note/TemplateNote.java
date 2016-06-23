package com.communote.plugins.mq.message.core.data.note;

import java.util.Arrays;
import java.util.HashSet;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.core.template.NoteTemplateService;

/**
 * POJO for easier creation of template and activity notes
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TemplateNote extends Note {

    private String templateId;
    private String templateProperties;
    private StringProperty[] finalProperties;
    private boolean activity;
    private StringProperty[] properties;

    @Override
    public StringProperty[] getProperties() {
        if (finalProperties == null) {
            HashSet<StringProperty> mergedProperties = new HashSet<StringProperty>();
            if (properties != null) {
                mergedProperties.addAll(Arrays.asList(properties));
            }
            if (templateId != null && templateId.length() > 0) {
                StringProperty property = new StringProperty();
                property.setGroup(PropertyManagement.KEY_GROUP);
                property.setKey(NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID);
                property.setValue(templateId);
                mergedProperties.add(property);
                if (templateProperties != null && templateProperties.length() > 0) {
                    property = new StringProperty();
                    property.setGroup(PropertyManagement.KEY_GROUP);
                    property.setKey(NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_PROPERTIES);
                    property.setValue(templateProperties);
                    mergedProperties.add(property);
                }
                // can't add the activity property here because we have no access to the group of
                // the property
            }

            finalProperties = mergedProperties.toArray(new StringProperty[0]);
        }
        return finalProperties;
    }

    @Override
    @JsonIgnore(value = false)
    public boolean isActivity() {
        return activity;
    }

    /**
     * Set whether the note is an activity.
     * 
     * @param activity
     *            true if the note is an activity
     */
    public void setActivity(boolean activity) {
        this.activity = activity;
    }

    @Override
    public void setProperties(StringProperty[] properties) {
        this.properties = properties;
        this.finalProperties = null;
    }

    /**
     * Set the ID of the template/activity that should be used for the note. If called, the note
     * property required for creating a template note will be added automatically to the properties
     * returned by {@link #getProperties()}.
     * 
     * @param templateId
     *            the ID of the template
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
        this.finalProperties = null;
    }

    /**
     * Set the properties that should be passed to the template engine. The value is expected to be
     * a string in JSON. If called, the note property holding the templateProperties will be added
     * automatically to the properties returned by {@link #getProperties()}.
     * 
     * @param templateProperties
     *            the properties to passed to the template engine
     */
    public void setTemplateProperties(String templateProperties) {
        this.templateProperties = templateProperties;
        this.finalProperties = null;
    }
}
