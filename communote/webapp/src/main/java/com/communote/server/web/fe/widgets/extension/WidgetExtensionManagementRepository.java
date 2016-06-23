package com.communote.server.web.fe.widgets.extension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that provides management capabilities for WidgetExtensionManagement instances and their
 * extensions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class WidgetExtensionManagementRepository {

    private final Map<Class<? extends WidgetExtensionManagement<?, ?>>, WidgetExtensionManagement<?, ?>> managements =
            new ConcurrentHashMap<Class<? extends WidgetExtensionManagement<?, ?>>, WidgetExtensionManagement<?, ?>>();

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(WidgetExtensionManagementRepository.class);

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
     * @return true if extension was added. If there is no matching extension management the
     *         extension won't be added and false is returned.
     * @see WidgetExtension#getManagementType()
     */
    public <E extends WidgetExtension<E, M>, M extends WidgetExtensionManagement<E, M>> boolean addExtension(
            E extension) {
        WidgetExtensionManagement<E, M> management = getExtensionManagement(extension
                .getManagementType());
        if (management != null) {
            management.addExtension(extension);
            return true;
        }
        return false;
    }

    /**
     * Add a widget extension management. If there is already a management of the same type the new
     * one is not added.
     * 
     * @param <E>
     *            the type of the widget extension that is handled by the added management
     * @param <M>
     *            the type of the management to add
     * @param management
     *            the management to register
     * @return true if the management was added, false otherwise
     */
    public synchronized <E extends WidgetExtension<E, M>, M extends WidgetExtensionManagement<E, M>> boolean
            addExtensionManagement(WidgetExtensionManagement<E, M> management) {
        if (!managements.containsKey(management.getClass())) {
            managements.put(
                    (Class<? extends WidgetExtensionManagement<?, ?>>) management.getClass(),
                    management);
            return true;
        }
        return false;
    }

    /**
     * Return a registered extension management that matches the given type.
     * 
     * @param <E>
     *            the type of the widget extension that is handled by the management
     * @param <M>
     *            the type of the management
     * @param extensionManagementType
     *            the type of the widget extension management
     * @return the management or null if none is registered
     */
    @SuppressWarnings("unchecked")
    public <E extends WidgetExtension<E, M>, M extends WidgetExtensionManagement<E, M>> M getExtensionManagement(
            Class<M> extensionManagementType) {
        WidgetExtensionManagement<?, ?> widgetExtensionManagement = managements
                .get(extensionManagementType);
        if (widgetExtensionManagement == null) {
            try {
                // Dynamically load the service. For this it must have an default constructor.
                widgetExtensionManagement = extensionManagementType.newInstance();
                managements.put(extensionManagementType, widgetExtensionManagement);
            } catch (Exception e) {
                LOGGER.error("Error dynamically loading a WidgetExtensionManagement for class {}",
                        extensionManagementType.getName());
                throw new RuntimeException(e);
            }
        }
        return (M) widgetExtensionManagement;
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
    public <E extends WidgetExtension<E, M>, M extends WidgetExtensionManagement<E, M>> void removeExtension(
            E extension) {
        WidgetExtensionManagement<E, M> management = getExtensionManagement(extension
                .getManagementType());
        if (management != null) {
            management.removeExtension(extension);
        }
    }

    /**
     * Remove a previously registered widget extension management
     * 
     * @param <E>
     *            the type of the widget extension
     * @param <M>
     *            the type of the management that handles the widget extension type
     * @param management
     *            the management to remove
     */
    public synchronized <E extends WidgetExtension<E, M>, M extends WidgetExtensionManagement<E, M>> void
            removeExtensionManagement(WidgetExtensionManagement<E, M> management) {
        managements.remove(management.getClass());
    }
}
