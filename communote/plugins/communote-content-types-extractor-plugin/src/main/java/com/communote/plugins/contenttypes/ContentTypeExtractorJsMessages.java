package com.communote.plugins.contenttypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.commons.i18n.JsMessagesExtension;
import com.communote.server.web.commons.i18n.JsMessagesRegistry;

@Component
@Provides
@Instantiate
public class ContentTypeExtractorJsMessages implements JsMessagesExtension {

    @Override
    public Map<String, Set<String>> getJsMessageKeys() {
        HashSet<String> messageKeys = new HashSet<String>();
        messageKeys.add("plugins.contenttypesextractor.embedmedia.action.less");
        messageKeys.add("plugins.contenttypesextractor.embedmedia.action.more.pl");
        messageKeys.add("plugins.contenttypesextractor.embedmedia.action.more.sg");
        messageKeys.add("plugins.contenttypesextractor.embedmedia.iframe.title.YOUTUBE");
        messageKeys.add("plugins.contenttypesextractor.embedmedia.iframe.title.VIMEO");
        messageKeys.add("plugins.contenttypesextractor.embedmedia.iframe.title.MICROSOFT_STREAM");
        
        HashMap<String, Set<String>> mapping = new HashMap<String, Set<String>>();
        mapping.put(JsMessagesRegistry.CATEGORY_PORTAL, messageKeys);
        return mapping;
    }
}
