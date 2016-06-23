package com.communote.server.web.fe.widgets.extension.note;

import java.util.Map;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.fe.widgets.extension.WidgetExtensionManagement;

/**
 * Manager for {@link CPLNoteMetaDataProvider}s.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CPLNoteMetaDataProviderManagement extends
        WidgetExtensionManagement<CPLNoteMetaDataProvider, CPLNoteMetaDataProviderManagement> {

    /**
     * Add additional meta-data for the note to the provided map by calling the registered
     * providers.
     * 
     * @param requestParameters
     *            the parameters of the current request (e.g. full or partial widget refresh)
     * @param data
     *            object holding details of the note
     * @param metaData
     *            mapping from meta data key to value
     */
    public void addMetaData(Map<String, String> requestParameters, NoteData data,
            Map<String, Object> metaData) {
        for (CPLNoteMetaDataProvider provider : getExtensions()) {
            provider.addMetaData(requestParameters, data, metaData);
        }
    }
}
