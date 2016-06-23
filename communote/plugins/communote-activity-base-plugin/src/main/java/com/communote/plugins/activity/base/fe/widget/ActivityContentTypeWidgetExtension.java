package com.communote.plugins.activity.base.fe.widget;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.fe.widgets.type.ContentTypeWidgetExtension;


/**
 * ContentType for activities.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
public class ActivityContentTypeWidgetExtension extends ContentTypeWidgetExtension {
    /**
     * Constructor.
     */
    public ActivityContentTypeWidgetExtension() {
        super(
                "activity",
                "plugins.activity.content.type",
                "['Note','com.communote.plugins.communote-plugin-activity-base',"
                        + "'contentTypes.activity','activity','EQUALS']",
                1,
                CATEGORY_NOTE_LIST);
    }
}
