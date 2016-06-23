package com.communote.server.web.fe.widgets.extension;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.web.WebServiceLocator;
import com.communote.server.widgets.SimpleWidgetFactory;
import com.communote.server.widgets.Widget;
import com.communote.server.widgets.WidgetCreationException;
import com.communote.server.widgets.WidgetFactory;

/**
 * Factory that uses the {@link WidgetFactoryRegistry} to create widgets provided by plugins. The
 * factory uses the registry whenever a widget with a widget group which starts with
 * <code>plugin/</code> should be created. If a group does not start with this prefix the widget
 * creation is delegated to the parent class. The bundle name of the plugin whose factory should be
 * used can be provided as part of the widget group or as a request parameter named
 * <code>bundleName</code>. In case it is part of the widget group it is expected to follow the
 * <code>plugin/</code> prefix. If there is no factory for the bundle the widget creation fails.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExtendableWidgetFactory extends SimpleWidgetFactory {

    private static final String PLUGIN_WIDGET_GROUP_PREFIX = "plugin/";
    private static final String PARAM_BUNDLE_NAME = "bundleName";
    private WidgetFactoryRegistry widgetFactoryRegistry;

    /**
     * Create a widget using a factory registered to the WidgetFactory
     *
     * @param localWidgetGroupName
     *            the group of the widget without the leading 'plugin/'
     * @param widgetName
     *            the name of the widget
     * @param requestParameters
     *            the current request parameters, which will be checked for the bundleName
     * @return the created widget
     * @throws WidgetCreationException
     *             in case there was no factory for the bundle or the factory could not create the
     *             widget
     */
    private Widget createPluginWidget(String localWidgetGroupName, String widgetName,
            Map<String, String[]> requestParameters) throws WidgetCreationException {
        String[] bundleNameParamValue = requestParameters.get(PARAM_BUNDLE_NAME);
        String bundleName = null;
        // just take first parameter value
        if (bundleNameParamValue != null && bundleNameParamValue.length > 0) {
            bundleName = bundleNameParamValue[0];
        }
        if (StringUtils.isBlank(bundleName)) {
            // expect identifier to be first element in group
            int idx = localWidgetGroupName.indexOf('/');
            if (idx > 0) {
                bundleName = localWidgetGroupName.substring(0, idx);
                localWidgetGroupName = idx + 1 < localWidgetGroupName.length() ? localWidgetGroupName
                        .substring(idx + 1) : StringUtils.EMPTY;
            } else if (idx == -1) {
                bundleName = localWidgetGroupName;
                localWidgetGroupName = StringUtils.EMPTY;
            }

        }
        WidgetFactory factory = getWidgetFactoryRegistry().getWidgetFactory(bundleName);
        if (factory == null) {
            throw new WidgetCreationException("No widget factory for bundle " + bundleName
                    + " found");
        }
        return factory.createWidget(localWidgetGroupName, widgetName, requestParameters);
    }

    @Override
    public Widget createWidget(String widgetGroupName, String widgetName,
            Map<String, String[]> requestParameters) throws WidgetCreationException {
        if (widgetGroupName.startsWith(PLUGIN_WIDGET_GROUP_PREFIX)) {
            int pluginLocalWidgetGroupStartIdx = PLUGIN_WIDGET_GROUP_PREFIX.length();
            widgetGroupName = widgetGroupName.length() > pluginLocalWidgetGroupStartIdx ? widgetGroupName
                    .substring(pluginLocalWidgetGroupStartIdx) : StringUtils.EMPTY;
            return createPluginWidget(widgetGroupName, widgetName, requestParameters);
        }
        return super.createWidget(widgetGroupName, widgetName, requestParameters);
    }

    /**
     * @return the lazily fetched widget factory registry
     */
    private WidgetFactoryRegistry getWidgetFactoryRegistry() {
        if (widgetFactoryRegistry == null) {
            widgetFactoryRegistry = WebServiceLocator.instance().getService(
                    WidgetFactoryRegistry.class);
        }
        return widgetFactoryRegistry;
    }
}
