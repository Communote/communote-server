package com.communote.plugins.embed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.commons.i18n.JsMessagesExtension;
import com.communote.server.web.commons.i18n.JsMessagesRegistry;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Provides
@Instantiate
public class EmbedJsMessages implements JsMessagesExtension {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Set<String>> getJsMessageKeys() {
        HashSet<String> messageKeys = new HashSet<String>();
        messageKeys.add("plugins.embed.snippet.menu.title");
        messageKeys.add("plugins.embed.snippet.popup.code.title");
        messageKeys.add("plugins.embed.snippet.popup.explanation");
        messageKeys.add("plugins.embed.snippet.popup.height");
        messageKeys.add("plugins.embed.snippet.popup.select.button");
        messageKeys.add("plugins.embed.snippet.popup.title");
        messageKeys.add("plugins.embed.snippet.popup.width");

        HashMap<String, Set<String>> mapping = new HashMap<String, Set<String>>();
        mapping.put(JsMessagesRegistry.CATEGORY_PORTAL, messageKeys);
        return mapping;
    }

}
