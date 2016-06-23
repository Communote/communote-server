package com.communote.plugins.widget.extension;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.fe.widgets.extension.WidgetExtension;
import com.communote.server.web.fe.widgets.type.ContentTypeWidgetExtension;
import com.communote.server.web.fe.widgets.type.ContentTypeWidgetExtensionManagement;


/**
 * ContentType for discussion/conversations.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
// We implement WidgetExtension here explicitly, else maven-bundle-plugin wouldn't import it and the
// plugin couldn't be deployed.
public class DiscussionContentTypeWidgetExtension extends ContentTypeWidgetExtension implements
        WidgetExtension<ContentTypeWidgetExtension, ContentTypeWidgetExtensionManagement> {
    /**
     * Constructor.
     */
    public DiscussionContentTypeWidgetExtension() {
        super("conversation", "type.conversation",
                "['Note','com.communote','contentTypes.discussion','discussion','EQUALS']", 0,
                CATEGORY_DEFAULT);
    }
}
