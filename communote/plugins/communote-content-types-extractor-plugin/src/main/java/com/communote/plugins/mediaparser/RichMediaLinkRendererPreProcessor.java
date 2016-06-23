package com.communote.plugins.mediaparser;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;

/**
 * Implementation of the NoteRenderingPreProcessor which extracts all links for rich media in a
 * note.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RichMediaLinkRendererPreProcessor implements NoteMetadataRenderingPreProcessor {

    private static final String PROPERTY_RICHMEDIA = "richmedia-content";

    private final static RichMediaExtractor EXTRACTOR = new RichMediaExtractor();

    /**
     * @return {@link NoteMetadataRenderingPreProcessor#DEFAULT_ORDER}
     */
    @Override
    public int getOrder() {
        return NoteMetadataRenderingPreProcessor.DEFAULT_ORDER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(NoteRenderContext context, NoteData item) {
        String content = item.getContent();
        if (StringUtils.isBlank(content)) {
            return false;
        }
        Collection<RichMediaDescription> richMediaDescriptions = EXTRACTOR
                .getRichMediaDescriptions(content);

        if (!richMediaDescriptions.isEmpty()) {
            if (item.getProperty(PROPERTY_RICHMEDIA) == null) {
                item.setProperty(PROPERTY_RICHMEDIA, richMediaDescriptions);
            } else {
                Collection<RichMediaDescription> property = item.getProperty(PROPERTY_RICHMEDIA);
                property.addAll(richMediaDescriptions);
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(NoteRenderMode noteRenderMode) {
        return NoteRenderMode.PORTAL == noteRenderMode;
    }
}