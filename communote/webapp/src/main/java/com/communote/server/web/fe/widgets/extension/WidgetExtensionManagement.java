package com.communote.server.web.fe.widgets.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.communote.common.util.DescendingOrderComparator;

/**
 * Class that manages a specific type of widget extensions.
 * 
 * @param <E>
 *            the type of the widget extension
 * @param <M>
 *            the type of the management that handles the widget extension type. This type parameter
 *            ensures that management and extension are tied together.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class WidgetExtensionManagement<E extends WidgetExtension<E, M>,
        M extends WidgetExtensionManagement<E, M>> {

    private List<E> extensions = new ArrayList<E>();

    /**
     * Add a new widget extension to this manager. The extension will be added according to its
     * order value. In case there is already an extension with the order value the new is added
     * after the existing.
     * 
     * @param extension
     *            the extension to add
     */
    public synchronized void addExtension(E extension) {
        ArrayList<E> newExtensions = new ArrayList<E>(extensions);
        newExtensions.add(extension);
        Collections.sort(newExtensions, new DescendingOrderComparator());
        extensions = newExtensions;
    }

    /**
     * @return the registered extensions ordered by their order value (highest first). The returned
     *         collection shouldn't be modified directly, use the add and remove methods instead.
     */
    protected List<E> getExtensions() {
        return extensions;
    }

    /**
     * Remove a previously registered extension.
     * 
     * @param extension
     *            the extension to remove
     */
    public synchronized void removeExtension(E extension) {
        ArrayList<E> newExtensions = new ArrayList<E>(extensions);
        newExtensions.remove(extension);
        extensions = newExtensions;
    }
}
