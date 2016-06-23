package com.communote.server.web.fe.widgets.type;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.web.fe.widgets.extension.WidgetExtensionManagement;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ContentTypeWidgetExtensionManagement extends
        WidgetExtensionManagement<ContentTypeWidgetExtension, ContentTypeWidgetExtensionManagement> {
    /**
     * Method to get all extensions for the given categeries.
     * 
     * @param categories
     *            The categories. If no category is given, the default category will be used
     *            (CATEGORY_DEFAULT)
     * @return Extensions for the given categories.
     */
    public List<ContentTypeWidgetExtension> getExtensions(String... categories) {
        if (categories.length == 0) {
            categories = new String[] { ContentTypeWidgetExtension.CATEGORY_DEFAULT };
        }
        List<ContentTypeWidgetExtension> extensions = new ArrayList<ContentTypeWidgetExtension>();
        for (ContentTypeWidgetExtension extension : getExtensions()) {
            for (String category : categories) {
                if (extension.getCategories().contains(category)) {
                    extensions.add(extension);
                    continue;
                }
            }
        }
        return extensions;
    }
}
