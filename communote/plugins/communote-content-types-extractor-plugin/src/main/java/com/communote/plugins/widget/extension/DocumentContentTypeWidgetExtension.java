package com.communote.plugins.widget.extension;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.fe.widgets.type.ContentTypeWidgetExtension;


/**
 * ContentType for documents, like pdf or word.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
public class DocumentContentTypeWidgetExtension extends ContentTypeWidgetExtension {

    /**
     * Constructor.
     */
    public DocumentContentTypeWidgetExtension() {
        super("document", "type.document",
                "['Note','com.communote','contentTypes.document','document','EQUALS']", 100,
                CATEGORY_NOTE_LIST);
    }
}
