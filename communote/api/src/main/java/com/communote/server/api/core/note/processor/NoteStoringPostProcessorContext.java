package com.communote.server.api.core.note.processor;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds context data which should be shared between different NoteStoringPostProcessor extensions.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteStoringPostProcessorContext {

    private final Map<String, String> properties;
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Creates a new context
     *
     * @param properties
     *            the properties which were added by NoteStoringPostProcessors during synchronous
     *            processing
     */
    public NoteStoringPostProcessorContext(Map<String, String> properties) {
        this.properties = new HashMap<>();
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    /**
     * @return the attributes stored in the context
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * @return the properties which were added by NoteStoringPostProcessors during synchronous
     *         processing
     */
    public Map<String, String> getProperties() {
        return properties;
    }

}
