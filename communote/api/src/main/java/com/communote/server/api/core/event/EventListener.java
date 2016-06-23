package com.communote.server.api.core.event;

/**
 * The EventListener provides the logic to handle an event.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the event type the listener observes and handles
 */
public interface EventListener<T extends Event> {

    /**
     * Defines the types of events about which this listener will be informed.
     * 
     * @return the class of the observed event
     */
    Class<T> getObservedEvent();

    /**
     * Is called by the {@link EventDispatcher} when the observed event was fired.
     * 
     * @param event
     *            the event that was fired
     */
    void handle(T event);
}
