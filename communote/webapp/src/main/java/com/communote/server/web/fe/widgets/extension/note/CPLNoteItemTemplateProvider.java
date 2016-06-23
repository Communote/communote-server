package com.communote.server.web.fe.widgets.extension.note;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.fe.widgets.extension.WidgetExtension;


/**
 * Extension for the ChronologicalPostList widget which provides means to render the HTML of a note
 * with another template.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class CPLNoteItemTemplateProvider implements
        WidgetExtension<CPLNoteItemTemplateProvider, CPLNoteItemTemplateProviderManagement> {

    @Override
    public Class<CPLNoteItemTemplateProviderManagement> getManagementType() {
        return CPLNoteItemTemplateProviderManagement.class;
    }

    /**
     * Return the identifier of a template that should be used to render the HTML of the provided
     * note.
     * 
     * @param data
     *            object holding details of the note
     * @return the template identifier or null if the note should be rendered with the default
     *         template
     */
    public abstract String getNoteItemTemplate(NoteData data);
}
