package com.communote.server.test.event;

import com.communote.server.api.core.event.Event;
import com.communote.server.api.core.event.EventDispatcher;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class DeactivatableEventDispatcher extends EventDispatcher {

    private boolean active = true;

    /**
     * Activate the dispatching of events
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivate the dispatching of events. Can be re-activated with {@link #activate()}.
     */
    public void deactivate() {
        this.active = false;
    }

    @Override
    public void fire(Event event) {
        if (active) {
            super.fire(event);
        }
    }
}
