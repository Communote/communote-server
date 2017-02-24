package com.communote.server.core.template;

import java.util.Locale;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringEditableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.model.note.Note;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Note storing pre-processor which will set the default content of a template note by rendering the
 * template with default language and plain text render mode.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TemplateNoteStoringPreProcessor implements NoteStoringEditableContentPreProcessor {

    private NoteTemplateService templateService;

    @Override
    public int getOrder() {
        return NoteStoringImmutableContentPreProcessor.DEFAULT_ORDER + 1;
    }

    /**
     * @return the lazily initialized template service
     */
    private NoteTemplateService getTemplateService() {
        if (templateService == null) {
            templateService = ServiceLocator.instance().getService(NoteTemplateService.class);
        }
        return templateService;
    }

    @Override
    public boolean isProcessAutosave() {
        return false;
    }

    @Override
    public NoteStoringTO process(NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        StringPropertyTO templateIdProperty = PropertyHelper.getPropertyTO(
                noteStoringTO.getProperties(), PropertyManagement.KEY_GROUP,
                NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID);
        if (templateIdProperty != null) {
            StringPropertyTO templatePropsProperty = PropertyHelper.getPropertyTO(
                    noteStoringTO.getProperties(), PropertyManagement.KEY_GROUP,
                    NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_PROPERTIES);
            String templatePropertiesJSON = templatePropsProperty == null ? null
                    : templatePropsProperty.getPropertyValue();
            // it's a template note, set content to template rendered with default locale and plain
            // render mode
            Locale locale = ClientHelper.getDefaultLanguage();
            NoteRenderContext context = new NoteRenderContext(NoteRenderMode.PLAIN, locale);
            try {
                String rendered = getTemplateService().renderTemplate(
                        templateIdProperty.getPropertyValue(), templatePropertiesJSON, context,
                        true);
                noteStoringTO.setContent(rendered);
                noteStoringTO.setContentType(NoteContentType.PLAIN_TEXT);
            } catch (NoteTemplateNotFoundException e) {
                throw new NoteStoringPreProcessorException("The referenced template "
                        + templateIdProperty.getPropertyValue() + " is not registered", e);
            } catch (NoteTemplateRenderException e) {
                throw new NoteStoringPreProcessorException(
                        "Rendering the default content of the template note failed", e);
            }
        }
        return noteStoringTO;
    }

    @Override
    public NoteStoringTO processEdit(Note noteToEdit, NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        return process(noteStoringTO);
    }
}
