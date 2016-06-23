package com.communote.server.web.fe.widgets.extension.note;

import java.util.Map;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.fe.widgets.blog.ChronologicalPostListWidget;
import com.communote.server.web.fe.widgets.extension.WidgetExtension;

/**
 * Extension for the {@link ChronologicalPostListWidget} which allows adding meta-data to a note
 * which should be available in the JavaScript frontend, for example in a NoteActionHandler or a
 * note click handler.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class CPLNoteMetaDataProvider implements
        WidgetExtension<CPLNoteMetaDataProvider, CPLNoteMetaDataProviderManagement> {

    /**
     * Add additional meta-data for the given note. The meta-data, which is transported in JSON to
     * the JavaScript frontend, can be retrieved from the JavaScript widget instance by invoking
     * getNoteMetaData(noteId). For performance reasons implementors are advised to only add String,
     * Boolean and Number meta-data values to the map.
     * 
     * @param requestParameters
     *            the parameters of the current request (e.g. full or partial widget refresh)
     * @param data
     *            object holding details of the note
     * @param metaData
     *            mapping from meta data key to value. Values should be of a simple type like
     *            String, Number and Boolean.
     */
    public abstract void addMetaData(Map<String, String> requestParameters, NoteData data,
            Map<String, Object> metaData);

    @Override
    public Class<CPLNoteMetaDataProviderManagement> getManagementType() {
        return CPLNoteMetaDataProviderManagement.class;
    }

}
