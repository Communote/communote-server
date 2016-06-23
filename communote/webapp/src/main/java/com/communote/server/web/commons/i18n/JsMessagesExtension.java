package com.communote.server.web.commons.i18n;

import java.util.Map;
import java.util.Set;

/**
 * Extension to add additional message keys to categories managed by the {@link JsMessagesRegistry}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface JsMessagesExtension {

    /**
     * Returns the message keys to add to the named category. When creating a Javascript object with
     * message key to localized message mappings for that category the returned message keys will be
     * included.
     * 
     * @return a mapping of category name to message key collection. If the category name is not yet
     *         known a new category will be created, otherwise the existing one will be extended.
     */
    public Map<String, Set<String>> getJsMessageKeys();

}
