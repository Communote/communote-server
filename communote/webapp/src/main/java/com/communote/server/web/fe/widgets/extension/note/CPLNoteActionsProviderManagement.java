package com.communote.server.web.fe.widgets.extension.note;

import java.util.List;
import java.util.Map;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.fe.widgets.extension.WidgetExtensionManagement;

/**
 * Manager for {@link CPLNoteActionsProvider}s.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CPLNoteActionsProviderManagement extends
        WidgetExtensionManagement<CPLNoteActionsProvider, CPLNoteActionsProviderManagement> {

    /**
     * Add the names of the actions which should be available for the given note by invoking the
     * registered providers.
     * 
     * @param requestParameters
     *            the parameters of the current request (e.g. full or partial widget refresh)
     * @param data
     *            object holding details of the note
     * @param actions
     *            the list to add the actions to.
     */
    public void addActions(Map<String, String> requestParameters, NoteData data,
            List<String> actions) {
        for (CPLNoteActionsProvider provider : getExtensions()) {
            if (!provider.addNoteActions(requestParameters, data, actions)) {
                return;
            }
        }
    }

}
