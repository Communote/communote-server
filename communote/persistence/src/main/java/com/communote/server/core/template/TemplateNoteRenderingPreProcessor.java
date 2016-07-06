package com.communote.server.core.template;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteContentRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;

/**
 * RenderingPreProcessor which replaces the content of a template note with the result of the
 * rendered template that is associated with that note. A template note is a note with a specific
 * note property. The rendering will take the current render mode and locale into account.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TemplateNoteRenderingPreProcessor implements NoteContentRenderingPreProcessor {

    private NoteTemplateService noteTemplateService;

    /**
     * @return the lazily initialized template service
     */
    private NoteTemplateService getNoteTemplateService() {
        if (noteTemplateService == null) {
            noteTemplateService = ServiceLocator.instance().getService(NoteTemplateService.class);
        }
        return noteTemplateService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return NoteMetadataRenderingPreProcessor.DEFAULT_ORDER + 1;
    }

    @Override
    public boolean isCachable() {
        // note cachable since it modifies the content in a locale specific way
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean processNoteContent(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException {
        boolean modified = false;
        // directly look for property in NoteData because the converter adds it
        try {
            StringPropertyTO noteTemplateIdProperty = PropertyHelper
                    .getPropertyTO(item.getObjectProperties(),
                            PropertyManagement.KEY_GROUP,
                            NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID);
            // get template properties which is a serialized JSON object
            StringPropertyTO noteTemplatePropsProperty = PropertyHelper.getPropertyTO(
                    item.getObjectProperties(), PropertyManagement.KEY_GROUP,
                    NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_PROPERTIES);
            NoteTemplateDefinition definition = getNoteTemplateService()
                    .getDefinition(noteTemplateIdProperty.getPropertyValue());
            if (definition != null) {
                String propertiesJson = noteTemplatePropsProperty == null ? null
                        : noteTemplatePropsProperty.getPropertyValue();
                // don't validate because we expect this was done while storing in database
                String content = getNoteTemplateService().renderTemplate(
                        noteTemplateIdProperty.getPropertyValue(), propertiesJson,
                        context, false);
                // TODO should we wrap the content in a P tag?
                item.setContent(content);
                // remove short content because it could be in wrong locale and mode. Creating
                // short content on the fly would be too expensive.
                item.setShortContent(null);
            }
            if (noteTemplatePropsProperty != null) {
                // remove the serialized json from the object properties because no one else
                // needs it
                // TODO good idea? well, saves bandwidth when using REST API
                item.getObjectProperties().remove(noteTemplatePropsProperty);
            }
            modified = true;
        } catch (NoteTemplateNotFoundException e) {
            // just return the unmodified item
        } catch (NoteTemplateRenderException e) {
            // TODO or should we just return the unmodified note?
            throw new NoteRenderingPreProcessorException(e.getMessage(), e);
        }
        return modified;
    }

    @Override
    public boolean replacesContent() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(NoteRenderMode mode, NoteData item) {
        // only support notes that are template notes
        return PropertyHelper.getPropertyTO(item.getObjectProperties(),
                PropertyManagement.KEY_GROUP, NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID) != null;
    }

}
