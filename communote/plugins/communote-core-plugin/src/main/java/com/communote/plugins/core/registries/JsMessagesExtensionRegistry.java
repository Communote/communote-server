package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;
import org.osgi.framework.ServiceReference;

import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.i18n.JsMessagesExtension;
import com.communote.server.web.commons.i18n.JsMessagesRegistry;

/**
 * Component to register JsMessagesExtension objects
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
@Instantiate(name = "JsMessagesExtensionRegistry")
public class JsMessagesExtensionRegistry {

    private JsMessagesRegistry messagesRegistry;

    /**
     * @return the lazily initialized registry
     */
    private JsMessagesRegistry getMessagesRegistry() {
        if (messagesRegistry == null) {
            messagesRegistry = WebServiceLocator.instance().getJsMessagesRegistry();
        }
        return messagesRegistry;
    }

    /**
     * Register a JS messages extension
     *
     * @param extension
     *            the extension to register
     * @param reference
     *            reference to the OSGI service
     */
    @Bind(id = "registerJsMessagesExtension", aggregate = true, optional = true)
    public void register(JsMessagesExtension extension, ServiceReference reference) {
        String symbolicName = reference.getBundle().getSymbolicName();
        JsMessagesRegistry registry = getMessagesRegistry();
        for (String category : extension.getJsMessageKeys().keySet()) {
            registry.addMessageKeys(symbolicName, category, extension.getJsMessageKeys().get(
                    category));
        }
    }

    /**
     * Unregister a JS messages extension
     *
     * @param extension
     *            the extension to unregister
     * @param reference
     *            reference to the OSGI service
     */
    @Unbind(id = "registerJsMessagesExtension", optional = true, aggregate = true)
    public void unregister(JsMessagesExtension extension, ServiceReference reference) {
        String symbolicName = reference.getBundle().getSymbolicName();
        getMessagesRegistry().removeMessageKeysOfProvider(symbolicName);
    }
}
