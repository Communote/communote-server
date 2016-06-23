package com.communote.server.web.fe.widgets.type;

import java.util.List;

import com.communote.server.web.WebServiceLocator;
import com.communote.server.widgets.AbstractWidget;

/**
 * Controller for the the Widget to list workspaces.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ContentTypeWidget extends AbstractWidget {

    private static final String PARAMETER_CATEGORIES = "categories";

    private final ContentTypeWidgetExtensionManagement extensionManagement = WebServiceLocator
            .instance().getWidgetExtensionManagementRepository()
            .getExtensionManagement(ContentTypeWidgetExtensionManagement.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.content.type";
    }

    /**
     * @return A list of {@link ContentTypeWidgetExtension}.
     */
    @Override
    public Object handleRequest() {
        List<ContentTypeWidgetExtension> extensions = extensionManagement
                .getExtensions(getStringArrayParameter(PARAMETER_CATEGORIES));
        return extensions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        setParameter(PARAMETER_CATEGORIES, ContentTypeWidgetExtension.CATEGORY_DEFAULT);
    }
}
