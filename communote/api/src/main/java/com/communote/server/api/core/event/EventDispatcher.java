package com.communote.server.api.core.event;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The EventDispatcher is the main class for managing events. It provides methods to add event
 * listeners and a method to fire events.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EventDispatcher {

    private final ConcurrentHashMap<Class<? extends Event>, Set<EventListener<? extends Event>>> registeredListeners =
            new ConcurrentHashMap<Class<? extends Event>, Set<EventListener<? extends Event>>>();

    /**
     * Returns all event classes of an Event. This includes all super type and interface entities
     * that are instances of the Event interface.
     * 
     * @param event
     *            the event
     * @return the event classes and interfaces
     */
    private Set<Class<? extends Event>> extractAllEventClassesAndInterfaces(
            Event event) {
        Class<?> eventClass = event.getClass();
        Set<Class<? extends Event>> observedClasses = new HashSet<Class<? extends Event>>();
        Class<Event> eventParentClass = Event.class;
        while (eventClass != null) {
            if (eventParentClass.isAssignableFrom(eventClass)) {
                observedClasses.add((Class<? extends Event>) eventClass);
                extractAllEventInterfaces(eventClass, observedClasses, eventParentClass);
            }
            eventClass = eventClass.getSuperclass();
        }
        return observedClasses;
    }

    /**
     * Recursively extract the interfaces of a class that extend the Event interface and add it to
     * the
     * 
     * @param eventClass
     *            the class whose interfaces are to be returned
     * @param observedClasses
     *            set to store the found interfaces
     * @param eventParentClass
     *            the class object of the Event interface
     */
    private void extractAllEventInterfaces(Class<?> eventClass,
            Set<Class<? extends Event>> observedClasses, Class<Event> eventParentClass) {
        // check interfaces
        Class<?>[] interfaces = eventClass.getInterfaces();
        for (Class<?> intface : interfaces) {
            if (eventParentClass.isAssignableFrom(intface)) {
                observedClasses.add((Class<? extends Event>) intface);
                // recurse interfaces of the interface to get the extended interfaces
                extractAllEventInterfaces(intface, observedClasses, eventParentClass);
            }
        }
    }

    /**
     * Fires an event by dispatching it to all registered listeners. There is no guaranteed order in
     * which the listeners will be called.
     * 
     * @param event
     *            the event
     */
    public void fire(Event event) {
        // get all classes and interfaces that are an Event
        Set<Class<? extends Event>> eventClasses = extractAllEventClassesAndInterfaces(event);
        // check listeners for all Event classes
        for (Class<? extends Event> eventClass : eventClasses) {
            Set<EventListener<? extends Event>> assignedListeners = registeredListeners
                    .get(eventClass);
            if (assignedListeners != null) {
                synchronized (assignedListeners) {
                    for (EventListener listener : assignedListeners) {
                        listener.handle(event);
                    }
                }
            }
        }
    }

    /**
     * Registers an event listener.
     * 
     * @param listener
     *            the listener to register
     * @param <T>
     *            The event.
     */
    public <T extends Event> void register(EventListener<T> listener) {
        Class<T> eventClass = listener.getObservedEvent();
        Set<EventListener<? extends Event>> assignedListeners = new HashSet<EventListener<? extends Event>>();
        Set<EventListener<? extends Event>> previouslyAssignedListeners = registeredListeners
                .putIfAbsent(
                        eventClass, assignedListeners);
        if (previouslyAssignedListeners != null) {
            assignedListeners = previouslyAssignedListeners;
        }
        synchronized (assignedListeners) {
            assignedListeners.add(listener);
        }
    }

    /**
     * Removes a previously registered listener.
     * 
     * @param listener
     *            the listener to remove
     * @param <T>
     *            Type of Event.
     */
    public <T extends Event> void unregister(EventListener<T> listener) {
        Class<? extends Event> eventClass = listener.getObservedEvent();
        Set<EventListener<? extends Event>> assignedListeners = registeredListeners.get(eventClass);
        if (assignedListeners != null) {
            synchronized (assignedListeners) {
                assignedListeners.remove(listener);
            }
        }

    }
}
