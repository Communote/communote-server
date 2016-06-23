package com.communote.plugins.widget.extension;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.fe.widgets.type.ContentTypeWidgetExtension;


/**
 * ContentType for images.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
public class ImageContentTypeWidgetExtension extends ContentTypeWidgetExtension {
    /**
     * Constructor.
     */
    public ImageContentTypeWidgetExtension() {
        super("image", "type.image",
                "['Note','com.communote','contentTypes.image','image','EQUALS']", 1000,
                CATEGORY_NOTE_LIST);
    }
}
