package com.communote.server.api.core.note.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds context data which should be shared between different NoteStoringPostProcessor extensions.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteStoringPostProcessorContext {

    private final Set<Long> userIdsToSkip = new HashSet<Long>();
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Creates a new context
     *
     * @param userIdsToSkip
     *            the IDs of users to be skipped
     */
    public NoteStoringPostProcessorContext(Long[] userIdsToSkip) {
        if (userIdsToSkip != null) {
            for (int i = 0; i < userIdsToSkip.length; i++) {
                this.userIdsToSkip.add(userIdsToSkip[i]);
            }
        }
    }

    /**
     * Creates a new context
     *
     * @param userIdsToSkip
     *            the IDs of users to be skipped
     */
    public NoteStoringPostProcessorContext(Set<Long> userIdsToSkip) {
        if (userIdsToSkip != null) {
            this.userIdsToSkip.addAll(userIdsToSkip);
        }
    }

    /**
     * @return the attributes stored in the context
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * @return the IDs of the users that should not be notified about notes.
     */
    // TODO this is not generic enough. This should just be an attribute.
    public Set<Long> getUserIdsToSkip() {
        return userIdsToSkip;
    }

}
