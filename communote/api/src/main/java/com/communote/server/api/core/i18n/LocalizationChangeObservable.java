package com.communote.server.api.core.i18n;

/**
 * Interface to be implemented by components that use some kind of localization providing source
 * which can change at runtime.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface LocalizationChangeObservable {

    /**
     * Return the event type that will be fired when the localizations which are used by this
     * component have changed.<br>
     * This event type can be used to register an event listener to be notified about the changes of
     * the localizations which is for instance useful when doing some local caching of localized
     * content.
     *
     *
     * @return the event class
     */
    Class<? extends LocalizationChangedEvent> getChangeNotificationEvent();
}
