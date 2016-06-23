package com.communote.plugins.mq.provider.activemq.impl;

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
@Instantiate(name = "MessageQueueJsMessages")
public class CustomJsMessages implements JsMessagesExtension {

    /**
     * {@inheritDoc}
     */
    public Map<String, Set<String>> getJsMessageKeys() {
        HashSet<String> messageKeys = new HashSet<String>();
        messageKeys.add("plugins.mq.provider.configuration.button.user.dialog.title");
        messageKeys.add("plugins.mq.provider.configuration.button.user.dialog.question");
        HashMap<String, Set<String>> mapping = new HashMap<String, Set<String>>();
        mapping.put(JsMessagesRegistry.CATEGORY_ADMINISTRATION, messageKeys);
        return mapping;
    }

}
