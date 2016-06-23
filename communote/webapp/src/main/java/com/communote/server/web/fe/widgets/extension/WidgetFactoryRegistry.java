package com.communote.server.web.fe.widgets.extension;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.widgets.WidgetFactory;

/**
 * Registry for dynamically adding and removing widget factories (of plugins) at runtime.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class WidgetFactoryRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetFactoryRegistry.class);
    private Map<String, WidgetFactory> widgetFactories = new HashMap<String, WidgetFactory>(0);

    /**
     * Add a plugin widget factory to the registry. If there is already a factory for the plugin the
     * call will be ignored.
     * 
     * @param identifier
     *            unique identifier of the provider of the factory, which should usually be the
     *            bundle name. This factory will only be used if a request for a widget of this
     *            provider is received.
     * @param factory
     *            the factory to register
     */
    public synchronized void addWidgetFactory(String identifier, WidgetFactory factory) {
        if (widgetFactories.containsKey(identifier)) {
            LOGGER.debug("There is already a registered factory for the provided "
                    + "identifier {}", identifier);
        } else {
            Map<String, WidgetFactory> newFactories = new HashMap<String, WidgetFactory>(
                    widgetFactories.size() + 1);
            newFactories.putAll(widgetFactories);
            newFactories.put(identifier, factory);
            widgetFactories = newFactories;
            LOGGER.debug("Added WidgetFactory with identifier {}", identifier);
        }
    }

    /**
     * Return a registered widget factory.
     * 
     * @param identifier
     *            unique identifier of the provider of the factory, which should usually be the
     *            bundle name
     * @return the registered widget class or null if there is none for the group and name
     */
    public WidgetFactory getWidgetFactory(String identifier) {
        return widgetFactories.get(identifier);
    }

    /**
     * Remove a previously added widget factory from the registry. If there is no such factory the
     * call will be ignored.
     * 
     * @param identifier
     *            unique identifier of the provider of the factory, which was used to register the
     *            factory
     * @see #addWidgetFactory(String, WidgetFactory)
     */
    public synchronized void removeWidgetFactory(String identifier) {
        if (widgetFactories.containsKey(identifier)) {
            Map<String, WidgetFactory> newFactories = new HashMap<String, WidgetFactory>(
                    widgetFactories.size());
            for (Map.Entry<String, WidgetFactory> entry : widgetFactories.entrySet()) {
                if (!entry.getKey().equals(identifier)) {
                    newFactories.put(entry.getKey(), entry.getValue());
                }
            }
            widgetFactories = newFactories;
            LOGGER.debug("Removed WidgetFactory with identifier {}", identifier);
        }
    }
}
