package com.communote.server.core.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.property.PropertyEvent;
import com.communote.server.api.core.property.PropertyType;

/**
 * Test Event Listener for {@link PropertyEvent}s. It just takes the incoming events and stores them
 * in an internal list.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TestPropertyEventListener implements EventListener<PropertyEvent> {

    private final List<PropertyEvent> receivedEvents = Collections
            .synchronizedList(new ArrayList<PropertyEvent>());

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<PropertyEvent> getObservedEvent() {
        return PropertyEvent.class;
    }

    /**
     * Searches the received events and returns the first property matching.
     *
     * Remark: In case first an property is created and then updated two events are added and this
     * method will always return the first one. To check the second one do a clear on the received
     * events before updating the property.
     *
     * @param type
     *            the property type the event must have
     * @param keyGroup
     *            the key group the event must have
     * @param key
     *            the key the event must have
     * @return the event found or null
     */
    public PropertyEvent getProperty(PropertyType type, String keyGroup, String key) {
        for (PropertyEvent event : receivedEvents) {
            if (type.equals(event.getPropertyType()) && keyGroup.equals(event.getKeyGroup())
                    && key.equals(event.getKey())) {
                return event;
            }
        }
        return null;
    }

    /**
     *
     * @return all received events so far
     */
    public List<PropertyEvent> getReceivedEvents() {
        return receivedEvents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(PropertyEvent event) {
        this.receivedEvents.add(event);
    }

}