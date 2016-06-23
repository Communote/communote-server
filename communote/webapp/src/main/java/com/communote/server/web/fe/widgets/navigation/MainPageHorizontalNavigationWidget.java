package com.communote.server.web.fe.widgets.navigation;

import java.util.HashMap;

import com.communote.common.util.ParameterHelper;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget that shows a horizontal navigation that allows activating different views for a context.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MainPageHorizontalNavigationWidget extends AbstractWidget {

    private HashMap<String, String> customLabels;
    private HashMap<String, String> knownLabels;
    private String contextId;

    /**
     * @return the ID of the current context
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Return the text that should be rendered as label for a given viewId. The label will be taken
     * from the customLabels request parameter if provided or loaded from the i18n language packs
     * using a key like mainpage.horizontal.navigation.[contextId].[viewId]
     * 
     * @param viewId
     *            the ID of the view that should be labeled
     * @return the label
     */
    public String getLabel(String viewId) {
        String label = null;
        // little optimization, since it is called more than once
        label = knownLabels.get(viewId);
        if (label == null) {
            if (customLabels != null) {
                label = customLabels.get(contextId + "." + viewId);
            }
            if (label == null) {
                label = MessageHelper.getText(getRequest(), "mainpage.horizontal.navigation."
                        + contextId + "." + viewId);
            }
            knownLabels.put(viewId, label);
        }
        return label;
    }

    @Override
    public String getTile(String outputType) {
        return "core.widget.navigation.mainPageHorizontal";
    }

    @Override
    public Object handleRequest() {
        parseCustomLabels();
        contextId = getParameter("contextId");
        String[] viewIds = ParameterHelper.getParameterAsStringArray(getParameters(), "viewIds",
                ",");
        if (viewIds == null) {
            return new String[0];
        } else {
            knownLabels = new HashMap<>();
        }
        return viewIds;
    }

    @Override
    protected void initParameters() {
    }

    /**
     * Check for custom labels which will override the default label that is retrieved from i18n
     * language pack. The customLabels request parameter is expected to have the format
     * contextId.viewId:label,contextId.viewId:label,...
     */
    private void parseCustomLabels() {
        String labelsString = getParameter("customLabels");
        if (labelsString != null) {
            customLabels = new HashMap<String, String>();
            String[] labels = labelsString.split(",");
            for (String label : labels) {
                // expect label encoded as contextId.viewId:label
                String[] mapping = label.split(":");
                if (mapping.length == 2) {
                    customLabels.put(mapping[0].trim(), mapping[1].trim());
                }
            }
        }
    }
}