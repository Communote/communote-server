package com.communote.server.web.fe.widgets.extension;

import com.communote.common.util.Orderable;

/**
 * Marker interface for arbitrary extensions of the server side component of a widget.
 * 
 * @param <E>
 *            the type of the widget extension
 * @param <M>
 *            the type of the management that handles the widget extension type
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface WidgetExtension<E extends WidgetExtension<E, M>, M extends WidgetExtensionManagement<E, M>>
        extends Orderable {

    /**
     * The default order value that should be used when the extension has no specific requirements
     * to its invocation order.
     */
    int DEFAULT_ORDER_VALUE = 1000;

    /**
     * @return the type of the management that handles this extension
     */
    Class<M> getManagementType();
}
