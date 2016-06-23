package com.communote.server.web.fe.widgets.extension.note;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.view.TemplateManager;
import com.communote.server.web.fe.widgets.extension.WidgetExtensionManagement;


/**
 * Manager for the CPLNoteItemTemplateProvider extension.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CPLNoteItemTemplateProviderManagement
        extends
        WidgetExtensionManagement<CPLNoteItemTemplateProvider, CPLNoteItemTemplateProviderManagement> {

    private TemplateManager templateResolver;

    /**
     * Return the location of the template that should be used to render the HTML of the provided
     * note. This method will check all registered providers and will return the first template
     * found.
     * 
     * @param data
     *            object holding details of the note
     * @return the file location of the template or null if the note should be rendered with the
     *         default template
     */
    public String getNoteItemTemplate(NoteData data) {
        String template = null;
        for (CPLNoteItemTemplateProvider provider : getExtensions()) {
            template = provider.getNoteItemTemplate(data);
            if (template != null) {
                template = getTemplateResolver().getTemplate(template);
                break;
            }
        }
        return template;
    }

    /**
     * @return the lazily initialized resolver
     */
    private TemplateManager getTemplateResolver() {
        if (templateResolver == null) {
            templateResolver = WebServiceLocator.instance().getService(TemplateManager.class);
        }
        return templateResolver;
    }
}
