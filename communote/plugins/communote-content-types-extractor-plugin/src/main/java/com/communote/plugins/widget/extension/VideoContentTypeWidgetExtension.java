package com.communote.plugins.widget.extension;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.fe.widgets.type.ContentTypeWidgetExtension;


/**
 * ContentType for videos.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
public class VideoContentTypeWidgetExtension extends ContentTypeWidgetExtension {
    /**
     * Constructor.
     */
    public VideoContentTypeWidgetExtension() {
        super("video", "type.video",
                "['Note','com.communote','contentTypes.richMedia','richMedia','EQUALS']", 0,
                CATEGORY_NOTE_LIST);
    }
}
