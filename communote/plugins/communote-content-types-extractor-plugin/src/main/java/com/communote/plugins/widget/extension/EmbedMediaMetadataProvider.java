package com.communote.plugins.widget.extension;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.mediaparser.RichMediaDescription;
import com.communote.plugins.mediaparser.RichMediaLinkRendererPreProcessor;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.fe.widgets.extension.note.CPLNoteMetaDataProvider;

@Component
@Provides
@Instantiate
public class EmbedMediaMetadataProvider extends CPLNoteMetaDataProvider {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void addMetaData(Map<String, String> requestParameters, NoteData data,
            Map<String, Object> metaData) {
        Collection<RichMediaDescription> descriptors = data.getProperty(RichMediaLinkRendererPreProcessor.PROPERTY_RICHMEDIA);
        if (descriptors != null && !descriptors.isEmpty()) {
            Map<String, Object>[] mediaEntries = new HashMap[descriptors.size()];
            int i = 0;
            for (RichMediaDescription descriptor : descriptors) {
                HashMap<String, Object> mediaEntry = new HashMap<>();
                mediaEntry.put("type", descriptor.getMediaTypeId());
                mediaEntry.put("id", descriptor.getMediaId());
                mediaEntry.put("https", descriptor.isUseHttps());
                mediaEntries[i] = mediaEntry;
                i++;
            }
            metaData.put("embedMedia", mediaEntries);
        }

    }

}
