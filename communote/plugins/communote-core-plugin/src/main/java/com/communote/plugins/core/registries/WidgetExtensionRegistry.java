package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.fe.widgets.extension.WidgetExtension;
import com.communote.server.web.fe.widgets.extension.WidgetExtensionManagement;
import com.communote.server.web.fe.widgets.extension.WidgetExtensionManagementRepository;


/**
 * Registry to automatically add widget extensions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate
public class WidgetExtensionRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetExtensionRegistry.class);
    private WidgetExtensionManagementRepository extensionRepository;

    /**
     * @return the lazily initialized repository
     */
    private WidgetExtensionManagementRepository getExtensionRepository() {
        if (extensionRepository == null) {
            extensionRepository = WebServiceLocator.instance()
                    .getWidgetExtensionManagementRepository();
        }
        return extensionRepository;
    }

    /**
     * Add a widget extension to a registered management. The management to use is defined by the
     * extension.
     * 
     * @param <E>
     *            the type of the widget extension
     * @param <M>
     *            the type of the management that handles the widget extension type
     * @param extension
     *            the extension to add
     */
    @Bind(id = "registerWidgetExtension", optional = true, aggregate = true)
    public <E extends WidgetExtension<E, M>, M extends WidgetExtensionManagement<E, M>> void registerExtension(
            E extension) {
        if (!getExtensionRepository().addExtension(extension)) {
            LOGGER.info("Adding widget extension {} failed", extension.getClass().getName());
        } else {
            LOGGER.debug("Added widget extension {}", extension.getClass().getName());
        }
    }

    /**
     * Remove a previously registered extension from its management.
     * 
     * @param <E>
     *            the type of the widget extension
     * @param <M>
     *            the type of the management that handles the widget extension type
     * @param extension
     *            the extension to remove
     */
    @Unbind(id = "registerWidgetExtension", optional = true, aggregate = true)
    public <E extends WidgetExtension<E, M>, M extends WidgetExtensionManagement<E, M>> void removeExtension(
            E extension) {
        getExtensionRepository().removeExtension(extension);
        LOGGER.debug("Removed widget extension {}", extension.getClass().getName());
    }
}
