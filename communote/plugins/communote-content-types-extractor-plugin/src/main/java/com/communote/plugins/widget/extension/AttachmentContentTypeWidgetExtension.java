package com.communote.plugins.widget.extension;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.fe.widgets.type.ContentTypeWidgetExtension;

/**
 * ContentType for attachments.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
public class AttachmentContentTypeWidgetExtension extends ContentTypeWidgetExtension {
    /**
     * Constructor.
     */
    public AttachmentContentTypeWidgetExtension() {
        super("attachment", "type.attachment",
                "['Note','com.communote','contentTypes.attachment','attachment','EQUALS']", 0,
                CATEGORY_NOTE_LIST);
    }
}
